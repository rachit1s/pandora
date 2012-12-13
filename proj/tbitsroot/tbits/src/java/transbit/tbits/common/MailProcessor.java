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
 * MailProcessor.java
 *
 * $Header:
 */
package transbit.tbits.common;

//~--- non-JDK imports --------------------------------------------------------

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

//~--- classes ----------------------------------------------------------------

/**
 * <p>
 * The MailProcessor class reads the inputstream of email message and forms a
 * Java Mime Message object. This mime-message object is then parsed to extract
 * header information like From,To,Cc etc, text and html part, and attachments.
 * This information can be accessed using accessor methods.
 * </p>
 * <p>
 *  Regarding mail content (Description)
 * <UL>
 * <LI> The Html and Text part can be accessed as is using accessor method or
 *      the processed description can be used as described in below points.
 * <LI> If the mail has Html content-type , the description is text extracted
 *      from Html part using HtmlToText convertor.
 * <LI> If the mail has only Text content-Type, the description is text
 *      processed to remove line breaks if configured.
 * <LI> The entire message serialized as string is also provided.
 * </UL>
 * </p>
 * <p>
 * Regarding HtmlToText convertor binaries.
 * <UL>
 * <LI> Binaries should either be present in &lt;APP.HOME&gt;/etc.
 * <LI> Or you can specify the path as binaries.home system property.
 * <LI> If nothing is specified, binaries are looked in
 *      /prod/tbits/commons/bin
 * </UL>
 * <p>
 * Regarding Attachments
 * <UL>
 * <LI> Tmp location to store attachments should be provided in constructor.
 * <LI> A prefix to associate the attachments to this email can be provided
 *      in constructor, else userLogin extracted from From/ReplyTo is used.
 *      UserLogin is extracted using ypmatch.
 * <LI> If two attachments with same file are present, a counter is added to
 *      the file name to form a unique fileName.
 * <LI> An .msg attachment  is stored as html/txt, based on its content-type.
 *      the header information is present as comments in the source of
 *      html/txt.
 * <LI> If an .msg is present as attachment, which inturn has attachments,
 *      they are also extracted as attchements and  attachments names are
 *      Prefixed with "MessageNN" along with /after user specified prefix,
 *      where NN is a counter.
 * </UL>
 * </p>
 *
 *
 * @author  : Vaibhav.
 * @author  : Vinod Gupta
 * @version : $Id: $
 */
public final class MailProcessor {

    // The Logger.
    private static final String PKG_NAME = "transbit.tbits.common";
    public static TBitsLogger   LOG      = TBitsLogger.getLogger(PKG_NAME);

    // Mail formats.
    public static int TEXT_FORMAT = 0;
    public static int HTML_FORMAT = 1;

    //~--- fields -------------------------------------------------------------

    private Session                    session            = null;
    private String                     myToAddresses      = "";
    private String                     myTextDescription  = "";
    private String                     mySubject          = "";
    private String                     myReplyTo          = "";
    private boolean                    myRepairLinks      = false;
    private String                     myPrefix           = "";
    private int                        myMsgAttachmentCtr = 1;
    private String                     myMessageId        = "";
    private MimeMessage                myMessage          = null;
    private Hashtable<String, String>  myLocationTable    = new Hashtable<String, String>();
    private String[]                   myLinksList        = null;
    private StringBuilder              myInfoErrorLog     = new StringBuilder();
    private String                     myImportance       = "";
    private String                     myHtmlDescription  = "";
    private String                     myFromAddress      = "";
    private String                     myDescription      = "";
    private int 					   descriptionContentType = APIUtil.CONTENT_TYPE_HTML;	
    private String                     myCcAddresses      = "";
    private String                     myAttachments      = "";
    private String                     myAttachmentPath   = "";
    private Hashtable<String, Boolean> myAttNameTable     = new Hashtable<String, Boolean>();
    private int                        inlineBMPCtr       = 1;

    //~--- constructors -------------------------------------------------------

    /**
     * The only constructor that needs the location where attachments if any,
     * should be stored and the prefix that should be associated with the
     * attachment names for uniqueness.<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * &nbsp;&nbsp;If there are attachments and no
     * prefix is supplied, userLogin is extracted from "From" or "replyTo"
     * and is used as prefix.
     *
     * @param aAttachmentPath Location where attachments should be stored.
     * @param aPrefix Prefix to be attached to attachments.
     */
    public MailProcessor(String aAttachmentPath, String aPrefix) {
        myAttachmentPath = aAttachmentPath;
        myPrefix         = aPrefix;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This function extracts headers from the MimeMessage
     * and add them inline with the content.
     * -- Main Headers like From,To etc are added at the Top.
     * -- Also all headers are added as comments at end of content
     *
     * @param aMsg     MimeMessage to extract headers
     * @param aContent the mail content
     * @param aFormat  the format of mail, txt or htm.
     */
    private String addHeadersToContent(MimeMessage aMsg, String aContent, int aFormat) {
        StringBuilder sb = new StringBuilder();

        /*
         * -- Main Headers like From: etc are formatted and
         *    added before content of message.
         */
        StringBuffer formattedHeader   = new StringBuffer();
        String[]     formatHeadersList = { "From", "Sent", "To", "Cc", "Subject" };
        String       val               = "";

        for (int i = 0; i < formatHeadersList.length; i++) {
            try {
                switch (i) {
                case 0 :
                    val = Utilities.arrayListToString(getPersonalNamesList(aMsg.getFrom()));

                    break;

                case 1 :
                    val = aMsg.getSentDate().toString();

                    break;

                case 2 :
                    val = Utilities.arrayListToString(getPersonalNamesList(aMsg.getRecipients(Message.RecipientType.TO)));

                    break;

                case 3 :
                    val = Utilities.arrayListToString(getPersonalNamesList(aMsg.getRecipients(Message.RecipientType.CC)));

                    break;

                case 4 :
                    val = aMsg.getSubject();

                    break;

                default :
                    val = aMsg.getHeader(formatHeadersList[i], ", ");
                }
            } catch (MessagingException e) {
                LOG.info("",(e));

                continue;
            }

            if ((val != null) && (val.trim().equals("") == false)) {

                /*
                 * For html Format header Name should be bolded and
                 * value should be html encoded.
                 */
                if (aFormat == HTML_FORMAT) {
                    formattedHeader.append("<b>").append(formatHeadersList[i]).append(":</b> ").append(Utilities.htmlEncode(val)).append("<br>\n");
                } else {
                    formattedHeader.append(formatHeadersList[i]).append(": ").append(val).append("\n");
                }
            }
        }

        /*
         * -- Also all the headers are attached at the end
         *    of mail content as hidden comments
         */
        StringBuffer headerBuffer    = new StringBuffer();
        Enumeration  headerLinesEnum = null;

        try {
            headerLinesEnum = aMsg.getAllHeaderLines();
        } catch (MessagingException e) {
            LOG.info("",(e));
        }

        if (headerLinesEnum != null) {
            while (headerLinesEnum.hasMoreElements()) {
                val = (String) headerLinesEnum.nextElement();

                /*
                 * Val should not be html encoded since it is being
                 * added as comment.
                 */
                if ((val != null) && (val.trim().equals("") == false)) {
                    headerBuffer.append("\n<!-- ").append(val).append(" -->");
                }
            }
        }

        if (aFormat == HTML_FORMAT) {

            /*
             * For html, Headers should be put withing the body tags.
             */
            int index = aContent.toLowerCase().indexOf("<body>");

            if (index != -1) {
                sb.append(aContent.substring(0, index + 6)).append(formattedHeader).append(aContent.substring(index + 6)).append(headerBuffer);
            } else {
                sb.append(formattedHeader).append(aContent).append(headerBuffer);
            }
        } else {
            sb.append(formattedHeader).append("--------------------------------------------------\n").append(aContent).append(headerBuffer);
        }

        return sb.toString();
    }

    /**
     * This method checks if attachments location and prefix has been passed.
     *
     * @exception IllegalArgumentException
     */
    private void checkAttachmentProperties() throws IllegalArgumentException {
        if ((myAttachmentPath == null) || myAttachmentPath.trim().equals("")) {
            throw new IllegalArgumentException("Attachment storage location is mandatory.");
        }

        if (myAttachmentPath.trim().endsWith("/") == false) {
            myAttachmentPath = myAttachmentPath + "/";
        }

        if ((myPrefix == null) || myPrefix.trim().equals("")) {
            throw new IllegalArgumentException("Prefix to be attached to attachments is mandatory.");
        }
    }

    /*
     *  This function replacs bullet tag <ul> with <menu>
     * if <ul> is not followed by <li>
     * to avoid unnecessary bullets in fwded emails indentations.
     * If <li> is there, bullets are maintained.
     * In htmlToText <menu> maintains indentation and
     * <ul> maintains bullet with indentation.
     */
    private static String checkBulletTags(String aHtmlText) {
        String        temp = aHtmlText;
        StringBuilder sb   = new StringBuilder();
        StringBuilder sb1  = new StringBuilder();
        Pattern       p    = Pattern.compile("<ul>(.*)</ul>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        while (true) {
            Matcher m = p.matcher(temp);

            if (m.find() == true) {
                String ulText = m.group(1).toLowerCase();
                int    lIndex = ulText.indexOf("<li>");
                int    uIndex = ulText.indexOf("<ul>");
                int    dIndex = ulText.indexOf("<dir>");

                if ((lIndex == -1) || ((uIndex != -1) && (uIndex < lIndex)) || ((dIndex != -1) && (dIndex < lIndex))) {
                    sb.append(temp.substring(0, temp.indexOf(m.group())));
                    sb.append("<menu>");

                    int len = temp.indexOf(m.group()) + m.group().length();

                    len = (len == temp.length())
                          ? len
                          : len + 1;
                    sb1.insert(0, "</menu>" + temp.substring(len));
                    temp = m.group(1);
                } else {
                    sb.append(temp);

                    break;
                }
            } else {
                sb.append(temp);

                break;
            }
        }

        return sb.append(sb1).toString();
    }

    /*
     */
    private static String cleanTableTags(String aHtmlText) {
        if ((aHtmlText == null) || (aHtmlText.equals("") == true)) {
            return "";
        }

        // Replace All the <TABLE styles...> tags within <TABLE>
        aHtmlText = aHtmlText.replaceAll("<[Tt][Aa][Bb][Ll][Ee][^>]*>", "<TABLE>");

        // Replace All the <TR styles..> tags with <TR>
        aHtmlText = aHtmlText.replaceAll("<[Tt][Rr][^>]*>", "<TR>");

        // Replace All the <TH styles..> tags with <TH>
        aHtmlText = aHtmlText.replaceAll("<[Tt][Hh][^>]*>", "<TH>");

        // Replace All the <TD styles..> tags with <TD>
        aHtmlText = aHtmlText.replaceAll("<[Tt][Dd][^>]*>", "<TD>");

        String        temp   = aHtmlText;
        String        str    = aHtmlText;
        Pattern       p      = Pattern.compile("(<t[dh]>)(.*?)(</t[dh]>)", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
        Matcher       m      = p.matcher(temp);
        StringBuilder buffer = new StringBuilder();

        while (m.find() == true) {
            String tdText = m.group(2);

            // Remove all the opening tags.
            String cleanedTdText = tdText.replaceAll("<[^/][^<>]*>", "");

            // Remove all the other closing tags.
            cleanedTdText = cleanedTdText.replaceAll("</[^<>]*>", "");

            if (cleanedTdText.trim().equals("") == true) {
                cleanedTdText = "-";
            }

            cleanedTdText = m.group(1) + cleanedTdText + m.group(3);

            String temp1 = str.substring(0, str.indexOf(m.group()));

            buffer.append(temp1).append(cleanedTdText);
            str = str.substring(str.indexOf(m.group()) + m.group().length());
        }

        buffer.append(str);

        return buffer.toString();
    }

    /**
     * This method converts a list to the text with formatting.
     *
     * @param  aContent the string to be converted to text
     * @return the converted string
     */
    private String convertListToText(String aContent) {
        String       rValue = aContent.toLowerCase();
        StringBuffer sb     = new StringBuffer(300);

        // Split the string by the list marker tags.(OL, UL, DIR, MENU)
        String[]       contentArray = aContent.split("<[/]{0,1}([OoUu][Ll]|" + "[Mm][Ee][Nn][Uu]|" + "[Dd][Ii][Rr])" + "([\\s>])");
        String         indentation  = "";
        Stack<Integer> countStack   = new Stack<Integer>();
        int            arrayLength  = contentArray.length;

        for (int i = 0; i < arrayLength; i++) {
            String tempContent = contentArray[i];

            // Its a closing tag.
            if (rValue.startsWith("</ol>") || rValue.startsWith("</ul>") || rValue.startsWith("</menu>") || rValue.startsWith("</dir>")) {
                sb.append("\n");

                int position = rValue.indexOf('>');

                if (position > 0) {
                    rValue = rValue.substring(position + 1);
                }

                // Reduce the indentation by one tab when a tag closes.
                indentation = indentation.replaceFirst("\\t", "");

                if (!countStack.empty()) {
                    countStack.pop();
                }
            }

            if (rValue.startsWith(tempContent.toLowerCase())) {
                rValue = rValue.substring(tempContent.length());

                // Making all LIs in capital
                tempContent = tempContent.replaceAll("<[Ll][Ii]([\\s>])", "<LI$1");

                // The stack has count for ordered list.
                if (!countStack.empty()) {
                    int position = tempContent.indexOf("<LI");
                    int count    = ((Integer) countStack.pop()).intValue();

                    for (; (position > -1) && (count != 0); position = tempContent.indexOf("<LI")) {
                        tempContent = tempContent.replaceFirst("<[Ll][Ii][^<>]*>", "\n" + indentation + count++ + ".\t");
                    }

                    countStack.push(new Integer(count));
                }

                sb.append("").append(tempContent);
            } else if (rValue.startsWith("<ul") || rValue.startsWith("<menu") || rValue.startsWith("<dir")) {
                indentation = indentation + "\t";

                int position = rValue.indexOf('>');

                if (position > 0) {
                    rValue = rValue.substring(position + 1);
                }

                rValue = rValue.substring(tempContent.length());
                sb.append("\n");
                sb.append(tempContent.replaceAll("<[Ll][Ii][\\s>]", "\n" + indentation + "*\t$0"));
                countStack.push(new Integer(0));
            } else if (rValue.startsWith("<ol")) {
                int position = rValue.indexOf('>');

                if (position > 0) {
                    rValue = rValue.substring(position + 1);
                }

                rValue      = rValue.substring(tempContent.length());
                tempContent = tempContent.replaceAll("<[Ll][Ii]([\\s>])", "<LI$1");
                indentation = indentation + "\t";
                position    = tempContent.indexOf("<LI");

                int count = 1;

                for (count = 1; position > -1; position = tempContent.indexOf("<LI")) {
                    tempContent = tempContent.replaceFirst("<[Ll][Ii][^<>]*>", "\n" + indentation + count++ + ".\t");
                }

                countStack.push(new Integer(count));
                sb.append("\n");
                sb.append(tempContent);
            }
        }

        return sb.toString();
    }

    /**
     * This method extracts the data and fills up the corresponding variables.
     *
     * @param aMimeMessage MimeMessage that should be processed.
     *
     * @exception FileNotFoundException
     * @exception IllegalArgumentException
     * @throws TBitsException 
     */
    private void extractDataFromMessage(MimeMessage aMimeMessage) throws FileNotFoundException, IllegalArgumentException, APIException {
        try {

        	//Message aMessage;
        	
            // Get Message Id.
            myMessageId = aMimeMessage.getMessageID();
        } catch (MessagingException e) {
            myInfoErrorLog.append(TBitsLogger.getStackTrace(e) + "\n\n");
        }

        try {

            // Get the Subject.
            mySubject = aMimeMessage.getSubject();
        } catch (MessagingException e) {
            myInfoErrorLog.append(TBitsLogger.getStackTrace(e) + "\n\n");
        }

        try {

            // Get the Importance Header.
            myImportance = aMimeMessage.getHeader("Importance", null);
        } catch (MessagingException e) {
            myInfoErrorLog.append(TBitsLogger.getStackTrace(e) + "\n\n");
        }

        //
        // Get the address related attributes of the mail. These include
        // - From Address
        // - Reply-To Address
        // - Cc Address
        // - To Address
        //
        getAddressLists(aMimeMessage);

        //
        // set prefix to be add to attachments using replyto or from, if
        // not specified in constructor.
        //
        if ((myPrefix == null) || myPrefix.trim().equals("")) {
            setAttachmentPrefix();
        }

        try {

            //
            // Get the content type of the message in lowercase.
            // Processing path from here is based on the content type.
            //
            String contentType = aMimeMessage.getContentType().toLowerCase();

            if (contentType.indexOf("text/plain") >= 0) {
                handleTextPart(aMimeMessage);

                return;
            } else if (contentType.indexOf("text/html") >= 0) {
                handleHtmlPart(aMimeMessage);

                return;
            } else if (contentType.indexOf("multipart/alternative") >= 0) {
                LOG.debug("Received Multipart/Alternative Message.....");

                //
                // We are dealing with text/html mail message W/O attachments.
                // - Encoding: Each part is encoded separately.
                // - Map the content to MimeMultipart in JavaMail terminology.
                //
                MimeMultipart mmp = (MimeMultipart) aMimeMessage.getContent();

                handleMultipartAlternative(mmp);

                return;
            } else if ((contentType.indexOf("multipart/mixed") >= 0) || (contentType.indexOf("multipart/related") >= 0)) {
                LOG.debug("Received Multipart/Mixed Message.....");

                //
                // We are dealing with a mail message WITH attachments.
                // Body can be plain-text/rtf/html.
                //
                handleMultipartMixed(aMimeMessage);

                return;
            } else {

                //
                // This is the case of attaching an image in plain text
                // with no text content.
                //
                LOG.debug("Received " + contentType);
                myDescription = "";

                String fileName = aMimeMessage.getFileName();

                if ((fileName == null) || (fileName.trim().equals("") == true)) {
                    LOG.debug("FileName null hence returning." + "Header details are:\n" + aMimeMessage.getAllHeaders());

                    return;
                }

                storeAttachment(fileName, aMimeMessage.getInputStream(), myPrefix);
            }
        } catch (MessagingException e) {
            myInfoErrorLog.append(TBitsLogger.getStackTrace(e) + "\n\n");
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            myInfoErrorLog.append(TBitsLogger.getStackTrace(e) + "\n\n");
        }
    }

    /**
     * This method handles the attachments encountered while processing the
     * message.
     *
     * @param mbp        MimeBodyPart that contain the attachment.
     * @param aPrefix    Prefix to be associated with these attachments
     *
     * @exception MessagingException
     * @exception IOException
     * @exception FileNotFoundException
     * @exception IllegalArgumentException
     */
    private void handleAttachment(MimeBodyPart mbp, String aPrefix) throws MessagingException, IOException, FileNotFoundException, IllegalArgumentException, APIException {
        checkAttachmentProperties();

        String contentType     = mbp.getContentType();
        String contentDisp     = mbp.getDisposition();
        String contentLocation = mbp.getHeader("Content-Location", ",");
        String fileName        = mbp.getFileName();

        LOG.info("Content Type: " + contentType);
        LOG.info("Content Disp: " + contentDisp);
        LOG.info("Content Loc: " + contentLocation);
        LOG.info("File Name: " + fileName);

        if (contentType == null) {
            myInfoErrorLog.append("Unknown Content-Type. Skipping attachment: " + ((fileName == null)
                    ? ""
                    : fileName) + "\n\n");

            return;
        } else {
            contentType = contentType.toLowerCase();
        }

        if (fileName != null) {

            /*
             * If the file name is already encountered, throw APIException (instead of 
             * prepending the file name with the count as done previously)
             */
            if (myAttNameTable.get(fileName) != null) { 
            	TBitsException tbe = new TBitsException("Attachments with same name cannot be logged into tbits. Please log attachments with unique attachment names. ");
            	APIException apie = new APIException();
            	apie.addException(tbe, 3);
            	throw apie;				
            	/*
                fileName     = inlineBMPCtr + fileName;
                inlineBMPCtr = inlineBMPCtr + 1;
            	 */
            }

            // Check if this is an image.
            if (contentType.indexOf("image/bmp") >= 0) {
                if (contentLocation != null) {
                    myLocationTable.put(fileName, contentLocation);
                }
            }

            storeAttachment(fileName, mbp.getInputStream(), aPrefix);

            return;
        } else {

            //
            // This is an outlook message attached here.
            // 1) Store the text content as msg is stored as .htm
            // 2) Add headers to .htm , as comments.
            // 3) store all attachments present in the msg.
            //
            if (contentType.indexOf("message/rfc822") >= 0) {
                MimeMessage msg           = (MimeMessage) mbp.getContent();
                String      noExtFileName = "Msg_Message" + myMsgAttachmentCtr;
                Object      content       = msg.getContent();

                //
                // Form content fileName as <Messagecounter_subject.htm>
                //
                fileName = msg.getSubject();

                if (fileName == null) {
                    fileName = noExtFileName;
                } else {
                    fileName = noExtFileName + "_" + fileName;
                }

                if (fileName.length() > 64) {
                    fileName = fileName.substring(0, 63);
                }

                if (content instanceof String) {
                    String contentStr = (String) content;

                    if (msg.getContentType().indexOf("text/plain") >= 0) {
                        contentStr = addHeadersToContent(msg, contentStr, TEXT_FORMAT);
                        storeAttachment(fileName + ".txt", contentStr, aPrefix);
                    } else {
                        contentStr = addHeadersToContent(msg, contentStr, HTML_FORMAT);
                        storeAttachment(fileName + ".htm", contentStr, aPrefix);
                    }
                } else if (content instanceof MimeMultipart) {
                    MimeMultipart mmp         = (MimeMultipart) content;
                    int           lCount      = mmp.getCount();
                    boolean       gotHtml     = false;
                    String        textContent = "";

                    for (int i = 0; i < lCount; i++) {
                        Part part = mmp.getBodyPart(i);

                        contentType = part.getContentType();

                        if ((contentType.indexOf("multipart/alternative") >= 0) && (part.getContent() instanceof MimeMultipart)) {
                            mmp    = (MimeMultipart) part.getContent();
                            i      = 0;
                            lCount = mmp.getCount();

                            continue;
                        }

                        //
                        // Look for html content. In case html content is
                        // not present, store the text part as .htm
                        //
                        if (((contentType.indexOf("text/plain") >= 0) || (contentType.indexOf("text/html") >= 0)) && (part.getFileName() == null)) {
                            String partContent = part.getContent().toString();

                            if (contentType.indexOf("text/plain") >= 0) {
                                textContent = partContent;
                            } else if (contentType.indexOf("text/html") >= 0) {
                                gotHtml     = true;
                                partContent = addHeadersToContent(msg, partContent, HTML_FORMAT);
                                storeAttachment(fileName + ".htm", partContent, aPrefix);

                                break;
                            }
                        }

                        //
                        // Html content not prsent. Store Text part.
                        //
                        if ((gotHtml == false) && (textContent.equals("") == false) && (i == (lCount - 1))) {
                            textContent = addHeadersToContent(msg, textContent, TEXT_FORMAT);
                            storeAttachment(fileName + ".txt", textContent, aPrefix);
                        }
                    }
                } else {

                    //
                    // LOG as we could not recognise the msg text.
                    //
                    LOG.severe("Could not parse Attached .msg text" + "\n\nMessageId: " + myMessageId + "\nFrom: " + myFromAddress + "\nReplyTo: " + myReplyTo + "\nSubject: " + mySubject + "\n\n"
                               + readStream(msg.getInputStream()));
                }

                myMsgAttachmentCtr++;
                processAttachmentsInMSG(msg, aPrefix, noExtFileName);
            }
        }
    }

    /*
     * This method extracts the html as description
     *
     * @param aPart MimeMessage/MimeMultipart that should be processed.
     *
     * @exception IOException
     * @exception MessagingException
     */
    private void handleHtmlPart(Part aPart) throws IOException, MessagingException {
        LOG.debug("Received Text/Html message.....");

        //
        // We are dealing with a text/html mail message
        // W/O attachments.
        // - Decoding : Using our custom html2Text parser.
        //
        Object content = null;

        try {
            content = aPart.getContent();
        } catch (UnsupportedEncodingException uee) {
            LOG.warning(uee.toString());

            try {
                InputStream   is = aPart.getInputStream();
                int           b  = -1;
                StringBuilder sb = new StringBuilder();

                while ((b = is.read()) != -1) {
                    sb.append((char) b);
                }

                content = sb.toString();
            } catch (Exception e) {
                LOG.severe("",(e));
            }
        }

        if (content != null) {
            LOG.debug("Extracted Some Html Description.....");
            myDescription     = content.toString();
            myHtmlDescription = myDescription;
            descriptionContentType = TBitsConstants.CONTENT_TYPE_HTML;
            //
            // GetHtmlBodyContent() gives <body>content</body> removing
            // other html head tags etc. HtmlToText works well with this.
            //
            myDescription = getHtmlBodyContent(myDescription);
//            myDescription = htmlToText(myDescription);
        }
    }

    /**
     * This method handles a message of type multipart/alternative.
     *
     * @param mmp in multipart/alternative format.
     *
     * @exception MessagingException
     * @exception IOException
     * @exception FileNotFoundException
     * @exception IllegalArgumentException
     * @throws TBitsException 
     */
    private void handleMultipartAlternative(MimeMultipart mmp) throws MessagingException, IOException, FileNotFoundException, IllegalArgumentException, APIException {
        LOG.debug("In Multipart/Alternative Message Handler.....");

        int partCount = mmp.getCount();

        LOG.debug("PartCount: " + partCount + "  .....");

        for (int i = 0; i < partCount; i++) {
            MimeBodyPart mbp         = (MimeBodyPart) mmp.getBodyPart(i);
            String       contentType = mbp.getContentType();

            if (contentType == null) {
                continue;
            } else {
                contentType = contentType.toLowerCase();
            }

            LOG.debug("ContentType of part-" + i + ": " + contentType);

            if (contentType.indexOf("text/plain") >= 0) {

                // This is the Text Part of alternative mail
                // Content-Type     : text/plain
                // Content-Class    : String
                // Content-Encoding : quoted-printable
                // Content-Decoding : No Use of decoding.
                // So, we ignore this.
                if (myDescription.trim().equals("") == true) {

                    //
                    // Lets store the text part. If html part is there,
                    // text part will be overwritten by it.
                    //
                    handleTextPart(mbp);

                    continue;
                } else {
                    continue;
                }
            } else if (contentType.indexOf("text/html") >= 0) {
                handleHtmlPart(mbp);

                continue;
            } else if (contentType.indexOf("multipart/alternative") >= 0) {

                // This is the case if the Mail contains some inline images.
                MimeMultipart cmmp = (MimeMultipart) mbp.getContent();

                handleMultipartAlternative(cmmp);

                continue;
            } else {

                // Otherwise, this might be an inline image.
                handleAttachment(mbp, myPrefix);
            }
        }
    }

    /**
     * This method handles a Multipart/Mixed type of message.
     *
     * @param aMimeMessage Mimemessage in this format.
     * @exception MessagingException
     * @exception IOException
     * @exception FileNotFoundException
     * @exception IllegalArgumentException
     * @throws TBitsException 
     */
    private void handleMultipartMixed(MimeMessage aMimeMessage) throws MessagingException, IOException, FileNotFoundException, IllegalArgumentException, APIException {
        LOG.debug("In Multipart/Mixed Message Handler .....");

        Object temp = null;

        try {
            temp = aMimeMessage.getContent();
        } catch (UnsupportedEncodingException uee) {
            LOG.warning(uee.toString());

            try {
                InputStream   is = aMimeMessage.getInputStream();
                int           b  = -1;
                StringBuilder sb = new StringBuilder();

                while ((b = is.read()) != -1) {
                    sb.append((char) b);
                }

                temp = sb.toString();
            } catch (Exception e) {
                LOG.severe("",(e));
            }
        }

        if ((temp != null) && (temp instanceof MimeMultipart)) {
            MimeMultipart mmp       = (MimeMultipart) temp;
            int           partCount = mmp.getCount();

            LOG.debug("PartCount: " + partCount + " .....");

            for (int i = 0; i < partCount; i++) {
                MimeBodyPart mbp = (MimeBodyPart) mmp.getBodyPart(i);

                if (mbp == null) {
                    continue;
                }

                // LOG.debug("Class of Part- " + i + ": " +
                // mbp.getContent().getClass());
                String contentType = mbp.getContentType();

                if (contentType == null) {
                    continue;
                } else {
                    contentType = contentType.toLowerCase();
                }

                if (mbp.getFileName() != null) {
                    LOG.debug("This is an attachment with filename: " + mbp.getFileName() + " .....");

                    // Any Part with a non-null filename is an attachment.
                    // Store It.
                    handleAttachment(mbp, myPrefix);

                    continue;
                }

                LOG.debug("This part is not an attachment. " + "Its content-type is: " + mbp.getContentType() + " .....");

                if (contentType.indexOf("text/plain") >= 0) {
                    handleTextPart(mbp);
                } else if (contentType.indexOf("text/html") >= 0) {
                    handleHtmlPart(mbp);
                } else if (contentType.indexOf("multipart/alternative") >= 0) {

                    //
                    // We are dealing with text/html message with
                    // attachments. The first part is a multipart that has
                    // both text and html versions of the append.
                    // Handle this to the multipartAlternative handler.
                    //
                    MimeMultipart multipart = (MimeMultipart) (mbp.getContent());

                    handleMultipartAlternative(multipart);
                } else {

                    // This might be an msg attachment.
                    handleAttachment(mbp, myPrefix);
                }
            }
        } else {
            if (temp != null) {
                LOG.severe("Unhandled content: " + temp.getClass());
            }
        }
    }

    /*
     * This method extracts the text as description
     *
     * @param aPart MimeMessage/MimeMultipart that should be processed.
     *
     * @exception IOException
     * @exception MessagingException
     */
    private void handleTextPart(Part aPart) throws IOException, MessagingException {
        LOG.debug("Received Text/Plain message.....");

        //
        // We are dealing with a text/rtf mail message W/O attachments.
        // - Encoding : 8bit [X-MIME-AutoConverted From
        // quoted-printable].
        // - Decoding : No Use.
        //
        Object content = null;

        try {
            content = aPart.getContent();
        } catch (UnsupportedEncodingException uee) {
            LOG.warning(uee.toString());

            try {
                InputStream   is = aPart.getInputStream();
                int           b  = -1;
                StringBuilder sb = new StringBuilder();

                while ((b = is.read()) != -1) {
                    sb.append((char) b);
                }

                content = sb.toString();
            } catch (Exception e) {
                LOG.severe("",(e));
            }
        }

        if (content != null) {
            LOG.debug("Extracted Some Description.....");
            myDescription     = content.toString();
            myTextDescription = myDescription;
            descriptionContentType = TBitsConstants.CONTENT_TYPE_TEXT;
            
            if (myRepairLinks == true) {
                myDescription = repairWrappedLinks(myDescription, myLinksList, " ");
            }
        }
    }

    public int getDescriptionContentType() {
		return descriptionContentType;
	}

	/**
     * This method extracts the text embedded in HTML.
     *
     * @param aText HTML with embedded text.
     *
     * @return Text that is embedded in HTML.
     */
    public String htmlToText(String aText) {
        String plainText = "";
        String osArch    = System.getProperty("os.arch");
        File   file      = Configuration.findPath("html2text-" + osArch);

        if (file == null) {
            LOG.warn("html2text-" + osArch + " convertor not found");
        } else {
            LOG.debug("HtmlConvertor invoked: " + file.toString());

            try {
                String htmlText = aText;
                String temp     = htmlText.toLowerCase();

                //
                // Remove Xml Tags.
                //
                htmlText = htmlText.replaceAll("<\\?[Xx][Mm][Ll]:[^>]*>", "");

                // &#... are unicode characters encoded. not properly
                // supported by html2Text.
                htmlText = htmlText.replaceAll("&#([0-9]+);", "&amp;#$1;");

                // Replace All the <TABLE styles...> tags within <TABLE>
                htmlText = cleanTableTags(htmlText);

                // Replace All <UL> with <MENU> if no <LI> after <UL>
                htmlText = checkBulletTags(htmlText);

                // Remove all <BR> before </UL> to avoid empty bullet.
                htmlText = htmlText.replaceAll("<[Bb][Rr]>[\\s\n\r]*</[Uu][Ll]>", "</UL>");

                // move all <BR> after </LI> to before <LI>
                // to avoid empty bullet.
                htmlText = htmlText.replaceAll("</[Ll][Ii]>[\\s\n\r]*<[Bb][Rr]>", "<BR></LI>");

                //
                // Remove Font,B,U,I tags as they are inconsistent.
                // U gives underline that we don't want. neither do we use
                // Font info. Also combination of these inserts line
                // breaks at random
                //
                htmlText = htmlText.replaceAll("</?[bBuUiI]>", "");
                htmlText = htmlText.replaceAll("<[Ff][Oo][Nn][Tt][^>]*>", "");
                htmlText = htmlText.replaceAll("</[Ff][Oo][Nn][Tt]>", "");

                // The paragraphs<P> are given one space at beginning
                htmlText = htmlText.replaceAll("<[Pp][^Rr>]*>", "$0<BR>");

                // Extracting the hyperlink
                htmlText = htmlText.replaceAll("<[Aa][^<>]*[Hh][Rr][Ee][Ff]\\s*=\\s*[\"\']" + "(([Hh][Tt][Tt][Pp]:)|([Ff][Tt][Pp]:)" + "|([Ff][Ii][Ll][Ee]:)" + "|(\\\\\\\\[Dd][Ee][Ss][Hh][Aa][Ww])"
                                               + "|([oO][uU][tT][lL][oO][oO][kK]:\\\\\\\\)" + ")([^\"\']*)" + "[\"\'][^<>]*>([\\w\\W]*?)</[aA]>", "$8 &lt;$1$7&gt;");

                // Removing </HR> tags, if any.
                htmlText = htmlText.replaceAll("</[Hh][Rr]>", "");

                // <HR> tag is converted to a line of 76 hyphens.
                String HR_LINE = "";

                for (int i = 0; i < 4; i++) {

                    // Adding 19 hyphens to HR_LINE
                    HR_LINE = HR_LINE + "-------------------";
                }

                HR_LINE  = HR_LINE + "<BR>";
                htmlText = htmlText.replaceAll("<[\\s]*[Hh][Rr][^<>]*>", HR_LINE);

                //
                // The html to text converter ignores trailing white spaces
                // before any closing tag. and white spaces after an
                // opening tag.
                // text1 </FONT><FONT> text2 was converted to text1text2
                // So, taking care of that. Now it converts to text1  text2
                // Adding onle one space for any number of spaces.
                //
                htmlText = htmlText.replaceAll("[ ]</", "&nbsp;</");
                htmlText = htmlText.replaceAll(">[ ]", ">&nbsp;");

                String      command = file.toString() + " -nobs -style pretty -width 64000";
                Runtime     runtime = Runtime.getRuntime();
                Process     process = runtime.exec(command);
                PrintStream ps      = new PrintStream(process.getOutputStream(), false);

                ps.println(htmlText);
                ps.close();

                InputStreamReader ipstream = new InputStreamReader(process.getInputStream());
                BufferedReader    buffer   = new BufferedReader(ipstream, 8000);
                String            line     = buffer.readLine();
                StringBuffer      sb       = new StringBuffer(300);

                while (line != null) {
                    sb.append(line).append("\n");
                    line = buffer.readLine();
                }

                plainText = sb.toString();

                //
                // This is to remove the large no. of spaces sometimes
                // introduced.
                //
                plainText = plainText.replaceAll("\\n\\s*\\n", "\n\n");
                plainText = plainText.replaceAll("[\\s]{200,}", "\n");

                // When the plain text is returned.
                if (plainText.length() > 0) {
                    return plainText;
                }
            } catch (IOException ioe) {
                LOG.severe("\nMessageId: " + myMessageId + "\nFrom: " + myFromAddress + "\nReplyTo: " + myReplyTo + "\nSubject: " + mySubject + "\nError in executing the html to text conveter."
                           + "\n\n" + "",(ioe));
            }
        }

        String lValue = aText.toLowerCase();
        String rValue = aText;
        int    index  = 0;

        // Rip off the text before <body> tag.
        index = lValue.indexOf("<body");

        if (index >= 0) {
            rValue = rValue.substring(index);
            lValue = lValue.substring(index);
        }

        // Rip off the text after </body> tag.
        index = lValue.indexOf("</body>");

        if (index >= 0) {
            rValue = rValue.substring(0, index);
            lValue = lValue.substring(0, index);
        }

        // Remove all new-lines.
        StringBuffer buffer = new StringBuffer();
        int          length = rValue.length();

        for (int i = 0; i < length; i++) {
            if ((rValue.charAt(i) != '\n') && (rValue.charAt(i) != '\r')) {
                buffer.append(rValue.charAt(i));
            }
        }

        rValue = buffer.toString();

        // Extracting the hyperlink from the <A> tag.
        rValue = rValue.replaceAll("<[Aa][^<>]*[Hh][Rr][Ee][Ff]\\s*=\\s*[\"\']([^\"\']*)" + "[\"\'][^<>]*>([\\w\\W]*?)</[aA]>", "$2 &lt;$1&gt;");

        // Now change all <BR> to \n characters.
        rValue = rValue.replaceAll("<[Bb][rR]>", "\n");

        // Replace All the <P> tags with \n<P>
        rValue = rValue.replaceAll("<[Pp][\\s>]", "\n$0");

        // Replace All the <DIV> tags with \n<DIV>
        rValue = rValue.replaceAll("<[Dd][Ii][Vv][\\s>]", "\n$0");

        // Replace All the <TABLE> tags with \n<TABLE>
        rValue = rValue.replaceAll("<[Tt][Aa][Bb][Ll][Ee][\\s>]", "\n$0");

        // Replace All the <TR> tags with \n<TR>
        rValue = rValue.replaceAll("<[Tt][Rr][\\s>]", "\n$0");

        // Replace All the <TD> tags with \t<TD>
        rValue = rValue.replaceAll("<[Tt][Dd][\\s>]", "\t$0");

        // Replace All the <LI> tags with \n<LI>
        // rValue = rValue.replaceAll("<[Ll][Ii][\\s>]", "\n$0");
        rValue = convertListToText(rValue);

        // Replace All the <Address> tags with \n<Address>
        rValue = rValue.replaceAll("<[Aa][Dd]{2}[Rr][Ee][Ss]{2}[\\s>]", "\n$0");

        // Replace All the <H>(heading) tags with \n<H>
        rValue = rValue.replaceAll("<[Hh][1-6][\\s>]", "\n\n$0");

        // Replace All the <MENU> tags with \n<MENU>
        // rValue = rValue.replaceAll("<[Mm][Ee][Nn][Uu][\\s>]", "\n$0");
        // Replace All the <DIR> tags with \n<DIR>
        // rValue = rValue.replaceAll("<[Dd][Ii][Rr][\\s>]", "\n$0");
        // Replace All the <PRE> tags with \n<PRE>
        rValue = rValue.replaceAll("<[Pp][Rr][Ee][\\s>]", "\n$0");

        // Replace All the <OL> tags with \n<OL>
        // rValue = rValue.replaceAll("<[Oo][Ll][\\s>]", "\n$0");
        // Replace All the <UL> tags with \n<UL>
        // rValue = rValue.replaceAll("<[Uu][Ll][\\s>]", "\n$0");
        // Replace All the <DL> tags with \n
        rValue = rValue.replaceAll("<[Dd][Ll][\\s>]", "\n$0");

        // Replace All the <DT> tags with \n<DT>
        rValue = rValue.replaceAll("<[Dd][Tt][\\s>]", "\n$0");

        // Replace All the <DD> tags with \n<DD>
        rValue = rValue.replaceAll("<[Dd]{2}[\\s>]", "\n\t$0");

        // <HR> tag is converted to a line of 76 hyphens.
        String HR_LINE = "";

        for (int i = 0; i < 4; i++) {
            HR_LINE = HR_LINE + "-------------------";
        }

        rValue = rValue.replaceAll("<[\\s]*[Hh][Rr][^<>]*>", HR_LINE + "\n");

        // Remove all the opening tags.
        rValue = rValue.replaceAll("<[^/][^<>]*>", "");

        // Replace All the </P> tags with \n
        rValue = rValue.replaceAll("</[Pp][//s>]>", "\n");

        // Replace All the </TABLE> tags with \n<TABLE>
        rValue = rValue.replaceAll("</[Tt][Aa][Bb][Ll][Ee][\\s>]", "\n$0");

        // Replace All the </H>(heading closing) tags with \n</H>
        rValue = rValue.replaceAll("</[Hh][1-6][\\s>]", "\n");

        // Replace All the </DL> tags with \n
        rValue = rValue.replaceAll("</[Dd][Ll][\\s>]", "\n");

        // Remove all the other closing tags.
        rValue = rValue.replaceAll("</[^<>]*>", "");

        // Replace the &nbsp; with
        rValue = rValue.replaceAll("&nbsp;", " ");

        return Utilities.htmlDecode(rValue);
    }

    /**
     * This method prints all the exception logs with Object dump.
     *
     * @param aErrorDesc         one line description of errors
     * @param aErrorLog          the errors stack trace to be reported
     * @param aIncludeObjectDump boolean to dump object contents
     * @param aLevel             the level to log with
     */
    public void logErrorWithObjectDump(String aErrorDesc, String aErrorLog, boolean aIncludeObjectDump, String aLevel) {
        StringBuilder sb = new StringBuilder();

        if (!aErrorDesc.equals("")) {
            sb.append(aErrorDesc).append("\n\n");
        }

        if (!aErrorLog.equals("")) {
            sb.append(aErrorLog).append("\n\n");
        }

        if (aIncludeObjectDump == true) {
            sb.append("\nMessageId: ").append(myMessageId).append("\nFrom: ").append(myFromAddress).append("\nReplyTo: ").append(myReplyTo).append("\nSubject: ").append(mySubject).append(
                "\nTo: ").append(myToAddresses).append("\nCc: ").append(myCcAddresses).append("\nImportance: ").append(myImportance).append("\nAttachments: ").append(myAttachments).append(
                "\nDescription: ").append(myDescription);
        }

        if (aLevel.equalsIgnoreCase("debug")) {
            LOG.debug(sb.toString());
        } else if (aLevel.equalsIgnoreCase("info")) {
            LOG.info(sb.toString());
        } else if (aLevel.equalsIgnoreCase("warn")) {
            LOG.warn(sb.toString());
        } else if (aLevel.equalsIgnoreCase("error")) {
            LOG.error(sb.toString());
        } else if (aLevel.equalsIgnoreCase("fatal")) {
            LOG.fatal(sb.toString());
        }
    }

    /**
     * The main method - Entry point into the program.
     *
     * @param arg List of command-line arguments.
     *
     * @exception Exception
     */
    public static void main(String arg[]) throws Exception {
        MailProcessor     mp       = new MailProcessor(arg[0], null);
        InputStreamReader ipstream = new InputStreamReader(System.in);
        StringBuilder     stb      = new StringBuilder();
        int               chr      = ipstream.read();

        while (chr != -1) {
            stb.append(String.valueOf((char) chr));
            chr = ipstream.read();
        }

        ipstream.close();

        String aString = stb.toString();

        // System.out.println("\nInput: " +aString);
        aString = mp.htmlToText(aString);
        System.out.println("\nText Output: " + aString);
    }

    /**
     * This method reads the value of a specified address header and parse it
     * to form an array of Address object, when the header format is not
     * readable by the java api.
     *
     * @param aMsg The Mime Message
     * @param aHeader The header name to be parsed
     *
     * @return aAddressList The Adress array after parsing
     */
    private Address[] parseAddressHeader(MimeMessage aMsg, String aHeader) {
        Address[] addressList = null;

        // The implementatiom will be extended as per the specific error cases
        return addressList;
    }

    /**
     * This method processes the attachments if any present within an Outlook
     * Message.
     *
     * @param aMsg Outlook message
     * @param aPrefix Prefix to be associated with these attachments
     * @param aMessageCounter MessageCount to be prefixed to file Names.
     * @exception MessagingException
     * @exception IOException
     * @exception FileNotFoundException
     * @exception IllegalArgumentException
     * @throws TBitsException 
     */
    private void processAttachmentsInMSG(MimeMessage aMsg, String aPrefix, String aMessageCounter) throws MessagingException, IOException, FileNotFoundException, IllegalArgumentException,  APIException {
        if (aMsg == null) {
            return;
        }

        //
        // If this is not a multipart/mixed message, it does not have any
        // attachments in it.
        //
        String contentType = aMsg.getContentType();

        if (contentType == null) {
            return;
        } else {
            contentType = contentType.trim().toLowerCase();
        }

        if ((contentType.indexOf("multipart/mixed") < 0) && (contentType.indexOf("multipart/related") < 0)) {
            return;
        }

        Object content = aMsg.getContent();

        if (content instanceof MimeMultipart) {
            MimeMultipart mmp       = (MimeMultipart) content;
            int           partCount = mmp.getCount();

            for (int i = 0; i < partCount; i++) {
                MimeBodyPart mbp         = (MimeBodyPart) mmp.getBodyPart(i);
                String       fileName    = mbp.getFileName();
                String       disposition = mbp.getDisposition();

                contentType = mbp.getContentType();

                if (contentType == null) {
                    continue;
                } else {
                    contentType = contentType.trim().toLowerCase();
                }

                //
                // Generally txt file/html file will have the content-type
                // as text/plain, text/html and they will have a valid
                // filename. In case there is no valid filename, then this
                // is a text/html part and not an attachment.
                //
                if (((contentType.indexOf("text/plain") >= 0) || (contentType.indexOf("text/html") >= 0)) && (fileName == null)) {
                    continue;
                }

                if (fileName != null) {
                    mbp.setFileName(aMessageCounter + "_" + fileName);
                }

                handleAttachment(mbp, aPrefix);
            }
        }
    }

    /**
     * This method is the initiator of the process of extracting data from a
     * given input stream which is a mail message.
     *
     * @param aInputStream InputStream that contains the message.
     *
     * @exception MessagingException
     * @exception FileNotFoundException
     * @exception IllegalArgumentException
     * @throws TBitsException 
     */
    public void processMail(Message m) throws MessagingException, FileNotFoundException, IllegalArgumentException, APIException {
        long start = System.currentTimeMillis();

        //
        // Form the MailMessage object from the InputStream passed. If the
        // MailMessage has attachments, then this formation process takes
        // a lot of time depending on the attachment size.
        //
        //myMessage = new MimeMessage(session, aInputStream);
        myMessage = (MimeMessage)m;//(session, aInputStream);
        
        // Extract the data from the message.
        extractDataFromMessage(myMessage);
        
        
        
        //
        // This logs all exceptions occured while parsing Message
        // headers/contents. These exceptions should not halt message
        // processing but should be logged as info.
        //
        if (!myInfoErrorLog.toString().equals("")) {
            logErrorWithObjectDump("FYI: exceptions while parsing message", myInfoErrorLog.toString(), true, "info");
        }

        long end = System.currentTimeMillis();

        LOG.info("Time taken to  parse Mail: " + (end - start) + " msecs");

        return;
    }

    /**
     * This method reads the content of a stream and returns it.
     *
     * @param aIs InputStream to be read.
     *
     * @return Content of input stream passed.
     *
     * @exception IOException that occured while reading the InputStream.
     */
    private String readStream(InputStream aIs) throws IOException {

        // Open a BufferedReader and read 1MB at a single go.
        BufferedReader br     = new BufferedReader(new InputStreamReader(aIs));
        int            size   = 1024 * 1024;
        byte[]         b      = new byte[size];
        int            read   = 0;
        StringBuilder  buffer = new StringBuilder();

        while ((read = aIs.read(b, 0, size)) > 0) {
            buffer.append(new String(b, 0, read));
        }

        return buffer.toString();
    }

    /**
     * This function removes soft link breaks from specified link within the
     * content.
     *
     * @param aContent
     * @param aLinksList the array of link prefixes to be cleaned
     * @param aLinkEnd the link spacer
     *
     * @return String ,the cleaned content
     */
    private String repairWrappedLinks(String aContent, String[] aLinksList, String aLinkEnd) {
        if ((aLinksList == null) || (aLinksList.length == 0)) {
            return aContent;
        }

        // to remove "> " inserted at bergining of new line in rtf format
        aContent = aContent.replaceAll("\r?\n> ", "\n");

        for (int i = 0; i < aLinksList.length; i++) {
            StringBuilder result        = new StringBuilder();
            String        wrappedText   = aContent;
            
            // TODO : What does 66,76 mean??
            String        unWrappedText = aContent.replaceAll("\r?\n([^\r\n]{66,76}) ?\r?\n", "$1");
            String        aLinkStart    = aLinksList[i];

            try {
                int wIndex = wrappedText.indexOf(aLinkStart);
                int uIndex = unWrappedText.indexOf(aLinkStart);

                while (wIndex >= 0) {

                    // Get the space character from this index in both
                    // wrapped and unwrapped versions.
                    int swIndex = wrappedText.indexOf(aLinkEnd, wIndex);
                    int suIndex = unWrappedText.indexOf(aLinkEnd, uIndex);

                    if ((swIndex < 0) && (suIndex < 0)) {
                        suIndex = unWrappedText.length();
                        swIndex = wrappedText.length();
                    }

                    result.append(wrappedText.substring(0, wIndex)).append(unWrappedText.substring(uIndex, suIndex));
                    unWrappedText = unWrappedText.substring(suIndex);
                    wrappedText   = wrappedText.substring(swIndex);
                    wIndex        = wrappedText.indexOf(aLinkStart);
                    uIndex        = unWrappedText.indexOf(aLinkStart);
                }

                result.append(wrappedText);
            } catch (Exception e) {
                myInfoErrorLog.append(TBitsLogger.getStackTrace(e) + "\n\n");
                result.append(wrappedText);
            }

            aContent = result.toString();
        }

        return aContent;
    }

    /**
     * This method stores the attachment with the specified filename. The
     * content of the attachment is in the given InputStream.
     *
     * @param aFileName Name of the attachment.
     * @param aIs      InputStream that contains the content of the attachment.
     * @param aPrefix  the prefix to be attached to store in tmp location.
     *
     * @exception IOException while reading the stream
     * @exception FileNotFoundException when creating the file.
     */
    private void storeAttachment(String aFileName, InputStream aIs, String aPrefix) throws IOException, FileNotFoundException {
        checkAttachmentProperties();

        String           storedName = aFileName.replaceAll("[^a-zA-Z0-9._]", "_");
        String           fullPath   = "";
        File newFileLocation;
        //Spin until you find a file name that doesnt exist so that 
        //existing file in tmp doesnt get overwritten.        
        int fileTmpId = 0;
        do
        {
        	fullPath = myAttachmentPath + fileTmpId++ + "-" + storedName;
        	//myAttachmentPath + aPrefix + "-" + fileTmpId++ + "-" + storedName
        	//example: "/opt/tbits/tmp/mohit-0-my_doc__.doc
        	newFileLocation = new File(fullPath);
        }
        while(newFileLocation.exists()); //do while ends
        
        FileOutputStream fos        = new FileOutputStream(fullPath);
        int              i          = 0;

        while ((i = aIs.read()) != -1) {
            fos.write(i);
        }

        fos.close();

        
        AttachmentInfo attInfo = null;
        ArrayList<AttachmentInfo> attCollection = new ArrayList<AttachmentInfo>();
        Uploader uploader = new Uploader();
        File newAttachment = new File(fullPath);
        if (newAttachment.exists()){
        	attInfo = uploader.moveIntoRepository(newAttachment, aFileName);  
        	attInfo.name = aFileName;
        	attInfo.size = (int) newAttachment.length();
        }
		
        if ((myAttachments == null) || myAttachments.trim().equals("")) {
        	//File newFile = new File(pdfFilePath);    		      	
            //myAttachments = fullPath + "\t" + aFileName;
        	attCollection.add(attInfo);
        	myAttachments = AttachmentInfo.toJson(attCollection);  
        } else {
            //myAttachments = myAttachments + "\n" + fullPath + "\t" + aFileName;
        	attCollection.addAll(AttachmentInfo.fromJson(myAttachments));
        	attCollection.add(attInfo);
        	myAttachments = AttachmentInfo.toJson(attCollection);
        }
        myAttNameTable.put(aFileName, true);
    }

    /**
     * This method stores the attachment with the specified filename.
     *
     * @param aFileName Name of the attachment.
     * @param aContent  the content of the attachment as String.
     * @param aPrefix  the prefix to be attached to store in tmp location.
     *
     * @exception IOException while reading the stream
     * @exception FileNotFoundException when creating the file.
     */
    private void storeAttachment(String aFileName, String aContent, String aPrefix) throws IOException, FileNotFoundException {
    	
    	//StringReader sr = new StringReader(aContent);
    	ByteArrayInputStream bais = new ByteArrayInputStream(aContent.getBytes());  	
    	storeAttachment(aFileName, bais, aPrefix);
    	bais.close();
//        String           storedName = aFileName.replaceAll("[^a-zA-Z0-9._]", "_");
//        String           fullPath   = myAttachmentPath + aPrefix + "-" + storedName;
//        FileOutputStream fos        = new FileOutputStream(fullPath);
//
//        fos.write(aContent.getBytes());
//        fos.close();
//
//        if ((myAttachments == null) || myAttachments.trim().equals("")) {
//            myAttachments = fullPath + "\t" + aFileName;
//        } else {
//            myAttachments = myAttachments + "\n" + storedName + "\t" + aFileName;
//        }
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method gets an ArrayList of email addresses from the array of
     * InternetAddresses
     *
     * @param aRec Array of address objects
     *
     * @return ArrayList of corresponding email address strings.
     */
    public static ArrayList<String> getAddressList(Address[] aRec) {
        ArrayList<String> list = new ArrayList<String>();

        if ((aRec == null) || (aRec.length == 0)) {
            return list;
        }

        for (int i = 0; i < aRec.length; i++) {
            InternetAddress ia = (InternetAddress) aRec[i];

            list.add(ia.getAddress());
        }

        return list;
    }

    /**
     * This method extracts the address related properties from the MimeMessage
     * which include
     *  <ul>
     *      <li>From
     *      <li>Reply-To
     *      <li>To
     *      <li>Cc
     *  </ul>
     *
     * @param aMimeMessage Message from which these attributes should be read.
     */
    private void getAddressLists(MimeMessage aMimeMessage) {
        Address[] temp = null;

        // Get the From Address.
        try {
            temp = aMimeMessage.getFrom();
        } catch (MessagingException e) {
            myInfoErrorLog.append(TBitsLogger.getStackTrace(e) + "\n\n");
            temp = parseAddressHeader(aMimeMessage, "from");
        }

        myFromAddress = Utilities.arrayListToString(getAddressList(temp));

        // Get the Reply To Address
        try {
            temp = aMimeMessage.getReplyTo();
        } catch (MessagingException e) {
            myInfoErrorLog.append(TBitsLogger.getStackTrace(e) + "\n\n");
            temp = parseAddressHeader(aMimeMessage, "replyTo");
        }

        myReplyTo = Utilities.arrayListToString(getAddressList(temp));

        // Get the To List.
        try {
            temp = aMimeMessage.getRecipients(Message.RecipientType.TO);
        } catch (MessagingException e) {
            myInfoErrorLog.append(TBitsLogger.getStackTrace(e) + "\n\n");
            temp = parseAddressHeader(aMimeMessage, "to");
        }

        myToAddresses = Utilities.arrayListToString(getAddressList(temp));

        // Get the Cc List.
        try {
            temp = aMimeMessage.getRecipients(Message.RecipientType.CC);
        } catch (MessagingException e) {
            myInfoErrorLog.append(TBitsLogger.getStackTrace(e) + "\n\n");
            temp = parseAddressHeader(aMimeMessage, "cc");
        }

        myCcAddresses = Utilities.arrayListToString(getAddressList(temp));
    }

    /**
     * Accessor method to get the line break seperated list of
     * Attachment names.<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * Format of attachment name is : encoded file name '\t' original file name
     *
     * @return line break separated list of Attachment names.
     */
    public String getAttachments() {
        myAttachments = (myAttachments == null)
                        ? ""
                        : myAttachments.trim();
        return myAttachments;
    }

    /**
     * Accessor method to get the Cc.
     *
     * @return Cc.
     */
    public String getCc() {
        myCcAddresses = (myCcAddresses == null)
                        ? ""
                        : myCcAddresses.trim();

        return myCcAddresses;
    }

    /**
     * Accessor method to get the Message as string
     *
     * @return Message as String
     */
    public String getCompleteMessage() {
        if (myMessage == null) {
            return "";
        }

        ByteArrayOutputStream bStream    = new ByteArrayOutputStream();
        String                messageStr = "";

        try {
            myMessage.writeTo(bStream);
            messageStr = bStream.toString();
        } catch (MessagingException e) {
            myInfoErrorLog.append(TBitsLogger.getStackTrace(e) + "\n\n");
        } catch (IOException e) {
            myInfoErrorLog.append(TBitsLogger.getStackTrace(e) + "\n\n");
        } catch (Exception e) {
            myInfoErrorLog.append(TBitsLogger.getStackTrace(e) + "\n\n");
        }

        return messageStr;
    }

    /**
     * Accessor method to get the value of mail content as plain-text.<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;If the mail content
     * has html part, text is extracted from it using htmlToText convertor.<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;If the mail
     * content-type is text, text is parsed to remove soft-line breaks from
     * links<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; based on
     * value of myRepairLinks and myLinks List.
     *
     * @return Description.
     */
    public String getDescription() {
        myDescription = (myDescription == null)
                        ? ""
                        : myDescription.trim();

        return myDescription;
    }

    /**
     * Accessor method to get the From Address.
     *
     * @return FromAddress.
     */
    public String getFrom() {
        myFromAddress = (myFromAddress == null)
                        ? ""
                        : myFromAddress.trim();

        return myFromAddress;
    }

    /**
     * This method returns the value of the given Header.
     *
     * @param aHeaderKey Key of the header.
     *
     * @return Value of the header.
     */
    public String getHeaderValue(String aHeaderKey) {
        String value = "";

        try {
            value = myMessage.getHeader(aHeaderKey, ",");
        } catch (Exception e) {
            e.printStackTrace();
            value = "";
        }

        return value;
    }

    /**
     * Method to get the html content within &lt;BODY&gt; tags
     *
     * @return  html content within <BODY> tags
     */
    public String getHtmlBodyContent(String aStr) {
        if ((aStr == null) || aStr.equals("")) {
            return aStr;
        }

        String lValue = aStr.toLowerCase();
        int    index  = 0;

        // Rip off the text before <body> tag.
        index = lValue.indexOf("<body");

        if (index >= 0) {
            aStr   = aStr.substring(index);
            lValue = lValue.substring(index);
        }

        // Rip off the text after </body> tag.
        index = lValue.indexOf("</body>");

        if (index >= 0) {
            aStr = aStr.substring(0, index + 7);
        }

        return aStr;
    }

    /**
     * Accessor method to get the html content of mail as is.
     *
     * @return Description.
     */
    public String getHtmlDescription() {
        myHtmlDescription = (myHtmlDescription == null)
                            ? ""
                            : myHtmlDescription.trim();

        return myHtmlDescription;
    }

    /**
     * Accessor method to get the value of Importance flag.
     *
     * @return Importance Flag.
     */
    public String getImportance() {
        myImportance = (myImportance == null)
                       ? ""
                       : myImportance.trim().toLowerCase();

        return myImportance;
    }

    /**
     * Accessor method to get the value of Location Table.
     *
     * @return Location table.
     */
    public Hashtable<String, String> getLocationTable() {
        return myLocationTable;
    }

    /**
     * Accessor method to get the Message Id.
     *
     * @return MessageId
     */
    public String getMessageId() {
        myMessageId = (myMessageId == null)
                      ? ""
                      : myMessageId.trim();

        return myMessageId;
    }

    /**
     * Accessor method to get the Message.
     *
     * @return Message
     */
    public MimeMessage getMimeMessage() {
        return myMessage;
    }

    /**
     * This method gets an ArrayList of personal names from the array of
     * InternetAddresses
     *
     * @param aRec Array of address objects
     *
     * @return ArrayList of corresponding personal names strings.
     */
    private ArrayList<String> getPersonalNamesList(Address[] aRec) {
        ArrayList<String> list = new ArrayList<String>();

        if ((aRec == null) || (aRec.length == 0)) {
            return list;
        }

        for (int i = 0; i < aRec.length; i++) {
            InternetAddress ia = (InternetAddress) aRec[i];

            list.add(ia.getPersonal());
        }

        return list;
    }

    /**
     * Accessor method to get the Reply-To.
     *
     * @return Reply-To.
     */
    public String getReplyTo() {
        myReplyTo = (myReplyTo == null)
                    ? ""
                    : myReplyTo.trim();

        return myReplyTo;
    }

    /**
     * Accessor method to get the value of Subject.
     *
     * @return Subject.
     */
    public String getSubject() {
        mySubject = (mySubject == null)
                    ? ""
                    : mySubject.trim();

        return mySubject;
    }

    /**
     * Accessor method to get the text content of mail as is.
     *
     * @return Description.
     */
    public String getTextDescription() {
        myTextDescription = (myTextDescription == null)
                            ? ""
                            : myTextDescription.trim();

        return myTextDescription;
    }

    /**
     * Accessor method to get the To.
     *
     * @return To.
     */
    public String getTo() {
        myToAddresses = (myToAddresses == null)
                        ? ""
                        : myToAddresses.trim();

        return myToAddresses;
    }

    /**
     * This method return the userlogin given the email address.
     *
     * @param aEmail
     * @return Userlogin corresponding to the specified email address.
     */
    private String getUserLogin(String aEmail) {
    	return aEmail;
//        String userLogin = "";
//
//        try {
//            String command = "ypmatch -k " + aEmail.toLowerCase() + " mail.aliases";
//
//            /*
//             * Create a process and execute the above command. Create a
//             * stream gobbler object to capture the error output in a separate
//             * thread.
//             */
//            Process       proc = Runtime.getRuntime().exec(command);
//            StreamGobbler sg   = new StreamGobbler(proc.getErrorStream());
//
//            sg.start();
//
//            // Read the output of the command.
//            InputStream  is     = proc.getInputStream();
//            StringBuffer output = new StringBuffer();
//            int          chr    = 0;
//
//            while ((chr = is.read()) != -1) {
//                output.append((char) chr);
//            }
//
//            is.close();
//
//            // Get the error message.
//            String errorMessage = sg.getMessage();
//
//            if ((errorMessage != null) && (errorMessage.trim().equals("") == false)) {
//                LOG.info("Error Message from ypmatch: " + errorMessage);
//                userLogin = aEmail;
//            } else {
//                String outputMessage = output.toString().trim();
//
//                // Index of colon (:)
//                int cIndex = outputMessage.indexOf(":");
//
//                // Index of ampersand (@)
//                int aIndex = outputMessage.indexOf("@");
//
//                if ((cIndex > 0) && (aIndex > 0)) {
//                    userLogin = outputMessage.substring(cIndex + 1, aIndex).trim();
//                } else {
//                    userLogin = aEmail;
//                }
//            }
//
//            proc.destroy();
//        } catch (Exception e) {
//            LOG.warn("Exception during ypmatch:" + "",(e));
//            userLogin = aEmail;
//        }
//
//        return userLogin;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * This method sets the prefix to be added to attachments for uniqueness
     * using from or replyTo address.
     *
     */
    private void setAttachmentPrefix() {
        String email = getFrom();

        if (email.equals("")) {
            email = getReplyTo();
        }

        int index = email.indexOf("@");

        if (email.indexOf("@") == -1) {
            myPrefix = email;
        } else {

            // set user login a sprefix
            myPrefix = getUserLogin(email.substring(0, index));
        }
    }

    /**
     * Setter method to set the value of linksList to repair
     *
     * @param aLinksList
     */
    public void setLinksList(String[] aLinksList) {
        myLinksList = aLinksList;
    }

    /**
     * Setter method to set the boolean value or repairLinks.
     *
     * @param aRepairLinks
     */
    public void setRepairLinks(boolean aRepairLinks) {
        myRepairLinks = aRepairLinks;
    }
}
