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
import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import corrGeneric.com.tbitsGlobal.client.CorrAdminUtils;
import corrGeneric.com.tbitsGlobal.client.CorrConstants;
import corrGeneric.com.tbitsGlobal.client.modelData.CorrNumberConfigClient;
import corrGeneric.com.tbitsGlobal.client.modelData.OnBehalfMapClient;

public class CorrNumberConfigPanel extends
		AbstractAdminBulkUpdatePanel<CorrNumberConfigClient> {
	protected BusinessAreaClient currentBa;
	protected boolean isFirstRefresh = true;
	protected StoreFilterField<CorrNumberConfigClient> filter;

	public CorrNumberConfigPanel() {
		super();

		isExcelImportSupported = false;
		canAddRows = true;
		canReorderRows = false;
		canCopyPasteRows = false;
		buildToolbar();

	}

	private void buildToolbar() {
		final ComboBox<BusinessAreaClient> bacombo = CorrAdminUtils
				.getBACombo();
		bacombo.setEmptyText("Please Select a BA");
		bacombo.setWidth(300);
		bacombo.addSelectionChangedListener(new SelectionChangedListener<BusinessAreaClient>() {
			public void selectionChanged(
					SelectionChangedEvent<BusinessAreaClient> se) {
				currentBa = se.getSelectedItem();
				bacombo.setValue(currentBa);
				((CorrNumberConfigGrid) commonGridContainer.getBulkGrid())
						.setCurrentBa(currentBa);
				((CorrNumberConfigGrid) singleGridContainer.getBulkGrid())
						.setCurrentBa(currentBa);
				refresh(1);
			}
		});

		toolbar.add(bacombo);
		LabelField filterLabel = new LabelField("Search : ");
		toolbar.add(filterLabel);

		applySearchFilter();
		toolbar.add(filter);
	}

	private void applySearchFilter() {
		filter = new StoreFilterField<CorrNumberConfigClient>() {

			@Override
			protected boolean doSelect(Store<CorrNumberConfigClient> store,
					CorrNumberConfigClient parent,
					CorrNumberConfigClient record, String property,
					String filter) {

				String numTyp1 = record.getNumType1().getDisplayName();
				numTyp1 = numTyp1.toLowerCase();

				String numTyp2 = record.getNumType2().getDisplayName();
				numTyp2 = numTyp2.toLowerCase();

				String numTyp3 = record.getNumType3().getDisplayName();
				numTyp3 = numTyp3.toLowerCase();

			/*	String numFields = record.getNumFields().getDisplayName();
				numFields = numFields.toLowerCase();

				String maxIdFields = record.getMaxIdFields().getDisplayName();
				maxIdFields = maxIdFields.toLowerCase();*/

				if ((numTyp1.contains(filter.toLowerCase()))
						|| (numTyp2.contains(filter.toLowerCase()))
						|| (numTyp3.contains(filter.toLowerCase())))

					return true;

				return false;
			}
		};
		filter.bind(((CorrNumberConfigGrid) singleGridContainer.getBulkGrid())
				.getStore());
		filter.setEmptyText(" Search ");

	}

	@Override
	public void refresh(int page) {
		if (null != currentBa) {
			TbitsInfo.info("Loading... Please Wait...");
			populateGridComboBoxes();
			getCorrNumberConfig(page);
		}

	}

	private void getCorrNumberConfig(int i) {

		if (null != currentBa) {
			CorrConstants.corrAdminService.getCorrNumberConfig(
					this.currentBa.getSystemPrefix(),
					new AsyncCallback<ArrayList<CorrNumberConfigClient>>() {

						@Override
						public void onFailure(Throwable caught) {
							TbitsInfo
									.error("Could not load Corr Number Config from database... See logs for more information...",
											caught);
							Log.error(
									"Could not load Corr Number Config from database...",
									caught);
						}

						@Override
						public void onSuccess(
								ArrayList<CorrNumberConfigClient> result) {
							if (null == result) {
								TbitsInfo
										.info("No number configuration exists for the specified BA ...");
								singleGridContainer.removeAllModels();
								return;
							} else {
								singleGridContainer.removeAllModels();
								singleGridContainer.addModel(result);
								populateGridComboBoxes();
								if (isFirstRefresh) {
									isFirstRefresh = false;
									refresh(0);
								}
							}
						}

					});
		}

	}

	private void populateGridComboBoxes() {
		((CorrNumberConfigGrid) singleGridContainer.getBulkGrid())
				.getCorrNumTypes();
		((CorrNumberConfigGrid) commonGridContainer.getBulkGrid())
				.getCorrNumTypes();

		((CorrNumberConfigGrid) singleGridContainer.getBulkGrid())
				.populateComboBoxes();
		((CorrNumberConfigGrid) commonGridContainer.getBulkGrid())
				.populateComboBoxes();

	}

	@Override
	protected void onSave(List<CorrNumberConfigClient> models, Button btn) {
		if (0 == models.size()) {
			TbitsInfo.info("Cannot save empty table...");
			return;
		}
		ArrayList<CorrNumberConfigClient> savedProperties = new ArrayList<CorrNumberConfigClient>();
		for (CorrNumberConfigClient property : models) {
			/*if ((null == property.getNumFields().getName())
					|| (property.getNumFields().getName().equals("NULL"))
					|| (property.getNumFields().getName().equals(""))) {
				TbitsInfo
						.error("Invalid value in 'Num Field'... Please enter a valid value...");
				return;
			}*/
			// property.setSysPrefix(currentBa.getSystemPrefix());
			savedProperties.add(property);
		}

		TbitsInfo.info("Saving... Please Wait...");
		CorrConstants.corrAdminService.saveCorrNumberConfig(savedProperties,
				new AsyncCallback<ArrayList<CorrNumberConfigClient>>() {

					@Override
					public void onFailure(Throwable caught) {
						TbitsInfo
								.error("Could not save Corr Number Config to database... See logs for more information...",
										caught);
						Log.error(
								"Could not save Corr Number Config to database...",
								caught);

					}

		
					@Override
					public void onSuccess(ArrayList<CorrNumberConfigClient> result) {
						if (null != result) {
							TbitsInfo
									.info("Successfully saved report properties to database...");
							singleGridContainer.removeAllModels();
							singleGridContainer.addModel(result);
						}
						
					}
				});
	}

	protected void getData() {
		CorrConstants.corrAdminService.getCorrNumberConfig(
				this.currentBa.getSystemPrefix(),
				new AsyncCallback<ArrayList<CorrNumberConfigClient>>() {

					@Override
					public void onFailure(Throwable caught) {
						TbitsInfo
								.error("Could not load Corr Number Config from database... See logs for more information...",
										caught);
						Log.error(
								"Could not load Corr Number Config from database...",
								caught);
					}

					@Override
					public void onSuccess(
							ArrayList<CorrNumberConfigClient> result) {
						if (null == result) {
							TbitsInfo
									.info("No number configuration exists for the specified BA ...");
							singleGridContainer.removeAllModels();
							return;
						} else {
							singleGridContainer.removeAllModels();
							singleGridContainer.addModel(result);
							populateGridComboBoxes();
							if (isFirstRefresh) {
								isFirstRefresh = false;
								refresh(0);
							}
						}
					}

				});
	}

	protected void onRemove() {
		final List<CorrNumberConfigClient> selectedModels = singleGridContainer
				.getSelectedModels();
		if (selectedModels.size() > 0) {
			if (Window.confirm("Do you wish to delete " + selectedModels.size()
					+ " records?")) {
				CorrConstants.corrAdminService.deleteCorrNumberConfig(
						selectedModels, new AsyncCallback<Integer>() {

							@Override
							public void onFailure(Throwable caught) {
								TbitsInfo
										.error("Could not delete corr number config.. See logs for details..",
												caught);
								Log.error(
										"Could not delete corr number config.. See logs for details..",
										caught);
							}

							@Override
							public void onSuccess(Integer result) {
								if (1 == result) {
									TbitsInfo
											.info("Successfully deleted 1 property from database...");
									for (CorrNumberConfigClient model : selectedModels) {
										singleGridContainer.getBulkGrid()
												.getStore().remove(model);
									}
								} else if (result > 1) {
									TbitsInfo.info("Successfully deleted "
											+ result
											+ " properties from database...");
									for (CorrNumberConfigClient model : selectedModels) {
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
/*
		FieldClient newField = new FieldClient();
		newField.setName("NULL");
		newField.setDescription("NULL");
		newField.setDisplayName("NULL");
		newField.setSystemId(-1);
		newField.setFieldId(-1);*/

		CorrNumberConfigClient newClient = new CorrNumberConfigClient();
		newClient.setId("-1");
		newClient.setSysPrefix(currentBa.getSystemPrefix());
		newClient.setNumType1(newType);
		newClient.setNumType2(newType);
		newClient.setNumType3(newType);
		newClient.setNumFormat("NULL");
		newClient.setNumFields("NULL");
		newClient.setMaxIdFormat("NULL");
		newClient.setMaxIdFields("NULL");
        newClient.setStatus("---");
		singleGridContainer.addModel(newClient);

		if ((null != currentBa)) {
			/*
			 * WORKAROUND: In an empty grid, if this is called, then the
			 * dropdowns of cells are not filled up automatically, so call them
			 * explicitly. Only fill the cell dropdowns if currentuser and
			 * currentBa are selected and a row has been added to the grid.
			 */
			populateGridComboBoxes();
		}
	}

	@Override
	public CorrNumberConfigClient getEmptyModel() {
		TypeClient newType = new TypeClient();
		newType.setName("NULL");
		newType.setDisplayName("NULL");
		newType.setDescription("NULL");
		newType.setSystemId(-1);
		newType.setTypeId(-1);

	/*	FieldClient newField = new FieldClient();
		newField.setName("NULL");
		newField.setDescription("NULL");
		newField.setDisplayName("NULL");
		newField.setSystemId(-1);
		newField.setFieldId(-1);*/

		CorrNumberConfigClient newClient = new CorrNumberConfigClient();
		newClient.setId("-1");
		newClient.setSysPrefix("NULL");
		newClient.setNumType1(newType);
		newClient.setNumType2(newType);
		newClient.setNumType3(newType);
		newClient.setNumFormat("NULL");
		newClient.setNumFields("NULL");
		newClient.setMaxIdFormat("NULL");
		newClient.setMaxIdFields("NULL");
		newClient.setStatus("---");
		return newClient;
	}

	@Override
	protected BulkUpdateGridAbstract<CorrNumberConfigClient> getNewBulkGrid(
			BulkGridMode mode) {

		return new CorrNumberConfigGrid(mode);
	}

}
