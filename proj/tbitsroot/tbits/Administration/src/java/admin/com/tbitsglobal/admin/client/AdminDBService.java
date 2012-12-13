package admin.com.tbitsglobal.admin.client;

import java.util.List;

import admin.com.tbitsglobal.admin.client.trn.models.TrnAttachmentList;
import admin.com.tbitsglobal.admin.client.trn.models.TrnDistList;
import admin.com.tbitsglobal.admin.client.trn.models.TrnFieldMapping;
import admin.com.tbitsglobal.admin.client.trn.models.TrnPostProcessValue;
import admin.com.tbitsglobal.admin.client.trn.models.TrnProcess;
import admin.com.tbitsglobal.admin.client.trn.models.TrnProcessParam;

import commons.com.tbitsGlobal.utils.client.Dummy;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.service.UtilService;

public interface AdminDBService extends UtilService {
	
	public List<BusinessAreaClient> getBAs() throws TbitsExceptionClient;
	
	public List<TrnProcess> saveTransmittalProcesses(List<TrnProcess> processes) throws TbitsExceptionClient;
	
	public List<TrnProcessParam> saveProcessParams(TrnProcess process, List<TrnProcessParam> params) throws TbitsExceptionClient;
	
	public List<TrnPostProcessValue> savePostProcessFieldValues(TrnProcess process, List<TrnPostProcessValue> values) throws TbitsExceptionClient;
	
	public List<TrnFieldMapping> saveMapValues(TrnProcess process, List<TrnFieldMapping> mappings) throws TbitsExceptionClient;
	
	public List<TrnDistList> saveDistLists(TrnProcess process, List<TrnDistList> params) throws TbitsExceptionClient;
	
	public List<TrnAttachmentList> saveAttachmentLists(TrnProcess process, List<TrnAttachmentList> params) throws TbitsExceptionClient;
	
	public List<TrnProcess> getTransmittalProcesses() throws TbitsExceptionClient;
	
	public List<TrnProcessParam> getProcessParams(TrnProcess process) throws TbitsExceptionClient;
	
	public List<TrnPostProcessValue> getPostProcessFieldValues(TrnProcess process) throws TbitsExceptionClient;
	
	public List<TrnFieldMapping> getMapValues(TrnProcess process) throws TbitsExceptionClient;
	
	public List<TrnDistList> getDistList(TrnProcess process) throws TbitsExceptionClient;
	
	public List<TrnAttachmentList> getAttachmentList(TrnProcess process) throws TbitsExceptionClient;
	
	Dummy getDummy(Dummy dummy);
	
	BusinessAreaClient getDummyBA(BusinessAreaClient ba);
}
