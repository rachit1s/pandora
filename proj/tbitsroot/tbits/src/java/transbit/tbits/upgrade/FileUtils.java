package transbit.tbits.upgrade;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtils {

	public static String getContents(File file) throws IOException {
		StringBuilder sb = new StringBuilder();
	
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line).append(System.getProperty("line.separator"));
		}
		return sb.toString();
	}

}
