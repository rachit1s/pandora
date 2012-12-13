package commons.com.tbitsGlobal.utils.client;

import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Params;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class TbitsInfoConfig {
	
	public static AbstractImagePrototype INFO 	= IconHelper.create(IconConstants.INFO, 16, 16);
	public static AbstractImagePrototype WARN 	= IconHelper.create(IconConstants.WARNING);
	public static AbstractImagePrototype ERROR 	= IconHelper.create(IconConstants.ERROR);
	
	/**
	   * The info title (defaults to null).
	   */
	  public String text;

	  /**
	   * Throwable in case of errors
	   */
	  public Throwable caught;

	  /**
	   * The index or key based substitution values.
	   */
	  public Params params;

	  /**
	   * The time in milliseconds to display a message (defaults to 5000).
	   */
	  public int display = 5000;

	  /**
	   * The info width (defaults to 225).
	   */
	  public int width = XDOM.getViewportWidth() / 2;

	  /**
	   * The info height (defaults to 75).
	   */
	  public int height = 45;

	  /**
	   * Listener to be notified when the info is displayed (defaults to null).
	   */
	  public Listener<ComponentEvent> listener;

	  public TbitsInfoConfig(String text){
		  this.text = text;
	  }
	  
	  public TbitsInfoConfig(String text, Throwable caught) {
		  this(text);
		  this.caught = caught;
	  }

	  public TbitsInfoConfig(String text, Throwable caught, Params params) {
		  this(text, caught);
		  this.params = params;
	  }
}
