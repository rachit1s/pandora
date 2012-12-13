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
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.modelData.EscalationConditionDetailClient;
import com.tbitsGlobal.admin.client.services.EscalationConditionService;
import com.tbitsGlobal.admin.client.services.JobActionService;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.domainObjects.JobDetailClient;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;


public class EscalationConditionMapView extends APTabItem {
	
	private final int MEDIUM_BUTTON_WIDTH = 100;

	static String PAGE_HEADER = "Escalation Conditions Detail";
	static String MAIN_TAB_HEADER = "Esaclation Conditions";
	static String CREATE_BUTTON_TITLE = "Create New";
	static String EDIT_BUTTON_TITLE = "Edit";
	static String DELETE_BUTTON_TITLE = "Delete";
	private TabPanel myTabPanel;
	private TabItem defaultTab;

	private Grid<EscalationConditionDetailClient> grid;

	private SelectionListener<ButtonEvent> editButtonlistener;

	private EscalationConditionDetailClient selectedCond;

	private EscalationConditionEditorForm createForm; // only single instance of create tab allowed



	public EscalationConditionMapView(LinkIdentifier linkId) {
		super(linkId);
		
		this.setClosable(true);
		this.setLayout(new FitLayout());
		defineHandlers();
		makeGrid();
		makeDefaultTab();
	}
	
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
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
					createForm = new EscalationConditionEditorForm(EscalationConditionService.CREATE_CONDITION,EscalationConditionService.CREATE_CONDITION){
						public void reloadGrid() {
							gridLoader();}
					};
					createForm.setItemId(EscalationConditionService.CREATE_CONDITION);
					myTabPanel.add(createForm);
					myTabPanel.setSelection(createForm);
				}
				//show edit job tab
				if(ce.getButton().getText() == EDIT_BUTTON_TITLE){
					selectedCond = grid.getSelectionModel().getSelectedItem();
					if(selectedCond== null){
						TbitsInfo.warn("Select a Condition to edit");
						return;
					}
					
					for(TabItem ti : myTabPanel.getItems()){
						if(ti.getItemId().equals(selectedCond.getEscCondId().toString())){
							myTabPanel.setSelection(ti);
							return;
						}
					}
					
					EscalationConditionEditorForm editForm = new EscalationConditionEditorForm(selectedCond.getDisName(),EscalationConditionService.EDIT_CONDITION,selectedCond){
								public void reloadGrid() {
									gridLoader();}};
					editForm.setItemId(selectedCond.getEscCondId().toString());
					myTabPanel.add(editForm);
					myTabPanel.setSelection(editForm);
				}
				
				if(ce.getButton().getText() == DELETE_BUTTON_TITLE){
					selectedCond = grid.getSelectionModel().getSelectedItem();
					if(selectedCond == null){
						TbitsInfo.warn("Select a Condition to delete");
						return;
					}
					deleteCond(selectedCond.getEscCondId());
				}
			}
		};
		
	}
	
	private void makeGrid(){
		
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		CheckBoxSelectionModel< EscalationConditionDetailClient> sm = new CheckBoxSelectionModel<EscalationConditionDetailClient>();
		configs.add(sm.getColumn());
		configs.add(new ColumnConfig(EscalationConditionDetailClient.ESC_COND_ID,"esc_cond_id",100));
		configs.add(new ColumnConfig(EscalationConditionDetailClient.DISPLAY_NAME,"dis_name",150));
		configs.add(new ColumnConfig(EscalationConditionDetailClient.SRC_BA,"src_ba",100));
		configs.add(new ColumnConfig(EscalationConditionDetailClient.DQL,"dql",100));
		configs.add(new ColumnConfig(EscalationConditionDetailClient.DESCRIPTION,"description",100));
		grid = new Grid<EscalationConditionDetailClient>(new ListStore<EscalationConditionDetailClient>(),new ColumnModel(configs));
		grid.setAutoExpandColumn(EscalationConditionDetailClient.DESCRIPTION);
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
			tb[i].add(createButton);
			tb[i].add(editButton);
			tb[i].add(delButton);
		}
		mainPanel.setTopComponent(tb[0]);
		mainPanel.add(grid);
		mainPanel.setBottomComponent(tb[1]);
		
		
		StoreFilterField<EscalationConditionDetailClient> filter = new StoreFilterField<EscalationConditionDetailClient>() {
			protected void onBlur(ComponentEvent ce) {
			  }
			protected boolean doSelect(Store<EscalationConditionDetailClient> store,
					EscalationConditionDetailClient parent, EscalationConditionDetailClient record,
					String property, String filter) {

				String name = "";
				name = record.getDisName();
				
				if(name.toLowerCase().contains(filter.toLowerCase()))
					return true;
				return false;
			}};
		filter.setEmptyText("Search Condition");
		filter.bind(grid.getStore());
		//TODO
		defaultTab.add(mainPanel);
		
	}
	
	private void gridLoader(){
		
		APConstants.apService.getAllEscCondition(new AsyncCallback<ArrayList<EscalationConditionDetailClient>>() {
			
			@Override
			public void onSuccess(ArrayList<EscalationConditionDetailClient> result) {
				grid.getStore().removeAll();
				if(result != null)
					grid.getStore().add(result);
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				TbitsInfo.error("error in fetching conditions", caught);
				
			}
		});
		
	}
	
	private void deleteCond(Integer escCondId){
		
		if(Window.confirm("Are you sure you want to delete Job : " + escCondId + " ?"))
			APConstants.apService.deleteEscCondtion(escCondId, new AsyncCallback<Boolean>() {

				@Override
				public void onFailure(Throwable caught) {
					TbitsInfo.error("Delete Condition failed...try refreshing", caught);
					
				}

				@Override
				public void onSuccess(Boolean arg0) {
					TbitsInfo.info("Condition deleted");
					gridLoader();
					
				}
			});
		
	}

}
