package transbit.tbits.ExtUI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public interface ISubRequestFooterSlotFiller extends ISlotFiller {

	public String getSubRequestFooterHtml(
			HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			BusinessArea ba, Request parentRequest, User user);

	public double getSubRequestFooterOrder();

}
