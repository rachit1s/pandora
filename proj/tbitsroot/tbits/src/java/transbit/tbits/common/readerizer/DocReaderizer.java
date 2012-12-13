package transbit.tbits.common.readerizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;

public class DocReaderizer implements IReaderizer {

	public Reader getReader(File inputFile) throws Exception{
		POITextExtractor poitex = ExtractorFactory.createExtractor(new FileInputStream(inputFile));
		Reader reader = new StringReader(poitex.getText());
		return reader;
	}

}
