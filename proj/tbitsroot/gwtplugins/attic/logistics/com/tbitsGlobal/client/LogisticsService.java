package logistics.com.tbitsGlobal.client;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

public interface LogisticsService extends RemoteService{
	public HashMap<String, Stage> getStagesMap() throws TbitsExceptionClient;
	
	public List<TbitsTreeRequestData> getPreStageRequests(Stage stage, int requestId) throws TbitsExceptionClient;
	
	public boolean setPreStageRequests(Stage stage, int requestId, List<Integer> preRequestIds) throws TbitsExceptionClient;
}
