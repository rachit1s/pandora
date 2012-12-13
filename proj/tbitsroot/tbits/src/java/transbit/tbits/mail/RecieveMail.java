
/*
 * RecieveMail.java
 *
 * Created on June 22, 2006, 12:49 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package transbit.tbits.mail;

//~--- non-JDK imports --------------------------------------------------------

import static transbit.tbits.Helper.TBitsConstants.PKG_MAIL;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.APIUtil;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.MailProcessor;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Utilities;
import transbit.tbits.common.PropertiesHandler.MailProperties;
import transbit.tbits.domain.BAMailAccount;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Type;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;

//~--- classes ----------------------------------------------------------------


/**
 *
 * @author Vaibhav Sharma
 * This class receives all mail form the Logger and thus
 */
public class RecieveMail implements ITBitsJob{

    private static final String EMAIL_HDR_AUTO_SUBMITTED = "Auto-Submitted";
	/**
     * Creates a new instance of RecieveMail
     *
     */
    public static TBitsLogger LOG = TBitsLogger.getLogger(PKG_MAIL);
    private  Message curMessage; 
    private boolean shutdownRequested = false;

    //~--- constructors -------------------------------------------------------

    public RecieveMail() {}
    private static boolean isRunning = false;
    //~--- methods ------------------------------------------------------------
	
    /*
     * Takes care of locking and calls RecieveAllMailAsync
     */
    public int RecieveAllMail() {
	if(!isRunning)
	{
		int retValue = 0;
		try
		{
			isRunning = true;
			LOG.info("Starting to process mails");
			retValue = RecieveAllMailASync();	
		}
		catch(Exception e)
		{
			LOG.error(e.getMessage());
		}
		finally
		{
			isRunning = false;
			LOG.info("Finished processing mails");
		}	
		return retValue;
	}
	else 
	{
		LOG.warn("Skipping recieve mail as the process is already running");
		return 1;
	}
    }
    /*
     * To recive all mails for all specified business areas
     * 1)retrive details of business area mail accounts
     * 2)for each business area read all mails from mail box
     * 3)for each mail use class LogMailRequest to log message
     * 4)mark messages as read in INBOX
     */
    protected int RecieveAllMailASync() {
		Vector<BAMailAccount> mailAccounts = null;
		 Runtime r = Runtime.getRuntime();
		try {
			mailAccounts = BAMailAccount.lookupAll();
			LOG.debug("BAMailAccount.lookupAll executed");
		} catch (DatabaseException e) {
			LOG.severe("",(e));
			return 1;
		}
		LOG.debug("number of mail Accounts found in DataBase = " + mailAccounts.size());

		boolean memoryOverflow = false;
		for(BAMailAccount bac:mailAccounts)
		{
			if(!bac.isActive())
				continue;
			if(shutdownRequested)
				break;
			if(memoryOverflow)
				break;
//			r = Runtime.getRuntime();
//		    r.gc();
//		    
			
			LOG.debug("Entering in  Iteration  ba accout: [" + bac + "]");
			LOG.info("Checking mails for ba: [" + bac.getMyBAPrefix()+"]");

			String host = bac.getMyMailServer();
			String protocol = bac.getMyProtocol().toLowerCase();
			
			if(!BAMailAccount.isAllowedProtocol(protocol))
			{
				IllegalArgumentException iae = new IllegalArgumentException("The mail protocol '" + protocol + "' is not supported.");
				LOG.error(iae);
				continue;
			}
			// Get system properties
			Properties props = PropertiesHandler.getAppAndSysProperties();
			
			props.setProperty("mail." + protocol + ".host", host);
			props.setProperty("mail." + protocol + ".port",Integer.toString(bac.getPort()));
			
			MailAuthenticator passwordAuthentication = new MailAuthenticator(
					bac.getMyEmailID(), bac.getMyPassward());
			Session session = Session
					.getInstance(props, passwordAuthentication);
			Folder folder = null;
			Store store = null;
			try 
			{
				store = session.getStore(protocol);
				store.connect();
				// Get folder
				if(shutdownRequested)
					break;
				
				folder = store.getFolder("INBOX");
				folder.open(Folder.READ_WRITE);
				Message[] messages = folder.getMessages();
				
				if(shutdownRequested)
					break;
				
				for (int j = 0, n = messages.length; j < n; j++) 
				{
					if(memoryOverflow) break;
					
					if(shutdownRequested)
						break;
					
					LOG.debug("Entering loop For" + bac.getMyEmailID()
								+ " checking  Message" + j + " of " + n);
					curMessage = messages[j];
					if (!curMessage.getFlags().contains(Flags.Flag.SEEN)) {
							
						try 
						{		
							LOG.info("Reading Message New Message" + j + " of "
									+ bac.getMyEmailID());
							BusinessArea ba = BusinessArea.lookupBySystemPrefix(bac.getMyBAPrefix());
							String catName = "";
							int catId = bac.getCategoryId();
							try
							{
								if(catId != 0)
								{
									Type category = Type.lookupBySystemIdAndFieldNameAndTypeId(ba.getSystemId(), "category_id", bac.getCategoryId());
									catName = category.getName();
								}
								else
									LOG.info("Skipping the category Id: " + catId);
							}
							catch(Exception exp)
							{
								LOG.error(exp);
							}
							boolean shouldSkip = false;
							//TODO: Should be extended using  
							if( isTBitsGenerated( curMessage ))
							{
								shouldSkip = true;
							}
							else if( !isAutoReplyAllowed(curMessage) && isAutoGenerated(curMessage))
							{
								shouldSkip = true;
							}
							
							if(shouldSkip)
							{
								curMessage.setFlag(Flags.Flag.SEEN, true);
								continue;
							}
							
							if(shutdownRequested)
								break;
							int retValue = LogMailRequest.logMessage(bac.getMyBAPrefix(), curMessage, catName);
							
							LOG.info("Deleting message");
							try
							{	
								curMessage.setFlag(Flags.Flag.DELETED, true);
							}
							catch (MessagingException e)
							{
//								r = Runtime.getRuntime();
//								r.gc();
								LOG.error("Unable to mark email as deleted.", e);
							}
						}
						catch (OutOfMemoryError t)
						{
							t.printStackTrace();
						    r = Runtime.getRuntime();
						    r.gc();
						    memoryOverflow = true;						
						}
						catch (Exception e) {
							String msg = "Failed while fetching and logging mails for ba '" + bac.getMyBAPrefix() 
								+ "' and user '" + bac.getMyEmailID() + "'";
							LOG.severe(msg, e);
							memoryOverflow = true;
						}
					  }//end  if
						
				}//end for on messages
				
			} catch (Exception e) {
				LOG.severe("",(e));
			}
			catch (OutOfMemoryError t)
			{
				 r = Runtime.getRuntime();
				 r.gc();
				memoryOverflow = true;
			}
			finally
			{
				LOG.debug("Closing INBOX for " + bac.getMyEmailID());
				try
				{
					if (folder != null)
						folder.close(true);
					if (store != null)
						store.close();
				}
				catch (Exception e)
				{
					LOG.error("Unable to close the inbox.", e);
//					r = Runtime.getRuntime();
//					r.gc();
				}
			}
			
		} //end of for on mail boxes
		LOG.info("Finished All mail retrivals ");
		return 0;
	}
    
    public static boolean isAutoReplyAllowed( Message aMsg)
    {
    	Address[] sender = null;
		try {
			sender = aMsg.getFrom();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		ArrayList<String> fromList = MailProcessor.getAddressList(sender);
		String from = null;
		if( null != fromList )
			from = fromList.get(0);
    			
		return isAutoReplyAllowed(from);
    }
    
    public static boolean isAutoReplyAllowed( String from )
    {
    	String emailsList1 = PropertiesHandler.getProperty(MailProperties.AllowAutoReplyFrom_WithNotification);
		String emailsList2 = PropertiesHandler.getProperty(MailProperties.AllowAutoReplyFrom_WithoutNotification);

		ArrayList<String> emails = Utilities.toArrayList(emailsList1);
		if( null == emails )
			emails = new ArrayList<String>();
		
		ArrayList<String> emails2 = Utilities.toArrayList(emailsList2);
		if( null != emails2 )
			emails.addAll(emails2);
		
		return emails.contains(from);
			// this is included in the exclusion list. So the email will be received
			// even when it is auto-reply
	}
    
	private boolean isAutoGenerated(Message aMsg)
			throws MessagingException {
		boolean isAutoGeneratedEmail = false;
		
		String[] autoReplyHs = aMsg.getHeader(TBitsConstants.X_AUTOREPLY);
		if((autoReplyHs != null) && (autoReplyHs.length > 0))
		{
			String autoReplyH = autoReplyHs[0];
			if((autoReplyH != null) && (autoReplyH.equalsIgnoreCase("yes")))
			{
				LOG.info("tBits has detected that it is an auto reply mail with '"
						+ TBitsConstants.X_AUTOREPLY + "' header so skipping it.");
				isAutoGeneratedEmail = true;
			}
		}
		// If Auto-Submitted is present and not 'no'
		// Using specs: http://www.iana.org/assignments/auto-submitted-keywords/auto-submitted-keywords.xhtml
		String[] autoSubmitted = aMsg.getHeader(EMAIL_HDR_AUTO_SUBMITTED);
		if (autoSubmitted != null) {
		    for (int i = 0; i < autoSubmitted.length; i++) 
		    {
		        if (!autoSubmitted[i].equalsIgnoreCase("no")) 
		        {
		        	LOG.info("tBits has detected that it is an auto reply mail with '"
							+ EMAIL_HDR_AUTO_SUBMITTED + "' header so skipping it.");
					isAutoGeneratedEmail = true;
		        }
		    }
		}
		return isAutoGeneratedEmail;
	}
	
	public boolean isTBitsGenerated(Message aMsg)
	{
		String[] tBitsCatsHs = null;
		try {
			tBitsCatsHs = aMsg.getHeader(TBitsConstants.X_TBITS_CATEGORY);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		if((tBitsCatsHs != null) && (tBitsCatsHs.length > 0))
		{
			LOG.info("tBits has detected that the mail is created by tBits. So skipping it.");
			return true;
		}
		
		return false;
	}
	
    public void RequestShutDown()
    {
    	shutdownRequested = true;
    }

    public static void  main(String args[]) {
       RecieveMail rm = new RecieveMail();
       rm.RecieveAllMail();
    }
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		  //RecieveMail rm = new RecieveMail();
	      RecieveAllMail();
	      try {
			arg0.getScheduler().addSchedulerListener(new SchedulerListener(){

				public void jobScheduled(Trigger arg0) {
					// TODO Auto-generated method stub
					
				}

				public void jobUnscheduled(String arg0, String arg1) {
					// TODO Auto-generated method stub
					
				}

				public void jobsPaused(String arg0, String arg1) {
					// TODO Auto-generated method stub
					
				}

				public void jobsResumed(String arg0, String arg1) {
					// TODO Auto-generated method stub
					
				}

				public void schedulerError(String arg0, SchedulerException arg1) {
					// TODO Auto-generated method stub
					
				}

				public void schedulerShutdown() {
					// TODO Auto-generated method stub
					System.out.println("SchedulerShutdown is requesting RecieveMail's Shutdown.");
			    	
					RequestShutDown();
				}

				public void triggerFinalized(Trigger arg0) {
					// TODO Auto-generated method stub
					
				}

				public void triggersPaused(String arg0, String arg1) {
					// TODO Auto-generated method stub
					
				}

				public void triggersResumed(String arg0, String arg1) {
					// TODO Auto-generated method stub
					
				}

				public void jobAdded(JobDetail arg0) {
					// TODO Auto-generated method stub
					
				}

				public void jobDeleted(String arg0, String arg1) {
					// TODO Auto-generated method stub
					
				}

				public void schedulerInStandbyMode() {
					// TODO Auto-generated method stub
					
				}

				public void schedulerShuttingdown() {
					// TODO Auto-generated method stub
					
				}

				public void schedulerStarted() {
					// TODO Auto-generated method stub
					
				}
				  
			  });
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void destroy()
	{
		RequestShutDown();
	}
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "Fetch Incoming Mails";
	}
	public Hashtable<String, JobParameter> getParameters() throws SQLException {
		// TODO Auto-generated method stub
		return new Hashtable<String, JobParameter>() ;
	}
	public boolean validateParams(Hashtable<String, String> params)
			throws IllegalArgumentException {
		return true;
	}
}
