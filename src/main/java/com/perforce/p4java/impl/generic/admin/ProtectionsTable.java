/**
 * Copyright (c) 2014 Perforce Software.  All rights reserved.
 */
package com.perforce.p4java.impl.generic.admin;

import java.util.List;

import com.perforce.p4java.admin.IProtectionEntry;
import com.perforce.p4java.admin.IProtectionsTable;

/**
 * Default IProtectionsTable implementation class.
 */
public class ProtectionsTable implements IProtectionsTable {

	/**
	 * List of protection entries
	 */
	private List<IProtectionEntry> entries = null;

	/**
	 * Default constructor.
	 */
	public ProtectionsTable() {
	}
	
	/**
	 * Explicit-value constructor.
	 * @param entries protections
	 */
	public ProtectionsTable(List<IProtectionEntry> entries) {
		this.entries = entries;
	}
	
	/**
	 * @see com.perforce.p4java.admin.IProtectionsTable#getEntries()
	 */
	public List<IProtectionEntry> getEntries() {
		return this.entries;
	}

	/**
	 * @see com.perforce.p4java.admin.IProtectionsTable#setEntries(java.util.List)
	 */
	public void setEntries(List<IProtectionEntry> entries) {
		this.entries = entries;
	}
}
