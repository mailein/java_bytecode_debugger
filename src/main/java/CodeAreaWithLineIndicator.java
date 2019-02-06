import java.util.function.IntFunction;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.reactfx.value.Val;

public class CodeAreaWithLineIndicator extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        CodeArea codeArea = new CodeArea();

        IntFunction<Node> numberFactory = LineNumberFactory.get(codeArea);
        IntFunction<Node> arrowFactory = new ArrowFactory(codeArea.currentParagraphProperty());
        IntFunction<Node> graphicFactory = line -> {
            HBox hbox = new HBox(
                numberFactory.apply(line),
                arrowFactory.apply(line));
            hbox.setAlignment(Pos.CENTER_LEFT);
            return hbox;
        };
        codeArea.setParagraphGraphicFactory(graphicFactory);

        primaryStage.setScene(new Scene(new StackPane(codeArea), 600, 400));
        primaryStage.show();
    }
}

class ArrowFactory implements IntFunction<Node> {
    private final ObservableValue<Integer> shownLine;

    ArrowFactory(ObservableValue<Integer> shownLine) {
        this.shownLine = shownLine;
    }

//    @Override
//    public Node apply(int lineNumber) {
//        Circle circle = new Circle(3.0);
//        circle.setFill(Color.BLUE);
//
//        ObservableValue<Boolean> visible = Val.map(
//                shownLine,
//                sl -> sl == lineNumber);
//
//        circle.visibleProperty().bind(((Val) visible).conditionOnShowing(circle));
//
//        return circle;
//    }
    @Override
    public Node apply(int lineNumber) {
    	Polygon triangle = new Polygon(0.0, 0.0, 10.0, 5.0, 0.0, 10.0);
    	triangle.setFill(Color.GREEN);
    	
    	ObservableValue<Boolean> visible = Val.map(
    			shownLine,
    			sl -> sl == lineNumber);
    	
    	triangle.visibleProperty().bind(((Val) visible).conditionOnShowing(triangle));
    	
    	return triangle;
    }
}






