package mom.com.tbitsGlobal.client;

import mom.com.tbitsGlobal.client.service.MOMService;
import mom.com.tbitsGlobal.client.service.MOMServiceAsync;

import com.google.gwt.core.client.GWT;

public interface MOMConstants {
	public final MOMServiceAsync momService = GWT.create(MOMService.class);
	
	public static String CAPTION_MEETING = "Meeting";
	public static String CAPTION_AGENDA = "Agenda";
	
	public final String RECORDTYPE = "recordtype";
	public final String ACTION_ITEM = "Action Item";
	public final String AGENDA_ITEM = "Agenda Item";
	public final String MEETING = "Meeting";
	public final String AGENDA = "Agenda";
	
	public final String SIGNED = "Signed";
	public final String AGREED = "Agreed";
	
	public final String DATE_FORMAT = "yyyy-MM-dd";
}
