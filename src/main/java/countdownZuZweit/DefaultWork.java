/*******************************************************************************
* Copyright (c) 2013, Saarland University. All rights reserved.
* Lisa Detzler
******************************************************************************/

package countdownZuZweit;

 /**
 * Represents the work of the default case.
 *
 * @author Lisa Detzler
 */ 
public class DefaultWork extends Work {

	/**
	* Creates a new default work.
	*
	* @param channel The channel of the work.
	* @param isInput is <tt>true</tt> if a message is send over the channel, 
	*		<tt>false</tt> otherwise.
	* @param thread The thread offering the work.
	* @param workId The ID of the work.
	*
	* @author Lisa Detzler
	*/
	public DefaultWork(Channel channel, boolean isInput, 
			Thread thread, int workId) {
		super(channel, isInput, thread, workId);
	}
}
