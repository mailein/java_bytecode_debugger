package debugger.command;

import java.util.List;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;

import debugger.Debugger;

public class StepCommand extends Command{
	
	private ThreadReference thread;
	private int size;
	private int depth;
	
	public StepCommand(Debugger debugger, ThreadReference thread, int size, int depth) {
		super(debugger);
		this.thread = thread;
		this.size = size;
		this.depth = depth;
	}
	
	@Override
	public void execute() {
		// delete step request of current thread
		List<StepRequest> stepRequests = eventReqMgr.stepRequests();
		for (StepRequest s : stepRequests) {
			if (s.thread().equals(thread))
				eventReqMgr.deleteEventRequest(s);
		}

		System.out.println(thread.name() + " sets a new stepi request");
		StepRequest stepRequest = eventReqMgr.createStepRequest(thread, size, depth);
		stepRequest.addCountFilter(1);
		stepRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
		stepRequest.enable();
	}
	
}
