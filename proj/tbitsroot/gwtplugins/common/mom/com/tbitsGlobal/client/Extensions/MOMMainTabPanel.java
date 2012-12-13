package mom.com.tbitsGlobal.client.Extensions;

import mom.com.tbitsGlobal.client.DraftData;
import mom.com.tbitsGlobal.client.MOM;
import mom.com.tbitsGlobal.client.MOMConstants;
import mom.com.tbitsGlobal.client.Meeting;
import mom.com.tbitsGlobal.client.PrintData;
import mom.com.tbitsGlobal.client.Events.OnGetDraftMeeting;
import mom.com.tbitsGlobal.client.Events.ToAddAgendaTab;
import mom.com.tbitsGlobal.client.Events.ToAddMeetingTab;

import com.tbitsGlobal.jaguar.client.widgets.TbitsMainTabPanel;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;

public class MOMMainTabPanel extends TbitsMainTabPanel implements MOMConstants {
	public MOMMainTabPanel() {
		super();
	}

	/**
	 * Adds a Meeting Tab.
	 * 
	 * @param caption
	 */
	public void addMeetingTab(PrintData printData) {
		this.addMeetingTab(0, printData);
	}

	/**
	 * Adds a Meeting Tab with specified meeting id, action items and meeting
	 * data.
	 * 
	 * @param caption
	 * @param draftId
	 * @param actions
	 *            . Action Items.
	 * @param prefilledData
	 *            . Meeting Data
	 */
	public void addMeetingTab(int draftId, PrintData printData) {
		Meeting meeting = new Meeting(draftId, printData);
		MOMGridTab momTab = meeting.getMomGridTab();
		this.add(momTab);
		this.setSelection(momTab);
	}

	protected void addEventHandles() {
		super.addEventHandles();
		
		final ITbitsEventHandle<ToAddMeetingTab> handleAddMeetingTab = new ITbitsEventHandle<ToAddMeetingTab>() {
			public void handleEvent(ToAddMeetingTab event) {
				if(isCurrentBAmom())
					addMeetingTab(event.getData());
			}
		};
		observable.subscribe(ToAddMeetingTab.class, handleAddMeetingTab);

		final ITbitsEventHandle<ToAddAgendaTab> handleAddAgendaTab = new ITbitsEventHandle<ToAddAgendaTab>() {

			public void handleEvent(ToAddAgendaTab event) {
				if(isCurrentBAmom())
					addMeetingTab(event.getData());
			}
		};
		observable.subscribe(ToAddAgendaTab.class, handleAddAgendaTab);

		final ITbitsEventHandle<OnGetDraftMeeting> handleGetDraftMeeting = new ITbitsEventHandle<OnGetDraftMeeting>() {
			public void handleEvent(final OnGetDraftMeeting event) {
				if(isCurrentBAmom()){
					int draftId = event.getDraftId();
					DraftData draftData = event.getDraft();
					addMeetingTab(draftId, draftData.getPrintData());
				}
			}
		};
		observable.subscribe(OnGetDraftMeeting.class, handleGetDraftMeeting);
	}
	
	private boolean isCurrentBAmom(){
		if(MOM.validBAList != null && MOM.validBAList.contains(ClientUtils.getSysPrefix()))
			return true;
		return false;
	}
}
