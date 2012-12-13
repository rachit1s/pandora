/**
 * 
 */
package transbit.tbits.events;

import transbit.tbits.domain.User;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class BeforeUserInsertEvent implements IEvent 
{
	User user ;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BeforeUserInsertEvent [user=" + user + "]";
	}

	/**
	 * @param user
	 */
	public BeforeUserInsertEvent(User user) {
		super();
		this.user = user;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
}
