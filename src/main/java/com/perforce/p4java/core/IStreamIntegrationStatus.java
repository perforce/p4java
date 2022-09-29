/*
 * Copyright 2011 Perforce Software Inc., All Rights Reserved.
 */
package com.perforce.p4java.core;

import java.util.List;

/**
 * Defines the stream's cached integration status with respect to its parent. If
 * the cache is stale, either because newer changes have been submitted or the
 * stream's branch view has changed, 'istat' checks for pending integrations and
 * updates the cache before showing status.
 */
public interface IStreamIntegrationStatus {

	/**
	 * Defines the cached state of the stream's integration status without
	 * refreshing stale data.
	 */
	public interface ICachedState {

		/**
		 * Get the changelist.
		 *
		 * @return change
		 */
		int getChange();

		/**
		 * Get the parent changelist.
		 *
		 * @return parent change
		 */
		int getParentChange();

		/**
		 * Get the copy parent changelist.
		 *
		 * @return copy parent
		 */
		int getCopyParent();

		/**
		 * Get the merge parent changelist.
		 *
		 * @return merge parent
		 */
		int getMergeParent();

		/**
		 * Get the merge high value changelist.
		 *
		 * @return merge high value
		 */
		int getMergeHighVal();

		/**
		 * Get the branch hash.
		 *
		 * @return branch hash
		 */
		int getBranchHash();

		/**
		 * Get the status
		 *
		 * @return status
		 */
		int getStatus();
	}

	/**
	 * Get the stream's path in a stream depot.
	 *
	 * @return stream
	 */
	String getStream();

	/**
	 * Get the stream's parent.
	 *
	 * @return parent
	 */
	String getParent();

	/**
	 * Get the stream's type.
	 *
	 * @return type
	 */
	IStreamSummary.Type getType();

	/**
	 * Is firmer than parent.
	 *
	 * @return if true
	 */
	boolean isFirmerThanParent();

	/**
	 * Is change flows to parent.
	 *
	 * @return if true
	 */
	boolean isChangeFlowsToParent();

	/**
	 * Is change flows from parent.
	 *
	 * @return if true
	 */
	boolean isChangeFlowsFromParent();

	/**
	 * Is integration to parent.
	 *
	 * @return if true
	 */
	boolean isIntegToParent();

	/**
	 * Get how the integration to parent was performed.
	 *
	 * @return how
	 */
	String getIntegToParentHow();

	/**
	 * Get the to result.
	 *
	 * @return to result
	 */
	String getToResult();

	/**
	 * Is integration from parent.
	 *
	 * @return if true
	 */
	boolean isIntegFromParent();

	/**
	 * Get how the integration from parent was performed.
	 *
	 * @return how
	 */
	String getIntegFromParentHow();

	/**
	 * Get the from result.
	 *
	 * @return from result
	 */
	String getFromResult();

	/**
	 * Get the cached states
	 *
	 * @return list of cached states
	 */
	List<ICachedState> getCachedStates();
}
