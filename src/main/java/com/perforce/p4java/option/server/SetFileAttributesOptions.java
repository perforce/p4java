/**
 *
 */
package com.perforce.p4java.option.server;

import com.perforce.p4java.exception.OptionsException;
import com.perforce.p4java.option.Options;
import com.perforce.p4java.server.IServer;

import java.util.List;

/**
 * Options class for the IOptionsServer setFileAttributes method.
 *
 * @since 2011.1
 */
public class SetFileAttributesOptions extends Options {

	/**
	 * Options: -e, -f, -p, -T0 | -T1, I
	 */
	public static final String OPTIONS_SPECS = "b:e b:f b:p b:T0 b:T1 s:I";

	/**
	 * If true, indicates values are in hex format.
	 * Corresponds to p4's -e flag.
	 */
	protected boolean hexValue = false;

	/**
	 * If true, attributes are set on submitted files.
	 * Corresponds to -f.
	 */
	protected boolean setOnSubmittedFiles = false;

	/**
	 * If true, creates attributes whose value will be propagated
	 * when the files are opened with 'p4 add', 'p4 edit', or 'p4 delete'.
	 * Corresponds to -p.
	 */
	protected boolean propagateAttributes = false;

	/**
	 * If true, saves the mentioned attribute's to db.traits
	 * Corresponds to -T0
	 * */
	protected boolean storageDbTraits = false;

	/**
	 * If true, saves the mentioned attribute's to traits depot
	 * Corresponds to -T1
	 * */
	protected boolean storageTraitsDepot = false;

	/**
	 * If true, the attribute value is read from a file rather than stdin
	 * This option can't be used with the -e option
	 * */
	protected String readAttributeValueFromFile = null;

	/**
	 * Default constructor.
	 */
	public SetFileAttributesOptions() {
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
	public SetFileAttributesOptions(String... options) {
		super(options);
	}

	/**
	 * Explicit-value constructor.
	 *
	 * @param hexValue            hexValue
	 * @param setOnSubmittedFiles setOnSubmittedFiles
	 * @param propagateAttributes propagateAttributes
	 */
	public SetFileAttributesOptions(boolean hexValue, boolean setOnSubmittedFiles, boolean propagateAttributes) {
		super();
		this.hexValue = hexValue;
		this.setOnSubmittedFiles = setOnSubmittedFiles;
		this.propagateAttributes = propagateAttributes;
	}

	/**
	 * @see com.perforce.p4java.option.Options#processOptions(com.perforce.p4java.server.IServer)
	 */
	public List<String> processOptions(IServer server) throws OptionsException {
		this.optionList = this.processFields(OPTIONS_SPECS, this.isHexValue(), this.isSetOnSubmittedFiles(), this.isPropagateAttributes(), this.isStorageDbTraits(), this.isStorageTraitsDepot(), this.getAttributeValueFileName());
		return this.optionList;
	}

	public boolean isHexValue() {
		return hexValue;
	}

	public SetFileAttributesOptions setHexValue(boolean hexValue) {
		this.hexValue = hexValue;
		return this;
	}

	public boolean isSetOnSubmittedFiles() {
		return setOnSubmittedFiles;
	}

	public SetFileAttributesOptions setSetOnSubmittedFiles(boolean setOnSubmittedFiles) {
		this.setOnSubmittedFiles = setOnSubmittedFiles;
		return this;
	}

	public boolean isPropagateAttributes() {
		return propagateAttributes;
	}

	public SetFileAttributesOptions setPropagateAttributes(boolean propagateAttributes) {
		this.propagateAttributes = propagateAttributes;
		return this;
	}

	public boolean isStorageDbTraits() {
		return storageDbTraits;
	}

	public SetFileAttributesOptions setStorageDbTraits(boolean storageDbTraits) {
		this.storageDbTraits = storageDbTraits;
		return this;
	}

	public boolean isStorageTraitsDepot() {
		return storageTraitsDepot;
	}

	public SetFileAttributesOptions setStorageTraitsDepot(boolean storageTraitsDepot) {
		this.storageTraitsDepot = storageTraitsDepot;
		return this;
	}

	public String getAttributeValueFileName() { return readAttributeValueFromFile; }

	public SetFileAttributesOptions setAttributeValueFileName(String readAttributeValueFromFile) {
		this.readAttributeValueFromFile = readAttributeValueFromFile;
		this.hexValue = false;
		return this;
	}
}
