package transmittal.com.tbitsGlobal.server;

import java.util.HashMap;
import java.util.List;


import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.SysConfigClient;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;
import transmittal.com.tbitsGlobal.client.admin.TrnAdminService;
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
import transmittal.com.tbitsGlobal.server.admin.AttachmentListMap;
import transmittal.com.tbitsGlobal.server.admin.CreateProcess;
import transmittal.com.tbitsGlobal.server.admin.DistListMap;
import transmittal.com.tbitsGlobal.server.admin.DrawingNumberField;
import transmittal.com.tbitsGlobal.server.admin.Dropdown;
import transmittal.com.tbitsGlobal.server.admin.PostTrnFieldMap;
import transmittal.com.tbitsGlobal.server.admin.ReplicateProcess;
import transmittal.com.tbitsGlobal.server.admin.SrcTargetFieldMap;
import transmittal.com.tbitsGlobal.server.admin.TrnProcessParams;
import transmittal.com.tbitsGlobal.server.admin.TrnProcesses;
import transmittal.com.tbitsGlobal.server.admin.ValidationRules;

public class TrnAdminServiceImpl extends TbitsRemoteServiceServlet implements TrnAdminService {

	public List<TrnProcess> getTransmittalProcesses() throws TbitsExceptionClient{
		return TrnProcessParams.getTransmittalProcesses();
	}

	public List<TrnProcess> getTransmittalProcessesForBa(BusinessAreaClient currentSrcBa) throws TbitsExceptionClient {
		return TrnProcessParams.getTransmittalProcessesForBa(currentSrcBa);
	}
	
	public List<TrnProcessParam> getProcessParams(TrnProcess process) throws TbitsExceptionClient{
		return TrnProcessParams.getProcessParams(process);
	}

	public List<TrnProcessParam> saveProcessParams(TrnProcess process, List<TrnProcessParam> params) throws TbitsExceptionClient {
		return TrnProcessParams.saveProcessParams(process, params);
	}
	
	public List<TrnProcess> saveTransmittalProcesses(List<TrnProcess> processes) throws TbitsExceptionClient {
		return TrnProcesses.saveTransmittalProcesses(processes);
	}
	
	public List<TrnPostProcessValue> savePostProcessFieldValues(TrnProcess process, List<TrnPostProcessValue> values) throws TbitsExceptionClient {
		return PostTrnFieldMap.savePostProcessFieldValues(process, values);
	}
	
	public List<TrnPostProcessValue> getPostProcessFieldValues(TrnProcess process) throws TbitsExceptionClient {
		return PostTrnFieldMap.getPostProcessFieldValues(process, this.getRequest());
	}
	
	public List<TrnFieldMapping> getSrcTargetFieldMap(TrnProcess process) throws TbitsExceptionClient {
		return SrcTargetFieldMap.getSrcTargetFieldMap(process, this.getRequest());
	}
	
	public List<TrnFieldMapping> saveSrcTargetFieldMap(TrnProcess process, List<TrnFieldMapping> mappings) throws TbitsExceptionClient {
		return SrcTargetFieldMap.saveSrcTargetFieldMap(process, mappings);
	}
	
	public List<TrnDistList> getDistList(TrnProcess process)throws TbitsExceptionClient {
		return DistListMap.getDistList(process);
	}
	
	public List<TrnDistList> saveDistLists(TrnProcess process, List<TrnDistList> params) throws TbitsExceptionClient {
		return DistListMap.saveDistLists(process, params);
	}
	
	public List<TrnAttachmentList> saveAttachmentLists(TrnProcess process, List<TrnAttachmentList> params) throws TbitsExceptionClient {
		return AttachmentListMap.saveAttachmentLists(process, params);
	}
	
	public List<TrnAttachmentList> getAttachmentList(TrnProcess process)throws TbitsExceptionClient {
		return AttachmentListMap.getAttachmentList(process, this.getRequest());
	}
	
	public List<TrnDropdown> getDropdownTable(BusinessAreaClient ba) throws TbitsExceptionClient {
		return Dropdown.getDropdownTable(ba);
	}

	public List<TrnDropdown> saveDropdownTable(BusinessAreaClient ba, List<TrnDropdown> list) throws TbitsExceptionClient {
		return Dropdown.saveDropdownTable(ba, list);
	}
	
	public List<TrnDropdown> getAllDropdownEntries()throws TbitsExceptionClient {
		return Dropdown.getAllDropdownEntries();
	}
	
	public List<TrnDrawingNumber> getDrawingNumberFields() throws TbitsExceptionClient {
		return DrawingNumberField.getDrawingNumberFields(this.getRequest());
	}
	
	public List<TrnDrawingNumber> saveDrawingNumberFields(List<TrnDrawingNumber> list) throws TbitsExceptionClient {
		return DrawingNumberField.saveDrawingNumberFields(list);
	}

	public List<TrnValidationRule> getValidationRulesForProcess(TrnProcess process) throws TbitsExceptionClient {
		return ValidationRules.getValidationRulesForProcess(process, this.getRequest());
	}
	
	public List<TrnValidationRule> saveValidationRulesForProcess(TrnProcess process, List<TrnValidationRule> savedProperties)throws TbitsExceptionClient {
		return ValidationRules.saveValidationRulesForProcess(process, savedProperties);
	}
	
	public List<TrnReplicateProcess> getProcessParams(TrnProcess process, BusinessAreaClient srcBa) throws TbitsExceptionClient {
		return ReplicateProcess.getProcessParams(process, srcBa);
	}
	
	public List<TrnReplicateProcess> copyProcess(List<TrnReplicateProcess> processParams, TrnProcess currentProcess, BusinessAreaClient destSrcBa) throws TbitsExceptionClient {
		return ReplicateProcess.copyProcess(processParams, currentProcess, destSrcBa, this.getRequest());
	} 
	
	public List<TrnCreateProcess> getAllProcessParams(TrnProcess process)throws TbitsExceptionClient {
		return CreateProcess.getAllProcessParams(process, this.getRequest());
	}
	
	public Integer getMaxIdTrnProcess() throws TbitsExceptionClient {
		return CreateProcess.getMaxIdTrnProcess();
	}
	
	public Integer getMaxIdTrnDropdown() throws TbitsExceptionClient {
		return CreateProcess.getMaxIdTrnDropdown();
	}
	
	public boolean saveNewProcessValues(TrnSaveCreateProcess values)throws TbitsExceptionClient {
		return CreateProcess.saveNewProcessValues(values);
	}
	
	//---------Dummy Method(s) To Tell GWT that the specified type is serializable-------//
	public BusinessAreaClient getBaClient(BusinessAreaClient baclient) {
		return baclient;
	}

	public BAField getBaField(BAField baField) {
		return baField;
	}
	
	public SysConfigClient getSysconfigClient(SysConfigClient sysconfig) {
		return sysconfig;
	}
}
