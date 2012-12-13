/**
 * 
 */
package transbit.tbits.events;

import java.sql.Connection;

import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class AddPostEvent extends PostEvent implements IAddPostEvent{

	/**
	 * @param con
	 * @param ba
	 * @param oldRequest
	 * @param currentRequest
	 * @param source
	 * @param user
	 * @param isAddRequest
	 */
	public AddPostEvent(Connection con, BusinessArea ba, Request oldRequest,
			Request currentRequest, int source, User user, boolean isAddRequest ,TBitsResourceManager tBitsResourceManager) {
		super(con, ba, oldRequest, currentRequest, source, user, isAddRequest, tBitsResourceManager);
		// TODO Auto-generated constructor stub
	}

}
