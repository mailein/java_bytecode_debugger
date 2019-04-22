package debugger.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

import debugger.Debugger;
import debugger.GUI;
import debugger.misc.SuspensionLocation;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class ThreadAreaController {
	// tree > root > debugger > threadReferences > stackFrames per threadReference
	@FXML
	private AnchorPane anchorPane;
	private TreeView<String> tree;
	TreeItem<String> debuggerTreeItem;
	private Debugger debugger;
	Map<ThreadReference, TreeItem<String>> threadsTreeItems = new HashMap<>();
	Map<ThreadReference, Map<TreeItem<String>, StackFrame>> stackFramesTreeItems = new HashMap<>();
	private ThreadReference selectedThread;
	private boolean isDebuggerselected = false;
//	private StackFrame selectedStackFrame;
//	private StackFrame prevStackFrame;//for popFrames

	// to make sure thread has been added to threadsTreeItems,
	// so that threadsTreeItems.get(thread) won't be null
	private Lock threadsTreeItemsLock = new ReentrantLock();
	private Condition addedThread = threadsTreeItemsLock.newCondition();

	private boolean terminated = false;
	private String terminatedMarker = "<terminated>";
	private String debuggerNameMarker = "[Java Application]";
	private String threadNameMarker = "Thread[";
	private String stackNameMarker = " line:";

	@FXML
	private void initialize() {
		TreeItem<String> root = new TreeItem<>();
		root.setExpanded(true);
		tree = new TreeView<String>(root);
		anchorPane.getChildren().add(tree);

		tree.prefWidthProperty().bind(anchorPane.widthProperty());// fit tree's size to parent anchorpane
		tree.prefHeightProperty().bind(anchorPane.heightProperty());

//		tree.setOnMouseClicked(e -> toggleThread(e));

		// it's safe to select stackFrame because the debuggee is suspended.
		tree.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
			if (!terminated && debugger != null && nv != null) {
				if (nv.getValue().contains(debuggerNameMarker)) {
					this.isDebuggerselected = true;
//					tree.getSelectionModel().selectNext();
				}
				if (nv.getValue().contains(threadNameMarker)) {
					this.isDebuggerselected = false;
					this.selectedThread = String2Thread(nv.getValue());
					GUI.getWatchpointAreaController().evalAll();//
//					this.prevStackFrame = null;//current frame is top frame
				}
				if (nv.getValue().contains(stackNameMarker)) {
					this.isDebuggerselected = false;
					String threadString = nv.getParent().getValue();
					this.selectedThread = String2Thread(threadString);
					GUI.getWatchpointAreaController().evalAll();
//					TreeItem<String> prevFrame = nv.previousSibling();
//					if(prevFrame == null) {
//						this.prevStackFrame = null;
//						System.out.println("at top frame");
//					}else {
//						this.prevStackFrame = stackFramesTreeItems.get(selectedThread).get(prevFrame);
//						System.out.println("prev frame is" + prevFrame.getValue());
//					}
				}
			}
		});
		tree.setShowRoot(false);
	}

	public void addDebugger(Debugger debugger) {
		this.debugger = debugger;
		addDebuggerToTree(debugger);
		System.out.println("added debugger to tree------------");
	}

	public void applyTerminatedMarker(Debugger debugger) {
		String s = terminatedMarker + generateDebuggerName(debugger);
		debuggerTreeItem.setValue(s);

		terminated = true;// can not remove this debugger yet

		// remove all treeItems under this debugger other than debuggerTreeItem
		removeDebuggerChildrenFromTree(debugger);
	}

	public void removeTerminatedDebugger() {
		if (terminated)
			removeDebugger(debugger);
	}

	public void removeDebugger(Debugger debugger) {
		removeDebuggerFromTree(debugger);
		this.debugger = null;
		this.terminated = false;
	}

	// only added Debugger and its threadReferences to root, stackFrame is not added
	// in this method
	private void addDebuggerToTree(Debugger debugger) {
		// debugger
		debuggerTreeItem = addBranch(generateDebuggerName(debugger), tree.getRoot());
		// threads
		ObservableList<ThreadReference> threads = debugger.getThreads();
		threads.addListener((ListChangeListener.Change<? extends ThreadReference> c) -> {
			while (c.next()) {
				if (c.wasAdded()) {
					c.getAddedSubList().forEach(thread -> {
						if (!terminated) {
							String name = generateThreadName(thread);
							TreeItem<String> threadTreeItem = addBranch(name, debuggerTreeItem);
							threadTreeItem.setGraphic(getPauseIcon());
							tree.getSelectionModel().select(threadTreeItem);
							try {
								threadsTreeItemsLock.lock();// on GUI thread now
								threadsTreeItems.put(thread, threadTreeItem);
								addedThread.signal();
							} finally {
								threadsTreeItemsLock.unlock();
							}
						}
					});
				}
				if (c.wasRemoved()) {
					c.getRemoved().forEach(thread -> {
						String name = generateThreadName(thread);
						removeBranch(name, debuggerTreeItem);
						if (this.selectedThread.equals(thread)) {
							tree.getSelectionModel().selectPrevious();
						}
						try {
							threadsTreeItemsLock.lock();// on GUI thread now
							threadsTreeItems.remove(thread);
						} finally {
							threadsTreeItemsLock.unlock();
						}
					});
				}
			}
		});
	}

	private void removeDebuggerFromTree(Debugger debugger) {
		tree.getRoot().getChildren().remove(debuggerTreeItem);
	}

	private void removeDebuggerChildrenFromTree(Debugger debugger) {
		debuggerTreeItem.getChildren().clear();
	}

	// add threads to Debugger; add stackFrames to thread
	private TreeItem<String> addBranch(String self, TreeItem<String> parent) {
		TreeItem<String> item = new TreeItem<>();
		item.setValue(self);
		item.setExpanded(true);
		parent.getChildren().add(item);
		return item;
	}

	private void removeBranch(String son, TreeItem<String> parent) {
		List<TreeItem<String>> list = parent.getChildren().stream().filter(sonItem -> sonItem.getValue().equals(son))
				.collect(Collectors.toList());
		parent.getChildren().removeAll(list);
	}

	/**
	 * @param thread of the Breakpoint/Step Event, which is suspended right now
	 */
	public void updateStackFrameBranches(ThreadReference thread) {
		// remove all old stackFrames for this thread
		TreeItem<String> threadTreeItem = null;
		try {
			threadsTreeItemsLock.lock();// on Debugger thread now
			while (!threadsTreeItems.containsKey(thread)) {
				addedThread.await();
			}
			threadTreeItem = threadsTreeItems.get(thread);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			threadsTreeItemsLock.unlock();
		}
		System.out.println("contains thread " + thread.name() + ": " + threadsTreeItems.containsKey(thread));
		threadTreeItem.getChildren().clear();// view
		stackFramesTreeItems.remove(thread);// data
		// add all current stackFrames for this thread
		Map<TreeItem<String>, StackFrame> map = new HashMap<>();
		stackFramesTreeItems.put(thread, map);// data
		StackFrame topFrame = null;
		try {
			topFrame = thread.frame(0);
			for (StackFrame sf : thread.frames()) {
				String stackFrameName = generateStackFrameName(sf);
				TreeItem<String> stackFrameTreeItem = addBranch(stackFrameName, threadTreeItem);// view
				map.put(stackFrameTreeItem, sf);// data
			}
		} catch (IncompatibleThreadStateException e) {
			e.printStackTrace();
		}
		// top frame's *.java open in selectedTab?
		Location loc = topFrame.location();
		System.out.println("before goto location, thread: " + thread.name());
		SuspensionLocation.gotoLocationFile(loc);
	}

	// s is generated by Debugger/ThreadReference/StackFrame
	private TreeItem<String> getTreeItem(String s, TreeItem<String> parent) {
		for (TreeItem<String> item : parent.getChildren()) {
			if (item.getValue().equals(s))
				return item;
		}
		return null;
	}

	private ThreadReference String2Thread(String threadString) {
		if (this.debugger == null || terminated)
			return null;
		for (ThreadReference thread : this.debugger.getThreads()) {
			if (threadString.equals(generateThreadName(thread)))
				return thread;
		}
		return null;
	}

	private String generateDebuggerName(Debugger dbg) {
		return dbg.name() + debuggerNameMarker;
	}

	private String generateThreadName(ThreadReference thread) {
		return threadNameMarker + thread.name() + "]";
	}

	// not the same as eclipse's version, but mine should be easier to find *.class
	private String generateStackFrameName(StackFrame stackFrame) {
		Location loc = stackFrame.location();
		String className = loc.declaringType().name();
		String methodName = loc.method().name();
		String[] argNames = { "" };
		loc.method().argumentTypeNames().forEach(argName -> argNames[0] = argNames[0] + argName + ", ");
		if (argNames[0].endsWith(", "))
			argNames[0] = argNames[0].substring(0, argNames[0].lastIndexOf(", "));
		String lineNumber = String.valueOf(loc.lineNumber());
		String bci = String.valueOf(loc.codeIndex());
		return className + "." + methodName + "(" + argNames[0] + ")" + stackNameMarker + lineNumber + " bci:" + bci;
	}

//	private void toggleThread(MouseEvent e) {
//		if (e.getClickCount() == 2) {
//			ImageView node;
//			if (!debugger.getSuspendedThreads().contains(selectedThread)) {// playing -> pause
//				debugger.getSuspendedThreads().add(selectedThread);
//				node = getPlayIcon();
//			} else {// paused -> play
//				debugger.getSuspendedThreads().remove(selectedThread);
//				node = getPauseIcon();
//			}
//
//			TreeItem<String> threadTreeItem = getTreeItem(generateThreadName(selectedThread), debuggerTreeItem);
//			threadTreeItem.setGraphic(node);
//		}
//	}

	public Debugger getRunningDebugger() {
		if (!terminated)
			return debugger;
		return null;
	}

	public boolean isDebuggerselected() {
		return isDebuggerselected;
	}

	public ThreadReference getSelectedThread() {
		return selectedThread;
	}

	// https://www.iconfinder.com/icons/3855622/pause_play_icon
	// https://www.iconfinder.com/icons/3855607/parallel_pause_icon
	private ImageView getPauseIcon() {
		try {
			return new ImageView(
					new Image(new FileInputStream(new File("src/main/resources/debugger/view/pause.png"))));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	private ImageView getPlayIcon() {
		try {
			return new ImageView(new Image(new FileInputStream(new File("src/main/resources/debugger/view/play.png"))));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
//
//	public StackFrame getPrevStackFrame() {
//		return prevStackFrame;
//	}
}
