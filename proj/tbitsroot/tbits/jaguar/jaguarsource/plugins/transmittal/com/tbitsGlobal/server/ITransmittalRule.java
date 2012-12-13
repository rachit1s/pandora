package transmittal.com.tbitsGlobal.server;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Hashtable;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;
import transmittal.com.tbitsGlobal.server.TransmittalProcess;

public interface ITransmittalRule {

	//Connection connection, BusinessArea ba, Request oldRequest, Request currentRequest, int Source, User user,
	//Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest, Collection<AttachmentInfo> attachments
	void process(Connection connection, Request transmittalRequest, BusinessArea currentBA, BusinessArea dcrBA, 
			Request dcrRequest, Hashtable <String,String> paramTable, TransmittalProcess transmittalProcess, 
			HashMap<String, String> transmittalParams, int businessAreaType, boolean isAddRequest)
	throws TBitsException;
	double getSequence();
	/**
	 * @return Gets the name of plug-in.  
	 */
	String getName();
}