/**
 * 
 */
package ncc;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Hashtable;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.exception.TBitsException;
import transmittal.com.tbitsGlobal.server.ITransmittalRule;
import transmittal.com.tbitsGlobal.server.TransmittalProcess;

/**
 * @author lokesh
 *
 */
public class NCCPostTransmittalRulesHandler implements ITransmittalRule {



	public static final String SUBMISSION_DATEBY_SOURCE = "SubmissionDatebySource";
	public static final String TYPE_APPROVED = "Approved";
	public static final String DECISION_TO_NCC = "DecisionToNCC";

	/* (non-Javadoc)
	 * @see ncc.ITransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/* (non-Javadoc)
	 * @see ncc.ITransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 0;
	}

	public void process(Connection connection, Request transmittalRequest,
			BusinessArea currentBA, BusinessArea dcrBA, Request dcrRequest,
			Hashtable<String, String> paramTable,
			TransmittalProcess transmittalProcess,
			HashMap<String, String> transmittalParams, int businessAreaType,
			boolean isAddRequest) throws TBitsException {

		if ((dcrBA == null) || (currentBA == null))
			return;

		//For transmittals from NCC.
		if (dcrBA.getSystemId() == NCCTransmittalUtils.NCC_SYSTEM_ID){ 
			Type decisionFromNCC = dcrRequest.getRequestTypeId();
			switch (currentBA.getSystemId()){
				case NCCTransmittalUtils.NCC_SYSTEM_ID:{
					try {						
						
						if (decisionFromNCC != null){ 							
							if (decisionFromNCC.getName().equals(TYPE_APPROVED)){
								paramTable.put(SUBMISSION_DATEBY_SOURCE, "");
								return;
							}
						}
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}
					break;
				}			
				case NCCTransmittalUtils.CSEPDI_SYSTEM_ID:{
					try {
						
						if (decisionFromNCC != null){ 
							if (decisionFromNCC.getName().equals(TYPE_APPROVED)){
								paramTable.put(Field.DUE_DATE, "");
								return;
							}
						}
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}
					break;
				}
				case NCCTransmittalUtils.DCPL_SYSTEM_ID:{
					try {						
						
						if (decisionFromNCC != null){ 
							if (decisionFromNCC.getName().equals(TYPE_APPROVED)){
								paramTable.put(Field.DUE_DATE, "");
								return;
							}
						}
						//getName().equals("TransmitToDCPLWorkFlow6")
						if (transmittalProcess.getTrnProcessId() == 10){
							paramTable.put("DTNFromNCC", dcrRequest.getExString("DTNToDCPL"));
						}
						
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
					break;
				}				
				case NCCTransmittalUtils.VENDOR_SYSTEM_ID:{
					try {						
						
						if (decisionFromNCC != null){ 
							if (decisionFromNCC.getName().equals(TYPE_APPROVED)){
								paramTable.put(Field.DUE_DATE, "");
								return;
							}
						}						
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}
					break;
				}
				case NCCTransmittalUtils.STUP_SYSTEM_ID:{
					try {						
						
						if (decisionFromNCC != null){ 
							if (decisionFromNCC.getName().equals(TYPE_APPROVED)){
								paramTable.put(Field.DUE_DATE, "");
								return;
							}
						}						
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}
					break;
				}
				//case TransmittalUtils.
				//If required add something to default.
				default: break;
			}
			
		}		
	}
}
