package debugger.dataType;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

public class HistoryRecord {

	private String refTypeName;
	private String methodName;
	private ThreadReference thread;
	private boolean modifyOrAccess = false;		//modify: true, access: false
	private Value currV;
	private Value vToBe;
	private int line;
	private long bci;
	
	public HistoryRecord(String refTypeName, String methodName, ThreadReference thread, boolean modifyOrAccess, Value currV, Value vToBe, int line, long bci) {
		this.refTypeName = refTypeName;
		this.methodName = methodName;
		this.thread = thread;
		this.modifyOrAccess = modifyOrAccess;
		this.currV = currV;
		this.vToBe = vToBe;
		this.line = line;
		this.bci = bci;
	}
	
	public String toString() {
		String ret = "class " + refTypeName + ",method " + methodName + ",line " + line + ",bci " + bci + ": "
				+ "thread[" + thread.name() + "]";
		if(modifyOrAccess) {
			ret += " writes " + vToBe.toString() + "(old value: " + currV.toString() + ")";
		}else {
			ret += " reads " + currV.toString();
		}
		return ret;
	}
}
