package transbit.tbits.common.readerizer;

import java.io.File;
import java.io.Reader;

import org.htmlparser.HTMLParser;

public class HtmlReaderizer implements IReaderizer {

	public Reader getReader(File inputFile) throws Exception {
		HTMLParser parser = new HTMLParser(inputFile.getAbsolutePath());
		return parser.getReader();
	}

}
