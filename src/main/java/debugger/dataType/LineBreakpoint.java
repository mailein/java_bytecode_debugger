package debugger.dataType;

import java.io.File;
import java.nio.file.Paths;

import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.EventRequestManager;

import debugger.GUI;
import debugger.misc.SourceClassConversion;
import javafx.beans.property.SimpleStringProperty;

public class LineBreakpoint extends Breakpoint {
	private String sourceName;
	private String methodSignature;
	
	private SimpleStringProperty fileNameString;
	private SimpleStringProperty lineNumberString;
	
	public LineBreakpoint(String fileSourcepath, int lineNumber) {
		super(fileSourcepath, lineNumber);
		String tmp = fileSourcepath.substring(0, fileSourcepath.lastIndexOf(".java"));
		tmp = tmp.substring(tmp.lastIndexOf(File.separatorChar) + 1);
		fileNameString = new SimpleStringProperty(tmp);
		lineNumberString = new SimpleStringProperty(String.valueOf(lineNumber));
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
	
	public SimpleStringProperty fileNameString() {
		return fileNameString;
	}

	public String getFileNameString() {
		return fileNameString.get();
	}
	
	public void setFileNameString(String name) {
		fileNameString.set(name);
	}
	
	public SimpleStringProperty lineNumberString() {
		return lineNumberString;
	}
	
	public String getLineNumberString() {
		return lineNumberString.get();
	}
	
	public void setLineNumberString(String line) {
		lineNumberString.set(line);
	}
}
