package idc.com.tbitsGlobal.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;

public interface IDCService extends RemoteService {
	RequestData getIDCRequest(ArrayList<Integer> param, String sysPrefix);

	ArrayList<String> getValidBAs() throws TbitsExceptionClient;

	ArrayList<String> getValidSrcBAs() throws TbitsExceptionClient;
}
