package commons.com.tbitsGlobal.utils.client;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;

public abstract class ClickListener<E extends ComponentEvent> implements Listener<E>{
	public void handleEvent(E e) {
		EventType type = e.getType();
	    if (type == Events.OnClick) {
	      componentClicked(e);
	    }
	};
	
	public abstract void componentClicked(E ce);
}
