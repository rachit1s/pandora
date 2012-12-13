package transbit.tbits.Helper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.exception.TBitsException;

/**
 * Utility for making deep copies (vs. clone()'s shallow copies) of
 * objects. Objects are first serialized and then deserialized. Error
 * checking is fairly minimal in this implementation. If an object is
 * encountered that cannot be serialized (or that references an object
 * that cannot be serialized) an error is printed to System.err and
 * null is returned. Depending on your specific application, it might
 * make more sense to have copy(...) re-throw the exception.
 */
public class SerialObjectCloner 
{
	private static TBitsLogger LOG = TBitsLogger.getLogger("transbit.tbits.Helper");
    /**
     * Returns a copy of the object, or Exception if the object cannot
     * be serialized.
     * @throws TBitsException 
     */
    public static Object copy(Object orig) throws TBitsException {
    	
    	if( null == orig )
    		return null ;
    	
        Object obj = null;
        try 
        {
            // Write the object out to a byte array
            FastByteArrayOutputStream fbos =
                    new FastByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(fbos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Retrieve an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in =
                new ObjectInputStream(fbos.getInputStream());
            obj = in.readObject();
            in.close() ;
            return obj;
        }
        catch(Exception e) 
        {
        	LOG.info("",(e)) ;
        	throw new TBitsException( e ) ;
        }       
    }

}
