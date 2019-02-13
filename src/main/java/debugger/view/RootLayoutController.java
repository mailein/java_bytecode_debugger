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
	private int nameCountGen = 0;

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
			GUI.getConfigurations().put(configName, conf.setMainClass(mainClass).setProgArg(progArg)
					.setSourcepath(sourcepath).setClasspath(classpath).setConfigName(configName).setShown(true));
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
		run.setOnMouseClicked(event -> handleRunOrDebug(mainClassTextArea.getText(), classpathTextArea.getText(), false));
		Button debug = new Button("Debug");
		debug.setOnMouseClicked(event -> handleRunOrDebug(mainClassTextArea.getText(), classpathTextArea.getText(), true));
		ButtonBar buttonbar = new ButtonBar();
		buttonbar.getButtons().addAll(run, debug);
		VBox right = new VBox(5.0, mainClassLabel, mainClassTextArea, programArgLabel, programArgTextArea,
				sourcepathLabel, sourcepathTextArea, classpathLabel, classpathTextArea, buttonbar);
		right.setPrefSize(400, 150);

		// splitPane left
		VBox left = new VBox();
		Button newButton = new Button("new");
		Button refreshButton = new Button("refresh");
		HBox hbox = new HBox(5.0, newButton, refreshButton);
		left.getChildren().add(hbox);
		newButton.setOnMouseClicked(event -> addNewConfig(left, mainClassTextArea, programArgTextArea,
				sourcepathTextArea, classpathTextArea, "", "", "", ""));
		refreshButton.setOnMouseClicked(event -> updateExistingConfig(left, mainClassTextArea, programArgTextArea,
				sourcepathTextArea, classpathTextArea));
		addNewConfig(left, mainClassTextArea, programArgTextArea, sourcepathTextArea, classpathTextArea, "", "", "",
				"");
		left.setPrefSize(50, 150);

		// add listener for textarea in right splitpane
		textAreaAddListener(mainClassTextArea, textAreaType.mainClass);
		textAreaAddListener(programArgTextArea, textAreaType.progArg);
		textAreaAddListener(sourcepathTextArea, textAreaType.sourcepath);
		textAreaAddListener(classpathTextArea, textAreaType.classpath);

		SplitPane splitPane = new SplitPane(left, right);
		splitPane.setDividerPositions(0.3f, 0.7f);

		Scene scene = new Scene(splitPane);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setOnCloseRequest(e -> {
			GUI.setSourcepath(sourcepathTextArea.getText());
			GUI.setClasspath(classpathTextArea.getText());
			GUI.getConfigurations().forEach((name, c) -> c.setShown(false));
		});
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
		handleRunOrDebug("", "", false);
	}

	@FXML
	private void handleDebug() {
		handleRunOrDebug("", "", true);
	}

	// TODO classpath if N/A, then the current working dir
	// TODO classPath contains the class with main method
	// TODO mainClass comes from the file of selectedTab, but it should be the
	// mainClass of the last runConfiguration
	private void handleRunOrDebug(String mainClass, String classpath, boolean debugMode) {
		try {
			if(mainClass.isEmpty() || classpath.isEmpty()) {
				File file = codeAreaController.getFileOfSelectedTab();
				if (file == null)
					return;
				// parameters
				String fileSourcepath = file.getCanonicalPath();
				classpath = GUI.getClasspath();
				mainClass = fileSourcepath.substring(fileSourcepath.indexOf(classpath) + classpath.length(),
						fileSourcepath.indexOf("."));
				String regex = System.getProperty("file.separator").equals("\\") ? "\\\\"
						: System.getProperty("file.separator");
				mainClass = mainClass.replaceAll(regex, ".");
				mainClass = mainClass.startsWith(".") ? mainClass.substring(1) : mainClass;
				System.out.println("user.dir: " + System.getProperty("user.dir"));
				System.out.println(
						"root fileSourcepath: " + fileSourcepath + " classpath: " + classpath + " mainclass: " + mainClass);
				
				System.out.println("root controller, mainclass: " + mainClass);
				
				// create new configuration, add it to GUI's configurations
				Configuration config = new Configuration().setConfigName(mainClass).setMainClass(mainClass).setProgArg("")
						.setSourcepath(classpath).setClasspath(classpath).setShown(false);
				GUI.getConfigurations().put(mainClass, config);
			}
			
			// debugger and thread
			Debugger debugger= new Debugger(mainClass, classpath, debugMode);//TODO add progArg to where???
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
