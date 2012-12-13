package com.tbitsGlobal.jaguar.client.events;

import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;

public class ToAddNewRequest extends TbitsBaseEvent{
	private String text;
	
	public ToAddNewRequest(String text) {
		this.setText(text);
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
