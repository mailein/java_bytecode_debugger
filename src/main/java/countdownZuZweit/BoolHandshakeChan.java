/*******************************************************************************
* Copyright (c) 2013, Saarland University. All rights reserved.
* Lisa Detzler
******************************************************************************/

package countdownZuZweit;

import java.util.LinkedList;

/**
* Represents PseuCo's handshake channel <tt>boolchan</tt>.
*
* @author Lisa Detzler
*/
public class BoolHandshakeChan extends BoolChannel {

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
	public Boolean handleSelect(Work w) {
		LinkedList<Work> list = new LinkedList<Work>();
		list.add(w);
		return Handshake.handleSelect(list).getMessage(false);
	}

	/**
	* @author Lisa Detzler
	*/
	@Override
	public Boolean selectPut(Object exp, PseuCoThread thread) {
		return null;
	}

	/**
	* @author Lisa Detzler
	*/
	@Override
	public Boolean selectTake(PseuCoThread thread) {
		return null;
	}

}