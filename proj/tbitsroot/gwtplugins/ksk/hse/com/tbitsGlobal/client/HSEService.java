package hse.com.tbitsGlobal.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;



public interface HSEService extends RemoteService {
	
	public String parseId(String relString,String sysPrefix);
	public RequestData getTransferRequestData(String userLogin,TbitsTreeRequestData requestModel);

	

}
