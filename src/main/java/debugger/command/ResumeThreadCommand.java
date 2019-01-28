package debugger.command;

import com.sun.jdi.ThreadReference;

import debugger.Debugger;

public class ResumeThreadCommand extends Command {
	
	private ThreadReference thread;
	
	public ResumeThreadCommand(Debugger debugger, ThreadReference thread) {
		super(debugger);
		this.thread = thread;
	}
	@Override
	public void execute() {
		while(thread.suspendCount() > 1) //2, 3, ...
			thread.resume();
	}

}
