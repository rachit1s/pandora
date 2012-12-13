package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.ClickableLink;
import commons.com.tbitsGlobal.utils.client.TbitsCellEditor;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.ClickableLink.ClickableLinkListener;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.LinkCellRenderer;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserTypeClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * Grid which edits/adds the properties of 'All Users' page
 *
 */
public class UserBulkGrid extends BulkUpdateGridAbstract<UserClient>{

	public UserBulkGrid(BulkGridMode mode) {
		super(mode);
		
		canRemoveRow = false;
		showStatus = false;
	}
	
	protected void createColumns(){
		
		ColumnConfig idCol = new ColumnConfig(UserClient.USER_ID, "Id", 100);
		idCol.setFixed(true);
		cm.getColumns().add(idCol);
		
		ColumnConfig loginCol = new ColumnConfig(UserClient.USER_LOGIN, "Login", 200);
		loginCol.setSortable(true);
		cm.getColumns().add(loginCol);
		this.setValidator(UserClient.USER_LOGIN, new Validator(){
			public String validate(Object value) {
				if(value == null || !(value instanceof String) || ((String)value).trim().equals("")){
					return "Login can not be null or empty";
				}
				return null;
			}
		});
		
		ColumnConfig fNameCol = new ColumnConfig(UserClient.FIRST_NAME, "First Name", 200);
		fNameCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(fNameCol);
		
		
		ColumnConfig lNameCol = new ColumnConfig(UserClient.LAST_NAME, "Last Name", 200);
		lNameCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(lNameCol);
		
		ColumnConfig dNameCol = new ColumnConfig(UserClient.DISPLAY_NAME, "Display Name", 200);
		dNameCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(dNameCol);
		
		ColumnConfig sexCol = new ColumnConfig(UserClient.SEX, "Sex", 50);
		final SimpleComboBox<String> sex = new SimpleComboBox<String>();
		sex.setFieldLabel("Sex");
		sex.add("M");
		sex.add("F");
		sex.setSimpleValue("M");
		sexCol.setEditor(new TbitsCellEditor(sex){
			@Override
			public Object preProcessValue(Object value) {
				if(value != null && value instanceof String){
					String sexType = (String) value;
					return sex.getStore().findModel("value", sexType);
				}
				return null;
			}
			
			public Object postProcessValue(Object value) {
				if(value != null && value instanceof SimpleComboValue){
					SimpleComboValue<String> sexType = (SimpleComboValue<String>) value;
					return sexType.getValue();
				}
				return null;
			}
		});
		cm.getColumns().add(sexCol);
		
		CheckColumnConfig isActiveCol = getCheckColumn();
		isActiveCol.setWidth(50);
		isActiveCol.setId(UserClient.IS_ACTIVE);
		isActiveCol.setHeader("Active");
		cm.getColumns().add(isActiveCol);
		this.addPlugin(isActiveCol);
		
		ColumnConfig emailCol = new ColumnConfig(UserClient.EMAIL, "E-Mail", 200);
		emailCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(emailCol);
		
		ColumnConfig userTypeCol = new ColumnConfig(UserClient.USER_TYPE_ID, "User Type", 200);
		ComboBox<UserTypeClient> userTypeCombo = new ComboBox<UserTypeClient>();
		final ListStore<UserTypeClient> typeStore = new ListStore<UserTypeClient>();
		typeStore.add(UserTypeClient.getUserTypes());
		userTypeCombo.setStore(typeStore);
		userTypeCombo.setDisplayField(UserTypeClient.NAME);
		userTypeCol.setEditor(new TbitsCellEditor(userTypeCombo){
			@Override
			public Object preProcessValue(Object value) {
				if(value != null && value instanceof Integer){
					int userTypeId = (Integer) value;
					return typeStore.findModel(UserTypeClient.USER_TYPE_ID, userTypeId);
				}
				return null;
			}
			
			public Object postProcessValue(Object value) {
				if(value != null && value instanceof UserTypeClient){
					UserTypeClient userType = (UserTypeClient) value;
					return userType.getUserTypeId();
				}
				return null;
			}
		});
		userTypeCol.setRenderer(new GridCellRenderer<UserClient>(){
			public Object render(UserClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<UserClient> store, Grid<UserClient> grid) {
				try{
					int userTypeId = model.getUserTypeId();
					UserTypeClient userType = typeStore.findModel(UserTypeClient.USER_TYPE_ID, userTypeId);
					if(userType != null)
						return userType.getName();
				}catch(Exception e){}
				
				return "";
			}});
		cm.getColumns().add(userTypeCol);
		
		ColumnConfig mobileCol = new ColumnConfig(UserClient.MOBILE, "Mobile", 200);
		mobileCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(mobileCol);
		
		ColumnConfig locationCol = new ColumnConfig(UserClient.LOCATION, "Location", 200);
		locationCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(locationCol);
		
		ColumnConfig firmCol = new ColumnConfig(UserClient.FULL_FIRM_NAME, "Firm Name", 200);
		firmCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(firmCol);
		
		ColumnConfig firmCodeCol = new ColumnConfig(UserClient.FIRM_CODE, "Firm Code", 200);
		firmCodeCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(firmCodeCol);
		
		ColumnConfig Designation = new ColumnConfig(UserClient.DESIGNATION, "Designation", 200);
		Designation.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(Designation);
		
		ColumnConfig firmAddCol = new ColumnConfig(UserClient.FIRM_ADDRESS, "Firm Address", 200);
		firmAddCol.setEditor(new TbitsCellEditor(new TextField<String>()));
		cm.getColumns().add(firmAddCol);
		
		ColumnConfig webProfileCol = new ColumnConfig("webconfig", "Web Profile", 120);
		webProfileCol.setFixed(true);
		GridCellRenderer<UserClient> savebuttonRenderer = new LinkCellRenderer<UserClient>(){
			private ClickableLink link;
			
			public Object render(UserClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<UserClient> store,
					Grid<UserClient> grid) {
				if(link == null){
					link = new ClickableLink("Web Profile", new ClickableLinkListener<GridEvent<UserClient>>(){
							public void onClick(GridEvent<UserClient> e) {
							Grid<UserClient> grid = e.getGrid();
							if(grid != null){
								final UserClient user =  grid.getStore().getAt(e.getRowIndex());
								
								final Window webC = new Window();
								webC.setModal(true);
								webC.setHeading("Web Profile");
								webC.setSize(520, 352);
								webC.setClosable(true);
								webC.setLayout(new FitLayout());
								final TextArea ta = new TextArea();
								webC.add(ta, new FitData());

								ta.setValue(user.getWebConfig());
								
								Button saveWebconfig = new Button("Save", new SelectionListener<ButtonEvent>(){
									public void componentSelected(ButtonEvent ce) {
										user.setWebConfig(ta.getValue());
										APConstants.apService.updateUser(user, new AsyncCallback<UserClient>(){
											public void onFailure(Throwable caught) {
												TbitsInfo.error("Error while saving the web profile", caught);
												Log.error("Error while saving the web profile", caught);
											}

											public void onSuccess(UserClient result) {
												TbitsInfo.info("Successfully updated user web profile");
											}
										});
									}
								});
								webC.addButton(saveWebconfig);
								
								webC.show();
							}
						}});
					
					addLink(link);
				}
				return link.getHtml();
			}};
		webProfileCol.setRenderer(savebuttonRenderer);
		cm.getColumns().add(webProfileCol);
		
		ColumnConfig changePassCol = new ColumnConfig("changepass", "Change Password", 120);
		changePassCol.setFixed(true);
		GridCellRenderer<UserClient> changePassRenderer = new LinkCellRenderer<UserClient>(){
			private ClickableLink link;
			
			public Object render(UserClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<UserClient> store,
					Grid<UserClient> grid) {
				if(link == null){
					link = new ClickableLink("Change Password", new ClickableLinkListener<GridEvent<UserClient>>(){
							public void onClick(GridEvent<UserClient> e) {
							Grid<UserClient> grid = e.getGrid();
							if(grid != null){
								final UserClient user =  grid.getStore().getAt(e.getRowIndex());
								
								final Window changePassWindow = new Window();
								changePassWindow.setHeading("Change Password");
								changePassWindow.setModal(true);
								changePassWindow.setClosable(true);
								changePassWindow.setLayout(new FitLayout());
								changePassWindow.setHeight(120);
								
								FormPanel formPanel = new FormPanel();
								formPanel.setLabelWidth(150);
								formPanel.setHeaderVisible(false);
								formPanel.setBodyBorder(false);

								final TextField<String> newpassword = new TextField<String>();
								newpassword.setFieldLabel("New Password");
								newpassword.setPassword(true);
								
								final TextField<String> confirmpassword = new TextField<String>();
								confirmpassword.setFieldLabel("Confirm Password");
								confirmpassword.setPassword(true);

								formPanel.add(newpassword, new FormData("100%"));
								formPanel.add(confirmpassword, new FormData("100%"));
								
								changePassWindow.add(formPanel, new FitData());

								Button submit = new Button("Submit", new SelectionListener<ButtonEvent>() {
									public void componentSelected(ButtonEvent se) {
										if(newpassword.getValue() != null && confirmpassword.getValue() != null){

											if(newpassword.getValue().equals(confirmpassword.getValue())){
												APConstants.apService.setPassword(user.getUserLogin(), newpassword.getValue(), new AsyncCallback<Boolean>(){
													public void onFailure(Throwable caught) {
														TbitsInfo.error("Error while setting new password", caught);
														Log.error("Error while setting new password", caught);
													}
													public void onSuccess(Boolean result) {
														TbitsInfo.info("Password is Changed Successfully");
														changePassWindow.hide();
													}
												});
											}
											else 
												TbitsInfo.error("The two passwords did not match");
										}
										else
											TbitsInfo.error("Password cannot be empty.");
									}
								});
								
								changePassWindow.addButton(submit);
								
								Button cancel = new Button("Cancel", new SelectionListener<ButtonEvent>() {
									public void componentSelected(ButtonEvent se) {
										changePassWindow.hide();
									}
								});
								changePassWindow.addButton(cancel);
								
								changePassWindow.show();
							}
						}});
					
					addLink(link);
				}
				return link.getHtml();
			}};
		changePassCol.setRenderer(changePassRenderer);
		cm.getColumns().add(changePassCol);
	}
}
