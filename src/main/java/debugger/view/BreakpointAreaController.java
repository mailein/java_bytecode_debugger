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
//	private List<LineBreakpoint> waiting = new ArrayList<>();// wrong! because there can be many debuggers

	@FXML
	private void initialize() {
		// add new breakpoints to loaded class or anonymous class, if not loaded, leave
		// it.
		breakpoints.addListener((ListChangeListener.Change<? extends LineBreakpoint> c) -> {
			while (c.next()) {
				if (c.wasAdded()) {
					c.getAddedSubList().forEach(linebp -> {
						if (linebp.getReferenceType() == null) {// haven't requested yet
							// for the situation: add breakpoints AFTER debuggers launch
							Map<Thread, Debugger> debuggers = GUI.getThreadAreaController().getRunningDebuggers();
							debuggers.forEach((t, dbg) -> {
								String className = getClassName(linebp, dbg);
								addLineBreakpointToDebugger(dbg, className, linebp);
							});
						}
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
