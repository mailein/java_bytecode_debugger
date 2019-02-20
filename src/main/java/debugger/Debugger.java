package debugger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
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
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ThreadStartRequest;

import debugger.dataType.HistoryRecord;
import debugger.dataType.LineBreakpoint;
import debugger.misc.SourceClassConversion;
import debugger.view.BreakpointAreaController;
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

	private Map<String, List<HistoryRecord>> VarTable = new HashMap<>();// <fieldName, {thread, read/write, value}>

	private String mainClassName;
	private String sourcepath;
	private String classpath;
	private boolean debugMode;

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
		Thread outThread = new Thread(new IOThread(process.getInputStream(), System.out));
		outThread.setDaemon(true);
		outThread.start();
		Thread errThread = new Thread(new IOThread(process.getErrorStream(), System.out));
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

		if (debugMode) {
			eventLoop();
		}

		process.destroy();
	}

	private void eventLoop() throws Exception {
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
					execute(event);

//					//TODO localVar
					List<ThreadReference> threads = mainThread.threadGroup().threads();
					threads.forEach(thread -> {
					});
				}
			}
		}
	}

	private void execute(Event event) throws Exception {
		if (event instanceof VMStartEvent) {
			mainThread = ((VMStartEvent) event).thread(); // get mainThread of targetVM
			System.out.println("--------\nVMStartEvent");
			eventSet.resume();
		} else if (event instanceof ThreadStartEvent) {
			ThreadReference thread = ((ThreadStartEvent) event).thread();
			if (mainThread.threadGroup().equals(thread.threadGroup())) {
				threads.add(thread);
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

				// for the situation: add breakpoints BEFORE debuggers launch
				// 0. get all breakpoints
				BreakpointAreaController bpController = GUI.getBreakpointAreaController();
				ObservableList<LineBreakpoint> lineBreakpoints = bpController.getBreakpoints();
				// 1. get className from lineBreakpoints
				List<LineBreakpoint> matchingLinebp = lineBreakpoints.stream().filter(linebp -> {
					String bpClassName = bpController.getClassName(linebp, this);
					return (!bpClassName.isEmpty() && bpClassName.equals(className));
				}).collect(Collectors.toList());

				matchingLinebp.forEach(b -> {
					System.out.println("matching Line breakpoint at line: " + b.getLineNumber());
				});

				// 2. if !className.isEmpty() && same with the className of this
				// ClassPrepareEvent, check if already requested by checking if line#
				// same with any bpReq's locations' line#
				List<LineBreakpoint> notRequestedMatchingLinebp = new ArrayList<>();
				if (matchingLinebp != null && !matchingLinebp.isEmpty()) {
					List<BreakpointRequest> bpReqs = eventRequestManager.breakpointRequests();
					for (LineBreakpoint mLinebp : matchingLinebp) {
						boolean requested = false;
						for (BreakpointRequest bReq : bpReqs) {
							if (mLinebp.getLineNumber() == bReq.location().lineNumber()) {
								requested = true;
								break;// must break loop here
							}
						}
						if (!requested) {
							notRequestedMatchingLinebp.add(mLinebp);
						}
					}
				}
				// 3. if not requested, then create bpReq and update info in lineBreakpoint
				notRequestedMatchingLinebp.forEach(nRmLinebp -> {
					try {
						List<Location> locations = classRefType.locationsOfLine(nRmLinebp.getLineNumber());
						if (locations != null && !locations.isEmpty()) {
							BreakpointRequest bpReq = eventRequestManager.createBreakpointRequest(locations.get(0));
							bpReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
							bpReq.enable();
							nRmLinebp.setDebugger(this);
							nRmLinebp.setReferenceType(classRefType);
							System.out.println("added breakpoint in classRefType " + classRefType + " at line "
									+ nRmLinebp.getLineNumber());
						}
					} catch (AbsentInformationException e) {
						e.printStackTrace();
					}
				});
			}
			eventSet.resume();
		} else if (event instanceof ClassUnloadEvent) {
			ClassUnloadEvent classUnloadEvent = (ClassUnloadEvent) event;
			String className = classUnloadEvent.className();
			// no need to filter, because classes are inside Debugger
			classes.remove(className);
			System.out.println("--------\n" + "className: " + className + " is unloaded.");
			eventSet.resume();
		} else if (event instanceof BreakpointEvent) {// switch thread, breakpointReq, stepiReq
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
			System.out.println("--------\nBreakpointEvent" + "\n(" + thread.name() + ")" + "\n|line: " + lineNumber
					+ "\n|bci: " + bci + "\n|_");
			// TODO resume controlled by GUI/ controller
		} else if (event instanceof StepEvent) {// switch thread, breakpointReq, stepiReq
			StepEvent stepEvent = (StepEvent) event;
			ThreadReference thread = stepEvent.thread();
			Location location = stepEvent.location();
			int lineNumber = location.lineNumber();
			long bci = location.codeIndex();
			Method method = location.method();
			if (lineNumber == -1 || bci == -1) {
				System.out.println("Problem: stepi into a native method.");
				return;
			}
			ReferenceType classRefType = method.declaringType();
			if (classRefType == null) {
				return;
			}
			System.out.println("--------\nStepEvent" + "\n(" + thread.name() + ")" + "\n|line: " + lineNumber
					+ "\n|bci: " + bci + "\n|_");
			// TODO resume controlled by GUI/ controller
		} else if (event instanceof AccessWatchpointEvent) {
			AccessWatchpointEvent accessWatchpointEvent = (AccessWatchpointEvent) event;
			ThreadReference thread = accessWatchpointEvent.thread();
			Field f = accessWatchpointEvent.field();
			Value v = accessWatchpointEvent.valueCurrent();
			Location location = accessWatchpointEvent.location();
			int line = location.lineNumber();
			long bci = location.codeIndex();
			HistoryRecord record = new HistoryRecord(thread, false, v, null, line, bci);
			List<HistoryRecord> history = VarTable.get(f.name());
			if (history == null) {
				history = new ArrayList<>();
			}
			history.add(record);
			VarTable.put(f.name(), history);
			eventSet.resume();
		} else if (event instanceof ModificationWatchpointEvent) {
			ModificationWatchpointEvent modificationWatchpointEvent = (ModificationWatchpointEvent) event;
			ThreadReference thread = modificationWatchpointEvent.thread();
			Field f = modificationWatchpointEvent.field();
			Value currV = modificationWatchpointEvent.valueCurrent();
			Value vToBe = modificationWatchpointEvent.valueToBe();
			Location location = modificationWatchpointEvent.location();
			int line = location.lineNumber();
			long bci = location.codeIndex();
			HistoryRecord record = new HistoryRecord(thread, true, currV, vToBe, line, bci);
			List<HistoryRecord> history = VarTable.get(f.name());
			if (history == null) {
				history = new ArrayList<>();
			}
			history.add(record);
			VarTable.put(f.name(), history);
			eventSet.resume();
		} else {
			eventSet.resume();
		}
	}

	private void printHistoryEntries(String name) {
		List<HistoryRecord> history = VarTable.get(name);
		if (history != null) {
			System.out.println("History for " + name + ":");
			for (HistoryRecord record : history) {
				System.out.println("|" + record.toString());
			}
			System.out.println("|_");
		} else {
			System.out.println("Error. No watchpoint set for " + name);
		}
	}

	private List<StackFrame> getStackFrames(ThreadReference thread) {
		try {
			List<StackFrame> stackFrames = thread.frames();
			return stackFrames;
		} catch (IncompatibleThreadStateException e) {// if the thread is not suspended in the target VM
			return null;
		}
	}

	private List<LocalVariable> getLocalVar(StackFrame stackFrame) {
//		StackFrame stackFrame = thread.frame(0);//TODO when to check local variables
		try {
			List<LocalVariable> localVariables = stackFrame.visibleVariables();
			return localVariables;
//			localVariables.forEach(var -> Value value = stackFrame.getValue(var));
//		int breakpointLoop = ((IntegerValue) breakpointValueLoop).intValue();
		} catch (AbsentInformationException e) {
			return null;
		}
	}

	public String name() {
		return mainClassName;
	}

	public String sourcepath() {
		return sourcepath;
	}

	public String classpath() {
		return classpath;
	}

//	public VirtualMachine getVm() {
//		return vm;
//	}

	public EventRequestManager getEventRequestManager() {
		return eventRequestManager;
	}

	public ObservableList<ThreadReference> getThreads() {
		return threads;
	}

	public ObservableMap<String, ReferenceType> getClasses() {
		return classes;
	}

	public List<ReferenceType> getAnonymousClasses(String startingWithClassName) {
		List<ReferenceType> anonymousClasses = new ArrayList<>();
		classes.forEach((className, refType) -> {
			if (className.startsWith(startingWithClassName) && !className.equals(startingWithClassName))
				anonymousClasses.add(refType);
		});
		return anonymousClasses;
	}

//	// check if breakpoint of this fileSourcepath and lineNumber has been requested,
//	// despite enabled or disabled
//	public boolean existsLineBreakpoint(String fileSourcepath, int lineNumber) {
//		List<BreakpointRequest> bpReq = eventRequestManager.breakpointRequests();
//		return bpReq.stream().anyMatch(req -> {
//			try {
//				return (SourceClassConversion
//						.getFileSourcepath(Paths.get(this.sourcepath), Paths.get(req.location().sourcePath()))
//						.equals(Paths.get(fileSourcepath)) && req.location().lineNumber() == lineNumber);
//			} catch (AbsentInformationException e) {
//				e.printStackTrace();
//				return false;
//			}
//		});
//	}

	@Override
	public void run() {
		try {
			debug(mainClassName, classpath, debugMode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}