package debugger.view;

import java.util.Iterator;

import debugger.dataType.Watchpoint;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

public class WatchpointAreaController {

	private ObservableList<Watchpoint> watchpoints = FXCollections.observableArrayList();
	
	@FXML
	private GridPane gridPane;
	private int rowCount = 0;
	
	@FXML
	private void initialize() {
	    watchpoints.addListener((ListChangeListener.Change<? extends Watchpoint> c) -> {
			//tell all running debuggers to inject
		});
//		gridPane.setHgap(0);
//		gridPane.setVgap(0);
		addNewRow();
	}
	
	//after debugger launch
	private void addWatchpoint() {
		
	}
	
	private void removeWatchpoint() {
		
	}
	
	//whenever user inputs in last line 
	private void addNewRow() {
		TextField watchpointName = new TextField();
		watchpointName.setPromptText("Add new watchpoint");
		Label watchpointValue = new Label();
		
		gridPane.add(watchpointName, 0, rowCount);
		gridPane.add(watchpointValue, 1, rowCount);
		rowCount++;
		watchpointName.textProperty().addListener((obs, ov, nv) -> {
			if(ov.isEmpty() && !nv.isEmpty())
				addNewRow();
			if(!ov.isEmpty() && nv.isEmpty()) {
				int rowIndex = GridPane.getRowIndex(watchpointName);
				removeRow(rowIndex);
			}
		});
	}
	
	//only called after deleting watchfieldName
	private void removeRow(int rowIndex) {
		Iterator<Node> iter = gridPane.getChildren().iterator(); 
		while(iter.hasNext()) {
			Node node = iter.next();
			if(node instanceof TextField || node instanceof Label) { 
				if(GridPane.getRowIndex(node) == rowIndex) 
					iter.remove();
			}
		}
		rowCount--;//assert the row is actually deleted
	}

	public ObservableList<Watchpoint> getWatchpoints() {
		return watchpoints;
	}
	
}
