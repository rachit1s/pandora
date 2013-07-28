

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.sf.ghost4j.document.PDFDocument;
import net.sf.ghost4j.renderer.SimpleRenderer;

public class PDF2ImageConverter {
	
	
	public static File getImage (File file) {
		
		
		PDFDocument document = new PDFDocument();
		List<Image> images = null;
		File tempFile = null;
		try {
			
			document.load(file);
			
			SimpleRenderer renderer = new SimpleRenderer();

			// set resolution (in DPI)
			renderer.setResolution(300);

			images = renderer.render(document,0,0);
			

			
				tempFile = new File(file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf('.'))+"0.png");
				tempFile.createNewFile();
		
				ImageIO.write((RenderedImage) images.get(0), "png", tempFile);
				
				
//				builder.append(ImageTextExtracter.extractText(
//						tempFile));
//				builder.append(" ");
				
			
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
			if (document != null) {
				
				document = null;
				
				
				
			}
			if(images != null){
				for(Image i:images){
					
					
					i.flush();
					
					
				}
				images.clear();
			}
			
			images = null;
			System.gc();
		}
		
		return tempFile;
	}

	public static void main(String[] args) {
		PDF2ImageConverter conv = new PDF2ImageConverter();
		File files = conv.getImage(new File(
				"D:\\DTN Zipped File\\DTN Zipped File\\600MP_DTN\\FMG-EXT-600-7335\\16-2-600MP0053-02012-DR-ME-0017_A_2.pdf"));
		//System.out.println(build.toString());
	}

}
