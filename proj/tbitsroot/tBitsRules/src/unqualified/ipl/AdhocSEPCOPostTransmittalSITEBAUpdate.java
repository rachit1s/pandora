/**
 * 
 */
package ipl;

import java.sql.Connection;
import java.util.Hashtable;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public class AdhocSEPCOPostTransmittalSITEBAUpdate implements ITransmittalRule {

	public static final String IPLP_RESPONSE_DATE = "IPLPResponseDate";

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Updates the DCPL business area post SEPCO transmittal.";
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 1.3;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType, 
			int businessAreaType, boolean isAddRequest)
			throws TBitsException {
		if (IPLUtils.isAdhocBHELDCR(dcrBA) && currentBA.getSystemPrefix().trim().equals(IPLUtils.ADHOC_SITE)){
			/*
			    1.	Due Date is set to SEPCO DTN Date + RT-IPLE (based on rev)
				2.	Revision is updated to the values in Revision in SEPCO DCR.
				3.	Flow Status From SEPCO is changed to Under Review
				4.	Decision to SEPCO is changed to None
				5.	Flow Status To DCPL is changed to Pending Submission
				6.	Decision from DCPL is changed to None
				7.	Decision from IPLP is changed to Pending Decision
				8.	IPLP Response Date is set to SEPCO DTN Date + RT-IPLP (based on rev)
				9.	DTN from SEPCO Ext is updated with SEPCO DTN No
			 */
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			String sepcoDTNNumber = trnIdPrefix + IPLUtils.getFormattedStringFromNumber(transmittalId);
			//Set notify to false.
			paramTable.put(Field.NOTIFY, IPLUtils.FALSE);
			paramTable.put(Field.USER, "DC-BHEL");
			
			if (transmittalType.equals("TransmitForRFC")){
				//1.Due Date is set to Null
				paramTable.put(Field.DUE_DATE, "");
				//2.Flow Status From SEPCO is changed to RFC
				paramTable.put(Field.STATUS, IPLUtils.RELEASE_FOR_CONSTRUCTION);
				//3.Flow Status To DCPL is changed to Pending Submission
				//paramTable.put("FlowStatusToDCPL", IPLUtils.PENDING_SUBMISSION);
				//4.DTN from SEPCO Ext is updated with SEPCO DTN No
				paramTable.put(IPLUtils.DTN_FROM_SEPCO_EXT, sepcoDTNNumber);
			}			
		}
	}
}

/*if (transmittalType.equals("TransmitToIPLE")){
//Check if required.
paramTable.put(Field.ASSIGNEE, "IPLE");

//2.	Revision is updated to the values in Revision in SEPCO DCR.
//Already being set based on transmittal field mapping table.
//String revisionNumber = transmittalRequest.get(IPLUtils.FIELD_REVISION);
//paramTable.put(IPLUtils.FIELD_REVISION, revisionNumber);

//3. Flow Status From SEPCO is changed to Under Review
paramTable.put(Field.STATUS, IPLUtils.STATUS_UNDER_REVIEW);
//4. Decision to SEPCO is changed to None
paramTable.put(Field.SEVERITY, IPLUtils.STRING_NONE);
//5.	Flow Status To DCPL is changed to Pending Submission
paramTable.put(IPLUtils.FLOW_STATUS_TO_DCPL, IPLUtils.PENDING_SUBMISSION);
//6.	Decision from DCPL is changed to None
paramTable.put(DECISION_FROM_DCPL, IPLUtils.STRING_NONE);
//TODO: 7.	Decision from IPLP is changed to Pending Submission
paramTable.put(DECISION_FROM_IPLP, IPLUtils.STRING_NONE);

//Reset SEPCO response date.
paramTable.put(SEPCO_RESPONSE_DATE, "");

//8.	IPLP Response Date is set to SEPCO DTN Date + RT-IPLP (based on rev)
int slideIPLPOffset = IPLUtils.getExFieldSlideOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable, 
									10, 7, isAddRequest, IPLUtils.DTN_FROM_SEPCO_EXT);//"DTNFromSEPCOExt");				
if (slideIPLPOffset != 0){
	Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideIPLPOffset);
	paramTable.put(IPLP_RESPONSE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
}

//9.	DTN from SEPCO Ext is updated with SEPCO DTN No
paramTable.put(IPLUtils.DTN_FROM_SEPCO_EXT, sepcoDTNNumber);
paramTable.put (Field.DESCRIPTION,"Document submitted to IPLE for review via letter of Transmital number #" 
		+ sepcoDTNNumber + " [DTN#" + transmittalRequest.getRequestId() + "]");

//1.	Due Date is set to SEPCO DTN Date + RT-IPLE (based on rev)
int slideDueDateOffset = IPLUtils.getExFieldSlideOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable,
									12, 9, isAddRequest, IPLUtils.DTN_FROM_SEPCO_EXT);				
if (slideDueDateOffset != 0){
	Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideDueDateOffset);
	paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
}	

}*/
//else 
