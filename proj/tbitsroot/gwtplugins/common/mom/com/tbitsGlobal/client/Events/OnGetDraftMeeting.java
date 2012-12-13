package mom.com.tbitsGlobal.client.Events;

import mom.com.tbitsGlobal.client.DraftData;

import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;

public class OnGetDraftMeeting extends TbitsBaseEvent {
	private int draftId;
	private DraftData draft;

	public OnGetDraftMeeting(int draftId, DraftData draft) {
		super("Retrieving draft... Please Wait...",
				"Error while retrieving draft... Try Again!!!");
		this.draftId = draftId;
		this.draft = draft;
	}

	public void setDraft(DraftData draft) {
		this.draft = draft;
	}

	public DraftData getDraft() {
		return draft;
	}

	public void setDraftId(int draftId) {
		this.draftId = draftId;
	}

	public int getDraftId() {
		return draftId;
	}
}
