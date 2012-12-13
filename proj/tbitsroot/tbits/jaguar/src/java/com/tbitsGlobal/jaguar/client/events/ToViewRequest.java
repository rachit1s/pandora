package com.tbitsGlobal.jaguar.client.events;

import com.tbitsGlobal.jaguar.client.state.AppState;
import commons.com.tbitsGlobal.utils.client.Captions;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;

public class ToViewRequest extends TbitsBaseEvent {
	private TbitsTreeRequestData data;
	private int requestId;
	
	public ToViewRequest(int requestId) {
		super();
		this.requestId = requestId;
	}
	
	public ToViewRequest(TbitsTreeRequestData data) {
		super();
		this.setData(data);
	}

	public TbitsTreeRequestData getData() {
		return data;
	}

	public void setData(TbitsTreeRequestData data) {
		this.data = data;
		if(this.data.getRequestId() != 0){
			this.requestId = this.data.getRequestId();
		}
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}
	
	@Override
	public boolean beforeFire() {
		if(AppState.checkAppStateIsBefore(AppState.FieldsReceived)){
			if(requestId != 0){
				this.setMessage("Displaying " + Captions.getRecordDisplayName() + " for tBits Id : " + requestId + "... Please Wait...");
				this.setError("Could not display " + Captions.getRecordDisplayName() + " for tBits Id : " + requestId + "... Try Again!!!");
			}else{
				this.setMessage("Displaying " + Captions.getRecordDisplayName() + "... Please Wait...");
				this.setError("Could not display " + Captions.getRecordDisplayName() + "... Try Again!!!");
			}
			return true;
		}else{
			AppState.delayTillAppStateIsBefore(AppState.FieldsReceived, this);
		}
		return false;
	}
}
