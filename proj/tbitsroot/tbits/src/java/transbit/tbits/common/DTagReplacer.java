/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



/**
 * DTagReplacer.java
 *
 * $Header:
 */
package transbit.tbits.common;

//~--- non-JDK imports --------------------------------------------------------

//Current Package Imports
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.CaptionsProps;

//~--- JDK imports ------------------------------------------------------------

//Java imports
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

//~--- classes ----------------------------------------------------------------

/**
 * This class reads a file with DTags and replaces them with the specified
 * text. A DTag is of the form &lt;%=TAG%&gt;
 *
 * @author  : Vinod Gupta
 * @version : $Id: $
 */
public class DTagReplacer {

    /*
     * Map that stores the contents of files read to avoid reading again.
     */
    private static HashMap<String, String> ourFileContentMap = new HashMap<String, String>();
    private static boolean                 ourCacheContent   = false;

    // The Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger("transbit.tbits.common");

    //~--- static initializers ------------------------------------------------

    static {

        /*
         * Set the value of cache content only if you don't find the system
         * property by name app.devel.
         */
        String value = System.getProperty("app.devel");

        if ((value == null) || (value.trim().equals("") == true)) {
            ourCacheContent = true;
        } else {
            LOG.info("Content will not be cached for developer instances");
        }
    }

    //~--- fields -------------------------------------------------------------

    // default variables
    private HashMap<String, String> myHash = new HashMap<String, String>();
    private String                  myHtmlFileContent;

    //~--- constructors -------------------------------------------------------

    /**
     * The Constructor that takes the html file path as the input.
     *
     * @param aHtmlFile relative/absolute path of the file.
     *
     * @exception FileNotFoundException If the file is not found.
     * @exception IOException  If an error occurs while reading the file
     */
    public DTagReplacer(String aHtmlFile) throws FileNotFoundException, IOException {
    	File file = Configuration.findPath(aHtmlFile);

    	if (file == null) {
    		throw new FileNotFoundException(aHtmlFile + " is not found.");
    	}

    	this.dTagReplacer(file);
    }
    
    /**
     * The Constructor that takes the html file as the input.
     *
     * @param aHtmlFile relative/absolute path of the file.
     *
     * @exception FileNotFoundException If the file is not found.
     * @exception IOException  If an error occurs while reading the file
     */
    public DTagReplacer (File file) throws FileNotFoundException, IOException{
    	if (file == null)
    		throw new FileNotFoundException("Could not find the file.");
    	
    	this.dTagReplacer(file);
    }

	/**
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void dTagReplacer(File file) throws FileNotFoundException,
			IOException {
		/*
         * Check if the content of this file already read by DTagReplacer before
         * If not read the content and store it in ourFileContentMap.
         */
        myHtmlFileContent = ourFileContentMap.get(file.toString());

        if (myHtmlFileContent == null) {
            long           start = System.currentTimeMillis();
            BufferedReader fr    = null;

            try {
                fr = new BufferedReader(new FileReader(file));

                StringBuilder content = new StringBuilder();
                String        aLine   = "";

                while ((aLine = fr.readLine()) != null) {
                    content.append(aLine).append("\n");
                }

                myHtmlFileContent = content.toString();

                long end = System.currentTimeMillis();

                /*
                 * Store the content of this file in the static map for future
                 * use.
                 */
                if (ourCacheContent == true) {
                    ourFileContentMap.put(file.toString(), myHtmlFileContent);
                }
            } finally {
                if (fr != null) {
                    fr.close();
                }
            }
        } else {

            // Need not reading the file again.
        }
	}

    //~--- methods ------------------------------------------------------------

    /**
     * A mutator method to clear the file content map.
     */
    public static void clearFileContentMap() {
        ourFileContentMap.clear();
    }

    /*
     * Pass 0 as systemId to load the defaults for all bas
     */
    public String parse(int systemId)
    {
    	//Fill the captions first
    	String newContent = parse(myHtmlFileContent, CaptionsProps.getInstance().getCaptionsHashMap(systemId));
    	
    	///then by variables
    	return parse(newContent, myHash);
    }
  
    /**
     * Use when you just want to replace your tags in your "tagged" file
     * @return the 'tag-replaced' String or return NULL if the file content or the hash is null
     */
    public String parse()
    {
    	if( myHtmlFileContent != null && myHash != null )
    		return parse( myHtmlFileContent, myHash ) ;
    	
    	else return null ;
    }
    /**
     * Method that parses the html file content and replaces the key
     * with the value.
     * NOTE:
     * 1. <%xyz%> should be in the same line
     * 2. We can not display <%xyz%> in the output.
     *
     * @return the replaced file as a string.
     */
    public String parse(String aContent, HashMap <String, String> aVars) {
        StringBuilder content = new StringBuilder();
        String        prefixString, suffixString;
        Pattern       p1    = Pattern.compile("\r?\n");
        String[]      lines = p1.split(aContent);

        for (int i = 0; i < lines.length; i++) {
            suffixString = lines[i];

            // Get the index where the tag opens.
            int tagOpen = suffixString.indexOf("<%=");

            while (tagOpen != -1) {

                // Get the index where the tag closes.
                int tagClose = suffixString.indexOf("%>", tagOpen);

                //
                // String in between the tag-start and tag-close is the
                // key.
                //
                String key = suffixString.substring(tagOpen + 3, tagClose);

                // Get the value for the key.
                String value = aVars.get(key);
                // String before the tag-start is prefix.
                prefixString = suffixString.substring(0, tagOpen);

                // Append the prefix string to the final content.
                content.append(prefixString);
             
                //
                // Now append the value to the final content if you get
                // it from the hash. Otherwise print an info statement
                // and continue with the process.
                //
                if (value == null) {
                    content.append("<%=").append(key).append("%>");
                } else {
                    content.append(value);
                }

                // String after the tag-close is suffix.
                suffixString = suffixString.substring(tagClose + 2);

                // Do the above procedure for the rest of the string.
                tagOpen = suffixString.indexOf("<%=");
            }

            // Now append the suffixString to the final content before you
            // move to the next line.
            content.append(suffixString).append("\n");
        }
        return content.toString();
    }

    /**
     * Method that replaces the key with the corresponding value.
     *
     * @param aKey   the key which has to be replaced in the html file.
     * @param aValue the value with which the key has to be replaced.
     */
    public void replace(String aKey, String aValue) {
        if (aKey == null) {
            return;
        }

        if (aValue == null) {
            aValue = "";
        }

        myHash.put(aKey, aValue);
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Mutator method for the myHash property
     *
     * @param aHash the new value of Hash table property
     */
    public void setHash(HashMap<String, String> aHash) {
        myHash = aHash;
    }

    /**
     * Mutator method for the myHtmlFileContent property
     *
     * @param aContent the new value of Html contents property
     */
    public void setHtmlFileContents(String aContent) {
        myHtmlFileContent = aContent;
    }
    
    public String getFileContents()
    {
    	return myHtmlFileContent ;
    }
}
