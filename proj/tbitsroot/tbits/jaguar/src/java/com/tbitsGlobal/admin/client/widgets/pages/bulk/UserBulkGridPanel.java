package com.tbitsGlobal.admin.client.widgets.pages.bulk;

import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;
import com.tbitsGlobal.admin.client.events.ToRefreshUsersCache;
import com.tbitsGlobal.admin.client.utils.APConstants;
import com.tbitsGlobal.admin.client.widgets.pages.UsersPage;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportWindow;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserTypeClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.tags.TagsUtils;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * Panel to hold the bulk update grids for 'All Users' page. The panel
 * encapsulates a bulk grid in which the column configs are to be defined.
 * 
 */
public class UserBulkGridPanel extends AbstractAdminBulkUpdatePanel<UserClient> {

	public UserBulkGridPanel() {
		super();

		canCopyPasteRows = false;
		canReorderRows = false;
		canDeleteRows = false;

		this.enablePaging(20);
	}

	protected void beforeRender() {
		StoreFilterField<UserClient> filterField = new StoreFilterField<UserClient>() {
			protected boolean doSelect(Store<UserClient> store,
					UserClient parent, UserClient record, String property,
					String filter) {
				if (filter == null || filter.equals(""))
					return false;
				if (record.getUserLogin() != null
						&& record.getUserLogin().toLowerCase().contains(
								filter.toLowerCase()))
					return true;
				return false;
			}
		};
		filterField.bind(singleGridContainer.getBulkGrid().getStore());
		toolbar.add(filterField);

		super.beforeRender();

		toolbar.add(new SeparatorToolItem());
		toolbar.add(new Html("<b>Search : </b>"));
		addSearcherToToolbar();

		pagingBar.setAllowedSizes(Arrays.asList(50, 100, 150, 200, 500, 1000));
	}

	private void addSearcherToToolbar() {
		final ComboBox<UserFilterColumn> combo = new ComboBox<UserFilterColumn>();
		ListStore<UserFilterColumn> store = new ListStore<UserFilterColumn>();
		UserFilterColumn defaultVal = new UserFilterColumn("Login",
				"user_login");
		store.add(defaultVal);
		store.add(new UserFilterColumn("First Name", "first_name"));
		store.add(new UserFilterColumn("Last Name", "last_name"));
		store.add(new UserFilterColumn("Display Name", "display_name"));
		store.add(new UserFilterColumn("Email", "email"));
		store.add(new UserFilterColumn("Location", "location"));
		store.add(new UserFilterColumn("Firm Name", "full_firm_name"));
		store.add(new UserFilterColumn("Firm Code", "firm_code"));
		store.add(new UserFilterColumn("Firm Address", "firm_address"));
		combo.setStore(store);
		combo.setDisplayField("displayName");
		combo.setValue(defaultVal);

		final TextField<String> searchParam = new TextField<String>();

		combo.addKeyListener(new KeyListener() {
			public void componentKeyUp(ComponentEvent event) {
				if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
					searchUser(combo.getSelection().get(0).getColumnName(),
							searchParam.getValue());
				}
			}
		});
		toolbar.add(combo);

		searchParam.addKeyListener(new KeyListener() {
			public void componentKeyUp(ComponentEvent event) {
				if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
					searchUser(combo.getSelection().get(0).getColumnName(),
							searchParam.getValue());
				}
			}
		});
		toolbar.add(searchParam);

		ToolBarButton search = new ToolBarButton("Search",
				new SelectionListener<ButtonEvent>() {

					public void componentSelected(ButtonEvent ce) {
						searchUser(combo.getSelection().get(0).getColumnName(),
								searchParam.getValue());
					}
				});
		toolbar.add(search);
	}

	private void searchUser(String filter, String userSearchParam) {

		if (userSearchParam == null || userSearchParam.equals(""))
			return;

		TbitsInfo.info("Fetching queried users. Please wait.");
		Log.info("Fetching queried users. Please wait.");

		APConstants.apService.fetchQueriedUsers(filter, userSearchParam,
				new AsyncCallback<UsersPage>() {

					public void onFailure(Throwable caught) {
						TbitsInfo.error("Could not retrieve queried Users.",
								caught);
						Log.error("Could not retrieve queried Users.", caught);
					}

					public void onSuccess(UsersPage result) {
						Log.info("Received queried users.");
						if (result != null && result.getUsers() != null) {
							singleGridContainer.removeAllModels();
							singleGridContainer.addModel(result.getUsers());
							pagingBar.adjustButtons(1, result.getTotalUsers());
							singleGridContainer.getBulkGrid().getStore().sort(
									UserClient.USER_LOGIN, SortDir.ASC);
						}
						Log.info("Finished rendering queried users.");
					}
				});
	}

	protected void onSave(List<UserClient> models, Button btn) {
		APConstants.apService.updateUsers(models, new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				TbitsInfo.error("Unable to update users", caught);
				Log.error("Unable to update users", caught);
			}

			public void onSuccess(Boolean result) {
				if (result) {
					TbitsInfo.info("Users updated successfully");
				} else {
					TbitsInfo.error("Unable to update users");
				}
			}
		});
	}

	public UserClient getEmptyModel() {
		return new UserClient();
	}

	public void refresh(final int page) {
		singleGridContainer.removeAllModels();
		APConstants.apService.getAllUsersPage(page, getPageSize(),
				new AsyncCallback<UsersPage>() {
					public void onFailure(Throwable caught) {
						TbitsInfo
								.error("Could not retrieve All Users.", caught);
						Log.error("Could not retrieve All Users.", caught);
					}

					public void onSuccess(UsersPage result) {
						if (result != null && result.getUsers() != null) {
							singleGridContainer.addModel(result.getUsers());
							pagingBar.adjustButtons(page, result
									.getTotalUsers());
							singleGridContainer.getBulkGrid().getStore().sort(
									UserClient.USER_LOGIN, SortDir.ASC);

							ListStore<UserClient> userStore = singleGridContainer
									.getBulkGrid().getStore();

							/**
							 *client side sorting
							 */
							userStore
									.setStoreSorter(new StoreSorter<UserClient>() {
										@Override
										public int compare(Store store,
												UserClient m1, UserClient m2,
												String property) {
											System.out.println(property);
											if (property.equals("user_login"))
												return m1
														.getUserLogin()
														.compareToIgnoreCase(
																m2
																		.getUserLogin());
											else if (property
													.equals("first_name"))
												return m1
														.getFirstName()
														.compareToIgnoreCase(
																m2
																		.getFirstName());
											else if (property
													.equals("last_name"))
												return m1
														.getLastName()
														.compareToIgnoreCase(
																m2
																		.getLastName());
											else

												return super.compare(store, m1,
														m2, property);

										}
									});
						}
					}
				});

	}

	protected void onAdd() {
		Window addUserWin = new Window();
		addUserWin.setLayout(new FormLayout());

		FormData formData = new FormData("-20");

		final TextField<String> login = new TextField<String>();
		login.setFieldLabel("Login");
		addUserWin.add(login, formData);

		final TextField<String> password = new TextField<String>();
		password.setFieldLabel("password");
		addUserWin.add(password, formData);

		final TextField<String> firstname = new TextField<String>();
		firstname.setFieldLabel("First Name");
		addUserWin.add(firstname, formData);

		final TextField<String> lastname = new TextField<String>();
		lastname.setFieldLabel("Last Name");
		addUserWin.add(lastname, formData);

		final TextField<String> dname = new TextField<String>();
		dname.setFieldLabel("Display Name");
		addUserWin.add(dname, formData);

		final SimpleComboBox<String> sex = new SimpleComboBox<String>();
		sex.setFieldLabel("Sex");
		sex.add("M");
		sex.add("F");
		sex.setSimpleValue("M");

		final CheckBox isactive = new CheckBox();
		isactive.setFieldLabel("Is Active:");
		isactive.setBoxLabel("");
		addUserWin.add(isactive, formData);

		final TextField<String> email = new TextField<String>();
		email.setFieldLabel("Email");
		addUserWin.add(email, formData);

		final ComboBox<UserTypeClient> type = new ComboBox<UserTypeClient>();
		ListStore<UserTypeClient> typeStore = new ListStore<UserTypeClient>();
		typeStore.add(UserTypeClient.getUserTypes());
		type.setStore(typeStore);
		type.setDisplayField(UserTypeClient.NAME);
		type.setFieldLabel("User Type");
		addUserWin.add(type, formData);

		final TextField<String> mobile = new TextField<String>();
		mobile.setFieldLabel("Mobile Number");
		addUserWin.add(mobile, formData);

		addUserWin.addButton(new Button("Save",
				new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						final UserClient userClient = new UserClient();
						if (login.getValue() == null
								|| login.getValue().trim().equals("")) {
							com.google.gwt.user.client.Window
									.alert("Login can not be empty.");
							login.focus();
							return;
						}
						userClient.setUserLogin(login.getValue());
						userClient.setFirstName(firstname.getValue());
						userClient.setLastName(lastname.getValue());
						userClient.setDisplayName(dname.getValue());
						userClient.setIsActive(isactive.getValue());
						userClient.setEmail(email.getValue());
						userClient.setUserTypeId(type.getValue()
								.getUserTypeId());
						userClient.setSex(sex.getSimpleValue());
						userClient.setMobile(mobile.getValue());
						if(password.getValue() != null  && !(password.getValue().trim().equals("")))
							userClient.set("password", password.getValue().trim());

						APConstants.apService.insertUser(userClient,
								new AsyncCallback<UserClient>() {
									public void onFailure(Throwable caught) {
										TbitsInfo
												.error(
														"Error while inserting the new user",
														caught);
										Log
												.error(
														"Error while inserting the new user",
														caught);
									}

									public void onSuccess(UserClient result) {
										if (null == result) {
											TbitsInfo
													.error("User Login Already Exist");
										} else {
											TbitsInfo
													.info("User has been added successfully");
											TbitsEventRegister
													.getInstance()
													.fireEvent(
															new ToRefreshUsersCache(
																	result,
																	ToRefreshUsersCache.ADD));
											singleGridContainer
													.addModel(result);
										}
									}
								});
					}
				}));

		addUserWin.show();
	}

	protected BulkUpdateGridAbstract<UserClient> getNewBulkGrid(
			BulkGridMode mode) {
		return new UserBulkGrid(mode);
	}

	protected ExcelImportWindow<UserClient> onImport() {
		ExcelImportWindow<UserClient> window = super.onImport();
		window.setDefaultUniqueMatchingProperty(UserClient.USER_LOGIN);
		return window;
	}

	/**
	 * Internal class to depict the user filters
	 * 
	 * @author karan
	 */
	public class UserFilterColumn extends TbitsModelData {

		public UserFilterColumn(String name, String column) {
			this.set("displayName", name);
			this.set("columnName", column);
		}

		public String getDisplayName() {
			return this.get("displayName");
		}

		public String getColumnName() {
			return this.get("columnName");
		}
	}
}
