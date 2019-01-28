/*******************************************************************************
* Copyright (c) 2013, Saarland University. All rights reserved.
* Lisa Detzler
******************************************************************************/

package countdownZuZweit;

/**
* Represents PseuCo's buffered <tt>boolchan</tt>.
*
* @author Lisa Detzler
*/
public abstract class BoolChannel extends Channel{

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
	abstract public Boolean handleSelect(Work w) ;
	
	/**
	*  Puts the specified expression to the channel queue.
	*
	* @param exp The expression to send over the channel.
	* @param thread The thread sending the specified expression.
	* @author Lisa Detzler
	*/
	abstract public Boolean selectPut(Object exp, PseuCoThread thread);
	
	/**
	*  Takes an message from the channel queue.
	*
	* @param thread The thread that receives the message.
	* @return The received expression.
	* @author Lisa Detzler
	*/
	abstract public Boolean selectTake(PseuCoThread thread);

}
