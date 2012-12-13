package transmittal.com.tbitsGlobal.client;

import java.util.HashMap;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;

public class ReportPreviewPage extends
		TransmittalAbstractWizardPage<FormPanel, HashMap<String, String>> {
	private static final String APPROVAL_ROLE_NAME = "approvalRoleName";
	final HTML birtGeneratedHTML = new HTML();
	public TransmittalWizardConstants twc;
	private WizardData dataObject;

	public WizardData getDataObject() {
		return dataObject;
	}

	public void setDataObject(WizardData dataObject) {
		this.dataObject = dataObject;
	}

	
	public ReportPreviewPage(DefaultUIContext context, WizardData data) {
		super(context);
		this.setDataObject(data);
		buildPage(data);
	}

	public void buildPage() {
		widget.add(birtGeneratedHTML);
	}

	private HashMap<String, String> getParamTable() {
		HashMap<String, String> paramTable = new HashMap<String, String>();

		for (String key : getDataObject().getData().keySet()) {
			if (!(key.equals("BAFields") || key.equals("mapOfRequests")
					|| key.equals("ListOfRequests")
					|| key.equals("currentbaclient")
					|| key.equals("validationRulesList")
					|| key.equals("distributionTableColumnsList")
					|| key.equals("attachmentTableColumnList") || key
					.equals("trnExtendedFields")||key.equals("dropdownlist")||key.equals("transmittalProcessParams"))) {
			System.out.println(key);
			System.out.println( getDataObject().getData().get(key));
				paramTable
						.put(key, (String) (getDataObject().getData().get(key)+""));
			}
		}

		return paramTable;
	}

/*	private void getParamTable(
			ITransmittalWizardPage<? extends LayoutContainer, ?> currentPage,
			HashMap<String, String> paramTable) {

		ITransmittalWizardPage<? extends LayoutContainer, ?> prevPage = currentPage
				.getPrevious();
		if (prevPage != null) {
			HashMap<String, String> values = (HashMap<String, String>) prevPage
					.getValues();
			if (values != null) {
				paramTable.putAll(values);
			}
			getParamTable(prevPage, paramTable);
		}
	}*/

	public int getDisplayOrder() {
		return 3;
	}

	public HashMap<String, String> getValues() {
		return null;
	}

	public FormPanel getWidget() {
		return widget;
	}

	public void initializeWidget() {
		widget = new FormPanel();
		widget.setLabelWidth(150);
		widget.setBodyBorder(false);
		widget.setScrollMode(Scroll.AUTO);
		widget.setHeading("Report Preview Page");

	}

	public void onDisplay() {
		
		
		String status =	(String) getDataObject().getData().get(
		"statusOfFinishButton");
		Boolean flag = Boolean.valueOf(status);
		if (flag) {
			wizardContext.getValue(
					TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
					TransmittalAbstractWizard.class).showFinishButton();
			wizardContext.getValue(TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
					TransmittalAbstractWizard.class).hidePreviewDOCButton();
		} else {
			wizardContext.getValue(
					TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
					TransmittalAbstractWizard.class).showPreviewDOCButton();
			wizardContext.getValue(
					TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
					TransmittalAbstractWizard.class).hideFinishButton();
		}

		HashMap<String, String> paramTable = getParamTable();
		final MessageBox messageBox = MessageBox.wait("Please Wait",
						"Preview is being generated", "Please Wait...");

				getBirtGeneratedHtml(paramTable, messageBox);

				if ((birtGeneratedHTML != null)
						|| (!birtGeneratedHTML.getText().isEmpty()))
					widget.layout();
				else
					ReportPreviewPage.this.onDisplay();

				wizardContext.getValue(TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
						TransmittalAbstractWizard.class).hideNextButton();
				wizardContext.getValue(TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
						TransmittalAbstractWizard.class).showBackButton();
				 wizardContext.getValue(TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
						 TransmittalAbstractWizard.class).hideFinishTransmittalForPage1Btn();
				 wizardContext.getValue(TransmittalAbstractWizard.Transmittal_CONTEXT_WIZARD,
						 TransmittalAbstractWizard.class).hideFinishTransmittalForPage2Btn();
		
						
			
			
		// wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD,
		// AbstractWizard.class).showFinishButton();
	}

	private void getBirtGeneratedHtml(HashMap<String, String> paramTable,
			final MessageBox messageBox) {
		TbitsModelData processParams = (TbitsModelData) getDataObject().getData().get("transmittalProcessParams");
		for (String key : processParams.getPropertyNames()) {
			if (paramTable.get(key) == null) {
				String value = String.valueOf(processParams
						.get(key));
				paramTable.put(key, value);
			}
		}

		TransmittalConstants.dbService.getHTMLTransmittalPreviewUsingBirt(
				paramTable, new AsyncCallback<String>() {

					public void onFailure(Throwable caught) {
						caught.printStackTrace();
						messageBox.close();
						Window
								.alert("Transmittal note preview did not succeed: \n"
										+ caught.getMessage());
					}

					public void onSuccess(String result) {
						messageBox.close();
						birtGeneratedHTML.setHTML(result);
					}
				});
	}

	public void onInitialize() {

	}

	public boolean onLeave() {
		return true;
	}

	@Override
	public void buildPage(WizardData data) {
		widget.add(birtGeneratedHTML);
	}

	@Override
	public boolean 	 funcToBeCalledOnBack() {
	return true;
		
	}
}
