package transbit.tbits.ExtUI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;

public interface ISearchResultsHeaderSlotFiller extends ISlotFiller {
	
	public String getSearchResultsHeaderHtml(
			HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			BusinessArea ba, User user) ;

	public int getSearchResultsHeaderOrder();
}
