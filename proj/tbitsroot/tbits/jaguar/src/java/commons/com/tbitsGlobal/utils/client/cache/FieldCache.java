package commons.com.tbitsGlobal.utils.client.cache;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnFieldsReceived;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.ToRefreshFieldCache;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * 
 * @author sourabh
 * 
 * Cache for fields
 */
public class FieldCache extends AbstractCache<String,BAField> {
	
	public FieldCache() {
		super();
		
		this.subscribe(ToRefreshFieldCache.class, new ITbitsEventHandle<ToRefreshFieldCache>(){
			public void handleEvent(ToRefreshFieldCache event) {
				refresh();
			}});
	}
	
	protected void getFromServer() {
		GlobalConstants.utilService.getFields(ClientUtils.getSysPrefix() ,new AsyncCallback<List<BAField>>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error while loading fields... Please refresh!!!");
				Log.error("Error while loading fields... Please refresh!!!", caught);
			}

			public void onSuccess(List<BAField> result) {
				if(result == null)
					return;
				for(BAField field : result){
					cache.put(field.getName(), field);
				}
				onRefresh();
			}
		});
	}

	public void onRefresh() {
		TbitsEventRegister.getInstance().fireEvent(new OnFieldsReceived());
	}

}
