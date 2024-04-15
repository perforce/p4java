/*
 * Copyright 2008 Perforce Software Inc., All Rights Reserved.
 */

package com.perforce.p4java.server;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IUserGroup;
import com.perforce.p4java.core.file.DiffType;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConfigException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.impl.mapbased.server.cmd.IListDelegator;
import com.perforce.p4java.server.callback.IBrowserCallback;
import com.perforce.p4java.server.callback.ICommandCallback;
import com.perforce.p4java.server.callback.IParallelCallback;
import com.perforce.p4java.server.callback.IProgressCallback;
import com.perforce.p4java.server.callback.ISSOCallback;
import com.perforce.p4java.server.callback.IStreamingCallback;
import com.perforce.p4java.server.delegator.IAttributeDelegator;
import com.perforce.p4java.server.delegator.IBranchDelegator;
import com.perforce.p4java.server.delegator.IBranchesDelegator;
import com.perforce.p4java.server.delegator.IChangeDelegator;
import com.perforce.p4java.server.delegator.IChangesDelegator;
import com.perforce.p4java.server.delegator.IClientDelegator;
import com.perforce.p4java.server.delegator.IClientsDelegator;
import com.perforce.p4java.server.delegator.ICommitDelegator;
import com.perforce.p4java.server.delegator.IConfigureDelegator;
import com.perforce.p4java.server.delegator.ICounterDelegator;
import com.perforce.p4java.server.delegator.ICountersDelegator;
import com.perforce.p4java.server.delegator.IDBSchemaDelegator;
import com.perforce.p4java.server.delegator.IDepotDelegator;
import com.perforce.p4java.server.delegator.IDepotsDelegator;
import com.perforce.p4java.server.delegator.IDescribeDelegator;
import com.perforce.p4java.server.delegator.IDiff2Delegator;
import com.perforce.p4java.server.delegator.IDirsDelegator;
import com.perforce.p4java.server.delegator.IDiskspaceDelegator;
import com.perforce.p4java.server.delegator.IDuplicateDelegator;
import com.perforce.p4java.server.delegator.IExportDelegator;
import com.perforce.p4java.server.delegator.IExtensionDelegator;
import com.perforce.p4java.server.delegator.IFileAnnotateDelegator;
import com.perforce.p4java.server.delegator.IFileLogDelegator;
import com.perforce.p4java.server.delegator.IFilesDelegator;
import com.perforce.p4java.server.delegator.IFixDelegator;
import com.perforce.p4java.server.delegator.IFixesDelegator;
import com.perforce.p4java.server.delegator.IFstatDelegator;
import com.perforce.p4java.server.delegator.IGraphCommitLogDelegator;
import com.perforce.p4java.server.delegator.IGraphListTreeDelegator;
import com.perforce.p4java.server.delegator.IGraphReceivePackDelegator;
import com.perforce.p4java.server.delegator.IGraphRevListDelegator;
import com.perforce.p4java.server.delegator.IGraphShowRefDelegator;
import com.perforce.p4java.server.delegator.IGrepDelegator;
import com.perforce.p4java.server.delegator.IGroupDelegator;
import com.perforce.p4java.server.delegator.IGroupsDelegator;
import com.perforce.p4java.server.delegator.IInfoDelegator;
import com.perforce.p4java.server.delegator.IIntegratedDelegator;
import com.perforce.p4java.server.delegator.IInterchangesDelegator;
import com.perforce.p4java.server.delegator.IJobDelegator;
import com.perforce.p4java.server.delegator.IJobSpecDelegator;
import com.perforce.p4java.server.delegator.IJobsDelegator;
import com.perforce.p4java.server.delegator.IJournalWaitDelegator;
import com.perforce.p4java.server.delegator.IKeyDelegator;
import com.perforce.p4java.server.delegator.IKeysDelegator;
import com.perforce.p4java.server.delegator.ILabelDelegator;
import com.perforce.p4java.server.delegator.ILabelsDelegator;
import com.perforce.p4java.server.delegator.ILicenseDelegator;
import com.perforce.p4java.server.delegator.ILogTailDelegator;
import com.perforce.p4java.server.delegator.ILogin2Delegator;
import com.perforce.p4java.server.delegator.ILoginDelegator;
import com.perforce.p4java.server.delegator.ILogoutDelegator;
import com.perforce.p4java.server.delegator.IMonitorDelegator;
import com.perforce.p4java.server.delegator.IMoveDelegator;
import com.perforce.p4java.server.delegator.IObliterateDelegator;
import com.perforce.p4java.server.delegator.IOpenedDelegator;
import com.perforce.p4java.server.delegator.IPasswdDelegator;
import com.perforce.p4java.server.delegator.IPrintDelegator;
import com.perforce.p4java.server.delegator.IPropertyDelegator;
import com.perforce.p4java.server.delegator.IProtectDelegator;
import com.perforce.p4java.server.delegator.IProtectsDelegator;
import com.perforce.p4java.server.delegator.IReloadDelegator;
import com.perforce.p4java.server.delegator.IRenameClientDelegator;
import com.perforce.p4java.server.delegator.IRenameUserDelegator;
import com.perforce.p4java.server.delegator.IReposDelegator;
import com.perforce.p4java.server.delegator.IReviewDelegator;
import com.perforce.p4java.server.delegator.IReviewsDelegator;
import com.perforce.p4java.server.delegator.ISearchDelegator;
import com.perforce.p4java.server.delegator.ISizesDelegator;
import com.perforce.p4java.server.delegator.ISpecDelegator;
import com.perforce.p4java.server.delegator.IStatDelegator;
import com.perforce.p4java.server.delegator.IStreamDelegator;
import com.perforce.p4java.server.delegator.IStreamlogDelegator;
import com.perforce.p4java.server.delegator.IStreamsDelegator;
import com.perforce.p4java.server.delegator.ITagDelegator;
import com.perforce.p4java.server.delegator.ITriggersDelegator;
import com.perforce.p4java.server.delegator.IUnloadDelegator;
import com.perforce.p4java.server.delegator.IUserDelegator;
import com.perforce.p4java.server.delegator.IUsersDelegator;
import com.perforce.p4java.server.delegator.IVerifyDelegator;

import java.io.InputStream;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Provides an interface onto a Perforce SCM server.
 * <p>
 * This is the main interface for Perforce services that are typically Perforce
 * client workspace-independent, or that affect entire Perforce depots or
 * servers. Some of these services are also available through various client,
 * job, changelist, etc., interfaces methods, but in general, most Perforce
 * services are always available through methods on this interface as well.
 * <p>
 * IServer interfaces for specific Perforce servers are issued by the P4Javs
 * server factory class, ServerFactory; the factory can return interfaces that
 * use a small variety of communication protocols to access the Perforce server.
 *
 * @see com.perforce.p4java.server.ServerFactory
 */

public interface IServer extends IHelixCommandExecutor, IAttributeDelegator, IBranchDelegator, IBranchesDelegator, IChangeDelegator, IChangesDelegator, IClientDelegator, IClientsDelegator, IConfigureDelegator, ICounterDelegator, ICountersDelegator, IDBSchemaDelegator, IDepotDelegator, IDepotsDelegator, IReposDelegator, IDescribeDelegator, IDiff2Delegator, IDirsDelegator, IDiskspaceDelegator, IDuplicateDelegator, IExportDelegator, IFileAnnotateDelegator, IFileLogDelegator, IFilesDelegator, IFixDelegator, IFixesDelegator, IFstatDelegator, IGrepDelegator, IGroupDelegator, IGroupsDelegator, IInfoDelegator, IIntegratedDelegator, IInterchangesDelegator, IStatDelegator, IJobDelegator, IJobsDelegator, IJobSpecDelegator, IJournalWaitDelegator, IKeyDelegator, IKeysDelegator, ILabelDelegator, ILabelsDelegator, ILoginDelegator, ILogoutDelegator, ILogTailDelegator, IMonitorDelegator, IMoveDelegator, IObliterateDelegator, IOpenedDelegator, IPasswdDelegator, IPrintDelegator, IPropertyDelegator, IProtectDelegator, IProtectsDelegator, IReloadDelegator, IRenameUserDelegator, IReviewDelegator, IReviewsDelegator, ISearchDelegator, ISizesDelegator, IStreamDelegator, IStreamsDelegator, ITagDelegator, ITriggersDelegator, IUnloadDelegator, IUserDelegator, IUsersDelegator, IVerifyDelegator, IGraphListTreeDelegator, ICommitDelegator, IGraphRevListDelegator, IGraphCommitLogDelegator, IGraphReceivePackDelegator, IListDelegator, IGraphShowRefDelegator, ILogin2Delegator, ISpecDelegator, ILicenseDelegator, IExtensionDelegator, IStreamlogDelegator, IRenameClientDelegator {

	String ATTRIBUTE_STREAM_MAP_KEY = "attributeInstream";

	/**
	 * Property key for overriding the default tagged/non-tagged behavior of a
	 * command. This is a per-command property, set on a command's "inMap".
	 */
	String IN_MAP_USE_TAGS_KEY = "useTags";

	/**
	 * Try to get the Perforce server version. This is likely to be the first
	 * time
	 * actual connectivity is tested for the server...
	 * Since this is called before we know much about the state or type of the
	 * Perforce server, we do virtually no real error checking or recovery -- we
	 * either get a suitable response and dig out the server version, or we just
	 * leave things alone.
	 * <p>
	 * NOTE: has the side effect of setting the server impl's serverVersion
	 * field.
	 *
	 * @return version
	 * @throws ConnectionException on error
	 */
	int getServerVersion() throws ConnectionException;

	/**
	 * Connect to the Perforce server associated with this server object.
	 * <p>
	 * This method's detailed semantics depend on the underlying transport
	 * implementation, but in general, it's intended to be called before any
	 * attempt is made to issue a command to the associated Perforce server.
	 * It's also intended to be called after any (intentional or accidental)
	 * disconnect.
	 * <p>
	 * Note that certain implementations may try to request a client, etc., on
	 * connection (in response to property values passed in through the URL,
	 * etc.), which may cause a RequestException to be generated.
	 *
	 * @throws ConnectionException if the Perforce server is unreachable or is not connected.
	 * @throws AccessException     if the Perforce server denies access to the caller
	 * @throws RequestException    if the Perforce server encounters an error during its
	 *                             processing of the request
	 * @throws ConfigException     if local I/O exception occurs
	 */
	void connect() throws ConnectionException, AccessException, RequestException, ConfigException;

	/**
	 * Create a new Perforce user group on the Perforce server.
	 *
	 * @param group non-null IUserGroup to be created.
	 * @return possibly-null status message string as returned from the server
	 * @throws ConnectionException if the Perforce server is unreachable or is not connected.
	 * @throws RequestException    if the Perforce server encounters an error during its
	 *                             processing of the request.
	 * @throws AccessException     if the Perforce server denies access to the caller.
	 */
	String createUserGroup(IUserGroup group) throws ConnectionException, RequestException, AccessException;

	/**
	 * Delete a Perforce user group from the Perforce server.
	 *
	 * @param group non-null group to be deleted.
	 * @return possibly-null status message string as returned from the server
	 * @throws ConnectionException if the Perforce server is unreachable or is not connected.
	 * @throws RequestException    if the Perforce server encounters an error during its
	 *                             processing of the request.
	 * @throws AccessException     if the Perforce server denies access to the caller.
	 */
	String deleteUserGroup(IUserGroup group) throws ConnectionException, RequestException, AccessException;

	/**
	 * Disconnect from this Perforce server. Does not affect the current
	 * IServer's current user, password, or client settings, but if you later
	 * reconnect to the same Perforce server, you may also need to re-login.
	 * <p>
	 * This command should be run at the point at which this server is not going
	 * to be used any more, and attempts to disconnect from the associated
	 * server. "Disconnect" here has different meanings according to the
	 * underlying transport mechanism, but in practice it will mean that
	 * attempting to use this server object to issue Perforce commands will
	 * fail, usually with a ConnectionException exception.
	 *
	 * @throws ConnectionException on error
	 * @throws AccessException     on error
	 */
	void disconnect() throws ConnectionException, AccessException;

	/**
	 * Issue an arbitrary P4Java command to the Perforce server and return the
	 * results as a map.
	 * <p>
	 * This method is intended for low-level commands in the spirit and format
	 * of the p4 command line interpreter, and offers a simple way to issue
	 * commands to the associated Perforce server without the overhead of the
	 * more abstract Java interfaces and methods.
	 * <p>
	 * No guidance is given here on the format of the returned map; however, it
	 * produces the same output as the p4 command line interpreter in -G (Python
	 * map) mode.
	 * <p>
	 * Note that this method does not allow you to set "usage" options for the
	 * command; these may be added later. Note also that although option
	 * arguments passed to this method must be in a form recognized by the p4
	 * command line interpreter, that does <i>not</i> mean the method is being
	 * implemented by the interpreter -- the actual implementation depends on
	 * the options used to get the server object in the first place from the
	 * server factory.
	 *
	 * @param cmdName  the command to be issued; must be non-null, and correspond to
	 *                 a Perforce command recognized by P4Java and defined in
	 *                 CmdSpec.
	 * @param cmdArgs  the array of command arguments (options and file arguments,
	 *                 etc.) to be sent to the Perforce server. These must be in the
	 *                 form used by the corresponding p4 command line interpreter.
	 *                 Ignored if null.
	 * @param inString an optional string to be sent to the server as standard input
	 *                 unchanged (this must be in the format expected by the server,
	 *                 typically as required when using the "-i" flag to the p4
	 *                 command line app for the same command). You must remember to
	 *                 issue the relevant command-specific option to enable this if
	 *                 needed.
	 * @return a non-null Java Map of results; these results are as returned
	 * from issuing the command using the -G option with the p4 command
	 * line interpreter.
	 * @throws P4JavaException if an error occurs processing this method and its parameters
	 * @since 2011.1
	 */
	Map<String, Object>[] execInputStringMapCmd(String cmdName, String[] cmdArgs, String inString) throws P4JavaException;

	/**
	 * Issue a streaming map command to the Perforce server, using an optional
	 * string for any input expected by the server (such as label or job specs,
	 * etc.).
	 * <p>
	 * Streaming commands allow users to get each result from a suitably-issued
	 * command as it comes in from the server, rather than waiting for the
	 * entire command method to complete (and getting the results back as a
	 * completed List or Map or whatever).
	 * <p>
	 * The results are sent to the user using the IStreamingCallback
	 * handleResult method; see the IStreamingCallback Javadoc for details. The
	 * payload passed to handleResult is usually the raw map gathered together
	 * deep in the RPC protocol layer, and the user is assumed to have the
	 * knowledge and technology to be able to parse it and use it suitably in
	 * much the same way as a user unpacks or processes the results from the
	 * other low-level exec methods like execMapCommand.
	 * <p>
	 * NOTE: 'streaming' here has nothing at all to do with Perforce 'streams',
	 * which are (or will be) implemented elsewhere.
	 *
	 * @param cmdName  the command to be issued; must be non-null, and correspond to
	 *                 a Perforce command recognized by P4Java and defined in
	 *                 CmdSpec.
	 * @param cmdArgs  the array of command arguments (options and file arguments,
	 *                 etc.) to be sent to the Perforce server. These must be in the
	 *                 form used by the corresponding p4 command line interpreter.
	 *                 Ignored if null.
	 * @param inString an optional string to be sent to the server as standard input
	 *                 unchanged (this must be in the format expected by the server,
	 *                 typically as required when using the "-i" flag to the p4
	 *                 command line app for the same command). You must remember to
	 *                 issue the relevant command-specific option to enable this if
	 *                 needed.
	 * @param callback a non-null IStreamingCallback to be used to process the
	 *                 incoming results.
	 * @param key      an opaque integer key that is passed to the IStreamingCallback
	 *                 callback methods to identify the action as being associated
	 *                 with this specific call.
	 * @throws P4JavaException if an error occurs processing this method and its parameters.
	 * @since 2011.1
	 * @deprecated As of release 2013.1, replaced by
	 * {@link com.perforce.p4java.server.IOptionsServer#execInputStringStreamingMapCmd(java.lang.String, java.lang.String[], java.lang.String, com.perforce.p4java.server.callback.IStreamingCallback, int)}
	 */
	@Deprecated
	void execInputStringStreamingMapComd(String cmdName, String[] cmdArgs, String inString, IStreamingCallback callback, int key) throws P4JavaException;

	/**
	 * Issue an arbitrary P4Java command to the Perforce server and return the
	 * results as a map.
	 * <p>
	 * This method is intended for low-level commands in the spirit and format
	 * of the p4 command line interpreter, and offers a simple way to issue
	 * commands to the associated Perforce server without the overhead of the
	 * more abstract Java interfaces and methods.
	 * <p>
	 * No guidance is given here on the format of the returned map; however, it
	 * produces the same output as the p4 command line interpreter in -G (Python
	 * map) mode.
	 * <p>
	 * Note that this method does not allow you to set "usage" options for the
	 * command; these may be added later. Note also that although option
	 * arguments passed to this method must be in a form recognized by the p4
	 * command line interpreter, that does <i>not</i> mean the method is being
	 * implemented by the interpreter -- the actual implementation depends on
	 * the options used to get the server object in the first place from the
	 * server factory.
	 *
	 * @param cmdName the command to be issued; must be non-null, and correspond to
	 *                a Perforce command recognized by P4Java and defined in
	 *                CmdSpec.
	 * @param cmdArgs the array of command arguments (options and file arguments,
	 *                etc.) to be sent to the Perforce server. These must be in the
	 *                form used by the corresponding p4 command line interpreter.
	 *                Ignored if null.
	 * @param inMap   an optional map to be sent to the server as standard input,
	 *                using the Python map format (-G) form. You must remember to
	 *                issue the relevant command-specific option to enable this if
	 *                needed.
	 * @return a non-null Java Map of results; these results are as returned
	 * from issuing the command using the -G option with the p4 command
	 * line interpreter.
	 * @throws ConnectionException if the Perforce server is unreachable or is not connected.
	 * @throws RequestException    if the Perforce server encounters an error during its
	 *                             processing of the request
	 * @throws AccessException     if the Perforce server denies access to the caller
	 */
	Map<String, Object>[] execMapCmd(String cmdName, String[] cmdArgs, Map<String, Object> inMap) throws ConnectionException, RequestException, AccessException;

	/**
	 * Issue an arbitrary P4Java command to the Perforce server and return the
	 * results as a map without invoking any command callbacks.
	 * <p>
	 * Basically equivalent to execMapCmd with temporary disabling of any
	 * ICommandCallback calls and / or listeners; this turns out to be useful
	 * for various reasons we won't go into here...
	 * <p>
	 *
	 * @param cmdName the command to be issued; must be non-null, and correspond to
	 *                a Perforce command recognized by P4Java and defined in
	 *                CmdSpec.
	 * @param cmdArgs the array of command arguments (options and file arguments,
	 *                etc.) to be sent to the Perforce server. These must be in the
	 *                form used by the corresponding p4 command line interpreter.
	 *                Ignored if null.
	 * @param inMap   an optional map to be sent to the server as standard input,
	 *                using the Python map format (-G) form. You must remember to
	 *                issue the relevant command-specific option to enable this if
	 *                needed.
	 * @return a non-null Java Map of results; these results are as returned
	 * from issuing the command using the -G option with the p4 command
	 * line interpreter.
	 * @throws ConnectionException if the Perforce server is unreachable or is not connected.
	 * @throws RequestException    if the Perforce server encounters an error during its
	 *                             processing of the request
	 * @throws AccessException     if the Perforce server denies access to the caller
	 */
	Map<String, Object>[] execQuietMapCmd(String cmdName, String[] cmdArgs, Map<String, Object> inMap) throws ConnectionException, RequestException, AccessException;

	/**
	 * Issue an arbitrary P4Java command to the Perforce server and get the
	 * results as a stream without invoking any command callbacks.
	 * <p>
	 * Basically equivalent to execStreamCmd with temporary disabling of any
	 * ICommandCallback calls and / or listeners; this turns out to be useful
	 * for various reasons we won't go into here...
	 * <p>
	 *
	 * @param cmdName the command to be issued; must be non-null, and correspond to
	 *                a Perforce command recognized by P4Java and defined in
	 *                CmdSpec.
	 * @param cmdArgs the array of command arguments (options and file arguments,
	 *                etc.) to be sent to the Perforce server. These must be in the
	 *                form used by the corresponding p4 command line interpreter.
	 *                Ignored if null.
	 * @return an InputStream on the command output. This will never be null,
	 * but it may be empty. You <i>must</i> properly close this stream
	 * after use or temporary files may be left lying around the VM's
	 * java.io.tmpdir area.
	 * @throws ConnectionException if the Perforce server is unreachable or is not connected.
	 * @throws RequestException    if the Perforce server encounters an error during its
	 *                             processing of the request
	 * @throws AccessException     if the Perforce server denies access to the caller
	 */
	InputStream execQuietStreamCmd(String cmdName, String[] cmdArgs) throws ConnectionException, RequestException, AccessException;

	/**
	 * Issue a streaming map command to the Perforce server, using an optional
	 * map for any input expected by the server (such as label or job specs,
	 * etc.).
	 * <p>
	 * Streaming commands allow users to get each result from a suitably-issued
	 * command as it comes in from the server, rather than waiting for the
	 * entire command method to complete (and getting the results back as a
	 * completed List or Map or whatever).
	 * <p>
	 * The results are sent to the user using the IStreamingCallback
	 * handleResult method; see the IStreamingCallback Javadoc for details. The
	 * payload passed to handleResult is usually the raw map gathered together
	 * deep in the RPC protocol layer, and the user is assumed to have the
	 * knowledge and technology to be able to parse it and use it suitably in
	 * much the same way as a user unpacks or processes the results from the
	 * other low-level exec methods like execMapCommand.
	 * <p>
	 * NOTE: 'streaming' here has nothing at all to do with Perforce 'streams',
	 * which are (or will be) implemented elsewhere.
	 *
	 * @param cmdName  the command to be issued; must be non-null, and correspond to
	 *                 a Perforce command recognized by P4Java and defined in
	 *                 CmdSpec.
	 * @param cmdArgs  the array of command arguments (options and file arguments,
	 *                 etc.) to be sent to the Perforce server. These must be in the
	 *                 form used by the corresponding p4 command line interpreter.
	 *                 Ignored if null.
	 * @param inMap    an optional map to be sent to the server as standard input,
	 *                 using the Python map format (-G) form. You must remember to
	 *                 issue the relevant command-specific option to enable this if
	 *                 needed.
	 * @param callback a non-null IStreamingCallback to be used to process the
	 *                 incoming results.
	 * @param key      an opaque integer key that is passed to the IStreamingCallback
	 *                 callback methods to identify the action as being associated
	 *                 with this specific call.
	 * @throws P4JavaException if an error occurs processing this method and its parameters.
	 * @since 2011.1
	 */
	void execStreamingMapCommand(String cmdName, String[] cmdArgs, Map<String, Object> inMap, IStreamingCallback callback, int key) throws P4JavaException;

	/**
	 * Issue a parallelised streaming map command to the Perforce server, using an optional
	 * map for any input expected by the server (such as label or job specs,
	 * etc.).
	 * <p>
	 * Streaming commands allow users to get each result from a suitably-issued
	 * command as it comes in from the server, rather than waiting for the
	 * entire command method to complete (and getting the results back as a
	 * completed List or Map or whatever).
	 * <p>
	 * The results are sent to the user using the IStreamingCallback
	 * handleResult method; see the IStreamingCallback Javadoc for details. The
	 * payload passed to handleResult is usually the raw map gathered together
	 * deep in the RPC protocol layer, and the user is assumed to have the
	 * knowledge and technology to be able to parse it and use it suitably in
	 * much the same way as a user unpacks or processes the results from the
	 * other low-level exec methods like execMapCommand.
	 * <p>
	 * NOTE: 'streaming' here has nothing at all to do with Perforce 'streams',
	 * which are (or will be) implemented elsewhere.
	 *
	 * @param cmdName          the command to be issued; must be non-null, and correspond to
	 *                         a Perforce command recognized by P4Java and defined in
	 *                         CmdSpec.
	 * @param cmdArgs          the array of command arguments (options and file arguments,
	 *                         etc.) to be sent to the Perforce server. These must be in the
	 *                         form used by the corresponding p4 command line interpreter.
	 *                         Ignored if null.
	 * @param inMap            an optional map to be sent to the server as standard input,
	 *                         using the Python map format (-G) form. You must remember to
	 *                         issue the relevant command-specific option to enable this if
	 *                         needed.
	 * @param callback         a non-null IStreamingCallback to be used to process the
	 *                         incoming results.
	 * @param key              an opaque integer key that is passed to the IStreamingCallback
	 *                         callback methods to identify the action as being associated
	 *                         with this specific call.
	 * @param parallelCallback IParallelCallback used to parallelise the task
	 * @throws P4JavaException on error
	 * @since 2017.1
	 */
	public void execStreamingMapCommand(String cmdName, String[] cmdArgs, Map<String, Object> inMap, IStreamingCallback callback, int key, IParallelCallback parallelCallback) throws P4JavaException;

	/**
	 * Return the current Perforce authentication ticket being used by this
	 * server, if any. This ticket is not always guaranteed to be currently
	 * valid, so reuse should be done carefully.
	 *
	 * @return possibly-null Perforce authentication ticket
	 */
	String getAuthTicket();

	/**
	 * Return the Perforce authentication ticket for specified user.
	 *
	 * @param userName non-null Perforce user name
	 * @return possibly-null Perforce authentication ticket
	 * @since 2011.2
	 */
	String getAuthTicket(String userName);

	/**
	 * Return the Perforce authentication ticket for specified user.
	 *
	 * @param userName non-null Perforce user name
	 * @param serverId non-null Perforce server's auth.id/IP:port
	 * @return possibly-null Perforce authentication ticket
	 * @since 2021.2
	 */
	String getAuthTicket(String userName, String serverId);

	boolean isLoginNotRequired(String msgStr);

	/**
	 * Get an InputStream onto the file diffs associated with a specific
	 * submitted changelist. This method (like the similar "p4 describe"
	 * command) will not return diffs for pending changelists.
	 * <p>
	 * This is one of the guaranteed "live" method on this interface, and will
	 * return the diff output as it exists when called (rather than when the
	 * underlying implementation object was created). This can be an expensive
	 * method to evaluate, and can generate reams and reams (and reams) of
	 * output, so don't use it willy-nilly.
	 * <p>
	 * Note that unlike the corresponding command-line command, which keeps
	 * going in the face of errors by moving on to the next file (or whatever),
	 * any errors encountered in this method will cause an exception from this
	 * method at the first error, so plan accordingly....
	 *
	 * @param id       the ID of the target changelist
	 * @param diffType if non-null, describes which type of diff to perform.
	 * @return InputStream onto the diff stream. Note that while this stream
	 * will not be null, it may be empty
	 * @throws ConnectionException if the Perforce server is unreachable or is not connected.
	 * @throws RequestException    if the Perforce server encounters an error during its
	 *                             processing of the request
	 * @throws AccessException     if the Perforce server denies access to the caller
	 */
	InputStream getChangelistDiffs(int id, DiffType diffType) throws ConnectionException, RequestException, AccessException;


	/**
	 * @param repo   the graph repo
	 * @param commit the commit SHA
	 * @return non-null (but possibly empty) list of files associated with the commit.
	 * @throws ConnectionException if the Perforce server is unreachable or is not connected.
	 * @throws RequestException    if the Perforce server encounters an error during its
	 *                             processing of the request
	 * @throws AccessException     if the Perforce server denies access to the caller
	 */
	List<IFileSpec> getCommitFiles(final String repo, final String commit) throws ConnectionException, RequestException, AccessException;

	/**
	 * Get the current charset name for the server connection. May be null, in
	 * which case there is no associated character set.
	 *
	 * @return charset name associated with this server; may be null.
	 */
	String getCharsetName();

	int getGenericCode(Map<String, Object> map);

	/**
	 * Get a list of changes and / or associated files not yet integrated
	 * (unsupported). Corresponds fairly closely to the p4 interchanges command
	 * for filespecs.
	 * <p>
	 * Note that if showFiles is true, the returned files are attached to the
	 * associated changelist, and can be retrieved using the getFiles(false)
	 * method -- and note that if you call getFiles(true) you will get a
	 * refreshed list of <i>all</i> files associated with the changelist, which
	 * is probably different from the list associated with the integration.
	 * <p>
	 * Note also that if there are no qualifying changes, this method will
	 * return an empty list rather than throw an exception; this behaviour is
	 * different to that seen with the p4 command line which will throw an
	 * exception.
	 *
	 * @param fromFile        non-null from-file specification.
	 * @param toFile          non-null to-file specification.
	 * @param showFiles       if true, show the individual files that would require
	 *                        integration.
	 * @param longDesc        if true, return a long description in the changelist.
	 * @param maxChangelistId if greater than zero, only consider integration history from
	 *                        changelists at or below the given number
	 * @return non-null (but possibly empty) list of qualifying changelists.
	 * Note that the changelists returned here may not have all fields
	 * set (only description, ID, date, user, and client are known to be
	 * properly set by the server for this command).
	 * @throws ConnectionException if the Perforce server is unreachable or is not connected.
	 * @throws RequestException    if the Perforce server encounters an error during its
	 *                             processing of the request
	 * @throws AccessException     if the Perforce server denies access to the caller
	 */
	List<IChangelist> getInterchanges(IFileSpec fromFile, IFileSpec toFile, boolean showFiles, boolean longDesc, int maxChangelistId) throws ConnectionException, RequestException, AccessException;

	/**
	 * Get a list of changes and / or associated files not yet integrated, based
	 * on branchspecs (unsupported). Corresponds fairly closely to the p4
	 * interchanges command for branchspecs.
	 * <p>
	 * Note that if showFiles is true, the returned files are attached to the
	 * associated changelist, and can be retrieved using the getFiles(false)
	 * method -- and note that if you call getFiles(true) you will get a
	 * refreshed list of <i>all</i> files associated with the changelist, which
	 * is probably different from the list associated with the integration.
	 * <p>
	 * Note also that if there are no qualifying changes, this method will
	 * return an empty list rather than throw an exception; this behaviour is
	 * different to that seen with the p4 command line which will throw an
	 * exception.
	 *
	 * @param branchSpecName  non-null, non-empty branch spec name.
	 * @param fromFileList    if non-null and not empty, and biDirectional is true, use this
	 *                        as the from file list.
	 * @param toFileList      if non-null and not empty, use this as the to file list.
	 * @param showFiles       if true, show the individual files that would require
	 *                        integration.
	 * @param longDesc        if true, return a long description in the changelist.
	 * @param maxChangelistId if greater than zero, only consider integration
	 * @param reverseMapping  if true, reverse the mappings in the branch view, with the
	 *                        target files and source files exchanging place.
	 * @param biDirectional   if true, bi-directional option
	 * @return non-null (but possibly empty) list of qualifying changelists.
	 * Note that the changelists returned here may not have all fields
	 * set (only description, ID, date, user, and client are known to be
	 * properly set by the server for this command).
	 * @throws ConnectionException if the Perforce server is unreachable or is not connected.
	 * @throws RequestException    if the Perforce server encounters an error during its
	 *                             processing of the request
	 * @throws AccessException     if the Perforce server denies access to the caller
	 */
	List<IChangelist> getInterchanges(String branchSpecName, List<IFileSpec> fromFileList, List<IFileSpec> toFileList, boolean showFiles, boolean longDesc, int maxChangelistId, boolean reverseMapping, boolean biDirectional) throws ConnectionException, RequestException, AccessException;

	/**
	 * Return an array of strings representing "known" charsets (e.g. "utf8" or
	 * "utf32le".
	 * <p>
	 * Note that in this context, "known" simply means that Perforce servers
	 * supported by this API can potentially recognize the charset name and
	 * (hopefully) act accordingly.
	 * <p>
	 * Charset support in Perforce is described in more detail in the main p4
	 * command documentation; in summary, although the list returned here is
	 * comprehensive and quite impressive, unless the Perforce server is
	 * actually primed to cope with Unicode (which, by default, they're not),
	 * the only charset listed here that will work is "none"; furthermore,
	 * actual charset support is somewhat idiosyncratic -- please refer to
	 * specific documentation for guidance with this. You probably need to use
	 * this method in conjunction with the supportsUnicode() method above.
	 *
	 * @return a non-null array of strings representing lower-case charset names
	 * known to the server.
	 */
	String[] getKnownCharsets();

	/**
	 * Return the Java properties associated with this server. The Properties
	 * returned here are the actual properties used in the server and can be
	 * updated through this method (i.e. the object is not just a copy). The
	 * interpretation of the individual Properties are implementation-specific
	 * and not discussed here.
	 *
	 * @return Properties object; may be empty but will not be null.
	 */
	Properties getProperties();

	/**
	 * Get the Perforce version number of the Perforce server associated with
	 * this IServer object, if any. This will be in the form 20092 or 20073
	 * (corresponding to 2009.2 and 2007.3 respectively), but the version number
	 * will not be available if you're not actually connected to a Perforce
	 * server.
	 *
	 * @return positive integer version number or -1 if not known or
	 * unavailable.
	 */
	int getServerVersionNumber();

	int getSeverityCode(Map<String, Object> map);

	/**
	 * Return the current status of this server object.
	 *
	 * @return non-null ServerStatus representing the server status.
	 */
	ServerStatus getStatus();

	/**
	 * Get a list of Perforce user groups from the server.
	 * <p>
	 * Note that the Perforce server considers it an error to have both indirect
	 * and displayValues parameters set true; this will cause the server to
	 * throw a RequestException with an appropriate usage message.
	 *
	 * @param userOrGroupName if non-null, restrict the list to the specified group or
	 *                        username.
	 * @param indirect        if true, also displays groups that the specified user or group
	 *                        belongs to indirectly via subgroups.
	 * @param displayValues   if true, display the MaxResults, MaxScanRows, MaxLockTime, and
	 *                        Timeout values for the named group.
	 * @param maxGroups       if &gt; 0, display only the first m results.
	 * @return a non-zero but possibly-empty list of qualifying groups.
	 * @throws ConnectionException if the Perforce server is unreachable or is not connected.
	 * @throws RequestException    if the Perforce server encounters an error during its
	 *                             processing of the request.
	 * @throws AccessException     if the Perforce server denies access to the caller.
	 */
	List<IUserGroup> getUserGroups(String userOrGroupName, boolean indirect, boolean displayValues, int maxGroups) throws ConnectionException, RequestException, AccessException;

	/**
	 * Return the user name currently associated with this server, if any. User
	 * names are set using the setUserName method.
	 *
	 * @return the user name currently associated with this server, if any; null
	 * otherwise.
	 */
	String getUserName();

	/**
	 * Get the underlying server's notion of the current working directory. If
	 * this method returns null, the server is using the JVM's current working
	 * directory, typically available as the System user.dir property.
	 *
	 * @return current working directory path, or null if not set
	 */
	String getWorkingDirectory();

	/**
	 * Return the Perforce client currently associated with this Perforce
	 * server, if any.
	 *
	 * @return IClient representing the current client, or null if no client
	 * associated with this server.
	 */
	IClient getCurrentClient();

	/**
	 * Returns whether the Perforce server associated with this IServer object
	 * is case sensitive.
	 *
	 * @return - true if case sensitive, false if case insensitive.
	 */
	boolean isCaseSensitive();

	/**
	 * Return true iff and the server object is connected to the associated
	 * Perforce server.
	 * <p>
	 * The meaning of "connected" is generally dependent on the underlying
	 * transport layer, but in general, if the server is not connected, issuing
	 * server commands to the associated Perforce server will fail with a
	 * connection exception.
	 *
	 * @return - true iff connected, false otherwise
	 */
	boolean isConnected();

	/**
	 * Register a P4Java command callback with this Perforce server.
	 * <p>
	 * See the ICommandCallback javadocs for callback semantics. Note that only
	 * one command callback can be active and registered for a given server at
	 * any one time.
	 *
	 * @param callback ICommandCallback object to be registered; if null, command
	 *                 callbacks are disabled.
	 * @return the previous command callback, if it existed; null otherwise
	 */
	ICommandCallback registerCallback(ICommandCallback callback);

	/**
	 * Register a P4Java command progress callback with this Perforce server.
	 * <p>
	 * See the IProgressCallback javadocs for callback semantics. Note that only
	 * one progress callback can be active and registered for a given server at
	 * any one time.
	 *
	 * @param callback IProgressCallback object to be registered; if null, progress
	 *                 callbacks are disabled.
	 * @return the previous progress callback, if it existed; null otherwise
	 */
	IProgressCallback registerProgressCallback(IProgressCallback callback);

	/**
	 * Register a Perforce Single Sign On (SSO) callback and key for this
	 * server.
	 * <p>
	 * See the ISSOCallback Javadoc comments for an explanation of the SSO
	 * callback feature; note that only one SSO callback can be active and
	 * registered for a given P4Jserver object at any one time.
	 * <p>
	 * Note that SSO callbacks work only with the (default) pure Java (RPC)
	 * protocol implementation.
	 *
	 * @param callback ISSOCallback object to be registered; if null, SSO callbacks
	 *                 are disabled.
	 * @param ssoKey   opaque string to be passed untouched to the callback; can be
	 *                 null, in which case null is passed in to the callback
	 */
	void registerSSOCallback(ISSOCallback callback, String ssoKey);

	/**
	 * Register a Perforce browser callback to launch the browser for a given
	 * url for this server.
	 * <p>
	 * See the IBrowserCallback Javadoc comments for an explanation of the browser
	 * callback feature; note that only one browser callback can be active and
	 * registered for a given P4server object at any one time.
	 * <p>
	 *
	 * @param browserCallback BrowserCallback object to be registered; if null, browser
	 *                        callbacks are disabled.
	 */
	void registerBrowserCallback(IBrowserCallback browserCallback);

	/**
	 * Set the server's Perforce authentication ticket to the passed-in string.
	 * If the string is null, auth tickets won't be used when talking to the
	 * associated Perforce server; otherwise, the auth ticket will be used to
	 * authenticate against the Perforce server for each call to the server.
	 * <p>
	 * No checking is performed on the passed-in ticket, and any changes to
	 * existing tickets can cause authentication failures, so you should ensure
	 * the passed-in ticket is valid and makes sense for the current context.
	 *
	 * @param authTicket possibly-null Perforce authentication ticket
	 */
	void setAuthTicket(String authTicket);

	/**
	 * Set the server's Info object without calling `p4 info`.  Intended to
	 * be used for initialising new server connections for parallel threads.
	 *
	 * @param info possibly-null Perforce server information
	 */
	void setCurrentServerInfo(IServerInfo info);

	/**
	 * Get the server's Info object without calling `p4 info`.  Intended to
	 * be used for initialising new server connections for parallel threads.
	 *
	 * @return the current server's info (may be null)
	 */
	IServerInfo getCurrentServerInfo();

	/**
	 * Set the Perforce server's charset to the passed-in charset name. The
	 * semantics of this are described in the full Perforce documentation, but
	 * note that odd things will happen if the named charset isn't recognized by
	 * both the JVM and the Perforce server (i.e. "utf8" works fine, but bizarre
	 * variants may not). What constitutes a good charset name, and whether or
	 * not the server recognises it, is somewhat fraught and may involve
	 * retrieving the unicode counter and using the (printed) list of recognised
	 * charsets.
	 *
	 * @param charsetName charset name; if null, resets the charset to "no charset".
	 * @return true if the attempt to set the charset name succeeded; false
	 * otherwise. False will only be returned if the JVM doesn't support
	 * the charset. (an exception will be thrown if the server doesn't
	 * recognize it).
	 * @throws UnsupportedCharsetException if the Perforce server doesn't support or recognize the
	 *                                     charset name.
	 */
	boolean setCharsetName(String charsetName) throws UnsupportedCharsetException;

	/**
	 * Set the Perforce client associated with this server.
	 *
	 * @param client client object
	 * @throws ConnectionException if the Perforce server is unreachable or is not connected.
	 * @throws RequestException    if the Perforce server encounters an error during its
	 *                             processing of the request
	 * @throws AccessException     if the Perforce server denies access to the caller
	 */
	void setCurrentClient(IClient client) throws ConnectionException, RequestException, AccessException;

	/**
	 * Set the Perforce user name to be used with this server. This does not
	 * perform any login or checking, just associates a user name with this
	 * session. Once set, the user name is used with all commands where it makes
	 * sense.
	 * <p>
	 * Note that the auth ticket (if available) for this user will also be set
	 * to this server instance.
	 *
	 * @param userName Perforce user name; can be null, which is interpreted as
	 *                 "don't associate a user name with this server".
	 */
	void setUserName(String userName);

	/**
	 * Set the Perforce server's idea of each command's working directory. This
	 * affects all commands on this server from this point on, and the passed-in
	 * path should be both absolute and valid, otherwise strange errors may
	 * appear from the server. If dirPath is null, the Java VM's actual current
	 * working directory is used instead (which is almost always a safe option
	 * unless you're using Perforce alt roots).
	 * <p>
	 * Note: no checking is done at call time for correctness (or otherwise) of
	 * the passed-in path.
	 *
	 * @param dirPath absolute path of directory to be used, or null
	 */
	void setWorkingDirectory(String dirPath);

	/**
	 * Set the Perforce P4CLIENTPATH - directories the client can access.
	 * <p>
	 * A list of directories to which Perforce applications are permitted to write.
	 * Any attempt by a Perforce application to access or modify files outside these
	 * areas of the filesystem will result in an error message. To specify more than
	 * one directory, separate the directories with semicolons.
	 *
	 * @param clientPath client path
	 */
	void setClientPath(String clientPath);

	/**
	 * Return true IFF the underlying Perforce server supports the new 2009.1
	 * and later "smart move" command. Note that this returns whether the server
	 * can support moves only at the time the server is first created; it's
	 * entirely possible for the underlying server to change versions, etc.,
	 * under the user in the meanitme or over time. In any case, if you do try
	 * to run a move command on such a server, the results will be safe, if not
	 * entirely what you expected. As of 2010.2 it also possible for the server
	 * to be configured to disable the move command, in which case this function
	 * will return false.
	 *
	 * @return true iff the server supports the smart move command.
	 * @throws ConnectionException if the Perforce server is unreachable or is not connected.
	 * @throws RequestException    if the Perforce server encounters an error during its
	 *                             processing of the request
	 * @throws AccessException     if the Perforce server denies access to the caller
	 */
	boolean supportsSmartMove() throws ConnectionException, RequestException, AccessException;

	/**
	 * Return true if the underlying Perforce server supports Unicode (and is
	 * connected). In this context "supporting unicode" simply means that the
	 * method was able to contact the server and retrieve a "true"
	 * unicode-enabled status using the info command.
	 * <p>
	 *
	 * @return true iff the underlying server supports Unicode.
	 * @throws ConnectionException if the Perforce server is unreachable or is not connected.
	 * @throws RequestException    if the Perforce server encounters an error during its
	 *                             processing of the request
	 * @throws AccessException     if the Perforce server denies access to the caller
	 */
	boolean supportsUnicode() throws ConnectionException, RequestException, AccessException;

	/**
	 * Update a Perforce user group on the Perforce server.
	 *
	 * @param group         non-null user group to be updated.
	 * @param updateIfOwner if true, allows a user without 'super' access to modify the
	 *                      group only if that user is an 'owner' of that group.
	 * @return possibly-null status message string as returned from the server
	 * @throws ConnectionException if the Perforce server is unreachable or is not connected.
	 * @throws RequestException    if the Perforce server encounters an error during its
	 *                             processing of the request.
	 * @throws AccessException     if the Perforce server denies access to the caller.
	 */
	String updateUserGroup(IUserGroup group, boolean updateIfOwner) throws ConnectionException, RequestException, AccessException;
}
