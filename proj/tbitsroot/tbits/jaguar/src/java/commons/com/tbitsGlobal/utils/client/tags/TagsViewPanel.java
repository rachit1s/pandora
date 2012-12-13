package commons.com.tbitsGlobal.utils.client.tags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EditorEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.RefreshTagsList;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;
import commons.com.tbitsGlobal.utils.client.urlManager.TbitsURLManager;

/**
 * This class defines a tag view panel that lists all the tags for the current user.
 * <br>
 * The tags view panel has the following features :
 * <br>- A checklist of all the tags for the current user. The tag names are editable on click.
 * <br>- A "View" button to display the requests tagged by the tags checked in the checklist
 * <br>- A "Delete" button to delete all the selected tags and disassociate all the requests 
 * 		 associated with those tags.
 * 
 * @author karan
 *
 */

public class TagsViewPanel extends ContentPanel{

	//============================================================================================

	protected EditorGrid<TbitsModelData> publicTagGrid = null;
	protected EditorGrid<TbitsModelData> privateTagGrid = null;
	protected Button viewButton = null;
	protected Button deleteButton = null;
	protected boolean showButtons = true;
	protected TbitsObservable observable;
	
	//============================================================================================

	/**
	 * Constructor
	 */
	public TagsViewPanel(){
		
		this.setScrollMode(Scroll.AUTO);
		this.setHeading("My Tags");
		this.setAnimCollapse(true);
		
		this.setLayout(new FlowLayout(0)); 
		
		observable = new BaseTbitsObservable();
		observable.attach();
		
		// Fetch the tags for the user and populate the panel
		TagsUtils.fetchTags(ClientUtils.getCurrentUser());
		observable.subscribe(RefreshTagsList.class, new ITbitsEventHandle<RefreshTagsList>(){
			public void handleEvent(RefreshTagsList event) {
				populatePanel(event.getPrivateTags(), event.getPublicTags());
			}});
		
//		observable.subscribe(OnHistoryTokensChanged.class, new ITbitsEventHandle<OnHistoryTokensChanged>() {
//
//			public void handleEvent(OnHistoryTokensChanged event) {
//				
//				privateTagGrid.getSelectionModel().deselectAll();
//				publicTagGrid.getSelectionModel().deselectAll();
//			}
//			
//		});
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
	
	public HashMap<String, List<String>> getSelectedTags(){
		
		if(privateTagGrid == null || publicTagGrid == null)
			return null;
		
		HashMap<String, List<String>> selectedTags = new HashMap<String, List<String>>();
		List<TbitsModelData> selected = privateTagGrid.getSelectionModel().getSelectedItems();
		ArrayList<String> privateTags = new ArrayList<String>();
		for(TbitsModelData data : selected){
			privateTags.add((String) data.get("tag"));
		}
		selectedTags.put(TagsUtils.PRIVATE, privateTags);
		
		selected = publicTagGrid.getSelectionModel().getSelectedItems();
		ArrayList<String> publicTags = new ArrayList<String>();
		for(TbitsModelData data : selected){
			publicTags.add((String) data.get("tag"));
		}
		selectedTags.put(TagsUtils.PUBLIC, publicTags);
		
		return selectedTags;
	}

	//============================================================================================

	/**
	 * Make a checkbox view of the tags in the view panel. 
	 * Add the necessary buttons for modification and viewing.
	 * @param tags
	 */
	private void populatePanel(List<String> tags, List<String> publicTags) {
		
		if(tags == null)
			return;
		this.removeAll();
		makeCheckBoxView(tags, publicTags);
	}

	//============================================================================================

	/**
	 * Add the checkboxes for viewing the tags.
	 * Also add the operations button and the view button.
	 * @param tags
	 */
	private void makeCheckBoxView(List<String> tags, List<String> publicTags){
		
		addPublicTagsGrid(publicTags);
		addPrivateTagsGrid(tags);
        
        // Add the deleteButton at the botton of the view panel
        if(deleteButton == null && showButtons) {
        	deleteButton = new Button("Delete", new SelectionListener<ButtonEvent>() {

    			@Override
    			public void componentSelected(ButtonEvent ce) {
    				
    				final HashMap<String, List<String>> selectedTags = getSelectedTags();
    				
    				if(selectedTags.get(TagsUtils.PUBLIC).size() > 0){
    					TbitsInfo.info("Deletion of public tags not permitted. Deleting only private tags");
    				}
    				if(selectedTags.size() == 0)
    					return;
    				else if(selectedTags.size() == 1){
    					TagsUtils.deleteTags(ClientUtils.getCurrentUser(), selectedTags.get(TagsUtils.PRIVATE));
    				}
    				else{
    					MessageBox.confirm("Confirm", "Are you sure you want to delete all the selected tags?", 
    	        				new Listener<MessageBoxEvent>() {  
    	        			
    						public void handleEvent(MessageBoxEvent be) {  
    							// Return if the "No" button was clicked
    							String clickedText = be.getButtonClicked().getText().toLowerCase();
    							if(clickedText.equals("no"))
    								return;
    							else{
    								TagsUtils.deleteTags(ClientUtils.getCurrentUser(), selectedTags.get(TagsUtils.PRIVATE));
    							}
    						}
    	        		});  
    				}
    			}
    			
    		});
    		
            this.addButton(deleteButton);
    		this.layout();
        }
        
        // Add the viewButton at the botton of the view panel
        if(viewButton == null && showButtons) {
        	viewButton = new Button("View", new SelectionListener<ButtonEvent>() {

    			@Override
    			public void componentSelected(ButtonEvent ce) {
    				
    				ListStore<HistoryToken> existingTokens = TbitsURLManager.getInstance().stringToStore();
    				for(HistoryToken ht : existingTokens.findModels(HistoryToken.KEY, GlobalConstants.TOKEN_TAGS_PUBLIC)){
    					TbitsURLManager.getInstance().removeToken(ht);
    				}
    				for(HistoryToken ht : existingTokens.findModels(HistoryToken.KEY, GlobalConstants.TOKEN_TAGS_PRIVATE)){
    					TbitsURLManager.getInstance().removeToken(ht);
    				}
    				
    				ArrayList<HistoryToken> tokens = new ArrayList<HistoryToken>();
    				HashMap<String, List<String>> selectedTags = getSelectedTags();
    				for(String privateTag : selectedTags.get(TagsUtils.PRIVATE)){
    					tokens.add(new HistoryToken(GlobalConstants.TOKEN_TAGS_PRIVATE, privateTag, true));
    				}
    				for(String publicTag : selectedTags.get(TagsUtils.PUBLIC)){
    					tokens.add(new HistoryToken(GlobalConstants.TOKEN_TAGS_PUBLIC, publicTag, true));
    				}
    				
    				TbitsURLManager.getInstance().addTokens(tokens);
    			}
    			
    		});
    		
            this.addButton(viewButton);
    		this.layout();
        }

	}

	private void addPublicTagsGrid(List<String> publicTags) {
		List<ColumnConfig> columnConfig = new ArrayList<ColumnConfig>();
		
		// Checkbox selection model column
		CheckBoxSelectionModel<TbitsModelData> sm = new CheckBoxSelectionModel<TbitsModelData>();
		sm.setSelectionMode(SelectionMode.MULTI);
		columnConfig.add(sm.getColumn());
		
		// Set the tags column
		ColumnConfig column = new ColumnConfig();
		column.setId("tag");
		column.setHeader("<b>Public Tags</b>");
		column.setResizable(false);
		column.setWidth(233);
		column.setSortable(false);
		columnConfig.add(column);
		
		// Add the tag names to the store
		final ListStore<TbitsModelData> store = new ListStore<TbitsModelData>();
		for(String tag : publicTags){
			TbitsModelData data = new TbitsModelData();
        	data.set("tag", tag);
        	store.add(data);
		}
		
		// Make the grid that contains all the tags and add it to the view panel
		publicTagGrid = new EditorGrid<TbitsModelData>(store, new ColumnModel(columnConfig));
		publicTagGrid.setBorders(true);
		publicTagGrid.setSelectionModel(sm);
		publicTagGrid.setAutoHeight(true);
		if(!publicTagGrid.isRendered())
			publicTagGrid.addPlugin(sm);
		this.add(publicTagGrid);
        this.layout();
        
	}

	private void addPrivateTagsGrid(List<String> tags) {
		
		List<ColumnConfig> columnConfig = new ArrayList<ColumnConfig>();
		
		// Checkbox selection model column
		CheckBoxSelectionModel<TbitsModelData> sm = new CheckBoxSelectionModel<TbitsModelData>();
		sm.setSelectionMode(SelectionMode.MULTI);
		columnConfig.add(sm.getColumn());
		
		// Set the tags column
		ColumnConfig column = new ColumnConfig();
		column.setId("tag");
		column.setHeader("<b>My Tags</b>");
		column.setResizable(false);
		column.setSortable(false);
		column.setWidth(233);
		TextField<String> textEditor = new TextField<String>();
		textEditor.setAllowBlank(false);
		// Make the tag field editor
		CellEditor tagEditor = new TbitsCellEditor(textEditor);
		tagEditor.setCancelOnEsc(true);
		tagEditor.setCompleteOnEnter(true);
		tagEditor.addListener(Events.BeforeComplete, new Listener<EditorEvent>() {
			public void handleEvent(EditorEvent ee) {
				if(((String)ee.getStartValue()).equals((String)ee.getValue()))
					return;
				TagsUtils.modifyTag((String)ee.getStartValue(), (String)ee.getValue(), ClientUtils.getCurrentUser());
			}
		});
		column.setEditor(tagEditor);
		columnConfig.add(column);
		
		// Add the tag names to the store
		final ListStore<TbitsModelData> store = new ListStore<TbitsModelData>();
		for(String tag : tags){
			TbitsModelData data = new TbitsModelData();
        	data.set("tag", tag);
        	store.add(data);
		}
		
		// Make the grid that contains all the tags and add it to the view panel
		privateTagGrid = new EditorGrid<TbitsModelData>(store, new ColumnModel(columnConfig));
		privateTagGrid.setBorders(true);
		privateTagGrid.setSelectionModel(sm);
		privateTagGrid.setAutoHeight(true);
		if(!privateTagGrid.isRendered())
			privateTagGrid.addPlugin(sm);
		this.add(privateTagGrid);
        this.layout();
	}
	
	public void disableButtons(){
		showButtons = false;
		if(viewButton != null){
			this.remove(viewButton);
			viewButton = null;
		}
		if(deleteButton != null){
			this.remove(deleteButton);
			deleteButton = null;
		}
	}

	//============================================================================================
	
}
