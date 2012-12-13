package hse.com.tbitsGlobal.server;


import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_AGE;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_BRIEF_DESCRIPTION;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_CONDITIONS;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_DATE;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_DESIGNATION;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_EQUIPMENT;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_LOCATION;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_NAME;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_OTHER_INFO;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_PREFIX;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REMIDY;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_AGE;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_CAUSE;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_CONTRACTOR;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_DATE;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_DESCRIPTION;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_DESIGNATION;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_EQUIPMENT;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_LOCATION;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_LOGGED_DATE;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_LOGGER_NAME;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_NAME;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_NATURE;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_OTHERINFORMATION;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_PREVENT;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_REPORTNO;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_SAFTYAPPLIANCES;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_SEX;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_REP_WITNESS;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_SAFETY_APPLIANCES;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_SEX;
import static hse.com.tbitsGlobal.shared.HSEConstants.PAR_WITNESS;
import static hse.com.tbitsGlobal.shared.HSEConstants.REP_RID;
import static hse.com.tbitsGlobal.shared.HSEConstants.REP_TBITS_BASE_URL_KEY;

import java.io.File;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.report.TBitsReportEngine;
import transbit.tbits.webapps.WebUtil;

public class PARPreRule implements IRule {


	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user, boolean isAddRequest) {

		if(ba.getSystemPrefix().equalsIgnoreCase(PAR_PREFIX) && isAddRequest){
			Hashtable<Object,Object> params = new Hashtable<Object,Object>();

			try{

				int parReportId=HSEUtils.getNextRepNo(connection,"PAR_REP_ID");
                
				Type inj=currentRequest.getRequestTypeId();
				Type cont=currentRequest.getCategoryId();
				
				
				
				params.put(PAR_REP_REPORTNO,"PAR-"+parReportId);
				params.put(PAR_REP_CONTRACTOR,cont.getDisplayName());
				params.put(PAR_REP_DATE,currentRequest.get(PAR_DATE));
				params.put(PAR_REP_LOCATION,currentRequest.get(PAR_LOCATION));
				params.put(PAR_REP_DESCRIPTION,currentRequest.get(PAR_BRIEF_DESCRIPTION));
				params.put(PAR_REP_NATURE,inj.getDisplayName());
				params.put(PAR_REP_EQUIPMENT,currentRequest.get(PAR_EQUIPMENT));
				params.put(PAR_REP_CAUSE,currentRequest.get(PAR_CONDITIONS));
				params.put(PAR_REP_SAFTYAPPLIANCES,currentRequest.get(PAR_SAFETY_APPLIANCES));
				params.put(PAR_REP_OTHERINFORMATION,currentRequest.get(PAR_OTHER_INFO));
				params.put(PAR_REP_WITNESS,currentRequest.get(PAR_WITNESS));
				params.put(PAR_REP_PREVENT,currentRequest.get(PAR_REMIDY));
				params.put(PAR_REP_NAME,currentRequest.get(PAR_NAME));
				params.put(PAR_REP_AGE,currentRequest.get(PAR_AGE));
				params.put(PAR_REP_SEX,currentRequest.get(PAR_SEX));
				params.put(PAR_REP_DESIGNATION,currentRequest.get(PAR_DESIGNATION));
				params.put(PAR_REP_LOGGED_DATE, Timestamp.toCustomFormat(currentRequest.getLoggedDate(),"yyyy-MM-dd"));
				params.put(PAR_REP_LOGGER_NAME,currentRequest.get(Field.LOGGER));


				String reportName = "ksk_hse_Preliminary_Accident.rptdesign" ;

				HashMap<String,Object> reportParamMap = new HashMap<String,Object>() ;
				String rid = Integer.toString(currentRequest.getRequestId()) ;
				reportParamMap.put(REP_RID, rid  ) ;			
				String tbits_base_url = WebUtil.getNearestPath("") ; // PropertiesHandler.getProperty(TBitsPropEnum.KEY_NEAREST_INSTANCE)
				System.out.println( "tbits_base_url : " + tbits_base_url ) ;
				reportParamMap.put(REP_TBITS_BASE_URL_KEY, tbits_base_url );

				String format = "pdf";			
				//File pdfFile = HSEUtils.generateReport( reportName, params, reportParamMap, format ) ;
				File dummyOutFile = null;
				File pdfFile = TBitsReportEngine.getInstance().generatePDFFile(reportName,params , reportParamMap,dummyOutFile );
				////////////////////////////////////
				if( pdfFile == null ) 
				{
					return new RuleResult( false , "Cannot Generate the PAR File.", false ) ;
					// throw new TBitsException( "Cannot Generate the Correspondance File." ) ;
				}
				else
				{				

					String displayName="PAR-"+parReportId+".pdf";							
					int requestId = currentRequest.getRequestId() ;
					int actionId = currentRequest.getMaxActionId() ;
					String prefix = ba.getSystemPrefix() ;
					Uploader up = new Uploader( requestId, actionId, prefix ) ;
					AttachmentInfo atinfo = up.moveIntoRepository(pdfFile) ;
					atinfo.name=displayName;
					Collection<AttachmentInfo> attachments=(Collection<AttachmentInfo>)currentRequest.getObject(Field.ATTACHMENTS);
					attachments.add(atinfo);
					currentRequest.setAttachments(attachments);
					return new RuleResult( true , "PARPreRule finished Successfully." , true ) ;
				}				
			}
			catch( Exception e ) 
			{
				e.printStackTrace();
				return new RuleResult( false , e.getMessage() , false ) ;
			}

		}
		else
			return new RuleResult( true, "Skipping : " + getName() + ", because the ba is not : " + PAR_PREFIX , true ) ;
	}



	public String getName() {
		return "PARPreRule";
	}

	public double getSequence() {
		return 0;
	}

}
