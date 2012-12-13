package commons.com.tbitsGlobal.utils.client.search.grids;

import java.util.List;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.OnChangeBA;
import commons.com.tbitsGlobal.utils.client.Events.OnFieldsReceived;
import commons.com.tbitsGlobal.utils.client.bafield.ColPrefs;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGrid;
import commons.com.tbitsGlobal.utils.client.search.searchpanel.DQL;

/**
 * 
 * @author sourabh
 * 
 * Abstract grid for all the search grids.
 */
public abstract class AbstractSearchGrid  extends RequestsViewGrid{
	
	private DQL dql;
	
	/**
	 * Contructor
	 * @param prefs. The column preferences
	 */
	public AbstractSearchGrid(String sysPrefix) {
		super(sysPrefix);
		
		if(this.sysPrefix.equals(ClientUtils.getSysPrefix())){
			/**
			 * Make the column model again when the fields arrive.
			 */
			final ITbitsEventHandle<OnFieldsReceived> fieldsReceivedHandle = new ITbitsEventHandle<OnFieldsReceived>(){
				public void handleEvent(OnFieldsReceived event) {
					List<ColPrefs> prefs = ClientUtils.getPrefsForView(getViewId(), AbstractSearchGrid.this.sysPrefix);
					setPrefs(prefs);
				}};
				
			observable.subscribe(OnFieldsReceived.class, fieldsReceivedHandle);
		}
		
		observable.subscribe(OnChangeBA.class, new ITbitsEventHandle<OnChangeBA>(){
			public void handleEvent(OnChangeBA event) {
				clearStore();
			}});
	}

	/**
	 * @return DQL through which the data has been populated for the grid
	 */
	public DQL getDql() {
		return dql;
	}

	public void setDql(DQL dql) {
		this.dql = dql;
	}

}
