package transbit.tbits.ExtUI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public interface IUpdateRequestFooterSlotFiller extends ISlotFiller 
{
	public String getUpdateRequestFooterHtml( HttpServletRequest httpRequest, HttpServletResponse httpResponse, BusinessArea ba, Request oldRequest, User user) ;
	
	public double getUpdateRequestFooterSlotFillerOrder() ;
}
