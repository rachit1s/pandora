/**
 * @author Nitiraj Singh Rathore
 *
 */

package transbit.tbits.common;

import transbit.tbits.api.FileResourceManager;
import transbit.tbits.indexer.IndexerResourceManager;

/**
 * Takes all resource Managers and commits or rollbacks all
 * of them at once.
 */
public class TBitsResourceManager implements IResourceManager 
{
	
	private MailResourceManager mailResMgr ;
	private FileResourceManager fileResMgr ; 
	private IndexerResourceManager indexerResMgr ;
	
	/**
	 * Takes all the TBitsResourceManager from other resource managers 
	 * @param mailResMgr
	 * @param fileResMgr
	 * @param indexerResMgr
	 */
	public TBitsResourceManager( MailResourceManager mailResMgr, FileResourceManager fileResMgr, IndexerResourceManager indexerResMgr)
	{
		this.mailResMgr = mailResMgr ;
		this.fileResMgr = fileResMgr ;
		this.indexerResMgr = indexerResMgr ;
	}
	
	/**
	 * Creates the various managers for you
	 */
	public TBitsResourceManager()
	{
		mailResMgr = new MailResourceManager() ;
		fileResMgr = new FileResourceManager(); 
		indexerResMgr = new IndexerResourceManager() ;
	}
	
	public MailResourceManager getMailResourceManager()
	{
		return mailResMgr ;
	}
	
	public FileResourceManager getFileResourceManager()
	{
		return fileResMgr ;
	}
	
	public IndexerResourceManager getIndexerResourceManager()
	{
		return indexerResMgr ;
	}
	/**
	 * Commits all resource Managers
	 */
	public void commit() 
	{
		mailResMgr.commit();
		fileResMgr.commit() ;
		indexerResMgr.commit();
	}

	/**
	 * Rollback all resource Managers
	 */
	public void rollback() 
	{		
		mailResMgr.rollback();
		fileResMgr.rollback() ;
		indexerResMgr.rollback() ;
	}
}
