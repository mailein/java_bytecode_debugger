package debugger.view;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.List;

import debugger.Debugger;
import debugger.GUI;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
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
	// TODO classPattern if "", then the current working dir/*
	// TODO mainClass comes from the file of selectedTab, but it should be the
	// mainClass of the last runConfiguration
	private void handleRunOrDebug(boolean debugMode) {
		File file = codeAreaController.getFileOfSelectedTab();
		if (file == null)
			return;
		try {
			// parameters
			String path = file.getCanonicalPath();
			String currWorkingDir = GUI.getCurrentWorkingDir();
			assert (path.startsWith(currWorkingDir));
			String mainClass = path.substring(path.indexOf(currWorkingDir) + currWorkingDir.length(),
					path.indexOf("."));
			String regex = System.getProperty("file.separator").equals("\\") ? "\\\\"
					: System.getProperty("file.separator");
			mainClass = mainClass.replaceAll(regex, ".");
			mainClass = mainClass.startsWith(".") ? mainClass.substring(1) : mainClass;
			String classPattern = mainClass.contains(".") ? (mainClass.substring(0, mainClass.indexOf(".")) + ".*")
					: mainClass;
			String classPath = Files.exists(Paths.get(currWorkingDir, "bin"), LinkOption.NOFOLLOW_LINKS)
					? Paths.get(currWorkingDir, "bin").toString()
					: path.substring(0, path.lastIndexOf(System.getProperty("file.separator")));

			// debugger and thread
			Debugger debugger;
			debugger = new Debugger(mainClass, classPattern, classPath, debugMode);
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
