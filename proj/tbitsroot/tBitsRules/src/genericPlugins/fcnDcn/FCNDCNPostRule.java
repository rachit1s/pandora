package fcnDcn;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import transbit.tbits.api.IPostRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
/**
 * 
 * @author sajal
 *
 */
public class FCNDCNPostRule implements IPostRule {

	private static final String TBITS_ROOT = "root";
	public static String FCN_SUFFIX="_FCN";
	public static String DCN_SUFFIX="_DCN";

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {

		if(ba!=null){
			String baPrefix=ba.getSystemPrefix();
			if(baPrefix.endsWith(FCN_SUFFIX)){
				String description="FCN Created via  ["+ba.getSystemPrefix()+"#"+currentRequest.getRequestId()+"]";
				updateSourceRequests(currentRequest,description);
			}else
				if(baPrefix.endsWith(DCN_SUFFIX)){
					String description="DCN Created via  ["+ba.getSystemPrefix()+"#"+currentRequest.getRequestId()+"]";
					updateSourceRequests(currentRequest,description);
				}
			return new RuleResult(true , this.getName()+"completed" , true );
		}else			
			return new RuleResult(true,this.getName()+ " : ba passed was null." , false ) ;
	}

	/**
	 * @param ba
	 * @param currentRequest
	 */
	private void updateSourceRequests(Request currentRequest,String description) {
		{				
			String logger = getUpdateLogger(currentRequest);
			
			ArrayList<Request> sourceRequestList;
			try {
				sourceRequestList = getSourceRequestsListFromRelatedRequests(currentRequest);
				

				for(Request req:sourceRequestList){
					Hashtable<String,String> params = new Hashtable<String,String>();
					params.put(Field.DESCRIPTION,description);
					params.put(Field.REQUEST,req.getRequestId()+"");
					params.put(Field.USER,logger);
					params.put(Field.BUSINESS_AREA,req.getSystemId()+"");
					UpdateRequest updater=new UpdateRequest();
					try {
						updater.updateRequest(params);
					} catch (APIException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TBitsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private String getUpdateLogger(Request currentRequest)
	{
		String logger=new String();
		Collection<RequestUser> loggerList=currentRequest.getLoggers();
		Iterator<RequestUser> i=loggerList.iterator();
		if(i.hasNext()){
			try {
				logger=i.next().getUser().getUserLogin();
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		if(logger==null)
			logger=TBITS_ROOT;
		return logger;
	}

	private static ArrayList<Request> getSourceRequestsListFromRelatedRequests(Request request)
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
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "FCNDCNPostRule";
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
