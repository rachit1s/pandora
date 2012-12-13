package corrGeneric.com.tbitsGlobal.server.cache;

import java.util.Hashtable;
import java.util.Iterator;

import corrGeneric.com.tbitsGlobal.server.interfaces.ICache;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.*;

/**
 * 
 * @author nitiraj
 * This cache is different from AbstractLRUCache in the sense
 * that it expands to accomodate everything and never remove 
 * any un-used objects 
 */
public abstract class AbstractCache<T,S> implements ICache<T,S> 
{

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AbstractCache [ currentSize=" + size() + ", map=" + map + "]";
	}

	private Hashtable<T,S> map = null;
	
	protected AbstractCache()
	{
		map = new Hashtable<T,S>();
	}
	
	/**
	 * will throw null pointer exception if t is null.
	 * not checking for null values to improve performance 
	 * @throws CorrException 
	 */
	public S get(T t) throws CorrException 
	{
		Utility.LOG.debug("request for : " + t );
		S s = map.get(t);
		
		if( null == s )
		{
			Utility.LOG.debug("not found in cache : " + t + ". Searching ....");
			s = search(t);
			if(null != s)
			{
				insert(t,s);
			}
		}
		
		Utility.LOG.debug("returning : " + s );
		return s ;
	}

	/**
	 * Inserts object=s for key=t and returns previous value
	 * @param t
	 * @param s
	 * @return
	 * throws null pointer ex. if s / t is null.
	 */
	public S insert(T t, S s) 
	{
		Utility.LOG.debug("Inserting : " + s + " for " + t);
		return map.put(t, s);
	}
	
	/**
	 * removes mapping for t and returns the previous value
	 * throws null pointer ex. if t is null.
	 * @param t
	 * @return
	 */
	public S remove(T t)
	{
		Utility.LOG.debug("Removing mapping for : " + t);
		return map.remove(t);
	}

	public final Iterator<T> iterator()
	{
		return map.keySet().iterator();
	}
	
	public final int size()
	{
		return map.size();
	}
	
	public void clear()
	{
		map.clear();
	}
	
	protected abstract S search(T t) throws CorrException ;
}
