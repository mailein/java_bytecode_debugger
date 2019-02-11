package debugger.misc;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Source2Class {

	// *.java --> *.class
	public static Path mapSourcepath2ClassPath(Path sourcepath, Path classpath, Path fileSourcepath) {
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

	public static void main(String[] args) {
		Path dir = Paths.get("/Users/m/", "/Desktop/debugger/countdownZuZweit/");
		Path sourcepath = dir.resolve("src");
		Path classpath = dir.resolve("bin");
		Path fileSourcepath = sourcepath.resolve("Main.java");
		Path fileClasspath = mapSourcepath2ClassPath(sourcepath, classpath, fileSourcepath);
		System.out.println(fileClasspath.toString());
	}
}
