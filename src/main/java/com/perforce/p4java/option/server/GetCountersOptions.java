/**
 *
 */
package com.perforce.p4java.option.server;

import com.perforce.p4java.exception.OptionsException;
import com.perforce.p4java.option.Options;
import com.perforce.p4java.server.IServer;

import java.util.List;

/**
 * Options class for the IOptionsServer's getCounters method.
 */
public class GetCountersOptions extends Options {

	/**
	 * Options: -u, -e[nameFilter] ..., -m[max]
	 */
	public static final String OPTIONS_SPECS = "b:u s:e s[]:e i:m:gtz";


	protected boolean undocCounter = false;

	/**
	 * If non-null, limits output to counters whose name matches
	 * the nameFilter pattern. Corresponds to '-e nameFilter' flag
	 */
	protected String nameFilter = null;

	/**
	 * If non-null, limits output to counters whose name matches
	 * any of the nameFilter patterns. Corresponds to the multiple
	 * '-u -e nameFilter -e nameFilter ...' flags.
	 */
	protected String[] nameFilters = null;

	/**
	 * If greater than zero, limit output to the first maxResults
	 * number of counters. Corresponds to '-m max' flag.
	 */
	protected int maxResults = 0;

	/**
	 * Default constructor.
	 */
	public GetCountersOptions() {
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
	public GetCountersOptions(String... options) {
		super(options);
	}

	/**
	 * Explicit value constructor.
	 *
	 * @param undocCounter undocCounter
	 * @param nameFilter   nameFilter
	 * @param maxResults   maxResults
	 */
	public GetCountersOptions(boolean undocCounter, String nameFilter, int maxResults) {
		super();
		this.undocCounter = undocCounter;
		this.nameFilter = nameFilter;
		this.maxResults = maxResults;
	}

	/**
	 * @see com.perforce.p4java.option.Options#processOptions(com.perforce.p4java.server.IServer)
	 */
	public List<String> processOptions(IServer server) throws OptionsException {
		this.optionList = this.processFields(OPTIONS_SPECS, this.isUndocCounter(), this.getNameFilter(), this.getNameFilters(), this.getMaxResults());

		return this.optionList;
	}

	public boolean isUndocCounter() {
		return undocCounter;
	}

	public GetCountersOptions setUndocCounter(boolean undocCounter) {
		this.undocCounter = undocCounter;
		return this;
	}

	public String getNameFilter() {
		return nameFilter;
	}

	public GetCountersOptions setNameFilter(String nameFilter) {
		this.nameFilter = nameFilter;
		return this;
	}

	public String[] getNameFilters() {
		return nameFilters;
	}

	public GetCountersOptions setNameFilters(String[] nameFilters) {
		this.nameFilters = nameFilters;
		return this;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public GetCountersOptions setMaxResults(int maxResults) {
		this.maxResults = maxResults;
		return this;
	}

}
