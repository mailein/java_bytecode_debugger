/*******************************************************************************
* Copyright (c) 2013, Saarland University. All rights reserved.
* Lisa Detzler
******************************************************************************/

package countdownZuZweit;

/**
* Represents a message that can be sent or received over/from a channel.
*
* @author Lisa Detzler
*/
public class Message {

	/**
	* Specifies the interger value of the message. This field is 
	* empty if the message stores no integer.
	*
	* @author Lisa Detzler
	*/
	private Integer iMessage;

	/**
	* Specifies the string value of the message. This field is 
	* empty if the message stores no string. 
	*
	* @author Lisa Detzler
	*/
	private String sMessage;
	
	/**
	* Specifies the boolean value of the message. This field is 
	* empty if the message stores no boolean. 
	*
	* @author Lisa Detzler
	*/
	private Boolean bMessage;
	
	/**
	* Specifies the ID of the work that sends of sent the
	* message.
	*
	* @author Lisa Detzler
	*/
	private int workId;

	/**
	* Creates a new message with the specified value.
	*
	* @param i The value of the message.
	* @param workId The ID of the work that sends or sent
	*		the message.
	* @author Lisa Detzler
	*/	
	public Message(Object i, int workId) {
		if (i instanceof Integer) {
			this.iMessage = (Integer) i;
		} else {
			if (i instanceof Boolean) {
				this.bMessage = (Boolean) i;
			} else {
				this.sMessage = (String) i;
			}
		}
		this.workId = workId;
	}

	/**
	* Creates a new message with the specified integer value.
	*
	* @param i The integer of the message.
	* @param workId The ID of the work that sends or sent
	*		the message.
	* @author Lisa Detzler
	*/	
	public Message(Integer i, int workId) {
		this.iMessage = i;
		this.workId = workId;
	}

	/**
	* Creates a new message with the specified boolean value.
	*
	* @param b The boolean value of the message.
	* @param workId The ID of the work that sends or sent
	*		the message.
	* @author Lisa Detzler
	*/	
	public Message(Boolean i, int workId) {
		this.bMessage = i;
		this.workId = workId;
	}

	/**
	* Creates a new message with the specified string value.
	*
	* @param i The string value of the message.
	* @param workId The ID of the work that sends or sent
	*		the message.
	* @author Lisa Detzler
	*/	
	public Message(String i, int workId) {
		this.sMessage = i;
		this.workId = workId;
	}

	/**
	* Returns the integer value of the message.
	*
	* @return The integer value of the message.
	*
	* @author Lisa Detzler
	*/	
	public Integer getMessage(int i) {
		return iMessage;
	}

	/**
	* Returns the boolean value of the message.
	*
	* @return The boolean value of the message.
	*
	* @author Lisa Detzler
	*/
	public String getMessage(String s) {
		return sMessage;
	}

	/**
	* Returns the string value of the message.
	*
	* @return The string value of the message.
	*
	* @author Lisa Detzler
	*/
	public Boolean getMessage(boolean b) {
		return bMessage;
	}

	/**
	* Returns the message object.
	*
	* @return The the message object.
	*
	* @author Lisa Detzler
	*/
	public Object getMessage() {
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
	* Returns the ID of the work that send or sent the message.
	*
	* @return The ID of the work that send or sent the message.
	*
	* @author Lisa Detzler
	*/
	public int getWorkId() {
		return workId;
	}

	/**
	* Sets the ID of the work that send or sent the message to 
	* the specified one.
	*
	* @param workId The new ID.
	*
	* @author Lisa Detzler
	*/
	public void setWorkId(int workId) {
		this.workId = workId;
	}
}
