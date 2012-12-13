package transbit.tbits.addons;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 * Represents exception thrown while loading a jar file corresponding to the addon
 */
public class AddonLoaderException extends AddonException{

	/**
	 * @param message
	 */
	public AddonLoaderException(String message) {
		super(message);
	}
	
	public AddonLoaderException() {
		super();
	} 
	
	public AddonLoaderException(String message, Throwable throwable) {
		super(message,throwable);
	}
	
	public AddonLoaderException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}
