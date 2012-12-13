package browse.com.tbitsglobal.browse.client;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.RemoteService;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;

public interface BrowseService extends RemoteService{
	public HashMap<String, Params> getParamsMap() throws TbitsExceptionClient;
}
