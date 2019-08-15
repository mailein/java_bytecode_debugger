package debugger.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import com.sun.jdi.Method;

import debugger.dataType.Bytecode;
import debugger.dataType.Bytecode.LineNumberTableEntry;
import debugger.dataType.Bytecode.MyMethod;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

public class BytecodeAreaController {
	@FXML
	private AnchorPane anchorPane;
//	@FXML
//	private TabPane tabPane;
	private CodeArea bytecodeArea = new CodeArea();
	private List<Integer> fileIndices = new ArrayList<>();
	private int indicator;
	private Bytecode currentBytecode;
	private Map<String, Bytecode> bytecodeMap = new HashMap<>();// <*.bytecode, Bytecode> and all Bytecode are processed
																// in compile functionality

	@FXML
	private void initialize() {
		bytecodeArea.setEditable(false);
		VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(bytecodeArea);
		this.anchorPane.getChildren().add(scrollPane);
		AnchorPane.setBottomAnchor(scrollPane, 0.0);
		AnchorPane.setLeftAnchor(scrollPane, 0.0);
		AnchorPane.setRightAnchor(scrollPane, 0.0);
		AnchorPane.setTopAnchor(scrollPane, 0.0);
	}

	class LineRangeFactory implements IntFunction<Node> {
		@Override
		public Node apply(int value) {
			Label label = new Label();
			label.setBorder(new Border(new BorderStroke(Color.WHITE, null, null, null)));// see no words when scroll
																							// right
			label.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
			Rectangle square = new Rectangle(10.0, 10.0, Color.ORANGE);
			label.setGraphic(square);
			if (!fileIndices.contains(value))
				square.setVisible(false);
			return label;
		}
	}

	class LineIndicatorFactory implements IntFunction<Node> {
		@Override
		public Node apply(int value) {
			Polygon triangle = new Polygon(0.0, 2.0, 5.0, 2.0, 5.0, 5.0, 10.0, 0.0, 5.0, -5.0, 5.0, -2.0, 0.0, -2.0);
			triangle.setFill(Color.ORANGE);
			if (indicator != value)
				triangle.setVisible(false);
			return triangle;
		}
	}

	// called once the compile button is clicked
	public void content2Bytecode(String pathname) {// pathname: *.class or *.bytecode
		String bytecodePathname = pathname.contains(".bytecode") ? pathname : pathname.replace(".class", ".bytecode");
		String content = "";
		try {
			Scanner scanner = new Scanner(new File(bytecodePathname));
			content = scanner.useDelimiter("\\Z").next();
			scanner.close();
		} catch (NoSuchElementException e) {
			content = "";
		} catch (FileNotFoundException e) {
			System.out.println("It's alright, no corresponding bytecode means this class file is not in the project.");
			// e.printStackTrace();
		}

		// Bytecode processing
		Bytecode bc = new Bytecode(content);
		bytecodeMap.put(bytecodePathname, bc);
	}

	public void openFile(String pathname, Method method, int lineNumber, long bci) {
		String bytecodePathname = pathname.replace(".class", ".bytecode");
		String content = "";
		try {
			Scanner scanner = new Scanner(new File(bytecodePathname));
			content = scanner.useDelimiter("\\Z").next();
			scanner.close();
		} catch (NoSuchElementException e) {
			content = "";
		} catch (FileNotFoundException e) {
			System.out.println("It's alright, no corresponding bytecode means this class file is not in the project.");
//			e.printStackTrace();
		}
		bytecodeArea.replaceText(content);

		// Bytecode processing
		currentBytecode = bytecodeMap.get(bytecodePathname);
		refreshParagraphGraphicFactory(method, lineNumber, bci);
	}

	// TODO when a thread is selected, line indicator in BytecodeArea should also
	// change
	public void refreshParagraphGraphicFactory(Method method, int lineNumber, long bci) {
		if (currentBytecode == null) {
			return;
		}
		List<MyMethod> mymethods = currentBytecode.getMethods();
		// get line# -> BCI -> fileIndex
		boolean findMethod = false;
		String returnTypeName = method.returnTypeName();
		String name = method.name();
		List<String> argumentTypeNames = method.argumentTypeNames();

		List<LineNumberTableEntry> tableEntries = null;
		Map<Long, Integer> BCI2FileIndex = null;
		for (MyMethod m : mymethods) {
			if (m.matchName(returnTypeName, name, argumentTypeNames)) {
				tableEntries = m.getTableEntries();
				BCI2FileIndex = m.getBCI2FileIndex();
				findMethod = true;
				break;
			}
		}
		if (!findMethod || tableEntries == null || BCI2FileIndex == null)
			return;
		List<LineNumberTableEntry> matchingEntries = tableEntries.stream()
				.filter(entry -> (entry.getLineNumber() == lineNumber)).collect(Collectors.toList());
		fileIndices.clear();
		for (LineNumberTableEntry entry : matchingEntries) {
			List<Long> BCIs = entry.getBCIs();
			for (long BCI : BCIs) {
				fileIndices.add(BCI2FileIndex.get(BCI));
			}
		}
		indicator = BCI2FileIndex.get(bci);
		bytecodeArea.showParagraphInViewport(indicator - 1);

		IntFunction<Node> lineNumberFactory = LineNumberFactory.get(bytecodeArea);
		IntFunction<Node> lineRangeFactory = new LineRangeFactory();
		IntFunction<Node> lineIndicatorFactory = new LineIndicatorFactory();
		// line in CodeArea starts from 0, line in user's view from 1
		IntFunction<Node> graphicFactory = line -> {
			Node range = lineRangeFactory.apply(line + 1);
			Node lineNum = lineNumberFactory.apply(line);
			Node triangle = lineIndicatorFactory.apply(line + 1);
			HBox hBox = new HBox(range, lineNum, triangle);
			hBox.setAlignment(Pos.CENTER_LEFT);
			return hBox;
		};
		bytecodeArea.setParagraphGraphicFactory(graphicFactory);
	}

	public Map<String, Bytecode> getBytecodeMap() {
		return this.bytecodeMap;
	}

	public List<String> getBytecodeFiles(String fileSourcepath) {
		File file = new File(fileSourcepath).getParentFile();
		File[] classFiles = file.listFiles((dir, name) -> {
			if (name.endsWith(".class")) {
				return true;
			} else {
				return false;
			}
		});
		List<String> bytecodeFiles = new ArrayList<>();
		for (int i = 0; i < classFiles.length; i++) {
			String classFilePath = "";
			try {
				classFilePath = classFiles[i].getCanonicalPath();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			bytecodeFiles.add(classFilePath.replace(".class", ".bytecode"));
		}
		return bytecodeFiles;
	}

	/**
	 * @param fileSourcepath *.java file in the tab
	 */
	public int nextExecutableLine(String fileSourcepath, int lineNumber) {
		// if user doesn't click compile, no bytecodeMap available, generate here!
		List<String> bytecodeFiles = getBytecodeFiles(fileSourcepath);
		for (int i = 0; i < bytecodeFiles.size(); i++) {
			String bytecodeName = bytecodeFiles.get(i);
			content2Bytecode(bytecodeName);
		}

		// all Bytecode objects under this name
		String toSearch1 = fileSourcepath.substring(0, fileSourcepath.indexOf(".java")) + ".bytecode";
		String toSearch2 = fileSourcepath.substring(0, fileSourcepath.indexOf(".java")) + "$";
		List<String> matchingKeys = bytecodeMap.keySet().stream()
				.filter(bytecodeFilePath -> (bytecodeFilePath.equals(toSearch1)
						|| bytecodeFilePath.startsWith(toSearch2)))
				.collect(Collectors.toList());
		// all methods
		List<MyMethod> allMethods = new ArrayList<>();
		matchingKeys.forEach(bytecodeFilePath -> {
			allMethods.addAll(bytecodeMap.get(bytecodeFilePath).getMethods());
		});
		// all lineNumberTableEntry per method
		int[] minNextLine = { Integer.MAX_VALUE };
		boolean[] exactMatch = { false };
		for (MyMethod myMethod : allMethods) {
			List<LineNumberTableEntry> entries = myMethod.getTableEntries();
			for (LineNumberTableEntry entry : entries) {
				int entryLine = entry.getLineNumber();
				if (entryLine == lineNumber) {
					exactMatch[0] = true;
					break;
				} else if (entryLine > lineNumber && entryLine < minNextLine[0]) {
					minNextLine[0] = entryLine;
				}
			}
			if (exactMatch[0]) {
				break;
			}
		}
		// case 1. exact match; case 2. MAX_VALUE (no entryLine#>line#)
		if (exactMatch[0] || minNextLine[0] == Integer.MAX_VALUE) {
			return lineNumber;
		}
		// case 3. min{entryLine#|entryLine#>line#}
		return minNextLine[0];
	}
}