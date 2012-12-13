package logistics.com.tbitsGlobal.client.widgets;

import logistics.com.tbitsGlobal.client.Stage;


import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGridContainer;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGridToolBar;

public class LogisticsRequestsViewGridContainer extends RequestsViewGridContainer{

	public LogisticsRequestsViewGridContainer(LogisticsRequestViewGrid grid, Stage stage, Mode mode) {
		super(stage.getPreSysPrefix(), grid);
		
		this.setStyleAttribute("margin", "5px");
		this.setHeight(300);
		this.setCollapsible(true);
		
		if(stage.getParams().getPreStageComponentName() != null)
			this.setHeading(stage.getParams().getPreStageComponentName());
		
		if(mode == Mode.EDIT){
			DefaultUIContext context = new DefaultUIContext();
			context.setValue(RequestsViewGridToolBar.CONTEXT_GRID, getGrid());
			
			this.setToolbar(new LogisticsRequestsViewGridToolBar(sysPrefix, context));
		}
	}

}
