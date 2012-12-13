package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;
import com.tbitsGlobal.admin.client.modelData.EscalationHierarchiesClient;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.log.Log;



/**
 * @author Nirmal Agrawal
 *
 */

public class EscalationHierarchyDetailsView extends AbstractAdminBulkUpdatePanel<EscalationHierarchiesClient> {

	public  EscalationHierarchyDetailsView()
	{
		super();
		commonGridDisabled = true;
		isExcelImportSupported = false;
		canAddRows			= true;
		canReorderRows		= false;
		canCopyPasteRows	= false;
		
		refresh(0);
		
		
		
	}

	@Override
	protected void onSave(List<EscalationHierarchiesClient> models,final Button btn) {
		
		if(models != null){
			btn.setText("Saving... Please Wait...");
			btn.disable();
			
			TbitsInfo.info("Saving... Please Wait...");
			APConstants.apService.updateEscalationHierarchies(models, new AsyncCallback<List<EscalationHierarchiesClient>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error in saving escalation hierarchies values  : "+ caught.getMessage(), caught);
					Log.error("Error in saving escalation hierarchies values : ", caught);
					btn.setText("Save");
					btn.enable();
				}
	
				public void onSuccess(List<EscalationHierarchiesClient> result) {
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
					btn.setText("Save");
					btn.enable();
					refresh(0);
				}});
		
		}
		
	}

	@Override
	public EscalationHierarchiesClient getEmptyModel() {
		
				return new EscalationHierarchiesClient();
	}

	@Override
	protected BulkUpdateGridAbstract<EscalationHierarchiesClient> getNewBulkGrid(
			BulkGridMode mode) {
		
		return new EscalationHierarchyDetailGrid(mode);
	}

	@Override
	public void refresh(int page) {
		
		APConstants.apService.getEscalationHierarchies(new AsyncCallback<List<EscalationHierarchiesClient>>() {

			@Override
			public void onFailure(Throwable caught) {

				TbitsInfo.error(
						"Error in fetching escalations hierarchies",
						caught);
				Log.error("Error in fetching escalations hierarchies",
						caught);

			}

			@Override
			public void onSuccess(List<EscalationHierarchiesClient> result) {
				if (result != null) {
					
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
				}

			}
		});
		
	}
	
	@Override
	protected void onAdd() {
		
		final Window addEscWin=new Window();
		addEscWin.setLayout(new FormLayout());
		FormData formData = new FormData("-20");
		
		final TextField<String> escHierarNameField=new TextField<String>();
		escHierarNameField.setFieldLabel("NAME:");
		addEscWin.add(escHierarNameField,formData);
		
		final TextField<String> escHierarDisNameField=new TextField<String>();
		escHierarDisNameField.setFieldLabel("DISPALY NAME:");
		addEscWin.add(escHierarDisNameField,formData);
		
		final TextField<String> escHierarDesField=new TextField<String>();
		escHierarDesField.setFieldLabel("DESCRIPTION:");
		addEscWin.add(escHierarDesField,formData);
		
		addEscWin.addButton(new Button("save",
				new SelectionListener<ButtonEvent>() {

					@Override
					public void componentSelected(ButtonEvent ce) {

						final EscalationHierarchiesClient hierarchy = new EscalationHierarchiesClient();
						if (escHierarNameField.getValue() == null
								|| escHierarNameField.getValue().trim() == "") {
							com.google.gwt.user.client.Window
									.alert("escaltion hierarchy name can not be empty...");
							escHierarNameField.focus();
							return;
						}
						
						if(escHierarDisNameField.getValue()==null || escHierarDisNameField.getValue()=="")
							escHierarDisNameField.setValue(escHierarNameField.getValue());
						hierarchy.setName(escHierarNameField.getValue());
						hierarchy.setDisplayName(escHierarDisNameField.getValue());
						hierarchy.setDescription(escHierarDesField.getValue());
						APConstants.apService.insertEscalationHierarchies(hierarchy,new AsyncCallback<EscalationHierarchiesClient>() {
							
							@Override
							public void onSuccess(EscalationHierarchiesClient result) {
								
								if (null == result) {
									TbitsInfo
											.error("Escalation Hierarchy Name Already Exist");
								} else {
									TbitsInfo
											.info("Escalation Hierarchy has been added successfully");
									addEscWin.hide();
									refresh(0);
								}
								
							}
							
							@Override
							public void onFailure(Throwable caught) {
										TbitsInfo
												.error(
														"Error while inserting the new escalation hierarchy",
														caught);
										Log
												.error(
														"Error while inserting the new escalation hierarchy",
														caught);
								
							}
						});
					}

			
			}
		));
	addEscWin.show();

	}
	

}
