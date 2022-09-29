package com.perforce.p4java.core;

import java.util.Date;

public interface IRepo extends IServerResource {

	/**
	 * Get the repo's name.
	 *
	 * @return name
	 */
	String getName();

	/**
	 * Get the Perforce user name of the repo's owner.
	 *
	 * @return owner
	 */
	String getOwnerName();

	/**
	 * Get the date the repo was created.
	 *
	 * @return date
	 */
	Date getCreatedDate();

	/**
	 * Get the date the repo was last pushed.
	 *
	 * @return date
	 */
	Date getPushedDate();

	/**
	 * Get the description associated with this repo.
	 *
	 * @return description
	 */
	String getDescription();

	/**
	 * Set the description associated with this repo.
	 *
	 * @param description new repo description string.
	 */
	void setDescription(String description);

	/**
	 * @return fork
	 */
	String getForkedFrom();

	/**
	 * @param forkedFrom fork
	 */
	void setForkedFrom(String forkedFrom);

	/**
	 * @return branch
	 */
	String getDefaultBranch();

	/**
	 * @param defaultBranch branch
	 */
	void setDefaultBranch(String defaultBranch);

	/**
	 * @return mirror
	 */
	String getMirroredFrom();

	/**
	 * @param mirroredFrom mirror
	 */
	void setMirroredFrom(String mirroredFrom);
}
