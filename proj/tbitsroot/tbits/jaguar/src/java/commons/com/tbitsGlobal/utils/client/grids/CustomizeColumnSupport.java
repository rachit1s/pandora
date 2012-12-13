package commons.com.tbitsGlobal.utils.client.grids;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;


import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.ColPrefs;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * 
 * @author sourabh
 * 
 * The Grids that want to give customize column support have to include an instance of this class
 */
public class CustomizeColumnSupport implements IFixedFields{
	
	private GridColumnView viewId;
	
	public CustomizeColumnSupport(GridColumnView viewId) {
		super();
		
		this.viewId = viewId;
	}
	
	/**
	 * Shows the Cusomize Columns window for the prefs
	 * @param prefs. The current prefs set for the grid
	 */
	public void showWindow(List<ColPrefs> prefs){
		if(viewId != null){
			FieldCache cache = CacheRepository.getInstance().getCache(FieldCache.class);
			final ArrayList<ColPrefs> fields = new ArrayList<ColPrefs>();
			for(BAField baField : cache.getValues()){
				if(baField.getName().equals(REQUEST))
					continue;
				if(shouldShowField(baField)){
					ColPrefs c = new ColPrefs(baField);
					fields.add(c);
				}
			}
			
			List<ColPrefs> currentPrefs = new ArrayList<ColPrefs>();
			
			for(ColPrefs pref : prefs){
				BAField field = cache.getObject(pref.getName());
				if(field != null && shouldShowField(field)){
					pref.setDisplayName(field.getDisplayName());
					currentPrefs.add(pref);
				}
			}
			
			String[] arr = {ColPrefs.DISPLAY_NAME, ColPrefs.NAME};
			ColPrefCustomizeColWindow window = new ColPrefCustomizeColWindow(fields, currentPrefs, ColPrefs.DISPLAY_NAME, arr){
				@Override
				protected void save(List<ColPrefs> fields) {
					saveColPref(fields);
				}
			};
			window.show();
		}
	}
	
	/**
	 * @param baField
	 * @return True if the field is to be shown for configuration
	 */
	public boolean shouldShowField(BAField baField){
		return baField.isCanViewInBA();
	}
	
	/**
	 * Saves the Preferences
	 * @param prefs
	 */
	protected void saveColPref(final List<ColPrefs> prefs) {
		beforeSave();
		
		final int sysId = ClientUtils.getCurrentBA().getSystemId();
		
		GlobalConstants.utilService.setColPreferences(ClientUtils.getCurrentUser().getUserId(), viewId.getId(), sysId, prefs,
			new AsyncCallback<String>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Failed to save Preferences... See logs for Details...", caught);
					Log.error("Failed to save Preferences... See logs for Details...", caught);
				}
				public void onSuccess(String result) {
					HashMap<Integer, HashMap<Integer, List<ColPrefs>>> allPrefs = ClientUtils.getCurrentUser().getColPrefs();
					HashMap<Integer, List<ColPrefs>> prefsForBa = allPrefs.get(sysId);
					if(prefsForBa != null){
						if(prefsForBa.get(viewId.getId()) != null)
							prefsForBa.get(viewId.getId()).clear();
						else
							prefsForBa.put(viewId.getId(),new ArrayList<ColPrefs>());
					}else{
						allPrefs.put(sysId,new HashMap<Integer, List<ColPrefs>>());
						allPrefs.get(sysId).put(viewId.getId(), new ArrayList<ColPrefs>());
						prefsForBa = allPrefs.get(sysId);
					}
					prefsForBa.get(viewId.getId()).addAll(prefs);
					
					TbitsInfo.info("Column preferences saved successfully");
					
					afterSave(prefs);
				}
			});
	}
	
	/**
	 * Called before saving the prefs. Default implemetation is empty
	 */
	protected void beforeSave(){};
	
	/**
	 * Called after the prefs have been saved. Default implementation is empty
	 * @param prefs. The prefs post save operation
	 */
	protected void afterSave(List<ColPrefs> prefs){}
}
