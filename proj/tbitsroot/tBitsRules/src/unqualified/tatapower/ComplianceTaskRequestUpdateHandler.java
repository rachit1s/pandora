/**
 * 
 */
package tatapower;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;

/**
 * @author Nitiraj
 *
 */
public class ComplianceTaskRequestUpdateHandler implements IRule {

	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, java.util.Hashtable, boolean, java.util.Collection)
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {		
		
		// first check if this is the ba to whome this rule applies
		String ba_list = TataPowerUtils.getProperty( "tatapower.ComplianceTaskRepeatitive.sysprefixs" ) ;
		String my_ba = ba.getSystemPrefix() ;
		//LOG.info("NITIRAJ : running ComplianceTaskRequestUpdateHandler : my_ba = " + my_ba + ", SysPrefix_list = " + ba_list) ;
		if( TataPowerUtils.isExistsInString(ba_list, my_ba))
		{
			// apply rule only for parent requests ..
			if( 0 == currentRequest.getParentRequestId() )
			{
		//		LOG.info("NITIRAJ: prerule : this is parent req and my requestid = " + currentRequest.getRequestId() ) ;
				try 
				{
					if( isAddRequest )
					{
		//				LOG.info("NITIRAJ: prerule : inside add request. putting the repeat date = null") ;
						// put next repeat date to null or ""				
						TataPowerUtils.setExDate(TataJobUtils.NEXT_REPEAT_DATE_FIELD_NAME,null, currentRequest) ;				
					}
					else
					{
		//				LOG.info("NITIRAJ: prerule : inside udpate request.") ;
						// this is update
						Timestamp old_fad = TataPowerUtils.getExDate(TataJobUtils.FIRST_ASSIGN_DATE_FIELD_NAME, oldRequest) ;
						Timestamp cur_fad = TataPowerUtils.getExDate(TataJobUtils.FIRST_ASSIGN_DATE_FIELD_NAME, currentRequest) ;
						String old_type = TataPowerUtils.getExType(TataJobUtils.REPEAT_TYPE_FIELD_NAME, oldRequest).getName() ;
						String cur_type = TataPowerUtils.getExType(TataJobUtils.REPEAT_TYPE_FIELD_NAME, currentRequest).getName() ;
						
						if( !old_fad.isSameDate(cur_fad) || !cur_type.equalsIgnoreCase(old_type) )
						{
		//					LOG.info("NITIRAJ: prerule : putting next repeat to null.") ;
							TataPowerUtils.setExDate( TataJobUtils.NEXT_REPEAT_DATE_FIELD_NAME,null,currentRequest ) ;					
						}				
					}
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();				
				}
			}
			else
			{
				// do nothing .. 
	//			LOG.info( "NITIRAJ: prerule : This is not a parent request." ) ;
			}
		}
		

		return new RuleResult( true );
	}
	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	public String getName() {		
		return this.getClass().getSimpleName() + "-This rule updates the date fields for repetitive tasks.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
