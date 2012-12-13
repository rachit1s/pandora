package com.tbitsGlobal.jaguar.client.events;

import com.tbitsGlobal.jaguar.client.state.AppState;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.DQL;

public class ToSearch extends TbitsBaseEvent {
	private String sysPrefix;
	private DQL dql;
	private int page;
	private int pageSize;
	private boolean getTotalIds;
	private boolean isBasicSearch;
	
	public ToSearch(String sysPrefix, DQL dql, int page, int pageSize, boolean getTotalIds) {
		this(sysPrefix, dql, page, pageSize, getTotalIds, true);
	}
	
	public ToSearch(String sysPrefix, DQL dql, int page, int pageSize, boolean getTotalIds,
			boolean isBasicSearch) {
		super("Searching... Please Wait...", "Error while searching... Try Again!!!");
		
		this.sysPrefix = sysPrefix;
		this.dql = dql;
		this.page = page;
		this.pageSize = pageSize;
		this.getTotalIds = getTotalIds;
		this.isBasicSearch = isBasicSearch;
	}

	@Override
	public boolean beforeFire() {
		if(AppState.checkAppStateIsTill(AppState.FieldsReceived)){
			return true;
		}else{
			AppState.delayTillAppStateIsBefore(AppState.FieldsReceived, this);
		}
		return false;
	}

	public void setDql(DQL dql) {
		this.dql = dql;
	}

	public DQL getDql() {
	
	return dql;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPage() {
		return page;
	}

	public void setGetTotalIds(boolean getTotalIds) {
		this.getTotalIds = getTotalIds;
	}

	public boolean isGetTotalIds() {
		return getTotalIds;
	}

	public void setBasicSearch(boolean isBasicSearch) {
		this.isBasicSearch = isBasicSearch;
	}

	public boolean isBasicSearch() {
		return isBasicSearch;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setSysPrefix(String sysPrefix) {
		this.sysPrefix = sysPrefix;
	}

	public String getSysPrefix() {
		return sysPrefix;
	}
}
