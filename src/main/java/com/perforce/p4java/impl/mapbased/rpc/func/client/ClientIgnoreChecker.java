/*
 * Copyright 2012 Perforce Software Inc., All Rights Reserved.
 */
package com.perforce.p4java.impl.mapbased.rpc.func.client;

import com.perforce.p4java.mapapi.MapHalf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Handle the checking of patterns in ignore files.
 */
public class ClientIgnoreChecker {

	/** The client root. */
	private String clientRoot = null;
	
	/** The ignore file name. */
	private String ignoreFileName = null;

	/** The charset. */
	private Charset charset = null;
	
	/**
	 * Instantiates a new ignore file checker.
	 * 
	 * @param clientRoot
	 *            the client root
	 * @param ignoreFileName
	 *            the ignore file name
	 * @param charset
	 *            the charset
	 */
	public ClientIgnoreChecker(String clientRoot, String ignoreFileName, Charset charset) {
		if (clientRoot == null) {
			throw new IllegalArgumentException(
					"Null client root directory passed to IgnoreFileChecker constructor.");
		}
		if (ignoreFileName == null) {
			throw new IllegalArgumentException(
					"Null ignore file passed to IgnoreFileChecker constructor.");
		}
		if (charset == null) {
			throw new IllegalArgumentException(
					"Null charset passed to IgnoreFileChecker constructor.");
		}
		this.clientRoot = clientRoot;
		this.ignoreFileName = ignoreFileName;
		this.charset = charset;
	}

	/**
	 * Check for an ignore match of the file.
	 * 
	 * @param file
	 *            the file
	 * @return true, if successful
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public boolean match(File file) throws FileNotFoundException, IOException {
		if (file != null) {
			if (checkIgnoreFiles(file)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check for the ignore file as an absolute path
	 * or check the ignore file up to the client root directory.
	 * 
	 * @param file
	 *            the file
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private boolean checkIgnoreFiles(File file) throws IOException {
		if (file != null) {
			// when ignoreFileName is in the form of absolute path
			File ignoreFile = new File(ignoreFileName);
			if (ignoreFile.exists() && !ignoreFileName.startsWith(".")) {
				if (checkIgnorePatternInSubDirectories(ignoreFile, file)) {
					return true;
				}
			}
			// when ignoreFileName is in the form of a relative path of client root
			else {
				File clientRootDir = new File(clientRoot);
				File fileDir = file;
				do {
					fileDir = fileDir.getParentFile();
					if (fileDir != null) {
						ignoreFile = new File(fileDir, ignoreFileName);
						if (ignoreFile.exists()) {
							if (checkIgnorePatternInSubDirectories(ignoreFile, file)) {
								return true;
							}
						}
					}
				} while (fileDir != null && !fileDir.getAbsoluteFile().equals(clientRootDir));
			}
		}

		return false;
	}

	/**
	 * Checks for the ignore file patterns in all the directories and sub-directories
	 *
	 * @param ignoreFile
	 *            the ignore file
	 * @param file
	 *            the file
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private boolean checkIgnorePatternInSubDirectories(File ignoreFile, File file) throws IOException {
		if (file != null && ignoreFile.exists()) {
			// Signal for inverse match (if negation is used in ignore rules)
			Negate negate = this.new Negate();
	
			File clientRootDir = new File(clientRoot);
			File fileDir = file;

			do {
				fileDir = fileDir.getParentFile();
				if (fileDir != null) {
					if (traverseIgnoreFileForPattern(ignoreFile, fileDir, file, negate)) {
						// Inverse match
						if (negate.isMatch()) {
							return false;
						}
						return true;
					}
				}
			} while (fileDir != null && !fileDir.getAbsoluteFile().equals(clientRootDir));
		}

		return false;
	}

	/**
	 * Inversely loop through patterns in an ignore file and check for a match.
	 * 
	 * @param ignoreFile
	 *            the ignore file
	 * @param currentDir
	 *            the current directory
	 * @param file
	 *            the file
	 * @param negate
	 *            the negate
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private boolean traverseIgnoreFileForPattern(File ignoreFile, File currentDir, File file, Negate negate)
			throws IOException {

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					ignoreFile), this.charset));
			ArrayList<String> list = new ArrayList<String>();
			String line;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}

			// Reverse the lines
			Collections.reverse(list);

			for (String entry : list) {
				if (checkIgnorePattern(entry, currentDir, file, negate)) {
					return true;
				}
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}

		return false;
	}

	/**
	 * Check for a pattern match.
	 * 
	 * @param pattern
	 *            the pattern
	 * @param currentDir
	 *            the current directory
	 * @param file
	 *            the file
	 * @param negate
	 *            the negate
	 * @return true, if successful
	 */
	private boolean checkIgnorePattern(String pattern, File currentDir, File file, Negate negate) {

		boolean wildcard = false;
		boolean ellipsis = false;
		boolean negation = false;
		boolean onlyDir = false;
		boolean isRel = false;

		if (file == null) {
			return false;
		}

		if (pattern == null) {
			return false;
		}

		pattern = pattern.trim();

        // Replacing windows slashes with linux
        pattern = pattern.replaceAll("\\\\", "/");

        if (pattern.startsWith("#")) {
			return false;
		}

		// Check for negation
		if (pattern.startsWith("!")) {
			negation = true;
			pattern = pattern.substring(1);
		}

		if (pattern.length() == 0) {
			return false;
		}

		// Check for wildcard
		if (pattern.contains("*")) {
			wildcard = true;
		}

		// Check for ellipsis
		if (pattern.contains("**") || pattern.contains("...")) {
			ellipsis = true;
			pattern = pattern.replace("**", "...");
		}

		// Check if pattern is dir
		if (pattern.endsWith("/")) {
			onlyDir = true;
		}
        if (pattern.startsWith("/")) {
			isRel = true;
		}

		// Match file name or path
		if (!wildcard  && !ellipsis) {
			pattern = "..." + pattern + "...";
		}

		String path = file.getAbsolutePath();
		if (!isRel && !onlyDir) {
			path = path.substring(currentDir.getAbsolutePath().length()+1);
		} else {
			path = path.substring(currentDir.getAbsolutePath().length());
		}
        path = path.replaceAll("\\\\", "/");

		MapHalf mapPattern = new MapHalf(pattern);
		MapHalf mapPath = new MapHalf(path);
		boolean match = mapPattern.match(mapPath);

		// Match pattern
		if (match) {
			if (negation) {
				negate.setMatch(true);
			}

			return true;
		}

		return false;
	}

	/**
	 * Signal for negate pattern.
	 */
	private class Negate {
		
		/** The match. */
		private boolean match = false;

		/**
		 * Checks if is match.
		 * 
		 * @return true, if is match
		 */
		public boolean isMatch() {
			return match;
		}

		/**
		 * Sets the match.
		 * 
		 * @param match
		 *            the new match
		 */
		public void setMatch(boolean match) {
			this.match = match;
		}
	}
}