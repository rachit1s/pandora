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
public class IPLPPostTransmittalDTRUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Updates DTR business area post-transmittal for IPLP initiated transmittal.";
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
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
				
		if ((businessAreaType == IPLUtils.DTR_BUSINESS_AREA) && IPLUtils.isIPLPDCR(dcrBA)){	
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = IPLUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			
			//Set notify to false.
			paramTable.put(Field.NOTIFY, IPLUtils.FALSE);		
			
			//Based on the type of transmittal change logger, assignee, description and due-date.
			if (transmittalType.equals("TransmitToIPLE")){
				
				paramTable.put(Field.USER, "DC-IPLP, DC-IPLE");
				paramTable.put(Field.ASSIGNEE, "IPLE");
				paramTable.put (Field.DESCRIPTION,"Document submitted to DCPL for review via letter of Transmital number #" 
														+ trnIdPrefix + IPLUtils.getFormattedStringFromNumber(transmittalId)
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				
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
				paramTable.put (Field.DESCRIPTION,"Document submitted to DCPL for review via letter of Transmital number #" 
														+ trnIdPrefix + IPLUtils.getFormattedStringFromNumber(transmittalId)
														+ " [DTN#" + transmittalRequest.getRequestId() + "]");				
			}
		}
	} 
}
