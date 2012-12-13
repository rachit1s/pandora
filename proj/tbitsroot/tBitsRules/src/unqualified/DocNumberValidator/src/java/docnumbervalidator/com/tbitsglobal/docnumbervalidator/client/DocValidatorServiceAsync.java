package docnumbervalidator.com.tbitsglobal.docnumbervalidator.client;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DocValidatorServiceAsync {
	void testNumbers(String sysPrefix, String fieldName, ArrayList<String> numbers, AsyncCallback<HashMap<String, Integer>> callback);
}
