package commons.com.tbitsGlobal.utils.client;

/**
 * 
 * @author sourabh
 * 
 * Serializable Exception that can be transferred over the wire
 */
public class TbitsExceptionClient extends Exception {
	public TbitsExceptionClient() {
		super();
	}

	public TbitsExceptionClient(String message, Throwable cause) {
		super(message, cause);
	}

	public TbitsExceptionClient(String message) {
		super(message);
	}

	public TbitsExceptionClient(Throwable cause) {
		super(cause);
	}
}
