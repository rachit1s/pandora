package com.tbitsGlobal.admin.client.modelData;

import java.io.Serializable;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class EscalationConditionParametersClient extends TbitsModelData implements Serializable{
	
	public static String NAME="name";
	public static String VALUE="value";
	
	public EscalationConditionParametersClient() {

	}
	
	public EscalationConditionParametersClient(String name,String value){
		this.set(NAME,name);
		this.set(VALUE,value);
	}
	
	public void setName(String name) {
		this.set(NAME,name);
	}

	public String getName() {
		return this.get(NAME);
	}
	public void setValue(String value) {
		this.set(VALUE,value);
	}

	public String getValues() {
		return this.get(VALUE);
	}

	

	
}
