package debugger.misc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;

import debugger.Debugger;
import debugger.GUI;
import javafx.scene.control.Tab;

public class SuspensionLocation {

	public static void gotoLocationFile(Location location) {
			File file = new File(loc2sourcePath(location));
			if(inProject(location)) {
				GUI.getCodeAreaController().gotoTabOfFile(file);
			}else {
				GUI.getCodeAreaController().gotoTabOfError(file);
			}
	}
	
	private static String loc2sourcePath(Location location) {
		try {
			String locationPath = location.sourcePath();
			return locationPath;
		} catch (AbsentInformationException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private static boolean inProject(Location location) {
		String locationPath = loc2sourcePath(location);
		if(!locationPath.isEmpty()) {
			Debugger debugger = GUI.getThreadAreaController().getRunningDebugger();
			if(Files.isRegularFile(Paths.get(debugger.sourcepath(), locationPath), LinkOption.NOFOLLOW_LINKS)) {
				return true;
			}
		}
		return false;
	}

	public static boolean atSelectedTab(Location location) {
		//file path
		File file = GUI.getCodeAreaController().getFileOfSelectedTab();
		if(file == null)
			return false;
		String filePath = "";
		try {
			filePath = file.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		boolean existsFile = Files.isRegularFile(Paths.get(filePath), LinkOption.NOFOLLOW_LINKS);
		if(existsFile) {
			//location path
			String locationPath;
			try {
				locationPath = location.sourcePath();
			} catch (AbsentInformationException e) {
				e.printStackTrace();
				return false;
			}
			Debugger debugger = GUI.getThreadAreaController().getRunningDebugger();
			Path path = Paths.get(debugger.sourcepath(), locationPath);
			if(Paths.get(filePath).equals(path)) {
				return true;
			}
		}
		return false;
	}
}
