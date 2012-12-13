package zipuploader.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import transmittal.com.tbitsGlobal.client.TransmittalAbstractWizard;
import transmittal.com.tbitsGlobal.client.TransmittalAbstractWizardPage;
import transmittal.com.tbitsGlobal.client.WizardData;
import zipuploader.com.tbitsGlobal.shared.HolcimPluginConstants;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

public class HolcimGridWizardPage extends
		TransmittalAbstractWizardPage<LayoutContainer, HashMap<String, String>> {

	private HolcimPanel panel;
	ArrayList<DocNumberFileTuple> ModelCarryingForNotFoundDocs;
	ArrayList<DocNumberFileTuple> PathsOfTheFoundData;
	final HolcimWizard holcimWizard;
	final WizardData wizardData;

	protected HolcimGridWizardPage(UIContext wizardContext,
			HolcimWizard holcimWizard, WizardData dataObject) {
		super(wizardContext);
		ModelCarryingForNotFoundDocs = new ArrayList<DocNumberFileTuple>();
		PathsOfTheFoundData = new ArrayList<DocNumberFileTuple>();
		this.holcimWizard = holcimWizard;
		wizardData = dataObject;

	}

	@Override
	public void buildPage(WizardData data) {
		/*
		 * params repofileId , sysid , field_name ,user,
		 */

		panel = new HolcimPanel(wizardContext);

		panel.getSingleGridContainer().removeAllModels();
		panel.getCommonGridContainer().removeAllModels();

		HolcimGridWizardPage.this.getWidget().setScrollMode(Scroll.NONE);
		HolcimGridWizardPage.this.getWidget().add(panel, new FitData());

		HolcimConstants.dbService.processRepoFileId(
				HolcimMultiUploader.repoFileId, ClientUtils.getCurrentBA()
						.getSystemId(), ClientUtils.getCurrentUser(),
				new AsyncCallback<HashMap<String, Object>>() {

					@Override
					public void onFailure(Throwable caught) {

						System.out.println("failure");
					}

					@SuppressWarnings("unchecked")
					@Override
					public void onSuccess(HashMap<String, Object> result) {

						// HashMap<Integer, TbitsTreeRequestData> requestData =
						// (HashMap<Integer, TbitsTreeRequestData>)
						// result.get("FMap");
						// deliverableFieldID TRN_PROCESS_ID docNoField

						/*
						 * panel = new HolcimPanel(wizardContext);
						 * 
						 * panel.getSingleGridContainer().removeAllModels();
						 * panel.getCommonGridContainer().removeAllModels();
						 * 
						 * HolcimGridWizardPage.this.getWidget().setScrollMode(
						 * Scroll.NONE);
						 * HolcimGridWizardPage.this.getWidget().add(panel, new
						 * FitData());
						 */

						ModelCarryingForNotFoundDocs = (ArrayList<DocNumberFileTuple>) result
								.get("NMap");

						PathsOfTheFoundData = (ArrayList<DocNumberFileTuple>) result
								.get("PathMap");

						ArrayList<DocNumberFileTuple> Foundrequests = new ArrayList<DocNumberFileTuple>();

						/*
						 * for (TbitsTreeRequestData req : requestData.values())
						 * { req.set("REQUESTID", new
						 * Integer(req.getRequestId())); Foundrequests.add(req);
						 * 
						 * }
						 */
						for (DocNumberFileTuple a : ModelCarryingForNotFoundDocs) {
							TbitsTreeRequestData ab = new TbitsTreeRequestData();
							// ab.set("SEPCODrawingNo", a.getDOC_NUMBER());
							// ab.set("REQUESTID", new Integer(-1));
							Foundrequests.add(a);
						}

						for (DocNumberFileTuple a : PathsOfTheFoundData) {
							TbitsTreeRequestData ab = new TbitsTreeRequestData();
							// ab.set("SEPCODrawingNo", a.getDOC_NUMBER());
							// ab.set("REQUESTID", new Integer(-1));
							Foundrequests.add(a);
						}

						panel.getSingleGridContainer().addModel(Foundrequests);
					}

				});

	}

	@Override
	public boolean funcToBeCalledOnBack() {
		List<DocNumberFileTuple> models = panel.getSingleGridContainer()
				.getModels();

		HolcimConstants.dbService.processGridData(
				(ArrayList<DocNumberFileTuple>) models, ClientUtils
						.getCurrentBA().getSystemId(),
				new AsyncCallback<HashMap<String, Object>>() {

					@Override
					public void onFailure(Throwable caught) {

						System.out.println("failure");
					}

					@SuppressWarnings("unchecked")
					@Override
					public void onSuccess(HashMap<String, Object> result) {

						Boolean verdict = (Boolean) result.get("result");

						ModelCarryingForNotFoundDocs = (ArrayList<DocNumberFileTuple>) result
								.get("NMap");

						PathsOfTheFoundData = (ArrayList<DocNumberFileTuple>) result
								.get("PathMap");

						ArrayList<DocNumberFileTuple> Foundrequests = new ArrayList<DocNumberFileTuple>();

						for (DocNumberFileTuple a : ModelCarryingForNotFoundDocs) {
							TbitsTreeRequestData ab = new TbitsTreeRequestData();
							// ab.set("SEPCODrawingNo", a.getDOC_NUMBER());
							// ab.set("REQUESTID", new Integer(-1));
							Foundrequests.add(a);
						}

						for (DocNumberFileTuple a : PathsOfTheFoundData) {
							TbitsTreeRequestData ab = new TbitsTreeRequestData();
							// ab.set("SEPCODrawingNo", a.getDOC_NUMBER());
							// ab.set("REQUESTID", new Integer(-1));
							Foundrequests.add(a);
						}
						panel.getSingleGridContainer().removeAllModels();
						panel.getSingleGridContainer().addModel(Foundrequests);

						if (!verdict) {
							Window
									.alert("Validation has been unsuccessfull, Please remove the incorrect data ");
						} else {
							Window
									.alert("Validation has been successfull, Please click Upate Request button to continue");
						}
					}
				});

		return false;
	}

	@Override
	public int getDisplayOrder() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public HashMap<String, String> getValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LayoutContainer getWidget() {
		// TODO Auto-generated method stub
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
				TransmittalAbstractWizard.class).showNextButton();

		wizardContext.getValue(
				TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
				TransmittalAbstractWizard.class).showBackButton();

		wizardContext.getValue(
				TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
				TransmittalAbstractWizard.class).hidePreviewDOCButton();
	}

	@Override
	public void onInitialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onLeave() {
		// TODO Auto-generated method stub

		/*
		 * result NMap PathMap
		 */
		List<DocNumberFileTuple> models = panel.getSingleGridContainer()
				.getModels();

		for (DocNumberFileTuple data : models) {
			if (data.getSubject() == null
					|| data.getSubject().trim().equals("")) {
				Window.alert("Subject/Title field can not be empty");
				return false;
			}
		}

		HolcimConstants.dbService.processGridData(
				(ArrayList<DocNumberFileTuple>) models, ClientUtils
						.getCurrentBA().getSystemId(),
				new AsyncCallback<HashMap<String, Object>>() {

					@Override
					public void onFailure(Throwable caught) {

						System.out.println("failure");
					}

					@SuppressWarnings("unchecked")
					@Override
					public void onSuccess(HashMap<String, Object> result) {

						Boolean verdict = (Boolean) result.get("result");

						ModelCarryingForNotFoundDocs = (ArrayList<DocNumberFileTuple>) result
								.get("NMap");

						PathsOfTheFoundData = (ArrayList<DocNumberFileTuple>) result
								.get("PathMap");

						ArrayList<DocNumberFileTuple> Foundrequests = new ArrayList<DocNumberFileTuple>();

						for (DocNumberFileTuple a : ModelCarryingForNotFoundDocs) {
							TbitsTreeRequestData ab = new TbitsTreeRequestData();
							// ab.set("SEPCODrawingNo", a.getDOC_NUMBER());
							// ab.set("REQUESTID", new Integer(-1));
							Foundrequests.add(a);
						}

						for (DocNumberFileTuple a : PathsOfTheFoundData) {
							TbitsTreeRequestData ab = new TbitsTreeRequestData();
							// ab.set("SEPCODrawingNo", a.getDOC_NUMBER());
							// ab.set("REQUESTID", new Integer(-1));
							Foundrequests.add(a);
						}
						panel.getSingleGridContainer().removeAllModels();
						panel.getSingleGridContainer().addModel(Foundrequests);

						if (!verdict) {
							Window
									.alert("Please remove the documents from the grid before progressing else enter a  existing doc number ");
						} else {

							// final MessageBox messageBox =
							// MessageBox.wait("Please Wait",
							// "Requests are being updated", "Please Wait...");

							HolcimConstants.dbService
									.AddAndUpdateRequest(
											HolcimPluginConstants.deliverableFieldID,
											ClientUtils.getCurrentBA()
													.getSystemId(),
											Foundrequests,
											ClientUtils.getCurrentUser(),
											new AsyncCallback<HashMap<String, Object>>() {

												@Override
												public void onFailure(
														Throwable caught) {

													caught.printStackTrace();
												}

												@Override
												public void onSuccess(
														HashMap<String, Object> result) {

													if (result != null) {
														// messageBox.close();
														Window
																.alert("Requests have been updated");

														ArrayList<DocNumberFileTuple> modelData = (ArrayList<DocNumberFileTuple>) result
																.get("modelData");

														wizardData
																.getData()
																.put(
																		"FinalMapOfData",
																		modelData);
														wizardData
																.getData()
																.put(
																		"dtnData",
																		result
																				.get("dataObjcet"));

														holcimWizard
																.callNext1Button(wizardData);
													} else {
														Window
																.alert("Some error ocurred while updating requests, Please check the logs");
													}

												}

											});

						}

					}
				});

		return false;
	}

}
