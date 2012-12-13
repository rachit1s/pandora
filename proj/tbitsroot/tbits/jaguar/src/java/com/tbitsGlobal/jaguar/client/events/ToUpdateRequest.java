package com.tbitsGlobal.jaguar.client.events;

import com.extjs.gxt.ui.client.widget.treegrid.EditorTreeGrid;
import com.tbitsGlobal.jaguar.client.state.AppState;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;

public class ToUpdateRequest extends TbitsBaseEvent {
	private EditorTreeGrid<TbitsTreeRequestData> sourceGrid;
	private TbitsTreeRequestData data;
	private int requestId;
	
	public ToUpdateRequest(int requestId) {
		super();
		this.requestId = requestId;
	}

	public ToUpdateRequest(TbitsTreeRequestData data,
			EditorTreeGrid<TbitsTreeRequestData> sourceGrid) {
		super();
		this.data = data;
		this.sourceGrid = sourceGrid;
	}

	public EditorTreeGrid<TbitsTreeRequestData> getSourceGrid() {
		return sourceGrid;
	}

	public void setSourceGrid(EditorTreeGrid<TbitsTreeRequestData> sourceGrid) {
		this.sourceGrid = sourceGrid;
	}

	public TbitsTreeRequestData getData() {
		return data;
	}

	public void setData(TbitsTreeRequestData data) {
		this.data = data;
	}
	
	@Override
	public boolean beforeFire() {
		if(AppState.checkAppStateIsBefore(AppState.FieldsReceived)){
			return true;
		}else{
			AppState.delayTillAppStateIsBefore(AppState.FieldsReceived, this);
		}
		return false;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public int getRequestId() {
		return requestId;
	}
}
