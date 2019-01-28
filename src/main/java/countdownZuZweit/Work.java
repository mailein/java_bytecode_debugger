/*******************************************************************************
* Copyright (c) 2013, Saarland University. All rights reserved.
* Lisa Detzler
******************************************************************************/

package countdownZuZweit;

public class Work {

	/**
	* Specifies the channel of the work.
	*
	* @author Lisa Detzler
	*/
	Channel channel;

	/**
	* Specifies whether the work sends or receives a message. 
	* <tt>true</tt> if it sends one, <tt>false</tt> otherwise. 
	*
	* @author Lisa Detzler
	*/
	boolean isInput;
	
	/**
	* Specifies the integer message of the work. If the channel of the work 
	* is not of type <tt>intchan</tt> this field is empty.
	*
	* @author Lisa Detzler
	*/
	Integer iMessage;

	/**
	* Specifies the string message of the work. If the channel of the work 
	* is not of type <tt>stringchan</tt> this field is empty.
	*
	* @author Lisa Detzler
	*/
	String sMessage;
	
	/**
	* Specifies the boolean message of the work. If the channel of the work 
	* is not of type <tt>boolchan</tt> this field is empty.
	*
	* @author Lisa Detzler
	*/
	Boolean bMessage;
	
	/**
	* Specifies the thread of the work.
	*
	* @author Lisa Detzler
	*/
	PseuCoThread thread;
	
	/**
	* Specifies the ID of the work.
	*
	* @author Lisa Detzler
	*/
	int workId;

	/**
	* Creates a new work receiving a message from the specified 
	* channel.
	*
	* @param channel The channel of the work.
	* @param isInput is <tt>true</tt> if a message is send over  
	*		the channel, <tt>false</tt> otherwise.
	* @param thread The thread offering the work.
	* @param workId The ID of the work.
	*
	* @author Lisa Detzler
	*/
	public Work(Channel channel, boolean isInput, Thread thread, int workId) {
		init(channel, isInput, thread, workId);
	}

	/**
	* Initializes the work.
	*
	* @param channel The channel of the work.
	* @param isInput is <tt>true</tt> if a message is send over  
	*		the channel, <tt>false</tt> otherwise.
	* @param thread The thread offering the work.
	* @param workId The ID of the work.
	*
	* @author Lisa Detzler
	*/
	private void init(Channel channel, boolean isInput, Thread thread,
			int workId) {
		this.channel = channel;
		this.isInput = isInput;
		PseuCoThread t;
		try {
			t = (PseuCoThread) PseuCoThread.currentThread();
		} catch (java.lang.ClassCastException e) {
			t = Main.thread;
		}
		this.thread = t;
		this.workId = workId;
		iMessage = null;
		bMessage = null;
		sMessage = null;
	}

	/**
	* Creates a new work sending the specified integer over the 
	* specified channel.
	*
	* @param channel The channel of the work.
	* @param isInput is <tt>true</tt> if a message is send over  
	*		the channel, <tt>false</tt> otherwise.
	* @param iMessage The integer message to send.
	* @param thread The thread offering the work.
	* @param workId The ID of the work.
	*
	* @author Lisa Detzler
	*/
	public Work(Channel channel, boolean isInput, int iMessage,
			Thread thread, int workId) {
		init(channel, isInput, thread, workId);
		this.iMessage = iMessage;
	}

	/**
	* Creates a new work sending the specified string over the 
	* specified channel.
	*
	* @param channel The channel of the work.
	* @param isInput is <tt>true</tt> if a message is send over  
	*		the channel, <tt>false</tt> otherwise.
	* @param sMessage The string message to send.
	* @param thread The thread offering the work.
	* @param workId The ID of the work.
	*
	* @author Lisa Detzler
	*/
	public Work(Channel channel, boolean isInput, String sMessage,
			Thread thread, int workId) {
		init(channel, isInput, thread, workId);
		this.sMessage = sMessage;
	}

	/**
	* Creates a new work sending the specified boolean over the 
	* specified channel.
	*
	* @param channel The channel of the work.
	* @param isInput is <tt>true</tt> if a message is send over  
	*		the channel, <tt>false</tt> otherwise.
	* @param bMessage The boolean message to send.
	* @param thread The thread offering the work.
	* @param workId The ID of the work.
	*
	* @author Lisa Detzler
	*/
	public Work(Channel channel, boolean isInput, boolean bMessage,
			Thread thread, int workId) {
		init(channel, isInput, thread, workId);
		this.bMessage = bMessage;
	}

	/**
	* Returns the channel of the work.
	*
	* @return The channel.
	* @author Lisa Detzler
	*/
	synchronized public Channel getChannel() {
		return channel;
	}

	/**
	* Checks whether work sends or receives a message.
	*
	* @return <tt>true</tt> if the work send a message,
			<tt>false</tt> otherwise.
	* @author Lisa Detzler
	*/
	synchronized public boolean isInput() {
		return isInput;
	}

	/**
	* Stores that the work sends a message if <tt>isInput</tt> 
	* is <tt>true</tt>. Otherwise work receives a message.
	*
	* @param isInput Specifies whether work sends or receives
	* 		a message.
	* @author Lisa Detzler
	*/
	synchronized public void setInput(boolean isInput) {
		this.isInput = isInput;
	}

	/**
	* Returns the message of the work.
	*
	* @return The message of the work.
	* @author Lisa Detzler
	*/
	synchronized public Object getMessage() {
		if (iMessage != null) {
			return iMessage;
		} else {
			if (bMessage != null) {
				return bMessage;
			} else {
				return sMessage;
			}
		}
	}

	/**
	* Returns the thread of the work.
	*
	* @return The thread of the work.
	* @author Lisa Detzler
	*/
	synchronized public PseuCoThread getThread() {
		return thread;
	}

	/**
	* Returns the ID of the work.
	*
	* @return The ID of the work.
	* @author Lisa Detzler
	*/
	public int getWorkId() {
		return workId;
	}

	/**
	* Sets the ID of the work to the specified one.
	*
	* @param workId The new ID of the work.
	* @author Lisa Detzler
	*/
	public void setWorkId(int workId) {
		this.workId = workId;
	}

}
