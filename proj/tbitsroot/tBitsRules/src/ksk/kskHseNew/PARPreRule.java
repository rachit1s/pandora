package kskHseNew;

import static kskHseNew.HSEConstants.PAR_PREFIX;
import static kskHseNew.HSEConstants.REP_RID;
import static kskHseNew.HSEConstants.REP_TBITS_BASE_URL_KEY;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
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
				params.put("request_object",currentRequest);
				params.put("report_id",parReportId);

				String reportName = "ksk_hse_Preliminary_Accident.rptdesign" ;

				HashMap<String,Object> reportParamMap = new HashMap<String,Object>() ;
				String rid = Integer.toString(currentRequest.getRequestId()) ;
				reportParamMap.put(REP_RID, rid  ) ;			
				String tbits_base_url = WebUtil.getNearestPath("") ; // PropertiesHandler.getProperty(TBitsPropEnum.KEY_NEAREST_INSTANCE)
				System.out.println( "tbits_base_url : " + tbits_base_url ) ;
				reportParamMap.put(REP_TBITS_BASE_URL_KEY, tbits_base_url );
				File dummyOutFile = null;
				File pdfFile = TBitsReportEngine.getInstance().generatePDFFile(reportName,params , reportParamMap,dummyOutFile );
				System.out.println(pdfFile.getAbsolutePath());
				////////////////////////////////////
				if( pdfFile == null ) 
				{
					return new RuleResult( false , "Cannot Generate the PAR File.", false ) ;

				}
				else
				{				

					String displayName="PAR-"+parReportId+".pdf";							
					int requestId = currentRequest.getRequestId() ;
					int actionId = currentRequest.getMaxActionId() ;
					String prefix = ba.getSystemPrefix() ;
					Uploader up = new Uploader( requestId, actionId, prefix ) ;
					AttachmentInfo atinfo = up.moveIntoRepository(pdfFile);
					//AttachmentInfo atinfo = up.copyIntoRepository(pdfFile);
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

	public static void main(String[] args) {
		PARPreRule parRule = new PARPreRule();
		BusinessArea ba;
		try {
			ba = BusinessArea.lookupBySystemPrefix(PAR_PREFIX);

			Request req = Request.lookupBySystemIdAndRequestId(ba.getSystemId(),1);
			User user = User.lookupAllByUserLogin("root");
			parRule.execute(DataSourcePool.getConnection(), ba,req,req,TBitsConstants.SOURCE_CMDLINE, user,true);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
