/**
 * 
 */
package transbit.tbits.events;

import transbit.tbits.domain.User;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class BeforeUserUpdateEvent implements IEvent 
{
	User user;

	/**
	 * @param user
	 */
	public BeforeUserUpdateEvent(User user) {
		super();
		this.user = user;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BeforeUserUpdateEvent [user=" + user + "]";
	}
	
}
