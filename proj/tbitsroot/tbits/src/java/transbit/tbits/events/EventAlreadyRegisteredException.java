/**
 * 
 */
package transbit.tbits.events;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 * This event is an indication that either the {@link EventRegistry} was already registered with the {@link EventManager}
 * or the eventClass and eventHandler combination was already registered. So this exception should be handled separately
 * as this indicates that something seriously wrong is going on in the system.
 */
public class EventAlreadyRegisteredException extends EventException 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String eventClassName;
	String handlerClassName;
	
	/**
	 * @return the eventClassName
	 */
	public String getEventClassName() {
		return eventClassName;
	}
	/**
	 * @return the handlerClassName
	 */
	public String getHandlerClassName() {
		return handlerClassName;
	}
	/**
	 * @param message
	 * @param cause
	 */
	public EventAlreadyRegisteredException(String eventClassName, String handlerClassName,String message, Throwable cause) {
		super(message + ". Provided EventClassName : " + eventClassName + " and EventHandlerClassName : " + handlerClassName , cause);
		this.eventClassName = eventClassName;
		this.handlerClassName = handlerClassName;
	}
	/**
	 * @param message
	 */
	public EventAlreadyRegisteredException(String eventClassName, String handlerClassName,String message) {
		super(message + ". Provided EventClassName : " + eventClassName + " and EventHandlerClassName : " + handlerClassName );
		this.eventClassName = eventClassName;
		this.handlerClassName = handlerClassName;
	}
}
