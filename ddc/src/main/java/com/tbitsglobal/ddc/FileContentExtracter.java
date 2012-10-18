package com.tbitsglobal.ddc;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;




public class FileContentExtracter {

	private static final Logger logger = Logger
			.getLogger(FileContentExtracter.class);
	
	private static final String hostname = "localhost";

	

	public static String extractContent(File file) {
		System.out.println("started");
		String content = null;
		TikaConfig tc = TikaConfig.getDefaultConfig();

		PDFParser parser = new PDFParser(); // Should auto-detect!
		ContentHandler handler = new BodyContentHandler(-1);
		ParseContext context = new ParseContext();
		Metadata metadata = new Metadata();
		InputStream stream = null;
		try {
			stream = new FileInputStream(file);
			
			parser.parse(stream, handler, metadata, context);
			content = handler.toString();
			String[] names = metadata.names();

//			System.out.println("content:"+content.trim().length());
			for (String name : names) {
//				System.out.println("metadata name:"+name+", value="+metadata.get(name));
			//	content = content.concat(metadata.get(name)).concat(" ");
			}

		} catch (Exception e) {
			logger.debug("Error extracting File content of" + file + ":" + e);
			System.out.println(e);
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (Exception e) {
				logger.debug("Error closing file input stream:" + e);
			}
		}

		String mimeType = metadata.get(HttpHeaders.CONTENT_TYPE);
		if (mimeType == null) {
			return content;
		} else {
//			String text = extractContentFromTEService(file, mimeType);
			//content = content.concat(" " + text);
		}

		

		return content;
	}

	
	public static void extractText(String src) throws IOException {
		
		PdfReader pdf = new PdfReader(src);  //doc is just a byte[]
		int pageCount = pdf.getNumberOfPages();
		for (int i = 1; i <= pageCount; i++) {
		    PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(pdf);
		    String pageText = pdfTextExtractor.getTextFromPage(i);
		    System.out.println(pageText);
		}
//        PrintWriter out = new PrintWriter(new FileOutputStream(dest));
//        
//        PdfReader reader = new PdfReader(src);
//        
//        RenderListener listener = new MyTextRenderListener(out);
//        PdfContentStreamProcessor processor = new PdfContentStreamProcessor(listener);
//        PdfDictionary pageDic = reader.getPageN(1);
//        PdfDictionary resourcesDic = pageDic.getAsDict(PdfName.RESOURCES);
//        processor.processContent(ContentByteUtils.getContentBytesForPage(reader, 1), resourcesDic);
//        out.flush();
//        out.close();
    }
	public static String extractContentFromTEService(File file, String mimeType) {
		System.out.println("mimeType:"+mimeType);
		String result = new String();
		if (mimeType.contains("svg")) {
			// get text from images in pdf
			try {
//				result = SVGTextExtracter
//						.extractText(file).toString();
			} catch (Exception e) {
				logger.debug(e.getMessage());
				System.out.println(e);
			}
			
			
		}
		else if (mimeType.startsWith("image/")) {
			// get image content from TE service
			try {
//				result = TEServiceClient
//						.extractImageText("//"+hostname+"/files/"
//								+ file.getName());
			} catch (Exception e) {
//				logger.debug(e.getMessage());
			}
		} else if (mimeType.contains("pdf")) {
			// get text from images in pdf
			try {
//				result = TEServiceClient
//						.extractPDFText("//"+hostname+"/files/"
//								+ file.getName());
			} catch (Exception e) {
//				logger.debug(e.getMessage());
//				System.out.println(e);
			}
			
			
		}
		
		return result;
	}

	public static void main(String args[]) {
		try {
			

			 File f = new File("D:\\Downloads\\rahul_files_dc\\rahul_files_dc\\FMG-EXT-600-9167\\FMG-EXT-600-9167TransmittalReport.pdf");
			
			FileContentExtracter.extractText(f.getAbsolutePath());
//			
//			 String result = FileContentExtracter.extractContent(f);
//			 System.out.println("result:"+result);
//			 if(result.trim().length() == 0){
//				 System.out.println("Drawing file");
//				 return;
//			 }
//			 else{
//				 System.out.println("Transmital doc");
//			 }
////			 System.out.println("result:"+result);
//			 String projectNo = result.substring(result.indexOf("TRANSMITTAL NO: " )+"TRANSMITTAL NO: ".length(),result.indexOf('-', result.indexOf("TRANSMITTAL NO: " )+"TRANSMITTAL NO: ".length())); 
//			 System.out.println("projectNo:"+projectNo);
//			 
//			 
//			 String table = result.substring(result.indexOf("here below"),result.indexOf( "Comments"));
////			 System.out.println("table:"+table);
//			 StringTokenizer token = new StringTokenizer(table);
//			 List<DocumentsData> data = new ArrayList<DocumentsData>();
//			 while(token.hasMoreElements()){
//				 
//				 
//				 String ele = String.valueOf(token.nextElement());
////				 System.out.println("ele:"+ele);
//				  if(ele.startsWith(projectNo)){
//					 DocumentsData record = new DocumentsData();
//					 record.setDocNo1(ele);
//					 
//					 ele = String.valueOf(token.nextElement());
//					 if(ele.startsWith(projectNo) ){
//						 record.setDocNo2(ele);
//						 
//						 ele = String.valueOf(token.nextElement());
//						 while(ele.length() != 1){
//							 record.setDocNo2(record.getDocNo2().concat(ele));
//							 ele = String.valueOf(token.nextElement());
//						 }
//						 						 
//						 if(ele.length() == 1){
//							 record.setRev(ele);
//						 }
//						 
//						 ele = String.valueOf(token.nextElement());
//						 if(ele.length() == 1){
//							 record.setCat(ele);
//							 data.add(record);
//						 }
//					 }
//					
//					 
//				 }
//			 }
//			 
//			 for(int i=0;i<data.size();i++){
//				 data.get(i).print();
//			 }
//			 
//			 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
}
