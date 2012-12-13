package ipl;

import java.sql.Connection;
import java.util.Hashtable;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.exception.TBitsException;

public interface IPreTransmittalRule {

	double getSequence();
	/**
	 * @return Gets the name of plug-in.  
	 */
	String getName();
	
	void process(Connection connection, int transmittalRequestId,
			BusinessArea dcrBA, String[] dcrRequestList,
			Hashtable<String, String> dtnRequestParamTable, 
			Hashtable<String, String> transmittalProcessParams,
			String transmittalType, boolean isAddRequest) throws TBitsException;
}