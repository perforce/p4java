package com.perforce.p4java.server.callback.progress;

public enum ProgressUnit {
	UNSPECIFIED(0, "Unspecified"),
	PERCENTAGE(1, "Percentage Complete"),
	FILES(2, "Number of files"),
	K_BYTES(3, "1024 Bytes"),
	M_BYTES(4, "1024*1024 bytes"),
	DELTAS(5, "Number of deltas"),
	ITEMS(6, "Number of items"),
	DIRS(7, "Number of directories");

	final int value;
	final String description;

	ProgressUnit(int value, String description) {
		this.value = value;
		this.description = description;
	}

	public static ProgressUnit getType(int value) {
		for (ProgressUnit type : ProgressUnit.values()) {
			if (type.value == value) {
				return type;
			}
		}
		return UNSPECIFIED;
	}
}
