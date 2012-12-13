package docnumbervalidator.com.tbitsglobal.docnumbervalidator.client;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.RemoteService;

public interface DocValidatorService extends RemoteService {
	public HashMap<String, Integer> testNumbers(String sysPrefix, String fieldName, ArrayList<String> numbers);
}
