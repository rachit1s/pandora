package commons.com.tbitsGlobal.utils.client.cache;

import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;
import commons.com.tbitsGlobal.utils.client.urlManager.TbitsURLManager;

/**
 * 
 * @author sourabh
 * 
 * Cache for Business Areas
 */
public class BusinessAreaCache extends AbstractCache<String, BusinessAreaClient>{
	
	private String currentSysPrefix;
	
	public BusinessAreaCache() {
		super();
	}
	
	@Override
	protected void getFromServer() {
		GlobalConstants.utilService.getBAList(new AsyncCallback<List<BusinessAreaClient>>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error while initializing Business Areas...", caught);
				Log.error("Error while initializing Business Areas...", caught);
			}

			public void onSuccess(List<BusinessAreaClient> result) {
				if(result != null){
					for(BusinessAreaClient ba : result){
						cache.put(ba.getSystemPrefix(), ba);
					}
					onRefresh();
				}
			}
		});
	}
	
	public void onRefresh() {
		if(!TbitsURLManager.getInstance().hasKey(GlobalConstants.TOKEN_BA)){
			String defaultBA = ClientUtils.getCurrentUser().getDefaultBA();
			if(defaultBA != null){
				for(String key : cache.keySet()){
					if(key.toUpperCase().equals(defaultBA.toUpperCase())){
						TbitsURLManager.getInstance().addToken(new HistoryToken(GlobalConstants.TOKEN_BA, key, true));
						break;
					}
				}
			}else
				Window.alert("We could not find the default Business Area : " + defaultBA + ". Please select it from the drop down list.");
		}
	}
	
	/**
	 * Please do not call this function arbitrarily.
	 * It is for the used of certain events
	 * @param currentBA
	 */
	public void setCurrentBA(BusinessAreaClient currentBA) {
		setSysPrefix(currentBA.getSystemPrefix());
	}
	
	private void setSysPrefix(String sysPrefix) {
		currentSysPrefix = sysPrefix;
	}

	public String getCurrentSysPrefix() {
		return currentSysPrefix;
	}
	
	@Override
	public void refresh() {
		super.refresh();
	}

}
