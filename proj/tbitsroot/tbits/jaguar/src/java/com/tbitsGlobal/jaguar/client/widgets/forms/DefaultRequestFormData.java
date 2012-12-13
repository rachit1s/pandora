package com.tbitsGlobal.jaguar.client.widgets.forms;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.store.ListStore;
import com.tbitsGlobal.jaguar.client.cache.DisplayGroupCache;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserDraftClient;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IRequestFormData;

public class DefaultRequestFormData implements IRequestFormData{
	
	/**
	 * Sys Prefix of the BA for which the form is drawn
	 */
	protected String sysPrefix;	
	
	/**
	 * Model if any to fill in the form
	 */
	protected TbitsTreeRequestData requestModel;
	
	/**
	 * Display groups in the applied BA
	 */
	protected ListStore<DisplayGroupClient> displayGroups;
	
	/**
	 * Fields in the applied BA
	 */
	protected ListStore<BAField> fields;
	
	protected UIContext myContext;
	
	public DefaultRequestFormData(UIContext parentContext) {
		super();
		
	    this.myContext = parentContext;
	    if(myContext.hasKey(CONTEXT_REQUEST_DATA)){ //Request of any BA
			RequestData data = myContext.getValue(CONTEXT_REQUEST_DATA, RequestData.class);
			
			this.sysPrefix = data.getSysPrefix();
			
			this.displayGroups = new ListStore<DisplayGroupClient>();
			this.displayGroups.add(data.getDisplayGroups());
			
			this.fields = new ListStore<BAField>();
			this.fields.add(data.getFields());
			
			this.requestModel = data.getModel();
		}else { // Request of current BA. Load display groups and fields from cache
			this.sysPrefix = ClientUtils.getSysPrefix();
			
			if(myContext.hasKey(CONTEXT_DRAFT)){
				UserDraftClient draft = myContext.getValue(CONTEXT_DRAFT, UserDraftClient.class);
				this.requestModel = draft.getModel();
			}else{
				if(myContext.hasKey(CONTEXT_MODEL)){
					this.requestModel = myContext.getValue(CONTEXT_MODEL, TbitsTreeRequestData.class);
			    }
			}
			
			this.displayGroups = new ListStore<DisplayGroupClient>();
		    DisplayGroupCache cache = CacheRepository.getInstance().getCache(DisplayGroupCache.class);
			if(cache.isInitialized()){
				this.displayGroups.add(new ArrayList<DisplayGroupClient>(cache.getValues()));
			}
			
			this.fields = new ListStore<BAField>();
			FieldCache fieldCache = CacheRepository.getInstance().getCache(FieldCache.class);
			if(fieldCache.isInitialized()){
				this.fields.add(new ArrayList<BAField>(fieldCache.getValues()));
			}
		}
	}
	
	public ListStore<BAField> getBAFields(){
		return this.fields;
	}
	
	public TbitsTreeRequestData getRequestModel() {
		return requestModel;
	}
	
	public ListStore<DisplayGroupClient> getDisplayGroups()
	{
		return this.displayGroups;
	}
	
	public void setRequestModel(TbitsTreeRequestData requestModel){
		this.requestModel = requestModel;
	}

	public String getSysPrefix() {
		return sysPrefix;
	}
}
