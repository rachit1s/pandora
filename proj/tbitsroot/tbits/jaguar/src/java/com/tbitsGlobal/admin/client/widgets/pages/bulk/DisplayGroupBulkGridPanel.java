package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class DisplayGroupBulkGridPanel extends AbstractAdminBulkUpdatePanel<DisplayGroupClient>{

	public DisplayGroupBulkGridPanel() {
		super();
		
		canCopyPasteRows = false;
		commonGridDisabled = true;
	}

	@Override
	protected void onSave(List<DisplayGroupClient> models, Button btn) {
		TbitsInfo.info("Saving Display Groups... Please Wait...");
		int count = 0;
		boolean isInActive = false;
		for (DisplayGroupClient dgc : models) {
			if (dgc.getIsDefault() == true)
				count++;
			if (dgc.getIsDefault() == true && dgc.getIsActive()==false)
				isInActive = true;
			
		}if (count != 1 ) {
			TbitsInfo.info("Display groups cannot update as is_default column has more than 1 value or less than 1");

		}else if (isInActive){
			TbitsInfo.info("Default Display groups cannot be marked as inactive");
		}else{
			APConstants.apService.updateDisplayGroups(ClientUtils.getCurrentBA().getSystemPrefix(), models,
					new AsyncCallback<List<DisplayGroupClient>>() {
						public void onFailure(Throwable caught) {
							TbitsInfo.error("DisplayGroup not Updated ...Please Refresh ...",caught);
							Log.error("DisplayGroup not Updated ...Please Refresh ...",caught);
						}

						public void onSuccess(List<DisplayGroupClient> result) {
							if (result != null) {
								TbitsInfo.info("Display groups updated Successfully");
							}
						}
					});
		}
	}

	@Override
	public DisplayGroupClient getEmptyModel() {
		return new DisplayGroupClient();
	}

	public void refresh(int page) {
		singleGridContainer.removeAllModels();
		TbitsInfo.info("Fetching Display Group from database... Please Wait...");
		APConstants.apService.getDisplayGroups(ClientUtils.getSysPrefix(),new AsyncCallback<ArrayList<DisplayGroupClient>>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("DisplayGroup not Loaded ...Please Refresh ...", caught);
			}
			public void onSuccess(ArrayList<DisplayGroupClient> result) {
				if(result != null){
					List<DisplayGroupClient> models = ClientUtils.sort(result, -1, -1, true);
					singleGridContainer.addModel(models);
				}
			}
		});	
	}
	
	@Override
	protected void onAdd() {
		final Window win = new Window();
		
		ToolButton s = new ToolButton("x-tool-help", new SelectionListener<IconButtonEvent>(){
			@Override
			public void componentSelected(IconButtonEvent ce) {
				MessageBox box  = new MessageBox();
				box.setTitle(" Display Group ");
				box.setMessage("Please enter the Display Name, Display Order & "
						+  "check the Is Active if active ");
				box.show();
			}
		});
		win.getHeader().addTool(s);;
		win.setHeading("Add Display Group");
		win.setModal(true);
		win.setLayout(new FitLayout());
		win.setSize(300, 200);
		
		FormPanel formPanel = new FormPanel();
		FormLayout formLayout = new FormLayout();
		formLayout.setLabelSeparator("");
		formLayout.setLabelWidth(60);
		formPanel.setFrame(false);
		formPanel.setHeaderVisible(false);
		formPanel.setBodyBorder(false);

		final TextField<String> dispName = new TextField<String>();
		dispName.setFieldLabel("Display Name");
		dispName.setEmptyText("enter Display Name");
		formPanel.add(dispName, new FormData("100%"));

		final SpinnerField dispOrder = new SpinnerField();
		dispOrder.setFieldLabel("Display Order");
		dispOrder.setEmptyText("enter Display Order");
		dispOrder.setAllowDecimals(false);
		dispOrder.setAllowNegative(false);
		dispOrder.getPropertyEditor().setFormat(NumberFormat.getDecimalFormat());
		formPanel.add(dispOrder, new FormData("100%"));

		final CheckBox isActive = new CheckBox();
		isActive.setFieldLabel("Is Active");
		isActive.setValue(false);
		formPanel.add(isActive, new FormData("100%"));
		
		final CheckBox isDefault = new CheckBox();
		isDefault.setFieldLabel("Is Default");
		isDefault.setValue(false);
		formPanel.add(isDefault, new FormData("100%"));

		Button submit = new Button("Submit");
		Button cancel = new Button("Cancel");

		win.addButton(submit);
		win.addButton(cancel);
		
		win.add(formPanel, new FitData());

		submit.addSelectionListener( new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent se) {
				if(dispName.getValue() != null && dispOrder.getValue() != null){
					final String name = dispName.getValue();
					final int order = dispOrder.getValue().intValue();
					
					APConstants.apService.insertDisplayGroup(ClientUtils.getCurrentBA().getSystemPrefix(),name, order, isActive.getValue(),isDefault.getValue(), new AsyncCallback<DisplayGroupClient>() {
						public void onFailure(Throwable caught) {
							TbitsInfo.error("DisplayGroup not Added ...Please Refresh ...", caught);
							Log.error("DisplayGroup not Added ...Please Refresh ...", caught);
						}
						public void onSuccess(DisplayGroupClient result) {
							if(result != null){
								singleGridContainer.addModel(result);
								TbitsInfo.info("DisplayGroup has been added ...");
							}
							win.hide();
						}
					});		
				}
				else{
					TbitsInfo.error("Enter Display Name && Display Order");
				}
			}
		});

		cancel.addSelectionListener( new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent se) {
				win.hide();
			}
		});
		
		win.show();
	}
	
	@Override
	protected void onRemove() {
		final List<DisplayGroupClient> selectedItems = singleGridContainer.getSelectedModels();
		if(selectedItems != null){
			for(DisplayGroupClient dc : selectedItems)
			{
				
			if(dc.getIsDefault()==false && selectedItems.size() > 0 && 
					com.google.gwt.user.client.Window.confirm("Do you want to delete " + selectedItems.size() + " selected Display Groups?")){
				APConstants.apService.deleteDisplayGroups(ClientUtils.getSysPrefix(), selectedItems, new AsyncCallback<Boolean>(){
					public void onFailure(Throwable caught) {
						TbitsInfo.error("Unable to delete Display Groups", caught);
						Log.error("Unable to delete Display Groups", caught);
					}

					public void onSuccess(Boolean result) {
						if(result){
							TbitsInfo.info("Display Groups deleted");
							for(DisplayGroupClient model : selectedItems){
								singleGridContainer.getBulkGrid().getStore().remove(model);
							}
						}
					}});
			}
			else if(dc.getIsDefault()==true) 
			{
				TbitsInfo.info("Default Display Group cannot be deleted");
			}
			}
				
		}
	}

	@Override
	protected BulkUpdateGridAbstract<DisplayGroupClient> getNewBulkGrid(
			BulkGridMode mode) {
		return new DisplayGroupBulkGrid(mode);
	}

}
