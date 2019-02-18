package debugger.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.IntFunction;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import org.reactfx.value.Val;

import debugger.GUI;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class CodeAreaController {
	@FXML
	private AnchorPane anchorPane = new AnchorPane();
	@FXML
	private TabPane tabPane = new TabPane();
	@FXML
	private Label classpathLabel = new Label("classpath");
	@FXML
	private TextField classpathTextField = new TextField();
	
	private int newCount = 1;
	private Map<Tab, File> tabsWithFile = new HashMap<Tab, File>();
	private Tab selectedTab = null;
	
	class BreakpointFactory implements IntFunction<Node> {
		@Override
		public Node apply(int lineNumber) {
			Circle circle = new Circle(5.0, Color.BLUE);
			circle.setVisible(false);
			Label label = new Label();
			label.setBorder(new Border(new BorderStroke(Color.WHITE, null, null, null)));
			label.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
			label.setGraphic(circle);
			label.setCursor(Cursor.HAND);
			label.setOnMouseClicked(click -> {
				if(click.getClickCount() == 2 && click.getButton() == MouseButton.PRIMARY) {
					boolean visible = circle.isVisible();
					circle.setVisible(!visible);
					if(visible) {//remove breakpoint
						
					}else {//add breakpoint
						
					}
				}
				activateClasspath();
			});
			return label;
		}
    }
	
	class LineIndicatorFactory implements IntFunction<Node> {//TODO
    	private final ObservableValue<Integer> suspendAtLineNumber;
    	
    	public LineIndicatorFactory(ObservableValue<Integer> suspendAtLineNumber) {
    		this.suspendAtLineNumber = suspendAtLineNumber;
    	}
    	
		@Override
		public Node apply(int lineNumber) {
			Polygon triangle = new Polygon(0.0, 0.0, 10.0, 5.0, 0.0, 10.0);
			triangle.setFill(Color.GREEN);
			
			ObservableValue<Boolean> visible = Val.map(suspendAtLineNumber, currLine -> currLine == lineNumber);
			
			EventStream<Boolean> isVisible = EventStreams.nonNullValuesOf(visible);
			isVisible.subscribe(v -> triangle.setVisible(v));
			
			return triangle;
		}
    }
	
	private void activateClasspath() {
		if(!classpathTextField.isVisible()) {
			classpathTextField.setVisible(true);
			classpathLabel.setBackground(new Background(new BackgroundFill(Color.YELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
		}
	}
	
	private void deactivateClasspath() {
		if(classpathTextField.isVisible()) {
			classpathTextField.setVisible(false);
			classpathLabel.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		}
	}
	
	private void toggleClasspath() {
		boolean isActive = classpathTextField.isVisible();
		classpathTextField.setVisible(!isActive);
		if(isActive) {
			classpathLabel.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		}else {
			classpathLabel.setBackground(new Background(new BackgroundFill(Color.YELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
		}
	}
	
	@FXML
	private void initialize() {// happens after constructor
		classpathLabel.setCursor(Cursor.HAND);
		classpathLabel.setOnMouseClicked(event -> toggleClasspath());
		classpathLabel.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		classpathTextField.setMinWidth(150.0);
		classpathTextField.prefColumnCountProperty().bind(classpathTextField.textProperty().length());
		classpathTextField.setVisible(true);
		classpathTextField.textProperty().bindBidirectional(GUI.getClasspath());		
		
		// update selectedTab
		this.tabPane.getSelectionModel().selectedItemProperty()
				.addListener((obs, ov, nv) -> this.selectedTab = this.tabPane.getSelectionModel().getSelectedItem());

		// init with a new tab
		File file = new File("");
		newTab(file);
		this.tabPane.getTabs().addListener((ListChangeListener.Change<? extends Tab> c) -> {
			if (tabPane.getTabs().size() == 0) {
				File f = new File("");
				newTab(f);
			}
		});
	}

	// handle MenuItems: New and Open
	public void newTab(File file) {
		activateClasspath();
		// get name and content
		String tempName = file.getName();
		String content = "";
		if (file.getPath().isEmpty()) {
			tempName = "new" + newCount;
			newCount++;
		} else {
			try {
				Scanner scanner = new Scanner(file);
				content = scanner.useDelimiter("\\Z").next();
				scanner.close();
			} catch (NoSuchElementException e) {
				content = "";
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		String name = tempName;

		CodeArea codeArea = new CodeArea();
		codeArea.replaceText(content);
		IntFunction<Node> breakpointFactory = new BreakpointFactory();
        IntFunction<Node> lineNumberFactory = LineNumberFactory.get(codeArea);
//        IntFunction<Node> lineIndicatorFactory = new LineIndicatorFactory(codeArea.currentParagraphProperty());
        IntFunction<Node> graphicFactory = line -> {
        	Node bp = breakpointFactory.apply(line);
        	Node lineNum = lineNumberFactory.apply(line);
        	lineNum.setCursor(Cursor.HAND);
        	lineNum.setOnMouseClicked(click -> {
        		if(click.getClickCount() == 2 && click.getButton() == MouseButton.PRIMARY) {
        			Node circle = ((Label)bp).getGraphic();
        			circle.setVisible(!circle.isVisible());
        		}
        	});
        	HBox hBox = new HBox(
        			bp
        			,lineNum
//        			,lineIndicatorFactory.apply(line)
        			);
        	hBox.setAlignment(Pos.CENTER_LEFT);
			return hBox;
        };
        codeArea.setParagraphGraphicFactory(graphicFactory);
		
		Tab tab = new Tab(name, new VirtualizedScrollPane<>(codeArea));
		codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!tab.getText().contains("*")) {
				tab.setText("*" + name);
			}
		});
		tab.setOnCloseRequest(e -> {
			closeTab();
		});

		// tab and file
		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);
		tabsWithFile.put(tab, file);
	}

	private void closeTab() {
		if (this.selectedTab.getText().contains("*")) {
			saveDialog();
		}
		tabsWithFile.remove(this.selectedTab);
	}

	// only called when file changed, including unsaved new file
	private void saveDialog() {
		File file = tabsWithFile.get(this.selectedTab);
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation Dialog");
		String name = file.getName();
		if (name.isEmpty())
			name = this.selectedTab.getText().substring(1);
		alert.setContentText("Save and close file " + name + "?");
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			// ... user chose OK
			saveFile();
		} else {
			// ... user chose CANCEL or closed the dialog
		}
	}

	public void saveFile() {
		String tabName = this.selectedTab.getText();
		if (tabName.contains("*")) {
			File file = tabsWithFile.get(this.selectedTab);
			if (file.getName().isEmpty()) {
				saveAsFile();
			} else {
				writeToFile(this.selectedTab, file);
				this.selectedTab.setText(file.getName());
			}
		}
	}

	public void saveAsFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save As");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Java Files", "*.java"),
				new FileChooser.ExtensionFilter("All Files", "*.*"));
		Stage stage = new Stage();
		File file = fileChooser.showSaveDialog(stage);
		if (file != null) {
			writeToFile(this.selectedTab, file);
			this.selectedTab.setText(file.getName());
		}
	}

	private void writeToFile(Tab tab, File file) {
		try {
			CodeArea codeArea = (CodeArea) ((VirtualizedScrollPane<?>) tab.getContent()).getContent();
			Files.write(file.toPath(), codeArea.getText().getBytes(), StandardOpenOption.CREATE);
			tabsWithFile.put(tab, file);// update File in tabsWithFile
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeCodeview() {
		tabsWithFile.forEach((tab, file) -> {
			if (tab.getText().contains("*")) {
				tabPane.getSelectionModel().select(tab);
				saveDialog();
			}
		});
	}

	public boolean hasOpened(File file) {
		if (tabsWithFile.containsValue(file)) {
			return true;
		} else {
			return false;
		}
	}
	
	public File getFileOfSelectedTab() {
		if(selectedTab == null)
			return null;
		return tabsWithFile.get(selectedTab);
	}
}
