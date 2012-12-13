package transbit.tbits.common.readerizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Date;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import net.gencsoy.tesjeract.EANYCodeChar;
import net.gencsoy.tesjeract.Tesjeract;

import org.apache.batik.ext.awt.image.codec.tiff.TIFFImageEncoder;

import transbit.tbits.common.Configuration;

public class ImageReaderizer implements IReaderizer {

	public Reader getReader(File image) throws Exception {
		
		if(!System.getProperty("os.name").startsWith("Windows"))
			throw new Exception("Image reading is only functional for Windows right now.");
		
		// Convert the image into a tiff for tesjeract
		String imageName = image.getName();
		String format = "tiff";
		File tiff = null;
		try{
			int clipIndex = imageName.lastIndexOf(".");
			if(clipIndex > 0){
				format = imageName.substring(clipIndex+1);
				imageName = imageName.substring(0, clipIndex);
			}
			if(format.equals("tiff")){
				tiff = image;
			}
			else{
				String tempTiffPath = Configuration.findPath("tmp")+ File.separator + "images" + File.separator;
				do{
					tiff = new File(tempTiffPath + imageName + "_" + new Date().getTime() + ".tiff");
				}
				while(tiff.exists());
				tiff.getParentFile().mkdirs();
				tiff.createNewFile();
				FileOutputStream out = new FileOutputStream(tiff);
				RenderedOp src = JAI.create("fileload", image.getAbsolutePath());
				TIFFImageEncoder encoder = new TIFFImageEncoder (out, null);
				try{
					encoder.encode (src);
				}
				catch(IOException e){
					throw new Exception("Unable to convert " + image.getAbsolutePath() + " into TIFF.");
				}
				System.out.println("Tiff created at " + tiff.getAbsolutePath());
			}
			
			// Read the image using tesjeract and return the reader.
			try{
				System.loadLibrary("tessdll");
				System.loadLibrary("tesjeract");
			}
			catch(UnsatisfiedLinkError e){
				throw new Exception("Tesjeract libraries not found! Unable to process image.", e);
			}
			catch(SecurityException e){
				throw new Exception("Tesjeract libraries could not be loaded! Unable to process image.", e);
			}

			MappedByteBuffer buf = new FileInputStream(tiff).getChannel().map(MapMode.READ_ONLY, 0, tiff.length());
			Tesjeract tess = new Tesjeract("eng");
			EANYCodeChar[] words = tess.recognizeAllWords(buf);

			StringBuilder strBuilder = new StringBuilder();
			for (EANYCodeChar c:words) {
				while (c.blanks-- > 0)
					strBuilder.append(" ");

				strBuilder.append((char) c.char_code);
			}
			
			return new StringReader(strBuilder.toString());
		}
		finally{
			if(tiff != null && tiff.exists())
				tiff.delete();
		}
	}

}
