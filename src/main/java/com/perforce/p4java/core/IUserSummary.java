/*
 * Copyright 2009 Perforce Software Inc., All Rights Reserved.
 */
package com.perforce.p4java.core;

import com.perforce.p4java.Log;

import java.util.Date;

/**
 * Defines a Perforce user summary as returned from an IServer
 * getUsers method and similar. IUserSummary objects contain
 * only the summary information returned by the p4 users
 * command; to get the full user information from the server,
 * use the IUser interface returned from getUser, etc.
 * <p>
 * Fields and methods below are basically self-explanatory
 * and more detailed documentation can be found in the main
 * p4 documentation. Note that any or all of these methods
 * are allowed to return null.
 * <p>
 * IUserSummary objects are complete and neither refreshable nor updateable.
 */
public interface IUserSummary extends IServerResource {

	/**
	 * Describes the type (service or standard) of this user.
	 *
	 * @since 2011.1
	 */
	public enum UserType {
		STANDARD, OPERATOR, SERVICE, UNKNOWN;

		/**
		 * Return a suitable User type as inferred from the passed-in
		 * string, which is assumed to be the string form of a User type.
		 * Otherwise return the UNKNOWN type
		 *
		 * @param str str
		 * @return UserType
		 */
		public static UserType fromString(String str) {
			if (str == null) {
				return null;
			}

			try {
				return UserType.valueOf(str.toUpperCase());
			} catch (IllegalArgumentException iae) {
				Log.error("Bad conversion attempt in UserType.fromString; string: " + str + "; message: " + iae.getMessage());
				Log.exception(iae);
				return UNKNOWN;
			}
		}
	}

	;

	String getLoginName();

	void setLoginName(String loginName);

	Date getUpdate();

	void setUpdate(Date update);

	Date getAccess();

	void setAccess(Date access);

	String getFullName();

	void setFullName(String fullName);

	String getEmail();

	void setEmail(String email);

	/**
	 * Get the UserType associated with this user. May return
	 * null if no type was set.
	 *
	 * @return user type
	 * @since 2011.1
	 */
	UserType getType();

	/**
	 * Set the UserType associated with this user.
	 *
	 * @param type type
	 * @since 2011.1
	 */
	void setType(UserType type);

	/**
	 * Get the date the ticket associated with this summary user expires.
	 * <p>
	 * This field will only be non-null if a) the user summary object
	 * it's a part of was retrieved using the IOptionsServer.getUsers method
	 * with the GetUsersOptions().setExtendedOutput option set to true;
	 * b) the caller was a super-user or admin; and, c) the Perforce
	 * server was 2011.1 or above. <b>In all other cases the value
	 * of this field is either null or not reliable</b>.
	 * <p>
	 * This is a read-only field synthesized by the Perforce server, and
	 * can not be meaningfully set in the client.
	 *
	 * @return possibly-null Date object.
	 * @since 2011.1
	 */
	Date getTicketExpiration();

	/**
	 * Get the date the password associated with this summary user expires.
	 * <p>
	 * This field will only be non-null if a) the user summary object
	 * it's a part of was retrieved using the IOptionsServer.getUsers method
	 * with the GetUsersOptions().setExtendedOutput option set to true;
	 * b) the caller was a super-user or admin; and, c) the Perforce
	 * server was 2011.1 or above. <b>In all other cases the value
	 * of this field is either null or not reliable</b>.
	 * <p>
	 * This is a read-only field synthesized by the Perforce server, and
	 * can not be meaningfully set in the client.
	 *
	 * @return possibly-null Date object.
	 * @since 2011.1
	 */
	Date getPasswordChange();
}
