package transmittal.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import transmittal.com.tbitsGlobal.client.models.AttachmentModel;
import transmittal.com.tbitsGlobal.client.models.Attachmentinfo;

import com.axeiya.gwtckeditor.client.CKConfig;
import com.axeiya.gwtckeditor.client.CKEditor;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.cache.UserCache;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldAttachment;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCheckBox;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldMultiValue;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldString;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;
import commons.com.tbitsGlobal.utils.client.widgets.DateTimeControl;
import commons.com.tbitsGlobal.utils.client.widgets.TypeFieldControl;
import commons.com.tbitsGlobal.utils.client.widgets.UserPicker;

/**
 * 
 * @author rohit
 * 
 */
public class ProcessParametersWizardPage extends
		TransmittalAbstractWizardPage<FormPanel, HashMap<String, String>> {

	private WizardData dataObject;

	TransmittalWizardConstants twc;
	private static final String NOTIFY = "notify";
	private static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	private static final String ACCESS_TO = "AccessTo";
	private static final String FIELD_ID = "field_id";
	private static final String DISPLAY_NAME = "display_name";
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
	ComboBox<TbitsModelData> transmittalTypeCombo;
	ComboBox<TbitsModelData> fieldForTrnKey;
	private UserPicker toList; // , from;
	ComboBox<UserClient> from;
	Label validationResult;
	private UserPicker ccList;
	private UserPicker accessTo;
	private DateTimeControl actualDate;
	private TextField<String> actualNumber;
	private TextArea remarks;
	private TextField<String> Subject;
	private CKEditor emailBody;
	private TextField<String> draftedBy;
	private Integer currentTransmittalProcessId = -1;
	private HashMap<String, Integer> stickinessMap = new HashMap<String, Integer>();
	private ArrayList<TbitsModelData> validationRulesList = new ArrayList<TbitsModelData>();
	boolean isCanContinue = true;
	String originalTrnPrefix = "";
	TbitsModelData default_data;
	TbitsModelData data_for_db;
	boolean flagForApprovalButton = false;
	boolean flagForCreateTransmittalButton = false;
	// Is common for a user logged for any transmittal process since role
	// is based on a business area, hence no need to reset this value.
	boolean isExistsInHistoryRole = false;
	private CheckBox notify;
	HashMap<Integer, TbitsTreeRequestData> requestData;
	private String listOfCC = null;
	private static final String TRN_EXTENDED_FIELDS = "trnExtendedFields";

	private static final long serialVersionUID = 1L;

	protected static final String IS_ACTIVE_COLUMN = "is_active";

	protected static final String IS_EDITABLE_COLUMN = "is_editable";

	protected static final String DEFAULT_VALUE_COLUMN = "default_value";

	protected static final String DATA_TYPE_ID_COLUMN = "data_type_id";

	protected static final String FIELD_ID_COLUMN = "field_id";

	protected static final String NAME_COLUMN = "name";

	protected static final String TRN_PROCESS_ID_COLUMN = "trn_process_id";

	protected static final String COLUMN_ORDER = "column_order";

	protected static final String DISPLAY_NAME_COLUMN = "display_name";

	protected static final String FIELD_ORDER = "field_order";

	protected static final String IS_DTN_NUMBER_PART = "is_dtn_number_part";

	public static final String KEYFIELD = "keyfield";

	public static final String KEYFIELD_LIST = "keyfieldList";

	int visits;
	String listOfPrevCC = null;

	String dropDownKey = "";

	HashMap<Integer, BAField> map = new HashMap<Integer, BAField>();

	public ProcessParametersWizardPage(UIContext wizardContext, WizardData data) {
		super(wizardContext);
		this.setDataObject(data);
		Collection<BAField> fields = TransmittalConstants.fieldCache
				.getValues();

		for (BAField f : fields) {
			map.put(f.getFieldId(), f);
		}
		buildPage(data);
		visits = 0;

	}

	public WizardData getDataObject() {
		return dataObject;
	}

	public void setDataObject(WizardData data) {
		this.dataObject = data;
	}

	public int getDisplayOrder() {
		return 0;
	}

	@SuppressWarnings("unchecked")
	public void setValues() {

		if (!Boolean.valueOf((String) getDataObject().getData().get(
				"inapprovalcycle"))) {
			String selectedCCList = (ccList.getStringValue() == null) ? ""
					: ccList.getStringValue().trim();

			String listOfActiveCC = "";
			HashSet<String> userSet = new HashSet<String>();
			String nonActiveCC = "";
			String msg = "These are the users that are no longer active :"
					+ "\n";

			UserCache userCache = CacheRepository.getInstance().getCache(
					UserCache.class);

			for (String userLogin : selectedCCList.trim().split(",")) {
				if (!userLogin.trim().equals("")) {
					userLogin = userLogin.trim();

					UserClient uc = userCache.getObject(userLogin);

					if (uc != null)
						userSet.add(userLogin);
					else {
						if (nonActiveCC.equals("")) {
							nonActiveCC = nonActiveCC + userLogin;
						} else {
							nonActiveCC = nonActiveCC + "," + userLogin;
						}
					}
				}
			}
			if (!nonActiveCC.equals("")) {
				Window.alert(msg + nonActiveCC);
				Log.info(msg + nonActiveCC);
			}
			if (userSet.size() > 0) {
				for (String userLogin : userSet) {
					if (listOfActiveCC.equals("")) {
						listOfActiveCC = listOfActiveCC + userLogin;
					} else {
						listOfActiveCC = listOfActiveCC + "," + userLogin;
					}
				}
			}

			getDataObject().getData().put(ccList.getName(), listOfActiveCC);

			if (DistributionDataSelectionPage.visits > 0) {

				ArrayList<TbitsModelData> distributionListModelData = new ArrayList<TbitsModelData>();
				ArrayList<String> curCC = new ArrayList<String>();
				String listOfnewlyAddedCC = "";
				ArrayList<String> deletedCc = new ArrayList<String>();
				String listOfDeletedCC = "";
				ArrayList<TbitsModelData> distributionDataColumns = (ArrayList<TbitsModelData>) getDataObject()
						.getData().get("distributionTableColumnsList");

				ArrayList<String> propList = new ArrayList<String>();

				for (TbitsModelData md : distributionDataColumns) {
					propList.add((String) md.get("name"));
				}

				for (String totalCC : userSet) {
					boolean ismatched = false;

					for (String prevcc : listOfCC.split(",")) {
						if (totalCC.equals(prevcc)) {
							ismatched = true;
							break;
						}

					}
					if (!ismatched) {
						curCC.add(totalCC);
						listOfnewlyAddedCC = listOfnewlyAddedCC + totalCC + ",";
					}

				}

				for (String prevcc : listOfCC.split(",")) {
					boolean ismatched = false;

					for (String curcc : userSet) {
						if (prevcc.equals(curcc)) {
							ismatched = true;
							break;
						}

					}

					if (!ismatched) {
						deletedCc.add(prevcc);
						listOfDeletedCC = listOfDeletedCC + prevcc + ",";
					}

				}

				ArrayList<String[]> ConvertedDistributionList = (ArrayList<String[]>) getDataObject()
						.getData().get("NewConvertedDistributionList");
				TbitsModelData tempData = new TbitsModelData();

				for (String[] eachData : ConvertedDistributionList) {
					Boolean ignore = false;

					tempData = new TbitsModelData();

					for (String eachProp : eachData) {
						String[] data;
						String prop;
						String value;
						if (!eachProp.equals("-")) {
							data = eachProp.split(",");

							prop = data[0];
							value = data[1];
							if (prop.equals("login")) {
								if (deletedCc.contains(value)) {
									ignore = true;
								}
								tempData.set("login", value);
							}

							if (propList.contains(prop)) {

								tempData.set(prop, value);

							}
						}
					}
					if (!ignore)
						distributionListModelData.add(tempData);
				}

				for (String userLogin1 : curCC) {
					if (!userLogin1.trim().equals("")) {
						UserClient uc = userCache.getObject(userLogin1);// getUserBasedOnLogin(userMap,
						// userLogin);
						if (uc != null) {
							TbitsModelData tempData2 = new TbitsModelData();
							tempData2.set("login", uc.getUserLogin());

							for (TbitsModelData distColumnTmd : distributionDataColumns) {
								String property = (String) distColumnTmd
										.get("name");
								if (property != null) {
									// Check if its a user property if
									// not,
									// fetch
									// default values from db.
									String value = (String) uc.get(property);
									if ((value != null)
											&& (!value.trim().equals("")))
										tempData2.set(property, value);
									else {
										String tempValue = distColumnTmd
												.get(TransmittalConstants.FIELD_CONFIG);
										if ((Integer) distColumnTmd
												.get("data_type_id") != 9) {
											if ((tempValue != null)
													&& (!tempValue.trim()
															.equals("")))
												tempData2.set(property,
														tempValue);
											else
												tempData2.set(property, "-");
										} else {
											HashMap<String, String> typesMap = new HashMap<String, String>();
											DistributionTableColumnsConfig
													.fetchKeyValuePairsfromJsonString(
															typesMap, tempValue);
											tempValue = typesMap.keySet()
													.iterator().next();
											if (tempValue != null)
												tempData2.set(property,
														tempValue);
											else
												tempData2.set(property, "-");
										}
									}
								}
							}
							distributionListModelData.add(tempData2);
						}
					}
				}

				JSONArray arrayOfPrevcc = getDistributionListJsonString(
						distributionListModelData, propList);

				JSONArray newJsonArray = new JSONArray();

				int startingIndexForTotalCCInSecondCycle = 0;

				while ((arrayOfPrevcc.get(startingIndexForTotalCCInSecondCycle) != null)) {
					newJsonArray
							.set(
									startingIndexForTotalCCInSecondCycle,
									arrayOfPrevcc
											.get(startingIndexForTotalCCInSecondCycle++));
				}

				this.getDataObject().getData().put(
						TransmittalConstants.DISTRIBUTION_TABLE,
						newJsonArray.toString());

			}
			/*
			 * Action when accessing the page after the first time
			 */
			else {

				this.getDataObject().getData().put(
						TransmittalConstants.DISTRIBUTION_TABLE,
						getDataForPage3().toString());
			}

			if (ccList.getStringValue() == null) {
				listOfCC = "";
			} else {
				listOfCC = listOfActiveCC;
			}

			getDataObject().getData().put("ccList", listOfActiveCC);
			getDataObject().getData().put(
					toList.getName(),
					(toList.getStringValue() == null) ? "" : toList
							.getStringValue().trim());
			getDataObject().getData().put(
					accessTo.getName(),
					(accessTo.getStringValue() == null) ? "" : accessTo
							.getStringValue().trim());

			getDataObject().getData().put(notify.getName(),
					String.valueOf(notify.getValue()));

			getDataObject().getData()
					.put(remarks.getName(), remarks.getValue());
			getDataObject().getData().put(EMAIL_BODY, emailBody.getHTML());
			getDataObject().getData()
					.put(Subject.getName(), Subject.getValue());
			getDataObject().getData().put(draftedBy.getName(),
					draftedBy.getValue());

			String value = "";
			if (from.getValue() != null)
				value = from.getValue().getUserLogin();
			getDataObject().getData().put(DTN_SIGNATORY, value);

			if (isExistsInHistoryRole) {
				if (actualDate.getValue() != null) {
					String dateString = DateTimeFormat.getFormat(
							YYYY_MM_DD_HH_MM_SS).format(actualDate.getValue());
					if (dateString != null && !dateString.trim().equals("")) {
						getDataObject().getData().put(actualDate.getName(),
								dateString);
					}
				}
				if ((actualNumber.getValue() != null)
						&& (!actualNumber.getValue().trim().equals("")))
					getDataObject().getData().put(actualNumber.getName(),
							actualNumber.getValue());
			}

			// Replace the user input part.
			String trnPrefix = originalTrnPrefix;

			// Add all trn_wizard fields values.
			List<Field<?>> fieldsList = widget.getFields();
			ArrayList<TbitsModelData> extendedFields = (ArrayList<TbitsModelData>) getDataObject()
					.getData().get(TRN_EXTENDED_FIELDS);
			if (extendedFields != null) {
				for (TbitsModelData field : extendedFields) {
					String property = field.get("name");
					String trnFieldVal = fillValueForField(field, fieldsList);
					getDataObject().getData().put(property, trnFieldVal);
				}
			}
			if (trnPrefix.contains(TransmittalConstants.USER_INPUT)) {

				String isPartFieldName = getExtendedFieldNameToBePartOfDtnNumber(getDataObject()
						.getData());

				String partValue = (String) getDataObject().getData().get(
						isPartFieldName);
				if (partValue != null) {
					trnPrefix = trnPrefix.replace(
							TransmittalConstants.USER_INPUT, partValue);

					getDataObject().getData().put(
							TransmittalConstants.TRANSMITTAL_ID_PREFIX,
							trnPrefix);
				}
			}

			// Replace the request field part
			String requestFieldPart = getRequestFieldValueToBePartOfDtnNumber(trnPrefix);

			if (requestFieldPart == null) {

				TbitsModelData tmd = (TbitsModelData) getDataObject().getData()
						.get("transmittalProcessParams");
				// trnPrefix = trnPrefix.concat("" + keyDuetoDropdow);
				tmd.set(TransmittalConstants.TRANSMITTAL_ID_PREFIX, trnPrefix);
				getDataObject().getData().put(
						TransmittalConstants.TRANSMITTAL_ID_PREFIX, trnPrefix);

			} else {

				TbitsModelData tmd = (TbitsModelData) getDataObject().getData()
						.get("transmittalProcessParams");

				tmd.set(TransmittalConstants.TRANSMITTAL_ID_PREFIX, (trnPrefix
						.replaceFirst(TransmittalConstants.FIELD_NAME_REGEX,
								requestFieldPart)));
				getDataObject().getData().put(
						TransmittalConstants.TRANSMITTAL_ID_PREFIX,
						(trnPrefix.replaceFirst(
								TransmittalConstants.FIELD_NAME_REGEX,
								requestFieldPart)));

			}

			getDataObject().getData().put("trnProcessId",
					currentTransmittalProcessId);
		}
		/*
		 * start of approval cycle
		 */
		else {
			String selectedCCList = (ccList.getStringValue() == null) ? ""
					: ccList.getStringValue().trim();
			String listOfActiveCC = "";
			ArrayList<String> userSet = new ArrayList<String>();
			String nonActiveCC = "";
			String msg = "These are the users that are no longer active :"
					+ "\n";

			UserCache userCache = CacheRepository.getInstance().getCache(
					UserCache.class);
			for (String userLogin : selectedCCList.trim().split(",")) {
				if (!userLogin.trim().equals("")) {
					userLogin = userLogin.trim();
					UserClient uc = userCache.getObject(userLogin);

					if (uc != null)
						userSet.add(userLogin);
					else {
						if (nonActiveCC.equals("")) {
							nonActiveCC = nonActiveCC + userLogin;
						} else {
							nonActiveCC = nonActiveCC + "," + userLogin;
						}
					}
				}
			}
			if (!nonActiveCC.equals("")) {
				Window.alert(msg + nonActiveCC);
				Log.info(msg + nonActiveCC);
			}
			if (userSet.size() > 0) {
				for (String userLogin : userSet) {
					if (listOfActiveCC.equals("")) {
						listOfActiveCC = listOfActiveCC + userLogin;
					} else {
						listOfActiveCC = listOfActiveCC + "," + userLogin;
					}
				}
			}
			getDataObject().getData().put(ccList.getName(), listOfActiveCC);

			ArrayList<String> propList = new ArrayList<String>();

			ArrayList<TbitsModelData> distributionDataColumns = (ArrayList<TbitsModelData>) getDataObject()
					.getData().get("distributionTableColumnsList");

			/*
			 * Comparator<TbitsModelData> comp = new
			 * Comparator<TbitsModelData>() { public int compare(TbitsModelData
			 * o1, TbitsModelData o2) {
			 * 
			 * if ((o1 != null) && (o2 != null)) { int s1 = (Integer) o1
			 * .get(TransmittalConstants.COLUMN_ORDER); int s2 = (Integer) o2
			 * .get(TransmittalConstants.COLUMN_ORDER); if (s1 > s2) return 1;
			 * else if (s1 == s2) return 0; else if (s1 < s2) return -1; }
			 * return 0; } }; // Sort the column info, before creating column
			 * configs out of them. // So, that they maintain the sort order and
			 * // hence the column order in the table.
			 * 
			 * Collections.sort(distributionDataColumns, comp);
			 */

			for (TbitsModelData md : distributionDataColumns) {
				propList.add((String) md.get("name"));
			}

			ArrayList<String[]> ConvertedDistributionList = (ArrayList<String[]>) getDataObject()
					.getData().get("ConvertedDistributionList");

			if (Boolean.valueOf((String) getDataObject().getData().get(
					"Wizard_Version"))
					&& (ConvertedDistributionList != null)
					&& ConvertedDistributionList.size() > 0)

			{
				if (DistributionDataSelectionPage.visits == 0) {

					ArrayList<String> curCC = new ArrayList<String>();
					String listOfnewlyAddedCC = "";
					ArrayList<String> deletedCc = new ArrayList<String>();
					String listOfDeletedCC = "";

					HashMap<String, String> hm = (HashMap<String, String>) getDataObject()
							.getData().get("HashMapForTransientData");
					String CarryOverCCs = hm.get("ccList");

					getDataObject().getData().put("CarryOverCC", CarryOverCCs);

					for (String totalCC : userSet) {
						boolean ismatched = false;

						for (String prevcc : CarryOverCCs.split(",")) {
							if (totalCC.equals(prevcc)) {
								ismatched = true;
								break;
							}

						}
						if (!ismatched) {
							curCC.add(totalCC);
							listOfnewlyAddedCC = listOfnewlyAddedCC + totalCC
									+ ",";
						}

					}

					for (String prevcc : CarryOverCCs.split(",")) {
						boolean ismatched = false;

						for (String curcc : userSet) {
							if (prevcc.equals(curcc)) {
								ismatched = true;
								break;
							}

						}

						if (!ismatched) {
							deletedCc.add(prevcc);
							listOfDeletedCC = listOfDeletedCC + prevcc + ",";
						}

					}

					TbitsModelData tempData = new TbitsModelData();

					ArrayList<TbitsModelData> distributionListModelData = new ArrayList<TbitsModelData>();

					for (String userLogin1 : curCC) {
						if (!userLogin1.trim().equals("")) {
							UserClient uc = userCache.getObject(userLogin1);// getUserBasedOnLogin(userMap,
							// userLogin);
							if (uc != null) {
								TbitsModelData tempData2 = new TbitsModelData();
								tempData2.set("login", uc.getUserLogin());

								for (TbitsModelData distColumnTmd : distributionDataColumns) {
									String property = (String) distColumnTmd
											.get("name");
									if (property != null) {
										// Check if its a user property if
										// not,
										// fetch
										// default values from db.
										String value = (String) uc
												.get(property);
										if ((value != null)
												&& (!value.trim().equals("")))
											tempData2.set(property, value);
										else {
											String tempValue = distColumnTmd
													.get(TransmittalConstants.FIELD_CONFIG);
											if ((Integer) distColumnTmd
													.get("data_type_id") != 9) {
												if ((tempValue != null)
														&& (!tempValue.trim()
																.equals("")))
													tempData2.set(property,
															tempValue);
												else
													tempData2
															.set(property, "-");
											} else {
												HashMap<String, String> typesMap = new HashMap<String, String>();
												DistributionTableColumnsConfig
														.fetchKeyValuePairsfromJsonString(
																typesMap,
																tempValue);
												tempValue = typesMap.keySet()
														.iterator().next();
												if (tempValue != null)
													tempData2.set(property,
															tempValue);
												else
													tempData2
															.set(property, "-");
											}
										}
									}
								}
								distributionListModelData.add(tempData2);
							}
						}
					}

					ConvertedDistributionList = (ArrayList<String[]>) getDataObject()
							.getData().get("ConvertedDistributionList");

					for (String[] eachData : ConvertedDistributionList) {
						Boolean ignore = false;

						tempData = new TbitsModelData();

						for (String eachProp : eachData) {
							String[] data;
							String prop;
							String value;
							if (!eachProp.equals("-")) {
								data = eachProp.split(",");

								prop = data[0];
								value = data[1];
								if (prop.equals("login")) {
									if (deletedCc.contains(value)) {
										ignore = true;
									}
								}

								if (propList.contains(prop)) {

									tempData.set(prop, value);

								}
							}
						}
						if (!ignore)
							distributionListModelData.add(tempData);
					}

					JSONArray arrayOfPrevcc = getDistributionListJsonString(
							distributionListModelData, propList);

					JSONArray newJsonArray = new JSONArray();

					int startingIndexForTotalCCInSecondCycle = 0;

					while ((arrayOfPrevcc
							.get(startingIndexForTotalCCInSecondCycle) != null)) {
						newJsonArray
								.set(
										startingIndexForTotalCCInSecondCycle,
										arrayOfPrevcc
												.get(startingIndexForTotalCCInSecondCycle++));
					}

					this.getDataObject().getData().put(
							TransmittalConstants.DISTRIBUTION_TABLE,
							newJsonArray.toString());

				}
				/*
				 * on more than one visit
				 */
				else {

					ArrayList<String> curCC = new ArrayList<String>();
					String listOfnewlyAddedCC = "";
					ArrayList<String> deletedCc = new ArrayList<String>();
					String listOfDeletedCC = "";
					String CarryOverCCs = listOfCC;

					for (String totalCC : userSet) {
						boolean ismatched = false;

						for (String prevcc : CarryOverCCs.split(",")) {
							if (totalCC.equals(prevcc)) {
								ismatched = true;
								break;
							}

						}
						if (!ismatched) {
							curCC.add(totalCC);
							listOfnewlyAddedCC = listOfnewlyAddedCC + totalCC
									+ ",";
						}

					}

					for (String prevcc : CarryOverCCs.split(",")) {
						boolean ismatched = false;

						for (String curcc : userSet) {
							if (prevcc.equals(curcc)) {
								ismatched = true;
								break;
							}

						}

						if (!ismatched) {
							deletedCc.add(prevcc);
							listOfDeletedCC = listOfDeletedCC + prevcc + ",";
						}

					}

					TbitsModelData tempData = new TbitsModelData();

					ArrayList<TbitsModelData> distributionListModelData = new ArrayList<TbitsModelData>();

					for (String userLogin1 : curCC) {
						if (!userLogin1.trim().equals("")) {
							UserClient uc = userCache.getObject(userLogin1);// getUserBasedOnLogin(userMap,
							// userLogin);
							if (uc != null) {
								TbitsModelData tempData2 = new TbitsModelData();
								tempData2.set("login", uc.getUserLogin());

								for (TbitsModelData distColumnTmd : distributionDataColumns) {
									String property = (String) distColumnTmd
											.get("name");
									if (property != null) {
										// Check if its a user property if
										// not,
										// fetch
										// default values from db.
										String value = (String) uc
												.get(property);
										if ((value != null)
												&& (!value.trim().equals("")))
											tempData2.set(property, value);
										else {
											String tempValue = distColumnTmd
													.get(TransmittalConstants.FIELD_CONFIG);
											if ((Integer) distColumnTmd
													.get("data_type_id") != 9) {
												if ((tempValue != null)
														&& (!tempValue.trim()
																.equals("")))
													tempData2.set(property,
															tempValue);
												else
													tempData2
															.set(property, "-");
											} else {
												HashMap<String, String> typesMap = new HashMap<String, String>();
												DistributionTableColumnsConfig
														.fetchKeyValuePairsfromJsonString(
																typesMap,
																tempValue);
												tempValue = typesMap.keySet()
														.iterator().next();
												if (tempValue != null)
													tempData2.set(property,
															tempValue);
												else
													tempData2
															.set(property, "-");
											}
										}
									}
								}
								distributionListModelData.add(tempData2);
							}
						}
					}

					ArrayList<String[]> ConvertedDistributionList1 = (ArrayList<String[]>) getDataObject()
							.getData().get("NewConvertedDistributionList");

					for (String[] eachData : ConvertedDistributionList1) {
						Boolean ignore = false;

						tempData = new TbitsModelData();

						for (String eachProp : eachData) {
							String[] data;
							String prop;
							String value;
							if (!eachProp.equals("-")) {
								data = eachProp.split(",");

								prop = data[0];
								value = data[1];
								if (prop.equals("login")) {
									if (deletedCc.contains(value)) {
										ignore = true;
									}
								}

								if (propList.contains(prop)) {

									tempData.set(prop, value);

								}
							}
						}
						if (!ignore)
							distributionListModelData.add(tempData);
					}

					JSONArray arrayOfPrevcc = getDistributionListJsonString(
							distributionListModelData, propList);

					JSONArray newJsonArray = new JSONArray();

					int startingIndexForTotalCCInSecondCycle = 0;

					while ((arrayOfPrevcc
							.get(startingIndexForTotalCCInSecondCycle) != null)) {
						newJsonArray
								.set(
										startingIndexForTotalCCInSecondCycle,
										arrayOfPrevcc
												.get(startingIndexForTotalCCInSecondCycle++));
					}

					this.getDataObject().getData().put(
							TransmittalConstants.DISTRIBUTION_TABLE,
							newJsonArray.toString());

				}
				/*
				 * end of code
				 */
			}
			/*
			 * if version is old or process is different from thee original one
			 */

			else {
				this.getDataObject().getData().put(
						TransmittalConstants.DISTRIBUTION_TABLE,
						getDataForPage3().toString());
			}

			if (ccList.getStringValue() == null) {
				listOfCC = "";
			} else {
				listOfCC = listOfActiveCC;
			}

			getDataObject().getData().put("trnProcessId",
					currentTransmittalProcessId);

			getDataObject().getData().put("ccList", listOfActiveCC);
			getDataObject().getData().put(
					toList.getName(),
					(toList.getStringValue() == null) ? "" : toList
							.getStringValue().trim());
			getDataObject().getData().put(
					accessTo.getName(),
					(accessTo.getStringValue() == null) ? "" : accessTo
							.getStringValue().trim());

			getDataObject().getData().put(notify.getName(),
					String.valueOf(notify.getValue()));

			getDataObject().getData()
					.put(remarks.getName(), remarks.getValue());
			getDataObject().getData().put(EMAIL_BODY, emailBody.getHTML());
			getDataObject().getData()
					.put(Subject.getName(), Subject.getValue());
			getDataObject().getData().put(draftedBy.getName(),
					draftedBy.getValue());

			String value = "";
			if (from.getValue() != null)
				value = from.getValue().getUserLogin();
			getDataObject().getData().put(DTN_SIGNATORY, value);

			if (isExistsInHistoryRole) {
				if (actualDate.getValue() != null) {
					String dateString = DateTimeFormat.getFormat(
							YYYY_MM_DD_HH_MM_SS).format(actualDate.getValue());
					if (dateString != null && !dateString.trim().equals("")) {
						getDataObject().getData().put(actualDate.getName(),
								dateString);
					}
				}
				if ((actualNumber.getValue() != null)
						&& (!actualNumber.getValue().trim().equals("")))
					getDataObject().getData().put(actualNumber.getName(),
							actualNumber.getValue());
			}

			// Replace the user input part.
			String trnPrefix = originalTrnPrefix;

			// Add all trn_wizard fields values.
			List<Field<?>> fieldsList = widget.getFields();
			ArrayList<TbitsModelData> extendedFields = (ArrayList<TbitsModelData>) getDataObject()
					.getData().get(TRN_EXTENDED_FIELDS);
			if (extendedFields != null) {
				for (TbitsModelData field : extendedFields) {
					String property = field.get("name");
					String trnFieldVal = fillValueForField(field, fieldsList);
					getDataObject().getData().put(property, trnFieldVal);
				}
			}
			if (trnPrefix.contains(TransmittalConstants.USER_INPUT)) {

				String isPartFieldName = getExtendedFieldNameToBePartOfDtnNumber(getDataObject()
						.getData());

				String partValue = (String) getDataObject().getData().get(
						isPartFieldName);
				if (partValue != null) {
					trnPrefix = trnPrefix.replace(
							TransmittalConstants.USER_INPUT, partValue);

					getDataObject().getData().put(
							TransmittalConstants.TRANSMITTAL_ID_PREFIX,
							trnPrefix);
				}
			}

			// Replace the request field part
			String requestFieldPart = getRequestFieldValueToBePartOfDtnNumber(trnPrefix);

			if (requestFieldPart == null) {

				TbitsModelData tmd = (TbitsModelData) getDataObject().getData()
						.get("transmittalProcessParams");
				tmd.set(TransmittalConstants.TRANSMITTAL_ID_PREFIX, trnPrefix);
				getDataObject().getData().put(
						TransmittalConstants.TRANSMITTAL_ID_PREFIX, trnPrefix);

			} else {

				TbitsModelData tmd = (TbitsModelData) getDataObject().getData()
						.get("transmittalProcessParams");
				tmd.set(TransmittalConstants.TRANSMITTAL_ID_PREFIX, trnPrefix
						.replaceFirst(TransmittalConstants.FIELD_NAME_REGEX,
								requestFieldPart));
				getDataObject().getData().put(
						TransmittalConstants.TRANSMITTAL_ID_PREFIX,
						trnPrefix.replaceFirst(
								TransmittalConstants.FIELD_NAME_REGEX,
								requestFieldPart));

			}

		}

	}

	@SuppressWarnings("unchecked")
	public void setValues1() {

		String selectedCCList = (ccList.getStringValue() == null) ? "" : ccList
				.getStringValue().trim();
		String listOfActiveCC = "";
		ArrayList<String> userSet = new ArrayList<String>();
		String nonActiveCC = "";
		String msg = "These are the users that are no longer active :" + "\n";

		UserCache userCache = CacheRepository.getInstance().getCache(
				UserCache.class);
		for (String userLogin : selectedCCList.trim().split(",")) {
			if (!userLogin.trim().equals("")) {

				UserClient uc = userCache.getObject(userLogin);

				if (uc != null)
					userSet.add(userLogin);
				else {
					if (nonActiveCC.equals("")) {
						nonActiveCC = nonActiveCC + userLogin;
					} else {
						nonActiveCC = nonActiveCC + "," + userLogin;
					}
				}
			}
		}
		if (!nonActiveCC.equals("")) {
			Window.alert(msg + nonActiveCC);
			Log.info(msg + nonActiveCC);
		}
		if (userSet.size() > 0) {
			for (String userLogin : userSet) {
				if (listOfActiveCC.equals("")) {
					listOfActiveCC = listOfActiveCC + userLogin;
				} else {
					listOfActiveCC = listOfActiveCC + "," + userLogin;
				}
			}
		}
		ArrayList<String> propList = new ArrayList<String>();

		ArrayList<TbitsModelData> distributionDataColumns = (ArrayList<TbitsModelData>) getDataObject()
				.getData().get("distributionTableColumnsList");

		for (TbitsModelData md : distributionDataColumns) {
			propList.add((String) md.get("name"));
		}

		ArrayList<String[]> ConvertedDistributionList = (ArrayList<String[]>) getDataObject()
				.getData().get("ConvertedDistributionList");

		if (Boolean.valueOf((String) getDataObject().getData().get(
				"Wizard_Version"))
				&& (ConvertedDistributionList != null)
				&& ConvertedDistributionList.size() > 0)

		{
			if (DistributionDataSelectionPage.visits == 0) {

				ArrayList<String> curCC = new ArrayList<String>();
				String listOfnewlyAddedCC = "";
				ArrayList<String> deletedCc = new ArrayList<String>();
				String listOfDeletedCC = "";

				HashMap<String, String> hm = (HashMap<String, String>) getDataObject()
						.getData().get("HashMapForTransientData");
				String CarryOverCCs = hm.get("ccList");

				getDataObject().getData().put("CarryOverCC", CarryOverCCs);

				for (String totalCC : userSet) {
					boolean ismatched = false;

					for (String prevcc : CarryOverCCs.split(",")) {
						if (totalCC.equals(prevcc)) {
							ismatched = true;
							break;
						}

					}
					if (!ismatched) {
						curCC.add(totalCC);
						listOfnewlyAddedCC = listOfnewlyAddedCC + totalCC + ",";
					}

				}

				for (String prevcc : CarryOverCCs.split(",")) {
					boolean ismatched = false;

					for (String curcc : userSet) {
						if (prevcc.equals(curcc)) {
							ismatched = true;
							break;
						}

					}

					if (!ismatched) {
						deletedCc.add(prevcc);
						listOfDeletedCC = listOfDeletedCC + prevcc + ",";
					}

				}

				TbitsModelData tempData = new TbitsModelData();

				ArrayList<TbitsModelData> distributionListModelData = new ArrayList<TbitsModelData>();

				for (String userLogin1 : curCC) {
					if (!userLogin1.trim().equals("")) {
						UserClient uc = userCache.getObject(userLogin1);// getUserBasedOnLogin(userMap,
						// userLogin);
						if (uc != null) {
							TbitsModelData tempData2 = new TbitsModelData();
							tempData2.set("login", uc.getUserLogin());

							for (TbitsModelData distColumnTmd : distributionDataColumns) {
								String property = (String) distColumnTmd
										.get("name");
								if (property != null) {
									// Check if its a user property if
									// not,
									// fetch
									// default values from db.
									String value = (String) uc.get(property);
									if ((value != null)
											&& (!value.trim().equals("")))
										tempData2.set(property, value);
									else {
										String tempValue = distColumnTmd
												.get(TransmittalConstants.FIELD_CONFIG);
										if ((Integer) distColumnTmd
												.get("data_type_id") != 9) {
											if ((tempValue != null)
													&& (!tempValue.trim()
															.equals("")))
												tempData2.set(property,
														tempValue);
											else
												tempData2.set(property, "-");
										} else {
											HashMap<String, String> typesMap = new HashMap<String, String>();
											DistributionTableColumnsConfig
													.fetchKeyValuePairsfromJsonString(
															typesMap, tempValue);
											tempValue = typesMap.keySet()
													.iterator().next();
											if (tempValue != null)
												tempData2.set(property,
														tempValue);
											else
												tempData2.set(property, "-");
										}
									}
								}
							}
							distributionListModelData.add(tempData2);
						}
					}
				}

				ConvertedDistributionList = (ArrayList<String[]>) getDataObject()
						.getData().get("ConvertedDistributionList");

				for (String[] eachData : ConvertedDistributionList) {
					Boolean ignore = false;

					tempData = new TbitsModelData();

					for (String eachProp : eachData) {
						String[] data;
						String prop;
						String value;
						if (!eachProp.equals("-")) {
							data = eachProp.split(",");

							prop = data[0];
							value = data[1];
							if (prop.equals("login")) {
								if (deletedCc.contains(value)) {
									ignore = true;
								}
							}

							if (propList.contains(prop)) {

								tempData.set(prop, value);

							}
						}
					}
					if (!ignore)
						distributionListModelData.add(tempData);
				}

				JSONArray arrayOfNewCC = getDataForPage3InApprovalCycle(listOfnewlyAddedCC);

				JSONArray arrayOfPrevcc = getDistributionListJsonString(
						distributionListModelData, propList);

				JSONArray newJsonArray = new JSONArray();
				int indexForNewCCJsonArray = 0;

				while ((arrayOfNewCC.get(indexForNewCCJsonArray) != null)) {
					newJsonArray.set(indexForNewCCJsonArray, arrayOfNewCC
							.get(indexForNewCCJsonArray++));
				}

				int indexForPrevCCJsonArray = 0;
				int startingIndexForTotalCCInSecondCycle = indexForPrevCCJsonArray
						+ indexForNewCCJsonArray;

				while ((arrayOfPrevcc.get(indexForPrevCCJsonArray) != null)) {
					newJsonArray.set(startingIndexForTotalCCInSecondCycle++,
							arrayOfPrevcc.get(indexForPrevCCJsonArray++));
				}

				this.getDataObject().getData().put(
						TransmittalConstants.DISTRIBUTION_TABLE,
						newJsonArray.toString());

			} else {

				ArrayList<String> curCC = new ArrayList<String>();
				String listOfnewlyAddedCC = "";
				ArrayList<String> deletedCc = new ArrayList<String>();
				String listOfDeletedCC = "";
				String CarryOverCCs = listOfCC;

				for (String totalCC : userSet) {
					boolean ismatched = false;

					for (String prevcc : CarryOverCCs.split(",")) {
						if (totalCC.equals(prevcc)) {
							ismatched = true;
							break;
						}

					}
					if (!ismatched) {
						curCC.add(totalCC);
						listOfnewlyAddedCC = listOfnewlyAddedCC + totalCC + ",";
					}

				}

				for (String prevcc : CarryOverCCs.split(",")) {
					boolean ismatched = false;

					for (String curcc : userSet) {
						if (prevcc.equals(curcc)) {
							ismatched = true;
							break;
						}

					}

					if (!ismatched) {
						deletedCc.add(prevcc);
						listOfDeletedCC = listOfDeletedCC + prevcc + ",";
					}

				}

				TbitsModelData tempData = new TbitsModelData();

				ArrayList<TbitsModelData> distributionListModelData = new ArrayList<TbitsModelData>();

				for (String userLogin1 : curCC) {
					if (!userLogin1.trim().equals("")) {
						UserClient uc = userCache.getObject(userLogin1);// getUserBasedOnLogin(userMap,
						// userLogin);
						if (uc != null) {
							TbitsModelData tempData2 = new TbitsModelData();
							tempData2.set("login", uc.getUserLogin());

							for (TbitsModelData distColumnTmd : distributionDataColumns) {
								String property = (String) distColumnTmd
										.get("name");
								if (property != null) {
									// Check if its a user property if
									// not,
									// fetch
									// default values from db.
									String value = (String) uc.get(property);
									if ((value != null)
											&& (!value.trim().equals("")))
										tempData2.set(property, value);
									else {
										String tempValue = distColumnTmd
												.get(TransmittalConstants.FIELD_CONFIG);
										if ((Integer) distColumnTmd
												.get("data_type_id") != 9) {
											if ((tempValue != null)
													&& (!tempValue.trim()
															.equals("")))
												tempData2.set(property,
														tempValue);
											else
												tempData2.set(property, "-");
										} else {
											HashMap<String, String> typesMap = new HashMap<String, String>();
											DistributionTableColumnsConfig
													.fetchKeyValuePairsfromJsonString(
															typesMap, tempValue);
											tempValue = typesMap.keySet()
													.iterator().next();
											if (tempValue != null)
												tempData2.set(property,
														tempValue);
											else
												tempData2.set(property, "-");
										}
									}
								}
							}
							distributionListModelData.add(tempData2);
						}
					}
				}

				ArrayList<String[]> ConvertedDistributionList1 = (ArrayList<String[]>) getDataObject()
						.getData().get("NewConvertedDistributionList");

				for (String[] eachData : ConvertedDistributionList1) {
					Boolean ignore = false;

					tempData = new TbitsModelData();

					for (String eachProp : eachData) {
						String[] data;
						String prop;
						String value;
						if (!eachProp.equals("-")) {
							data = eachProp.split(",");

							prop = data[0];
							value = data[1];
							if (prop.equals("login")) {
								if (deletedCc.contains(value)) {
									ignore = true;
								}
							}

							if (propList.contains(prop)) {

								tempData.set(prop, value);

							}
						}
					}
					if (!ignore)
						distributionListModelData.add(tempData);
				}

				JSONArray arrayOfNewCC = getDataForPage3InApprovalCycle(listOfnewlyAddedCC);

				JSONArray arrayOfPrevcc = getDistributionListJsonString(
						distributionListModelData, propList);

				JSONArray newJsonArray = new JSONArray();
				int indexForNewCCJsonArray = 0;

				while ((arrayOfNewCC.get(indexForNewCCJsonArray) != null)) {
					newJsonArray.set(indexForNewCCJsonArray, arrayOfNewCC
							.get(indexForNewCCJsonArray++));
				}

				int indexForPrevCCJsonArray = 0;
				int startingIndexForTotalCCInSecondCycle = indexForPrevCCJsonArray
						+ indexForNewCCJsonArray;

				while ((arrayOfPrevcc.get(indexForPrevCCJsonArray) != null)) {
					newJsonArray.set(startingIndexForTotalCCInSecondCycle++,
							arrayOfPrevcc.get(indexForPrevCCJsonArray++));
				}

				this.getDataObject().getData().put(
						TransmittalConstants.DISTRIBUTION_TABLE,
						newJsonArray.toString());

			}
		} else {
			ArrayList<TbitsModelData> distributionListModelData = new ArrayList<TbitsModelData>();

			for (String userLogin1 : listOfActiveCC.split(",")) {
				if (!userLogin1.trim().equals("")) {
					UserClient uc = userCache.getObject(userLogin1);// getUserBasedOnLogin(userMap,
					// userLogin);
					if (uc != null) {
						TbitsModelData tempData2 = new TbitsModelData();
						tempData2.set("login", uc.getUserLogin());

						for (TbitsModelData distColumnTmd : distributionDataColumns) {
							String property = (String) distColumnTmd
									.get("name");
							if (property != null) {
								// Check if its a user property if
								// not,
								// fetch
								// default values from db.
								String value = (String) uc.get(property);
								if ((value != null)
										&& (!value.trim().equals("")))
									tempData2.set(property, value);
								else {
									String tempValue = distColumnTmd
											.get(TransmittalConstants.FIELD_CONFIG);
									if ((Integer) distColumnTmd
											.get("data_type_id") != 9) {
										if ((tempValue != null)
												&& (!tempValue.trim()
														.equals("")))
											tempData2.set(property, tempValue);
										else
											tempData2.set(property, "-");
									} else {
										HashMap<String, String> typesMap = new HashMap<String, String>();
										DistributionTableColumnsConfig
												.fetchKeyValuePairsfromJsonString(
														typesMap, tempValue);
										tempValue = typesMap.keySet()
												.iterator().next();
										if (tempValue != null)
											tempData2.set(property, tempValue);
										else
											tempData2.set(property, "-");
									}
								}
							}
						}
						distributionListModelData.add(tempData2);
					}
				}
			}

		}

		if (ccList.getStringValue() == null) {
			listOfCC = "";
		} else {
			listOfCC = listOfActiveCC;
		}

		this.getDataObject().getData().put("ccList", listOfActiveCC);

		getDataObject().getData().put("trnProcessId",
				currentTransmittalProcessId);

	}

	/*
	 * if version is old or process is different
	 */

	public FormPanel getWidget() {
		return widget;
	}

	public void initializeWidget() {
		widget = new FormPanel();
		widget.setLabelWidth(150);
		widget.setBodyBorder(false);
		widget.setScrollMode(Scroll.AUTO);
		widget.setHeading("Process Parameter Page");
		widget.setFrame(true);

	}

	public void onDisplay() {

		if (Boolean.valueOf((String) dataObject.getData().get(
				"defaultProcessExists"))) {
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
		} else {
			if (visits == 0) {
				wizardContext.getValue(
						TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
						TransmittalAbstractWizard.class).hidePreviewDOCButton();
				wizardContext.getValue(
						TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
						TransmittalAbstractWizard.class).hideFinishButton();

			} else {
				if (flagForCreateTransmittalButton) {
					wizardContext
							.getValue(
									TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
									TransmittalAbstractWizard.class)
							.showFinishButton();
					wizardContext
							.getValue(
									TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
									TransmittalAbstractWizard.class)
							.hidePreviewDOCButton();
				} else {
					wizardContext
							.getValue(
									TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
									TransmittalAbstractWizard.class)
							.showPreviewDOCButton();
					wizardContext
							.getValue(
									TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
									TransmittalAbstractWizard.class)
							.hideFinishButton();
				}
			}

		}

		wizardContext.getValue(
				TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
				TransmittalAbstractWizard.class).showNextButton();
		wizardContext.getValue(
				TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
				TransmittalAbstractWizard.class).hideBackButton();

		wizardContext.getValue(
				TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
				TransmittalAbstractWizard.class).showFinishTransmittalButton1();
		wizardContext.getValue(
				TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
				TransmittalAbstractWizard.class)
				.hideFinishTransmittalForPage2Btn();
		++visits;
	}

	public void onInitialize() {

	}

	@SuppressWarnings("unchecked")
	public boolean onLeave() {

		if (!validateDropDown()) {
			return false;
		}
		if (!isCanContinue)
			return false;

		if ((toList.getStringValue() == null)
				|| toList.getStringValue().trim().equals("")) {
			Window.alert("'To' field cannot be empty. Please select a user.");
			return false;
		}

		if (currentTransmittalProcessId == 0) {
			Window
					.alert("Please choose an appropriate transmittal type other than \"-\", from the"
							+ " selection dropdown. Else please try clicking next again.");
			return false;
		}

		/*
		 * if (keyDuetoDropdow.equals("")) {
		 * Window.alert("Please select a appropriate value in " +
		 * fieldForTrnKey.getFieldLabel() + " else select a different process");
		 * return false; }
		 */
		if (!checkStickinessCriteria())
			return false;

		String ruleResult = runValidationRules(validationRulesList,
				(HashMap<Integer, TbitsTreeRequestData>) getDataObject()
						.getData().get("mapOfRequests"));
		if ((ruleResult != null) && (!ruleResult.trim().isEmpty())) {
			TbitsInfo.info("Validation rule result for process: "
					+ currentTransmittalProcessId);
			Window
					.alert(ruleResult
							+ "\n Hence cannot continue transmittal process. Please make the changes "
							+ "for the above mentioned fields and then transmit.");
			return false;
		}
		validationResult.setText(ruleResult);
		this.setValues();
		this.nextPage.buildPage(dataObject);
		(this.nextPage.getNext()).buildPage(dataObject);
		HashMap<String, String> page2Data = getDataForPage2();
		for (String keyName : page2Data.keySet()) {
			String JsonString = page2Data.get(keyName);
			this.getDataObject().getData().put(keyName, JsonString);
		}

		return true;
	}

	private boolean validateDropDown() {
		// TODO Auto-generated method stub

		boolean flag = true;
		StringBuilder errorMessage = new StringBuilder("");
		if (fieldForTrnKey != null) {
			if (!dropDownKey.equals("")) {
				ArrayList<TbitsTreeRequestData> requestList = (ArrayList<TbitsTreeRequestData>) getDataObject()
						.getData().get("TreeModeldataOfRequest");
				if (fieldForTrnKey.getValue() != null) {
					String text = fieldForTrnKey.getValue().get("name");
					for (TbitsTreeRequestData trd : requestList) {
						if (!((String) trd.get(dropDownKey)).equals(text)) {
							flag = false;
							errorMessage
									.append("For request "
											+ trd.getRequestId()
											+ " The value of field "
											+ dropDownKey
											+ " does not match with the selected value "
											+ text);
							errorMessage.append("\n");
						}
					}

				} else {
					return true;
				}

				if (!flag) {

					Window.alert(errorMessage.toString());
				}
				return flag;
			}
		}
		return true;
	}

	private void fetchTrnWizardExtendedFieldsFromDB(final FormData formData) {
		try {

			TransmittalConstants.dbService.getTrnExtendedFields(
					currentTransmittalProcessId,
					new AsyncCallback<ArrayList<TbitsModelData>>() {

						public void onFailure(Throwable throwable) {
							throwable.printStackTrace();
							Window.alert(throwable.getMessage());
						}

						@SuppressWarnings("unchecked")
						public void onSuccess(
								ArrayList<TbitsModelData> columnList) {
							if (columnList != null) {
								getDataObject().getData().put(
										TRN_EXTENDED_FIELDS, columnList);
								addExtendedFields(
										widget,
										formData,
										(ArrayList<TbitsModelData>) getDataObject()
												.getData().get(
														"trnExtendedFields"));
							}
						}
					});
		} catch (TbitsExceptionClient e) {
			e.printStackTrace();
			Window.alert(e.getMessage());
		}
	}

	/**
	 * Fetch the transaction process parameters from database for the given
	 * transaction process id
	 * 
	 * @param formData
	 *            -
	 * @param ttId
	 *            - transaction process id
	 * @param dcrSystemId
	 *            - source system id
	 * @param dcrRequestList
	 *            - request list for the source system id
	 */
	private void fetchTrnProcessParametersFromDB(final FormData formData,
			int ttId, int dcrSystemId, ArrayList<Integer> dcrRequestList) {

		TransmittalConstants.dbService.getTransmittalProcessParameters(
				dcrSystemId, dcrRequestList, ttId,
				new AsyncCallback<TbitsModelData>() {

					public void onFailure(Throwable caught) {
						caught.printStackTrace();
						Window
								.alert("Error occurred while fetching transmittal process parameters.\n"
										+ caught.getMessage());
					}

					public void onSuccess(TbitsModelData result) {
						if (result != null) {
							TbitsModelData processparams = new TbitsModelData();
							for (String property : result.getPropertyNames()) {
								System.out.println(result.get(property));
								Object value = result.get(property);

								processparams.set(property, value);

							}
							getDataObject().getData().put(
									"transmittalProcessParams", processparams);
							String stickinessJsonString = (String) getDataObject()
									.getData().get("stickiness");

							if ((stickinessJsonString != null)
									&& (!stickinessJsonString.trim().equals(""))) {
								HashMap<String, Integer> stickinessDegrees = getStickinessDegrees(stickinessJsonString);
								if (stickinessDegrees != null)
									stickinessMap.putAll(stickinessDegrees);
							}

							String fromListVal = (String) result
									.get(DTN_SIGNATORY);
							List<UserClient> fromClientList = new ArrayList<UserClient>();
							if (fromListVal != null) {
								if (!fromListVal.trim().equals("")) {
									UserCache userCache = CacheRepository
											.getInstance().getCache(
													UserCache.class);
									// HashMap<String, UserClient> userMap =
									// userCache.getMap();
									for (String userLogin : fromListVal
											.split(",")) {
										UserClient uc = userCache
												.getObject(userLogin);
										// if (uc == null)
										// uc =
										// compareUserLoginByIgnoringCase(userMap,
										// userLogin);

										if ((uc != null) && (uc.getIsActive()))
											fromClientList.add(uc);
										// else{
										// isCanContinue = false;
										// Window.alert("User login provided in \"DTN Signatory\" in transmittal parameters,"
										// +
										// " is invalid/inactive. Hence cannot continue.");
										// return;
										// }
									}
									from.getStore().add(fromClientList);
									from.setForceSelection(true);

									if (fromClientList.size() > 0) {
										from.setValue(fromClientList.get(0));
										// from.setSelection(fromClientList);
									} else {
										from.setValue(new UserClient());
									}

								}
							} else {
								from.setStore(new ListStore<UserClient>());
								// Window.alert("No default users list provided for \"From\" field. Cannot continue, hence please check "
								// +
								// "the configuration.");
								// return;
							}
							// from.setStringValue(fromListVal);

							toList.reset();
							String toListVal = (String) result.get(TO_LIST);
							toList.setStringValue(toListVal);
							ccList.reset();
							String ccListVal = (String) result.get(CC_LIST);
							ccList.setStringValue(ccListVal);

							accessTo.reset();
							String accessToVal = (String) result.get(ACCESS_TO);

							if (accessToVal != null) {
								if (!accessToVal.trim().equals(""))
									accessTo.setStringValue(accessToVal);
								if (GXT.isIE)
									accessTo.setWidth(ccList.getWidth() - 12);
								accessTo.show();
							} else {
								accessTo.hide();
							}

							String remarksVal = (String) result.get(REMARKS);
							if ((remarksVal == null)
									|| (remarksVal.trim().equals("")))
								remarksVal = "-";
							remarks.setRawValue(remarksVal);
							String subjectVal = (String) result
									.get(TRANSMITTAL_SUBJECT);
							Subject.setRawValue(subjectVal);
							String emailBodyVal = (String) result
									.get(EMAIL_BODY);
							emailBody.setHTML(emailBodyVal);
							currentTransmittalProcessId = (Integer) result
									.get("trnProcessId");

							originalTrnPrefix = (String) result
									.get(TransmittalConstants.TRANSMITTAL_ID_PREFIX);
							getDataObject().getData().put("trnProcessId",
									currentTransmittalProcessId);

							if (Boolean.valueOf((String) result
									.get("isExistInRole"))) {
								wizardContext
										.getValue(
												TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
												TransmittalAbstractWizard.class)
										.showFinishButton();
								wizardContext
										.getValue(
												TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
												TransmittalAbstractWizard.class)
										.hidePreviewDOCButton();
								getDataObject().getData().put(
										"statusOfFinishButton",
										String.valueOf(true));
								flagForApprovalButton = false;
								flagForCreateTransmittalButton = true;
							} else {
								wizardContext
										.getValue(
												TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
												TransmittalAbstractWizard.class)
										.showPreviewDOCButton();
								wizardContext
										.getValue(
												TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
												TransmittalAbstractWizard.class)
										.hideFinishButton();
								getDataObject().getData().put(
										"statusOfFinishButton",
										String.valueOf(false));
								flagForApprovalButton = true;
								flagForCreateTransmittalButton = false;
							}

							/*
							 * if (map.containsKey(Integer.parseInt((String)
							 * result
							 * .get(ProcessParametersWizardPage.KEYFIELD)))) {
							 * fieldForTrnKey.setFieldLabel("Select a " +
							 * map.get(Integer.parseInt((String)
							 * result.get(ProcessParametersWizardPage
							 * .KEYFIELD))) .getDisplayName());
							 * 
							 * ArrayList<TbitsModelData> store = result
							 * .get(ProcessParametersWizardPage.KEYFIELD_LIST);
							 * fieldForTrnKey.getStore().add(store); }
							 */
							if (currentTransmittalProcessId > 0) {
								fetchValidationRules();
								fetchTrnAttachmentTableColumnsFromDB();
								fetchDistributionTableColumnsFromDB();
								fetchTrnWizardExtendedFieldsFromDB(formData);

							}

							if (fieldForTrnKey != null) {
								fieldForTrnKey.getStore().removeAll();
								fieldForTrnKey.clear();
								if (result.get(KEYFIELD) != null) {
									fieldForTrnKey.show();
									if (map
											.containsKey(Integer
													.parseInt((String) result
															.get(ProcessParametersWizardPage.KEYFIELD)))) {

										ArrayList<TbitsModelData> store = result
												.get(ProcessParametersWizardPage.KEYFIELD_LIST);

										fieldForTrnKey.show();
										dropDownKey = map
												.get(
														Integer
																.parseInt((String) result
																		.get(KEYFIELD)))
												.getName();
										fieldForTrnKey
												.setFieldLabel("Select a "
														+ map
																.get(
																		Integer
																				.parseInt((String) result
																						.get(KEYFIELD)))
																.getDisplayName());

										fieldForTrnKey.getStore().add(store);
									}
								} else {
									fieldForTrnKey.hide();
									dropDownKey = "";
								}

							}

						}
					}
				});
	}

	/**
	 * Get the validation rules for selected transmittal process id
	 */
	private void fetchValidationRules() {
		if (validationRulesList != null)
			validationRulesList.clear();
		TransmittalConstants.dbService.getValidationRules(
				currentTransmittalProcessId,
				new AsyncCallback<ArrayList<TbitsModelData>>() {

					public void onFailure(Throwable caught) {
						caught.printStackTrace();
						Window
								.alert("Error occurred while fetching validation rules.");
					}

					@SuppressWarnings("unchecked")
					public void onSuccess(ArrayList<TbitsModelData> result) {
						if (result != null) {
							validationRulesList.addAll(result);
							String ruleResult = runValidationRules(
									validationRulesList,
									(HashMap<Integer, TbitsTreeRequestData>) getDataObject()
											.getData().get("mapOfRequests"));
							validationResult.setText(ruleResult);
						}
					}
				});
	}

	/**
	 * Fetch the distribution table columns config from db for this transmittal
	 * process
	 */
	public void fetchDistributionTableColumnsFromDB() {
		try {

			TransmittalConstants.dbService.getDistributionTableColumns(
					currentTransmittalProcessId,
					new AsyncCallback<ArrayList<TbitsModelData>>() {

						public void onFailure(Throwable throwable) {
							throwable.printStackTrace();
							Window.alert(throwable.getMessage());
						}

						public void onSuccess(
								ArrayList<TbitsModelData> columnList) {
							if (columnList != null) {

								Comparator<TbitsModelData> comp = new Comparator<TbitsModelData>() {
									public int compare(TbitsModelData o1,
											TbitsModelData o2) {

										if ((o1 != null) && (o2 != null)) {
											int s1 = (Integer) o1
													.get("column_order");
											int s2 = (Integer) o2
													.get("column_order");
											if (s1 > s2)
												return 1;
											else if (s1 == s2)
												return 0;
											else if (s1 < s2)
												return -1;
										}
										return 0;
									}
								};
								// Sort the column info, before creating column
								// configs out of them. So, that they maintain
								// the sort order and
								// hence the column order in the table.
								Collections.sort(columnList, comp);

								getDataObject().getData().put(
										"distributionTableColumnsList",
										columnList);

							}
						}
					});
		} catch (TbitsExceptionClient e) {
			Window.alert(e.getMessage());
		}
	}

	/**
	 * get attachment selection table columns list from db
	 */
	private void fetchTrnAttachmentTableColumnsFromDB() {

		try {

			TransmittalConstants.dbService.getAttachmentSelectionTableColumns(
					currentTransmittalProcessId,
					new AsyncCallback<ArrayList<TbitsModelData>>() {

						public void onFailure(Throwable throwable) {
							throwable.printStackTrace();
							Window.alert(throwable.getMessage());
						}

						public void onSuccess(
								ArrayList<TbitsModelData> columnList) {
							if (columnList != null) {

								Comparator<TbitsModelData> comp = new Comparator<TbitsModelData>() {
									public int compare(TbitsModelData o1,
											TbitsModelData o2) {

										if ((o1 != null) && (o2 != null)) {
											int s1 = (Integer) o1
													.get("column_order");
											int s2 = (Integer) o2
													.get("column_order");
											if (s1 > s2)
												return 1;
											else if (s1 == s2)
												return 0;
											else if (s1 < s2)
												return -1;
										}
										return 0;
									}
								};
								// Sort the column info, before creating column
								// configs out of them. So, that they maintain
								// the sort order and
								// hence the column order in the table.
								Collections.sort(columnList, comp);

								getDataObject().getData()
										.put("attachmentTableColumnList",
												columnList);
							}
						}
					});
		} catch (TbitsExceptionClient e) {
			e.printStackTrace();
			Window.alert(e.getMessage());
		}
	}

	/**
	 * @return
	 */
	private boolean checkStickinessCriteria() {
		for (String fieldName : stickinessMap.keySet()) {
			if (fieldName != null) {
				int degree = stickinessMap.get(fieldName);
				Field<?> field = getFieldByName(fieldName);

				TbitsModelData processparams = (TbitsModelData) getDataObject()
						.getData().get("transmittalProcessParams");
				String fieldValue = processparams.get(fieldName);
				if ((field != null) && (field instanceof UserPicker)) {
					if ((fieldValue == null))
						fieldValue = "";
					String rawValue = ((UserPicker) field).getStringValue();
					if (rawValue.equals(""))
						rawValue = field.getRawValue();
					String[] usersFromField = rawValue.split(",");
					String[] defaultUsers = fieldValue.split(",");
					switch (degree) {
					case 1: {
						Log
								.info("Stickiness 1, applied, hence allowing any user selection.");
						// TbitsInfo.info("Stickiness 1, applied, hence allowing any user selection.");
						break;
					}
					case 2: {
						if (usersFromField.length >= 1) {
							int defaultCount = getNonEmptyCount(defaultUsers);
							int actualCount = 0;
							int diffCount = 0;
							if (defaultCount > 0) {
								for (int i = 0; i < usersFromField.length; i++) {
									for (int j = 0; j < defaultUsers.length; j++) {
										if (usersFromField[i].trim().equals(
												defaultUsers[j].trim()))
											actualCount += 1;
										else
											diffCount += 1;
									}
								}
								if (fieldValue.equals(""))
									continue;
								else if ((defaultCount == actualCount)
										&& (diffCount >= 0))
									continue;
								else {
									Window
											.alert("Cannot remove existing(default) users from field: "
													+ field.getFieldLabel()
													+ ", hence resetting them.");
									((UserPicker) field)
											.setStringValue(fieldValue);
									return false;
								}
							}
						}
						break;
					}
					case 3: {
						if (usersFromField.length >= 1) {
							int defaultCount = getNonEmptyCount(defaultUsers);
							int actualCount = 0;
							if (defaultCount > 0) {
								for (int i = 0; i < usersFromField.length; i++) {
									for (int j = 0; j < defaultUsers.length; j++) {
										if (usersFromField[i].trim().equals(
												defaultUsers[j].trim()))
											actualCount += 1;
									}
								}
								if (defaultCount == actualCount)
									continue;
								else {
									Window
											.alert("Cannot add/remove to existing(default) users from field: "
													+ field.getFieldLabel()
													+ ", resetting to default.");
									((UserPicker) field)
											.setStringValue(fieldValue);
									return false;
								}
							}
						}
						break;
					}
					}
				}
			}
		}
		return true;
	}

	private static int getNonEmptyCount(String[] userNames) {
		int count = 0;
		if (userNames == null)
			return count;
		for (int i = 0; i < userNames.length; i++) {
			if (!userNames[i].trim().equals(""))
				count += 1;
		}

		return count;
	}

	private Field<?> getFieldByName(String fieldName) {
		if (fieldName == null)
			return null;
		List<Field<?>> fields = widget.getFields();
		if (fields != null) {
			for (Field<?> field : fields) {
				if (field.getName().equalsIgnoreCase(fieldName.trim()))
					return field;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void addExtendedFields(FormPanel widget, FormData formData,
			ArrayList<TbitsModelData> trnWizardExtendedFields) {
		if ((trnWizardExtendedFields != null)
				&& (!trnWizardExtendedFields.isEmpty())) {

			for (TbitsModelData extField : trnWizardExtendedFields) {
				if (extField != null) {
					String name = extField
							.get(TransmittalConstants.NAME_COLUMN);
					Integer fieldId = (Integer) extField.get(FIELD_ID);
					String fieldConfig = extField.get(FIELD_CONFIG);
					Integer index = (Integer) extField.get(FIELD_ORDER);
					BAField baField;

					baField = Utils.getBAFieldById(fieldId,
							(ArrayList<BAField>) getDataObject().getData().get(
									"BAFields"));

					if (baField == null) {
						TbitsInfo.error("Transmittal wizard field with id: "
								+ fieldId + ", does not exist in BA: "
								+ ClientUtils.getCurrentBA().getSystemPrefix());
						Window.alert("Transmittal wizard field with id: "
								+ fieldId + ", does not exist in BA: "
								+ ClientUtils.getCurrentBA().getSystemPrefix()
								+ ". Hence cannot continue.");
						isCanContinue = false;
					}

					switch (baField.getDataTypeId()) {
					case TransmittalConstants.TYPE: {
						TypeFieldControl typeField = new TypeFieldControl(
								(BAFieldCombo) baField);
						typeField.setName(name);
						TypeClient defTypeClient = typeField
								.getModelForStringValue(fieldConfig);
						if (defTypeClient != null) {
							typeField.setValue(defTypeClient);
						}

						// if default ordering is set, then set the ordering to
						// such that they occur before
						// subject and email fields.
						if (index == 0)
							index = widget.getFields().size() - 2;
						widget.insert(typeField, index, formData);
						widget.layout(true);
						break;
					}
					case TransmittalConstants.INT:
					case TransmittalConstants.TEXT:
					case TransmittalConstants.STRING: {
						TextField<String> textField = new TextField<String>();
						textField.setFieldLabel(name);
						textField.setName(name);
						widget.insert(textField, index, formData);
						widget.layout(true);
						break;
					}
					}
				}
			}
		}
	}

	/**
	 * @param name
	 * @param dName
	 * @param typesMap
	 * @return
	 */
	@SuppressWarnings("unused")
	private ComboBox<TbitsModelData> getComboBoxForTypeField(String name,
			HashMap<String, String> typesMap) {
		ListStore<TbitsModelData> ls = new ListStore<TbitsModelData>();
		ComboBox<TbitsModelData> typeField = new ComboBox<TbitsModelData>();
		typeField.setStore(ls);
		typeField.setTriggerAction(TriggerAction.ALL);
		typeField.setFieldLabel(name);
		typeField.setName(name);
		typeField.setDisplayField("name");
		typeField.setMinListWidth(350);

		if (typesMap != null) {
			TbitsModelData tmd = null;
			for (String s : typesMap.keySet()) {
				TbitsModelData tmpModel = new TbitsModelData();
				tmpModel.set("name", s);
				tmpModel.set(DISPLAY_NAME, typesMap.get(s));
				typeField.getStore().add(tmpModel);
				if (tmd == null)
					tmd = tmpModel;
			}
			typeField.setValue(tmd);
		}
		return typeField;
	}

	/**
	 * To replace a part of transmittal prefix, with the value input by the user
	 * in the transmittal prefix, in the extended field configured in the
	 * transmittal wizard.
	 * 
	 * @param hashMap
	 * @return a string value representing the replacement to be substituted in
	 *         the the transmittal prefix.
	 */
	@SuppressWarnings("unchecked")
	private String getExtendedFieldNameToBePartOfDtnNumber(
			HashMap<String, Object> hashMap) {
		String isExtFieldName = null;
		ArrayList<TbitsModelData> extendedFields = (ArrayList<TbitsModelData>) getDataObject()
				.getData().get(TRN_EXTENDED_FIELDS);
		for (TbitsModelData field : extendedFields) {
			if (field != null) {
				String property = field.get("name");
				if ((Boolean) field.get(IS_DTN_NUMBER_PART)) {
					isExtFieldName = property;
					break;
				}
			}
		}
		if (isExtFieldName == null) {
			isCanContinue = false;
			TbitsInfo
					.error("Invalid transmittal process configuration, as the field(user-input) value to be replaced in transmittal"
							+ "number( ,"
							+ originalTrnPrefix
							+ ") is missing in \"transmittal wizard fields\" configuration. Please check if"
							+ " user-input field is not configured or, the transmittal number prefix is configured wrongly.");
			Window
					.alert("Invalid transmittal process configuration, as the field(user-input) value to be replaced in transmittal"
							+ " number( ,"
							+ originalTrnPrefix
							+ ") is missing in \"transmittal wizard fields\" configuration. Please check if"
							+ " user-input field is not configured or, the transmittal number prefix is configured wrongly.");
		}

		for (Field<?> wField : widget.getFields()) {
			if (wField != null) {
				String name = wField.getName();
				String value = (String) hashMap.get(name);
				if ((name != null) && name.equals(isExtFieldName))
					hashMap.put(isExtFieldName, value);
			}
		}

		return isExtFieldName;
	}

	private String getRequestFieldValueToBePartOfDtnNumber(String trnPrefix) {
		String requestFieldPart = null;
		String property = null;
		if (trnPrefix != null) {
			int beginIndex = trnPrefix.indexOf("<");
			int lastIndex = trnPrefix.indexOf(">");
			if (beginIndex < lastIndex)
				property = trnPrefix.substring(beginIndex + 1, lastIndex);
		}

		requestFieldPart = getRequestFieldValue(property);

		return requestFieldPart;
	}

	/**
	 * This method iterates over the requests to find a value of the field,
	 * whose value has to be replaced in the transmittal number prefix
	 * 
	 * @param property
	 *            Name of the field, whose value has to be replaced.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String getRequestFieldValue(String property) {
		String requestFieldPart = null;
		if ((requestFieldPart == null) && (property != null)) {
			HashMap<Integer, TbitsTreeRequestData> requestMap = (HashMap<Integer, TbitsTreeRequestData>) getDataObject()
					.getData().get("mapOfRequests");
			for (TbitsTreeRequestData trd : requestMap.values()) {
				BAField baField = TransmittalConstants.fieldCache
						.getObject(property);
				if (baField != null) {
					if (baField instanceof BAFieldCombo) {
						String typeName = trd.getAsString(property);
						if (typeName != null) {
							TypeClient selectedType = ((BAFieldCombo) baField)
									.getModelForName(typeName);
							if (selectedType != null) {
								requestFieldPart = selectedType
										.getDescription();
							} else
								continue;
						}
					} else
						requestFieldPart = trd.getAsString(property);
					break;
				}
			}
		}
		return requestFieldPart;
	}

	/**
	 * Iterates over each matching regular expression to pick out the
	 * field_names if present in the transmittal prefix, to be replaced by the
	 * field value for the drawings, being transmitted. This is to be used in
	 * case of type fields, even though it can be used with other than the type
	 * field except for attachment/bit fields, which would not make much sense
	 * replacing in a transmittal number.
	 * 
	 * 
	 * @param trnPrefix
	 *            transmittal_id_prefix parameter from trn_process_parameters
	 *            table.
	 * @param pattern
	 *            <[a-zA-Z0-9_-]+>
	 *            {@link TransmittalConstants.TRANSMITTAL_ID_PREFIX}
	 * @return a string(trnPrefix) with field values substituted.
	 */
	String getRequestFieldValueToBePartOfDtnNumber(String trnPrefix,
			String pattern) {
		if (trnPrefix != null) {
			int beginIndex = trnPrefix.indexOf("<");
			int lastIndex = trnPrefix.indexOf(">");
			trnPrefix = getRequestFieldValueToBePartOfDtnNumber(trnPrefix,
					pattern, beginIndex, lastIndex);
		}
		return trnPrefix;
	}

	/**
	 * Recursive call to replace the field-names in the transmittal prefix with
	 * the appropriate values from the requests(drawings/documents) selected for
	 * transmittal. This is to be used in case of type fields, even though it
	 * can be used with other than the type field except for attachment/bit
	 * fields, which would not make much sense replacing in a transmittal
	 * number.
	 * 
	 * @param trnPrefix
	 *            - transmittal_id_prefix parameter from trn_process_parameters
	 *            table.
	 * @param pattern
	 *            - <[a-zA-Z0-9_-]+>
	 *            {@link TransmittalConstants.TRANSMITTAL_ID_PREFIX}
	 * @param beginIndex
	 *            - first index of the matched sub-string.
	 * @param lastIndex
	 *            - last index of the matched sub-string.
	 * @return - a string(trnPrefix) with field values substituted.
	 */
	String getRequestFieldValueToBePartOfDtnNumber(String trnPrefix,
			String pattern, int beginIndex, int lastIndex) {

		String property = null;
		if (trnPrefix != null) {
			if ((beginIndex == -1) && (lastIndex == -1))
				return trnPrefix;
			else if ((beginIndex == -1) && (lastIndex > -1)
					|| (beginIndex > -1) && (lastIndex == -1))
				return null;
			else if (beginIndex > lastIndex)
				return null;
			else if (beginIndex < lastIndex) {
				property = trnPrefix.substring(beginIndex + 1, lastIndex);
				String requestFieldPart = getRequestFieldValue(property);
				if (requestFieldPart != null)
					trnPrefix = trnPrefix.replaceFirst(pattern,
							requestFieldPart);
				else
					return null;
				beginIndex = trnPrefix.indexOf("<");
				lastIndex = trnPrefix.indexOf(">");
				trnPrefix = getRequestFieldValueToBePartOfDtnNumber(trnPrefix,
						pattern, beginIndex, lastIndex);
			}
		}
		return trnPrefix;
	}

	/*
	 * String replaceRequestFieldPartsInTrnPrefix(String trnPrefix, String
	 * pattern) {
	 * 
	 * Pattern p = Pattern.compile(pattern); Matcher matcher =
	 * p.matcher(trnPrefix); StringBuffer sb = new StringBuffer(); while
	 * (matcher.find()) { String matchedString = matcher.group(); String
	 * fieldName = matchedString.substring(1, (matchedString.length()-1));
	 * String requestFieldValue = getRequestFieldValue(fieldName); if
	 * (requestFieldValue == null) return null; else
	 * matcher.appendReplacement(sb , requestFieldValue); }
	 * matcher.appendTail(sb); return sb.toString(); }
	 */

	/**
	 * @param userClients
	 * @return
	 */
	@SuppressWarnings("unused")
	private static String getUserLoginListFromUserClients(
			ArrayList<UserClient> userClients) {
		String userLogins = "";
		if (userClients != null) {
			for (UserClient uc : userClients) {
				userLogins = (userLogins == null) ? uc.getUserLogin()
						: userLogins + "," + uc.getUserLogin();
			}
		}
		return userLogins;
	}

	/**
	 * trnProcessId Get the stickiness from the jsonString extracted from
	 * transaction process parameters
	 * 
	 * @param jsonString
	 *            of type {"fieldName1":3, "fieldName2":2}
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private HashMap<String, Integer> getStickinessDegrees(String jsonString) {
		HashMap<String, Integer> stickinessMap = new HashMap<String, Integer>();
		try {
			JSONValue jsonValue = JSONParser.parse(jsonString);

			JSONObject fieldStickinessObj = jsonValue.isObject();
			if (fieldStickinessObj != null) {
				for (String key : fieldStickinessObj.keySet()) {
					JSONValue degreeJsonVal = fieldStickinessObj.get(key);
					if (degreeJsonVal != null)
						stickinessMap.put(key, Integer.parseInt(degreeJsonVal
								.toString()));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			TbitsInfo
					.error(
							"Error occurred while calculating strictness criteria for various fields. May be invalid "
									+ "configuration in transmittal parameters.\n",
							e);
			Window
					.alert("Error occurred while calculating stictness criteria for various fields. May be invalid "
							+ "configuration in transmittal parameters.\n"
							+ e.getMessage());
			this.isCanContinue = false;
		}
		return stickinessMap;
	}

	/**
	 * Method consisting of the execution of validation rules
	 * 
	 * @param ruleList
	 *            for the currentProcessID
	 * @param hashmap
	 *            of request id and rquest
	 */
	@SuppressWarnings("unchecked")
	String runValidationRules(ArrayList<TbitsModelData> rulesList,
			HashMap<Integer, TbitsTreeRequestData> requestsData) {
		StringBuffer result = new StringBuffer();

		if (rulesList != null) {
			for (TbitsTreeRequestData trd : requestsData.values()) {
				BusinessAreaClient baClient = (BusinessAreaClient) getDataObject()
						.getData().get("currentbaclient");
				String requestId = baClient.getSystemPrefix() + "#"
						+ trd.getAsString(IFixedFields.REQUEST);

				if (trd != null)
					for (TbitsModelData tmd : rulesList) {
						if (tmd != null) {
							Object fieldObj = tmd.get(FIELD_ID);
							if (fieldObj == null)
								continue;
							int fieldId = (Integer) fieldObj;
							String value = (String) tmd.get("value");
							BAField baField;
							if (fieldId > 0) {

								baField = Utils.getBAFieldById(fieldId,
										(ArrayList<BAField>) getDataObject()
												.getData().get("BAFields"));

								if (baField != null) {
									if (baField instanceof BAFieldCombo) {
										if ((value == null)
												|| (value.trim().isEmpty())) {
											continue;
										}
										String[] typesList = value.split(",");
										String newValue = "";
										for (int i = 0; i <= 3; i++) {
											newValue += typesList[i] + ",";
										}
										boolean isFound = false;
										String requestVal = trd
												.getAsString(baField.getName());
										if (requestVal != null) {
											for (String type : typesList) {

												if (type.trim().contentEquals(
														requestVal)) {
													isFound = true;
													break;
												} else
													continue;
											}
											if (!isFound)
												result
														.append("In drawing: "
																+ requestId
																+ ", field:\""
																+ baField
																		.getDisplayName()
																		.replace(
																				"&amp;",
																				"&")
																+ "\" should be in the one of the following: "
																+ newValue
																+ "... Please see the Validation Rule for: "
																+ baField
																		.getDisplayName()
																		.replace(
																				"&amp;",
																				"&")
																+ ".\n");
										}
									} else if (baField instanceof BAFieldCheckBox) {
										if ((!value.equalsIgnoreCase("true"))
												&& (!value
														.equalsIgnoreCase("false")))
											result
													.append("Value provided for validation should be either: true/false. "
															+ "Value set in database for validation is: "
															+ value + "\n");
										else {
											String booleanVal = trd
													.getAsString(baField
															.getName());
											if (booleanVal != null) {
												if (booleanVal
														.equalsIgnoreCase(value))
													continue;
												else
													result
															.append("In drawing: "
																	+ requestId
																	+ ", field:\""
																	+ baField
																			.getDisplayName()
																			.replace(
																					"&amp;",
																					"&")
																	+ "\" should be, \""
																	+ value
																	+ "\".\n");
											}
										}
									} else if (baField instanceof BAFieldString) {
										String varcharValue = (String) trd
												.getAsString(baField.getName());
										if (value.equalsIgnoreCase("notnull")) {
											if ((varcharValue != null)
													&& (!varcharValue.isEmpty()))
												continue;
											else {
												result
														.append("In drawing: "
																+ requestId
																+ ", in  field:\""
																+ baField
																		.getDisplayName()
																		.replace(
																				"&amp;",
																				"&")
																+ "\" value should not be empty. \n");
											}
										} else if (value
												.equalsIgnoreCase("null")) {
											if ((varcharValue == null)
													|| (varcharValue.isEmpty()))
												continue;
											else {
												result
														.append("In drawing: "
																+ requestId
																+ ", in  field:\""
																+ baField
																		.getDisplayName()
																		.replace(
																				"&amp;",
																				"&")
																+ "\" value should be empty. \n");
											}
										}
									} else if (baField instanceof BAFieldAttachment) {
										POJO attPOJO = trd.getAsPOJO(baField
												.getName());
										if (value.equalsIgnoreCase("null")) {
											if (attPOJO == null)
												continue;
											else {
												result
														.append("In drawing: "
																+ requestId
																+ ", in  field:\""
																+ baField
																		.getDisplayName()
																		.replace(
																				"&amp;",
																				"&")
																+ "\" attachments should be empty. \n");
											}
										} else if (value
												.equalsIgnoreCase("notnull")) {
											if ((attPOJO != null)
													&& (!((POJOAttachment) attPOJO)
															.getValue()
															.isEmpty()))
												continue;
											else
												result
														.append("In drawing: "
																+ requestId
																+ ", field:\""
																+ baField
																		.getDisplayName()
																		.replace(
																				"&amp;",
																				"&")
																+ "\" should not be empty. \n");
										}
									}
								}
							}
						}
					}
			}
		}
		return result.toString();
		//return "";
	}

	@SuppressWarnings("unused")
	private UserClient compareUserLoginByIgnoringCase(
			HashMap<String, UserClient> userMap, String userLogin) {
		if (userLogin != null) {
			for (String tempUserLogin : userMap.keySet()) {
				if (tempUserLogin != null) {
					if (tempUserLogin.equalsIgnoreCase(userLogin))
						return userMap.get(tempUserLogin);
				}
			}
		}
		return null;
	}

	/**
	 * @param fieldTMD
	 * @param fields
	 */
	@SuppressWarnings("unchecked")
	private String fillValueForField(TbitsModelData fieldTMD,
			List<Field<?>> fields) {
		String val = "";
		for (Field<?> wField : fields) {
			if (wField != null) {
				String name = wField.getName();
				if (fieldTMD != null) {
					String property = fieldTMD.get("name");
					if ((property != null)
							&& name.trim().equals(property.trim())) {
						if (wField instanceof ComboBox) {
							TypeClient typeClient = (TypeClient) wField
									.getValue();
							if (typeClient != null) {
								val = typeClient.get("description");
								if ((val == null) || (val.trim().length() == 0))
									val = wField.getRawValue();
							}
						} else
							val = wField.getRawValue();
						break;
					}
				}
			}
		}
		return val;
	}

	/**
	 * Method building the content of the page1 of the wizard,It attaches all of
	 * the widgets with the form
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void buildPage(final WizardData params) {

		if (Boolean.valueOf((String) params.getData().get("inapprovalcycle"))) {
			HashMap<String, Object> data = params.getData();
			execute(params);
			HashMap<String, String> mapOfCarryOverData = (HashMap<String, String>) params
					.getData().get("HashMapForTransientData");

			validationResult = new Label();
			validationResult.setPosition(0, 0);
			validationResult.setStyleAttribute("position", "relative");
			validationResult.setStyleAttribute("color", "red");
			widget.add(validationResult);

			final FormData formData = new FormData();
			formData.setWidth(700);

			ListStore<TbitsModelData> ls = new ListStore<TbitsModelData>();
			transmittalTypeCombo = new ComboBox<TbitsModelData>();
			transmittalTypeCombo.setStore(ls);
			transmittalTypeCombo.setTriggerAction(TriggerAction.ALL);
			transmittalTypeCombo.setFieldLabel("Select a transmittal type");
			transmittalTypeCombo.setName("transmittalTypeCombo");
			transmittalTypeCombo.setId("transmittalTypeCombo");
			transmittalTypeCombo.setDisplayField("name");
			transmittalTypeCombo.setMinListWidth(350);
			widget.add(transmittalTypeCombo);

			transmittalTypeCombo.getStore().add(
					(List<TbitsModelData>) data.get("dropdownlist"));
			ArrayList<TbitsModelData> arr = (ArrayList<TbitsModelData>) data
					.get("dropdownlist");
			for (TbitsModelData tmd1 : arr) {
				if (tmd1.get("name").equals(
						(String) data.get("selected_dropdown_name"))) {
					transmittalTypeCombo.setValue(tmd1);
				}
			}

			transmittalTypeCombo
					.addSelectionChangedListener(new SelectionChangedListener<TbitsModelData>() {
						public void selectionChanged(
								SelectionChangedEvent<TbitsModelData> se) {

							originalTrnPrefix = "";
							currentTransmittalProcessId = 0;
							isCanContinue = true;
							stickinessMap.clear();
							actualDate.reset();
							actualNumber.reset();
							from.getStore().removeAll();
							notify.reset();
							ArrayList<TbitsModelData> extendedFields = (ArrayList<TbitsModelData>) getDataObject()
									.getData().get(TRN_EXTENDED_FIELDS);
							if (extendedFields != null) {
								for (TbitsModelData extField : extendedFields) {
									if (extField != null) {
										for (Field wField : widget.getFields()) {
											String fName = extField.get("name");
											if (wField != null) {
												String wName = wField.getName();
												if ((wName != null)
														&& wName
																.equalsIgnoreCase(fName)) {
													widget.remove(wField);
													break;
												}
											}
										}
									}
								}
							}
							widget.layout(true);

							// ----------get the trasmittal selected from the
							// dropdown---------------//
							TbitsModelData sMD = se.getSelectedItem();
							int ttId = Integer.parseInt((String) sMD
									.get("trnDropDownId"));
							int dcrSystemId = Integer.parseInt((String) sMD
									.get("dcrSystemId"));

							ArrayList<Integer> dcrRequestList = new ArrayList<Integer>();
							ArrayList<TbitsTreeRequestData> requestdata = (ArrayList<TbitsTreeRequestData>) getDataObject()
									.getData().get("TreeModeldataOfRequest");
							for (TbitsTreeRequestData trd : requestdata) {
								dcrRequestList.add(trd.getRequestId());
							}
							fetchTrnProcessParametersFromDB(formData, ttId,
									dcrSystemId, dcrRequestList);
							params.getData().put("editableColumnsList", null);
							params.getData().put(
									"editableAttachmentColumnsList", null);
							params.getData().put("ConvertedDistributionList",
									null);
						}

					});

			ListStore<UserClient> store = new ListStore<UserClient>();

			from = new ComboBox<UserClient>();
			from.setStore(store);
			from.setDisplayField(UserClient.USER_LOGIN);
			from.setFieldLabel("From");
			from.setName(DTN_SIGNATORY);
			widget.add(from, formData);

			toList = new UserPicker(
					(BAFieldMultiValue) TransmittalConstants.fieldCache
							.getObject(IFixedFields.ASSIGNEE));
			toList.setFieldLabel("To");
			toList.setName(TO_LIST);

			widget.add(toList, formData);

			ccList = new UserPicker(
					(BAFieldMultiValue) TransmittalConstants.fieldCache
							.getObject(IFixedFields.SUBSCRIBER));
			ccList.setFieldLabel("Cc");
			ccList.setName(CC_LIST);

			widget.add(ccList, formData);

			accessTo = new UserPicker(
					(BAFieldMultiValue) TransmittalConstants.fieldCache
							.getObject(IFixedFields.SUBSCRIBER));
			accessTo.setFieldLabel("Access To");
			accessTo.setName(ACCESS_TO);

			widget.add(accessTo, formData);

			notify = new CheckBox();
			notify.setFieldLabel("Notify");
			notify.setName(NOTIFY);
			notify.setBoxLabel("");
			notify.setToolTip("Enable/disable email notification");
			widget.add(notify, formData);

			actualDate = new DateTimeControl();
			actualDate.setFieldLabel("Actual Date(of transmittal)");
			actualDate.setName(ACTUAL_DATE);
			actualDate.setEditable(true);
			actualDate.setFormat("dd MMM yyyy");
			actualDate.setMaxValue(new Date());
			actualDate.hide();
			widget.add(actualDate, formData);

			actualNumber = new TextField<String>();
			actualNumber.setFieldLabel("Actual DTN Number");
			actualNumber.setName(ACTUAL_NUMBER);
			actualNumber.hide();
			widget.add(actualNumber, formData);

			remarks = new TextArea();
			remarks.setFieldLabel("Comments");
			remarks.setName(REMARKS);
			widget.add(remarks, formData);

			Subject = new TextField<String>();
			Subject.setFieldLabel("Subject");
			Subject.setName(TRANSMITTAL_SUBJECT);
			widget.add(Subject, formData);

			LabelField labelField = new LabelField();
			labelField.setFieldLabel("Email Body:");
			widget.add(labelField, new FormData("-20"));

			CKConfig ckc = new CKConfig();
			ckc.setResizeMinHeight(100);
			ckc.setWidth("860px");
			ckc.setHeight("100px");
			ckc.setToolbar(CKConfig.PRESET_TOOLBAR.BASIC);
			emailBody = new CKEditor(ckc);

			widget.add(emailBody, formData);

			draftedBy = new TextField<String>();
			draftedBy.setFieldLabel("Drafted By");
			draftedBy.setName(DRAFTED_BY);
			draftedBy.hide();
			widget.add(draftedBy, formData);

			if (Boolean.valueOf(((String) data.get("StatusOfDtnDate")))) {
				isExistsInHistoryRole = true;
				actualDate.show();
				actualNumber.show();
				actualNumber.setValue(mapOfCarryOverData.get(actualNumber
						.getName()));
				/*
				 * actualDate.setValue(new
				 * Date(mapOfCarryOverData.get(actualDate .getName())));
				 */
			}

			String trnProcessId = mapOfCarryOverData.get("trnProcessId");// type
			currentTransmittalProcessId = Integer.parseInt(trnProcessId);
			String dtnSignatory = mapOfCarryOverData.get("dtnSignatory");

			List<UserClient> fromClientList = new ArrayList<UserClient>();
			if (dtnSignatory != null) {
				if (!dtnSignatory.trim().equals("")) {
					UserCache userCache = CacheRepository.getInstance()
							.getCache(UserCache.class);

					for (String userLogin : dtnSignatory.split(",")) {
						UserClient uc = userCache.getObject(userLogin);

						if ((uc != null) && (uc.getIsActive()))
							fromClientList.add(uc);
					}
					from.getStore().add(fromClientList);
					from.setForceSelection(true);

					if (fromClientList.size() > 0)
						from.setValue(fromClientList.get(0));
					from.setSelection(fromClientList);
				}
			} else {
				from.setStore(new ListStore<UserClient>());
			}

			String toListVal = mapOfCarryOverData.get("toList");
			toList.setStringValue(toListVal);

			String notify_string = mapOfCarryOverData.get("notify");
			Boolean notify_bool = Boolean.parseBoolean(notify_string);

			notify.setValue(notify_bool);

			String remarksVal = mapOfCarryOverData.get(REMARKS);
			if ((remarksVal == null) || (remarksVal.trim().equals("")))
				remarksVal = "-";
			remarks.setValue(remarksVal);

			String subjectVal = mapOfCarryOverData.get(TRANSMITTAL_SUBJECT);
			Subject.setValue(subjectVal);

			String emailBodyVal = mapOfCarryOverData.get("emailBody");
			emailBody.setHTML(emailBodyVal);

			String stickinessJsonString = mapOfCarryOverData.get("stickiness");

			if ((stickinessJsonString != null)
					&& (!stickinessJsonString.trim().equals(""))) {
				HashMap<String, Integer> stickinessDegrees = getStickinessDegrees(stickinessJsonString);
				if (stickinessDegrees != null)
					stickinessMap.putAll(stickinessDegrees);
			}

			originalTrnPrefix = mapOfCarryOverData
					.get(TransmittalConstants.TRANSMITTAL_ID_PREFIX);

			String ccListVal = mapOfCarryOverData.get(CC_LIST);
			ccList.setStringValue(ccListVal);

			String accessToVal = mapOfCarryOverData.get(ACCESS_TO);

			if (accessToVal != null) {
				if (!accessToVal.trim().equals(""))
					accessTo.setStringValue(accessToVal);
				if (GXT.isIE)
					accessTo.setWidth(ccList.getWidth() - 12);
				accessTo.show();
			} else {
				accessTo.hide();
			}

			this.addExtendedFields(widget, formData,
					(ArrayList<TbitsModelData>) data.get(TRN_EXTENDED_FIELDS));

			ArrayList<TbitsModelData> tempValidatationList = (ArrayList<TbitsModelData>) data
					.get("validationRulesList");
			validationRulesList.addAll(tempValidatationList);
			String ruleResult = runValidationRules(validationRulesList,
					(HashMap<Integer, TbitsTreeRequestData>) getDataObject()
							.getData().get("mapOfRequests"));
			if ((ruleResult != null) && (!ruleResult.trim().isEmpty())) {
				TbitsInfo.info("Validation rule result for process: "
						+ currentTransmittalProcessId);
				validationResult.setText(ruleResult);
				return;

			}

		}

		else {

			final HashMap<String, Object> data = params.getData();
			final FormData formData = new FormData();
			formData.setWidth(700);
			validationResult = new Label();
			validationResult.setPosition(0, 0);
			validationResult.setStyleAttribute("position", "relative");
			validationResult.setStyleAttribute("color", "red");
			widget.add(validationResult);

			ListStore<TbitsModelData> ls1 = new ListStore<TbitsModelData>();
			ListStore<TbitsModelData> ls2 = new ListStore<TbitsModelData>();

			transmittalTypeCombo = new ComboBox<TbitsModelData>();
			transmittalTypeCombo.setStore(ls1);
			transmittalTypeCombo.setTriggerAction(TriggerAction.ALL);
			transmittalTypeCombo.setFieldLabel("Select a transmittal type");
			transmittalTypeCombo.setName("transmittalTypeCombo");
			transmittalTypeCombo.setId("transmittalTypeCombo");
			transmittalTypeCombo.setDisplayField("name");
			transmittalTypeCombo.setMinListWidth(350);
			widget.add(transmittalTypeCombo);

			fieldForTrnKey = new ComboBox<TbitsModelData>();
			fieldForTrnKey.setStore(ls2);
			fieldForTrnKey.hide();
			fieldForTrnKey.setTriggerAction(TriggerAction.ALL);
			fieldForTrnKey.setName("trnKeyCombo");
			fieldForTrnKey.setId("trnKeyCombo");
			fieldForTrnKey.setFieldLabel("Select a Dropdown");
			fieldForTrnKey.setDisplayField("display_name");
			fieldForTrnKey.setMinListWidth(350);
			widget.add(fieldForTrnKey);

			fieldForTrnKey
					.addSelectionChangedListener(new SelectionChangedListener<TbitsModelData>() {

						@SuppressWarnings("deprecation")
						@Override
						public void selectionChanged(
								SelectionChangedEvent<TbitsModelData> se) {
							if (se.getSelectedItem() != null) {
								TbitsModelData tmd = se.getSelectedItem();
								String value = tmd.get("value");
								if (!value.equals("")) {

									// jsonMap.put("-", "-");
									JSONValue parsedJson = JSONParser
											.parse(value);
									JSONArray jsonArray = parsedJson.isArray();
									if (jsonArray != null) {
										for (int i = 0; i < jsonArray.size(); i++) {
											JSONObject jsonObj = jsonArray.get(
													i).isObject();
											if (jsonObj != null) {
												String name = jsonObj.get(
														"name").isString()
														.stringValue();
												String data = jsonObj.get(
														"value").isString()
														.stringValue();

												if (name.contains("ccList")) {
													ccList.reset();
													ccList.setStringValue(data);
												} else if (name
														.contains("toList")) {
													toList.reset();
													toList.setStringValue(data);
												} else if (name
														.contains("from")) {

													from.clear();
													from.getStore().getModels()
															.clear();
													List<UserClient> fromClientList = new ArrayList<UserClient>();
													if (data != null) {
														if (!data.trim()
																.equals("")) {
															UserCache userCache = CacheRepository
																	.getInstance()
																	.getCache(
																			UserCache.class);
															// HashMap<String,
															// UserClient>
															// userMap =
															// userCache.getMap();
															for (String userLogin : data
																	.split(",")) {
																UserClient uc = userCache
																		.getObject(userLogin);

																if ((uc != null)
																		&& (uc
																				.getIsActive()))
																	fromClientList
																			.add(uc);
															}
															from
																	.getStore()
																	.add(
																			fromClientList);
															from
																	.setForceSelection(true);

															if (fromClientList
																	.size() > 0) {
																from
																		.setValue(fromClientList
																				.get(0));
															} else {
																from
																		.setValue(new UserClient());
															}

														}
													}

												}

											}
										}
									}

								}
							}
						}
					});

			transmittalTypeCombo.getStore().add(
					(List<TbitsModelData>) data.get("dropdownlist"));

			if (Boolean.valueOf((String) data.get("defaultProcessExists"))) {

				ArrayList<TbitsModelData> arr = (ArrayList<TbitsModelData>) data
						.get("dropdownlist");
				for (TbitsModelData tmd1 : arr) {
					if (tmd1.get("name").equals(
							(String) data.get("selected_dropdown_name"))) {
						transmittalTypeCombo.setValue(tmd1);
					}
				}
				TbitsModelData tmd = (TbitsModelData) data
						.get("transmittalProcessParams");

				if (tmd.get(ProcessParametersWizardPage.KEYFIELD) != null) {
					if (map.containsKey(Integer.parseInt((String) tmd
							.get(ProcessParametersWizardPage.KEYFIELD)))) {

						fieldForTrnKey.show();
						ArrayList<TbitsModelData> store = ((TbitsModelData) data
								.get("transmittalProcessParams"))
								.get(ProcessParametersWizardPage.KEYFIELD_LIST);

						dropDownKey = map
								.get(
										Integer
												.parseInt((String) ((TbitsModelData) data
														.get("transmittalProcessParams"))
														.get(KEYFIELD)))
								.getName();

						fieldForTrnKey
								.setFieldLabel("Select a "
										+ map
												.get(
														Integer
																.parseInt((String) ((TbitsModelData) data
																		.get("transmittalProcessParams"))
																		.get(KEYFIELD)))
												.getDisplayName());

						fieldForTrnKey.getStore().add(store);
					}
				} else {
					fieldForTrnKey.hide();
				}
			}

			transmittalTypeCombo
					.addSelectionChangedListener(new SelectionChangedListener<TbitsModelData>() {
						public void selectionChanged(
								SelectionChangedEvent<TbitsModelData> se) {

							originalTrnPrefix = "";
							currentTransmittalProcessId = 0;
							isCanContinue = true;
							stickinessMap.clear();
							actualDate.reset();
							actualNumber.reset();
							from.getStore().removeAll();
							notify.reset();
							ArrayList<TbitsModelData> extendedFields = (ArrayList<TbitsModelData>) getDataObject()
									.getData().get(TRN_EXTENDED_FIELDS);
							if (extendedFields != null) {
								for (TbitsModelData extField : extendedFields) {
									if (extField != null) {
										for (Field wField : widget.getFields()) {
											String fName = extField.get("name");
											if (wField != null) {
												String wName = wField.getName();
												if ((wName != null)
														&& wName
																.equalsIgnoreCase(fName)) {
													widget.remove(wField);
													break;
												}
											}
										}
									}
								}
							}
							widget.layout(true);

							// ----------get the trasmittal selected from the
							// dropdown---------------//
							TbitsModelData sMD = se.getSelectedItem();
							int ttId = Integer.parseInt((String) sMD
									.get("trnDropDownId"));
							int dcrSystemId = Integer.parseInt((String) sMD
									.get("dcrSystemId"));

							ArrayList<Integer> dcrRequestList = new ArrayList<Integer>();
							ArrayList<TbitsTreeRequestData> requestdata = (ArrayList<TbitsTreeRequestData>) data
									.get("TreeModeldataOfRequest");
							for (TbitsTreeRequestData trd : requestdata) {
								dcrRequestList.add(trd.getRequestId());
							}
							fetchTrnProcessParametersFromDB(formData, ttId,
									dcrSystemId, dcrRequestList);

						}

					});

			ListStore<UserClient> store = new ListStore<UserClient>();

			from = new ComboBox<UserClient>();
			from.setStore(store);
			from.setDisplayField(UserClient.USER_LOGIN);
			from.setFieldLabel("From");
			from.setName(DTN_SIGNATORY);
			widget.add(from, formData);

			toList = new UserPicker(
					(BAFieldMultiValue) TransmittalConstants.fieldCache
							.getObject(IFixedFields.ASSIGNEE));
			toList.setFieldLabel("To");
			toList.setName(TO_LIST);

			widget.add(toList, formData);

			ccList = new UserPicker(
					(BAFieldMultiValue) TransmittalConstants.fieldCache
							.getObject(IFixedFields.SUBSCRIBER));
			ccList.setFieldLabel("Cc");
			ccList.setName(CC_LIST);

			widget.add(ccList, formData);

			accessTo = new UserPicker(
					(BAFieldMultiValue) TransmittalConstants.fieldCache
							.getObject(IFixedFields.SUBSCRIBER));
			accessTo.setFieldLabel("Access To");
			accessTo.setName(ACCESS_TO);

			widget.add(accessTo, formData);

			notify = new CheckBox();
			notify.setFieldLabel("Notify");
			notify.setName(NOTIFY);
			notify.setBoxLabel("");
			notify.setValue(true);
			notify.setToolTip("Enable/disable email notification");
			widget.add(notify, formData);

			actualDate = new DateTimeControl();
			actualDate.setFieldLabel("Actual Date(of transmittal)");
			actualDate.setName(ACTUAL_DATE);
			actualDate.setEditable(true);
			actualDate.setFormat("dd MMM yyyy");
			actualDate.setMaxValue(new Date());
			actualDate.hide();
			widget.add(actualDate, formData);

			actualNumber = new TextField<String>();
			actualNumber.setFieldLabel("Actual DTN Number");
			actualNumber.setName(ACTUAL_NUMBER);
			actualNumber.hide();
			widget.add(actualNumber, formData);

			remarks = new TextArea();
			remarks.setFieldLabel("Comments");
			remarks.setName(REMARKS);
			widget.add(remarks, formData);

			Subject = new TextField<String>();
			Subject.setFieldLabel("Subject");
			Subject.setName(TRANSMITTAL_SUBJECT);
			widget.add(Subject, formData);

			LabelField labelField = new LabelField();
			labelField.setFieldLabel("Email Body:");
			widget.add(labelField, new FormData("-20"));

			CKConfig ckc = new CKConfig();
			ckc.setResizeMinHeight(100);
			ckc.setWidth("860px");
			ckc.setHeight("100px");
			ckc.setToolbar(CKConfig.PRESET_TOOLBAR.BASIC);
			emailBody = new CKEditor(ckc);

			widget.add(emailBody, formData);

			draftedBy = new TextField<String>();
			draftedBy.setFieldLabel("Drafted By");
			draftedBy.setName(DRAFTED_BY);
			draftedBy.hide();
			widget.add(draftedBy, formData);

			if (Boolean.valueOf(((String) data.get("StatusOfDtnDate")))) {
				isExistsInHistoryRole = true;
				actualDate.show();
				actualNumber.show();

			}

			/*
			 * If a default process exist then values of all of the fields will
			 * be set
			 */

			if (Boolean.valueOf((String) data.get("defaultProcessExists"))) {

				currentTransmittalProcessId = (Integer) data
						.get("trnProcessId");
				TbitsModelData tmd = (TbitsModelData) data
						.get("transmittalProcessParams");

				String dtnSignatory = tmd.get("dtnSignatory");

				List<UserClient> fromClientList = new ArrayList<UserClient>();
				if (dtnSignatory != null) {
					if (!dtnSignatory.trim().equals("")) {
						UserCache userCache = CacheRepository.getInstance()
								.getCache(UserCache.class);

						for (String userLogin : dtnSignatory.split(",")) {
							UserClient uc = userCache.getObject(userLogin);

							if ((uc != null) && (uc.getIsActive()))
								fromClientList.add(uc);
						}
						from.getStore().add(fromClientList);
						from.setForceSelection(true);

						if (fromClientList.size() > 0)
							from.setValue(fromClientList.get(0));
						from.setSelection(fromClientList);
					}
				} else {
					from.setStore(new ListStore<UserClient>());
				}

				String toListVal = tmd.get("toList");
				toList.setStringValue(toListVal);

				String remarksVal = tmd.get(REMARKS);
				if ((remarksVal == null) || (remarksVal.trim().equals("")))
					remarksVal = "-";
				remarks.setValue(remarksVal);

				String subjectVal = tmd.get(TRANSMITTAL_SUBJECT);
				Subject.setValue(subjectVal);

				String emailBodyVal = tmd.get("emailBody");
				emailBody.setHTML(emailBodyVal);

				String stickinessJsonString = tmd.get("stickiness");

				if ((stickinessJsonString != null)
						&& (!stickinessJsonString.trim().equals(""))) {
					HashMap<String, Integer> stickinessDegrees = getStickinessDegrees(stickinessJsonString);
					if (stickinessDegrees != null)
						stickinessMap.putAll(stickinessDegrees);
				}

				originalTrnPrefix = (String) tmd
						.get(TransmittalConstants.TRANSMITTAL_ID_PREFIX);

				String ccListVal = tmd.get(CC_LIST);
				ccList.setStringValue(ccListVal);

				String accessToVal = tmd.get(ACCESS_TO);

				if (accessToVal != null) {
					if (!accessToVal.trim().equals(""))
						accessTo.setStringValue(accessToVal);
					if (GXT.isIE)
						accessTo.setWidth(ccList.getWidth() - 12);
					accessTo.show();
				} else {
					accessTo.hide();
				}

				this.addExtendedFields(widget, formData,
						(ArrayList<TbitsModelData>) data
								.get(TRN_EXTENDED_FIELDS));

				/*
				 * Execution of Validation rules
				 */
				ArrayList<TbitsModelData> tempValidatationList = (ArrayList<TbitsModelData>) data
						.get("validationRulesList");
				validationRulesList.addAll(tempValidatationList);

				String ruleResult = runValidationRules(
						validationRulesList,
						(HashMap<Integer, TbitsTreeRequestData>) getDataObject()
								.getData().get("mapOfRequests"));
				if ((ruleResult != null) && (!ruleResult.trim().isEmpty())) {

					TbitsInfo.info("Validation rule result for process: "
							+ currentTransmittalProcessId);
					/*
					 * Window .alert(ruleResult + "\n Please make the changes "
					 * + "for the above mentioned fields and then transmit.");
					 * return;
					 */

					validationResult.setText(ruleResult);

				}
			}
		}
	}

	// @SuppressWarnings("unchecked")
	// public void buildPage(HashMap fetchedParameters) {
	//
	// final FormData formData = new FormData();
	// formData.setWidth(700);
	// HashMap<String, String> tempHashMapContaingTransientData =
	// (HashMap<String, String>) fetchedParameters
	// .get("HashMapForTransientData");
	//
	// ListStore<TbitsModelData> ls = new ListStore<TbitsModelData>();
	// TbitsModelData mData = new TbitsModelData();
	// mData.set("value", "-");
	// mData.set("name", "-");
	// ls.add(mData);
	//
	// @SuppressWarnings("unused")
	// int a = Integer.parseInt((tempHashMapContaingTransientData
	// .get("trnProcessId")));
	// currentTransmittalProcessId = a;
	// transmittalTypeCombo = new ComboBox<TbitsModelData>();
	// transmittalTypeCombo.setStore(ls);
	// transmittalTypeCombo.setTriggerAction(TriggerAction.ALL);
	// transmittalTypeCombo.setFieldLabel("Select a transmittal type");
	// transmittalTypeCombo.setName("transmittalTypeCombo");
	// transmittalTypeCombo.setId("transmittalTypeCombo");
	// transmittalTypeCombo.setDisplayField("name");
	// transmittalTypeCombo.setMinListWidth(350);
	// transmittalTypeCombo.getStore().add(
	// (List<TbitsModelData>) fetchedParameters.get("dropdownlist"));
	// ArrayList<TbitsModelData> arr = (ArrayList) fetchedParameters
	// .get("dropdownlist");
	// for (TbitsModelData tmd : arr) {
	// if (tmd.get("name").equals(
	// (String) fetchedParameters.get("selected_dropdown_name"))) {
	// transmittalTypeCombo.setValue(tmd);
	// }
	// }
	//
	// widget.add(transmittalTypeCombo);
	//
	// @SuppressWarnings("unused")
	// int currentSysId;
	// if (ClientUtils.getCurrentBA() != null)
	// currentSysId = ClientUtils.getCurrentBA().getSystemId();
	//
	// UserClient currentUser = ClientUtils.getCurrentUser();
	// BusinessAreaClient currentBA = ClientUtils.getCurrentBA();
	//
	// if ((currentUser != null) && (currentBA != null)) {
	// TransmittalConstants.dbService.checkUserExistsInRole(currentBA
	// .getSystemId(), currentUser.getUserId(),
	// new AsyncCallback<Boolean>() {
	//
	// public void onFailure(Throwable caught) {
	// caught.printStackTrace();
	//
	// }
	//
	// public void onSuccess(
	// Boolean isExistsInHistoryDataInputRole) {
	// if (isExistsInHistoryDataInputRole) {
	// isExistsInHistoryRole = true;
	// actualDate.show();
	// actualNumber.show();
	// }
	// }
	// });
	// }
	//
	// transmittalTypeCombo
	// .addSelectionChangedListener(new
	// SelectionChangedListener<TbitsModelData>() {
	// @SuppressWarnings("unchecked")
	// public void selectionChanged(
	// SelectionChangedEvent<TbitsModelData> se) {
	//
	// // Reset variables that change based on the transmittal
	// // type being selected
	// // from the transmittal type drop down in this page.
	// originalTrnPrefix = "";
	// currentTransmittalProcessId = 0;
	// isCanContinue = true;
	// stickinessMap.clear();
	// requestList = "";
	// actualDate.reset();
	// actualNumber.reset();
	// from.getStore().removeAll();
	// notify.reset();
	//
	// // Clear any extended fields.
	// for (TbitsModelData extField : twc
	// .getTrnwizardextendedfields()) {
	// if (extField != null) {
	// for (Field wField : widget.getFields()) {
	// String fName = extField.get("name");
	// if (wField != null) {
	// String wName = wField.getName();
	// if ((wName != null)
	// && wName
	// .equalsIgnoreCase(fName)) {
	// widget.remove(wField);
	// break;
	// }
	// }
	// }
	// }
	// }
	// widget.layout(true);
	// if (!twc.getTrnwizardextendedfields().isEmpty())
	// twc.getTrnwizardextendedfields().clear();
	//
	// // ----------get the trasmittal selected from the
	// // dropdown---------------//
	// TbitsModelData sMD = se.getSelectedItem();
	// twc.setEditableColumns(null);
	// twc.setEditableAttchmentColumns(null);
	// twc.setTrnDistributionData(null);
	// String value = (String) sMD.get("value");
	// if (value != null) {
	// if (value.toString().trim().equals("-"))
	// return;
	// }
	// int ttId = Integer.parseInt((String) sMD
	// .get("trnProcessId"));
	// int dcrSystemId = Integer.parseInt((String) sMD
	// .get("dcrSystemId"));
	//
	// ArrayList<Integer> dcrRequestList = new ArrayList<Integer>();
	//
	// for (TbitsTreeRequestData trd : twc.getRequestList()) {
	// dcrRequestList.add(trd.getRequestId());
	// requestList = (requestList.equals("")) ? String
	// .valueOf(trd.getRequestId()) : requestList
	// + "," + (Integer) trd.getRequestId();
	// }
	//					
	// fetchTrnProcessParametersFromDB(formData, ttId,
	// dcrSystemId, dcrRequestList);
	//
	// }
	// });
	//
	// ListStore<UserClient> store = new ListStore<UserClient>();
	// String dtnSignatory = tempHashMapContaingTransientData
	// .get("dtnSignatory");
	// from = new ComboBox<UserClient>();
	// from.setStore(store);
	// from.setDisplayField(UserClient.USER_LOGIN);
	// from.setFieldLabel("From");
	// from.setName(DTN_SIGNATORY);
	// widget.add(from, formData);
	// List<UserClient> fromClientList = new ArrayList<UserClient>();
	// // if(dtnSignatory != null)
	// // {
	// //
	// // UserCache userCache =
	// // CacheRepository.getInstance().getCache(UserCache.class);
	// // // HashMap<String, UserClient> userMap = userCache.getMap();
	// // UserClient uc = userCache.getObject(dtnSignatory);
	// //
	// // fromClientList.add(uc);
	// // //
	// // from.getStore().add(fromClientList);
	// // from.setForceSelection(true);
	// // from.setSelection(fromClientList);
	// // if (fromClientList.size() > 0)
	// // from.setValue(fromClientList.get(0));
	// //
	// // }
	//
	// if (dtnSignatory != null) {
	// if (!dtnSignatory.trim().equals("")) {
	// UserCache userCache = CacheRepository.getInstance().getCache(
	// UserCache.class);
	//
	// for (String userLogin : dtnSignatory.split(",")) {
	// UserClient uc = userCache.getObject(userLogin);
	//
	// if ((uc != null) && (uc.getIsActive()))
	// fromClientList.add(uc);
	// }
	// from.getStore().add(fromClientList);
	// from.setForceSelection(true);
	//
	// if (fromClientList.size() > 0)
	// from.setValue(fromClientList.get(0));
	// from.setSelection(fromClientList);
	// }
	// } else {
	// from.setStore(new ListStore<UserClient>());
	// }
	// toList = new UserPicker(
	// (BAFieldMultiValue) TransmittalConstants.fieldCache
	// .getObject(IFixedFields.ASSIGNEE));
	// toList.setFieldLabel("To");
	// toList.setName(TO_LIST);
	// String toListVal = tempHashMapContaingTransientData.get("toList");
	// toList.setStringValue(toListVal);
	// widget.add(toList, formData);
	//
	// ccList = new UserPicker(
	// (BAFieldMultiValue) TransmittalConstants.fieldCache
	// .getObject(IFixedFields.SUBSCRIBER));
	// ccList.setFieldLabel("Cc");
	// ccList.setName(CC_LIST);
	// String ccListVal = tempHashMapContaingTransientData.get(CC_LIST);
	// ccList.setStringValue(ccListVal);
	// widget.add(ccList, formData);
	//
	// accessTo = new UserPicker(
	// (BAFieldMultiValue) TransmittalConstants.fieldCache
	// .getObject(IFixedFields.SUBSCRIBER));
	// accessTo.setFieldLabel("Access To");
	// accessTo.setName(ACCESS_TO);
	// String accessToVal = tempHashMapContaingTransientData.get(ACCESS_TO);
	//
	// if (accessToVal != null) {
	// if (!accessToVal.trim().equals(""))
	// accessTo.setStringValue(accessToVal);
	// if (GXT.isIE)
	// accessTo.setWidth(ccList.getWidth() - 12);
	// accessTo.show();
	// } else {
	// accessTo.hide();
	// }
	//
	// widget.add(accessTo, formData);
	//
	// String notify_string = tempHashMapContaingTransientData.get("notify");
	// Boolean notify_bool = Boolean.parseBoolean(notify_string);
	// notify = new CheckBox();
	// notify.setFieldLabel("Notify");
	// notify.setValue(notify_bool);
	// notify.setName(NOTIFY);
	// notify.setBoxLabel("");
	// notify.setToolTip("Enable/disable email notification");
	// widget.add(notify, formData);
	//
	// actualDate = new DateTimeControl();
	// actualDate.setFieldLabel("Actual Date(of transmittal)");
	// actualDate.setName(ACTUAL_DATE);
	// actualDate.setEditable(true);
	// actualDate.setFormat("dd MMM yyyy");
	// actualDate.setMaxValue(new Date());
	// actualDate.hide();
	// widget.add(actualDate, formData);
	//
	// actualNumber = new TextField<String>();
	// actualNumber.setFieldLabel("Actual DTN Number");
	// actualNumber.setName(ACTUAL_NUMBER);
	// actualNumber.hide();
	// widget.add(actualNumber, formData);
	//
	// remarks = new TextArea();
	// remarks.setFieldLabel("Comments");
	// remarks.setName(REMARKS);
	// String remarksVal = tempHashMapContaingTransientData.get(REMARKS);
	// if ((remarksVal == null) || (remarksVal.trim().equals("")))
	// remarksVal = "-";
	// remarks.setValue(remarksVal);
	//
	// widget.add(remarks, formData);
	//
	// Subject = new TextField<String>();
	//
	// Subject.setFieldLabel("Subject");
	// Subject.setName(TRANSMITTAL_SUBJECT);
	// String subjectVal = tempHashMapContaingTransientData
	// .get(TRANSMITTAL_SUBJECT);
	// Subject.setValue(subjectVal);
	// widget.add(Subject, formData);
	//
	// LabelField labelField = new LabelField();
	// labelField.setFieldLabel("Email Body:");
	// widget.add(labelField, new FormData("-20"));
	//
	// CKConfig ckc = new CKConfig();
	// ckc.setResizeMinHeight(100);
	// ckc.setWidth("860px");
	// ckc.setHeight("100px");
	// ckc.setToolbar(CKConfig.PRESET_TOOLBAR.BASIC);
	// emailBody = new CKEditor(ckc);
	// String emailBodyVal = tempHashMapContaingTransientData.get("emailBody");
	// emailBody.setHTML(emailBodyVal);
	// widget.add(emailBody, formData);
	//
	// draftedBy = new TextField<String>();
	// draftedBy.setFieldLabel("Drafted By");
	// draftedBy.setName(DRAFTED_BY);
	// draftedBy.hide();
	// widget.add(draftedBy, formData);
	//
	// fetchValidationRules();
	// fetchTrnAttachmentTableColumnsFromDB();
	// fetchDistributionTableColumnsFromDB();
	// fetchTrnWizardExtendedFieldsFromDB(formData);
	// String stickinessJsonString = tempHashMapContaingTransientData
	// .get("stickiness");
	//
	// if ((stickinessJsonString != null)
	// && (!stickinessJsonString.trim().equals(""))) {
	// HashMap<String, Integer> stickinessDegrees =
	// getStickinessDegrees(stickinessJsonString);
	// if (stickinessDegrees != null)
	// stickinessMap.putAll(stickinessDegrees);
	// }
	// ArrayList<Integer> dcrRequestList_for_default_values = new
	// ArrayList<Integer>();
	// for (TbitsTreeRequestData trd : twc.getRequestList()) {
	// dcrRequestList_for_default_values.add(trd.getRequestId());
	// requestList = (requestList.equals("")) ? String.valueOf(trd
	// .getRequestId()) : requestList + ","
	// + (Integer) trd.getRequestId();
	// }
	//
	// String system_id = (String) fetchedParameters.get("sysid");
	// String dropdownid = (String) fetchedParameters
	// .get("selected_dropdown_id");
	// int selected_dropdown_id = Integer.parseInt(dropdownid);
	// int sysid = Integer.parseInt(system_id);
	//
	// originalTrnPrefix = (String) tempHashMapContaingTransientData
	// .get(TransmittalConstants.TRANSMITTAL_ID_PREFIX);
	//
	// fetchTrnProcessParametersFromDB(formData, selected_dropdown_id, sysid,
	// dcrRequestList_for_default_values);
	//
	// }

	void buildGridForPage3() {
		this.nextPage.buildPage(dataObject);
	}

	@SuppressWarnings("unchecked")
	public JSONArray getDataForPage3() {
		JSONArray tableJson = null;
		UserCache userCache = CacheRepository.getInstance().getCache(
				UserCache.class);
		ArrayList<TbitsModelData> distributionListModelData = new ArrayList<TbitsModelData>();

		ArrayList<TbitsModelData> DistributionTableColumnsList = (ArrayList<TbitsModelData>) getDataObject()
				.getData().get("distributionTableColumnsList");
		/*
		 * Comparator<TbitsModelData> comp = new Comparator<TbitsModelData>() {
		 * public int compare(TbitsModelData o1, TbitsModelData o2) { if ((o1 !=
		 * null) && (o2 != null)) { int s1 = (Integer) o1
		 * .get(TransmittalConstants.COLUMN_ORDER); int s2 = (Integer) o2
		 * .get(TransmittalConstants.COLUMN_ORDER); if (s1 > s2) return 1; else
		 * if (s1 == s2) return 0; else if (s1 < s2) return -1; } return 0; } };
		 * 
		 * Collections.sort(DistributionTableColumnsList, comp);
		 */

		String cclist = (String) getDataObject().getData().get("ccList");

		HashSet<String> userSet = new HashSet<String>();
		for (String userLogin : cclist.trim().split(",")) {
			if (!userLogin.trim().equals("")) {
				userSet.add(userLogin);
			}
		}
		for (String userLogin1 : userSet) {
			if (!userLogin1.trim().equals("")) {
				UserClient uc = userCache.getObject(userLogin1);// getUserBasedOnLogin(userMap,
				// userLogin);
				if (uc != null) {
					TbitsModelData tempData2 = new TbitsModelData();
					tempData2.set("login", userLogin1);
					for (TbitsModelData distColumnTmd : DistributionTableColumnsList) {
						String property = (String) distColumnTmd.get("name");
						if (property != null) {
							// Check if its a user property if not,
							// fetch
							// default values from db.
							String value = (String) uc.get(property);
							if ((value != null) && (!value.trim().equals("")))
								tempData2.set(property, value);
							else {
								String tempValue = distColumnTmd
										.get(TransmittalConstants.FIELD_CONFIG);
								if ((Integer) distColumnTmd.get("data_type_id") != 9) {
									if ((tempValue != null)
											&& (!tempValue.trim().equals("")))
										tempData2.set(property, tempValue);
									else
										tempData2.set(property, "-");
								} else {
									HashMap<String, String> typesMap = new HashMap<String, String>();
									DistributionTableColumnsConfig
											.fetchKeyValuePairsfromJsonString(
													typesMap, tempValue);
									tempValue = typesMap.keySet().iterator()
											.next();
									if (tempValue != null)
										tempData2.set(property, tempValue);
									else
										tempData2.set(property, "-");
								}
							}
						}
					}

					distributionListModelData.add(tempData2);
				}
			}
		}

		ArrayList<String> propertyList = new ArrayList<String>();

		for (TbitsModelData md : DistributionTableColumnsList) {
			propertyList.add((String) md.get("name"));
		}

		try {
			tableJson = getDistributionListJsonString(
					distributionListModelData, propertyList);

		} catch (JSONException je) {
			Window.alert(je.getMessage());
		}
		return tableJson;
	}

	@SuppressWarnings("unchecked")
	public JSONArray getDataForPage3InApprovalCycle(String newlyAddedCC) {
		JSONArray tableJson = null;
		UserCache userCache = CacheRepository.getInstance().getCache(
				UserCache.class);
		ArrayList<TbitsModelData> distributionListModelData = new ArrayList<TbitsModelData>();

		ArrayList<TbitsModelData> DistributionTableColumnsList = (ArrayList<TbitsModelData>) getDataObject()
				.getData().get("distributionTableColumnsList");

		/*
		 * Comparator<TbitsModelData> comp = new Comparator<TbitsModelData>() {
		 * public int compare(TbitsModelData o1, TbitsModelData o2) { if ((o1 !=
		 * null) && (o2 != null)) { int s1 = (Integer) o1
		 * .get(TransmittalConstants.COLUMN_ORDER); int s2 = (Integer) o2
		 * .get(TransmittalConstants.COLUMN_ORDER); if (s1 > s2) return 1; else
		 * if (s1 == s2) return 0; else if (s1 < s2) return -1; } return 0; } };
		 * // Sort the column info, before creating column configs out of them.
		 * So, // that they maintain the sort order and // hence the column
		 * order in the table. Collections.sort(DistributionTableColumnsList,
		 * comp);
		 */

		String cclist = newlyAddedCC;

		HashSet<String> userSet = new HashSet<String>();
		for (String userLogin : cclist.trim().split(",")) {
			if (!userLogin.trim().equals("")) {
				userSet.add(userLogin);
			}
		}
		for (String userLogin1 : userSet) {
			if (!userLogin1.trim().equals("")) {
				UserClient uc = userCache.getObject(userLogin1);// getUserBasedOnLogin(userMap,
				// userLogin);
				if (uc != null) {
					TbitsModelData tempData2 = new TbitsModelData();
					for (TbitsModelData distColumnTmd : DistributionTableColumnsList) {
						String property = (String) distColumnTmd.get("name");
						if (property != null) {
							// Check if its a user property if not,
							// fetch
							// default values from db.
							String value = (String) uc.get(property);
							if ((value != null) && (!value.trim().equals("")))
								tempData2.set(property, value);
							else {
								String tempValue = distColumnTmd
										.get(TransmittalConstants.FIELD_CONFIG);
								if ((Integer) distColumnTmd.get("data_type_id") != 9) {
									if ((tempValue != null)
											&& (!tempValue.trim().equals("")))
										tempData2.set(property, tempValue);
									else
										tempData2.set(property, "-");
								} else {
									HashMap<String, String> typesMap = new HashMap<String, String>();
									DistributionTableColumnsConfig
											.fetchKeyValuePairsfromJsonString(
													typesMap, tempValue);
									tempValue = typesMap.keySet().iterator()
											.next();
									if (tempValue != null)
										tempData2.set(property, tempValue);
									else
										tempData2.set(property, "-");
								}
							}
						}
					}

					distributionListModelData.add(tempData2);
				}
			}
		}

		ArrayList<String> propertyList = new ArrayList<String>();

		for (TbitsModelData md : DistributionTableColumnsList) {
			propertyList.add((String) md.get("name"));
		}

		try {
			tableJson = getDistributionListJsonString(
					distributionListModelData, propertyList);

		} catch (JSONException je) {
			Window.alert(je.getMessage());
		}
		return tableJson;
	}

	private JSONArray getDistributionListJsonString(
			List<TbitsModelData> models, ArrayList<String> fieldNames)
			throws JSONException {

		JSONArray tableJson = new JSONArray();
		int count = 0;
		for (TbitsModelData model : models) {
			JSONArray drawingListValues = new JSONArray();

			int i = 0;
			for (; i < fieldNames.size(); i++) {
				String fValue = String.valueOf(model.get(fieldNames.get(i)));
				String prop = fieldNames.get(i);
				fValue = prop + "," + fValue;
				drawingListValues.set(i + 1, new JSONString(fValue));
			}
			String fValue = String.valueOf(model.get("login"));
			String prop = "login";
			fValue = prop + "," + fValue;
			drawingListValues.set(i + 1, new JSONString(fValue));

			tableJson.set(count, drawingListValues);
			count++;
		}
		return tableJson;

	}

	@SuppressWarnings("unchecked")
	HashMap<String, String> getDataForPage2() {
		HashMap<String, String> valuesMap = new HashMap<String, String>();

		// ArrayList<TbitsTreeRequestData> models =
		// (ArrayList<TbitsTreeRequestData>)getDataObject().getData().get("TreeModeldataOfRequest");

		HashMap<Integer, TbitsTreeRequestData> requestMap = (HashMap<Integer, TbitsTreeRequestData>) getDataObject()
				.getData().get("mapOfRequests");

		ArrayList<TbitsTreeRequestData> models = new ArrayList<TbitsTreeRequestData>();
		for (TbitsTreeRequestData trd : requestMap.values()) {
			models.add(trd);
		}

		BAField baField;

		ArrayList<TbitsModelData> listOfColumns = (ArrayList<TbitsModelData>) getDataObject()
				.getData().get("attachmentTableColumnList");

		/*
		 * Comparator<TbitsModelData> comp = new Comparator<TbitsModelData>() {
		 * public int compare(TbitsModelData o1, TbitsModelData o2) { if ((o1 !=
		 * null) && (o2 != null)) { int s1 = (Integer) o1
		 * .get(TransmittalConstants.COLUMN_ORDER); int s2 = (Integer) o2
		 * .get(TransmittalConstants.COLUMN_ORDER); if (s1 > s2) return 1; else
		 * if (s1 == s2) return 0; else if (s1 < s2) return -1; } return 0; } };
		 * // Sort the column info, before creating column configs out of them.
		 * So, // that they maintain the sort order and // hence the column
		 * order in the table. Collections.sort(listOfColumns, comp);
		 */
		ArrayList<BAField> baFields = (ArrayList<BAField>) getDataObject()
				.getData().get("BAFields");
		ArrayList<String> baFieldNames = new ArrayList<String>();

		for (TbitsModelData md : listOfColumns) {
			int dataTypeId = (Integer) md
					.get(TransmittalConstants.DATA_TYPE_ID_COLUMN);

			if (dataTypeId != TransmittalConstants.ATTACHMENTS) {
				Integer fieldId = (Integer) md
						.get(TransmittalConstants.FIELD_ID_COLUMN);
				if (fieldId > 0) {

					baField = Utils.getBAFieldById(fieldId, baFields);

					if (baField != null) {
						baFieldNames.add(baField.getName());
					}
				}
			}
		}

		ArrayList<String> DeliverableFieldsNames = new ArrayList<String>();

		String deliverableAttachmentProperties = "";
		for (TbitsModelData md : listOfColumns) {
			if (((Integer) md.get("data_type_id")) == TransmittalConstants.ATTACHMENTS
					&& ((Boolean) md.get(TransmittalConstants.IS_ACTIVE_COLUMN))) {

				Integer fieldId = (Integer) md
						.get(TransmittalConstants.FIELD_ID_COLUMN);
				if (fieldId > 0) {

					baField = Utils.getBAFieldById(fieldId, baFields);

					DeliverableFieldsNames.add(baField.getName());
					deliverableAttachmentProperties = ((deliverableAttachmentProperties
							.equals("")) ? (String) baField.get("name")
							: deliverableAttachmentProperties + ","
									+ (String) baField.get("name"));
				}
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

	@SuppressWarnings("unchecked")
	public void updateRequestsAttachmentsInApprovalCycle(ArrayList<String> arr) {

		HashMap<Integer, TbitsTreeRequestData> requests = (HashMap<Integer, TbitsTreeRequestData>) getDataObject()
				.getData().get("mapOfRequests");

		for (TbitsTreeRequestData trd : requests.values()) {

			for (String attFieldName : arr) {
				POJO obj = trd.getAsPOJO(attFieldName);
				if (obj == null || !(obj instanceof POJOAttachment)) {

					obj = new POJOAttachment(new ArrayList<FileClient>());
				}

				List<FileClient> delAttachments = (List<FileClient>) obj
						.getValue();
				List<FileClient> freshAttachments = new ArrayList<FileClient>();
				if (delAttachments.size() == 0) {
					continue;
				} else {
					for (FileClient fc : delAttachments) {
						if (Boolean.valueOf((String) fc
								.get("IS_CHECKED_IN_TRANSIENT_DATA"))) {
							freshAttachments.add(fc);
						}

					}
				}

				trd.set(attFieldName, freshAttachments);
			}
		}

	}

	/*
	 * private JSONArray getDrawingListsJsonString( List<TbitsTreeRequestData>
	 * models, ArrayList<String> baFieldNames) { JSONArray tableJson = new
	 * JSONArray(); int count = 0; ArrayList<BAField> baFields =
	 * (ArrayList<BAField>) getDataObject() .getData().get("BAFields");
	 * 
	 * for (TbitsTreeRequestData model : models) { int reqId =
	 * model.getRequestId(); TbitsTreeRequestData trd = ((HashMap<Integer,
	 * TbitsTreeRequestData>) getDataObject()
	 * .getData().get("mapOfRequests")).get(reqId); if (trd != null) {
	 * 
	 * JSONArray drawingListValues = new JSONArray(); for (int i = 0; i <
	 * baFieldNames.size(); i++) { String fValue = String
	 * .valueOf(trd.get(baFieldNames.get(i)));
	 * 
	 * if (fValue.equals("") || fValue == null) {
	 * 
	 * } BAField baField = Utils.getBAFieldByname(baFieldNames .get(i),
	 * baFields); String field_id = baField.getFieldId() + ""; if (baField
	 * instanceof BAFieldCombo) { fValue = getTypeValueBasedOnConfig(
	 * (BAFieldCombo) baField, fValue); }
	 * 
	 * drawingListValues.set(i + 1, new JSONString(field_id + "," + fValue)); }
	 * drawingListValues.set(0, new JSONString(model.getRequestId() + ""));
	 * tableJson.set(count, drawingListValues); count++; } } return tableJson; }
	 */

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
				/*
				 * if (typeValueSrc < 2) return fValue;
				 */
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
	private JSONArray getSelectedAttachmentslist() {

		JSONArray attachmentModel = new JSONArray();
		int count = 0;

		HashMap<Integer, TbitsTreeRequestData> map = (HashMap<Integer, TbitsTreeRequestData>) getDataObject()
				.getData().get("mapOfRequests");
		ArrayList<Integer> arr = new ArrayList<Integer>();
		for (Integer i : map.keySet()) {
			arr.add(i);
		}

		for (Integer requestId : arr) {
			TbitsTreeRequestData trd = map.get(requestId);

			JSONArray temp = setJsonFieldValue1(trd);
			attachmentModel.set(count, temp);
			count++;

		}
		return attachmentModel;
	}

	@SuppressWarnings("unchecked")
	private JSONArray setJsonFieldValue1(TbitsTreeRequestData trd) {
		BAField baField;
		JSONArray finalArr = new JSONArray();
		JSONArray tempArr1 = new JSONArray();
		JSONArray tempArr = new JSONArray();
		JSONArray tempArr2 = new JSONArray();
		finalArr.set(0, new JSONString(trd.getRequestId() + ""));
		finalArr.set(1, new JSONString((String) trd.get("subject")));
		int count1 = 2;
		ArrayList<BAField> baFields = (ArrayList<BAField>) getDataObject()
				.getData().get("BAFields");
		ArrayList<TbitsModelData> arr = (ArrayList<TbitsModelData>) getDataObject()
				.getData().get("attachmentTableColumnList");
		if (arr != null) {
			for (TbitsModelData tmd : arr) {
				boolean isEditable = (Boolean) tmd.get("is_editable");
				Integer fieldId = (Integer) tmd
						.get(TransmittalConstants.FIELD_ID_COLUMN);
				if (fieldId > 0) {
					baField = Utils.getBAFieldById(fieldId, baFields);

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

	@SuppressWarnings("unchecked")
	protected String getTypeValueBasedOnConfig(BAFieldCombo baField,
			String currentTypeName) {
		String fValue = currentTypeName;
		int typeValueSrc = 0;
		ArrayList<TbitsModelData> arr = (ArrayList<TbitsModelData>) getDataObject()
				.getData().get("attachmentTableColumnList");
		for (TbitsModelData md : arr) {
			Integer fieldId = (Integer) md
					.get(TransmittalConstants.FIELD_ID_COLUMN);
			if (fieldId == baField.getFieldId()) {
				typeValueSrc = (Integer) md
						.get(TransmittalConstants.TYPE_VALUE_SOURCE_COLUMN);
				/*
				 * if (typeValueSrc < 2) return fValue;
				 */
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

	@Override
	public boolean funcToBeCalledOnBack() {
		return true;
	}

	@Override
	public HashMap<String, String> getValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public void execute(WizardData data) {

		HashMap<Integer, TbitsTreeRequestData> requests = (HashMap<Integer, TbitsTreeRequestData>) data
				.getData().get("mapOfRequests");
		ArrayList<AttachmentModel> attchementList = (ArrayList<AttachmentModel>) data
				.getData().get("ConvertedAttachmentTable");

		List<FileClient> newAttachemnts = null;
		String errorString = "";

		for (TbitsTreeRequestData trd : requests.values()) {
			if (attchementList.size() > 0) {
				for (AttachmentModel eachRow : attchementList) {
					if (((Integer) trd.getRequestId()).toString().equals(
							eachRow.getREQUEST_ID())) {
						HashMap<String, List<Attachmentinfo>> hm = eachRow
								.getAttachmentDetails();
						for (String eachProperty : hm.keySet()) {
							POJO obj = trd.getAsPOJO(eachProperty);
							if (obj == null || !(obj instanceof POJOAttachment)) {
								obj = new POJOAttachment(
										new ArrayList<FileClient>());
							}
							List<FileClient> delAttachments = (List<FileClient>) obj
									.getValue();

							List<Attachmentinfo> attachments = hm
									.get(eachProperty);
							newAttachemnts = new ArrayList<FileClient>();
							for (Attachmentinfo attachment : attachments) {
								String filename = attachment.getFILE_NAME();
								int requestFileId = attachment
										.getREQUEST_FILE_ID();

								Boolean isDocumentPresent = false;
								for (FileClient eachAttachment : delAttachments) {

									if ((eachAttachment.getRequestFileId() == requestFileId)
											&& (eachAttachment.getFileName()
													.equals(filename))) {
										eachAttachment.set(
												"IS_CHECKED_IN_TRANSIENT_DATA",
												String.valueOf(true));
										isDocumentPresent = true;
										break;

									}

								}
								if (!isDocumentPresent) {
									errorString = errorString
											+ filename
											+ "Document is not present anymore for Request having requestId "
											+ trd.getRequestId() + "" + "\n";
									// Window.alert("One or more selected documents  are not present anymore");

								}
							}
							for (FileClient eachAttachment : delAttachments) {
								if (eachAttachment
										.get("IS_CHECKED_IN_TRANSIENT_DATA") == null) {
									eachAttachment.set(
											"IS_CHECKED_IN_TRANSIENT_DATA",
											String.valueOf(false));

								}
							}
							newAttachemnts.addAll(delAttachments);
							trd.set(eachProperty, newAttachemnts);

						}

					}
				}
			}
		}

		if (!errorString.equals(""))
			Window.alert(errorString);

		BAField baField;

		ArrayList<TbitsModelData> listOfColumns = (ArrayList<TbitsModelData>) getDataObject()
				.getData().get("attachmentTableColumnList");

		/*
		 * Comparator<TbitsModelData> comp = new Comparator<TbitsModelData>() {
		 * public int compare(TbitsModelData o1, TbitsModelData o2) { if ((o1 !=
		 * null) && (o2 != null)) { int s1 = (Integer) o1
		 * .get(TransmittalConstants.COLUMN_ORDER); int s2 = (Integer) o2
		 * .get(TransmittalConstants.COLUMN_ORDER); if (s1 > s2) return 1; else
		 * if (s1 == s2) return 0; else if (s1 < s2) return -1; } return 0; } };
		 * // Sort the column info, before creating column configs out of them.
		 * So, // that they maintain the sort order and // hence the column
		 * order in the table. Collections.sort(listOfColumns, comp);
		 */
		ArrayList<BAField> baFields = (ArrayList<BAField>) getDataObject()
				.getData().get("BAFields");
		ArrayList<String> baFieldNames = new ArrayList<String>();

		for (TbitsModelData md : listOfColumns) {
			int dataTypeId = (Integer) md
					.get(TransmittalConstants.DATA_TYPE_ID_COLUMN);
			if (dataTypeId != TransmittalConstants.ATTACHMENTS) {
				Integer fieldId = (Integer) md
						.get(TransmittalConstants.FIELD_ID_COLUMN);
				if (fieldId > 0) {

					baField = Utils.getBAFieldById(fieldId, baFields);

					if (baField != null) {
						baFieldNames.add(baField.getName());
					}
				}
			}
		}

		ArrayList<String> DeliverableFieldsNames = new ArrayList<String>();

		String deliverableAttachmentProperties = "";
		for (TbitsModelData md : listOfColumns) {
			if (((Integer) md.get("data_type_id")) == TransmittalConstants.ATTACHMENTS
					&& ((Boolean) md.get(TransmittalConstants.IS_ACTIVE_COLUMN))) {

				Integer fieldId = (Integer) md
						.get(TransmittalConstants.FIELD_ID_COLUMN);
				if (fieldId > 0) {

					baField = Utils.getBAFieldById(fieldId, baFields);

					DeliverableFieldsNames.add(baField.getName());
					deliverableAttachmentProperties = ((deliverableAttachmentProperties
							.equals("")) ? (String) baField.get("name")
							: deliverableAttachmentProperties + ","
									+ (String) baField.get("name"));
				}
			}
		}

		updateRequestsAttachmentsInApprovalCycle(DeliverableFieldsNames);

	}

}
