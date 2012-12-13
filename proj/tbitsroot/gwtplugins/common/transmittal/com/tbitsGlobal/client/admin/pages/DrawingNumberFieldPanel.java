package transmittal.com.tbitsGlobal.client.admin.pages;

import java.util.List;

import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.log.Log;

import transmittal.com.tbitsGlobal.client.admin.TrnAdminConstants;
import transmittal.com.tbitsGlobal.client.models.TrnDrawingNumber;
/**
 * Panel to hold the drawing number grid
 * @author devashish
 *
 */
public class DrawingNumberFieldPanel extends AbstractAdminBulkUpdatePanel<TrnDrawingNumber> {

	public DrawingNumberFieldPanel(){
		super();
		commonGridDisabled	 = true;
		isExcelImportSupported = false;
		canAddRows			= true;
		canReorderRows		= false;
		canCopyPasteRows	= false;
	}
	
	protected void onSave(List<TrnDrawingNumber> models, final Button btn) {
		btn.setText("Saving... Please Wait...");
		btn.disable();
		
		TbitsInfo.info("Saving... Please Wait...");
		TrnAdminConstants.trnAdminService.saveDrawingNumberFields(models, new AsyncCallback<List<TrnDrawingNumber>>(){

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error saving Drawing Number Field Map to database...", caught);
				Log.error("Error saving Drawing Number Field Map to database...", caught);
				
			}
			public void onSuccess(List<TrnDrawingNumber> result) {
				singleGridContainer.removeAllModels();
				singleGridContainer.addModel(result);
				btn.setText("Save");
				btn.enable();
			}
		});
	}


	public void refresh(int page) {
		TbitsInfo.info("Loading...Please Wait...");
		TrnAdminConstants.trnAdminService.getDrawingNumberFields(new AsyncCallback<List<TrnDrawingNumber>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error Fetching Drawing Number Fields from database...", caught);
				Log.error("Error Fetching Drawing Number Fields from database...", caught);
			}

			public void onSuccess(List<TrnDrawingNumber> result) {
				if(result.isEmpty()){
					TbitsInfo.info("No Entries Exist in the drawing number field table...");
				}else{
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
				}
			}
			
		});
		
	}
	
	public TrnDrawingNumber getEmptyModel() {
		return new TrnDrawingNumber();
	}

	protected BulkUpdateGridAbstract<TrnDrawingNumber> getNewBulkGrid(BulkGridMode mode) {
		return new DrawingNumberFieldGrid(mode);
	}
}
