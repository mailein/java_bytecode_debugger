package debugger.misc;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;

public class BytecodePrefix {

	private SimpleStringProperty prefix;
	private SimpleStringProperty operandType;
	
	public BytecodePrefix(String prefixString, String operandTypeString) {
		this.prefix = new SimpleStringProperty(prefixString);
		this.operandType = new SimpleStringProperty(operandTypeString);
	}
	
	public SimpleStringProperty prefix() {
		return prefix;
	}
	
	public String getPrefix() {
		return prefix.get();
	}
	
	public void setPrefix(String prefixString) {
		this.prefix.set(prefixString);
	}
	
	public SimpleStringProperty operandType() {
		return operandType;
	}
	
	public String getOperandType() {
		return operandType.get();
	}
	
	public void setOperandType(String operandTypeString) {
		this.operandType.set(operandTypeString);
	}
	
	public static List<BytecodePrefix> getData(){
		List<BytecodePrefix> list = new ArrayList<>();
		list.add(new BytecodePrefix("i", "integer"));
		list.add(new BytecodePrefix("l", "long"));
		list.add(new BytecodePrefix("s", "short"));
		list.add(new BytecodePrefix("b", "byte"));
		list.add(new BytecodePrefix("c", "character"));
		list.add(new BytecodePrefix("f", "float"));
		list.add(new BytecodePrefix("d", "double"));
		list.add(new BytecodePrefix("a", "reference"));
		return list;
	}
}
