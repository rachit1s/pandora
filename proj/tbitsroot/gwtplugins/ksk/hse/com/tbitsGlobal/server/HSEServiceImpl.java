package hse.com.tbitsGlobal.server;
import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_AGE;
import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_ANY_OTHER_INFORMATION;
import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_BRIEF_DESCRIPTION;
import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_CONDITIONS;
import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_CONTRACTOR;
import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_DATE;
import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_DEPARTMENT;
import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_DESIGNATION;
import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_EQUIPMENT;
import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_INJURY_TYPE;
import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_LINKED_REQUEST;
import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_LOCATION;
import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_Name;
import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_PREFIX;
import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_Remidy;
import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_SEX;
import static hse.com.tbitsGlobal.shared.HSEConstants.AIR_SUBJECT;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_AGE;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_BRIEF_DESCRIPTION;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_CONDITIONS;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_CONTRACTOR;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_DATE;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_DEPARTMENT;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_DESIGNATION;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_EQUIPMENT;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_INJURY_TYPE;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_LINKED_REQUEST;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_LOCATION;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_NAME;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_OTHER_INFO;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_PREFIX;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REMIDY;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_SEX;
import hse.com.tbitsGlobal.client.HSEService;

import java.util.ArrayList;
import java.util.List;

import com.tbitsGlobal.jaguar.client.serializables.RequestData;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;

public class HSEServiceImpl extends TbitsRemoteServiceServlet implements HSEService{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String parseId(String relString,String sysPrefix){
		return HSEUtils.extractRelatedRequestId(relString, sysPrefix);
	}

	@Override
	public RequestData getTransferRequestData(String userLogin,TbitsTreeRequestData sourceRequestModel) {
		// TODO : changes here
		User user;
		try {
			user = User.lookupAllByUserLogin(userLogin);
			BusinessArea ba=BusinessArea.lookupBySystemPrefix(AIR_PREFIX);
			ArrayList <DisplayGroupClient> dgcal = GWTServiceHelper.getDisplayGroups(ba) ;
			List<BAField> baFields = GWTServiceHelper.getFields(ba, user) ;
			if( null == baFields||null ==dgcal ){
				throw new Exception("Cannot find Fields or DisplayGroups for BusinessArea with sysPrefix : " + ba.getSystemPrefix());

			}else
			{
				ArrayList<BAField> bfal = new ArrayList<BAField>() ;
				bfal.addAll(baFields);
				TbitsTreeRequestData ttrd = new TbitsTreeRequestData();

				if( sourceRequestModel != null )
				{
					String linkedReq=PAR_PREFIX+"#"+sourceRequestModel.getRequestId();																													
					String subject= "Contractor:"+sourceRequestModel.getAsPOJO(PAR_CONTRACTOR)+" "+"person:" +sourceRequestModel.getAsString(PAR_NAME) +" "+"injury type:"+sourceRequestModel.getAsString(PAR_INJURY_TYPE);
					ttrd.set(AIR_LINKED_REQUEST,linkedReq);
					ttrd.set(AIR_BRIEF_DESCRIPTION,sourceRequestModel.getAsPOJO(PAR_BRIEF_DESCRIPTION));
					ttrd.set(AIR_LOCATION,sourceRequestModel.getAsPOJO(PAR_LOCATION));
					ttrd.set(AIR_Name,sourceRequestModel.getAsPOJO(PAR_NAME));
					ttrd.set(AIR_AGE,sourceRequestModel.getAsPOJO(PAR_AGE));
					ttrd.set(AIR_SEX,sourceRequestModel.getAsPOJO(PAR_SEX));
					ttrd.set(AIR_CONDITIONS,sourceRequestModel.getAsPOJO(PAR_CONDITIONS));
					ttrd.set(AIR_EQUIPMENT,sourceRequestModel.getAsPOJO(PAR_EQUIPMENT));
					ttrd.set(AIR_ANY_OTHER_INFORMATION,sourceRequestModel.getAsPOJO(PAR_OTHER_INFO));
					ttrd.set(AIR_Remidy,sourceRequestModel.getAsPOJO(PAR_REMIDY));
					ttrd.set(AIR_SUBJECT,subject);
					ttrd.set(AIR_DESIGNATION,sourceRequestModel.getAsPOJO(PAR_DESIGNATION));
					ttrd.set(AIR_CONTRACTOR,sourceRequestModel.getAsPOJO(PAR_CONTRACTOR));
					ttrd.set(AIR_INJURY_TYPE,sourceRequestModel.getAsPOJO(PAR_INJURY_TYPE));
					ttrd.set(AIR_DEPARTMENT,sourceRequestModel.getAsPOJO(PAR_DEPARTMENT));
					ttrd.set(AIR_DATE,sourceRequestModel.getAsPOJO(PAR_DATE));
					ttrd.set(Field.BUSINESS_AREA,ba.getSystemId());	
					



					String relReq=sourceRequestModel.getAsString(PAR_LINKED_REQUEST);
					String relReqId=HSEUtils.extractRelatedRequestId(relReq, AIR_PREFIX);

					if(relReq.isEmpty()){
						RequestData rd = new RequestData(AIR_PREFIX,0,ttrd,dgcal,bfal) ;
						return rd;
					}else{
						RequestData rd = new RequestData(AIR_PREFIX,Integer.parseInt(relReqId),ttrd,dgcal,bfal) ;
						return rd;

					}
				} 
			}

		}catch (DatabaseException e) {
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
}

