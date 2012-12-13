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
public class BHELPostTransmittalDTRUpdate implements ITransmittalRule {

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
		return 0;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType, 
			int businessAreaType, boolean isAddRequest)
			throws TBitsException {
					
		if ((businessAreaType == IPLUtils.DTR_BUSINESS_AREA) && IPLUtils.isBHELDCR(dcrBA)){	
			/*
			 * 	1.	Logger : SEPCO-mailing List
				2.	Description is updated to following Value
					•	Submitted to IPLE for Approval. CC to IPLP
					•	Revision: SEPCO_DCR Revision
					•	Drawing No: SEPCO_DCR SEPCO Drawing No
					•	Drawing Title: SEPCO_DCR Drawing Title (Subject)
					•	Discipline: SEPCO_DCR Discipline
					•	Package: SEPCO_DCR Package
					•	Flow Status To IPLE: SEPCO_DCR Flow Status To IPLE
					•	DTN: SEPCO_DCR  DTN to IPLE/SITE
			*/

			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			
			//Set notify to false.
			paramTable.put(Field.NOTIFY, IPLUtils.FALSE);			
			
			//Based on the type of transmittal change logger, assignee, description and due-date.
			if (transmittalType.equals("TransmitToIPLE")){
				//1.	Logger : SEPCO-mailing List
				paramTable.put(Field.USER, "BHEL,IPLE");
				
				//2.	Assignee: IPLE-mailing List
				paramTable.put(Field.ASSIGNEE, "IPLE");
				
				//3.	Subscriber: IPLP-mailing List
				//paramTable.put(Field.SUBSCRIBER, "IPLP");
				
				paramTable.put(Field.STATUS, dcrRequest.getStatusId().getName());
				////10.	Priority: Is set to None
				paramTable.put(Field.SEVERITY, IPLUtils.STRING_NONE);	
				
				paramTable.put(Field.SUBJECT, dcrRequest.getSubject());
				
				paramTable.put (Field.DESCRIPTION,"Document submitted to IPLE for review via letter of Transmital number #" 
														+ trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId)
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				
				/*int slideOffset = IPLUtils.getSlideOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable, 12, 9, isAddRequest);				
				if (slideOffset != 0){
					Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}*/
			}
			else if (transmittalType.equals("TransmitForRFC")){
				
				//1.	Logger : SEPCO-mailing List
				paramTable.put(Field.USER, "BHEL, SITE");
				
				//2.	Assignee: IPLE-mailing List
				paramTable.put(Field.ASSIGNEE, "SITE");
				
				//3.	Subscriber: IPLP-mailing List
				paramTable.put(Field.SUBSCRIBER, "IPLE");
				
				paramTable.put(Field.STATUS, IPLUtils.RELEASE_FOR_CONSTRUCTION);
				
				////10.	Priority: Is set to None
				paramTable.put(Field.SEVERITY, IPLUtils.STRING_NONE);
				
				paramTable.put (Field.DESCRIPTION,"Document submitted via letter of Transmital number #" 
														+ trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId)
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				
				/*int slideOffset = IPLUtils.getSlideOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable, 12, 9, isAddRequest);				
				if (slideOffset != 0){
					Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}*/
			}
		}
	} 
}
