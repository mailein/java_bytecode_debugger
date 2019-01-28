/*******************************************************************************
* Copyright (c) 2013, Saarland University. All rights reserved.
* Lisa Detzler
******************************************************************************/

package countdownZuZweit;

import java.io.File;

/**
* Handles the runtie errors.
*
* @author Lisa Detzler
*/
public class ErrorHandling {

	/**
	* Handles the specified runtime exception thrown by the JVM 
	* during the execution of the generated Java code.
	*
	* @param e The runtime exception thrown by the JVM during
	* the execution of the generated Java code.
	* @author Lisa Detzler
	*/
	public static void handleExceptions(Throwable e) {
		StackTraceElement[] traces = e.getStackTrace();
		String exception = "";
		if (e != null) {
			exception = e.toString();
		}
		String part1 = "Error: " + exception;

		String part2 = "";
		for (int i = 0; i < traces.length; i++) {
			StackTraceElement trace = traces[i];
			String fileName = trace.getFileName();
			int index = fileName.indexOf(".java");
			String fileName2 = fileName.substring(0, index);
			File file = new File(fileName);
			Settings.initLineNumberMapping();
			if (/*file.exists()
					&&*/ (!Settings.getListOfExternJavaFiles().contains(fileName) || fileName
							.contains("Main.java"))) {

				CodeGenError.errorInPseuCoCode(e, part1, part2, trace, fileName2);
				return;
			}
		}
		if (part2.equals("")) {
			CodeGenError.compilerError();
		} else {
			System.err.println(part1 + part2);
		}
	}
}
