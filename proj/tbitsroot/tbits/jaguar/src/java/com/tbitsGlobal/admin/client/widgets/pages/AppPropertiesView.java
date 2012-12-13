package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * The Class for AppsProperty
 * displays the property name and value in grid
 * Grid name = grid
 * saveproperties is the hashmap of current stored properties in the database 
 * 
 * @author naveen
 *
 */

public class AppPropertiesView extends APTabItem{
	private static final String APP_PROP_VALUE = "appPropValue";
	private static final String APP_PROP = "appProp";

	private EditorGrid<TbitsModelData> grid; 

	public AppPropertiesView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setLayout(new FitLayout());
	}

	public void onRender(Element parent, int pos){
		super.onRender(parent, pos);

		ContentPanel cp = new ContentPanel();
		cp.setLayout(new FitLayout());
		cp.setHeaderVisible(false);

		final ListStore<TbitsModelData> store = new ListStore<TbitsModelData>();
		
		CheckBoxSelectionModel<TbitsModelData> sm = new CheckBoxSelectionModel<TbitsModelData>();
		sm.setSelectionMode(SelectionMode.SINGLE);
		
		ColumnConfig appProp = new ColumnConfig(APP_PROP,"Property",500);
		ColumnConfig appPropValue = new ColumnConfig(APP_PROP_VALUE,"Property Value",500);

		TextField<String> propertyValue = new TextField<String>();
		propertyValue.setAllowBlank(false);
		appPropValue.setEditor(new TbitsCellEditor(propertyValue));

		List<ColumnConfig> clist = new ArrayList<ColumnConfig>();
		clist.add(sm.getColumn());
		clist.add(appProp);
		clist.add(appPropValue);
		final ColumnModel cm = new ColumnModel(clist);
		grid = new EditorGrid<TbitsModelData>(store,cm);
		grid.setSelectionModel(sm);
		grid.addPlugin(sm);
		cp.add(grid, new FitData());

		// filter for AppsProperty & AppsPropertValue
		StoreFilterField<TbitsModelData> filter = new StoreFilterField<TbitsModelData>(){
			@Override
			protected boolean doSelect(Store<TbitsModelData> store,TbitsModelData parent, TbitsModelData record,String property, String filter) {
				String name = record.get(APP_PROP);
				name = name.toLowerCase();
				String value = record.get(APP_PROP_VALUE);
				value= value.toLowerCase();
				if (name.contains(filter.toLowerCase()) || value.contains(filter.toLowerCase())) {  
					return true;  
				}
				return false;
			}
		};
		filter.bind(grid.getStore());
		filter.setEmptyText("Search Apps Property");

		LabelField filterLabel = new LabelField("Search:");
		ToolBar top = new ToolBar();
		top.add(filterLabel);
		top.add(filter);
		cp.setTopComponent(top);

		ToolBar tb = new ToolBar();
		ToolBarButton saveButton = new ToolBarButton("Save Settings", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				HashMap<String,String> saveProperties = new HashMap<String, String>();
				for(TbitsModelData temp : grid.getStore().getModels()){
					saveProperties.put((String)temp.get(APP_PROP),(String)temp.get(APP_PROP_VALUE));
				}
				APConstants.apService.updateAppProperties(saveProperties, new AsyncCallback<Boolean>(){
					public void onFailure(Throwable caught) {
						TbitsInfo.error("couldnot be saved ...Please Refresh ...", caught);
					}
					public void onSuccess(Boolean result) {
						TbitsInfo.info("successfully updated ...");
						loadData();
					}
				});		
			}
		});
		ToolBarButton revertButton = new ToolBarButton("Revert to Last Saved Setting", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				loadData();
			}
		});
		ToolBarButton addButton = new ToolBarButton("Add New Property", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {

				final Window db = new Window();
				db.setHeading("Add App Property "); 

				FormPanel formPanel = new FormPanel();
				FormLayout formLayout = new FormLayout();
				formLayout.setLabelSeparator("");
				formLayout.setLabelWidth(60);
				formPanel.setLayout(formLayout);  
				formPanel.setFrame(false);
				formPanel.setHeaderVisible(false);
				formPanel.setBodyBorder(false);

				final TextField<String> name = new TextField<String>();
				name.setFieldLabel("Name");
				name.setLabelSeparator("");

				final TextField<String> value = new TextField<String>();
				value.setFieldLabel("Value");
				value.setLabelSeparator("");

				Button submit = new Button("Submit");

				formPanel.add(name);
				formPanel.add(value);
				formPanel.add(submit);

				db.add(formPanel);
				db.setModal(true);
				db.show();

				submit.addSelectionListener(new SelectionListener<ButtonEvent>(){
					public void componentSelected(ButtonEvent ce) {
						if(name.getValue() != null && value.getValue() != null){
							if(grid.getStore().findModel(APP_PROP, name.getValue()) == null){
								APConstants.apService.insertAppProperties(name.getValue(), value.getValue(), new AsyncCallback<Boolean>(){
									public void onFailure(Throwable caught) {
										TbitsInfo.error("App Property cannot be added ... Please see the logs for details...", caught);
										Log.error("App Property cannot be added ... Please see the logs for details...", caught);
									}
									public void onSuccess(Boolean result) {
										if(result){
											TbitsModelData tb = new TbitsModelData();
											tb.set(APP_PROP, name.getValue());
											tb.set(APP_PROP_VALUE, value.getValue());
											grid.getStore().add(tb);
											
											TbitsInfo.info("App Property added successfully..");
										}
									}
								});
								db.hide();
							}else{
								com.google.gwt.user.client.Window.alert("The property : " + name.getValue() + " already exists..");
							}
						}else com.google.gwt.user.client.Window.alert("Name and Value can't be Left Empty ...");
					}
				});
			}
		});
		ToolBarButton deleteButton = new ToolBarButton("Delete Property", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				final TbitsModelData model = grid.getSelectionModel().getSelectedItem();
				if(model != null){
					if(com.google.gwt.user.client.Window.confirm("Are you sure you want to Delete this property?")){
						String name = model.get(APP_PROP);
						if(name != null){
							APConstants.apService.deleteAppProperties(name, new AsyncCallback<Boolean>(){
								public void onFailure(Throwable caught) {
									TbitsInfo.error("App Property cannot be added ... Please Refresh ...", caught);
									Log.error("App Property cannot be added ... Please Refresh ...", caught);
								}
								public void onSuccess(Boolean result) {
									if(result){
										grid.getStore().remove(model);
									}
								}
							});
						}
					}
				}else TbitsInfo.info("Please Select a App Property to Delete ...");
			}
		});

		tb.add(saveButton);
		tb.add(revertButton);
		tb.add(addButton);
		tb.add(deleteButton);
		cp.setBottomComponent(tb);

		this.add(cp);
		
		loadData();
	}

	// Function to gets the AppsProperty from Database & add it to Grid
	private void loadData(){
		grid.getStore().removeAll();
		APConstants.apService.getAppProperties(new AsyncCallback<HashMap<String,String>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error in getting appproperties ... Please Refresh ...", caught);
				Log.error("Error in getting appproperties ... Please Refresh ...", caught);
			}
			public void onSuccess(HashMap<String, String> result) {
				if(result != null){
					for(String s : result.keySet()){
						if(s.equalsIgnoreCase("transbit.database.password"))
							continue;
						TbitsModelData field = new TbitsModelData();
						field.set(APP_PROP, s);
						field.set(APP_PROP_VALUE, result.get(s));
						grid.getStore().add(field);
					}
				}
			}
		});
	}

}

