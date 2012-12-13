package mom.com.tbitsGlobal.client.Events;

import mom.com.tbitsGlobal.client.PrintData;

import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;

public class ToAddMeetingTab extends TbitsBaseEvent {
	private PrintData data;

	public ToAddMeetingTab(PrintData data) {
		super();
		this.data = data;
	}

	public void setData(PrintData data) {
		this.data = data;
	}

	public PrintData getData() {
		return data;
	}
}
