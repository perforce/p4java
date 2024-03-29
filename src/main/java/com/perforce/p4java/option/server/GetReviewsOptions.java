/**
 *
 */
package com.perforce.p4java.option.server;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.exception.OptionsException;
import com.perforce.p4java.option.Options;
import com.perforce.p4java.server.IServer;

import java.util.List;

/**
 * Options class for the IOptionsServer.getReviews method.
 *
 * @see com.perforce.p4java.server.IOptionsServer#getReviews(java.util.List, com.perforce.p4java.option.server.GetReviewsOptions)
 */
public class GetReviewsOptions extends Options {

	/**
	 * Options: -c[changelist]
	 */
	public static final String OPTIONS_SPECS = "i:c:cl";

	/**
	 * If non-negative, use the value of changelistId for
	 * the changelist; use IChangelist.DEFAULT for the default changelist.
	 * Corresponds to -c#.
	 */
	protected int changelistId = IChangelist.UNKNOWN;

	/**
	 * Default constructor.
	 */
	public GetReviewsOptions() {
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
	public GetReviewsOptions(String... options) {
		super(options);
	}

	/**
	 * Explicit-value constructor.
	 *
	 * @param changelistId changelistId
	 */
	public GetReviewsOptions(int changelistId) {
		super();
		this.changelistId = changelistId;
	}

	/**
	 * @see com.perforce.p4java.option.Options#processOptions(com.perforce.p4java.server.IServer)
	 */
	public List<String> processOptions(IServer server) throws OptionsException {
		this.optionList = this.processFields(OPTIONS_SPECS, this.getChangelistId());
		return this.optionList;
	}

	public int getChangelistId() {
		return changelistId;
	}

	public GetReviewsOptions setChangelistId(int changelistId) {
		this.changelistId = changelistId;
		return this;
	}
}
