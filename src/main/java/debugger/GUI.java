package debugger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import debugger.dataType.Configuration;
import debugger.view.BreakpointAreaController;
import debugger.view.BytecodeAreaController;
import debugger.view.CodeAreaController;
import debugger.view.LocalVarAreaController;
import debugger.view.OutputAreaController;
import debugger.view.OverviewController;
import debugger.view.RootLayoutController;
import debugger.view.ThreadAreaController;
import debugger.view.WatchpointAreaController;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GUI extends Application {
	private Stage primaryStage;
	private BorderPane rootLayout;
	private AnchorPane overview;

	private static RootLayoutController rootLayoutController;
	private static OverviewController overviewController;
	private static CodeAreaController codeAreaController;
	private static ThreadAreaController threadAreaController;
	private static WatchpointAreaController watchpointAreaController;
	private static BreakpointAreaController breakpointAreaController;
	private static LocalVarAreaController localVarAreaController;
	private static BytecodeAreaController bytecodeAreaController;
	private static OutputAreaController outputAreaController;

	//Breakpoint setting depends on this classpath
	//these are the current configuration's sourcepath and classpath 
	private static StringProperty sourcepath = new SimpleStringProperty(System.getProperty("user.dir"));
	private static StringProperty classpath = new SimpleStringProperty(System.getProperty("user.dir"));

//	private static ObservableMap<String, Configuration> configurations = FXCollections.observableHashMap();
	private static Map<String, Configuration> configurations = new HashMap<>();// <configName, configuration>

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("debugger");

		boolean[] canStart = { false };
		setDefaultPath(canStart);
		if (canStart[0]) {
			initOverview();// layout splitPane
			initRootLayout();// layout of menubar
			primaryStage.setOnCloseRequest(e -> codeAreaController.closeCodeview());
		}
	}

	/**
	 * @param path: either "sourcepath" or "classpath"
	 */
	private GridPane pathRow(GridPane gridpane, int rowIndex, String path) {
		Label pathLabel = new Label(path + ": ");
		TextField pathTextField = new TextField();
		pathTextField.setPrefWidth(300.0);
//		pathTextField.prefColumnCountProperty().bind(pathTextField.textProperty().length());
		pathTextField.textProperty().addListener((obs, ov, nv) -> {
			if (path.equalsIgnoreCase("sourcepath")) {
				setSourcepath(nv);
//			}
//			if (path.equalsIgnoreCase("classpath")) {
				setClasspath(nv);//compile integrated, no need for classpath input
			}
		});
		return gridpane;
	}

	private void setDefaultPath(boolean[] canStart) {
		Label label = new Label("sourcepath:");
		TextField textField = new TextField();
		textField.setPrefWidth(300.0);
		textField.setPromptText("path/to/parentFolderOfExportedJavaFolder");
		textField.setText(Paths.get(System.getProperty("user.dir")).toString());//<CWD> as default path:)
		Button openButton = new Button("Open");
		openButton.setOnAction(e -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setTitle("Open");
			directoryChooser.setInitialDirectory(Paths.get(System.getProperty("user.dir")).toFile());
			File directory = directoryChooser.showDialog(new Stage());
			if (directory != null) {
				try {
					textField.setText(directory.getCanonicalPath());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		HBox hbox = new HBox(10.0, label, textField, openButton);
		hbox.setAlignment(Pos.CENTER);
		Button saveButton = new Button("save");
		Button quitButton = new Button("quit");
		ButtonBar buttonBar = new ButtonBar();
		buttonBar.getButtons().addAll(saveButton, quitButton);
		VBox vbox = new VBox(10.0, hbox, buttonBar);
		vbox.setAlignment(Pos.CENTER);
		vbox.setPadding(new Insets(35.0));

		Scene scene = new Scene(vbox, 500.0, 200.0);
		Stage stage = new Stage();
		stage.setTitle("Set paths");
		stage.setScene(scene);
		stage.initModality(Modality.APPLICATION_MODAL);

		saveButton.setOnAction(e -> {//TODO set sourcepath, classpath
			String sourcepath = textField.getText();
			if(!sourcepath.isEmpty()) {
				setSourcepath(sourcepath);
				setClasspath(sourcepath);
				canStart[0] = true;
				stage.close();
			}else {
				saveButton.setTooltip(new Tooltip("enter sourcepath!"));
			}
		});
		quitButton.setOnAction(e -> stage.close());
		stage.showAndWait();
	}

	private void initOverview() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(GUI.class.getResource("view/Overview.fxml"));
			overview = (AnchorPane) loader.load();
			overviewController = loader.getController();
			codeAreaController = overviewController.getCodeAreaController();
			threadAreaController = overviewController.getThreadAreaController();
			watchpointAreaController = overviewController.getWatchpointAreaController();
			breakpointAreaController = overviewController.getBreakpointAreaController();
			localVarAreaController = overviewController.getLocalVarAreaController();
			bytecodeAreaController = overviewController.getBytecodeAreaController();
			outputAreaController = overviewController.getOutputAreaController();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initRootLayout() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(GUI.class.getResource("view/RootLayout.fxml"));
			rootLayout = (BorderPane) loader.load();
			rootLayout.setCenter(overview);

			rootLayoutController = loader.getController();
			rootLayoutController.setOverviewController(overviewController);

			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static RootLayoutController getRootLayoutController() {
		return rootLayoutController;
	}

	public static OverviewController getOverviewController() {
		return overviewController;
	}

	public static CodeAreaController getCodeAreaController() {
		return codeAreaController;
	}

	public static ThreadAreaController getThreadAreaController() {
		return threadAreaController;
	}

	public static WatchpointAreaController getWatchpointAreaController() {
		return watchpointAreaController;
	}

	public static BreakpointAreaController getBreakpointAreaController() {
		return breakpointAreaController;
	}

	public static LocalVarAreaController getLocalVarAreaController() {
		return localVarAreaController;
	}

	public static BytecodeAreaController getBytecodeAreaController() {
		return bytecodeAreaController;
	}
	
	public static OutputAreaController getOutputAreaController() {
		return outputAreaController;
	}

	public static StringProperty getSourcepath() {
		return sourcepath;
	}

	//only be used at the very first pathSetting window when starting GUI 
	public static void setSourcepath(String sourcepath) {
		GUI.sourcepath.setValue(sourcepath);
	}

	public static StringProperty getClasspath() {
		return classpath;
	}

	//only be used at the very first pathSetting window when starting GUI 
	//and codeArea's classpath bidirectional binding
	public static void setClasspath(String classpath) {
		GUI.classpath.setValue(classpath);
	}

	public static Map<String, Configuration> getConfigurations() {
		return configurations;
	}
}