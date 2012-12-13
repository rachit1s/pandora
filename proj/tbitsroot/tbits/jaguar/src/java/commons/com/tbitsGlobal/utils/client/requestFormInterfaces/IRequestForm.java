package commons.com.tbitsGlobal.utils.client.requestFormInterfaces;

import com.google.gwt.user.client.ui.Widget;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;


/**
 * 
 * @author sourabh
 * 
 * Interface to be extended by forms that intend to add/update/view request
 */
public interface IRequestForm{
	
	public static String CONTEXT_PARENT_TAB		=	"parent_tab";
	
	/**
	 * @return The widget which contains the form
	 */
	public Widget getWidget() ;
	
	/**
	 * Recreate the form useing the requestModel
	 * @param requestModel
	 */
	public void reCreate(TbitsTreeRequestData requestModel) ;
	
	/**
	 * @return The instance of data object.
	 */
	public IRequestFormData getData();
}
