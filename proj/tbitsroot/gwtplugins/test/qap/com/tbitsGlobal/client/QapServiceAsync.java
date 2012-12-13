package qap.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;

public interface QapServiceAsync {

	void getRequestData(String sysPrefix, int sysid,
			ArrayList<Integer> requestIds,
			AsyncCallback<HashMap<String, Object>> callback);

	void getHTMLTransmittalPreviewUsingBirt(HashMap<String, Object> paramTable,	AsyncCallback<String> asyncCallback);

	void createTransmittal(
			HashMap<String, Object> paramTable,
			HashMap<Integer, HashMap<String, List<FileClient>>> attachmentInfoClientsMap,
			AsyncCallback<TbitsTreeRequestData> callback);

	void getApplicableBas(AsyncCallback<ArrayList<String>> callback);



	

}
