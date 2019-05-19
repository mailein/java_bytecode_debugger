package debugger.view;

import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import debugger.GUI;
import debugger.dataType.LocalVar;
import debugger.misc.SuspensionLocation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class LocalVarAreaController {
	@FXML
	private VBox vbox = new VBox(5.0);
	private Label tableLabel = new Label("Local Variables");
	private TableView<LocalVar> table = new TableView<>();

	private ObservableList<LocalVar> localVars = FXCollections.observableArrayList();

	@FXML
	private void initialize() {
		TableColumn<LocalVar, String> nameCol = new TableColumn<LocalVar, String>("Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		nameCol.setMinWidth(50);

		TableColumn<LocalVar, String> valueCol = new TableColumn<LocalVar, String>("Value");
		valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
		valueCol.setMinWidth(100);

		table.setItems(localVars);
		table.getColumns().addAll(nameCol, valueCol);

		vbox.getChildren().addAll(tableLabel, table);
	}

	// called at BreakpointEvent or StepEvent
	public void refresh() {
		localVars.clear();
		ThreadReference thread = GUI.getThreadAreaController().getSelectedThread();
		try {
			StackFrame currFrame = thread.frame(0);
			Location currFrameLocation = currFrame.location();
			boolean suspendAtSelectedTab = SuspensionLocation.atSelectedTab(currFrameLocation);
			if (suspendAtSelectedTab) {
				List<LocalVariable> locals = null;
				try {
					locals = currFrame.visibleVariables();
					locals.forEach(local -> {
						Value value = currFrame.getValue(local);
						localVars.add(new LocalVar(local.name(), value.toString()));
					});
				} catch (AbsentInformationException e) {
					System.out.println("It's alright, there is no local variable information for this method.");
//					e.printStackTrace();
				}
			}
		} catch (IncompatibleThreadStateException e) {
			e.printStackTrace();
		}catch (IndexOutOfBoundsException e) {
			//frame index
		}
	}

	// for running threads
	public void clear() {
		localVars.clear();
	}
}
