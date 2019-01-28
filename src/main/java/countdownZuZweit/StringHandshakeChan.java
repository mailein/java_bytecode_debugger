/*******************************************************************************
* Copyright (c) 2013, Saarland University. All rights reserved.
* Lisa Detzler
******************************************************************************/

package countdownZuZweit;

import java.util.LinkedList;

public class StringHandshakeChan extends StringChannel {

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

	/**
	* @author Lisa Detzler
	*/
	@Override
	public String selectPut(Object exp, PseuCoThread thread) {
		return null;
	}
	
	/**
	* @author Lisa Detzler
	*/
	@Override
	public String selectTake(PseuCoThread thread) {
		return null;
	}

}