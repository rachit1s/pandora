package transbit.tbits.ExtUI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public interface IActionDetailsHeaderSlotFiller extends ISlotFiller
{
	public String getActionDetailsHeaderHtml( HttpServletRequest httpRequest, HttpServletResponse httpResponse, BusinessArea ba, Request request, User user ) ;
	
	public double getActionDetailsHeaderOrder() ;
}
