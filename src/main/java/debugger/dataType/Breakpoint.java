package debugger.dataType;

import com.sun.jdi.ReferenceType;

import debugger.Debugger;

public abstract class Breakpoint {

	//directly set by CodeAreaController
	private String fileSourcepath;//acquired from the file name of the tab
	private int lineNumber;
	
	//to be deduced
	private Debugger debugger = null;//running debugger
	private ReferenceType referenceType = null;//*.class of this breakpoint, if not null, then debugger has created breakpoint request
	private boolean toBeDisabled = false;//TODO
	
	public Breakpoint(String fileSourcepath, int lineNumber) {
		this.fileSourcepath = fileSourcepath;
		this.lineNumber = lineNumber;
	}

	public abstract void add();
	public abstract void remove();
	public abstract void disable();
	public abstract boolean isLineBreakpoint();
	public abstract boolean isWatchpoint();
	
//	public boolean isWaitForClassLoaded() {
//		return waitForClassLoaded;
//	}
//
//	public void setWaitForClassLoaded(boolean waitForClassLoaded) {
//		this.waitForClassLoaded = waitForClassLoaded;
//	}

	public String getFileSourcepath() {
		return fileSourcepath;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	public ReferenceType getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(ReferenceType refType) {
		this.referenceType = refType;
	}

	public Debugger getDebugger() {
		return debugger;
	}

	public void setDebugger(Debugger debugger) {
		this.debugger = debugger;
	}

	public boolean isToBeDisabled() {
		return toBeDisabled;
	}

	public void setToBeDisabled(boolean toBeDisabled) {
		this.toBeDisabled = toBeDisabled;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Breakpoint))
			return false;
		Breakpoint bp = (Breakpoint)o;
		if(this.getFileSourcepath().equals(bp.getFileSourcepath()) && this.getLineNumber() == bp.getLineNumber())
			return true;
		return false;
	}
}
