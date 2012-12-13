package browse.com.tbitsglobal.browse.client;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface BrowseServiceAsync {

	void getParamsMap(AsyncCallback<HashMap<String, Params>> callback);

}
