import java.io.File;

public class FileName {
	public static void main(String[] args) {
		File folder = new File("/home/anon/rhino_test/files/tplhydprod/VENDOR_SCHNEIDER");
		for( File file : folder.listFiles() )
		{
			String name = file.getName();
			String filePath = file.getParent();
			String newName = name.replaceAll(" ", "\f");
			File newFile = new File(filePath + "\\" + newName);
			file.renameTo(newFile);
		}
	}
}
