package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.HashMap;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.state.AppState;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * The class for Captions
 * @param store contains the store for grid containing captions name and value
 * @param defaultstore contains the store for defaultcaptions
 * 
 * @author naveen
 *
 */

public class CaptionsView extends APTabItem {

	private static String CAPTION_NAME = "name";
	private static String CAPTION_VALUE = "value";
	private static String TAB_NAME = "Captions";
	private static String DEFAULT_TAB_NAME = "Default Captions";
	private HashMap<String,String> saveCaptions = new HashMap<String,String>();
	private HashMap<String,String> defaultCaptions = new HashMap<String,String>();
	private EditorGrid<TbitsModelData>grid; 
	private final ListStore<TbitsModelData>store = new ListStore<TbitsModelData>();
	private final ListStore<TbitsModelData>defaultstore = new ListStore<TbitsModelData>();
	private TabItem defcaptions = new TabItem();
	private TbitsModelData selected = new TbitsModelData();
	private boolean defaultopen = false;

	TabPanel tabPanel = new TabPanel();

	public CaptionsView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setLayout(new FitLayout());
		this.setClosable(true);

		observable.subscribe(OnChangeBA.class, new ITbitsEventHandle<OnChangeBA>() {
			public void handleEvent(OnChangeBA event) {
				store.removeAll();
				loadData();
			}
		});

		defcaptions.addListener(Events.Close, new Listener<TabPanelEvent>() {
			public void handleEvent(TabPanelEvent be) {
				defaultopen = false;
			}
		});
	}

	public void onRender(Element parent,int pos){
		super.onRender(parent,pos);

		TabItem current =  new TabItem();
		current.setText(TAB_NAME);
		ContentPanel cp = new ContentPanel(new FitLayout());
		cp.setHeaderVisible(false);
		ColumnConfig captionsField = new ColumnConfig(CAPTION_NAME,"Caption",500);
		ColumnConfig captionsData = new ColumnConfig(CAPTION_VALUE,"Caption Value",500);
		TextField<String> captionValue = new TextField<String>();
		captionValue.setAllowBlank(false);
		CellEditor c = new TbitsCellEditor(captionValue);
		captionsData.setEditor(c);
		ArrayList<ColumnConfig> clist = new ArrayList<ColumnConfig>();
		clist.add(captionsField);
		clist.add(captionsData);
		final ColumnModel cm = new ColumnModel(clist);
		grid=new EditorGrid<TbitsModelData>(store,cm);
		ToolBar tb = new ToolBar();
		tb.setHeight(25);
		ToolBarButton defaultButton = new ToolBarButton("Default Captions");
		ToolBarButton saveButton = new ToolBarButton("Save Settings");
		ToolBarButton revertButton = new ToolBarButton("Revert to Last Saved Setting");
		ToolBarButton addButton = new ToolBarButton("Add a new caption");
		final ToolBarButton deleteButton = new ToolBarButton("Delete This Caption");

		deleteButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				Listener<MessageBoxEvent> l = new Listener<MessageBoxEvent>(){
					public void handleEvent(MessageBoxEvent be) {
						Button b = be.getButtonClicked();
						if(b.getText().endsWith("Yes")){

							if(selected != null){
								APConstants.apService.deletecaption((String)selected.get(CAPTION_NAME),(String)selected.get(CAPTION_VALUE), ClientUtils.getCurrentBA().getSystemId(), new AsyncCallback<Boolean>(){
									public void onFailure(Throwable caught) {
										TbitsInfo.error("Error while deleting", caught);
									}
									public void onSuccess(Boolean result) {
										TbitsInfo.info("Caption has beeen deleted successfully");
										loadData();
										deleteButton.disable();
									}
								});
							}
						}
					}
				};
				MessageBox.confirm("Confirm", "Are you sure you want to Delete ?",l);
			}
		});

		tb.add(defaultButton);
		tb.add(saveButton);
		tb.add(revertButton);
		tb.add(addButton);
		tb.add(deleteButton);
		deleteButton.disable();
		cp.setBottomComponent(tb);

		// filter for AppsProperty & AppsPropertValue
		StoreFilterField<TbitsModelData> filter = new StoreFilterField<TbitsModelData>(){
			@Override
			protected boolean doSelect(Store<TbitsModelData> store,TbitsModelData parent, TbitsModelData record,String property, String filter) {
				String name = record.get(CAPTION_NAME);
				name = name.toLowerCase();
				String value = record.get(CAPTION_VALUE);
				value= value.toLowerCase();
				if (name.contains(filter.toLowerCase()) || value.contains(filter.toLowerCase())) {  
					return true;  
				}
				return false;
			}
		};
		filter.bind(grid.getStore());
		filter.setEmptyText("Search Captions");

		LabelField filterLabel = new LabelField("Search:");
		ToolBar top = new ToolBar();
		top.add(filterLabel);
		top.add(filter);
		cp.setTopComponent(top);
		
		grid.addListener(Events.CellClick,new Listener<GridEvent<TbitsModelData>>(){
			public void handleEvent(final GridEvent<TbitsModelData> be) {
				selected = be.getModel();
				if(be.getModel().get("deleted").equals(true))
					deleteButton.enable();
				else deleteButton.disable();
			}
		});

		loadData();
		cp.add(grid);
		current.add(cp);
		current.setLayout(new FitLayout());
		tabPanel.add(current);
		add(tabPanel);
		saveButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				ListStore<TbitsModelData> updateStore = new ListStore<TbitsModelData>();
				updateStore.removeAll();
				updateStore = grid.getStore();
				saveCaptions.clear();
				for(TbitsModelData temp : updateStore.getModels()){
					saveCaptions.put((String)temp.get(CAPTION_NAME),(String)temp.get(CAPTION_VALUE));
				}
				APConstants.apService.updateCaptions(saveCaptions, ClientUtils.getCurrentBA().getSystemId(),new AsyncCallback<Boolean>(){
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Captions could not be updated........!!!!!",caught);
					}
					public void onSuccess(Boolean result) {
						if(result == true)
							TbitsInfo.info("successfully updated");
						loadData();
					}
				});
			}
		});

		revertButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				store.removeAll();
				for(String s : defaultCaptions.keySet()){
					if(saveCaptions.containsKey(s))continue;
					else{	
						TbitsModelData temp = new TbitsModelData();
						temp.set(CAPTION_NAME,s);
						temp.set(CAPTION_VALUE,defaultCaptions.get(s));
						store.add(temp);
					}
				}
				for(String s : saveCaptions.keySet()){
					TbitsModelData temp = new TbitsModelData();
					temp.set(CAPTION_NAME,s);
					temp.set(CAPTION_VALUE,saveCaptions.get(s));
					store.add(temp);
				}
			}
		});

		addButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				final Window addcaption = new Window();
				addcaption.setHeading("Add Caption");

				FormPanel formPanel = new FormPanel();
				FormLayout formLayout = new FormLayout();
				formLayout.setLabelSeparator("");
				formLayout.setLabelWidth(60);
				formPanel.setLayout(formLayout);  
				formPanel.setFrame(false);
				formPanel.setHeaderVisible(false);
				formPanel.setBodyBorder(false);

				HorizontalPanel buttonPanel = new HorizontalPanel();

				final TextField<String> name = new TextField<String>();
				name.setFieldLabel("Caption Name");
				final TextField<String> value = new TextField<String>();
				value.setFieldLabel("Caption Value");

				Button submit = new Button("Submit");
				Button cancel = new Button("Cancel");
				buttonPanel.add(submit);
				buttonPanel.add(cancel);

				formPanel.add(name);
				formPanel.add(value);
				formPanel.add(buttonPanel);
				addcaption.add(formPanel);

				addcaption.setModal(true);
				addcaption.show();

				submit.addSelectionListener(new SelectionListener<ButtonEvent>(){
					public void componentSelected(ButtonEvent ce) {
						if(name.getValue() != null && value.getValue() != null){
							HashMap<String,String>map = new HashMap<String, String>();
							map.put(CaptionsView.CAPTION_NAME,name.getValue());
							map.put(CaptionsView.CAPTION_VALUE,value.getValue());
							APConstants.apService.addCaptions(map,ClientUtils.getCurrentBA().getSystemId(),new AsyncCallback<Boolean>(){
								public void onFailure(Throwable caught) {
									TbitsInfo.error("Error while adding the new caption try again.....!!!!",caught);
									addcaption.hide();
									addcaption.setModal(false);
								}
								public void onSuccess(Boolean result) {
									TbitsInfo.info("Caption was added successfully");
									addcaption.hide();
									addcaption.setModal(false);
									loadData();
								}
							});
						}
						else {
							TbitsInfo.error("Caption cannot be empty try again.....!!!!");
							addcaption.hide();
							addcaption.setModal(false);
						}	
					}
				});

				cancel.addSelectionListener(new SelectionListener<ButtonEvent>(){
					public void componentSelected(ButtonEvent ce) {
						addcaption.hide();
						addcaption.setModal(false);
					}
				});
			}
		});

		// to open the Default Caption Tab 
		defaultButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				if(!defaultopen){
					defaultopen = true;
					defcaptions.setText(DEFAULT_TAB_NAME);
					defcaptions.setClosable(true);
					ContentPanel cp = new ContentPanel();
					cp.setHeaderVisible(false);
					cp.setLayout(new FitLayout());
					ColumnConfig captionsField = new ColumnConfig(CAPTION_NAME,"Caption",500);
					ColumnConfig captionsData = new ColumnConfig(CAPTION_VALUE,"Caption Value",500);
					TextField<String> captionValue = new TextField<String>();
					captionValue.setAllowBlank(false);
					CellEditor c = new TbitsCellEditor(captionValue);
					captionsData.setEditor(c);
					ArrayList<ColumnConfig> clist = new ArrayList<ColumnConfig>();
					clist.add(captionsField);
					clist.add(captionsData);
					final ColumnModel cm = new ColumnModel(clist);
					EditorGrid<TbitsModelData>defaultgrid=new EditorGrid<TbitsModelData>(defaultstore,cm);
					cp.add(defaultgrid);
					ToolBar bottom = new ToolBar();
					ToolBarButton saveDefault = new ToolBarButton("Save");
					bottom.add(saveDefault);
					bottom.setHeight(25);
					cp.setBottomComponent(bottom);
					
					// filter for AppsProperty & AppsPropertValue
					StoreFilterField<TbitsModelData> filter = new StoreFilterField<TbitsModelData>(){
						@Override
						protected boolean doSelect(Store<TbitsModelData> store,TbitsModelData parent, TbitsModelData record,String property, String filter) {
							String name = record.get(CAPTION_NAME);
							name = name.toLowerCase();
							String value = record.get(CAPTION_VALUE);
							value= value.toLowerCase();
							if (name.contains(filter.toLowerCase()) || value.contains(filter.toLowerCase())) {  
								return true;  
							}
							return false;
						}
					};
					filter.bind(defaultgrid.getStore());
					filter.setEmptyText("Search Captions");

					LabelField filterLabel = new LabelField("Search:");
					ToolBar top = new ToolBar();
					top.add(filterLabel);
					top.add(filter);
					cp.setTopComponent(top);
					
					defcaptions.add(cp);
					defcaptions.setLayout(new FitLayout());
					tabPanel.add(defcaptions);
					tabPanel.setSelection(defcaptions);
					saveDefault.addSelectionListener(new SelectionListener<ButtonEvent>(){

						@SuppressWarnings("unchecked")
						@Override
						public void componentSelected(ButtonEvent ce) {
							HashMap map = new HashMap<String, String>();
							for(TbitsModelData t : defaultstore.getModels()){
								map.put(t.get(CAPTION_NAME),t.get(CAPTION_VALUE));
							}
							APConstants.apService.updateDefaultCaptions(map, new AsyncCallback<Boolean>(){

								public void onFailure(Throwable caught) {
									TbitsInfo.error("Error while updating the default captions",caught);
								}

								public void onSuccess(Boolean result) {
									loadData();
									TbitsInfo.info("Successfully updated");
								}
							});
						}
					});
					ToolBarButton revert = new ToolBarButton("Revert");
					bottom.add(revert);
					revert.addSelectionListener(new SelectionListener<ButtonEvent>(){
						public void componentSelected(ButtonEvent ce) {
							loadData();
						}
					});
				}
				else tabPanel.setSelection(defcaptions);
			}
		});
	}

	// The Function gets The BA Captions from Database
	private void loadData(){
		if (!AppState.checkAppStateIsTill(AppState.BAChanged)) {
			TbitsInfo.info("Business Area not loaded");
			return;
		}
		int k = 0;

		//getting the default captions values(sys_id = 0)
		APConstants.apService.getAllBACaptionsbySysId(k,new AsyncCallback<HashMap<String,String>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Unable to load default captions",caught);
			}
			public void onSuccess(HashMap<String, String> result) {
				defaultCaptions.clear();
				defaultstore.removeAll();
				for(String s : result.keySet()){
					defaultCaptions.put(s,result.get(s));
					TbitsModelData temp = new TbitsModelData();
					temp.set(CAPTION_NAME, s);
					temp.set(CAPTION_VALUE, result.get(s));
					defaultstore.add(temp);
				}

				// Getting the BA Caption
				APConstants.apService.getAllBACaptionsbySysId(ClientUtils.getCurrentBA().getSystemId(),new AsyncCallback<HashMap<String,String>>(){
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Unable toload captions",caught);
					}
					public void onSuccess(HashMap<String, String> result) {
						saveCaptions.clear();
						if(null != result){
							for(String s : result.keySet()){
								saveCaptions.put(s,result.get(s));
							}
						}
						store.removeAll();
						for(String s : defaultCaptions.keySet()){
							if(saveCaptions.containsKey(s))continue;
							else{	
								TbitsModelData temp = new TbitsModelData();
								temp.set(CAPTION_NAME,s);
								temp.set(CAPTION_VALUE,defaultCaptions.get(s));
								temp.set("deleted",false);
								store.add(temp);
							}
						}
						for(String s : saveCaptions.keySet()){
							TbitsModelData temp = new TbitsModelData();
							temp.set(CAPTION_NAME,s);
							temp.set(CAPTION_VALUE,saveCaptions.get(s));
							temp.set("deleted",true);
							store.add(temp);
						}
					}
				});
			}
		});
	}
}


