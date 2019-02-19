package debugger.dataType;

import com.sun.jdi.Location;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

public class LineBreakpoint extends Breakpoint {

	public LineBreakpoint(String fileSourcepath, int lineNumber) {
		super(fileSourcepath, lineNumber);
	}


	private void setBreakpoint(EventRequestManager eventReqMgr, Location loc) {
		BreakpointRequest breakpointRequest = eventReqMgr.createBreakpointRequest(loc);
		breakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
		breakpointRequest.enable();
	}


	@Override
	public void add() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void disable() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean isLineBreakpoint() {
		return true;
	}


	@Override
	public boolean isWatchpoint() {
		return false;
	}
	
	
	
}
