package corrGeneric.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.cache.UserCacheAdmin;
import com.tbitsGlobal.admin.client.state.AppState;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class CorrAdminUtils {
	
	/**
	 * Get the list of users
	 * @return List of all the users
	 */
	public static List<UserClient> getUsersList(){
		if(AppState.checkAppStateIsTill(AppState.UserReceived)){
			UserCacheAdmin cache = CacheRepository.getInstance().getCache(UserCacheAdmin.class);
			List<UserClient> userList = new ArrayList<UserClient>(cache.getValues());
			if((userList != null) && (!userList.isEmpty())){
				return userList;
			}else{
				TbitsInfo.error("Could not get the list of users... Please refresh....");
				Log.error("Error while getting list of users");
			}
		}
		return null;
	}
	
	public static ComboBox<BusinessAreaClient> getBACombo(){
		ListStore<BusinessAreaClient> baStore = new ListStore<BusinessAreaClient>();
		final ComboBox<BusinessAreaClient> baCombo = new ComboBox<BusinessAreaClient>();
		baCombo.setStore(baStore);
		baCombo.setDisplayField(BusinessAreaClient.SYSTEM_PREFIX);
		baCombo.setTemplate(getBATemplate());
		
		APConstants.apService.getBAList(new AsyncCallback<List<BusinessAreaClient>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error fetching business area list from database...", caught);
				Log.error("Error fetching business area list from database...", caught);
				caught.printStackTrace();
			}
			public void onSuccess(List<BusinessAreaClient> result) {
				baCombo.getStore().add(result);
			}});
		
		return baCombo;
	}
	
	private static native String getBATemplate() /*-{ 
	return  [ 
	'<tpl for=".">', 
	'<div class="x-combo-list-item">{display_name} [{system_prefix}]</div>', 
	'</tpl>' 
	].join(""); 
	}-*/; 
	
	/**
	 * 
	 * @param userListStore - The values to be populated into the liststore
	 * @return User list combo box
	 */
	public static ComboBox<UserClient> getUserCombo(){
		ListStore<UserClient> userListStore = new ListStore<UserClient>();
		userListStore.add(getUsersList());
		final ComboBox<UserClient> userComboBox = new ComboBox<UserClient>();
		userComboBox.setStore(userListStore);
		userComboBox.setDisplayField(UserClient.USER_LOGIN);
		userComboBox.setForceSelection(false);
		userComboBox.setTriggerAction(TriggerAction.ALL);
		userComboBox.setLoadingText("Loding Users...");
		userComboBox.setSelectOnFocus(true);
		userComboBox.getStore().sort(UserClient.DISPLAY_NAME, SortDir.ASC);
		
		return userComboBox;
	}
}
