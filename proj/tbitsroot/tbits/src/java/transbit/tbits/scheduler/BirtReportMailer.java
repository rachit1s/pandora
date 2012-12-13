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
 * ReportJob.java
 *
 * $Header:
 */
package transbit.tbits.scheduler;

//~--- non-JDK imports --------------------------------------------------------

import static transbit.tbits.Helper.TBitsConstants.PKG_SCHEDULER;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.common.Mail;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.scheduler.ui.JobUtil;
import transbit.tbits.scheduler.ui.ParameterType;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author  Vaibhav.
 * @version $Id: $
 */
public class BirtReportMailer implements ITBitsJob {

    // Application logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_SCHEDULER);

    // Name of the html interface that renders the search results.
    public static final String CMD_PARAM_SUBJECT    = "subject";
    public static final String CMD_PARAM_RECIPIENTS = "recipients";
    public static final String CMD_PARAM_URL      	= "reportURL";
    public static final String CMD_PARAM_FROM       = "fromAddress";
    public static final String CMD_PARAM_BODY       = "body";
    public static final String CMD_PARAM_FILENAME   = "fileName";
    public static final String CMD_DISPLAY_NAME = "Report Mailer Using URL";

    //~--- methods ------------------------------------------------------------

    /**
     * The method that gets executed when the job is triggered. 
     *
     * @param arg0  JobExecutionContext that holds the JobDetail and Trigger
     *              information.
     *
     * @exception   JobExecutionException
     */
    public void execute(JobExecutionContext arg0) throws JobExecutionException {

        // Get the JobDetail object.
        JobDetail jd = arg0.getJobDetail();
      
        System.out.println(jd);
        // Read the properties of the Job from the JobDetail object.
        JobDataMap jdm      = jd.getJobDataMap();

		System.out.println("Starting wget fetch...");
		
		
		
		try
		{
        	wgetAndSendReport(jdm);
		}
		catch(JobExecutionException jee)
		{
			jee.printStackTrace();
			
		
			
			System.out.println("unable to execute a job");
		}
		System.out.println("Finished wget fetch...");

    }

    public void wgetAndSendReport(JobDataMap jdm) throws JobExecutionException {

    	
        String recipients = jdm.getString(CMD_PARAM_RECIPIENTS);

        if ((recipients == null) || recipients.trim().equals("")) {
            String message = "Recipients list is empty.";
            throw new JobExecutionException(message);
        }

        recipients = recipients.replace(';', ',');
        String reportURL = jdm.getString(CMD_PARAM_URL);
        String fromAddress = jdm.getString(CMD_PARAM_FROM);
        String subject = jdm.getString(CMD_PARAM_SUBJECT);
        String body = jdm.getString(CMD_PARAM_BODY);
        String fileName = jdm.getString(CMD_PARAM_FILENAME);
        subject = (subject == null)
        ? ""
        : subject.trim();

        Timestamp ts = new Timestamp();
		if (subject.indexOf("$date") >= 0) {
			subject = subject.replace("$date", ts.toCustomFormat("yyyy-MM-dd"));
		}
		try {
			System.out.println("Sending content");
			sendContent(fromAddress, recipients, subject, body, fileName, reportURL);
			System.out.println("Finished sending content");
		} catch (AddressException e) {
			    throw new JobExecutionException("Invalid URL: " + reportURL.toString() + "." + e.getMessage());
		} catch (MalformedURLException e) {
			throw new JobExecutionException("Invalid URL: " + reportURL.toString() + "." + e.getMessage());
		} catch (MessagingException e) {
			throw new JobExecutionException("Unable to send message." 
					+ e.getMessage() 
					+ "." 
					+ e.getStackTrace());
		}		
    }

//    String getReportContent(String aUrl) throws IOException
//    {
//    	StringBuilder sb = new StringBuilder();
//    	URL url = new URL(aUrl);
//        URLConnection urlc = url.openConnection();
//        BufferedReader in = new BufferedReader(
//                                new InputStreamReader(
//                                		urlc.getInputStream()));
//        String inputLine;
//
//        while ((inputLine = in.readLine()) != null) 
//            sb.append(inputLine);
//        in.close();
//        
//    	return sb.toString();
//    }
    
    private void sendContent(String from, String to, String subject, String body, String fileName, String url4Attachment) throws AddressException, MessagingException, MalformedURLException
    {
    	//MOST of the code is taken from: http://www.paolocorti.net/public/wordpress/index.php/2007/06/05/deployment-of-birt-reports-by-email/
    	MimeMessage message = new MimeMessage(Mail.getSession());
	    message.setFrom(new InternetAddress(from));
	 
	    String toEmails[] = to.split(",");
	    for(int i=0; i<toEmails.length; i++)
	    {
	    	message.addRecipient(Message.RecipientType.TO,
	                         new InternetAddress(toEmails[i]));
	    }

	    message.setSubject(subject);
	 
	    Multipart multipart = new MimeMultipart();
	    BodyPart messageBodyPart = new MimeBodyPart();
	    messageBodyPart.setText(body);
	    multipart.addBodyPart(messageBodyPart);
	 
	    messageBodyPart = new MimeBodyPart(); 
	    URL url = new URL(url4Attachment); //("http://localhost//test.doc");
		DataSource source = new URLDataSource(url);
	    messageBodyPart.setDataHandler(new DataHandler(source));
	    messageBodyPart.setFileName(fileName);
	 
	    // Add the second part (attached mimebody)
	    multipart.addBodyPart(messageBodyPart);
	 
	    // Put parts in message
	    message.setContent(multipart);
	 
	    // Send message
	    Transport.send(message);
	    // rawat
	    System.out.println("Message has been sent");
    }
    
    public Hashtable<String, JobParameter> getParameters() throws SQLException{
		Hashtable<String, JobParameter> params = new Hashtable<String, JobParameter>();
		JobParameter param;
		
		param = new JobParameter();
		param.setName(CMD_PARAM_FROM);
		param.setType(ParameterType.Text);
		param.setMandatory(true);
		params.put(CMD_PARAM_FROM, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_RECIPIENTS);
		param.setMandatory(true);
		param.setType(ParameterType.Text);
		params.put(CMD_PARAM_RECIPIENTS, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_SUBJECT);
		param.setType(ParameterType.Text);
		params.put(CMD_PARAM_SUBJECT, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_BODY);
		param.setType(ParameterType.TextArea);
		params.put(CMD_PARAM_BODY, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_URL);
		param.setMandatory(true);
		param.setType(ParameterType.Text);
		params.put(CMD_PARAM_URL, param);
		
		param = new JobParameter();
		param.setName(CMD_PARAM_FILENAME);
		param.setType(ParameterType.Text);
		params.put(CMD_PARAM_FILENAME, param);
		
		return params;
	}
    
    public String getDisplayName(){
    	return CMD_DISPLAY_NAME;
    }
    
    public boolean validateParams(Hashtable<String,String> params) 
    	throws IllegalArgumentException{    	
    	
    	if( null == params.get(CMD_PARAM_RECIPIENTS) || "".equals(params.get(CMD_PARAM_RECIPIENTS).trim()) ){
    		throw new IllegalArgumentException("Illegal Argument for " + CMD_PARAM_RECIPIENTS + " field.");
    	}
    	
    	if( null == params.get(CMD_PARAM_FROM) || "".equals(params.get(CMD_PARAM_FROM).trim()) )
    		throw new IllegalArgumentException("Illegal Argument for " + CMD_PARAM_FROM + " field.") ;
    	
    	if( null == params.get(CMD_PARAM_URL) || "".equals(params.get(CMD_PARAM_URL).trim()) )
    		throw new IllegalArgumentException( "Illegal Argument for " + CMD_PARAM_URL + " field.") ;
    	
    	return true;
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 6) {
            StringBuilder usage = new StringBuilder();

            usage.append("Usage:\n\t")
            .append("ReportJob <FromAddress> <Subject> <URL> <Recipients> <BODY> <FILENAME>")
            .append("\n\nWhere\n\t")
            .append("FromAddress - From Address on the envelope of ").append("mail\n\t")
            .append("Subject     - Subject of the mail\n\t").append("              (can contain $date to print date ").append("in yyyy-MM-dd format\n\t")
            .append("Recipients  - Recipients of the mail\n\n")
            .append("FileName  - Just the name of attachment to given to the downloaded url\n\n");
            
            System.err.println(usage.toString());
            return;
        }

        /*
         * Form the data map out of the command line arguments and generate
         * the report.
         */
        JobDataMap jdm = new JobDataMap();

        jdm.put(CMD_PARAM_FROM, args[0]);
        jdm.put(CMD_PARAM_SUBJECT, args[1]);
        jdm.put(CMD_PARAM_URL, args[2]);
        jdm.put(CMD_PARAM_RECIPIENTS, args[3]);
        jdm.put(CMD_PARAM_BODY, args[4]);
        jdm.put(CMD_PARAM_FILENAME, args[5]);
        try {
            BirtReportMailer rj = new BirtReportMailer();

            rj.wgetAndSendReport(jdm);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            //Mapper.stop();
            //DataSourcePool.shutdownPooling();
        }

        return;
    }
}
