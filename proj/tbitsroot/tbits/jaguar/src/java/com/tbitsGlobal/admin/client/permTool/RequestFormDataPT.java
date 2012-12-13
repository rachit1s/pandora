package com.tbitsGlobal.admin.client.permTool;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.store.ListStore;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.client.requestFormInterfaces.IRequestFormData;

/**
 * Implementation of IRequestFormData for the permissioning tool. Uses PTCache to fetch the fields and display groups.
 * 
 * @author Karan Gupta
 *
 */
public class RequestFormDataPT implements IRequestFormData {

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
	
	public RequestFormDataPT(TbitsTreeRequestData requestModel) {
		
		super();
		
		this.sysPrefix = ClientUtils.getSysPrefix();
		
		this.requestModel = requestModel;
		
		PTCache cache = PTCache.getInstance();
		
		this.displayGroups = new ListStore<DisplayGroupClient>();
		if(cache.getDisplayGroupValues() != null)
			this.displayGroups.add(new ArrayList<DisplayGroupClient>(cache.getDisplayGroupValues()));
		
		this.fields = new ListStore<BAField>();
		if(cache.getFieldValues() != null)
			this.fields.add(new ArrayList<BAField>(cache.getFieldValues()));
	}
	
	public ListStore<BAField> getBAFields() {
		return fields;
	}

	public ListStore<DisplayGroupClient> getDisplayGroups() {
		return displayGroups;
	}

	public TbitsTreeRequestData getRequestModel() {
		return requestModel;
	}

	public String getSysPrefix() {
		return sysPrefix;
	}

	public void setRequestModel(TbitsTreeRequestData requestModel) {
		this.requestModel = requestModel;
	}

}
