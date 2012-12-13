package com.tbitsGlobal.admin.client.events;

import java.util.List;

import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;

public class OnFieldsDelete extends TbitsBaseEvent{
	private List<FieldClient> fields;
	
	public OnFieldsDelete(List<FieldClient> fields) {
		super();
		
		this.fields = fields;
	}

	public void setFields(List<FieldClient> fields) {
		this.fields = fields;
	}

	public List<FieldClient> getFields() {
		return fields;
	}
}
