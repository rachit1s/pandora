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

public class NetInvoiceDateCalculationForBill implements IRule {
	
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			Collection<AttachmentInfo> attachments) {
		
		double netInvoiceValue = 0;
		RuleResult ruleResult = new RuleResult();
		
		String sysPrefix = ba.getSystemPrefix();
		boolean isApplicable = TataPowerUtils.isExistsInBillBABasedRulesSysPrefixes(sysPrefix);
		
		if (isAddRequest && isApplicable) {			
			try {
				Type percentageType = currentRequest.getExType(TataPowerUtils.PERCENTAGE);
				int percentage = Integer.parseInt(percentageType.getName());
				double invoiceValue = currentRequest.getExReal(TataPowerUtils.INVOICE_VALUE_FIELD);
				netInvoiceValue = invoiceValue * percentage / 100;
				currentRequest.setExReal(TataPowerUtils.NET_INVOICE_VALUE_FIELD, netInvoiceValue);				
			} catch (IllegalStateException e) {
				e.printStackTrace();
				ruleResult.setMessage("Illegal State Exception occurred, hence rule did not succeed, but continuing adding request.");
			} catch (DatabaseException e) {
				e.printStackTrace();
				ruleResult.setMessage("Database exception occurred, hence rule did not succeed, but continuing adding request.");
			}
			ruleResult.setMessage("Rule succeeded and the net invoice value is: " + netInvoiceValue);
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
