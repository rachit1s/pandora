package commons.com.tbitsGlobal.utils.client.Events;


public class OnDeleteDraft extends TbitsBaseEvent{
	private int draftId = 0;
	
	public OnDeleteDraft(int draftId) {
		this.draftId = draftId;
	}

	public void setDraftId(int draftId) {
		this.draftId = draftId;
	}

	public int getDraftId() {
		return draftId;
	}
}
