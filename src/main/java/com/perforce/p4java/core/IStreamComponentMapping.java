package com.perforce.p4java.core;

import java.util.Arrays;
import java.util.Optional;

public interface IStreamComponentMapping extends IMapEntry {

	enum Type {
		READONLY("readonly"), WRITE_IMPORT("writeimport+"), WRITE_ALL("writeall");

		final String text;

		Type(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}

		public static Type fromString(String typeString) {
			return Arrays.stream(Type.values())
					.filter(type -> typeString.equals(type.getText()))
					.findAny()
					.orElse(null);
		}
	}

	Type getComponentType();

	void setComponentType(Type type);

	String getDirectory();

	void setDirectory(String directory);

	String getStream();

	void setStream(String stream);

}
