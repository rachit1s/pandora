/**
 * 
 */
package pyramid;

import java.sql.Connection;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * @author Lokesh
 *
 */
public class DCRStatusAfterTransmittal implements IRule {

	private static final String TRANSMITTED_TO_CLIENT = "TransmittedToClient";
	private static final String STATUS_IFC = "ifc";
	private static final String STATUS_AS_BUILT = "AsBuilt";
	private static final String STATUS_SUBMITTED_TO_CLIENT = "submittedtoclient";
	private static final String FIELD_STATUS = "status_id";
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		RuleResult ruleResult = new RuleResult();
		//String sysPrefixes = "DCR343,DCR326,VDCR326,DCR345";
		boolean isApplicable = PyramidUtils.inProperty345SysPrefixes(ba.getSystemPrefix());
		
		if (isApplicable && !isAddRequest){			
			//Get extended field Transmitted To Client
			Field ttClient = null;
			Type stcStatus = null;
			int systemId = ba.getSystemId();
			//String sysPrefix = ba.getSystemPrefix();
			
			try {
				ttClient = Field.lookupBySystemIdAndFieldName(systemId, TRANSMITTED_TO_CLIENT);
				stcStatus = Type.lookupAllBySystemIdAndFieldNameAndTypeName(systemId, FIELD_STATUS, STATUS_SUBMITTED_TO_CLIENT);
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			if ((ttClient != null) && (stcStatus != null)){
				RequestEx ttClientReq = extendedFields.get(ttClient);
				if (ttClientReq.getBitValue()){
					Type statusType = currentRequest.getStatusId();
					String status = statusType.getName();
					if (status.equalsIgnoreCase(STATUS_IFC) || status.equalsIgnoreCase(STATUS_AS_BUILT) || status.equalsIgnoreCase("IFR")){
						ruleResult.setCanContinue(true);
						ruleResult.setMessage("Status is " + status + ". Hence continuing without changing the status");
					}
					else{
						currentRequest.setStatusId(stcStatus);
						ruleResult.setSuccessful(true);
						ruleResult.setMessage("Changed status to " + stcStatus.getDisplayName() + " from status " + statusType.getDisplayName());
					}
				}
				else{
					ruleResult.setCanContinue(true);
					ruleResult.setMessage("Is applicable only while creating transmittals");
				}					
			}
			else{
				ruleResult.setCanContinue(true);
				ruleResult.setMessage("Not applicable to business area: " + ba.getDisplayName());
			}		
		}
		else{
			ruleResult.setCanContinue(true);
			if (isAddRequest)
				ruleResult.setMessage("Not applicable while adding new request");	
			else
				ruleResult.setMessage("Not applicable to the Business Area: " + ba.getDisplayName());
		}
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + " - DCR Status changes to \"Submitted To Client\" if status is anything other than \"IFC/As Built\"";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}	
}
