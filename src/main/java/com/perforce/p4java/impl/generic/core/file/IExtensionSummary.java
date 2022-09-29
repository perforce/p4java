package com.perforce.p4java.impl.generic.core.file;

public interface IExtensionSummary {
	/**
	 * A Perforce Extension Specification.
	 *
	 * Fields:
	 *         901 ExtName line 64 once
	 *         906 Name line 32 default - Namspace
	 *         903 ExtVersion line 32 once
	 *         904 ExtUUID line 36 once
	 *         905 ExtRev word 20 once
	 *         916 ExtEnabled word 12 default
	 */

	/**
	 * Get the name of the Extension being configured.
	 *
	 * @return Extension name
	 */
	String getExtName();

	/**
	 * Get the version of the Extension being configured.
	 *
	 * @return Extension version
	 */
	String getExtVersion();

	/**
	 * Get the UUID/key of the Extension being configured.
	 *
	 * @return Extension UUID
	 */
	String getExtUUID();

	/**
	 * Get the revision of the Extension being configured.
	 *
	 * @return Extension revision
	 */
	String getExtRev();

	/**
	 * Check if Extension is enabled or disabled.
	 *
	 * @return true/false
	 */
	String getExtEnabled();

	/**
	 * Get the namespace of this Extension config.
	 *
	 * @return Namespace
	 **/
	String getNameSpace();

	/**
	 * Get Extension developer.
	 *
	 * @return Extension developer
	 */
	String getExtDeveloper();

	/**
	 * Get the Extension description snippet.
	 *
	 * @return Description snippet
	 */
	String getExtDescriptionSnippet();

	/**
	 * Returns the path to the file in the server extension’s unpacked archive directory.
	 * This path is relative to the server.extensions.dir configurable.
	 *
	 * @return Extension archive directory
	 */
	String getExtArchDir();

	/**
	 * Returns the path in which the server extension will store the files it creates.
	 *
	 * @return Extension data directory
	 */
	String getExtDataDir();

	/**
	 * Check if Extension has global config
	 *
	 * @return true/false
	 */
	boolean getExtGlobalConf();

	/**
	 * Check if Extension has instance config
	 *
	 * @return true/false
	 */
	boolean getExtInstanceConf();

	/**
	 * Set the name of the Extension being configured.
	 *
	 * @param extName name
	 */
	void setExtName(String extName);

	/**
	 * Set the version of the Extension being configured.
	 *
	 * @param extVersion version
	 */
	void setExtVersion(String extVersion);

	/**
	 * Set the UUID/key of the Extension being configured.
	 *
	 * @param extUUID UUID
	 */
	void setExtUUID(String extUUID);

	/**
	 * Set the revision of the Extension being configured.
	 *
	 * @param extRev revision
	 */
	void setExtRev(String extRev);

	/**
	 * Enable or disable the extension
	 *
	 * @param extEnabled true/false
	 */
	void setExtEnabled(String extEnabled);

	/**
	 * Set the namespace of this Extension config.
	 *
	 * @param name name
	 */
	void setNameSpace(String name);

	/**
	 * Set the extension developer field.
	 *
	 * @param extDeveloper developer
	 */
	void setExtDeveloper(String extDeveloper);

	/**
	 * Set the Extension description snippet.
	 *
	 * @param extDescriptionSnippet description
	 */
	void setExtDescriptionSnippet(String extDescriptionSnippet);

	/**
	 * Set the path to the file in the server extension’s unpacked archive directory.
	 * This path is relative to the server.extensions.dir configurable.
	 *
	 * @param extArchDir archive directory
	 */
	void setExtArchDir(String extArchDir);

	/**
	 * Set path in which the server extension will store the files it creates.
	 *
	 * @param extDataDir data directory
	 */
	void setExtDataDir(String extDataDir);

	/**
	 * Set extension as a global.
	 *
	 * @param extGlobalConf is global
	 */
	void setExtGlobalConf(boolean extGlobalConf);

	/**
	 * Set extension as an instance.
	 *
	 * @param extInstanceConf is instance
	 */
	void setExtInstanceConf(boolean extInstanceConf);
}
