package debugger;
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
			if(nv) {
				newDebug();
			}
		});
		Application.launch(debugger.GUI.class);
//		Thread t = new Thread(() -> Application.launch(debugger.GUI.class));
//		t.start();
//		Application.launch(JavaKeywordsDemo.class);
	}

	private static void newDebug() {
		//should use Task instead of Thread, so it will run background
		//and this thread wait won't cause UI thread to block!!!
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				Debugger debugger = new Debugger(mainClassArg, sourcepathArg, classpathArg, debugModeArg);// TODO add progArg to
				Thread t = new Thread(debugger);
				GUI.getThreadAreaController().removeAllTerminatedDebugger();
				GUI.getThreadAreaController().addDebugger(debugger, t);
				t.start();
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				GUI.getThreadAreaController().applyTerminatedMarker(debugger, t);
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
