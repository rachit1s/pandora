/**
 * 
 */
package lntCCC;

import java.sql.Connection;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * @author Lokesh
 *
 */
public class CommentFilesRule implements IRule {

	private static final String COMMENTED_FILE_FROM_CCC = "CommentedFilefromCCC_SO";
	
	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, boolean)
	 */
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		RuleResult ruleResult = new RuleResult();
		String baListStr = PropertiesHandler.getProperty(LnTCCCUtils.PLUGIN_LNTCCC_RULES_BALIST);
		boolean isApplicableBA = LnTCCCUtils.isApplicableBA(baListStr, ba);
		
		if (!isApplicableBA)
			return ruleResult;
		
		String commentedFiles = currentRequest.get(COMMENTED_FILE_FROM_CCC);
		if ((commentedFiles != null) && (commentedFiles.trim().length()!= 0)){
			Type preparedStatusId;
			try {
				preparedStatusId = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), Field.STATUS, "Prepared");
				if (preparedStatusId != null)
					currentRequest.setStatusId(preparedStatusId);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}			
		}
		
		return ruleResult;
	}


	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	@Override
	public String getName() {		
		return this.getClass().getSimpleName();
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	@Override
	public double getSequence() {
		return 0;
	}

}
