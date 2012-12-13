package transmittal.com.tbitsGlobal.client;


import com.extjs.gxt.ui.client.widget.LayoutContainer;
import transmittal.com.tbitsGlobal.client.WizardData;

public interface ITransmittalWizardPage <T extends LayoutContainer, V>{
	
	/**
	 * @return T such that <T extends {@link LayoutContainer}>
	 * 
	 * In almost all cases this method should return this
	 */
	public T getWidget();
	
	public void initializeWidget();
	
	/**
	 * This method builds that page by adding UI components to the page
	 */
	public void buildPage(WizardData data);
	
	/**
	 * @return the order in which the page should be displayed. The order starts from 0.
	 */
	public int getDisplayOrder();
	
	/**
	 * This method is called when the page is added to the Wizard at the time of creation
	 * All the prefilling operations must be done here.
	 */
	public void onInitialize();
	
	/**
	 * This method is called when the page is displayed via next or back button of the wizard
	 */
	public void onDisplay();
	
	/**
	 * This method is called when the user leaves current page to go to some other page
	 * @return true if the user can be allowed to move to the next or previous page
	 */
	public boolean onLeave();
	
	/**
	 * This method returns the previous page in order
	 * @return the previous page
	 */
	public ITransmittalWizardPage<? extends LayoutContainer, ?> getPrevious();
	
	/**
	 * This method returns the next page in order
	 * @return the next page
	 */
	public ITransmittalWizardPage<? extends LayoutContainer, ?> getNext();
	
	/**
	 * This method sets the previous page
	 * @param the previous page
	 */
	public void setPrevious(ITransmittalWizardPage<? extends LayoutContainer, ?> prePage);
	
	/**
	 * This method sets the next page
	 * @param the next page
	 */
	public void setNext(ITransmittalWizardPage<? extends LayoutContainer, ?> nextPage);
	
	/**
	 * Returns the relevant data or information from current page
	 * @return data
	 * 
	 * TODO : this function is rubbish. Need to get a better mechanism to collect data from pages
	 */
	public V getValues();

	public boolean funcToBeCalledOnBack();
}

