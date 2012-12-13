package billtracking.com.tbitsGlobal.server.rules;

import static billtracking.com.tbitsGlobal.server.BillProperties.billProperties;

import java.sql.Connection;
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
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import billtracking.com.tbitsGlobal.shared.IBillConstants;
import billtracking.com.tbitsGlobal.shared.IBillProperties;

public class BillPostRule implements IPostRule,IBillProperties,IBillConstants{
	private static final String TBITS_ROOT = "root";
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {

		if(ba!=null){
			String baPrefix=ba.getSystemPrefix();
			String billPrefix=billProperties.get(PROPERTY_BILL_BA_PREFIX);
			if(billPrefix.equalsIgnoreCase(baPrefix)){
				String description=" Bill Transferred for Approval flow  ["+ba.getSystemPrefix()+"#"+currentRequest.getRequestId()+"]";
				updateSourceRequests(currentRequest,oldRequest,description,isAddRequest);
			}
			return new RuleResult(true , this.getName()+"completed" , true );
		}else			
			return new RuleResult(true,this.getName()+ " : ba passed was null." , false ) ;
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

	/**
	 * @param ba
	 * @param currentRequest
	 */
	private void updateSourceRequests(Request currentRequest,Request oldRequest,String description,Boolean isAddRequest) {
		{				
			String logger = getUpdateLogger(currentRequest);
			try {
				String srcSysPrefix = null;
				BusinessArea srcBA = null;
				String relatedRequests = currentRequest.getRelatedRequests();

				if ((relatedRequests != null) && (relatedRequests.trim().length() != 0)){
					String[] srcRequestsSmartTags = relatedRequests.split(",");
					if (srcRequestsSmartTags != null){						
						for (String srcReqSmartTag : srcRequestsSmartTags){

							if(srcReqSmartTag != null){
								int requestId = 0;
								String[] part = srcReqSmartTag.split("#");
								if (part != null){
									srcSysPrefix = part[0];
									srcBA = BusinessArea.lookupBySystemPrefix(srcSysPrefix);
									if (srcBA == null)
										throw new TBitsException("Invalid business area: " + srcSysPrefix);
									requestId = Integer.parseInt(part[1]);

									if (requestId > 0){
										Request req = Request.lookupBySystemIdAndRequestId(srcBA.getSystemId(), requestId);
										String poPrefix=billProperties.get(PROPERTY_PO_BA_PREFIX);
										String subPrefix=billProperties.get(PROPERTY_SUBMISSION_BA_PREFIX);

										if(srcSysPrefix.equals(poPrefix))
											updatePORequest(description, logger, req,isAddRequest);
										else
											if(srcSysPrefix.equals(subPrefix))
												updateSubmissionRequest(currentRequest,oldRequest,description, logger, req,isAddRequest);

									}

								}

							}
						}
					}
				}
			}

			catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TBitsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}


	private void updateSubmissionRequest(Request currentBTRequest,Request oldBTRequest, String description, String logger,
			Request billSubmRequest,Boolean isAddRequest) throws TBitsException {
		{
			Hashtable<String,String> params = new Hashtable<String,String>();
			//params.put(Field.DESCRIPTION,description);
			params.put(Field.REQUEST,billSubmRequest.getRequestId()+"");
			params.put(Field.USER,logger);
			params.put(Field.BUSINESS_AREA,billSubmRequest.getSystemId()+"");

			Type status=null;
			if(oldBTRequest == null)
				status = (Type) currentBTRequest.getObject(Statuswithdepartments);
			else
				status = (Type) oldBTRequest.getObject(Statuswithdepartments);

			Type exPMRecomm = (Type) currentBTRequest.getObject(ExPMReco);
			Type ppmRecomm = (Type) currentBTRequest.getObject(PPMReco);
			Type paymentClearance = (Type) currentBTRequest.getObject("PaymentClearance");

			if(status.getName().equals(Type_Status_with_departments_Closed))
				params.put(Field.STATUS, "Closed");
			else if(status.getName().equals(Type_Status_with_departments_Pendingwithexecution))
			{
				if(exPMRecomm.getName().equals(Type_Ex_PM_Reco_Approved))
					params.put(Field.STATUS, "Approved");
			}
			else if(status.getName().equals(Type_Status_with_departments_Pendingwithscm)){
				if(ppmRecomm.getName().equals(Type_PPM_Reco_Approved))
					params.put(Field.STATUS, "Authorized");
			}
			else if(status.getName().equals(Type_Status_with_departments_Pendingwithfinance)){
				if(paymentClearance.getName().equals("A"))
					params.put(Field.STATUS, "Closed");
			}
			if(params.get(Field.STATUS)==null)
				params.put(Field.STATUS,"Active");

			String billSubmissionStatus = ((Type)billSubmRequest.getObject(Field.STATUS)).getName();
			String billStatus = params.get(Field.STATUS);

			if ( 
				!billSubmissionStatus.equals(billStatus) 
					//HACK: This is temporary hack for the cases when Certifier is being assigned, the bill submisstion's status changes from Approved to Active.
					&& !(billSubmissionStatus.equals("Approved") && billStatus.equals("Active"))
				) 
			{
					UpdateRequest updater = new UpdateRequest();
					try {
						updater.updateRequest(params);
					} catch (APIException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
	}


	private void updatePORequest(String description, String logger,
			Request req,Boolean isAddRequest) throws TBitsException {

		if (isAddRequest) {
			Hashtable<String, String> params = new Hashtable<String, String>();
			params.put(Field.DESCRIPTION, description);
			params.put(Field.REQUEST, req.getRequestId() + "");
			params.put(Field.USER, logger);
			params.put(Field.BUSINESS_AREA, req.getSystemId() + "");
			UpdateRequest updater = new UpdateRequest();
			try {
				updater.updateRequest(params);
			} catch (APIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	public String getName() {
		// TODO Auto-generated method stub
		return "BillPostRule";
	}

	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
