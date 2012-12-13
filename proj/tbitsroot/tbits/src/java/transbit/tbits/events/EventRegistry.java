/**
 * 
 */
package transbit.tbits.events;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 -- sql create statement
 
 CREATE TABLE dbo.event_registry
(
event_id bigint IDENTITY (1,1) NOT NULL,
source_id varchar(255) NOT NULL,
event_class varchar(255) NOT NULL,
event_handler_class varchar(255) NOT NULL,
is_enabled bit NOT NULL,
event_order int NOT NULL,
description varchar(1023),
PRIMARY KEY (event_id),
UNIQUE (event_class, event_handler_class)
)
 
  
 */
public class EventRegistry 
{
	private long eventId;
	private String sourceId;
	private String eventClass;
	private String eventHandlerClass;
	private boolean isEnabled;
	private int eventOrder;
	private String description;
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @param eventId
	 * @param sourceId : can be null if the event-handler is not associated with any addon. eg. event-handler is inside tbits core module or other plugin which does not follow the specification of addon mechanism
	 * @param eventClass
	 * @param eventHandlerClass
	 * @param isEnabled
	 * @param eventOrder
	 */
	public EventRegistry(long eventId, String sourceId, String eventClass,
			String eventHandlerClass, boolean isEnabled, int eventOrder, String description) {
		super();
		this.eventId = eventId;
		this.sourceId = sourceId;
		this.eventClass = eventClass;
		this.eventHandlerClass = eventHandlerClass;
		this.isEnabled = isEnabled;
		this.eventOrder = eventOrder;
		this.description = description;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof EventRegistry)) {
			return false;
		}
		EventRegistry other = (EventRegistry) obj;
		if (eventId != other.eventId) {
			return false;
		}
		return true;
	}
	
	/**
	 * @return the addonId
	 */
	public String getSourceId() {
		return sourceId;
	}
	/**
	 * @return the eventClass
	 */
	public String getEventClass() {
		return eventClass;
	}
	/**
	 * @return the eventHandlerClass
	 */
	public String getEventHandlerClass() {
		return eventHandlerClass;
	}
	/**
	 * @return the eventId
	 */
	public long getEventId() {
		return eventId;
	}
	/**
	 * @return the eventOrder
	 */
	public int getEventOrder() {
		return eventOrder;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (eventId ^ (eventId >>> 32));
		return result;
	}
	
	/**
	 * @return the isEnabled
	 */
	public boolean isEnabled() {
		return isEnabled;
	}
	
	/**
	 * @param sourceId the addonId to set
	 */
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}


	/**
	 * @param isEnabled the isEnabled to set
	 */
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	/**
	 * @param eventClass the eventClass to set
	 */
	public void setEventClass(String eventClass) {
		this.eventClass = eventClass;
	}

	/**
	 * @param eventHandlerClass the eventHandlerClass to set
	 */
	public void setEventHandlerClass(String eventHandlerClass) {
		this.eventHandlerClass = eventHandlerClass;
	}
	
	/**
	 * @param eventId the eventId to set
	 */
	public void setEventId(long eventId) {
		this.eventId = eventId;
	}
	/**
	 * @param eventOrder the eventOrder to set
	 */
	public void setEventOrder(int eventOrder) {
		this.eventOrder = eventOrder;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EventRegistry [sourceId=" + sourceId + ", eventClass="
				+ eventClass + ", eventHandlerClass=" + eventHandlerClass
				+ ", eventId=" + eventId + ", eventOrder=" + eventOrder
				+ ", isEnabled=" + isEnabled
				+ ", description=" + description + "]";
	}
	
	
}
