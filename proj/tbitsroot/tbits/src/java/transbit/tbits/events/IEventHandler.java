/**
 * 
 */
package transbit.tbits.events;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 * All the EventHandlers should implement this interface
 */
public interface IEventHandler<T extends IEvent> 
{
	/**
	 * This will return the initial order for this event execution. 
	 * The order of EventRegistry, if any, for this class will take precedence over this.
	 * When setting event-registry's order from this, the double will be down-casted to int.
	 * @return
	 */
	public double getInitialOrder();
	/**
	 * should return a human understandable description of the event handler
	 * @return
	 */
	public String getDescription();
	
	/**
	 * Implement how do you want to handle this event. This method will be called synchronously and in sequence
	 * mentioned somewhere else. If your task is long and can be done asynchronously, think about using Threads.
	 * @param event : the event that was fired.
	 */
	public void handle(T event) throws EventFailureException;
}
