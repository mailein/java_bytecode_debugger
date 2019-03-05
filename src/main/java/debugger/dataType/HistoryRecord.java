package debugger.dataType;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import javafx.beans.property.SimpleStringProperty;

public class HistoryRecord {

	private String refTypeName;
	private String methodName;
	private ThreadReference thread;
	private boolean modifyOrAccess = false;		//modify: true, access: false
	private Value currV;
	private Value vToBe;
	private int line;
	private long bci;
	
	private SimpleStringProperty nameProperty;
	private SimpleStringProperty locationProperty;
	private SimpleStringProperty threadProperty;
	private SimpleStringProperty ReadWriteProperty;
	private SimpleStringProperty valueOldProperty;
	private SimpleStringProperty valueNewProperty;
	
	public HistoryRecord(String refTypeName, String methodName, ThreadReference thread, boolean modifyOrAccess, Value currV, Value vToBe, int line, long bci) {
		this.refTypeName = refTypeName;
		this.methodName = methodName;
		this.thread = thread;
		this.modifyOrAccess = modifyOrAccess;
		this.currV = currV;
		this.vToBe = vToBe;
		this.line = line;
		this.bci = bci;
		nameProperty = new SimpleStringProperty("class " + refTypeName + ",method " + methodName);
		locationProperty = new SimpleStringProperty("line " + line + ",bci " + bci);
		threadProperty = new SimpleStringProperty(thread.name());
		ReadWriteProperty = new SimpleStringProperty(modifyOrAccess? "write" : "read");
		valueOldProperty = new SimpleStringProperty(currV.toString());
		valueNewProperty = new SimpleStringProperty((vToBe == null) ? "/" : vToBe.toString());
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

	public SimpleStringProperty nameProperty() {
		return nameProperty;
	}

	public SimpleStringProperty locationProperty() {
		return locationProperty;
	}

	public SimpleStringProperty threadProperty() {
		return threadProperty;
	}

	public SimpleStringProperty readWriteProperty() {
		return ReadWriteProperty;
	}

	public SimpleStringProperty valueOldProperty() {
		return valueOldProperty;
	}

	public SimpleStringProperty valueNewProperty() {
		return valueNewProperty;
	}
	
	public String getNameProperty() {
		return nameProperty.get();
	}
	
	public String getLocationProperty() {
		return locationProperty.get();
	}
	
	public String getThreadProperty() {
		return threadProperty.get();
	}
	
	public String getReadWriteProperty() {
		return ReadWriteProperty.get();
	}
	
	public String getValueOldProperty() {
		return valueOldProperty.get();
	}
	
	public String getValueNewProperty() {
		return valueNewProperty.get();
	}
}
