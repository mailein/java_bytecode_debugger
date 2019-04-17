package debugger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.request.ThreadStartRequest;

import debugger.dataType.HistoryRecord;
import debugger.dataType.LineBreakpoint;
import debugger.dataType.Watchpoint;
import debugger.misc.SourceClassConversion;
import debugger.misc.SuspensionLocation;
import debugger.view.BreakpointAreaController;
import debugger.view.BytecodeAreaController;
import debugger.view.WatchpointAreaController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class Debugger implements Runnable {

	private VirtualMachine vm;
	private Process process;
	private EventRequestManager eventRequestManager;
	private EventQueue eventQueue;
	private EventSet eventSet;
	private boolean vmExit = false;

	private ThreadReference mainThread;
	private ObservableList<ThreadReference> threads = FXCollections.observableArrayList();
	private ObservableMap<String, ReferenceType> classes = FXCollections.observableHashMap(); // <complete className,
																								// refType>
//	private Map<String, List<HistoryRecord>> VarTable = new HashMap<>();// <fieldName, {thread, read/write, value}>
	private List<ThreadReference> suspendedThreads = new ArrayList<>();
	private Map<ThreadReference, Event> currentEvent = new HashMap<>();// thread: actually no need, because global
																		// variables
																		// doesn't keep old value for different events

	private String mainClassName;
	private String sourcepath;
	private String classpath;
	private boolean debugMode;

	private String bytecodepath;

	/**
	 * @param mainClass  is complete name, eg. countdownZuZweit.Main
	 * @param sourcepath
	 * @param classpath
	 * @param debugMode
	 */
	public Debugger(String mainClass, String sourcepath, String classpath, boolean debugMode) {
		this.mainClassName = mainClass;
		this.sourcepath = sourcepath;
		this.classpath = classpath;
		this.bytecodepath = classpath;
		this.debugMode = debugMode;
	}

	// TODO ask for permission, ref:
	// https://github.com/jfager/jdiscript/blob/f3768f29d3042d35ee71ba1a80c176d93fae3de5/src/main/java/org/jdiscript/util/VMLauncher.java
	// https://github.com/jfager/jdiscript/blob/f3768f29d3042d35ee71ba1a80c176d93fae3de5/src/main/java/org/jdiscript/util/StreamRedirectThread.java
	class IOThread implements Runnable {
		private BufferedReader in;
		private PrintStream out;

		public IOThread(InputStream input, OutputStream output) {
			this.in = new BufferedReader(new InputStreamReader(input));
			this.out = new PrintStream(output);
		}

		@Override
		public void run() {
			String s = "";
			while (s != null) {
				out.println(s);
				try {
					s = in.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void debug(String mainClassName, String classPath, boolean debugMode)
			throws IOException, IllegalConnectorArgumentsException, VMStartException, Exception {
		System.out.println("in debug: mainclass: " + mainClassName + " classpath: " + classPath);

		LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();

		// get arg(home, options, main, suspended, quote, vmexec) of launching connector
		Map<String, Connector.Argument> defaultArguments = launchingConnector.defaultArguments();
		Connector.Argument mainArg = defaultArguments.get("main");
		Connector.Argument optionsArg = defaultArguments.get("options");
		Connector.Argument suspendArg = defaultArguments.get("suspend");
		mainArg.setValue(mainClassName);
		optionsArg.setValue("-cp " + classPath);
		suspendArg.setValue("true");

		vm = launchingConnector.launch(defaultArguments);
		process = vm.process();

		// redirect debuggee's IO
		PrintStream ps = new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				Platform.runLater(() -> {
					byte[] bb = { (byte) (b & 0xff) };
					GUI.getOutputAreaController().append(new String(bb, StandardCharsets.UTF_8));
				});
			}
		}, true);
		Thread outThread = new Thread(new IOThread(process.getInputStream(), ps));
		outThread.setDaemon(true);
		outThread.start();
		Thread errThread = new Thread(new IOThread(process.getErrorStream(), ps));
		errThread.setDaemon(true);
		errThread.start();

		eventRequestManager = vm.eventRequestManager();

		ClassPrepareRequest classPrepareRequest = eventRequestManager.createClassPrepareRequest();
//		classPrepareRequest.addClassFilter(classPattern);
//		classPrepareRequest.addCountFilter(1);//no need, so that all class fitting classPattern can have a ClassPrepareEvent
		classPrepareRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
		classPrepareRequest.enable();

		ThreadStartRequest threadStartRequest = eventRequestManager.createThreadStartRequest();
		threadStartRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
		threadStartRequest.enable();

//		if (debugMode) {//no output after hitting Button "Run" w/o eventLoop
		eventLoop(debugMode);
//		}

		process.destroy();
	}

	private void eventLoop(boolean debugMode) throws Exception {
		eventQueue = vm.eventQueue();
		while (!vmExit) {
			eventSet = eventQueue.remove();
			EventIterator eventIterator = eventSet.eventIterator();
			while (eventIterator.hasNext()) {
				Event event = eventIterator.next();
//				System.out.println("++++++++++++++" + event.toString());
				if (event instanceof VMDisconnectEvent || event instanceof VMDeathEvent) {
					System.out.println("--------\n" + "VM disconnected or dead");
					vmExit = true;
				} else {
					execute(event, debugMode);

//					//TODO localVar
//					List<ThreadReference> threads = mainThread.threadGroup().threads();
//					threads.forEach(thread -> {
//					});
				}
			}
		}
	}

	private void execute(Event event, boolean debugMode) throws Exception {
		if (event instanceof VMStartEvent) {
			mainThread = ((VMStartEvent) event).thread(); // get mainThread of targetVM
			System.out.println("--------\nVMStartEvent");
			eventSet.resume();
		} else if (event instanceof ThreadStartEvent) {
			ThreadReference thread = ((ThreadStartEvent) event).thread();
			if (mainThread.threadGroup().equals(thread.threadGroup())) {
				Platform.runLater(() -> threads.add(thread));
			}
			eventSet.resume();
		} else if (event instanceof ClassPrepareEvent) {
			ClassPrepareEvent classPrepareEvent = (ClassPrepareEvent) event;
			ReferenceType classRefType = classPrepareEvent.referenceType();
			String className = classRefType.name();
			// filter only those classes on classpath
			Path fileClasspath = SourceClassConversion.mapClassName2FileClasspath(className, Paths.get(classpath));
			if (Files.exists(fileClasspath, LinkOption.NOFOLLOW_LINKS)) {
				classes.put(className, classRefType);

				System.out.println("--------\nClassPrepareEvent\nclassName: " + className 
						+ "\nmethods: "	+ classRefType.methods());

				// request breakpoints
				addSetLineBreakpointsToDebugger(classRefType, className);

				// request watchpoints
				requestWatchpoints(classRefType);// add watchpoints before launching debugger to enable R/W history.
			}
			eventSet.resume();
		} else if (event instanceof ClassUnloadEvent) {
			ClassUnloadEvent classUnloadEvent = (ClassUnloadEvent) event;
			String className = classUnloadEvent.className();
			// no need to filter, because classes are inside Debugger
			classes.remove(className);
			System.out.println("--------\n" + "className: " + className + " is unloaded.");
			eventSet.resume();
		} else if (event instanceof BreakpointEvent && debugMode) {// switch thread, breakpointReq, stepiReq
			BreakpointEvent breakpointEvent = (BreakpointEvent) event;
			ThreadReference thread = breakpointEvent.thread();
			Location location = breakpointEvent.location();
			int lineNumber = location.lineNumber();
			long bci = location.codeIndex();
			Method method = location.method();
			ReferenceType classRefType = method.declaringType();
			if (classRefType == null) {
				return;
			}
			System.out.println("--------\nBreakpointEvent" + "\n(" + thread.name() + ")\n|" + method.name()
					+ "\n|line: " + lineNumber + "\n|bci: " + bci + "\n|_");

			try {
				List<LocalVariable> locals = method.variables();
			} catch (AbsentInformationException e) {
				System.out.println("AbsentInformationException in bp event, no local var for this method" + method.name());
			}
			
			// finds also anonymous class
			String fileClasspath = SuspensionLocation.inProject(location, Paths.get(classpath), false);
			if (!fileClasspath.isEmpty()) {
				BytecodeAreaController bytecodeAreaController = GUI.getBytecodeAreaController();
				Platform.runLater(() -> {
					bytecodeAreaController.openFile(fileClasspath, method, lineNumber, bci);
				});
			}

			// for line indicator
			Platform.runLater(() -> GUI.getCodeAreaController().setCurrLine(lineNumber));
			// refresh stackFrames
			GUI.getThreadAreaController().updateStackFrameBranches(thread);
			// request watchpoints
			requestWatchpoints(classRefType);
			// refresh watchpoint, localVar
			this.currentEvent.put(thread, breakpointEvent);
			GUI.getWatchpointAreaController().evalAll();
			GUI.getLocalVarAreaController().refresh();
			// resume controlled by GUI/ controller
			vm.suspend();
			eventSet.resume();
		} else if (event instanceof StepEvent) {// switch thread, breakpointReq, stepiReq
			StepEvent stepEvent = (StepEvent) event;
			ThreadReference thread = stepEvent.thread();
			Location location = stepEvent.location();
			int lineNumber = location.lineNumber();
			long bci = location.codeIndex();
			Method method = location.method();
			if (lineNumber == -1 || bci == -1) {
				System.out.println("Problem: step into a native method.");
				return;
			}
			ReferenceType classRefType = method.declaringType();
			if (classRefType == null) {
				return;
			}
			System.out.println("--------\nStepEvent" + "\n(" + thread.name() + ")\n|" + method.name() + "\n|line: "
					+ lineNumber + "\n|bci: " + bci + "\n|_");

			try {
				List<LocalVariable> locals = method.variables();
			} catch (AbsentInformationException e) {
				System.out.println("AbsentInformationException in step event, no local var for this method" + method.name());
			}
			
			// finds also anonymous class
			String fileClasspath = SuspensionLocation.inProject(location, Paths.get(classpath), false);
			if (!fileClasspath.isEmpty()) {
				BytecodeAreaController bytecodeAreaController = GUI.getBytecodeAreaController();
				Platform.runLater(() -> {
					bytecodeAreaController.openFile(fileClasspath, method, lineNumber, bci);
				});
			}

			// for line indicator
			// TODO sometimes no indicator
			Platform.runLater(() -> GUI.getCodeAreaController().setCurrLine(lineNumber));
			// refresh stackFrames
			GUI.getThreadAreaController().updateStackFrameBranches(thread);
			// request watchpoints
			requestWatchpoints(classRefType);
			// refresh watchpoint, localVar
			this.currentEvent.put(thread, stepEvent);
			GUI.getWatchpointAreaController().evalAll();
			GUI.getLocalVarAreaController().refresh();
			// resume controlled by GUI/ controller
			vm.suspend();
			eventSet.resume();
		} else if (event instanceof AccessWatchpointEvent) {
			AccessWatchpointEvent accessWatchpointEvent = (AccessWatchpointEvent) event;
			ThreadReference thread = accessWatchpointEvent.thread();
			Field f = accessWatchpointEvent.field();
			Value v = accessWatchpointEvent.valueCurrent();
			Location location = accessWatchpointEvent.location();
			ReferenceType refType = location.declaringType();
			Method method = location.method();
			int line = location.lineNumber();
			long bci = location.codeIndex();
			HistoryRecord record = new HistoryRecord(refType.name(), method.name(), thread, false, v, null, line, bci);
			ObservableList<Watchpoint> watchpoints = GUI.getWatchpointAreaController().getWatchpoints();
			int index = watchpoints.indexOf(new Watchpoint(f.name()));
			Watchpoint wp = watchpoints.get(index);
			wp.addHistoryRecord(record);
			eventSet.resume();
		} else if (event instanceof ModificationWatchpointEvent) {
			ModificationWatchpointEvent modificationWatchpointEvent = (ModificationWatchpointEvent) event;
			ThreadReference thread = modificationWatchpointEvent.thread();
			Field f = modificationWatchpointEvent.field();
			Value currV = modificationWatchpointEvent.valueCurrent();
			Value vToBe = modificationWatchpointEvent.valueToBe();
			Location location = modificationWatchpointEvent.location();
			ReferenceType refType = location.declaringType();
			Method method = location.method();
			int line = location.lineNumber();
			long bci = location.codeIndex();
			HistoryRecord record = new HistoryRecord(refType.name(), method.name(), thread, true, currV, vToBe, line,
					bci);
			ObservableList<Watchpoint> watchpoints = GUI.getWatchpointAreaController().getWatchpoints();
			int index = watchpoints.indexOf(new Watchpoint(f.name()));
			Watchpoint wp = watchpoints.get(index);
			wp.addHistoryRecord(record);
			eventSet.resume();
		} else {
			eventSet.resume();
		}
	}

	private void requestWatchpoints(ReferenceType refType) {
		WatchpointAreaController wpController = GUI.getWatchpointAreaController();
		ObservableList<Watchpoint> watchpoints = wpController.getWatchpoints();
		watchpoints.forEach(wp -> {
			if(!wp.getRequested()) {
				String withoutFieldName = wp.stripOffFieldName();
				String fieldName = wp.strip2fieldName();
				if ((!withoutFieldName.equals("") && refType.name().endsWith(withoutFieldName))
						|| withoutFieldName.equals("")) {
					Field field = refType.fieldByName(fieldName);
					if (field != null) {
						AccessWatchpointRequest accessRequest;
						if (vm.canWatchFieldAccess()) {
							accessRequest = eventRequestManager.createAccessWatchpointRequest(field);
//							accessRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
							accessRequest.enable();
						}
						ModificationWatchpointRequest modificationRequest;
						if (vm.canWatchFieldModification()) {
							modificationRequest = eventRequestManager.createModificationWatchpointRequest(field);
//							modificationRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
							modificationRequest.enable();
						}
						wp.setRequested(true);
					}
				}
			}
		});
	}

	private void addSetLineBreakpointsToDebugger(ReferenceType classRefType, String className) {
		// for the situation: add breakpoints BEFORE debuggers launch
		// 0. get all breakpoints
		BreakpointAreaController bpController = GUI.getBreakpointAreaController();
		ObservableList<LineBreakpoint> lineBreakpoints = bpController.getBreakpoints();

		// 1. get lineBreakpoints of this className && contains linebp's lineNumber
		// 1.a. normal className 1.b. anonymous className
		Map<LineBreakpoint, Location> matchingLinebp = new HashMap<>();
		for (LineBreakpoint linebp : lineBreakpoints) {
			String bpClassName = bpController.getClassName(linebp, this);
			if (!bpClassName.isEmpty() && (bpClassName.equals(className)
					|| (className.startsWith(bpClassName) && className.contains("$")))) {
				try {
					List<Location> locations = classRefType.locationsOfLine(linebp.getLineNumber());
					if (locations != null && !locations.isEmpty())
						matchingLinebp.put(linebp, locations.get(0));
				} catch (AbsentInformationException e) {
					e.printStackTrace();
				}
			}
		}
		matchingLinebp.forEach((b, l) -> {
			System.out.println("matching LineBreakpoint at line: " + b.getLineNumber());
		});

		// 2. if !className.isEmpty() && matches normal className or anonymous
		// className, check if already requested by checking if same location
		Map<LineBreakpoint, Location> notRequestedMatchingLinebp = new HashMap<>();
		if (matchingLinebp != null && !matchingLinebp.isEmpty()) {
			List<BreakpointRequest> bpReqs = eventRequestManager.breakpointRequests();
			for (Map.Entry<LineBreakpoint, Location> entry : matchingLinebp.entrySet()) {
				LineBreakpoint mLinebp = entry.getKey();
				Location loc = entry.getValue();
				boolean requested = false;
				for (BreakpointRequest bReq : bpReqs) {
					if (bReq.location().equals(loc)) {
						requested = true;
						break;// must break loop here
					}
				}
				if (!requested) {
					notRequestedMatchingLinebp.put(mLinebp, loc);
				}
			}
		}

		// 3. if not requested, then create bpReq and update info in lineBreakpoint
		notRequestedMatchingLinebp.forEach((nRmLinebp, loc) -> {
			// request
			BreakpointRequest bpReq = eventRequestManager.createBreakpointRequest(loc);
			bpReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
			// for the situation: set hitCount BEFORE debuggers launch
			if (!nRmLinebp.getHitCount().isEmpty()) {
				try {
					int count = Integer.parseUnsignedInt(nRmLinebp.getHitCount());
					bpReq.addCountFilter(count);
				} catch (NumberFormatException exception) {
					// no count filter
				}
			}
			bpReq.enable();
			nRmLinebp.setBreakpointRequest(bpReq);
			System.out.println(
					"added breakpoint in classRefType " + classRefType + " at line " + nRmLinebp.getLineNumber());
			// update nRmlinebp
			String[] sourceName = { "" };
			try {
				sourceName[0] = loc.sourceName();
			} catch (AbsentInformationException e) {
				e.printStackTrace();
			}
			sourceName[0] = sourceName[0].substring(0, sourceName[0].indexOf(".java"));
			String methodSignature = loc.method().signature();
			// Using Platform.runLater to update content on javafx thread
			Platform.runLater(
					() -> nRmLinebp.updateInfo(eventRequestManager, classRefType, loc, sourceName[0], methodSignature));

		});
	}

//	private void printHistoryEntries(String name) {
//		List<HistoryRecord> history = VarTable.get(name);
//		if (history != null) {
//			System.out.println("History for " + name + ":");
//			for (HistoryRecord record : history) {
//				System.out.println("|" + record.toString());
//			}
//			System.out.println("|_");
//		} else {
//			System.out.println("Error. No watchpoint set for " + name);
//		}
//	}
//
//	private List<StackFrame> getStackFrames(ThreadReference thread) {
//		try {
//			List<StackFrame> stackFrames = thread.frames();
//			return stackFrames;
//		} catch (IncompatibleThreadStateException e) {// if the thread is not suspended in the target VM
//			return null;
//		}
//	}
//
//	private List<LocalVariable> getLocalVar(StackFrame stackFrame) {
////		StackFrame stackFrame = thread.frame(0);//TODO when to check local variables
//		try {
//			List<LocalVariable> localVariables = stackFrame.visibleVariables();
//			return localVariables;
////			localVariables.forEach(var -> Value value = stackFrame.getValue(var));
////		int breakpointLoop = ((IntegerValue) breakpointValueLoop).intValue();
//		} catch (AbsentInformationException e) {
//			return null;
//		}
//	}

	public String name() {
		return mainClassName;
	}

	public String sourcepath() {
		return sourcepath;
	}

	public String classpath() {
		return classpath;
	}

	public EventRequestManager getEventRequestManager() {
		return eventRequestManager;
	}

	public ObservableList<ThreadReference> getThreads() {
		return threads;
	}

	public ObservableMap<String, ReferenceType> getClasses() {
		return classes;
	}

	public ThreadReference getMainThread() {
		return mainThread;
	}

	public Map<ThreadReference, Event> getCurrentEvent() {
		return currentEvent;
	}

	public List<String> generateAllValidNames(Field field) {
		List<String> names = new ArrayList<>();
		String fieldString = field.toString();
		String fieldName = field.name();
		names.add(fieldName);
		if (!fieldString.equals(fieldName)) {
			String[] splits = fieldString.split("\\.");
			String name = fieldName;
			for (int i = splits.length - 2; i >= 0; i--) {
				name = splits[i] + "." + name;
				names.add(name);
			}
		}
		return names;
	}

	public List<ReferenceType> getLoadedAnonymousClasses(String startingWithClassName) {
		List<ReferenceType> anonymousClasses = new ArrayList<>();
		classes.forEach((className, refType) -> {
			if (className.startsWith(startingWithClassName) && !className.contains("$"))
				anonymousClasses.add(refType);
		});
		return anonymousClasses;
	}

	/**
	 * @param thread
	 * @param        idealSuspendCount: 2 is to suspend, 1 resume
	 */
	private void setSuspendCount(ThreadReference thread, int idealSuspendCount) {
		int count = thread.suspendCount();
		if (count == idealSuspendCount)
			return;
		while (thread.suspendCount() < idealSuspendCount)
			thread.suspend();
		while (thread.suspendCount() > idealSuspendCount)
			thread.resume();
	}

	public void resume() {
		threads.forEach(thread -> {
			if (suspendedThreads.contains(thread)) {
				setSuspendCount(thread, 2);// suspend thread
			} else {
				setSuspendCount(thread, 1);// resume thread
			}
		});
		vm.resume();
	}

	public void terminate() {
		vmExit = true;
		vm.resume();
	}

	public List<ThreadReference> getSuspendedThreads() {
		return suspendedThreads;
	}

	@Override
	public void run() {
		try {
			debug(mainClassName, classpath, debugMode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}