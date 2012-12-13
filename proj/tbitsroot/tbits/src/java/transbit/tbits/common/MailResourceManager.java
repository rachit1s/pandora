package transbit.tbits.common;
import java.util.ArrayList;

import transbit.tbits.domain.Request;

/**
 * Manages the mail resource session
 */
public class MailResourceManager {
	
	private boolean autoCommit = false;
	private ArrayList<Request> waitList = new ArrayList<Request>();
	final MailQueue mq = MailQueue.getInstance();
	final MailTransportEngine mte = MailTransportEngine.getInstance();
	
	public void queueForDelivery(Request request)
	{
		if(autoCommit)
		{
			deliver(request);
		}
		else
		{
			synchronized(waitList)
			{
				waitList.add(request);
			}
		}
	}
	
	private void deliver(Request request)
	{
		try {
			mq.put(request);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Inform the Transport Manager to send out the emails.
	 */
	public void commit()
	{
		synchronized(waitList)
		{
			for(Request r:waitList)
			{
				deliver(r);
			}
			waitList.clear();
			
		}
	}
	
	/*
	 * Inform the transport manager to remove the email from queue.
	 */
	public void rollback()
	{
		waitList = new ArrayList<Request>();
	}

	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public boolean isAutoCommit() {
		return autoCommit;
	}
}
