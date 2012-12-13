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
public interface IPreRequestCommitEvent extends IEvent 
{
//
//	private Connection con;
//	private BusinessArea ba;
//	private Request oldRequest;
//	private Request currentRequest;
//	private int source;
//	private User user;
//	private boolean isAddRequest;
//
//	/**
//	 * @param con
//	 * @param ba
//	 * @param oldRequest
//	 * @param currentRequest
//	 * @param source
//	 * @param user
//	 * @param isAddRequest
//	 */
//	public PreRequestCommitEvent(Connection con, BusinessArea ba, Request oldRequest,
//			Request currentRequest, int source, User user, boolean isAddRequest) {
//		super();
//		this.con = con;
//		this.ba = ba;
//		this.oldRequest = oldRequest;
//		this.currentRequest = currentRequest;
//		this.source = source;
//		this.user = user;
//		this.isAddRequest = isAddRequest;
//	}
//
//
//
//	/**
//	 * @return the con
//	 */
//	public Connection getConnection() {
//		return con;
//	}
//
//
//
//	/**
//	 * @return the ba
//	 */
//	public BusinessArea getBa() {
//		return ba;
//	}
//
//
//
//	/**
//	 * @return the oldRequest
//	 */
//	public Request getOldRequest() {
//		return oldRequest;
//	}
//
//
//
//	/**
//	 * @return the currentRequest
//	 */
//	public Request getCurrentRequest() {
//		return currentRequest;
//	}
//
//
//
//	/**
//	 * @return the source
//	 */
//	public int getSource() {
//		return source;
//	}
//
//
//
//	/**
//	 * @return the user
//	 */
//	public User getUser() {
//		return user;
//	}
//
//
//
//	/**
//	 * @return the isAddRequest
//	 */
//	public boolean isAddRequest() {
//		return isAddRequest;
//	}
//
//
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#toString()
//	 */
//	@Override
//	public String toString() {
//		return "RequestEvent [con=" + con + ", ba=" + ba + ", oldRequest="
//				+ oldRequest + ", currentRequest=" + currentRequest
//				+ ", source=" + source + ", user=" + user + ", isAddRequest="
//				+ isAddRequest + "]";
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
