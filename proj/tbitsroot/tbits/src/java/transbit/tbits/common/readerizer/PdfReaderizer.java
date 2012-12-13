package transbit.tbits.common.readerizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

public class PdfReaderizer implements IReaderizer {

	public Reader getReader(File inputFile) throws Exception {
		
		PDDocument pdfDocument = null;
        try {
            pdfDocument = PDDocument.load(inputFile);
            if(pdfDocument.isEncrypted())
            {
            	System.out.println("Document is encrypted: " + inputFile.getAbsolutePath());
            }
            
            // To get a reader for the PDF document,
            //
            // 1. write the document as bytes to an output stream and then
            // 2. get the bytes into an array
            // 3. form a string object from this byte array.
            // 4. open a string reader with this string
            //
            ByteArrayOutputStream baos     = new ByteArrayOutputStream();
            OutputStreamWriter    writer   = new OutputStreamWriter(baos);
            PDFTextStripper       stripper = new PDFTextStripper();
            
            stripper.writeText(pdfDocument, writer);
            writer.close();
            
            String content = new String(baos.toByteArray());
            
            return new StringReader(content);
        } 
        finally {
            if (pdfDocument != null) {
                pdfDocument.close();
            }
        }
	}

}
