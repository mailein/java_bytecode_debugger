package debugger.view;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

import debugger.Debugger;
import debugger.GUI;
import debugger.dataType.LineBreakpoint;
import debugger.misc.SourceClassConversion;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

public class BreakpointAreaController {
	@FXML
	private AnchorPane anchorPane;
	@FXML
	private GridPane gridPane;
	@FXML
	private Button removeButton;
	@FXML
	private Button removeAllButton;

	private Node selectedNode;
	private ObservableList<LineBreakpoint> breakpoints = FXCollections.observableArrayList();// +- by codeAreaController
//	private List<LineBreakpoint> waiting = new ArrayList<>();// wrong! because there can be many debuggers

	@FXML
	private void initialize() {
		gridPane.setOnMouseClicked(e -> {
			this.selectedNode = (Node) e.getSource();
		});
		removeButton.setOnAction(e -> {
			if (!gridPane.getChildren().isEmpty()) {
				gridPane.getChildren().remove(selectedNode);
			}
		});
		removeAllButton.setOnAction(e -> {
			if (!gridPane.getChildren().isEmpty()) {
				gridPane.getChildren().clear();
			}
		});
		// add new breakpoints to loaded normal/anonymous class, if not loaded, leave
		// it.

		breakpoints.addListener((ListChangeListener.Change<? extends LineBreakpoint> c) -> {
			while (c.next()) {
				if (c.wasAdded()) {
					c.getAddedSubList().forEach(linebp -> {
						// view
						linebp.updatedOnceProperty().addListener((obs, ov, nv) -> {
							if(nv)
								addLineBreakpointToView(linebp.getFileSourcepath(), linebp.getLineNumber());
						});

						// for the situation: add breakpoints AFTER debuggers launch
						Map<Thread, Debugger> debuggers = GUI.getThreadAreaController().getRunningDebuggers();
						debuggers.forEach((t, dbg) -> {
							String className = getClassName(linebp, dbg);
							addLineBreakpointToDebugger(dbg, className, linebp);
						});
					});
				}
				if (c.wasRemoved()) {
					c.getRemoved().forEach(linebp -> {
						// view
						removeLineBreakpointFromView(linebp.getFileSourcepath(), linebp.getLineNumber());

						if (linebp.updatedOnceProperty().get()) {// has been requested
							linebp.getEventReqMgrs()
									.forEach(eventReqMgr -> removeLineBreakpointFromDebugger(eventReqMgr,
											linebp.getLoc(), linebp));
						}
						int l = linebp.getLineNumber();
						breakpoints.remove(linebp);
						System.out.println("remove LineBreakpoint from controller at line " + l);
					});
				}
			}
		});
	}

	/**
	 * @param linebp
	 * @param dbg
	 * @return "", if this LineBreakpoint doesn't belong to this Debugger
	 */
	public String getClassName(LineBreakpoint linebp, Debugger dbg) {
		String sourcepath = dbg.sourcepath();
		String classpath = dbg.classpath();
		// 1. check if fileClasspath exists
		Path fileClasspath = SourceClassConversion.mapFileSourcepath2FileClasspath(Paths.get(sourcepath),
				Paths.get(classpath), Paths.get(linebp.getFileSourcepath()));
		boolean exists = Files.exists(fileClasspath, LinkOption.NOFOLLOW_LINKS);
		if (exists) {
			// 2. if exists, get className and refType
			// Attention: this className is NEVER a anonymous class' className, need to be
			// dealt with later
			String className = SourceClassConversion.mapFileSourcepath2ClassName(Paths.get(sourcepath),
					Paths.get(linebp.getFileSourcepath()));
			return className;
		}
		return "";
	}

	/**
	 * @param dbg
	 * @param        className: normal class or prefix of anonymous class
	 * @param linebp
	 */
	private void addLineBreakpointToDebugger(Debugger dbg, String className, LineBreakpoint linebp) {
		// 3. add to loaded normal class or loaded anonymous class
		ReferenceType refType = dbg.getClasses().get(className);
		if (refType != null && !addLineBreakpoint(dbg, refType, linebp)) {
			List<ReferenceType> anonymousClasses = dbg.getLoadedAnonymousClasses(className);
			anonymousClasses.forEach(ac -> addLineBreakpoint(dbg, ac, linebp));
		} // else: normal class not loaded, leave it, wait for it to be loaded in
			// Debugger, then add linebp there
	}

	private boolean addLineBreakpoint(Debugger dbg, ReferenceType refType, LineBreakpoint linebp) {
		List<Location> locations = null;
		try {
			locations = refType.locationsOfLine(linebp.getLineNumber());
		} catch (Exception e) {
			// anonymous class not loaded, leave it, wait for it to be loaded in Debugger,
			// then add linebp there
			e.printStackTrace();
		}
		if (locations != null && !locations.isEmpty()) {
			// request
			Location loc = locations.get(0);
			EventRequestManager eventReqMgr = dbg.getEventRequestManager();
			BreakpointRequest breakpointRequest = eventReqMgr.createBreakpointRequest(loc);
			breakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
			breakpointRequest.enable();
			int lineNumber = linebp.getLineNumber();
			System.out.println("added Breakpoint for " + refType + " at line: " + lineNumber);
			// update linebp
			String sourceName = "";
			try {
				sourceName = loc.sourceName();
			} catch (AbsentInformationException e) {
				e.printStackTrace();
			}
			sourceName = sourceName.substring(0, sourceName.indexOf(".java"));
			String methodSignature = loc.method().signature();
			linebp.updateInfo(eventReqMgr, refType, loc, sourceName, methodSignature);
			return true;
		}
		return false;
	}

	public void removeLineBreakpointFromDebugger(EventRequestManager eventReqMgr, Location loc, LineBreakpoint linebp) {
		List<BreakpointRequest> matchingBpReqs = eventReqMgr.breakpointRequests().stream()
				.filter(req -> req.location().equals(loc)).collect(Collectors.toList());
		eventReqMgr.deleteEventRequests(matchingBpReqs);
		System.out.println("remove LineBreakpoint from debugger for line " + linebp.getLineNumber());
	}

	private void disableLineBreakpoint() {
		// TODO
	}

	private String generateBreakpointLabelText(String fileSourcepath, int lineNumber) {
		String fileName = Paths.get(fileSourcepath).getFileName().toString();
		String sourceName = fileName.substring(0, fileName.indexOf(".java"));
		String text = sourceName + " [line: " + lineNumber + "]";
		return text;
	}

	private void addLineBreakpointToView(String sourceName, int lineNumber) {
		String text = generateBreakpointLabelText(sourceName, lineNumber);
		Label label = new Label(text);
		gridPane.add(label, 0, gridPane.getRowCount());
	}

	private void removeLineBreakpointFromView(String sourceName, int lineNumber) {
		String text = generateBreakpointLabelText(sourceName, lineNumber);
		gridPane.getChildren().removeIf(node -> {
			if (node instanceof Label) {
				Label l = (Label) node;
				if (l.getText().equals(text))
					return true;
			}
			return false;
		});
	}

	public ObservableList<LineBreakpoint> getBreakpoints() {
		return breakpoints;
	}
}
