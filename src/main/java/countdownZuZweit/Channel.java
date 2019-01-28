/*******************************************************************************
* Copyright (c) 2013, Saarland University. All rights reserved.
* Lisa Detzler
******************************************************************************/

package countdownZuZweit;

import java.util.concurrent.locks.ReentrantLock;

/**
	* Represents a channel.
	*
	* @author Lisa Detzler
	*/
public abstract class Channel {

	/**
	* Defines the lock which is used to lock the channel.
	*
	* @author Lisa Detzler
	*/
	private ReentrantLock lock = new ReentrantLock();

	/**
	* Returns the lock of the channel.
	*
	* @return The lock of the channel.
	* @author Lisa Detzler
	*/
	public ReentrantLock getLock() {
		return lock;
	}

	/**
	* Sets the lock of the channel to the specified one.
	*
	* @param lock The new lock.
	* @author Lisa Detzler
	*/
	public void setLock(ReentrantLock lock) {
		this.lock = lock;
	}
	

}
