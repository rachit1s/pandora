package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.events.OnFieldAdd;
import com.tbitsGlobal.admin.client.events.OnFieldsDelete;
import com.tbitsGlobal.admin.client.events.OnFieldsUpdate;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import com.tbitsGlobal.admin.client.widgets.pages.bulk.TypeUserBulkGridPanel;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.domainObjects.DataTypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * Page to show Categories tab in the admin panel
 *
 */
public class CategoriesView extends APTabItem{
	
	private static final int BOOLEAN_NO = 0;
	private static final int BOOLEAN_YES = 1;
	
	private ComboBox<FieldClient> fieldCombo;
	private ListView<TypeClient> typeList;
	private TypeUserBulkGridPanel bulkGridPanel;
	
	public CategoriesView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setLayout(new FitLayout());
		
		observable.subscribe(OnChangeBA.class, new ITbitsEventHandle<OnChangeBA>(){
			public void handleEvent(OnChangeBA event) {
				getTypeFields();
			}
		});
		
		observable.subscribe(OnFieldAdd.class, new ITbitsEventHandle<OnFieldAdd>() {
			public void handleEvent(OnFieldAdd event) {
				FieldClient fieldClient = event.getField();
				if(fieldClient.getDataTypeId() == DataTypeClient.TYPE || fieldClient.getDataTypeId() == DataTypeClient.BOOLEAN)
					getTypeFields();
			}
		});
		
		observable.subscribe(OnFieldsDelete.class, new ITbitsEventHandle<OnFieldsDelete>() {
			public void handleEvent(OnFieldsDelete event) {
				getTypeFields();
			}
		});
		
		observable.subscribe(OnFieldsUpdate.class, new ITbitsEventHandle<OnFieldsUpdate>() {
			public void handleEvent(OnFieldsUpdate event) {
				getTypeFields();
			}
		});
	}
	
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		ContentPanel container = new ContentPanel();
		container.setLayout(new BorderLayout());
		container.setHeaderVisible(false);
		container.setBodyBorder(false);
		
		ToolBar topToolBar = new ToolBar();
		topToolBar.add(new Label("Select Field"));
		
		fieldCombo = new ComboBox<FieldClient>();
		fieldCombo.setEmptyText("Select a field");
		fieldCombo.setDisplayField(FieldClient.DISPLAY_NAME);
		fieldCombo.setStore(new ListStore<FieldClient>());
		fieldCombo.addSelectionChangedListener(new SelectionChangedListener<FieldClient>() {
			public void selectionChanged(SelectionChangedEvent<FieldClient> se){
				FieldClient currentField = se.getSelectedItem();
				if (currentField != null)
					getTypes(currentField);
			}});
		topToolBar.add(fieldCombo);
		
		container.setTopComponent(topToolBar);
		
		ContentPanel typesListContainer = new ContentPanel(new FitLayout());
		typesListContainer.setHeading("Types");
		typeList = new ListView<TypeClient>();
		typeList.setBorders(false);
		typeList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		typeList.setDisplayProperty(TypeClient.DISPLAY_NAME);
		typeList.setStore(new ListStore<TypeClient>());
		typeList.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<TypeClient>(){
			public void selectionChanged(SelectionChangedEvent<TypeClient> se) {
				TypeClient currentType = se.getSelectedItem();
				bulkGridPanel.setType(currentType);
				bulkGridPanel.refresh(0);
			}
		});
		typesListContainer.add(typeList, new FitData());
		
		StoreFilterField<TypeClient> typeListFilter = new StoreFilterField<TypeClient>() {
			protected void onBlur(ComponentEvent ce) {
			  }
			protected boolean doSelect(Store<TypeClient> store,TypeClient parent, 
					TypeClient record,String property, String filter) {
				String name =  record.getDisplayName().toString().toLowerCase();
				if(name.contains(filter.toLowerCase())) 
					return true;
				return false;
		}};
		typeListFilter.setFieldLabel("Filter");
		typeListFilter.setEmptyText("Search type");
		typeListFilter.bind(typeList.getStore());
		
		typesListContainer.setTopComponent(typeListFilter);
		
		container.add(typesListContainer, new BorderLayoutData(LayoutRegion.WEST, 300));
		
		bulkGridPanel = new TypeUserBulkGridPanel();
		container.add(bulkGridPanel, new BorderLayoutData(LayoutRegion.CENTER));
		
		this.add(container, new FitData());
		
		this.getTypeFields();
	}
	
	private void getTypeFields(){
		fieldCombo.getStore().removeAll();
		APConstants.apService.getFieldClients(ClientUtils.getSysPrefix(), new AsyncCallback<List<FieldClient>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not fetch Type fields... See logs for more details...", caught);
				Log.error("Could not fetch Type fields...", caught);
			}

			public void onSuccess(List<FieldClient> result) {
				if (result != null) {
					for (FieldClient fieldClient : result) {
						if(fieldClient.getDataTypeId() == DataTypeClient.TYPE || fieldClient.getDataTypeId() == DataTypeClient.BOOLEAN)
							fieldCombo.getStore().add(fieldClient);
					}
				}	
				if(fieldCombo.getStore().getAt(0) != null)
					fieldCombo.setValue(fieldCombo.getStore().getAt(0));
			}
		});
	}
	
	private void getTypes(FieldClient field){
		typeList.getStore().removeAll();
		if(field != null){
			if(field.getDataTypeId() == DataTypeClient.BOOLEAN){
				TypeClient yes = new TypeClient();
				yes.setFieldId(field.getFieldId());
				yes.setTypeId(BOOLEAN_YES);
				yes.setDisplayName("yes");
				yes.setName("yes");
				yes.setOrdering(1);
				
				TypeClient no = new TypeClient();
				no.setFieldId(field.getFieldId());
				no.setTypeId(BOOLEAN_NO);
				no.setDisplayName("no");
				no.setName("no");
				no.setOrdering(2);
				
				typeList.getStore().add(yes);
				typeList.getStore().add(no);
				typeList.getSelectionModel().select(typeList.getStore().getAt(0), false);
			}else{
				APConstants.apService.getTypeList(ClientUtils.getSysPrefix(),field.getName(),new AsyncCallback<ArrayList<TypeClient>>() {
						public void onFailure(Throwable caught) {
							Log.error("Error while loading type list... Please refresh!!!", caught);
							TbitsInfo.error("Error while loading type list... Please refresh!!!", caught);
						}
						public void onSuccess(ArrayList<TypeClient> result) {
							if (result != null){
								typeList.getStore().add(result);
								typeList.getSelectionModel().select(typeList.getStore().getAt(0), false);
							}
						}
					}
				);
			}
		}
	}
}
