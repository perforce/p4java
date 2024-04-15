package com.perforce.p4java.impl.mapbased.server.cmd;

import static com.perforce.p4java.impl.mapbased.server.cmd.ResultMapParser.parseCommandResultMapIfIsInfoMessageAsString;
import static com.perforce.p4java.server.CmdSpec.RENAMECLIENT;

import java.util.List;
import java.util.Map;

import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4java.server.delegator.IRenameClientDelegator;
import org.apache.commons.lang3.Validate;

/**
 * Implementation to handle the RenameClient command.
 */
public class RenameClientDelegator extends BaseDelegator implements IRenameClientDelegator {
    /**
     * Instantiate a new RenameClientDelegator, providing the server object that will be used to
     * execute Perforce Helix attribute commands.
     *
     * @param server a concrete implementation of a Perforce Helix Server
     */
    public RenameClientDelegator(IOptionsServer server) {
        super(server);
    }

    @Override
    public String renameClient(String oldClientName, String newClientName) throws P4JavaException {
        Validate.notBlank(oldClientName, "Old Client name shouldn't null or empty");
        Validate.notBlank(newClientName, "New Client name shouldn't null or empty");

        List<Map<String, Object>> resultMaps = execMapCmdList(
                RENAMECLIENT,
                new String[]{"--from=" + oldClientName, "--to=" + newClientName},
                null);

        return parseCommandResultMapIfIsInfoMessageAsString(resultMaps);
    }
}
