package logistics.com.tbitsGlobal.client.widgets;

import java.util.List;

import logistics.com.tbitsGlobal.client.LogisticsConstants;
import logistics.com.tbitsGlobal.client.Stage;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.grids.GridColumnView;
import commons.com.tbitsGlobal.utils.client.grids.RequestsViewGrid;
import commons.com.tbitsGlobal.utils.client.log.Log;

public class LogisticsRequestViewGrid extends RequestsViewGrid{
	
	private Mode mode;
	private Stage stage;
	private int requestId;
	
	public LogisticsRequestViewGrid(Stage stage, Mode mode){
		super(stage.getPreSysPrefix());
		
		this.mode = mode;
		this.stage = stage;
		
		showContextMenu = false;
		isCustomizable = false;
		showTags = false;
		
		if(this.mode == Mode.VIEW)
			showSelectionModel = false;
	}
	
	public LogisticsRequestViewGrid(Stage stage, Mode mode, int requestId) {
		this(stage, mode);
		
		this.requestId = requestId;
	}
	
	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);
		
		this.clearAndFill();
	}
	
	public void clearAndFill(){
		this.getStore().removeAll();
		
		if(requestId != 0){
			LogisticsConstants.logisticsService.getPreStageRequests(stage, requestId, new AsyncCallback<List<TbitsTreeRequestData>>(){
				@Override
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Failed to get the " + stage.getParams().getPreStageComponentName() + "... Please see logs for details", 
							caught);
					Log.error("Failed to get the " + stage.getParams().getPreStageComponentName() + "... Please see logs for details", 
							caught);
				}
	
				@Override
				public void onSuccess(List<TbitsTreeRequestData> result) {
					if(result != null)
						LogisticsRequestViewGrid.this.addModels(result);
				}});
		}
	}
	
	@Override
	public GridColumnView getViewId() {
		return GridColumnView.SearchGrid;
	}

}
