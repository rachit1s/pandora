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
public class SITEPostTransmittalIDTRUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ipl.ITransmittalRule#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ipl.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see ipl.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.Request, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId,
			Request transmittalRequest, BusinessArea currentBA,
			BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType,
			int businessAreaType, boolean isAddRequest) throws TBitsException {

		if (IPLUtils.isSiteDCR(dcrBA)){	

			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			String siteDTNNumber = trnIdPrefix +  IPLUtils.getFormattedStringFromNumber(transmittalId);

			//Set notify to false.
			paramTable.put(Field.NOTIFY, IPLUtils.FALSE);
			//Based on the type of transmittal change logger, assignee, description and due-date.
			if (transmittalType.equals(IPLUtils.TRANSMIT_TO_IPLE) && 
					currentBA.getSystemPrefix().trim().equals(IPLUtils.IDTR)){
				//1.Logger :  Sub-Root
				paramTable.put(Field.USER, "root");
				
				//2.Description is updated to following Value
				//Submitted to IPLP With Decision.
				String description = "Submitted to IPLP With Decision.\n";				
				//Revision: SITE BA Revision
				try {
					description = description + "Revision: " + dcrRequest.getExType(IPLUtils.REVISION).getName() + "\n";
					//Drawing No: SITE BA SEPCO Drawing No 
					description = description + "SEPCO Drawing No: " + dcrRequest.getExString(IPLUtils.SEPCO_DRAWING_NO) + "\n";
					//Drawing Title: SITE BA Drawing Title (Subject)
					description = description + "Drawing Title: " + dcrRequest.getSubject() + "\n";					
					//Discipline: SITE BA Discipline
					description = description + "\nDiscipline: " + dcrRequest.getCategoryId().getDisplayName();
					//Package: SITE BA Package
					description = description + "\nPackage: " + dcrRequest.getRequestTypeId().getDisplayName();
					//Flow Status To IPLP: Decision to IPLP  in SITE BA
					//description = description + "Decision to IPLP: " + dcrRequest.get + "\n";
					//DTN: SITE BA  DTN to IPLP
					description = description + "\nDTN to IPLE: " + siteDTNNumber 
												+ " [DTN#" + transmittalRequest.getRequestId() + "]";				
					paramTable.put(Field.DESCRIPTION, description);
					
				} catch (IllegalStateException e) {
					e.printStackTrace();
					throw new TBitsException(e);
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException(e);
				}
			}
		}
	}
}
