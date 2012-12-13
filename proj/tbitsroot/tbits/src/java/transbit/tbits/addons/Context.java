/**
 * 
 */
package transbit.tbits.addons;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 * The context interface which will be used through out tbits to pass variable number of parameters to various
 * classes.
 */
public interface Context 
{
	public Object get(Object key);
	public void set(Object key, Object value);
}
