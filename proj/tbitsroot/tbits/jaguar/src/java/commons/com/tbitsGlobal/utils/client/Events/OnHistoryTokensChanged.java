package commons.com.tbitsGlobal.utils.client.Events;

import com.extjs.gxt.ui.client.store.ListStore;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;


public class OnHistoryTokensChanged extends TbitsBaseEvent{
	
	private ListStore<HistoryToken> store;
	
	public OnHistoryTokensChanged(ListStore<HistoryToken> store) {
		super();
		
		this.store = store;
	}

	public void setStore(ListStore<HistoryToken> store) {
		this.store = store;
	}

	public ListStore<HistoryToken> getStore() {
		return store;
	}
}
