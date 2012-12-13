package transmittal.com.tbitsGlobal.client;

import java.util.HashMap;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;

import commons.com.tbitsGlobal.utils.client.wizards.IWizardPage;

public abstract class TransmittalAbstractWizard extends AbstractWizard {

	protected HashMap<Integer, ITransmittalWizardPage<? extends LayoutContainer, ?>> pages;
	protected ITransmittalWizardPage<? extends LayoutContainer, ?> activePage;
	public static String Transmittal_CONTEXT_WIZARD = "wizard";
	protected String sysPrefix;

	protected Button FinishTransmittalForPage1Btn;
	protected Button FinishTransmittalForPage2Btn;
	
	protected DefaultUIContext context;
	
	/**
	 * Constructor for the class
	 */
	protected TransmittalAbstractWizard() {
		super();
		
		context = new DefaultUIContext();
		context.setValue(Transmittal_CONTEXT_WIZARD, this);
		pages = new HashMap<Integer, ITransmittalWizardPage<? extends LayoutContainer, ?>>();

	}

	

	

	/**
	 * This method shows the back button on the bottom of the wizard
	 */
	public void showBackButton() {
		if (backBtn == null) {
			addBackButton();
		} else
			backBtn.show();
	}

	/**
	 * This method hides the back button on the bottom of the wizard
	 */
	public void hideBackButton() {
		if (backBtn != null)
			backBtn.hide();
	}

	/**
	 * This method shows the next button on the bottom of the wizard
	 */
	public void showNextButton() {
		if (nextBtn == null) {
			addNextButton();
		} else
			nextBtn.show();
	}

	/**
	 * This method hides the next button on the bottom of the wizard
	 */
	public void hideNextButton() {
		if (nextBtn != null)
			nextBtn.hide();
	}

	

	/**
	 * This method hides the finish button on the bottom of the wizard
	 */
	public void hideFinishButton() {
		if (finishBtn != null)
			finishBtn.hide();
	}

	/**
	 * This method shows the finish button on the bottom of the wizard
	 */
	public void showFinishButton() {
		if (finishBtn == null) {
			addFinishButton();
		} else
			finishBtn.show();
	}

	/**
	 * This method shows the preview button on the bottom of the wizard
	 */
	public void showPreviewPDFButton() {
		if (previewPDFBtn == null) {
			addPreviewPDFButton();
		} else
			previewPDFBtn.show();
	}

	public void showPreviewDOCButton() {
		if (previewDOCBtn == null) {
			addPreviewDOCButton();
		} else
			previewDOCBtn.show();
	}

	/**
	 * This method hides the preview button on the bottom of the wizard
	 */
	public void hidePreviewButton() {
		if (previewPDFBtn != null)
			previewPDFBtn.hide();
		if (previewDOCBtn != null)
			previewDOCBtn.hide();
	}

	/**
	 * This method shows the preview button on the bottom of the wizard
	 */
	public void showFinishTransmittalButton1() {
		if (FinishTransmittalForPage1Btn == null) {
			addFinishTransmittalButtonForPage1();
		} else
			FinishTransmittalForPage1Btn.show();
	}

	/**
	 * This method hides the preview button on the bottom of the wizard
	 */
	public void hideFinishTransmittalForPage1Btn() {

		if (FinishTransmittalForPage1Btn != null)
			FinishTransmittalForPage1Btn.hide();
	}

	/**
	 * This method shows the preview button on the bottom of the wizard
	 */
	public void showFinishTransmittalForPage2Btn() {
		if (FinishTransmittalForPage2Btn == null) {
			addFinishTransmittalButtonForPage2();
		} else
			FinishTransmittalForPage2Btn.show();
	}

	/**
	 * This method hides the preview button on the bottom of the wizard
	 */
	public void hideFinishTransmittalForPage2Btn() {

		if (FinishTransmittalForPage2Btn != null)
			FinishTransmittalForPage2Btn.hide();
	}
	protected abstract void addFinishTransmittalButtonForPage1();
	protected abstract void addFinishTransmittalButtonForPage2();
	
}
