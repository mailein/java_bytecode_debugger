package debugger.dataType;

import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

public class Breakpoint {

	//add breakpoints to all debuggers with the same classpath (no need to force mainClassName to be the same)
//	private String classpath;
	
	private String className;//acquired from the file name of the tab
	private int lineNumber;
	private boolean waitForClassLoaded = false;
	private boolean toBeDeleted = false;
	private boolean toBeDisabled = false;
	
	public Breakpoint(String className, int lineNumber) {
		this.className = className;
		this.lineNumber = lineNumber;
	}

	private List<Location> getLocationsOfLineInClass(ReferenceType refType, int lineNumber) {
		List<Location> locations = null;
		try {
			locations = refType.locationsOfLine(lineNumber);
		} catch (ClassNotPreparedException e) {
			this.waitForClassLoaded = true;
		} catch (AbsentInformationException e) {
			e.printStackTrace();
		}
		return locations;
	}
	
	private void setBreakpoint(EventRequestManager eventReqMgr, Location loc) {
		BreakpointRequest breakpointRequest = eventReqMgr.createBreakpointRequest(loc);
		breakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
		breakpointRequest.enable();
	}
	
	public boolean isWaitForClassLoaded() {
		return waitForClassLoaded;
	}

	public void setWaitForClassLoaded(boolean waitForClassLoaded) {
		this.waitForClassLoaded = waitForClassLoaded;
	}

	public boolean isToBeDeleted() {
		return toBeDeleted;
	}

	public void setToBeDeleted(boolean toBeDeleted) {
		this.toBeDeleted = toBeDeleted;
	}

	public boolean isToBeDisabled() {
		return toBeDisabled;
	}

	public void setToBeDisabled(boolean toBeDisabled) {
		this.toBeDisabled = toBeDisabled;
	}

	public String getClassName() {
		return className;
	}

	public int getLineNumber() {
		return lineNumber;
	}
	
}
