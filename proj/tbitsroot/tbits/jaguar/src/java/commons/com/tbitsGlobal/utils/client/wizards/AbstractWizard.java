package commons.com.tbitsGlobal.utils.client.wizards;

import java.util.HashMap;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import commons.com.tbitsGlobal.utils.client.UIContext.DefaultUIContext;

/**
 * 
 * @author sourabh
 * 
 * The abstract class to be extended by all the Wizards
 */

public abstract class AbstractWizard extends Window{
	protected String sysPrefix;
	
	protected CardLayout layout;
	protected HashMap<Integer, IWizardPage<? extends LayoutContainer, ?>> pages;
	protected IWizardPage<? extends LayoutContainer, ?> activePage;
	
	protected Button backBtn;
	protected Button nextBtn;
	protected Button finishBtn;
	protected Button previewPDFBtn;
	protected Button previewDOCBtn;
	
	
	protected DefaultUIContext context;
	public static String CONTEXT_WIZARD = "wizard";
	
	/**
	 *	Constructor for the class
	 */
	protected AbstractWizard() {
		super();
		this.setHeading("Transmittal Wizard");
		this.setModal(true);
		layout = new CardLayout();
		this.setLayout(layout);
		this.setWidth(com.google.gwt.user.client.Window.getClientWidth() - 100);
		this.setHeight(com.google.gwt.user.client.Window.getClientHeight() - 100);
		
		context = new DefaultUIContext();
		context.setValue(CONTEXT_WIZARD, this);
		
		pages = new HashMap<Integer, IWizardPage<? extends LayoutContainer, ?>>();
		
		//this.addBackButton();
		//this.addNextButton();
		//this.addFinishButton();
	}
	public  abstract void addBackButton();
	public abstract void  addNextButton();
	public abstract void addFinishButton();
	
	
	/**
	 * This method shows the back button on the bottom of the wizard
	 */
	public void showBackButton(){
		if(backBtn == null){
			addBackButton();
		}else backBtn.show();
	}
	
	/**
	 * This method hides the back button on the bottom of the wizard
	 */
	public void hideBackButton(){
		if(backBtn != null)
			backBtn.hide();
	}
	
	
	/**
	 * This method shows the next button on the bottom of the wizard
	 */
	public void showNextButton(){
		if(nextBtn == null){
			addNextButton();
		}else nextBtn.show();
	}
	
	/**
	 * This method hides the next button on the bottom of the wizard
	 */
	public void hideNextButton(){
		if(nextBtn != null)
			nextBtn.hide();
	}
	/**
	 * This method hides the finish button on the bottom of the wizard
	 */
	public void hideFinishButton(){
		if(finishBtn != null)
			finishBtn.hide();
	}
	
	/**
	 * This method shows the finish button on the bottom of the wizard
	 */
	public void showFinishButton(){
		if(finishBtn == null){
			addFinishButton();
		}else finishBtn.show();
	}
	
	protected abstract void addPreviewPDFButton();
	protected abstract void addPreviewDOCButton();
	
	/**
	 * This method shows the preview button on the bottom of the wizard
	 */
	public void showPreviewPDFButton(){
		if(previewPDFBtn == null){
			addPreviewPDFButton();
		}else previewPDFBtn.show();
	}
	
	public void showPreviewDOCButton(){
		if(previewDOCBtn == null){
			addPreviewDOCButton();
		}else previewDOCBtn.show();
	}
	
	/**
	 * This method hides the preview button on the bottom of the wizard
	 */
	public void hidePreviewDOCButton(){
		if(previewPDFBtn != null)
			previewPDFBtn.hide();
		if(previewDOCBtn != null)
			previewDOCBtn.hide();
	}
	
	
	
	
	
	
	/**
	 * This method adds a page to the wizard
	 * 
	 * @param a {@link IWizardPage}
	 */
	protected void addPage(IWizardPage<? extends LayoutContainer, ?> page){
		this.add(page.getWidget());
		page.onInitialize();
		int displayOrder = page.getDisplayOrder();
		if(pages.containsKey(displayOrder - 1)){
			IWizardPage<? extends LayoutContainer, ?> prePage = pages.get(displayOrder - 1);
			prePage.setNext(page);
			page.setPrevious(prePage);
		}
		if(pages.containsKey(displayOrder + 1)){
			IWizardPage<? extends LayoutContainer, ?> nextPage = pages.get(displayOrder + 1);
			nextPage.setPrevious(page);
			page.setNext(nextPage);
		}
		pages.put(displayOrder,page);
	}
	
	/**
	 * This method is called when the Finish Button is clicked by the user
	 */
	protected abstract void onSubmit();
	
	
}
