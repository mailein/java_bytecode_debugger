package debugger.command;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequestManager;

import debugger.Debugger;

public abstract class Command {

	Debugger debugger;
	EventRequestManager eventReqMgr;
	VirtualMachine vm;
	
	public Command(Debugger debugger) {
		this.debugger = debugger;
		this.eventReqMgr = debugger.getEventRequestManager();
		this.vm = debugger.getVm();
	}
	
	public abstract void execute();
}
