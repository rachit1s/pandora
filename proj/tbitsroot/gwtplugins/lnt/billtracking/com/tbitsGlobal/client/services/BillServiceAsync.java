package billtracking.com.tbitsGlobal.client.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

public interface BillServiceAsync {

	void getBillProperties(AsyncCallback<HashMap<String,String>> callback);

	void linkBills(ArrayList<Integer> srcReqIdList, String srcSysPrefix,
			AsyncCallback<List<RequestData>> callback);

	void getAttachmentMsg(String stateId, TbitsTreeRequestData model,String sysPrefix, AsyncCallback<String> callback);
	
	void belongsToRole(String sysPrefix, String userName, String roleName,
			AsyncCallback<Boolean> callback);

}
