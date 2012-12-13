package transbit.tbits.upgrade;

import java.io.File;

public class SQLScriptFile implements Comparable<SQLScriptFile> {
	public SQLScriptFile(String fileName, File file) {
		this.fileName = fileName;
		this.file = file;
	}

	private String fileName;
	private File file;

	public int compareTo(SQLScriptFile o) {
		return fileName.compareTo(o.getFileName());
	}

	public String getFileName() {
		return fileName;
	}

	public File getFile() {
		return file;
	}

	public String toString() {
		return fileName + ":" + file.getAbsoluteFile();
	}
}

