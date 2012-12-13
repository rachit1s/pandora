package transmittal.com.tbitsGlobal.client.admin;

import java.util.HashMap;
import java.util.List;

import transmittal.com.tbitsGlobal.client.models.TrnAttachmentList;
import transmittal.com.tbitsGlobal.client.models.TrnCreateProcess;
import transmittal.com.tbitsGlobal.client.models.TrnDistList;
import transmittal.com.tbitsGlobal.client.models.TrnDrawingNumber;
import transmittal.com.tbitsGlobal.client.models.TrnDropdown;
import transmittal.com.tbitsGlobal.client.models.TrnFieldMapping;
import transmittal.com.tbitsGlobal.client.models.TrnPostProcessValue;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;
import transmittal.com.tbitsGlobal.client.models.TrnProcessParam;
import transmittal.com.tbitsGlobal.client.models.TrnReplicateProcess;
import transmittal.com.tbitsGlobal.client.models.TrnSaveCreateProcess;
import transmittal.com.tbitsGlobal.client.models.TrnValidationRule;

import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.SysConfigClient;

public interface TrnAdminServiceAsync {

	/**
	 * Get The List of transmittal Processes
	 * @param callback
	 */
	void getTransmittalProcesses(AsyncCallback<List<TrnProcess>> callback);
	/**
	 * Get the list of transmittal processes for specified source ba
	 * @param currentSrcBa
	 * @param callback
	 */
	void getTransmittalProcessesForBa(BusinessAreaClient currentSrcBa, AsyncCallback<List<TrnProcess>> callback);
	/**
	 * Get The list of transmittal process parameters
	 * @param process
	 * @param callback
	 */
	void getProcessParams(TrnProcess process,AsyncCallback<List<TrnProcessParam>> callback);
	/**
	 * Save Transmittal Process Parameters
	 * @param process
	 * @param params
	 * @param callback
	 */
	void saveProcessParams(TrnProcess process, List<TrnProcessParam> params, AsyncCallback<List<TrnProcessParam>> callback);
	
	
	/**
	 * Save Transmittal Processes
	 * @param processes
	 * @param callback
	 */
	void saveTransmittalProcesses(List<TrnProcess> processes, AsyncCallback<List<TrnProcess>> callback);
	
	
	/**
	 * Save Post Transmittal Field Values Map to database
	 * @param process
	 * @param values
	 * @param callback
	 */
	void savePostProcessFieldValues(TrnProcess process, List<TrnPostProcessValue> values, AsyncCallback<List<TrnPostProcessValue>> callback);
	/**
	 * Get Post Transmittal Field Values Map from database
	 * @param process
	 * @param callback
	 */
	void getPostProcessFieldValues(TrnProcess process, AsyncCallback<List<TrnPostProcessValue>> callback);
	
	
	/**
	 * Return Source Target Field Map from database
	 * @param process
	 * @param callback
	 */
	void getSrcTargetFieldMap(TrnProcess process, AsyncCallback<List<TrnFieldMapping>> callback);
	
	/**
	 * Save Source Target Field Map to database
	 * @param process
	 * @param mappings
	 * @param callback
	 */
	void saveSrcTargetFieldMap(TrnProcess process, List<TrnFieldMapping> mappings, AsyncCallback<List<TrnFieldMapping>> callback);
	
	/**
	 * Get Transmittal Distribution List from database
	 * @param process
	 * @param callback
	 */
	void getDistList(TrnProcess process, AsyncCallback<List<TrnDistList>> callback);
	
	/**
	 * Save Distribution List Table to database
	 * @param process
	 * @param params
	 * @param callback
	 */
	void saveDistLists(TrnProcess process, List<TrnDistList> params, AsyncCallback<List<TrnDistList>> callback);
	
	
	/**
	 * Save Attachment List Table to database
	 * @param process
	 * @param params
	 * @param callback
	 */
	void saveAttachmentLists(TrnProcess process, List<TrnAttachmentList> params, AsyncCallback<List<TrnAttachmentList>> callback);
	/**
	 * Get Attachment List Table from database
	 * @param process
	 * @param callback
	 */
	void getAttachmentList(TrnProcess process, AsyncCallback<List<TrnAttachmentList>> callback);
	
	/**
	 * Get The dropdown table for the specified business area
	 * @param ba
	 * @param callback
	 */
	void getDropdownTable(BusinessAreaClient ba, AsyncCallback<List<TrnDropdown>> callback);
	/**
	 * Save the dropdown table for specified busineess area
	 * @param ba
	 * @param list
	 * @param callback
	 */
	void saveDropdownTable(BusinessAreaClient ba, List<TrnDropdown> list, AsyncCallback<List<TrnDropdown>> callback);
	/**
	 * Get the dropdown table for all business areas
	 * @param callback
	 */
	void getAllDropdownEntries(AsyncCallback<List<TrnDropdown>> callback);
	
	/**
	 * Get Drawing Number fields for all business areas
	 * @param callback
	 */
	void getDrawingNumberFields(AsyncCallback<List<TrnDrawingNumber>> callback);
	
	/**
	 * Save the Drawing Number fields for all business areas
	 * @param list
	 * @param callback
	 */
	void saveDrawingNumberFields(List<TrnDrawingNumber> list, AsyncCallback<List<TrnDrawingNumber>> callback);
	
	/**
	 * Get Trn Validation Rules for the specified Process
	 * @param callback
	 */
	void getValidationRulesForProcess(TrnProcess process, AsyncCallback<List<TrnValidationRule>> callback);
	
	/**
	 * Save the Trn Validation Rules for specified process
	 * @param process
	 * @param savedProperties
	 * @param callback
	 */
	void saveValidationRulesForProcess(TrnProcess process, List<TrnValidationRule> savedProperties, AsyncCallback<List<TrnValidationRule>> callback);
	
	/**
	 * Get the parameters for the specifed process for replication
	 * @param process
	 * @param srcBa
	 * @param callback
	 */
	void getProcessParams(TrnProcess process, BusinessAreaClient srcBa,	AsyncCallback<List<TrnReplicateProcess>> callback);
	
	/**
	 * Copy the current process to a new process
	 * @param processParams
	 * @param currentProcess
	 * @param destSrcBa
	 * @param callback
	 */
	void copyProcess(List<TrnReplicateProcess> processParams, TrnProcess currentProcess, BusinessAreaClient destSrcBa, AsyncCallback<List<TrnReplicateProcess>> callback);
	
	
	/**
	 * Get all the parameters associated with this transmittal process
	 * @param process
	 * @param callback
	 */
	void getAllProcessParams(TrnProcess process, AsyncCallback<List<TrnCreateProcess>> callback);
	
	/**
	 * Get the Maximum value of Transmittal Process ID
	 * @param callback
	 */
	void getMaxIdTrnProcess(AsyncCallback<Integer> callback);
	
	/**
	 * Get the Maximum value of Transmittal Dropdown ID
	 * @param callback
	 */
	void getMaxIdTrnDropdown(AsyncCallback<Integer> callback);
	
	/**
	 * Save all the values for the new process in database
	 * @param values
	 * @param callback
	 */
	void saveNewProcessValues(TrnSaveCreateProcess values, AsyncCallback<Boolean> callback);
	//-------------Dummy Functions----------------------------//
	
	void getBaClient(BusinessAreaClient baclient, AsyncCallback<BusinessAreaClient> callback);
	void getBaField(BAField baField, AsyncCallback<BAField> callback);
	void getSysconfigClient(SysConfigClient sysconfig, AsyncCallback<SysConfigClient> callback);
}
