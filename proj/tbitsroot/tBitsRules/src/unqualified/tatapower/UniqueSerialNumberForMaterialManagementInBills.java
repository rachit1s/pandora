package tatapower;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class UniqueSerialNumberForMaterialManagementInBills implements IRule {
	
	final static String GET_MAX_ID_PROCEDURES = "stp_getAndIncrMaxId";
	final static String  DEPT_BILL_SEQ = "depttBillSeq";

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		RuleResult ruleResult = new RuleResult();
		int mmSeqNumber = 0;
		Type categoryType = currentRequest.getCategoryId();
		if (isAddRequest && TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(ba.getSystemPrefix())
				&& categoryType.getName().equals(TataPowerUtils.MATERIAL_MANAGEMENT)){
			try {
				mmSeqNumber = TataPowerUtils.getUniqMaxId(connection, "materialManagementSeqNumber");
				currentRequest.setExInt(DEPT_BILL_SEQ, mmSeqNumber);
			} catch (SQLException e) {
				e.printStackTrace();
				ruleResult.setMessage("Database exception occurred, hence rule did not succeed, but continuing adding request.");
			} catch (DatabaseException e) {
				e.printStackTrace();
				ruleResult.setMessage("Database exception occurred, hence rule did not succeed, but continuing adding request.");
			}
			ruleResult.setMessage("Rule succeded and the material management sequence number is: " + mmSeqNumber);
		}
		else
			ruleResult.setMessage("Not applicable.");
				
		return ruleResult;
	}

	public String getName() {
		return this.getClass().getSimpleName() + "Sets unique serial number for Material Management in Bills business area.";
	}

	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
