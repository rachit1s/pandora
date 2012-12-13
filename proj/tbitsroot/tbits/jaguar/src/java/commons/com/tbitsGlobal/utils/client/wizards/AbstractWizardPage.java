package commons.com.tbitsGlobal.utils.client.wizards;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;

public abstract class AbstractWizardPage<T extends LayoutContainer, V> implements IWizardPage<T, V> {
	protected T widget;
	protected UIContext wizardContext;
	protected IWizardPage<? extends LayoutContainer, ?> prePage;
	protected IWizardPage<? extends LayoutContainer, ?> nextPage;
	protected AbstractWizard parentWizard;
	
	
	public AbstractWizard getParentWizard() {
		return parentWizard;
	}

	
	public void setParentWizard(AbstractWizard parentWizard) {
		this.parentWizard = parentWizard;
	}

	private AbstractWizardPage() {
		initializeWidget();
	}
	
	protected AbstractWizardPage(UIContext wizardContext) {
		this();
		this.wizardContext = wizardContext;
	}
	
	
	public IWizardPage<? extends LayoutContainer, ?> getNext() {
		return nextPage;
	}

	
	public IWizardPage<? extends LayoutContainer, ?> getPrevious() {
		return prePage;
	}

	
	public void setNext(IWizardPage<? extends LayoutContainer, ?> nextPage) {
		this.nextPage = nextPage;
	}

	
	public void setPrevious(IWizardPage<? extends LayoutContainer, ?> prePage) {
		this.prePage = prePage;
	}
	public  boolean canMoveToNext()
	{
		return true;
	}
}
