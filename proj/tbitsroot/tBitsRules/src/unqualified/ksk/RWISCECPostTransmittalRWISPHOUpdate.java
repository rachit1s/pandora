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
public class RWISCECPostTransmittalRWISPHOUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": RWIS-CEC initiated post transmittal update in RWIS-PHO business area";
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
		
		if(KSKUtils.isRWISCECDCR(dcrBA) && (currentBA.getSystemPrefix().equals(KSKUtils.RWIS))){
			
			paramTable.put(Field.STATUS, dcrRequest.getStatusId().getName());
			paramTable.put(Field.USER, "DC-RWIS-ERC");
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ZUBERI)){
				paramTable.put(Field.DUE_DATE, "");		
				paramTable.put(Field.DESCRIPTION, "Document commented by WPCL via letter of Transmittal number #"
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN_RWIS#" + transmittalRequest.getRequestId() + "]");				
				
			}
			/*else if(transmittalType.equals(KSKUtils.TRANSMIT_RFC_VALIDATION)){
				
				paramTable.put(Field.DESCRIPTION, "The RFC file has been validated by WPCL-DCPL & has been found" +
						"as Correct/Incorrect (one of the two based on RFC Validation) & is transmitted via letter of" +
						" Transmittal number #"	+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN_RWIS#" + transmittalRequest.getRequestId() + "]");
				//if (dcrRequest.get(KSKUtils.RFC_VALIDATION).equals(KSKUtils.APPROVED)){
				paramTable.put(Field.DUE_DATE, "");				
				}
				else if (dcrRequest.get(KSKUtils.RFC_VALIDATION).equals(KSKUtils.REJECTED)){
					Timestamp gmtDueDate = KSKUtils.getSlidedDueDate(isAddRequest, 2);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}

			}
			else if(transmittalType.equals(KSKUtils.TRANSMIT_AB_VALIDATION)){
				
				//if (dcrRequest.get(KSKUtils.AS_BUILT_VALIDATION).equals(KSKUtils.APPROVED)){
				paramTable.put(Field.DUE_DATE, "");				
				}
				else if (dcrRequest.get(KSKUtils.AS_BUILT_VALIDATION).equals(KSKUtils.REJECTED)){
					Timestamp gmtDueDate = KSKUtils.getSlidedDueDate(isAddRequest, 2);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}

				paramTable.put(Field.DESCRIPTION, "The AsBuilt file has been validated by WPCL-DCPL & has been" +
						"found as Correct/Incorrect (one of the two based on RFC Validation) & is transmitted via" +
						"letter of Transmittal number #" + trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN_RWIS#" + transmittalRequest.getRequestId() + "]");
			}*/
		}
	}

}
