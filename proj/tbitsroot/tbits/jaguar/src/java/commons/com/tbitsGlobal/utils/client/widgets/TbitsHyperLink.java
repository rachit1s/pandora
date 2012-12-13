package commons.com.tbitsGlobal.utils.client.widgets;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

/**
 * 
 * @author sourabh
 * 
 * A widget that looks like an Anchor element
 */
public class TbitsHyperLink extends BoxComponent{

	private String text;
	
	public static String COLOR_DEFAULT_BLUE = "#00f";
	  
	public TbitsHyperLink() {
		super();
		this.setStyleAttribute("color", COLOR_DEFAULT_BLUE);
		this.setStyleAttribute("text-decoration", "underline");
	}
		
	public TbitsHyperLink(String text){
		this();
		this.setText(text);
	}
	  
	public TbitsHyperLink(String text, SelectionListener<TbitsHyperLinkEvent> listener) {
		this(text);
		addSelectionListener(listener);
	}
	
	public void addSelectionListener(SelectionListener<TbitsHyperLinkEvent> listener) {
	    addListener(Events.Select, listener);
	}
	  
	@Override
	public void onComponentEvent(ComponentEvent ce) {
	    super.onComponentEvent(ce);
	    switch (ce.getEventTypeInt()) {
	      case Event.ONCLICK:
	        ce.stopEvent();
	        onClick(ce);
	        break;
	    }
	}

	public void removeSelectionListener(SelectionListener<TbitsHyperLinkEvent> listener) {
	    removeListener(Events.Select, listener);
	}

	public void setText(String text) {
	    this.text = text;
	    if (rendered) {
	        getElement().setInnerHTML(getHTML(text));
	      }
	}
	
	protected String getHTML(String text){
		return text != null ? "<a style='color:inherit; cursor:pointer; cursor:hand;'>" + text + "</a>" : "&nbsp;";
	}
	
	public String getText(){
		return this.text;
	}
	  
	  @Override
	  protected void onRender(Element parent, int index) {
	    setElement(DOM.createDiv(), parent, index);
	    if (text != null) {
	      setText(text);
	    }
	    sinkEvents(Event.ONCLICK + Event.ONMOUSEOVER + Event.ONMOUSEOUT);
	  }

	  @Override
	  protected ComponentEvent createComponentEvent(Event event) {
	    return new TbitsHyperLinkEvent(this);
	  }

	  protected void onClick(ComponentEvent ce) {
	    ce.preventDefault();
	    focus();
	    hideToolTip();
	    if (!disabled) {
	    	TbitsHyperLinkEvent be = new TbitsHyperLinkEvent(this);
	      if (!fireEvent(Events.BeforeSelect, be)) {
	        return;
	      }
	      fireEvent(Events.Select, be);
	    }
	  }	
}
