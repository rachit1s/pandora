package icms.icmscommon;

import java.sql.Connection;
import java.util.Collection;

import transbit.tbits.api.APIUtil;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestDataType;
import transbit.tbits.domain.User;

public class QapQuantityTrackSupport implements IRule {

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		String sysPrefix=ba.getSystemPrefix();
		if(sysPrefix.equalsIgnoreCase("INSPECTION") && isAddRequest)
		{
			
			Integer offerQty=Integer.parseInt((String)currentRequest.getObject("OfferQuantity"));
			String linkedRequest=currentRequest.getRelatedRequests();
			Collection<RequestDataType> rdt=APIUtil.getRequestCollection(linkedRequest);
			for(RequestDataType rd:rdt)
			{
				 int sysId=rd.getSysId();
				
					try {
						Request r=Request.lookupBySystemIdAndRequestId(rd.getSysId(), rd.getRequestId());
						Integer inventory=(Integer) r.getObject("inventory");
						if(offerQty > inventory)
						{
							return new RuleResult(false, "You can not offer more than inventory in offer quantity");
						}
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
				
			}
		}
		return new RuleResult(true);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 2.0;
	}

}
