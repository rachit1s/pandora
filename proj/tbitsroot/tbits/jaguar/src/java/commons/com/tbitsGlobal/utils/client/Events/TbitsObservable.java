package commons.com.tbitsGlobal.utils.client.Events;

import java.util.List;
/**
 * A class which wants to be observed needs to implement this interface.
 * @author sandeepgiri
 *
 */
public interface TbitsObservable {
	
	/**
	 * Adds a listener to an event.
	 * @param <T>
	 * @param eventType
	 * @param handle
	 */
	public <T extends TbitsBaseEvent> void subscribe(Class<T> eventType, ITbitsEventHandle<? extends TbitsBaseEvent> handle);
	
	/**
	 * Removes a listener from an event
	 * @param <T>
	 * @param eventType
	 * @param handle
	 * @return
	 */
	public <T extends TbitsBaseEvent> boolean unSubscribe(Class<T> eventType, ITbitsEventHandle<? extends TbitsBaseEvent> handle);
	
	/**
	 * Fires an event
	 * @param <T>
	 * @param event
	 * @return
	 */
	public <T extends TbitsBaseEvent> boolean fireEvent(T event);
	
	/**
	 * Get listeners for a particular event type
	 * @param <T>
	 * @param eventType
	 * @return
	 */
	public <T extends TbitsBaseEvent> List<ITbitsEventHandle<TbitsBaseEvent>> getHandles(Class<T> eventType);
	
	/**
	 * @return True is has any listeners;
	 */
	public boolean hasHandles();
	
	/**
	 * @param <T>
	 * @param eventType
	 * @return True if has any listeners for a given eventType
	 */
	public <T extends TbitsBaseEvent> boolean hasHandles(Class<T> eventType);
	
	/**
	 * Remove all listeners
	 */
	public void unSubscribeAllHandles();
	
	/**
	 * Used when the instance has to be registered in the central event bus or some other similar bus.
	 * This method has to be explicitly called to enable it to listen global events.
	 */
	public boolean attach();
	
	/**
	 * Used when the instance has to be de-registered from the central event bus or some other similar bus
	 * 
	 * <b>If {@link #attach()} has been called then this method should be explicitly called to remove its instance for global registry. 
	 * Otherwise it may lead to memory leaks</b>
	 */
	public boolean detach();
}
