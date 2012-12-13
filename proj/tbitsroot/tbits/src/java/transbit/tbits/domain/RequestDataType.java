package transbit.tbits.domain;

import java.io.Serializable;


public class RequestDataType implements Serializable
{	
	private int sysId ;
	private int requestId ;
	/**
	 * ActionId can be zero in case where you are talking about a request as a whole
	 * and not about any particular action of that request
	 */
	private int actionId ;
	
	public RequestDataType(int sysId, int requestId)
	{
		this.sysId = sysId ;
		this.requestId = requestId ;
		this.actionId = 0 ;
	}

	public RequestDataType(int sysId, int requestId, int actionId)
	{
		this.sysId = sysId ;
		this.requestId = requestId ;
		this.actionId = actionId ;
	}
	
	public int getSysId()
	{
		return sysId ;
	}
	
	public int getRequestId()
	{
		return requestId ;
	}
	
	public int getActionid() 
	{
		return actionId ;
	}
}
