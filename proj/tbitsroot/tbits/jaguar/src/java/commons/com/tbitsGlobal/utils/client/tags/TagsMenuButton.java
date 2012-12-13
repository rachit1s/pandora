package commons.com.tbitsGlobal.utils.client.tags;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.KeyCodes;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.RefreshTagsList;
import commons.com.tbitsGlobal.utils.client.Events.TagRequests;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;
import commons.com.tbitsGlobal.utils.client.widgets.TriStateCheckMenuItem.TriState;

/**
 * The class defines a tag menu button for toolbars. The tagmenu button
 * is currently added to the toolbars attached to the searchgrids.
 * To attach it to any other grid, simply call the addTagsButton method 
 * in the initializeButtons method of the required toolbar 
 * (which should extend TbitsToolBar).
 * <br>
 * The tags menu button carries the names of all the available tags. 
 * Clicking on the name of the tag will tag the selected requests with
 * that particular tag. There is a <b>X</b> button provided next to every tag 
 * to remove the tag from selected requests. The menu button is scrollable.
 * <br>
 * The button subscribes to the 'OnTagsEdit' event to repopulate the menu.
 * 
 * @author karan
 *
 */

public class TagsMenuButton extends ToolBarButton{
	
	//============================================================================================

	// To observe events
	protected TbitsObservable observable;
	// Initial snapshot of tag menu items
	protected ArrayList<TagsMenuItem> onClickList;
	// List of current state of tag menu items
	protected ArrayList<TagsMenuItem> itemList;
	// Flag for tri or two state menu items
	protected boolean triState;
	
	//============================================================================================
	
	/**
	 * <b>Constructor</b>
	 * <br>
	 * The tags menu button carries the names of all the available tags. 
	 * Clicking on the name of the tag will tag the selected requests with
	 * that particular tag. There is a <b>X</b> button provided next to every tag 
	 * to remove the tag from selected requests. The menu button is scrollable.
	 * </br>
	 * <br>
	 * The button subscribes to the 'OnTagsEdit' event to repopulate the menu.
	 * </br>
	 */
	public TagsMenuButton(String name, boolean isTriState){
		
		super(name);
		triState = isTriState;
		makeNewTagMenu(null, null);
		observable = new BaseTbitsObservable();
		observable.attach();
		TagsUtils.fetchTags(ClientUtils.getCurrentUser());
		subscribeToEvents();
		
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		
		observable.attach();
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		
		observable.detach();
	}

	//============================================================================================

	/**
	 * Make and add a new tags menu item for the provided tags. Associate the selection listeners here.
	 * @param publicTags : ArrayList<String> of public tag names that are to be shown in the menu 
	 * @param tags : ArrayList<String> of tag names that are to be shown in the menu 
	 */
	private void makeNewTagMenu(List<String> publicTags, List<String> tags) {
		
		itemList = new ArrayList<TagsMenuItem>();
		onClickList = new ArrayList<TagsMenuItem>();
		
		Menu tagsMenu = new Menu();
		tagsMenu.setMaxHeight(300);
		
		tagsMenu.add(addTagItem());
		tagsMenu.add(applyChangesItem());
		
		if(publicTags!=null && publicTags.size()!=0){
			tagsMenu.add(new SeparatorMenuItem());
			MenuItem name = new MenuItem("<b>Public Tags</b>");
			name.setEnabled(false);
			tagsMenu.add(name);
			for(final String tag : publicTags){
				TagsMenuItem tagItem = new TagsMenuItem("public", tag);
				tagItem.setIsTriState(triState);
				itemList.add(tagItem);
				tagsMenu.add(tagItem);
				TagsMenuItem onClickTagItem = new TagsMenuItem("public", tag);
				onClickTagItem.setIsTriState(triState);
				onClickList.add(onClickTagItem);
			}
		}
		
		if(tags!=null && tags.size()!=0){
			tagsMenu.add(new SeparatorMenuItem());
			MenuItem name = new MenuItem("<b>My Tags</b>");
			name.setEnabled(false);
			tagsMenu.add(name);
			for(final String tag : tags){
				TagsMenuItem tagItem = new TagsMenuItem("private", tag);
				tagItem.setIsTriState(triState);
				itemList.add(tagItem);
				tagsMenu.add(tagItem);
				TagsMenuItem onClickTagItem = new TagsMenuItem("private", tag);
				onClickTagItem.setIsTriState(triState);
				onClickList.add(onClickTagItem);
			}
		}
		
		this.setMenu(tagsMenu);
		this.setToolTip("Tag the selected requests");
	}

	//============================================================================================

	/**
	 * The MenuItem for applying the chosen state of tags. When selected, it applies the changes made
	 * (if any) to the state of tags for the selected requests. The state of tags is determined by the
	 * TagsMenuItem.
	 * @return MenuItem for handling the application of changes in the state of tags.
	 */
	private MenuItem applyChangesItem() {
		
		MenuItem applyChangesItem = new MenuItem("<b>Apply Changes</b>");
		applyChangesItem.addSelectionListener(new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				
				for( int i=0; i<itemList.size(); i++){
					TagsMenuItem tag = itemList.get(i);
					// Continue if the state of the tag has not changed
					if(tag.getTriState().equals(onClickList.get(i).getTriState()))
						continue;
					if(tag.getTriState().equals(TriState.CHECKED))
						TbitsEventRegister.getInstance().fireEvent(new TagRequests(tag.tagType, tag.tagName, TagRequests.APPLY));
					else if(tag.getTriState().equals(TriState.UNCHECKED))
						TbitsEventRegister.getInstance().fireEvent(new TagRequests(tag.tagType, tag.tagName, TagRequests.REMOVE));
				}
			}
		});
		return applyChangesItem;
	}

	//============================================================================================

	/**
	 * The MenuItem for adding the new tag. When selected, it prompts the user to enter the 
	 * name of the tag to be created. The tag is created and applied to the selected requests
	 * if the given tag name does not already exist.
	 * @return MenuItem for handling the creation of a new tag item
	 */
	private MenuItem addTagItem() {
		MenuItem addTagItem = new MenuItem("<b>Create New Tag</b>");
		addTagItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				
				final Window window = new Window();
                window.setWidth(400);
                window.setHeading("Enter New Tag");
                window.setModal(true);
                
                final TextField<String> tag = new TextField<String>();                
                tag.setWidth(400);
                tag.addKeyListener(new KeyListener(){
                	public void componentKeyUp(ComponentEvent event) {
                		if(event.getKeyCode() == KeyCodes.KEY_ENTER){
                			String tagName = tag.getValue();
                			TagsUtils.addTag(tagName, ClientUtils.getCurrentUser());
                			window.hide();
                		}
                	}
                });
                
                window.add(tag);
                
                ToolBar tb = new ToolBar();
                tb.setAlignment(HorizontalAlignment.CENTER);
                tb.add(new Button("OK", new SelectionListener<ButtonEvent>() {
					
					public void componentSelected(ButtonEvent ce) {
						String tagName = tag.getValue();
						TagsUtils.addTag(tagName, ClientUtils.getCurrentUser());
						window.hide();
					}
				}));
                tb.add(new Button("Cancel", new SelectionListener<ButtonEvent>() {
					
					public void componentSelected(ButtonEvent ce) {
						window.hide();
					}
				}));
                window.setBottomComponent(tb);
                window.show();        
				tag.focus();
			}
		});
		return addTagItem;
	}

	//============================================================================================	
	
	private void resetItemLists() {
		
		for(int i = 0; i<itemList.size(); i++){
			onClickList.get(i).reset();
			itemList.get(i).reset();
		}
	}

	//============================================================================================	
	
	private void subscribeToEvents() {
		
		observable.subscribe(RefreshTagsList.class, new ITbitsEventHandle<RefreshTagsList>(){
			
			public void handleEvent(RefreshTagsList event) {
				
				makeNewTagMenu(event.getPublicTags(), event.getPrivateTags());
			}
		});
		
	}

	//============================================================================================	

	/**
	 * Set the check status of all the tags
	 * 
	 * @param selectedRequests
	 */
	public void setTagsCheckStatus(List<TbitsTreeRequestData> selectedRequests){
		
		resetItemLists();
		for(TbitsTreeRequestData currRequest : selectedRequests){
			String private_tags = currRequest.get("private_request_tags");
			String public_tags = currRequest.get("public_request_tags");
			if(private_tags == null && public_tags == null)
				return;
			ArrayList<String> privateTagTokens = tokenise(private_tags, ",");
			ArrayList<String> publicTagTokens = tokenise(public_tags, ",");
			// Make a list of all tags
			for(int i=0; i<itemList.size(); i++){
				TagsMenuItem tag = itemList.get(i);
				if((tag.tagType.equals("private") && privateTagTokens.contains(tag.tagName)) 
						|| (tag.tagType.equals("public") && publicTagTokens.contains(tag.tagName))){
					tag.setFound(true);
					onClickList.get(i).setFound(true);
				}
				else{
					tag.setFoundInAll(false);
					onClickList.get(i).setFoundInAll(false);
				}
			}
		}
		
		// Set the states of the menu items
		for(TagsMenuItem currItem : itemList){
			if(currItem.isFound()){
				if(currItem.isFoundInAll()){
					currItem.setTriState(TriState.CHECKED);
					currItem.nextState = TriState.UNCHECKED;
				}
				else{
					currItem.setTriState(TriState.PARTIAL);
				}
			}
			else{
				currItem.setTriState(TriState.UNCHECKED);
				currItem.nextState = TriState.CHECKED;
			}
		}
		
		for(TagsMenuItem currItem : onClickList){
			if(currItem.isFound()){
				if(currItem.isFoundInAll())
					currItem.setTriState(TriState.CHECKED);
				else
					currItem.setTriState(TriState.PARTIAL);
			}
			else
				currItem.setTriState(TriState.UNCHECKED);
		}
		
	}
	
	// Utility function to tokenise the tags
	private ArrayList<String> tokenise(String tags, String delim) {
		
		ArrayList<String> tokens = new ArrayList<String>();
		String tagsLeft = tags;
		int delimIndex = tagsLeft.indexOf(delim);
		while(delimIndex != -1){
			tokens.add(tagsLeft.substring(0, delimIndex).trim());
			tagsLeft = tagsLeft.substring(delimIndex + 1);
			delimIndex = tagsLeft.indexOf(delim);
		}
		tokens.add(tagsLeft.trim());
		return tokens;
	}
	
	//============================================================================================	

}
