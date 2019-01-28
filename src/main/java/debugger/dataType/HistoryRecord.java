package debugger.dataType;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

public class HistoryRecord {

	private ThreadReference thread;
	private boolean modifyOrAccess = false;		//modify: true, access: false
	private Value currV;
	private Value vToBe;
	private int line;
	private long bci;
	
	public HistoryRecord(ThreadReference thread, boolean modifyOrAccess, Value currV, Value vToBe, int line, long bci) {
		this.thread = thread;
		this.modifyOrAccess = modifyOrAccess;
		this.currV = currV;
		this.vToBe = vToBe;
		this.line = line;
		this.bci = bci;
	}
	
	public String toString() {
		String ret = "line " + line + ",bci " + bci + ": "
				+ "thread[" + thread.name() + "]";
		if(modifyOrAccess) {
			ret += " writes " + vToBe.toString() + "(old value: " + currV.toString() + ")";
		}else {
			ret += " reads " + currV.toString();
		}
		return ret;
	}
}
