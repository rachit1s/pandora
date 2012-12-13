package com.tbitsGlobal.jaguar.client.widgets.grids;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Util;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.google.gwt.user.client.Element;

public class GridCellToolTip extends ToolTip  {
	private boolean initialized;
	private boolean interceptTitles;
	private Element targetElem;
	
	/**
	   * Creates a new quick tip instance.
	   * 
	   * @param component the source component
	   */
	  public GridCellToolTip(Component component) {
	    super(component);
	    toolTipConfig.setAutoHide(false);
//	    this.setClosable(true);
	  }

	  @Override
	  public void initTarget(Component target) {
	    if (listener == null) {
	      listener = new Listener<ComponentEvent>() {
	        public void handleEvent(ComponentEvent be) {
	          EventType type = be.getType();
	          if (type == Events.OnMouseOver) {
	            onTargetOver(be);
//	          } else if (type == Events.OnMouseOut) {
//	            onTargetOut(be);
//	          } else if (type == Events.OnMouseMove) {
//	            onMouseMove(be);
//	          } else if (type == Events.Hide || type == Events.Detach) {
//	            hide();
	          }
	        }
	      };
	    }
	    super.initTarget(target);
	  }

	  /**
	   * Returns true if intercept titles is enabled.
	   * 
	   * @return the intercept title state
	   */
	  public boolean isInterceptTitles() {
	    return interceptTitles;
	  }

	  /**
	   * True to automatically use the element's DOM title value if available
	   * (defaults to false).
	   * 
	   * @param intercepTitiles true to to intercept titles
	   */
	  public void setInterceptTitles(boolean intercepTitiles) {
	    this.interceptTitles = intercepTitiles;
	  }

	  @Override
	  protected void onHide() {
	    super.onHide();
	    targetElem = null;
	  }

	  @Override
	  protected void onTargetOut(ComponentEvent ce) {
		if (targetElem == null) {
	      super.onTargetOut(ce);
	    }
	  }

	  @Override
	  protected void onTargetOver(ComponentEvent ce) {
	    if (disabled) {
	      return;
	    }

	    Element t = ce.getTarget();
	    while (t != null && t != target.getElement()) {
	      if (hasTip(t)) {
	        break;
	      }
	      t = (Element) t.getParentElement();
	    }

	    boolean hasTip = t != null && hasTip(t);
	    
	    if (!initialized && !hasTip) {
	      return;
	    }
	    initialized = true;

	    if (targetElem == null && hasTip) {
	      updateTargetElement(t);
	    } else {
	      if (hasTip && targetElem != t) {
	        updateTargetElement(t);
	      } else if (targetElem != null && ce.within(targetElem)) {
	        return;
	      } else {
	        clearTimers();
	        hide();
	        targetElem = null;
	        text = null;
	        title = null;
	        return;
	      }
	    }

//	    clearTimer("hide");
	    targetXY = ce.getXY();
	    delayShow();
	  }

	  private String getAttributeValue(Element target, String attr) {
	    String v = target.getAttribute(attr);
	    return hasAttributeValue(v) ? v : null;
	  }

	  private boolean hasAttributeValue(String v) {
	    return v != null && !v.equals("");
	  }

	  private boolean hasTip(Element target) {
	    String tip = target.getAttribute("qtip");
	    String title = target.getAttribute("title");
	    return hasAttributeValue(tip) || (interceptTitles && hasAttributeValue(title));
	  }

	  private void updateTargetElement(Element target) {
	    targetElem = target;
	    text = interceptTitles ? getAttributeValue(target, "title") : getAttributeValue(
	        target, "qtip");
	    title = getAttributeValue(target, "qtitle");

	    String width = getAttributeValue(target, "qwidth");
	    if (width != null) {
	      setWidth(Util.parseInt(width, 100));
	    }
	  }
}
