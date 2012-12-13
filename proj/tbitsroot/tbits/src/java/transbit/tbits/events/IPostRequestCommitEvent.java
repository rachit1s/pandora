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
public interface IPostRequestCommitEvent extends IEvent
{

//	/**
//	 * @param con
//	 * @param ba
//	 * @param oldRequest
//	 * @param currentRequest
//	 * @param source
//	 * @param user
//	 * @param isAddRequest
//	 */
//	public PostRequestCommitEvent(Connection con, BusinessArea ba,
//			Request oldRequest, Request currentRequest, int source, User user,
//			boolean isAddRequest) {
//		super(con, ba, oldRequest, currentRequest, source, user, isAddRequest);
//		// TODO Auto-generated constructor stub
//	}

	public Connection getConnection();
	
	/**
	 * @return the ba
	 */
	public BusinessArea getBa();
	
	/**
	 * @return the oldRequest
	 */
	public Request getOldRequest();
	 
	/**
	 * @return the currentRequest
	 */
	public Request getCurrentRequest();
	
	/**
	 * @return the source
	 */
	public int getSource();
	
	/**
	 * @return the user
	 */
	public User getUser();
	
	/**
	 * @return the isAddRequest
	 */
	public boolean isAddRequest();
	
	public TBitsResourceManager getTBitsResourceManager();
}
