package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.services.JobActionService;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.domainObjects.JobDetailClient;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * This class creates the default tab  
 * displaying a list of all job 
 * Also it adds edit tab and create tab
 * when needed.
 *  
 * @author kshitiz
 *
 */
public class JobListView extends APTabItem {
	private final int MEDIUM_BUTTON_WIDTH = 100;

	static String PAGE_HEADER = "Job List";
	static String MAIN_TAB_HEADER = "Jobs";
	static String CREATE_BUTTON_TITLE = "Create New";
	static String EDIT_BUTTON_TITLE = "Edit";
	static String DELETE_BUTTON_TITLE = "Delete";
	static String EXECUTE_BUTTON_TITLE = "Execute";

	private TabPanel myTabPanel;
	private TabItem defaultTab;
	private JobEditorForm  createForm; // only single instance of create tab allowed
	
	private Grid <JobDetailClient> grid;
	
	private SelectionListener<ButtonEvent> editButtonlistener;
	
	private JobDetailClient selectedJob;
	
	public JobListView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setLayout(new FitLayout());
		defineHandlers();
		makeGrid();
		makeDefaultTab();
	}
	
	public void onRender(Element parent, int pos){
		super.onRender(parent, pos);
		
		ContentPanel mainContainer = new ContentPanel();
		mainContainer.setHeaderVisible(false);
		mainContainer.setLayout(new FitLayout());
		myTabPanel = new TabPanel();
		mainContainer.add(myTabPanel);
		myTabPanel.add(defaultTab);
		add(mainContainer);
	}
	
	
	private void defineHandlers(){
		editButtonlistener = new SelectionListener<ButtonEvent>(){
			//show create job tab
			public void componentSelected(final ButtonEvent ce) {
				if(ce.getButton().getText() == CREATE_BUTTON_TITLE){
					if(createForm != null){
						for(TabItem ti : myTabPanel.getItems()){
							if(ti.getItemId().equals(createForm.getItemId())){
								myTabPanel.setSelection(createForm);
								return;
							}
						}
					}
					createForm = new JobEditorForm(JobActionService.CREATE_JOB,JobActionService.CREATE_JOB){
									public void reloadGrid() {
										gridLoader();}};
					createForm.setItemId(JobActionService.CREATE_JOB);
					myTabPanel.add(createForm);
					myTabPanel.setSelection(createForm);
				}
				//show edit job tab
				if(ce.getButton().getText() == EDIT_BUTTON_TITLE){
					selectedJob = grid.getSelectionModel().getSelectedItem();
					if(selectedJob == null){
						TbitsInfo.warn("Select a job to edit");
						return;
					}
					String temp1 = selectedJob.getJobName().trim();
					String temp2 = selectedJob.getJobGroup().trim();
					
					String id = temp1 + "(" + temp2.substring(0,3) + ")";
					String title  = temp1.substring(0,Math.min(8,temp1.length())) + "(" 
									+ temp2.substring(0,Math.min(3,temp2.length())) + ")";
					
					for(TabItem ti : myTabPanel.getItems()){
						if(ti.getItemId().equals(id)){
							myTabPanel.setSelection(ti);
							return;
						}
					}
					
					JobEditorForm editForm = new JobEditorForm(title,JobActionService.EDIT_JOB,selectedJob){
								public void reloadGrid() {
									gridLoader();}};
					editForm.setItemId(id);
					myTabPanel.add(editForm);
					myTabPanel.setSelection(editForm);
				}
				if(ce.getButton().getText() == DELETE_BUTTON_TITLE){
					selectedJob = grid.getSelectionModel().getSelectedItem();
					if(selectedJob == null){
						TbitsInfo.warn("Select a job to delete");
						return;
					}
					deleteJob(selectedJob.getJobName(),selectedJob.getJobGroup());
				}
				
				if(ce.getButton().getText() == EXECUTE_BUTTON_TITLE){
					selectedJob = grid.getSelectionModel().getSelectedItem();
					if(selectedJob == null){
						TbitsInfo.warn("Select a job to execute");
						return;
					}
					executeJob(selectedJob.getJobName(),selectedJob.getJobGroup());
				}
			}
		};
	}
	
	private void makeGrid(){
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		CheckBoxSelectionModel< JobDetailClient> sm = new CheckBoxSelectionModel<JobDetailClient>();
		configs.add(sm.getColumn());
		configs.add(new ColumnConfig(JobDetailClient.JOB_STATE,"State",50));
		final ColumnConfig col = new ColumnConfig("Action", "action", 80);
		col.setRenderer(new GridCellRenderer<JobDetailClient>(){
			public Object render(final JobDetailClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<JobDetailClient> store, Grid<JobDetailClient> grid) {
					ToolBarButton button = new ToolBarButton("start");
					button.setWidth(col.getWidth());
					if((model.getJobState()).equalsIgnoreCase("Paused"))
						button.setText("Resume");
					else if(model.getJobState().equalsIgnoreCase("Blocked") 
							|| model.getJobState().equalsIgnoreCase("Complete")
							|| model.getJobState().equalsIgnoreCase("Error")
							|| model.getJobState().equalsIgnoreCase("none"))
						return null;
					else
						button.setText("Pause");
					button.setWidth(45);
					button.addSelectionListener(new SelectionListener<ButtonEvent>(){

						public void componentSelected(ButtonEvent ce) {
							if((model.getJobState()).equalsIgnoreCase("Paused"))
								resumeJob((String)model.getJobName(),(String)model.getJobGroup());
							else
								pauseJob((String)model.getJobName(),(String)model.getJobGroup());
						}
					});
					return button;
			}
			
		});
		configs.add(col);
		configs.add(new ColumnConfig(JobDetailClient.JOB_NAME,"job name",150));
		configs.add(new ColumnConfig(JobDetailClient.JOB_GROUP,"Job_group",150));
		configs.add(new ColumnConfig(JobDetailClient.DESCRIPTION,"description",200));
		configs.add(new ColumnConfig(JobDetailClient.CRON_EXPRESSION,"cron expression",150));
		
		grid = new Grid<JobDetailClient>(new ListStore<JobDetailClient>(),new ColumnModel(configs));
		grid.setAutoExpandColumn(JobDetailClient.DESCRIPTION);
		grid.setAutoExpandMin(150);
		grid.setLoadMask(true);
		grid.setSelectionModel(sm);
		gridLoader();
	}
	
	private void makeDefaultTab(){
		defaultTab = new TabItem(MAIN_TAB_HEADER);
		defaultTab.setLayout(new FitLayout());
		
		ContentPanel mainPanel = new ContentPanel();
		mainPanel.setLayout(new FitLayout(){
			@Override
			protected void onLayout(Container<?> container, El target) {
				super.onLayout(container, target);
				int width = Math.max(800, container.getWidth());
				container.setWidth(width);
			}
		});
		mainPanel.setScrollMode(Scroll.AUTOX);
		mainPanel.setHeaderVisible(false);

		ToolBar tb[] = new ToolBar[2];
		for(int i = 0 ; i< 2 ;i ++){
			tb[i] = new ToolBar();
			ToolBarButton createButton = new ToolBarButton(CREATE_BUTTON_TITLE , editButtonlistener);
			createButton.setWidth(MEDIUM_BUTTON_WIDTH);
			ToolBarButton editButton = new ToolBarButton(EDIT_BUTTON_TITLE , editButtonlistener);
			editButton.setWidth(MEDIUM_BUTTON_WIDTH);
			ToolBarButton delButton = new ToolBarButton(DELETE_BUTTON_TITLE , editButtonlistener);
			delButton.setWidth(MEDIUM_BUTTON_WIDTH);
			ToolBarButton execButton = new ToolBarButton(EXECUTE_BUTTON_TITLE , editButtonlistener);
			execButton.setWidth(MEDIUM_BUTTON_WIDTH);
			tb[i].add(createButton);
			tb[i].add(editButton);
			tb[i].add(delButton);
			tb[i].add(execButton);
		}
		mainPanel.setTopComponent(tb[0]);
		mainPanel.add(grid);
		mainPanel.setBottomComponent(tb[1]);
		
		
		StoreFilterField<JobDetailClient> filter = new StoreFilterField<JobDetailClient>() {
			protected void onBlur(ComponentEvent ce) {
			  }
			protected boolean doSelect(Store<JobDetailClient> store,
					JobDetailClient parent, JobDetailClient record,
					String property, String filter) {

				String name = "";
				name = record.getJobName();
				
				if(name.toLowerCase().contains(filter.toLowerCase()))
					return true;
				return false;
			}};
		filter.setEmptyText("Search job");
		filter.bind(grid.getStore());
		//TODO
		defaultTab.add(mainPanel);
	}
	
	
//---------------------------Server access methods-------------------------
	private void gridLoader(){
		APConstants.apService.getJobDetails(new AsyncCallback<ArrayList<JobDetailClient>>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("error in fetching jobs", caught);
			}
			public void onSuccess(ArrayList<JobDetailClient> result) {
				grid.getStore().removeAll();
				if(result != null)
					grid.getStore().add(result);
			}
		});
	}
	private void pauseJob(String jobName, String jobGroup){
		APConstants.apService.pauseJob(jobName,jobGroup , new AsyncCallback<Boolean>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Pause Job failed...try refreshing", caught);
			}
			public void onSuccess(Boolean result) {
				TbitsInfo.info("job paused");
				gridLoader();
			}
		});
	}
	private void resumeJob(String jobName, String jobGroup){
		APConstants.apService.resumeJob(jobName,jobGroup , new AsyncCallback<Boolean>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Resume Job failed...try refreshing", caught);
			}
			public void onSuccess(Boolean result) {
				TbitsInfo.info("job resumed");
				gridLoader();
			}
		});
	}
	private void deleteJob(String jobName, String jobGroup){
		if(Window.confirm("Are you sure you want to delete Job : " + jobName + " ?"))
			APConstants.apService.deleteJob(jobName,jobGroup , new AsyncCallback<Boolean>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Delete Job failed...try refreshing", caught);
				}
				public void onSuccess(Boolean result) {
					TbitsInfo.info("Job deleted");
					gridLoader();
				}
			});
	}
	private void executeJob(String jobName, String jobGroup){
		APConstants.apService.executeJob(jobName,jobGroup , new AsyncCallback<Boolean>(){
			public void onFailure(Throwable caught) {
				TbitsInfo.error("ExecuteJob failed...try refreshing", caught);
			}
			public void onSuccess(Boolean result) {
				TbitsInfo.info("job executed");
			}
		});
	}
}
