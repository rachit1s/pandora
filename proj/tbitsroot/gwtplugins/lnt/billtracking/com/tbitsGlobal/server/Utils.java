package billtracking.com.tbitsGlobal.server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;
/*
 * Utility Class
 */
public class Utils {

	public static Date incrementTs(Date da,int noOfDays){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(da.getTime());
		cal.add(Calendar.DAY_OF_MONTH,noOfDays);
		Date nda=new Date(cal.getTimeInMillis());
		return nda;
	}

	public static String fieldTrimmer(String fieldName){
		//Pattern p = Pattern.compile("[^A-Za-z0-9_-.,]");
		Pattern p = Pattern.compile("[^\\p{Graph}]");
		Matcher m = p.matcher(fieldName);
		String trimmedOne=m.replaceAll("");
		return trimmedOne;
	
	}
	
	public static ArrayList<Request> getSourceRequestsListFromRelatedRequests(Request request)
	throws DatabaseException, TBitsException {
	
	String srcSysPrefix = null;
	BusinessArea srcBA = null;
	ArrayList<Request> srcReqList = new ArrayList<Request>();
	String relatedRequests = request.getRelatedRequests();
	
	if ((relatedRequests != null) && (relatedRequests.trim().length() != 0)){
		String[] srcRequestsSmartTags = relatedRequests.split(",");
		if (srcRequestsSmartTags != null){						
			for (String srcReqSmartTag : srcRequestsSmartTags){
				
				if(srcReqSmartTag != null){
					int requestId = 0;
					String[] part = srcReqSmartTag.split("#");
					if (part != null){
						if (srcSysPrefix == null){
							srcSysPrefix = part[0];
							srcBA = BusinessArea.lookupBySystemPrefix(srcSysPrefix);
							if (srcBA == null)
								throw new TBitsException("Invalid business area: " + srcSysPrefix);
						}
						
						requestId = Integer.parseInt(part[1]);
						
						if (requestId > 0){
							Request tmpRequest = Request.lookupBySystemIdAndRequestId(srcBA.getSystemId(), requestId);
							srcReqList.add(tmpRequest);
						}
					}
				}
			}
		}
	}
	return srcReqList;
}

}
