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
public class PreEvent implements IPreRequestCommitEvent
{
	
	private Connection con;
	private BusinessArea ba;
	private Request oldRequest;
	private Request currentRequest;
	private int source;
	private User user;
	private boolean isAddRequest;
	private TBitsResourceManager tBitsResourceManager;

	/**
	 * @param con
	 * @param ba
	 * @param oldRequest
	 * @param currentRequest
	 * @param source
	 * @param user
	 * @param isAddRequest
	 */
	public PreEvent(Connection con, BusinessArea ba, Request oldRequest,
			Request currentRequest, int source, User user, boolean isAddRequest, TBitsResourceManager tBitsResourceManager) {
		super();
		this.con = con;
		this.ba = ba;
		this.oldRequest = oldRequest;
		this.currentRequest = currentRequest;
		this.source = source;
		this.user = user;
		this.isAddRequest = isAddRequest;
		this.tBitsResourceManager = tBitsResourceManager;
	}



	/**
	 * @return the con
	 */
	public Connection getConnection() {
		return con;
	}



	/**
	 * @return the ba
	 */
	public BusinessArea getBa() {
		return ba;
	}



	/**
	 * @return the oldRequest
	 */
	public Request getOldRequest() {
		return oldRequest;
	}



	/**
	 * @return the currentRequest
	 */
	public Request getCurrentRequest() {
		return currentRequest;
	}



	/**
	 * @return the source
	 */
	public int getSource() {
		return source;
	}



	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}



	/**
	 * @return the isAddRequest
	 */
	public boolean isAddRequest() {
		return isAddRequest;
	}

	@Override
	public TBitsResourceManager getTBitsResourceManager() {
		return tBitsResourceManager;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PreEvent [con=" + con + ", ba=" + ba + ", source=" + source + ", user=" + user + ", isAddRequest="
				+ isAddRequest + "]";
	}
}
