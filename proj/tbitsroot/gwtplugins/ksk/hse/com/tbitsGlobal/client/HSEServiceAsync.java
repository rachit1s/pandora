package hse.com.tbitsGlobal.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

public interface HSEServiceAsync {

	void parseId(String relString, String sysPrefix,
			AsyncCallback<String> callback);

	void getTransferRequestData(String userLogin,TbitsTreeRequestData requestModel,
			AsyncCallback<RequestData> callback);

	

}
