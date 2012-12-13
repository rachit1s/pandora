package billtracking.com.tbitsGlobal.client.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

public interface BillService extends RemoteService {

	HashMap<String, String> getBillProperties();
	List<RequestData> linkBills(ArrayList<Integer> srcReqIdList,
			String srcSysPrefix);
	String getAttachmentMsg(String stateId, TbitsTreeRequestData model,String sysPrefix);
	Boolean belongsToRole(String sysPrefix, String userName, String roleName);
}
