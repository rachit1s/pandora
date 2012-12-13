package commons.com.tbitsGlobal.utils.client.Events;

/**
 * 
 * @author sutta
 *
 * @param <T> Event Type
 * 
 * Handles an event
 */
public interface ITbitsEventHandle<T extends TbitsBaseEvent> {
	public void handleEvent(T event);
}
