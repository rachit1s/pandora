package transmittal.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transmittal.com.tbitsGlobal.client.models.AttachmentModel;
import transmittal.com.tbitsGlobal.client.models.DrawinglistModel;
import transmittal.com.tbitsGlobal.client.models.TrnEditableColumns;

//import transbit.tbits.common.DatabaseException;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.JaguarConstants;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;

import commons.com.tbitsGlobal.utils.client.wizards.IWizardPage;

/**
 * Initiate the transmittal wizard
 * 
 */
public class TransmittalWizard extends TransmittalAbstractWizard {

	TransmittalWizardConstants twc;
	WizardData data;

	private static final String NOTIFY = "notify";
	private static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	private static final String ACCESS_TO = "AccessTo";
	private static final String FIELD_ID = "field_id";
	private static final String IS_DTN_NUMBER_PART = "is_dtn_number_part";
	private static final String DISPLAY_NAME = "display_name";
	private static final String FIELD_ORDER = "field_order";
	private static final String FIELD_CONFIG = "field_config";
	private static final String DTN_SIGNATORY = "dtnSignatory";
	private static final String DRAFTED_BY = "draftedBy";
	private static final String EMAIL_BODY = "emailBody";
	private static final String TRANSMITTAL_SUBJECT = "transmittalSubject";
	private static final String REMARKS = "Remarks";
	private static final String ACTUAL_DATE = "actualDate";
	private static final String ACTUAL_NUMBER = "actualNumber";
	private static final String CC_LIST = "ccList";
	private static final String TO_LIST = "toList";

	public TransmittalWizard() {
		super();
		twc = TransmittalWizardConstants.getInstance();

	}

	public WizardData getDataObject() {
		return data;
	}

	public void setDataObject(WizardData data) {
		this.data = data;
	}

	
	/**
	 * @param requestIds
	 *            - list of request id's passed to the transmittal wizard
	 */
	public TransmittalWizard(ArrayList<Integer> requestIds) {
		this();
		this.addBackButton();
		this.addNextButton();
		this.addFinishButton();

		this.finishBtn.setText("Create Transmittal");
		this.addPreviewDOCButton();
		processIds(requestIds);

	}

	public TransmittalWizard(TbitsTreeRequestData requestModel, boolean editable) {
		this();
		this.addBackButton();
		this.addNextButton();
		this.addFinishButton();

		this.finishBtn.setText("Create Transmittal");

		int request_id = requestModel.getRequestId();
		int system_id = requestModel.getSystemId();

		try {
			processRequestModel(requestModel, editable, request_id, system_id);
		} catch (NumberFormatException e) {
			// 
			e.printStackTrace();
		} catch (TbitsExceptionClient e) {

			e.printStackTrace();
		}
	}

	/**
	 * Method to process the selected request in the approval business area.
	 * 
	 * @param systemId
	 * @param requestId
	 * @param requestModel
	 *            :Selected request
	 * @param editable
	 *            :Whether changes in the wizard supposed to be done
	 * @throws TbitsExceptionClient
	 * @throws NumberFormatException
	 */
	private void processRequestModel(final TbitsTreeRequestData requestModel,
			final boolean editable, final int requestId, final int systemId)
			throws TbitsExceptionClient, NumberFormatException {
		data = new WizardData();
		data.getData().put("PresentInApprovalBA", String.valueOf(true));
		data.getData().put("CurrentApprovalBASysID", systemId);
		data.getData().put("CurrentApprovalRequestID", requestId);
		TransmittalConstants.dbService.getTansParams(requestId, systemId,
				new AsyncCallback<HashMap<String, Object>>() {

					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}

					@SuppressWarnings("unchecked")
					public void onSuccess(HashMap<String, Object> result) {

						ArrayList<TbitsTreeRequestData> TreeRequestList = (ArrayList<TbitsTreeRequestData>) result
								.get("TreeModeldataOfRequest");

						if (TreeRequestList.size() == 0) {
							Window
									.alert("No Requests Found,They may have been deleted,Hence exiting");
						} else {
							if (editable) {
								try {
									HashMap<String, String> hm = (HashMap<String, String>) result
											.get("HashMapForTransientData");

									if (hm.containsKey("version")) {
										data.getData().put("Wizard_Version",
												String.valueOf(true));

									} else {
										data.getData().put("Wizard_Version",
												String.valueOf(false));
									}

									data.getData().put(
											"currentbaclient",
											ClientUtils.getBAbySysId(Integer
													.parseInt((String) result
															.get("sysid"))));

									data.getData().putAll(result);

									ArrayList<TbitsTreeRequestData> TreeRequestData = (ArrayList<TbitsTreeRequestData>) result
											.get("TreeModeldataOfRequest");
									String requestListInString = "";
									for (TbitsTreeRequestData trd : TreeRequestData) {

										requestListInString = (requestListInString
												.equals("")) ? String
												.valueOf(trd.getRequestId())
												: requestListInString
														+ ","
														+ (Integer) trd
																.getRequestId();
									}

									data.getData().put("requestList",
											requestListInString);
									data.getData().put("isApprovalCycle",
											String.valueOf(false));
									data.getData().put("inapprovalcycle",
											String.valueOf(true));
								
									
									ProcessParametersWizardPage page1 = new ProcessParametersWizardPage(
											context, data);
									TransmittalWizard.this.addPage(page1);
									TransmittalWizard.this
											.addPage(new AttachmentSelectionPage(
													context, data));
									TransmittalWizard.this
											.addPage(new DistributionDataSelectionPage(
													context,data));
									TransmittalWizard.this
											.addPage(new ReportPreviewPage(
													context,data));
									TransmittalWizard.this.show();
									activePage = page1;
									activePage.onDisplay();
								} catch (Exception e) {
									e.printStackTrace();
								}
							} else {
								if ((((HashMap<String, Object>) result
										.get("HashMapForTransientData"))
										.containsKey("version"))) {

									((HashMap<String, String>) result
											.get("HashMapForTransientData"))
											.put("isApprovalCycle", String.valueOf(false));

								
									data.getData().putAll(result);
									HashMap<String, ArrayList<DrawinglistModel>>hm_drawing = (HashMap<String, ArrayList<DrawinglistModel>>) result
											.get("ConvertedDrawingList");
									ArrayList<AttachmentModel> attchementList = (ArrayList<AttachmentModel>) result
											.get("ConvertedAttachmentTable");

									try {
										new Utils(data).updateRequestListWithDefaultVlaues();
										new Utils(data)
												.updateDrawingListOfRequest(hm_drawing);
										new Utils(data)
												.updateAttchmentListOfRequest(attchementList);
										createGenericTransmittalProcess((HashMap<String, String>) result
												.get("HashMapForTransientData"));
									} catch (TbitsExceptionClient e) {

										e.printStackTrace();
									}

								} else {
									Window
											.alert("Transmittal cant be created on the basis of Old data in this mode,Please Consider Creating Transmittal using Wizard");
								}
							}
						}
					}
				});

	}

	/**
	 * Add the preview document button, this is for approval cycle
	 */
	protected void addPreviewDOCButton() {
		previewDOCBtn = new Button("Submit For Approval",
				new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						try {
							if (activePage.onLeave())
								onSubmitForApproval();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
		this.addButton(previewDOCBtn);
	}

	/**
	 * while leaving the wizard and in approval cycle, disable the preview
	 * button
	 */
	protected void onSubmitForApproval() {
		this.previewDOCBtn.disable();
		
		data.getData().put("isApprovalCycle", String.valueOf(true));

		onSubmit();
	}

	@Override
	protected void addPreviewPDFButton() {

	}

/*	private HashMap<String, String> getParamTable1() {
		HashMap<String, String> paramTable = new HashMap<String, String>();

		for (ITransmittalWizardPage<? extends LayoutContainer, ?> page : pages
				.values()) {
			HashMap<String, String> values = (HashMap<String, String>) page
					.getValues();
			if (values != null) {
				paramTable.putAll(values);
			}
		}
		return paramTable;
	}*/

	private HashMap<String, String> getParamTable() {
		HashMap<String, String> paramTable = new HashMap<String, String>();

		for (String key : data.getData().keySet()) {
			if (!(key.equals("BAFields") || key.equals("mapOfRequests")
					|| key.equals("ListOfRequests")
					|| key.equals("currentbaclient")
					|| key.equals("validationRulesList")
					|| key.equals("distributionTableColumnsList")
					|| key.equals("attachmentTableColumnList")
					|| key.equals("trnExtendedFields")
					|| key.equals("dropdownlist") || key
					.equals("transmittalProcessParams"))) {

				paramTable.put(key, (String) (data.getData().get(key) + ""));
			}
		}

		return paramTable;
	}

	/**
	 * Gather the parameters for creating the transmittal and call transmittal
	 * creator
	 */
	protected void onSubmit() {
		this.finishBtn.disable();
		final HashMap<String, String> paramTable = getParamTable();
		paramTable.put("user", ClientUtils.getCurrentUser().getUserLogin());

		TbitsModelData processParams = (TbitsModelData) data.getData().get(
				"transmittalProcessParams");
		for (String key : processParams.getPropertyNames()) {
			if (paramTable.get(key) == null) {
				String value = String.valueOf(processParams.get(key));
				paramTable.put(key, value);
			}
		}
		createGenericTransmittalProcess(paramTable);
	}

	/**
	 * Create the transmittal from the parameters received
	 * 
	 * @param paramTable
	 *            - transmittal parameter values
	 * 
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void createGenericTransmittalProcess(
			HashMap<String, String> paramTable) {

		if(!Boolean.valueOf((String)getDataObject().getData().get("PresentInApprovalBA")))
		{
		HashMap<Integer, HashMap<String, List<FileClient>>> attachmentInfoClientsMap = new HashMap<Integer, HashMap<String, List<FileClient>>>();

		String deliverableNames = paramTable
				.get(TransmittalConstants.DELIVERABLE_FIELD_NAME);
		if (deliverableNames == null)
			return;
		else {
			String[] deliverableNameArray = deliverableNames.split(",");
			HashMap<Integer, TbitsTreeRequestData> requestMap = (HashMap<Integer, TbitsTreeRequestData>) data
					.getData().get("mapOfRequests");
			for (Integer requestId : requestMap.keySet()) {

				final HashMap<String, List<FileClient>> deliverableFileClients = new HashMap<String, List<FileClient>>();

				for (String fieldName : deliverableNameArray) {
					TbitsTreeRequestData trd = requestMap.get(requestId);
					if (trd != null) {
						if (fieldName != null) {
							POJO obj = trd.getAsPOJO(fieldName);
							if (obj != null) {
								List<FileClient> delAttachments = ((POJOAttachment) obj)
										.getValue();
								deliverableFileClients.put(fieldName,
										delAttachments);
							}
						}
					}
				}
				attachmentInfoClientsMap.put(requestId, deliverableFileClients);
			}
		}

		final Boolean isApproval = Boolean.valueOf(paramTable
				.get("isApprovalCycle"));
		String trnMessage = "Transmittal is being created";
		if (isApproval)
			trnMessage = "Submission for Approval is in process.";
		final MessageBox messageBox = MessageBox.wait("Please Wait",
				trnMessage, "Please Wait...");

		try {
			TransmittalConstants.dbService.createTransmittal(paramTable,
					attachmentInfoClientsMap, new AsyncCallback<String>() {

						public void onFailure(Throwable caught) {
							caught.printStackTrace();
							messageBox.close();
							TbitsInfo.error(caught.getMessage(), caught);
							Window
									.alert("Transmittal process did not succeed: "
											+ caught.getLocalizedMessage());
							TransmittalWizard.this.hide(finishBtn);
						}

						public void onSuccess(String result) {
							messageBox.close();
							if ((result != null) && (!result.trim().equals(""))) {
								TransmittalWizard.this.hide();
								if (isApproval) {
									Window
											.alert("Submission for approval successful: "
													+ result);
								} else {
									Window
											.alert("Transmittal created successfully: "
													+ result);
								}
								if (previewDOCBtn != null)
									TransmittalWizard.this.hide(previewDOCBtn);
								TransmittalWizard.this.close();
								TransmittalWizard.this.hide(finishBtn);
							} else {
								if (isApproval)
									Window
											.alert("Submission for approval did not succeed.");
								else
									Window
											.alert("Transmittal process did not succeed.");
								TransmittalWizard.this.hide(finishBtn);
								TransmittalWizard.this.close();
								if (previewDOCBtn != null)
									TransmittalWizard.this.hide(previewDOCBtn);
							}
						}
					});
		} catch (TbitsExceptionClient e) {
			e.printStackTrace();
			if (isApproval)
				Window.alert("Submission for approval did not succeed.");
			else
				Window.alert("Transmittal process was not successful. \n"
						+ e.toString());
			TransmittalWizard.this.hide(finishBtn);
		}
		}
		else
		{
			createGenericTransmittalProcessInApprovalBA(paramTable);
		}
	}

	@SuppressWarnings("unchecked")
	private void createGenericTransmittalProcessInApprovalBA(
			HashMap<String, String> paramTable) {

		HashMap<Integer, HashMap<String, List<FileClient>>> attachmentInfoClientsMap = new HashMap<Integer, HashMap<String, List<FileClient>>>();

		String deliverableNames = paramTable
				.get(TransmittalConstants.DELIVERABLE_FIELD_NAME);
		if (deliverableNames == null)
			return;
		else {
			String[] deliverableNameArray = deliverableNames.split(",");
			HashMap<Integer, TbitsTreeRequestData> requestMap = (HashMap<Integer, TbitsTreeRequestData>) data
					.getData().get("mapOfRequests");
			for (Integer requestId : requestMap.keySet()) {

				final HashMap<String, List<FileClient>> deliverableFileClients = new HashMap<String, List<FileClient>>();

				for (String fieldName : deliverableNameArray) {
					TbitsTreeRequestData trd = requestMap.get(requestId);
					if (trd != null) {
						if (fieldName != null) {
							POJO obj = trd.getAsPOJO(fieldName);
							if (obj != null) {
								List<FileClient> delAttachments = ((POJOAttachment) obj)
										.getValue();
								deliverableFileClients.put(fieldName,
										delAttachments);
							}
						}
					}
				}
				attachmentInfoClientsMap.put(requestId, deliverableFileClients);
			}
		}

		final Boolean isApproval = Boolean.valueOf(paramTable
				.get("isApprovalCycle"));
		String trnMessage = "Transmittal is being created";
		if (isApproval)
			trnMessage = "Submission for Approval is in process.";
		final MessageBox messageBox = MessageBox.wait("Please Wait",
				trnMessage, "Please Wait...");

		try {
			TransmittalConstants.dbService.createTransmittalPostApproval(((Integer) getDataObject().getData().get("CurrentApprovalBASysID")), ((Integer) getDataObject().getData().get("CurrentApprovalRequestID")), paramTable, attachmentInfoClientsMap, new AsyncCallback<String>() {

						public void onFailure(Throwable caught) {
							caught.printStackTrace();
							messageBox.close();
							TbitsInfo.error(caught.getMessage(), caught);
							Window
									.alert("Transmittal process did not succeed: "
											+ caught.getLocalizedMessage());
							TransmittalWizard.this.hide(finishBtn);
						}

						public void onSuccess(String result) {
							messageBox.close();
							if ((result != null) && (!result.trim().equals(""))) {
								TransmittalWizard.this.hide();
								if (isApproval) {
									Window
											.alert("Submission for approval successful: "
													+ result);
								} else {
									Window
											.alert("Transmittal created successfully: "
													+ result);
								}
								if (previewDOCBtn != null)
									TransmittalWizard.this.hide(previewDOCBtn);
								TransmittalWizard.this.hide(finishBtn);
							} else {
								if (isApproval)
									Window
											.alert("Submission for approval did not succeed.");
								else
									Window
											.alert("Transmittal process did not succeed.");
								TransmittalWizard.this.hide(finishBtn);
								if (previewDOCBtn != null)
									TransmittalWizard.this.hide(previewDOCBtn);
							}
						}
					});
		} catch (TbitsExceptionClient e) {
			e.printStackTrace();
			if (isApproval)
				Window.alert("Submission for approval did not succeed.");
			else
				Window.alert("Transmittal process was not successful. \n"
						+ e.toString());
			TransmittalWizard.this.hide(finishBtn);
		}
	}
	
	public static String printStackTrace(Object[] stackTrace) {
		// add the class name and any message passed to constructor
		// final StringBuilder result = new StringBuilder( "" );
		// result.append(aThrowable.toString());
		// final String NEW_LINE = "\n";
		// result.append(NEW_LINE);
		//	  
		// //add each element of the stack trace
		// for (StackTraceElement element : aThrowable.getStackTrace() ){
		// if (aThrowable instanceof TbitsExceptionClient)
		// {
		// result.append( element );
		// result.append( NEW_LINE );
		// }
		// }
		// return result.toString();

		String output = "";
		for (Object line : stackTrace) {
			output += line + "<br/>";
		}
		return output;
	}

	/**
	 * Call each page in the wizard one by one and collect the properties in a
	 * common table in tranmittalConstants
	 * 
	 * @param requestIds
	 */
	private void processIds(final ArrayList<Integer> requestIds) {
		
		try {
			TransmittalConstants.dbService.getTansParamsBeforeTransmittal(
					ClientUtils.getCurrentBA().getSystemPrefix(), ClientUtils
							.getCurrentBA().getSystemId(), requestIds,
					new AsyncCallback<HashMap<String, Object>>() {
						public void onFailure(Throwable caught) {
							caught.printStackTrace();
							Window
									.alert("Error occurred while contacting the database. Please try again");
						}

						@SuppressWarnings( { "deprecation", "unchecked" })
						@Override
						public void onSuccess(HashMap<String, Object> result) {

							ArrayList<BAField> fields = new ArrayList<BAField>();
							fields.addAll(CacheRepository.getInstance()
									.getCache(FieldCache.class).getMap()
									.values());

							HashMap<Integer, TbitsTreeRequestData> mapOfRequests = (HashMap<Integer, TbitsTreeRequestData>) result
									.get("mapOfRequests");
							ArrayList<TbitsTreeRequestData> requestList = new ArrayList<TbitsTreeRequestData>();
							for (TbitsTreeRequestData tmd : mapOfRequests
									.values()) {
								requestList.add(tmd);
							}

							String requestListInString = "";
							for (TbitsTreeRequestData trd : requestList) {

								requestListInString = (requestListInString
										.equals("")) ? String.valueOf(trd
										.getRequestId()) : requestListInString
										+ "," + (Integer) trd.getRequestId();
							}


							data = new WizardData();
							data.getData().put("PresentInApprovalBA", String.valueOf(false));
							data.getData().put("BAFields", fields);
							data.getData().put("TreeModeldataOfRequest", requestList);
							data.getData().put("requestList",
									requestListInString);
							data.getData().put("currentbaclient",
									ClientUtils.getCurrentBA());
							data.getData().put("isApprovalCycle",
									String.valueOf(false));
							data.getData().put("inapprovalcycle",
									String.valueOf(false));
							data.getData().put("version", "new");
							data.getData().put("Type_Value_Source_Supported",String.valueOf(true) );
							if (Boolean.valueOf((String) result
									.get("defaultProcessExists"))) {
								TbitsModelData processParams = (TbitsModelData) result
										.get("transmittalProcessParams");
								data.getData().put(
										"trnProcessId",
										(Integer) processParams
												.get("trnProcessId"));
								data.setData(result);
							} else {
								data.getData().putAll(result);
							}

							ProcessParametersWizardPage page1 = new ProcessParametersWizardPage(
									context, data);
							TransmittalWizard.this.addPage(page1);
							TransmittalWizard.this
									.addPage(new AttachmentSelectionPage(
											context, data));
							TransmittalWizard.this
									.addPage(new DistributionDataSelectionPage(
											context, data));
							TransmittalWizard.this
									.addPage(new ReportPreviewPage(context,
											data));
							TransmittalWizard.this.show();
							activePage = page1;
							activePage.onDisplay();

						}

					});
		} catch (NumberFormatException e) {

			e.printStackTrace();
		} catch (TbitsExceptionClient e) {

			e.printStackTrace();
		}
	}

	/**
	 * This method adds a page to the wizard. Insert a page in the order and
	 * define the page before and the page after this page. (similar to
	 * inserting in a linked list)
	 * 
	 * @param a
	 *            {@link IWizardPage}
	 */
	protected void addPage(
			ITransmittalWizardPage<? extends LayoutContainer, ?> page) {
		this.add(page.getWidget());
		page.onInitialize();
		int displayOrder = page.getDisplayOrder();
		if (pages.containsKey(displayOrder - 1)) {
			ITransmittalWizardPage<? extends LayoutContainer, ?> prePage = pages
					.get(displayOrder - 1);
			prePage.setNext(page);
			page.setPrevious(prePage);
		}
		if (pages.containsKey(displayOrder + 1)) {
			ITransmittalWizardPage<? extends LayoutContainer, ?> nextPage = pages
					.get(displayOrder + 1);
			nextPage.setPrevious(page);
			page.setNext(nextPage);
		}
		pages.put(displayOrder, page);
	}

	@Override
	protected void addFinishTransmittalButtonForPage1() {
		FinishTransmittalForPage1Btn = new Button("Preview Transmittal",
				new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						try {
							if (activePage.onLeave())
								onAbruptFinishTransmittal();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				});
		this.addButton(FinishTransmittalForPage1Btn);

	}

	@Override
	protected void addFinishTransmittalButtonForPage2() {
		FinishTransmittalForPage2Btn = new Button("Preview Transmittal",
				new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						try {
							if (activePage.onLeave())
								onAbruptFinishTransmittal();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				});
		this.addButton(FinishTransmittalForPage2Btn);

	}

	/**
	 * 
	 * @param isMergeAllAttachments
	 * @return
	 */
	private JSONArray getSelectedAttachmentslist() {

		JSONArray attachmentModel = new JSONArray();
		int count = 0;
		for (Integer requestId : twc.getRequests().keySet()) {
			TbitsTreeRequestData trd = twc.getRequests().get(requestId);

			JSONArray temp = setJsonFieldValue1(trd);
			attachmentModel.set(count, temp);
			count++;

		}
		return attachmentModel;
	}

	private JSONArray setJsonFieldValue1(TbitsTreeRequestData trd) {
		BAField baField;
		JSONArray finalArr = new JSONArray();
		JSONArray tempArr1 = new JSONArray();
		JSONArray tempArr = new JSONArray();
		JSONArray tempArr2 = new JSONArray();
		finalArr.set(0, new JSONString(trd.getRequestId() + ""));
		finalArr.set(1, new JSONString((String) trd.get("subject")));
		int count1 = 2;
		if (twc.getAttachmentTableColumnsList() != null) {
			for (TbitsModelData tmd : twc.getAttachmentTableColumnsList()) {
				int dataTypeId = (Integer) tmd.get("data_type_id");
				boolean isEditable = (Boolean) tmd.get("is_editable");
				Integer fieldId = (Integer) tmd
						.get(TransmittalConstants.FIELD_ID_COLUMN);
				if (fieldId > 0) {
					baField = Utils.getBAFieldById(fieldId, twc
							.getBaFieldList());

					if (baField != null) {
						if (trd != null) {
							if (TransmittalConstants.ATTACHMENTS == baField
									.getDataTypeId()
									&& isEditable) {

								String property = baField.getName();
								tempArr = new JSONArray();
								tempArr.set(0, new JSONString(property));

								POJO obj = trd.getAsPOJO(property);

								if (obj == null
										|| !(obj instanceof POJOAttachment)) {
									obj = new POJOAttachment(
											new ArrayList<FileClient>());
								}

								List<FileClient> delAttachments = ((POJOAttachment) obj)
										.getValue();

								if (delAttachments.size() == 0) {
									tempArr.set(1, new JSONString(""));
									finalArr.set(count1, tempArr);
									++count1;
								} else {
									tempArr2 = new JSONArray();
									int count = 0;
									for (FileClient eachAttachMent : delAttachments) {
										tempArr1 = new JSONArray();
										tempArr1.set(0, new JSONString(
												eachAttachMent.getFileName()));
										tempArr1.set(1, new JSONString(
												eachAttachMent
														.getRequestFileId()
														+ ""));
										tempArr2.set(count, tempArr1);
										count++;
									}
									tempArr.set(1, tempArr2);
									finalArr.set(count1, tempArr);
									++count1;
								}

							}
						}
					}
				}
			}
		}

		return finalArr;

	}

	private JSONArray getDrawingListsJsonString(
			List<TbitsTreeRequestData> models, ArrayList<String> baFieldNames) {
		JSONArray tableJson = new JSONArray();
		int count = 0;
		for (TbitsTreeRequestData model : models) {
			int reqId = model.getRequestId();
			TbitsTreeRequestData trd = twc.getRequests().get(reqId);
			if (trd != null) {

				JSONArray drawingListValues = new JSONArray();
				for (int i = 0; i < baFieldNames.size(); i++) {
					String fValue = String
							.valueOf(trd.get(baFieldNames.get(i)));

					BAField baField = Utils.getBAFieldByname(baFieldNames
							.get(i), twc.getBaFieldList());
					String field_id = baField.getFieldId() + "";
					if (baField instanceof BAFieldCombo) {
						fValue = getTypeValueBasedOnConfig(
								(BAFieldCombo) baField, fValue);
					}

					drawingListValues.set(i + 1, new JSONString(field_id + ","
							+ fValue));
				}
				drawingListValues.set(0, new JSONString(model.getRequestId()
						+ ""));
				tableJson.set(count, drawingListValues);
				count++;
			}
		}
		return tableJson;
	}

	protected String getTypeValueBasedOnConfig(BAFieldCombo baField,
			String currentTypeName) {
		String fValue = currentTypeName;
		int typeValueSrc = 0;
		for (TbitsModelData md : twc.getAttachmentTableColumnsList()) {
			Integer fieldId = (Integer) md
					.get(TransmittalConstants.FIELD_ID_COLUMN);
			if (fieldId == baField.getFieldId()) {
				typeValueSrc = (Integer) md
						.get(TransmittalConstants.TYPE_VALUE_SOURCE_COLUMN);
				if (typeValueSrc < 2)
					return fValue;
				break;
			}
		}

		List<TypeClient> types = ((BAFieldCombo) baField).getTypes();
		for (TypeClient type : types) {
			String typeName = type.getName();
			if (typeName.equals(fValue)) {
				switch (typeValueSrc) {
				case 0:
				case 1:
					fValue = type.getName();
					break;
				case 2:
					fValue = type.getDisplayName();
					break;
				case 3:
					fValue = type.getDescription();
					break;
				}
				return fValue;
			}
		}
		return fValue;
	}

	HashMap<String, String> getDataForPage1() {
		return twc.getDataOfPage1();
	}

	HashMap<String, String> getDataForPage2() {
		HashMap<String, String> valuesMap = new HashMap<String, String>();
		List<TbitsTreeRequestData> models = twc.getRequestList();
		BAField baField;
		ArrayList<String> baFieldNames = new ArrayList<String>();
		for (TbitsModelData md : twc.getAttachmentTableColumnsList()) {
			int dataTypeId = (Integer) md
					.get(TransmittalConstants.DATA_TYPE_ID_COLUMN);
			if (dataTypeId != TransmittalConstants.ATTACHMENTS) {
				Integer fieldId = (Integer) md
						.get(TransmittalConstants.FIELD_ID_COLUMN);
				if (fieldId > 0) {

					baField = Utils.getBAFieldById(fieldId, twc
							.getBaFieldList());

					if (baField != null) {
						baFieldNames.add(baField.getName());
					}
				}
			}
		}
		String deliverableAttachmentProperties = "";
		for (TbitsModelData md : twc.getAttachmentTableColumnsList()) {
			if (((Integer) md.get("data_type_id")) == TransmittalConstants.ATTACHMENTS) {
				deliverableAttachmentProperties = ((deliverableAttachmentProperties
						.equals("")) ? (String) md.get("name")
						: deliverableAttachmentProperties + ","
								+ (String) md.get("name"));
			}
		}

		JSONArray tableJson = getDrawingListsJsonString(models, baFieldNames);
		valuesMap.put(TransmittalConstants.DRAWING_TABLE_KEY_WORD, tableJson
				.toString());
		valuesMap.put(TransmittalConstants.DELIVERABLE_FIELD_NAME,
				deliverableAttachmentProperties);

		valuesMap.put("SelectedAttachmentsTable", getSelectedAttachmentslist()
				.toString());

		return valuesMap;
	}

	public HashMap<String, String> getDataForPage3() {
		HashMap<String, String> valuesMap = new HashMap<String, String>();
		List<TbitsTreeRequestData> models = twc.getRequestList();

		// Using json instead of creating delimiter-separated strings to pass
		// values.
		ArrayList<String> propertyList = new ArrayList<String>();
		for (TbitsModelData md : twc.getDistributionTableColumnsList()) {
			propertyList.add((String) md.get("name"));
		}

		try {
			JSONArray tableJson = getDistributionListJsonString(models,
					propertyList);
			valuesMap.put(TransmittalConstants.DISTRIBUTION_TABLE, tableJson
					.toString());
		} catch (JSONException je) {
			Window.alert(je.getMessage());
		}
		return valuesMap;
	}

	private JSONArray getDistributionListJsonString(
			List<TbitsTreeRequestData> models, ArrayList<String> fieldNames)
			throws JSONException {
		JSONArray tableJson = new JSONArray();
		int count = 0;
		for (TbitsTreeRequestData model : models) {
			JSONArray drawingListValues = new JSONArray();
			for (int i = 0; i < fieldNames.size(); i++) {
				String fValue = String.valueOf(model.get(fieldNames.get(i)));
				String prop = fieldNames.get(i);
				fValue = prop + "," + fValue;
				drawingListValues.set(i + 1, new JSONString(fValue));
			}
			tableJson.set(count, drawingListValues);
			count++;
		}
		return tableJson;
	}

	public void addBackButton() {
		backBtn = new Button("Back", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				try {
				if(	activePage.funcToBeCalledOnBack())
				{
					int current = activePage.getDisplayOrder();
					if (pages.containsKey(current - 1)) {
						ITransmittalWizardPage<? extends LayoutContainer, ?> prePage = activePage
								.getPrevious();
						layout.setActiveItem(prePage.getWidget());
						activePage = prePage;
						activePage.onDisplay();
					}
				}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		this.addButton(backBtn);
	}

	public void addNextButton() {
		nextBtn = new Button("Next", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				try {
					int current = activePage.getDisplayOrder();
					if (pages.containsKey(current + 1)) {
						if (activePage.onLeave()) {
							ITransmittalWizardPage<? extends LayoutContainer, ?> nextPage = activePage
									.getNext();
							layout.setActiveItem(nextPage.getWidget());
							activePage = nextPage;
							activePage.onDisplay();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		this.addButton(nextBtn);
	}

	private void onAbruptFinishTransmittal() {

		ITransmittalWizardPage<? extends LayoutContainer, ?> nextPage = pages
				.get(3);
		layout.setActiveItem(nextPage.getWidget());
		activePage = pages.get(3);
		activePage.onDisplay();

	}

	public void addFinishButton() {
		finishBtn = new Button("Finish", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				try {
					if (activePage.onLeave())
						onSubmit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		this.addButton(finishBtn);
	}
}
