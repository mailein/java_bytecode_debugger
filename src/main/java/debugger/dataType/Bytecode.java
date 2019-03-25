package debugger.dataType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Bytecode {// fileIndex starts with 1

	private String content;
	private List<MyMethod> methods = new ArrayList<>();

	public Bytecode(String content) {
		this.content = content;
		parseBytecode();
		collectBCI();
	}

	private void parseBytecode() {// parsing content -> methods
		String[] allLines = content.split("\\r?\\n");
		String lastLine = "";
		MyMethod currMethod = null;
		boolean codeParsing = false;
		boolean tableParsing = false;
		for (int i = 0; i < allLines.length; i++) {
			String line = allLines[i];

			if (codeParsing) {
				codeParsing = currMethod.code.parseOneCodeLine(line, i + 1);
				if (!codeParsing)
					currMethod.code.fileEndIndex = i;
			}
			if (tableParsing) {
				tableParsing = currMethod.table.parseOneTableLine(line);
				if (!tableParsing)
					currMethod.table.fileEndIndex = i;
			}

			if (!codeParsing && !tableParsing && line.strip().equals("Code:") && lastLine.contains("(")) {
				currMethod = new MyMethod(lastLine, i + 1);
				methods.add(currMethod);
				codeParsing = true;
				currMethod.code = new Code(currMethod.methodName, i + 2);
			}
			if (!codeParsing && !tableParsing && line.strip().equals("LineNumberTable:") && currMethod != null) {
				tableParsing = true;
				currMethod.table = new LineNumberTable(currMethod.methodName, i + 2);
			}
			if (line.strip().isEmpty() && currMethod != null) {
//				currMethod.fileEndIndex = i;
				currMethod = null;
			}

			lastLine = line;
		}
	}

	private void collectBCI() {
		for (MyMethod method : methods) {
			long lastBCI = method.code.lastBCI;
			List<LineNumberTableEntry> tableEntries = method.table.tableEntries;
			for (int i = 0; i < tableEntries.size(); i++) {
				LineNumberTableEntry entry = tableEntries.get(i);
				long minIncluding = entry.startingBCI;
				long maxExcluding = 0;
				if (i + 1 == tableEntries.size()) {
					maxExcluding = lastBCI + 1;
				} else {
					maxExcluding = tableEntries.get(i + 1).startingBCI;
				}
				// collect bci of the same line#
				for (long bci : method.code.BCI2FileIndex.keySet()) {
					if (bci >= minIncluding && bci < maxExcluding)
						entry.BCIs.add(bci);
				}
			}
		}
	}

	public class MyMethod {
		private String methodName;// the whole line above the line "Code:"
		private int fileStartIndex;
//		private int fileEndIndex;// after the last method's may not be an empty line

		private Code code;
		private LineNumberTable table;

		private MyMethod(String methodName, int fileStartIndex) {
			this.methodName = methodName;
			this.fileStartIndex = fileStartIndex;
		}

		public boolean matchName(String returnTypeName, String name, List<String> argumentTypeNames) {
			String[] arg = { "" };
			argumentTypeNames.forEach(s -> arg[0] = arg[0] + s + ", ");
			if (arg[0].endsWith(", "))
				arg[0] = arg[0].substring(0, arg[0].lastIndexOf(", "));
			String str = returnTypeName + " " + name + "(" + arg[0] + ");";
			if (methodName.contains(str))
				return true;
			return false;
		}

		public List<LineNumberTableEntry> getTableEntries() {
			return table.tableEntries;
		}

		public Map<Long, Integer> getBCI2FileIndex() {
			return code.BCI2FileIndex;
		}
	}

	private class Code {// "Code:"
		private String methodName;
		private int fileStartIndex;
		private int fileEndIndex;
		private long lastBCI;

		private Map<Long, Integer> BCI2FileIndex = new HashMap<>();// <bci, fileIndex>

		private Code(String methodName, int fileStartIndex) {
			this.methodName = methodName;
			this.fileStartIndex = fileStartIndex;
		}

		private boolean parseOneCodeLine(String oneCodeLine, int fileIndex) {
			String tmp = oneCodeLine.substring(0, oneCodeLine.indexOf(":"));
			try {
				long bci = Long.parseUnsignedLong(tmp.strip());
				BCI2FileIndex.put(bci, fileIndex);
				lastBCI = bci;// update it every time, so that the last time it's right
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}

	private class LineNumberTable {// "LineNumberTable:"
		private String methodName;
		private int fileStartIndex;
		private int fileEndIndex;

		private List<LineNumberTableEntry> tableEntries = new ArrayList<>();// <line#, bci>

		private LineNumberTable(String methodName, int fileStartIndex) {
			this.methodName = methodName;
			this.fileStartIndex = fileStartIndex;
		}

		private boolean parseOneTableLine(String oneTableLine) {
			String lineString = oneTableLine.substring(oneTableLine.indexOf("line") + 4, oneTableLine.indexOf(":"));
			String bciString = oneTableLine.substring(oneTableLine.indexOf(":") + 1);
			try {
				int line = Integer.parseUnsignedInt(lineString.strip());
				long bci = Integer.parseUnsignedInt(bciString.strip());
				tableEntries.add(new LineNumberTableEntry(line, bci));
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}

	public class LineNumberTableEntry {// can have multiple same lineNumber in one table
		private int lineNumber;
		private long startingBCI;
		private List<Long> BCIs = new ArrayList<>();// all bci for this lineNumber

		private LineNumberTableEntry(int lineNumber, long startingBCI) {
			this.lineNumber = lineNumber;
			this.startingBCI = startingBCI;
		}

		public int getLineNumber() {
			return lineNumber;
		}

		public List<Long> getBCIs() {
			return BCIs;
		}
	}

	public List<MyMethod> getMethods() {
		return methods;
	}

	public static void main(String[] args) {
		String content = "";
		try {
			Scanner scanner = new Scanner(new File("C:\\Users\\m\\Desktop\\debugger\\countdownZuZweit\\Main.bytecode"));
			content = scanner.useDelimiter("\\Z").next();
			scanner.close();
		} catch (NoSuchElementException e) {
			content = "";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Bytecode bc = new Bytecode(content);
		System.out.println("done");
	}
}
