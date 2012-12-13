package transmittal.com.tbitsGlobal.client.admin.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import transmittal.com.tbitsGlobal.client.admin.TrnAdminConstants;
import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnAttachmentList;
import transmittal.com.tbitsGlobal.client.models.TrnDistList;
import transmittal.com.tbitsGlobal.client.models.TrnDrawingNumber;
import transmittal.com.tbitsGlobal.client.models.TrnFieldMapping;
import transmittal.com.tbitsGlobal.client.models.TrnPostProcessValue;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;
import transmittal.com.tbitsGlobal.client.models.TrnProcessParam;
import transmittal.com.tbitsGlobal.client.models.TrnSaveCreateProcess;
import transmittal.com.tbitsGlobal.client.models.TrnValidationRule;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;
import commons.com.tbitsGlobal.utils.client.wizards.IWizardPage;

public class CreateProcessWizard extends AbstractWizard {

	protected CreateProcessPage1 page1;
	protected CreateProcessPage2 page2;
	protected CreateProcessPage3 page3;
	protected CreateProcessPage4 page4;
	protected CreateProcessPage5 page5;
	protected CreateProcessPage6 page6;
	protected CreateProcessPage7 page7;
	protected CreateProcessPage8 page8;
	
	protected TrnProcess currentProcess = null;
	protected List<BusinessAreaClient> destBaSrcTargetFieldMap 	= null;
	protected List<BusinessAreaClient> destBaPostTrnFieldMap	= null;
	
	protected TrnSaveCreateProcess newProcessValues;
	
	public CreateProcessWizard(){
		super();
		this.addBackButton();
		this.addNextButton();
		this.addFinishButton();
		
		this.setHeading("Create Process Wizard");
		finishBtn.setText("Create Process");
		
		page1 = new CreateProcessPage1(context);
		page2 = new CreateProcessPage2(context);
		page3 = new CreateProcessPage3(context);
		page4 = new CreateProcessPage4(context);
		page5 = new CreateProcessPage5(context);
		page6 = new CreateProcessPage6(context);
		page7 = new CreateProcessPage7(context);
		page8 = new CreateProcessPage8(context);
		
		currentProcess = new TrnProcess();
		destBaPostTrnFieldMap = new ArrayList<BusinessAreaClient>();
		destBaSrcTargetFieldMap = new ArrayList<BusinessAreaClient>();
		addPages();
		newProcessValues = new TrnSaveCreateProcess();
	}
	
	protected void addPages(){
		CreateProcessWizard.this.addPage(page1);
		CreateProcessWizard.this.addPage(page2);
		CreateProcessWizard.this.addPage(page3);
		CreateProcessWizard.this.addPage(page4);
		CreateProcessWizard.this.addPage(page5);
		CreateProcessWizard.this.addPage(page6);
		CreateProcessWizard.this.addPage(page7);
		CreateProcessWizard.this.addPage(page8);
		
		activePage = page1;
		activePage.onDisplay();
	}
	
	@Override
	protected void addPreviewDOCButton() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void addPreviewPDFButton() {
		// TODO Auto-generated method stub

	}

	protected void onSubmit() {
		saveValuesPage1();
		saveValuesPage2();
		saveValuesPage3();
		saveValuesPage4();
		saveValuesPage5();
		saveValuesPage6();
		saveValuesPage7();
		saveValuesPage8();
		
		
		TrnAdminConstants.trnAdminService.saveNewProcessValues(newProcessValues, new AsyncCallback<Boolean>(){

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error while saving new transmittal process to database...", caught);
				Log.error("Error while saving new transmittal process to database...", caught);
			}

			public void onSuccess(Boolean result) {
				if(result){
					Window.alert("Successfully Created new process");
				}else{
					TbitsInfo.error("Error while saving new transmittal process to database... See logs for more info...");
				}
			}
		});
	}
	
	/**
	 * Get and save the values from page 1
	 */
	protected void saveValuesPage1(){
		final HashMap<String, String> page1Values = new HashMap<String, String>(page1.getValues());
		
		
		TrnAdminConstants.trnAdminService.getMaxIdTrnProcess(new AsyncCallback<Integer>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not fetch Max Transmittal Id from database...", caught);
				Log.error("Could not fetch Max Transmittal Id from database...", caught);
			}
			
			public void onSuccess(Integer result) {
				currentProcess.setProcessId(result);
			}
		});
		
		TrnAdminConstants.trnAdminService.getMaxIdTrnDropdown(new AsyncCallback<Integer>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not fetch Max Dropdown Id from database...", caught);
				Log.error("Could not fetch Max Dropdown Id from database...", caught);
			}

			public void onSuccess(Integer result) {
				page1Values.put(CreateProcessConstants.TRN_DROPDOWN_ID, Integer.toString(result));
			}
		});
		currentProcess.setProcessId(Integer.parseInt(page1Values.get(CreateProcessConstants.TRN_PROCESS_ID)));
		currentProcess.setDescription(page1Values.get(CreateProcessConstants.TRN_PROCESS_DESC));
		currentProcess.setName(page1Values.get(CreateProcessConstants.TRN_DROPDOWN_NAME));
		currentProcess.setSrcBA(TrnAdminUtils.getBA(null, Integer.valueOf(page1Values.get(CreateProcessConstants.SRC_BA))));
		currentProcess.setDTNBA(TrnAdminUtils.getBA(null, Integer.valueOf(page1Values.get(CreateProcessConstants.DTN_BA))));
		currentProcess.setDTRBA(TrnAdminUtils.getBA(null, Integer.valueOf(page1Values.get(CreateProcessConstants.DTR_BA))));
		currentProcess.setSerialKey(page1Values.get(CreateProcessConstants.TRN_MAX_KEY));
		
		
		String [] destBaSrcTargetFieldMapList = page1Values.get(CreateProcessConstants.DEST_BA_SRC_TARGET_MAP).split(",");
		for(String baSysprefix : destBaSrcTargetFieldMapList){
			if(isValidInteger(baSysprefix, "DestBA Src Target Field Map", false)){
				if(TrnAdminUtils.getBA(null, Integer.valueOf(baSysprefix)) == null){
					TbitsInfo.error("Invalid value of BA in 'Destination BA [Source Target Field Map]' parameter");
					return;
				}
				destBaSrcTargetFieldMap.add(TrnAdminUtils.getBA(null, Integer.valueOf(baSysprefix)));
				continue;
			}else if(TrnAdminUtils.getBA(baSysprefix, null) == null){
				TbitsInfo.error("Invalid value of BA in 'Destination BA [Source Target Field Map]' parameter");
				return;
			}
			destBaSrcTargetFieldMap.add(TrnAdminUtils.getBA(baSysprefix, null));
		}
		
		String [] destBaPostTrnFieldMapList = page1Values.get(CreateProcessConstants.DEST_BA_POST_TRN_MAP).split(",");
		for(String baSysprefix : destBaPostTrnFieldMapList){
			if(isValidInteger(baSysprefix, "DestBA Post Trn Field Map", false)){
				if(TrnAdminUtils.getBA(null, Integer.valueOf(baSysprefix)) == null){
					TbitsInfo.error("Invalid value of BA in 'Destination BA [Post Trn Field Map]' parameter");
					return;
				}
				destBaPostTrnFieldMap.add(TrnAdminUtils.getBA(null, Integer.valueOf(baSysprefix)));
				continue;
			}else if(TrnAdminUtils.getBA(baSysprefix, null) == null){
				TbitsInfo.error("Invalid value of BA in 'Destination BA [Post Trn Field Map]' parameter");
				return;
			}
			destBaPostTrnFieldMap.add(TrnAdminUtils.getBA(baSysprefix, null));
			
			
			if(TrnAdminUtils.getBA(baSysprefix, null) == null){
				TbitsInfo.error("Invalid value of Destination BA in Post Transmittal Field Map");
				return;
			}
			destBaPostTrnFieldMap.add(TrnAdminUtils.getBA(baSysprefix, null));
		}
		
		newProcessValues.setValuesPage1(page1Values);
		
//		TrnAdminConstants.trnAdminService.saveValuesPage1(page1Values, new AsyncCallback<HashMap<String,String>>(){
//
//			public void onFailure(Throwable caught) {
//				TbitsInfo.error("Could not save properties for new transmittal process", caught);
//				Log.error("Could not save properties for new transmittal process", caught);
//			}
//
//			public void onSuccess(HashMap<String, String> result) {
//				saveValuesPage2();
//			}
//		});
		
	}
	
	/**
	 * Get and save values from Page 2
	 */
	protected void saveValuesPage2(){
		List<TrnProcessParam> processParamsList = new ArrayList<TrnProcessParam>(page2.getValues());
		
		for(TrnProcessParam entry : processParamsList){
			entry.setSrcBA(this.currentProcess.getSrcBA());
			entry.setProcessId(this.currentProcess.getProcessId());
		}
		
		newProcessValues.setValuesPage2(processParamsList);
		
//		TrnAdminConstants.trnAdminService.saveValuesPage2(processParamsList, new AsyncCallback<List<TrnProcessParam>>(){
//
//			public void onFailure(Throwable caught) {
//				TbitsInfo.error("Could not save Transmittal Process Parameters for new transmittal processs", caught);
//				Log.error("Could not save Transmittal Process Parameters for new transmittal processs", caught);
//			}
//
//			public void onSuccess(List<TrnProcessParam> result) {
//				saveValuesPage3();
//			}
//		});
	}
	
	/**
	 * Get and save values from Page 3
	 */
	protected void saveValuesPage3(){
		List<TrnPostProcessValue> postProcessParamsList = new ArrayList<TrnPostProcessValue>(page3.getValues());
		
		for(TrnPostProcessValue entry : postProcessParamsList){
			entry.setSrcBA(this.currentProcess.getSrcBA());
			entry.setProcessId(this.currentProcess.getProcessId());
			if(!isValidDestinationBA(entry.getTargetBA(), CreateProcessConstants.DEST_BA_POST_TRN_MAP)){
				TbitsInfo.error("Invalid value of destination BA Selected in Post Transmittal Field Map");
				Log.error("Invalid value of destination BA selected in Post Transmittal Field Map. Please select" +
						" a value which is one of the values entered in Destination BA in Page 1");
				return;
			}
		}
		
		newProcessValues.setValuesPage3(postProcessParamsList);
		
//		TrnAdminConstants.trnAdminService.saveValuesPage3(postProcessParamsList, new AsyncCallback<List<TrnPostProcessValue>>(){
//
//			public void onFailure(Throwable caught) {
//				TbitsInfo.error("Could not save Post Transmittal Field Values for the new process", caught);
//				Log.error("Could not save Post Transmittal Field Values for the new process", caught);
//			}
//
//			public void onSuccess(List<TrnPostProcessValue> result) {
//				saveValuesPage4();
//			}
//		});
	}
	
	/**
	 * Save values from page 4
	 */
	protected void saveValuesPage4(){
		List<TrnFieldMapping> srcTargetFieldMappingList	 = new ArrayList<TrnFieldMapping>(page4.getValues());
		
		for(TrnFieldMapping entry : srcTargetFieldMappingList){
			entry.setSrcBA(this.currentProcess.getSrcBA());
			entry.setProcessId(this.currentProcess.getProcessId());
			if(!isValidDestinationBA(entry.getTargetBA(), CreateProcessConstants.DEST_BA_SRC_TARGET_MAP)){
				TbitsInfo.error("Invalid value of destination BA Selected in Source Target Field Map");
				Log.error("Invalid value of destination BA selected in Source Target Field Map. Please select " +
						" a value which is one of the values entered in Destination BA in Page 1");
				return;
			}
		}
		
		newProcessValues.setValuesPage4(srcTargetFieldMappingList);
		
//		TrnAdminConstants.trnAdminService.saveValuesPage4(srcTargetFieldMappingList, new AsyncCallback<List<TrnFieldMapping>>(){
//
//			public void onFailure(Throwable caught) {
//				TbitsInfo.error("Could not save Source Target Field Mapping values for the new process", caught);
//				Log.error("Could not save Source Target Field Mapping values for the new process", caught);
//			}
//			
//			public void onSuccess(List<TrnFieldMapping> result) {
//				saveValuesPage5();
//			}
//		});
	}
	
	/**
	 * Save values from page 5
	 */
	protected void saveValuesPage5(){
		List<TrnAttachmentList> attachmentMapList	= new ArrayList<TrnAttachmentList>(page5.getValues());
		
		for(TrnAttachmentList entry : attachmentMapList){
			entry.setProcessId(this.currentProcess.getProcessId());
		}
		
		newProcessValues.setValuesPage5(attachmentMapList);
		
//		TrnAdminConstants.trnAdminService.saveValuesPage5(attachmentMapList, new AsyncCallback<List<TrnAttachmentList>>(){
//
//			public void onFailure(Throwable caught) {
//				TbitsInfo.error("Could not save Attachment Selection Table values for new process", caught);
//				Log.error("Could not save Attachment Selection Table values for new process", caught);
//			}
//
//			public void onSuccess(List<TrnAttachmentList> result) {
//				saveValuesPage6();
//			}
//		});
	}
	
	/**
	 * Save values from page 6
	 */
	protected void saveValuesPage6(){
		List<TrnDistList> distList	= new ArrayList<TrnDistList>(page6.getValues());
		
		for(TrnDistList entry : distList){
			entry.setProcessId(this.currentProcess.getProcessId());
		}
		
		newProcessValues.setValuesPage6(distList);
//		TrnAdminConstants.trnAdminService.saveValuesPage6(distList, new AsyncCallback<List<TrnDistList>>(){
//
//			public void onFailure(Throwable caught) {
//				TbitsInfo.error("Could not save Distribution Table values for new process", caught);
//				Log.error("Could not save Distribution Table values for new process", caught);
//			}
//
//			public void onSuccess(List<TrnDistList> result) {
//				saveValuesPage7();
//			}
//		});
	}
	
	/**
	 * Save values from page 7
	 */
	protected void saveValuesPage7(){
		List<TrnDrawingNumber> trnDrawingNumberList = new ArrayList<TrnDrawingNumber>(page7.getValues());
		
		for(TrnDrawingNumber entry : trnDrawingNumberList){
			entry.setSrcBa(this.currentProcess.getSrcBA());	
		}
		
		newProcessValues.setValuesPage7(trnDrawingNumberList);
		
//		TrnAdminConstants.trnAdminService.saveValuesPage7(trnDrawingNumberList, new AsyncCallback<List<TrnDrawingNumber>>(){
//			public void onFailure(Throwable caught) {
//				TbitsInfo.error("Could not save Drawing Number Table for new process", caught);
//				Log.error("Could not save Drawing Number Table for new process", caught);
//			}
//
//			public void onSuccess(List<TrnDrawingNumber> result) {
//				saveValuesPage8();
//			}
//		});
	}
	
	/**
	 * Save values from page 8
	 */
	protected void saveValuesPage8(){
		List<TrnValidationRule> validationRulesList	 = new ArrayList<TrnValidationRule>(page8.getValues());
		
		for(TrnValidationRule entry : validationRulesList){
			entry.setSrcBa(this.currentProcess.getSrcBA());
			entry.setProcess(this.currentProcess);
		}
		
		newProcessValues.setValuesPage8(validationRulesList);
		
//		TrnAdminConstants.trnAdminService.saveValuesPage8(validationRulesList, new AsyncCallback<List<TrnValidationRule>>(){
//
//			public void onFailure(Throwable caught) {
//				TbitsInfo.error("Could not save Validation Rules for new process", caught);
//				Log.error("Could not save Validation Rules for new process", caught);
//			}
//
//			public void onSuccess(List<TrnValidationRule> result) {
//				Window.alert("Created new process");
//			}
//		});
		
		this.hide();
	}
	
	/**
	 * Check if the specified BA is a valid destination BA for the specified table. The value is compared with
	 * the list of destination BA that the user selected in Page 1 of the wizard. Any value of destination BA selected
	 * for other tables must be among the values already selected, else error will be thrown.
	 * @param destBa- Destination BA
	 * @param map	- Table for which the BA should be a valid destination BA
	 * @return		- True, if a valid destination ba, false othervise 
	 */
	protected boolean isValidDestinationBA(BusinessAreaClient destBa, String map){
		if(map.trim().equals(CreateProcessConstants.DEST_BA_POST_TRN_MAP)){
			for(BusinessAreaClient destBaEntry : destBaPostTrnFieldMap){
				if(destBa.getSystemId() == destBaEntry.getSystemId()){
					return true;
				}
			}
		}
		
		if(map.trim().equals(CreateProcessConstants.DEST_BA_SRC_TARGET_MAP)){
			for(BusinessAreaClient destBaEntry : destBaSrcTargetFieldMap){
				if(destBa.getSystemId() == destBaEntry.getSystemId()){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Validate if the parameter is an integer or not
	 * @param paramValue
	 * @param paramName
	 * @return
	 */
	private boolean isValidInteger(String paramValue, String paramName, boolean showMessage){
		try{
			Integer.parseInt(paramValue);
		}catch (NumberFormatException ne){
			if(showMessage){
				TbitsInfo.error("Invalid value of " + paramName);
				Log.error("Invalid value of " + paramName, ne);
			}
			return false;
		}
		return true;
	}

	public void addBackButton() {
		backBtn = new Button("Back", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				try{
					int current = activePage.getDisplayOrder();
					if(pages.containsKey(current - 1)){
						IWizardPage<? extends LayoutContainer, ?> prePage = activePage.getPrevious();
						layout.setActiveItem(prePage.getWidget());
						activePage = prePage;
						activePage.onDisplay();
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}});
		this.addButton(backBtn);
	}
	
	public void addNextButton() {
		nextBtn = new Button("Next", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				try{
					int current = activePage.getDisplayOrder();
					if(pages.containsKey(current + 1)){
						if(activePage.onLeave()){
							IWizardPage<? extends LayoutContainer, ?> nextPage = activePage.getNext();
							layout.setActiveItem(nextPage.getWidget());
							activePage = nextPage;
							activePage.onDisplay();
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}});
		this.addButton(nextBtn);
	}
	
	
	public  void addFinishButton() {
		finishBtn = new Button("Finish", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				try{
					if(activePage.onLeave())
						onSubmit();
				}catch(Exception e){
					e.printStackTrace();
				}
			}});
		this.addButton(finishBtn);
		}
}
