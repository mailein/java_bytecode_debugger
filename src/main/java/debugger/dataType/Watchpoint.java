package debugger.dataType;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.StepEvent;

import debugger.Debugger;
import debugger.GUI;
import debugger.view.ThreadAreaController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Watchpoint {// doesn't include local var

	// TODO if contains . then "count.Main.n", "Main.n", "n" are all correct
	// field.toString(): complete name, eg. "count.Main.n"
	// field.name(): name, eg. "n"
	private SimpleStringProperty name;// global variable, if local get it from localVarAreaController
	private SimpleStringProperty value;
	private boolean requested = false;

	private ObservableList<HistoryRecord> history = FXCollections.observableArrayList();

	public Watchpoint(String name) {
		this.name = new SimpleStringProperty(name);
		this.value = new SimpleStringProperty("<Error>");
	}

	public SimpleStringProperty nameProperty() {
		return name;
	}

	public SimpleStringProperty valueProperty() {
		return value;
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public String getValue() {
		return value.get();
	}

	public void setValue(String value) {
		this.value.set(value);
	}

	public ObservableList<HistoryRecord> getHistory() {
		return history;
	}

	public void addHistoryRecord(HistoryRecord record) {
		history.add(record);
	}

	public String strip2fieldName() {
		String fieldName = getName();
		if (fieldName.contains(".")) {
			int index = fieldName.lastIndexOf(".");
			fieldName = fieldName.substring(index + 1);
		}
		return fieldName;
	}

	public String stripOffFieldName() {
		String className = "";
		if (getName().contains(".")) {
			int index = getName().lastIndexOf(".");
			className = getName().substring(0, index);
		}
		return className;
	}

	public void eval() {
		ThreadAreaController threadAreaController = GUI.getThreadAreaController();
		if (threadAreaController != null) {
			Debugger debugger = threadAreaController.getRunningDebugger();
			ThreadReference thread = threadAreaController.getSelectedThread();
			if (debugger != null && thread.isSuspended()) {
				Event currentEvent = debugger.getCurrentEvent().get(thread);
				if (currentEvent != null) {
					Location loc = null;
					if (currentEvent instanceof BreakpointEvent) {
						BreakpointEvent bpEvent = (BreakpointEvent) currentEvent;
						loc = bpEvent.location();
					}
					if (currentEvent instanceof StepEvent) {
						StepEvent stepEvent = (StepEvent) currentEvent;
						loc = stepEvent.location();
					}
					if (loc != null) {
						ReferenceType refType = loc.declaringType();
						String withoutFieldName = stripOffFieldName();
						if ((!withoutFieldName.equals("") && refType.name().endsWith(withoutFieldName))
								|| withoutFieldName.equals("")) {
							Field field = refType.fieldByName(getName());
							if (field != null) {
								setValue(refType.getValue(field).toString());
							} else {
								setValue("<Error>");
							}
						}
					}
				}
			}
		}
	}

	public boolean getRequested() {
		return this.requested;
	}

	public void setRequested(boolean b) {
		this.requested = b;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Watchpoint) {
			Watchpoint wp = (Watchpoint) o;
			String name = wp.getName();
			if (name.equals(getName())) {
				return true;
			}
		}
		return false;
	}
}
