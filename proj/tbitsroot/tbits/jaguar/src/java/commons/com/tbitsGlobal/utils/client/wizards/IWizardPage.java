package commons.com.tbitsGlobal.utils.client.wizards;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

/**
 * 
 * @author sourabh
 *
 * @param a panel class T such that <T extends {@link LayoutContainer}>
 * 
 * This interface must be implemented by every page of the wizard
 */
public interface IWizardPage<T extends LayoutContainer, V>{
	
	/**
	 * @return T such that <T extends {@link LayoutContainer}>
	 * 
	 * In almost all cases this method should return this
	 */
	public T getWidget();
	
	public void setParentWizard(AbstractWizard aw);
	public AbstractWizard getParentWizard();
	
	public void initializeWidget();
	
	/**
	 * This method builds that page by adding UI components to the page
	 */
	public void buildPage();
	
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
	public IWizardPage<? extends LayoutContainer, ?> getPrevious();
	
	/**
	 * This method returns the next page in order
	 * @return the next page
	 */
	public IWizardPage<? extends LayoutContainer, ?> getNext();
	
	/**
	 * This method sets the previous page
	 * @param the previous page
	 */
	public void setPrevious(IWizardPage<? extends LayoutContainer, ?> prePage);
	
	/**
	 * This method sets the next page
	 * @param the next page
	 */
	public void setNext(IWizardPage<? extends LayoutContainer, ?> nextPage);
	
	/**
	 * Returns the relevant data or information from current page
	 * @return data
	 * 
	 * TODO : this function is rubbish. Need to get a better mechanism to collect data from pages
	 */
	public V getValues();
	
	public  boolean canMoveToNext();
}
