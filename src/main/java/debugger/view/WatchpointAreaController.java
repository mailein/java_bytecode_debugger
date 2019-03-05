package debugger.view;

import debugger.dataType.HistoryRecord;
import debugger.dataType.Watchpoint;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class WatchpointAreaController {

	private ObservableList<Watchpoint> watchpoints = FXCollections.observableArrayList();

	@FXML
	private ScrollPane scrollPane = new ScrollPane();
	private VBox vbox = new VBox(5.0);
	private Label tableLabel = new Label("Watchpoints");
	private TableView<Watchpoint> table = new TableView<>();

	@FXML
	private void initialize() {
		TableColumn<Watchpoint, String> historyCol = new TableColumn<>();// dummy column for history button
		historyCol.setCellValueFactory(new PropertyValueFactory<>(""));
		historyCol.setCellFactory(new Callback<TableColumn<Watchpoint, String>, TableCell<Watchpoint, String>>() {
			@Override
			public TableCell<Watchpoint, String> call(TableColumn<Watchpoint, String> param) {
				TableCell<Watchpoint, String> cell = new TableCell<>() {
					Button button = new Button("History");
					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setGraphic(null);
							setText(null);
						} else {
							button.setOnAction(event -> {
								Watchpoint wp = watchpoints.get(getIndex());
								handleHistory(wp);
							});
							setGraphic(button);
							setText(null);
						}
					}
				};
				return cell;
			}
		});

		TableColumn<Watchpoint, String> nameCol = new TableColumn<Watchpoint, String>("Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		nameCol.setMinWidth(50);
		nameCol.setEditable(true);
		nameCol.setCellFactory(TextFieldTableCell.<Watchpoint>forTableColumn());
		nameCol.setOnEditCommit((CellEditEvent<Watchpoint, String> t) -> {
			Watchpoint wp = watchpoints.get(t.getTablePosition().getRow());
			String nv = t.getNewValue();
			if (nv.equals("")) {// remove watchpoint
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
		table.getColumns().addAll(nameCol, valueCol, historyCol);

		TextField addName = new TextField();
		addName.setPromptText("name");
		addName.setMaxWidth(nameCol.getPrefWidth());
		Button addButton = new Button("Add");
		addButton.setOnAction(e -> {
			watchpoints.add(new Watchpoint(addName.getText()));
			addName.clear();
		});
		HBox hbox = new HBox(3.0, tableLabel, addName, addButton);
		hbox.setAlignment(Pos.CENTER_LEFT);
		
//		tableLabel.setFont(new Font("Arial", 12));
//		vbox.setPadding(new Insets(10, 0, 0, 10));
		vbox.getChildren().addAll(hbox, table);
		scrollPane.setContent(vbox);
	}

	public void evalAll() {
		watchpoints.forEach(wp -> wp.eval());
	}
	
	private void handleHistory(Watchpoint wp) {
		Label historyTableLabel = new Label("History [" + wp.getName() + "]");
		
		TableColumn<HistoryRecord, String> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("nameProperty"));
		TableColumn<HistoryRecord, String> locationCol = new TableColumn<>("Location");
		locationCol.setCellValueFactory(new PropertyValueFactory<>("locationProperty"));
		TableColumn<HistoryRecord, String> threadCol = new TableColumn<>("Thread");
		threadCol.setCellValueFactory(new PropertyValueFactory<>("threadProperty"));
		TableColumn<HistoryRecord, String> readWriteCol = new TableColumn<>("R/W");
		readWriteCol.setCellValueFactory(new PropertyValueFactory<>("ReadWriteProperty"));
		TableColumn<HistoryRecord, String> valueOldCol = new TableColumn<>("Value(Old)");
		valueOldCol.setCellValueFactory(new PropertyValueFactory<>("valueOldProperty"));
		TableColumn<HistoryRecord, String> valueNewCol = new TableColumn<>("Value(New)");
		valueNewCol.setCellValueFactory(new PropertyValueFactory<>("valueNewProperty"));
		
		TableView<HistoryRecord> historyTable = new TableView<>();
		historyTable.setItems(wp.getHistory());
		historyTable.getColumns().addAll(nameCol, locationCol, threadCol, readWriteCol, valueOldCol, valueNewCol);
		
		VBox historyVbox = new VBox(5.0);
		historyVbox.getChildren().addAll(historyTableLabel, historyTable);
		
		Scene scene = new Scene(historyVbox);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.show();
	}

	public ObservableList<Watchpoint> getWatchpoints() {
		return watchpoints;
	}

}
