package commons.com.tbitsGlobal.utils.client;

import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.google.gwt.user.client.Element;
import commons.com.tbitsGlobal.utils.client.widgets.TbitsHyperLink;
import commons.com.tbitsGlobal.utils.client.widgets.TbitsHyperLinkEvent;

/**
 * 
 * @author sourabh
 * 
 * A message box meant to be shown at the top of Forms
 * 
 * It shows to kinds of messages : 
 * 1. Info using {@link #info(String)}
 * 2. Error using {@link #error(String)}
 * 
 * Description of form can be specified by using {@link #setDefaultInfo(String)}.
 * 
 * The box can be reset to initial state using {@link #reset()}
 */
public class TbitsMessageBox extends LayoutContainer{
	private int INFO	=	1;
	private int ERROR	=	2;
	
	private String defaultInfo;
	
	private TbitsHyperLink clear;
	
	private LayoutContainer messageContainer;
	
	/**
	 * Constructor
	 */
	public TbitsMessageBox(){
		super(new ColumnLayout());
		
		this.setVisible(false);
		this.setStyleAttribute("padding", "5px");
		this.setStyleAttribute("border", "1px solid #F6BC5D");
		this.setStyleAttribute("fontWeight", "bold");
		this.setStyleAttribute("fontSize", "11px");
		
		messageContainer = new LayoutContainer();
		
		clear = new TbitsHyperLink("Clear", new SelectionListener<TbitsHyperLinkEvent>(){
			@Override
			public void componentSelected(TbitsHyperLinkEvent ce) {
				reset();
			}});
		clear.setStyleAttribute("marginLeft", "5px");
	}
	
	public TbitsMessageBox(String defaultInfo) {
		this();
		
		this.defaultInfo = defaultInfo;
	}

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		clear.setVisible(false);
		
		this.add(messageContainer);
		this.add(clear, new ColumnData(35));
		
		// Write the default info if any
		if(defaultInfo != null)
			writeDefault();
	}
	
	/**
	 * Appends info
	 * @param text
	 */
	public void info(String text){
		info(text, true);
	}
	
	/**
	 * Writes info
	 * @param text
	 * @param append
	 */
	public void info(String text, boolean append){
		this.setStyleAttribute("background", "#faeead");
		write(text, INFO, append);
	}
	
	/**
	 * Appends Error message
	 * @param text
	 */
	public void error(String text){
		error(text, true);
	}
	
	/**
	 * Writes error message
	 * @param text
	 * @param append
	 */
	public void error(String text, boolean append){
		this.setStyleAttribute("background", "#cc5555");
		write(text, ERROR, append);
	}
	
	private void write(String text, int mode, boolean append){
		if(text == null || text.equals("")) return;
		
		if(!append)
			this.reset();
		
		this.messageContainer.addText(text);
		
		this.show();
		this.clear.show();
		
		this.layout();
		
		onChangeHeight();
	}
	
	private void writeDefault(){
		this.messageContainer.removeAll();
		this.info(defaultInfo);
		
		this.clear.hide();
	}
	
	/**
	 * Resets to initial state
	 */
	public void reset(){
		if(defaultInfo != null){
			writeDefault();
		}else if(this.isRendered()){
			this.messageContainer.removeAll();
			this.hide();
		}
		
		onChangeHeight();
	}
	
	/**
	 * Called when there is a possible change in height of this component
	 */
	public void onChangeHeight(){}

	public void setDefaultInfo(String defaultInfo) {
		this.defaultInfo = defaultInfo;
	}

	public String getDefaultInfo() {
		return defaultInfo;
	}
}
