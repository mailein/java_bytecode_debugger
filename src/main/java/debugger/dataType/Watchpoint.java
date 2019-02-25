package debugger.dataType;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.Field;

public class Watchpoint{

	// TODO if contains . then "count.Main.n", "Main.n", "n" are all correct
	//field.toString(): complete name, eg. "count.Main.n"
	//field.name(): name, eg. "n"
	private String name = "";// global variable, if local get it from localVarAreaController
	
	private List<Field> fields = new ArrayList<>();
	
	
	public Watchpoint(String name) {
		this.name = name;
	}

//	public void updateInfo(EventRequestManager eventReqMgr, ReferenceType refType, Location loc) {
//		this.eventReqMgrs.add(eventReqMgr);
//		if (!this.updatedOnceProperty.get()) {// I think these will be the same for this Breakpoint
//			this.referenceType = refType;
//			this.loc = loc;
//			this.updatedOnceProperty.set(true);
//		}
//	}

	public String getName() {
		return name;
	}

	public String strip2fieldName() {
		String fieldName = name;
		if(fieldName.contains(".")) {
			int index = fieldName.lastIndexOf(".");
			fieldName = fieldName.substring(index + 1);
		}
		return fieldName;
	}
}
