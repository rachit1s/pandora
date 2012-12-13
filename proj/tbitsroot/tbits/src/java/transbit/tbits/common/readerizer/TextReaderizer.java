package transbit.tbits.common.readerizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

public class TextReaderizer implements IReaderizer {

	public Reader getReader(File inputFile) throws Exception {
		Reader reader = new BufferedReader(new FileReader(inputFile));
		return reader;
	}

}
