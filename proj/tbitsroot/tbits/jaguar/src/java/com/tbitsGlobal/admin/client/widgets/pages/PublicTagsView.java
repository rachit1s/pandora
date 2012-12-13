package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EditorEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.RefreshTagsList;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.tags.TagsUtils;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;


public class PublicTagsView extends APTabItem{

	private static PublicTagsView instance;
	protected TbitsObservable observable;
	private UserClient publicTagsUser;
	private ToolBar toolbar;
	private ContentPanel main;
	private EditorGrid<TbitsModelData> publicTagGrid;
	
	
	
	public PublicTagsView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setLayout(new FitLayout());
		this.setClosable(true);
		
		observable = new BaseTbitsObservable();
		observable.attach();
		
		publicTagsUser = new UserClient();
		publicTagsUser.setUserId(TagsUtils.PUBLIC_TAGS_USER);
	
		
		main = new ContentPanel(new FitLayout());
		main.setLayout(new FitLayout(){
			@Override
			protected void onLayout(Container<?> container, El target) {
				super.onLayout(container, target);
				int width = Math.max(800, container.getWidth());
				container.setWidth(width);
			}
		});
		/*ScrollPanel sp = new ScrollPanel();
		sp.setAlwaysShowScrollBars(true);*/
		main.setScrollMode(Scroll.AUTO);
	    main.setVScrollPosition(800);
		main.setHeaderVisible(false);
       /*view = new ContentPanel(new CenterLayout());
		view.setHeaderVisible(false);
		main.add(view);*/
		toolbar = new ToolBar();
		toolbar.setHeight(30);
		//toolbar.setAlignment(HorizontalAlignment.CENTER);
		toolbar.setAlignment(HorizontalAlignment.LEFT);
		addButtons();
		//main.setBottomComponent(toolbar);
		main.setTopComponent(toolbar);
		
		this.add(main);
		
		// Fetch the tags for the user and populate the panel
		TagsUtils.fetchTags(publicTagsUser);
		observable.subscribe(RefreshTagsList.class, new ITbitsEventHandle<RefreshTagsList>(){
			public void handleEvent(RefreshTagsList event) {
				initialise(event.getPublicTags());
			}});
	}

	private void initialise(List<String> publicTags) {
		
		if(publicTags == null)
			return;
		main.removeAll();
		
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
		column.setWidth(400);
		column.setSortable(false);
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
				TagsUtils.modifyTag((String)ee.getStartValue(), (String)ee.getValue(), publicTagsUser);
			}
		});
		column.setEditor(tagEditor);
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
		main.add(publicTagGrid);
        main.layout();
        toolbar.enable();
	}
	
	private void addButtons() {
		

		ToolBarButton button = new ToolBarButton("Create New Tag", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				
				MessageBox box = MessageBox.prompt("Enter New Tag Name", "Enter the new public tag name:");
				box.addCallback(new Listener<MessageBoxEvent>() {  
					public void handleEvent(MessageBoxEvent be) { 
						// Return if the "Cancel" button was clicked
						String clickedText = be.getButtonClicked().getText().toLowerCase();
						if(clickedText.equals(MessageBox.CANCEL))
							return;
						else{
							final String tag = be.getValue();
							TagsUtils.addTag(tag, publicTagsUser);
						}
					}  
				});
			}
			
		});
        toolbar.add(button);
	 button = new ToolBarButton("Delete", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				
				final ArrayList<String> selectedTags = new ArrayList<String>();
				List<TbitsModelData> selected = publicTagGrid.getSelectionModel().getSelectedItems();
				for(TbitsModelData data : selected){
					selectedTags.add((String) data.get("tag"));
				}
				
				if(selectedTags.size() == 0)
					return;
				else if(selectedTags.size() == 1){
					TagsUtils.deleteTags(publicTagsUser, selectedTags);
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
								TagsUtils.deleteTags(publicTagsUser, selectedTags);
							}
						}
	        		});  
				}
			}
			
		});
        toolbar.add(button);
        /*
        button = new ToolBarButton("Create", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				
				MessageBox box = MessageBox.prompt("Enter New Tag Name", "Enter the new public tag name:");
				box.addCallback(new Listener<MessageBoxEvent>() {  
					public void handleEvent(MessageBoxEvent be) { 
						// Return if the "Cancel" button was clicked
						String clickedText = be.getButtonClicked().getText().toLowerCase();
						if(clickedText.equals(MessageBox.CANCEL))
							return;
						else{
							final String tag = be.getValue();
							TagsUtils.addTag(tag, publicTagsUser);
						}
					}  
				});
			}
			
		});
        toolbar.add(button);*/
        
        toolbar.disable();
	}

}
