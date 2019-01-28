/*******************************************************************************
* Copyright (c) 2013, Saarland University. All rights reserved.
* Lisa Detzler
******************************************************************************/

package countdownZuZweit;

import java.util.LinkedList;
import java.util.Random;

/**
* Represents a simulator for a hardware interrupt.
*
* @author Lisa Detzler
*/ 
public class Simulate {

	/**
	* Simulates a random hardware interrupt.
	*
	* @author Lisa Detzler
	*/
	public static void HWInterrupt() {
		if (Math.random() < 0.8){
			try{
				Thread.sleep(5);
			}catch(InterruptedException e){
			}			
			Thread.currentThread().yield();
		}
	}
}
