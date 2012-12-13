package hse.com.tbitsGlobal.server;

import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_PREFIX;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_BRIEF_DESCRIPTION;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_PREFIX;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IPostRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;


public class AIRPostRule implements IPostRule {
	TBitsLogger log = TBitsLogger.getLogger("rule");
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		if(ba.getSystemPrefix().equalsIgnoreCase(AIR_PREFIX)){
			String relatedReqString = currentRequest.get(Field.RELATED_REQUESTS);
			System.out.println("relatedRequests string : " + relatedReqString);
		
			String relCorrReq = HSEUtils.extractRelatedRequestId(relatedReqString,PAR_PREFIX);
			BusinessArea pa=null;
			try {
				pa = BusinessArea.lookupBySystemPrefix(PAR_PREFIX);
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Request parRequest = Request.lookupBySystemIdAndRequestId(pa.getSystemId(),Integer.parseInt(relCorrReq));
				int requestId=parRequest.getRequestId();
				log.info("updating Request_id#"+requestId);			

				Hashtable<String,String> newParams=new Hashtable<String,String>();
				ArrayList<Field> fList = Field.lookupBySystemId(pa.getSystemId());
				for(Field fld:fList){
					if( fld.getIsActive() == false 
							|| fld.getName().equals(Field.HEADER_DESCRIPTION)
							|| fld.getName().equals(Field.MAX_ACTION_ID)
							|| fld.getName().equals(Field.MEMO)
							|| fld.getName().equals(Field.REPLIED_TO_ACTION)
							|| fld.getName().equals(Field.APPEND_INTERFACE)
					)
						continue ;			
					if(fld.getName().equals(Field.RELATED_REQUESTS))
					{
						newParams.put(fld.getName(),AIR_PREFIX+"#"+currentRequest.getRequestId());
						continue;
					}
					
					if(fld.getName().equals(PAR_BRIEF_DESCRIPTION))
					{
						newParams.put(fld.getName(),"*AIR has been generated for this PAR <BR><BR>"+currentRequest.get(PAR_BRIEF_DESCRIPTION));
						continue;
					}

					String fieldValue=parRequest.get(fld.getName());
					if(fieldValue!=null)
						newParams.put(fld.getName(),fieldValue);
				}
				newParams.put(Field.USER,"root");
				UpdateRequest upr = new UpdateRequest();
				upr.setSource(TBitsConstants.SOURCE_CMDLINE);
				try {
					upr.updateRequest(newParams);
				} catch (TBitsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (APIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "AIRPostRule";
	}

	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
