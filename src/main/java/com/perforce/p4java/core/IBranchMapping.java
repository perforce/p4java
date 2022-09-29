/**
 *
 */
package com.perforce.p4java.core;

/**
 * Defines an individual Perforce branch view mapping between a source path
 * and a target path.
 */
public interface IBranchMapping extends IMapEntry {

	/**
	 * Get a branch view entry's "source" spec; this corresponds
	 * to the left entry of the associated mapping.
	 *
	 * @return LHS branch mapping
	 */
	String getSourceSpec();

	/**
	 * Set a branch view entry's "source" spec; this corresponds
	 * to the left entry of the associated mapping.
	 *
	 * @param sourceSpec LHS branch mapping
	 */
	void setSourceSpec(String sourceSpec);

	/**
	 * Get a branch view entry's "target" spec; this corresponds
	 * to the right entry of the associated mapping.
	 *
	 * @return RHS branch mapping
	 */
	String getTargetSpec();

	/**
	 * Set a branch view entry's "target" spec; this corresponds
	 * to the right entry of the associated mapping.
	 *
	 * @param targeSpec RHS branch mapping
	 */
	void setTargetSpec(String targeSpec);
}
