/**
 *
 */
package com.perforce.p4java.option.server;

import com.perforce.p4java.core.IChangelist.Type;
import com.perforce.p4java.exception.OptionsException;
import com.perforce.p4java.option.Options;
import com.perforce.p4java.server.IServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Options class for IOptionsServer getChangelists method(s).
 *
 * @see com.perforce.p4java.server.IOptionsServer#getChangelists(java.util.List, com.perforce.p4java.option.server.GetChangelistsOptions)
 */
public class GetChangelistsOptions extends Options {

	/**
	 * Options: -i, -l, -c[client [-E | --client-case-insensitive]], -m[max], -s[status], -u[user [-E | --user-case-insensitive]], -f, -L
	 */
	public static final String OPTIONS_SPECS = "b:i b:l s:c b:-client-case-insensitive i:m:gtz s:s s:u b:-user-case-insensitive b:f b:L";

	/**
	 * If positive, restrict the list to the maxMostRecent most recent changelists.
	 * Corresponds to -mmax.
	 */
	protected int maxMostRecent = 0;

	/**
	 * If non-null, restrict the results to changelists associated
	 * with the given client. Corresponds to -cclient flag.
	 */
	protected String clientName = null;

	/**
	 * If positive, flag indicates that the client
	 * value is a case-insensitive search pattern
	 */
	protected boolean clientCaseInsensitive = false;

	/**
	 * If non-null, restrict the results to changelists associated
	 * with the given user name. Corresponds to -uuser flag.
	 */
	protected String userName = null;

	/**
	 * If positive, flag indicates that the user
	 * value is a case-insensitive search pattern
	 */
	protected boolean userCaseInsensitive = false;

	/**
	 * If true, also include any changelists integrated into the
	 * specified files (if any). Corresponds to -i flag.
	 */
	protected boolean includeIntegrated = false;

	/**
	 * If not null, restrict output to pending, shelved or
	 * submitted changelists. Corresponds to -sstatus flag.
	 */
	protected Type type = null;

	/**
	 * If true, produce a non-truncated long version of the description.
	 * Corresponds to the -l flag.
	 */
	protected boolean longDesc = false;

	/**
	 * If true, enables admins to see restricted changes.
	 * Corresponds to -f flag.
	 */
	protected boolean viewRestricted = false;

	/**
	 * If true, truncate the changelist descriptions to 250
	 * characters if longer; corresponds to -L.
	 *
	 * @since 2011.1
	 */
	protected boolean truncateDescriptions = false;

	/**
	 * Default constructor.
	 */
	public GetChangelistsOptions() {
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
	public GetChangelistsOptions(String... options) {
		super(options);
	}

	/**
	 * Explicit-value constructor.
	 *
	 * @param maxMostRecent     maxMostRecent
	 * @param clientName        clientName
	 * @param userName          userName
	 * @param includeIntegrated includeIntegrated
	 * @param type              type
	 * @param longDesc          longDesc
	 */
	public GetChangelistsOptions(int maxMostRecent, String clientName, String userName, boolean includeIntegrated, Type type, boolean longDesc) {
		super();
		this.maxMostRecent = maxMostRecent;
		this.clientName = clientName;
		this.userName = userName;
		this.includeIntegrated = includeIntegrated;
		this.type = type;
		this.longDesc = longDesc;
	}

	/**
	 * Explicit-value constructor.
	 *
	 * @param maxMostRecent     maxMostRecent
	 * @param clientName        clientName
	 * @param userName          userName
	 * @param includeIntegrated includeIntegrated
	 * @param type              type
	 * @param longDesc          longDesc
	 * @param viewRestricted    viewRestricted
	 */
	public GetChangelistsOptions(int maxMostRecent, String clientName, String userName, boolean includeIntegrated, Type type, boolean longDesc, boolean viewRestricted) {
		super();
		this.maxMostRecent = maxMostRecent;
		this.clientName = clientName;
		this.userName = userName;
		this.includeIntegrated = includeIntegrated;
		this.type = type;
		this.longDesc = longDesc;
		this.viewRestricted = viewRestricted;
	}

	/**
	 * Explicit-value constructor.
	 *
	 * @param maxMostRecent        maxMostRecent
	 * @param clientName           clientName
	 * @param userName             userName
	 * @param includeIntegrated    includeIntegrated
	 * @param type                 type
	 * @param longDesc             longDesc
	 * @param viewRestricted       viewRestricted
	 * @param truncateDescriptions truncateDescriptions
	 * @since 2011.1
	 */
	public GetChangelistsOptions(int maxMostRecent, String clientName, String userName, boolean includeIntegrated, Type type, boolean longDesc, boolean viewRestricted, boolean truncateDescriptions) {
		super();
		this.maxMostRecent = maxMostRecent;
		this.clientName = clientName;
		this.userName = userName;
		this.includeIntegrated = includeIntegrated;
		this.type = type;
		this.longDesc = longDesc;
		this.viewRestricted = viewRestricted;
		this.truncateDescriptions = truncateDescriptions;
	}

	/**
	 * @see com.perforce.p4java.option.Options#processOptions(com.perforce.p4java.server.IServer)
	 */
	public List<String> processOptions(IServer server) throws OptionsException {
		if (this.optionList == null) {
			this.optionList = this.processFields(OPTIONS_SPECS, this.includeIntegrated, this.isLongDesc(), this.getClientName(), this.isClientCaseInsensitive(), this.getMaxMostRecent(), (this.getType() == null ? null : this.getType().toString()), this.getUserName(), this.isUserCaseInsensitive(), this.isViewRestricted(), this.isTruncateDescriptions());
		} else {
			this.optionList.addAll(this.processFields(OPTIONS_SPECS, this.includeIntegrated, this.isLongDesc(), this.getClientName(), this.isClientCaseInsensitive(), this.getMaxMostRecent(), (this.getType() == null ? null : this.getType().toString()), this.getUserName(), this.isUserCaseInsensitive(), this.isViewRestricted(), this.isTruncateDescriptions()));
		}

		return this.optionList;
	}

	public int getMaxMostRecent() {
		return maxMostRecent;
	}

	public GetChangelistsOptions setMaxMostRecent(int maxMostRecent) {
		this.maxMostRecent = maxMostRecent;
		return this;
	}

	public String getClientName() {
		return clientName;
	}

	public GetChangelistsOptions setClientName(String clientName) {
		this.clientName = clientName;
		return this;
	}

	public boolean isClientCaseInsensitive() { return this.clientCaseInsensitive; }

	public GetChangelistsOptions setClientCaseInsensitiveSubOption(boolean clientCaseInsensitive) {
		this.clientCaseInsensitive = clientCaseInsensitive;
		return this;
	}

	public String getUserName() {
		return userName;
	}

	public GetChangelistsOptions setUserName(String userName) {
		this.userName = userName;
		return this;
	}

	public boolean isUserCaseInsensitive() { return this.userCaseInsensitive; }

	public GetChangelistsOptions setUserCaseInsensitiveSubOption(boolean userCaseInsensitive) {
		this.userCaseInsensitive = userCaseInsensitive;
		return this;
	}

	public boolean isIncludeIntegrated() {
		return includeIntegrated;
	}

	public GetChangelistsOptions setIncludeIntegrated(boolean includeIntegrated) {
		this.includeIntegrated = includeIntegrated;
		return this;
	}

	public Type getType() {
		return type;
	}

	public GetChangelistsOptions setType(Type type) {
		this.type = type;
		return this;
	}

	public boolean isLongDesc() {
		return longDesc;
	}

	public GetChangelistsOptions setLongDesc(boolean longDesc) {
		this.longDesc = longDesc;
		return this;
	}

	public boolean isViewRestricted() {
		return viewRestricted;
	}

	public GetChangelistsOptions setViewRestricted(boolean viewRestricted) {
		this.viewRestricted = viewRestricted;
		return this;
	}

	/**
	 * @since 2011.1
	 * @return true if description is truncated
	 */
	public boolean isTruncateDescriptions() {
		return truncateDescriptions;
	}

	/**
	 * @param truncateDescriptions truncate option
	 * @since 2011.1
	 * @return GetChangelistsOptions class
	 */
	public GetChangelistsOptions setTruncateDescriptions(boolean truncateDescriptions) {
		this.truncateDescriptions = truncateDescriptions;
		return this;
	}

	public GetChangelistsOptions setMultipleClientNames(String... multipleClientNames) {
		if (this.optionList == null) {
			this.optionList = new ArrayList<>();
		}

		for (String str : multipleClientNames) {
			this.optionList.add("-c"+str);
		}

		return this;
	}

	public GetChangelistsOptions setMultipleUserNames(String... multipleUserNames) {
		if (this.optionList == null) {
			this.optionList = new ArrayList<>();
		}

		for (String str : multipleUserNames) {
			this.optionList.add("-u"+str);
		}

		return this;
	}
}
