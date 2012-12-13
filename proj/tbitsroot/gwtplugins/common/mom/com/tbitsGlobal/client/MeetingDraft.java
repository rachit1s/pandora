package mom.com.tbitsGlobal.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

public class MeetingDraft implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<TbitsTreeRequestData> actionsData;
	private TbitsTreeRequestData headerData;

	public MeetingDraft() {
		actionsData = new ArrayList<TbitsTreeRequestData>();
		headerData = new TbitsTreeRequestData();
	}

	public MeetingDraft(TbitsTreeRequestData headerData,
			List<TbitsTreeRequestData> actionsData) {
		this();
		this.actionsData = actionsData;
		this.headerData = headerData;
	}

	public List<TbitsTreeRequestData> getActionsData() {
		return actionsData;
	}

	public void setActionsData(List<TbitsTreeRequestData> actionsData) {
		this.actionsData = actionsData;
	}

	public TbitsTreeRequestData getHeaderData() {
		return headerData;
	}

	public void setHeaderData(TbitsTreeRequestData headerData) {
		this.headerData = headerData;
	}
}
