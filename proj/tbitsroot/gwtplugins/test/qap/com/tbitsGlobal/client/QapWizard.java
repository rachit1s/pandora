package qap.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;
import commons.com.tbitsGlobal.utils.client.wizards.IWizardPage;

public class QapWizard extends AbstractWizard {

	// these are fields whose values should be same in all the request

	public static final String ANALYSISRESULTS = "AnalysisResults";
	/*
	 * public static final String CONTRACTOR = "Contractor"; public static final
	 * String SUB_VENDOR_NAME = "SubVendorName";
	 */
	static String[] page1Specific;
	static String[] page1Common;
	static String[] page2Fields;
	static String reportName;
	String requestList = "";
	ArrayList<TbitsTreeRequestData> arr;
	String deliverable;
	
	//public QapWizard data;

	public QapWizard(ArrayList<Integer> requestIds) {

		super();

		this.setHeading("MDCC Wizard");
		this.addBackButton();
		this.addNextButton();
		this.addFinishButton();
		arr = new ArrayList<TbitsTreeRequestData>();
		processIds(requestIds);
	}
	@Override
	public void addBackButton() {
		backBtn = new Button("Back", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				try {
					int current = activePage.getDisplayOrder();
					if (pages.containsKey(current - 1)) {
						IWizardPage<? extends LayoutContainer, ?> prePage = activePage
								.getPrevious();
						layout.setActiveItem(prePage.getWidget());
						activePage = prePage;
						activePage.onDisplay();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		this.addButton(backBtn);
	}

	@Override
	public void addFinishButton() {

		finishBtn = new Button("Create MDCC",
				new SelectionListener<ButtonEvent>() {
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

	@Override
	public void addNextButton() {
		nextBtn = new Button("Next", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				try {
					int current = activePage.getDisplayOrder();
					if (pages.containsKey(current + 1)) {
						if (activePage.onLeave()) {
							IWizardPage<? extends LayoutContainer, ?> nextPage = activePage
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

	private void processIds(final ArrayList<Integer> requestIds) {

		try {
			QAPConstants.dbService.getRequestData(ClientUtils.getSysPrefix(),
					ClientUtils.getCurrentBA().getSystemId(), requestIds,
					new AsyncCallback<HashMap<String, Object>>() {

						@Override
						public void onSuccess(HashMap<String, Object> modelData) {

							ArrayList<TbitsTreeRequestData> requests = (ArrayList<TbitsTreeRequestData>) modelData.get("requestData");

							HashMap<String, String> map = (HashMap<String, String>) modelData.get("gridColumns");
							
							
							for(TbitsTreeRequestData trd:requests)
							{
								String nextTestNumber=trd.getAsString("next_sr_no");
								
								if(!nextTestNumber.equalsIgnoreCase("mdcc"))
								{
									Window
									.alert("For creating  the MDCC for all of the selected requests, In Request Id :  " + trd.getRequestId()
											+ "  Next Test Serial Number Should be MDCC");
									return;
								}
							}
							
							

							if (map.get("page1common") != null)
								page1Common = map.get("page1common").split(",");

							if (map.get("pag1specific") != null)
								page1Specific = map.get("pag1specific").split(
										",");

							if (map.get("page2fields") != null)
								page2Fields = map.get("page2fields").split(",");

							if (map.get("reportname") != null) {
								reportName = map.get("reportname");
							}
							if (map.get("reportfield") != null)
								deliverable = map.get("reportfield");

							// here we checking for same values of certain
							// fields in all the request
							int size = requests.size();
							String val1 = requests.get(0).getAsString(
									ANALYSISRESULTS);
							if (!val1.equalsIgnoreCase("Approved")) {
								Window
										.alert("For creating  the MDCC for all of the selected requests "
												+ " status of Final Owner Decision field Should be [IRN-Proceed to Next Stage] and..");
								return;
							}

							StringBuilder errormessgae = new StringBuilder(
									"Please correct these errors:\n");
							Boolean flag = false;
							for (String eachKey : page1Common) {

								String value = "";

								for (TbitsTreeRequestData trd : requests) {

									if (value.equals("")) {
										value = trd.getAsString(eachKey);
									} else {
										if (!trd.getAsString(eachKey).equals(
												value)) {
											errormessgae
													.append("For the field "
															+ eachKey
															+ " All values  should be same "
															+ "\n");
											flag = true;
										}

									}
								}

							}
							/*
							 * String val2 = requests.get(0).getAsString(
							 * CONTRACTOR); String val3 =
							 * requests.get(0).getAsString( SUB_VENDOR_NAME);
							 * Boolean flag = true;
							 * 
							 * for (int i = 1; i <= size - 1; i++) {
							 * 
							 * to -do add the statements for field 2
							 * 
							 * String temp1 = requests.get(i).getAsString(
							 * ANALYSISRESULT); String temp2 =
							 * requests.get(i).getAsString( CONTRACTOR); String
							 * temp3 = requests.get(i).getAsString(
							 * SUB_VENDOR_NAME);
							 * 
							 * if (!temp1.equalsIgnoreCase(val1) ||
							 * !temp2.equalsIgnoreCase(val2) ||
							 * !temp3.equalsIgnoreCase(val3)) { flag = false;
							 * break; }
							 * 
							 * }
							 */
							if (flag) {
								Window.alert(errormessgae.toString());
								return;
							}

							for (TbitsTreeRequestData trd : requests) {

								requestList = (requestList.equals("")) ? String
										.valueOf(trd.getRequestId())
										: requestList + ","
												+ (Integer) trd.getRequestId();
							}

							arr = requests;
							if (QAPConstants.requestData != null) {
								QAPConstants.requestData.clear();
							}
							if (QAPConstants.requestData == null) {
								QAPConstants.requestData = new ArrayList<TbitsTreeRequestData>();
							}
							QAPConstants.requestData.addAll(requests);
							QapPage3 QapPage3=new QapPage3(context,requests);
							QapPage1 page1 = new QapPage1(context, requests);
							QapPage2 page2 = new QapPage2(context, requests);
							QapReportPreviewPage page3 = new QapReportPreviewPage(
									context, requestList);

							QapWizard.this.addPage(QapPage3);
							QapWizard.this.addPage(page1);
							QapWizard.this.addPage(page2);
							QapWizard.this.addPage(page3);

							QapWizard.this.show();
							activePage = QapPage3;
							activePage.onDisplay();
						}

						@Override
						public void onFailure(Throwable arg0) {

							Window
									.alert("Not able to fetch request objects from database");

						}

					});

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	@Override
	protected void addPreviewDOCButton() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void addPreviewPDFButton() {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	private HashMap<String, Object> getParamTable() {
		HashMap<String, Object> paramTable = new HashMap<String, Object>();
		IWizardPage<? extends LayoutContainer, ?> page = pages.get(2); // what
																		// is
																		// this.....??????
		IWizardPage<? extends LayoutContainer, ?> page2 = pages.get(0);
		
		HashMap<String, String> values = (HashMap<String, String>) page
				.getValues();

		HashMap<String, String> hmabc = (HashMap<String, String>) page2.getValues();

		paramTable.put("Remarks", values.get("Remarks"));
		paramTable.put("Description", values.get("Description"));
		paramTable.put("page1common", page1Common);
		paramTable.put("page1specific", page1Specific);
		paramTable.put("logger_ids", hmabc.get("from"));
		paramTable.put("assignee_ids", hmabc.get("toList"));
		paramTable.put("subscriber_ids", hmabc.get("ccList"));
		paramTable.put("AccessTo", hmabc.get("accessTo"));
		paramTable.put("notify", hmabc.get("notify"));
		paramTable.put("subject", hmabc.get("subject"));
		paramTable.put("emailBody", hmabc.get("emailBody"));
		paramTable.put("OS_Unit", hmabc.get("OS_Unit"));
		paramTable.put("QA_Ref_No", hmabc.get("QA_Ref_No"));
		paramTable.put("Shipping_Release_No", hmabc.get("Shipping_Release_No"));
		paramTable.put("Office_SR_No", hmabc.get("Office_SR_No"));
		paramTable.put("QA_Ref_No_for_Main_Order", hmabc.get("QA_Ref_No_for_Main_Order"));
		paramTable.put("reportname", reportName);

		if (values != null) {
			paramTable.put("sysPrefix", ClientUtils.getSysPrefix());
			paramTable.put("user", ClientUtils.getCurrentUser().getUserLogin());
		}

		return paramTable;
	}

	@Override
	protected void onSubmit() {
		this.finishBtn.disable();
		HashMap<String, Object> paramTable = new HashMap<String, Object>();
		paramTable.putAll(getParamTable());

		System.out.println(requestList);

		paramTable.put("requestList", requestList);

		String[] deliverableNameArray = new String[1];

		deliverableNameArray[0] = deliverable;

		HashMap<Integer, HashMap<String, List<FileClient>>> attachmentInfoClientsMap = new HashMap<Integer, HashMap<String, List<FileClient>>>();

		for (TbitsTreeRequestData trd : arr) {

			final HashMap<String, List<FileClient>> deliverableFileClients = new HashMap<String, List<FileClient>>();

			for (String fieldName : deliverableNameArray) {

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
			attachmentInfoClientsMap.put(trd.getRequestId(),
					deliverableFileClients);
		}

		QAPConstants.dbService.createTransmittal(paramTable,
				attachmentInfoClientsMap,
				new AsyncCallback<TbitsTreeRequestData>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert("ERROR:: Generation of MDCC FAILED!!!!!!");
						QapWizard.this.close();

					}

					@SuppressWarnings("deprecation")
					@Override
					public void onSuccess(TbitsTreeRequestData req) {
						if (req != null) {
							String mdccNo = req.getAsString("MDCC");
							String reqId = req.getAsString("request_id");
							String sysPrefix=ClientUtils.getBAbySysId(req.getSystemId()).getSystemPrefix();
							
							Window
									.alert("MDCC Generated Succeesfully Via..MDCC No.["
											+ mdccNo
											+ "] and MDCC Document No.["+ sysPrefix +"#"
											+ reqId + "]");

						}
						QapWizard.this.close();
					}

				});

	}
	

}
