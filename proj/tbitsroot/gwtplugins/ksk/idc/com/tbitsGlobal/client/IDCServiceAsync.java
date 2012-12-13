package idc.com.tbitsGlobal.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;

public interface IDCServiceAsync {
	
	void getIDCRequest(ArrayList<Integer> param, String sysPrefix,
			AsyncCallback<RequestData> asyncCallback);

	void getValidBAs(AsyncCallback<ArrayList<String>> callback);

	void getValidSrcBAs(AsyncCallback<ArrayList<String>> callback);

	
}
