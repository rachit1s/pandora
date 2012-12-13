package billtracking.com.tbitsGlobal.server;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.tbitsGlobal.jaguar.client.serializables.RequestData;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;
import transbit.tbits.webapps.WebUtil;
import billtracking.com.tbitsGlobal.client.services.BillService;
import billtracking.com.tbitsGlobal.shared.IBillConstants;
import billtracking.com.tbitsGlobal.shared.IBillProperties;

import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;
import commons.com.tbitsGlobal.utils.client.pojo.POJOBoolean;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;

public class BillServiceImpl extends TbitsRemoteServiceServlet implements BillService,IBillProperties,IBillConstants,IState {

	private static final long serialVersionUID = 1L;

	public HashMap<String,String> getBillProperties() {
		// TODO Auto-generated method stub
		return BillProperties.getBillProperties();
	}


	public List<RequestData> linkBills(ArrayList<Integer> srcReqIdList,String srcSysPrefix) {
		// TODO Auto-generated method stub
		try {
			BusinessArea srcBa = null ;

			srcBa = BusinessArea.lookupBySystemPrefix(srcSysPrefix);

			if(srcBa==null)
				throw new TBitsException("invalid BA");
			HttpServletRequest request=this.getRequest();
			User user  = WebUtil.validateUser(request);

			String targetBaSysPrefix=BillProperties.get(PROPERTY_BILL_BA_PREFIX);
			BusinessArea targetBa=BusinessArea.lookupBySystemPrefix(targetBaSysPrefix);
			List<RequestData> rdList=new ArrayList<RequestData>();
			for(Integer srcReqId:srcReqIdList){
				TbitsTreeRequestData sourceRequestModel=GWTServiceHelper.getDataByRequestId(user, srcBa, srcReqId);

				ArrayList <DisplayGroupClient> dgcal = GWTServiceHelper.getDisplayGroups(targetBa) ;
				List<BAField> baFields = GWTServiceHelper.getFields(targetBa, user) ;

				if( null == baFields||null ==dgcal ){
					throw new Exception("Cannot find Fields or DisplayGroups for BusinessArea with sysPrefix : " + targetBa.getSystemPrefix());

				}else{
					ArrayList<BAField> bfal = new ArrayList<BAField>() ;
					bfal.addAll(baFields);
					TbitsTreeRequestData ttrd = new TbitsTreeRequestData();
					if( sourceRequestModel != null )
					{
						String linkedReq=srcSysPrefix+"#"+sourceRequestModel.getRequestId();																													
						ttrd.set(LinkedRequests,linkedReq);
						ttrd.set(Field.BUSINESS_AREA,targetBa.getSystemId());
						ttrd.set(Field.USER,user.getUserLogin());
						ttrd.set(Subject,sourceRequestModel.get(IFixedFields.SUBJECT));
						ttrd.set(Description, sourceRequestModel.get(IFixedFields.DESCRIPTION));

						POJOAttachment pojoAtt=(POJOAttachment)sourceRequestModel.getAsPOJO(IFixedFields.ATTACHMENTS);
						ttrd.set(OtherAttachments,pojoAtt);
						ttrd.set(Project, sourceRequestModel.get(IFixedFields.CATEGORY));
						RequestData rd = new RequestData(targetBaSysPrefix,0,ttrd,dgcal,bfal) ;
						rdList.add(rd);
					}
				} 
			}

			return rdList;
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TbitsExceptionClient e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


	public String getAttachmentMsg(String stateId,TbitsTreeRequestData model,String sysPrefix) {
		// TODO Auto-generated method stub
		BusinessArea ba;
		StringBuffer sb=new StringBuffer();
		try {
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);

			Process process=Process.getProcess(model);
			int id=0;
			if(stateId!=null)
				id = Integer.parseInt(stateId);
			State state = process.getState(id);
			String attFieldIds=state.get(state_attachment_ids);

			if(attFieldIds!=null){
				ArrayList<String> attids=Utilities.toArrayList(attFieldIds);

				if(attids!=null){
					for(String str:attids){
						int attid=Integer.parseInt(str);
						Field f=Field.lookupBySystemIdAndFieldId(ba.getSystemId(),attid);
						POJOBoolean isChecked=(POJOBoolean)model.get(f.getName());
						if(isChecked.equals("false"))
							sb.append("\nPlease Attach the Document: "+f.getDisplayName());

					}
				}
			}



		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}


	@Override
	public Boolean belongsToRole(String sysPrefix,String userName, String roleName) {
		return userBelongsToRole(sysPrefix, userName, roleName);
	}


	public static Boolean userBelongsToRole(String sysPrefix, String userName,
			String roleName) {
		User user;
		try {
			user = User.lookupAllByUserLogin(userName);
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			Role role =	Role.lookupBySystemIdAndRoleName(ba.getSystemId(),roleName);
			ArrayList<Role> roleList=null;
		
			if(role!=null && user!=null)
			roleList = Role.lookupRolesBySystemIdAndUserId(ba.getSystemId(),user.getUserId());
			
			if(roleList!=null)
			for(Role r:roleList){
				if(r.getRoleName().equals(role.getRoleName()))return true;
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return false;
	}

}


