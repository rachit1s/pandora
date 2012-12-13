/**
 * 
 */
package ipl;

import java.sql.Connection;
import java.util.Hashtable;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public class IPLPPreTransmittalRule implements IPreTransmittalRule {

	/* (non-Javadoc)
	 * @see ipl.IPreTransmittalRule#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName() + ": Pre-Transmittal Rule for SEPCO Business Area." ;
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
		// TODO Auto-generated method stub
		
	}

}
