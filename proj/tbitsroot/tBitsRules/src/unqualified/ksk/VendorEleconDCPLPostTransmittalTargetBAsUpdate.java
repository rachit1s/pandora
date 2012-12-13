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
public class VendorEleconDCPLPostTransmittalTargetBAsUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": DCPL initiated post transmittal update in PHO business area";
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 4.2;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType,
			int businessAreaType, boolean isAddRequest) throws TBitsException {
	
		if(KSKUtils.isDCPLDCR(dcrBA) && (currentBA.getSystemPrefix().equals(KSKUtils.PHO_SYS_PREFIX) 
											|| currentBA.getSystemPrefix().equals(KSKUtils.CEC_SYS_PREFIX))){
						
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON)){
				paramTable.put(Field.STATUS, dcrRequest.getStatusId().getName());
				paramTable.put(Field.USER, "DC-ELECON-DCPL");
				int dcrSystemId = dcrBA.getSystemId();
				String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
				
				paramTable.put(Field.DUE_DATE, "");		
				paramTable.put(Field.DESCRIPTION, "Document commented via letter of Transmittal number #"
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");				
				
			}
			else if(transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON_RFC_VALIDATION)){				
				paramTable.put(Field.STATUS, dcrRequest.getStatusId().getName());
				paramTable.put(Field.USER, "DC-ELECON-DCPL");
				int dcrSystemId = dcrBA.getSystemId();
				String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
				
				paramTable.put(Field.DESCRIPTION, "The RFC file has been validated & has been found" +
						"as Correct/Incorrect (one of the two based on RFC Validation) & is transmitted via letter of" +
						" Transmittal number #"	+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				paramTable.put(Field.DUE_DATE, "");				
			}
			else if(transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON_AB_VALIDATION)){
				paramTable.put(Field.STATUS, dcrRequest.getStatusId().getName());
				paramTable.put(Field.USER, "DC-ELECON-DCPL");
				int dcrSystemId = dcrBA.getSystemId();
				String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
				
				paramTable.put(Field.DUE_DATE, "");				
				paramTable.put(Field.DESCRIPTION, "The AsBuilt file has been validated & has been" +
						"found as Correct/Incorrect (one of the two based on RFC Validation) & is transmitted via" +
						"letter of Transmittal number #" + trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
			}
		}
	}

}
