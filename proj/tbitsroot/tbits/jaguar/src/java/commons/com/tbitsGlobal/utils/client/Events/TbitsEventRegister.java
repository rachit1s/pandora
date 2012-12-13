package commons.com.tbitsGlobal.utils.client.Events;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author sutta
 * 
 * A central event bus for the application
 * It keeps a list of {@link TbitsObservable}s and delegates events to all of them
 */
public class TbitsEventRegister{
	private static TbitsEventRegister eventRegister;
	
	private List<TbitsObservable> observables;
	
	/**
	 * 
	 * @author sutta
	 *
	 * @param <T> Event Type
	 * 
	 * executed before the event is fired
	 */
	public interface IPreFireHandle<T extends TbitsBaseEvent>{
		public boolean beforeFire(T event);
	}
	
	/**
	 * 
	 * @author sutta
	 *
	 * @param <T> Event Type
	 * 
	 * executed after the event is fired
	 */
	public interface IPostFireHandle<T extends TbitsBaseEvent>{
		public void afterFire(T event);
	}
	
	/**
	 * 
	 * @author sutta
	 * 
	 * An object that carries {@link IPreFireHandle}s and {@link IPostFireHandle}s for events
	 * There must be a single context handle for an independent event bus.
	 */
	public interface IContextHandle{
		public <T extends TbitsBaseEvent> IPreFireHandle getPreFireHandle(Class<T> eventClazz);
		
		public <T extends TbitsBaseEvent> IPostFireHandle getPostFireHandle(Class<T> eventClazz);
	}
	
	private IContextHandle contextHandle;
	
	private TbitsEventRegister() {
		super();
		
		observables = new ArrayList<TbitsObservable>();
	}
	
	public static TbitsEventRegister getInstance(){
		if(eventRegister == null)
			eventRegister = new TbitsEventRegister();
		
		return eventRegister;
	}
	
	/**
	 * Fires an event on the global bus
	 * @param <T>
	 * @param event
	 * @return True if the event was fired successfully
	 */
	public <T extends TbitsBaseEvent> boolean fireEvent(T event){
		synchronized (observables) {
			if(contextHandle != null){ // Fire PreFireHandles
				IPreFireHandle<T> handle = contextHandle.getPreFireHandle(event.getClass());
				if(handle != null && !handle.beforeFire(event))
					return false;
			}
			if(event.beforeFire()){ // Fire the event
				List<TbitsObservable> observables = new ArrayList<TbitsObservable>();
				observables.addAll(this.observables);
				if(event.isDisplayMessage())
					event.displayMessage();
				for(TbitsObservable observable : observables){
					if(!observable.fireEvent(event))
						return false;
				}
				event.afterFire();
				
				if(contextHandle != null){ // Fire PostFireHandles
					IPostFireHandle<T> handle = contextHandle.getPostFireHandle(event.getClass());
					if(handle != null)
						handle.afterFire(event);
				}
			}else
				return false;
		}
		
		return true;
	}
	
	public boolean attach(final TbitsObservable observable){
		synchronized (observables) {
			if(!contains(observable))
				return observables.add(observable);
			return false;
		}
	}
	
	public boolean detach(final TbitsObservable observable){
		synchronized (observables) {
			return observables.remove(observable);
		}
	}
	
	private boolean contains(TbitsObservable observable){
		synchronized (observables) {
			return observables.contains(observable);
		}
	}

	public void setContextHandle(IContextHandle contextHandle) {
		this.contextHandle = contextHandle;
	}

	public IContextHandle getContextHandle() {
		return contextHandle;
	}
}
