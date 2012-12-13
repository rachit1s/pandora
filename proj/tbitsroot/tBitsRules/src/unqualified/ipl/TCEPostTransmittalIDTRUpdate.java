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
public class TCEPostTransmittalIDTRUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName() + ": Updates IDTR business area post-transmittal for TCE initiated transmittal.";
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
				
//		1.Logger : DCPL-mailing List
//		2.Assignee: IPLE-mailing List
//		3.Revision: Same as in DCPL DCR
//		4.SEPCO Drawing No: Same as in DCPL DCR
//		5.Drawing Title: Same as in DCPL DCR
//		6.Discipline: Same as in DCPL DCR
//		7.Package: Same as in DCPL DCR
//		8.Status: Set value to Decision Received
//		9.Priority: Set value of Decision to IPLE  in DCPL_DCR

		
		if (IPLUtils.isTCEDCR(dcrBA) && (currentBA.getSystemPrefix().equals("IDTR"))){	
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			
			//Set notify to false.
			paramTable.put(Field.NOTIFY, IPLUtils.FALSE);
			
			//Based on the type of transmittal change logger, assignee, description and due-date.
			if (transmittalType.equals("TransmitToIPLE")){
				paramTable.put(Field.USER, "DC-TCE");
				//paramTable.put(Field.ASSIGNEE, "IPLE");
				
				String description = "Document submitted to IPLE with Decision via letter of Transmital number #" 
					+ trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId) + " [DTN#" + transmittalRequest.getRequestId() + "]";

				description = description + "\nDrawing Number = " + dcrRequest.get(IPLUtils.SEPCO_DRAWING_NO);
				description = description + "\nRevision = " + dcrRequest.get(IPLUtils.REVISION);
				description = description + "\nDrawing Title = " + dcrRequest.getSubject();
				description = description + "\nDiscipline = " + dcrRequest.getCategoryId().getDisplayName();
				description = description + "\nPackage = " + dcrRequest.getRequestTypeId().getDisplayName();
				try {
					description = description + "\nFlow Status To IPLE = " + dcrRequest.getExType(IPLUtils.DECISION_TO_IPLE).getName();
				} catch (IllegalStateException e) {
					e.printStackTrace();
					new TBitsException(e);
				} catch (DatabaseException e) {
					e.printStackTrace();
					new TBitsException(e);
				}
				description = description + "DTN = " + trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId);
				paramTable.put (Field.DESCRIPTION, description);
			}
		}
	} 
}
