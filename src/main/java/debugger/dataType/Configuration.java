package debugger.dataType;

public class Configuration {//new Config for every new run/debug

	private String configName = "";
	private String progArg;
	private String sourcepath;
	private String classpath;
	private String mainClass;
	private boolean shown = false;
	
	public String getProgArg() {
		return progArg;
	}
	public Configuration setProgArg(String progArg) {
		this.progArg = progArg;
		return this;
	}
	public String getSourcepath() {
		return sourcepath;
	}
	public Configuration setSourcepath(String sourcepath) {
		this.sourcepath = sourcepath;
		return this;
	}
	public String getClasspath() {
		return classpath;
	}
	public Configuration setClasspath(String classpath) {
		this.classpath = classpath;
		return this;
	}
	public String getMainClass() {
		return mainClass;
	}
	public Configuration setMainClass(String mainClass) {
		this.mainClass = mainClass;
		return this;
	}
	public String getConfigName() {
		return configName;
	}
	public Configuration setConfigName(String configName) {
		this.configName = configName;
		return this;
	}
	public boolean isShown() {
		return shown;
	}
	public Configuration setShown(boolean shown) {
		this.shown = shown;
		return this;
	}
}
