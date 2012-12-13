package mom.com.tbitsGlobal.client.service;

import java.util.HashMap;
import java.util.List;

import mom.com.tbitsGlobal.client.DraftData;
import mom.com.tbitsGlobal.client.PrintData;

import com.google.gwt.user.client.rpc.RemoteService;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;

public interface MOMService extends RemoteService {
	int addMeeting(String sysPrefix, PrintData printData) throws TbitsExceptionClient;

	String preview(String sysPrefix, PrintData printData) throws TbitsExceptionClient;
	
	List<String> getValidBAList();
	// dummy
	POJO getPrimitiveObject();
	
	int saveDraft(int draftId, DraftData printData) throws TbitsExceptionClient;
	DraftData readDraft(int draftId) throws TbitsExceptionClient;
	boolean deleteDraft(int draftId) throws TbitsExceptionClient;
	HashMap<Integer, DraftData> getDrafts() throws TbitsExceptionClient;
}
