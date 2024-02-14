/**
 * Copyright 2008 Perforce Software Inc., All Rights Reserved.
 */
package com.perforce.p4java.exception;

import com.perforce.p4java.Log;

public class LowResourceException extends RequestException {
	public LowResourceException(String message, Throwable cause) { super(message, cause); }

	public LowResourceException(String message) { super(message); }

	public LowResourceException(Throwable cause) {
		super(cause);
	}

	public LowResourceException(String message, String codeString) {
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