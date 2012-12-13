package com.tbitsGlobal.jaguar.client.dashboard;

import java.io.Serializable;

public class ScalarParam implements Serializable{
	private String name;
	private String dataType;
	private String value;
	
	public ScalarParam() {
		super();
	}
	
	public ScalarParam(String dataType, String name, String value) {
		this();
		this.dataType = dataType;
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
