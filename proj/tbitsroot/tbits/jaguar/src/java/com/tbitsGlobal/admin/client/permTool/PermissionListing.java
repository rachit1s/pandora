package com.tbitsGlobal.admin.client.permTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.modelData.RolePermissionModel;
import com.tbitsGlobal.admin.client.utils.APConstants;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.RoleClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

/**
 * Tab item to display the permission info, roles list and mailing lists corresponding to a
 * specified user and request.
 * 
 * @author Karan Gupta
 *
 */
public class PermissionListing extends TabItem {

	// Panel to store the lists and grid
	private HorizontalPanel userPermissions;
	// Popup window to display the roles that affect a particular field permissions
	private Window window;
	
	// Grid and lists to display the relevant permissioning information
	private Grid<RolePermissionModel> permissionGrid;
	private ListView<RoleClient> roles;
	private ListView<UserClient> mailingLists;
	
	// Stores for the grids and lists above
	private ListStore<RolePermissionModel> permStore;
	private ListStore<RoleClient> roleStore;
	private ListStore<UserClient> mailingListStore;
	
	// Column config for the grid
	private List<ColumnConfig> configs;
	
	// boolean flag to indicate if the popup window is being constructed
	private boolean fetchingRolesAffecting = false;
	
	/**
	 * Constructor
	 * 
	 * @param heading
	 */
	public PermissionListing(String heading) {
		super(heading);

		initialise();
		
		userPermissions = new HorizontalPanel();
		this.add(userPermissions);
		this.layout();
	}

	/**
	 * Initialise the various components of the permission listing tab
	 */
	private void initialise() {
		
		// Create the column config for the permission grid
		configs = new ArrayList<ColumnConfig>();
		ColumnConfig column = new ColumnConfig();
		column.setId(RolePermissionModel.FIELD_NAME);
		column.setHeader("Field Name");
		column.setWidth(120);
		configs.add(column);
		column = new ColumnConfig();
		column.setId("PT"+RolePermissionModel.IS_VIEW);
		column.setHeader("View");
		column.setWidth(50);
		configs.add(column);
		column = new ColumnConfig();
		column.setId("PT"+RolePermissionModel.IS_ADD);
		column.setHeader("Add");
		column.setWidth(50);
		configs.add(column);
		column = new ColumnConfig();
		column.setId("PT"+RolePermissionModel.IS_UPDATE);
		column.setHeader("Change");
		column.setWidth(50);
		configs.add(column);
		column = new ColumnConfig();
		column.setId("PT"+RolePermissionModel.IS_EMAIL);
		column.setHeader("Email");
		column.setWidth(50);
		configs.add(column);
		
		// Initialise the permission grid, its store and add onClick activity
		permStore = new ListStore<RolePermissionModel>();
		permissionGrid = new Grid<RolePermissionModel>(permStore, new ColumnModel(configs));
		permissionGrid.setHeight(330);
		permissionGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		permissionGrid.addListener(Events.OnClick, new Listener<BaseEvent>() {

			public void handleEvent(BaseEvent be) {
				displayRolesAffecting(permissionGrid.getSelectionModel().getSelectedItem());
			}
		});
		
		// Initialise the role listing and its store
		roleStore = new ListStore<RoleClient>();
		roles = new ListView<RoleClient>(roleStore){
			protected RoleClient prepareData(RoleClient role){
				String s = role.getRoleName();
				role.set("shortName", Format.ellipse(s, 15));
				role.set("name", s);
				return role;  
			}
		};
		roles.setDisplayProperty("name");
		roles.setHeight(330);
		roles.setWidth(150);
		
		// Initialise the mailing list view and its store
		mailingListStore = new ListStore<UserClient>();
		mailingLists = new ListView<UserClient>(mailingListStore){
			protected UserClient prepareData(UserClient user) {
				String s = user.getUserLogin();  
				user.set("shortName", Format.ellipse(s, 15));
				user.set("name", s);
				return user;  
			}  
		};
		mailingLists.setDisplayProperty("name");
		mailingLists.setHeight(330);
		mailingLists.setWidth(150);
		
		// Initialise the popup window
		window = new Window();
		window.setSize(420, 300);
		window.setLayout(new FitLayout());
		window.setPlain(true);
		window.setModal(true);  
	}
	
	/**
	 * Fetches the permissions for the specified inputs.
	 * 
	 * @param sysId
	 * @param userId
	 * @param reqId
	 */
	public void fetchPermissions(int sysId, final int userId, final int reqId){
		
		APConstants.apService.fetchPermissionInformation(sysId, userId, reqId, new AsyncCallback<PermissionInfo>() {

			public void onFailure(Throwable caught) {
				TbitsInfo.error("Unable to fetch permission information for userId : " + userId + " and requestId : " + reqId);
			}

			public void onSuccess(PermissionInfo result) {
				
				permStore.removeAll();
				mailingListStore.removeAll();
				roleStore.removeAll();
				
				mailingListStore.add(result.getMailingLists());
				roleStore.add(result.getRoles());
				for(BAField f : PTCache.getInstance().getFieldValues()){
					RolePermissionModel rpm = new RolePermissionModel();
					rpm.setFieldName(f.getName());
					// Create the required RolePermissionModel for displaying in the grid
					if(result.getFieldMap().keySet().contains(f.getName())){
						rpm.setAdd(result.getFieldMap().get(f.getName()).get(RolePermissionModel.IS_ADD));
						rpm.setView(result.getFieldMap().get(f.getName()).get(RolePermissionModel.IS_VIEW));
						rpm.setUpdate(result.getFieldMap().get(f.getName()).get(RolePermissionModel.IS_UPDATE));
						rpm.setEMail(result.getFieldMap().get(f.getName()).get(RolePermissionModel.IS_EMAIL));
						
						if(!result.getFieldMap().get(f.getName()).get(RolePermissionModel.IS_ADD))
							rpm.set("PT"+RolePermissionModel.IS_ADD, "");
						else
							rpm.set("PT"+RolePermissionModel.IS_ADD, "true");
						
						if(!result.getFieldMap().get(f.getName()).get(RolePermissionModel.IS_VIEW))
							rpm.set("PT"+RolePermissionModel.IS_VIEW, "");
						else
							rpm.set("PT"+RolePermissionModel.IS_VIEW, "true");
						
						if(!result.getFieldMap().get(f.getName()).get(RolePermissionModel.IS_UPDATE))
							rpm.set("PT"+RolePermissionModel.IS_UPDATE, "");
						else
							rpm.set("PT"+RolePermissionModel.IS_UPDATE, "true");
						
						if(!result.getFieldMap().get(f.getName()).get(RolePermissionModel.IS_EMAIL))
							rpm.set("PT"+RolePermissionModel.IS_EMAIL, "");
						else
							rpm.set("PT"+RolePermissionModel.IS_EMAIL, "true");
					}
					permStore.add(rpm);
				}
				
				refreshInfo();
			}
		
		});
	}
	
	/**
	 * Refresh the view and show the currently set permissions, roles and mailing lists
	 */
	private void refreshInfo(){
		
		permissionGrid.reconfigure(permStore, new ColumnModel(configs));
		roles.setStore(roleStore);
		mailingLists.setStore(mailingListStore);
		
		ContentPanel pPanel = new ContentPanel(new FitLayout());
		pPanel.add(permissionGrid);
		pPanel.setHeading("Permissions");
		pPanel.setToolTip("Click on any row to view permission sources for the field");
		pPanel.setStyleAttribute("padding", "5px");
		ContentPanel rPanel = new ContentPanel(new FitLayout());
		rPanel.add(roles);
		rPanel.setHeading("Roles");
		rPanel.setStyleAttribute("padding", "5px");
		ContentPanel mlPanel = new ContentPanel(new FitLayout());
		mlPanel.add(mailingLists);
		mlPanel.setHeading("Mailing Lists");
		mlPanel.setStyleAttribute("padding", "5px");
		
		userPermissions.removeAll();
		userPermissions.add(pPanel);
		userPermissions.add(rPanel);
		userPermissions.add(mlPanel);
		userPermissions.layout();
	}
	
	/**
	 * Display the roles affecting the field corresponding to the given RolePermissionModel.
	 * 
	 * @param rpm
	 */
	private void displayRolesAffecting(final RolePermissionModel rpm){
		
		// Do not fetch if there are no roles that the user belongs to
		if(roles.getStore().getModels().size() == 0){
			TbitsInfo.info("The user does not exist in any roles!");
		}
		// Do not fetch if none of the permissions are true
		else if(rpm.get(RolePermissionModel.IS_VIEW).equals("false") && rpm.get(RolePermissionModel.IS_ADD).equals("false") 
				&& rpm.get(RolePermissionModel.IS_UPDATE).equals("false") && rpm.get(RolePermissionModel.IS_EMAIL).equals("false")){
			TbitsInfo.info("None of the permissions are true. Clicking on the field shows which roles set its permissions true for View/Add/Change/Email.");
		}
		// Do not fetch if there is already a fetch in process
		else if(fetchingRolesAffecting)
			return;
		// Fetch if all above evaluate to false
		else{
			fetchingRolesAffecting = true;
			ArrayList<Integer> relevantRoleIds = new ArrayList<Integer>();
			for(RoleClient r : roles.getStore().getModels()){
				relevantRoleIds.add(r.getRoleId());
			}
			APConstants.apService.fetchRolesAffecting(ClientUtils.getCurrentBA().getSystemId(), rpm, relevantRoleIds, new AsyncCallback<HashMap<String, List<String>>>() {
				public void onFailure(Throwable caught) {
					TbitsInfo.error("unable to fetch the relevant roles!");
					fetchingRolesAffecting = false;
				}
				public void onSuccess(HashMap<String, List<String>> result) {
					if(result.size() == 0){
						TbitsInfo.info("No roles fetched!");
						return;
					}
					
					// Content panels for all the different permission types
					
					String roleStr = "";
					ContentPanel cpView = new ContentPanel();
					cpView.setHeading("View : " + rpm.get(RolePermissionModel.IS_VIEW));
					cpView.setStyleAttribute("padding", "2px"); 
					cpView.setWidth(100);
					cpView.setHeight(250);
					if((Boolean)rpm.get(RolePermissionModel.IS_VIEW)){
						if(result.containsKey(RolePermissionModel.IS_VIEW)){
							for(String role : result.get(RolePermissionModel.IS_VIEW)){
								roleStr += role + "<br>";
							}	
						}
						cpView.add(new Html(roleStr));
					}
					
					ContentPanel cpAdd = new ContentPanel();
					cpAdd.setHeading("Add : " + rpm.get(RolePermissionModel.IS_ADD));
					cpAdd.setStyleAttribute("padding", "2px");
					cpAdd.setWidth(100);
					cpAdd.setHeight(250);
					roleStr = "";
					if((Boolean)rpm.get(RolePermissionModel.IS_ADD)){
						if(result.containsKey(RolePermissionModel.IS_ADD)){
							for(String role : result.get(RolePermissionModel.IS_ADD)){
								roleStr += role + "<br>";
							}	
						}
						cpAdd.add(new Html(roleStr));
					}
					
					ContentPanel cpChange = new ContentPanel();
					cpChange.setHeading("Change : " + rpm.get(RolePermissionModel.IS_UPDATE));
					cpChange.setStyleAttribute("padding", "2px");
					cpChange.setWidth(100);
					cpChange.setHeight(250);
					roleStr = "";
					if((Boolean)rpm.get(RolePermissionModel.IS_UPDATE)){
						if(result.containsKey(RolePermissionModel.IS_UPDATE)){
							for(String role : result.get(RolePermissionModel.IS_UPDATE)){
								roleStr += role + "<br>";
							}	
						cpChange.add(new Html(roleStr));
						}
					}
						
					
					ContentPanel cpEmail = new ContentPanel();
					cpEmail.setHeading("Email : " + rpm.get(RolePermissionModel.IS_EMAIL));
					cpEmail.setStyleAttribute("padding", "2px");
					cpEmail.setWidth(100);
					cpEmail.setHeight(250);
					roleStr = "";
					if((Boolean)rpm.get(RolePermissionModel.IS_EMAIL)){
						if(result.containsKey(RolePermissionModel.IS_EMAIL)){
							for(String role : result.get(RolePermissionModel.IS_EMAIL)){
								roleStr += role + "<br>";
							}	
						}
						cpEmail.add(new Html(roleStr));
					}
					
					// Add all the content panels to the window and show the window
					HorizontalPanel hp = new HorizontalPanel();
					hp.add(cpView);
					hp.add(cpAdd);
					hp.add(cpChange);
					hp.add(cpEmail);
					
					window.setHeading(rpm.getFieldName());  
					window.removeAll();
					window.add(hp);
					window.show();

					fetchingRolesAffecting = false;
				}
			});
		}
	}
}
