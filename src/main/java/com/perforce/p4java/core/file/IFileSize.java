/**
 * Copyright 2013 Perforce Software Inc., All Rights Reserved.
 */
package com.perforce.p4java.core.file;


/**
 * Describes information about the size of the files in the depot. For specified file specification,
 * it shows the depot file name, revision, file count and file size. If you use client syntax for
 * the file specification,the view mapping is used to list the corresponding depot files. Full field
 * semantics and usage are given in the main Perforce documentation.
 */
public interface IFileSize {

	/**
	 * @return the depot file.
	 */
	String getDepotFile();

	/**
	 * Set the depot file.
	 *
	 * @param depotFile depotFile
	 */
	void setDepotFile(String depotFile);

	/**
	 * @return the file revision ID.
	 */
	long getRevisionId();

	/**
	 * Set the file revision ID.
	 *
	 * @param revisionId revisionId
	 */
	void setRevisionId(long revisionId);

	/**
	 * @return the file size.
	 */
	long getFileSize();

	/**
	 * Set the file size.
	 *
	 * @param fileSize fileSize
	 */
	void setFileSize(long fileSize);

	/**
	 * @return the path.
	 */
	String getPath();

	/**
	 * Set the path.
	 *
	 * @param path path
	 */
	void setPath(String path);

	/**
	 * @return the file count.
	 */
	long getFileCount();

	/**
	 * Set the file count.
	 *
	 * @param fileCount fileCount
	 */
	void setFileCount(long fileCount);

	/**
	 * @return the shelved changelist ID.
	 */
	long getChangelistId();

	/**
	 * Set the shelved changelist ID.
	 *
	 * @param changeListId changeListId
	 */
	void setChangelistId(long changeListId);
}
