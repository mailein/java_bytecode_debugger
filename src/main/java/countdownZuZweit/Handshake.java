/*******************************************************************************
* Copyright (c) 2013, Saarland University. All rights reserved.
* Lisa Detzler
******************************************************************************/

package countdownZuZweit;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

/**
* Implements message passing in shared memory. 
*
* @author Lisa Detzler
*/
public class Handshake {

	/**
	* Stores the work lists containing the works of handshake channels 
	* offered by the threads executing a select-case statement. If a 
	* thread offers handshake works it checks whether another thread
	* offers a handshake work that matches with its own work. The threads
	* offered by others threads are stored in <tt>handshakeList</tt>. If
	* no work of the thread is possible, it stores its handshake works into
	* a work list and puts it to <tt>handshakeList</tt>.
	*
	* @author Lisa Detzler
	*/
	private static HashSet<WorkList> handshakeList = new HashSet<WorkList>();
	
	/**
	* Stores all available channels.
	*
	* @author Lisa Detzler
	*/
	public static LinkedList<Channel> channels = new LinkedList<Channel>();

	/**
	* Is used to lock <tt>handshakeList</tt>.
	*
	* @author Lisa Detzler
	*/
	public static ReentrantLock lock = new ReentrantLock();

	/**
	* Start the select-case algorithm. Checks whether one of the works
	* of the specified list is possible. Doing so, the works with buffered
	* channels, the works with handshake channels and the potetially existing
	* default offer are checked. The order defining which of them is checked 
	* firstly is randomly chosen.
	*
	* @param offerList The list containing the works that represent the cases
	* 		of the executed select-case statement.
	* @return The sent or received message. <tt>null</tt> is returned if no message is 
	*		sent or received.
	*
	* @author Lisa Detzler
	*/
	public static Message handleSelect(LinkedList<Work> offerList) {

		// Distributes the offerList to the randomly chosen lists
		// bufferedOfferList and handshakeOfferedList
		WorkList handshakeOfferList = new WorkList();
		LinkedList<Work> bufferedOfferList = new LinkedList<Work>();
		Random r = new Random();
		boolean containsDefault = false;
		PseuCoThread defaultThread = null;
		Random random = new Random();
		while (offerList.size() > 0) {
			int j = random.nextInt(offerList.size());
			// for (int j = 0; j < list.size(); j++) {
			Work w = offerList.get(j);
			if (w instanceof DefaultWork) {
				if (r.nextInt(offerList.size()) == 0) {
					return new Message("", -1);
				} else {
					defaultThread = w.getThread();
					containsDefault = true;
					offerList.remove(w);
					continue;
				}
			}
			if (w.getChannel() instanceof IntHandshakeChan
					|| w.getChannel() instanceof BoolHandshakeChan
					|| w.getChannel() instanceof StringHandshakeChan) {
				handshakeOfferList.addWork(w, w.getThread());
				offerList.remove(w);
			} else {
				bufferedOfferList.add(w);
				offerList.remove(w);
			}
		}

		if(!handshakeOfferList.getWorkList().isEmpty()){
			lock.lock();
		}
		lockChannels(bufferedOfferList);
		try {// list no doesn't contain HandshakeChans now
			PseuCoThread thread = getCurrentThread();

			Random random2 = new Random();
			if (random2.nextBoolean()) {

				// proofs success with BuffChans
				for (Work w : bufferedOfferList) {
					Message m = handleOfferViaBufferedChannel(
							bufferedOfferList, handshakeOfferList, w);
					if (m != null) {
						return m;
					}
				}
				
				// proofs if there is a convenient Work in HandshakeList
				if (Handshake.handleOfferViaHandshakeChannels(
						handshakeOfferList, containsDefault, defaultThread)) {
					return thread.getMessage();
				}
			} else {				
				// proofs if there is a convenient Work in HandshakeList
				if (Handshake.handleOfferViaHandshakeChannels(
						handshakeOfferList, containsDefault, defaultThread)) {
					return thread.getMessage();
				}
				
				// proofs success with BuffChans
				for (Work w : bufferedOfferList) {
					Message m = handleOfferViaBufferedChannel(
							bufferedOfferList, handshakeOfferList, w);
					if (m != null) {
						return m;
					}
				}
			}

			while (!Thread.currentThread().isInterrupted()) {
				// waits
				synchronized (thread) {
					unlockChannels(bufferedOfferList);
					if(!handshakeOfferList.getWorkList().isEmpty()){
						lock.unlock();
					}
					try {
						thread.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(!handshakeOfferList.getWorkList().isEmpty()){
					lock.lock();
				}
				lockChannels(bufferedOfferList);

				// proofs if someone wants to synchonize
				Message m = thread.getMessage();
				if (m != null) {
					Object i = m.getMessage();
					if (i != null) {
						return m;
					}
				}

				// proofs success with BufChans
				for (Work w : bufferedOfferList) {
					Message message = handleOfferViaBufferedChannel(
							bufferedOfferList, handshakeOfferList, w);
					if (message != null) {
						return message;
					}
				}
			}
			return null;
		} finally {
			carryOutFromWaitLists(bufferedOfferList);
			for (Work w : bufferedOfferList) {
				if (w.getChannel().getLock().isHeldByCurrentThread()) {
					w.getChannel().getLock().unlock();
				}
			}
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	/**
	* Checks whether a work using a handshake channel of the specified list
	* is possible. Doing so, the algorithm checks whether there exists a work
	* in <tt>handshakeList</tt> that matches with one of the works of the list.
	* If <tt>true</tt> the threads of the two works synchronize together. If
	* <tt>false</tt> the specified work list os added to <tt>handshakeList</tt>
	*
	* @param workList The list containing the handshake works that represent the 
	* 		cases of the executed select-case statement.
	* @param containsDefault Specifies if the select-case statement contains a
	*		default work.
	* @param defaultThread The thread of the default work.
	* @return The sent or received message. <tt>null</tt> is returned if no 
	*		message is sent of received.
	* @author Lisa Detzler
	*/
	public static boolean handleOfferViaHandshakeChannels(WorkList workList,
			boolean containsDefault, PseuCoThread defaultThread) {
		if(workList.getWorkList().isEmpty()){
			return false;
		}
		LinkedList<WorkList> handshakeListCopy = new LinkedList<WorkList>();
		for (WorkList list : handshakeList) {
			handshakeListCopy.add(list);
		}

		Random r = new Random();
		int index = -1;
		while (handshakeListCopy.size() != 0) {
			index = r.nextInt(handshakeListCopy.size());
			WorkList wList = handshakeListCopy.get(index);

			for (Work w : wList.getWorkList()) {
				for (Work work : workList.getWorkList()) {
					if (w.isInput() != work.isInput
							&& w.getChannel().equals(work.channel)) {
						handshakeList.remove(wList);

						Work workContainigMessage;
						if (work.getMessage() != null) {
							workContainigMessage = work;
						} else {
							workContainigMessage = w;
						}
						w.getThread().addMessage(
								new Message(workContainigMessage.getMessage(),
										w.getWorkId()));
						work.getThread().addMessage(
								new Message(workContainigMessage.getMessage(),
										work.getWorkId()));

						synchronized (w.getThread()) {
							w.getThread().notifyAll();
						}
						return true;
					}
				}
			}
			handshakeListCopy.remove(index);

		}
		if (containsDefault) {
			defaultThread.addMessage(new Message("", -1));
			return true;
		}
		handshakeList.add(workList);
		return false;
	}

	/**
	* Checks whether the specified work using a buffered channel 
	* is possible. Doing so, the algorithm checks whether the specified work can 
	* send its value to its channel or respectively receive a value from its channel.
	* If <tt>true</tt> the thread sends or receives the value. If <tt>false</tt> 
	* the thread registers itself in the waiting list of the work channel.
	*
	* @param bufferedOfferList The list containing the buffered works that represent 
	* 		the cases of the executed select-case statement.
	* @param handshakeOfferList The list containing the handshake works that represent 
	* 		the cases of the executed select-case statement.
	* @param w The work to check.
	* @return The sent or received message. <tt>null</tt> is returned if no message is 
	*		sent or received.
	* @author Lisa Detzler
	*/
	private static Message handleOfferViaBufferedChannel(
			LinkedList<Work> bufferedOfferList, WorkList handshakeOfferList,
			Work w) {
		if (w.isInput) {

			Object i;
			if (w.getChannel() instanceof IntChannel) {
				i = ((IntChannel) w.getChannel()).selectTake(w.getThread());
			} else {
				if (w.getChannel() instanceof BoolChannel) {
					i = ((BoolChannel) w.getChannel())
							.selectTake(w.getThread());
				} else {
					i = ((StringChannel) w.getChannel()).selectTake(w
							.getThread());
				}
			}

			if (i != null) {
				carryOutFromWaitLists(bufferedOfferList);
				if (handshakeList.contains(handshakeOfferList)) {
					handshakeList.remove(handshakeOfferList);
				}
				if (i instanceof Integer) {
					return new Message((Integer) i, w.getWorkId());
				} else {
					if (i instanceof Boolean) {
						return new Message((Boolean) i, w.getWorkId());
					} else {
						return new Message((String) i, w.getWorkId());
					}
				}
			}
		} else {
			Object i;
			if (w.getChannel() instanceof IntChannel) {
				i = ((IntChannel) w.getChannel()).selectPut(w.getMessage(),
						w.getThread());
			} else {
				if (w.getChannel() instanceof BoolChannel) {
					i = ((BoolChannel) w.getChannel()).selectPut(
							w.getMessage(), w.getThread());
				} else {
					i = ((StringChannel) w.getChannel()).selectPut(
							w.getMessage(), w.getThread());
				}

			}
			if (i != null) {
				carryOutFromWaitLists(bufferedOfferList);
				if (handshakeList.contains(handshakeOfferList)) {
					handshakeList.remove(handshakeOfferList);
				}
				if (i instanceof Integer) {
					return new Message((Integer) i, w.getWorkId());
				} else {
					if (i instanceof Boolean) {
						return new Message((Boolean) i, w.getWorkId());
					} else {
						return new Message((String) i, w.getWorkId());
					}
				}
			}
		}
		return null;
	}

	/**
	* Locks all channels of the works contained in the specified list.
	*
	* @param list Contains all works whose channels has to be locks.
	* @author Lisa Detzler
	*/
	public static void lockChannels(LinkedList<Work> list) {
		for (int i = 0; i<channels.size();i++) {
			Channel c = channels.get(i);
			for (Work w : list) {
				if (w.getChannel().equals(c)) {
					c.getLock().lock();
				}
			}
		}
	}

	/**
	* Locks all channels of the works contained in the specified list.
	*
	* @param list Contains all works whose channels has to be locks.
	* @author Lisa Detzler
	*/
	public static void unlockChannels(LinkedList<Work> list) {
		for (int i = channels.size()-1; i>=0;i--) {
			Channel c = channels.get(i);
			for (Work w : list) {
				if (w.getChannel().equals(c)) {
					if (c.getLock().isHeldByCurrentThread()) {
						c.getLock().unlock();
					}
				}
			}
		}
	}

	/**
	* Removes the current executing thread from all waiting lists of
	* the work channels contained in the specified list.
	*
	* @param list Contains all works from whose channels the thread has
	*		tu unregister itself.
	* @author Lisa Detzler
	*/
	public static void carryOutFromWaitLists(LinkedList<Work> list) {
		for (Work w : list) {
			if (w.getChannel() instanceof Intchan) {
				((Intchan) w.getChannel()).removeFromWaitingThreads(w
						.getThread());
			} else {
				if (w.getChannel() instanceof Boolchan) {
					((Boolchan) w.getChannel()).removeFromWaitingThreads(w
							.getThread());
				} else {
					if (w.getChannel() instanceof Stringchan) {
						((Stringchan) w.getChannel())
								.removeFromWaitingThreads(w.getThread());
					} else {
						throw new ClassCastException();
					}
				}
			}
		}
	}

	/**
	* Returns the <tt>PseuCoThread</tt> of current executing thread.
	*
	* @return The <tt>PseuCoThread</tt> of current executing thread.
	*
	* @author Lisa Detzler
	*/
	private static PseuCoThread getCurrentThread() {
		PseuCoThread thread;
		try {
			thread = (PseuCoThread) PseuCoThread.currentThread();
		} catch (java.lang.ClassCastException e) {
			thread = Main.thread;
		}
		return thread;
	}
}