package debugger.view;

import debugger.dataType.Breakpoint;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class BreakpointAreaController {
	private ObservableMap<String, Breakpoint> breakpoints = FXCollections.observableHashMap(); //<classpath, Breakpoint>
}
