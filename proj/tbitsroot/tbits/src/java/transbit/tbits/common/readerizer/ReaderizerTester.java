package transbit.tbits.common.readerizer;

import java.io.File;
import java.io.Reader;

public class ReaderizerTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			Reader reader = new ImageReaderizer().getReader(new File("/home/karan/Desktop/Screenshot.png"));
			char[] target = new char[100];
			while(reader.read(target, 0, 100) > 0)
				System.out.println(target);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
