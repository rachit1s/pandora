package transmittal.com.tbitsGlobal.client.admin.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import transmittal.com.tbitsGlobal.client.admin.TrnAdminConstants;
import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.state.AppState;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.cache.BusinessAreaCache;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizardPage;

public class CreateProcessPage1 extends AbstractWizardPage<FormPanel, HashMap<String, String>> {

	protected TextField<String> trnProcessIdField;
	ComboBox<BusinessAreaClient> srcBa;
	protected TextField<String> trnProcessDesc;
	protected TextField<String> trnDropdownIdField;
	protected TextField<String> trnDropdownName;
	protected TextField<String> trnDropdownSortOrder;
	protected TextField<String> trnMaxKey;
	protected ComboBox<BusinessAreaClient> dtrBa;
	protected ComboBox<BusinessAreaClient> dtnBa;
	protected TextField<String> destBaSrcTargetFieldMap;
	protected TextField<String> destBaPostTrnFieldMap;
	protected BusinessAreaCache cache ;
	protected boolean canContinue;
	
	protected CreateProcessPage1(UIContext wizardContext) {
		super(wizardContext);
		canContinue = false;
		cache = CacheRepository.getInstance().getCache(BusinessAreaCache.class);
		
	}

	public void buildPage() {
		
		final FormData formData = new FormData();
		formData.setWidth(500);
		
		/**
		 * FIXME: The NumberField widget defined in gxt does not work. So in order to bind the input 
		 * with an integer, perform a check after the form is submitted.
		 */
		trnProcessIdField = new TextField<String>();
		trnProcessIdField.setWidth(200);
		trnProcessIdField.setFieldLabel("Process ID");
		TrnAdminConstants.trnAdminService.getMaxIdTrnProcess(new AsyncCallback<Integer>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not fetch Max Transmittal Id from database...", caught);
				Log.error("Could not fetch Max Transmittal Id from database...", caught);
			}
			
			public void onSuccess(Integer result) {
				trnProcessIdField.setValue(result.toString());
			}
		});
		widget.add(trnProcessIdField);
		
		srcBa = TrnAdminUtils.getBACombo();
		srcBa.setFieldLabel("Source BA");
		widget.add(srcBa);
		
		trnProcessDesc = new TextField<String>();
		trnProcessDesc.setWidth(200);
		trnProcessDesc.setFieldLabel("Transmittal Process Description");
		widget.add(trnProcessDesc, formData);
		
		trnDropdownIdField = new TextField<String>();
		trnDropdownIdField.setWidth(200);
		trnDropdownIdField.setFieldLabel("Dropdown ID");
		TrnAdminConstants.trnAdminService.getMaxIdTrnDropdown(new AsyncCallback<Integer>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Could not fetch Max Dropdown Id from database...", caught);
				Log.error("Could not fetch Max Dropdown Id from database...", caught);
			}

			public void onSuccess(Integer result) {
				trnDropdownIdField.setValue(result.toString());
			}
		});
		widget.add(trnDropdownIdField);
		
		trnDropdownSortOrder = new TextField<String>();
		trnDropdownSortOrder.setWidth(200);
		trnDropdownSortOrder.setFieldLabel("Dropdown Sort Order");
		widget.add(trnDropdownSortOrder);
		
		trnDropdownName = new TextField<String>();
		trnDropdownName.setFieldLabel("Dropdown Name");
		widget.add(trnDropdownName, formData);
		
		trnMaxKey = new TextField<String>();
		trnMaxKey.setWidth(200);
		trnMaxKey.setFieldLabel("Max Key");
		widget.add(trnMaxKey);
		
		dtrBa = TrnAdminUtils.getBACombo();
		dtrBa.setFieldLabel("DTR BA");
		widget.add(dtrBa);
		
		dtnBa = TrnAdminUtils.getBACombo();
		dtnBa.setFieldLabel("DTN BA");
		widget.add(dtnBa);
		
		destBaSrcTargetFieldMap = new TextField<String>();
		destBaSrcTargetFieldMap.setFieldLabel(CreateProcessConstants.DEST_BA_SRC_TARGET_MAP);
		destBaSrcTargetFieldMap.setLabelStyle("width:250");
		destBaSrcTargetFieldMap.setEmptyText("sys_prefix1,sys_prefix2...");
		widget.add(destBaSrcTargetFieldMap, formData);
		
		
		destBaPostTrnFieldMap = new TextField<String>();
		destBaPostTrnFieldMap.setFieldLabel(CreateProcessConstants.DEST_BA_POST_TRN_MAP);
		destBaPostTrnFieldMap.setLabelStyle("width:250");
		destBaPostTrnFieldMap.setEmptyText("sys_prefix1,sys_prefix2...");
		widget.add(destBaPostTrnFieldMap, formData);
	}

	public int getDisplayOrder() {
		return 0;
	}

	public HashMap<String, String> getValues() {
		HashMap<String, String> values = new HashMap<String, String>();
		
		values.put(CreateProcessConstants.TRN_PROCESS_ID, trnProcessIdField.getValue());
		values.put(CreateProcessConstants.TRN_PROCESS_DESC, trnProcessDesc.getValue());
		values.put(CreateProcessConstants.TRN_DROPDOWN_ID, trnDropdownIdField.getValue());
		values.put(CreateProcessConstants.TRN_DROPDOWN_SORT_ORDER, trnDropdownSortOrder.getValue());
		values.put(CreateProcessConstants.TRN_DROPDOWN_NAME, trnDropdownName.getValue());
		values.put(CreateProcessConstants.TRN_MAX_KEY, trnMaxKey.getValue());
		values.put(CreateProcessConstants.DEST_BA_SRC_TARGET_MAP, destBaSrcTargetFieldMap.getValue());
		values.put(CreateProcessConstants.DEST_BA_POST_TRN_MAP, destBaPostTrnFieldMap.getValue());
		values.put(CreateProcessConstants.SRC_BA, Integer.toString(srcBa.getSelection().get(0).getSystemId()));
		values.put(CreateProcessConstants.DTR_BA, Integer.toString(dtrBa.getSelection().get(0).getSystemId()));
		values.put(CreateProcessConstants.DTN_BA, Integer.toString(dtnBa.getSelection().get(0).getSystemId()));
		
		return values;
	}

	public FormPanel getWidget() {
		return widget;
	}

	public void initializeWidget() {
		widget = new FormPanel();
		widget.setHeaderVisible(true);
		widget.setHeading("Basic Parameters [Page 1]");
		widget.setLabelWidth(150);
		widget.setBodyBorder(false);
		widget.setScrollMode(Scroll.AUTO);
	}

	public void onDisplay() {
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).hideFinishButton();
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).showNextButton();
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).hideBackButton();
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).hidePreviewDOCButton();
	}

	public void onInitialize() {
		buildPage();
	}

	public boolean onLeave() {
		validateValues();
		return canContinue;
	}

	/**
	 * Check all the values inserted in the page for validity
	 */
	protected void validateValues(){
		
		String processId = trnProcessIdField.getValue();
		if(!isValidInteger(processId, "Transmittal Process ID", true)){
			return;
		}
		
		if(TrnAdminUtils.isProcessExists(Integer.valueOf(processId))){
			TbitsInfo.error("The specified Process ID already exists.");
			return;
		}
		
		if(srcBa.getSelection().size() == 0){
			TbitsInfo.error("Please Select a valid 'Source Ba' parameter");
			return;
		}
		
		String trnProcessDesc = this.trnProcessDesc.getValue();
		if((null == trnProcessDesc) || (trnProcessDesc.trim().equals(""))){
			TbitsInfo.error("Please set the value of 'Transmittal Process Description' parameter");
			return;
		}
		
		String dropdownId = trnDropdownIdField.getValue();
		if(!isValidInteger(dropdownId, "Dropdown ID", true)){
			return;
		}
		
		String sortOrder = trnDropdownSortOrder.getValue();
		if(!isValidInteger(sortOrder, "Sort Order", true)){
			return;
		}
		
		String dropdownName = trnDropdownName.getValue();
		if((null == dropdownName) || (dropdownName.trim().equals(""))){
			TbitsInfo.error("Please set the value of 'Dropdown Name' parameter");
			return;
		}
		
		String maxKey = trnMaxKey.getValue();
		if((null == maxKey) || (maxKey.trim().equals(""))){
			TbitsInfo.error("Please set the value of 'Max Key' parameter");
			return;
		}
		
		if(dtrBa.getSelection().size() == 0){
			TbitsInfo.error("Please Select a valid 'DTR BA' parameter");
			return;
		}
		
		if(dtnBa.getSelection().size() == 0){
			TbitsInfo.error("Please Select a valid 'DTN Ba' parameter");
			return;
		}
		
		String destBaSrcTargetFieldMap = this.destBaSrcTargetFieldMap.getValue();
		if((null == destBaSrcTargetFieldMap) || (destBaSrcTargetFieldMap.trim().equals(""))){
			TbitsInfo.error("Please set the value of 'Destination BA [Source Target Field Map]' parameter");
			return;
		}
		
		String [] destBaSrcTargetFieldMapList = destBaSrcTargetFieldMap.split(",");
		for(String ba : destBaSrcTargetFieldMapList){
			
			if(isValidInteger(ba, "Destba Src Target Field Map", false)){
				if(!isValidBa(null, Integer.valueOf(ba))){
					TbitsInfo.error("Invalid value of BA in 'Destination BA [Source Target Field Map]' parameter");
					return;
				}
			}else if(!isValidBa(ba, null)){
					TbitsInfo.error("Invalid value of BA in 'Destination BA [Source Target Field Map]' parameter");
					return;
				}
			}
		
		String destBaPostTrnFieldMap = this.destBaPostTrnFieldMap.getValue();
		if((null == destBaPostTrnFieldMap) || (destBaPostTrnFieldMap.trim().equals(""))){
			TbitsInfo.error("Please set the value of 'Destination BA [Post Transmittal Field Map]' parameter");
			return;
		}
		
		String [] destBaPostTrnFieldMapList = destBaPostTrnFieldMap.split(",");
		for(String ba : destBaPostTrnFieldMapList){
			if(isValidInteger(ba, "Destba PostTrn Field Map", false)){
				if(!isValidBa(null, Integer.valueOf(ba))){
					TbitsInfo.error("Invalid value of BA in 'Destination BA [Post Trn Field Map]' parameter");
					return;
				}
			}else if(!isValidBa(ba, null)){
					TbitsInfo.error("Invalid value of BA in 'Destination BA [Post Trn Field Map]' parameter");
					return;
			}
		}
		
		canContinue = true;
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
	
	/**
	 * Check if the business area specified by "either" sysprefix or sysId exists or not. If only one value is known,
	 * put the other one as null
	 * @param baSysPrefix 	- sysprefix of destination ba
	 * @param baSysId 		- sysid of destination ba
	 * @return
	 */
	private boolean isValidBa(String baSysPrefix, Integer baSysId){
		if(AppState.checkAppStateIsTill(AppState.BAMapReceived)){
			List<BusinessAreaClient> baList = new ArrayList<BusinessAreaClient>(cache.getValues());
			if((baList != null) && (!baList.isEmpty())){
				for(BusinessAreaClient entry : baList){
					if(null != baSysPrefix){
						if(baSysPrefix.toLowerCase().trim().equals(entry.getSystemPrefix().toLowerCase().trim()))
							return true;
					}
					if(null != baSysId){
						if(baSysId == entry.getSystemId())
							return true;
					}
				}
			}else{
				TbitsInfo.error("Could not get the list of business areas... Please refresh....");
				Log.error("Error while getting list of ba's");
			}
		}
		return false;
	}
}
