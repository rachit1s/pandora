/**
 * 
 */
package transbit.tbits.events;

import java.util.Hashtable;

import javax.mail.Message;

import transbit.tbits.mail.LogMailRequest;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 * This event will be fired just before submitting the request to the Add/Update Request API.
 */
public class BeforeEmailSubmitEvent implements IEvent {
	/**
	 * the hashtable params that will be given to the API for request submission.
	 * This needs to be changed in case if request is to be changed.
	 */
	Hashtable<String, String> requestHash;
	
	/**
	 * The javax Mail message object created from the received email.
	 * You can get information about the email from this object.
	 */
	Message message;
	
	/**
	 * internal object that has created the requestHash from message.
	 */
	LogMailRequest logMailRequest;

	/**
	 * @return the requestHash
	 */
	public Hashtable<String, String> getRequestHash() {
		return requestHash;
	}

	/**
	 * @return the message
	 */
	public Message getMessage() {
		return message;
	}

	/**
	 * @return the logMailRequest
	 */
	public LogMailRequest getLogMailRequest() {
		return logMailRequest;
	}

	/**
	 * @param requestHash
	 * @param aMessage
	 * @param logMailRequest
	 */
	public BeforeEmailSubmitEvent(Hashtable<String, String> requestHash,
			Message message, LogMailRequest logMailRequest) 
	{
		this.requestHash = requestHash;
		this.message = message;
		this.logMailRequest = logMailRequest;
	}
}
