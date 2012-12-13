package logistics.com.tbitsGlobal.client.widgets;

import java.util.ArrayList;
import java.util.List;

import logistics.com.tbitsGlobal.client.LogisticsConstants;
import logistics.com.tbitsGlobal.client.LogisticsUtils;
import logistics.com.tbitsGlobal.client.Stage;
import logistics.com.tbitsGlobal.client.widgets.window.LogisticsComponentWindow;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.widgets.forms.UpdateRequestForm;
import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.search.SearchWindow.ISubmitHandler;

public class LogisticsUpdateRequestForm extends UpdateRequestForm{
	/**
	 * Contains the grid showing the component from previous stage
	 */
	private LogisticsRequestsViewGridContainer gridContainer;
	
	/**
	 * Object that carry all the configuration params for the current stage
	 */
	private Stage stage;
	
	public LogisticsUpdateRequestForm(UIContext parentContext, Stage stage) {
		super(parentContext);
		
		this.stage = stage;
		
		// The Button the add the components
		this.addButton(new Button(LogisticsUtils.getAddHeadingString(this.stage.getParams()), new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				LogisticsComponentWindow window = new LogisticsComponentWindow(LogisticsUpdateRequestForm.this.stage);
				window.show();
				
				window.setSubmitHandler(new ISubmitHandler(){
					@Override
					public void onSubmit(List<TbitsTreeRequestData> models) {
						if(gridContainer != null){
							gridContainer.getGrid().addModels(models);
						}
					}});
			}}));	
	}
	
	@Override
	protected void afterCreate() {
		super.afterCreate();
		
		// Component Grid
		if(this.getData().getRequestModel() != null){
			LogisticsRequestViewGrid grid = new LogisticsRequestViewGrid(stage, Mode.EDIT, this.getData().getRequestModel().getRequestId());
			gridContainer = new LogisticsRequestsViewGridContainer(grid, stage, Mode.EDIT);
			this.add(gridContainer);
		}
	}
	
	@Override
	public void afterSubmit(final String sysPrefix, final int requestId) {
		List<TbitsTreeRequestData> models = gridContainer.getGrid().getStore().getModels();
		
		List<Integer> preRequestIds = new ArrayList<Integer>();
		for(TbitsTreeRequestData model : models){
			preRequestIds.add(model.getRequestId());
		}
		
		LogisticsConstants.logisticsService.setPreStageRequests(stage, requestId, preRequestIds, new AsyncCallback<Boolean>(){
			@Override
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Unable to save the " + stage.getParams().getPreStageComponentName() + "... Please see logs for details", caught);
				Log.error("Unable to save the " + stage.getParams().getPreStageComponentName() + "... Please see logs for details", caught);
			}

			@Override
			public void onSuccess(Boolean result) {
				if(result){
					LogisticsUpdateRequestForm.super.afterSubmit(sysPrefix, requestId);
				}else{
					TbitsInfo.error("Unable to save the " + stage.getParams().getPreStageComponentName() + "... Please see logs for details");
				}
			}});
	}
}
