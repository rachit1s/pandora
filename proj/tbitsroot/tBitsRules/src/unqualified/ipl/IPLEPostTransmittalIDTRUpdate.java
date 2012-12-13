/**
 * 
 */
package ipl;

import java.sql.Connection;
import java.util.Hashtable;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public class IPLEPostTransmittalIDTRUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName() + ": Updates IDTR business area post-transmittal for SEPCO initiated transmittal.";
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
		 	•	Submitted to TPSC for Comments
			•	Revision: IPLE_DCR Revision
			•	Drawing No: IPLE _DCR SEPCO Drawing No
			•	Drawing Title: IPLE _DCR Drawing Title (Subject)
			•	Discipline: IPLE _DCR Discipline
			•	Package: IPLE _DCR Package
			•	Flow Status To TPSC: IPLE_DCR Flow Status To DCPL
			•	DTN: IPLE_DCR  DTN to DCPL
		 */
				
		if (currentBA.getSystemPrefix().equals("IDTR") && IPLUtils.isIPLEDCR(dcrBA)){	
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			String ipleDTNNumber =  trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId);
			
			//Set notify to false.
			paramTable.put(Field.NOTIFY, IPLUtils.FALSE);
			
			//Based on the type of transmittal change logger, assignee, description and due-date.
			if (transmittalType.equals("TransmitToTCE")){
				paramTable.put(Field.USER, "DC-IPLE");
				paramTable.put(Field.ASSIGNEE, "TCE");
				paramTable.put("DTNFromIPLE", ipleDTNNumber);
				
				String description = "Document submitted to TCE for comments." ;
				
				description = description + "\nDrawing Number = " + dcrRequest.get(IPLUtils.SEPCO_DRAWING_NO);
				description = description + "\nRevision = " + dcrRequest.get(IPLUtils.REVISION);
				description = description + "\nDrawing Title = " + dcrRequest.getSubject();
				description = description + "\nDiscipline = " + dcrRequest.getCategoryId().getDisplayName();
				description = description + "\nPackage = " + dcrRequest.getRequestTypeId().getDisplayName();
				description = description + "\nFLow Status to DCPL = " + dcrRequest.get(IPLUtils.FLOW_STATUS_TO_DCPL);
				description = description + "\n DTN: " + ipleDTNNumber + " [DTN#" + transmittalRequest.getRequestId() + "]";
				
				paramTable.put (Field.DESCRIPTION, description);
				paramTable.put (IPLUtils.FLOW_STATUS_TO_DCPL, dcrRequest.get(IPLUtils.FLOW_STATUS_TO_DCPL));
				
				/*int slideOffset = IPLUtils.getSlideOffsetBasedOnRevision(connection, currentBA.getSystemId(), dcrRequest, paramTable, 10, 7, isAddRequest);				
				if (slideOffset != 0){
					Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}*/
			}
			/*else if (transmittalType.equals("TransmitToTPSC")){
				paramTable.put(Field.USER, "DC-IPLE");
				paramTable.put(Field.ASSIGNEE, "TPSC");
				paramTable.put("DTNFromIPLE", ipleDTNNumber);
				
				String description = "Document submitted to TPSC via letter of Transmital number #" 
									+ ipleDTNNumber + " [DTN#" + transmittalRequest.getRequestId() + "]";
				
				description = description + "\nDrawing Number = " + dcrRequest.get(IPLUtils.SEPCO_DRAWING_NO);
				description = description + "\nRevision = " + dcrRequest.get(IPLUtils.REVISION);
				description = description + "\nDrawing Title = " + dcrRequest.getSubject();
				description = description + "\nDiscipline = " + dcrRequest.getCategoryId().getDisplayName();
				description = description + "\nPackage = " + dcrRequest.getRequestTypeId().getDisplayName();
				description = description + "\nFLow Status to TPSC = " + dcrRequest.get(IPLUtils.FLOW_STATUS_TO_DCPL);
				
				paramTable.put (Field.DESCRIPTION, description);
				try {
					paramTable.put (IPLUtils.FLOW_STATUS_TO_TPSC, dcrRequest.getExType(IPLUtils.FLOW_STATUS_TO_DCPL).getName());
				} catch (IllegalStateException e){
					e.printStackTrace();
					throw new TBitsException(e);
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException(e);
				}
				
				int slideOffset = IPLUtils.getSlideOffsetBasedOnRevision(connection, currentBA.getSystemId(), dcrRequest, paramTable, 10, 7, isAddRequest);				
				if (slideOffset != 0){
					Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}
			}*/
			else if (transmittalType.equals("TransmitToBHEL")){
				paramTable.put(Field.USER, "DC-IPLE");
				paramTable.put(Field.ASSIGNEE, "BHEL");				
				
				paramTable.put(IPLUtils.DTN_FROM_IPLE_EXT, ipleDTNNumber);
				
				String description = "Document submitted to BHEL via letter of Transmital number #" 
									+ ipleDTNNumber + " [DTN#" + transmittalRequest.getRequestId() + "]";
				
				description = description + "\nDrawing Number = " + dcrRequest.get(IPLUtils.SEPCO_DRAWING_NO);
				description = description + "\nRevision = " + dcrRequest.get(IPLUtils.REVISION);
				description = description + "\nDrawing Title = " + dcrRequest.getSubject();
				description = description + "\nDiscipline = " + dcrRequest.getCategoryId().getDisplayName();
				description = description + "\nPackage = " + dcrRequest.getRequestTypeId().getDisplayName();
				description = description + "\nDecision To BHEL = " + dcrRequest.getSeverityId().getDisplayName();
				
				paramTable.put (Field.DESCRIPTION, description);
				paramTable.put (IPLUtils.FLOW_STATUS_TO_TPSC, dcrRequest.get(IPLUtils.FLOW_STATUS_TO_DCPL));
				
				/*int slideOffset = 7;				
				if (slideOffset != 0){
					Timestamp gmtDueDate = IPLUtils.getSlidedDueDate(isAddRequest, slideOffset);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}*/
			}
			
			else if (transmittalType.equals("TransmitToSite")){
				/*1.Logger : IPLP-mailing List
				2.Assignee: SITE-mailing List
				3.Revision: Same as in IPLP DCR
				4.SEPCO Drawing No: Same as in IPLP DCR
				5.Drawing Title: Same as in IPLP DCR
				6.Discipline: Same as in IPLP DCR
				7.Package: Same as in IPLP DCR
				8.Status: Same as the value of Flow Status to SITE in IPLP DCR
				9.Priority:  Set to None*/
				paramTable.put(Field.USER, "DC-IPLP, DC-SITE");
				paramTable.put(Field.ASSIGNEE, "SITE");
				paramTable.put (Field.DESCRIPTION,"Document submitted to SITE via letter of Transmital number #" 
														+ trnIdPrefix + IPLUtils.getFormattedStringFromNumber(transmittalId)
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");				
			}
		}
	} 
}
