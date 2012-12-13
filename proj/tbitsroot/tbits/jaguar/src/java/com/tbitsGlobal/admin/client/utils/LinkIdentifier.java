package com.tbitsGlobal.admin.client.utils;

public class LinkIdentifier{
	private String historyKey;
	private String pageCaption;
	
	public LinkIdentifier(String pageCaption, String historyKey) {
		super();
		this.historyKey = historyKey;
		this.pageCaption = pageCaption;
	}

	public void setHistoryKey(String historyKey) {
		this.historyKey = historyKey;
	}

	public String getHistoryKey() {
		return historyKey;
	}

	public void setPageCaption(String pageCaption) {
		this.pageCaption = pageCaption;
	}

	public String getPageCaption() {
		return pageCaption;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.historyKey.equals(((LinkIdentifier)obj).getHistoryKey());
	}
}