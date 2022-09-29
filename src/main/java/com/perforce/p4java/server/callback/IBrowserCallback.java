package com.perforce.p4java.server.callback;

public interface IBrowserCallback {

	/**
	 * @param url url
	 * @throws Exception on error
	 */
	public void launchBrowser(String url) throws Exception;

}
