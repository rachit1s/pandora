package commons.com.tbitsGlobal.utils.client;

import java.util.List;

import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

public class UserListLoadResult extends BaseListLoadResult<UserClient>{

	public UserListLoadResult() {
		super(null);
	}
	
	public UserListLoadResult(List<UserClient> list) {
		super(list);
	}

}
