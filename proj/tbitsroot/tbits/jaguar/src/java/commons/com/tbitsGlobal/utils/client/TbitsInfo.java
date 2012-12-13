package commons.com.tbitsGlobal.utils.client;

import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.util.Point;
import com.extjs.gxt.ui.client.util.Size;
import com.extjs.gxt.ui.client.util.Util;
import com.extjs.gxt.ui.client.widget.tips.Tip;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * 
 * @author sourabh
 * 
 * A tip used to display messages at the top of the application
 */
public class TbitsInfo extends Tip{
	
	protected TbitsInfoConfig config;
	public static int ERROR	=	2;
	public static int INFO	=	1;
	
	private static TbitsInfo display(TbitsInfoConfig config) {
	    return create().show(config);
	}
	
	private static TbitsInfo display(TbitsInfoConfig config, AbstractImagePrototype icon) {
	    return create().show(config, icon);
	}
	
	public static TbitsInfo info(TbitsInfoConfig config) {
	    return display(config);
	}
	
	public static TbitsInfo info(String text) {
	    return display(new TbitsInfoConfig(text));
	}
	
	public static TbitsInfo info(String text, Throwable caught) {
	    return display(new TbitsInfoConfig(text, caught));
	}
	
	public static TbitsInfo warn(TbitsInfoConfig config) {
	    return display(config, TbitsInfoConfig.WARN);
	}
	
	public static TbitsInfo warn(String text) {
	    return display(new TbitsInfoConfig(text), TbitsInfoConfig.WARN);
	}
	
	public static TbitsInfo warn(String text, Throwable caught) {
	    return display(new TbitsInfoConfig(text, caught), TbitsInfoConfig.WARN);
	}
	
	public static TbitsInfo error(TbitsInfoConfig config) {
	    return display(config, TbitsInfoConfig.ERROR);
	}
	
	public static TbitsInfo error(String text) {
	    return display(new TbitsInfoConfig(text), TbitsInfoConfig.ERROR);
	}
	
	public static TbitsInfo error(String text, Throwable caught) {
	    return display(new TbitsInfoConfig(text, caught), TbitsInfoConfig.ERROR);
	}
	
	private static TbitsInfo create() {
	    TbitsInfo info = new TbitsInfo();
	    return info;
	}
	
	private TbitsInfo() {
		super();
		
	    this.setClosable(true);
	    this.setShadow(false);
	    this.setMaxWidth(510);	// The background image of GXT is 500px in width. 10px is to allow for close button
	}
	
	private TbitsInfo show(TbitsInfoConfig config) {
	    this.config = config;
	    onShowInfo();
	    
	    return this;
	}
	
	private TbitsInfo show(TbitsInfoConfig config, AbstractImagePrototype icon) {
		this.setIcon(icon);
		return show(config);
	}
	
	protected void onShowInfo() {
	    Point p = position();
	    this.showAt(p);

	    afterShow();
	}
	
	protected Point position() {
	    Size s = XDOM.getViewportSize();
	    int left = (s.width - Util.constrain(config.width, this.getMinWidth(), this.getMaxWidth()))/2;
	    int top = 2;
	    return new Point(left, top);
	}
	
	protected void doAutoWidth() {
	    if (width == null) {
	    	setWidth(Util.constrain(config.width, this.getMinWidth(), this.getMaxWidth()));
	    }
	  }
	
	private void afterShow() {
	    Timer t = new Timer() {
	      public void run() {
	    	  hide();
	      }
	    };
	    t.schedule(config.display);
	}
	
	@Override
	protected void updateContent() {
		super.updateContent();
		if (config.params != null) {
			config.text = Format.substitute(config.text, config.params);
	    }
		String title = "<span style=\"font-size:12px;\">" + config.text + "</span>";
	    getHeader().setText(title == null ? "" : title);
	}

	/**
	 * Use {@link TbitsInfo#info(String)} or {@link TbitsInfo#warn(String)} or similar methods
	 * @param text
	 * @param mode
	 */
	@Deprecated
	public static void write(String text, int mode){
		write(text, mode, false);
	}
	
	/**
	 * Use {@link TbitsInfo#info(String)} or {@link TbitsInfo#warn(String)} or similar methods
	 * @param text
	 * @param mode
	 */
	@Deprecated
	public static void write(String text, int mode, boolean needErase){
		if(mode == TbitsInfo.INFO)
			info(text);
		else if(mode == TbitsInfo.ERROR)
			error(text);
		
		return;
	}
}
