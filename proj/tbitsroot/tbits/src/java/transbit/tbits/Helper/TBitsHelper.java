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
 *
 * $Header:
 */
package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Mail;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.BAConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.TransferredRequest;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.webapps.WebUtil;

//~--- classes ----------------------------------------------------------------

/**
 * This class provides utility services used across various tbits classes.
 *
 *
 * @author : Vinod Gupta
 * @version : $Id: $
 */
public class TBitsHelper implements TBitsConstants {
    public static final TBitsLogger LOG      = TBitsLogger.getLogger(PKG_UTIL);
    public static final String[]    patterns = {
        "about", "after", "all", "also", "an", "and", "another", "any", "are", "as", "at", "be", "because", "been", "before", "being", "between", "both", "but", "by", "came", "can", "come", "could",
        "did", "do", "each", "for", "from", "get", "got", "has", "had", "he", "have", "her", "here", "him", "himself", "his", "how", "if", "in", "into", "is", "if", "like", "make", "many", "me",
        "might", "more", "most", "much", "must", "my", "never", "now", "of", "on", "only", "or", "other", "our", "out", "over", "said", "same", "see", "should", "since", "some", "still", "such",
        "take", "than", "that", "the", "their", "them", "then", "there", "these", "they", "this", "those", "through", "to", "too", "under", "up", "very", "was", "way", "we", "well", "were", "what",
        "where", "which", "while", "who", "with", "would", "you", "your", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y",
        "z", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"
    };
    public static String pattern = "";

    //~--- static initializers ------------------------------------------------

    static {
        int i = 0;

        while (i < patterns.length) {
            pattern += "(\\b" + patterns[i] + "\\b)|";
            i++;
        }
    }
    
    public static boolean isSMSEnabled()
    {
    	boolean  isSMSEnabled = false;
    	try
    	{
    		isSMSEnabled = Boolean.parseBoolean(PropertiesHandler.getProperty(TBitsPropEnum.IS_SMS_ENABLED));
    	}
    	catch(IllegalArgumentException e)
    	{
    		LOG.warn("Unable to find transbit.tbits.sms.enabled. Perhaps your configuration file is old. Switching off SMS.");
    	}
    	return isSMSEnabled;
    }
    /*
	 * Reads a file to the end and returns the string.
	 * Note: It removes all the trailing new line chars ignored by 
	 * BufferedReader.readLine() and adds a "\n" at the end of every line.
	 */
    private static String ReadFileToEnd(String file, boolean haveNewLines) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		String str;
		while ((str = br.readLine()) != null) {
			sb.append(str);
			if(haveNewLines)
				sb.append("\n"); //TODO: why \n?? or CRLF or \r?
		}
		br.close();
		return sb.toString();
	}
    
	public static String ReadFileToEnd(String file) throws IOException
	{
		return ReadFileToEnd(file, true);
	}
	
	/*
	 * Reads a file to the end and returns the string.
	 * It removes all the trailing new line chars ignored by BufferedReader.readLine().
	 */
	public static String ReadFileToEndAsSingleLine(String file) throws IOException
	{
		return ReadFileToEnd(file, false);
	}
	
    
    //~--- methods ------------------------------------------------------------

    /**
     * This method detects the source of invocation by checking the sytem
     * parameters passed on the command line.
     *
     * @param propValue
     *
     * @return Source value.
     */
    public static Source detectSource(StringBuilder propValue) {

        /*
         * Let us assume that this is an invocation from a web process.
         */
        Source source = Source.WEB;

        try {

            /*
             * Check if this is an email invocation.
             */
            String value = PropertiesHandler.getProperty(PROP_BA_NAME);

            source = Source.EMAIL;
            propValue.append(value);
        } catch (IllegalArgumentException iae) {

            // This is not for email invocation.
        }

        /*
         * If ourSource is still web, check if this is cmdline invocation.
         */
        if (source == Source.WEB) {
            try {

                /*
                 * Check if this is to be built for an email invocation.
                 */
                String value = PropertiesHandler.getProperty(PROP_BA_PREFIX);

                source = Source.CMDLINE;
                propValue.append(value);
            } catch (IllegalArgumentException iae) {

                /*
                 * This is not for command-line invocation.
                 */
            }
        }

        return source;
    }

    public static String formatText(String aSbString) {

        // Replace the \n with <br> and \t with 4 spaces.
        aSbString = aSbString.replaceAll("^[\n]+", "");
        aSbString = aSbString.replaceAll("\\r\\n", "<br>");
        aSbString = aSbString.replaceAll("\\n", "<br>");

        // aSbString = aSbString.replaceAll("\\r", "<br>");
        // aSbString = aSbString.replaceAll("<br>", "\n<br>");
        // aSbString = aSbString.replaceAll("<br> ", "<br>&nbsp;");
        aSbString = aSbString.replaceAll("  ", "&nbsp;&nbsp;");
        aSbString = aSbString.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");

        return aSbString;
    }

    public static final String escapeHTML(String s){
   StringBuffer sb = new StringBuffer();
   int n = s.length();
   for (int i = 0; i < n; i++) {
      char c = s.charAt(i);
      switch (c) {
         case '<': sb.append("&lt;"); break;
         case '>': sb.append("&gt;"); break;
         case '&': sb.append("&amp;"); break;
         case '"': sb.append("&quot;"); break;
         case ' ': sb.append("");break;         
         
         default:  sb.append(c); break;
      }
   }
   return sb.toString();
}
    public static void main(String[] args) {

        // String s1 = "Life it seems\n to fade away.Drifting further everyday.";
        // String s2 = "Life it seems is\n a lesson.Goes futher nevertheless.";
        // /*long start = new Date().getTime();
        // LOG.info("Diff is " + getDiffHtml(s1, s2));
        // long end = new Date().getTime();
        // LOG.info("Time taken "+ (end - start) + "msecs");*/
    }

    /**
     * This method sorts the given list of strings.
     *
     */
    public static ArrayList<String> sortList(ArrayList<String> list) {
        if ((list == null) || (list.size() == 0)) {
            return list;
        }

        Object[] arr = list.toArray();

        Arrays.sort(arr);
        list = new ArrayList<String>();

        for (int i = 0; i < arr.length; i++) {
            list.add((String) arr[i]);
        }

        return list;
    }

    public static void informExtUser(String emailId, String password, User aUser)
    {
    	boolean shouldSendWelcomeMail = false;
    	String sendWelcomeMailString =  PropertiesHandler.getProperty("transbit.tbits.mail.sendWelcomeMail");
    	if((sendWelcomeMailString != null) && (sendWelcomeMailString.trim().length() > 0))
    	{
    		try
    		{
    			shouldSendWelcomeMail = Boolean.parseBoolean(sendWelcomeMailString);
    		}
    		catch(Exception exp)
    		{
    			LOG.warn("Invalid value for " + "transbit.tbits.mail.sendWelcomeMail.", exp);
    		}
    	}
    	if(!shouldSendWelcomeMail)
    	{
    		LOG.info("Welcome Mail turned off. Skipping it for email: " + emailId);
    		return;
    	}
    	DTagReplacer hp = null;
		try {
			hp = new DTagReplacer("web/extuser-add-welcomemail.htm");
		} catch (FileNotFoundException e) {
			LOG.error(e);
		} catch (IOException e) {
			LOG.error(e);
		}
		if(hp == null)
			return;
		hp.replace("nearestPath", WebUtil.getNearestPath(""));
		hp.replace("email", emailId);
		hp.replace("password", password);
		if(aUser == null)
			System.out.println("informExtUser user is null");
		String s = aUser.getDisplayName();
		if(s == null)
			s = aUser.getUserLogin();
		hp.replace("current_user_display_name", s);
		String str = hp.parse(0);
		try {
			Mail.sendWithHtmlAndReplyTo(emailId, PropertiesHandler.getProperty(TBitsPropEnum.KEY_LOGGER_NOTIFY_FROM), "Welcome to tBits.", str, PropertiesHandler.getProperty(TBitsPropEnum.KEY_LOGGER_NOTIFY_FROM));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
    }
    
    public static User getAPIUser( String login ) throws DatabaseException
    {
    	User user = null ;
    	
    	// this is supposed to give active or inactive users    	
		user = User.lookupAllByUserLogin(login) ;
		
		if( null == user ) 	// if user not found. Then to comply with previous implementation try to find user with login as email-id
			user = User.lookupAllByEmail(login) ;
		
		if( user != null )
		{
			if( user.getIsActive() == false )		
			{
				LOG.info("The user : " + login + " is inactive.") ;
				return null ;
			}
			else
				return user ;
		}		
		else
    	{
			return getNewExternalUser( login ) ;
    	}
    }
    
    public static User getNewExternalUser( String name )
    {
    	//try to insert it
    	User user = null ;
//		boolean allowAutoAddUser = true;
        String allowAutoAddUserStr = null;        
        try 
        {
        	allowAutoAddUserStr = PropertiesHandler.getProperty(TBitsPropEnum.KEY_ALLOW_AUTO_ADD_USER);
		
	        if(allowAutoAddUserStr != null && allowAutoAddUserStr.equalsIgnoreCase("true"))
	        {        // create the user with id = -1 and External User
	        		System.out.println("Returning new external user: " + name);
	        		//TODO: if allowed
	        		user = new User(-1, // userId
	        				name, // userLogin
	        				name, // FirstName
	        				name, // LastName
	        				name, // DisplayName
	        				name, // Email
	        				true, // IsActive
	        				UserType.EXTERNAL_USER, // UserTypeId
	        				"", // WebConfig
	        				"", // WindowsConfig
	        				false, // OnVacation
	        				false, "", "", "", "", "", "", "", "", "", "");	        
	        }
	        
			return user;	            
        } 
        catch (IllegalArgumentException e) {
			LOG.warn("Unable to find '" + TBitsPropEnum.KEY_ALLOW_AUTO_ADD_USER 
					+ "' key in property file. Enabling default additions of subscribers as external users.");
			return null ;
		}        
    }
    /**
     * This method validates user by its email address/login, in DB or
     * as unix user by ypmatch.
     * IF address valid in DB, then it returns the corresponding User Object.
     * ELSE IF address valid transbit unix address(ypmatch), then it returns
     * the corresponding User Object, else return null.
     * ELSE return a new External User object.
     *
     * @param aAddress Users's email/login to be validated
     *
     * @exception DatabaseException
     * @exception IOExcepiton
     */
    public static User validateEmailUser(String aAddress) throws DatabaseException, IOException {
        
    	LOG.debug("Validating '" + aAddress);
        
    	if ((aAddress == null) || aAddress.trim().equals("")) {
            return null;
        }

        String originalAddress = aAddress;

        aAddress = aAddress.trim().toLowerCase();
        User   user       = null;
        String userLogin  = "";
        String userDomain = "";
        int    startIndex = aAddress.indexOf('<');
        int    endIndex   = aAddress.indexOf('>');

        if ((startIndex != -1) && (endIndex != -1)) {
            aAddress = aAddress.substring(startIndex + 1, endIndex).trim();
        }


        //
        // First look up by Email in Mapper/DB
        //
        user = User.lookupAllByEmail(aAddress);

        //
        // If user is inactive, return null
        //
        if (user != null )
        {
        	if(user.getIsActive() == false) 
        	{
        		return null;
        	}
        	
            return user ;
        }
        
        user = getNewExternalUser(aAddress) ;
        
        return user;
    }

    // ~--- get methods --------------------------------------------------------

    /**
	 * This method returns the diff between two Strings.
	 * 
	 * @param str1
	 * @param str2
	 * 
	 * @return Diff Html of the two Strings
	 */
    public static String getDiffHtml(String str1, String str2) {

        // LOG.severe("String 1"+str1);
        // LOG.severe("String 2"+str2);
        // Pattern p =
		// Pattern.compile("([^\\s]*\\s*[^\\s]*)|(\\s*[^\\s]*\\s*)");
        // Pattern p = Pattern.compile("[^\\s]+|[\\s]+");
        // Pattern p = Pattern.compile("[^\\s]+|[ \\t\\f\\r]+|[\\n]+]");
        Pattern       p  = Pattern.compile("[\\w]+|[\\W]");
        StringBuilder sb = new StringBuilder();

        // String[] strList1 = str2.split("[\\s && [^[\n][\r]+]]");
        // String[] strList2 = str1.split("[\\s && [^[\n][\r]+]]");
        // String[] strList1 = str2.split("\\b");
        // String[] strList2 = str1.split("\\b");
        // String[] strList1a = str2.split("\\s+");
        // String[] strList1b = str2.split("\\S+");
        // String[] strList2a = str1.split("\\s+");
        // String[] strList2b = str1.split("\\S+");
        ArrayList<TextDiff> array1 = new ArrayList<TextDiff>();
        ArrayList<TextDiff> array2 = new ArrayList<TextDiff>();

        /*
         * for(int i = 0 ; i < strList1.length ; i++)
         * {
         *   TextDiff t1 = new TextDiff();
         *   t1.setCurrentText(strList1[i]);
         *   array1.add(t1);
         *   LOG.severe("word "+strList1[i]);
         * }
         *
         * for(int i = 0 ; i < strList2.length ; i++)
         * {
         *   TextDiff t1 = new TextDiff();
         *   t1.setCurrentText(strList2[i]);
         *   array2.add(t1);
         *   }
         */
        Matcher m1 = p.matcher(str2);

        while (m1.find()) {
            String   strGroup = m1.group();
            TextDiff t1       = new TextDiff();

            t1.setCurrentText(strGroup);
            array1.add(t1);
        }

        Matcher m2 = p.matcher(str1);

        while (m2.find()) {
            String   strGroup = m2.group();
            TextDiff t1       = new TextDiff();

            t1.setCurrentText(strGroup);
            array2.add(t1);
        }

        Hashtable<String, TextDiff> strHash1 = new Hashtable<String, TextDiff>();
        Hashtable<String, TextDiff> strHash2 = new Hashtable<String, TextDiff>();

        for (int i = 0; i < array1.size(); i++) {
            if (strHash1.get(array1.get(i).getCurrentText()) == null) {
                strHash1.put(array1.get(i).getCurrentText(), new TextDiff());
            }

            strHash1.get(array1.get(i).getCurrentText()).pushArrayList(new Integer(i));
        }

        for (int i = 0; i < array2.size(); i++) {
            if (strHash2.get(array2.get(i).getCurrentText()) == null) {
                strHash2.put(array2.get(i).getCurrentText(), new TextDiff());
            }

            strHash2.get(array2.get(i).getCurrentText()).pushArrayList(new Integer(i));
        }

        Enumeration<String> enum1 = strHash1.keys();
        boolean             flag  = false;

        while (enum1.hasMoreElements()) {
            String  word = enum1.nextElement();
            Matcher m    = p.matcher(word);

            if ((word.matches(pattern) == false) && (

            // (word.matches("[\\s]*[\\w]+[\\s]*]") == false &&
            // word.length() >= 2)) &&
            strHash1.get(word) != null) && (strHash1.get(word).getArrayList().size() == 1) && (strHash2.get(word) != null) && (strHash2.get(word).getArrayList().size() == 1)) {
                flag = true;

                TextDiff td1 = new TextDiff();

                td1.setText(array1.get(strHash1.get(word).getArrayList().get(0)).getCurrentText());
                td1.setOther(strHash2.get(word).getArrayList().get(0));
                array1.set(strHash1.get(word).getArrayList().get(0), td1);

                TextDiff td2 = new TextDiff();

                td2.setText(array2.get(strHash2.get(word).getArrayList().get(0)).getCurrentText());
                td2.setOther(strHash1.get(word).getArrayList().get(0));
                array2.set(strHash2.get(word).getArrayList().get(0), td2);
            }
        }

        for (int i = 0; i < array1.size() - 1; i++) {
            TextDiff t1 = array1.get(i);
            TextDiff t2 = array1.get(i + 1);

            if ((t1.getText() != null) && (t2.getText() == null) && (t1.getOther() < (array2.size() - 1)) && (array2.get(t1.getOther() + 1).getText() == null)
                    && (t2.getCurrentText().equals(array2.get(t1.getOther() + 1).getCurrentText()))) {
                TextDiff td1 = new TextDiff();

                td1.setText(t2.getCurrentText());
                td1.setOther(t1.getOther() + 1);
                array1.set(i + 1, td1);

                TextDiff td2 = new TextDiff();

                td2.setText(array2.get(t1.getOther() + 1).getCurrentText());
                td2.setOther(i + 1);
                array2.set(t1.getOther() + 1, td2);
            }
        }

        for (int i = array1.size() - 1; i > 1; i--) {
            if ((array1.get(i).getText() != null) && (array1.get(i - 1).getText() == null) && (array1.get(i).getOther() > 0) && (array2.get(array1.get(i).getOther() - 1).getText() == null)
                    && (array1.get(i - 1).getCurrentText().equals(array2.get(array1.get(i).getOther() - 1).getCurrentText()))) {
                TextDiff td1 = new TextDiff();

                td1.setText(array1.get(i - 1).getCurrentText());
                td1.setOther(array1.get(i).getOther() - 1);
                array1.set(i - 1, td1);

                TextDiff td2 = new TextDiff();

                td2.setText(array2.get(array1.get(i).getOther() - 1).getCurrentText());
                td2.setOther(i - 1);
                array2.set(array1.get(i).getOther() - 1, td2);
            }
        }

        for (int i = 0; i < array1.size(); i++) {
            if (array1.get(i).getText() == null) {
                if (!array1.get(i).getCurrentText().equals(" ")) {
                    sb.append("<span style='background:#00FF00;'>").append(Utilities.htmlEncode(array1.get(i).getCurrentText())).append("</span>");
                } else {
                    sb.append("<span style='background:#00FF00;'>").append("&nbsp;").append("</span>");
                }
            } else {
                StringBuilder st = new StringBuilder();
                int           j  = array1.get(i).getOther() + 1;

                while ((j < array2.size()) && (array2.get(j).getText() == null)) {

                    // if(array2.get(j).getText() != null)
                    // continue outer;
                    if (!array2.get(j).getCurrentText().equals(" ")) {
                        st.append("<span style='background:#C0C0C0;'>").append(Utilities.htmlEncode(array2.get(j).getCurrentText())).append("</span>");
                    } else {
                        st.append("<span style='background:#C0C0C0;'>").append("&nbsp;").append("</span>");
                    }

                    j++;
                }

                if (!array1.get(i).getText().equals(" ")) {
                    sb.append(Utilities.htmlEncode(array1.get(i).getText()) + st.toString());
                } else {
                    sb.append("&nbsp;" + st.toString());
                }
            }
        }

        if (flag == false) {
            StringBuilder st = new StringBuilder();

            sb.append("\n");

            for (int i = 0; i < array2.size(); i++) {
                sb.append("<span style='background:#C0C0C0;'>").append(Utilities.htmlEncode(array2.get(i).getCurrentText())).append("</span>");
            }
        }

        String aSbString = sb.toString();

        return formatText(aSbString);
    }

    /**
     * This method checks if the specified request is locked
     *
     * @param tr TransferredRequest object.
     *
     * @return True  - If locked.
     *         False - Otherwise.
     */
    public static boolean isRequestLocked(TransferredRequest tr) {
        boolean returnValue = false;

        if (tr != null) {
            if (tr.getTargetRequestId() == -1) {

                // There is an entry corresponding to this prefix and
                // requestid and the target is -1, so this request is
                // locked
                returnValue = true;
            }
        }

        return returnValue;
    }

    /**
     * This method checks if the specified request is transferred. Incase it
     * is transferred, the corresponding record is returned.
     *
     * @param aPrefix    Prefix of BA.
     * @param aRequestId Request Id.
     *
     * @return TransferredRequest object if present otherwise null.
     */
    public static TransferredRequest isTransferred(String aPrefix, int aRequestId) {
        TransferredRequest tr = null;

        try {
            tr = TransferredRequest.lookupBySourcePrefixAndRequestId(aPrefix, aRequestId);
        } catch (Exception e) {
            StringBuffer message = new StringBuffer();

            message.append("An error occurred while looking up the ").append("transferred_requests table with the following ").append("information: ").append("\nSys Prefix: ").append(aPrefix).append(
                "\nRequest Id: ").append(aRequestId).append("\n\n").append(TBitsLogger.getStackTrace(e));
            LOG.severe(message.toString());
            tr = null;
        }

        return tr;
    }

    /**
     */
    public static boolean isVEEnable(int aUserId, int aSystemId) {
        try {
            User user = User.lookupByUserId(aUserId);

            if (user == null) {
                return false;
            }

            WebConfig webConfig = user.getWebConfigObject();

            if (webConfig == null) {
                return false;
            }

            Hashtable<String, BAConfig> baConfigs = webConfig.getBAConfigs();

            if (baConfigs == null) {
                return false;
            }

            BusinessArea ba = BusinessArea.lookupBySystemId(aSystemId);

            if (ba == null) {
                return false;
            }

            BAConfig baConfig = baConfigs.get(ba.getSystemPrefix().toUpperCase());

            if (baConfig == null) {
                return false;
            }

            if (baConfig.getEnableVE() == false) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            LOG.warn("",(e));

            return false;
        }
    }
}
