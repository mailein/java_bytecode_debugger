package debugger.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.Caret.CaretVisibility;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import com.sun.jdi.Method;

import debugger.GUI;
import debugger.dataType.Bytecode;
import debugger.dataType.Bytecode.LineNumberTableEntry;
import debugger.dataType.Bytecode.MyMethod;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
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

	@FXML
	private void initialize() {
		this.anchorPane.setOnMouseEntered(e -> halfWidthOfCodeArea());
		this.anchorPane.setOnMouseExited(e -> {
			this.anchorPane.prefWidthProperty().unbind();
			this.anchorPane.setPrefWidth(25);
		});
		this.anchorPane.setBorder(
				new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
		bytecodeArea.setEditable(false);
		VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(bytecodeArea);
		this.anchorPane.getChildren().add(scrollPane);
		AnchorPane.setBottomAnchor(scrollPane, 0.0);
		AnchorPane.setLeftAnchor(scrollPane, 0.0);
		AnchorPane.setRightAnchor(scrollPane, 0.0);
		AnchorPane.setTopAnchor(scrollPane, 0.0);

		this.anchorPane.setManaged(false);
	}

	class LineRangeFactory implements IntFunction<Node> {
		@Override
		public Node apply(int value) {
			Rectangle square = new Rectangle(10.0, 10.0);
			square.setFill(Color.ORANGE);
			if (!fileIndices.contains(value))
				square.setVisible(false);
			return square;
		}
	}

	class LineIndicatorFactory implements IntFunction<Node> {
		@Override
		public Node apply(int value) {
			Polygon triangle = new Polygon(0.0, 0.0, 10.0, 5.0, 0.0, 10.0);
			triangle.setFill(Color.ORANGE);
			if (indicator != value)
				triangle.setVisible(false);
			return triangle;
		}
	}

	public void openFile(String pathname, Method method, int lineNumber, long bci) {
		String bytecodePathname = pathname.replace(".class", ".bytecode");
		boolean findMethod = false;
		String content = "";
		try {
			Scanner scanner = new Scanner(new File(bytecodePathname));
			content = scanner.useDelimiter("\\Z").next();
			scanner.close();
		} catch (NoSuchElementException e) {
			content = "";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		bytecodeArea.replaceText(content);

		String returnTypeName = method.returnTypeName();
		String name = method.name();
		List<String> argumentTypeNames = method.argumentTypeNames();

		// Bytecode processing
		Bytecode bc = new Bytecode(content);
		List<MyMethod> mymethods = bc.getMethods();
		// get line# -> BCI -> fileIndex
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

	public void setBytecodeAreaVisible(boolean visible) {
		halfWidthOfCodeArea();
		this.anchorPane.setManaged(visible);
		this.anchorPane.setVisible(visible);
		// TODO square(same line), line#, indicator
	}

	private void halfWidthOfCodeArea() {
		this.anchorPane.prefWidthProperty()
				.bind(GUI.getCodeAreaController().getAnchorPanePrefWidthProperty().divide(2.0));
	}

}