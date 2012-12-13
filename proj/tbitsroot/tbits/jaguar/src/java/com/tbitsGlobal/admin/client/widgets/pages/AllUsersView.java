package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.cache.UserCacheAdmin;
import com.tbitsGlobal.admin.client.events.OnUserAdd;
import com.tbitsGlobal.admin.client.events.OnUserChange;
import com.tbitsGlobal.admin.client.events.OnUserDelete;
import com.tbitsGlobal.admin.client.events.OnUsersReceived;
import com.tbitsGlobal.admin.client.events.ToRefreshUsersCache;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.utils.LinkIdentifier;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.MultiFilterStore;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserTypeClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;


public class AllUsersView extends APTabItem{
	
	private ListStore<UserTypeClient> typeStore;
	private ComboBox<UserClient> usersCombo;
	
	private HashMap<String, Field> values;

	public AllUsersView(LinkIdentifier linkId){
		super(linkId);
		
		this.setLayout(new FitLayout());
		this.setClosable(true);
		
		values = new HashMap<String, Field>();
		
		typeStore = new ListStore<UserTypeClient>();
		typeStore.add(UserTypeClient.getUserTypes());
		
		usersCombo = new ComboBox<UserClient>();
		usersCombo.setStore(new MultiFilterStore<UserClient>());
		List<String> filterProperties = new ArrayList<String>();
		filterProperties.add(UserClient.USER_LOGIN);
		filterProperties.add(UserClient.DISPLAY_NAME);
		((MultiFilterStore<UserClient>)usersCombo.getStore()).setFilterProperties(filterProperties);
		usersCombo.setDisplayField(UserClient.USER_LOGIN);
		usersCombo.setTemplate(getTemplate());
		usersCombo.setSelectOnFocus(true);
		usersCombo.addSelectionChangedListener(new SelectionChangedListener<UserClient>() {
			public void selectionChanged(SelectionChangedEvent<UserClient> se) {
				UserClient currentUser = se.getSelectedItem();
				fillDetails(currentUser);
			}
		});
		
		UserCacheAdmin cache = CacheRepository.getInstance().getCache(UserCacheAdmin.class);
		if(cache.isInitialized())
			refreshCombo();
		
		observable.subscribe(OnUsersReceived.class,new ITbitsEventHandle<OnUsersReceived>(){
			public void handleEvent(OnUsersReceived event) {
				refreshCombo();
			}

		});
		
		observable.subscribe(OnUserAdd.class, new ITbitsEventHandle<OnUserAdd>(){
			
			public void handleEvent(OnUserAdd event) {
				UserClient user = event.getUser();
				usersCombo.getStore().add(user);
				usersCombo.setValue(user);
			}});
		
		observable.subscribe(OnUserDelete.class, new ITbitsEventHandle<OnUserDelete>(){
			
			public void handleEvent(OnUserDelete event) {
				usersCombo.getStore().remove(event.getUser());
				usersCombo.setValue(usersCombo.getStore().getAt(0));
			}});
		
		observable.subscribe(OnUserChange.class, new ITbitsEventHandle<OnUserChange>(){
			
			public void handleEvent(OnUserChange event) {
				UserClient user = event.getUser();
				usersCombo.getStore().remove(usersCombo.getStore().findModel(UserClient.USER_LOGIN, user.getUserLogin()));
				usersCombo.getStore().add(user);
				usersCombo.setValue(user);
			}});
	}
	
	public void onRender(Element parent , int pos){
		super.onRender(parent, pos);

		ContentPanel main = new ContentPanel();
		main.setLayout(new ColumnLayout());
		main.setScrollMode(Scroll.AUTO);
		main.setHeaderVisible(false);
		main.setBodyBorder(false);
		
		ToolBar topToolBar = new ToolBar();
		
		LabelField userLabel = new LabelField("Users");
		topToolBar.add(userLabel);
		
		topToolBar.add(usersCombo);
		
		ToolBarButton webconfig = new ToolBarButton("Web Profile", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce) {
				UserClient user = usersCombo.getValue();
				if(user != null)
					showWebProfile(user);
			}
		});
		
//		ToolBarButton more = new ToolBarButton("more", new SelectionListener<ButtonEvent>(){
//			public void componentSelected(ButtonEvent ce) {
//				if(more.getText().equals("more")){
//				((LayoutContainer)widgets.get("right")).setVisible(true);
//				more.setText("less");
//			}
//				else{
//					((LayoutContainer)widgets.get("right")).setVisible(false);
//					more.setText("more");
//				}
//			}
//
//		});
//		topToolBar.add(more);
		
		topToolBar.add(webconfig);
		
		main.setTopComponent(topToolBar);

		FormPanel left = new FormPanel();
		left.setHeaderVisible(false);
		left.setBodyBorder(false);

		FormData formData = new FormData("-20");
		
		TextField<String> login = new TextField<String>();
		login.setFieldLabel("Login");
		login.disable();
		values.put(UserClient.USER_LOGIN, login);        
		left.add(login, formData);

		TextField<String> firstname = new TextField<String>();
		firstname.setFieldLabel("First Name");
		values.put(UserClient.FIRST_NAME, firstname);      
		left.add(firstname, formData);

		TextField<String> lastname = new TextField<String>();
		lastname.setFieldLabel("Last Name");
		values.put(UserClient.LAST_NAME, lastname);
		left.add(lastname, formData);

		TextField<String> dname = new TextField<String>();
		dname.setFieldLabel("Display Name");
		values.put(UserClient.DISPLAY_NAME, dname);
		left.add(dname, formData);

		final CheckBox isactive = new CheckBox();
		isactive.setFieldLabel("Is Active:");
		isactive.setBoxLabel("");
		values.put(UserClient.IS_ACTIVE, isactive);
		left.add(isactive, formData);

		TextField<String> email = new TextField<String>();
		email.setFieldLabel("Email");
		values.put(UserClient.EMAIL,email);
		left.add(email, formData);

		ComboBox<UserTypeClient> type = new ComboBox<UserTypeClient>();
		type.setStore(typeStore);
		type.setDisplayField(UserTypeClient.NAME);
		type.setFieldLabel("User Type");
		values.put(UserClient.USER_TYPE_ID,type);
		left.add(type, formData);

		TextField<String> mobile = new TextField<String>();
		mobile.setFieldLabel("Mobile Number");
		values.put(UserClient.MOBILE, mobile);
		left.add(mobile, formData);
		
		main.add(left, new ColumnData(0.5));
		
		FormPanel right = new FormPanel();
		right.setHeaderVisible(false);
		right.setBodyBorder(false);

		TextField<String> locate = new TextField<String>();
		locate.setFieldLabel("Location");
		values.put(UserClient.LOCATION, locate);
		right.add(locate, formData);

		TextField<String> firmname = new TextField<String>();
		firmname.setFieldLabel("Full Firm Name");
		values.put(UserClient.FULL_FIRM_NAME_COLUMN_NAME, firmname);
		right.add(firmname, formData);

		TextField<String> firmcode = new TextField<String>();
		firmcode.setFieldLabel("Firm Code");
		values.put(UserClient.FIRM_CODE_COLUMN_NAME, firmcode);
		right.add(firmcode, formData);

		TextArea firmadd = new TextArea();
		firmadd.setFieldLabel("Firm Address");
		values.put(UserClient.FIRM_ADDRESS_COLUMN_NAME, firmadd);
		right.add(firmadd, formData);

		TextField<String> design = new TextField<String>();
		design.setFieldLabel("Designation");
		values.put(UserClient.DESIGNATION_COLUMN_NAME, design);
		right.add(design, formData);

		SimpleComboBox<String> sex = new SimpleComboBox<String>();
		sex.setFieldLabel("Sex");
		sex.add("M");
		sex.add("F");
		sex.setSimpleValue("M");
		right.add(sex, formData);
		values.put(UserClient.SEX_COLUMN_NAME, sex);
		
		main.add(right, new ColumnData(0.5));
		
		ToolBar footer = new ToolBar();
		ToolBarButton add = new ToolBarButton("Add a user", new SelectionListener<ButtonEvent>(){
			@SuppressWarnings("unchecked")
			public void componentSelected(ButtonEvent ce){
				UserClient usrclient = new UserClient();
				usrclient.setIsActive(true);
				usrclient.setUserTypeId(7);
				fillDetails(usrclient);
				
				TextField<String> loginField = (TextField)(values.get(UserClient.USER_LOGIN));
				loginField.enable();
				loginField.setAllowBlank(false);
				loginField.setEmptyText("Enter user login");
			}
		});
		footer.add(add);

		ToolBarButton changePassword = new ToolBarButton("Change Password");
		footer.add(changePassword);
		
		final ToolBarButton save = new ToolBarButton("Save");
		footer.add(save);
		
		final ToolBarButton revert = new ToolBarButton("Revert", new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce){
				UserClient currentUser = usersCombo.getValue();
				fillDetails(currentUser);
			}
		});
		footer.add(revert);

		changePassword.addSelectionListener(new SelectionListener<ButtonEvent>(){
			
			public void componentSelected(ButtonEvent ce) {
				UserClient user = usersCombo.getValue();
				changePass(user);
			}
		});

		save.addSelectionListener(new SelectionListener<ButtonEvent>(){
			public void componentSelected(ButtonEvent ce){
				final UserClient saveUser = getUserClient();
				
				if(usersCombo.getStore().findModel(UserClient.USER_LOGIN, saveUser.getUserLogin()) != null){
					APConstants.apService.updateUser(saveUser, new AsyncCallback<UserClient> (){
						public void onFailure(Throwable caught) {
							TbitsInfo.error("Unable to update user", caught);
							Log.error("Unable to update user", caught);
						}
						public void onSuccess(UserClient result) {
							TbitsInfo.info("updated successfully");
							if(saveUser.getIsActive()){
								TbitsEventRegister.getInstance().fireEvent(new ToRefreshUsersCache(result,ToRefreshUsersCache.CHANGE));
							} else {
								TbitsEventRegister.getInstance().fireEvent(new ToRefreshUsersCache(result, ToRefreshUsersCache.DELETE));
							}
						}
					});
				}else{
					insertUser(saveUser);
				}
			}
		});

		main.setBottomComponent(footer);
		
		this.add(main, new FitData());
	}
	
	private void changePass(final UserClient user){
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
	
	private void insertUser(UserClient uc){
		if(null == uc.getUserLogin()){
			TbitsInfo.info("Insert the user login.....it cant be left empty");
		}else{
			APConstants.apService.insertUser(uc, new AsyncCallback<UserClient>(){
				public void onFailure(Throwable caught) {
					TbitsInfo.error("error while inserting the new user", caught);
					Log.error("error while inserting the new user", caught);
				}
				public void onSuccess(UserClient result) {
					if(null == result){
						TbitsInfo.error("User Login Already Exist");
					}
					else{
						TbitsInfo.info("user has been added successfully");
						TbitsEventRegister.getInstance().fireEvent(new ToRefreshUsersCache(result,ToRefreshUsersCache.ADD));
					}
				}
			});
		}
	}
	
	private void showWebProfile(final UserClient user){
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

	@SuppressWarnings("unchecked")
	private void fillDetails(UserClient uc){
		if (uc == null)
			return;
		
		for(String property : values.keySet()){
			if(property.equals(UserClient.USER_TYPE_ID)){
				UserTypeClient userType = typeStore.findModel(UserTypeClient.USER_TYPE_ID, uc.get(property));
				values.get(property).setValue(userType);
			}else if(property.equals(UserClient.SEX)){
				((SimpleComboBox<String>) values.get(property)).setSimpleValue(uc.getSex());
			}else
				values.get(property).setValue(uc.get(property));
		}
	}

	@SuppressWarnings("unchecked")
	private UserClient getUserClient(){
		UserClient uc = new UserClient();
		for(String property : values.keySet()){
			if(property.equals(UserClient.USER_TYPE_ID)){
				UserTypeClient userType = ((ComboBox<UserTypeClient>)values.get(property)).getValue();
				uc.setUserTypeId(userType.getUserTypeId());
			}else if(property.equals(UserClient.SEX)){
				String sex = ((SimpleComboBox<String>) values.get(property)).getSimpleValue();
				uc.setSex(sex);
			}else
				uc.set(property, values.get(property).getValue());
		}
		return uc;
	}

	private native String getTemplate()/*-{ 
    return  [ 
    '<tpl for=".">', 
    '<div class="x-combo-list-item" qtip="Description:{description}" qtitle="{user_login}">{display_name} [{user_login}]</div>', 
    '</tpl>' 
    ].join("");
	}-*/;

	private void refreshCombo(){
		usersCombo.getStore().removeAll();
		UserCacheAdmin cache = CacheRepository.getInstance().getCache(UserCacheAdmin.class);
		for(UserClient user : cache.getMap().values()){
			usersCombo.getStore().add(user);
		}
		usersCombo.getStore().sort(UserClient.USER_LOGIN, SortDir.ASC);
		if (usersCombo.getStore().getCount() != 0) {
			usersCombo.setValue(usersCombo.getStore().getModels().get(0));
		}
	}

}
