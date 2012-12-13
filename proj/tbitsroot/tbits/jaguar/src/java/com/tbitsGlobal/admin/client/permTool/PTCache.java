package com.tbitsGlobal.admin.client.permTool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;

/**
 * Caches the display groups and fields for providing the request layout in the permission tool.
 * This class needs to be initialised when the permissioning tool is being loaded.
 * 
 * @author Karan Gupta
 *
 */
public class PTCache {

	// Singleton instance
	private static PTCache instance;
	
	// cache and its init flags
	private boolean dginit = false;
	private boolean finit = false;
	private HashMap<Integer, DisplayGroupClient> dgcache;
	private HashMap<String, BAField> fcache;
	
	/**
	 * Private constructor
	 */
	private PTCache(){
		
		dgcache = new HashMap<Integer, DisplayGroupClient>();
		fcache = new HashMap<String, BAField>();
		
		APConstants.apService.getDisplayGroups(ClientUtils.getSysPrefix(),new AsyncCallback<ArrayList<DisplayGroupClient>>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error while loading display groups. Please reinitialise admin panel.");
			}
			public void onSuccess(ArrayList<DisplayGroupClient> result) {
				for(DisplayGroupClient d:result){
					dgcache.put(d.getId(), d);
					dginit = true;
			    }
			}
	    });
		
		APConstants.apService.getFields(ClientUtils.getSysPrefix() ,new AsyncCallback<List<BAField>>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error while loading fields. Please reinitialise admin panel.");
			}

			public void onSuccess(List<BAField> result) {
				if(result == null)
					return;
				for(BAField field : result){
					fcache.put(field.getName(), field);
					finit = true;
				}
			}
		});
	}
	
	/**
	 * Method to get the singleton instance of the cache.
	 * 
	 * @return the singleton nstance of the cache
	 */
	public static PTCache getInstance(){
		if(instance == null)
			instance = new PTCache();
		return instance;
	}
	
	// Getter and setter functions
	public Collection<DisplayGroupClient> getDisplayGroupValues(){
		if(!dginit)
			return null;
		return dgcache.values();
	}
	
	public Collection<BAField> getFieldValues(){
		if(!finit)
			return null;
		return fcache.values();
	}
	
	/**
	 * Explicit initialiser of the cache instance
	 */
	public static void initialise(){
		instance = new PTCache();
	}
	
}
