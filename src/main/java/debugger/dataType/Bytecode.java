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
	private List<Method> methods = new ArrayList<>();

	public Bytecode(String content) {
		this.content = content;
		parseBytecode();
		collectBCI();
	}

	private void parseBytecode() {// parsing content -> methods
		String[] allLines = content.split("\\r?\\n");
		String lastLine = "";
		Method currMethod = null;
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
				currMethod = new Method(lastLine, i + 1);
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
		for(Method method : methods) {
			int lastBCI = method.code.lastBCI;
			List<LineNumberTableEntry> tableEntries = method.table.line2BCI;
			for(int i = 0; i < tableEntries.size(); i++) {
				LineNumberTableEntry entry = tableEntries.get(i);
				int minIncluding = entry.startingBCI;
				int maxExcluding = 0;
				if(i + 1 == tableEntries.size()) {
					maxExcluding = lastBCI + 1;
				}else {
					maxExcluding = tableEntries.get(i + 1).startingBCI;
				}
				//collect bci of the same line#
				for(int bci : method.code.BCI2FileIndex.keySet()) {
					if(bci >= minIncluding && bci < maxExcluding)
						entry.BCIs.add(bci);
				}
			}
		}
	}
	
	class Method {
		private String methodName;
		private int fileStartIndex;
//		private int fileEndIndex;// after the last method's may not be an empty line

		private Code code;
		private LineNumberTable table;

		private Method(String methodName, int fileStartIndex) {
			this.methodName = methodName;
			this.fileStartIndex = fileStartIndex;
		}
	}

	class Code {// "Code:"
		private String methodName;
		private int fileStartIndex;
		private int fileEndIndex;
		private int lastBCI;

		private Map<Integer, Integer> BCI2FileIndex = new HashMap<>();// <bci, fileIndex>

		private Code(String methodName, int fileStartIndex) {
			this.methodName = methodName;
			this.fileStartIndex = fileStartIndex;
		}

		private boolean parseOneCodeLine(String oneCodeLine, int fileIndex) {
			String tmp = oneCodeLine.substring(0, oneCodeLine.indexOf(":"));
			try {
				int bci = Integer.parseUnsignedInt(tmp.strip());
				BCI2FileIndex.put(bci, fileIndex);
				lastBCI = bci;//update it every time, so that the last time it's right
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}

	}

	class LineNumberTable {// "LineNumberTable:"
		private String methodName;
		private int fileStartIndex;
		private int fileEndIndex;

		private List<LineNumberTableEntry> line2BCI = new ArrayList<>();// <line#, bci>

		private LineNumberTable(String methodName, int fileStartIndex) {
			this.methodName = methodName;
			this.fileStartIndex = fileStartIndex;
		}

		private boolean parseOneTableLine(String oneTableLine) {
			String lineString = oneTableLine.substring(oneTableLine.indexOf("line") + 4, oneTableLine.indexOf(":"));
			String bciString = oneTableLine.substring(oneTableLine.indexOf(":") + 1);
			try {
				int line = Integer.parseUnsignedInt(lineString.strip());
				int bci = Integer.parseUnsignedInt(bciString.strip());
				line2BCI.add(new LineNumberTableEntry(line, bci));
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}
	
	class LineNumberTableEntry{//can have multiple same lineNumber in one table
		private int lineNumber;
		private int startingBCI;
		private List<Integer> BCIs = new ArrayList<>();//all bci for this lineNumber
		
		private LineNumberTableEntry(int lineNumber, int bci) {
			this.lineNumber = lineNumber;
			this.startingBCI = bci;
		}
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
