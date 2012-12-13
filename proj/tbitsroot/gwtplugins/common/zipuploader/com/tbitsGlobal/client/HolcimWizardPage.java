package zipuploader.com.tbitsGlobal.client;

import java.util.HashMap;

import transmittal.com.tbitsGlobal.client.TransmittalAbstractWizard;
import transmittal.com.tbitsGlobal.client.TransmittalAbstractWizardPage;
import transmittal.com.tbitsGlobal.client.WizardData;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

public class HolcimWizardPage extends
		TransmittalAbstractWizardPage<FormPanel, HashMap<String, String>> {

	private HolcimAttachmentFieldContainer fieldContainer;
	HolcimWizard parentWizard;

	protected HolcimWizardPage(UIContext wizardContext,
			HolcimWizard holcimWizard) {
		super(wizardContext);
		parentWizard = holcimWizard;

		buildPage(new WizardData());
	}

	public void buildPage() {

	}

	@Override
	public int getDisplayOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public HashMap<String, String> getValues() {
		// TODO Auto-generated method stub
		return null;
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
		widget.setHeaderVisible(false);
		widget.setBodyBorder(false);
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
				TransmittalAbstractWizard.class).hidePreviewDOCButton();
	}

	@Override
	public void onInitialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onLeave() {

		// HolcimMultiUploader.repoFileId = 180;
		if (HolcimMultiUploader.repoFileId != -1) {
			this.nextPage.buildPage(new WizardData());

			return true;
		} else {
			com.google.gwt.user.client.Window
					.alert("Error uploading the file... See logs for more information");
			return false;
		}
	}

	@Override
	public void buildPage(WizardData data) {

		final FormData formData = new FormData();
		formData.setWidth(700);
		LabelField msgBox = new LabelField();
		msgBox.setReadOnly(true);
		msgBox
				.setValue("Please upload the zip file containing the list of documents,Once upload finishes you will be navigated to the grid consisting of the results of uploading ");
		widget.add(msgBox, formData);
		fieldContainer = new HolcimAttachmentFieldContainer(parentWizard);

		widget.add(fieldContainer, formData);

	}

	@Override
	public boolean funcToBeCalledOnBack() {
		// TODO Auto-generated method stub
		return false;
	}

}
