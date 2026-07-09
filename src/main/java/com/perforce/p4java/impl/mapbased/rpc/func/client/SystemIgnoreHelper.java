/*
 * Copyright 2024 Perforce Software Inc., All Rights Reserved.
 */
package com.perforce.p4java.impl.mapbased.rpc.func.client;

import com.perforce.p4java.Log;
import com.perforce.p4java.client.IClient;
import com.perforce.p4java.env.PerforceEnvironment;
import com.perforce.p4java.impl.mapbased.rpc.RpcServer;
import com.perforce.p4java.impl.mapbased.rpc.connection.RpcConnection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Client-side guard, refuses to write (on sync) or send (on submit) files whose
 * local path is protected, via two layers:
 *
 * <ol>
 *   <li><b>Always-on</b> — {@code P4TRUST} and {@code P4TICKETS} are blocked unconditionally,
 *       even when the adjustable map is cleared with {@code $none}.</li>
 *   <li><b>Adjustable map</b> — the per-server {@code P4_<PORT>_SYSTEMIGNORE} variable, a
 *       {@code ;}-separated list. When unset/empty the built-in defaults apply ({@code $defaults}):
 *       {@code .p4config}, {@code p4config.txt}, {@code .p4enviro}, {@code p4enviro.txt}, and the
 *       resolved {@code P4CONFIG}/{@code P4ENVIRO} files.</li>
 * </ol>
 *
 * <p>Each entry is a literal path/filename or a token ({@code $defaults}, {@code $none},
 * {@code $P4CONFIG}, {@code $P4ENVIRO}, {@code $home}/{@code ~} — also a leading prefix), and may
 * be prefixed with {@code !} or {@code -} to unblock; entries evaluate in order, last match wins.
 *
 * <p>The value is read <b>only</b> from the environment (with a JVM system-property fallback for
 * tests), never from a {@code P4CONFIG} file — a planted {@code .p4config} must not influence the
 * map. The port-var name uppercases P4PORT and replaces every non-alphanumeric character with
 * {@code _}, e.g. {@code ssl:perforce.example.com:1666} → {@code P4_SSL_PERFORCE_EXAMPLE_COM_1666_SYSTEMIGNORE}.
 */
public final class SystemIgnoreHelper {

    /** Name of the P4CONFIG environment variable (holds the config-file name). */
    public static final String P4CONFIG_ENV_VAR = "P4CONFIG";

    /** Name of the P4ENVIRO environment variable (holds the enviro-file path). */
    public static final String P4ENVIRO_ENV_VAR = "P4ENVIRO";

    /** Prefix for the per-server system-ignore environment variable. */
    public static final String SYSTEMIGNORE_ENV_PREFIX = "P4_";

    /** Suffix for the per-server system-ignore environment variable. */
    public static final String SYSTEMIGNORE_ENV_SUFFIX = "_SYSTEMIGNORE";

    /** Token: expand to the built-in default block list. */
    private static final String TOKEN_DEFAULTS = "$defaults";
    /** Token: clear the adjustable map. */
    private static final String TOKEN_NONE = "$none";
    /** Token: the resolved P4CONFIG file. */
    private static final String TOKEN_P4CONFIG = "$P4CONFIG";
    /** Token: the resolved P4ENVIRO file. */
    private static final String TOKEN_P4ENVIRO = "$P4ENVIRO";
    /** Token: the user's home directory. */
    private static final String TOKEN_HOME = "$home";

    /** The built-in default block list, expanded by {@code $defaults} / when the env is unset. */
    private static final String[] DEFAULT_IGNORE_NAMES = {
            ".p4config", "p4config.txt", ".p4enviro", "p4enviro.txt"
    };

    /** Paths compare case-insensitively on Windows, case-sensitively elsewhere. */
    private static final boolean CASE_INSENSITIVE =
            System.getProperty("os.name", "").toLowerCase().contains("win");

    private SystemIgnoreHelper() { /* utility class - not instantiable */ }

    /**
     * Returns true if the given local path is protected and must not be written or sent.
     *
     * @param path          absolute local path to check
     * @param server        connected RPC server
     * @param rpcConnection active RPC connection; may be null to skip the per-server
     *                      environment-variable lookup (the built-in defaults still apply)
     * @return true if the write/send should be blocked
     */
    public static boolean isProtected(
            @Nonnull String path,
            @Nonnull RpcServer server,
            @Nullable RpcConnection rpcConnection) {
        return isProtected(path, server, rpcConnection, null);
    }

    /**
     * Core guard logic with the adjustable system-ignore value supplied
     * directly, so it can be unit-tested without mutating the process
     * environment. Package-private deliberately: the public 3-arg method
     * delegates here, letting buildIgnoreRules read the environment normally
     * while tests may pass an explicit value.
     *
     * @param path          local path to check
     * @param server        RPC server; may be {@code null} when only the
     *                      explicit systemIgnoreValue list is exercised
     *                      (skips the P4TRUST/P4TICKETS always-on checks)
     * @param rpcConnection active RPC connection; may be {@code null}
     * @param systemIgnoreValue explicit P4_<port>_SYSTEMIGNORE value to match
     *                      against; {@code null} means "use normal resolution"
     * @return {@code true} if the write should be blocked
     */
    static boolean isProtected(
            @Nonnull String path,
            @Nullable RpcServer server,
            @Nullable RpcConnection rpcConnection,
            @Nullable String systemIgnoreValue) {

        String candidate = canonicalize(path);
        if (candidate == null) {
            return false;
        }

        // Always-on: P4TRUST / P4TICKETS, regardless of the adjustable map (including $none).
        if (server != null
                && (samePath(candidate, server.getTrustFilePath())
                        || samePath(candidate, server.getTicketsFilePath()))) {
            return true;
        }

        // Adjustable system-ignore map.
        String baseName = baseName(candidate);
        boolean blocked = false;
        List<IgnoreRule> rules = (systemIgnoreValue != null)
                ? buildIgnoreRules(null, systemIgnoreValue)
                : buildIgnoreRules(server, readSystemIgnoreValue(rpcConnection));
        for (IgnoreRule rule : rules) {
            boolean hit = rule.byName
                    ? equalsPath(baseName, rule.value)
                    : equalsPath(candidate, rule.value);
            if (hit) {
                blocked = !rule.exclude; // last match wins
            }
        }
        return blocked;
    }

    /**
     * Builds the ordered list of system-ignore rules from the raw
     * {@code P4_<PORT>_SYSTEMIGNORE} value (falling back to the built-in defaults
     * when null or empty). A {@code null} server skips P4CONFIG/P4ENVIRO resolution.
     */
    private static List<IgnoreRule> buildIgnoreRules(
            @Nullable RpcServer server, @Nullable String raw) {

        List<IgnoreRule> rules = new ArrayList<>();
        String[] entries = (raw == null || raw.trim().isEmpty())
                ? new String[]{TOKEN_DEFAULTS}
                : raw.split(";");

        for (String rawEntry : entries) {
            String entry = rawEntry.trim();
            if (entry.isEmpty()) {
                continue;
            }

            boolean exclude = false;
            if (entry.startsWith("!") || entry.startsWith("-")) {
                exclude = true;
                entry = entry.substring(1).trim();
                if (entry.isEmpty()) {
                    continue;
                }
            }

            if (entry.equalsIgnoreCase(TOKEN_NONE)) {
                rules.clear();
            } else if (entry.equalsIgnoreCase(TOKEN_DEFAULTS)) {
                addDefaults(rules, server, exclude);
            } else if (entry.equalsIgnoreCase(TOKEN_P4CONFIG)) {
                addPath(rules, resolveP4ConfigFile(server), exclude);
            } else if (entry.equalsIgnoreCase(TOKEN_P4ENVIRO)) {
                addPath(rules, resolveP4EnviroFile(), exclude);
            } else if (entry.equals("~") || entry.equalsIgnoreCase(TOKEN_HOME)) {
                addPath(rules, homeDir(), exclude);
            } else {
                String expanded = expandLeadingHome(entry);
                if (looksLikePath(expanded)) {
                    addPath(rules, expanded, exclude);
                } else {
                    rules.add(new IgnoreRule(true, expanded, exclude));
                }
            }
        }
        return rules;
    }

    private static void addDefaults(List<IgnoreRule> rules, RpcServer server, boolean exclude) {
        for (String name : DEFAULT_IGNORE_NAMES) {
            rules.add(new IgnoreRule(true, name, exclude));
        }
        addPath(rules, resolveP4ConfigFile(server), exclude);
        addPath(rules, resolveP4EnviroFile(), exclude);
    }

    private static void addPath(List<IgnoreRule> rules, String path, boolean exclude) {
        String canonical = canonicalize(path);
        if (canonical != null) {
            rules.add(new IgnoreRule(false, canonical, exclude));
        }
    }

    /**
     * Reads the {@code P4_<PORT>_SYSTEMIGNORE} value from the process environment, falling
     * back to a JVM system property of the same name. Returns null when there is no active
     * connection to derive the variable name from.
     */
    @Nullable
    private static String readSystemIgnoreValue(@Nullable RpcConnection rpcConnection) {
        if (rpcConnection == null) {
            return null;
        }
        String name = buildSystemIgnoreEnvVarName(rpcConnection);
        String value = System.getenv(name);
        if (value == null) {
            value = System.getProperty(name);
        }
        return value;
    }

    /**
     * Returns the {@code P4_<PORT>_SYSTEMIGNORE} environment-variable name for the given connection.
     * For example, localhost:1666 becomes P4_LOCALHOST_1666_SYSTEMIGNORE.
     */
    public static String buildSystemIgnoreEnvVarName(@Nonnull RpcConnection rpcConnection) {
        String portStr = (rpcConnection.isSecure() ? "ssl:" : "")
                + rpcConnection.getHostName()
                + ":"
                + rpcConnection.getHostPort();
        return SYSTEMIGNORE_ENV_PREFIX + sanitizePortForEnvVar(portStr) + SYSTEMIGNORE_ENV_SUFFIX;
    }

    /**
     * Changes the port string to upper case and replaces every character that is not
     * a letter or digit with an underscore. For example ssl:perforce.example.com:1666
     * becomes SSL_PERFORCE_EXAMPLE_COM_1666.
     */
    public static String sanitizePortForEnvVar(@Nonnull String portStr) {
        return portStr.toUpperCase().replaceAll("[^A-Z0-9]", "_");
    }

    /**
     * Walks up from the client root to find the P4CONFIG file.
     * Returns null if the P4CONFIG environment variable is not set, the client root is
     * unavailable, or no matching file is found in the directory tree.
     */
    @Nullable
    public static String resolveP4ConfigFile(@Nonnull RpcServer server) {
        String p4ConfigName = System.getenv(P4CONFIG_ENV_VAR);
        if (p4ConfigName == null || p4ConfigName.isEmpty()) {
            return null;
        }

        try {
            IClient client = server.getCurrentClient();
            if (client == null) {
                return null;
            }
            String clientRoot = client.getRoot();
            if (clientRoot == null || clientRoot.isEmpty()) {
                return null;
            }

            for (File dir = new File(clientRoot); dir != null; dir = dir.getParentFile()) {
                File configFile = new File(dir, p4ConfigName);
                if (configFile.isFile()) {
                    return configFile.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            Log.warn("SystemIgnoreHelper: unable to resolve P4CONFIG file path: " + e.getMessage());
        }
        return null;
    }

    /**
     * Resolves the P4ENVIRO file path: the value of the P4ENVIRO environment variable, or the
     * platform default ({@code ~/.p4enviro}) when unset. Never returns null.
     */
    @Nonnull
    public static String resolveP4EnviroFile() {
        return PerforceEnvironment.getP4Enviro();
    }

    // ---- helpers ------------------------------------------------------------

    /** Expands a leading {@code ~} or {@code $home} in an entry to the home directory. */
    private static String expandLeadingHome(String entry) {
        if (entry.equals("~") || entry.startsWith("~/") || entry.startsWith("~\\")) {
            return homeDir() + entry.substring(1);
        }
        if (entry.regionMatches(true, 0, TOKEN_HOME, 0, TOKEN_HOME.length())) {
            return homeDir() + entry.substring(TOKEN_HOME.length());
        }
        return entry;
    }

    private static boolean looksLikePath(String entry) {
        return entry.indexOf('/') >= 0 || entry.indexOf('\\') >= 0;
    }

    private static String homeDir() {
        return System.getProperty("user.home");
    }

    private static String baseName(String normalizedPath) {
        int slash = normalizedPath.lastIndexOf('/');
        return slash >= 0 ? normalizedPath.substring(slash + 1) : normalizedPath;
    }

    /** True when both inputs canonicalise to the same local path. */
    private static boolean samePath(String canonicalCandidate, @Nullable String other) {
        String canonicalOther = canonicalize(other);
        return canonicalOther != null && equalsPath(canonicalCandidate, canonicalOther);
    }

    private static boolean equalsPath(String a, String b) {
        return CASE_INSENSITIVE ? a.equalsIgnoreCase(b) : a.equals(b);
    }

    /**
     * Returns the canonical absolute path with forward-slash separators, or null if the input
     * is null or empty. Falls back to path normalisation if the file does not yet exist.
     */
    @Nullable
    private static String canonicalize(@Nullable String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        String expanded = expandLeadingHome(path);
        try {
            return normalizeSeparators(new File(expanded).getCanonicalPath());
        } catch (Exception e) {
            // File may not exist yet; normalize without resolving symlinks.
            try {
                return normalizeSeparators(Paths.get(expanded).normalize().toAbsolutePath().toString());
            } catch (Exception ex) {
                // The path cannot be parsed on this platform (e.g. illegal characters such
                // as an embedded ':' in a malformed Windows path). Treat it as
                // not-canonicalizable so the caller reports "not protected" instead of
                // letting an InvalidPathException propagate and break the file operation.
                return null;
            }
        }
    }

    private static String normalizeSeparators(String path) {
        return path.replace('\\', '/');
    }

    /** One system-ignore rule: a bare filename or a literal path, optionally an exclusion. */
    private static final class IgnoreRule {
        final boolean byName;  // true: match candidate's base name; false: match the full path
        final String value;    // a bare filename, or a canonical path
        final boolean exclude; // true: this rule un-blocks a path an earlier rule blocked

        IgnoreRule(boolean byName, String value, boolean exclude) {
            this.byName = byName;
            this.value = value;
            this.exclude = exclude;
        }
    }
}
