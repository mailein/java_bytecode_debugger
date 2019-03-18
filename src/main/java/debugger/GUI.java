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
import debugger.view.OverviewController;
import debugger.view.RootLayoutController;
import debugger.view.ThreadAreaController;
import debugger.view.WatchpointAreaController;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
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

	//Breakpoint setting depends on this classpath
	//these are the current configuration's sourcepath and classpath 
	private static StringProperty sourcepath = new SimpleStringProperty(System.getProperty("user.dir"));
	private static StringProperty classpath = new SimpleStringProperty(System.getProperty("user.dir"));

//	private static ObservableMap<String, Configuration> configurations = FXCollections.observableHashMap();
	private static Map<String, Configuration> configurations = new HashMap<>();// <configName, configuration>

	public void main(String[] args) {
		// String[] s = new String[0];
		// It must be a public subclass of Application with a public no-argument
		// constructor
		launch(new String[0]);
	}

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
			}
			if (path.equalsIgnoreCase("classpath")) {
				setClasspath(nv);
			}
		});
		Button pathButton = new Button("open");
		
		gridpane.add(pathLabel, 0, rowIndex);
		gridpane.add(pathTextField, 1, rowIndex);
		gridpane.add(pathButton, 2, rowIndex);
		
		pathButton.setOnAction(e -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setTitle("Open");
			directoryChooser.setInitialDirectory(Paths.get(System.getProperty("user.dir")).toFile());
			File directory = directoryChooser.showDialog(new Stage());
			if (directory != null) {
				try {
					pathTextField.setText(directory.getCanonicalPath());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		return gridpane;
	}

	private void setDefaultPath(boolean[] canStart) {
		GridPane gridpane = new GridPane();
		gridpane.setAlignment(Pos.CENTER);
		gridpane.setHgap(10.0);
		gridpane.setVgap(10.0);
		gridpane = pathRow(gridpane, 0, "sourcepath");
		gridpane = pathRow(gridpane, 1, "classpath");
		Button saveButton = new Button("save");
		Button quitButton = new Button("quit");
		ButtonBar buttonBar = new ButtonBar();
		buttonBar.getButtons().addAll(saveButton, quitButton);
		VBox vbox = new VBox(10.0, gridpane, buttonBar);
		vbox.setAlignment(Pos.CENTER);

		Scene scene = new Scene(vbox, 500.0, 200.0);
		Stage stage = new Stage();
		stage.setTitle("Setting paths");
		stage.setScene(scene);
		stage.initModality(Modality.APPLICATION_MODAL);

		saveButton.setOnAction(e -> {
			canStart[0] = true;
			stage.close();
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