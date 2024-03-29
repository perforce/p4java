/**
 *
 */
package com.perforce.p4java.option.server;

import com.perforce.p4java.exception.OptionsException;
import com.perforce.p4java.option.Options;
import com.perforce.p4java.server.IServer;

import java.util.List;

/**
 * Options class for IOptionsServer.getBranchSpec method.
 *
 * @see com.perforce.p4java.server.IOptionsServer#getBranchSpec(java.lang.String, com.perforce.p4java.option.server.GetBranchSpecOptions)
 */
public class GetBranchSpecOptions extends Options {

	/**
	 * Options: -S[stream], -P[parentStream]
	 */
	public static final String OPTIONS_SPECS = "s:S s:P";

	/**
	 * If not null, the -S stream flag will expose the internally generated
	 * mapping. The stream's path in a stream depot, of the form //depotname/streamname.
	 */
	protected String stream = null;

	/**
	 * If not null, the -P flag may be used with -S to treat the stream as if it
	 * were a child of a different parent. The parent stream's path in a stream
	 * depot, of the form //depotname/streamname.
	 */
	protected String parentStream = null;

	/**
	 * Default constructor.
	 */
	public GetBranchSpecOptions() {
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
	public GetBranchSpecOptions(String... options) {
		super(options);
	}

	/**
	 * Explicit value constructor.
	 * @param stream       stream
	 * @param parentStream parent stream
	 */
	public GetBranchSpecOptions(String stream, String parentStream) {
		super();
		this.stream = stream;
		this.parentStream = parentStream;
	}

	/**
	 * @see com.perforce.p4java.option.Options#processOptions(com.perforce.p4java.server.IServer)
	 */
	public List<String> processOptions(IServer server) throws OptionsException {
		this.optionList = this.processFields(OPTIONS_SPECS,
				this.getStream(),
				this.getParentStream());
		return this.optionList;
	}

	public String getStream() {
		return stream;
	}

	public GetBranchSpecOptions setStream(String stream) {
		this.stream = stream;
		return this;
	}

	public String getParentStream() {
		return parentStream;
	}

	public GetBranchSpecOptions setParentStream(String parentStream) {
		this.parentStream = parentStream;
		return this;
	}
}
