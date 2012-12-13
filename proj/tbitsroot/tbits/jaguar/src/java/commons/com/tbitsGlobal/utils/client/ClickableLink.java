package commons.com.tbitsGlobal.utils.client;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.DomEvent;
import com.google.gwt.user.client.Random;

/**
 * @author sourabh
 * 
 * A class that makes HTML DIV element that listens to click events
 */
public class ClickableLink {
	
	private String text;
	private String className;
	
	private List<ClickableLinkListener> listeners;
	
	public interface ClickableLinkListener<E extends DomEvent>{
		public void onClick(E e);
	}
	
	public ClickableLink(String text) {
		
		this.text = text;
		
		this.listeners = new ArrayList<ClickableLinkListener>();
		
		int n = Random.nextInt();
		className = "tbits-click-link-" + n;
	}
	
	public <E extends DomEvent> ClickableLink(String text, ClickableLinkListener<E> listener) {
		this(text);
		this.addListener(listener);
	}
	
	/**
	 * Add listeners to be executed on click
	 * @param <E>
	 * @param listener
	 */
	public <E extends DomEvent> void addListener(ClickableLinkListener<E> listener){
		listeners.add(listener);
	}
	
	public <E extends DomEvent> void executeListeners(E e){
		for(ClickableLinkListener<E> listener : listeners)
			listener.onClick(e);
	}

	public String getText() {
		return text;
	}
	
	public void setText(String text){
		this.text = text;
	}

	public String getClassName() {
		return "div." + className;
	}

	/**
	 * @return The html for the element
	 */
	public String getHtml() {
		return "<span style='cursor:pointer; cursor:hand; color:#00f; display:inline;'><div class='" + className + "' style='display:inline;'>" + this.text + "</div></span>";
	}
}
