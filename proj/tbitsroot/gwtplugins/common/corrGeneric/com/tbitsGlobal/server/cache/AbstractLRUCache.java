package corrGeneric.com.tbitsGlobal.server.cache;

import java.util.Iterator;

import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;


public abstract class AbstractLRUCache<T,S> extends AbstractCache<T,S>
{
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AbstractLRUCache [capacity=" + capacity + ", " +  super.toString() + ", window=" + window
				+ "]";
	}

	CacheWindow<T> window = null;
	int capacity = 0 ;
	
	protected AbstractLRUCache(int capacity, int windowSize)
	{
		super();
		
		if( capacity <= 0 )
			throw new IllegalArgumentException("Capacity must be positive integer : give capacity = " + capacity);
		
		if( windowSize <= 0 )
			throw new IllegalArgumentException("Window Size must be positive integer : give window size = " + windowSize);
		
		this.capacity = capacity;
		window = new CacheWindow<T>(windowSize);		
	}
	
	public S get(T t) throws CorrException 
	{
		S s = super.get(t);
		
		if( null != s )
			incrCount(t);
		
		return s ;
	}
	
	private void incrCount(T t) 
	{
		window.insert(t);
	}

	public synchronized S insert(T t, S s) 
	{
		S removedObject = null;
		while( size() >= capacity ) // while instead of if because the victim returned 
		{								// by window might not be present in the map 
			T victim = findVictim();	// in that case we will again findVictim 
			if( null != victim )
			{
				removedObject = remove(victim);
				clearItemFromWindow(victim);
			}
			else
			{
				// remove any object at random
//				Enumeration<T> keys = map.keys() ;
//				T key = keys.nextElement();
//				map.remove(key);
				
				Iterator<T> iter = iterator();
				if( iter.hasNext() )
				{
					T t1 = iter.next();
					removedObject = remove(t1);
				}
			}
		}
		
		return super.insert(t, s);
	}

	private void clearItemFromWindow(T victim) 
	{
		window.removeAllOccurrences(victim);
	}

	private T findVictim() {
		return window.findVictim();
	}
	
	public void clear()
	{
		super.clear();
		window.clear();
	}

}
