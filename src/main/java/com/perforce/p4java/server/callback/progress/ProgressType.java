package com.perforce.p4java.server.callback.progress;

public enum ProgressType {
	UNSPECIFIED(0, "Unspecified"),
	SEND_FILE(1, "Files sent to the serve."),
	RECEIVE_FILE(2, "Files received from server."),
	FILES(3, "Miscellaneous file events."),
	COMPUTATION(4, "Computation performed server-side."),
	ITEMS(5, "Revisions, integrations, and other items that are not files."),
	DIRS(6, "Directories transmitted."),
	DELETE_FILE(7, "Files deleted.");

	final int value;
	final String description;

	ProgressType(int value, String description) {
		this.value = value;
		this.description = description;
	}

	public static ProgressType getType(int value) {
		for (ProgressType type : ProgressType.values()) {
			if (type.value == value) {
				return type;
			}
		}
		return UNSPECIFIED;
	}

}



