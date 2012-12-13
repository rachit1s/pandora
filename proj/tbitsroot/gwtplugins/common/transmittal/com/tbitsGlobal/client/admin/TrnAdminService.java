package transmittal.com.tbitsGlobal.client.admin;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

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


import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.SysConfigClient;

public interface TrnAdminService extends RemoteService {
	//-------------------For Tranmittal Process Parameters-----------------------//
	public List<TrnProcess> getTransmittalProcesses() throws TbitsExceptionClient;
	public List<TrnProcess> getTransmittalProcessesForBa(BusinessAreaClient currentSrcBa) throws TbitsExceptionClient;
	public List<TrnProcessParam> getProcessParams(TrnProcess process) throws TbitsExceptionClient;
	public List<TrnProcessParam> saveProcessParams(TrnProcess process, List<TrnProcessParam> params) throws TbitsExceptionClient;
	
	//-------------------For Transmittal Processes-------------------------------//
	public List<TrnProcess> saveTransmittalProcesses(List<TrnProcess> processes) throws TbitsExceptionClient;
	
	//-------------------For Post Transmittal Field Values-----------------------//
	public List<TrnPostProcessValue> savePostProcessFieldValues(TrnProcess process, List<TrnPostProcessValue> values) throws TbitsExceptionClient;
	public List<TrnPostProcessValue> getPostProcessFieldValues(TrnProcess process) throws TbitsExceptionClient;
	
	//-------------------For Source Target Field Map ----------------------------//
	public List<TrnFieldMapping> getSrcTargetFieldMap(TrnProcess process) throws TbitsExceptionClient;
	public List<TrnFieldMapping> saveSrcTargetFieldMap(TrnProcess process, List<TrnFieldMapping> mappings) throws TbitsExceptionClient;
	
	//-------------------For Distribution List Map-------------------------------//
	public List<TrnDistList> getDistList(TrnProcess process) throws TbitsExceptionClient;
	public List<TrnDistList> saveDistLists(TrnProcess process, List<TrnDistList> params) throws TbitsExceptionClient;

	//-------------------For Attachment List Table------------------------------//
	public List<TrnAttachmentList> saveAttachmentLists(TrnProcess process, List<TrnAttachmentList> params) throws TbitsExceptionClient;
	public List<TrnAttachmentList> getAttachmentList(TrnProcess process) throws TbitsExceptionClient;

	//-------------------For Trn Dropdown Table---------------------------------//
	public List<TrnDropdown> getDropdownTable(BusinessAreaClient ba) throws TbitsExceptionClient;
	public List<TrnDropdown> saveDropdownTable(BusinessAreaClient ba, List<TrnDropdown> list) throws TbitsExceptionClient;
	public List<TrnDropdown> getAllDropdownEntries() throws TbitsExceptionClient;
	
	//-------------------For Trn Drawing Number Field Table---------------------//
	public List<TrnDrawingNumber> getDrawingNumberFields() throws TbitsExceptionClient;
	public List<TrnDrawingNumber> saveDrawingNumberFields(List<TrnDrawingNumber> list) throws TbitsExceptionClient;

	//-------------------For Trn Validation Rules Table-------------------------//
	public List<TrnValidationRule> getValidationRulesForProcess(TrnProcess process) throws TbitsExceptionClient;
	public List<TrnValidationRule> saveValidationRulesForProcess(TrnProcess process, List<TrnValidationRule> savedProperties) throws TbitsExceptionClient;

	//-------------------For Process Replication--------------------------------//
	public List<TrnReplicateProcess> getProcessParams(TrnProcess process, BusinessAreaClient srcBa) throws TbitsExceptionClient;
	public List<TrnReplicateProcess> copyProcess(List<TrnReplicateProcess> processParams, TrnProcess currentProcess, BusinessAreaClient destSrcBa) throws TbitsExceptionClient;
	
	//-------------------For creating new Process-------------------------------//
	public List<TrnCreateProcess> getAllProcessParams(TrnProcess process) throws TbitsExceptionClient;
	public Integer getMaxIdTrnProcess() throws TbitsExceptionClient;
	public Integer getMaxIdTrnDropdown() throws TbitsExceptionClient;
	
	public boolean saveNewProcessValues(TrnSaveCreateProcess values) throws TbitsExceptionClient;
	
	
	
	public BusinessAreaClient getBaClient(BusinessAreaClient baclient);
	public SysConfigClient getSysconfigClient(SysConfigClient sysconfig);
	public BAField getBaField(BAField baField);
}
