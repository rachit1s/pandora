/**
 * 
 */
package transbit.tbits.addons;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class Result 
{
	Exception exception ;
	String message ;
	
	public Result()
	{
		this.message  = "Successful";
	}
	
	public Result(String message)
	{
		this.message = message;
	}
	/** The exception object if not null represents the exception that occured even after the proper execution.
	 * @return the e
	 */
	public Exception getException() {
		return exception;
	}
	/**
	 * @param e the e to set
	 */
	public void setException(Exception e) {
		this.exception = e;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @param e
	 * @param message
	 */
	public Result(String message,Exception e) {
		super();
		this.exception = e;
		this.message = message;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Result [exception=" + (null == exception ? "null" : exception.getMessage()) + ", message=" + message + "]";
	}
	
}
