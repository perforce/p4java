/**
 * 
 */
package com.perforce.p4java.core.file;

import java.util.Date;
import java.util.List;

/**
 * Describes a Perforce file revision in detail, including the changelist number and
 * associated description, action, user, etc. data. Full field semantics and usage
 * are given in the main Perforce documentation.
 */

public interface IFileRevisionData {
	
	/**
	 * @return the revision ID associated with this revision.
	 */
	int getRevision();
	
	/**
	 * @return the changelist ID associated with this revision.
	 */
	int getChangelistId();
	
	/**
	 * @return the file action associated with this revision.
	 */
	FileAction getAction();
	
	/**
	 * @return the date associated with this revision.
	 */
	Date getDate();
	
	/**
	 * @return the Perforce user name associated with this revision.
	 */
	String getUserName();
	
	/**
	 * @return the Perforce file type string associated with this revision.
	 */
	String getFileType();
	
	/**
	 * @return the description string associated with this revision.
	 */
	String getDescription();
	
	/**
	 * @return the depot file name associated with this revision.
	 */
	String getDepotFileName();
	
	/**
	 * @return the client file name associated with this revision.
	 */
	String getClientName();
	
	/**
	 * This method can be used to retrieve a (possibly-empty or even
	 * null) list of contributory integration data for revisions that
	 * have resulted from (or caused) a merge or branch. There's generally
	 * no easy way to tell whether there's anything to be retrieved here,
	 * so you may have to always call it and if it's null or empty, just
	 * ignore it...
	 * 
	 * @return potentially null or empty list of revision integration data
	 * 			for this specific revision.
	 */
	List<IRevisionIntegrationData> getRevisionIntegrationDataList();

	@Deprecated
	List<IRevisionIntegrationData> getRevisionIntegrationData();
}
