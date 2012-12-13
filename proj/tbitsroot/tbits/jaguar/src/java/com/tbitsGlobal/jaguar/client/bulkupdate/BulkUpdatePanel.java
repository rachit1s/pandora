package com.tbitsGlobal.jaguar.client.bulkupdate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.cache.UserCache;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.bulkupdate.AbstractBulkUpdatePanel;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract;
import commons.com.tbitsGlobal.utils.client.bulkupdate.IBulkUpdateConstants;
import commons.com.tbitsGlobal.utils.client.bulkupdate.BulkUpdateGridAbstract.BulkGridMode;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;
import commons.com.tbitsGlobal.utils.client.pojo.POJODate;
import commons.com.tbitsGlobal.utils.client.widgets.ToolBarButton;
import commons.com.tbitsGlobal.utils.client.widgets.UserPicker;

/**
 * The panel that wraps the Bulk Update grids.
 * 
 * @author sourabh
 * 
 */
public class BulkUpdatePanel extends
		AbstractBulkUpdatePanel<TbitsTreeRequestData> {
	public static int SHOW_SUBMIT = 1;
	public static int SHOW_RESTORE = SHOW_SUBMIT << 1;

	private int config;
	public static int DEFAULT_CONFIG = SHOW_SUBMIT | SHOW_RESTORE;

	private String sysPrefix;

	public static final String SIZE = "size";

	public static final String REPO_ID = "repo_id";

	public static final String ATT_NAME = "Att_Name";

	public static final String REQ_ID = "reqId";

	public static final int DATE = 2;
	public static final int BOOLEAN = 1;
	public static final int TIME = 3;
	public static final int TEXT = 8;
	public static final int STRING = 7;
	public static final int REAL = 6;
	public static final int INT = 5;
	public static final int DATETIME = 4;
	public static final int TYPE = 9;
	public static final int USERTYPE = 10;
	public static final int ATTACHMENTS = 11;

	/**
	 * Constructor
	 */
	private BulkUpdatePanel(String sysPrefix) {
		super();

		this.sysPrefix = sysPrefix;

		this.config = DEFAULT_CONFIG;
		isExcelImportSupported = true;

		BulkUpdateConstants.models = new ListStore<TbitsTreeRequestData>();

		/**
		 * Add Individual grid to the center.
		 */
		DefaultUIContext context = new DefaultUIContext();
		context.setValue(BulkUpdateGrid.CONTEXT_BULK_UPDATE_PANEL, this);
		BulkUpdateGrid bulkGrid = new BulkUpdateGrid(sysPrefix, context,
				BulkGridMode.SINGLE);
		singleGridContainer = new SingleGridContainer(bulkGrid);

		/**
		 * Add Common Grid to the south.
		 */
		context = new DefaultUIContext();
		context.setValue(BulkUpdateGrid.CONTEXT_SINGLE_GRID_CONTAINER,
				singleGridContainer);
		bulkGrid = new BulkUpdateGrid(sysPrefix, context, BulkGridMode.COMMON);
		commonGridContainer = new CommonGridContainer(context, bulkGrid);
	}

	public BulkUpdatePanel(String sysPrefix, int config) {
		this(sysPrefix);

		this.config = config;
	}

	/**
	 * Constructor.
	 * 
	 * @param models
	 *            . The list of {@link TbitsTreeRequestData} to initiate the
	 *            grid.
	 * @param users
	 *            . Users to be displayed in {@link UserPicker}
	 */
	public BulkUpdatePanel(String sysPrefix, List<TbitsTreeRequestData> models,
			int config) {
		this(sysPrefix, config);

		if (models != null && models.size() > 0)
			this.singleGridContainer.addModel(models);
	}

	@Override
	protected void beforeRender() {
		super.beforeRender();

		this.createButtons();
	}

	/**
	 * Creates buttons add the bottom
	 */
	private void createButtons() {
		if ((this.config & SHOW_RESTORE) != 0) {
			/*
			 * Restores the original data to the individual grid.
			 */
			this.addButton(new Button("Restore Original",
					new SelectionListener<ButtonEvent>() {
						public void componentSelected(ButtonEvent ce) {
							singleGridContainer.removeAllModels();
							singleGridContainer
									.addModel(BulkUpdateConstants.models
											.getModels());
						}
					}));
		}

		if ((this.config & SHOW_SUBMIT) != 0) {
			this.addButton(new Button("Submit",
					new SelectionListener<ButtonEvent>() {
						@Override
						public void componentSelected(final ButtonEvent ce) {
							if (!isBusyUploading()) {
								ce.getButton().disable();
								final MessageBox message = MessageBox.wait(
										"Please Wait...",
										"Bulk Update is in progress",
										"Please Wait...");
								List<TbitsTreeRequestData> models = singleGridContainer
										.getModels();

								int count = 1;
								for (TbitsTreeRequestData model : models) {
									model.set(IBulkUpdateConstants.ROW_NUMBER,
											count);
									model.removeError();
									count++;
								}
								List<TbitsTreeRequestData> requestTree = ClientUtils
										.listToRequestTree(
												models,
												IBulkUpdateConstants.ROW_NUMBER,
												IBulkUpdateConstants.PARENT_ROW);

								BulkUpdateConstants.bulkUpdateService
										.bulkUpdate(
												sysPrefix,
												requestTree,
												new AsyncCallback<HashMap<Integer, TbitsTreeRequestData>>() {
													public void onFailure(
															Throwable caught) {
														TbitsInfo
																.error(
																		"Error while performing bulk update...",
																		caught);
														Log
																.error(
																		"Error while performing bulk update...",
																		caught);
														ce.getButton().enable();
														message.close();
													}

													public void onSuccess(
															HashMap<Integer, TbitsTreeRequestData> result) {
														try {
															singleGridContainer
																	.showStatus(result);
															singleGridContainer
																	.updateModels();
														} catch (Exception e) {
															TbitsInfo
																	.error(
																			e
																					.getMessage(),
																			e);
															Log
																	.warn(
																			e
																					.getMessage(),
																			e);
														} finally {
															ce.getButton()
																	.enable();
															message.close();
														}
													}
												});
							}
						}
					}));

		}

		toolbar.add(new ToolBarButton("Import From CSV",
				new SelectionListener<ButtonEvent>() {

					@Override
					public void componentSelected(ButtonEvent ce) {

						final com.extjs.gxt.ui.client.widget.Window window = new com.extjs.gxt.ui.client.widget.Window();
						window.setWidth(400);
						window.setHeight(250);
						window.setHeading("Import Data from CSV File");
						window.setModal(true);
						window
								.addText("<div style='margin:5px'>This wizard helps import bulk data (in CSV) to create or update requests</div>");
						final TabPanel tabPanel = new TabPanel();
						tabPanel.setBorderStyle(false);
						tabPanel.setHeight(200);
						final TabItem templateTab = new TabItem(
								"Download Template");
						templateTab
								.addText("<div style='margin:5px'><p>Click '<a target='_blank' href='"
										+ ClientUtils
												.getUrlToFilefromBase("/importdata?ba="
														+ sysPrefix)
										+ "'>template.csv</a>' on this tab<br />"
										+ "Click 'Save to Disk' on the window prompt and click 'OK' Open the template<br />"
										+ "The first two rows on the template contain field names and field display names respectively.<br />"
										+ "Fill in data from the third row. You can use Microsoft Excel or any text editor to edit this file<br />"
										+ "To update a request instead of creating a new one, mention the request id in the 'request_id' column<br />"
										+ "Click 'Next.' This will take you to the 'Import Data' tab.</p></div>");
						tabPanel.add(templateTab);

						final TabItem importTab = new TabItem("Import Data");
						// importTab.setLayout(new FitLayout());
						importTab
								.addText("<div style='margin:5px'><p>Click 'Browse.' Select the file containing data in the template format.<br />"
										+ "Click 'Next.' This will upload the data and create or update requests. This may take some time.<br />"
										+ "Click 'Save As' on the window prompt to save the uploaded results.<br />"
										+ "NB: Uploading the data may take some time. You can open tbits in a new browser window to check its progress</p></div>");

						final FormPanel form = new FormPanel();
						form.setHeaderVisible(false);
						form.setBodyBorder(false);
						form.setEncoding(Encoding.MULTIPART);
						form.setAction(ClientUtils
								.getUrlToFilefromBase("/importdata"));
						form.setMethod(Method.POST);

						HiddenField<String> hiddenBa = new HiddenField<String>();
						hiddenBa.setName("ba");
						hiddenBa.setValue(ClientUtils.getSysPrefix());
						form.add(hiddenBa);

						HiddenField<String> hiddeFlag = new HiddenField<String>();
						hiddeFlag.setName("flag");
						hiddeFlag.setValue("true");
						form.add(hiddeFlag);

						FileUploadField uploadField = new FileUploadField();
						uploadField.setName("up");
						uploadField.setFieldLabel("Upload CSV");
						form.add(uploadField, new FormData("-20"));

						importTab.add(form);
						tabPanel.add(importTab);

						window.add(tabPanel);

						form.addListener(Events.Submit,
								new Listener<FormEvent>() {

									@Override
									public void handleEvent(FormEvent be) {

										window.close();
										System.out.println(be.getResultHtml());
										int length = be.getResultHtml()
												.length();
										HashSet<String> attfields = new HashSet<String>();
										HashSet<String> otherFields = new HashSet<String>();

										try {/*
											 * TbitsInfo.info((be
											 * .getResultHtml() .substring(5,
											 * length - 6)));
											 */
											ArrayList<TbitsTreeRequestData> tmd = new ArrayList<TbitsTreeRequestData>();
											String html = be.getResultHtml();

											if (!html.startsWith("<pre>")) {
												tmd = parseJson(be
														.getResultHtml(),
														attfields, otherFields);
											} else {
												tmd = parseJson(be
														.getResultHtml()
														.substring(5,
																length - 6),
														attfields, otherFields);
											}
											BulkUpdatePanel.this.singleGridContainer
													.addModel(tmd);
											System.out.println(otherFields
													.remove("request_id"));
										} catch (JSONException je) {
											/*
											 * TbitsInfo .error(
											 * "Invalid value provided for configuring type field in circulation list."
											 * , je);
											 */
										}
										for (String attField : attfields) {
											System.out.print(attField + "-->");
										}

										for (String attField : otherFields) {
											System.out.println(attField);
										}

										System.out
												.println("ArrayList Uploaded Successfully");

									}

									@SuppressWarnings("deprecation")
									private ArrayList<TbitsTreeRequestData> parseJson(
											String a, HashSet<String> attInfo,
											HashSet<String> otherFields) {

										ArrayList<TbitsTreeRequestData> arr = new ArrayList<TbitsTreeRequestData>();

										JSONValue parsedJson = JSONParser
												.parse(a);
										JSONArray jsonArray = parsedJson
												.isArray();
										ArrayList<BAField> fields = new ArrayList<BAField>();

										fields.addAll(CacheRepository
												.getInstance().getCache(
														FieldCache.class)
												.getMap().values());

										ArrayList<UserClient> users = new ArrayList<UserClient>();
										users.addAll(CacheRepository
												.getInstance().getCache(
														UserCache.class)
												.getMap().values());

										HashMap<String, BAField> fieldsMap = new HashMap<String, BAField>();

										for (BAField eachfield : fields) {
											fieldsMap.put(eachfield.getName(),
													eachfield);
										}

										HashSet<String> userSet = new HashSet<String>();

										for (UserClient user : users) {
											userSet.add(user.getUserLogin());
										}

										if (a != null) {

											if (jsonArray != null) {
												for (int i = 0; i < jsonArray
														.size(); i++) {

													TbitsTreeRequestData trd = new TbitsTreeRequestData();

													if (jsonArray.get(i) != null) {
														JSONArray eachObject = jsonArray
																.get(i)
																.isArray();

														for (int j = 1; j < eachObject
																.size(); j++) {

															JSONArray finalArr = eachObject
																	.get(j)
																	.isArray();

															JSONString key = finalArr
																	.get(0)
																	.isString();

															JSONArray value = finalArr
																	.get(1)
																	.isArray();

															String parsedKey = SubString(key
																	.toString());

															if (value == null) {
																JSONString val = finalArr
																		.get(1)
																		.isString();

																String parsedVal = SubString(val
																		.toString());

																if (!parsedKey
																		.equals("requestID")) {

																	if ((fieldsMap
																			.containsKey(parsedKey))
																			&& (fieldsMap
																					.get(
																							parsedKey)
																					.getDataTypeId() == DATETIME)) {

																		String parsedDate = SubString(val
																				.toString());

																		/*
																		 * Date
																		 * temp
																		 * = new
																		 * Date(
																		 * parsedDate
																		 * );
																		 * Long
																		 * l =
																		 * temp
																		 * .getTime
																		 * ();
																		 */
																		if (!parsedDate
																				.equals(""))
																			trd
																					.set(
																							parsedKey,
																							new POJODate(
																									new Date(
																											parsedDate)));

																	} else if ((fieldsMap
																			.containsKey(parsedKey))
																			&& (fieldsMap
																					.get(
																							parsedKey)
																					.getDataTypeId() == TYPE)) {

																		BAFieldCombo baFieldCombo = null;

																		if (parsedVal
																				.equals("")) {/*
																							 * 
																							 * BAField
																							 * comboField
																							 * =
																							 * fieldsMap
																							 * .
																							 * get
																							 * (
																							 * parsedKey
																							 * )
																							 * ;
																							 * if
																							 * (
																							 * comboField
																							 * !=
																							 * null
																							 * )
																							 * {
																							 * 
																							 * baFieldCombo
																							 * =
																							 * (
																							 * BAFieldCombo
																							 * )
																							 * comboField
																							 * ;
																							 * trd
																							 * .
																							 * set
																							 * (
																							 * parsedKey
																							 * ,
																							 * baFieldCombo
																							 * .
																							 * getDefaultValue
																							 * (
																							 * )
																							 * .
																							 * getName
																							 * (
																							 * )
																							 * )
																							 * ;
																							 * }
																							 * 
																							 * /
																							 * /
																							 */
																		}

																		else

																		{
																			trd
																					.set(
																							parsedKey,
																							parsedVal);

																		}

																	}

																	else if (fieldsMap
																			.containsKey(parsedKey)
																			&& (fieldsMap
																					.get(
																							parsedKey)
																					.getDataTypeId() == BOOLEAN)) {

																		if (!parsedVal
																				.equals("")) {

																			parsedVal = parsedVal
																					.toLowerCase();
																			// true/false/yes/no/1/0)
																			if (parsedVal
																					.equals("true")
																					|| parsedVal
																							.equals("1")
																					|| parsedVal
																							.equals("yes")) {
																				trd
																						.set(
																								parsedKey,
																								true);
																			} else if (parsedVal
																					.equals("false")
																					|| parsedVal
																							.equals("0")
																					|| parsedVal
																							.equals("no")) {
																				trd
																						.set(
																								parsedKey,
																								false);
																			}
																		}

																	} else if (fieldsMap
																			.containsKey(parsedKey)
																			&& (fieldsMap
																					.get(
																							parsedKey)
																					.getDataTypeId() == USERTYPE)) {

																		String existingUsers = "";

																		if (!parsedVal
																				.equals("")) {

																			String[] listOfUsers = parsedVal
																					.split(",");
																			for (String eachUser : listOfUsers) {
																				if (userSet
																						.contains(eachUser)) {

																					if (existingUsers
																							.equals("")) {
																						existingUsers = existingUsers
																								+ eachUser;
																					} else {
																						existingUsers = existingUsers
																								+ ","
																								+ eachUser;
																					}
																				}
																			}

																		}
																		trd
																				.set(
																						parsedKey,
																						existingUsers);

																	} else {
																		trd
																				.set(
																						parsedKey,
																						parsedVal);
																	}
																	otherFields
																			.add(parsedKey);
																}

															} else {

																if (fieldsMap
																		.containsKey(parsedKey)
																		&& (fieldsMap
																				.get(
																						parsedKey)
																				.getDataTypeId() == ATTACHMENTS)) {
																	BAField field = fieldsMap
																			.get(parsedKey);

																	attInfo
																			.add(SubString(key
																					.toString()));
																	ArrayList<FileClient> files = new ArrayList<FileClient>();

																	for (int k = 0; k < value
																			.size(); k++)

																	{
																		JSONArray attinfo = value
																				.get(
																						k)
																				.isArray();

																		FileClient f = new FileClient();
																		f
																				.setFileName(SubString(attinfo
																						.get(
																								0)
																						.isString()
																						.toString()));
																		f
																				.setRepoFileId(Integer
																						.parseInt(SubString(attinfo
																								.get(
																										1)
																								.isString()
																								.toString())));
																		f
																				.setSize(Integer
																						.parseInt(SubString(attinfo
																								.get(
																										2)
																								.isString()
																								.toString())));
																		f
																				.setRequestFileId(Integer
																						.parseInt(SubString(attinfo
																								.get(
																										3)
																								.isString()
																								.toString())));
																		f
																				.setFieldId(field
																						.getFieldId());
																		f
																				.setRequestId(Integer
																						.parseInt(SubString(attinfo
																								.get(
																										4)
																								.isString()
																								.toString())));

																		files
																				.add(f);

																	}

																	trd
																			.set(
																					field
																							.getName(),
																					new POJOAttachment(
																							files));

																}
															}
														}

													}
													trd
															.remove("parent_request_id");
													arr.add(trd);

												}
											}
										}
										/*
										 * TbitsInfo.error(
										 * "size of arraylist modeldata returned"
										 * + arr.size());
										 */

										return arr;

									}

									private BAField getBAComboField(String key,
											ArrayList<BAField> fieldList) {

										if (fieldList != null) {

											for (BAField tField : fieldList) {
												if ((tField != null)
														&& (tField.get("name")
																.equals(key))) {
													if (tField.getDataTypeId() == 9) {
														return tField;
													}
												}
											}

										}
										return null;
									}

									private boolean checkIfColumnIsDate(
											String key,
											ArrayList<BAField> fieldList) {
										// TODO Auto-generated method stub
										BAField baField = null;

										if (fieldList != null) {

											for (BAField tField : fieldList) {
												if ((tField != null)
														&& (tField.get("name")
																.equals(key))) {
													if (tField.getDataTypeId() == 4) {
														return true;
													} else {
														return false;
													}
												}
											}
										}
										return false;

									}

									private boolean checkIfColumnIsMultiValue(
											String key,
											ArrayList<BAField> fieldList) {
										// TODO Auto-generated method stub
										BAField baField = null;

										if (fieldList != null) {

											for (BAField tField : fieldList) {
												if ((tField != null)
														&& (tField.get("name")
																.equals(key))) {
													if (tField.getDataTypeId() == 9) {
														return true;
													} else {
														return false;
													}
												}
											}
										}
										return false;

									}

									private String SubString(String string) {
										// TODO Auto-generated method stub
										return string.substring(1, string
												.length() - 1);
									}
								}

						);

						Button next = new Button("Next",
								new SelectionListener<ButtonEvent>() {

									public void componentSelected(ButtonEvent ce) {
										if (tabPanel.getSelectedItem().equals(
												templateTab)) {
											tabPanel.setSelection(importTab);
										} else {
											// formPanel.submit();
											form.submit();
											System.out
													.println("Form Submitted");
										}
									}

								});

						window.addButton(next);
						window.show();

					}
				}));

	}

	public void hideWindows() {
		((BulkUpdateGrid) singleGridContainer.getBulkGrid()).hideWindows();
		((BulkUpdateGrid) commonGridContainer.getBulkGrid()).hideWindows();
	}

	public boolean isBusyUploading() {
		if (((BulkUpdateGrid) singleGridContainer.getBulkGrid())
				.isBusyUploading()
				|| ((BulkUpdateGrid) commonGridContainer.getBulkGrid())
						.isBusyUploading()) {
			Window
					.alert("File Uploads in progess. \nPlease wait till they finish or cancel them first.");
			return true;
		}
		return false;
	}

	@Override
	public TbitsTreeRequestData getEmptyModel() {
		return new TbitsTreeRequestData();
	}

	@Override
	protected BulkUpdateGridAbstract<TbitsTreeRequestData> getNewBulkGrid(
			BulkGridMode mode) {
		DefaultUIContext context = new DefaultUIContext();
		BulkUpdateGrid bulkGrid = new BulkUpdateGrid(sysPrefix, context,
				BulkGridMode.SINGLE);

		return bulkGrid;
	}
}
