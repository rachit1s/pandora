package qap.com.tbitsGlobal.client;

import java.util.HashMap;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizardPage;

public class QapReportPreviewPage extends
		AbstractWizardPage<FormPanel, HashMap<String, String>> {

	final HTML birtGeneratedHTML = new HTML();
	String reqList;

	protected QapReportPreviewPage(UIContext wizardContext, String requestList) {
		super(wizardContext);
		reqList = requestList;
		buildPage();
	}

	@Override
	public void buildPage() {
		widget.add(birtGeneratedHTML);

	}

	@Override
	public int getDisplayOrder() {

		return 3;
	}

	@Override
	public HashMap<String, String> getValues() {

		HashMap<String, String> paramTable = new HashMap<String, String>();
		return paramTable;
	}

	@Override
	public FormPanel getWidget() {
		// TODO Auto-generated method stub
		return widget;
	}

	@Override
	public void initializeWidget() {

		widget = new FormPanel();
		widget.setLabelWidth(150);
		widget.setBodyBorder(false);
		widget.setScrollMode(Scroll.AUTO);
		widget.setHeading("MDCC Report Preview Page");
	}

	@Override
	public void onDisplay() {

		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD,
				AbstractWizard.class).showBackButton();

		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD,
				AbstractWizard.class).showFinishButton();

		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD,
				AbstractWizard.class).hideNextButton();

		HashMap<String, Object> paramTable = getParamTable();
		final MessageBox messageBox = MessageBox.wait("Please Wait",
				"Preview is being generated", "Please Wait...");

		getBirtGeneratedHtml(paramTable, messageBox);

		if ((birtGeneratedHTML != null)
				|| (!birtGeneratedHTML.getText().isEmpty()))
			widget.layout(); // what is this....?????
		else
			QapReportPreviewPage.this.onDisplay();

	}

	private void getBirtGeneratedHtml(HashMap<String, Object> paramTable,
			final MessageBox messageBox) {

		QAPConstants.dbService.getHTMLTransmittalPreviewUsingBirt(paramTable,
				new AsyncCallback<String>() {

					public void onFailure(Throwable caught) {
						caught.printStackTrace();
						messageBox.close();
						Window.alert("MDCC note preview did not succeed: \n"
								+ caught.getMessage());
					}

					public void onSuccess(String result) {
						messageBox.close();
						birtGeneratedHTML.setHTML(result);
					}
				});

	}

	private HashMap<String, Object> getParamTable() {

		QapDataWizard QapDataWizardObject = new QapDataWizard();
		HashMap<String, String> hm = (HashMap<String, String>) this
				.getPrevious().getValues();
		String Remarks = hm.get("Remarks");
		String Description = hm.get("Description");
		
		HashMap<String, String> hmpg1 = (HashMap<String, String>) this
				.getPrevious().getPrevious().getPrevious().getValues();

		HashMap<String, Object> hm1 = new HashMap<String, Object>();

		hm1.put("Remarks", Remarks);
		hm1.put("Description", Description);
		hm1.put("req", reqList);
		hm1.put("sysPrefix", ClientUtils.getSysPrefix());
		hm1.put("page1common", QapWizard.page1Common);
		hm1.put("page1specific", QapWizard.page1Specific);
		hm1.put("reportname", QapWizard.reportName);
		hm1.put("user", ClientUtils.getCurrentUser().getUserLogin());
		hm1.put("OS_Unit", hmpg1.get("OS_Unit"));
		hm1.put("QA_Ref_No", hmpg1.get("QA_Ref_No"));
		hm1.put("Shipping_Release_No", hmpg1.get("Shipping_Release_No"));
		hm1.put("Office_SR_No", hmpg1.get("Office_SR_No"));
		hm1.put("QA_Ref_No_for_Main_Order", hmpg1.get("QA_Ref_No_for_Main_Order"));
		hm1.put("assignee_ids", hmpg1.get("toList"));
		hm1.put("subscriber_ids", hmpg1.get("ccList"));
		return hm1;
	}

	@Override
	public void onInitialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onLeave() {
		// TODO Auto-generated method stub
		return true;
	}

}
