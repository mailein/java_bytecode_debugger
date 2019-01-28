package debugger.command;

import com.sun.jdi.Location;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;

import debugger.Debugger;


public class BreakpointCommand extends Command{

	private Location loc;
	
	public BreakpointCommand(Debugger debugger, Location location) {
		super(debugger);
		this.loc = location;
	}
	
	@Override
	public void execute() {
		BreakpointRequest breakpointRequest = eventReqMgr.createBreakpointRequest(loc);
		breakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
		breakpointRequest.enable();
		System.out.println("Breakpoint set for line: " + loc.lineNumber());
	}

}
