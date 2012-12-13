package ncchse;

import java.io.File;
import java.sql.Connection;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import static ncchse.HSEConstants.*;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.webapps.WebUtil;

public class AIRPreRule implements IRule {


	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user, boolean isAddRequest) {

		if(ba.getSystemPrefix().equalsIgnoreCase(AIR_PREFIX)){
			try{

				int airReportId=getNextRepNo(connection,"AIR_REP_ID");
				String rid = Integer.toString(currentRequest.getRequestId()) ;
				
				
				String accName = currentRequest.get(AIR_ACCIDENT_CATEGORY);
				Type acat=Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(),AIR_ACCIDENT_CATEGORY, accName);
				Type inj=currentRequest.getRequestTypeId();
				Type cont=currentRequest.getCategoryId();
				
				//Timestamp ws=currentRequest.getExDate(AIR_WORKING_SINCE);
                String workSince=Timestamp.toCustomFormat((Date)currentRequest.getObject(AIR_WORKING_SINCE),"yyyy-MM-dd");
				if (workSince==null)
                	workSince=" ";
				                
				
				Hashtable<String,String> params = new Hashtable<String,String>();
				params.put(AIR_REP_REPORTNO,"AIR-"+airReportId);
				params.put(AIR_REP_SITEENGINEER,currentRequest.get(AIR_SITE_ENGINEER));
				params.put(AIR_REP_HSEOFFICER,currentRequest.get(AIR_HSE_OFFICER));
				params.put(AIR_REP_SITEINCHARGE,currentRequest.get(AIR_SITE_INCHARGE));
				params.put(AIR_REP_SUBCONTRACTOR,cont.getDisplayName());
				params.put(AIR_REP_CATEGORY,acat.getDisplayName());
				params.put(AIR_REP_NAME,currentRequest.get(AIR_Name));
				params.put(AIR_REP_AGE,currentRequest.get(AIR_AGE));
				params.put(AIR_REP_SEX,currentRequest.get(AIR_SEX));
				params.put(AIR_REP_DESIGNATION,currentRequest.get(AIR_DESIGNATION));
				params.put(AIR_REP_WORKINGSINCE,workSince);
				params.put(AIR_REP_DATE,currentRequest.get(AIR_DATE));
				params.put(AIR_REP_LOCATION,currentRequest.get(AIR_LOCATION));
				params.put(AIR_REP_NATURE,inj.getDisplayName());
				params.put(AIR_REP_EQUIPMENT,currentRequest.get(AIR_EQUIPMENT));
				params.put(AIR_REP_DESCRIPTION,currentRequest.get(AIR_BRIEF_DESCRIPTION));
				params.put(AIR_REP_DIRECTCAUSE,currentRequest.get(AIR_DIRECT_CAUSE));
				params.put(AIR_REP_INDIRECTCAUSE,currentRequest.get(AIR_INDIRECT_CAUSE));
				params.put(AIR_REP_PRECAUTIONARY,currentRequest.get(AIR_Remidy));
				params.put(AIR_REP_MANHOURLOST,currentRequest.get(AIR_LOST_MANHOUR));
				params.put(AIR_REP_OTHERINFORMATION,currentRequest.get(AIR_ANY_OTHER_INFORMATION));
				params.put(AIR_REP_LOGGED_DATE, Timestamp.toCustomFormat(currentRequest.getLoggedDate(),"yyyy-MM-dd"));




				String reportName = AIR_REP_FILE_NAME ;

				HashMap<String,String> reportParamMap = new HashMap<String,String>() ;
				reportParamMap.put(REP_RID, rid  ) ;			
				String tbits_base_url = WebUtil.getNearestPath("") ; // PropertiesHandler.getProperty(TBitsPropEnum.KEY_NEAREST_INSTANCE)
				System.out.println( "tbits_base_url : " + tbits_base_url ) ;
				reportParamMap.put(REP_TBITS_BASE_URL_KEY, tbits_base_url );

				String format = "pdf";			
				File pdfFile = generateReport( reportName, params, reportParamMap, format ) ;
				////////////////////////////////////
				if( pdfFile == null ) 
				{
					return new RuleResult( false , "Cannot Generate the AIR File.", false ) ;
					// throw new TBitsException( "Cannot Generate the Correspondance File." ) ;
				}
				else
				{				

					String displayName="AIR-"+airReportId+".pdf";					
					int requestId = currentRequest.getRequestId() ;
					int actionId = currentRequest.getMaxActionId() ;
					String prefix = ba.getSystemPrefix() ;
					Uploader up = new Uploader( requestId, actionId, prefix ) ;
					AttachmentInfo atinfo = up.moveIntoRepository(pdfFile) ;
					atinfo.name=displayName;
					Collection<AttachmentInfo> attachments=(Collection<AttachmentInfo>)currentRequest.getObject(Field.ATTACHMENTS);
					attachments.add(atinfo);
					currentRequest.setAttachments(attachments);
					return new RuleResult( true , "AIRPreRule finished Successfully." , true ) ;
				}				
			}
			catch( Exception e ) 
			{
				e.printStackTrace();
				return new RuleResult( false , e.getMessage() , false ) ;
			}

		}
		else
			return new RuleResult( true, "Skipping : " + getName() + ", because the ba is not : " + AIR_PREFIX , true ) ;
	}



	public String getName() {
		return "AIRPreRule";
	}

	public double getSequence() {
		return 0;
	}

}
