package debugger.view;

import java.io.IOException;

import debugger.GUI;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

public class OverviewController {
	@FXML
	private AnchorPane threadArea;
	@FXML
	private AnchorPane watchpointArea;
	@FXML
	private AnchorPane breakpointArea;
	@FXML
	private AnchorPane localVarArea;
	@FXML
	private AnchorPane codeArea;
	@FXML
	private AnchorPane bytecodeArea;
	@FXML
	private AnchorPane outputArea;

	private ThreadAreaController threadAreaController;
	private WatchpointAreaController watchpointAreaController;
	private BreakpointAreaController breakpointAreaController;
	private LocalVarAreaController localVarAreaController;
	private CodeAreaController codeAreaController;
	private BytecodeAreaController bytecodeAreaController;
	private OutputAreaController outputAreaController;

	@FXML
	private void initialize() {
		this.threadAreaController = (ThreadAreaController) initArea("view/ThreadArea.fxml", this.threadArea, 0.0, 0.0,
				0.0, 0.0);
		this.watchpointAreaController = (WatchpointAreaController) initArea("view/WatchpointArea.fxml",
				this.watchpointArea, 0.0, 0.0, 0.0, 0.0);
		this.breakpointAreaController = (BreakpointAreaController) initArea("view/BreakpointArea.fxml",
				this.breakpointArea, 0.0, 0.0, 0.0, 0.0);
		this.localVarAreaController = (LocalVarAreaController) initArea("view/LocalVarArea.fxml", this.localVarArea,
				0.0, 0.0, 0.0, 0.0);
		this.codeAreaController = (CodeAreaController) initArea("view/CodeArea.fxml", this.codeArea, 0.0, 0.0, 0.0,
				0.0);
		this.bytecodeAreaController = (BytecodeAreaController) initArea("view/BytecodeArea.fxml", this.bytecodeArea,
				0.0, 0.0, 0.0, 0.0);
		this.outputAreaController = (OutputAreaController) initArea("view/OutputArea.fxml", this.outputArea, 0.0, 0.0,
				0.0, 0.0);
	}

	private Object initArea(String resourcePath, AnchorPane parent, double top, double bottom, double left,
			double right) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(GUI.class.getResource(resourcePath));
			AnchorPane myArea = (AnchorPane) loader.load();
			Object controller = loader.getController();
			// add myArea to parentView
			addToParentView(parent, myArea, top, bottom, left, right);
			return controller;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void addToParentView(AnchorPane parent, AnchorPane son, double top, double bottom, double left,
			double right) {
		parent.getChildren().add(son);
		AnchorPane.setTopAnchor(son, top);
		AnchorPane.setBottomAnchor(son, bottom);
		AnchorPane.setLeftAnchor(son, left);
		AnchorPane.setRightAnchor(son, right);
	}

	public ThreadAreaController getThreadAreaController() {
		return threadAreaController;
	}

	public WatchpointAreaController getWatchpointAreaController() {
		return watchpointAreaController;
	}

	public BreakpointAreaController getBreakpointAreaController() {
		return breakpointAreaController;
	}

	public LocalVarAreaController getLocalVarAreaController() {
		return localVarAreaController;
	}

	public CodeAreaController getCodeAreaController() {
		return this.codeAreaController;
	}

	public BytecodeAreaController getBytecodeAreaController() {
		return this.bytecodeAreaController;
	}

	public OutputAreaController getOutputAreaController() {
		return outputAreaController;
	}
}
