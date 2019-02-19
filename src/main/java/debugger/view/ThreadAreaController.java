package debugger.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

import debugger.Debugger;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;

public class ThreadAreaController {// TODO handle resume and suspend threadReference
	// tree > root > debuggers > threadReferences for each debugger > stackFrames
	// for each threadReference
	@FXML
	private AnchorPane anchorPane = new AnchorPane();
	private TreeView<String> tree;

	private ObservableMap<Thread, Debugger> debuggers = FXCollections.observableHashMap();
	private Debugger selectedDebugger;
	private ThreadReference selectedThread;
	private StackFrame selectedStack;

	private Map<Thread, Debugger> terminatedDebuggers = new HashMap<>();
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

		// it's safe to select stackFrame because the debuggee is suspended.
		tree.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
			if (nv.getValue().contains(debuggerNameMarker)) {
				this.selectedDebugger = String2Debugger(nv.getValue());
				this.selectedThread = null;
				this.selectedStack = null;
			}
			if (nv.getValue().contains(threadNameMarker)) {
				String debuggerString = nv.getParent().getValue();
				this.selectedDebugger = String2Debugger(debuggerString);
				this.selectedThread = String2Thread(nv.getValue());
				try {
					this.selectedStack = this.selectedThread.frame(0);
				} catch (IncompatibleThreadStateException e) {
					e.printStackTrace();
				}
			}
			if (nv.getValue().contains(stackNameMarker)) {
				String threadString = nv.getParent().getValue();
				String debuggerString = nv.getParent().getParent().getValue();
				this.selectedDebugger = String2Debugger(debuggerString);
				this.selectedThread = String2Thread(threadString);
				this.selectedStack = String2StackFrame(nv.getValue());
			}
		});
		tree.setShowRoot(false);
	}

	public void addDebugger(Debugger debugger, Thread t) {
		debuggers.put(t, debugger);
		addDebuggerToTree(debugger, t);
		System.out.println("added debugger to tree------------");
		System.out.println(debugger.getThreads().size());
	}

	public void applyTerminatedMarker(Debugger debugger, Thread t) {
		String s = generateDebuggerName(debugger, t);
		TreeItem<String> dbgTreeItem = getTreeItem(s, tree.getRoot());
		s = terminatedMarker + s;
		dbgTreeItem.setValue(s);

		terminatedDebuggers.put(t, debugger);

		// remove all treeItems under this debugger other than debuggerTreeItem
		removeDebuggerChildrenFromTree(debugger, t);
	}

	public void removeAllTerminatedDebugger() {
		terminatedDebuggers.forEach((t, debugger) -> removeDebugger(debugger, t));
		terminatedDebuggers.clear();
	}

	public void removeDebugger(Debugger debugger, Thread t) {
		debuggers.remove(t, debugger);
		removeDebuggerFromTree(debugger, t);
	}

	// only added Debugger and its threadReferences to root, stackFrame is not added
	// in this method
	private void addDebuggerToTree(Debugger debugger, Thread t) {
		TreeItem<String> dbgTreeItem = addBranch(generateDebuggerName(debugger, t), tree.getRoot());
		ObservableList<ThreadReference> threads = debugger.getThreads();
		threads.addListener((ListChangeListener.Change<? extends ThreadReference> c) -> {
			while (c.next()) {
				if (c.wasAdded()) {
					c.getAddedSubList().forEach(thread -> addBranch(generateThreadName(thread), dbgTreeItem));
				}
				if (c.wasRemoved()) {
					c.getRemoved().forEach(thread -> removeBranch(generateThreadName(thread), dbgTreeItem));
				}
			}
		});
	}

	private void removeDebuggerFromTree(Debugger debugger, Thread t) {
		TreeItem<String> dbgTreeItem = getTreeItem((terminatedMarker + generateDebuggerName(debugger, t)),
				tree.getRoot());
		tree.getRoot().getChildren().remove(dbgTreeItem);
	}

	private void removeDebuggerChildrenFromTree(Debugger debugger, Thread t) {
		TreeItem<String> dbgTreeItem = getTreeItem((terminatedMarker + generateDebuggerName(debugger, t)),
				tree.getRoot());
		dbgTreeItem.getChildren().clear();
	}

	// add threads to Debugger; add stackFrames to thread
	private TreeItem<String> addBranch(String son, TreeItem<String> parent) {
		TreeItem<String> item = new TreeItem<>();
		item.setValue(son);
		item.setExpanded(true);
		parent.getChildren().add(item);
		return item;
	}

	private void removeBranch(String son, TreeItem<String> parent) {
		List<TreeItem<String>> list = parent.getChildren().stream().filter(sonItem -> sonItem.getValue().equals(son))
				.collect(Collectors.toList());
		parent.getChildren().removeAll(list);
	}

	// called at every suspension!!
	public void addStackFrameBranch() {// TODO
//		for (Entry<Thread, Debugger> entry : debuggers.entrySet()) {
//			Thread dbgT = entry.getKey();
//			Debugger dbg = entry.getValue();
//			for (ThreadReference t : dbg.getThreads()) {
//				try {
//					for (StackFrame sf : t.frames()) {
//
//					}
//				} catch (IncompatibleThreadStateException e) {
//					e.printStackTrace();
//				}
//			}
//		}
	}

	// s is generated by Debugger/ThreadReference/StackFrame
	private TreeItem<String> getTreeItem(String s, TreeItem<String> parent) {
		for (TreeItem<String> item : parent.getChildren()) {
			if (item.getValue().equals(s))
				return item;
		}
		return null;
	}

	private Debugger String2Debugger(String debuggerString) {
		for (Entry<Thread, Debugger> entry : debuggers.entrySet()) {
			Debugger dbg = entry.getValue();
			Thread t = entry.getKey();
			String s = generateDebuggerName(dbg, t);
			if (debuggerString.equals(s) || debuggerString.equals(terminatedMarker + s))
				return dbg;
		}
		return null;
	}

	private ThreadReference String2Thread(String threadString) {
		if (this.selectedDebugger == null)
			return null;
		for (ThreadReference thread : this.selectedDebugger.getThreads()) {
			if (threadString.equals(generateThreadName(thread)))
				return thread;
		}
		return null;
	}

	private StackFrame String2StackFrame(String stackFrameString) {
		if (this.selectedDebugger == null || this.selectedThread == null)
			return null;
		try {
			for (StackFrame stackFrame : this.selectedThread.frames()) {
				if (stackFrameString.equals(generateStackFrameName(stackFrame)))
					return stackFrame;
			}
		} catch (IncompatibleThreadStateException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String generateDebuggerName(Debugger dbg, Thread t) {
		return dbg.name() + t.getId() + debuggerNameMarker;
	}

	private String generateThreadName(ThreadReference thread) {
		return threadNameMarker + thread.name() + "]";
	}

	private String generateStackFrameName(StackFrame stackFrame) {
		Location loc = stackFrame.location();
		String className = loc.declaringType().name();
		String methodSignature = loc.method().signature();
		String lineNumber = String.valueOf(loc.lineNumber());
		String bci = String.valueOf(loc.codeIndex());
		return className + "." + methodSignature + stackNameMarker + lineNumber + " bci:" + bci;
	}

	public Map<Thread, Debugger> getRunningDebuggers() {
		Map<Thread, Debugger> runningDebuggers = new HashMap<>();
		debuggers.forEach((t, dbg) -> {
			System.out.println("all debuggers: thread: " + t.getName() + ", debugger: " + dbg.name() + "classpath: " + dbg.classpath());
		});
		terminatedDebuggers.forEach((t, dbg) -> {
			System.out.println("terminated debuggers: thread: " + t.getName() + ", debugger: " + dbg.name() + "classpath: " + dbg.classpath());
		});
		debuggers.keySet().stream().filter(thread -> !terminatedDebuggers.keySet().contains(thread)).forEach(thread -> {
			runningDebuggers.put(thread, debuggers.get(thread));
		});
		runningDebuggers.forEach((t, dbg) -> {
			System.out.println("running debuggers: thread: " + t.getName() + ", debugger: " + dbg.name() + "classpath: " + dbg.classpath());
		});
		return runningDebuggers;
	}
}
