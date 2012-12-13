package rulestrainingtests.nirmal_a;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.mail.IMailPreProcessor;

public class MailAttachmentRule1 implements IMailPreProcessor {

	@Override
	public void executeMailPreProcessor(User user, Request request,
			ArrayList<Action> actionList,
			Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash,
			Hashtable<String, Integer> permissions) {
		int sys_id=request.getSystemId();
		BusinessArea ba;
		Field attField;
		Field otherAttField;
		try {
			
			 ba=BusinessArea.lookupBySystemId(sys_id);
			 attField=Field.lookupBySystemIdAndFieldName(sys_id,Field.ATTACHMENTS);
			 otherAttField=Field.lookupBySystemIdAndFieldName(sys_id, "VendorSubmissionFile");
			 if(!ba.getSystemPrefix().equalsIgnoreCase("GMR_SIEMENS"))
			 return;
			 
			 Collection<ActionFileInfo> myActionFileInfo=actionFileHash.get(request.getMaxActionId());
			 ArrayList<ActionFileInfo> list = new ArrayList<ActionFileInfo>(myActionFileInfo);
			 for(ActionFileInfo af:list)
			 {
				 System.out.println(af);
			 }
			 
			 Collections.sort(list, new Comparator<ActionFileInfo>() {

				@Override
				public int compare(ActionFileInfo o1, ActionFileInfo o2) {
					int i= o1.getName().compareToIgnoreCase(o2.getName());
					return i;
				}
			});
			 int count=1;
			 for(ActionFileInfo af:list)
			 {
				 
				if(af.getFieldId()==attField.getFieldId())
				{
					af.setPriority(count);
					count++;
					
				}
				
				if(af.getFieldId()==otherAttField.getFieldId())
				{
					af.setPriority(-1);
				}
				 
			 }
			 
		} catch (DatabaseException e) {
			
			
			e.printStackTrace();
		}
		
		

	}

	@Override
	public String getMailPreProcessorName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getMailPreProcessorOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

}
