package debugger.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import debugger.Debugger;
import debugger.GUI;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

public class CodeAreaController {
//	private MainApp mainApp;

	private int newCount = 1;
	private Map<Tab, File> tabsWithFile = new HashMap<Tab, File>();
	private Tab selectedTab = null;
	@FXML
	private TabPane tabPane = new TabPane();
	@FXML
	private TextArea inputLineNumber = new TextArea();
	
	@FXML
	private void initialize() {// happens after constructor
//		this.inputLineNumber.textProperty().addListener((observable, oldValue, newValue) -> {
//			int lineNumber = Integer.parseInt(newValue);
//			breakpointLineNumbers.add((Integer)lineNumber);
//			System.out.println("added line#: " + lineNumber);
//		});
		
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

		// tab(name as its title) has a textArea, textArea has text(context as its text)
		Tab tab = new Tab(name);
		TextArea textArea = new TextArea();
		textArea.setText(content);
		tab.setContent(textArea);
		
		// changeListener, event handler
		textArea.textProperty().addListener((observable, oldValue, newValue) -> {
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
			TextArea textArea = (TextArea) tab.getContent();
			Files.write(file.toPath(), textArea.getText().getBytes(), StandardOpenOption.CREATE);
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
