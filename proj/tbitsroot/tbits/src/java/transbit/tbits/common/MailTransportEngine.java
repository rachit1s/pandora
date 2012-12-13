package transbit.tbits.common;

import transbit.tbits.domain.Request;
import transbit.tbits.mail.TBitsMailer;

public class MailTransportEngine extends Thread{
	private static MailTransportEngine instance = null;
	final MailQueue mq = MailQueue.getInstance();
	
	public synchronized static MailTransportEngine getInstance()
	{
		if(instance == null)
		{
			instance = new MailTransportEngine();
			instance.start();
			System.out.println("MailTransportEngine: Instance created and started.");
		}
		return instance;
	}
	
	private MailTransportEngine()
	{
		
	}
	
	private boolean shouldStop = false;
	public synchronized void run() {
		
		while (!shouldStop) {
			try {
				Request request = mq.take();
				TBitsMailer tm = new TBitsMailer(request);
				//HANDLE EXCEPTIONS. IN case there is an error delivering it should put back in the queue.
				tm.sendMail();
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		instance = null;
	}

	public void requestStop() {
		this.shouldStop = true;
	}

	public boolean isStopping() {
		return shouldStop;
	}

}
