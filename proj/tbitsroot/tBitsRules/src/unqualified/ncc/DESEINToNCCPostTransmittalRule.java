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
public class DESEINToNCCPostTransmittalRule implements ITransmittalRule {

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
		
		//If transmittal from DESEIN to NCC and the current business area being updated is NCC.
		if ((dcrBA.getSystemId() == NCCTransmittalUtils.DESEIN_SYSTEM_ID) 
				&& (currentBA.getSystemId() == NCCTransmittalUtils.NCC_SYSTEM_ID)){
			try {
				Type decisionToNCC = dcrRequest.getExType(DECISION_TO_NCC);
				if (decisionToNCC != null){
					if (decisionToNCC.getName().equals("Approved")){
						paramTable.put(Field.DUE_DATE, "");
					}
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
				throw new TBitsException(e);
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException(e);
			}catch (NumberFormatException e) {
				e.printStackTrace();
				throw new TBitsException(e);
			}
		}			
	}
}
