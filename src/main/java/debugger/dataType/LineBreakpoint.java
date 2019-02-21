package debugger.dataType;

import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.EventRequestManager;

public class LineBreakpoint extends Breakpoint {

	private String sourceName = "";
	private String methodSignature = "";
	
	public LineBreakpoint(String fileSourcepath, int lineNumber) {
		super(fileSourcepath, lineNumber);
	}

	/**
	 * @param eventReqMgr: specify every time
	 * @param refType: no need to specify if updated once
	 * @param loc: no need to specify if updated once
	 * @param sourceName: no need to specify if updated once
	 * @param methodSignature: no need to specify if updated once
	 */
	public void updateInfo(EventRequestManager eventReqMgr, ReferenceType refType, Location loc, String sourceName, String methodSignature) {
		this.eventReqMgrs.add(eventReqMgr);
		if(!this.updatedOnceProperty.get()) {//I think these will be the same for this Breakpoint
			this.referenceType = refType;
			this.loc = loc;
			this.sourceName = sourceName;
			this.methodSignature = methodSignature;
			this.updatedOnceProperty.set(true);
		}
	}

	public String getSourceName() {
		return sourceName;
	}

	public String getMethodSignature() {
		return methodSignature;
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
