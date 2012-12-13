package transbit.tbits.common.readerizer;

import java.io.File;
import java.io.Reader;


public interface IReaderizer {
	
	Reader getReader(File inputFile) throws Exception;

}
