package debugger.misc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;

import debugger.Debugger;
import debugger.GUI;

public class SuspensionLocation {

	public static void gotoLocationFile(Location location) {
		Path directoryPath = Paths.get(GUI.getThreadAreaController().getRunningDebugger().sourcepath());
		String fileSourcepath = inProject(location, directoryPath, true);
		System.out.println("goto location, file: " + fileSourcepath);
		if (Files.isRegularFile(Paths.get(fileSourcepath), LinkOption.NOFOLLOW_LINKS)) {
			GUI.getCodeAreaController().gotoTabOfFile(new File(fileSourcepath));
		} else {
			GUI.getCodeAreaController().gotoTabOfError(new File(fileSourcepath));
		}
	}

	/**
	 * @param sourceOrClass: true for *.java, false for *.class
	 * @return
	 */
	public static String inProject(Location location, Path directoryPath, boolean sourceOrClass) {
		String filePath = "";
		try {
			String locationPath = "";
			if(sourceOrClass) {
				locationPath = location.sourcePath();//TODO anonymous class too???
				filePath = SourceClassConversion.getFileSourcepath(directoryPath, Paths.get(locationPath)).toString();
			}else {
				String className = location.declaringType().name();
				filePath = SourceClassConversion.mapClassName2FileClasspath(className, directoryPath).toString();
			}
		} catch (AbsentInformationException e) {
			e.printStackTrace();
		}
		return filePath;
		
	}

	// called by LocalVarAreaController
	public static boolean atSelectedTab(Location location) {
		// file path
		File file = GUI.getCodeAreaController().getFileOfSelectedTab();
		if (file == null)
			return false;
		String filePath = "";
		try {
			filePath = file.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		boolean existsFile = Files.isRegularFile(Paths.get(filePath), LinkOption.NOFOLLOW_LINKS);
		if (existsFile) {
			// location path
			String locationPath;
			try {
				locationPath = location.sourcePath();
			} catch (AbsentInformationException e) {
				e.printStackTrace();
				return false;
			}
			Debugger debugger = GUI.getThreadAreaController().getRunningDebugger();
			Path path = Paths.get(debugger.sourcepath(), locationPath);
			if (Paths.get(filePath).equals(path)) {
				return true;
			}
		}
		return false;
	}
}
