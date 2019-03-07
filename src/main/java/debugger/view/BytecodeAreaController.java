package debugger.view;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.paint.Color;

public class BytecodeAreaController {
	@FXML
	private AnchorPane anchorPane = new AnchorPane();
	
	@FXML
	private void initialize() {
		this.anchorPane.setPrefWidth(100);
		this.anchorPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
//		this.anchorPane.setManaged(false);//exclude bytecodeArea!!
	}

}
