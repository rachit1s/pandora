package commons.com.tbitsGlobal.utils.client.requestFormInterfaces;

/**
 * 
 * @author sourabh
 * 
 * Interface to be extended by forms that intend to view request
 */
public interface IViewRequestForm extends IRequestForm 
{
	/**
	 * Registers user read action when the form is opened
	 */
	public void registerReadAction();
	
	/**
	 * Refreshes the form for updated data
	 */
	public void refresh();
}
