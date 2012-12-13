package tpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public class TERRules implements IRule {
	
	private Date datecalculation(int a)
	{
		Calendar currentDate = Calendar.getInstance();
		  SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		  String dateNow = formatter.format(currentDate.getTime());
		  Date date=null;
		  try {
			date = date = (Date)formatter.parse(dateNow);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(date);  
		cal.add(Calendar.DATE, a);
		  
		date = cal.getTime();
		return date;
	}

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		// TODO Auto-generated method stub
		String sysPrefix = ba.getSystemPrefix();
		if(sysPrefix.equalsIgnoreCase("TPL_TER"))
		{
			Collection<AttachmentInfo> attachments = (Collection<AttachmentInfo>) (currentRequest.getObject("attachments"));
			Collection<AttachmentInfo> comments = (Collection<AttachmentInfo>) (currentRequest.getObject("Comments"));
			Collection<AttachmentInfo> oldattachments = null;
			int j=0;
			if(!isAddRequest)
			{
			oldattachments = (Collection<AttachmentInfo>) (oldRequest.getObject("attachments"));
			
			for(AttachmentInfo a : oldattachments)
				for(AttachmentInfo b : attachments)
				{
					if(a.repoFileId==b.repoFileId)
						j++;
				}
			}

			Integer duration=0;
			String TCEDuriations = currentRequest.get("TCEDuriation");
			Integer TCEDuriation = Integer.parseInt(TCEDuriations);
				if (!attachments.isEmpty() && comments.isEmpty() && TCEDuriation.intValue()==0)
				{
//					int a  = currentRequest.getMaxActionId();
					if(isAddRequest)
					{
						duration=(TCEDuriation.intValue()+5);
						currentRequest.setObject("due_datetime", datecalculation(5));
						currentRequest.setObject("TCEDuriation", duration);
						return new RuleResult(true);
					}
					else if((oldattachments.size()!=attachments.size()) && oldattachments.isEmpty())
					{
						duration=(TCEDuriation.intValue()+5);
						currentRequest.setObject("due_datetime", datecalculation(5));
						currentRequest.setObject("TCEDuriation", duration);
						return new RuleResult(true);
					}
				}
				else if (!attachments.isEmpty() && !comments.isEmpty() && TCEDuriation.intValue()==5)
				{   
			            if(oldattachments.size()!=attachments.size())
			            {
			            	duration=(TCEDuriation.intValue()+3);
			            	currentRequest.setObject("due_datetime", datecalculation(3));
			            	currentRequest.setObject("TCEDuriation", duration);
			            	return new RuleResult(true);
			            }
			            else if (j!=attachments.size())
			            {
			            	duration=(TCEDuriation.intValue()+3);
			            	currentRequest.setObject("due_datetime", datecalculation(3));
			            	currentRequest.setObject("TCEDuriation", duration);
			            	return new RuleResult(true);
			            }
				}
				
				else 
				{
					if(!isAddRequest && TCEDuriation.intValue()==8)
					{
						if(oldattachments.size()!=attachments.size())
						{
						currentRequest.setObject("due_datetime", datecalculation(1));
						return new RuleResult(true);
						}
						else if(j!=attachments.size())
						{
							currentRequest.setObject("due_datetime", datecalculation(1));
							return new RuleResult(true);
						}
					}
				}
		}
		return null;
	}

	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
