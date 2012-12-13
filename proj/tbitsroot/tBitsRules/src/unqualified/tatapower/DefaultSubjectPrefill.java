/**
 * 
 */
package tatapower;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.domain.IPreRenderer;
import transbit.tbits.exception.TBitsException;

/**
 * @author Lokesh
 *
 */
public class DefaultSubjectPrefill implements IPreRenderer {

	private static final String SUBJECT = "subject";
	private static final String ADD_SUBREQUEST = "add-subrequest";
	private static final String ADD_REQUEST = "add-request";
	private static final int ADD_REQUEST_INDEX = 1;
	private static final int SYSPREFIX_INDEX = 2;

	/* (non-Javadoc)
	 * @see transbit.tbits.domain.IPreRenderer#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.domain.IPreRenderer#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.util.Hashtable, java.util.ArrayList)
	 */
	public void process(HttpServletRequest request,
			HttpServletResponse response, Hashtable<String, Object> tagTable,
			ArrayList<String> tagList) throws TBitsException {		
		
		String uri = request.getRequestURI();
		String[] keys = uri.split("/");
		String sysPrefix = keys[SYSPREFIX_INDEX];
		String parentString = TataPowerUtils.getProperty("tatapower.subject_prefill_sys_prefixes");
		boolean isRuleApplicable = TataPowerUtils.isExistsInString(parentString , sysPrefix);
		String addRequest = keys[ADD_REQUEST_INDEX];
		if (isRuleApplicable && (addRequest.equals(ADD_REQUEST) || addRequest.equals(ADD_SUBREQUEST))){
			tagTable.put(SUBJECT, ".");
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
