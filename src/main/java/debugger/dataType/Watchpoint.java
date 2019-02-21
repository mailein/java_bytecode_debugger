package debugger.dataType;

import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.EventRequestManager;

public class Watchpoint extends Breakpoint{
	
	private String fieldName = "";
	
	public Watchpoint(String fileSourcepath, int lineNumber) {
		super(fileSourcepath, lineNumber);
	}
	//TODO if contains . eg. C.ccc then it can be evaluated if ccc is static in class C
	//but no need to test for static, just get the refType and the field

	public void updateInfo(EventRequestManager eventReqMgr, ReferenceType refType, Location loc, String fieldName) {
		this.eventReqMgrs.add(eventReqMgr);
		if(!this.updatedOnceProperty.get()) {//I think these will be the same for this Breakpoint
			this.referenceType = refType;
			this.loc = loc;
			this.fieldName = fieldName;
			this.updatedOnceProperty.set(true);
		}
	}
	
	public String getFieldName() {
		return fieldName;
	}

	@Override
	public boolean isLineBreakpoint() {
		return false;
	}

	@Override
	public boolean isWatchpoint() {
		return true;
	}
	
	
	
}
