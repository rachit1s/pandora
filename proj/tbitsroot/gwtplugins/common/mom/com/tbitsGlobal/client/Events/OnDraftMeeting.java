package mom.com.tbitsGlobal.client.Events;

import mom.com.tbitsGlobal.client.Meeting;

import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;

public class OnDraftMeeting extends TbitsBaseEvent {
	private Meeting meeting;
	private String message;

	public OnDraftMeeting(Meeting meeting, String subject) {
		super();
		this.meeting = meeting;
		this.message = subject;
	}

	public Meeting getMeeting() {
		return meeting;
	}

	public void setMeeting(Meeting meeting) {
		this.meeting = meeting;
	}

	public String getMessage() {
		return message;
	}

	public void setSubject(String message) {
		this.message = message;
	}
}
