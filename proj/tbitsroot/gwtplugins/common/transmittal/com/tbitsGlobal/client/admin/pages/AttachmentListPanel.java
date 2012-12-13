package transmittal.com.tbitsGlobal.client.admin.pages;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import transmittal.com.tbitsGlobal.client.TransmittalConstants;
import transmittal.com.tbitsGlobal.client.admin.TrnAdminConstants;
import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.models.TrnAttachmentList;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * Panel to hold attachment selection table
 * @author devashish
 *
 */
public class AttachmentListPanel extends AbstractAdminBulkUpdatePanel<TrnAttachmentList>{
	private TrnProcess currentProcess;

	 String COLUMN_ORDER = "column_order";
	public AttachmentListPanel() {
		super();
		commonGridDisabled = true;
		isExcelImportSupported = false;
		canAddRows			= true;
		canReorderRows		= false;
		canCopyPasteRows	= false;

		ComboBox<TrnProcess> processCombo = TrnAdminUtils.getTransmittalProcessesCombo();
		processCombo.addSelectionChangedListener(new SelectionChangedListener<TrnProcess>(){
			public void selectionChanged(SelectionChangedEvent<TrnProcess> se) {
				currentProcess = se.getSelectedItem();
				((AttachmentListGrid)commonGridContainer.getBulkGrid()).setSrcBA(currentProcess.getSrcBA());
				((AttachmentListGrid)singleGridContainer.getBulkGrid()).setSrcBA(currentProcess.getSrcBA());
				refresh(0);
			}});
		toolbar.add(processCombo);
	}
	
	protected void onSave(List<TrnAttachmentList> models, final Button btn) {
		if(currentProcess != null){
			btn.setText("Saving... Please Wait...");
			btn.disable();
			
			TbitsInfo.info("Saving... Please Wait..");
			TrnAdminConstants.trnAdminService.saveAttachmentLists(currentProcess, models, new AsyncCallback<List<TrnAttachmentList>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error saving post process field values", caught);
					Log.error("Error saving post process field values", caught);
					btn.setText("Save");
					btn.enable();
				}
	
				public void onSuccess(List<TrnAttachmentList> result) {
					TbitsInfo.info("Successfully Saved Attachment Selection Table to db...");
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
					btn.setText("Save");
					btn.enable();
				}});
		}
	}

	public void refresh(int page) {
		if(currentProcess != null){
			TbitsInfo.info("Loading... Please Wait...");
			TrnAdminConstants.trnAdminService.getAttachmentList(currentProcess, new AsyncCallback<List<TrnAttachmentList>>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Error fetching Attachment List", caught);
					Log.error("Error fetching Attachment List", caught);
				}

				public void onSuccess(List<TrnAttachmentList> result) {
					
					Comparator<TbitsModelData> comp = new Comparator<TbitsModelData>() {
						public int compare(TbitsModelData o1, TbitsModelData o2) {
							if ((o1 != null) && (o2 != null)) {
								int s1 = (Integer) o1
										.get(COLUMN_ORDER);
								int s2 = (Integer) o2
										.get(COLUMN_ORDER);
								if (s1 > s2)
									return 1;
								else if (s1 == s2)
									return 0;
								else if (s1 < s2)
									return -1;
							}
							return 0;
						}
					};
					// Sort the column info, before creating column configs out of them. So,
					// that they maintain the sort order and
					// hence the column order in the table.
					Collections.sort(result, comp);
					singleGridContainer.removeAllModels();
					singleGridContainer.addModel(result);
				}});
		}
	}
	
	public TrnAttachmentList getEmptyModel() {
		return new TrnAttachmentList();
	}

	protected BulkUpdateGridAbstract<TrnAttachmentList> getNewBulkGrid(BulkGridMode mode) {
		return new AttachmentListGrid(mode);
	}

}
