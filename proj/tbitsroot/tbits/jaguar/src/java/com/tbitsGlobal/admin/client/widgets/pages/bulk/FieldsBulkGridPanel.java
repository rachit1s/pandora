package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.List;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;
import com.tbitsGlobal.admin.client.events.OnFieldAdd;
import com.tbitsGlobal.admin.client.events.OnFieldsUpdate;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.widgets.pages.UsersPage;
import com.tbitsGlobal.admin.client.widgets.pages.bulk.UserBulkGridPanel.UserFilterColumn;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportWindow;
import commons.com.tbitsGlobal.utils.client.domainObjects.DataTypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

public class FieldsBulkGridPanel extends AbstractAdminBulkUpdatePanel<FieldClient>{

	public FieldsBulkGridPanel() {
		super();
		
		this.setHeaderVisible(false);
		this.setAnimCollapse(true);
		
		canCopyPasteRows = false;
		canReorderRows = false;
		canDeleteRows = false;
		isExcelImportSupported=false;
	}
	
	protected void beforeRender() {
		
		
		StoreFilterField<FieldClient> a = new StoreFilterField<FieldClient>() {
			protected boolean doSelect(Store<FieldClient> store,
					FieldClient parent, FieldClient record, String property,
					String filter) {
				if (filter == null || filter.equals(""))
					return false;
				if (record.getName()!= null
						&& record.getName().toLowerCase().contains(
								filter.toLowerCase()))
					return true;
				return false;
			}

		
		};
	
		
		a.bind(singleGridContainer.getBulkGrid().getStore());
		toolbar.add(a);
		super.beforeRender();

		toolbar.add(new SeparatorToolItem());
		toolbar.add(new Html("<b>Search : </b>"));
		addSearcherToToolbar();

		
	}
	
	@Override
	public void refresh(int page) {
		singleGridContainer.removeAllModels();
		TbitsInfo.info("Fetching Fields from database... Please Wait...");
		APConstants.apService.getFieldClients(ClientUtils.getCurrentBA().getSystemPrefix(), new AsyncCallback<List<FieldClient>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Unable to load fields...", caught);
				Log.error("Unable to load fields...", caught);
			}
			
			public void onSuccess(List<FieldClient> result) {
				if(result != null){
					singleGridContainer.addModel(result);
					singleGridContainer.getBulkGrid().getStore().sort(FieldClient.FIELD_ID, SortDir.ASC);
				}
			}});
	}

	@Override
	protected void onSave(final List<FieldClient> models, Button btn) {
		TbitsInfo.info("Updating Fields in database... Please Wait...");
		APConstants.apService.updateFields(ClientUtils.getSysPrefix(), models,new AsyncCallback<List<FieldClient>>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Failed to update fields... Please see log for details..", caught);
				Log.error("Failed to update fields... Please see log for details..", caught);
			}

			public void onSuccess(List<FieldClient> result) {
				if(result != null){
					TbitsInfo.info("Fields updated succesfully");
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
					TbitsEventRegister.getInstance().fireEvent(new OnFieldsUpdate(result));
				}
			}
		});
	}

	@Override
	public FieldClient getEmptyModel() {
		return new FieldClient();
	}

	@Override
	protected BulkUpdateGridAbstract<FieldClient> getNewBulkGrid(BulkGridMode mode) {
		return new FieldsBulkUpdateGrid(mode);
	}
	
	@Override
	protected void onAdd() {
		final Window w = new Window();
		w.setHeading("Add Extended Field");
		w.setModal(true);
		w.setClosable(true);
		
		FormPanel fp = new FormPanel();
		FormLayout formLayout = new FormLayout();
		formLayout.setLabelWidth(60);
		fp.setLayout(formLayout);
		fp.setHeaderVisible(false);
		fp.setBodyBorder(false);

		final TextField<String> name = new TextField<String>();
		name.setFieldLabel("Name");
		fp.add(name);

		final ComboBox<TbitsModelData> dataTypeCombo = new ComboBox<TbitsModelData>();
		dataTypeCombo.setDisplayField(DataTypeClient.DATA_TYPE);
		dataTypeCombo.setFieldLabel("Data type");
		dataTypeCombo.setEditable(false);
		
		ListStore<TbitsModelData> dataTypeStore = new ListStore<TbitsModelData>();
		for (int key : DataTypeClient.getDataTypeMap().keySet()) {
			TbitsModelData m = new TbitsModelData();
			m.set(DataTypeClient.DATA_TYPE, DataTypeClient.getDataTypeMap().get(key));
			m.set(DataTypeClient.DATA_TYPE_ID, key);
			dataTypeStore.add(m);
		}
		dataTypeCombo.setStore(dataTypeStore);
		
		dataTypeCombo.setValue(dataTypeCombo.getStore().getAt(0));
		
		fp.add(dataTypeCombo);

		Button submit = new Button("Submit",new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if(dataTypeCombo.getValue() == null || name.getValue() == null)
					return;
				
				FieldClient model = singleGridContainer.getBulkGrid().getStore().findModel(FieldClient.NAME, name.getValue());
				if(model != null){
					TbitsInfo.warn("cannot add field maybe because it already exists");
					return;
				}
				
				int dataTypeId = (Integer)dataTypeCombo.getValue().get(DataTypeClient.DATA_TYPE_ID);

				APConstants.apService.createNewField(ClientUtils.getSysPrefix(), name.getValue(), dataTypeId,new AsyncCallback<FieldClient>() {
					public void onFailure(Throwable caught) {
						TbitsInfo.error("cannot add field", caught);
					}
					public void onSuccess(FieldClient result) {
						if (result == null)
							TbitsInfo.error("cannot add field");
						else{
							w.hide();
							TbitsInfo.info("Added field successfully");
							singleGridContainer.getBulkGrid().getStore().add(result);
							
							TbitsEventRegister.getInstance().fireEvent(new OnFieldAdd(result));
						}
					}
				});
			}	
		});

		fp.addButton(submit);
		w.setFocusWidget(name);
		w.add(fp);

		w.show();
	}
	
	@Override
	protected ExcelImportWindow<FieldClient> onImport() {
		ExcelImportWindow<FieldClient> window = super.onImport();
		window.setDefaultUniqueMatchingProperty(FieldClient.NAME);
		
		return window;
	}
	

	private void addSearcherToToolbar() {
		final ComboBox<FieldFilterColumn> combo = new ComboBox<FieldFilterColumn>();
		ListStore<FieldFilterColumn> store = new ListStore<FieldFilterColumn>();
		
		FieldFilterColumn defaultVal = new FieldFilterColumn("Name","name");
		store.add(defaultVal);
		store.add(new FieldFilterColumn("Display Name", "display_name"));
		store.add(new FieldFilterColumn("Description", "description"));
		combo.setStore(store);
		combo.setDisplayField("displayName");
		combo.setValue(defaultVal);

		final TextField<String> searchParam = new TextField<String>();

		combo.addKeyListener(new KeyListener() {
			public void componentKeyUp(ComponentEvent event) {
				if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
					searchField(combo.getSelection().get(0).getColumnName(),
							searchParam.getValue());
				}
			}
		});
		toolbar.add(combo);

		searchParam.addKeyListener(new KeyListener() {
			public void componentKeyUp(ComponentEvent event) {
				if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
					searchField(combo.getSelection().get(0).getColumnName(),
							searchParam.getValue());
				}
			}
		});
		toolbar.add(searchParam);

		ToolBarButton search = new ToolBarButton("Search",
				new SelectionListener<ButtonEvent>() {

					public void componentSelected(ButtonEvent ce) {
						searchField(combo.getSelection().get(0).getColumnName(),
								searchParam.getValue());
					}
				});
		toolbar.add(search);
	}
	
	private void searchField(String filter, String fieldSearchParam) {

		if (fieldSearchParam == null || fieldSearchParam.equals(""))
			return;

		TbitsInfo.info("Fetching queried users. Please wait.");
		Log.info("Fetching queried users. Please wait.");

		APConstants.apService.fetchQueriedFields(filter, fieldSearchParam,ClientUtils.getCurrentBA().getSystemId(),
				new AsyncCallback <List<FieldClient>>() {

					public void onFailure(Throwable caught) {
						TbitsInfo.error("Could not retrieve queried Users.",
								caught);
						Log.error("Could not retrieve queried Users.", caught);
					}

					public void onSuccess(List<FieldClient> result) {
						Log.info("Received queried users.");
						if (result != null) {
							singleGridContainer.removeAllModels();
							singleGridContainer.addModel(result);
						//	pagingBar.adjustButtons(1, result.getTotalUsers());
						//	singleGridContainer.getBulkGrid().getStore().sort(UserClient.USER_LOGIN, SortDir.ASC);
						}
						Log.info("Finished rendering queried users.");
					}
				});
	}
	
	
	
	/**
	 * Internal class to depict the user filters
	 * 
	 * @author karan
	 */
	public class FieldFilterColumn extends TbitsModelData {

		public FieldFilterColumn(String name, String column) {
			this.set("displayName", name);
			this.set("columnName", column);
		}

		public String getDisplayName() {
			return this.get("displayName");
		}

		public String getColumnName() {
			return this.get("columnName");
		}
	}

}
