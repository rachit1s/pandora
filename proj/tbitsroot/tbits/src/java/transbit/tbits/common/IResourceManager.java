/**
 * @author Nitiraj Singh Rathore
 *
 */
package transbit.tbits.common;


/**
 * This interface is implemented by all the other resource
 * managers in tbits.
 */
public interface IResourceManager 
{
	
	public void commit() ;
	
	
	public void rollback() ;
	
}
