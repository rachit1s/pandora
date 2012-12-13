package transmittal.com.tbitsGlobal.server;

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
			TransmittalDropDownOption ntp, ArrayList<TransmittalProcess> transmittalTypes, 
			TransmittalProcess transmittalType) throws TBitsException;
}