package commons.com.tbitsGlobal.utils.client.widgets.forms;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsMessageBox;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.tags.TagsMenuButton;
import commons.com.tbitsGlobal.utils.client.tvn.TvnCheckoutButton;
import commons.com.tbitsGlobal.utils.client.widgets.EffectiveHeightContainer;

/**
 * 
 * @author sourabh
 * 
 * Component to be shown at the top of the panel
 * 
 * Contains the following : 
 * 1. sysPrefix#requestId : subject (if any)
 * 2. The buttons
 * 3. Message Box
 */
public class FormHeadingPanel extends EffectiveHeightContainer{
	
	private SplitButton actionsButton;
	private TagsMenuButton TAGS_BUTTON;
	private TvnCheckoutButton TVN_BUTTON;
	protected LayoutContainer requestHeaderContainer;
	
	protected ButtonBar bar;
	
	private TbitsMessageBox messageBox;
	
	public FormHeadingPanel() {
		super();
		
		this.requestHeaderContainer = new LayoutContainer();
		this.messageBox = new TbitsMessageBox(){
			@Override
			public void onChangeHeight() {
				super.onChangeHeight();
				
				FormHeadingPanel.this.layout();
			}
		};
		
		bar = new ButtonBar();
		bar.setStyleAttribute("padding", "5px 5px 10px 5px");
	}
	
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		this.add(messageBox);
		
		bar.add(requestHeaderContainer);
		
		bar.add(new FillToolItem());
		
		if(actionsButton != null)
			bar.add(actionsButton);
		
		if(TAGS_BUTTON !=null)
			bar.add(TAGS_BUTTON);
		
		if(TVN_BUTTON != null)
			bar.add(TVN_BUTTON);
		
		this.add(bar);
	}
	
	public void setRequestHeader(String sysPrefix, int requestId, String text){
		requestHeaderContainer.removeAll();
		requestHeaderContainer.addText(((requestId != 0)?("<span style='color:#00f; font-weight:bold; font-size:14px;' >" + sysPrefix + "#"
				+ requestId + "</span> : "):"") + "<span style='font-weight:bold; font-size:14px;'>" 
				+ text + "</span>");
	}

	public TbitsMessageBox getMessageBox() {
		return messageBox;
	}

	public void setActionsButton(SplitButton actionsButton) {
		this.actionsButton = actionsButton;
	}
	
	public void setTagsButton(TagsMenuButton tagsButton, final TbitsTreeRequestData requestModel){
		this.TAGS_BUTTON = tagsButton;
		TAGS_BUTTON.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				List<TbitsTreeRequestData> selectedItems = new ArrayList<TbitsTreeRequestData>();
				selectedItems.add(requestModel);
				TAGS_BUTTON.setTagsCheckStatus(selectedItems);
			}
		});
	}
	
	public void setTvnButton(final TvnCheckoutButton tvnButton){
		this.TVN_BUTTON = tvnButton;
	}
	
	public void displayTags(String tags) {
		if(tags != null){
			requestHeaderContainer.addText("Tags : " + tags);
		}
	}
	
	public void addExtButton(Component item){
		this.bar.add(item);
		if(this.bar.isRendered())
			this.bar.layout();
	}

}
