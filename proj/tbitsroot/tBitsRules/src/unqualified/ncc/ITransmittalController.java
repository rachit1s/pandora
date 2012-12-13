package ncc;

import java.sql.Connection;
import java.util.ArrayList;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;

public interface ITransmittalController {

	double getSequence();
	/**
	 * @return Gets the name of plug-in.  
	 */
	String getName();
	
	void process(Connection connection, BusinessArea dcrBA, ArrayList<Request> dcrRequestList,
			NCCTransmittalDropDownOption ntp, ArrayList<NCCTransmittalProcess> transmittalTypes, 
			NCCTransmittalProcess transmittalType) throws TBitsException;
}