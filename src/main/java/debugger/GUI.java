package debugger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import debugger.view.BreakpointAreaController;
import debugger.view.CodeAreaController;
import debugger.view.LocalVarAreaController;
import debugger.view.OverviewController;
import debugger.view.RootLayoutController;
import debugger.view.ThreadAreaController;
import debugger.view.WatchpointAreaController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GUI extends Application{
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

	private static String defaultWorkingDir = System.getProperty("user.dir");
	private static String currentWorkingDir = defaultWorkingDir;
//	private Path mainClassPath = Paths.get(currentWorkingDir, "countdownZuZweit", "Main.java");
//	private boolean exists = Files.exists(mainClassPath, LinkOption.NOFOLLOW_LINKS);
	
	public static void main(String[] args) {
		// String[] s = new String[0];
		// It must be a public subclass of Application with a public no-argument
		// constructor
		launch(new String[0]);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("debugger");

		initOverview();// layout splitPane
		initRootLayout();// layout of menubar
		primaryStage.setOnCloseRequest(e -> codeAreaController.closeCodeview());
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

	public static String getCurrentWorkingDir() {
		return currentWorkingDir;
	}
}