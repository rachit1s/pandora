/**
 * 
 */
package ipl;

import java.sql.Connection;
import java.util.Hashtable;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public class SEPCOPreTransmittalRule implements IPreTransmittalRule {

	/* (non-Javadoc)
	 * @see ipl.IPreTransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Pre-Transmittal Rule for transmittal to IPLE from SEPCO business area." ;
	}

	/* (non-Javadoc)
	 * @see ipl.IPreTransmittalRule#getSequence()
	 */
	public double getSequence() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see ipl.IPreTransmittalRule#process(java.sql.Connection, int, transbit.tbits.domain.BusinessArea, java.lang.String[], java.util.Hashtable, java.lang.String, boolean)
	 */
	public void process(Connection connection, int transmittalRequestId,
			BusinessArea dcrBA, String[] dcrRequestList,
			Hashtable<String, String> dtnRequestParamTable,
			Hashtable<String, String> transmittalProcessParams,
			String transmittalType, boolean isAddRequest) throws TBitsException {
		
//		1.Logger : IPLE-mailing List
//		2.Assignee: DCPL-mailing List
//		3.Subject: DTN Title (Currently this field is filled with Transmittal Note – DTN No) 
//		  This should capture Transmittal Subject mention during Transmittal creation wizard.
//		4.DTN No: SEPCO DTN No – This should capture DTN No
//		5.Originator and Recipient type fields should be set in accordance with Logger and Assignee.
		
		/*dtnRequestParamTable.put(Field.USER, "IPLE");
		dtnRequestParamTable.put(Field.ASSIGNEE, "DCPL");
		dtnRequestParamTable.put(Field.SUBJECT, "test transmittal request.");*/
		//dtnRequestParamTable.put("", "");
		

	}

}
