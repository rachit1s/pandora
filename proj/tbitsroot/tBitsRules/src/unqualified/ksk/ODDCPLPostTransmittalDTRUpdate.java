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
public class ODDCPLPostTransmittalDTRUpdate implements ITransmittalRule {

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": DCPL initiated post transmittal update in DTR(DTR) business area";
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 4.1;
	}

	/* (non-Javadoc)
	 * @see ksk.ITransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, java.util.Hashtable, java.lang.String, int, boolean)
	 */
	public void process(Connection connection, int transmittalId, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable, String transmittalType,
			int businessAreaType, boolean isAddRequest) throws TBitsException {

		
		if(KSKUtils.isODDCPLDCR(dcrBA) && (businessAreaType == KSKUtils.DTR_BUSINESS_AREA)){

			int dcrSystemId = dcrBA.getSystemId();
			String trnIdPrefix = KSKUtils.getTransmittalIdPrefix(connection, dcrSystemId, transmittalType);
			paramTable.put(Field.USER, "WPCL,SEPCO");
			paramTable.put(Field.ASSIGNEE, "SEPCO");
			//paramTable.put(Field.STATUS, dcrRequest.getStatusId().getName());
			paramTable.put(Field.DESCRIPTION, "Document commented by WPCL via letter of Transmittal number #"
					+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
					+ " [DTN#" + transmittalRequest.getRequestId() + "]");
			
			if (transmittalType.equals(KSKUtils.TRANSMIT_TO_SEPCO)){				
				//Set the due-date.
				boolean isSetSlidedDueDate = KSKUtils.isSetDCPLDueDate(dcrRequest.getStatusId().getName());
				if (isSetSlidedDueDate){
					Timestamp gmtDueDate = KSKUtils.getSlidedDueDate(isAddRequest, 10);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}
				else
					paramTable.put(Field.DUE_DATE, "");
				
				paramTable.put(KSKUtils.FIELD_INCOMING_TRANSMITTAL_NO,
									trnIdPrefix + KSKUtils.getFormattedStringFromNumber(transmittalId));
				paramTable.put(Field.DESCRIPTION, "Document commented by WPCL via letter of Transmittal number #"
									+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
									+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				//TODO: Compute the actual % complete
				//4.	The actual % complete is computed based on the decision status given by DCPL.
			}
			else if(transmittalType.equals(KSKUtils.TRANSMIT_RFC_VALIDATION)){
				/*1.1.2.2.1	If the RFC is Approved
				1.	The Assignee changes from WPCL to SEPCO
				2.	The Due Date = NULL 
				3.	“RFC Validation” field is updated with the decision as given by DCPL.
				4.	Description is updated as “The RFC file has been validated by WPCL – DCPL & has been found as Correct & is transmitted via letter of Transmittal number #”.

				1.1.2.2.2	If the RFC is Rejected
				1.	The Assignee changes from WPCL to SEPCO
				2.	The Due Date = Current Date + 2 Days
				3.	“RFC Validation” field is updated with the decision as given by DCPL.
				4.	Description is updated as “The RFC file has been validated by WPCL – DCPL & has been found as Incorrect & is transmitted via letter of Transmittal number #”.
				*/
				
				paramTable.put(Field.DESCRIPTION, "The RFC file has been validated by WPCL-DCPL & has been found" +
						"as Correct/Incorrect (one of the two based on RFC Validation) & is transmitted via letter of" +
						" Transmittal number #"+ trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
				if (dcrRequest.get(KSKUtils.RFC_VALIDATION).equals(KSKUtils.APPROVED)){
					paramTable.put(Field.DUE_DATE, "");				
				}
				else if (dcrRequest.get(KSKUtils.RFC_VALIDATION).equals(KSKUtils.REJECTED)){
					Timestamp gmtDueDate = KSKUtils.getSlidedDueDate(isAddRequest, 2);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}

			}
			else if(transmittalType.equals(KSKUtils.TRANSMIT_AB_VALIDATION)){
				
				if (dcrRequest.get(KSKUtils.AS_BUILT_VALIDATION).equals(KSKUtils.APPROVED)){
					paramTable.put(Field.DUE_DATE, "");				
				}
				else if (dcrRequest.get(KSKUtils.AS_BUILT_VALIDATION).equals(KSKUtils.REJECTED)){
					Timestamp gmtDueDate = KSKUtils.getSlidedDueDate(isAddRequest, 2);
					paramTable.put(Field.DUE_DATE, gmtDueDate.toCustomFormat("yyyy-MM-dd HH:mm:ss"));
				}

				paramTable.put(Field.DESCRIPTION, "The AsBuilt file has been validated by WPCL-DCPL & has been" +
						"found as Correct/Incorrect (one of the two based on RFC Validation) & is transmitted via" +
						"letter of Transmittal number #" + trnIdPrefix +  KSKUtils.getFormattedStringFromNumber(transmittalId)
						+ " [DTN#" + transmittalRequest.getRequestId() + "]");
			}
		}
		
	}

}
