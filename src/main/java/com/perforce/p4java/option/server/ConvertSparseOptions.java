/**
 *
 */
package com.perforce.p4java.option.server;

import com.perforce.p4java.exception.OptionsException;
import com.perforce.p4java.option.Options;
import com.perforce.p4java.server.IServer;

import java.util.List;

/**
 * Options subclass for the sparse stream convert.
 */
public class ConvertSparseOptions extends Options {

	/**
	 * Options: -q
	 */
	public static final String OPTIONS_SPECS = "b:q";

	/**
	 * If true, suppress normal output messages. Messages regarding
	 * errors or exceptional conditions are displayed.
	 * Corresponds to the -q flag.
	 * */
	public boolean suppressMessage = false;

	/**
	 * Default constructor.
	 */
	public ConvertSparseOptions() {
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
	public ConvertSparseOptions(String... options) {
		super(options);
	}

	/**
	 * Explicit-value constructor.
	 *
	 * @param suppressMessage suppressMessage
	 */
	public ConvertSparseOptions(boolean suppressMessage) {
		super();
		this.suppressMessage = suppressMessage;
	}

	/**
	 * @see com.perforce.p4java.option.Options#processOptions(com.perforce.p4java.server.IServer)
	 */
	public List<String> processOptions(IServer server) throws OptionsException {
		this.optionList = this.processFields(OPTIONS_SPECS, this.isConvertsparseSuppressMessage());
		return this.optionList;
	}

	public boolean isConvertsparseSuppressMessage() {
		return suppressMessage;
	}

	public ConvertSparseOptions setConvertsparseSuppressMessage(boolean suppressMessage) {
		this.suppressMessage = suppressMessage;
		return this;
	}
}