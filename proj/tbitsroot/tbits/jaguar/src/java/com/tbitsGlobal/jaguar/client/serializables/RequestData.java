package com.tbitsGlobal.jaguar.client.serializables;

import java.io.Serializable;
import java.util.ArrayList;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;

/**
 * 
 * @author sourabh
 *
 * Carries the whole information about a request. It consists of :
 * <ul>
 * 	<li>system prefix</li>
 * 	<li>request id</li>
 * 	<li>model of request</li>
 * 	<li>List of display groups</li>
 * 	<li>List of fields</li> 
 * </ul>
 * 
 * Typically used when we need to fetch a request of a BA other than the current BA.
 */
public class RequestData implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String sysPrefix;
	private int requestId;
	
	private TbitsTreeRequestData model;
	
	private ArrayList<DisplayGroupClient> displayGroups;
	private ArrayList<BAField> fields;
	
	/**
	 * Constructor. Don't delete. Required for {@link Serializable}
	 */
	public RequestData() {
	}

	public RequestData(String sysPrefix, int requestId,
			TbitsTreeRequestData model, ArrayList<DisplayGroupClient> displayGroups,
			ArrayList<BAField> fields) {
		this();
		this.sysPrefix = sysPrefix;
		this.requestId = requestId;
		this.model = model;
		this.displayGroups = displayGroups;
		this.fields = fields;
	}

	public String getSysPrefix() {
		return sysPrefix;
	}

	public void setSysPrefix(String sysPrefix) {
		this.sysPrefix = sysPrefix;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public TbitsTreeRequestData getModel() {
		return model;
	}

	public void setModel(TbitsTreeRequestData model) {
		this.model = model;
	}

	public ArrayList<DisplayGroupClient> getDisplayGroups() {
		return displayGroups;
	}

	public void setDisplayGroups(ArrayList<DisplayGroupClient> displayGroups) {
		this.displayGroups = displayGroups;
	}

	public ArrayList<BAField> getFields() {
		return fields;
	}

	public void setFields(ArrayList<BAField> fields) {
		this.fields = fields;
	}
}
