package debugger.view;

import java.util.Iterator;

import debugger.dataType.Watchpoint;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Callback;

public class WatchpointAreaController {

	private ObservableList<Watchpoint> watchpoints = FXCollections.observableArrayList();

	@FXML
	private VBox vbox = new VBox(5.0);
	private Label tableLabel = new Label("Watchpoints");
	private TableView<Watchpoint> table = new TableView<>();

	@FXML
	private void initialize() {
		TableColumn<Watchpoint, String> nameCol = new TableColumn<Watchpoint, String>("Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		nameCol.setMinWidth(100);
		nameCol.setEditable(true);
		nameCol.setCellFactory(TextFieldTableCell.<Watchpoint>forTableColumn());
		nameCol.setOnEditCommit((CellEditEvent<Watchpoint, String> t) -> {
			Watchpoint wp = watchpoints.get(t.getTablePosition().getRow());
			String nv = t.getNewValue();
			if (nv.equals("")) {//remove watchpoint
				String ov = t.getOldValue();
				watchpoints.remove(new Watchpoint(ov));
			} else {
//				if(watchpoints.contains(new Watchpoint(nv))) {//TODO
//					
//				}else {
//					
//				}
				wp.setName(nv);
				wp.eval();
			}
		});

		TableColumn<Watchpoint, String> valueCol = new TableColumn<Watchpoint, String>("Value");
		valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
		valueCol.setMinWidth(100);
		valueCol.setEditable(false);

		table.setItems(watchpoints);
		table.setEditable(true);
		table.getColumns().addAll(nameCol, valueCol);

		TextField addName = new TextField();
		addName.setPromptText("name");
		addName.setMaxWidth(nameCol.getPrefWidth());
		Button addButton = new Button("Add");
		addButton.setOnAction(e -> {
			watchpoints.add(new Watchpoint(addName.getText()));
			addName.clear();
		});
		HBox hbox = new HBox(3.0, addName, addButton);

		tableLabel.setFont(new Font("Arial", 12));
//		vbox.setPadding(new Insets(10, 0, 0, 10));
		vbox.getChildren().addAll(tableLabel, table, hbox);
	}
	
	public void evalAll() {
		watchpoints.forEach(wp -> wp.eval());
	}

	public ObservableList<Watchpoint> getWatchpoints() {
		return watchpoints;
	}

}
