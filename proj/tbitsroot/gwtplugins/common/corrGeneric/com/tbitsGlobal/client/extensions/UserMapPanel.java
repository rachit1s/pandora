package corrGeneric.com.tbitsGlobal.client.extensions;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;
import com.tbitsGlobal.admin.client.cache.UserCacheAdmin;
import com.tbitsGlobal.admin.client.state.AppState;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.ExcelImportWindow;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserTypeClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

import corrGeneric.com.tbitsGlobal.client.CorrAdminUtils;
import corrGeneric.com.tbitsGlobal.client.CorrConstants;
import corrGeneric.com.tbitsGlobal.client.modelData.OnBehalfMapClient;
import corrGeneric.com.tbitsGlobal.client.modelData.UserMapClient;

/**
 * Panel for holding user map grid
 * 
 * @author devashish
 * 
 */
public class UserMapPanel extends AbstractAdminBulkUpdatePanel<UserMapClient> {

	protected BusinessAreaClient currentBa;
	protected UserClient currentUser;
	protected StoreFilterField<UserMapClient> filter;
	protected boolean isFirstRefresh = true;

	public UserMapPanel() {
		super();

		isExcelImportSupported = false;
		canAddRows = true;
		canReorderRows = false;
		canCopyPasteRows = false;

		buildToolbar();

	}

	/**
	 * Add the ba selection combo and user selection combo to the top toolbar
	 */
	protected void buildToolbar() {
		final ComboBox<BusinessAreaClient> bacombo = CorrAdminUtils
				.getBACombo();
		final ComboBox<UserClient> userComboBox = CorrAdminUtils.getUserCombo();

		bacombo.setEmptyText("Please Select a BA");
		bacombo.setWidth(300);
		bacombo.addSelectionChangedListener(new SelectionChangedListener<BusinessAreaClient>() {
			public void selectionChanged(
					SelectionChangedEvent<BusinessAreaClient> se) {
				userComboBox.clearSelections();
				currentBa = se.getSelectedItem();
				bacombo.setValue(currentBa);
				((UserMapGrid) commonGridContainer.getBulkGrid())
						.setCurrentBa(currentBa);
				((UserMapGrid) singleGridContainer.getBulkGrid())
						.setCurrentBa(currentBa);
			}
		});

		toolbar.add(bacombo);

		userComboBox.setEmptyText("Choose a user login");
		userComboBox
				.addSelectionChangedListener(new SelectionChangedListener<UserClient>() {
					public void selectionChanged(
							SelectionChangedEvent<UserClient> se) {
						if (null == se.getSelectedItem()
								|| (0 != userComboBox.getSelectionLength())) {
							TbitsInfo
									.error("Null value of User or BA selected.... Please select a valid value");
							return;
						}
						if (null == currentBa) {
							TbitsInfo
									.info("Please select a valid BA before selecting the user...");
							return;
						}
						currentUser = se.getSelectedItem();
						userComboBox.setValue(currentUser);
						refresh(1);
					}
				});
		toolbar.add(userComboBox);

		LabelField filterLabel = new LabelField("Search : ");
		toolbar.add(filterLabel);

		applySearchFilter();
		toolbar.add(filter);
	}

	/**
	 * Apply search filter to the search box provided in the top toolbar
	 */
	protected void applySearchFilter() {
		filter = new StoreFilterField<UserMapClient>() {

			protected boolean doSelect(Store<UserMapClient> store,
					UserMapClient parent, UserMapClient record,
					String property, String filter) {

				String type1 = record.getType1().getDisplayName();
				type1 = type1.toLowerCase();

				String type2 = record.getType2().getDisplayName();
				type2 = type2.toLowerCase();

				String type3 = record.getType3().getDisplayName();
				type3 = type3.toLowerCase();

				String userTypeField = record.getUserTypeField().getName();
				userTypeField = userTypeField.toLowerCase();

				String userLogin = record.getUser().getUserLogin();
				userLogin = userLogin.toLowerCase();

				String userLoginValue = record.getUserLoginValue()
						.getUserLogin();
				userLoginValue = userLoginValue.toLowerCase();

				if ((type1.contains(filter.toLowerCase()))
						|| (type2.contains(filter.toLowerCase()))
						|| (type3.contains(filter.toLowerCase()))
						|| (userTypeField.contains(filter.toLowerCase()))
						|| (userLogin.contains(filter.toLowerCase()))
						|| (userLoginValue.contains(filter.toLowerCase())))
					return true;
				return false;
			}
		};
		filter.bind(((UserMapGrid) singleGridContainer.getBulkGrid())
				.getStore());
		filter.setEmptyText(" Search ");
	}

	protected void onSave(List<UserMapClient> models, Button btn) {
		if (0 == models.size()) {
			TbitsInfo.info("Cannot save empty table...");
			return;
		}

		ArrayList<UserMapClient> savedProperties = new ArrayList<UserMapClient>();

		for (UserMapClient property : models) {
			if ((null == property.getStrictness())
					|| (property.getStrictness().equals("-1"))
					|| (property.getStrictness().equals(""))) {
				TbitsInfo
						.error("Invalid value of strictness... Please enter a valid value...");
				return;
			}

			if ((null == property.getUserLoginValue())
					|| (property.getUserLoginValue().getUserLogin()
							.equals("NULL"))
					|| (property.getUserLoginValue().getUserLogin().equals(""))) {
				TbitsInfo
						.error("Invalid value in 'User Login Value' field... Please enter a valid value....");
				return;
			}

			if ((null == property.getUserTypeField().getName())
					|| (property.getUserTypeField().getName().equals("NULL"))
					|| (property.getUserTypeField().getName().equals(""))) {
				TbitsInfo
						.error("Invalid value in 'User Type Field'... Please enter a valid value...");
				return;
			}

			property.setUser(currentUser);
			property.setSysprefix(currentBa.getSystemPrefix());
			savedProperties.add(property);
		}

		TbitsInfo.info("Saving... Please Wait...");
		CorrConstants.corrAdminService.saveUserMapProperties(savedProperties,
				new AsyncCallback<ArrayList<UserMapClient>>() {

					public void onFailure(Throwable caught) {
						TbitsInfo
								.error("Could not save User Map Properties to database... See logs for more information...",
										caught);
						Log.error(
								"Could not save User Map properties to database...",
								caught);
					}

					public void onSuccess(ArrayList<UserMapClient> result) {
						if (null != result) {
							TbitsInfo
									.info("Successfully saved report properties to database...");
							singleGridContainer.removeAllModels();
							singleGridContainer.addModel(result);
						}
					}
				});

	}

	public void refresh(int page) {
		if ((null != currentBa) && (null != currentUser)) {
			TbitsInfo.info("Loading... Please Wait...");
			getUserMap(1);
		}
	}

	protected void getUserMap(int page) {
		if ((null != currentUser) && (null != currentBa)) {
			CorrConstants.corrAdminService.getUserMap(
					this.currentBa.getSystemPrefix(),
					currentUser.getUserLogin(),
					new AsyncCallback<ArrayList<UserMapClient>>() {
						public void onFailure(Throwable caught) {
							TbitsInfo
									.error("Could not fetch User Map from database... See logs for more information",
											caught);
							Log.error(
									"Could not fetch User Map from database...",
									caught);
						}

						public void onSuccess(ArrayList<UserMapClient> result) {
							if (null == result) {
								TbitsInfo
										.info("No map exists for the specified combination of BA and User Login...");
								singleGridContainer.removeAllModels();
								return;
							} else {
								singleGridContainer.removeAllModels();
								singleGridContainer.addModel(result);
								populateGridComboBoxes();
								if (isFirstRefresh) {
									/*
									 * FIXME: This is required because on the
									 * first load, the combo boxes containing
									 * the User Map Types are not populated.
									 * Only after a subsequent refresh takes
									 * place are they filled with the
									 * corresponding values. A dummy refresh is
									 * invoked here when the page is first
									 * loaded.
									 */
									isFirstRefresh = false;
									refresh(1);
								}
							}
						}
					});
		}
	}

	protected void onRemove() {
		final List<UserMapClient> selectedModels = singleGridContainer
				.getSelectedModels();
		if (selectedModels.size() > 0) {
			if (Window.confirm("Do you wish to delete " + selectedModels.size()
					+ " records?")) {
				CorrConstants.corrAdminService.deleteUserMapProperties(
						selectedModels, new AsyncCallback<Integer>() {

							@Override
							public void onFailure(Throwable caught) {
								TbitsInfo
										.error("Could not delete user maps.. See logs for details..",
												caught);
								Log.error(
										"Could not delete user maps.. See logs for details..",
										caught);
							}

							@Override
							public void onSuccess(Integer result) {
								if (1 == result) {
									TbitsInfo
											.info("Successfully deleted 1 property from database...");
									for (UserMapClient model : selectedModels) {
										singleGridContainer.getBulkGrid()
												.getStore().remove(model);
									}
								} else if (result > 1) {
									TbitsInfo.info("Successfully deleted "
											+ result
											+ " properties from database...");
									for (UserMapClient model : selectedModels) {
										singleGridContainer.getBulkGrid()
												.getStore().remove(model);
									}
								}
							}
						});
			}
		}
	}

	protected void onAdd() {

		UserMapClient newClient = new UserMapClient();
		newClient.setID("-1");
		newClient.setSysprefix("NULL");
		newClient.setUser(null);

		TypeClient newType = new TypeClient();
		newType.setName("NULL");
		newType.setDisplayName("NULL");
		newType.setDescription("NULL");
		newType.setSystemId(-1);
		newType.setTypeId(-1);

		FieldClient newField = new FieldClient();
		newField.setName("NULL");
		newField.setDescription("NULL");
		newField.setDisplayName("NULL");
		newField.setSystemId(-1);
		newField.setFieldId(-1);

		newClient.setType1(newType);
		newClient.setType2(newType);
		newClient.setType3(newType);
		newClient.setUserTypeField(newField);

		UserClient newUser = new UserClient();
		newUser.setUserLogin("NULL");
		newClient.setUserLoginValue(newUser);

		newClient.setStrictness("1");
		newClient.setStatus("---");
		singleGridContainer.addModel(newClient);

		if ((null != currentUser) && (null != currentBa)) {
			/*
			 * WORKAROUND: In an empty grid, if this is called, then the
			 * dropdowns of cells are not filled up automatically, so call them
			 * explicitly. Only fill the cell dropdowns if currentuser and
			 * currentBa are selected and a row has been added to the grid.
			 */
			populateGridComboBoxes();
		}
	}

	/**
	 * Populate the comboboxes of the grid.
	 */
	protected void populateGridComboBoxes() {
		((UserMapGrid) singleGridContainer.getBulkGrid()).getUserMapTypes();
		((UserMapGrid) commonGridContainer.getBulkGrid()).getUserMapTypes();

		((UserMapGrid) singleGridContainer.getBulkGrid()).populateComboBoxes();
		((UserMapGrid) commonGridContainer.getBulkGrid()).populateComboBoxes();
	}

	public UserMapClient getEmptyModel() {
		UserMapClient newClient = new UserMapClient();
		newClient.setID("-1");
		newClient.setSysprefix("NULL");
		newClient.setUser(null);

		TypeClient newType = new TypeClient();
		newType.setName("NULL");
		newType.setDisplayName("NULL");
		newType.setDescription("NULL");
		newType.setSystemId(-1);
		newType.setTypeId(-1);

		FieldClient newField = new FieldClient();
		newField.setName("NULL");
		newField.setDescription("NULL");
		newField.setDisplayName("NULL");
		newField.setSystemId(-1);
		newField.setFieldId(-1);

		newClient.setType1(newType);
		newClient.setType2(newType);
		newClient.setType3(newType);
		newClient.setUserTypeField(newField);

		UserClient newUser = new UserClient();
		newUser.setUserLogin("NULL");
		newClient.setUserLoginValue(newUser);

		newClient.setStrictness("1");
		newClient.setStatus("---");
		return newClient;
	}

	protected BulkUpdateGridAbstract<UserMapClient> getNewBulkGrid(
			BulkGridMode mode) {
		return new UserMapGrid(mode);
	}

	
}
