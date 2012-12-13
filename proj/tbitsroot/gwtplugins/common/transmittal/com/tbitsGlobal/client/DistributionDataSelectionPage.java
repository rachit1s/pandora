package transmittal.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tbitsGlobal.jaguar.client.cache.UserCache;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

/**
 * 
 * @author rohit
 * 
 */
public class DistributionDataSelectionPage extends
		TransmittalAbstractWizardPage<ContentPanel, HashMap<String, String>> {

	private String ccList = "";
	ArrayList<String> arrayOfPrevCC = new ArrayList<String>();

	private int prevTrnProcessId = 0;
	private WizardData dataObject;
	private Label l;
	public static int visits;
	Boolean flag = false;
	MessageBox messageBox;

	public WizardData getDataObject() {
		return dataObject;
	}

	public void setDataObject(WizardData dataObject) {
		this.dataObject = dataObject;
	}

	private EditorGrid<TbitsModelData> grid = null;
	DistributionTableColumnsConfig columnConfiguration;
	public static final int NAME = 1;
	public static final int EMAIL = 2;
	public static final int ORGANISATION = 3;
	public static final int QTY = 4;
	public static final int TYPE = 5;
	String prevcclist = "";

	public DistributionDataSelectionPage(UIContext wizardContext,
			WizardData data) {
		super(wizardContext);
		this.setDataObject(data);
		visits = 0;
	}

	public int getDisplayOrder() {
		return 2;
	}

	@SuppressWarnings("unchecked")
	public void setValue() {

		visits++;
		List<TbitsModelData> models = grid.getStore().getModels();
		ArrayList<TbitsModelData> distributionColumns = (ArrayList<TbitsModelData>) getDataObject()
				.getData().get("distributionTableColumnsList");

		ArrayList<String> propertyList = new ArrayList<String>();
		for (TbitsModelData md : distributionColumns) {
			propertyList.add((String) md.get("name"));
		}

		JSONArray tableJson = getDistributionListJsonString(models,
				propertyList);
		getDataObject().getData().put(TransmittalConstants.DISTRIBUTION_TABLE,
				tableJson.toString());

		TransmittalConstants.dbService.fetchArrayListFromJsonArray(tableJson
				.toString(), new AsyncCallback<ArrayList<String[]>>() {

			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
			}

			@Override
			public void onSuccess(ArrayList<String[]> result) {

				getDataObject().getData().put("NewConvertedDistributionList",
						result);
				flag = true;
				messageBox.close();
			}

		});

	}

	// }

	/**
	 * @param model
	 * @param fieldNames
	 * @return
	 */
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
			drawingListValues.set(i+1, new JSONString(fValue));

			tableJson.set(count, drawingListValues);
			count++;
		}
		return tableJson;
	}

	public ContentPanel getWidget() {
		return widget;
	}

	public void initializeWidget() {
		widget = new ContentPanel(new FitLayout());
		widget.setScrollMode(Scroll.AUTO);
		l = new Label();
		widget.setTopComponent(l);
		widget.setHeading("Distribution Data Selection Page");

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
				.hideFinishTransmittalForPage1Btn();
		wizardContext.getValue(
				TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
				TransmittalAbstractWizard.class)
				.hideFinishTransmittalForPage2Btn();
	}

	public void onInitialize() {

	}

	public boolean onLeave() {

		messageBox = MessageBox.wait("Please Wait",
				"Preview is being generated", "Please Wait...");
		setValue();

		return true;

	}

	protected static String getUserInfoBasedOnKey(UserClient uc, String key) {
		if ((key != null) && (!key.trim().equals(""))) {
			key = key.trim();
			return String.valueOf(uc.get(key));
		} else
			return "-";
	}

	protected static UserClient getUserBasedOnLogin(
			HashMap<Integer, UserClient> userMap, String userLogin) {
		if (userLogin == null)
			return null;
		if (userMap != null) {
			for (UserClient uc : userMap.values())
				if (uc.getUserLogin().equalsIgnoreCase(userLogin))
					return uc;
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public void buildPage(WizardData params) {

		if (Boolean.valueOf((String) params.getData().get("inapprovalcycle"))) {

			UserCache userCache = CacheRepository.getInstance().getCache(
					UserCache.class);

			ArrayList<TbitsModelData> distributionListModelData = new ArrayList<TbitsModelData>();
			ArrayList<TbitsModelData> distributionDataColumns = (ArrayList<TbitsModelData>) params
					.getData().get("distributionTableColumnsList");

			ArrayList<String> propList = new ArrayList<String>();

			for (TbitsModelData md : distributionDataColumns) {
				propList.add((String) md.get("name"));
			}

			ArrayList<String[]> ConvertedDistributionList = (ArrayList<String[]>) getDataObject()
					.getData().get("ConvertedDistributionList");

			Integer currentTrnId = (Integer) getDataObject().getData().get(
					"trnProcessId");

			String cc = (String) params.getData().get("ccList");

			if ((currentTrnId != prevTrnProcessId) || !cc.equals(ccList)) {

				if (Boolean.valueOf((String) getDataObject().getData().get(
						"Wizard_Version"))
						&& (ConvertedDistributionList != null)
						&& ConvertedDistributionList.size() > 0)

				{

					String selectedCC = (String) params.getData().get("ccList");

					HashSet<String> userSet = new HashSet<String>();
					for (String userLogin : selectedCC.trim().split(",")) {
						if (!userLogin.trim().equals("")) {
							userSet.add(userLogin);
						}
					}

					if (visits == 0) {
						ArrayList<String> curCC = new ArrayList<String>();
						String listOfnewlyAddedCC = "";
						ArrayList<String> deletedCc = new ArrayList<String>();
						String listOfDeletedCC = "";
						String CarryOverCCs = (String) getDataObject()
								.getData().get("CarryOverCC");

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
								listOfnewlyAddedCC = listOfnewlyAddedCC
										+ totalCC + ",";
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
								listOfDeletedCC = listOfDeletedCC + prevcc
										+ ",";
							}

						}

						TbitsModelData tempData = new TbitsModelData();

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

											String value = (String) uc
													.get(property);
											if ((value != null)
													&& (!value.trim()
															.equals("")))
												tempData2.set(property, value);
											else {
												String tempValue = distColumnTmd
														.get(TransmittalConstants.FIELD_CONFIG);
												if ((Integer) distColumnTmd
														.get("data_type_id") != 9) {
													if ((tempValue != null)
															&& (!tempValue
																	.trim()
																	.equals("")))
														tempData2.set(property,
																tempValue);
													else
														tempData2.set(property,
																"-");
												} else {
													HashMap<String, String> typesMap = new HashMap<String, String>();
													DistributionTableColumnsConfig
															.fetchKeyValuePairsfromJsonString(
																	typesMap,
																	tempValue);
													tempValue = typesMap
															.keySet()
															.iterator().next();
													if (tempValue != null)
														tempData2.set(property,
																tempValue);
													else
														tempData2.set(property,
																"-");
												}
											}
										}
									}
									distributionListModelData.add(tempData2);
								}
							}
						}

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
										tempData.set(prop, value);
									}

									if (propList.contains(prop)) {

										tempData.set(prop, value);

									}
								}
							}
							if (!ignore)
								distributionListModelData.add(tempData);
						}

					} else {
						ArrayList<String> curCC = new ArrayList<String>();
						String listOfnewlyAddedCC = "";
						ArrayList<String> deletedCc = new ArrayList<String>();
						String listOfDeletedCC = "";
						String CarryOverCCs = ccList;

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
								listOfnewlyAddedCC = listOfnewlyAddedCC
										+ totalCC + ",";
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
								listOfDeletedCC = listOfDeletedCC + prevcc
										+ ",";
							}

						}

						TbitsModelData tempData = new TbitsModelData();

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
													&& (!value.trim()
															.equals("")))
												tempData2.set(property, value);
											else {
												String tempValue = distColumnTmd
														.get(TransmittalConstants.FIELD_CONFIG);
												if ((Integer) distColumnTmd
														.get("data_type_id") != 9) {
													if ((tempValue != null)
															&& (!tempValue
																	.trim()
																	.equals("")))
														tempData2.set(property,
																tempValue);
													else
														tempData2.set(property,
																"-");
												} else {
													HashMap<String, String> typesMap = new HashMap<String, String>();
													DistributionTableColumnsConfig
															.fetchKeyValuePairsfromJsonString(
																	typesMap,
																	tempValue);
													tempValue = typesMap
															.keySet()
															.iterator().next();
													if (tempValue != null)
														tempData2.set(property,
																tempValue);
													else
														tempData2.set(property,
																"-");
												}
											}
										}
									}
									distributionListModelData.add(tempData2);
								}
							}
						}

						ArrayList<String[]> NewConvertedDistributionList = (ArrayList<String[]>) getDataObject()
								.getData().get("NewConvertedDistributionList");

						for (String[] eachData : NewConvertedDistributionList) {
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
										tempData.set(prop, value);
									}

									if (propList.contains(prop)) {

										tempData.set(prop, value);

									}
								}
							}
							if (!ignore)
								distributionListModelData.add(tempData);
						}

					}
				}
				/*
				 * if version is old or process is different
				 */
				else {
					String selectedCC = (String) params.getData().get("ccList");

					HashSet<String> userSet = new HashSet<String>();
					for (String userLogin : selectedCC.trim().split(",")) {
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
								tempData2.set("login", uc.getUserLogin());

								for (TbitsModelData distColumnTmd : distributionDataColumns) {
									String property = (String) distColumnTmd
											.get("name");
									if (property != null) {
										// Check if its a user property if not,
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

				}

				if (distributionListModelData == null
						|| distributionListModelData.size() == 0) {
					TbitsInfo.warn("Empty distribution list transmittal.");
					Window
							.alert("No users selected in cc list for transmittal."
									+ "Please add some users in cc list.");

				}

				ccList = (String) params.getData().get("ccList");

				arrayOfPrevCC.clear();
				for (String eachcc : ccList.split(","))
					arrayOfPrevCC.add(eachcc);

				prevTrnProcessId = currentTrnId;

				if (grid == null) {
					columnConfiguration = new DistributionTableColumnsConfig(
							params);
					List<ColumnConfig> configs = columnConfiguration
							.configureColumns();
					if ((configs == null) || configs.isEmpty()) {
						Window
								.alert("Attachment selection table could not configured. Please contact tBits team.");
						return;
					}

					grid = new EditorGrid<TbitsModelData>(
							new ListStore<TbitsModelData>(), new ColumnModel(
									configs));

					grid.setStyleAttribute("borderTop", "none");
					grid.setBorders(true);
					grid.setStripeRows(true);
					grid.getStore().add(distributionListModelData);

					GridView view = new GridView() {
						protected void onColumnWidthChange(int column, int width) {
							super.onColumnWidthChange(column, width);
							this.refresh(false);
						}
					};

					grid.setView(view);
					this.getWidget().add(grid, new FitData());
					this.getWidget().setScrollMode(Scroll.NONE);

				} else {
					grid.getStore().removeAll();
					grid.getStore().add(distributionListModelData);
				}

				if (cc.equals("")) {
					l.setVisible(true);
					l.setText("No cc Has been Selected");
					l.setSize(20, 20);
				} else {
					l.setVisible(false);
				}
				flag = false;
			}

		}
		/*
		 * end of approval cycle code
		 */
		else {

			UserCache userCache = CacheRepository.getInstance().getCache(
					UserCache.class);

			ArrayList<TbitsModelData> distributionListModelData = new ArrayList<TbitsModelData>();
			ArrayList<TbitsModelData> distributionDataColumns = (ArrayList<TbitsModelData>) params
					.getData().get("distributionTableColumnsList");

			ArrayList<String> propList = new ArrayList<String>();

			for (TbitsModelData md : distributionDataColumns) {
				propList.add((String) md.get("name"));
			}

			Integer currentTrnId = (Integer) getDataObject().getData().get(
					"trnProcessId");

			String cc = (String) params.getData().get("ccList");

			if ((currentTrnId != prevTrnProcessId) || !cc.equals(ccList)) {

				String selectedCC = (String) params.getData().get("ccList");

				HashSet<String> userSet = new HashSet<String>();
				for (String userLogin : selectedCC.trim().split(",")) {
					if (!userLogin.trim().equals("")) {
						userSet.add(userLogin);
					}
				}

				if (visits == 0) {
					for (String userLogin1 : userSet) {
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
										// Check if its a user property if not,
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
				}
				/*
				 * code for the operation after first viist
				 */
				else {

					ArrayList<String> curCC = new ArrayList<String>();
					String listOfnewlyAddedCC = "";
					ArrayList<String> deletedCc = new ArrayList<String>();
					String listOfDeletedCC = "";

					for (String totalCC : userSet) {
						boolean ismatched = false;

						for (String prevcc : arrayOfPrevCC) {
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

					for (String prevcc : arrayOfPrevCC) {
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
										// Check if its a user property if not,
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

				}

				if (distributionListModelData == null
						|| distributionListModelData.size() == 0) {
					TbitsInfo.warn("Empty distribution list transmittal.");
					Window
							.alert("No users selected in cc list for transmittal."
									+ "Please add some users in cc list.");

				}
				if (grid == null) {
					columnConfiguration = new DistributionTableColumnsConfig(
							params);
					List<ColumnConfig> configs = columnConfiguration
							.configureColumns();
					if ((configs == null) || configs.isEmpty()) {
						Window
								.alert("Attachment selection table could not configured. Please contact tBits team.");
						return;
					}

					grid = new EditorGrid<TbitsModelData>(
							new ListStore<TbitsModelData>(), new ColumnModel(
									configs));

					grid.setStyleAttribute("borderTop", "none");
					grid.setBorders(true);
					grid.setStripeRows(true);
					grid.getStore().add(distributionListModelData);

					GridView view = new GridView() {
						protected void onColumnWidthChange(int column, int width) {
							super.onColumnWidthChange(column, width);
							this.refresh(false);
						}
					};

					grid.setView(view);
					this.getWidget().add(grid, new FitData());
					this.getWidget().setScrollMode(Scroll.NONE);

				} else {
					grid.getStore().removeAll();
					grid.getStore().add(distributionListModelData);
				}

				if (cc.equals("")) {
					l.setVisible(true);
					l.setText("No cc Has been Selected");
					l.setSize(20, 20);
				} else {
					l.setVisible(false);
				}
				ccList = (String) params.getData().get("ccList");

				arrayOfPrevCC.clear();
				for (String eachcc : ccList.split(","))
					arrayOfPrevCC.add(eachcc);

				prevTrnProcessId = currentTrnId;

			}
		}
	}

	public boolean funcToBeCalledOnBack() {

		messageBox = MessageBox.wait("Please Wait",
				"Preview is being generated", "Please Wait...");
		setValue();

		return true;
	}

	@Override
	public HashMap<String, String> getValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public void buildPage1(WizardData params) {

		if (Boolean.valueOf((String) params.getData().get("inapprovalcycle"))) {

			UserCache userCache = CacheRepository.getInstance().getCache(
					UserCache.class);

			ArrayList<TbitsModelData> distributionListModelData = new ArrayList<TbitsModelData>();
			ArrayList<TbitsModelData> distributionDataColumns = (ArrayList<TbitsModelData>) params
					.getData().get("distributionTableColumnsList");

			ArrayList<String> propList = new ArrayList<String>();

			for (TbitsModelData md : distributionDataColumns) {
				propList.add((String) md.get("name"));
			}

			ArrayList<String[]> ConvertedDistributionList = (ArrayList<String[]>) getDataObject()
					.getData().get("ConvertedDistributionList");

			Integer currentTrnId = (Integer) getDataObject().getData().get(
					"trnProcessId");

			String cc = (String) params.getData().get("ccList");

			if ((currentTrnId != prevTrnProcessId) || !cc.equals(ccList)) {

				if (Boolean.valueOf((String) getDataObject().getData().get(
						"Wizard_Version"))
						&& (ConvertedDistributionList != null)
						&& ConvertedDistributionList.size() > 0)

				{

					String selectedCC = (String) params.getData().get("ccList");

					HashSet<String> userSet = new HashSet<String>();
					for (String userLogin : selectedCC.trim().split(",")) {
						if (!userLogin.trim().equals("")) {
							userSet.add(userLogin);
						}
					}

					if (visits == 0) {
						ArrayList<String> curCC = new ArrayList<String>();
						String listOfnewlyAddedCC = "";
						ArrayList<String> deletedCc = new ArrayList<String>();
						String listOfDeletedCC = "";
						String CarryOverCCs = (String) getDataObject()
								.getData().get("CarryOverCC");

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
								listOfnewlyAddedCC = listOfnewlyAddedCC
										+ totalCC + ",";
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
								listOfDeletedCC = listOfDeletedCC + prevcc
										+ ",";
							}

						}

						TbitsModelData tempData = new TbitsModelData();

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

											String value = (String) uc
													.get(property);
											if ((value != null)
													&& (!value.trim()
															.equals("")))
												tempData2.set(property, value);
											else {
												String tempValue = distColumnTmd
														.get(TransmittalConstants.FIELD_CONFIG);
												if ((Integer) distColumnTmd
														.get("data_type_id") != 9) {
													if ((tempValue != null)
															&& (!tempValue
																	.trim()
																	.equals("")))
														tempData2.set(property,
																tempValue);
													else
														tempData2.set(property,
																"-");
												} else {
													HashMap<String, String> typesMap = new HashMap<String, String>();
													DistributionTableColumnsConfig
															.fetchKeyValuePairsfromJsonString(
																	typesMap,
																	tempValue);
													tempValue = typesMap
															.keySet()
															.iterator().next();
													if (tempValue != null)
														tempData2.set(property,
																tempValue);
													else
														tempData2.set(property,
																"-");
												}
											}
										}
									}
									distributionListModelData.add(tempData2);
								}
							}
						}

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
										tempData.set(prop, value);
									}

									if (propList.contains(prop)) {

										tempData.set(prop, value);

									}
								}
							}
							if (!ignore)
								distributionListModelData.add(tempData);
						}

					} else {
						ArrayList<String> curCC = new ArrayList<String>();
						String listOfnewlyAddedCC = "";
						ArrayList<String> deletedCc = new ArrayList<String>();
						String listOfDeletedCC = "";
						String CarryOverCCs = ccList;

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
								listOfnewlyAddedCC = listOfnewlyAddedCC
										+ totalCC + ",";
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
								listOfDeletedCC = listOfDeletedCC + prevcc
										+ ",";
							}

						}

						TbitsModelData tempData = new TbitsModelData();

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
													&& (!value.trim()
															.equals("")))
												tempData2.set(property, value);
											else {
												String tempValue = distColumnTmd
														.get(TransmittalConstants.FIELD_CONFIG);
												if ((Integer) distColumnTmd
														.get("data_type_id") != 9) {
													if ((tempValue != null)
															&& (!tempValue
																	.trim()
																	.equals("")))
														tempData2.set(property,
																tempValue);
													else
														tempData2.set(property,
																"-");
												} else {
													HashMap<String, String> typesMap = new HashMap<String, String>();
													DistributionTableColumnsConfig
															.fetchKeyValuePairsfromJsonString(
																	typesMap,
																	tempValue);
													tempValue = typesMap
															.keySet()
															.iterator().next();
													if (tempValue != null)
														tempData2.set(property,
																tempValue);
													else
														tempData2.set(property,
																"-");
												}
											}
										}
									}
									distributionListModelData.add(tempData2);
								}
							}
						}

						ArrayList<String[]> NewConvertedDistributionList = (ArrayList<String[]>) getDataObject()
								.getData().get("NewConvertedDistributionList");

						for (String[] eachData : NewConvertedDistributionList) {
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
										tempData.set(prop, value);
									}

									if (propList.contains(prop)) {

										tempData.set(prop, value);

									}
								}
							}
							if (!ignore)
								distributionListModelData.add(tempData);
						}

					}
				}
				/*
				 * if version is old or process is different
				 */
				else {
					String selectedCC = (String) params.getData().get("ccList");

					HashSet<String> userSet = new HashSet<String>();
					for (String userLogin : selectedCC.trim().split(",")) {
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
								tempData2.set("login", uc.getUserLogin());

								for (TbitsModelData distColumnTmd : distributionDataColumns) {
									String property = (String) distColumnTmd
											.get("name");
									if (property != null) {
										// Check if its a user property if not,
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

				}

				if (distributionListModelData == null
						|| distributionListModelData.size() == 0) {
					TbitsInfo.warn("Empty distribution list transmittal.");
					Window
							.alert("No users selected in, to and cc list for transmittal."
									+ "Please add some users in, to and cc list.");

				}

				ccList = (String) params.getData().get("ccList");

				arrayOfPrevCC.clear();
				for (String eachcc : ccList.split(","))
					arrayOfPrevCC.add(eachcc);

				prevTrnProcessId = currentTrnId;

				if (grid == null) {
					columnConfiguration = new DistributionTableColumnsConfig(
							params);
					List<ColumnConfig> configs = columnConfiguration
							.configureColumns();
					if ((configs == null) || configs.isEmpty()) {
						Window
								.alert("Attachment selection table could not configured. Please contact tBits team.");
						return;
					}

					grid = new EditorGrid<TbitsModelData>(
							new ListStore<TbitsModelData>(), new ColumnModel(
									configs));

					grid.setStyleAttribute("borderTop", "none");
					grid.setBorders(true);
					grid.setStripeRows(true);
					grid.getStore().add(distributionListModelData);

					GridView view = new GridView() {
						protected void onColumnWidthChange(int column, int width) {
							super.onColumnWidthChange(column, width);
							this.refresh(false);
						}
					};

					grid.setView(view);
					this.getWidget().add(grid, new FitData());
					this.getWidget().setScrollMode(Scroll.NONE);

				} else {
					grid.getStore().removeAll();
					grid.getStore().add(distributionListModelData);
				}

				if (cc.equals("")) {
					l.setVisible(true);
					l.setText("No cc Has been Selected");
					l.setSize(20, 20);
				} else {
					l.setVisible(false);
				}
				flag = false;
			}

		}
	}

}