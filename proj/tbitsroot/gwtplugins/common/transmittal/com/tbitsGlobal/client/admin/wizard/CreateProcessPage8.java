package transmittal.com.tbitsGlobal.client.admin.wizard;

import java.util.List;

import transmittal.com.tbitsGlobal.client.models.TrnValidationRule;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizard;
import commons.com.tbitsGlobal.utils.client.wizards.AbstractWizardPage;

public class CreateProcessPage8 extends AbstractWizardPage<LayoutContainer, List<TrnValidationRule>> {

	protected CreateProcessPage8Panel cp;
	protected boolean canContinue;
	
	protected CreateProcessPage8(UIContext wizardContext) {
		super(wizardContext);
		cp = new CreateProcessPage8Panel();
		canContinue = false;
	}

	public void buildPage() {
		widget.add(cp, new FitData());
	}

	public int getDisplayOrder() {
		return 7;
	}

	public List<TrnValidationRule> getValues() {
		return cp.getValidationRules();
	}

	public LayoutContainer getWidget() {
		return widget;
	}

	public void initializeWidget() {
		widget = new LayoutContainer(new FitLayout());	
		widget.setScrollMode(Scroll.AUTO);
	}

	public void onDisplay() {
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).showFinishButton();
		wizardContext.getValue(AbstractWizard.CONTEXT_WIZARD, AbstractWizard.class).hideNextButton();
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
			TbitsInfo.info("No values inserted in Validation Rules Table");
			
		}
		
		canContinue = true;
	}

}
