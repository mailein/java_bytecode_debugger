package debugger.command;

import com.sun.jdi.Field;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;

import debugger.Debugger;

public class WatchpointCommand extends Command {
	
	private Field field;
	
	public WatchpointCommand(Debugger debugger, Field field) {
		super(debugger);
		this.field = field;
	}

	@Override
	public void execute() {
		AccessWatchpointRequest accessRequest;
		if (vm.canWatchFieldAccess()) {
			accessRequest = eventReqMgr.createAccessWatchpointRequest(field);
			accessRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
			accessRequest.enable();
		}
		ModificationWatchpointRequest modificationRequest;
		if (vm.canWatchFieldModification()) {
			modificationRequest = eventReqMgr.createModificationWatchpointRequest(field);
			modificationRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
			modificationRequest.enable();
		}
	}

}
