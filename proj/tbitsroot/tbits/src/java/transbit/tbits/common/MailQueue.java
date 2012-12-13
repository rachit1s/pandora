package transbit.tbits.common;


import java.util.concurrent.LinkedBlockingQueue;

import transbit.tbits.domain.Request;

public class MailQueue extends LinkedBlockingQueue<Request> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private static MailQueue instance = null;
	private MailQueue()
	{
		
	}
	public synchronized static MailQueue getInstance()
	{
		if(instance == null)
			instance = new MailQueue();
		return instance;
	}
}
