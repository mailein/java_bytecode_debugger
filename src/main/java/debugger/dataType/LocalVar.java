package debugger.dataType;

import javafx.beans.property.SimpleStringProperty;

public class LocalVar {

	private SimpleStringProperty name;
	private SimpleStringProperty value;
	
	public LocalVar(String name, String value) {
		this.name = new SimpleStringProperty(name);
		this.value = new SimpleStringProperty(value);
	}
	
	public SimpleStringProperty nameProperty() {
		return name;
	}

	public SimpleStringProperty valueProperty() {
		return value;
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public String getValue() {
		return value.get();
	}

	public void setValue(String value) {
		this.value.set(value);
	}
}
