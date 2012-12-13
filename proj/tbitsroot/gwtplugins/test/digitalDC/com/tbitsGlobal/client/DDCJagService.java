package digitalDC.com.tbitsGlobal.client;




import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.RemoteService;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;


public interface DDCJagService  extends RemoteService{


	HashMap<String, Object> processRepoFileId(int repoFileId, int sysId,UserClient user) throws TbitsExceptionClient;

	DocNumberFileTuple GetObject(DocNumberFileTuple a);
	
	TbitsTreeRequestData GetObject(TbitsTreeRequestData a);


		
	HashMap<String, Object> processGridData(ArrayList<DocNumberFileTuple> models,int sysid) throws TbitsExceptionClient;

	
	HashMap<String, Object> createDtn(int txnId,UserClient user,HashMap<String, Object> dtnDataMap);

	HashMap<String, Object> AddAndUpdateRequest(int deliverableFieldID,
			int sysId, ArrayList<DocNumberFileTuple> moelData,
			UserClient CurrentUser);

	HashMap<String, Object> fetchConstants(int systemId) throws TbitsExceptionClient;
	
}

