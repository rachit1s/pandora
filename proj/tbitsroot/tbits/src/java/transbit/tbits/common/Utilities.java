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
 * Utilities.java
 *
 * $Header:
 */
package transbit.tbits.common;

//~--- non-JDK imports --------------------------------------------------------

//Third Party Imports.
import org.apache.log4j.MDC;

import transbit.tbits.config.CaptionsProps;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

//~--- classes ----------------------------------------------------------------

/**
 * This class provides utility services that are generally used in web
 * applications
 *
 * @author  : Vaibhav, Vinod Gupta
 * @version : $Id: $
 */
public class Utilities {

	/*
	 * @return "" if s is null otherwise s;
	 */
	public static String checkNull(String s)
	{
		return (s == null)? "":s;
	}
    /**
     * Method to convert the ArrayList into comma seperated string
     *
     * @param aArrayList the ArrayList of strings
     * @return String the string, comma seperated
     */
    public static String arrayListToString(ArrayList<String> aArrayList) {
        if (aArrayList == null) {
            return "";
        }

        StringBuilder sb    = new StringBuilder();
        boolean       first = true;

        for (String str : aArrayList) {
            if (first == false) {
                sb.append(",");
            } else {
                first = false;
            }

            sb.append(str);
        }

        return sb.toString();
    }

    /**
     * This method clears the MDC Parameters set earlier, in this thread.
     */
    public static void clearMDCParams() {
        MDC.remove("HOST_NAME");
        MDC.remove("HOST_ADDR");
        MDC.remove("USER_NAME");
        MDC.remove("SERVER_NAME");
        MDC.remove("SYS_PREFIX");
        MDC.remove("REQUEST_ID");
    }

    /**
     * This method adds a space  a trailing back slash ocuuring just before
     * a line break, because sql otherwise merges the two lines.
     *
     * @param  aString the string to be processed
     * @return the resultant string
     */
    public static String hideTrailingSlash(String aString) {
        if ((aString == null) || aString.trim().equals("")) {
            return aString;
        }

        aString = aString.replaceAll("\\\\n", "\\\\n\\\n").replaceAll("\\\\r\\n", "\\\\r\\n\\\n").replaceAll("\\\\r", "\\\\r\\\n");

        return aString;
    }

    /**
     * This method html-decodes the given string and returns this decoded one.
     * The characters that are decoded are
     * <ul>
     *    <li> '&lt;'
     *    <li> '&gt;'
     *    <li> '&quot;'
     *    <li> '&amp;'
     * </ul>
     *
     * @param  aString the string to be html decoded
     * @return the resultant decoded string
     */
    public static String htmlDecode(String aString) {
        if ((aString == null) || aString.trim().equals("")) {
            return aString;
        }

        aString = aString.replaceAll("&lt;", "<");
        aString = aString.replaceAll("&gt;", ">");
        aString = aString.replaceAll("&quot;", "\"");

        // Order of decoding is important here. Why?
        aString = aString.replaceAll("&amp;", "&");

        return aString;
    }

    /**
     * This method html-encodes the given string and returns the encoded one.
     * The characters that are encoded are
     * <ul>
     *    <li> '&gt;'
     *    <li> '&lt;'
     *    <li> '&quot;'
     *    <li> '&amp;'
     * </ul>
     * At the same time, it does not encode &amp;# entities assuming these to
     * be representations of unicode characters.
     *
     * @param  aString the string to be html encoded
     * @return the resultant encoded string
     */
    public static String htmlEncode(String aString) {
        if ((aString == null) || aString.trim().equals("")) {
            return aString;
        }

        // Order of encoding is important here. Why?
        // &#... are unicode characters encoded. Let us not touch them. 
        // SG: Why shouldnt we touch them. This is the plain text and objective should to show the text as it 
        // SG: should be shown in text editor.
        aString = aString.replaceAll("&([^#])", "&amp;$1");
        aString = aString.replaceAll("<", "&lt;");
        aString = aString.replaceAll(">", "&gt;");
        aString = aString.replaceAll("\"", "&quot;");

        return aString;
    }

    /**
     * Main method for testing.
     *
     */
    public static void main(String arg[]) {
        System.out.println(getLocalHost());

        if (arg.length > 0) {
            System.out.println(getHostName(arg[0]));
        }
    }

    /**
     * This method sets the following parameters of the MDC (Mapped Diagnostic
     * Context) for this thread for the Logget to access this.
     * <UL>
     *    <LI>Client's Hostname</LI>
     *    <LI>Client's Address </LI>
     *    <LI>Client's Username</LI>
     * </UL>
     *
     * @param aRequest HttpServlet Request Object
     *
     */
    public static void registerMDCParams(HttpServletRequest aRequest) {
        String hostName   = aRequest.getRemoteHost();
        String hostAddr   = aRequest.getRemoteAddr();
        String userName   = aRequest.getRemoteUser();
        String serverName = aRequest.getServerName();

        hostName   = (hostName == null)
                     ? ""
                     : hostName.trim();
        hostAddr   = (hostAddr == null)
                     ? ""
                     : hostAddr.trim();
        userName   = (userName == null)
                     ? ""
                     : userName.trim();
        serverName = (serverName == null)
                     ? ""
                     : serverName.trim();
        hostName   = getHostName(hostName);
        MDC.put("HOST_NAME", hostName);
        MDC.put("HOST_ADDR", hostAddr);
        MDC.put("USER_NAME", userName);
        MDC.put("SERVER_NAME", serverName);

        return;
    }

    /**
     * This method sets the following key, value pair in the MDC
     * (Mapped Diagnostic Context) of this thread for the Logget to access this.
     *
     * @param aKey   key
     * @param aValue value.
     */
    public static void registerMDCParams(String aKey, String aValue) {
        MDC.put(aKey, aValue);

        return;
    }

    /**
     * This method sets the following parameters of the MDC (Mapped Diagnostic
     * Context) for this thread for the Logget to access this.
     * <UL>
     *    <LI>User Name</LI>
     *    <LI>Sys Prefix </LI>
     *    <LI>Request</LI>
     * </UL>
     *
     * @param userLogin Login name of the user.
     * @param sysPrefix System Prefix.
     * @param requestId Request Id.
     *
     */
    public static void registerMDCParams(String userLogin, String sysPrefix, String requestId) {
        userLogin = (userLogin == null)
                    ? "N/A"
                    : userLogin.trim();
        sysPrefix = (sysPrefix == null)
                    ? "N/A"
                    : sysPrefix.trim();
        requestId = (requestId == null)
                    ? "N/A"
                    : requestId.trim();

        String serverName = getLocalHost();

        MDC.put("HOST_NAME", "[Not Applicable]");
        MDC.put("HOST_ADDR", "[Not Applicable]");
        MDC.put("USER_NAME", userLogin);
        MDC.put("SERVER_NAME", serverName);
        MDC.put("SYS_PREFIX", sysPrefix);
        MDC.put("REQUEST_ID", requestId);
    }

    /**
     * This method sorts the given list in alphabetical order.
     *
     * @param list
     *
     * @return Returns the sorted list.
     */
    public static ArrayList<String> sort(ArrayList<String> list) {

        // Check for nulls.
        if (list == null) {
            return list;
        }

        // Form an array from the list.
        String[] arr = new String[list.size()];

        list.toArray(arr);

        // Sort the array.
        Arrays.sort(arr);

        // Put back the array into a list and return.
        list = new ArrayList<String>();

        for (int i = 0; i < arr.length; i++) {
            list.add(arr[i]);
        }

        return list;
    }

    /**
     * Method to convert the comma seperated string into an ArrayList.
     *
     * @param aString    Comma separated string.
     * @return ArrayList Arraylist containing all the strings
     */
    public static ArrayList<String> toArrayList(String aString) {

        // Default delimiter is ","(Comma).
        return toArrayList(aString, ",");
    }

    /**
     * Method to convert the comma seperated string to ArrayList.
     *
     * @param aString    the string with substrings separated by the given
     *                   separator
     * @param aSeparator the separators string
     * @return ArrayList the array list containing all the strings
     */
    public static ArrayList<String> toArrayList(String aString, String aSeparator) {
        ArrayList<String> arrayList = new ArrayList<String>();

        if ((aString == null) || (aString.equals("") == true)) {
            return arrayList;
        }

        StringTokenizer st = new StringTokenizer(aString, aSeparator);

        while (st.hasMoreTokens() == true) {
            String string = st.nextToken().trim();

            if (string.trim().equals("") == false) {
                arrayList.add(string);
            }
        }

        return arrayList;
    }

    /**
     * This method returns the comma separated list of the strings.
     *
     * @param aList the ArrayList of strings.
     * @return String Comma-separated string.
     */
    public static String toCSS(ArrayList<String> aList) {
        StringBuilder buffer = new StringBuilder();
        boolean       first  = true;

        for (String str : aList) {
            if (first == false) {
                buffer.append(",");
            } else {
                first = false;
            }

            buffer.append(str);
        }

        return buffer.toString();
    }

    /**
     * This method returns a Hashtable where keys are strings from the
     * specified array list and values are boolean true.
     *
     * @param aList the ArrayList of strings.
     * @return Table
     */
    public static Hashtable<String, Boolean> toHash(ArrayList<String> aList) {
        Hashtable<String, Boolean> table = new Hashtable<String, Boolean>();

        if (aList != null) {
            for (String str : aList) {
                table.put(str.toLowerCase(), true);
            }
        }

        return table;
    }

    /**
     * This method returns the comma separated list of the strings enclosed in
     * in quotes.
     *
     * @param aList the ArrayList of strings.
     * @return String Comma-separated string.
     */
    public static String toQuotedCSS(ArrayList<String> aList) {
        StringBuilder buffer = new StringBuilder();
        boolean       first  = true;

        for (String str : aList) {
            if (first == false) {
                buffer.append(",");
            } else {
                first = false;
            }

            buffer.append("\"").append(str).append("\"");
        }

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the name of the Host given the IP Address.
     *
     * @param aIPAddress IP Address of the host.
     *
     * @return Name of the host.
     */
    public static String getHostName(String aIPAddress) {
        String hostName = aIPAddress;

        try {
            InetAddress ia = InetAddress.getByName(aIPAddress);

            hostName = ia.getHostName();
        } catch (Exception e) {
            hostName = aIPAddress;
        }

        return hostName;
    }

    /**
     * This method returns the name of the local host
     *
     * @return Hostname.
     */
    public static String getLocalHost() {
        String hostname = "";

//        try {
//            Process     process = Runtime.getRuntime().exec("hostname");
//            InputStream is      = process.getInputStream();
//            int         i       = 0;
//
//            while ((i = is.read()) != -1) {
//                hostname.append((char) i);
//            }
//
//            is.close();
//            process.destroy();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try
		{
			hostname = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException uhe)
		{
			uhe.printStackTrace();
		}
        return hostname;
    }
    
    /*
     * Copies a file from one location to another. If the target file or its parent doesnt exist, it is created.
     */
    public static void copyFile(File srcFile, File targetFile) throws IOException {
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile(); //if necessary, creates the target file
        
        FileChannel srcChannel = new FileInputStream(srcFile).getChannel();
        FileChannel dstChannel = new FileOutputStream(targetFile).getChannel();
        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
        
        srcChannel.close();
        dstChannel.close();
    }
    
    /*
     * Copies file in chunks of 1KB. This is slower but more reliable.
     */
    public static void copyFileSlow(File srcFile, File targetFile) throws IOException {
    	 targetFile.getParentFile().mkdirs();
         targetFile.createNewFile(); //if necessary, creates the target file
         
        FileInputStream in = new FileInputStream(srcFile);
        FileOutputStream out = new FileOutputStream(targetFile);
        byte buffer[] = new byte[1024];
        int read = -1;
        while ((read = in.read(buffer, 0, 1024)) != -1) {
           out.write(buffer, 0, read);
        }
        out.flush();
        out.close();
        in.close();
     }
    
    /*
     * Creates a file with prposedPrefix and proposedSuffix such 
     * that proposedPrefix[n].proposedSuffix such that the file doesnt already exist.
     * n can be "", "1", "2", ...
     * This was required because the File.createTempFile introduces some random number.
     */
	public static File createTempFile(String proposedPrefix,
			String proposedSuffix, File parentDir) throws IOException {
		File f = new File(parentDir, proposedPrefix + "." + proposedSuffix);
		
		int counter = 1;
		while(!f.createNewFile())
		{
			f = new File(parentDir, proposedPrefix + counter++ + "." + proposedSuffix);
		}
		return f;
	}
	
	/**
	 * Returns the caption value based on the systemId if a value is found else will return the generic value.
	 * @param aSystemId
	 * @param captionName
	 * @return
	 */
	public static String getCaptionBySystemId(int aSystemId, String captionName){
		String linkCaption = "";
		HashMap<String, String> captionsHash = CaptionsProps.getInstance().getCaptionsHashMap(aSystemId);
		linkCaption = captionsHash.get(captionName);		
		return linkCaption;
	}
	
	public static String trimNonVisibleCharacters(String fieldName){
		Pattern p = Pattern.compile("[^\\p{Graph}]");
		Matcher m = p.matcher(fieldName);
		String trimmedOne=m.replaceAll("");
		return trimmedOne;
	
	}
}
