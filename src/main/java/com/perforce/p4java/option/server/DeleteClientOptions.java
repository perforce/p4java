/**
 *
 */
package com.perforce.p4java.option.server;

import com.perforce.p4java.exception.OptionsException;
import com.perforce.p4java.option.Options;
import com.perforce.p4java.server.IServer;

import java.util.List;

/**
 * Options object for the IOptionsServer deleteClient method.
 *
 * @see com.perforce.p4java.server.IOptionsServer#deleteClient(java.lang.String, com.perforce.p4java.option.server.DeleteClientOptions)
 */
public class DeleteClientOptions extends Options {

	/**
	 * Options: -f -Fd
	 */
	public static final String OPTIONS_SPECS = "b:f b:Fd";

	/** If true, tell the server to attempt to force the delete regardless of
	 * 	the consequences; corresponds to the -f flag.
	 */
	protected boolean force = false;

	/** If true, allows the deletion with -d of a client even when that client contains shelved changes.
	 *  The client and the shelved changes are both deleted. (You must use the -f option with the -Fd option.)
	 *  Corresponds to the -Fd flag
 	 */
	protected boolean deleteShelvedChanges = false;

	/**
	 * Default constructor.
	 */
	public DeleteClientOptions() {
		super();
	}

	/**
	 * Strings-based constructor; see 'p4 help [command]' for possible options.
	 * <p>
	 *
	 * <b>WARNING: you should not pass more than one option or argument in each
	 * string parameter. Each option or argument should be passed-in as its own
	 * separate string parameter, without any spaces between the option and the
	 * option value (if any).</b>
	 * <p>
	 *
	 * <b>NOTE: setting options this way always bypasses the internal options
	 * values, and getter methods against the individual values corresponding to
	 * the strings passed in to this constructor will not normally reflect the
	 * string's setting. Do not use this constructor unless you know what you're
	 * doing and / or you do not also use the field getters and setters.</b>
	 *
	 * @param options options
	 * @see com.perforce.p4java.option.Options#Options(java.lang.String...)
	 */
	public DeleteClientOptions(String... options) {
		super(options);
	}

	/**
	 * Explicit value constructor.
	 *
	 * @param force force option
	 */
	public DeleteClientOptions(boolean force) {
		super();
		this.force = force;
	}

	/**
	 * @see com.perforce.p4java.option.Options#processOptions(com.perforce.p4java.server.IServer)
	 */
	public List<String> processOptions(IServer server) throws OptionsException {
		this.optionList = this.processFields(OPTIONS_SPECS, this.isForce(), this.isDeleteShelvedChanges());
		return this.optionList;
	}

	public boolean isForce() {
		return force;
	}

	public boolean isDeleteShelvedChanges() { return deleteShelvedChanges; }

	public DeleteClientOptions setForce(boolean force) {
		this.force = force;
		return this;
	}

	public DeleteClientOptions setDeleteShelvedChanges(boolean deleteShelvedChanges) {
		this.deleteShelvedChanges = deleteShelvedChanges;
		this.force = true;
		return this;
	}
}
