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
public class IPLPPostTransmittalIDTRUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName() + ": Updates IDTR business area post-transmittal for IPLP initiated transmittal.";
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
				
		if (IPLUtils.isIPLPDCR(dcrBA)){	
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			
			//Set notify to false.
			paramTable.put(Field.NOTIFY, IPLUtils.FALSE);
			
			//Based on the type of transmittal change logger, assignee, description and due-date.
			if (transmittalType.equals("TransmitToIPLE") && (currentBA.getSystemPrefix().equals("IDTR"))){
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

				paramTable.put(Field.USER, "IPLP");
				paramTable.put(Field.ASSIGNEE, "IPLE");
				String description = "Submitted to IPLE With Comments.";
				description = description + "\nDrawing No: " + dcrRequest.get(IPLUtils.REVISION);
				description = description + "\nDrawing Title: " + dcrRequest.getSubject();
				description = description + "\nDiscipline: " + dcrRequest.getCategoryId().getDisplayName();
				description = description + "\nPackage: " + dcrRequest.getRequestTypeId().getDisplayName();
				description = description + "\nDecision to IPLE: " + dcrRequest.getSeverityId().getDisplayName();
				description = description + "\n" + trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId) ;
				description = description + "[DTN#" + transmittalRequest.getRequestId() + "]";
				
				paramTable.put (Field.DESCRIPTION, description);
				
			}
			else if (transmittalType.equals("TransmitToSite") && (currentBA.getSystemPrefix().equals("IDTR"))){
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

				paramTable.put(Field.USER, "IPLP");
				paramTable.put(Field.ASSIGNEE, "SITE");
				String description = "Submitted to IPLE With Comments.";
				description = description + "\nDrawing No: " + dcrRequest.get(IPLUtils.REVISION);
				description = description + "\nDrawing Title: " + dcrRequest.getSubject();
				description = description + "\nDiscipline: " + dcrRequest.getCategoryId().getDisplayName();
				description = description + "\nPackage: " + dcrRequest.getRequestTypeId().getDisplayName();
				description = description + "\nDecision to IPLE: " + dcrRequest.getSeverityId().getDisplayName();
				description = description + "\n" + trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId) ;
				description = description + "[DTN#" + transmittalRequest.getRequestId() + "]";
				
				paramTable.put (Field.DESCRIPTION, description);
				
			}
		}
	} 
}
