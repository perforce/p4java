package com.perforce.p4java.server.delegator;

import com.perforce.p4java.exception.P4JavaException;

/**
 * Interface to handle the RenameClient command.
 */
public interface IRenameClientDelegator {
	/**
	 * Completely renames a client, modifying all database records which mention
	 * the client.
	 * <p>
	 * This includes all workspaces, labels, branches, streams, etc. which are
	 * owned by the client, all pending, shelved, and committed changes created by
	 * the client, any files that the client has opened or shelved, any fixes that
	 * the client made to jobs, any properties that apply to the client, any groups
	 * that the client is in, and the client record itself.
	 * <p>
	 * Protection table entries that apply to the client are updated only if the
	 * Name: field exactly matches the client name; if the Name: field contains
	 * wildcards, it is not modified.
	 * <p>
	 * The full semantics of this operation are found in the main 'p4 help'
	 * documentation.
	 * <p>
	 * This method requires 'super' access granted by 'p4 protect'.
	 *
	 * @param oldClientName the old client name to be changed.
	 * @param newClientName the new client name to be changed to.
	 * @return non-null result message string from the reload operation.
	 * @throws P4JavaException if an error occurs processing this method and its parameters.
	 * @since 2014.1
	 */
	String renameClient(String oldClientName, String newClientName) throws P4JavaException;
}
