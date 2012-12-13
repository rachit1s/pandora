package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.HashMap;

import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.modelData.SysInfoClient;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
/**
 * Tab to display the System Status
 * extends APTabItem
 * @author devashish
 *
 */
public class SystemStatusView extends APTabItem{
	protected static String caption ;
	protected static final String SYS_PROP_VALUE	= "syspropvalue";
	protected static final String SYS_PROP			= "sysprop";
	protected HashMap<String, String> sysProperties;
	protected ContentPanel contentPanel;
	protected ListStore<TbitsModelData> store;
	protected GroupingStore<SysInfoClient> groupStore;
	
	protected Grid<TbitsModelData> grid;
	protected Grid<SysInfoClient> groupGrid;
	
	/**
	 * Initialise the tab item
	 */
	public SystemStatusView(LinkIdentifier linkId){
		super(linkId);
		
		this.caption = linkId.getPageCaption();
		
		this.setBorders(false);
		this.setLayout(new FitLayout());
		sysProperties = new HashMap<String, String>();
		contentPanel = new ContentPanel();
		//----------------Store for data----------------------//
		store 		= new ListStore<TbitsModelData>();
		groupStore 	= new GroupingStore<SysInfoClient>();
	}
	
	public void onRender(Element parent, int index) {
		super.onRender(parent, index);
//		build();
	}
	
	protected void build(){
		//----------------Initialize the content panel which will store the grid-----------------------//
		contentPanel.setLayout(new FitLayout());
		contentPanel.setHeaderVisible(false);		
		//----------------Define the columns------------------//
		
		ColumnConfig sysProp	= new ColumnConfig(SysInfoClient.SYS_PROP, "System Property", 500);
		ColumnConfig sysPropValue = new ColumnConfig(SysInfoClient.SYS_PROP_VALUE, "Value", 500);
		ColumnConfig sysGroup	= new ColumnConfig(SysInfoClient.GROUP, "Group", 200);
		//----------------Text field-------------------------//
		TextField<String> propertyValue = new TextField<String>();
		propertyValue.setAllowBlank(false);
		sysPropValue.setEditor(new TbitsCellEditor(propertyValue));
		
		ArrayList<ColumnConfig> clist = new ArrayList<ColumnConfig>();
		clist.add(sysProp);
		clist.add(sysPropValue);
		clist.add(sysGroup);
		groupGrid = new Grid<SysInfoClient>(groupStore, new ColumnModel(clist));
		
		getResults();
	}
	
	/**
	 * Retreive the corresponding system properties and populate the grid
	 */
	protected void getResults(){
		TbitsInfo.info("Loading " + caption + "... Please Wait...");
		APConstants.apService.getSysInfo(new AsyncCallback<ArrayList<SysInfoClient>>(){
			public void onFailure(Throwable caught){
				TbitsInfo.error("Error in getting System properties... Please Refresh ...", caught);
			}
			
			public void onSuccess(ArrayList<SysInfoClient> result){
				populateGrid(result);
			}
		});
	}
	
	/**
	 * Takes as parameter a hashmap of the values to be populated in the grid
	 * and accordingly fills the grid.
	 * @param result
	 */
	protected void populateGrid(ArrayList<SysInfoClient> result){
		groupStore.add(result);
		groupStore.groupBy(SysInfoClient.GROUP);
		
		GroupingView view = new GroupingView();
		view.setShowGroupedColumn(false);
		view.setGroupRenderer(new GridGroupRenderer() {  
	      public String render(GroupColumnData data) {    
	        return data.group;
	      }  
	    }); 
		groupGrid.setView(view);
		
		contentPanel.add(groupGrid);
		this.add(contentPanel);
		this.layout();
	}
}
