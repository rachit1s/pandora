package com.tbitsglobal.ddc.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.tbitsglobal.ddc.FileContentExtracter;
import com.tbitsglobal.ddc.dao.KeywordSetDao;
import com.tbitsglobal.ddc.dao.SearchAlgoDao;
import com.tbitsglobal.ddc.domain.FirmProperty;
import com.tbitsglobal.ddc.domain.KeywordSet;
import com.tbitsglobal.ddc.domain.SearchAlgo;

public class DDCHelper {

	private static final Logger logger = Logger.getLogger(DDCHelper.class);
	
	/**
	 * 1. finds the files which are text readable.
	 * 2. finds the file which contains all the keywords mentioned in the fp.
	 * @param files
	 * @param fp
	 * @return
	 */
	public static File getDTNFile(HashMap<File,String> texts, FirmProperty fp) 
	{
		// apply all files to the text extractor.

		KeywordSet ks = KeywordSetDao.getInstance().getById(fp.getDtnKeywordsId());
		
		for(File file : texts.keySet())
		{
			// check for each file if the text contains the keywords given in fp
			String content = texts.get(file);
			boolean found = false;
			for( String string : ks.getKeyWords())
			{
				if( !content.contains(string) )
				{
					found = false;
					break;
				}
			}
			
			if( found == true )
				return file;
		}
		
		return null;
	}

	/**
	 * ignores files which do not have a pdf extension
	 * @param files
	 * @return
	 * @throws IOException
	 */
	public static HashMap<File,String> extractPDFText(ArrayList<File>files)
	{
		HashMap<File,String> strs = new HashMap<File,String>(files.size());
		for( File file : files )
		{
			if( !file.getName().endsWith(".pdf"))
				strs.put(file,"");
			
			logger.debug("Starting to extract text of file : " + file.getName());
			
			String content = "";
			try {
				content = extractContent(file);
			} catch (Exception e) {
				logger.error(e);
			}
			
			strs.put(file,content);
		}
		
		return strs;
	}
	
	public static void pdfToTextFileUsingTika(ArrayList<File>files) throws IOException
	{
		for( File file : files )
		{
			if( !file.getName().endsWith(".pdf"))
				continue;
			
			System.out.println("Starting file : " + file.getName());
			
			File textFile = new File(file.getAbsolutePath() + ".txt");
			if( !textFile.exists() )
				textFile.createNewFile();
			
			Writer writer = new FileWriter(textFile);
			
			String content = "";
			try {
				content = extractContent(file);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TikaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writer.write(content);
			writer.close();
		}
	}
	
	/**
	 * uses tika to extract text from pdf.
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws TikaException
	 */
	public static String extractContent(File file) throws IOException, SAXException, TikaException {
		String content = "";
//		TikaConfig tc = TikaConfig.getDefaultConfig();

		PDFParser parser = new PDFParser(); // Should auto-detect!
		ContentHandler handler = new BodyContentHandler(-1);
		ParseContext context = new ParseContext();
		Metadata metadata = new Metadata();
		InputStream stream = null;
		try {
			stream = new FileInputStream(file);
			
			parser.parse(stream, handler, metadata, context);
			content = handler.toString();
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (Exception e) {
				logger.debug("Error closing file input stream:" + e);
			}
		}

		return content;
	}

//	public static String getNumber(File file, String text, Integer number1AlgoId) {
//		SearchAlgo sa = SearchAlgoDao.getInstance().getById(number1AlgoId);
//		
//		if(null == sa)
//		{
//			logger.info("SearchAlgo not found with id : " + number1AlgoId);
//			return null;
//		}
//		
//		if( sa.getSearchType().equals(SearchAlgo.SearchType_After))
//		{
//			int firstWordIndex = text.indexOf(sa.getFirstKeyword());
//			if( -1 != firstWordIndex )
//			{
//				 
//			}
//			else
//			{
//				return null;
//			}
//		}
//		
//		
//	}

}
