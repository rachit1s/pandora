package com.tbitsglobal.ddc;


import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;





import net.sf.ghost4j.document.PDFDocument;
import net.sf.ghost4j.renderer.SimpleRenderer;

public class PDF2ImageConverter {
	
	
	public static List<File> getImage (File file) {
		StringBuilder builder = new StringBuilder();
		List<File> tempFiles = new ArrayList<File>();
		try {
			PDFDocument document = new PDFDocument();
			document.load(file);
			
			SimpleRenderer renderer = new SimpleRenderer();

			// set resolution (in DPI)
			renderer.setResolution(300);

			List<Image> images = renderer.render(document);

			for (int i = 0; i < images.size(); i++) {
				File tempFile = new File(file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf('.'))+i+".png");
				tempFile.createNewFile();
				tempFiles.add(tempFile);
				ImageIO.write((RenderedImage) images.get(i), "png", tempFile);
//				builder.append(ImageTextExtracter.extractText(
//						tempFile));
//				builder.append(" ");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}
		
		return tempFiles;
	}

	public static void main(String[] args) {
		PDF2ImageConverter conv = new PDF2ImageConverter();
		List<File> files = conv.getImage(new File(
				"D:\\Downloads\\rahul_files_dc\\rahul_files_dc\\FMG-EXT-600-9167\\600MP0053-02012-DR-ME-0041_B_2.pdf"));
		//System.out.println(build.toString());
	}

}
