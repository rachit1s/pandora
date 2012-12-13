/**
 * 
 */
package transbit.tbits.events;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class BeforePasswordChangeEvent implements IEvent 
{
	private String password;
	private String userLogin;
	/**
	 * @param password
	 * @param userLogin
	 */
	public BeforePasswordChangeEvent(String password, String userLogin) {
		super();
		this.password = password;
		this.userLogin = userLogin;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the userLogin
	 */
	public String getUserLogin() {
		return userLogin;
	}
	/**
	 * @param userLogin the userLogin to set
	 */
	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BeforePasswordChangeEvent [password=" + password
				+ ", userLogin=" + userLogin + "]";
	}
}
