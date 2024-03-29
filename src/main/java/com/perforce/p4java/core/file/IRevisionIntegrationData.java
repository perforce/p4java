/*
 * Copyright 2009 Perforce Software Inc., All Rights Reserved.
 */
package com.perforce.p4java.core.file;

/**
 * Used to specify Perforce file integration data in short form
 * for specific file revisions. Unbranched / unmerged / unintegrated
 * (etc.) files do not commonly have any such data; others <i>may</i>
 * contain it, but this is not guaranteed.
 */

public interface IRevisionIntegrationData {

	/**
	 * @return the file this integration was from.
	 */
	String getFromFile();

	/**
	 * @return the end revision used in the integration.
	 */
	int getEndFromRev();

	/**
	 * @return the start revision used in the integration.
	 */
	int getStartFromRev();

	/**
	 * @return a string description of what happened in the integration.
	 */
	String getHowFrom();
}
