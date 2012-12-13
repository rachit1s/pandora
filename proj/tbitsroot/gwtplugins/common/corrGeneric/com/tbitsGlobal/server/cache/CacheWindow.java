package corrGeneric.com.tbitsGlobal.server.cache;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

public class CacheWindow<E>
{
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CacheWindow [ currentSize=" + size() + ", capacity=" + capacity + ", ll=" + ll + "]";
	}

	LinkedList<E> ll = null;
	int capacity = 0;
	
	public CacheWindow( int capacity) 
	{
		if( capacity <= 0 )
			throw new IllegalArgumentException("The capacity of CacheWindow must be positive integer.");
		this.capacity = capacity;
		ll = new LinkedList<E>();
	}
	
	public synchronized void insert( E e )
	{
		if( ll.size() == capacity )
			ll.removeLast();
		
		ll.addFirst(e);
	}
	
	public E findVictim()
	{
//		get the counts of all the items in window
		Hashtable<E,Integer> countMap = new Hashtable<E,Integer>();
		for( Iterator<E> iter = ll.iterator() ; iter.hasNext() ;)
		{
			E e = iter.next();
			if( e != null )
			{
				Integer count = countMap.get(e);
				if( null == count )
					count = 0;
				count++ ;
				countMap.put(e, count);
			}
		}
		
		// find the element with least count
		E victim = null;
		Integer vcount = capacity + 1 ;
		for( Enumeration<E> keys = countMap.keys(); keys.hasMoreElements() ; )
		{
			E key = keys.nextElement() ;
			Integer count = countMap.get(key);
			
			if(count < vcount)
			{	
				victim = key ;
				vcount = count;
			}
		}
		
		return victim ;
	}
	
	public void removeAllOccurrences(E e)
	{
		for( Iterator<E> iter = ll.iterator() ; iter.hasNext() ;)
		{
			E curr = iter.next();
			if(curr.equals(e))
				iter.remove();
		}
	}
	
	public int size()
	{
		return ll.size();
	}
	
	public void clear()
	{
		ll.clear();
	}
	
}
