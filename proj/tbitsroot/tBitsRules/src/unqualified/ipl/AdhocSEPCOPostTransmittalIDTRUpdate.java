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
public class AdhocSEPCOPostTransmittalIDTRUpdate implements ITransmittalRule {

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
			
			//4.	Revision: Same as in SEPCO DCR
			//5.	SEPCO Drawing No: Same as in SEPCO DCR
			//6.	Drawing Title: Same as in SEPCO DCR
			//7.	Discipline: Same as in SEPCO DCR
			//8.	Package: Same as in SEPCO DCR
			//9.	Status: Same as the value of Flow Status to IPLE in SEPCO DCR
			//10.	Priority: Is set to None

		
		if (IPLUtils.isAdhocBHELDCR(dcrBA) && (currentBA.getSystemPrefix().equals("IDTR"))){	
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			
			//Set notify to false.
			paramTable.put(Field.NOTIFY, IPLUtils.FALSE);
			
			//1.	Logger : SEPCO-mailing List
			paramTable.put(Field.USER, "BHEL");
			
			//Based on the type of transmittal change logger, assignee, description and due-date.
			if (transmittalType.equals("TransmitToIPLE")){
				
				//2.	Assignee: IPLE-mailing List
				paramTable.put(Field.ASSIGNEE, "IPLE");
				
				//3.	Subscriber: IPLP-mailing List
				//paramTable.put(Field.SUBSCRIBER, "IPLP");
				

				String description = "Submitted to IPLE With Comments.";
				description = description + "\nDrawing No: " + dcrRequest.get(IPLUtils.REVISION);
				description = description + "\nDrawing Title: " + dcrRequest.getSubject();
				description = description + "\nDiscipline: " + dcrRequest.getCategoryId().getDisplayName();
				description = description + "\nPackage: " + dcrRequest.getRequestTypeId().getDisplayName();
				description = description + "\nFlow Status to IPLE: " + dcrRequest.getStatusId().getDisplayName();
				description = description + "\nDTN = " + trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId) ;
				description = description + "[DTN#" + transmittalRequest.getRequestId() + "]";
				
				paramTable.put (Field.DESCRIPTION, description);
				
				
				String sepcoDtnNumber = trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId);
				
				paramTable.put(IPLUtils.DTN_TO_SEPCO_EXT, sepcoDtnNumber);
								
				/*paramTable.put (Field.DESCRIPTION,"Document submitted to IPLE for review via letter of Transmital number #" 
														+ sepcoDtnNumber
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");*/
				
				//Status
				paramTable.put(Field.STATUS, dcrRequest.getStatusId().getName());
				////10.	Priority: Is set to None
				paramTable.put(Field.SEVERITY, IPLUtils.STRING_NONE);
				
				/*int slideOffset = IPLUtils.getExFieldSlideOffset(connection, currentBA.getSystemId(), dcrRequest, paramTable,
											12, 9, isAddRequest, IPLUtils.DTN_TO_SEPCO_EXT);				
				if (slideOffset != 0){
					Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}*/
			}
			else if (transmittalType.equals("TransmitForRFC")){
				
				//2.	Assignee: IPLE-mailing List
				paramTable.put(Field.ASSIGNEE, "SITE");
				
				//3.	Subscriber: IPLP-mailing List
				paramTable.put(Field.SUBSCRIBER, "IPLP, IPLE");
				
				String sepcoDtnNumber = trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId);
				
				paramTable.put(IPLUtils.DTN_TO_SEPCO_EXT, sepcoDtnNumber);
								
//				1.Logger : IPLP-mailing List
//				2.Description is updated to following Value
//					Submitted to IPLE With Comments.
//					Revision: IPLE _DCR Revision
//					Drawing No: IPLE _DCR SEPCO Drawing No
//					Drawing Title: IPLE _DCR Drawing Title (Subject)
//					Discipline: IPLE _DCR Discipline
//					Package: IPLE _DCR Package
//					Decision to IPLE: IPLE_DCR Decision to IPLE
//					DTN: IPLE_DCR  DTN to IPLE
				
				String description = "Submitted to IPLE With Comments.";
				description = description + "\nDrawing No: " + dcrRequest.get(IPLUtils.REVISION);
				description = description + "\nDrawing Title: " + dcrRequest.getSubject();
				description = description + "\nDiscipline: " + dcrRequest.getCategoryId().getDisplayName();
				description = description + "\nPackage: " + dcrRequest.getRequestTypeId().getDisplayName();
				description = description + "\nFlow Status to IPLE: " + dcrRequest.getStatusId().getDisplayName();
				description = description + "\nDTN Number: " + trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId) ;
				description = description + "[DTN#" + transmittalRequest.getRequestId() + "]";
				
				paramTable.put (Field.DESCRIPTION, description);
				
				//Status
				paramTable.put(Field.STATUS, dcrRequest.getStatusId().getName());
				////10.	Priority: Is set to None
				paramTable.put(Field.SEVERITY, IPLUtils.STRING_NONE);
				
			}
		}
	} 
}
