package commons.com.tbitsGlobal.utils.client.Events;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * 
 * @author sutta
 * 
 * Base Class for all the Events
 */
public abstract class TbitsBaseEvent {
	protected String message;
	protected String error;
	
	protected boolean displayMessage;
	protected boolean displayError;
	
	protected boolean isCancelled = false;
	protected boolean hideMessageAfterFire = false;
	protected boolean hideErrorAfterFire = false;
	
	private TbitsInfo messageTip;
	private TbitsInfo errorTip;
	
	public TbitsBaseEvent() {
		super();
		
		this.displayMessage = false;
		this.displayError = false;
	}

	public TbitsBaseEvent(String message, String error) {
		this();
		this.message = message;
		this.error = error;
		
		this.displayMessage = true;
		this.displayError = true;
	}

	/**
	 * @return The message to be diplayed when event is fired
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * @param message The message to be diplayed when event is fired
	 */
	public void setMessage(String message) {
		this.message = message;
		this.displayMessage = true;
	}

	/**
	 * @return The error message to be diplayed when event throws an exception
	 */
	public String getError() {
		return error;
	}

	/**
	 * 
	 * @param error The error message to be diplayed when event throws an exception
	 */
	public void setError(String error) {
		this.error = error;
		this.displayError = true;
	}

	
	public boolean isDisplayMessage() {
		return displayMessage;
	}

	public void setDisplayMessage(boolean displayMessage) {
		this.displayMessage = displayMessage;
	}

	public boolean isDisplayError() {
		return displayError;
	}

	public void setDisplayError(boolean displayError) {
		this.displayError = displayError;
	}

	/**
	 * Displays the message
	 */
	public void displayMessage(){
		if(message != null && !message.trim().equals("")){
			messageTip = TbitsInfo.info(message);
			Log.info(message);
		}
	}
	
	/**
	 * Displays the error message
	 */
	public void displayError(){
		if(error != null && !error.trim().equals("")){
			errorTip = TbitsInfo.error(error);
			Log.error(message);
		}
	}
	
	/**
	 * @return Called before the event is fired. Event is stopped if it returns false
	 */
	public boolean beforeFire(){
		return true;
	}
	
	/**
	 * Called after the event is fired
	 */
	public void afterFire(){
		if(hideMessageAfterFire && messageTip != null && messageTip.isVisible())
			messageTip.hide();
		
		if(hideErrorAfterFire && errorTip != null && errorTip.isVisible())
			errorTip.hide();
	}

	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	public void setHideMessageAfterFire(boolean hideMessageAfterFire) {
		this.hideMessageAfterFire = hideMessageAfterFire;
	}

	public boolean isHideMessageAfterFire() {
		return hideMessageAfterFire;
	}

	public void setHideErrorAfterFire(boolean hideErrorAfterFire) {
		this.hideErrorAfterFire = hideErrorAfterFire;
	}

	public boolean isHideErrorAfterFire() {
		return hideErrorAfterFire;
	}

	public TbitsInfo getMessageTip() {
		return messageTip;
	}

	public TbitsInfo getErrorTip() {
		return errorTip;
	}
}
