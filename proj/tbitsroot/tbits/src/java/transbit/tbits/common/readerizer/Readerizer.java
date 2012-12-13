/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



/*
 * Readerizer.java
 *
 * $Header:
 */
package transbit.tbits.common.readerizer;

//~--- non-JDK imports --------------------------------------------------------

//POI Imports.
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.activation.MimetypesFileTypeMap;

import transbit.tbits.common.Configuration;
import transbit.tbits.common.StreamGobbler;
import transbit.tbits.common.TBitsLogger;

//~--- classes ----------------------------------------------------------------

/**
 * This class extracts text from different types of files. It employs special
 * text extractors for the following types of files:
 * <code>
 * <ul>
 *      <li>PDF -> PDFBox</li>
 *      <li>DOC -> Antiword</li>
 *      <li>RTF -> Java Swing's RTFEditorKit</li>
 *      <li>XLS -> xls2csv</li>
 *      <li>XLT -> xls2csv</li>
 *      <li>XLA -> xls2csv</li>
 *      <li>WKS -> xls2csv</li>
 *      <li>PPT -> Apache POI's PowerpointExtractor</li>
 *      <li>PPS -> Apache POI's PowerpointExtractor</li>
 * </ul>
 * </code>
 * The following types of files are considered as text files during extraction:
 * <CODE>
 *      <ul>
 *              <LI>asp</LI>
 *              <LI>bas</LI>
 *              <LI>csv</LI>
 *              <LI>java</LI>
 *              <LI>js</LI>
 *              <LI>jsp</LI>
 *              <LI>log</LI>
 *              <LI>msg</LI>
 *              <LI>pl</LI>
 *              <LI>pm</LI>
 *              <LI>properties</LI>
 *              <LI>sql</LI>
 *              <LI>txt</LI>
 *              <LI>xml</LI>
 *              <LI>rsl</LI>
 *              <LI>xsl</LI>
 *      </ul>
 * </CODE>
 *
 * @author   : Vaibhav
 * @version  : $Id: $
 */
public class Readerizer {
    
    // Name of the logger.
    public static final String LOGGER_NAME = "indexer";
    
    // Name of the logger.
    public static final String PACKAGE_NAME = "transbit.tbits.common";
    
    // Application Logger.
    public static final TBitsLogger LOG           =  TBitsLogger.getLogger(LOGGER_NAME, PACKAGE_NAME);
    
    // Default Mime Type.
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	private static final String MIME_READERIZERS = "mime_readerizers.properties";
    
    /*private static String           EXTRACT_PATH  =  Configuration.findAbsolutePath
    													(PropertiesHandler.getProperty(TBitsPropEnum.KEY_PATH_EXTRACT)); // "/usr/local/bin/xls2csv";
    private static String           ANTIWORD_PATH = Configuration.findAbsolutePath
    													(PropertiesHandler.getProperty(TBitsPropEnum.KEY_PATH_ANTIWORD)); //"/usr/local/bin/antiword";
    */
    //~--- methods ------------------------------------------------------------
    
    /**
     * This is the main method, the entry point into the program.
     *
     * @param arg List of command-line arguments.
     */
    public static void main(String arg[]) {
        if (arg.length != 1) {
            System.out.println("Usage: Readerize <FileName>");
            return;
        }
        
        String fileName = arg[0];
        
        try {
            Reader reader = readerize(fileName);
            
            if (reader == null) {
                LOG.info("Null Reader returned.");
                
                return;
            }
            
            BufferedReader br     = new BufferedReader(reader);
            int            size   = 1024 * 1024;
            char[]         ch     = new char[size];
            int            read   = 0;
            StringBuffer   buffer = new StringBuffer();
            
            while ((read = br.read(ch, 0, size)) >= 0) {
                buffer.append(ch, 0, read);
            }
            
            br.close();
            System.out.println(buffer);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        return;
    }
    
    /**
     * This method is used to read text from an input stream.
     *
     * @param aInputStream InputStream from which text should be read.
     *
     * @exception Throws IOException
     */
    private static String readStream(InputStream aInputStream) throws IOException {
        StringBuffer buffer = new StringBuffer();
        
        if (aInputStream == null) {
            return buffer.toString();
        }
        
        BufferedReader br   = new BufferedReader(new InputStreamReader(aInputStream));
        int            size = 1024 * 1024;
        char[]         ch   = new char[size];
        int            read = 0;
        
        while ((read = br.read(ch, 0, size)) >= 0) {
            buffer.append(ch, 0, read);
        }
        
        br.close();
        
        return buffer.toString();
    }
    
    /**
     * This method retuns the Reader for a given File.
     *
     */
    public static Reader readerize(String aFileName) 
    {

    	if ((aFileName == null) || aFileName.equals("")) 
        return null;
    	
    	int lastDotIndex = aFileName.lastIndexOf('.');
    	if( lastDotIndex == -1 || lastDotIndex + 1 == aFileName.length() )
    		return null ;
    	
    	File attFile = new File(aFileName);
    	if(!attFile.exists())
    		return null;
    	
    	String extension = aFileName.substring(lastDotIndex + 1).toLowerCase();
    	
    	try {
			Properties properties = new Properties();
			
			InputStream is = new Readerizer().getClass().getClassLoader().getResourceAsStream(MIME_READERIZERS);
			if(is == null){
				throw new Exception("Unable to load " + MIME_READERIZERS);
			}
			
			properties.load(is);
			if(properties.containsKey(extension)){
				IReaderizer readerizer = (IReaderizer) Class.forName((String) properties.get(extension)).newInstance();
				return (Reader)readerizer.getReader(attFile);
			}
			else{
				System.out.println("Unable to find readerizer class orresponding to ."+extension);
				return null;
			}
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
		
		
			
//            String extension = aFileName.substring(lastDotIndex + 1).toLowerCase();
//            
//            try {
//	                if (extension.equals("shtml") || extension.equals("html") || extension.equals("mht") || extension.equals("htm")) {
//	                    // HTMLREADERIZER
//	                }
//	                else if( extension.equals("doc") || extension.equals("docx") 
//	                		|| extension.equals("xls") || extension.equals("xlsx")
//	                		|| extension.equals("ppt") || extension.equals("pptx")                		
//	                	   )
//	                {
//	                	// DOCREADERIZER
//	            		
//	                }
//	                else if ( extension.equals("xla") || extension.equals("wks") || extension.equals("xlt")) {
//	                    // EXCELREADERIZER
//	                } 
//	                else if ( extension.equals("pps") ) {
//	                    // PPTREADERIZER
//	                } 
//	                else if (extension.equals("pdf")) {
//	                    // PDFREADERIZER
//	                }
//	                else if (extension.equals("rtf")) {
//	                    // RTFREADERIZER
//	            	}
//	                else if (extension.equals("asp") || extension.equals("bas") || extension.equals("csv") || extension.equals("java") || extension.equals("js") || extension.equals("jsp")
//	                || extension.equals("log") || extension.equals("msg") || extension.equals("pl") || extension.equals("pm") || extension.equals("properties") || extension.equals("sql")
//	                || extension.equals("txt") || extension.equals("xml") || extension.equals("rsl") || extension.equals("xsl"))
//	                    
//	                    // Treat this as a normal text file and try to readerize it
//	                {
//	                    // TEXTREADERIZER
//	                }
//            } catch (Exception e) {
//                LOG.error(e.getMessage());
//                e.printStackTrace();
//            }
//            catch( Throwable t)
//            {
//            	t.printStackTrace();
//            }
    }
    /**
     * This method returns a Reader object for the given text.
     *
     * @param aText Text to be readerized.
     *
     * @return Reader object
     */
    public static Reader readerizeBareText(String aText) {
        Reader sr = new StringReader(aText);
        
        return sr;
    }
    
    /**
     * This method returns a Reader object for the given XLS Document.
     *
     * @param aFile Document that can be readerized using extract utility.
     *
     * @return Reader object
     */
    private static Reader readerizeExcelFilesCmd(String aFile) throws InterruptedException, IOException {
        Process proc   = null;
        String  output = "";
        
        try {
            String cmd = ""; //"\"" ;//+ EXTRACT_PATH + "\" \"" + aFile +"\"";
            
            proc = Runtime.getRuntime().exec(cmd);
            
            StreamGobbler errorStream = new StreamGobbler(proc.getErrorStream());
            
            errorStream.start();
            
            // Read the output of the process
            output = readStream(proc.getInputStream());
            
            // Wait for the process to complete.
            int exitVal = proc.waitFor();
            
            // Now read the error message if any.
            String error = errorStream.getMessage();
            
            if (exitVal != 0) {
                LOG.info("Exit Value: " + exitVal);
                LOG.info("Error: " + error);
            }
        } finally {
            if (proc != null) {
                terminateProcess(proc);
            }
        }
        
        return new StringReader(output);
    }
    
    /**
     * This is just a dummy implementation which should be later replaced
     * by the actual implementation.
     * 
     * @param htmlReader : the reader for the html content
     * @return the reader which gives text inside the html
     */
    public static Reader extractTextFromHtml( Reader htmlReader )
    {
    	return htmlReader ;
    }
    
    /**
     * This method closes the input, output and error streams of the process
     * and calls destroy on it.
     *
     * @param aProc Process to be terminated.
     */
    private static void terminateProcess(Process aProc) {
        
        // Closing the input stream.
        try {
            InputStream is = aProc.getInputStream();
            
            if (is != null) {
                is.close();
            }
        } catch (Exception e) {
            LOG.info("",(e));
        }
        
        // Closing the output stream.
        try {
            OutputStream os = aProc.getOutputStream();
            
            if (os != null) {
                os.close();
            }
        } catch (Exception e) {
            LOG.info("",(e));
        }
        
        // Closing the Error stream.
        try {
            InputStream is = aProc.getErrorStream();
            
            if (is != null) {
                is.close();
            }
        } catch (Exception e) {
            LOG.info("",(e));
        }
        
        aProc.destroy();
    }
    
    public static String getMimeType(File file)
    {
    	File f = Configuration.findPath("etc/mimetypes.default");
    	String path = f.getAbsolutePath();
    	MimetypesFileTypeMap mtftm;
		try {
			mtftm = new MimetypesFileTypeMap(path);
		} catch (IOException e) {
			LOG.warn("file '"+ path +"' not found. Using the default mimetypes from activation.jar");
			mtftm = new MimetypesFileTypeMap();
		}
    	String mimeType = mtftm.getContentType(file);
    	if((mimeType == null) || (mimeType.length() == 0))
    		mimeType = DEFAULT_MIME_TYPE;
    	return mimeType;
    }
}
