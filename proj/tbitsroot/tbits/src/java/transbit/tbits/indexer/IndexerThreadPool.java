package transbit.tbits.indexer;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import transbit.tbits.domain.Request;
/**
 * 
 * @author Nitiraj
 *
 */



/**
 * This class maintains a pool of threads of RequestIndexer
 * and uses them to index items.
 * The main purpose of this class is to provide instant commit
 * by IndexerResourceManager still providing Indexing on demand
 */
public class IndexerThreadPool extends ThreadPoolExecutor
{
	/**
	 * The maximum number of RequestIndexer threads to be allowed
	 * in the system.
	 * NOTE : this should be set from some property of the system (tbits)
	 */
	private static final int MAXIMUMPOOLSIZE = 10 ;
	
	private static final int COREPOOLSIZE = 5 ;
	
	private static int KEEPALIVETIME = 1000 ; // in miliseconds
	
	/**
	 * maintain only single instance of this class  
	 */
	private static IndexerThreadPool myInstance = null ;
	
	/**
	 * This list maintains the waiting list for pending requests for Indexing 
	 * This is passed to ThreadPoolExecutor for maintaining queue
	 */
	private static BlockingQueue<Runnable> waitList = new LinkedBlockingQueue<Runnable>() ; 
	
	/**
	 * private constructor for singleton class
	 */
	private IndexerThreadPool()
	{		
		super(COREPOOLSIZE, MAXIMUMPOOLSIZE, KEEPALIVETIME, TimeUnit.MILLISECONDS, waitList ) ;	
	}
	
	public static synchronized IndexerThreadPool getInstance()
	{
		if( null == myInstance )
		{
			myInstance = new IndexerThreadPool() ;							
		}
		
		return myInstance ;		
	}
	
	public void submitRequestsForIndexing( ArrayList<RequestIndexer> reqIndList ) 
	{
		for( RequestIndexer reqInd : reqIndList )
			submit( reqInd ) ;			
	}
	
}