/**
 * 
 */
package ipl;

import java.sql.Connection;
import java.util.Hashtable;

import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public class AdhocSEPCOPostTransmittalIPLPUpdate implements ITransmittalRule {

	public static final String DECISION_FROM_SITE = "DecisionFromSite";
	public static final String DECISION_TO_IPLE = "DecisionToIPLE";

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName() + ": Updates DTR business area post-transmittal for SEPCO initiated transmittal.";
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 1.2;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType, 
			int businessAreaType, boolean isAddRequest)
			throws TBitsException {
		/*	
		 	1.	Due Date is set to SEPCO DTN Date + RT-IPLP (based on rev)
			2.	Revision is updated to the values in Revision in SEPCO DCR.
			3.	Flow Status From SEPCO is changed to Under Review
			4.	Decision to IPLPE is changed to None
			5.	Flow Status To Site is changed to Pending Submission
			6.	Decision From Site is changed to None
			7.	DTN from SEPCO Ext is updated with SEPCO DTN No
			8.	Final Decision to SEPCO is set to None
		*/			
		
		if (IPLUtils.isAdhocBHELDCR(dcrBA) && currentBA.getSystemPrefix().trim().equals(IPLUtils.ADHOC_IPLP)){	
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			
			//Set notify to false.
			paramTable.put(Field.NOTIFY, IPLUtils.FALSE);
			paramTable.put(Field.USER, "DC-BHEL");
			String sepcoDTNNumber = trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId);
			
			//Based on the type of transmittal change logger, assignee, description and due-date.
			if (transmittalType.equals("TransmitToIPLE")){
				
				paramTable.put(Field.ASSIGNEE, "IPLP");
				
				//2.	Revision is updated to the values in Revision in SEPCO DCR.
				//Already being set based on transmittal field mapping table.
				//String revisionNumber = transmittalRequest.get(IPLUtils.FIELD_REVISION);
				//paramTable.put(IPLUtils.FIELD_REVISION, revisionNumber);
								
				//3.	Flow Status From SEPCO is changed to Under Review
				paramTable.put(Field.STATUS, IPLUtils.STATUS_UNDER_REVIEW);
				
				//4.	Decision to IPLPE is changed to None
				paramTable.put(DECISION_TO_IPLE, IPLUtils.STRING_NONE);
				
				//5.	Flow Status To Site is changed to Pending Submission
				paramTable.put("FlowStatusToSite", IPLUtils.PENDING_SUBMISSION);
				
				//6.	Decision From Site is changed to None
				paramTable.put(DECISION_FROM_SITE, IPLUtils.STRING_NONE);
				
				//7.	DTN from BHEL Ext is updated with SEPCO DTN No
				paramTable.put(IPLUtils.DTN_FROM_SEPCO_EXT, sepcoDTNNumber);
								
				//8.	Final Decision to BHEL is set to None
				paramTable.put(Field.SEVERITY, IPLUtils.STRING_NONE);
				
				//1.	Due Date is set to BHEL DTN Date + RT-IPLP (based on rev)
				/*int slideOffset = IPLUtils.getExFieldSlideOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable,
												10, 7, isAddRequest, IPLUtils.DTN_FROM_SEPCO_EXT);				
				if (slideOffset != 0){
					Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}*/
				
				paramTable.put (Field.DESCRIPTION,"Document submitted to IPLE for review via letter of Transmital number #" 
						+ sepcoDTNNumber + " [DTN#" + transmittalRequest.getRequestId() + "]");				
			}
			else if(transmittalType.equals("TransmitForRFC")){
				//1.Due Date is set to Null
				paramTable.put(Field.DUE_DATE, "");
				//2.Flow Status From SEPCO is changed to RFC
				paramTable.put(Field.STATUS, IPLUtils.RELEASE_FOR_CONSTRUCTION);
				//3.DTN from SEPCO Ext is updated with BHEL DTN No
				paramTable.put(IPLUtils.DTN_FROM_SEPCO_EXT, sepcoDTNNumber);
				
				paramTable.put (Field.DESCRIPTION,"Document submitted to IPLE for review via letter of Transmital number #" 
						+ sepcoDTNNumber + " [DTN#" + transmittalRequest.getRequestId() + "]");				
			}
		}
	} 
}
