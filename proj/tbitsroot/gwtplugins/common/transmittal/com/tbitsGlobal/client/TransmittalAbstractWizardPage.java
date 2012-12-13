package transmittal.com.tbitsGlobal.client;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;


public abstract class TransmittalAbstractWizardPage <T extends LayoutContainer, V> implements ITransmittalWizardPage<T, V> {
	protected T widget;
	protected UIContext wizardContext;
	protected ITransmittalWizardPage<? extends LayoutContainer, ?> prePage;
	protected ITransmittalWizardPage<? extends LayoutContainer, ?> nextPage;
	
	private TransmittalAbstractWizardPage() {
		initializeWidget();
	}
	
	protected TransmittalAbstractWizardPage(UIContext wizardContext) {
		this();
		this.wizardContext = wizardContext;
	}
	public ITransmittalWizardPage<? extends LayoutContainer, ?> getNext() {
		return nextPage;
	}

	
	public ITransmittalWizardPage<? extends LayoutContainer, ?> getPrevious() {
		return prePage;
	}

	
	public void setNext(ITransmittalWizardPage<? extends LayoutContainer, ?> nextPage) {
		this.nextPage = nextPage;
	}

	
	public void setPrevious(ITransmittalWizardPage<? extends LayoutContainer, ?> prePage) {
		this.prePage = prePage;
	}
}

