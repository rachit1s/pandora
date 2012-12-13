package logistics.com.tbitsGlobal.client.widgets;

import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGridToolBar;

public class LogisticsRequestsViewGridToolBar extends RequestsViewGridToolBar{

	public LogisticsRequestsViewGridToolBar(String sysPrefix,
			UIContext parentContext) {
		super(sysPrefix, parentContext);
	}
	
	@Override
	protected void initializeButtons() {
		this.addDeleteButton();
	}

}
