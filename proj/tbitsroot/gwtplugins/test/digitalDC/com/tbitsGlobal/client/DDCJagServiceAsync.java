package digitalDC.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public interface DDCJagServiceAsync {

	void processRepoFileId(int repoFileId, int sysId,UserClient user, AsyncCallback<HashMap<String, Object>> callback);

	void GetObject(DocNumberFileTuple a,
			AsyncCallback<DocNumberFileTuple> callback);

	void GetObject(TbitsTreeRequestData a,
			AsyncCallback<TbitsTreeRequestData> callback);
	

	

	void processGridData(ArrayList<DocNumberFileTuple> models,int sysid,
			AsyncCallback<HashMap<String, Object>> callback);
	
	void AddAndUpdateRequest( int deliverableFieldID,
			int sysId, ArrayList<DocNumberFileTuple> moelData,UserClient CurrentUser,AsyncCallback<	HashMap<String,Object> > callback);
	
	void createDtn(int txnId,UserClient user,HashMap<String, Object> dtnDataMap,AsyncCallback<HashMap<String,Object>> callback);

	void fetchConstants(int systemId,
			AsyncCallback<HashMap<String, Object>> asyncCallback);


}