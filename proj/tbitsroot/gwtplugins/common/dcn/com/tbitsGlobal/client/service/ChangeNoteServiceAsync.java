package dcn.com.tbitsGlobal.client.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;

import dcn.com.tbitsGlobal.shared.ChangeNoteConfig;

public interface ChangeNoteServiceAsync {

	void lookupChangeNoteConfigSysPrefixAndBAType(String aSysPrefix,String aBAType,
			AsyncCallback<ChangeNoteConfig> callback);

	void lookupChangeNoteConfigByBAType(String aBAType,
			AsyncCallback<ArrayList<ChangeNoteConfig>> asyncCallback);
	
	void lookupAllChangeNoteConfig(
			AsyncCallback<ArrayList<ChangeNoteConfig>> asyncCallback);

	void getDCNRequest(ArrayList<Integer> requestIdList, ChangeNoteConfig changeNoteConfig, 
			AsyncCallback<RequestData> callback);

	void lookupSrcBusinessAreaByBAType(String baType,
			AsyncCallback<ArrayList<String>> callback);

	void lookupChangeNoteConfigBySourceSysPrefix(String sysPrefix,
			AsyncCallback<ArrayList<ChangeNoteConfig>> asyncCallback);
	
	void generatePdf(int systemId, int requestId, ChangeNoteConfig changeNoteConfig,
			AsyncCallback<String> asyncCallback);

	void lookupDistinctBATypes(AsyncCallback<HashMap<String, String>> callback);

}
