/**
 * This class is the singleton Manager for all TBits core events handling.
 */
package transbit.tbits.events;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.exception.PersistenceException;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 * Handler should be able to register for an Event at any depth of the event Heirarchy and should get the notifications for the complete sub tree.
 * Handlers should be make enable disabled from the UI and saved in DB so that next time the system starts it loads the Handlers accordingly
 * Throughout the complete code of this class always take lock first on {@link EventManager#managedHandlers } and then on {@link EventManager#eventHandlers }
 * to avoid deadlocks. Also see inside the called methods for synchronized statement when taking locks.
 * 
 * @see : http://www.marco.panizza.name/dispenseTM/slides/exerc/eventNotifier/eventNotifier.html
 */


public class EventManager 
{
	class Handler<T extends IEvent>
	{
		IEventHandler<T> handlerObject = null;
		EventRegistry er = null;
		/**
		 * @param eventClass
		 * @param er
		 */
		public Handler(IEventHandler<T> handlerObject, EventRegistry er) {
			super();
			this.handlerObject = handlerObject;
			this.er = er;
		}

		/**
		 * @param eventClass
		 * @param er
		 */
		public Handler(IEventHandler<T> handlerObject) {
			super();
			this.handlerObject = handlerObject;
			this.er = null;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Handler other = (Handler) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (handlerObject == null) {
				if (other.handlerObject != null)
					return false;
			} else if (!handlerObject.getClass().getName().equals(other.handlerObject.getClass().getName()))
				return false;
			return true;
		}
		private EventManager getOuterType() {
			return EventManager.this;
		}

		/**
		 * @return the handlerClass
		 */
		public IEventHandler<? extends IEvent> getHandlerObject() {
			return handlerObject;
		}

		/**
		 * @return the er
		 */
		public EventRegistry getEventRegistry() {
			return er;
		}

		/**
		 * @return the er
		 */
		public void setEventRegistry(EventRegistry eventRegistry) 
		{
			this.er = eventRegistry;
		}
		
		public double getOrder(){
			return ( null != er ? er.getEventOrder() : handlerObject.getInitialOrder() );
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Handler [handlerObject=" + handlerObject + ", er=" + er + "]";
		}
	}

	public static final Logger LOG = Logger.getLogger("com.tbitsglobal.event");
	
	private static EventManager instance = null;
	private Hashtable<EventRegistry,ClassLoader> managedHandlers = new Hashtable<EventRegistry, ClassLoader>();
	private Hashtable<Class<? extends IEvent>, List<Handler>> eventHandlers = new Hashtable<Class<? extends IEvent>, List<Handler>>();	
	private EventManager()
	{
		// nothing to be done
	}
	
	public synchronized static EventManager getInstance()
	{
		if( null == instance )
			instance = new EventManager();
		
		return instance;
	}
	
	/**
	 * this method will invoke the relevant method of all the EventHandler's that is registered directly
	 * for this event' class or any of the super class of this event. Thus registering for an event also
	 * automatically registers for all its sub-classes. This increases and decreases complexity at the same time
	 * go through : http://www.marco.panizza.name/dispenseTM/slides/exerc/eventNotifier/eventNotifier.html 
	 * @param event
	 * @throws EventFailureException 
	 */
	public <T extends IEvent> void fireEvent(T event) throws EventFailureException
	{
		Class<?> eventClass = event.getClass();
		
		// find all the event classes that are class / super-class of this event
		// so that we can run all those handler that subscribe directly or indirectly to this event
		// get all the handlers. Rest of the complexity is already handled using generics
		List<Handler> handlers = new ArrayList<Handler>();
		for( Class<? extends IEvent> ec : eventHandlers.keySet() )
		{
			if( ec.isAssignableFrom(eventClass) )
			{
				List<Handler> h = eventHandlers.get(ec);
				handlers.addAll(h);
			}
		}
		// sorting 
		Collections.sort(handlers, new Comparator<Handler>() {

			@Override
			public int compare(Handler o1, Handler o2) 
			{
				if( o1.getOrder() < o2.getOrder() )
					return -1 ;
				else if ( o1.getOrder() > o2.getOrder() )
					return 1;
				else // the event registries have same order. compare the initial-orders
				{
					if( o1.getHandlerObject().getInitialOrder() < o2.getHandlerObject().getInitialOrder() )
						return -1;
					else if( o1.getHandlerObject().getInitialOrder() > o2.getHandlerObject().getInitialOrder() )
						return 1;
				}
				return 0;
			}
		} );
		
		for( Handler handler : handlers )
		{
			// TODO : should we avoid this new object creation and create the object at the time of registry
			try 
			{
				IEventHandler<T> handlerObject = handler.getHandlerObject();
				LOG.info("Executing handler : " + handlerObject + ", for event : " + event ) ;
				handlerObject.handle(event);
				LOG.info("Successfully Executed handler : " + handlerObject + " , for event : " + event ) ;
			}
			catch(EventFailureException e)
			{
				LOG.log(Level.SEVERE, "Error occurred in creating/executing handler " + handler + " , for event : " + event + ". This exception will be thrown.");
				throw e;
			}
			catch (Exception e) {
				LOG.log(Level.SEVERE, "Error occurred in creating/executing handler " + handler + " , for event : " + event + ". This EventHandler is aborted. Changes made by it although might persist");
			}
		}
	}
	
	private <T extends IEvent, P extends IEventHandler<T>> void register( Class<P> handler, Class<T> eventClass, EventRegistry er ) throws EventAlreadyRegisteredException, EventException
	{
		synchronized(eventHandlers)
		{
			List<Handler> currentHandlers = eventHandlers.get(eventClass);
			if(null == currentHandlers)
				currentHandlers = new ArrayList<Handler>();
			IEventHandler<? extends IEvent> handlerObject;
			try {
				handlerObject = handler.newInstance();
			} catch (Exception e) {
				LOG.log(Level.WARNING,"Exception occured while instantiating object of handler." + handler,e);
				throw new EventException(e);
			} 
			// check if already registered
			if(currentHandlers.contains(new Handler(handlerObject)))
			{
				LOG.log(Level.WARNING, "The given handler was already registered for the given event. This request will be ignored. Handler = " + handler + " , event = "  + eventClass);
				throw new EventAlreadyRegisteredException(eventClass.getName(),handler.getName(),"The given handler was already registered for the given event. This request will be ignored.");
			}
			currentHandlers.add(new Handler(handlerObject,er));
			
			eventHandlers.put(eventClass, currentHandlers);
			LOG.info("Successfully Registered : " + handler + " for event " + eventClass);
		}
	}
	
	private <T extends IEvent, P extends IEventHandler<T>> void unregister( Class<P> handler, Class<T> eventClass ) throws InstantiationException, IllegalAccessException
	{
		synchronized(eventHandlers)
		{	
			List<Handler> currentHandlers = eventHandlers.get(eventClass);
			if(null == currentHandlers)
				currentHandlers = new ArrayList<Handler>();
			
			IEventHandler<? extends IEvent> handlerObject = handler.newInstance();
			if( currentHandlers.remove(new Handler(handlerObject)) == false )
			{
				LOG.log(Level.WARNING, "The given handler was not registered for the given event. This request will be ignored. handler : "+ handler + ", event : " + eventClass);
				return;
			}
			
			eventHandlers.put(eventClass, currentHandlers);
			LOG.info("Successfully unRegistered : " + handler + " for event " + eventClass);
		}
	}
	
	/**
	 * This method is used for registering the events which have {@link EventRegistry} in the DB table event_registry and hence called ManagedHandler. 
	 * The difference between managed-handler and unmanaged-handler is that, managed-event-handlers can be disabled or enabled at run time. 
	 * But unmanaged-event-handler cannot be enabled or disabled at run time. Unmanaged event handler might come from some plugins or internally 
	 * from tbits. All addons should only provide managed-event-handlers. Managed handlers must provide the {@link ClassLoader} from where the 
	 * respective class can be loaded. A managed-handler should only be unregistered using the {@link EventManager#unRegisterManagedHandler } method.
	 * @param er : the event-registry entry
	 * @param cl : the class loader from which we can load the EventHandler class and Event class for this entry
	 * @throws EventException
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void registerManagedHandler( EventRegistry er, ClassLoader cl ) throws ClassNotFoundException, EventException
	{
		LOG.info("Registering the eventRegistry : " + er);
		// get the classes -- to make sure that it won't generate any error later
		// register with the EventMechanism if the entry is enabled.
		// add the entry in managedHandlers
		
		synchronized(managedHandlers)
		{
			if( managedHandlers.get(er) != null )
				throw new EventAlreadyRegisteredException(er.getEventClass(),er.getEventHandlerClass(),"This entry is already registered. EntityRegistry : " + er);
			
			Class eventClass = cl.loadClass(er.getEventClass());
			Class eventHandlerClass =  cl.loadClass(er.getEventHandlerClass());
			
			if( er.isEnabled() )
			{
				register(eventHandlerClass, eventClass,er);
			}
			
			managedHandlers.put(er,cl);
		}
	}
	
	/**
	 * This method is used for registering the events which DO NOT have entries in the DB table event_registry and hence called UnManagedHandler.
	 * The difference between managed-handler and unmanaged-handler is that, managed-event-handlers can be disabled or enabled at run time. 
	 * But unmanaged-event-handler cannot be enabled or disabled at run time. Unmanaged event handler might come from some plugins or internally from
	 * tbits. All addons should only provide managed-event-handlers. Unmanaged handlers should only be unregistered using the unRegisterUnManagedHandler method.
	 * @param er
	 * @param cl
	 * @throws EventAlreadyRegisteredException 
	 * @throws EventException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public <T extends IEvent, P extends IEventHandler<T>> void registerUnManagedHandler( Class<P> handler, Class<T> eventClass ) throws EventAlreadyRegisteredException, EventException
	{
		if( null == handler )
		{
			LOG.log(Level.WARNING,"The handler provided was null");
			throw new EventException("The handler provided was null");
		}
		
		register(handler, eventClass,null);
	}
	
	/**
	 * This method unregisters the UnManaged EventHandlers from EventManager. This method ignores if the provided handler or eventClass is null 
	 * or if the handler was not at all registered. So this can be used to clean up states of events if something wrong happens.
	 * @param handler
	 * @param eventClass
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public <T extends IEvent, P extends IEventHandler<T>> void unRegisterUnManagedHandler( Class<P> handler, Class<T> eventClass ) throws InstantiationException, IllegalAccessException
	{
		LOG.info("Registering the handler : " + handler + " for event : " + eventClass);
		if( null == handler || null == eventClass)
		{
			LOG.log(Level.WARNING,"The handler or event provided was null. This request will be ignored.");
			return ;
		}
		
		unregister(handler, eventClass);
	}
	
	/**
	 * This method unregisters the Managed EventHandlers from EventManager. This method ignores if the provided event-registry is null 
	 * or if the handler was not at all registered. So this can be used to clean up states of events if something wrong happens.
	 * @param er
	 * @throws EventException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void unRegisterManagedHandler(EventRegistry er) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		
		if( null == er ) 
		{
			LOG.log(Level.WARNING,"The EventRegistry provided was null. This request will be ignored.");
			return;
		}
		
		synchronized(managedHandlers)
		{
			ClassLoader cl = managedHandlers.get(er);
			if( cl == null )
			{
				LOG.log(Level.WARNING, "This entry was not registered. EntityRegistry : " + er);
				return ;
			}
			
			Class eventClass = cl.loadClass(er.getEventClass());
			Class eventHandlerClass = cl.loadClass(er.getEventHandlerClass());
			
			// make sure by unregistering that no such entry is left in EventManager irrespective of whether the event state is enabled or disabled.
			unregister(eventHandlerClass, eventClass);
			managedHandlers.remove(er);
		}
	}

	/**
	 * toggles the state of event registry and make the necessary changes in the event-manager. The managed entity if it was register still remains
	 * registered with the event-manager.
	 * If it was enabled then now it will be disabled and if it was disabled now it will be enabled.
	 * @param er
	 * @throws EventAlreadyRegisteredException : this should be handled separately from EventException.
	 * @throws EventException
	 */
	public void toggleManagedEvent(EventRegistry er) throws EventAlreadyRegisteredException, EventException
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			con.setAutoCommit(false);
			EventRegistry der = EventRegistryManager.getInstance().getEventRegistryById(er.getEventId(), con);

			if( der.isEnabled() )
			{
				disableRegistry(der,con);
			}
			else
			{
				enableRegistry(der,con);
			}
			
			der.setEnabled(!der.isEnabled());
			EventRegistryManager.getInstance().persist(der);
			
			con.commit();
		}
		catch(Exception e)
		{
			try {
				con.rollback();
			} catch (SQLException e1) {
				LOG.log(Level.WARNING,"Exception occured while rollback of the event state." + e1);
			}
			LOG.log(Level.WARNING,"Exception occured while toggling the event state." + e);
			throw new EventException("Exception occured while toggling the event state." + e);
		}
		finally
		{
			if( null != con )
				try {
					if( !con.isClosed() )
						con.close();
				} catch (SQLException e) {
					LOG.log(Level.WARNING,"Exception occured while closing the connection." + e);
				}
		}
	}

	/**
	 * @param der
	 * @param con
	 * @throws EventException 
	 * @throws ClassNotFoundException 
	 * @throws EventAlreadyRegisteredException : this should be handled separately from EventException.
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private void enableRegistry(EventRegistry der, Connection con) throws  ClassNotFoundException, EventException, EventAlreadyRegisteredException, InstantiationException, IllegalAccessException 
	{
		synchronized(managedHandlers)
		{
			ClassLoader cl = managedHandlers.get(der);
			if( null == cl )
				throw new EventException("The managed event was not registered with the system. first register it to do any action on it. EventRegistry : " + der );

			Class eventClass = cl.loadClass(der.getEventClass());
			Class eventHandlerClass = cl.loadClass(der.getEventHandlerClass());
			
			register(eventHandlerClass, eventClass,der);
		}
	}

	/**
	 * @param der
	 * @param con
	 * @throws EventException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private void disableRegistry(EventRegistry der, Connection con) throws EventException, ClassNotFoundException, InstantiationException, IllegalAccessException 
	{
		synchronized(managedHandlers)
		{
			ClassLoader cl = managedHandlers.get(der);
			if( null == cl )
				throw new EventException("The managed event was not registered with the system. first register it to do any action on it. EventRegistry : " + der );

			Class eventClass = cl.loadClass(der.getEventClass());
			Class eventHandlerClass = cl.loadClass(der.getEventHandlerClass());
			
			unregister(eventHandlerClass, eventClass);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EventManager [managedHandlers=" + managedHandlers
				+ ",<br /><br />" +
				"eventHandlers=" + eventHandlers + "]";
	}

	/**
	 * @param er : The EventRegistry with the changed order.
	 * @param parseInt
	 * @return
	 * @throws EventException 
	 */
	public void changeEventOrder(EventRegistry er) throws EventException 
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			EventRegistry ner = EventRegistryManager.getInstance().persist(er,con);
			synchronized (managedHandlers) 
			{
				ClassLoader cls = managedHandlers.get(ner);
				if( null != cls )
				{
					// the event is registered.
					// remove the registry and add again with the changed eventRegistry
					managedHandlers.remove(ner);
					managedHandlers.put(ner, cls);
					
					synchronized( eventHandlers )
					{
						// see if any event-handler-class is registered with this eventRegistry
						// remove the corresponding event registry and add the new one
						for( List<Handler> handlerList : eventHandlers.values() )
						{
							if( null != handlerList )
							{
								for( Handler handler : handlerList )
								{
									if( handler.getEventRegistry().equals(ner) )
									{
										handler.setEventRegistry(ner);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventException("Error occured while changing event order.");
		} 
		finally
		{
			if( null != con )
				try {
					if( !con.isClosed() )
						con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	
	
}
