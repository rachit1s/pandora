package com.tbitsGlobal.admin.client.widgets.pages;

import java.util.List;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.admin.client.AbstractAdminBulkUpdatePanel;
import com.tbitsGlobal.admin.client.modelData.EscalationHierarchiesClient;
import com.tbitsGlobal.admin.client.modelData.EscalationHierarchyValuesClient;
import com.tbitsGlobal.admin.client.utils.APConstants;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;

/**
 * this class is for basic structure of escaltion hierarchy panel.
 * 
 * @author Nirmal Agrawal
 * 
 * 
 */
public class EscalationHierarchyMapPanel extends
		AbstractAdminBulkUpdatePanel<EscalationHierarchyValuesClient> {

	private EscalationHierarchiesClient currentHierarchy;
	ComboBox<EscalationHierarchiesClient> hierarchyCombo;

	public EscalationHierarchyMapPanel() {
		super();
		commonGridDisabled = true;
		isExcelImportSupported = false;
		canAddRows = true;
		canReorderRows = false;
		canCopyPasteRows = false;
		ToolBarButton addEscalationBtn = new ToolBarButton(
				"Manage Escalation Hierarchies");
		addEscalationBtn
				.addSelectionListener(new SelectionListener<ButtonEvent>() {

					@Override
					public void componentSelected(ButtonEvent ce) {

						insertNewGrid();

					}
				});

		hierarchyCombo = getEscalationHierarchyCombo();
		hierarchyCombo
				.addSelectionChangedListener(new SelectionChangedListener<EscalationHierarchiesClient>() {
					public void selectionChanged(
							SelectionChangedEvent<EscalationHierarchiesClient> se) {
						currentHierarchy = se.getSelectedItem();
						refresh(0);
					}
				});

		toolbar.add(hierarchyCombo);
		toolbar.add(addEscalationBtn);

	}

	@Override
	protected void onSave(List<EscalationHierarchyValuesClient> models,
			final Button btn) {
		if (currentHierarchy != null) {
			btn.setText("Saving... Please Wait...");
			btn.disable();

			TbitsInfo.info("Saving... Please Wait...");
			APConstants.apService.saveEscalationHierarchyValues(
					currentHierarchy, models,
					new AsyncCallback<List<EscalationHierarchyValuesClient>>() {
						public void onFailure(Throwable caught) {
							TbitsInfo.error(
									"Error in saving escalation hierarchies values  : "
											+ caught.getMessage(), caught);
							Log
									.error(
											"Error in saving escalation hierarchies values : ",
											caught);
							btn.setText("Save");
							btn.enable();
						}

						public void onSuccess(
								List<EscalationHierarchyValuesClient> result) {
							singleGridContainer.removeAllModels();
							singleGridContainer.addModel(result);
							btn.setText("Save");
							btn.enable();
						}
					});
		}
		refresh(0);

	}

	@Override
	public EscalationHierarchyValuesClient getEmptyModel() {

		return new EscalationHierarchyValuesClient();
	}

	@Override
	protected BulkUpdateGridAbstract<EscalationHierarchyValuesClient> getNewBulkGrid(
			BulkGridMode mode) {

		return new EscalationHierarchyGrid(mode);
	}

	@Override
	public void refresh(int page) {
		if (currentHierarchy != null) {
			TbitsInfo.info("Loading... Please Wait...");
			APConstants.apService.getEscalationHierarchiesValues(
					currentHierarchy,
					new AsyncCallback<List<EscalationHierarchyValuesClient>>() {

						@Override
						public void onFailure(Throwable caught) {
							TbitsInfo.error(
									"Error fetching post process parameters",
									caught);
							Log.error("Error fetching post process parameters",
									caught);

						}

						@Override
						public void onSuccess(
								List<EscalationHierarchyValuesClient> result) {

							singleGridContainer.removeAllModels();
							singleGridContainer.addModel(result);

						}
					});
		}
		hierarchyCombo = getEscalationHierarchyCombo();

	}

	public static ComboBox<EscalationHierarchiesClient> getEscalationHierarchyCombo() {
		final ListStore<EscalationHierarchiesClient> escHierarchyListStore = new ListStore<EscalationHierarchiesClient>();
		APConstants.apService
				.getEscalationHierarchies(new AsyncCallback<List<EscalationHierarchiesClient>>() {

					@Override
					public void onFailure(Throwable caught) {

						TbitsInfo.error(
								"Error in fetching escalations hierarchies",
								caught);
						Log.error("Error in fetching escalations hierarchies",
								caught);

					}

					@Override
					public void onSuccess(
							List<EscalationHierarchiesClient> result) {
						if (result != null) {
							escHierarchyListStore.add(result);

						}

					}
				});
		ComboBox<EscalationHierarchiesClient> escHirarchyCombo = new ComboBox<EscalationHierarchiesClient>();
		escHirarchyCombo.setWidth(400);
		escHirarchyCombo.setStore(escHierarchyListStore);
		escHirarchyCombo.setDisplayField(EscalationHierarchiesClient.ESC_ID);
		escHirarchyCombo.setTemplate(getEscHierarchyTemplate());
		escHirarchyCombo.setEmptyText("Select an Escalation hierarchy");
		return escHirarchyCombo;

	}

	private static native String getEscHierarchyTemplate() /*-{
		return [ '<tpl for=".">',
				'<div class="x-combo-list-item">{name} [{esc_id}]</div>',
				'</tpl>' ].join("");
	}-*/;

	protected void insertNewGrid() {
		final Window addEscDetailsWin = new Window();
		addEscDetailsWin.setSize(600, 400);
		addEscDetailsWin.setLayout(new FitLayout());
		addEscDetailsWin.setHeading("Escalation hierarchy details");
		EscalationHierarchyDetailsView eview = new EscalationHierarchyDetailsView();
		addEscDetailsWin.add(eview);
		addEscDetailsWin.show();
	}

}
