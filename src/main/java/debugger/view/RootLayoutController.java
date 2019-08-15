package debugger.view;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;

import debugger.Debugger;
import debugger.GUI;
import debugger.Main;
import debugger.dataType.Configuration;
import debugger.misc.BytecodePrefix;
import debugger.misc.SourceClassConversion;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RootLayoutController {
	@FXML
	private MenuItem newMenuItem;
	@FXML
	private MenuItem openMenuItem;
	@FXML
	private MenuItem saveMenuItem;
	@FXML
	private MenuItem saveAsMenuItem;
	@FXML
	private MenuItem configurationsMenuItem;
	@FXML
	private MenuItem informationMenuItem;
	@FXML
	private Button newButton;
	@FXML
	private Button openButton;
	@FXML
	private Button saveButton;
	@FXML
	private Button stepIButton;
	@FXML
	private Button stepIntoButton;
	@FXML
	private Button stepOverButton;
	@FXML
	private Button stepReturnButton;
	@FXML
	private Button compileButton;
	@FXML
	private Button runButton;
	@FXML
	private Button debugButton;
	@FXML
	private Button resumeButton;
//	@FXML
//	private Button suspendButton;// TODO
	@FXML
	private Button terminateButton;

	private OverviewController overviewController;
	private CodeAreaController codeAreaController;

	private TextField selectedTextField;
	private int nameCountGen = 0;

	@FXML
	private void initialize() {
		this.newMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
		this.openMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
		this.saveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		this.saveAsMenuItem.setAccelerator(
				new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));

		this.newButton.setTooltip(new Tooltip("Ctrl+N"));
		this.openButton.setTooltip(new Tooltip("Ctrl+O"));
		this.saveButton.setTooltip(new Tooltip("Ctrl+S"));
		this.resumeButton.setTooltip(new Tooltip("F8"));
//		this.suspendButton.setTooltip(new Tooltip("F9"));
		this.terminateButton.setTooltip(new Tooltip("Ctrl+F2"));
		this.stepIButton.setTooltip(new Tooltip("F4"));
		this.stepIntoButton.setTooltip(new Tooltip("F5"));
		this.stepOverButton.setTooltip(new Tooltip("F6"));
		this.stepReturnButton.setTooltip(new Tooltip("F7"));

		enableOrDisableButtons(true);

		Platform.runLater(() -> {
			this.resumeButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F8),
					() -> this.resumeButton.fire());
//			this.suspendButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F9),
//					() -> this.suspendButton.fire());
			this.terminateButton.getScene().getAccelerators().put(
					new KeyCodeCombination(KeyCode.F2, KeyCombination.CONTROL_DOWN), () -> this.terminateButton.fire());
			this.stepIButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F4),
					() -> this.stepIButton.fire());
			this.stepIntoButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F5),
					() -> this.stepIntoButton.fire());
			this.stepOverButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F6),
					() -> this.stepOverButton.fire());
			this.stepReturnButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F7),
					() -> this.stepReturnButton.fire());
		});
	}

	public void enableOrDisableButtons(boolean toDisable) {
		this.resumeButton.setDisable(toDisable);
//		this.suspendButton.setDisable(toDisable);
		this.terminateButton.setDisable(toDisable);
		this.stepIButton.setDisable(toDisable);
		this.stepIntoButton.setDisable(toDisable);
		this.stepOverButton.setDisable(toDisable);
		this.stepReturnButton.setDisable(toDisable);
	}

	@FXML
	private void handleNew() {
		File file = new File("");
		codeAreaController.newTab(file);
	}

	@FXML
	private void handleOpen() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Java Files", "*.java"),
				new FileChooser.ExtensionFilter("All Files", "*.*"));
		fileChooser.setInitialDirectory(Paths.get(GUI.getSourcepath().get()).toFile());
		Stage stage = new Stage();
		List<File> files = fileChooser.showOpenMultipleDialog(stage);
		if (files != null) {
			for (File f : files) {
				if (!codeAreaController.hasOpened(f)) {
					codeAreaController.newTab(f);
				}
			}
		}
	}

	@FXML
	private void handleSaveAs() {
		codeAreaController.saveAsFile();
	}

	@FXML
	private void handleSave() {
		codeAreaController.saveFile();
	}

	/**
	 * @param parent
	 * @param        mainclassTextArea: value
	 * @param        progArgTextArea: value
	 * @param        sourcepathTextArea: value
	 * @param        classpathTextArea: value
	 * @param        configNameTextField: key
	 */
	private void monitorConfigName(Pane parent, TextArea mainclassTextArea, TextArea progArgTextArea,
			TextArea sourcepathTextArea, TextArea classpathTextArea, TextField configNameTextField) {
		configNameTextField.textProperty().addListener((obs, ov, nv) -> {
			if (!ov.isEmpty()) {
				if (nv.isEmpty() && parent.getChildren().size() != 2) {// because one child is button "new" and
																		// "refresh"
					parent.getChildren().remove(configNameTextField);
				}
				GUI.getConfigurations().remove(ov);
				System.out.println("MONITOR -- removed " + ov + " from GUI");
			}
			if (nv.isEmpty()) {
				mainclassTextArea.setText("");
				progArgTextArea.setText("");
				sourcepathTextArea.setText("");
				classpathTextArea.setText("");
			}
			if (!nv.isEmpty()) {
				if (!GUI.getConfigurations().containsKey(nv)) {// to ensure no overwrite
					GUI.getConfigurations().put(nv,
							new Configuration().setConfigName(nv).setMainClass(mainclassTextArea.getText())
									.setProgArg(progArgTextArea.getText()).setSourcepath(sourcepathTextArea.getText())
									.setClasspath(classpathTextArea.getText()).setShown(true));
					System.out.println("MONITOR -- write " + nv + " to GUI: " + mainclassTextArea.getText() + ", "
							+ progArgTextArea.getText() + ", " + sourcepathTextArea.getText() + ", "
							+ classpathTextArea.getText() + ", " + "true");
				}
			}
		});
		configNameTextField.focusedProperty().addListener((obs, ov, nv) -> {
			this.selectedTextField = configNameTextField;
			if (nv) {
				if (!configNameTextField.getText().isEmpty()) {
					Configuration conf = GUI.getConfigurations().get(configNameTextField.getText());
					if (conf != null) {
						mainclassTextArea.setText(conf.getMainClass());
						progArgTextArea.setText(conf.getProgArg());
						sourcepathTextArea.setText(conf.getSourcepath());
						classpathTextArea.setText(conf.getClasspath());
						System.out.println("MONITOR -- read from GUI: " + conf.getMainClass() + ", " + conf.getProgArg()
								+ ", " + conf.getSourcepath() + ", " + conf.getClasspath());
					}
				} else {
					mainclassTextArea.setText("");
					progArgTextArea.setText("");
					sourcepathTextArea.setText("");
					classpathTextArea.setText("");
				}
			}
		});
	}

	private void updateExistingConfig(Pane parent, TextArea mainclassTextArea, TextArea progArgTextArea,
			TextArea sourcepathTextArea, TextArea classpathTextArea) {
		// add existing configurations to left splitpane
		GUI.getConfigurations().forEach((name, c) -> {
			if (!c.isShown()) {
				TextField tf = new TextField(name);
				c.setShown(true);
				parent.getChildren().add(tf);
				monitorConfigName(parent, mainclassTextArea, progArgTextArea, sourcepathTextArea, classpathTextArea,
						tf);
			}
		});
	}

	private void addNewConfig(Pane parent, TextArea mainclassTextArea, TextArea progArgTextArea,
			TextArea sourcepathTextArea, TextArea classpathTextArea, String mainClass, String progArg,
			String sourcepath, String classpath) {

		// TextArea in the left splitpane
		TextField configNameTextField = new TextField();
		String configName = "";
		if (!mainClass.isEmpty())
			configName = mainClass;
		String temp = configName;
		while (GUI.getConfigurations().containsKey(configName)) {
			this.nameCountGen++;
			configName = temp + "(" + this.nameCountGen + ")";
		}
		configNameTextField.setText(configName);

		// add to view
		parent.getChildren().add(configNameTextField);
		this.selectedTextField = configNameTextField;

		// TextArea in the right splitpane
		// ATTENTION: only setText for the right splitpane after setting
		// selectedTextField
		mainclassTextArea.setText(mainClass);
		progArgTextArea.setText(progArg);
		sourcepathTextArea.setText(sourcepath);
		classpathTextArea.setText(classpath);

		// add to GUI configurations
		if (!configName.isEmpty()) {
			Configuration conf = GUI.getConfigurations().get(configName);
			if (conf == null) {
				conf = new Configuration();
			}
			GUI.getConfigurations().put(configName, conf
					.setMainClass(mainClass)
					.setProgArg(progArg)
					.setSourcepath(sourcepath)
					.setClasspath(classpath)
					.setConfigName(configName)
					.setShown(true));
			System.out.println("NEW -- write " + configName + " to GUI: " + mainClass + ", " + progArg + ", "
					+ sourcepath + ", " + classpath + ", " + "true");
		}
		// add listener for textarea in left splitpane
		monitorConfigName(parent, mainclassTextArea, progArgTextArea, sourcepathTextArea, classpathTextArea,
				configNameTextField);
	}

	@FXML
	private void handleConfigurations() {
		System.out.println("-------print config start----------");
		GUI.getConfigurations().forEach((name, c) -> System.out.println(name));
		System.out.println("=======print config end============");

		// splitPane right
		Label mainClassLabel = new Label("main class:");
		TextArea mainClassTextArea = new TextArea();
		mainClassTextArea.setPromptText(
				"If empty, the selected tab must contain main method, \n"
						+ "otherwise enter eg. dir.Main if file path is sourcepath/dir/Main.java.");
		Label programArgLabel = new Label("program arguments:");
		TextArea programArgTextArea = new TextArea();
		programArgTextArea.setPromptText("args for debuggee's main method");
		Label sourcepathLabel = new Label("sourcepath:");
		TextArea sourcepathTextArea = new TextArea();
		sourcepathTextArea.setPromptText("If empty, same as the input at debugger launch.");
		Label classpathLabel = new Label("classpath:");
		TextArea classpathTextArea = new TextArea();
		classpathTextArea.setPromptText("If empty, same as the input at debugger launch.");
		// Update path for "compile"&"run"&"debug", so run and debug buttons in
		// buttonbar will read same path
		Button compile = new Button("compile");
		compile.setOnAction(e -> {
			updateGUIpath(sourcepathTextArea, classpathTextArea);
			handleCompile();
		});
		Button run = new Button("Run");
		run.setOnAction(event -> {
			updateGUIpath(sourcepathTextArea, classpathTextArea);
			handleRunOrDebug(mainClassTextArea.getText(), "", "", false);
		});
		Button debug = new Button("Debug");
		debug.setOnAction(event -> {
			updateGUIpath(sourcepathTextArea, classpathTextArea);
			handleRunOrDebug(mainClassTextArea.getText(), "", "", true);
		});
		ButtonBar buttonbar = new ButtonBar();
		buttonbar.getButtons().addAll(compile, run, debug);
		VBox right = new VBox(5.0, mainClassLabel, mainClassTextArea, programArgLabel, programArgTextArea,
				sourcepathLabel, sourcepathTextArea, classpathLabel, classpathTextArea, buttonbar);
		right.setPrefSize(500, 200);

		// splitPane left
		VBox left = new VBox();
		Button newButton = new Button("new");
		Button refreshButton = new Button("refresh");
		HBox hbox = new HBox(5.0, newButton, refreshButton);
		left.getChildren().add(hbox);
		newButton.setOnAction(event -> addNewConfig(left, mainClassTextArea, programArgTextArea, sourcepathTextArea,
				classpathTextArea, "", "", "", ""));
		refreshButton.setOnAction(event -> updateExistingConfig(left, mainClassTextArea, programArgTextArea,
				sourcepathTextArea, classpathTextArea));
		addNewConfig(left, mainClassTextArea, programArgTextArea, sourcepathTextArea, classpathTextArea, "", "", "",
				"");
		left.setPrefSize(100, 200);

		// add listener for textarea in right splitpane
		textAreaAddListener(mainClassTextArea, textAreaType.mainClass);
		textAreaAddListener(programArgTextArea, textAreaType.progArg);
		textAreaAddListener(sourcepathTextArea, textAreaType.sourcepath);
		textAreaAddListener(classpathTextArea, textAreaType.classpath);

		SplitPane splitPane = new SplitPane(left, right);
		splitPane.setDividerPositions(0.3f, 0.7f);

		Scene scene = new Scene(splitPane);
		Stage stage = new Stage();
		stage.setTitle("Set configurations");
		stage.setScene(scene);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setOnCloseRequest(e -> {
			// Attention: configuration shouldn't tamper with GUI's sourcepath and classpath
//			GUI.setSourcepath(sourcepathTextArea.getText());
//			GUI.setClasspath(classpathTextArea.getText());
			GUI.getConfigurations().forEach((name, c) -> c.setShown(false));
		});
		stage.show();
	}

	// TODO no need for classpath(same as sourcepath)
	@FXML
	private void handleCompile() {
		ProcessBuilder processBuilder = new ProcessBuilder();
		String sourcepath = GUI.getSourcepath().get();
		String fileSourcepath = "";
		try {
			fileSourcepath = GUI.getCodeAreaController().getFileOfSelectedTab().getCanonicalPath();
		} catch (IOException e1) {
			Alert alert = new Alert(AlertType.ERROR, "Selected tab must contain main method.", ButtonType.CLOSE);
			alert.showAndWait();
			e1.printStackTrace();
			return;
		}
		String relativeFileSourcepath = SourceClassConversion.mapFileSourcepath2relativeFileSourcepath(sourcepath,
				fileSourcepath);
		String cmd = "cd \'" + sourcepath + "\';"
				+ "(javac \'" + relativeFileSourcepath + "\' || exit 100);"
				+ "if test $? -ne 100; then (for i in $(find . -name '*.class'); do tmp=$i; javap -c -l $i > ${tmp%.*}.bytecode; done || exit 2); else (exit 1); fi;";
		// TODO UTF-8
		processBuilder.command("bash", "-c", cmd);
		try {
			Process process = processBuilder.start();
			int exitVal = process.waitFor();
			if (exitVal == 0) {// TODO pop out a window
				Alert success = new Alert(AlertType.INFORMATION, "Success", ButtonType.CLOSE);
				success.showAndWait();
			} else if (exitVal == 1) {
				if (relativeFileSourcepath.isEmpty())
					relativeFileSourcepath = "<empty>";
				Alert failure = new Alert(AlertType.INFORMATION,
						"Failure for javac! POSSIBLE wrong settings:"
								+ "\nSourcepath: " + sourcepath
								+ "\nFile with main method: " + relativeFileSourcepath
								+ "\n etc.",
						ButtonType.CLOSE);
				failure.showAndWait();
			} else if (exitVal == 2) {
				Alert failure = new Alert(AlertType.INFORMATION,
						"Failure for javap, but it's unlikely. Please contact developer.\n",
						ButtonType.CLOSE);
				failure.showAndWait();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void updateGUIpath(TextArea sourcepathTextArea, TextArea classpathTextArea) {
		String sourcepath = sourcepathTextArea.getText();
		if (!sourcepath.isEmpty())
			GUI.setSourcepath(sourcepath);
		String classpath = classpathTextArea.getText();
		if (!classpath.isEmpty())
			GUI.setClasspath(classpath);
	}

	@FXML
	private void handleInformation() {
		Label label = new Label("For more infomation:\n"
				+ "https://en.wikipedia.org/wiki/Java_bytecode_instruction_listings\n"
				+ "Java Virtual Machine Specification Chapter 6");

		TableView<BytecodePrefix> table = new TableView<>();
		TableColumn<BytecodePrefix, String> prefixCol = new TableColumn<>("Prefix/suffix");
		prefixCol.setCellValueFactory(new PropertyValueFactory<>("prefix"));
		prefixCol.setMinWidth(50);
		TableColumn<BytecodePrefix, String> operandTypeCol = new TableColumn<>("Operand type");
		operandTypeCol.setCellValueFactory(new PropertyValueFactory<>("operandType"));
		operandTypeCol.setMinWidth(50);
		table.setItems(FXCollections.observableList(BytecodePrefix.getData()));
		table.setPrefHeight(220);
		table.getColumns().addAll(prefixCol, operandTypeCol);

		Label example = new Label("Instructions fall into a number of broad groups:\n"
				+ "Load and store (e.g. aload_0, istore)\n"
				+ "Arithmetic and logic (e.g. ladd, fcmpl)\n"
				+ "Type conversion (e.g. i2b, d2i)\n"
				+ "Object creation and manipulation (new, putfield)\n"
				+ "Operand stack management (e.g. swap, dup2)\n"
				+ "Control transfer (e.g. ifeq, goto)\n"
				+ "Method invocation and return (e.g. invokespecial, areturn)");

		VBox vbox = new VBox(5.0, label, table, example);
		vbox.setPadding(new Insets(5.0));
		ScrollPane scrollPane = new ScrollPane(vbox);
		Scene scene = new Scene(scrollPane);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.show();
	}

	private enum textAreaType {
		mainClass, progArg, sourcepath, classpath
	};

	private void textAreaAddListener(TextArea programArgTextArea, textAreaType type) {
		programArgTextArea.textProperty().addListener((obs, ov, nv) -> {
			String name = this.selectedTextField.getText();
			if (!name.isEmpty()) {
				Configuration conf = GUI.getConfigurations().get(name);
				if (conf == null) {
					conf = new Configuration();
				}
				Configuration value = null;
				switch (type) {
				case mainClass:
					value = conf.setMainClass(nv);
					break;
				case progArg:
					value = conf.setProgArg(nv);
					break;
				case sourcepath:
					value = conf.setSourcepath(nv);
					break;
				case classpath:
					value = conf.setClasspath(nv);
					break;
				default:
					break;
				}
				GUI.getConfigurations().put(name, value);
				System.out.println("write " + type + " to GUI: " + nv);
			}
		});
	}

	@FXML
	private void handleRun() {// eventSet.resume();
		handleRunOrDebug("", "", "", false);
	}

	@FXML
	private void handleDebug() {
		handleRunOrDebug("", "", "", true);
	}

	public void enableRunOrDebug() {
		runButton.setDisable(false);
		debugButton.setDisable(false);
	}

	public void disableRunOrDebug() {
		runButton.setDisable(true);
		debugButton.setDisable(true);
	}

	// TODO if the class of currently selected tab doesn't contain main method, then
	// mainClass should come from last runConfiguration
	private void handleRunOrDebug(String mainClass, String sourcepath, String classpath, boolean debugMode) {
		try {
			if (sourcepath.isEmpty()) {
				sourcepath = GUI.getSourcepath().get();
			} else {
				GUI.setSourcepath(sourcepath);
			}
			if (classpath.isEmpty()) {
				classpath = GUI.getClasspath().get();
			} else {
				GUI.setClasspath(classpath);
			}
			if (mainClass.isEmpty()) {
				mainClass = extractMainClass(sourcepath);
				if (mainClass.isEmpty())
					return;
			}
			System.out.println(
					"root mainclass: " + mainClass + ", sourcepath: " + sourcepath + ", classpath: " + classpath);
			// create new configuration, add it to GUI's configurations
			Configuration config = new Configuration()
					.setConfigName(mainClass).setMainClass(mainClass).setProgArg("")
					.setSourcepath(sourcepath).setClasspath(classpath).setShown(false);
			GUI.getConfigurations().put(mainClass, config);

			// debugger and thread
			Main.setMainClass(mainClass);
			Main.setSourcepath(sourcepath);
			Main.setClasspath(classpath);
			Main.setDebugMode(debugMode);
			Main.getNewDebugger().set(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String extractMainClass(String sourcepath) {
		File file = codeAreaController.getFileOfSelectedTab();
		if (file == null || !file.isFile()) {
			System.out.println("I'm assuming selected tab's file has main method. But file error.");
			return "";
		}
		// parameters
		String fileSourcepath = "";
		try {
			fileSourcepath = file.getCanonicalPath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("root fileSourcepath: " + fileSourcepath);
		String mainClass = SourceClassConversion.mapFileSourcepath2ClassName(Paths.get(sourcepath),
				Paths.get(fileSourcepath));
		return mainClass;
	}

	@FXML
	private void handleResume() {
		Debugger currentDebugger = GUI.getThreadAreaController().getRunningDebugger();
		ThreadReference selectedThread = GUI.getThreadAreaController().getSelectedThread();
		if (selectedThread == null) {
			if (GUI.getThreadAreaController().isDebuggerselected()) {// all threads resume
				ObservableList<ThreadReference> threads = currentDebugger.getThreads();
				threads.forEach(thread -> {
					if(thread.isSuspended()) {
						currentDebugger.setSuspendCount(thread, 1);
					}
				});
				currentDebugger.getVm().resume();
			} else {// wrong

			}
		} else {// selected thread resume
			if (selectedThread.isSuspended()) {
				currentDebugger.setSuspendCount(selectedThread, 0);
			} else {// do nothing
					// TODO deactivate resume button ==> activate for other situations
			}
		}
	}

	@FXML
	private void handleTerminate() {
		Debugger currentDebugger = GUI.getThreadAreaController().getRunningDebugger();
		currentDebugger.terminate();
	}

	class StepCommand {
		private EventRequestManager eventReqMgr;
		private ThreadReference thread;
		private int size;
		private int depth;

		public StepCommand(Debugger debugger, ThreadReference thread, int size, int depth) {
			this.eventReqMgr = debugger.getEventRequestManager();
			this.thread = thread;
			this.size = size;
			this.depth = depth;
		}

		public void execute() {
			// delete step request of current thread
			List<StepRequest> stepRequests = eventReqMgr.stepRequests();
			for (StepRequest s : stepRequests) {
				if (s.thread().equals(thread)) {
					eventReqMgr.deleteEventRequest(s);
					break;
				}
			}

			System.out.println(thread.name() + " sets a new step request");
			StepRequest stepRequest = eventReqMgr.createStepRequest(thread, size, depth);
			stepRequest.addCountFilter(1);
			stepRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			stepRequest.enable();
		}
	}

	@FXML
	private void handleStepi() {
		handleSomeStep(StepRequest.STEP_MIN, StepRequest.STEP_INTO);
	}

	@FXML
	private void handleStepInto() {
		handleSomeStep(StepRequest.STEP_LINE, StepRequest.STEP_INTO);
	}

	@FXML
	private void handleStepOver() {
		handleSomeStep(StepRequest.STEP_LINE, StepRequest.STEP_OVER);
	}

	@FXML
	private void handleStepReturn() {
		handleSomeStep(StepRequest.STEP_LINE, StepRequest.STEP_OUT);
	}

	private void handleSomeStep(int size, int depth) {
		ThreadAreaController threadAreaController = GUI.getThreadAreaController();
		Debugger currentDebugger = threadAreaController.getRunningDebugger();
		ThreadReference currentThread = threadAreaController.getSelectedThread();
		// NO TODO popFrames before step(Into/Over/Return), enabling stepping based on
		// selectedFrame
//		StackFrame prevSf = GUI.getThreadAreaController().getPrevStackFrame();
//		if(prevSf != null) {
//			try {
//				currentThread.popFrames(prevSf);
//			} catch (Exception e) {//bug: com.sun.jdi.InvalidStackFrameException: Thread has been resumed
//				e.printStackTrace();
//			}
//		}
		if(currentThread.isSuspended()) {
			StepCommand stepi = new StepCommand(currentDebugger, currentThread, size, depth);
			stepi.execute();
			currentDebugger.setSuspendCount(currentThread, 0);
		}
	}

	public void setOverviewController(OverviewController overviewController) {
		this.overviewController = overviewController;
		this.codeAreaController = this.overviewController.getCodeAreaController();
	}
}
