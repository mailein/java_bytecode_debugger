package debugger.view;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BreakpointAreaController {
	@FXML
	private VBox vbox = new VBox(5.0);
	private Label tableLabel = new Label("Breakpoints");
	private TableView<LineBreakpoint> table = new TableView<>();
	private Button removeButton = new Button("Remove");
	private Button removeAllButton = new Button("Remove All");;

	private ObservableList<LineBreakpoint> breakpoints = FXCollections.observableArrayList();// +- by codeAreaController
//	private List<LineBreakpoint> waiting = new ArrayList<>();// wrong! because there can be many debuggers

	@FXML
	private void initialize() {
		TableColumn<LineBreakpoint, String> classNameCol = new TableColumn<>("Class Name");
		classNameCol.setCellValueFactory(new PropertyValueFactory<>("fileNameString"));
		classNameCol.setMinWidth(50);

		TableColumn<LineBreakpoint, String> lineNumberCol = new TableColumn<>("Line");
		lineNumberCol.setCellValueFactory(new PropertyValueFactory<>("lineNumberString"));
		lineNumberCol.setMinWidth(20);

		TableColumn<LineBreakpoint, String> hitCountCol = new TableColumn<>("Set Hit Count");
		hitCountCol.setCellValueFactory(new PropertyValueFactory<>("hitCount"));
		hitCountCol.setMinWidth(20);
		hitCountCol.setCellFactory(TextFieldTableCell.forTableColumn());
		hitCountCol.setOnEditCommit(e -> {
			LineBreakpoint linebp = table.getItems().get(e.getTablePosition().getRow());
			String hitCountString = e.getNewValue();
			linebp.setHitCount(hitCountString);
			BreakpointRequest bpReq = linebp.getBreakpointRequest();
			if (bpReq != null && !hitCountString.isEmpty()) {
				bpReq.disable();//because can't change enabled request 
				try {
					int count = Integer.parseUnsignedInt(hitCountString);
					bpReq.addCountFilter(count);
				} catch (NumberFormatException exception) {
					// no count filter
				}
				bpReq.enable();
			}
		});

		table.setItems(breakpoints);
		table.setEditable(true);// so that hitCountCol can be editable
		table.getColumns().addAll(classNameCol, lineNumberCol, hitCountCol);

		HBox hbox = new HBox(5.0, tableLabel, removeButton, removeAllButton);
		hbox.setAlignment(Pos.CENTER_LEFT);

		vbox.getChildren().addAll(hbox, table);

		// TODO after remove breakpoint, the blue dot doesn't disappear
		removeButton.setOnAction(e -> {
			breakpoints.remove(table.getSelectionModel().getSelectedItem());
			CodeAreaController codeAreaController = GUI.getCodeAreaController();
			codeAreaController.refreshParagraphGraphicFactory(-1, codeAreaController.getCurrLine());
		});
		removeAllButton.setOnAction(e -> {
			breakpoints.clear();
			CodeAreaController codeAreaController = GUI.getCodeAreaController();
			codeAreaController.refreshParagraphGraphicFactory(-1, codeAreaController.getCurrLine());
		});
		// add new breakpoints to loaded normal/anonymous class, if not loaded, leave
		// it.
		breakpoints.addListener((ListChangeListener.Change<? extends LineBreakpoint> c) -> {
			while (c.next()) {
				if (c.wasAdded()) {
					c.getAddedSubList().forEach(linebp -> {
						// for the situation: add breakpoints AFTER debuggers launch
						Debugger dbg = GUI.getThreadAreaController().getRunningDebugger();
						if (dbg != null) {
							String className = getClassName(linebp, dbg);
							addLineBreakpointToDebugger(dbg, className, linebp);
						}
					});
				}
				if (c.wasRemoved()) {
					c.getRemoved().forEach(linebp -> {
						if (linebp.updatedOnceProperty().get()) {// has been requested
							linebp.getEventReqMgrs()
									.forEach(eventReqMgr -> removeLineBreakpointFromDebugger(eventReqMgr,
											linebp.getLoc(), linebp));
						}
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
			linebp.setBreakpointRequest(breakpointRequest);
			int lineNumber = linebp.getLineNumber();
			System.out.println("added Breakpoint to debugger for " + refType + " at line: " + lineNumber);
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

	public boolean lineBreakpointInLine(String fileSourcepath, int lineNumber) {
		LineBreakpoint lineBp = new LineBreakpoint(fileSourcepath, lineNumber);
		if (breakpoints.contains(lineBp))
			return true;
		else
			return false;
	}

	public ObservableList<LineBreakpoint> getBreakpoints() {
		return breakpoints;
	}
}
