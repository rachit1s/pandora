package mom.com.tbitsGlobal.client;

import commons.com.tbitsGlobal.utils.client.IFixedFields;

public interface IFormConstants{
	public final String RECORD_TYPE = "record_type";

	public final String FORM_TITLE = IFixedFields.SUBJECT;
	public final String FORM_MEETING_TYPE = IFixedFields.REQUEST_TYPE;
	public final String FORM_START_DATE = "StartDate";
	public final String FORM_VENUE = "Venue";
	public final String FORM_START_TIME = "StartTime";
	public final String FORM_END_DATE = "EndDate";
	public final String FORM_END_TIME = "EndTime";
	public final String FORM_EXT_ATTENDEE = "ExtAttendee";
	public final String FORM_SUBSCRIBERS = IFixedFields.SUBSCRIBER;
	public final String FORM_ASSIGNEES = IFixedFields.ASSIGNEE;
	public final String FORM_ACCESS_TO = "access_to";
	public final String FORM_ATTACHMENTS = IFixedFields.ATTACHMENTS;
}
