package digitalDC.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import transbit.tbits.domain.Field;
import transmittal.com.tbitsGlobal.client.TransmittalAbstractWizard;
import transmittal.com.tbitsGlobal.client.TransmittalAbstractWizardPage;
import transmittal.com.tbitsGlobal.client.WizardData;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

public class DDCFinalGridWizardPage extends
		TransmittalAbstractWizardPage<LayoutContainer, HashMap<String, String>> {

	private DDCFinalGridPanel panel;

	private EditorGrid<DocNumberFileTuple> grid = null;
	HashMap<String, Object> dtnDataMap;

	protected DDCFinalGridWizardPage(UIContext wizardContext) {
		super(wizardContext);
		dtnDataMap = new HashMap<String, Object>();

	}

	@SuppressWarnings("unchecked")
	@Override
	public void buildPage(WizardData data) {
		/*
		 * panel = new HolcimFinalGridPanel(wizardContext);
		 * 
		 * panel.getSingleGridContainer().removeAllModels();
		 * panel.getCommonGridContainer().removeAllModels();
		 * 
		 * this.getWidget().setScrollMode(Scroll.NONE);
		 * this.getWidget().add(panel, new FitData());
		 * 
		 * 
		 * panel.getSingleGridContainer().addModel(listOfData);
		 */

	/*	LabelField msgBox = new LabelField();
		msgBox.setReadOnly(true);
		msgBox
				.setValue("Please  check the logs for request not added or updated,It will due to entering incorrect values for fields like incorrect revision ");
		widget.add(msgBox, new LayoutData() {
		});*/

		ArrayList<DocNumberFileTuple> listOfData = (ArrayList<DocNumberFileTuple>) data
				.getData().get("FinalMapOfData");
		dtnDataMap = (HashMap<String, Object>) data.getData().get("dtnData");

		DDCFinalGridColumnConfig columnConfig = new DDCFinalGridColumnConfig();
		List<ColumnConfig> configs = columnConfig.configureColumns();

		grid = new EditorGrid<DocNumberFileTuple>(
				new ListStore<DocNumberFileTuple>(), new ColumnModel(configs));

		grid.setStyleAttribute("borderTop", "none");
		grid.setBorders(true);
		grid.setStripeRows(true);
		grid.getStore().add(listOfData);

		GridView view = new GridView() {
			protected void onColumnWidthChange(int column, int width) {
				super.onColumnWidthChange(column, width);
				this.refresh(false);
			}
		};

		grid.setView(view);
		this.getWidget().add(grid, new FitData());
		this.getWidget().setScrollMode(Scroll.NONE);

	}

	@Override
	public boolean funcToBeCalledOnBack() {
		dtnDataMap.put(Field.BUSINESS_AREA, ClientUtils.getCurrentBA()
				.getSystemId()
				+ "");
		final MessageBox messageBox = MessageBox.wait("Please Wait",
				"Transmittal is being created", "Please Wait...");
		DDCConstants.dbService.createDtn(
				DDCClientConstants.TRN_PROCESS_ID, ClientUtils
						.getCurrentUser(), dtnDataMap,
				new AsyncCallback<HashMap<String, Object>>() {

					@Override
					public void onFailure(Throwable caught) {
						messageBox.close();

						com.google.gwt.user.client.Window
								.alert("Some problem occured while creating the transmittal, Please check the logs");
					}

					@Override
					public void onSuccess(HashMap<String, Object> result) {

						Boolean success = (Boolean) result.get("success");
						if (success) {
							messageBox.close();

							String dtnNO = (String) result
									.get("ResultOfDtnProcess");
							com.google.gwt.user.client.Window
									.alert("Dtn has been created succcessfully"
											+ "\n" + "Dtn Id =  " + "\t"
											+ dtnNO);

						} else {
							messageBox.close();

							com.google.gwt.user.client.Window
									.alert("Some problem occured while creating the transmittal, Please check the logs");
						}
					}

				});
		return false;
	}

	@Override
	public int getDisplayOrder() {

		return 2;
	}

	@Override
	public HashMap<String, String> getValues() {

		return null;
	}

	@Override
	public LayoutContainer getWidget() {

		return widget;
	}

	@Override
	public void initializeWidget() {
		widget = new LayoutContainer(new FitLayout());
		widget.setScrollMode(Scroll.AUTO);

	}

	@Override
	public void onDisplay() {
		wizardContext.getValue(
				TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
				TransmittalAbstractWizard.class).hideNextButton();

		wizardContext.getValue(
				TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
				TransmittalAbstractWizard.class).hideBackButton();

		wizardContext.getValue(
				TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
				TransmittalAbstractWizard.class).showPreviewDOCButton();
	}

	@Override
	public void onInitialize() {

	}

	@Override
	public boolean onLeave() {

		return false;
	}

}
