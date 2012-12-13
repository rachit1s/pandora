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
public class AdhocIPLEPostTransmittalDCPLUpdate implements ITransmittalRule {

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
				
		if (IPLUtils.isAdhocIPLEDCR(dcrBA)){	
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			String ipleDTNNumber = trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId);
			
			//Set notify to false.
			paramTable.put(Field.NOTIFY, IPLUtils.FALSE);
			paramTable.put(Field.USER, "DC-IPLE");
			//Based on the type of transmittal change logger, assignee, description and due-date.
			if (transmittalType.equals("TransmitToDCPL") && 
					currentBA.getSystemPrefix().trim().equals(IPLUtils.ADHOC_DCPL)){
				
				paramTable.put(Field.ASSIGNEE, "DCPL");
//				1.	Due Date is set to IPLE DTN Date + RT-DCPL (based on rev)
				/*int slideOffset = IPLUtils.getSlideOffsetBasedOnRevision(connection, currentBA.getSystemId(), dcrRequest, paramTable, 10, 7, isAddRequest);				
				if (slideOffset != 0){
					Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}*/
				
//				2.	Revision is updated to the values in Revision in IPLE DCR.
				// Done with transmittal field mapping.
				
//				3.	Flow Status From IPLE is changed to Under Review
				paramTable.put(Field.STATUS, IPLUtils.STATUS_UNDER_REVIEW);
				
//				4.	Decision to IPLPE is changed to None
				paramTable.put(IPLUtils.DECISION_TO_IPLE, IPLUtils.STRING_NONE);
				
//				5.	DTN from IPLE is updated with IPLE DTN No
				paramTable.put("DTNFromIPLE", ipleDTNNumber);
				
//				6.	DTN from SEPCO Ext is updated with DTN from SEPCO Ext from IPLE DCR					
				paramTable.put(IPLUtils.DTN_FROM_SEPCO_EXT, dcrRequest.get(IPLUtils.DTN_FROM_SEPCO_EXT));

				paramTable.put (Field.DESCRIPTION,"Document submitted to DCPL for review via letter of Transmital number #" 
														+ ipleDTNNumber
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");
			}
			else if (transmittalType.equals("TransmitToTPSC")&& 
					currentBA.getSystemPrefix().trim().equals(IPLUtils.ADHOC_TPSC)){
				paramTable.put(Field.USER, "DC-IPLE");
				paramTable.put(Field.ASSIGNEE, "TPSC");

//				1.	Due Date is set to IPLE DTN Date + RT-TPSC (based on rev)
				/*int slideOffset = IPLUtils.getSlideOffsetBasedOnRevision(connection, currentBA.getSystemId(), dcrRequest, paramTable, 10, 7, isAddRequest);				
				if (slideOffset != 0){
					Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}*/

//				2.	Revision is updated to the values in Revision in IPLE DCR.
				// Already done using transmittal field mapping in db.
				
//				3.	Flow Status From IPLE is changed to Under Review
				paramTable.put(Field.STATUS, IPLUtils.STATUS_UNDER_REVIEW);
				
//				4.	Decision to IPLPE is changed to None
				paramTable.put(IPLUtils.DECISION_TO_IPLE, IPLUtils.STRING_NONE);
				
//				5.	DTN from IPLE is updated with IPLE DTN No
				paramTable.put("DTNFromIPLE", ipleDTNNumber);
				
//				6.	DTN from SEPCO Ext is updated with DTN from SEPCO Ext from IPLE DCR
				paramTable.put(IPLUtils.DTN_FROM_SEPCO_EXT, dcrRequest.get(IPLUtils.DTN_FROM_SEPCO_EXT));
				
				paramTable.put (Field.DESCRIPTION,"Document submitted to TPSC for review via letter of Transmital number #" 
														+ ipleDTNNumber
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");
			}
			else if (transmittalType.equals("TransmitToSEPCO")&& 
					(currentBA.getSystemPrefix().trim().equals(IPLUtils.ADHOC_TPSC)
							|| currentBA.getSystemPrefix().trim().equals(IPLUtils.ADHOC_DCPL))){				
				
//				1.	DTN to SEPCO Ext is updated with IPLE DTN No
//				2.	Final Decision to SEPCO is set to value of Decision to SEPCO in IPLE_DCR:
				paramTable.put(IPLUtils.DTN_TO_SEPCO_EXT, ipleDTNNumber);
				paramTable.put(Field.SEVERITY, dcrRequest.getSeverityId().getName());
				
				//paramTable.put(Field.STATUS, "ReturnedWithDecision");
			
				paramTable.put(Field.USER, "DC-IPLE");
				paramTable.put (Field.DESCRIPTION,"Document returned with decision to SEPCO via letter of Transmital number #" 
														+ ipleDTNNumber
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				
			}
			
		}
	} 
}
