package debugger.dataType;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.EventRequestManager;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class Breakpoint {

	//directly set by CodeAreaController
	private String fileSourcepath;//acquired from the file name of the tab
	private int lineNumber;
	
	//to be deduced
	protected List<EventRequestManager> eventReqMgrs = new ArrayList<>();//running debugger//TODO debuggers
	protected ReferenceType referenceType = null;//*.class of this breakpoint, if not null, then debugger has created breakpoint request
	protected Location loc = null;
	protected boolean toBeDisabled = false;//TODO
	protected BooleanProperty updatedOnceProperty = new SimpleBooleanProperty(false);
	
	public Breakpoint(String fileSourcepath, int lineNumber) {
		this.fileSourcepath = fileSourcepath;
		this.lineNumber = lineNumber;
	}
	
	public String getFileSourcepath() {
		return fileSourcepath;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	public List<EventRequestManager> getEventReqMgrs() {
		return eventReqMgrs;
	}

	public ReferenceType getReferenceType() {
		return referenceType;
	}

	public Location getLoc() {
		return loc;
	}

	public boolean isToBeDisabled() {
		return toBeDisabled;
	}

	public void setToBeDisabled(boolean toBeDisabled) {
		this.toBeDisabled = toBeDisabled;
	}
	
	public BooleanProperty updatedOnceProperty() {
		return updatedOnceProperty;
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
