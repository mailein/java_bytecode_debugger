/*******************************************************************************
* Copyright (c) 2013, Saarland University. All rights reserved.
* Lisa Detzler
******************************************************************************/

package countdownZuZweit;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
* Extends the java.lang.Thread by some information needed for the 
* select-case algorithm.
*
* @author Lisa Detzler
*/
public class PseuCoThread extends java.lang.Thread {

	/**
	* Specifies the number of the started threads so far.
	*
	* @author Lisa Detzler
	*/
	public static int numberOfThreads = 0;

	/**
	* Specifies the ID of the thread. 
	*
	* @author Lisa Detzler
	*/
	private String pseuCoId;

	/**
	* Can store at most one message. Is used when two thread synchronize together
	* via a handshake channel. The messages are then exchanged using this queue.
	*
	* @author Lisa Detzler
	*/	
	private BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>(
			1);

	/**
	* Creates a new PseuCo thread.
	*
	* @param procName The name of the procedure the thread is executing when 
	* 		it is started.
	* @param The runnable executed by the thread.
	*
	* @author Lisa Detzler
	*/
	public PseuCoThread(String procName,Runnable r) {
		super(r);
		synchronized (this.getClass()) {
			pseuCoId = procName+":"+numberOfThreads;
			numberOfThreads=numberOfThreads+1;
		}
	}

	/**
	* Creates a new PseuCo thread.
	*
	* @param procName The name of the procedure the thread is executing when 
	* 		it is started.
	*
	* @author Lisa Detzler
	*/
	public PseuCoThread(String procName) {
		synchronized (this.getClass()) {
			pseuCoId = procName+":"+numberOfThreads;
			numberOfThreads=numberOfThreads+1;
		}
	}
	
	/**
	* Creates a new PseuCo thread.
	*
	* @author Lisa Detzler
	*/
	public PseuCoThread() {
	}

	/**
	* Adds the specified message to the message queue of the thread.
	*
	* @param message The message to add.
	*
	* @author Lisa Detzler
	*/
	public void addMessage(Message message) {
		try {
			messageQueue.put(message);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	* Returns the message in the queue of the thread.
	*
	* @return The message in the queue of the thread.
	*
	* @author Lisa Detzler
	*/
	public Message getMessage() {
		return messageQueue.poll();
	}

	/**
	* Returns the PseuCo ID of this thread.
	*
	* @return The PseuCo ID of this thread.
	*
	* @author Lisa Detzler
	*/
	public String getPseuCoId() {
		return this.pseuCoId;
	}

}
