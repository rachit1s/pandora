package corrGeneric.com.tbitsGlobal.server;

import static corrGeneric.com.tbitsGlobal.server.util.Utility.fdn;
import static corrGeneric.com.tbitsGlobal.server.util.Utility.tdn;
import static corrGeneric.com.tbitsGlobal.shared.CorrConst.ApplicableBas;
import static corrGeneric.com.tbitsGlobal.shared.CorrConst.InitFieldNameMap;
import static corrGeneric.com.tbitsGlobal.shared.CorrConst.InitOnBehalfMap;
import static corrGeneric.com.tbitsGlobal.shared.CorrConst.InitOptionsMap;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

import javax.mail.internet.MimeMultipart;

import com.tbitsGlobal.jaguar.client.serializables.RequestData;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.APIUtil;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Mail;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestDataType;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.mail.TBitsMailer;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;
import transbit.tbits.webapps.WebUtil;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.DisplayGroupClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;

import corrGeneric.com.tbitsGlobal.client.services.CorrDBService;
import corrGeneric.com.tbitsGlobal.server.interfaces.ICorrConstraintPlugin;
import corrGeneric.com.tbitsGlobal.server.managers.BaFieldManager;
import corrGeneric.com.tbitsGlobal.server.managers.CorrPluginManager;
import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.managers.OnBehalfManager;
import corrGeneric.com.tbitsGlobal.server.managers.ProtocolOptionsManager;
import corrGeneric.com.tbitsGlobal.server.managers.UserMapManager;
//import corrGeneric.com.tbitsGlobal.server.objects.OnBehalf;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.GenPDFUtil;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.CorrConst;
import corrGeneric.com.tbitsGlobal.shared.domain.BaFieldEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.OnBehalfEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.UserMapEntry;
import corrGeneric.com.tbitsGlobal.shared.key.BaFieldKey;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;
import corrGeneric.com.tbitsGlobal.shared.objects.OnBehalfClient;

public class CorrDBServiceImpl extends TbitsRemoteServiceServlet implements
		CorrDBService {

	public static TBitsLogger LOG = TBitsLogger.getLogger("corrGeneric.com.tbitsGlobal.server");
	public ArrayList<OnBehalfClient> getOnBehalfList(String sysPrefix,
			String userLogin) throws Exception 
	{
		try
		{
			ArrayList<OnBehalfEntry> map = OnBehalfManager.lookupOnBehalfList(sysPrefix, userLogin);
			ArrayList<OnBehalfClient> clientMap = new ArrayList<OnBehalfClient>(map.size());
			if( null == map )
				return null;
			
			HashMap<TypeTriplet,OnBehalfClient> utMap = new HashMap<TypeTriplet,OnBehalfClient>();
			for( OnBehalfEntry ob : map )
			{
				TypeTriplet triplet = new TypeTriplet(ob.getType1(),ob.getType2(),ob.getType3());
				OnBehalfClient onBehalfClient = utMap.get(triplet);
				if( null == onBehalfClient )
					onBehalfClient = new OnBehalfClient(ob.getSysPrefix(), ob.getUserLogin(), ob.getType1(), ob.getType2(), ob.getType3(), new ArrayList<String>());
				onBehalfClient.getOnBehalfUsers().add(ob.getOnBehalfUser());
				utMap.put(triplet ,onBehalfClient);
			}
			
			clientMap.addAll(utMap.values());
			if( clientMap.size() == 0 )
				return null;
			
			return clientMap;
		}
		catch(Exception te)
		{
			Utility.LOG.info(te);
			throw te;
		}
	}

	public HashMap<String,? extends Serializable> getApplicableBas() throws Exception
	{
		try 
		{
//			PropertyEntry appBas = PropertyManager.getProperty(GenericParams.CommaSeparatedListOfApplicableBa);
//			if( null == appBas || null == appBas.getValue() )
//				return null ;
			HashMap<String,Serializable> retMap = new HashMap<String,Serializable>();
			String appBas = null;
			try			
			{
				appBas = PropertiesHandler.getProperty(GenericParams.CommaSeparatedListOfApplicableBa);
			}
			catch(Exception iae)
			{
				LOG.error(TBitsLogger.getStackTrace(iae));
			}
			
			if( null == appBas || appBas.trim().equals(""))
				return retMap;
			
			ArrayList<String> baList = Utility.splitToArrayList(appBas.trim());
			
			retMap.put(ApplicableBas, baList);
			
//			HashMap<String, String> showConfirms = SpecialConfManager.getShowConfirmationMap();
//			HashMap<String,String> genCorrFields = SpecialConfManager.getGenerateCorrFieldNamesMap();
//			retMap.put(GenerateCorrFieldNames, genCorrFields);
//			retMap.put(ShowConfirmation, showConfirms);
			
			return retMap;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
//			throw new Exception( e.getDescription() );
		}
	}

	public HashMap<String, Object> getInitializingParams(String sysPrefix,String userLogin) throws Exception 
	{
		try
		{
			HashMap<String, FieldNameEntry> fnMap = getFieldNameMap(sysPrefix);
			HashMap<String, ProtocolOptionEntry> oMap = getOptionMap(sysPrefix);
			HashMap<String, HashMap<String, HashMap<String, Collection<String>>>> obMap =  getOnBehalfMap(sysPrefix, userLogin);//getOnBehalfList(sysPrefix, userLogin);
			
			HashMap<String,Object> initMap = new HashMap<String,Object>(3);
			initMap.put(InitFieldNameMap,fnMap);
			initMap.put(InitOptionsMap, oMap);
			initMap.put(InitOnBehalfMap, obMap);
			
			return initMap;
		}
		catch(TBitsException te)
		{
			Utility.LOG.error(te);
			throw new Exception(te.getDescription());
		}
		catch(Exception te)
		{
			Utility.LOG.error(te);
//			te.printStackTrace();
//			throw new Exception(te.getDescription());
			throw te;
		}
	}

	public HashMap<String, FieldNameEntry> getFieldNameMap(String sysPrefix) throws Exception 
	{
		try
		{
			Hashtable<String, FieldNameEntry> map = FieldNameManager.lookupFieldNameMap(sysPrefix);
			if( null == map )
				return null;
			
			HashMap<String,FieldNameEntry> fnMap = new HashMap<String,FieldNameEntry>(map); 
			return fnMap;
		}
		catch(Exception te)
		{
			Utility.LOG.error(te);
//			te.printStackTrace();
//			throw new Exception(te.getDescription());
			throw te;
		}
	}
	
	public HashMap<String,ProtocolOptionEntry> getOptionMap( String sysPrefix ) throws Exception
	{
		try
		{
			Hashtable<String, ProtocolOptionEntry> map = ProtocolOptionsManager.lookupAllProtocolEntry(sysPrefix);
			if( null == map )
				return null;
			HashMap<String,ProtocolOptionEntry> oMap = new HashMap<String,ProtocolOptionEntry>(map);
			return oMap;
		}
		catch(Exception te)
		{
			Utility.LOG.error(te);
//			te.printStackTrace();
//			throw new Exception(te.getDescription());
			throw te;
		}
	}

	public ArrayList<UserMapEntry> getUserMap(String sysPrefix, String userLogin) throws Exception 
	{
		try
		{
			return UserMapManager.lookupUserMap(sysPrefix, userLogin);
		}
		catch(Exception te)
		{
//			te.printStackTrace();
//			throw new Exception(te.getDescription());
			Utility.LOG.error(te);
			throw te;
		}
	}

	public HashMap<String, HashMap<String, HashMap<String, Collection<String>>>> getOnBehalfMap(
			String sysPrefix, String userLogin) throws Exception 
	{
		try
		{
			ArrayList<OnBehalfEntry> map = OnBehalfManager.lookupOnBehalfList(sysPrefix, userLogin);
			if( null == map )
				return null;
			
			return Utility.getOnBehalfMap(map);
		}
		catch( Exception te)
		{
//			te.printStackTrace();
//			throw new Exception(te.getDescription());
			Utility.LOG.error(te);
			throw te;
		}
	}

	public String getPDFUrl(TbitsTreeRequestData ttrd) throws Exception 
	{
		CorrObject coob = null ;
		Connection con = null ;
		try 
		{
			con = DataSourcePool.getConnection();
			coob = new CorrObject(con, ttrd );
			
			FieldNameEntry genCorrFne = FieldNameManager.lookupFieldNameEntry(coob.getBa().getSystemPrefix(), GenericParams.GenerateCorrespondenceFieldName);
			FieldNameEntry corrNofne = FieldNameManager.lookupFieldNameEntry(coob.getBa().getSystemPrefix(), GenericParams.CorrespondenceNumberFieldName);
			if( genCorrFne == null || genCorrFne.getBaFieldName() == null || null == corrNofne || null == corrNofne.getBaFieldName())
			{
				LOG.info("No Mapping found for " + GenericParams.GenerateCorrespondenceFieldName + " or " + GenericParams.CorrespondenceNumberFieldName + " in the ba : " + coob.getBa().getSystemPrefix() + ". Hence cannot create the Corr File.");
				throw new Exception("No Mapping found for " + GenericParams.GenerateCorrespondenceFieldName + " or " + GenericParams.CorrespondenceNumberFieldName + " in the ba : " + coob.getBa().getSystemPrefix() + ". Hence cannot create the Corr File.");			
			}
			String value = coob.getAsString(genCorrFne.getBaFieldName());
			if ( value != null)
			{
				Hashtable<String,Object> params = new Hashtable<String,Object>();
				params.put(ICorrConstraintPlugin.CONNECTION, con);
				params.put(ICorrConstraintPlugin.CORROBJECT, coob);
				
				CorrPluginManager.getInstance().executeConstraints(params);
				// get the corr no. and set it.
				// create the complete url using the HttpRequest object.
				// and send.
				
				if( !value.equals( GenericParams.GenerateCorr_NoPdforCorrNumber ) && !value.equals(GenericParams.GenerateCorr_OnlyNumber)  )
				{
					File tempDir = Configuration.findPath("webapps/tmp");
					Utility.LOG.info("tempDir for generating pdf : " + tempDir);
					
					if( null == tempDir ) // this will be null in development mode.
						tempDir = new File("tmp") ; // this will work if you are in development mode
					
					Utility.LOG.info("finding another temp file tempDir : " + tempDir);
					
					File outFile = File.createTempFile("correspondence_preview", ".pdf", tempDir) ;
				
					File pdfFile = GenPDFUtil.generateAndGetFile(con,coob,outFile );
					if( pdfFile == null )
						throw new Exception("Exception occured while generating preview. Please try again.");
			
					String prot = (getRequest().getProtocol().toLowerCase().contains("https") ? "https" : "http" );
					String toreturn = WebUtil.getNearestPath(getRequest(), "/tmp/" + pdfFile.getName());  
//					prot +"://" + getRequest().getServerName() + ":"
//			        + getRequest().getServerPort()
//			        + getRequest().getContextPath() + "/tmp/" + pdfFile.getName();
					
					return toreturn ;
				}
				else
				{
					throw new Exception("You have selected " + tdn(coob.getBa(),genCorrFne.getBaFieldName(),GenericParams.GenerateCorr_NoPdforCorrNumber) + " or " + tdn(coob.getBa(),genCorrFne.getBaFieldName(),GenericParams.GenerateCorr_OnlyNumber));
				}
			}
			else
			{
				LOG.info("No value found for " + fdn(coob.getBa(),genCorrFne.getBaFieldName())  + ". Hence cannot create the Corr File.");
				throw new Exception("No value found for " + fdn(coob.getBa(),genCorrFne.getBaFieldName())  + ". Hence cannot create the Corr File.");			
			}
		}
		catch(TBitsException te)
		{
			Utility.LOG.error(te);
			throw new Exception(te.getDescription());
		}
		catch (DatabaseException e) 
		{			
			e.printStackTrace();
			throw new Exception("Database exception occured while creating the preview.");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new Exception("Database exception occured while creating the preview.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new Exception("Cannot create temporary file.");
		}
		catch(CorrException ce)
		{
			ce.printStackTrace();
			Utility.LOG.error(ce);
			throw ce;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Utility.LOG.info("An unexpected exception occured on server-side. ErrorMessage : " + e.getMessage() );
			throw new Exception("An unexpected exception occured on server-side. ErrorMessage : " + e.getMessage() );
		}
		finally
		{
			try {
				if( null != con && con.isClosed() == false )
					con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public HashMap<String,Object> getViewRequestParams( String sysPrefix ) throws Exception 
	{
//		HashMap<String,Serializable> retMap = new HashMap<String, Serializable>(3);
		
		HashMap<String,Object> initMap = new HashMap<String,Object>(2);
		String appBas = null;
		try			
		{
			appBas = PropertiesHandler.getProperty(GenericParams.CommaSeparatedListOfApplicableBa);
		}
		catch(Exception iae)
		{
			LOG.error(TBitsLogger.getStackTrace(iae));
		}
		
//		if( null == appBas || appBas.trim().equals(""))
//			return retMap;
		
		try
		{
			HashMap<String, FieldNameEntry> fnMap = getFieldNameMap(sysPrefix);
			HashMap<String, ProtocolOptionEntry> oMap = getOptionMap(sysPrefix);
//			HashMap<String, HashMap<String, HashMap<String, Collection<String>>>> obMap =  getOnBehalfMap(sysPrefix, userLogin);//getOnBehalfList(sysPrefix, userLogin);
			

			initMap.put(InitFieldNameMap,fnMap);
			initMap.put(InitOptionsMap, oMap);
//			initMap.put(InitOnBehalfMap, obMap);
			
			return initMap;
		}
		catch(TBitsException te)
		{
			Utility.LOG.error(te);
			throw new Exception(te.getDescription());
		}
		catch(Exception te)
		{
//			te.printStackTrace();
//			throw new Exception(te.getDescription());
			Utility.LOG.error(te);
			throw te;
		}
		
//		ArrayList<ProtocolOptionEntry> tapp = SpecialConfManager.getTransferToProtocolOptionEntries();
//		HashMap<String, String> sapp = SpecialConfManager.getSendMeEmailApplicableBas();
//		HashMap<String,String> statmap = SpecialConfManager.getStatusFieldMapping();
		
//		retMap.put(CorrConst.SendMeEmailBas, sapp);
//		retMap.put(CorrConst.StatusFieldNames, statmap);
//		retMap.put(CorrConst.TransferToOptions, tapp);
//		return retMap;
	}

	public HashMap<String,? extends Serializable > getRequestDataForTransferRequest(String loginUser,
			int fromSysid, int requestId, TbitsTreeRequestData fromTtrd, String transferType,
			String toSysprefix) throws Exception 
	{
		try
		{
			User user = User.lookupByUserLogin(loginUser);
			
			if( null == user )
				throw new Exception("Illegal User with login : " + loginUser);
			
			BusinessArea toBA = BusinessArea.lookupBySystemPrefix(toSysprefix);
			if( null == toBA )
				throw new Exception("Illegal BusinessArea with sysPrefix : " + toSysprefix );
			
			BusinessArea fromBA = BusinessArea.lookupBySystemId(fromSysid);
			if( null == fromBA )
				throw new Exception("Illegal BusinessArea with sysId : " + fromSysid );

			BaFieldKey bfk = new BaFieldKey(fromBA.getSystemPrefix(), toBA.getSystemPrefix());
			
			Hashtable<String, BaFieldEntry> map = BaFieldManager.lookupBaFieldMap(bfk);
	
			ArrayList<BAField> fields = GWTServiceHelper.getFields(toBA, user);
			ArrayList<DisplayGroupClient> displayGroups = GWTServiceHelper.getDisplayGroups(toBA);
			if( null == fields || null == displayGroups )
				throw new Exception("Cannot find Fields or DisplayGroups for BusinessArea with sysPrefix : " + toBA.getSystemPrefix());
			
			Integer relatedRequestId = null;
			if( transferType.equals(GenericParams.ProtTransferTo_WithUpdate))
			{
				String relReqs = fromTtrd.getAsString(Field.RELATED_REQUESTS);
				if( null != relReqs && !relReqs.trim().equals(""))
				{
					Collection<RequestDataType> rr = APIUtil.getRequestCollection(relReqs);
					if( null != rr )
					{
						for(RequestDataType rdt : rr )
						{
							if( rdt.getSysId() == toBA.getSystemId() )
							{
								relatedRequestId = rdt.getRequestId() ;
								break;
							}
						}
					}
				}
			}
			
			TbitsTreeRequestData ttrd = null;
			if( null != relatedRequestId )
			{
				ttrd = GWTServiceHelper.getDataByRequestId(user, toBA, relatedRequestId);
				
				if( null == ttrd )
					throw new Exception("Cannot find data for request " + toBA.getSystemPrefix() + "#" + relatedRequestId);
			}
			else
			{
				ttrd = new TbitsTreeRequestData();
				ttrd.set(Field.REQUEST, 0);
				ttrd.set(Field.BUSINESS_AREA, toBA.getSystemId());
			}
			
			ArrayList<String> prefillFields = null;
			if( null != map )
			{
				prefillFields = new ArrayList<String>(map.size());
				for( String fieldName : map.keySet() )
				{
					POJO pojo = fromTtrd.getAsPOJO(fieldName);
					if( null != pojo )
					{
						BaFieldEntry bae = map.get(fieldName);
						if( bae != null && null != bae.getToFieldName())
						{
							ttrd.set(bae.getToFieldName(), pojo);
							prefillFields.add(bae.getToFieldName());
						}
					}
				}
			}
	
			// set from request in the related requests
			boolean alreadyContains = false;
			
			String fromRelatedRequest = ttrd.getAsString(Field.RELATED_REQUESTS);
			if( null != fromRelatedRequest && !fromRelatedRequest.trim().equals("") )
			{
				Collection<RequestDataType> rr = APIUtil.getRequestCollection(fromRelatedRequest);
				if( null != rr )
				{
					for( RequestDataType rdt : rr )
					{
						if( rdt.getSysId() == fromBA.getSystemId() && rdt.getRequestId() == requestId && rdt.getActionid() == fromTtrd.getMaxActionId())
						{
							alreadyContains = true;
						}
					}
				}
			}
			
			if( alreadyContains == false )
			{			
				if( fromRelatedRequest == null )
					fromRelatedRequest = "" ;
				
				ArrayList<String> relStrings =  new ArrayList<String>();
				
				if( !fromRelatedRequest.trim().equals(""))
					relStrings = Utility.splitToArrayList(fromRelatedRequest, ",");
				
				 relStrings.add(fromBA.getSystemPrefix() + "#" + requestId + "#" + fromTtrd.getMaxActionId());
				 
				 fromRelatedRequest = Utility.getStringWithSeparator(relStrings, ",");
				 
				ttrd.set(Field.RELATED_REQUESTS, fromRelatedRequest	);
			}
			
			RequestData rd = new RequestData(toBA.getSystemPrefix(), ttrd.getRequestId(), ttrd, displayGroups, fields) ;
			HashMap<String,Serializable> returnData = new HashMap<String,Serializable>();
			returnData.put(CorrConst.REQUEST_DATA, rd);
			returnData.put(CorrConst.PREFILL_FIELDS, prefillFields);
			return returnData;
		}
		catch(TBitsException te)
		{
			Utility.LOG.error(te);
			throw new Exception(te.getDescription());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	public void sendMeEmail(UserClient userClient,String sysPrefix, int reqId) throws Exception 
	{
		try
		{
			if( userClient == null )
				throw new Exception("Illegal User : null");
			
			User user = User.lookupByUserId(userClient.getUserId());
			
			if( null == user )
				throw new Exception("Cannot find user : " + userClient.getUserLogin());
			
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			if( null == ba )
				throw new Exception("Cannot find BusinessArea with sysPrefix : " + sysPrefix );
			
			Request request = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), reqId);		
			if( null == request )
				throw new Exception("Cannot find request with Id=" + reqId + " in BusinessArea with sysPrefix : " + sysPrefix);
			
			TBitsMailer tm = new TBitsMailer(request);
			Hashtable<String,Integer> permissions = RolePermission.getPermissionsBySystemIdAndRequestIdAndUserId(request.getSystemId(), request.getRequestId(),  user.getUserId());
			
			MimeMultipart mailContent = tm.generateMailForUser(user, tm.getRequest(), tm.getActionList(), tm.getActionFileHash(), permissions);
			
			String subject = ba.getSystemPrefix() + "#" + request.getRequestId() + ": " + request.getSubject();
    		
            String fromEmail =  "\""+user.getDisplayName()+"\""+"<"+ba.getEmail()+">";
            String toEmail = "\"" + user.getDisplayName() + "\"<" + user.getEmail() +  ">" ;
            Hashtable<String, String> extHdrs = new Hashtable<String,String>() ;
            extHdrs.put(TBitsConstants.X_AUTOREPLY, "yes");

            try
            {
            	Mail.sendHtmlAndAttachments(toEmail, fromEmail, subject,toEmail, 1 /*severityflag = high*/,"", extHdrs, mailContent);
            }
            catch(Exception e)
            {
            	Utility.LOG.error(TBitsLogger.getStackTrace(e));
            	throw new Exception("Mail sending failed because of external factors.\nPlease contact your Administrator.");
            }   	
		}
		catch(TBitsException te)
		{
			Utility.LOG.error(te);
			throw new Exception(te.getDescription());
		}
		catch(Exception e)
		{
			Utility.LOG.error(TBitsLogger.getStackTrace(e));
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public boolean isPreviewPdfEnable(String currentBa) throws Exception {
		
		String notApplicableList=PropertiesHandler.getProperty("disable_preview_pdf");
		if(notApplicableList != null)
		{
		String[] notAppBas=notApplicableList.split(",");
		for(String ba:notAppBas)
		{
			if(ba.equalsIgnoreCase(currentBa))
				return false;
		}
		}
		return true;
	}
}

class TypeTriplet
{
	String type1;
	String type2;
	String type3;
	public String getType1() {
		return type1;
	}
	public void setType1(String type1) {
		this.type1 = type1;
	}
	public String getType2() {
		return type2;
	}
	public void setType2(String type2) {
		this.type2 = type2;
	}
	public String getType3() {
		return type3;
	}
	public void setType3(String type3) {
		this.type3 = type3;
	}
	@Override
	public String toString() {
		return "TypeTriplet [type1=" + type1 + ", type2=" + type2 + ", type3="
				+ type3 + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type1 == null) ? 0 : type1.hashCode());
		result = prime * result + ((type2 == null) ? 0 : type2.hashCode());
		result = prime * result + ((type3 == null) ? 0 : type3.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypeTriplet other = (TypeTriplet) obj;
		if (type1 == null) {
			if (other.type1 != null)
				return false;
		} else if (!type1.equals(other.type1))
			return false;
		if (type2 == null) {
			if (other.type2 != null)
				return false;
		} else if (!type2.equals(other.type2))
			return false;
		if (type3 == null) {
			if (other.type3 != null)
				return false;
		} else if (!type3.equals(other.type3))
			return false;
		return true;
	}
	public TypeTriplet(String type1, String type2, String type3) {
		super();
		this.type1 = type1;
		this.type2 = type2;
		this.type3 = type3;
	}
}
