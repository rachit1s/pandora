package transbit.tbits.common.readerizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

public class RtfReaderizer implements IReaderizer{

	public Reader getReader(File inputFile) throws Exception {
		DefaultStyledDocument styledDoc = new DefaultStyledDocument();
		InputStream is = new FileInputStream(inputFile);
        new RTFEditorKit().read(is, styledDoc, 0);
        String bodyText = styledDoc.getText(0, styledDoc.getLength());
		return new StringReader(bodyText);
	}

}
