package com.tbitsglobal.ddc.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.RequestDataType;
import transbit.tbits.searcher.DqlSearcher;
import transbit.tbits.searcher.SearchResult;
import transbit.tbits.searcher.Searcher;

import com.tbitsglobal.ddc.FileContentExtracter;
import com.tbitsglobal.ddc.dao.KeywordSetDao;
import com.tbitsglobal.ddc.dao.SearchAlgoDao;
import com.tbitsglobal.ddc.domain.FirmProperty;
import com.tbitsglobal.ddc.domain.KeywordSet;
import com.tbitsglobal.ddc.domain.SearchAlgo;
import com.tbitsglobal.ddc.exception.FailedToFindObject;

import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class DDCHelper {

	private static final Logger logger = Logger.getLogger(DDCHelper.class);
	
	public static final String SPACE_PATTERN = "\\s+";
	public static final String NUMBER_PATTERN = "([-A-Za-z0-9]+)";
	
	public static int incrAndGetNext(Connection con, String corrCat ) throws CorrException
	{
		System.out.println("generating corr. no. for : " + corrCat );
		try {	
			CallableStatement stmt = con.prepareCall("stp_getAndIncrMaxId ?");
			stmt.setString(1, corrCat );
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("max_id");
				System.out.println("Returning the next corr. no. = " + id );
				return id;
			} else {
				throw new CorrException("Cannot generate the next correspondence number");
			}
		} catch (SQLException e) {
			throw new CorrException("Cannot generate the next correspondence number");
		}		
	}
	
	/**
	 * 1. finds the files which are text readable.
	 * 2. finds the file which contains all the keywords mentioned in the fp.
	 * @param files
	 * @param fp
	 * @return
	 * @throws FailedToFindObject 
	 */
	public static File getDTNFile(HashMap<File,String> texts, FirmProperty fp) throws FailedToFindObject 
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

	public static String getNumber(File file, String text, Long number1AlgoId) throws FailedToFindObject {
		SearchAlgo sa = SearchAlgoDao.getInstance().getById(number1AlgoId);
		
		if(null == sa)
		{
			logger.info("SearchAlgo not found with id : " + number1AlgoId);
			return null;
		}

		StringBuffer pattern = new StringBuffer();
		
		if( sa.getSearchType().equals(SearchAlgo.SearchType_After))
		{
			if( null != sa.getFirstKeyword() )
				pattern.append(sa.getFirstKeyword());
			
			if( sa.getSearchWhat().equals(SearchAlgo.SearchWhat_Exact))
			{
				pattern.append(SPACE_PATTERN).append(NUMBER_PATTERN);
			}
			else // there is a pattern
			{
				pattern.append(SPACE_PATTERN).append("(" +sa.getPattern() + ")");
			}
		}
		else if( sa.getSearchType().equals(SearchAlgo.SearchType_Before))
		{
			if( sa.getSearchWhat().equals(SearchAlgo.SearchWhat_Exact))
			{
				pattern.append(NUMBER_PATTERN).append(SPACE_PATTERN);
			}
			else // there is a pattern
			{
				pattern.append("(" +sa.getPattern() + ")").append(SPACE_PATTERN);
			}
			
			if( null != sa.getSecondKeyword() )
				pattern.append(sa.getSecondKeyword());
		}
		else if( sa.getSearchType().equals(SearchAlgo.SearchType_Between))
		{
			if( null != sa.getFirstKeyword() )
				pattern.append(sa.getFirstKeyword());
			
			if( sa.getSearchWhat().equals(SearchAlgo.SearchWhat_Exact))
			{
				pattern.append(SPACE_PATTERN).append(NUMBER_PATTERN).append(SPACE_PATTERN);
			}
			else // there is a pattern
			{
				pattern.append(SPACE_PATTERN).append("(" +sa.getPattern() + ")").append(SPACE_PATTERN);
			}
			
			if( null != sa.getSecondKeyword() )
				pattern.append(sa.getSecondKeyword());
		}
		
		String patternStr = pattern.toString();
		logger.info("Search for pattern : " + pattern);

		Pattern p = Pattern.compile(patternStr);
		
		
		Matcher matcher = p.matcher(text);
		if( matcher.find() )
		{
			 return matcher.group(1);
		}
		else
		{
			return null;
		}
	}

	/**
	 * @param number1Field
	 * @param firstNumber
	 * @param number2Field
	 * @param secondNumber
	 * @param number3Field
	 * @param thirdNumber
	 * @return
	 * @throws DatabaseException 
	 */
	public static List<RequestDataType> findSearchResults(String sysPrefix,HashMap<String,String> searchParams) throws DDCException
	{
		try
		{
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT sys_id, request_id  WHERE ");
			boolean first = true;
			for( String fieldName : searchParams.keySet() )
			{
				if( null != fieldName )
				{
					//assignee_ids:"smittal" AND  subject:"abc"
					String value = searchParams.get(fieldName); 
					if( null != value )
					{
						if( !first )
						{
							sb.append(" AND ");
							first = false;
						}
						
						sb.append(fieldName + ":\"" + value + "\"");
					}
				}
			}
			
			if( first == true )
			{
				// non of the number was found.
				logger.error("None of the configured number field was found in the DTN. Rejecting it.");
				throw new DDCException("None of the configured number field was found in the DTN. Rejecting it.");
			}
			sb.append(" ORDER BY request_id DESC");
	
			List<RequestDataType> rdtlist = new ArrayList<RequestDataType>();
			String dql = sb.toString();
			DqlSearcher searcher = new DqlSearcher(ba.getSystemId(),dql );
			searcher.search();
			List<SearchResult> result = searcher.getOrderedResult();
			for( SearchResult sr : result )
			{
				RequestDataType rdt = new RequestDataType(sr.getSysId(), sr.getRequestId());
				rdtlist.add(rdt);
			}
			return rdtlist;
		}
		catch (DDCException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error(e);
			throw new DDCException("Exception occured while searching for requests.");
		}
	}

	/**
	 * @param ps
	 * @param i
	 * @param docControlUserLogin
	 * @throws SQLException 
	 */
	public static void setNotNull(PreparedStatement ps, int index,
			String stringValue) throws SQLException 
	{
		if( null == stringValue )
			ps.setNull(index, Types.VARCHAR);
		else
			ps.setString(index, stringValue);
	}

	/**
	 * @param ps
	 * @param i
	 * @param number1AlgoId2
	 * @throws SQLException 
	 */
	public static void setNotNull(PreparedStatement ps, int index, Long longValue) throws SQLException {
		if( null == longValue )
			ps.setNull(index, Types.BIGINT);
		else
			ps.setLong(index, longValue);
	}

}
