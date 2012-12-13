package logistics.com.tbitsGlobal.client;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

public interface LogisticsServiceAsync{
	void getStagesMap(AsyncCallback<HashMap<String, Stage>> asyncCallback);

	void getPreStageRequests(Stage stage, int requestId,
			AsyncCallback<List<TbitsTreeRequestData>> callback);

	void setPreStageRequests(Stage stage, int requestId,
			List<Integer> preRequestIds, AsyncCallback<Boolean> callback);

}
