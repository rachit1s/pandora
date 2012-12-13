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
public class ODCECPostTransmittalDCRUpdate implements ITransmittalRule {

	public String getName() {
		return this.getClass().getSimpleName() + ": Post trasnmittal DCR update in CEC initiated transmittal process.";
	}


	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 3.0;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType,
			int businessAreaType, boolean isAddRequest) throws TBitsException {
		
		if(KSKUtils.isODCECDCR(dcrBA) && currentBA.getSystemPrefix().trim().equals("ODCEC")){
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
						
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_DCPL)){				
				paramTable.put(KSKUtils.TRANSMIT_TO_DCPL, KSKUtils.FALSE);
				paramTable.put(KSKUtils.TRANSMITTED_TO_DCPL, KSKUtils.TRUE);
				paramTable.put(Field.DUE_DATE, "");
				paramTable.put(Field.DESCRIPTION, "Document released to DCPL via letter  of Transmittal number #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				paramTable.put(Field.STATUS, KSKUtils.STATUS_DOCUMENT_COMMENTED);
			}
			
			else if (transmittalType.equals("TransmitToNWPDI")){
				paramTable.put(Field.USER, "DC-CEC");
				//paramTable.put(KSKUtils.TRANSMIT_TO_DCPL, KSKUtils.FALSE);
				//paramTable.put(KSKUtils.TRANSMITTED_TO_DCPL, KSKUtils.TRUE);
				paramTable.put(Field.DUE_DATE, "");
				paramTable.put(Field.DESCRIPTION, "Document sent via letter  of Transmittal number #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				//paramTable.put(Field.STATUS, KSKUtils.STATUS_DOCUMENT_COMMENTED);
			}
			
			else if (transmittalType.equals("TransmitToSITE")){
				paramTable.put(Field.USER, "DC-CEC");
				paramTable.put("TransmitToWPCL", KSKUtils.FALSE);
				paramTable.put("TransmittedToWPCL", KSKUtils.TRUE);
				paramTable.put(Field.DUE_DATE, "");
				paramTable.put(Field.DESCRIPTION, "Document sent via letter  of Transmittal number #" 
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
			}
		}	
	}

}
