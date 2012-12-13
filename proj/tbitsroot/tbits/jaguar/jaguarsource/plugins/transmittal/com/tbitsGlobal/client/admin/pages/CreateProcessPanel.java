package transmittal.com.tbitsGlobal.client.admin.pages;

import java.util.List;

import transmittal.com.tbitsGlobal.client.admin.TrnAdminConstants;
import transmittal.com.tbitsGlobal.client.admin.TrnAdminUtils;
import transmittal.com.tbitsGlobal.client.admin.wizard.CreateProcessWizard;
import transmittal.com.tbitsGlobal.client.models.TrnCreateProcess;
import transmittal.com.tbitsGlobal.client.models.TrnProcess;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * Panel to hold the grid
 * @author devashish
 *
 */
public class CreateProcessPanel extends ContentPanel {
	
	protected CreateProcessGrid createProcessGrid;
	protected ToolBar topBar;
	protected TrnProcess currentProcess;
	protected StoreFilterField<TrnCreateProcess> filter;
	
	
	
	public CreateProcessPanel(){
		this.setLayout(new FitLayout());
		this.setHeaderVisible(false);
		
		createProcessGrid = new CreateProcessGrid();
		
		buildTopToolbar();
	}
	
	
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		
		populatePanel();
	}
	
	protected void getProperties(){
		if(null != currentProcess){
			createProcessGrid.emptyStore();
			TbitsInfo.info("Fetching parameters... Please Wait...");
			TrnAdminConstants.trnAdminService.getAllProcessParams(currentProcess, new AsyncCallback<List<TrnCreateProcess>>(){
	
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Could not fetch properties from database...", caught);
					Log.error("Could not fetch properties from database", caught);
				}
	
				public void onSuccess(List<TrnCreateProcess> result) {
					TbitsInfo.info("Successfully Fetched properties from database...");
					createProcessGrid.populateStore(result);
				}
			});
		}
	}
	
	protected void populatePanel(){
		this.add(createProcessGrid.getGrid());
		this.layout();
	}
	
	protected void buildTopToolbar(){
		topBar = new ToolBar();
		
		ComboBox<TrnProcess> processCombo = TrnAdminUtils.getTransmittalProcessesCombo();
		processCombo.addSelectionChangedListener(new SelectionChangedListener<TrnProcess>(){
			public void selectionChanged(SelectionChangedEvent<TrnProcess> se) {
				currentProcess = se.getSelectedItem();
				getProperties();
			}});
		processCombo.setWidth(300);
		topBar.add(processCombo);
		
		
		applySearchFilter();
		topBar.add(filter);
		
		
		SelectionListener<ButtonEvent> selectionListener = new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				CreateProcessWizard wizard = new CreateProcessWizard();
				wizard.show();
			}
		};
		
		topBar.add(new ToolBarButton("Create Process", selectionListener));
		
		this.setTopComponent(topBar);
	}
	
	/**
	 * Apply search filter to the search box provided in the top toolbar
	 */
	protected void applySearchFilter(){
		filter = new StoreFilterField<TrnCreateProcess>(){
			
			protected boolean doSelect(Store<TrnCreateProcess> store, TrnCreateProcess parent, TrnCreateProcess record,
					String property, String filter) {
				
				String paramName = record.getName().toLowerCase();
				
				String paramValue = record.getValue().toLowerCase();
				
				String groupName = record.getGroup().toLowerCase();
				
				if(paramName.contains(filter.toLowerCase()) || paramValue.contains(filter.toLowerCase()) || groupName.contains(filter.toLowerCase()))
					return true;
				return false;
			}
		};
		filter.bind(createProcessGrid.groupStore);
		filter.setEmptyText(" Search ");
	}
}
