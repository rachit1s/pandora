package transmittal.com.tbitsGlobal.client;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.google.gwt.user.client.Element;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bulkupdate.AbstractBulkUpdatePanel;
import commons.com.tbitsGlobal.utils.client.bulkupdate.AbstractCommonBulkGridContainer;
import commons.com.tbitsGlobal.utils.client.bulkupdate.AbstractSingleBulkGridContainer;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;

/**
 * Panel for second page of transmittal wizard
 * @author devashish
 *
 */
public class Page2Panel extends AbstractBulkUpdatePanel<TbitsTreeRequestData> {

	
	private WizardData wizardData;
	
	public Page2Panel(UIContext context,WizardData wizardData){
		super();
		canAddRows = false;
		canDeleteRows = false;
		canReorderRows = false;
		
		AbstractSingleBulkGridContainer<TbitsTreeRequestData> singleContainer = new AbstractSingleBulkGridContainer<TbitsTreeRequestData>(new Page2Grid(BulkGridMode.SINGLE,wizardData)){
			public void showStatus(HashMap<Integer, TbitsTreeRequestData> statusMap) {
				
			}

			public void updateModels() {
				
			}};
		context.setValue(BulkUpdateGridAbstract.CONTEXT_SINGLE_GRID_CONTAINER, singleContainer);
		AbstractCommonBulkGridContainer<TbitsTreeRequestData> commonContainer = new AbstractCommonBulkGridContainer<TbitsTreeRequestData>(context, new Page2Grid(BulkGridMode.COMMON,wizardData)){};
		
		this.singleGridContainer = singleContainer;
		this.commonGridContainer = commonContainer;
	}
	
	public TbitsTreeRequestData getEmptyModel() {
		return new TbitsTreeRequestData();
	}

	protected BulkUpdateGridAbstract<TbitsTreeRequestData> getNewBulkGrid(BulkGridMode mode) {
		return new Page2Grid(mode, wizardData);
	}
	
	public AttachmentSelectionTableColumnsConfiguration getSingleGridColumnConfig(){
		return ((Page2Grid)this.singleGridContainer.getBulkGrid()).getColumnConfig();
	}
	public void UpdateGridColumnConfig( AttachmentSelectionTableColumnsConfiguration columnConfiguration2){
		 ((Page2Grid)this.singleGridContainer.getBulkGrid()).setmodifiedConfig(columnConfiguration2);
		 ((Page2Grid)this.commonGridContainer.getBulkGrid()).setmodifiedConfig(columnConfiguration2); 
	}
	public void refreshgrid (ArrayList<TbitsTreeRequestData> gridData,List<ColumnConfig> configs, AttachmentSelectionTableColumnsConfiguration columnConfiguration2){
		ListStore <TbitsTreeRequestData>ls = new ListStore<TbitsTreeRequestData>();
		ls.add(gridData);
		ColumnModel cm =  new ColumnModel(configs);
			
		
		getSingleGridContainer().getBulkGrid().reconfigure(ls,  cm);
		getCommonGridContainer().getBulkGrid().reconfigure(new ListStore<TbitsTreeRequestData>(),cm);
		this.UpdateGridColumnConfig(columnConfiguration2);
		this.getSingleGridColumnConfig();
	}
	public void setColumnConfig(AttachmentSelectionTableColumnsConfiguration columnConfiguration2){
		 ((Page2Grid)this.singleGridContainer.getBulkGrid()).setmodifiedConfig(columnConfiguration2);
		 ((Page2Grid)this.commonGridContainer.getBulkGrid()).setmodifiedConfig(columnConfiguration2); 
	}
	
	
	
	
}