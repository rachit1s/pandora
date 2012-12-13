package commons.com.tbitsGlobal.utils.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import commons.com.tbitsGlobal.utils.client.log.Log;

/**
 * 
 * @author sourabh
 * 
 * A handler that catches all exceptions that escape our try/catches
 */
public class TbitsUncaughtExceptionHandler implements UncaughtExceptionHandler {

	public void onUncaughtException(Throwable e) {
//		TbitsLog.write(e.getMessage(), TbitsLog.ERROR);
		Log.error(e.getMessage(), e);
		GWT.log(e.getMessage(), e);
//		System.out.println(e.getMessage());
//		e.printStackTrace();
	}
	
}
