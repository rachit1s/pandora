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
public class SITEPostTransmittalDTRUpdate implements ITransmittalRule {

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
					currentBA.getSystemPrefix().trim().equals(IPLUtils.DTR)){
				//1.Logger : SITE-mailing List, IPLP-mailing List
				paramTable.put(Field.USER, "DC-SITE, IPLE");

				//2.Assignee: IPLP-mailing List
				paramTable.put(Field.ASSIGNEE, "IPLE");
				
				//3.Revision: Same as in SITE DCR
				//4.SEPCO Drawing No: Same as in SITE DCR
				//5.Drawing Title: Same as in SITE DCR
				/*6.Discipline: Same as in SITE DCR
				7.Package: Same as in SITE DCR
				8.Status: Set equal to value of Flow Status From IPLP in SITE BA
				9.Priority: Set value of Decision to IPLP  in SITE BA*/
				
				try{
					paramTable.put(Field.STATUS, dcrRequest.getExType("FlowStatusFromIPLE").getName());
				} catch (IllegalStateException e) {
					e.printStackTrace();
					throw new TBitsException(e);
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException(e);
				}
				
				paramTable.put(Field.SEVERITY, dcrRequest.getSeverityId().getName());			
				
				paramTable.put (Field.DESCRIPTION,"Document returned with decision via letter of Transmital number #" 
						+ siteDTNNumber + " [DTN#" + transmittalRequest.getRequestId() + "]");
			}
		}
	}
}
