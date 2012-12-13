/**
 * @author Nitiraj Singh Rathore
 *
 */

package transbit.tbits.indexer;

import java.util.ArrayList;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.IResourceManager;
import transbit.tbits.common.PropertiesHandler;


public class IndexerResourceManager implements IResourceManager 
{
	private ArrayList<RequestIndexer> requestIndexerList ;
	
	public IndexerResourceManager()
	{
		requestIndexerList = new ArrayList<RequestIndexer>() ;		 
	}
	
	/**
	 * Queues the incoming request for indexing
	 * @param aSysPrefix
	 * @param aSystemId
	 * @param aRequestId
	 * @param aPrimaryIndexLocation
	 */
	public void queueForIndexing( String aSysPrefix, int aSystemId, int aRequestId,
			String aPrimaryIndexLocation  ) 
	{
		RequestIndexer reqInd = new RequestIndexer( aSysPrefix, aSystemId, aRequestId, aPrimaryIndexLocation ) ;
		synchronized( requestIndexerList )
		{
			requestIndexerList.add( reqInd ) ;
		}
	}	
	
	/**
	 * commits all the requests in the request list for indexing
	 */
	public void commit() 
	{		
		// to be enabled after fixing the bug in RequestIndexer.addDocstoIndex
		IndexerThreadPool itp = IndexerThreadPool.getInstance() ;
		synchronized ( requestIndexerList ) 
		{		
			itp.submitRequestsForIndexing(requestIndexerList) ;	
			requestIndexerList.clear() ;
		}
	}

	/**
	 * clears the request list in case of rollback
	 */
	public void rollback() 
	{	
		requestIndexerList = new ArrayList<RequestIndexer>() ;
	}

}
