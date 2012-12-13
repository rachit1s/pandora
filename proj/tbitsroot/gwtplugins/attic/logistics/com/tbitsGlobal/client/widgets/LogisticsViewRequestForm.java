package logistics.com.tbitsGlobal.client.widgets;

import logistics.com.tbitsGlobal.client.Stage;

import com.tbitsGlobal.jaguar.client.widgets.forms.RequestView;

import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

public class LogisticsViewRequestForm extends RequestView{

	/**
	 * Contains the grid showing the component from previous stage
	 */
	private LogisticsRequestsViewGridContainer gridContainer;
	
	/**
	 * Object that carry all the configuration params for the current stage
	 */
	private Stage stage;
	
	public LogisticsViewRequestForm(UIContext parentContext, Stage stage) {
		super(parentContext);
		
		this.stage = stage;
	}
	
	@Override
	protected void afterCreate() {
		super.afterCreate();
		
		// Component Grid
		if(this.getData().getRequestModel() != null){
			LogisticsRequestViewGrid grid = new LogisticsRequestViewGrid(stage, Mode.VIEW, this.getData().getRequestModel().getRequestId());
			gridContainer = new LogisticsRequestsViewGridContainer(grid, stage, Mode.VIEW);
			this.add(gridContainer);
		}
	}
	
	@Override
	public void refresh() {
		super.refresh();
		
		((LogisticsRequestViewGrid)gridContainer.getGrid()).clearAndFill();
	}

}
