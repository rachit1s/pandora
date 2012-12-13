package transmittal.com.tbitsGlobal.server.cache;

import java.util.Collection;

import net.sf.ehcache.CacheEntry;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.writer.CacheWriter;

/**
 * This Class is invoked when a data item in the cache is updated/deleted/entered
 * The methods to update/delete/insert the entries are defined in this. The user of
 * the cache is unaware of the underlying implemention of this class.
 * @author devashish
 *
 */
public class SrcTargetFieldMapWriter implements CacheWriter {

	public CacheWriter clone(Ehcache arg0) throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	public void delete(CacheEntry arg0) throws CacheException {
		/**
		 * TODO: Delete specified data from database
		 */

	}

	public void deleteAll(Collection<CacheEntry> arg0) throws CacheException {
		// TODO Auto-generated method stub

	}

	public void dispose() throws CacheException {
		// TODO Auto-generated method stub

	}

	public void init() {
		// TODO Auto-generated method stub

	}

	public void write(Element arg0) throws CacheException {
		/**
		 * TODO: Write data into db
		 */

	}

	public void writeAll(Collection<Element> arg0) throws CacheException {
		/**
		 * TODO: Write all data into db
		 */

	}

}
