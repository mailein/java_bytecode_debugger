/*******************************************************************************
* Copyright (c) 2013, Saarland University. All rights reserved.
* Lisa Detzler
******************************************************************************/

package countdownZuZweit;

import java.util.LinkedList;

/**
* Represents a list of <tt>Work</tt> objects representing the cases of  
* the select-case statement executed by a particular thread.
*
* @author Lisa Detzler
*/
public class WorkList {

	/**
	* Stores the <tt>Work</tt> objects representing the cases of the 
	* select-case statement executed by particular thread.
	*
	* @author Lisa Detzler
	*/
	private LinkedList<Work> workList = new LinkedList<Work>();

	/**
	* Specifies the thread executing the works of this work list.
	*
	* @author Lisa Detzler
	*/
	private PseuCoThread relatedThread;
	
	/**
	* Adds the specified work of the specified thread to the work list.
	*
	* @param w The work to add.
	* @param thread The thread of the work to add.
	* @author Lisa Detzler
	*/
	public void addWork(Work w,PseuCoThread thread){
		relatedThread=thread;
		workList.add(w);
	}
	
	/**
	* Returns the work list.
	*
	* @return The work list.
	* @author Lisa Detzler
	*/
	public LinkedList<Work> getWorkList(){
		return workList;
	}

	/**
	* Returns the thread of the works stored in the work list.
	*
	* @return Returns the thread of the works stored in the work list.
	* @author Lisa Detzler
	*/
	public PseuCoThread getRelatedThread() {
		return relatedThread;
	}
}

