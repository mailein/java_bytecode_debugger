package debugger.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.IntFunction;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import debugger.view.CodeAreaController.LineIndicatorFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class BytecodeAreaController {
	@FXML
	private AnchorPane anchorPane = new AnchorPane();
	private CodeArea bytecodeArea = new CodeArea();
	
	
	@FXML
	private void initialize() {
		this.anchorPane.setPrefWidth(100);
		this.anchorPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
//		this.anchorPane.setManaged(false);//exclude bytecodeArea!!
		
		VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(bytecodeArea);
		this.anchorPane.getChildren().add(scrollPane);
		AnchorPane.setTopAnchor(scrollPane, 0.0);
		AnchorPane.setRightAnchor(scrollPane, 0.0);
		AnchorPane.setBottomAnchor(scrollPane, 0.0);
		AnchorPane.setLeftAnchor(scrollPane, 0.0);
	}

	class LineRangeFactory implements IntFunction<Node> {
		@Override
		public Node apply(int value) {
			return null;
		}
	}
	
	public void openFile(Path fileBytecodepath) {
		String content = "";
		try {
			Scanner scanner = new Scanner(fileBytecodepath.toFile());
			content = scanner.useDelimiter("\\Z").next();
			scanner.close();
		} catch (NoSuchElementException e) {
			content = "";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		bytecodeArea.replaceText(content);
		IntFunction<Node> lineNumberFactory = LineNumberFactory.get(bytecodeArea);
		IntFunction<Node> lineRangeFactory = new LineRangeFactory();
		IntFunction<Node> graphicFactory = line -> {
			Node lineNum = lineNumberFactory.apply(line);
			Node range = lineRangeFactory.apply(line);
			HBox hBox = new HBox(lineNum, range);
			hBox.setAlignment(Pos.CENTER_LEFT);
			return hBox;
		};
		bytecodeArea.setParagraphGraphicFactory(graphicFactory);
	}
	
}