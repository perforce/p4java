/**
 *
 */
package com.perforce.p4java.option.server;

import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.OptionsException;
import com.perforce.p4java.option.Options;
import com.perforce.p4java.server.IServer;

import java.util.List;

/**
 * Options class for the IOptionsServer.getFileContents method.
 *
 * @see com.perforce.p4java.server.IOptionsServer#getFileContents(java.util.List, com.perforce.p4java.option.server.GetFileContentsOptions)
 */
public class GetFileContentsOptions extends Options {

	/**
	 * Options: -a, -q, --offset, --size
	 */
	public static final String OPTIONS_SPECS = "b:a b:q";

	/**
	 * Options: --offset, --size
	 */
	public static final String OPTION_SPEC_NEW = "l:-offset l:-size";

	/**
	 * If true, get the contents of all revisions within the specific range, rather
	 * than just the highest revision in the range. Corresponds to -a.
	 */
	protected boolean allrevs = false;

	/**
	 * If true, suppress the initial line that displays the file name and revision.
	 * Corresponds to -q.
	 */
	protected boolean noHeaderLine = false;

	/**
	 * If true, don't append revision specifiers (# and @) to the filespecs
	 * (Parameters.processParameters(...)). By default the filespecs passed to
	 * IOptionsServer.getFileContents() would get revisions appended to them
	 * during parameter processing.<p>
	 * Note that this is not a standard option for this command. It is merely a
	 * convenience flag to tell the parameter processor not to include revisions
	 * with the filespecs.
	 */
	protected boolean dontAnnotateFiles = false;

	/**
	 * Skip the specified number of bytes and only print what follows.
	 */
	protected long offset = 0L;

	/**
	 * Print the specified number of bytes from the offset.
	 */
	protected long size = 0L;

	/**
	 * Default constructor -- sets all fields to false.
	 */
	public GetFileContentsOptions() {
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
	public GetFileContentsOptions(String... options) {
		super(options);
	}

	/**
	 * Explicit-value constructor.
	 *
	 * @param allrevs      allrevs
	 * @param noHeaderLine noHeaderLine
	 */
	public GetFileContentsOptions(boolean allrevs, boolean noHeaderLine) {
		super();
		this.allrevs = allrevs;
		this.noHeaderLine = noHeaderLine;
	}

	/**
	 * @see com.perforce.p4java.option.Options#processOptions(com.perforce.p4java.server.IServer)
	 */
	public List<String> processOptions(IServer server) throws OptionsException {
		int serverVersion = 0;
		try {
			serverVersion = server.getServerVersion();
		} catch (ConnectionException e) {
			throw new OptionsException("Can not connect to server.", e);
		}
		if (serverVersion >= 20221) {
			this.optionList = this.processFields(OPTIONS_SPECS + " " + OPTION_SPEC_NEW,
					this.isAllrevs(), this.isNoHeaderLine(),
					this.offset, this.size);

		} else {
			this.optionList = this.processFields(OPTIONS_SPECS, this.isAllrevs(), this.isNoHeaderLine());
		}
		return this.optionList;
	}

	public boolean isAllrevs() {
		return allrevs;
	}

	public GetFileContentsOptions setAllrevs(boolean allrevs) {
		this.allrevs = allrevs;
		return this;
	}

	public boolean isNoHeaderLine() {
		return noHeaderLine;
	}

	public GetFileContentsOptions setNoHeaderLine(boolean noHeaderLine) {
		this.noHeaderLine = noHeaderLine;
		return this;
	}

	public boolean isDontAnnotateFiles() {
		return dontAnnotateFiles;
	}

	public GetFileContentsOptions setDontAnnotateFiles(boolean dontAnnotateFiles) {
		this.dontAnnotateFiles = dontAnnotateFiles;
		return this;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
}
