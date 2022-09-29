/*
 * Copyright 2011 Perforce Software Inc., All Rights Reserved.
 */
package com.perforce.p4java.core.file;

import java.util.List;

/**
 * Record stats returned by the obliterateFiles method. Obliterate removes files
 * and their history from the depot. The Perforce server returns information
 * about how many various types of records were deleted (or added).
 */

public interface IObliterateResult {
	
	/**
	 * @return the list of filespecs purged
	 */
	List<IFileSpec> getFileSpecs();

	/**
	 * @return the number of integration records added
	 */
	int getIntegrationRecAdded();
	
	/**
	 * @return the number of integration records deleted
	 */
	int getLabelRecDeleted();

	/**
	 * @return the number of client records deleted
	 */
	int getClientRecDeleted();

	/**
	 * @return the number of integration records deleted
	 */
	int getIntegrationRecDeleted();

	/**
	 * @return the number of working records deleted
	 */
	int getWorkingRecDeleted();

	/**
	 * @return the number of revision records deleted
	 */
	int getRevisionRecDeleted();
	
	/**
	 * @return true, if report only
	 */
	boolean isReportOnly();
}
