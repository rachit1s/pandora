/**
 * 
 */
package ncc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;
import transmittal.com.tbitsGlobal.server.ITransmittalController;
import transmittal.com.tbitsGlobal.server.TransmittalDropDownOption;
import transmittal.com.tbitsGlobal.server.TransmittalProcess;

/**
 * @author lokesh
 *
 */
public class NCCOrigniatingTransmittalController implements ITransmittalController {

	/* (non-Javadoc)
	 * @see ncc.IPreController#getName()
	 */
	public String getName() {		
		return this.getClass().getSimpleName();
	}

	/* (non-Javadoc)
	 * @see ncc.IPreController#getSequence()
	 */
	public double getSequence() {
		return 0;
	}

	@Override
	public void process(Connection connection, BusinessArea dcrBA,
			ArrayList<Request> dcrRequestList, TransmittalDropDownOption ntp,
			ArrayList<TransmittalProcess> transmittalTypes,
			TransmittalProcess transmittalType) throws TBitsException {
		
		if (ntp.getDcrSystemId() == NCCTransmittalUtils.NCC_SYSTEM_ID){
			
			switch (ntp.getId()){				
				case NCCTransmittalUtils.NCC_TO_DCPL_DROPDOWN_ID:{
					StringBuffer wf5Details = new StringBuffer(); 
					StringBuffer wf6Details = new StringBuffer();
					
					try{
						boolean isContinueWF5 = WorkflowRules.isBelongsToWorkFlow5(dcrRequestList, wf5Details);				
						if (isContinueWF5){
							transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
									dcrBA.getSystemId(), 9));
							return;
						}
						boolean isContinueWF6 = WorkflowRules.isBelongsToWorkFlow6(dcrRequestList, wf6Details);
						if (isContinueWF6){
							transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
									dcrBA.getSystemId(), 10));
							return;
						}
						if ((!isContinueWF5) && (!isContinueWF6)){
							TBitsException tbe = new TBitsException(NCCTransmittalUtils.ALL_DRAWINGS_SAME_CATEGORY_MSG + wf5Details + NCCTransmittalUtils.BR_B_OR_B_BR + wf6Details);							
//							tbe.setDescription("All the drawings selected must belong to one of the following categories:<br>"
//									+ wf5Details + "<b>OR</b>" + wf6Details);
							throw tbe;
						}
						
					} catch(SQLException sqle){
						throw new TBitsException("Error occurred while retrieving sub-process for transmittal from " + dcrBA.getSystemPrefix());
					}
					break;
				}
				case NCCTransmittalUtils.NCC_TO_EDTD_DROPDOWN_ID:{
					StringBuffer wf3Details = new StringBuffer(); 
					StringBuffer wf6Details = new StringBuffer();
					
					try{
						boolean isContinueWF5 = WorkflowRules.isBelongsToWorkFlow3(dcrRequestList, wf3Details);				
						if (isContinueWF5){
							transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
									dcrBA.getSystemId(), 13));
							return;
						}
						boolean isContinueWF6 = WorkflowRules.isBelongsToWorkFlow6(dcrRequestList, wf6Details);
						if (isContinueWF6){
							transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
									dcrBA.getSystemId(), 14));
							return;
						}
						if ((!isContinueWF5) && (!isContinueWF6)){
							TBitsException tbe = new TBitsException(NCCTransmittalUtils.ALL_DRAWINGS_SAME_CATEGORY_MSG + wf3Details.toString() +
									NCCTransmittalUtils.BR_B_OR_B_BR + wf6Details.toString());
//							tbe.setDescription(TransmittalUtils.ALL_DRAWINGS_SAME_CATEGORY_MSG + wf3Details.toString() +
//									TransmittalUtils.BR_B_OR_B_BR + wf6Details.toString());
							throw tbe;
						}
						
					} catch(SQLException sqle){
						throw new TBitsException("Error occurred while retrieving sub-process for transmittal from " + dcrBA.getSystemPrefix());
					}
				}
				case NCCTransmittalUtils.NCC_TO_VENDOR1_DROPDOWN_ID:{
					StringBuffer wf6Details = new StringBuffer();
					
					try{
						boolean isContinueWF6 = WorkflowRules.isBelongsToWorkFlow6(dcrRequestList, wf6Details);				
						if (isContinueWF6){
							transmittalType.setTransmittalType(TransmittalProcess.lookupTransmittalProcessBySystemIdAndTransmittalProcessId(
									dcrBA.getSystemId(), 19));
							return;
						}
						
						if (!isContinueWF6){
							TBitsException tbe = new TBitsException(wf6Details.toString());
//							tbe.setDescription(TransmittalUtils.ALL_DRAWINGS_SAME_CATEGORY_MSG + wf1Details.toString());
							throw tbe;
						}
						
					} catch(SQLException sqle){
						throw new TBitsException("Error occurred while retrieving sub-process for transmittal from " + dcrBA.getSystemPrefix());
					}
				}				
			}
		}
		
	}
}