package transmittal.com.tbitsGlobal.server.cache;

import java.util.Collection;

import net.sf.ehcache.CacheEntry;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.writer.CacheWriter;

/**
 * Handles the modifications to PostTransmittalFieldMap table
 * Implement the writeback methods to update the changes in configuration
 * back to database
 * @author devashish
 *
 */
public class PostTrnFieldMapWriter implements CacheWriter {

	public CacheWriter clone(Ehcache cache) throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	public void delete(CacheEntry entry) throws CacheException {
		// TODO Auto-generated method stub

	}

	public void deleteAll(Collection<CacheEntry> entries) throws CacheException {
		// TODO Auto-generated method stub

	}

	public void dispose() throws CacheException {
		// TODO Auto-generated method stub

	}

	public void init() {
		// TODO Auto-generated method stub

	}

	public void write(Element element) throws CacheException {
		// TODO Auto-generated method stub

	}

	public void writeAll(Collection<Element> elements) throws CacheException {
		// TODO Auto-generated method stub

	}

}
