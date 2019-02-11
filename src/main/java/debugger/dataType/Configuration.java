package debugger.dataType;

public class Configuration {//new Config for every new run/debug

	private String configName = "";
	private String progArg;
	private String sourcepath;
	private String classpath;
	private String mainClass;
	
	public String getProgArg() {
		return progArg;
	}
	public void setProgArg(String progArg) {
		this.progArg = progArg;
	}
	public String getSourcepath() {
		return sourcepath;
	}
	public void setSourcepath(String sourcepath) {
		this.sourcepath = sourcepath;
	}
	public String getClasspath() {
		return classpath;
	}
	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}
	public String getMainClass() {
		return mainClass;
	}
	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}
	public String getConfigName() {
		return configName;
	}
	public void setConfigName(String configName) {
		this.configName = configName;
	}	
}
