package debugger;

import com.sun.jdi.InvocationException;

import debugger.view.RootLayoutController;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;

public class Main {

	private static String mainClassArg;
	private static String sourcepathArg;
	private static String classpathArg;
	private static boolean debugModeArg;

	private static BooleanProperty newDebugger = new SimpleBooleanProperty(false);

	public static void main(String[] args) {
		newDebugger.addListener((obs, ov, nv) -> {
			if (nv) {
				GUI.getOutputAreaController().clear();
				GUI.getWatchpointAreaController().clearHistory();
				newDebug();
				newDebugger.set(false);
			}
		});
		Application.launch(debugger.GUI.class);
	}

	private static void newDebug() {
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				Debugger debugger = new Debugger(mainClassArg, sourcepathArg, classpathArg, debugModeArg);// TODO add
																											// progArg
																											// to
				Thread t = new Thread(debugger);
				GUI.getThreadAreaController().removeTerminatedDebugger();
				GUI.getThreadAreaController().addDebugger(debugger);
				t.start();
				RootLayoutController rootController = GUI.getRootLayoutController();
				rootController.disableRunOrDebug();
				rootController.enableOrDisableButtons(false);
				try {
					t.join();// without Task outside debugger Thread: GUI thread starts a debugger Thread and
								// waits for it to terminate per t.join(); That's why GUI thread is blocking!!
					rootController.enableRunOrDebug();
					rootController.enableOrDisableButtons(true);
					// clear view after each termination
					GUI.getLocalVarAreaController().clear();
					// TODO clear line indicator in CodeArea and BytecodeArea
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				GUI.getThreadAreaController().applyTerminatedMarker(debugger);
				System.out.println("debugger thread died");

				return null;
			}
		};
		Thread t = new Thread(task);
		t.setDaemon(true);
		t.start();
	}

	public static void setMainClass(String mainClass) {
		mainClassArg = mainClass;
	}

	public static void setSourcepath(String sourcepath) {
		sourcepathArg = sourcepath;
	}

	public static void setClasspath(String classpath) {
		classpathArg = classpath;
	}

	public static void setDebugMode(boolean debugMode) {
		debugModeArg = debugMode;
	}

	public static BooleanProperty getNewDebugger() {
		return newDebugger;
	}

}
