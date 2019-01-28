/*******************************************************************************
* Copyright (c) 2013, Saarland University. All rights reserved.
* Lisa Detzler
******************************************************************************/

package countdownZuZweit;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.HashSet;
import java.util.LinkedList;

/**
* Represents PseuCo's buffered <tt>stringchan</tt>.
*
* @author Lisa Detzler
*/
public class Stringchan extends StringChannel {

	/**
	* Represents the queue of the channel which contains the 
	* messages that are sent over the channel.
	*
	* @author Lisa Detzler
	*/
	private BlockingQueue<String> channel;
	
	/**
	* Stores all threads waiting that the channel queue is changed.
	*
	* @author Lisa Detzler
	*/
	private HashSet<PseuCoThread> waitingThreads = new HashSet<PseuCoThread>();

	/**
	* Creates a new string channel with the spcified capacity.
	*
	* @param channelSize The capacity of the channel.
	* @author Lisa Detzler
	*/
	public Stringchan(int channelSize) {
		this.channel = new ArrayBlockingQueue<String>(channelSize);
		Handshake.lock.lock();
		Handshake.channels.add(this);
		Handshake.lock.unlock();
	}

	/**
	* Removes the specified thread from the list of waiting threads.
	*
	* @param thread The thread to remove.
	* @author Lisa Detzler
	*/
	synchronized public void removeFromWaitingThreads(Thread thread) {
		waitingThreads.remove(thread);
	}

	/**
	*  Puts the specified expression to the channel queue.
	*
	* @param exp The expression to send over the channel.
	* @param thread The thread sending the specified expression.
	* @author Lisa Detzler
	*/
	public String selectPut(Object exp, PseuCoThread thread) {
		try {
			channel.add((String) exp);

			// notifies all waiting Threads
			for (PseuCoThread thread1 : waitingThreads) {
				synchronized (thread1) {
					thread1.notifyAll();
				}
			}
		} catch (IllegalStateException e) {
			waitingThreads.add(thread);
			return null;

		}
		return (String) exp;
	}

	/**
	*  Takes an message from the channel queue.
	*
	* @param thread The thread that receives the message.
	* @return The received expression.
	* @author Lisa Detzler
	*/
	public String selectTake(PseuCoThread thread) {
		String x = channel.poll();
		if (x == null) {
			waitingThreads.add(thread);
			return null;
		} else {
			// notifies all waiting Threads
			for (PseuCoThread thread1 : waitingThreads) {
				synchronized (thread1) {
					thread1.notifyAll();
				}
			}
			return x;
		}
	}

	/**
	* Handles a single-case statement with the specified case 
	* work. Every single send and receive expression can also  
	* be seen as single-case statement.
	*
	* @param w The work of the case which sends a message or 
	* 		receives one to/from this channel.
	* @return The received or sended message.
	* @author Lisa Detzler
	*/
	public String handleSelect(Work w) {
		LinkedList<Work> list = new LinkedList<Work>();
		list.add(w);
		return Handshake.handleSelect(list).getMessage("");
	}
}