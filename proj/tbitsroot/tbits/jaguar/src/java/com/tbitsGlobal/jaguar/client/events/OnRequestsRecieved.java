package com.tbitsGlobal.jaguar.client.events;


import commons.com.tbitsGlobal.utils.client.DQLResults;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.DQL;

public class OnRequestsRecieved extends TbitsBaseEvent{
	private DQL dql;
	private boolean isBasicSearch;
	private DQLResults dqlResults;
	private int page;
	private int pageSize;
	private String sortOrderColumn;
	private int sortDirection;
	
	public OnRequestsRecieved(DQL dql, boolean isBasicSearch, DQLResults dqlResults, int page, int pageSize) {
		super("Rendering Search Results... Please wait...", "Error while rendering Search Results... Please see logs for details");
		
		this.dql = dql;
		this.isBasicSearch = isBasicSearch;
		this.dqlResults = dqlResults;
		this.page = page;
		this.pageSize = pageSize;
	}

	public void setBasicSearch(boolean isBasicSearch) {
		this.isBasicSearch = isBasicSearch;
	}

	public boolean isBasicSearch() {
		return isBasicSearch;
	}

	public void setDql(DQL dql) {
		this.dql = dql;
	}

	public DQL getDql() {
		return dql;
	}

	public void setDqlResults(DQLResults dqlResults) {
		this.dqlResults = dqlResults;
	}

	public DQLResults getDqlResults() {
		return dqlResults;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPage() {
		return page;
	}

	public void setSortDirection(int sortDirection) {
		this.sortDirection = sortDirection;
	}

	public void setSortOrderColumns(String sortColumn) {
		this.sortOrderColumn = sortColumn;
	}

	public String getSortOrderColumn() {
		// TODO Auto-generated method stub
		return sortOrderColumn;
	}

	public int getSortDirection() {
		// TODO Auto-generated method stub
		return sortDirection;
	}
}
