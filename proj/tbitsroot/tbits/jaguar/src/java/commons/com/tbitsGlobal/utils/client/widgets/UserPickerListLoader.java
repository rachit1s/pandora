package commons.com.tbitsGlobal.utils.client.widgets;

import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.ModelReader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.UserListLoadResult;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldMultiValue;

public class UserPickerListLoader extends BaseListLoader<UserListLoadResult>{
	
	public UserPickerListLoader(RpcProxy<UserListLoadResult> proxy){
		super(proxy);
	}
	
	public UserPickerListLoader(RpcProxy<UserListLoadResult> proxy, ModelReader reader) {
		super(proxy, reader);
	}

	public static UserPickerListLoader getloader(final BAFieldMultiValue baField) {
		RpcProxy<UserListLoadResult> rpcProxy = new RpcProxy<UserListLoadResult>(){
			@Override
			protected void load(Object loadConfig,
					AsyncCallback<UserListLoadResult> callback) {
				BasePagingLoadConfig bpLoadConfig = (BasePagingLoadConfig) loadConfig;
				String query = bpLoadConfig.get("query");
				GlobalConstants.utilService.getActiveUsers(query, baField, callback);
			}};
			
			ModelReader reader = new ModelReader(); 
			
		return new UserPickerListLoader(rpcProxy, reader);
	}


}
