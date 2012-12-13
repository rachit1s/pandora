package transbit.tbits.common.readerizer;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;

import transbit.tbits.Helper.Xls2csv;

public class ExcelReaderizer implements IReaderizer {

	public Reader getReader(File inputFile) throws Exception {
		Xls2csv xls2csv = new Xls2csv();
        String output = xls2csv.getText(inputFile.getAbsolutePath());
        return new StringReader(output);
	}

}
