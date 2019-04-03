package debugger.view;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

public class OutputAreaController {
	@FXML
	private AnchorPane anchorPane;
	@FXML
	private TextArea textArea;
	
	@FXML
	private void initialize() {
		
	}

	public void append(String text) {
		textArea.appendText(text);
	}
	
}
