package transmittal.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import transmittal.com.tbitsGlobal.client.models.DrawinglistModel;
import transmittal.com.tbitsGlobal.client.models.TrnEditableColumns;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;

/**
 * @author rohit Page 2 For transmittal creator wizard 2 page
 * 
 */
public class AttachmentSelectionPage extends
		TransmittalAbstractWizardPage<ContentPanel, HashMap<String, String>> {

	private WizardData dataObject;
	public ArrayList<Integer> rqstid = new ArrayList<Integer>();
	private static final String IS_MERGE_SELECTED_ATTACHMENTS = "IsMergeSelectedAttachments";
	static final String DEFAULT_VALUE = "default_value";
	static final String IS_EDITABLE = "is_editable";
	static final String DATA_TYPE_ID = "data_type_id";
	Boolean flagForAttachmentGrid = false;

	private String deliverableAttachmentProperties = "";
	private static final String SELECTED_ATTACHMENTS_TABLE = "SelectedAttachmentsTable";
	private int prevTrnProcessId = 0;
	private Page2Panel panel;
	AttachmentSelectionTableColumnsConfiguration columnConfiguration;
	public TransmittalWizardConstants twc;
	int visits = 0;
	private Label label;

	public AttachmentSelectionPage(UIContext wizardContext, WizardData data) {
		super(wizardContext);
		setDataObject(data);
		columnConfiguration = new AttachmentSelectionTableColumnsConfiguration(
				data);

	}

	public WizardData getDataObject() {
		return dataObject;
	}

	public void setDataObject(WizardData dataObject) {
		this.dataObject = dataObject;
	}

	/**
	 * Method building the 2 page of the wizard
	 */
	public int getDisplayOrder() {
		return 1;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, String> getValues() {

		HashMap<String, String> valuesMap = new HashMap<String, String>();
		List<TbitsTreeRequestData> models = panel.getSingleGridContainer()
				.getModels();
		BAField baField;
		ArrayList<String> baFieldNames = new ArrayList<String>();
		for (TbitsModelData md : (ArrayList<TbitsModelData>) getDataObject()
				.getData().get("attachmentTableColumnList")) {
			int dataTypeId = (Integer) md
					.get(TransmittalConstants.DATA_TYPE_ID_COLUMN);
			if (dataTypeId != TransmittalConstants.ATTACHMENTS) {
				Integer fieldId = (Integer) md
						.get(TransmittalConstants.FIELD_ID_COLUMN);
				if (fieldId > 0) {

					baField = Utils.getBAFieldById(fieldId,
							(ArrayList<BAField>) getDataObject().getData().get(
									"BAFields"));

					if (baField != null) {
						baFieldNames.add(baField.getName());
					}
				}
			}
		}

		JSONArray tableJson = getDrawingListsJsonString(models, baFieldNames);
		valuesMap.put(TransmittalConstants.DRAWING_TABLE_KEY_WORD, tableJson
				.toString());
		valuesMap.put(TransmittalConstants.DELIVERABLE_FIELD_NAME,
				deliverableAttachmentProperties);

		boolean isMergeSelected = false;
		String isMergeSelectedAttStr = twc.getTransmittalProcessParams().get(
				IS_MERGE_SELECTED_ATTACHMENTS);
		if ((isMergeSelectedAttStr != null)
				&& (!isMergeSelectedAttStr.trim().equals("")))
			isMergeSelected = Boolean.parseBoolean(isMergeSelectedAttStr);
		valuesMap.put(SELECTED_ATTACHMENTS_TABLE, getSelectedAttachmentslist()
				.toString());

		return valuesMap;
	}

	@SuppressWarnings("unchecked")
	public void setValues() {

		List<TbitsTreeRequestData> models = panel.getSingleGridContainer()
				.getModels();
		BAField baField;
		ArrayList<TbitsModelData> attachmentSelectionColumns = (ArrayList<TbitsModelData>) getDataObject()
				.getData().get("attachmentTableColumnList");
		ArrayList<BAField> BAFieldList = (ArrayList<BAField>) getDataObject()
				.getData().get("BAFields");

		ArrayList<String> baFieldNames = new ArrayList<String>();
		for (TbitsModelData md : attachmentSelectionColumns) {
			int dataTypeId = (Integer) md
					.get(TransmittalConstants.DATA_TYPE_ID_COLUMN);
			if (dataTypeId != TransmittalConstants.ATTACHMENTS) {
				Integer fieldId = (Integer) md
						.get(TransmittalConstants.FIELD_ID_COLUMN);
				if (fieldId > 0) {

					baField = Utils.getBAFieldById(fieldId, BAFieldList);

					if (baField != null) {
						baFieldNames.add(baField.getName());
					}
				}
			}
		}

		JSONArray tableJson = getDrawingListsJsonString(models, baFieldNames);
		getDataObject().getData().put(
				TransmittalConstants.DRAWING_TABLE_KEY_WORD,
				tableJson.toString());
		getDataObject().getData().put(
				TransmittalConstants.DELIVERABLE_FIELD_NAME,
				deliverableAttachmentProperties);

		getDataObject().getData().put(SELECTED_ATTACHMENTS_TABLE,
				getSelectedAttachmentslist().toString());

	}

	/**
	 * Returns the drawing lists for the request
	 * 
	 * @param models
	 * @param baFieldNames
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private JSONArray getDrawingListsJsonString(
			List<TbitsTreeRequestData> models, ArrayList<String> baFieldNames) {
		JSONArray tableJson = new JSONArray();
		int count = 0;
		HashMap<Integer, TbitsTreeRequestData> requestMap = (HashMap<Integer, TbitsTreeRequestData>) getDataObject()
				.getData().get("mapOfRequests");

		ArrayList<BAField> BAFieldList = (ArrayList<BAField>) getDataObject()
				.getData().get("BAFields");

		for (TbitsTreeRequestData model : models) {
			int reqId = model.getRequestId();
			TbitsTreeRequestData trd = requestMap.get(reqId);
			if (trd != null) {

				JSONArray drawingListValues = new JSONArray();
				for (int i = 0; i < baFieldNames.size(); i++) {

					JSONArray tempJsonArrray1 = new JSONArray();
					String fValue = String
							.valueOf(trd.get(baFieldNames.get(i)));

					BAField baField = Utils.getBAFieldByname(baFieldNames
							.get(i), BAFieldList);
					String field_id = baField.getFieldId() + "";

					tempJsonArrray1.set(0, new JSONString(field_id));
					//

					StringBuffer tempStr = new StringBuffer("");
					JSONArray tempJsonArrray = new JSONArray();
					if (baField instanceof BAFieldCombo) {

						fValue = getTypeValueBasedOnConfigTypeSouceReturned(
								(BAFieldCombo) baField, fValue, tempStr);
						tempJsonArrray.set(0, new JSONString(fValue));
						tempJsonArrray.set(1,
								new JSONString(tempStr.toString()));
						tempJsonArrray1.set(1, tempJsonArrray);

					} else {
						tempJsonArrray1.set(1, new JSONString(fValue));
					}

					drawingListValues.set(i + 1, tempJsonArrray1);

				}
				drawingListValues.set(0, new JSONString(model.getRequestId()
						+ ""));
				tableJson.set(count, drawingListValues);
				count++;
			}
		}
		return tableJson;
	}

	@SuppressWarnings("unchecked")
	protected String getTypeValueBasedOnConfig(BAFieldCombo baField,
			String currentTypeName) {
		String fValue = currentTypeName;
		int typeValueSrc = 0;
		ArrayList<TbitsModelData> attachmentSelectionColumns = (ArrayList<TbitsModelData>) getDataObject()
				.getData().get("attachmentTableColumnList");

		for (TbitsModelData md : attachmentSelectionColumns) {
			Integer fieldId = (Integer) md
					.get(TransmittalConstants.FIELD_ID_COLUMN);
			if (fieldId == baField.getFieldId()) {
				typeValueSrc = (Integer) md
						.get(TransmittalConstants.TYPE_VALUE_SOURCE_COLUMN);
				/*
				 * if (typeValueSrc < 2) // return fValue;
				 */break;

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

	@SuppressWarnings("unchecked")
	protected String getTypeValueBasedOnConfigTypeSouceReturned(
			BAFieldCombo baField, String currentTypeName, StringBuffer str) {
		String fValue = currentTypeName;
		int typeValueSrc = 0;
		ArrayList<TbitsModelData> attachmentSelectionColumns = (ArrayList<TbitsModelData>) getDataObject()
				.getData().get("attachmentTableColumnList");

		for (TbitsModelData md : attachmentSelectionColumns) {
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
					str = str.append("name");
					fValue = type.getName();
					break;
				case 2:
					str = str.append("display_name");
					fValue = type.getDisplayName();
					break;
				case 3:
					str = str.append("description");
					fValue = type.getDescription();
					break;
				}
				return fValue;
			}
		}
		return fValue;
	}

	public ContentPanel getWidget() {
		return widget;
	}

	public void initializeWidget() {
		widget = new ContentPanel(new FitLayout());
		widget.setScrollMode(Scroll.AUTO);
		label = new Label();
		label.setHeight(20);
		label.setText("Attachment Selection Page");
		widget.setHeading(label.getText());
	}

	public void onDisplay() {

		String status = (String) getDataObject().getData().get(
				"statusOfFinishButton");
		Boolean flag = Boolean.valueOf(status);
		if (flag) {
			wizardContext.getValue(
					TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
					TransmittalAbstractWizard.class).showFinishButton();
			wizardContext.getValue(
					TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
					TransmittalAbstractWizard.class).hidePreviewDOCButton();
		} else {
			wizardContext.getValue(
					TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
					TransmittalAbstractWizard.class).showPreviewDOCButton();
			wizardContext.getValue(
					TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
					TransmittalAbstractWizard.class).hideFinishButton();
		}

		wizardContext.getValue(
				TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
				TransmittalAbstractWizard.class).showNextButton();
		wizardContext.getValue(
				TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
				TransmittalAbstractWizard.class).showBackButton();

		wizardContext.getValue(
				TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
				TransmittalAbstractWizard.class)
				.showFinishTransmittalForPage2Btn();
		wizardContext.getValue(
				TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
				TransmittalAbstractWizard.class)
				.hideFinishTransmittalForPage1Btn();

	}

	public void onInitialize() {

	}

	@SuppressWarnings("unchecked")
	public boolean onLeave() {
		if (canMoveToNext()) {
			HashSet<String> hs = new HashSet<String>();

			HashMap<Integer, HashMap<String, TransmittalAttachmentContainer>> requestAttachmentContainerMap = panel
					.getSingleGridColumnConfig()
					.getRequestAttachmentContainerMap();
			List<TbitsTreeRequestData> models = panel.getSingleGridContainer()
					.getModels();
			HashMap<Integer, TbitsTreeRequestData> requestMap = (HashMap<Integer, TbitsTreeRequestData>) getDataObject()
					.getData().get("mapOfRequests");
			
			for (TbitsTreeRequestData tmd : models) 
			{
				Object rqst = tmd.getRequestId();
				rqstid.add(Integer.parseInt(rqst.toString()));
			}

			ArrayList<Integer> selectedrqstid = new ArrayList<Integer>();
			
			for(Integer a : requestMap.keySet())
			{
				Object selrqstid = a;
				selectedrqstid.add(Integer.parseInt(selrqstid.toString()));
			}
			
			for(Integer b : rqstid)
			{
			  selectedrqstid.remove(b);
			}
			
			for(Integer s :selectedrqstid)
			{
				requestMap.remove(s);
			}
			
			HashMap<String, Object> abc = new HashMap<String, Object>();
            abc.putAll(getDataObject().getData());
            ArrayList<TbitsTreeRequestData> requestList = new ArrayList<TbitsTreeRequestData>();
			for (TbitsTreeRequestData tmd : requestMap
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
			
			JSONArray attachmentModel = new JSONArray();
			int count = 0;
			for (Integer requestId : requestMap.keySet()) {
				TbitsTreeRequestData trd = requestMap.get(requestId);

				JSONArray temp = setJsonFieldValue(trd);
				attachmentModel.set(count, temp);
				count++;

			}
			
			BAField baField;
			ArrayList<TbitsModelData> attachmentSelectionColumns = (ArrayList<TbitsModelData>) getDataObject()
					.getData().get("attachmentTableColumnList");
			ArrayList<BAField> BAFieldList = (ArrayList<BAField>) getDataObject()
					.getData().get("BAFields");

			ArrayList<String> baFieldNames = new ArrayList<String>();
			for (TbitsModelData md : attachmentSelectionColumns) {
				int dataTypeId = (Integer) md
						.get(TransmittalConstants.DATA_TYPE_ID_COLUMN);
				if (dataTypeId != TransmittalConstants.ATTACHMENTS) {
					Integer fieldId = (Integer) md
							.get(TransmittalConstants.FIELD_ID_COLUMN);
					if (fieldId > 0) {

						baField = Utils.getBAFieldById(fieldId, BAFieldList);

						if (baField != null) {
							baFieldNames.add(baField.getName());
						}
					}
				}
			}
			
			JSONArray tableJson = getDrawingListsJsonString(models, baFieldNames);
			
			abc.remove("TreeModeldataOfRequest");
			abc.remove(TransmittalConstants.DRAWING_TABLE_KEY_WORD);
			abc.remove(SELECTED_ATTACHMENTS_TABLE);
			abc.remove("drawingTable");
			abc.remove("mapOfRequests");
			abc.put("TreeModeldataOfRequest", requestList);
			abc.put("requestList", requestListInString);
			abc.put(SELECTED_ATTACHMENTS_TABLE, attachmentModel.toString());
			abc.put(TransmittalConstants.DRAWING_TABLE_KEY_WORD, tableJson.toString());
			abc.put("mapOfRequests", requestMap);
			getDataObject().getData().clear();
			getDataObject().getData().putAll(abc);
			rqstid.clear();
			
			for (Integer reqId : requestMap.keySet()) {
				TbitsTreeRequestData trd = requestMap.get(reqId);
				if (trd != null) {
					HashMap<String, TransmittalAttachmentContainer> tacMap = requestAttachmentContainerMap
							.get(reqId);

					if (tacMap != null) {
						for (String fieldName : tacMap.keySet()) {
							if (fieldName != null) {
								hs.add(fieldName);
								TransmittalAttachmentContainer tac = tacMap
										.get(fieldName);
								List<FileClient> aicList = new ArrayList<FileClient>();
								for (CheckBox cb : tac.checkBoxList) {
									if (Boolean.parseBoolean(cb.getRawValue())) {
										FileClient aic = cb
												.getData(TransmittalConstants.PROPERTY_ATTACHMENT_INFO);
										if (aic != null)
											aicList.add(aic);
									}
								}
								trd.set(fieldName, new POJOAttachment(aicList));
							}
						}
					}
				}
			}
			String attNames = "";
			for (String s : hs)
				attNames = (attNames.equals("")) ? s : attNames + "," + s;
			deliverableAttachmentProperties = attNames;
			setValues();
			this.nextPage.buildPage(dataObject);
			return true;
		} else {
			TbitsInfo.info("Please wait until grid is loaded completely");
			Window.alert("Grid is being loaded,Please wait");
			return false;
		}
	}

	/**
	 * Returns the Json array for the attachment list
	 * 
	 * @param isMergeAllAttachments
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private JSONArray getSelectedAttachmentslist() {

		HashMap<Integer, TbitsTreeRequestData> requestMap = (HashMap<Integer, TbitsTreeRequestData>) getDataObject()
				.getData().get("mapOfRequests");
		JSONArray attachmentModel = new JSONArray();
		int count = 0;
		for (Integer requestId : requestMap.keySet()) {
			TbitsTreeRequestData trd = requestMap.get(requestId);

			JSONArray temp = setJsonFieldValue(trd);
			attachmentModel.set(count, temp);
			count++;

		}
		return attachmentModel;
	}

	/**
	 * Methods used to form json array for attachment list
	 * 
	 * @param trd
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private JSONArray setJsonFieldValue(TbitsTreeRequestData trd) {
		BAField baField;
		JSONArray finalArr = new JSONArray();
		JSONArray tempArr1 = new JSONArray();
		JSONArray tempArr = new JSONArray();
		JSONArray tempArr2 = new JSONArray();
		finalArr.set(0, new JSONString(trd.getRequestId() + ""));
		finalArr.set(1, new JSONString((String) trd.get("subject")));
		int count1 = 2;
		ArrayList<TbitsModelData> attachmentSelectionColumns = (ArrayList<TbitsModelData>) getDataObject()
				.getData().get("attachmentTableColumnList");
		ArrayList<BAField> BAFieldList = (ArrayList<BAField>) getDataObject()
				.getData().get("BAFields");

		if (attachmentSelectionColumns != null) {
			for (TbitsModelData tmd : attachmentSelectionColumns) {

				Integer fieldId = (Integer) tmd
						.get(TransmittalConstants.FIELD_ID_COLUMN);
				if (fieldId > 0) {
					baField = Utils.getBAFieldById(fieldId, BAFieldList);

					if (baField != null) {
						if (trd != null) {
							if (TransmittalConstants.ATTACHMENTS == baField
									.getDataTypeId()) {

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

	/**
	 * @param model
	 * @param fieldNames
	 * @return
	 */
	@SuppressWarnings("unused")
	private JSONArray getDistributionListJsonString(
			List<TbitsModelData> models, ArrayList<String> fieldNames)
			throws JSONException {
		JSONArray tableJson = new JSONArray();
		int count = 0;
		for (TbitsModelData model : models) {
			JSONArray drawingListValues = new JSONArray();
			for (int i = 0; i < fieldNames.size(); i++) {
				String fValue = String.valueOf(model.get(fieldNames.get(i)));
				drawingListValues.set(i + 1, new JSONString(fValue));
			}
			tableJson.set(count, drawingListValues);
			count++;
		}
		return tableJson;
	}

	// void buildGrid() {
	//
	// HashMap<String, Object> data = getDataObject().getData();
	// ArrayList<TbitsModelData> attachmentSelectionColumns =
	// (ArrayList<TbitsModelData>) getDataObject()
	// .getData().get("attachmentTableColumnList");
	// TbitsModelData tmd0 = attachmentSelectionColumns.get(0);
	// int currentTrnId = Integer
	// .parseInt((String) tmd0.get("trn_process_id"));
	// // if grid is null or new process is there
	// if ((prevTrnProcessId != currentTrnId)) {
	//
	// prevTrnProcessId = currentTrnId;
	//
	// List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
	// AttachmentSelectionTableColumnsConfiguration columnConfiguration2 = new
	// AttachmentSelectionTableColumnsConfiguration();
	// configs = columnConfiguration2.configureColumns();
	//
	// if ((configs == null) || configs.isEmpty()) {
	// Window
	// .alert("Attachment selection table could not configured. Please contact tBits team.");
	// return;
	// }
	//
	// HashMap<Integer, TbitsTreeRequestData> requests = (HashMap<Integer,
	// TbitsTreeRequestData>) getDataObject()
	// .getData().get("mapOfRequests");
	// ArrayList<BAField> BAFieldList = (ArrayList<BAField>) getDataObject()
	// .getData().get("BAFields");
	// if (requests == null) {
	// TbitsInfo
	// .info("No drawings/documents selected for transmittal. Please select drawings/documents for transmittal.");
	// Window
	// .alert("No drawings/documents selected for transmittal. Please select drawings/documents for transmittal."
	// +
	// "Please close the transmittal window and select drawings/documents for transmittal.");
	// return;
	// }
	//
	// for (TbitsTreeRequestData trd : requests.values()) {
	// for (TbitsModelData tmd : attachmentSelectionColumns) {
	// Integer fieldId = (Integer) tmd
	// .get(TransmittalConstants.FIELD_ID_COLUMN);
	// if (fieldId > 0) {
	// int dataTypeId = (Integer) tmd.get(DATA_TYPE_ID);
	// boolean isEditable = (Boolean) tmd.get(IS_EDITABLE);
	// String defValue = (String) tmd.get(DEFAULT_VALUE);
	//
	// BAField baField;
	//
	// baField = Utils.getBAFieldById(fieldId, BAFieldList);
	//
	// if (isEditable && (baField != null)) {
	// String property = baField.getName();
	// if (dataTypeId != TransmittalConstants.ATTACHMENTS) {
	//
	// if (dataTypeId == TransmittalConstants.TYPE) {
	// if ((defValue != null)
	// && (!defValue.trim().equals(""))) {
	// TypeClient typeClient = ((BAFieldCombo) baField)
	// .getModelForName(defValue);
	// if (typeClient != null)
	// trd.set(property, defValue);
	// else
	// Window
	// .alert("No defualt type found for field: "
	// + baField
	// .getDisplayName());
	// }
	// } else {
	// if ((defValue == null)
	// || (defValue.trim().equals("")))
	// defValue = "-";
	// trd.set(property, defValue);
	// }
	// }
	// }
	// }
	// }
	// }
	//
	// ArrayList<TbitsTreeRequestData> gridData = new
	// ArrayList<TbitsTreeRequestData>(
	// requests.values());
	//
	// if ((grid == null)) {
	//
	// grid = new EditorGrid<TbitsTreeRequestData>(null, null);
	// panel = new Page2Panel(wizardContext);
	//
	// panel.getSingleGridContainer().removeAllModels();
	// panel.getCommonGridContainer().removeAllModels();
	// panel.getSingleGridContainer().getBulkGrid().getView().refresh(
	// true);
	// panel.getCommonGridContainer().getBulkGrid().getView().refresh(
	// true);
	// panel.getSingleGridContainer().addModel(gridData);
	//
	// this.getWidget().setScrollMode(Scroll.NONE);
	// this.getWidget().add(panel, new FitData());
	// } else {
	// panel.refreshgrid(gridData, configs, columnConfiguration2);
	//
	// }
	// }
	//
	// }

	// public void buildPage1(WizardData params) {
	//
	// HashMap<String,Object>data=params.getData();
	// ArrayList<TbitsModelData>attachmentSelectionColumns =
	// (ArrayList<TbitsModelData>)
	// params.getData().get("attachmentTableColumnList");
	// TbitsModelData tmd0 = attachmentSelectionColumns.get(0);
	// int currentTrnId = Integer.parseInt((String) tmd0
	// .get("trn_process_id"));
	// if ((grid == null) || (prevTrnProcessId != currentTrnId)) {
	//
	// prevTrnProcessId = currentTrnId;
	//
	// List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
	// AttachmentSelectionTableColumnsConfiguration columnConfiguration2 = new
	// AttachmentSelectionTableColumnsConfiguration();
	// configs = columnConfiguration2.configureColumns();
	//
	// if ((configs == null) || configs.isEmpty()) {
	// Window
	// .alert("Attachment selection table could not configured. Please contact tBits team.");
	// return;
	// }
	//
	// HashMap<Integer, TbitsTreeRequestData> requests = twc
	// .getRequests();
	// if (requests == null) {
	// TbitsInfo
	// .info("No drawings/documents selected for transmittal. Please select drawings/documents for transmittal.");
	// Window
	// .alert("No drawings/documents selected for transmittal. Please select drawings/documents for transmittal."
	// +
	// "Please close the transmittal window and select drawings/documents for transmittal.");
	// return;
	// }
	//
	// for (TbitsTreeRequestData trd : requests.values()) {
	// for (TbitsModelData tmd : twc
	// .getAttachmentTableColumnsList()) {
	// Integer fieldId = (Integer) tmd
	// .get(TransmittalConstants.FIELD_ID_COLUMN);
	// if (fieldId > 0) {
	// int dataTypeId = (Integer) tmd.get(DATA_TYPE_ID);
	// boolean isEditable = (Boolean) tmd.get(IS_EDITABLE);
	// String defValue = (String) tmd.get(DEFAULT_VALUE);
	//
	// BAField baField;
	//
	// baField = Utils.getBAFieldById(fieldId, twc
	// .getBaFieldList());
	//
	// if (isEditable && (baField != null)) {
	// String property = baField.getName();
	// if (dataTypeId != TransmittalConstants.ATTACHMENTS) {
	//
	// if (dataTypeId == TransmittalConstants.TYPE) {
	// if ((defValue != null)
	// && (!defValue.trim().equals(""))) {
	// TypeClient typeClient = ((BAFieldCombo) baField)
	// .getModelForName(defValue);
	// if (typeClient != null)
	// trd.set(property, defValue);
	// else
	// Window
	// .alert("No defualt type found for field: "
	// + baField
	// .getDisplayName());
	// }
	// } else {
	// if ((defValue == null)
	// || (defValue.trim().equals("")))
	// defValue = "-";
	// trd.set(property, defValue);
	// }
	// }
	// }
	// }
	// }
	// }
	//
	// ArrayList<TbitsTreeRequestData> gridData = new
	// ArrayList<TbitsTreeRequestData>(
	// requests.values());
	//
	//			
	// if(visits<1)
	// {
	// visits=visits+1;
	// panel = new Page2Panel(wizardContext,params);
	//
	// panel.getSingleGridContainer().removeAllModels();
	// panel.getCommonGridContainer().removeAllModels();
	// panel.getSingleGridContainer().getBulkGrid().getView()
	// .refresh(true);
	// panel.getCommonGridContainer().getBulkGrid().getView()
	// .refresh(true);
	// panel.getSingleGridContainer().addModel(gridData);
	//
	// this.getWidget().setScrollMode(Scroll.NONE);
	// this.getWidget().add(panel, new FitData());
	// }
	// else
	// {
	// panel.refreshgrid(gridData, configs,columnConfiguration2);
	//				
	// }
	// }
	//	
	//		
	// }

	@SuppressWarnings("unchecked")
	public void buildPage(WizardData params) {

		if (Boolean.valueOf((String) params.getData().get("inapprovalcycle"))) {
			ArrayList<TbitsModelData> attachmentSelectionColumns = (ArrayList<TbitsModelData>) getDataObject()
					.getData().get("attachmentTableColumnList");

			Integer currentTrnId = (Integer) getDataObject().getData().get(
					"trnProcessId");

			// if grid is null or new process is there
			if ((prevTrnProcessId != currentTrnId)) {

				prevTrnProcessId = currentTrnId;

				List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

				configs = columnConfiguration.configureColumns();

				if ((configs == null) || configs.isEmpty()) {
					Window
							.alert("Attachment selection table could not configured. Please contact tBits team.");
					return;
				}

				HashMap<Integer, TbitsTreeRequestData> requests = (HashMap<Integer, TbitsTreeRequestData>) getDataObject()
						.getData().get("mapOfRequests");
				ArrayList<BAField> BAFieldList = (ArrayList<BAField>) getDataObject()
						.getData().get("BAFields");
				if (requests == null) {
					TbitsInfo
							.info("No drawings/documents selected for transmittal. Please select drawings/documents for transmittal.");
					Window
							.alert("No drawings/documents selected for transmittal. Please select drawings/documents for transmittal."
									+ "Please close the transmittal window and select drawings/documents for transmittal.");
					return;
				}

				for (TbitsTreeRequestData trd : requests.values()) {
					for (TbitsModelData tmd : attachmentSelectionColumns) {
						Integer fieldId = (Integer) tmd
								.get(TransmittalConstants.FIELD_ID_COLUMN);
						if (fieldId > 0) {
							int dataTypeId = (Integer) tmd.get(DATA_TYPE_ID);
							boolean isEditable = (Boolean) tmd.get(IS_EDITABLE);
							String defValue = (String) tmd.get(DEFAULT_VALUE);

							BAField baField;

							baField = Utils
									.getBAFieldById(fieldId, BAFieldList);

							if (isEditable && (baField != null)) {
								String property = baField.getName();
								if (dataTypeId != TransmittalConstants.ATTACHMENTS) {

									if (dataTypeId == TransmittalConstants.TYPE) {
										if ((defValue != null)
												&& (!defValue.trim().equals(""))) {
											TypeClient typeClient = ((BAFieldCombo) baField)
													.getModelForName(defValue);
											if (typeClient != null)
												trd.set(property, defValue);
											else
												Window
														.alert("No defualt type found for field: "
																+ baField
																		.getDisplayName());
										}
									} else {
										if ((defValue == null)
												|| (defValue.trim().equals("")))
											defValue = "-";
										trd.set(property, defValue);
									}
								}
							}
						}
					}
				}

				String errorString = "";

				ArrayList<TrnEditableColumns> listOfEditableColumns = (ArrayList<TrnEditableColumns>) getDataObject()
						.getData().get("editableAttachmentColumnsList");

				if (listOfEditableColumns != null) {

					
					if (!errorString.equals(""))
						Window.alert(errorString);
				}
				// ----------------------Case in which process selected in
				// approval cycle is different from original
				// one-------------
				else {
					ArrayList<FileClient> newAttachemnts1 = new ArrayList<FileClient>();
					ArrayList<String> names = new ArrayList<String>();
					for (TbitsModelData tmd : attachmentSelectionColumns) {
						Integer fieldId = (Integer) tmd
								.get(TransmittalConstants.FIELD_ID_COLUMN);
						if (fieldId > 0) {
							int dataTypeId = (Integer) tmd.get(DATA_TYPE_ID);
							if (dataTypeId == TransmittalConstants.ATTACHMENTS) {
								names.add((Utils.getBAFieldById(fieldId,
										(ArrayList<BAField>) getDataObject()
												.getData().get("BAFields")))
										.getName());
							}
						}
					}

					for (TbitsTreeRequestData trd : requests.values()) {
						for (String prop : names) {
							newAttachemnts1 = new ArrayList<FileClient>();
							POJO obj = trd.getAsPOJO(prop);
							if (obj == null || !(obj instanceof POJOAttachment)) {
								obj = new POJOAttachment(
										new ArrayList<FileClient>());
							}
							List<FileClient> delAttachments = (List<FileClient>) obj
									.getValue();
							if (delAttachments.size() > 0) {
								for (FileClient eachAttachment : delAttachments) {

									eachAttachment.set(
											"IS_CHECKED_IN_TRANSIENT_DATA",
											String.valueOf(true));

								}
							}
							newAttachemnts1.addAll(delAttachments);
							trd.set(prop, newAttachemnts1);

						}
					}

				}

				// -------------------Updating the request using drawing
				// list Json------------//

				if (params.getData().get("editableColumnsList") != null) {

					ArrayList<TrnEditableColumns> EditableColumnList = (ArrayList<TrnEditableColumns>) getDataObject()
							.getData().get("editableColumnsList");

					HashMap<String, ArrayList<DrawinglistModel>> hm_drawing = (HashMap<String, ArrayList<DrawinglistModel>>) getDataObject()
							.getData().get("ConvertedDrawingList");

					for (Object reqId : hm_drawing.keySet()) {
						String reqID = (String) reqId;
						int reqid = Integer.parseInt(reqID);
						ArrayList<DrawinglistModel> arr = hm_drawing.get(reqId);

						for (DrawinglistModel eachfield : arr) {
							String value = eachfield.getFIELD_VALUE();
							int field_id = Integer.parseInt((String) eachfield
									.getFIELD_ID());

							for (TrnEditableColumns eachColumn : EditableColumnList) {
								if (eachColumn.getFIELDID() == field_id) {
									String property = eachColumn.getPROPERTY();
									requests.get(reqid).set(property, value);
								}
							}

						}

					}

					
				}
				ArrayList<TbitsTreeRequestData> gridData = new ArrayList<TbitsTreeRequestData>(
						requests.values());

				if (!flagForAttachmentGrid) {

					flagForAttachmentGrid = true;
					panel = new Page2Panel(wizardContext, params);

					panel.getSingleGridContainer().removeAllModels();
					panel.getCommonGridContainer().removeAllModels();
					panel.getSingleGridContainer().getBulkGrid().getView()
							.refresh(true);
					panel.getCommonGridContainer().getBulkGrid().getView()
							.refresh(true);
					panel.getSingleGridContainer().addModel(gridData);
					panel.setColumnConfig(columnConfiguration);
					this.getWidget().setScrollMode(Scroll.NONE);
					this.getWidget().add(panel, new FitData());
				} else {
					panel.refreshgrid(gridData, configs, columnConfiguration);

				}
			}
		} else {

			ArrayList<TbitsModelData> attachmentSelectionColumns = (ArrayList<TbitsModelData>) getDataObject()
					.getData().get("attachmentTableColumnList");

			Integer currentTrnId = (Integer) getDataObject().getData().get(
					"trnProcessId");

			// if grid is null or new process is there
			if ((prevTrnProcessId != currentTrnId)) {

				prevTrnProcessId = currentTrnId;

				List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

				configs = columnConfiguration.configureColumns();

				if ((configs == null) || configs.isEmpty()) {
					Window
							.alert("Attachment selection table could not configured. Please contact tBits team.");
					return;
				}

				HashMap<Integer, TbitsTreeRequestData> requests = (HashMap<Integer, TbitsTreeRequestData>) getDataObject()
						.getData().get("mapOfRequests");
				ArrayList<BAField> BAFieldList = (ArrayList<BAField>) getDataObject()
						.getData().get("BAFields");
				if (requests == null) {
					TbitsInfo
							.info("No drawings/documents selected for transmittal. Please select drawings/documents for transmittal.");
					Window
							.alert("No drawings/documents selected for transmittal. Please select drawings/documents for transmittal."
									+ "Please close the transmittal window and select drawings/documents for transmittal.");
					return;
				}

				for (TbitsTreeRequestData trd : requests.values()) {
					for (TbitsModelData tmd : attachmentSelectionColumns) {
						Integer fieldId = (Integer) tmd
								.get(TransmittalConstants.FIELD_ID_COLUMN);
						if (fieldId > 0) {
							int dataTypeId = (Integer) tmd.get(DATA_TYPE_ID);
							boolean isEditable = (Boolean) tmd.get(IS_EDITABLE);
							String defValue = (String) tmd.get(DEFAULT_VALUE);

							BAField baField;

							baField = Utils
									.getBAFieldById(fieldId, BAFieldList);

							if (isEditable && (baField != null)) {
								String property = baField.getName();
								if (dataTypeId != TransmittalConstants.ATTACHMENTS) {

									if (dataTypeId == TransmittalConstants.TYPE) {
										if ((defValue != null)
												&& (!defValue.trim().equals(""))) {
											TypeClient typeClient = ((BAFieldCombo) baField)
													.getModelForName(defValue);
											if (typeClient != null)
												trd.set(property, defValue);
											else
												Window
														.alert("No defualt type found for field: "
																+ baField
																		.getDisplayName());
										}
									} else {
										if ((defValue == null)
												|| (defValue.trim().equals("")))
											defValue = "-";
										trd.set(property, defValue);
									}
								}
							}
						}
					}
				}

				ArrayList<TbitsTreeRequestData> gridData = new ArrayList<TbitsTreeRequestData>(
						requests.values());

				if (!flagForAttachmentGrid) {

					flagForAttachmentGrid = true;
					panel = new Page2Panel(wizardContext, params);

					panel.getSingleGridContainer().removeAllModels();
					panel.getCommonGridContainer().removeAllModels();
					panel.getSingleGridContainer().getBulkGrid().getView()
							.refresh(true);
					panel.getCommonGridContainer().getBulkGrid().getView()
							.refresh(true);
					panel.getSingleGridContainer().addModel(gridData);
					panel.setColumnConfig(columnConfiguration);
					this.getWidget().setScrollMode(Scroll.NONE);
					this.getWidget().add(panel, new FitData());

				} else {
					panel.refreshgrid(gridData, configs, columnConfiguration);

				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	public boolean funcToBeCalledOnBack() {
		HashSet<String> hs = new HashSet<String>();

		HashMap<Integer, HashMap<String, TransmittalAttachmentContainer>> requestAttachmentContainerMap = panel
				.getSingleGridColumnConfig().getRequestAttachmentContainerMap();
		HashMap<Integer, TbitsTreeRequestData> requestMap = (HashMap<Integer, TbitsTreeRequestData>) getDataObject()
				.getData().get("mapOfRequests");

		for (Integer reqId : requestMap.keySet()) {
			TbitsTreeRequestData trd = requestMap.get(reqId);
			if (trd != null) {
				HashMap<String, TransmittalAttachmentContainer> tacMap = requestAttachmentContainerMap
						.get(reqId);

				if (tacMap != null) {
					for (String fieldName : tacMap.keySet()) {
						if (fieldName != null) {
							hs.add(fieldName);
							TransmittalAttachmentContainer tac = tacMap
									.get(fieldName);
							List<FileClient> aicList = new ArrayList<FileClient>();
							for (CheckBox cb : tac.checkBoxList) {
								if (Boolean.parseBoolean(cb.getRawValue())) {
									FileClient aic = cb
											.getData(TransmittalConstants.PROPERTY_ATTACHMENT_INFO);
									if (aic != null)
										aicList.add(aic);
								}
							}
							trd.set(fieldName, new POJOAttachment(aicList));
						}
					}
				}
			}
		}
		String attNames = "";
		for (String s : hs)
			attNames = (attNames.equals("")) ? s : attNames + "," + s;
		deliverableAttachmentProperties = attNames;
		setValues();
		return true;
	}

	public boolean canMoveToNext() {
		// TODO Auto-generated method stub
		if (columnConfiguration.flag)

			return true;
		else
			return false;
	}

}
