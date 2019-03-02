package debugger.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

import com.sun.jdi.ThreadReference;

import debugger.Debugger;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class ThreadAreaController {// TODO handle resume and suspend threadReference
	// tree > root > debuggers > threadReferences for each debugger > stackFrames
	// for each threadReference
	@FXML
	private AnchorPane anchorPane = new AnchorPane();
	private TreeView<String> tree;
	TreeItem<String> debuggerTreeItem;
	private Debugger debugger;
	private ThreadReference selectedThread;
//	private StackFrame selectedStack;

	private boolean terminated = false;
	private String terminatedMarker = "<terminated>";
	private String debuggerNameMarker = "[Java Application]";
	private String threadNameMarker = "Thread[";
//	private String stackNameMarker = " line:";

	@FXML
	private void initialize() {
		TreeItem<String> root = new TreeItem<>();
		root.setExpanded(true);
		tree = new TreeView<String>(root);
		anchorPane.getChildren().add(tree);
		tree.prefWidthProperty().bind(anchorPane.widthProperty());// fit tree's size to parent anchorpane
		tree.prefHeightProperty().bind(anchorPane.heightProperty());

		tree.setOnMouseClicked(e -> toggleThread(e));

		// it's safe to select stackFrame because the debuggee is suspended.
		tree.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
			if (nv.getValue().contains(debuggerNameMarker)) {
				tree.getSelectionModel().selectNext();
			}
			if (nv.getValue().contains(threadNameMarker)) {
				this.selectedThread = String2Thread(nv.getValue());
//				try {
//					this.selectedStack = this.selectedThread.frame(0);
//				} catch (IncompatibleThreadStateException e) {
//					e.printStackTrace();
//				}
			}
//			if (nv.getValue().contains(stackNameMarker)) {
//				String threadString = nv.getParent().getValue();
//				String debuggerString = nv.getParent().getParent().getValue();
//				this.selectedDebugger = String2Debugger(debuggerString);
//				this.selectedThread = String2Thread(threadString);
//				this.selectedStack = String2StackFrame(nv.getValue());
//			}
		});
		tree.setShowRoot(false);
	}

	public void addDebugger(Debugger debugger) {
		this.debugger = debugger;
		addDebuggerToTree(debugger);
		System.out.println("added debugger to tree------------");
		System.out.println(debugger.getThreads().size());
	}

	public void applyTerminatedMarker(Debugger debugger) {
		String s = generateDebuggerName(debugger);
		TreeItem<String> dbgTreeItem = getTreeItem(s, tree.getRoot());
		s = terminatedMarker + s;
		dbgTreeItem.setValue(s);

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
							TreeItem<String> threadTreeItem = addBranch(generateThreadName(thread), debuggerTreeItem);
							threadTreeItem.setGraphic(getPause());
							this.selectedThread = thread;
							tree.getSelectionModel().select(threadTreeItem);
						}
					});
				}
				if (c.wasRemoved()) {
					c.getRemoved().forEach(thread -> {
						removeBranch(generateThreadName(thread), debuggerTreeItem);
						if (this.selectedThread.equals(thread)) {
							this.selectedThread = null;
						}
					});
				}
			}
		});
	}

	private void toggleThread(MouseEvent e) {
		if (e.getClickCount() == 2) {
			ImageView node;
			if (!debugger.getSuspendedThreads().contains(selectedThread)) {// playing -> pause
				debugger.getSuspendedThreads().add(selectedThread);
				node = getPlay();
			} else {// paused -> play
				debugger.getSuspendedThreads().remove(selectedThread);
				node = getPause();
			}
			
			TreeItem<String> threadTreeItem = getTreeItem(generateThreadName(selectedThread), debuggerTreeItem);
			threadTreeItem.setGraphic(node);
		}
	}

	private void removeDebuggerFromTree(Debugger debugger) {
		TreeItem<String> dbgTreeItem = getTreeItem((terminatedMarker + generateDebuggerName(debugger)), tree.getRoot());
		tree.getRoot().getChildren().remove(dbgTreeItem);
	}

	private void removeDebuggerChildrenFromTree(Debugger debugger) {
		TreeItem<String> dbgTreeItem = getTreeItem((terminatedMarker + generateDebuggerName(debugger)), tree.getRoot());
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
//	public void addStackFrameBranch() {// TODO
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
//	}

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

//	private StackFrame String2StackFrame(String stackFrameString) {
//		if (this.selectedDebugger == null || this.selectedThread == null)
//			return null;
//		try {
//			for (StackFrame stackFrame : this.selectedThread.frames()) {
//				if (stackFrameString.equals(generateStackFrameName(stackFrame)))
//					return stackFrame;
//			}
//		} catch (IncompatibleThreadStateException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

	private String generateDebuggerName(Debugger dbg) {
		return dbg.name() + debuggerNameMarker;
	}

	private String generateThreadName(ThreadReference thread) {
		return threadNameMarker + thread.name() + "]";
	}

//	private String generateStackFrameName(StackFrame stackFrame) {
//		Location loc = stackFrame.location();
//		String className = loc.declaringType().name();
//		String methodSignature = loc.method().signature();
//		String lineNumber = String.valueOf(loc.lineNumber());
//		String bci = String.valueOf(loc.codeIndex());
//		return className + "." + methodSignature + stackNameMarker + lineNumber + " bci:" + bci;
//	}

	public Debugger getRunningDebugger() {
		if (!terminated)
			return debugger;
		return null;
	}

	public ThreadReference getSelectedThread() {
		return selectedThread;
	}

	// https://www.iconfinder.com/icons/3855622/pause_play_icon
	// https://www.iconfinder.com/icons/3855607/parallel_pause_icon
	private ImageView getPause() {
		try {
			return new ImageView(
					new Image(new FileInputStream(new File("src/main/resources/debugger/view/pause.png"))));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	private ImageView getPlay() {
		try {
			return new ImageView(new Image(new FileInputStream(new File("src/main/resources/debugger/view/play.png"))));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

//	public StackFrame getSelectedStack() {
//		return selectedStack;
//	}
}
