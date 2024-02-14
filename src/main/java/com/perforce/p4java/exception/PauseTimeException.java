/**
 * Copyright 2008 Perforce Software Inc., All Rights Reserved.
 */
package com.perforce.p4java.exception;

import com.perforce.p4java.Log;

public class PauseTimeException extends RequestException {
	public PauseTimeException(String message, Throwable cause) { super(message, cause); }

	public PauseTimeException(String message) { super(message); }

	public PauseTimeException(Throwable cause) {
		super(cause);
	}

	public PauseTimeException(String message, String codeString) {
		super(message);
		if (codeString != null) {
			try {
				setCodes(new Integer(codeString));
			} catch (Exception exc) {
				Log.exception(exc);
			}
		}
	}
}