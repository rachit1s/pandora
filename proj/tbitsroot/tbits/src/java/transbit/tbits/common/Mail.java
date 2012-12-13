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
 * Mail.java
 *
 * $Header:
 */
package transbit.tbits.common;

//~--- non-JDK imports --------------------------------------------------------

//Current Package Imports.
//import transbit.tbits.common.TBitsLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import transbit.tbits.common.PropertiesHandler.MailProperties;

/**
 * This class contains utility methods to send mails using /usr/lib/sendmail.
 *
 * @author  : Vinod Gupta
 * @author  : Vaibhav
 * @version : $Id: $
 */
public class Mail {
	public static final TBitsLogger LOG = TBitsLogger.getLogger("transbit.tbits.common");
    private static final String  BLANK_MAILADDRESS = "undisclosed@mytbits.com";
    // private static final String SENDMAIL_PATH = "/usr/lib/sendmail";
    
    
   
    
    /**
     * Main Function For Testing the Functionalities
     */
    public static void main(String[] args) throws Exception {
        //Hashtable<String, String> table = new Hashtable<String, String>();
        
        //table.put("X-TBits-Transfer-ACK", "TBits#1234,sysHyd#12345");
        //Mail.sendWithHtmlAndReplyTo("Vaibhav", "wintest2", "", "test", "test", "test", "pondalav", 1, "Vaibhav,pondalav", true);
        sendWithHtml("giris", "vineet", "tbits_cmd_test", "Tbits CommandLine Test", "Please Ignore.");
    }
    
    public static Session getSession()
    {
            Properties props = PropertiesHandler.getAppAndSysProperties();
            props.setProperty("mail.transport.protocol", MailProperties.SMTP_PROTOCOL);
            props.setProperty("mail."+ MailProperties.SMTP_PROTOCOL +".host", MailProperties.SMTP_SERVER);
            props.setProperty("mail."+ MailProperties.SMTP_PROTOCOL +".port", Integer.toString(MailProperties.SMTP_PORT));
            //FIXME:Use Secure Password Authentication SPA
            Session mailSession = null;
            if(MailProperties.AUTH.equalsIgnoreCase("y")){
            	props.put("mail."+ MailProperties.SMTP_PROTOCOL +".auth", "true");
                mailSession = Session.getInstance(props,MailProperties.tA);
            } else {
                mailSession = Session.getInstance(props);
            }
            if(MailProperties.isDebugSession)
            {
	            mailSession.setDebug(MailProperties.isDebugSession);
	            mailSession.setDebugOut(System.out);
            }
            return mailSession;
    }
    /**
     * This method invokes the Sendmail running on the localhost which
     * queues the message for further processing.postMessageToSMTPServer
     *
     * @param aMessage     MimeMessage object
     * @param aFromAddress From Address as returnpath.
     * @param aBccAddress  List of recipients.
     */
    public static void postMessageToSMTPServer(Session session, MimeMessage aMessage, String aFromAddress, String aBccAddress) throws Exception {
//        try {
        	//
            // If emails are set Inactive for the system
            // don't send mails and return
            //
        
			String enableOutMail = MailProperties.ENABLE_OUTGOING;
			if(enableOutMail==null || enableOutMail.trim().equalsIgnoreCase("false") || enableOutMail.trim().equalsIgnoreCase("0") || enableOutMail.trim().equalsIgnoreCase("disabled") ) 
			{
				LOG.warn("Outgoing mails are disabled in the system");
			    return;
			}
        	String [] temp=null;
			temp=aFromAddress.split("<");
			String userName = temp[0];
			
			if(userName==null)
			    userName=" ";
			
			String overWriteFrmAddr = MailProperties.OVERWRITE_FROM_ADDRESS;
			String commonFromAddr = MailProperties.COMMON_FROM_ADDRESS;
			
			if(overWriteFrmAddr==null)
			  overWriteFrmAddr="false";  
			
			if (commonFromAddr==null) 
			commonFromAddr=aFromAddress;
			else
			commonFromAddr=userName+"<"+commonFromAddr+">";    
			
			if(overWriteFrmAddr.equalsIgnoreCase("true")||overWriteFrmAddr.equalsIgnoreCase("y")||overWriteFrmAddr.equalsIgnoreCase("enabled")){
			    aMessage.setFrom(new InternetAddress(commonFromAddr));
			   }
			else
			{
			aMessage.setFrom( new InternetAddress(aFromAddress));
			}
			
			InternetAddress[] recipients = null;
        	try
        	{
        		recipients = InternetAddress.parse(aBccAddress);
        	}
        	catch(AddressException ae)
        	{
        		throw ae;
        	}
        	if(recipients != null)
        	{
        		//MailEnvelope me = new MailEnvelope(aMessage, recipients, session);
        		
        	   
        	    Transport.send(aMessage, recipients);
            	//mailResMgr.queueForDelivery(me);
        	}
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            LOG.error("",(e));
//        }
//        
//        return;
    }
    
    /**
     * This method replaces the an address with given one in the specified
     * list.
     *
     */
    public static Address[] replace(Address[] list, String replaced, String replacer) {
        try {
            if (list == null) {
                return list;
            }
            
            InternetAddress ir = new InternetAddress(replacer);
            
            for (int i = 0; i < list.length; i++) {
                InternetAddress ia  = (InternetAddress) list[i];
                String          str = ia.getAddress();
                
                if ((str != null) && str.equalsIgnoreCase(replaced)) {
                    list[i] = ir;
                }
            }
        } catch (AddressException ae) {
            LOG.error("",(ae));
        }
        
        return list;
    }
    
    /**
     * Sends a mail adding the given header information.
     *
     * @param toAddress    The recepient(s) for the message.
     * @param fromAddress  The sender address for the message.
     * @param headerList   Table of header keys and values.
     *
     */
    public static void send(String toAddress, String fromAddress, Hashtable<String, String> headerList) {
        if (headerList == null) {
            return;
        }
        
        // create properties and get the default Session.
        java.util.Properties properties = new java.util.Properties();
        Session              session    = getSession();
        
        try {
            
            // Create the MimeMessage and start filling the required fields.
            MimeMessage message = new MimeMessage(session);
            
            // Convert the fromAddress in string format to an internet address
            // format and set the From: Header of the message.
            message.setFrom(new InternetAddress(fromAddress));
            
            // Convert the toAddress in string format to an internet address
            // format.
            InternetAddress[] address = InternetAddress.parse(toAddress, true);
            
            // Set the To: Header
            message.setRecipients(Message.RecipientType.TO, address);
            
            // Set the Subject.
            message.setSubject("");
            
            // Set the description or body of the message.
            message.setText("");
            
            Enumeration<String> headerKeys = headerList.keys();
            
            while (headerKeys.hasMoreElements()) {
                String headerKey   = headerKeys.nextElement();
                String headerValue = headerList.get(headerKey);
                
                message.setHeader(headerKey, headerValue);
            }
            
            //
            // finally handover the message to the sendMail running on this
            // Machine for queuing this message for further processing.
            //
            try {
            	//dumpMessage(message);
                //Transport.send(message);
                postMessageToSMTPServer(session, message, fromAddress, toAddress);
            } catch (Exception e) {
                LOG.error("",(e));
            }
        } catch (AddressException e) {
            LOG.error("",(e));
        } catch (MessagingException e) {
            LOG.error("",(e));
        }
    }
    
    /**
     * Sends a mail without any attachment.
     *
     * @param toAddress    The recepient(s) for the message.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param text         The text of the message.
     *
     */
    public static void send(String toAddress, String fromAddress, String subject, String text) {
        send(toAddress, fromAddress, subject, text, fromAddress);
    }
    
    /**
     * Sends a mail without any attachment, but with a reply to
     *
     * @param toAddress    The recepient(s) for the message.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param text         The text of the message.
     * @param replyTo      The address which as to be replied to
     *
     */
    public static void send(String toAddress, String fromAddress, String subject, String text, String replyTo) {
        
        // create properties and get the default Session.
        java.util.Properties properties = new java.util.Properties();
        Session              session    = getSession();
        
        try {
            
            //
            // Create an internet address to specify this as the reply-to
            // for this generated mail.
            //
            InternetAddress[] replyToAddress = new InternetAddress[1];
            
            replyToAddress[0] = new InternetAddress(replyTo);
            
            final String messageTxt = text;
            
            // Create the MimeMessage and start filling the required fields.
            MimeMessage message = new MimeMessage(session);
            
            // Convert the fromAddress in string format to an internet address
            // format and set the From: Header of the message.
            message.setFrom(new InternetAddress(fromAddress));
            
            // Convert the toAddress in string format to an internet address
            // format.
            InternetAddress[] address = InternetAddress.parse(toAddress, true);
            
            // Set the To: Header
            message.setRecipients(Message.RecipientType.TO, address);
            
            // Set the Reply-To Header.
            message.setReplyTo(replyToAddress);
            
            // Set the Subject.
            message.setSubject(subject);
            
            // Set the description or body of the message.
            message.setText(text);
            
            //
            // finally handover the message to the sendMail running on this
            // Machine for queuing this message for further processing.
            //
            try {
            	//dumpMessage(message);
                //Transport.send(message);
                postMessageToSMTPServer(session, message, fromAddress, toAddress);
            } catch (Exception e) {
                LOG.error("",(e));
            }
        } catch (AddressException e) {
            LOG.error("",(e));
        } catch (MessagingException e) {
            LOG.error("",(e));
        }
    }
    
    /**
     * Sends a mail along with bounced mail as attachment.
     *
     * @param toAddress    The recepient(s) for the message.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param text         The text of the message.
     * @param attachment   The user mail that bounced.
     */
    public static void sendBouncedMailAsAttachment(String toAddress, String fromAddress, String subject, String text, MimeMessage attachment) {
        
        // create properties and get the default Session.
        java.util.Properties properties = new java.util.Properties();
        Session              session    = getSession();
        
        try {
            final String messageTxt = text;
            
            // Create a MimeMessage and start building it.
            MimeMessage message = new MimeMessage(session);
            
            // Set the Importance: header to High.
            message.addHeader("Importance", "high");
            
            // Set the From: Header.
            message.setFrom(new InternetAddress(fromAddress));
            
            // Set the To: Header.
            InternetAddress[] address = InternetAddress.parse(toAddress, true);
            
            message.setRecipients(Message.RecipientType.TO, address);
            
            // Set the Subject: Header.
            message.setSubject(subject);
            
            // create and fill the first message part
            MimeBodyPart mbp1 = new MimeBodyPart();
            
            mbp1.setContent(text, "text/plain");
            mbp1.setHeader("MIME-Version", "1.0");
            mbp1.setHeader("Content-Type", "text/plain; charset=UTF-8");
            
            Multipart mp = new MimeMultipart();
            
            mp.addBodyPart(mbp1, 0);
            
            Object        object      = attachment.getContent();
            MimeMultipart mailContent = null;
            
            if (object instanceof String) {
                
                // create and fill the second message part
                MimeBodyPart mbp2 = new MimeBodyPart();
                
                mbp2.setText((String) object);
                mbp2.setDisposition(Part.ATTACHMENT);
                mbp2.setFileName(attachment.getSubject() + ".txt");
                mp.addBodyPart(mbp2, 1);
            } else if (object instanceof MimeMultipart) {
                mailContent = (MimeMultipart) object;
                
                int    i        = 1;
                Part   bodyPart = (BodyPart) mailContent.getBodyPart(0);
                Object tempobj  = bodyPart.getContent();
                
                if (tempobj instanceof String) {
                    MimeBodyPart mbp2 = new MimeBodyPart();
                    
                    mbp2.setText((String) tempobj);
                    mbp2.setDisposition(Part.ATTACHMENT);
                    mbp2.setFileName(attachment.getSubject() + ".txt");
                    mp.addBodyPart(mbp2, i);
                } else if (tempobj instanceof MimeMultipart) {
                    MimeMultipart tempMp = (MimeMultipart) tempobj;
                    MimeBodyPart  mbp3   = new MimeBodyPart();
                    
                    mbp3.setDataHandler((tempMp.getBodyPart(0)).getDataHandler());
                    mbp3.setDisposition(Part.ATTACHMENT);
                    mbp3.setFileName(attachment.getSubject() + ".txt");
                    mp.addBodyPart(mbp3, i);
                    mbp3 = new MimeBodyPart();
                    mbp3.setDataHandler((tempMp.getBodyPart(1)).getDataHandler());
                    mbp3.setDisposition(Part.ATTACHMENT);
                    mbp3.setFileName(attachment.getSubject() + ".htm");
                    mp.addBodyPart(mbp3, ++i);
                }
                
                for (int j = 1; j < mailContent.getCount(); j++) {
                    MimeBodyPart mbp3 = new MimeBodyPart();
                    
                    mbp3.setDataHandler((mailContent.getBodyPart(j)).getDataHandler());
                    mbp3.setDisposition(Part.ATTACHMENT);
                    
                    String fileName = (mailContent.getBodyPart(j)).getFileName();
                    
                    if ((fileName == null) || fileName.equals("")) {
                        fileName = "untitled.txt";
                    }
                    
                    mbp3.setFileName(fileName);
                    mp.addBodyPart(mbp3, ++i);
                }
            }
            
            // add the Multipart to the message
            message.setContent(mp);
            message.setHeader("MIME-Version", "1.0");
            message.setHeader("Content-Type", mp.getContentType());
            
            //
            // finally handover the message to the sendMail running on this
            // Machine for queuing this message for further processing.
            //
            try {
            	//dumpMessage(message);
                //Transport.send(message);
            	postMessageToSMTPServer(session, message, fromAddress, toAddress);
            } catch (Exception e) {
                LOG.error("",(e));
            }
        } catch (AddressException e) {
            LOG.error("",(e));
        } catch (MessagingException e) {
            LOG.error("",(e));
        } catch (IOException e) {
            LOG.error("",(e));
        }
    }
    
    /**
     * Sends a mail along with html, and attachments
     *
     * @param toAddress    The recepient(s) for the message.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param aMultiPart   The html text and attachments formed as multipart
     * @throws Exception 
     *
     */
    public static void sendHtmlAndAttachments(String toAddress, String fromAddress, String subject, MimeMultipart aMultiPart) throws Exception {
        sendHtmlAndAttachments(toAddress, fromAddress, subject, fromAddress, 0, toAddress, aMultiPart);
    }
    
    /**
     * Sends a mail along with html, and attachments
     *
     * @param toAddress    The recepient(s) for the message.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param replyTo      The replyTo address for the message
     * @param severityFlag The severity of the message
     * @param aTBitsRecipients The value for TBits-Recipients header
     * @param aMultiPart   The html text and attachments formed as multipart
     * @throws Exception 
     *
     */
    public static void sendHtmlAndAttachments(String toAddress, String fromAddress, String subject, String replyTo, int severityFlag, String aTBitsRecipients, MimeMultipart aMultiPart) throws Exception {
        sendHtmlAndAttachments(toAddress, fromAddress, subject, replyTo, severityFlag, aTBitsRecipients, new Hashtable<String, String>(), aMultiPart);
    }
    
    /**
     * Sends a mail along with html, and attachments
     *
     * @param toAddress    The recepient(s) for the message.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param replyTo      The replyTo address for the message
     * @param severityFlag The severity of the message
     * @param aTBitsRecipients The value for TBits-Recipients header
     * @param aMultiPart   The html text and attachments formed as multipart
     * @throws Exception 
     *
     */
    public static void sendHtmlAndAttachments(String toAddress, String fromAddress, String subject, String replyTo, int severityFlag, String aTBitsRecipients, Hashtable<String, String> extHeaders,
            MimeMultipart aMultiPart) throws Exception {
        
        // create properties and get the default Session.
        java.util.Properties properties = new java.util.Properties();
        Session              session    = getSession();
        
        try {
            if ((fromAddress == null) || fromAddress.equals("")) {
                fromAddress = BLANK_MAILADDRESS;
            }
            
            // create a message.
            MimeMessage message = new MimeMessage(session);
            
           
             // Set the From: Header.
            message.setFrom(new InternetAddress(fromAddress));
            
            // Set the To: and Reply-To: Headers
            InternetAddress[] replyToAddress = new InternetAddress[1];
            
            replyToAddress[0] = new InternetAddress(replyTo);
            message.setReplyTo(replyToAddress);
            message.setRecipients(Message.RecipientType.TO, toAddress);
            
            // Set the actual recipients of the mail in Bcc List.
            InternetAddress[] ccInetAddress = InternetAddress.parse(toAddress, true);
            
            message.setRecipients(Message.RecipientType.BCC, ccInetAddress);
            message.setSubject(subject, "UTF-8");
            
            // Insert the Importance: header according to the severity.
            String severityHeaderValue = "";
            
            // Add severity header if severity is low, high or critical
            if (severityFlag == 1) {
                severityHeaderValue = "high";
            } else if (severityFlag == -1) {
                severityHeaderValue = "low";
            }
            
            message.addHeader("Importance", severityHeaderValue);
            
            // Add TBits-Recipients Header
            message.addHeader("X-TBits-Recipients", aTBitsRecipients);
            
            /*
             * Add any other extended headers passed as a part of extHeader
             * table.
             */
            if ((extHeaders != null) && (extHeaders.size() != 0)) {
                Enumeration<String> headerNames = extHeaders.keys();
                
                while (headerNames.hasMoreElements()) {
                    String headerName  = headerNames.nextElement();
                    String headerValue = extHeaders.get(headerName);
                    
                    if (headerValue != null) {
                        message.addHeader(headerName, headerValue);
                    }
                }
            }
            
            // add the Multipart to the message
            message.setContent(aMultiPart);
            message.setHeader("MIME-Version", "1.0");
            message.setHeader("Content-Type", aMultiPart.getContentType());
            
            //
            // finally handover the message to the sendMail running on this
            // Machine for queuing this message for further processing.
            //
            	//dumpMessage(message);
            	//Transport.send(message);
            	postMessageToSMTPServer(session, message, fromAddress, toAddress);
         
        } catch (AddressException e) {
            LOG.error("",(e));
            throw e ;
        } catch (MessagingException e) {
            LOG.error("",(e));
            throw e ;        
        } catch (Exception e) {
            LOG.error("",(e));
            throw e ;
        }
    }
    
    
    public static void sendHtmlAndAttachmentsWithCc(String toAddress, String ccAddress, String fromAddress, String subject, Multipart aMultiPart) {
        
    	LOG.error("toAddress : " + toAddress + " & ccAddress : "+ ccAddress +"&fromAddress"+fromAddress);
        // create properties and get the default Session.
        java.util.Properties properties = new java.util.Properties();
        Session              session    = getSession();
        
        try {
            if ((fromAddress == null) || fromAddress.equals("")) {
                fromAddress = BLANK_MAILADDRESS;
            }
            
            // create a message.
            MimeMessage message = new MimeMessage(session);
                                    
            // Set the From: Header.
            message.setFrom(new InternetAddress(fromAddress));
            
            // Set the To: and Reply-To: Headers
            InternetAddress[] replyToAddress = new InternetAddress[1];
            
            replyToAddress[0] = new InternetAddress(fromAddress);
            message.setReplyTo(replyToAddress);
            
            InternetAddress[] toInetAddress = InternetAddress.parse(toAddress, true);
            message.setRecipients(Message.RecipientType.TO, toInetAddress);
            
            // Set the actual recipients of the mail in cc List.
            InternetAddress[] ccInetAddress = InternetAddress.parse(ccAddress, true);            
            message.setRecipients(Message.RecipientType.CC, ccInetAddress);
            message.setSubject(subject);  
                       
            // add the Multipart to the message
            message.setContent(aMultiPart);
            message.setHeader("MIME-Version", "1.0");
            message.setHeader("Content-Type", aMultiPart.getContentType());
            
            //
            // finally handover the message to the sendMail running on this
            // Machine for queuing this message for further processing.
            //
            try {            	
            	postMessageToSMTPServer(session, message, fromAddress, toAddress+","+ccAddress);
            } catch (Exception e) {
                LOG.error("",(e));
            }
        } catch (AddressException e) {
            LOG.error("",(e));
        } catch (MessagingException e) {
            LOG.error("",(e));
        }
    }        
    
    private static void dumpMessage(MimeMessage message)
    {
    	System.out.print(message.toString());
    }
    /**
     * Sends a mail alongwith attachment(s).
     *
     * @param toAddress    The recepient(s) for the message.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param text         The text of the message.
     * @param attachment   The attachment (together with its path).
     *
     */
    public static void sendWithAttachment(String toAddress, String fromAddress, String subject, String text, String attachment) {
        sendWithAttachment(toAddress, fromAddress, subject, text, attachment, fromAddress);
    }
    
    /**
     * Sends a mail alongwith attachment(s).
     *
     * @param toAddress    The recepient(s) for the message.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param text         The text of the message.
     * @param attachment   The attachment (together with its path).
     * @param replyTo      The address which has to be replied to
     */
    public static void sendWithAttachment(String toAddress, String fromAddress, String subject, String text, String attachment, String replyTo) {
        
        // create properties and get the default Session.
        java.util.Properties properties = new java.util.Properties();
        Session              session    = getSession();
        
        try {
            InternetAddress[] replyToAddress = new InternetAddress[1];
            
            replyToAddress[0] = new InternetAddress(replyTo);
            
            // create a message.
            final String      messageTxt = text;
            MimeMessage message    = new MimeMessage(session);
            
            message.setFrom(new InternetAddress(fromAddress));
            
            InternetAddress[] address = InternetAddress.parse(toAddress, true);
            
            message.setRecipients(Message.RecipientType.TO, address);
            message.setReplyTo(replyToAddress);
            message.setSubject(subject);
            
            // create and fill the first message part
            MimeBodyPart mbp1 = new MimeBodyPart();
            
            mbp1.setText(text);
            
            File           file = new File(attachment);
            FileDataSource fds  = new FileDataSource(file);
            
            // create and fill the second message part
            MimeBodyPart mbp2 = new MimeBodyPart();
            
            mbp2.setDataHandler(new DataHandler(fds));
            mbp2.setDisposition(Part.ATTACHMENT);
            mbp2.setFileName(file.getName());
            
            // create the Multipart and its parts to it
            Multipart mp = new MimeMultipart();
            
            mp.addBodyPart(mbp1);
            mp.addBodyPart(mbp2);
            
            // add the Multipart to the message
            message.setContent(mp);
            
            //
            // finally handover the message to the sendMail running on this
            // Machine for queuing this message for further processing.
            //
            try {
            	//dumpMessage(message);
                //Transport.send(message);
            	postMessageToSMTPServer(session, message, fromAddress, toAddress);
            } catch (Exception e) {
                LOG.error("",(e));
            }
        } catch (AddressException e) {
            LOG.info("",(e));
        } catch (MessagingException e) {
            LOG.info("",(e));
        }
    }
    
    /**
     * Sends a mail without any attachment, but with a BCC
     *
     * @param bccAddress   The BCC address.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param text         The text of the message.
     * @param replyTo      The address which as to be replied to
     * @param severityFlag The importance of mail
     * @param aTBitsRecipients The value of TBits-Recipients header
     */
    public static void sendWithBCC(String bccAddress, String fromAddress, String subject, String text, String replyTo, int severityFlag, String aTBitsRecipients) {
        
        // create properties and get the default Session.
        java.util.Properties properties = new java.util.Properties();
        Session              session    = getSession();
        
        try {
            final String messageTxt = text;
            
            // Create the MimeMessage object and start building it.
            MimeMessage message = new MimeMessage(session);
            
            // Add severity header if severity is low, high or critical
            if (severityFlag == 1) {
                message.addHeader("Importance", "high");
            } else if (severityFlag == -1) {
                message.addHeader("Importance", "low");
            }
            
            // Add TBits-Recipients Header
            message.addHeader("X-TBits-Recipients", sortCSV(aTBitsRecipients));
            message.setFrom(new InternetAddress(fromAddress));
            
            if ((replyTo == null) || (replyTo.trim().equals("") == true)) {
                replyTo = fromAddress;
            }
            
            InternetAddress[] replyToAddress = new InternetAddress[1];
            
            replyToAddress[0] = new InternetAddress(replyTo);
            
            // Set the Reply-To:
            message.setReplyTo(replyToAddress);
            
            InternetAddress[] bccInetAddress = InternetAddress.parse(bccAddress, true);
            
            message.setRecipients(Message.RecipientType.BCC, bccInetAddress);
            message.setSubject(subject);
            
            MimeMultipart mp  = new MimeMultipart();
            MimeBodyPart  mbp = new MimeBodyPart();
            
            text = text.replaceAll("^[\n]+", "");
            text = Utilities.htmlEncode(text);
            text = text.replaceAll("\\r\\n", "<br>");
            text = text.replaceAll("\\n", "<br>");
            text = text.replaceAll("\\r", "<br>");
            text = text.replaceAll("<br>", "\n<br>");
            text = text.replaceAll("<br> ", "<br>&nbsp;");
            text = text.replaceAll("  ", "&nbsp;&nbsp;");
            text = text.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
            mbp.setContent(text, "text/html");
            mbp.setHeader("Content-Type", "text/html; charset=UTF-8");
            mp.addBodyPart(mbp);
            message.setContent(mp);
            
            //
            // finally handover the message to the sendMail running on this
            // Machine for queuing this message for further processing.
            //
            try {
            	//dumpMessage(message);
                //Transport.send(message);
            	postMessageToSMTPServer(session, message, fromAddress, bccAddress);
            } catch (Exception e) {
                LOG.error("",(e));
            }
        } catch (AddressException e) {
            LOG.error("",(e));
        } catch (MessagingException e) {
            LOG.error("",(e));
        }
    }
    
    /**
     * Sends a mail without any attachment, but with a cc
     *
     * @param toAddress    The recepient(s) for the message.
     * @param ccAddress    The CC address.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param text         The text of the message.
     *
     */
    public static void sendWithCC(String toAddress, String ccAddress, String fromAddress, String subject, String text) {
        sendWithCC(toAddress, ccAddress, fromAddress, subject, text, fromAddress, 0, toAddress + ccAddress);
    }
    
    /**
     * Sends a mail without any attachment, but with a cc and Reply To
     *
     * @param toAddress    The recepient(s) for the message.
     * @param ccAddress    The CC address.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param text         The text of the message.
     * @param replyTo      The address which as to be replied to
     * @param severityFlag The importance of mail
     * @param aTBitsRecipients The value of TBits-Recipients header
     */
    public static void sendWithCC(String toAddress, String ccAddress, String fromAddress, String subject, String text, String replyTo, int severityFlag, String aTBitsRecipients) {
        
        // create properties and get the default Session.
        java.util.Properties properties = new java.util.Properties();
        Session              session    = getSession();
        
        try {
            final String messageTxt = text;
            
            // Create the MimeMessage object and start building it.
            MimeMessage message = new MimeMessage(session);
            
            // Add severity header if severity is low, high or critical
            if (severityFlag == 1) {
                message.addHeader("Importance", "high");
            } else if (severityFlag == -1) {
                message.addHeader("Importance", "low");
            }
            
            // Add TBits-Recipients Header
            message.addHeader("X-TBits-Recipients", sortCSV(aTBitsRecipients));
            message.setFrom(new InternetAddress(fromAddress));
            
            if ((replyTo == null) || (replyTo.trim().equals("") == true)) {
                replyTo = fromAddress;
            }
            
            InternetAddress[] replyToAddress = new InternetAddress[1];
            
            replyToAddress[0] = new InternetAddress(replyTo);
            
            // Set the Reply-To
            message.setReplyTo(replyToAddress);
            
            InternetAddress[] toInetAddress = InternetAddress.parse(toAddress, true);
            
            message.setRecipients(Message.RecipientType.TO, toInetAddress);
            
            InternetAddress[] ccInetAddress = InternetAddress.parse(ccAddress, true);
            
            message.setRecipients(Message.RecipientType.CC, ccInetAddress);
            message.setSubject(subject);
            message.setText(text);
            
            //
            // finally handover the message to the sendMail running on this
            // Machine for queuing this message for further processing.
            //
            try {
            	//dumpMessage(message);
                //Transport.send(message);
            	postMessageToSMTPServer(session, message, fromAddress, ccAddress);
            } catch (Exception e) {
                LOG.error("",(e));
            }
        } catch (AddressException e) {
            LOG.error("",(e));
        } catch (MessagingException e) {
            LOG.error("",(e));
        }
    }
    
    /**
     * Sends a mail alongwith html.
     *
     * @param toAddress    The recepient(s) for the message.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param htmlText     The html part of message
     *
     */
    public static void sendWithHtml(String toAddress, String fromAddress, String subject, String htmlText) {
        sendWithHtml(toAddress, null, fromAddress, subject, htmlText);
    }
    
    /**
     * Sends a mail alongwith html, with the CC list
     *
     * @param toAddress    The recepient(s) for the message.
     * @param ccAddress    The cc list) for the message.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param htmlText     The html part of message
     *
     */
    public static void sendWithHtml(String toAddress, String ccAddress, String fromAddress, String subject, String htmlText) {
        
        // create properties and get the default Session.
        java.util.Properties properties = new java.util.Properties();
        Session              session    = getSession();
        
        try {
            if ((fromAddress == null) || fromAddress.equals("")) {
                fromAddress = BLANK_MAILADDRESS;
            }
            
            // create a message.
            MimeMessage message = new MimeMessage(session);
            
            message.setFrom(new InternetAddress(fromAddress));
            
            InternetAddress[] address = InternetAddress.parse(toAddress, true);
            
            message.setRecipients(Message.RecipientType.TO, address);
            
            if ((ccAddress != null) && (ccAddress.trim().equals("") == false)) {
                InternetAddress[] ccInetAddress = InternetAddress.parse(ccAddress, true);
                
                message.setRecipients(Message.RecipientType.CC, ccInetAddress);
                
                if ((toAddress != null) && (toAddress.trim().equals("") == false)) {
                    toAddress = toAddress + "," + ccAddress;
                } else {
                    toAddress = ccAddress;
                }
            }
            
            message.setSubject(subject);
            
            MimeBodyPart mbp2 = new MimeBodyPart();
            
            mbp2.setContent(htmlText, "text/html");
            
            // create the Multipart and its parts to it
            MimeMultipart mp = new MimeMultipart();
            
            mp.setSubType("alternative");
            
            // mp.addBodyPart(mbp1);
            mp.addBodyPart(mbp2);
            
            // add the Multipart to the message
            message.setContent(mp);
            
            //
            // finally handover the message to the sendMail running on this
            // Machine for queuing this message for further processing.
            //
            try {
            	//dumpMessage(message);
                //Transport.send(message);
            	postMessageToSMTPServer(session, message, fromAddress, toAddress);
            } catch (Exception e) {
                LOG.error("",(e));
            }
        } catch (AddressException e) {
            LOG.error("",(e));
        } catch (MessagingException e) {
            LOG.error("",(e));
        }
    }
    
    /**
     * Sends a mail without any attachment, but with a cc and Reply To
     *
     * @param toAddress    The recepient(s) for the message.
     * @param ccAddress    The CC address.
     * @param bccAddress   The CC address.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param htmlText     The html content of the message.
     * @param replyTo      The address which as to be replied to
     * @param severityFlag The importance of mail
     * @param aTBitsRecipients The value of TBits-Recipients header
     */
    public static void sendWithHtmlAndCC(String toAddress, String ccAddress, String bccAddress, String fromAddress, String subject, String htmlText, String replyTo, int severityFlag,
            String aTBitsRecipients) {
        
        // create properties and get the default Session.
        java.util.Properties properties = new java.util.Properties();
        Session              session    = getSession();
        
        try {
            
            // Create the MimeMessage object and start building it.
            MimeMessage message = new MimeMessage(session);
            
            // Add severity header if severity is low, high or critical
            if (severityFlag == 1) {
                message.addHeader("Importance", "high");
            } else if (severityFlag == -1) {
                message.addHeader("Importance", "low");
            }
            
            // Add TBits-Recipients Header
            message.addHeader("X-TBits-Recipients", sortCSV(aTBitsRecipients));
            message.setFrom(new InternetAddress(fromAddress));
            
            if ((replyTo == null) || (replyTo.trim().equals("") == true)) {
                replyTo = fromAddress;
            }
            
            InternetAddress[] replyToAddress = new InternetAddress[1];
            
            replyToAddress[0] = new InternetAddress(replyTo);
            
            // Set the Reply-To: and To: headers.
            message.setReplyTo(replyToAddress);
            
            InternetAddress[] toInetAddresses = InternetAddress.parse(toAddress, true);
            
            message.setRecipients(Message.RecipientType.TO, toInetAddresses);
            
            // Set the From: header.
            message.setFrom(new InternetAddress(fromAddress));
            
            if ((ccAddress != null) && (ccAddress.equals("") == false)) {
                InternetAddress[] temp = InternetAddress.parse(ccAddress, true);
                
                message.setRecipients(Message.RecipientType.CC, temp);
                ccAddress = toAddress + "," + ccAddress;
            } else {
                ccAddress = toAddress;
            }
            
            if ((bccAddress != null) && (bccAddress.equals("") == false)) {
                ccAddress = ccAddress + "," + bccAddress;
            }
            
            InternetAddress[] ccInetAddress = InternetAddress.parse(ccAddress, true);
            
            message.setRecipients(Message.RecipientType.BCC, ccInetAddress);
            message.setSubject(subject);
            
            MimeMultipart mp  = new MimeMultipart();
            MimeBodyPart  mbp = new MimeBodyPart();
            
            mbp.setContent(htmlText, "text/html");
            mp.addBodyPart(mbp);
            message.setContent(mp);
            
            //
            // finally handover the message to the sendMail running on this
            // Machine for queuing this message for further processing.
            //
            try {
            	//dumpMessage(message);
                //Transport.send(message);
            	postMessageToSMTPServer(session, message, fromAddress, ccAddress);
            } catch (Exception e) {
                LOG.error("",(e));
            }
        } catch (AddressException e) {
            LOG.error("",(e));
        } catch (MessagingException e) {
            LOG.error("",(e));
        }
    }
    
    /**
     * Sends a mail alongwith html and the replyTo mailId
     *
     * @param toAddress    The recepient(s) for the message.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param htmlText     The html part of message
     * @param replyTo      The address to which the mail has to be replied
     * @throws MessagingException 
     */
    public static void sendWithHtmlAndReplyTo(String toAddress, String fromAddress, String subject, String htmlText, String replyTo) throws MessagingException {
        sendWithHtmlAndReplyTo(toAddress, null, fromAddress, subject, htmlText, "", replyTo, 0, toAddress);
    }
    
    /**
     * Sends a mail alongwith html, with the CC list and replyTo address
     *
     * @param toAddress    The recepient(s) for the message.
     * @param ccAddress    The cc list) for the message.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param htmlText     The html part of message
     * @param plainText    The message content as plain text
     * @param replyTo      The reply to address of the mail
     * @param severityFlag The importance of mail
     * @param aTBitsRecipients The value for TBits-Recipients header
     * @throws MessagingException 
     */
    public static void sendWithHtmlAndReplyTo(String toAddress, String ccAddress, String fromAddress, String subject, String htmlText, String plainText, String replyTo, int severityFlag,
            String aTBitsRecipients) throws MessagingException {
        Hashtable<String, String> extHeaders = new Hashtable<String, String>();
        
        sendWithHtmlAndReplyTo(toAddress, ccAddress, fromAddress, subject, htmlText, plainText, replyTo, severityFlag, aTBitsRecipients, extHeaders);
    }
    
    /**
     * Sends a mail alongwith html, with the CC list and replyTo address
     *
     * @param toAddress    The recepient(s) for the message.
     * @param ccAddress    The cc list) for the message.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param htmlText     The html part of message
     * @param aPlainText   The message content as plain text
     * @param replyTo      The reply to address of the mail
     * @param severityFlag The importance of mail
     * @param aTBitsRecipients The value for TBits-Recipients header
     * @param printCcLabels  Flag to specify if Cc list should be printed on the
     *                       envelope.
     */
    public static void sendWithHtmlAndReplyTo(String toAddress, String ccAddress, String fromAddress, String subject, String htmlText, String aPlainText, String replyTo, int severityFlag,
            String aTBitsRecipients, boolean printCcLabels) {
        
        // create properties and get the default Session.
        java.util.Properties properties = new java.util.Properties();
        Session              session    = getSession();
        
        try {
            MimeMessage message = new MimeMessage(session);
            
            // Set the To: and Reply-To: Headers.
            InternetAddress[] replyToAddress = new InternetAddress[1];
            
            replyToAddress[0] = new InternetAddress(replyTo);
            message.setRecipients(Message.RecipientType.TO, replyToAddress);
            message.setReplyTo(replyToAddress);
            
            // Set the Importance: header according to the severity.
            String severityHeaderValue = "";
            
            // Add severity header if severity is low, high or critical
            if (severityFlag == 1) {
                severityHeaderValue = "high";
            } else if (severityFlag == -1) {
                severityHeaderValue = "low";
            }
            
            message.addHeader("Importance", severityHeaderValue);
            
            // Add Custom X-TBits-Recipients Header
            message.addHeader("X-TBits-Recipients", sortCSV(aTBitsRecipients));
            
            // Set the From: Header.
            message.setFrom(new InternetAddress(fromAddress));
            
            if (ccAddress != null) {
                if (printCcLabels == true) {
                    InternetAddress[] ccLabels = InternetAddress.parse(ccAddress, true);
                    
                    message.setRecipients(Message.RecipientType.CC, ccLabels);
                }
                
                ccAddress = toAddress + "," + ccAddress;
            } else {
                ccAddress = toAddress;
            }
            
            // Get the Internet address of all the actual recipients and put
            // them in the Bcc: List.
            InternetAddress[] ccInetAddress = InternetAddress.parse(ccAddress, true);
            
            message.setRecipients(Message.RecipientType.BCC, ccInetAddress);
            
            // Set the Subject Header.
            message.setSubject(subject);
            
            //
            // Create a multipart message and set the content-type to
            // multipart-alternative as it is going to contain the text-version
            // and html-version for this mail.
            //
            MimeMultipart mp       = new MimeMultipart("alternative");
            MimeBodyPart  htmlPart = new MimeBodyPart();
            MimeBodyPart  textPart = new MimeBodyPart();
            
            if ((aPlainText != null) && (aPlainText.trim().equals("") == false)) {
                textPart.setContent(aPlainText, "text/plain");
                textPart.setHeader("MIME-Version", "1.0");
                textPart.setHeader("Content-Type", "text/plain; charset=UTF-8");
                textPart.setHeader("Importance", severityHeaderValue);
                mp.addBodyPart(textPart);
            }
            
            if ((htmlText != null) && (htmlText.trim().equals("") == false)) {
                htmlText = htmlText.replaceAll("\\.\n", ".&nbsp;\n");
                htmlPart.setContent(htmlText, "text/html");
                htmlPart.setHeader("MIME-Version", "1.0");
                htmlPart.setHeader("Content-Type", "text/html; charset=UTF-8");
                htmlPart.setHeader("Importance", severityHeaderValue);
                mp.addBodyPart(htmlPart);
            }
            
            // add the Multipart to the message
            message.setContent(mp);
            message.setHeader("MIME-Version", "1.0");
            message.setHeader("Content-Type", mp.getContentType());
            
            //
            // finally handover the message to the sendMail running on this
            // Machine for queuing this message for further processing.
            //
            try {
            	//dumpMessage(message);
                //Transport.send(message);
            	postMessageToSMTPServer(session, message, fromAddress, ccAddress);
            } catch (Exception e) {
                LOG.error("",(e));
            }
        } catch (AddressException e) {
            LOG.error("",(e));
        } catch (MessagingException e) {
            LOG.error("",(e));
        }
    }
    
    /**
     * Sends a mail alongwith html, with the CC list and replyTo address
     *
     * @param toAddress    The recepient(s) for the message.
     * @param ccAddress    The cc list) for the message.
     * @param fromAddress  The sender address for the message.
     * @param subject      The subject of the message.
     * @param htmlText     The html part of message
     * @param aPlainText   The message content as plain text
     * @param replyTo      The reply to address of the mail
     * @param severityFlag The importance of mail
     * @param aTBitsRecipients The value for TBits-Recipients header
     * @throws MessagingException 
     */
    public static void sendWithHtmlAndReplyTo(String toAddress, String ccAddress, String fromAddress, 
    		String subject, String htmlText, String aPlainText, String replyTo, int severityFlag,
            String aTBitsRecipients, Hashtable<String, String> extHeaders) throws MessagingException {
        // create properties and get the default Session.
        java.util.Properties properties = new java.util.Properties();
        Session              session    = getSession();
        
        try {
            MimeMessage message = new MimeMessage(session);
            
            // Set the To: and Reply-To: Headers.
            InternetAddress[] replyToAddress = new InternetAddress[1];
            
            replyToAddress[0] = new InternetAddress(replyTo);
            message.setRecipients(Message.RecipientType.TO, toAddress);
            message.setReplyTo(replyToAddress);
            
            // Set the Importance: header according to the severity.
            String severityHeaderValue = "";
            
            // Add severity header if severity is low, high or critical
            if (severityFlag == 1) {
                severityHeaderValue = "high";
            } else if (severityFlag == -1) {
                severityHeaderValue = "low";
            }
            
            message.addHeader("Importance", severityHeaderValue);
            
            // Add Custom X-TBits-Recipients Header
            message.addHeader("X-TBits-Recipients", sortCSV(aTBitsRecipients));
            
            /*
             * Add any other extended headers passed as a part of extHeader
             * table.
             */
            if ((extHeaders != null) && (extHeaders.size() != 0)) {
                Enumeration<String> headerNames = extHeaders.keys();
                
                while (headerNames.hasMoreElements()) {
                    String headerName  = headerNames.nextElement();
                    String headerValue = extHeaders.get(headerName);
                    
                    if (headerValue != null) {
                        message.addHeader(headerName, headerValue);
                    }
                }
            }
            
            // Set the From: Header.
            message.setFrom(new InternetAddress(fromAddress));
            
            if (ccAddress != null) {
                ccAddress = toAddress + "," + ccAddress;
            } else {
                ccAddress = toAddress;
            }
            
            // Get the Internet address of all the actual recipients and put
            // them in the Bcc: List.
            InternetAddress[] ccInetAddress = InternetAddress.parse(ccAddress, true);
            
            message.setRecipients(Message.RecipientType.BCC, ccInetAddress);
            
            // Set the Subject Header.
            message.setSubject(subject);
            
            //
            // Create a multipart message and set the content-type to
            // multipart-alternative as it is going to contain the text-version
            // and html-version for this mail.
            //
            MimeMultipart mp       = new MimeMultipart("alternative");
            MimeBodyPart  htmlPart = new MimeBodyPart();
            MimeBodyPart  textPart = new MimeBodyPart();
            
            if ((aPlainText != null) && (aPlainText.trim().equals("") == false)) {
                textPart.setContent(aPlainText, "text/plain");
                textPart.setHeader("MIME-Version", "1.0");
                textPart.setHeader("Content-Type", "text/plain; charset=UTF-8");
                textPart.setHeader("Importance", severityHeaderValue);
                mp.addBodyPart(textPart);
            }
            
            if ((htmlText != null) && (htmlText.trim().equals("") == false)) {
                htmlText = htmlText.replaceAll("\\.\n", ".&nbsp;\n");
                htmlPart.setContent(htmlText, "text/html");
                htmlPart.setHeader("MIME-Version", "1.0");
                htmlPart.setHeader("Content-Type", "text/html; charset=UTF-8");
                htmlPart.setHeader("Importance", severityHeaderValue);
                mp.addBodyPart(htmlPart);
            }
            
            // add the Multipart to the message
            message.setContent(mp);
            message.setHeader("MIME-Version", "1.0");
            message.setHeader("Content-Type", mp.getContentType());
            
            //
            // finally handover the message to the sendMail running on this
            // Machine for queuing this message for further processing.
            //
            try {
            	//dumpMessage(message);
                //Transport.send(message);
            	postMessageToSMTPServer(session, message, fromAddress, ccAddress);
            } catch (Exception e) {
                LOG.error("",(e));
                LOG.error("Error while sending following message: ");
                LOG.error("TO: ");
                for(Address toAdd : message.getAllRecipients())
                {
                    LOG.error(toAdd.toString());
                }
                LOG.error("From: ");
                for(Address fromAdd : message.getFrom())
                {
                    LOG.error(fromAdd.toString());
                }
                LOG.error("Sender: " + message.getSender());
                LOG.error("Subject: " + message.getSubject());
            }
        } catch (AddressException e) {
            LOG.info("",(e));
            throw e ;
        } catch (MessagingException e) {
            LOG.info("",(e));
            throw e ;
        }
    }
    
    /**
     * This method returns the given comma-separated string values back in
     * same fashion but sorted in alphabetical order.
     *
     * @param aArg Argument String as a Comma-separated List.
     *
     * @return Comma-separated List of values passed in sorted order.
     */
    public static String sortCSV(String aArg) {
        StringTokenizer   st   = new StringTokenizer(aArg, ",");
        ArrayList<String> list = new ArrayList<String>();
        
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        
        Object[] arr = list.toArray();
        
        Arrays.sort(arr);
        
        StringBuilder buffer = new StringBuilder();
        
        if (arr.length > 0) {
            buffer.append((String) arr[0]);
        }
        
        for (int i = 1; i < arr.length; i++) {
            buffer.append(",").append((String) arr[i]);
        }
        
        return buffer.toString();
    }
}
