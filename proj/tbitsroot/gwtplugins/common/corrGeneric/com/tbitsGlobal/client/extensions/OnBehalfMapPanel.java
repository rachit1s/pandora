package corrGeneric.com.tbitsGlobal.client.extensions;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;

import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;

import corrGeneric.com.tbitsGlobal.client.CorrAdminUtils;
import corrGeneric.com.tbitsGlobal.client.CorrConstants;

import corrGeneric.com.tbitsGlobal.client.modelData.OnBehalfMapClient;

public class OnBehalfMapPanel extends
		AbstractAdminBulkUpdatePanel<OnBehalfMapClient> {

	protected BusinessAreaClient currentBa;
	protected UserClient currentUser;
	protected StoreFilterField<OnBehalfMapClient> filter;

	protected ComboBox<BusinessAreaClient> bacombo;
	protected ComboBox<UserClient> userComboBox;

	protected boolean isFirstRefresh = true;

	public OnBehalfMapPanel() {
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
		bacombo = CorrAdminUtils.getBACombo();
		userComboBox = CorrAdminUtils.getUserCombo();

		bacombo.setEmptyText("Please Select a BA");
		bacombo.setWidth(300);
		bacombo.addSelectionChangedListener(new SelectionChangedListener<BusinessAreaClient>() {
			public void selectionChanged(
					SelectionChangedEvent<BusinessAreaClient> se) {
				userComboBox.clearSelections();
				currentBa = se.getSelectedItem();
				bacombo.setValue(currentBa);
				((OnBehalfMapGrid) commonGridContainer.getBulkGrid())
						.setCurrentBa(currentBa);
				((OnBehalfMapGrid) singleGridContainer.getBulkGrid())
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
						refresh(0);
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
		filter = new StoreFilterField<OnBehalfMapClient>() {

			protected boolean doSelect(Store<OnBehalfMapClient> store,
					OnBehalfMapClient parent, OnBehalfMapClient record,
					String property, String filter) {

				String type1 = record.getType1().getDisplayName();
				type1 = type1.toLowerCase();

				String type2 = record.getType2().getDisplayName();
				type2 = type2.toLowerCase();

				String type3 = record.getType3().getDisplayName();
				type3 = type3.toLowerCase();

				String userLogin = record.getOnBehalfUser().getUserLogin();
				userLogin = userLogin.toLowerCase();

				if ((type1.contains(filter.toLowerCase()))
						|| (type2.contains(filter.toLowerCase()))
						|| (type3.contains(filter.toLowerCase()))
						|| (userLogin.contains(filter.toLowerCase())))
					return true;
				return false;
			}
		};
		filter.bind(((OnBehalfMapGrid) singleGridContainer.getBulkGrid())
				.getStore());
		filter.setEmptyText(" Search ");
	}

	public void refresh(int page) {
		if ((null != currentBa) && (null != currentUser)) {
			TbitsInfo.info("Loading... Please Wait...");
			populateGridComboBoxes();

			getOnBehalfMap(page);
		}
	}

	protected void getOnBehalfMap(int page) {
		if ((null != currentUser) && (null != currentBa)) {
			CorrConstants.corrAdminService.getOnBehalfMap(
					currentBa.getSystemPrefix(), currentUser.getUserLogin(),
					new AsyncCallback<ArrayList<OnBehalfMapClient>>() {

						public void onFailure(Throwable caught) {
							TbitsInfo
									.error("Could not fetch On Behalf Map from database... Please see logs for more information...",
											caught);
							Log.error(
									"Could not fetch On Behalf Map from database",
									caught);
						}

						public void onSuccess(
								ArrayList<OnBehalfMapClient> result) {
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
									 * the On Behalf Types are not populated.
									 * Only after a subsequent refresh takes
									 * place are they filled with the
									 * corresponding values. A dummy refresh is
									 * invoked here when the page is first
									 * loaded.
									 */

									isFirstRefresh = false;
									refresh(0);
								}
							}
						}
					});
		}
	}

	protected void populateGridComboBoxes() {
		((OnBehalfMapGrid) singleGridContainer.getBulkGrid())
				.getOnBehalfTypes();
		((OnBehalfMapGrid) commonGridContainer.getBulkGrid())
				.getOnBehalfTypes();

		((OnBehalfMapGrid) singleGridContainer.getBulkGrid())
				.populateComboBoxes();
		((OnBehalfMapGrid) commonGridContainer.getBulkGrid())
				.populateComboBoxes();
	}

	protected void onSave(List<OnBehalfMapClient> models, Button btn) {
		if (0 == models.size()) {
			TbitsInfo.info("Cannot save empty table...");
			return;
		}

		ArrayList<OnBehalfMapClient> savedProperties = new ArrayList<OnBehalfMapClient>();
		for (OnBehalfMapClient property : models) {

			if ((null == property.getOnBehalfUser()
					|| (property.getOnBehalfUser().getUserLogin().equals("")) || (property
					.getOnBehalfUser().getUserLogin().equals("NULL")))) {
				TbitsInfo
						.error("Invalid value in 'On Behalf Login' field... Please enter a valid value...");
				return;
			}

			property.setUser(currentUser);
			savedProperties.add(property);
		}
		TbitsInfo.info("Saving... Please Wait...");
		CorrConstants.corrAdminService.saveOnBehalfMap(savedProperties,
				new AsyncCallback<ArrayList<OnBehalfMapClient>>() {

					public void onFailure(Throwable caught) {
						TbitsInfo
								.error("Could not save On Behalf Map to database... See logs for more information...");
						Log.error(
								"Could not save On Behalf Map to database...",
								caught);
					}

					public void onSuccess(ArrayList<OnBehalfMapClient> result) {
						if (null != result) {
							TbitsInfo
									.info("Successfully saved report properties to database...");
							singleGridContainer.removeAllModels();
							singleGridContainer.addModel(result);
						}
					}

				});

	}

	protected void onRemove() {
		final List<OnBehalfMapClient> selectedModels = singleGridContainer
				.getSelectedModels();
		if (selectedModels.size() > 0) {
			if (Window.confirm("Do you wish to delete " + selectedModels.size()
					+ " records?")) {
				CorrConstants.corrAdminService.deleteOnBehalfProperties(
						selectedModels, new AsyncCallback<Integer>() {

							@Override
							public void onFailure(Throwable caught) {
								TbitsInfo
										.error("Could not delete on behalf maps.. See logs for details..",
												caught);
								Log.error(
										"Could not delete on behalf maps.. See logs for details..",
										caught);
							}

							@Override
							public void onSuccess(Integer result) {
								if (1 == result) {
									TbitsInfo
											.info("Successfully deleted 1 property from database...");
									for (OnBehalfMapClient model : selectedModels) {
										singleGridContainer.getBulkGrid()
												.getStore().remove(model);
									}
								} else if (result > 1) {
									TbitsInfo.info("Successfully deleted "
											+ result
											+ " properties from database...");
									for (OnBehalfMapClient model : selectedModels) {
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

		TypeClient newType = new TypeClient();
		newType.setName("NULL");
		newType.setDisplayName("NULL");
		newType.setDescription("NULL");
		newType.setSystemId(-1);
		newType.setTypeId(-1);

		OnBehalfMapClient newOnBehalfClient = new OnBehalfMapClient();
		newOnBehalfClient.setSysprefix(currentBa.getSystemPrefix());
		newOnBehalfClient.setID("-1");
		newOnBehalfClient.setUser(null);

		UserClient newUser = new UserClient();
		newUser.setUserLogin("NULL");

		newOnBehalfClient.setOnBehalfUser(newUser);
		newOnBehalfClient.setStatus("---");

		newOnBehalfClient.setType1(newType);
		newOnBehalfClient.setType2(newType);
		newOnBehalfClient.setType3(newType);

		singleGridContainer.addModel(newOnBehalfClient);

		if ((null != currentBa) && (null != currentUser)) {
			/*
			 * FIXME: In an empty grid, if this is called, then the dropdowns of
			 * cells are not filled up automatically, so call them explicitly.
			 * Only fill the cell dropdowns if currentuser and currentBa are
			 * selected and a row has been added to the grid.
			 */
			((OnBehalfMapGrid) singleGridContainer.getBulkGrid())
					.getOnBehalfTypes();
			((OnBehalfMapGrid) commonGridContainer.getBulkGrid())
					.getOnBehalfTypes();

			((OnBehalfMapGrid) singleGridContainer.getBulkGrid())
					.populateComboBoxes();
			((OnBehalfMapGrid) commonGridContainer.getBulkGrid())
					.populateComboBoxes();
		}
	}

	public OnBehalfMapClient getEmptyModel() {

		TypeClient newType = new TypeClient();
		newType.setName("NULL");
		newType.setDisplayName("NULL");
		newType.setDescription("NULL");
		newType.setSystemId(-1);
		newType.setTypeId(-1);

		OnBehalfMapClient newOnBehalfClient = new OnBehalfMapClient();
		newOnBehalfClient.setSysprefix("NULL");
		newOnBehalfClient.setID("-1");
		newOnBehalfClient.setUser(null);

		UserClient newUser = new UserClient();
		newUser.setUserLogin("NULL");
		newOnBehalfClient.setOnBehalfUser(newUser);
		newOnBehalfClient.setStatus("---");

		newOnBehalfClient.setType1(newType);
		newOnBehalfClient.setType2(newType);
		newOnBehalfClient.setType3(newType);
		return newOnBehalfClient;
	}

	protected BulkUpdateGridAbstract<OnBehalfMapClient> getNewBulkGrid(
			BulkGridMode mode) {
		return new OnBehalfMapGrid(mode);
	}

}
