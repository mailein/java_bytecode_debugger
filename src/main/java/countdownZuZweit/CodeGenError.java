/*******************************************************************************
* Copyright (c) 2013, Saarland University. All rights reserved.
* Lisa Detzler
******************************************************************************/

package countdownZuZweit;

import java.util.LinkedList;
import main.PseuCoCo;

/**
 * Handles the compiler errors.
 * 
 * @author Lisa Detzler
 */
public class CodeGenError {

	/**
	 * Throws an exception with the message that the file
	 * "..\\include\\ErrorHandlingForNewThreads.txt" could not be found.
	 * 
	 * @author Lisa Detzler
	 */
	public static void startThread() {
		System.err
				.println("Error: This program might not have installed "
						+ "correctly. File \"..\\include\\ErrorHandlingForNewThreads.txt\" cannot be found.");
	}

	/**
	 * Throws an exception with the message that an invalid identifier was used.
	 * 
	 * @param s
	 *            The id of the identifier.
	 * @param line
	 *            The PseuCo line of the identifier.
	 * @author Lisa Detzler
	 */
	public static void invalidIdentifier(String s, int line) {
		System.err.println("Error: Invalid Identifier: \"" + s + "\" in line "
				+ line + ". Only ASCII characters are allowed.");
	}

	/**
	 * Throws an exception with the message that the import file path was not
	 * specified.
	 * 
	 * @author Lisa Detzler
	 */
	public static void missingImportFile() {
		System.err.println("Error: No import file specified.");
	}

	/**
	 * Throws an exception with the message that the export file path was not
	 * specified.
	 * 
	 * @author Lisa Detzler
	 */
	public static void missingExportFile() {
		System.err.println("Error: No import file specified.");
	}

	/**
	 * Throws an exception with the message that a key word "-i" or "-e" is
	 * missing.
	 * 
	 * @author Lisa Detzler
	 */
	public static void missingImportExportFileSpecification() {
		System.err
				.println("Error: Missing import/export specification \"-i\"/\"-e\".");
	}

	/**
	 * Throws an exception with the message that the specified export folder
	 * path does not exist.
	 * 
	 * @author Lisa Detzler
	 */
	public static void unexistingExportFilePath() {
		System.err.println("Path of export folder doesn't exist.");
	}

	/**
	 * Throws the specified exception as compiler error.
	 * 
	 * @author Lisa Detzler
	 */
	public static void exceptionToString(Exception e) {
		System.err.println("Error: " + e.toString());
	}

	/**
	 * Prints the stack trace of the specified exception as compiler error.
	 * 
	 * @author Lisa Detzler
	 */
	public static void printStackTrace(Exception e) {
		System.err.println("Error: ");
		e.printStackTrace();
	}

	/**
	 * Throws an exception with the message that the occurred problem relies on
	 * a compiler error and should be reported to the support of PseuCoCo.
	 * 
	 * @author Lisa Detzler
	 */
	public static void compilerError() {
		/*String errorString = "Error: Der Compiler hat ein Problem bei der Uebersetzung dieses "
				+ "PseuCo-Source-Codes festgestellt. Bitte informieren Sie mich darueber, "
				+ "damit der Fehler behoben werden kann unter folgender "
				+ "Mail-Adresse: \"s9lidetz@stud.uni-saarland.de\". Geben Sie als "
				+ "Betreff bitte \"[PseuCoCompilerErrorReport]\" an und schicken Sie bitte "
				+ "den Source-Code, sowie die Fehlermeldung mit. "
				+ "Vielen Dank schon im Vorraus. ";
		System.err.println(errorString);*/
	}

	/**
	 * Throws the compiler exception corresponding to the specified exception
	 * <tt>e<tt>.
	 * 
	 * @param e
	 *            The original Java exception.
	 * @param part1
	 *            The exception header.	 
	 * @param part2
	 *            The string to be added after part1.
	 * @param trace
	 *            That part of the stack trace of the exception <tt>e<tt> that
	 *            is important for the compiler error.
	 * @param fileName2
	 *            The name of the file the error occurred in.
	 * 
	 * @author Lisa Detzler
	 */
	public static void errorInPseuCoCode(Throwable e, String part1,
			String part2, StackTraceElement trace, String fileName2) {

		String methodName = trace.getMethodName();
		int javaLineNumber = trace.getLineNumber();
		LinkedList<Integer> pseuCoLineNumber = Settings
				.getPseuCoLineNumber(javaLineNumber, fileName2, true);
		fileName2 = PseuCoCo.getErrorPath(fileName2, methodName);

		String pseuCoLineNumbers = PseuCoCo
				.handleLineNumberMatching(pseuCoLineNumber);

		fileName2 = fileName2 + ", line " + pseuCoLineNumbers;
				//+ " javaLineNumber: " + javaLineNumber;

		// handle IllegalStateMonitorException
		if (e instanceof IllegalMonitorStateException) {
			part2 = " in "
					+ fileName2
					+ System.getProperty("line.separator")
					+ "Perhaps an agent tried to unlock but wasn't the owner of that lock.";
		} else {
			part2 = part2 + System.getProperty("line.separator") + " in : " + fileName2;
		}

		System.err.println(part1 + part2);
	}

	/**
	 * Throws an exception with the message that the used java version is not
	 * correct.
	 * 
	 * @author Lisa Detzler
	 */
	public static void noJDKUsed() {
		System.err.println("Error: Make sure that your used java version is "
				+ "a jdk and not a jre or older than 1.6.");
	}

}
