package transbit.tbits.ExtUI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;

public interface IAddRequestFooterSlotFiller extends ISlotFiller 
{
	/**
	 * 
	 * @param httpRequest
	 * @param httpResponse
	 * @param ba
	 * @param user
	 * @return the hashtable containing the <tag,html> pairs to be replaced in the add-request page 
	 */
	public String getAddRequestFooterHtml( HttpServletRequest httpRequest, HttpServletResponse httpResponse, BusinessArea ba, User user );
	
	/**
	 * 
	 * @return the order of execution relative to other IAddRequestSlotFiller implementations
	 */
	public double getAddRequestFooterSlotFillerOrder() ;

}
