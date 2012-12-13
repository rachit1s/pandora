/**
 * 
 */
package transbit.tbits.events;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 * If any event throws this exception then the event execution will stop just after
 * this and the exception will be propogated.
 * Any exception other than this will be catched and ignored. So if some author wants
 * the execution of current thread of tbits to stop then he MUST throw this exception.
 */
public class EventFailureException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public EventFailureException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public EventFailureException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public EventFailureException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public EventFailureException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
}
