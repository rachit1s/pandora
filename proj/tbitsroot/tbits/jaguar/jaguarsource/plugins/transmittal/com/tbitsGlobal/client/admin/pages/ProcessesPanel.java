package transmittal.com.tbitsGlobal.client.admin.pages;

import java.util.List;

import transmittal.com.tbitsGlobal.client.admin.TrnAdminConstants;
import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
/**
 * Panel for holding Transmittal Process Grid
 * @author devashish
 *
 */
public class ProcessesPanel extends AbstractAdminBulkUpdatePanel<TrnProcess>{
	
	protected StoreFilterField<TrnProcess> filter;
	protected BusinessAreaClient currentSrcBa;
	protected Integer gridRowCount;
	
	public ProcessesPanel() {
		super();
		commonGridDisabled = true;
		isExcelImportSupported = false;
		canAddRows			= false;
		canReorderRows		= false;
		canCopyPasteRows	= false;
		canDeleteRows		= false;
		gridRowCount		= 0;
		addBaCombo();
		applySearchFilter();
	}
	
	protected void addBaCombo(){
		ComboBox<BusinessAreaClient> bacombo = TrnAdminUtils.getBACombo();
		bacombo.setEmptyText("Select a Business Area");
		bacombo.setWidth(300);
		bacombo.addSelectionChangedListener(new SelectionChangedListener<BusinessAreaClient>(){
			public void selectionChanged(SelectionChangedEvent<BusinessAreaClient> se) {
				currentSrcBa = se.getSelectedItem();
				refresh(0);
			}
		});
		bacombo.setEmptyText("Select a Source Business Area");
		toolbar.add(bacombo);
	}
	/**
	 * Apply search filter to the grid
	 */
	protected void applySearchFilter(){
		filter = new StoreFilterField<TrnProcess>(){
			protected boolean doSelect(Store<TrnProcess> store,	TrnProcess parent, TrnProcess record, String property,
					String filter) {
				
				String srcBa = record.getSrcBA().getSystemPrefix().toLowerCase();
				String description = record.getDescription().toLowerCase();
				String dtnBa = record.getDTNBA().getSystemPrefix().toLowerCase();
				String dtrBa = record.getDTRBA().getSystemPrefix().toLowerCase();
				
				filter = filter.toLowerCase();
				
				if(srcBa.contains(filter) || description.contains(filter) || dtnBa.contains(filter) || dtrBa.contains(filter)){
					return true;
				}				
				return false;
			}
		};
		
		filter.bind(singleGridContainer.getBulkGrid().getStore());
		filter.bind(commonGridContainer.getBulkGrid().getStore());
		filter.setEmptyText("Search");
		
		LabelField filterLabel = new LabelField("Search : ");
		toolbar.add(filterLabel);
		toolbar.add(filter);
	}
	
	public void refresh(int page){
		if(null != currentSrcBa){
			TbitsInfo.info("Loading... Please Wait...");
			TrnAdminConstants.trnAdminService.getTransmittalProcessesForBa(currentSrcBa, new AsyncCallback<List<TrnProcess>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error fetching processes", caught);
					Log.error("Error fetching processes", caught);
				}
	
				public void onSuccess(List<TrnProcess> result) {
					if(result.isEmpty()){
						TbitsInfo.info("No Process Configuration exists for selected BA as source...");
					}else{
						singleGridContainer.removeAllModels();
						singleGridContainer.addModel(result);
						gridRowCount = result.size();
					}
				}
			});
		}
	}

	protected void onSave(List<TrnProcess> models, final Button btn) {
		if(models.size() != gridRowCount){
			TbitsInfo.error("Cannnot Delete Rows from this page...");
			return;
		}
		
		btn.setText("Saving... Please Wait...");
		btn.disable();
		
		TrnAdminConstants.trnAdminService.saveTransmittalProcesses(models, new AsyncCallback<List<TrnProcess>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Error saving processes", caught);
				Log.error("Error saving processes", caught);
				btn.setText("Save");
				btn.enable();
			}

			public void onSuccess(List<TrnProcess> result) {
				TbitsInfo.info("Successfully saved Transmittal Processes to database...");
				singleGridContainer.removeAllModels();
				singleGridContainer.addModel(result);
				btn.setText("Save");
				btn.enable();
			}});
	}

	public TrnProcess getEmptyModel() {
		return new TrnProcess();
	}

	protected BulkUpdateGridAbstract<TrnProcess> getNewBulkGrid(BulkGridMode mode) {
		return new ProcessesGrid(mode);
	}
}
