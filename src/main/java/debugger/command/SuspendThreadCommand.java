package debugger.command;

import com.sun.jdi.ThreadReference;

import debugger.Debugger;

public class SuspendThreadCommand extends Command {

	private ThreadReference thread;
	
	public SuspendThreadCommand(Debugger debugger, ThreadReference thread) {
		super(debugger);
		this.thread = thread;
	}

	@Override
	public void execute() {
		while(thread.suspendCount() < 2) //0, 1
			thread.suspend();
	}

}
