package debugger.view;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;

import debugger.Debugger;
import debugger.GUI;
import debugger.dataType.LineBreakpoint;
import debugger.misc.SourceClassConversion;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class BreakpointAreaController {
	@FXML
	private AnchorPane anchorPane;
	private ObservableList<LineBreakpoint> breakpoints = FXCollections.observableArrayList();

	private List<LineBreakpoint> waiting = new ArrayList<>();

	@FXML
	private void initialize() {
		breakpoints.addListener((ListChangeListener.Change<? extends LineBreakpoint> c) -> {
			while (c.next()) {
				if (c.wasAdded()) {
					c.getAddedSubList().forEach(linebp -> {

						// for the situation: add breakpoints AFTER debuggers launch
						Map<Thread, Debugger> debuggers = GUI.getThreadAreaController().getRunningDebuggers();
						debuggers.forEach((t, dbg) -> {
							String className = getClassName(linebp, dbg);
							addLineBreakpointToDebugger(linebp, dbg, className);
						});
					});
				}
				if (c.wasRemoved()) {
					c.getRemoved().forEach(thread -> {
						// TODO
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
			System.out.println("trying to set bp for " + dbg.name());
			String className = SourceClassConversion.mapFileSourcepath2ClassName(Paths.get(sourcepath),
					Paths.get(linebp.getFileSourcepath()));
			return className;
		}
		return "";
	}

	private void addLineBreakpointToDebugger(LineBreakpoint linebp, Debugger dbg, String className) {
		// 3. check if class loaded or anonymous class problem
		ReferenceType refType = dbg.getClasses().get(className);
		if (refType == null) {// class not loaded
			waiting.add(linebp);
		} else {
			if (!addLineBreakpoint(dbg, refType, linebp)) {
				// get anonymous classes, whose className starts with this className
				List<ReferenceType> anonymousClasses = dbg.getAnonymousClasses(className);
				boolean addedToAnony = anonymousClasses.stream().anyMatch(ac -> addLineBreakpoint(dbg, ac, linebp));
				if (!addedToAnony)
					waiting.add(linebp);
			}
		}
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
			BreakpointRequest breakpointRequest = dbg.getEventRequestManager()
					.createBreakpointRequest(locations.get(0));
			breakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
			breakpointRequest.enable();
			System.out.println("added Breakpoint for " + refType + " at line: " + linebp.getLineNumber());
			linebp.setDebugger(dbg);
			linebp.setReferenceType(refType);
			return true;
		}
		return false;
	}

	public void removeLineBreakpoint() {

	}

	public void disableLineBreakpoint() {

	}

	public ObservableList<LineBreakpoint> getBreakpoints() {
		return breakpoints;
	}
}
