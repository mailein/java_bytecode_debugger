package debugger;

import java.io.IOException;
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
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ThreadStartRequest;

import debugger.dataType.HistoryRecord;
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
	private ObservableMap<Path, ReferenceType> classes = FXCollections.observableHashMap();	//<fileClasspath, refType>
	
	private Map<String, List<HistoryRecord>> VarTable = new HashMap<>();// <fieldName, {thread, read/write, value}>

	private String mainClass;
	private String classPath;
	private boolean debugMode;

	public Debugger(String mainClass, String classPath, boolean debugMode) {
		this.mainClass = mainClass;
		this.classPath = classPath;
		this.debugMode = debugMode;
	}

	private void debug(String mainClass, String classPath, boolean debugMode)
			throws IOException, IllegalConnectorArgumentsException, VMStartException, Exception {
		System.out.println("in debug: mainclass: " + mainClass + " classpath: " + classPath);
		
		LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();

		// get arg(home, options, main, suspended, quote, vmexec) of launching connector
		Map<String, Connector.Argument> defaultArguments = launchingConnector.defaultArguments();
		Connector.Argument mainArg = defaultArguments.get("main");
		Connector.Argument optionsArg = defaultArguments.get("options");
		Connector.Argument suspendArg = defaultArguments.get("suspend");
		mainArg.setValue(mainClass);
		optionsArg.setValue("-cp " + classPath);
		suspendArg.setValue("true");

		vm = launchingConnector.launch(defaultArguments);
		process = vm.process();
//		System.setOut(new PrintStream(process.getOutputStream(), true));
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
			// TODO inject Breakpoints
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
				System.out.println("++++++++++++++" + event.toString());
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
			if(mainThread.threadGroup().equals(thread.threadGroup())) {
				threads.add(thread);
			}
			eventSet.resume();
		} else if (event instanceof ClassPrepareEvent) {// TODO assume the first prepared class contains mainMethod
			ClassPrepareEvent classPrepareEvent = (ClassPrepareEvent) event;
			// get the referenceType of this class "Main" in this case
			ReferenceType classRefType = classPrepareEvent.referenceType();
			String className = classRefType.name();
			Path fileClasspath = refTypeOnClasspath(classRefType, Paths.get(classPath));
			if(fileClasspath != null) {
				classes.put(fileClasspath, classRefType);
				System.out.println("--------\n" + "Class " + className + " is already prepared.");
			}
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
			//TODO resume controlled by GUI/ controller
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
			//TODO resume controlled by GUI/ controller
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
		}else {
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

	public List<Location> getLocationsOfLineInClass(ReferenceType refType, int lineNumber) {
		List<Location> locations = null;
		try {
			locations = refType.locationsOfLine(lineNumber);
		} catch (AbsentInformationException e) {
			e.printStackTrace();
		}
		return locations;
	}

	private Path refTypeOnClasspath(ReferenceType refType, Path classpath) {
		Path path = classpath;
		String name = refType.name();
		for (String s : name.split("\\.")) {
			path = path.resolve(s);
		}
		path = Paths.get(path.toString() + ".class");
		if(Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			return path;
		}else {
			return null;
		}
	}
	
	public String name() {
		return (mainClass + " [Java Application]");
	}
	
	public VirtualMachine getVm() {
		return vm;
	}

	public EventRequestManager getEventRequestManager() {
		return eventRequestManager;
	}

	public ObservableList<ThreadReference> getThreads() {
		return threads;
	}

	public ObservableMap<Path, ReferenceType> getClasses() {
		return classes;
	}

	@Override
	public void run() {
		try {
			debug(mainClass, classPath, debugMode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}