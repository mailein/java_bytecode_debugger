package debugger.misc;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SourceClassConversion {

	// *.java --> *.class
	public static Path mapFileSourcepath2FileClasspath(Path sourcepath, Path classpath, Path fileSourcepath) {
		if (!fileSourcepath.startsWith(sourcepath))
			return null;
		Path fileClasspath = classpath;
		for (int i = sourcepath.getNameCount(); i < fileSourcepath.getNameCount(); i++) {
			Path p = fileSourcepath.getName(i);
			if (i == fileSourcepath.getNameCount() - 1) {
				String tmp = p.toString().replace(".java", ".class");
				p = Paths.get(tmp);
			}
			fileClasspath = fileClasspath.resolve(p);// get individual dir name
		}
		return fileClasspath;
	}

	public static String mapFileSourcepath2ClassName(Path sourcepath, Path fileSourcepath) {
		if (!fileSourcepath.startsWith(sourcepath))
			return null;
		String className = "";
		for (int i = sourcepath.getNameCount(); i < fileSourcepath.getNameCount(); i++) {
			String p = fileSourcepath.getName(i).toString() + ".";
			if (i == fileSourcepath.getNameCount() - 1) {
				p = p.substring(0, p.indexOf(".java"));
			}
			className += p;// get individual dir name
		}
		return className;
	}
	
	public static Path mapClassName2FileClasspath(String className, Path classpath) {
		String[] names = className.split("\\.");
		Path fileClasspath = classpath;
		for(String name : names) {
			fileClasspath = fileClasspath.resolve(name);
		}
		fileClasspath = Paths.get(fileClasspath.toString() + ".class");
		return fileClasspath;
	}
	
	/**
	 * @param sourcepath: debugger's sourcepath, eg. path\to\debugger
	 * @param locationSourcepath: fileSourcepath with no leading sourcepath and no leading fileSeparator, eg. count\Main.java
	 * @return fileSourcepath, eg. path\to\debugger\count\Main.java
	 */
	public static Path getFileSourcepath(Path sourcepath, Path locationSourcepath) {
		Path fileSourcepath = sourcepath;
		for(int i = 0; i < locationSourcepath.getNameCount(); i++) {
			fileSourcepath = fileSourcepath.resolve(locationSourcepath.getName(i));
		}
		return fileSourcepath;
	}
	
	public static void main(String[] args) {
		Path dir = Paths.get("/Users/m/", "/Desktop/debugger/countdownZuZweit/");
		Path sourcepath = dir.resolve("src");
		Path classpath = dir.resolve("bin");
		Path fileSourcepath = sourcepath.resolve("Main.java");
		Path fileClasspath = mapFileSourcepath2FileClasspath(sourcepath, classpath, fileSourcepath);
		String className = mapFileSourcepath2ClassName(sourcepath, fileSourcepath);
		System.out.println(fileClasspath.toString());
		System.out.println(className);
		className = "a.b.c.Main";
		System.out.println(mapClassName2FileClasspath(className, classpath));
	}
}
