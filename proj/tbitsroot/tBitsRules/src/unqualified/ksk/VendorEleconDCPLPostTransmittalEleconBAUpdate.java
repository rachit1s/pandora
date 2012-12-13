/**
 * 
 */
package ksk;

import java.sql.Connection;
import java.util.Hashtable;

import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public class VendorEleconDCPLPostTransmittalEleconBAUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Updates the DCPL business area post ELECON transmittal.";
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 4.3;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType, 
			int businessAreaType, boolean isAddRequest)
			throws TBitsException {

			
		if (KSKUtils.isDCPLDCR(dcrBA) && currentBA.getSystemPrefix().equals(KSKUtils.ELECON)){
						
			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON)){
				populateCommonFields(dcrRequest, paramTable);
				//Set the due-date.
				boolean isSetSlidedDueDate = isSetDCPLEleconDueDate(dcrRequest.getStatusId().getName());
				if (isSetSlidedDueDate){
					Timestamp gmtDueDate = KSKUtils.getSlidedDueDate(isAddRequest, 4);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}
				else
					paramTable.put(Field.DUE_DATE, "");
				
				paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO,
									trnIdPrefix + KSKUtils.getFormattedStringFromNumber(transmittalId));
				paramTable.put(Field.DESCRIPTION, "Document commented via letter of Transmittal number #"
									+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
									+ " [" + KSKUtils.DTN + "#" 
									+ transmittalRequest.getRequestId() + "]");
			}
			else if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON_RFC_VALIDATION)){
				populateCommonFields(dcrRequest, paramTable);
				
				paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO,
									trnIdPrefix + KSKUtils.getFormattedStringFromNumber(transmittalId));
				paramTable.put(Field.DESCRIPTION, "The RFC file has been validated & has been found" +
						"as Correct/Incorrect (one of the two based on RFC Validation) & is transmitted via letter of" +
						" Transmittal number #"+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [" + KSKUtils.DTN + "#" 
						+ transmittalRequest.getRequestId() + "]");
				if (dcrRequest.get(KSKUtils.RFC_VALIDATION).equals(KSKUtils.APPROVED)){
					paramTable.put(Field.DUE_DATE, "");				
				}
				else if (dcrRequest.get(KSKUtils.RFC_VALIDATION).equals(KSKUtils.REJECTED)){
					Timestamp gmtDueDate = KSKUtils.getSlidedDueDate(isAddRequest, 2);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}
			}
			else if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON_AB_VALIDATION)){
				populateCommonFields(dcrRequest, paramTable);
				
				paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO, trnIdPrefix 
									+ KSKUtils.getFormattedStringFromNumber(transmittalId));
				if (dcrRequest.get(KSKUtils.AS_BUILT_VALIDATION).equals(KSKUtils.APPROVED)){
					paramTable.put(Field.DUE_DATE, "");				
				}
				else if (dcrRequest.get(KSKUtils.AS_BUILT_VALIDATION).equals(KSKUtils.REJECTED)){
					Timestamp gmtDueDate = KSKUtils.getSlidedDueDate(isAddRequest, 2);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}

				paramTable.put(Field.DESCRIPTION, "The AsBuilt file has been validated & has been" +
						"found as Correct/Incorrect (one of the two based on RFC Validation) & is transmitted via" +
						"letter of Transmittal number #" + trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [" + KSKUtils.DTN + "#" 
						+ transmittalRequest.getRequestId() + "]");
			}
		}
	}
	
	public static boolean isSetDCPLEleconDueDate(String statusName) {
		
		if (statusName.equals(KSKUtils.STATUS_A2) || statusName.equals(KSKUtils.STATUS_A3) ||statusName.equals(KSKUtils.STATUS_A4) 
				|| statusName.equals(KSKUtils.STATUS_A5) || statusName.equals(KSKUtils.STATUS_A6))
			return true;
		return false;
		
	}

	private void populateCommonFields(Request dcrRequest,
			Hashtable<String, String> paramTable) {
		paramTable.put(Field.STATUS, dcrRequest.getStatusId().getName());
		paramTable.put(Field.CATEGORY, dcrRequest.getCategoryId().getName());
		paramTable.put(Field.REQUEST_TYPE, dcrRequest.getRequestTypeId().getName());
		paramTable.put(Field.SEVERITY, dcrRequest.getSeverityId().getName());
		paramTable.put(Field.USER, "DC-ELECON-DCPL");
	}
}
