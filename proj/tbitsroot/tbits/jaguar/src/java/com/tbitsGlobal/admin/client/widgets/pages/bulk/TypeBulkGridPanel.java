package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportWindow;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;

public class TypeBulkGridPanel extends AbstractAdminBulkUpdatePanel<TypeClient>{

	private FieldClient field;
	private ComboBox<TypeClient> defaultTypeCombo;
	
	private TypeBulkGridPanel() {
		super();
		
		this.setHeaderVisible(false);
		this.setAnimCollapse(true);
		
		canCopyPasteRows = false;
		
		this.enablePaging(20);
	}
	
	public TypeBulkGridPanel(FieldClient field) {
		this();
		
		this.field = field;
	}
	
	@Override
	protected void beforeRender() {
		super.beforeRender();
		
		defaultTypeCombo = new ComboBox<TypeClient>();
		defaultTypeCombo.setStore(singleGridContainer.getBulkGrid().getStore());
		defaultTypeCombo.setDisplayField(TypeClient.DISPLAY_NAME);
		
		defaultTypeCombo.addSelectionChangedListener(new SelectionChangedListener<TypeClient>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<TypeClient> se) {
				TypeClient selectedModel = se.getSelectedItem();
				
				List<TypeClient> models = singleGridContainer.getModels();
				for(TypeClient model : models){
					model.setIsDefault(false);
				}
				
				selectedModel.setIsDefault(true);
			}
		});
		
		toolbar.add(new SeparatorToolItem());
		toolbar.add(new LabelToolItem(" Default Type : "));
		toolbar.add(defaultTypeCombo);
	}
	
	@Override
	public TypeClient getEmptyModel() {
		return new TypeClient();
	}

	public void save(){
		List<TypeClient> models = singleGridContainer.getModels();
		onSave(models, null);
	}

	@Override
	protected void onSave(List<TypeClient> models, Button btn) {
		TbitsInfo.info("Updating Types into database... Please Wait...");
		APConstants.apService.updateTypes(ClientUtils.getSysPrefix(),field.getName(), models
				, new AsyncCallback<List<TypeClient>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error in Updating Types .. Please refresh ...", caught);
				Log.error("Error in Updating Types .. Please refresh ...", caught);
			}

			public void onSuccess(List<TypeClient> result) {
				if(result != null){
					TbitsInfo.info("Types updated successfully");
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
				}
			}
		});	
	}

	public void refresh(final int page) {
		singleGridContainer.removeAllModels();
		if(field != null){
			TbitsInfo.info("Fetching Types from database... Please Wait...");
			APConstants.apService.getTypeList(ClientUtils.getSysPrefix(), field.getName(), 
					new AsyncCallback<ArrayList<TypeClient>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error in Retrieving Types .. Please refresh ...", caught);
					Log.error("Error in Retrieving Types .. Please refresh ...", caught);
				}
				public void onSuccess(ArrayList<TypeClient> result) {
					if(result != null){
						int pageSize = getPageSize();
						if(page > 0 && pageSize > 0 && pagingBar != null){
							List<TypeClient> models = ClientUtils.sort(result, pageSize, page, true);
							singleGridContainer.addModel(models);
							pagingBar.adjustButtons(page, result.size());
						}else{
							List<TypeClient> models = ClientUtils.sort(result, -1, -1, true);
							singleGridContainer.addModel(models);
						}
						
						for(TypeClient type : defaultTypeCombo.getStore().getModels()){
							if(type.getIsDefault()){
								defaultTypeCombo.setValue(type);
								break;
							}
						}
					}
				}
			});
		}
	}

	public void setField(FieldClient field) {
		this.field = field;
	}

	public FieldClient getField() {
		return field;
	}
	
	@Override
	protected void onAdd() {
		final Window w = new Window();
		w.setHeading("Create a new Type");
		w.setModal(true);
		w.setClosable(true);
		
		FormPanel fp = new FormPanel();
		FormLayout formLayout = new FormLayout();
		formLayout.setLabelSeparator("");
		formLayout.setLabelWidth(60);
		fp.setLayout(formLayout);
		fp.setFrame(false);
		fp.setHeaderVisible(false);
		fp.setBodyBorder(false);

		final TextField<String> name = new TextField<String>();
		name.setFieldLabel("Name");
		fp.add(name);
		
		w.add(fp);

		Button submit = new Button("Submit",new SelectionListener<ButtonEvent>() {
		
			public void componentSelected(ButtonEvent ce) {
				TbitsInfo.info("Adding the type ... Please Wait ...");
				if( name.getValue() == null)
					return;
				
				APConstants.apService.createNewType(ClientUtils.getSysPrefix(), field.getName(), field.getFieldId(),name.getValue(),new AsyncCallback<TypeClient>() {
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Error in Creating Type .. Please refresh ...", caught);
						Log.error("Error in Creating Type .. Please refresh ...", caught);
					}
					public void onSuccess(TypeClient result) {
						if(result != null){
							TbitsInfo.info("Type Successfully added");
							singleGridContainer.addModel(result);
							w.hide();
						}else
							TbitsInfo.error("Type not Added ... Try Again ...");
					}
				});
			}	
		});

		w.addButton(submit);
		w.setFocusWidget(name);
		
		w.show();
	}
	
	@Override
	protected void onRemove() {
		final List<TypeClient> selectedItems = singleGridContainer.getSelectedModels();
		if(selectedItems != null){
			if(selectedItems.size() > 0 && 
					com.google.gwt.user.client.Window.confirm("Do you want to delete " + selectedItems.size() + " selected Types?")){
				APConstants.apService.deleteTypes(ClientUtils.getSysPrefix(),field.getName(), selectedItems, new AsyncCallback<Boolean>(){
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Unable to delete type", caught);
						Log.error("Unable to delete type", caught);
					}
					public void onSuccess(Boolean result) {
						if(result){
							TbitsInfo.info("Type deleted");
							for(final TypeClient model : selectedItems){
								singleGridContainer.getBulkGrid().getStore().remove(model);
							}
						}
					}
				});
			}
				
		}
	}

	@Override
	protected BulkUpdateGridAbstract<TypeClient> getNewBulkGrid(BulkGridMode mode) {
		return new TypeBulkGrid(mode);
	}

	@Override
	protected ExcelImportWindow<TypeClient> onImport() {
		ExcelImportWindow<TypeClient> window = super.onImport();
		window.setDefaultUniqueMatchingProperty(TypeClient.NAME);
		
		return window;
	}
}
