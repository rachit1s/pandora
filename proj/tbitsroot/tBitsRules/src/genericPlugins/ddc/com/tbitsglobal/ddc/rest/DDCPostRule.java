/**
 * 
 */
package ddc.com.tbitsglobal.ddc.rest;

import java.sql.Connection;

import ddc.com.tbitsglobal.ddc.exception.FailedToFindObject;

import transbit.tbits.api.IPostRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class DDCPostRule implements IPostRule{

	public static TBitsLogger logger = TBitsLogger.getLogger("ddc");
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IPostRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, boolean)
	 */
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		String ddcBA = PropertiesHandler.getProperty("ddcBa");
		if(ddcBA == null)
		{
			logger.info("ddcBa property not set. So skipping ddc");
			return new RuleResult(true,"ddcBa property not set. So skipping ddc",true);
		}
		
		if( !ddcBA.equals(ba.getSystemPrefix()))
		{
			logger.info("This is not the ddcBa. So skipping the rule");
			return new RuleResult(true,"This is not the ddcBa. So skipping the rule",true);
		}
		
		if( !isAddRequest )
		{
			logger.info("not executing ddc rule for update request.");
			return new RuleResult(true,"not executing ddc rule for update request.",true);
		}
		
		
		try {
			DDCObject ddco= new DDCObject(ba.getSystemPrefix(), currentRequest.getRequestId());
			ddco.doDDC();
			return new RuleResult(true,"DDC rule executed successfully.",true);
		} catch (Throwable e) {
			e.printStackTrace();
			logger.error(e.getMessage(),e);
			return new RuleResult(false,"DDC rule failed. Please see logs for more detail.",true);
		}
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IPostRule#getSequence()
	 */
	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IPostRule#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "DDCRule";
	}

}
