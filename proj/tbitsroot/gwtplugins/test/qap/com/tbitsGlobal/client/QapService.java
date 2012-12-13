package qap.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.google.gwt.user.client.rpc.RemoteService;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;

public interface QapService extends RemoteService {

	 HashMap<String,Object> getRequestData(String sysPrefix,int sysid, ArrayList<Integer> requestIds) throws TbitsExceptionClient;

	String getHTMLTransmittalPreviewUsingBirt(HashMap<String, Object> paramTable) throws TbitsExceptionClient;
	
	ArrayList<String> getApplicableBas() throws TbitsExceptionClient;
	
	TbitsTreeRequestData createTransmittal(	HashMap<String, Object> paramTable,	HashMap<Integer, HashMap<String, List<FileClient>>> attachmentInfoClientsMap) throws  TbitsExceptionClient;
	
}
