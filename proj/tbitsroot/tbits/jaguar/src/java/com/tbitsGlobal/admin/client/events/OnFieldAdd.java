package com.tbitsGlobal.admin.client.events;

import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;

public class OnFieldAdd extends TbitsBaseEvent{
	private FieldClient field;
	
	public OnFieldAdd(FieldClient field) {
		super();
		
		this.field = field;
	}

	public void setField(FieldClient field) {
		this.field = field;
	}

	public FieldClient getField() {
		return field;
	}
}
