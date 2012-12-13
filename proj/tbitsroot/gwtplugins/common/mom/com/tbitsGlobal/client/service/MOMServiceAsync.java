package mom.com.tbitsGlobal.client.service;

import java.util.HashMap;
import java.util.List;

import mom.com.tbitsGlobal.client.DraftData;
import mom.com.tbitsGlobal.client.PrintData;

import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;

public interface MOMServiceAsync {
	void addMeeting(String sysPrefix, PrintData printData, AsyncCallback<Integer> callback);

	void preview(String sysPrefix, PrintData printData, AsyncCallback<String> callback);

	void getValidBAList(AsyncCallback<List<String>> callback);
	// dummy
	void getPrimitiveObject(AsyncCallback<POJO> callback);
	
	void saveDraft(int draftId, DraftData printData, AsyncCallback<Integer> callback);
	
	void readDraft(int draftId, AsyncCallback<DraftData> callback);
	
	void deleteDraft(int draftId, AsyncCallback<Boolean> callback);
	
	void getDrafts(AsyncCallback<HashMap<Integer, DraftData>> callback);
}
