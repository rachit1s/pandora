/**
 * 
 */
package ksk;

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
public class ODCECPostTransmittalDTRUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": CEC initiated post transmittal update in DTR(IntDTR) BA";
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 3.1;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType,
			int businessAreaType, boolean isAddRequest) throws TBitsException {
		if(KSKUtils.isODCECDCR(dcrBA) && currentBA.getSystemPrefix().trim().equals("ODDTR")){
			
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			paramTable.put(Field.USER, "DC-CEC");
			
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_DCPL)){
				
				paramTable.put(Field.DESCRIPTION, "Comments sent by CEC to DCPL via DTN #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				paramTable.put(Field.STATUS, KSKUtils.DOCUMENT_COMMENTED_BY_CEC);
			}			
			else if (transmittalType.equals("TransmitToSITE")){
				
				paramTable.put(Field.DESCRIPTION, "Comments sent by CEC to DCPL via DTN #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				//paramTable.put(Field.STATUS, KSKUtils.DOCUMENT_COMMENTED_BY_CEC);
			}	
		}
		
		else if(KSKUtils.isCECDCR(dcrBA) && currentBA.getSystemPrefix().trim().equals("DTR")){
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			
			if (transmittalType.equals("TransmitToNWPDI")){
				paramTable.put(Field.USER, "DC-CEC");
				paramTable.put(Field.DESCRIPTION, "Comments sent by CEC to NWPDI via DTN #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				//paramTable.put(Field.STATUS, KSKUtils.DOCUMENT_COMMENTED_BY_CEC);
			}
		}
	}

}
