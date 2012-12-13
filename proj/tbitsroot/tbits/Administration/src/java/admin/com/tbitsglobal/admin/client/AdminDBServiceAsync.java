package admin.com.tbitsglobal.admin.client;

import java.util.List;

import admin.com.tbitsglobal.admin.client.trn.models.TrnAttachmentList;
import admin.com.tbitsglobal.admin.client.trn.models.TrnDistList;
import admin.com.tbitsglobal.admin.client.trn.models.TrnFieldMapping;
import admin.com.tbitsglobal.admin.client.trn.models.TrnPostProcessValue;
import admin.com.tbitsglobal.admin.client.trn.models.TrnProcess;
import admin.com.tbitsglobal.admin.client.trn.models.TrnProcessParam;

import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.Dummy;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.service.UtilServiceAsync;

public interface AdminDBServiceAsync extends UtilServiceAsync{
	void getBAs(AsyncCallback<List<BusinessAreaClient>> callback);
	
	void saveTransmittalProcesses(List<TrnProcess> processes, AsyncCallback<List<TrnProcess>> callback);
	
	void saveProcessParams(TrnProcess process, List<TrnProcessParam> params, AsyncCallback<List<TrnProcessParam>> callback);
	
	void savePostProcessFieldValues(TrnProcess process, List<TrnPostProcessValue> values, AsyncCallback<List<TrnPostProcessValue>> callback);
	
	void saveMapValues(TrnProcess process, List<TrnFieldMapping> mappings, AsyncCallback<List<TrnFieldMapping>> callback);
	
	void saveDistLists(TrnProcess process, List<TrnDistList> params, AsyncCallback<List<TrnDistList>> callback);
	
	void saveAttachmentLists(TrnProcess process, List<TrnAttachmentList> params, AsyncCallback<List<TrnAttachmentList>> callback);

	void getTransmittalProcesses(AsyncCallback<List<TrnProcess>> callback);
	
	void getProcessParams(TrnProcess process, AsyncCallback<List<TrnProcessParam>> callback);
	
	void getPostProcessFieldValues(TrnProcess process, AsyncCallback<List<TrnPostProcessValue>> callback);
	
	void getMapValues(TrnProcess process, AsyncCallback<List<TrnFieldMapping>> callback);
	
	void getDistList(TrnProcess process, AsyncCallback<List<TrnDistList>> callback);
	
	void getAttachmentList(TrnProcess process, AsyncCallback<List<TrnAttachmentList>> callback);
	
	void getDummy(Dummy dummy, AsyncCallback<Dummy> callback);
	
	void getDummyBA(BusinessAreaClient ba, AsyncCallback<BusinessAreaClient> callback);
}
