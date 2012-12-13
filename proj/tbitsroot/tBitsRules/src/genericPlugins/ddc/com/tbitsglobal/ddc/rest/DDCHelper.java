package ddc.com.tbitsglobal.ddc.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.tika.exception.TikaException;
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


import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import ddc.com.tbitsglobal.ddc.dao.SearchAlgoDao;
import ddc.com.tbitsglobal.ddc.domain.SearchAlgo;
import ddc.com.tbitsglobal.ddc.exception.FailedToFindObject;

public class DDCHelper {

	private static final Logger logger = Logger.getLogger(DDCHelper.class);
	
	public static final String SPACE_PATTERN = "\\s+";
	public static final String NUMBER_PATTERN = "([-A-Za-z0-9]+)";

	private static final int A4PortraitHeight = 843;

	private static final int A4PortraitWidth = 596;
	
	public static int incrAndGetNext(Connection con, String corrCat ) throws CorrException
	{
		System.out.println("generating Next number for : " + corrCat );
		try {	
			CallableStatement stmt = con.prepareCall("stp_getAndIncrMaxId ?");
			stmt.setString(1, corrCat );
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("max_id");
				System.out.println("Returning the next no. = " + id );
				return id;
			} else {
				throw new CorrException("Cannot generate the next number");
			}
		} catch (SQLException e) {
			throw new CorrException("Cannot generate the next number");
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
//	public static File getDTNFile(HashMap<File,String> texts, FirmProperty fp) throws FailedToFindObject 
//	{
//		// apply all files to the text extractor.
//
//		KeywordSet ks = KeywordSetDao.getInstance().getById(fp.getDtnKeywordsId());
//		
//		for(File file : texts.keySet())
//		{
//			// check for each file if the text contains the keywords given in fp
//			String content = texts.get(file);
//			boolean found = true;
//			for( String string : ks.getKeyWords())
//			{
//				if( !content.contains(string) )
//				{
//					found = false;
//					break;
//				}
//			}
//			
//			if( found == true )
//				return file;
//		}
//		
//		return null;
//	}

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

	public static List<String> getNumber(File file, String text, Long number1AlgoId) throws FailedToFindObject {
		if( null == number1AlgoId)
		{
			logger.info("SearchAlgo not found with id : " + number1AlgoId);
			return null;
		}
		
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
			
			pattern.append(SPACE_PATTERN).append("(" +sa.getPattern() + ")");
		}
		else if( sa.getSearchType().equals(SearchAlgo.SearchType_Before))
		{
			pattern.append("(" +sa.getPattern() + ")").append(SPACE_PATTERN);
			
			if( null != sa.getSecondKeyword() )
				pattern.append(sa.getSecondKeyword());
		}
		else if( sa.getSearchType().equals(SearchAlgo.SearchType_Between))
		{
			if( null != sa.getFirstKeyword() )
				pattern.append(sa.getFirstKeyword());
			
			pattern.append(SPACE_PATTERN).append("(" +sa.getPattern() + ")").append(SPACE_PATTERN);
			
			if( null != sa.getSecondKeyword() )
				pattern.append(sa.getSecondKeyword());
		}
		else if( sa.getSearchType().equals(SearchAlgo.SearchType_Anywhere))
		{
			// no first or second is valid here. Any string that matches the pattern in the complete text will be returned.
			// even if the string does not have any space delimiter.
			pattern.append("(" + sa.getPattern() + ")");
		}
		
		String patternStr = pattern.toString();
		logger.info("Search for pattern : " + pattern);

		Pattern p = Pattern.compile(patternStr);
		
		ArrayList<String> numbers = new ArrayList<String>();
		
		Matcher matcher = p.matcher(text);
		
		while( matcher.find() )
		{
			 numbers.add(matcher.group(1));
		}
		
		logger.info("Found the numbers : " + numbers);
		return numbers;
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
						}
						else first = false;
						
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
	 * checks if the input pdf file is A4 portrait
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static boolean isFirstPageA4(File file) throws IOException
	{ 
		PDDocument pdfDoc = null ;
		try
		{
			pdfDoc = PDDocument.load(file);
			PDDocumentCatalog docCat = pdfDoc.getDocumentCatalog();
			List<PDPage> pages = docCat.getAllPages();
			PDPage page = pages.get(0);
			
//			PDRectangle box = page.findCropBox(); // [0.0,0.0,596.0,843.0] // TODO :assuming crop-box tells right Ax size
//			PDRectangle box = page.findMediaBox(); // TODO :assuming crop-box tells right Ax size
			PDRectangle box = page.getTrimBox(); // [0.0,0.0,596.0,843.0]
			int height = (int) box.getHeight();
			int width = (int) box.getWidth();
			// have to take a little approximation to find 
			// as float values are a little dicy
			if(height <= (A4PortraitHeight + 1) 
			&& height >= (A4PortraitHeight - 1)
			&& width <= (A4PortraitWidth + 1)
			&& width >= (A4PortraitWidth - 1)
			) //[0.0,0.0,595.27563,841.8898]
			{
				// this is an A4 size paper
				return true;
			}
			
			return false;
		}
		finally
		{
			if( null != pdfDoc )
				pdfDoc.close();
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

	/**
	 * @param is
	 * @param os
	 * @throws IOException 
	 */
	public static void writeStream(InputStream is, OutputStream os) throws IOException {
		int blockSize = 1024;
		byte[] array = new byte[blockSize];
		
		int len = 0 ;
		while( (len = is.read(array) ) != -1 )
		{
			os.write(array, 0, len);
		}
	}
	
	public static void main(String argv[])
	{
//		File file = new File("/home/nitiraj/work/DC/12_oct_ddc_files_solomonnita/600MP0053-02012-DR-IN-0046_0_2.pdf");
//		try {
//			if( isFirstPageA4(file) )
//				System.out.println("A4");
//			else
//				System.out.println("NOT A4");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		try {
				
			File file = new File("/home/nitiraj/work/DC/remoredtnsofdifferentprojectsrequired_/");
			for( File f : file.listFiles() )
			{
				String content;
				content = extractContent(f);
				File txtFile = new File(f.getAbsolutePath() + ".txt");
				if( !txtFile.exists() )
					txtFile.createNewFile();
				
				FileWriter fos = new FileWriter(txtFile);
				fos.append(content);
				fos.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TikaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param ps
	 * @param index
	 * @param dtnExpected
	 * @throws SQLException 
	 */
	public static void setNotNull(PreparedStatement ps, int index,
			Boolean boolValue) throws SQLException {
		if( null == boolValue )
			ps.setNull(index, Types.BOOLEAN);
		else
			ps.setBoolean(index, boolValue);
	}

	/**
	 * @param ps
	 * @param index
	 * @param primaryRecordSearchFieldId
	 * @throws SQLException 
	 */
	public static void setNotNull(PreparedStatement ps, int index,
			Integer intValue) throws SQLException {
		if( null == intValue )
			ps.setNull(index, Types.INTEGER);
		else
			ps.setInt(index, intValue);
	}

	/**
	 * @param rs
	 * @param string
	 * @return
	 * @throws SQLException 
	 */
	public static String getNotNULL(ResultSet rs, String value) throws SQLException {
		return (rs.wasNull() ? null : value);
	}

	/**
	 * @param rs
	 * @param value
	 * @return
	 * @throws SQLException 
	 */
	public static Integer getNotNULL(ResultSet rs, int value) throws SQLException {
		return (rs.wasNull() ? null : value);
	}

	
	/**
	 * @param rs
	 * @param long1
	 * @return
	 * @throws SQLException 
	 */
	public static Long getNotNULL(ResultSet rs, long value) throws SQLException {
		return (rs.wasNull() ? null : value);
	}

	/**
	 * @param rs
	 * @param boolean1
	 * @return
	 * @throws SQLException 
	 */
	public static Boolean getNotNULL(ResultSet rs, boolean value) throws SQLException {
		return (rs.wasNull() ? null : value);
	}

}
