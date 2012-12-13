package com.tbitsGlobal.admin.client.modelData;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class ReportParamClient extends TbitsModelData{
	public static final String REPORT_ID = "report_id";
	public static final String PARAM_NAME = "param_name";
	public static final String PARAM_VALUE = "param_value";
	
	public ReportParamClient() {
		super();
	}
	
	public void setReportId(int id){
		this.set(REPORT_ID, id);
	}
	
	public int getReportId(){
		return (Integer)this.get(REPORT_ID);
	}
	
	public void setName(String name){
		this.set(PARAM_NAME, name);
	}
	
	public String getName(){
		return (String)this.get(PARAM_NAME);
	}
	
	public void setValue(String value){
		this.set(PARAM_VALUE, value);
	}
	
	public String getValue(){
		return (String)this.get(PARAM_VALUE);
	}
}
