package transmittal.com.tbitsGlobal.client.admin.wizard;

import java.util.List;

import transmittal.com.tbitsGlobal.client.models.TrnPostProcessValue;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizardPage;

public class CreateProcessPage3 extends AbstractWizardPage<LayoutContainer, List<TrnPostProcessValue>> {

	protected CreateProcessPage3Panel cp;
	protected boolean canContinue;
	
	protected CreateProcessPage3(UIContext wizardContext) {
		super(wizardContext);
		cp = new CreateProcessPage3Panel();
		canContinue = false;
	}

	public void buildPage() {
		widget.add(cp, new FitData());
		
	}

	public int getDisplayOrder() {
		return 2;
	}

	public List<TrnPostProcessValue> getValues() {
		return cp.getPostTrnFieldMap();
	}

	public LayoutContainer getWidget() {
		return widget;
	}

	public void initializeWidget() {
		widget = new LayoutContainer(new FitLayout());	
		widget.setScrollMode(Scroll.AUTO);
	}

	public void onDisplay() {
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).hideFinishButton();
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).showNextButton();
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).showBackButton();
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).hidePreviewDOCButton();
	}

	public void onInitialize() {
		buildPage();
	}

	public boolean onLeave() {
		validateValues();
		return canContinue;
	}
	
	protected void validateValues(){
		if(0 == getValues().size()){
			TbitsInfo.error("No values inserted in Post Transmittal Field Map");
			return;
		}
		canContinue = true;
	}
}
