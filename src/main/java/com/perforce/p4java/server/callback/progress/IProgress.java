package com.perforce.p4java.server.callback.progress;

/**
 * This interface defines a handler class that provides access to progress indicators from the server.
 * After implementing the IProgress, register the callback to an instance of server via registerProgressCallback()
 * {@link com.perforce.p4java.server.IServer#registerProgressCallback(IProgress)}
 *
 * For more details, see
 * @see <a href="https://help.perforce.com/helix-core/apis/p4api/current/Content/P4API/chapter.clientprogramming.clientprogress.html">Client Progress Documentation</a>
 * ClientProgressCallbackTest (module: p4java tests), provides a sample implementation of this interface.
 *
 * Note: The property "enableProgress" should be set to true on the server instance to enable progress callbacks.
 *
 */
public interface IProgress {

	/**
	 * This is the initialization method called at the start of a progress reporting sequence by the server
	 * @param type this indicates the type of progress being reported by server.
	 *             {@link ProgressType}
	 */
	void init(ProgressType type);

	/**
	 * This method provides a description of the current progress being reported by the server.
	 * @param description this is description of the current progress
	 * @param units this indicates the units of progress being reported by server.
	 *              {@link ProgressUnit}
	 */
	void description(String description, ProgressUnit units);

	/**
	 * This method provides the total amount of work to be done for the current progress reporting sequence.
	 * @param total this is the total amount of work to be done
	 */
	void total(long total);

	/**
	 * This method provides updates on the current position of work done for the current progress reporting sequence.
	 * @param position this is the current position of work done
	 * @return an integer value indicating whether to continue (0) or abort (non-zero)
	 */
	int update(long position);

	/**
	 * This method indicates that the current progress reporting sequence is done.
	 * @param fail this indicates whether the progress reporting sequence completed successfully (0)
	 *             or failed (non-zero)
	 */
	void done(int fail);
}
