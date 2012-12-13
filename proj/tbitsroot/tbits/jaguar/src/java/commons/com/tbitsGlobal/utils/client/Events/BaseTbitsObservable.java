package commons.com.tbitsGlobal.utils.client.Events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * 
 * @author sandeepgiri
 * 
 * Base implementation of {@link TbitsObservable}
 */
public class BaseTbitsObservable implements TbitsObservable{
	protected HashMap<Class<? extends TbitsBaseEvent>, List<ITbitsEventHandle<TbitsBaseEvent>>> register;
	
	public BaseTbitsObservable() {
		register = new HashMap<Class<? extends TbitsBaseEvent>, List<ITbitsEventHandle<TbitsBaseEvent>>>();
	}
	
	public <T extends TbitsBaseEvent> boolean fireEvent(final T event){
		synchronized (register) {
			if(hasHandles(event.getClass())){
				List<ITbitsEventHandle<TbitsBaseEvent>> handles = getHandles(event.getClass());
				for(ITbitsEventHandle<TbitsBaseEvent>  handle : handles){
					try{
						handle.handleEvent(event);
					}catch(Exception e){
						Log.error(e.getMessage() != null ? e.getMessage() : "", e);
						e.printStackTrace();
						if(event.isDisplayError())
							event.displayError();
					}catch(Throwable t){
						Log.error(t.getMessage() != null ? t.getMessage() : "", t);
						t.printStackTrace();
						if(event.isDisplayError())
							event.displayError();
					}
				}
			}
		}
		return !event.isCancelled();
	}
	
	/**
	 * Subscribes a {@link ITbitsEventHandle} for a specific event type
	 */
	public <T extends TbitsBaseEvent> void subscribe(final Class<T> eventType, final ITbitsEventHandle<? extends TbitsBaseEvent> handle){
		synchronized (register) {
			if(!register.containsKey(eventType))
				register.put(eventType, new ArrayList<ITbitsEventHandle<TbitsBaseEvent>>());	
			register.get(eventType).add((ITbitsEventHandle<TbitsBaseEvent>) handle);

		}
	}
	
	/**
	 * Unsubscribes the handle
	 */
	public <T extends TbitsBaseEvent> boolean unSubscribe(final Class<T> eventType, final ITbitsEventHandle<? extends TbitsBaseEvent> handle){
		synchronized (register) {
			if(register.get(eventType).contains(handle)){
				register.get(eventType).remove(handle);
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Get all the handles for a specific type of event
	 */
	public <T extends TbitsBaseEvent> List<ITbitsEventHandle<TbitsBaseEvent>> getHandles(Class<T> eventType) {
		List<ITbitsEventHandle<TbitsBaseEvent>> handles = register.get(eventType);
		
		List<ITbitsEventHandle<TbitsBaseEvent>> newHandles = new ArrayList<ITbitsEventHandle<TbitsBaseEvent>>();
		
		if(handles != null)
			newHandles.addAll(handles);
		
		return newHandles;
	}

	public boolean hasHandles() {
		return register.size() > 0;
	}

	public <T extends TbitsBaseEvent> boolean hasHandles(Class<T> eventType) {
		return getHandles(eventType) != null && getHandles(eventType).size() > 0;
	}

	public void unSubscribeAllHandles() {
		register.clear();
	}

	
	public boolean attach() {
		return TbitsEventRegister.getInstance().attach(this);
	}

	
	public boolean detach() {
		return TbitsEventRegister.getInstance().detach(this);
	}
}
