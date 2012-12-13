package transbit.tbits.common.readerizer;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;

import org.apache.poi.hslf.extractor.PowerPointExtractor;

public class PptReaderizer implements IReaderizer {

	public Reader getReader(File inputFile) throws Exception {
		PowerPointExtractor ppe    = new PowerPointExtractor(inputFile.getAbsolutePath());
		return new StringReader(ppe.getText(true, true));
	}

}
