package transbit.tbits.filegc;

import java.io.File;
import java.util.Hashtable;

/**
 * The listener of various file garbage collection events 
 * @author sandeepgiri
 *
 */
public interface IRepoFileDeletionFilter {
	
	/**
	 * @param toBeDeleted
	 * @return Returns the filtered files
	 */
	public Hashtable<Integer, File> filterFilesToBeDeleted(Hashtable<Integer, File> toBeDeleted);
	
}
