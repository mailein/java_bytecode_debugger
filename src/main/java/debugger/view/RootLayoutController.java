package debugger.view;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import debugger.Debugger;
import debugger.GUI;
import debugger.dataType.Configuration;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
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
	private Button StepReturnButton;
	@FXML
	private Button runButton;
	@FXML
	private Button debugButton;
	@FXML
	private Button resumeButton;
	@FXML
	private Button suspendButton;
	@FXML
	private Button terminateButton;

	private OverviewController overviewController;
	private CodeAreaController codeAreaController;

	private TextField selectedTextField;

	@FXML
	private void initialize() {
		this.newMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
		this.openMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
		this.saveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		this.saveAsMenuItem.setAccelerator(
				new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
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
		fileChooser.setInitialDirectory(Paths.get(System.getProperty("user.dir"), "").toFile());
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

	private void addNewConfig(Pane parent, TextArea mainclass, TextArea progArg, TextArea sourcepath, TextArea classpath) {
		TextField configName = new TextField();
		configName.setPromptText("new Configuration");
		mainclass.setText("");
		progArg.setText("");
		sourcepath.setText("");
		classpath.setText("");
		System.out.println("cleared 3 textareas");
		parent.getChildren().add(configName);
		configName.textProperty().addListener((obs, ov, nv) -> {
			if (ov.isEmpty() && !nv.isEmpty())
				addNewConfig(parent, mainclass, progArg, sourcepath, classpath);
			if (!ov.isEmpty() && nv.isEmpty()) {
				if (parent.getChildren().size() != 1)
					parent.getChildren().remove(configName);
			}
		});
		configName.focusedProperty().addListener((obs, ov, nv) -> {
			if (nv) {
				this.selectedTextField = configName;
				String name = configName.getText();
				Configuration conf = GUI.getConfigurations().get(configName.getText());
				if (conf != null) {
					mainclass.setText(conf.getMainClass());
					progArg.setText(conf.getProgArg());
					sourcepath.setText(conf.getSourcepath());
					classpath.setText(conf.getClasspath());
					System.out.println("read textareas from GUI: " + conf.getProgArg() + ", " + conf.getSourcepath()
							+ ", " + conf.getClasspath());
				}
			}
		});
	}

	@FXML
	private void handleConfigurations() {
		// splitPane right
		Label mainClassLabel = new Label("main class:");
		TextArea mainClassTextArea = new TextArea();
		mainClassTextArea.setPromptText("main class name(incl. package name)");
		Label programArgLabel = new Label("program arguments:");
		TextArea programArgTextArea = new TextArea();
		programArgTextArea.setPromptText("args for debuggee's main method");
		Label sourcepathLabel = new Label("sourcepath:");
		TextArea sourcepathTextArea = new TextArea();
		sourcepathTextArea.setPromptText("Default: the path of debugger's jar file");
		Label classpathLabel = new Label("classpath:");
		TextArea classpathTextArea = new TextArea();
		classpathTextArea.setPromptText("Default: the path of debugger's jar file");
		Button run = new Button("Run");
		Button debug = new Button("Debug");
		ButtonBar buttonbar2 = new ButtonBar();
		buttonbar2.getButtons().addAll(run, debug);
		VBox right = new VBox(5.0, mainClassLabel, mainClassTextArea, programArgLabel, programArgTextArea,
				sourcepathLabel, sourcepathTextArea, classpathLabel, classpathTextArea, buttonbar2);
		right.setPrefSize(400, 150);

		// splitPane left
		VBox left = new VBox();
		addNewConfig(left, mainClassTextArea, programArgTextArea, sourcepathTextArea, classpathTextArea);
		left.setPrefSize(50, 150);

		mainClassTextArea.textProperty().addListener((obs, ov, nv) -> {
			String name = this.selectedTextField.getText();
			if (!name.isEmpty()) {
				Configuration conf = GUI.getConfigurations().get(name);
				if (conf == null) {
					conf = new Configuration();
				}
				conf.setMainClass(nv);
				GUI.getConfigurations().put(name, conf);
				System.out.println("write mainclass to GUI: " + nv);
			}
		});
		
		programArgTextArea.textProperty().addListener((obs, ov, nv) -> {
			String name = this.selectedTextField.getText();
			if (!name.isEmpty()) {
				Configuration conf = GUI.getConfigurations().get(name);
				if (conf == null) {
					conf = new Configuration();
				}
				conf.setProgArg(nv);
				GUI.getConfigurations().put(name, conf);
				System.out.println("write progarg to GUI: " + nv);
			}
		});
		sourcepathTextArea.textProperty().addListener((obs, ov, nv) -> {
			String name = this.selectedTextField.getText();
			if (!this.selectedTextField.getText().isEmpty()) {
				Configuration conf = GUI.getConfigurations().get(name);
				if (conf == null) {
					conf = new Configuration();
				}
				conf.setSourcepath(nv);
				GUI.getConfigurations().put(name, conf);
				System.out.println("write sourcepath to GUI: " + nv);
			}
		});
		classpathTextArea.textProperty().addListener((obs, ov, nv) -> {
			String name = this.selectedTextField.getText();
			if (!this.selectedTextField.getText().isEmpty()) {
				Configuration conf = GUI.getConfigurations().get(name);
				if (conf == null) {
					conf = new Configuration();
				}
				conf.setClasspath(nv);
				GUI.getConfigurations().put(name, conf);
				System.out.println("write classpath to GUI: " + nv);
			}
		});

		SplitPane splitPane = new SplitPane(left, right);
		splitPane.setDividerPositions(0.3f, 0.7f);

		Scene scene = new Scene(splitPane);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setOnCloseRequest(e -> {
			GUI.setSourcepath(sourcepathTextArea.getText());
			GUI.setClasspath(classpathTextArea.getText());
		});
		stage.show();
	}

	@FXML
	private void handleRun() {// eventSet.resume();
		handleRunOrDebug(false);
	}

	@FXML
	private void handleDebug() {
		handleRunOrDebug(true);
	}

	// TODO classpath if N/A, then the current working dir
	// TODO classPath contains the class with main method
	// TODO mainClass comes from the file of selectedTab, but it should be the
	// mainClass of the last runConfiguration
	private void handleRunOrDebug(boolean debugMode) {
		File file = codeAreaController.getFileOfSelectedTab();
		if (file == null)
			return;
		try {
			// parameters
			String fileSourcepath = file.getCanonicalPath();
			String classpath = GUI.getClasspath();
			String mainClass = fileSourcepath.substring(fileSourcepath.indexOf(classpath) + classpath.length(),
					fileSourcepath.indexOf("."));
			System.out.println(
					"root fileSourcepath: " + fileSourcepath + " classpath: " + classpath + " mainclass: " + mainClass);
			String regex = System.getProperty("file.separator").equals("\\") ? "\\\\"
					: System.getProperty("file.separator");
			mainClass = mainClass.replaceAll(regex, ".");
			mainClass = mainClass.startsWith(".") ? mainClass.substring(1) : mainClass;

			System.out.println("root controller, mainclass: " + mainClass);

			// debugger and thread
			Debugger debugger;
			debugger = new Debugger(mainClass, classpath, debugMode);
			Thread t = new Thread(debugger);
			GUI.getThreadAreaController().addDebugger(debugger, t);
			t.start();
			t.join();
			GUI.getThreadAreaController().removeDebugger(debugger, t);
			System.out.println("debugger thread died");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setOverviewController(OverviewController overviewController) {
		this.overviewController = overviewController;
		this.codeAreaController = this.overviewController.getCodeAreaController();
	}
}
