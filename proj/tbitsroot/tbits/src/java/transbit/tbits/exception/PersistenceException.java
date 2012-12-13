/**
 * 
 */
package transbit.tbits.exception;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 * From now on this exception will be thrown when any persistence to database related exception occurs.
 * It does not mean that other exception cannot be thrown like SQLException and similar exception will still be thrown
 */

public class PersistenceException extends Exception
{
	/**
	 * 
	 */
	public PersistenceException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PersistenceException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public PersistenceException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public PersistenceException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
