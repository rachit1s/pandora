package corrGeneric.com.tbitsGlobal.server.protocol;

import static corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager.lookupFieldNameMap;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.GenerationAgencyFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.LoggerFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.OnBehalfType1;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.OnBehalfType2;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.OnBehalfType3;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.OriginatorFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.RecepientAgencyFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.UserMapType1;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.UserMapType2;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOInt;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
//import transbit.tbits.exception.CorrException;
import corrGeneric.com.tbitsGlobal.server.managers.ProtocolOptionsManager;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class CorrObject 
{
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CorrObject [ba=" + ba + ", generationAgency="
				+ generationAgency + ", loginUser=" + loginUser
				+ ", onBehalfType1=" + onBehalfType1 + ", onBehalfType2="
				+ onBehalfType2 + ", onBehalfType3=" + onBehalfType3
				+ ", originator=" + originator + ", recepientAgency="
				+ recepientAgency + ", source=" + source + ", type=" + type
				+ ", userMapType1=" + userMapType1 + ", userMapType2="
				+ userMapType2 + ", userMapType3=" + userMapType3
				+ ", userMapUsers=" + userMapUsers + "]";
	}
	/**
	 * @param ba the ba to set
	 */
	protected void setBa(BusinessArea ba) {
		this.ba = ba;
	}
	/**
	 * @param onBehalfType1 the onBehalfType1 to set
	 */
	protected void setOnBehalfType1(Type onBehalfType1) {
		this.onBehalfType1 = onBehalfType1;
	}
	/**
	 * @param onBehalfType2 the onBehalfType2 to set
	 */
	protected void setOnBehalfType2(Type onBehalfType2) {
		this.onBehalfType2 = onBehalfType2;
	}
	/**
	 * @param onBehalfType3 the onBehalfType3 to set
	 */
	protected void setOnBehalfType3(Type onBehalfType3) {
		this.onBehalfType3 = onBehalfType3;
	}
	/**
	 * @param userMapType1 the userMapType1 to set
	 */
	protected void setUserMapType1(Type userMapType1) {
		this.userMapType1 = userMapType1;
	}
	/**
	 * @param userMapType2 the userMapType2 to set
	 */
	protected void setUserMapType2(Type userMapType2) {
		this.userMapType2 = userMapType2;
	}
	/**
	 * @param userMapType3 the userMapType3 to set
	 */
	protected void setUserMapType3(Type userMapType3) {
		this.userMapType3 = userMapType3;
	}
	/**
	 * @param loginUser the loginUser to set
	 */
	protected void setLoginUser(User loginUser) {
		this.loginUser = loginUser;
	}
	/**
	 * @param type the type to set
	 */
	protected void setType(int type) {
		this.type = type;
	}
	/**
	 * @param source the source to set
	 */
	protected void setSource(int source) {
		this.source = source;
	}
//	public CorrObject(BusinessArea ba, Type onBehalfType1, Type onBehalfType2,
//			Type onBehalfType3, Type userMapType1, Type userMapType2,
//			Type userMapType3, User loginUser, ArrayList<User> userMapUsers,int type, int source) throws CorrException {
//		super();
//		this.setBa(ba);
//		this.setOnBehalfType1(onBehalfType1);
//		this.setOnBehalfType2(onBehalfType2);
//		this.setOnBehalfType3(onBehalfType3);
//		this.setUserMapType1(userMapType1);
//		this.setUserMapType2(userMapType2);
//		this.setUserMapType3(userMapType3);
//		this.setLoginUser(loginUser);
//		this.setType(type);
//		this.setSource(source);
//		this.setUserMapUsers(userMapUsers);
//		
//		Hashtable<String,FieldNameEntry> fieldNameMap = FieldNameManager.lookupFieldNameMap(ba.getSystemPrefix());
//		if( null == fieldNameMap )
//			throw new CorrException("Cannot find FieldNameMap for the ba with sysPrefix : " + ba.getSystemPrefix());
//		
//		this.setFieldNameMap(fieldNameMap);
//		Hashtable<String, ProtocolOptionEntry> optionMap = ProtocolOptionsManager.lookupAllProtocolEntry(ba.getSystemPrefix());
//		this.setOptionMap(optionMap);
//	}
	/**
	 * @return the fieldNameMap
	 */
	public Hashtable<String, FieldNameEntry> getFieldNameMap() {
		return fieldNameMap;
	}
	/**
	 * @param fieldNameMap the fieldNameMap to set
	 */
	private void setFieldNameMap(Hashtable<String, FieldNameEntry> fieldNameMap) {
		this.fieldNameMap = fieldNameMap;
	}
	/**
	 * @param userMapUsers the userMapUsers to set
	 */
	private void setUserMapUsers(ArrayList<User> userMapUsers) {
		this.userMapUsers = userMapUsers;
	}
	/**
	 * @return the userMapUsers
	 */
	public ArrayList<User> getUserMapUsers() {
		return userMapUsers;
	}
	public static int TypeAddRequest = 1;
	public static int TypeUpdateRequest = 2;
	public static int SourcePreview = 1;
	public static int SourceReal = 2;
	
	private BusinessArea ba ;
	private Type onBehalfType1 ;
	private Type onBehalfType2 ;
	private Type onBehalfType3 ;

	private Type userMapType1;
	private Type userMapType2;
	private Type userMapType3;
	
	private Type reportType1;
	private Type reportType2;
	private Type reportType3;
	/**
	 * @param reportType1 the reportType1 to set
	 */
	protected void setReportType1(Type reportType1) {
		this.reportType1 = reportType1;
	}
	/**
	 * @param reportType2 the reportType2 to set
	 */
	protected void setReportType2(Type reportType2) {
		this.reportType2 = reportType2;
	}
	/**
	 * @param reportType3 the reportType3 to set
	 */
	protected void setReportType3(Type reportType3) {
		this.reportType3 = reportType3;
	}
	/**
	 * @param reportType4 the reportType4 to set
	 */
	protected void setReportType4(Type reportType4) {
		this.reportType4 = reportType4;
	}
	/**
	 * @param reportType5 the reportType5 to set
	 */
	protected void setReportType5(Type reportType5) {
		this.reportType5 = reportType5;
	}
	private Type reportType4;
	private Type reportType5;
	
	/**
	 * @return the reportType1
	 */
	public Type getReportType1() {
		return reportType1;
	}
	/**
	 * @return the reportType2
	 */
	public Type getReportType2() {
		return reportType2;
	}
	/**
	 * @return the reportType3
	 */
	public Type getReportType3() {
		return reportType3;
	}
	/**
	 * @return the reportType4
	 */
	public Type getReportType4() {
		return reportType4;
	}
	/**
	 * @return the reportType5
	 */
	public Type getReportType5() {
		return reportType5;
	}
	private Type originator;	
	private Type generationAgency;
	private Type recepientAgency;
	private Type generate;
	
	
	/**
	 * @return the generate
	 */
	public Type getGenerate() {
		return generate;
	}
	/**
	 * @param generate the generate to set
	 */
	private void setGenerate(Type generate) {
		this.generate = generate;
	}
	private User loginUser ;
	private ArrayList<User> userMapUsers;
	private Request request ;
	private Request prevRequest;
	
	private TbitsTreeRequestData ttrd;
	/**
	 * @param ttrdd the ttrdd to set
	 */
	private void setTtrd(TbitsTreeRequestData ttrd) {
		this.ttrd = ttrd;
	}
	/**
	 * @return the ttrdd
	 */
	public TbitsTreeRequestData getTtrd() {
		return ttrd;
	}
	private int type;
	private int source;
	
	public Hashtable<String,FieldNameEntry> fieldNameMap = null;
	public Hashtable<String,ProtocolOptionEntry> optionMap = null;
	private Type disableProtocol = null;
	/**
	 * @return the disableProtocol
	 */
	public Type getDisableProtocol() {
		return disableProtocol;
	}
	/**
	 * @param disableProtocol the disableProtocol to set
	 */
	private void setDisableProtocol(Type disableProtocol) {
		this.disableProtocol = disableProtocol;
	}
	/**
	 * @return the originator
	 */
	public Type getOriginator() {
		return originator;
	}
	/**
	 * @return the generationAgency
	 */
	public Type getGenerationAgency() {
		return generationAgency;
	}
	/**
	 * @return the recepientAgency
	 */
	public Type getRecepientAgency() {
		return recepientAgency;
	}

	/**
	 * @return the optionMap
	 */
	public Hashtable<String, ProtocolOptionEntry> getOptionMap() {
		return optionMap;
	}
	/**
	 * @param optionMap the optionMap to set
	 */
	private void setOptionMap(Hashtable<String, ProtocolOptionEntry> optionMap) {
		this.optionMap = optionMap;
	}
	
	public CorrObject(Request request, Request prevRequest) throws CorrException
	{
		try
		{
			if( null == request )
				throw new IllegalArgumentException("request supplied was null.");
			
			BusinessArea ba = null;
			Type onBehalfType1 = null;
			Type onBehalfType2 = null;
			Type onBehalfType3 = null;
			Type userMapType1 = null;
			Type userMapType2 = null;
			Type userMapType3 = null;
			User loginUser = null ;
			ArrayList<User> userMapUsers = null; 
			int type;
			int source;
			
			int sysId = request.getSystemId();
			ba = BusinessArea.lookupBySystemId(sysId);
			this.setBa(ba);
			
			loginUser = User.lookupByUserId(request.getUserId());
			this.setLoginUser(loginUser);
			
			source = SourceReal;
			this.setSource(source);
			
			type = ( prevRequest == null ? TypeAddRequest : TypeUpdateRequest );
			this.setType(type);
			
			Hashtable<String,FieldNameEntry> fm = lookupFieldNameMap(ba.getSystemPrefix());
			if( null == fm )
				throw new CorrException("Cannot find FieldNameMaping for ba with sysPrefix : " + ba.getSystemPrefix());
			
			FieldNameEntry obt1 = fm.get(OnBehalfType1);
			if( null != obt1 )
				onBehalfType1 = (Type) request.getObject(obt1.getBaFieldName());

			FieldNameEntry obt2 = fm.get(OnBehalfType2);
			if( null != obt2 )
				onBehalfType2 = (Type) request.getObject(obt2.getBaFieldName());
			
			FieldNameEntry obt3 = fm.get(OnBehalfType3);
			if( null != obt3 )
				onBehalfType3 = (Type) request.getObject(obt3.getBaFieldName());
			
			FieldNameEntry umt1 = fm.get(UserMapType1);
			if( null != umt1 )
				userMapType1 = (Type) request.getObject(umt1.getBaFieldName());
			
			FieldNameEntry umt2 = fm.get(UserMapType2);
			if( null != umt2 )
				userMapType2 = (Type) request.getObject(umt2.getBaFieldName());
			
			FieldNameEntry umt3 = fm.get(UserMapType3);
			if( null != umt3 )
				userMapType3 = (Type) request.getObject(umt3.getBaFieldName());
			
			FieldNameEntry umutf = fm.get(LoggerFieldName);
			if( null != umutf )
				userMapUsers = Utility.getUsersFromRequestUser( (Collection<RequestUser>)request.getObject(umutf.getBaFieldName()));
			
			FieldNameEntry orig = fm.get(OriginatorFieldName);
			if( null != orig )
				this.setOriginator((Type)request.getObject(orig.getBaFieldName()));
			
			FieldNameEntry genAgency = fm.get(GenerationAgencyFieldName);
			if( null != genAgency )
				this.setGenerationAgency((Type)request.getObject(genAgency.getBaFieldName()));
			
			FieldNameEntry recepientAgency = fm.get(RecepientAgencyFieldName);
			if( null != recepientAgency )
				this.setRecepientAgency((Type)request.getObject(recepientAgency.getBaFieldName()));
			
			FieldNameEntry reportType1 = fm.get(ReportType1);
			if( null != reportType1 && null != reportType1.getBaFieldName())
				this.setReportType1((Type)request.getObject(reportType1.getBaFieldName()));
			
			FieldNameEntry reportType2 = fm.get(ReportType2);
			if( null != reportType2 && null != reportType2.getBaFieldName())
				this.setReportType2((Type)request.getObject(reportType2.getBaFieldName()));
			
			FieldNameEntry reportType3 = fm.get(ReportType3);
			if( null != reportType3 && null != reportType3.getBaFieldName())
				this.setReportType3((Type)request.getObject(reportType3.getBaFieldName()));
			
			FieldNameEntry reportType4 = fm.get(ReportType4);
			if( null != reportType4 && null != reportType4.getBaFieldName())
				this.setReportType4((Type)request.getObject(reportType4.getBaFieldName()));
			
			FieldNameEntry reportType5 = fm.get(ReportType5);
			if( null != reportType5 && null != reportType5.getBaFieldName())
				this.setReportType5((Type)request.getObject(reportType5.getBaFieldName()));
			
			FieldNameEntry disProtFne = fm.get(DisableProtocolFieldName);
			if( null != disProtFne && null != disProtFne.getBaFieldName())
				this.setDisableProtocol((Type)request.getObject(disProtFne.getBaFieldName()));
			
			FieldNameEntry generateFne = fm.get(GenerateCorrespondenceFieldName);
			if( null != generateFne && null != generateFne.getBaFieldName())
				this.setGenerate((Type)request.getObject(generateFne.getBaFieldName()));
			
			this.setOnBehalfType1(onBehalfType1);
			this.setOnBehalfType2(onBehalfType2);
			this.setOnBehalfType3(onBehalfType3);
			this.setUserMapType1(userMapType1);
			this.setUserMapType2(userMapType2);
			this.setUserMapType3(userMapType3);
			this.setUserMapUsers(userMapUsers);
			
			this.setFieldNameMap(fm);
			Hashtable<String, ProtocolOptionEntry> optionMap = ProtocolOptionsManager.lookupAllProtocolEntry(ba.getSystemPrefix());
			this.setOptionMap(optionMap);
			this.setRequest(request);
			this.setPrevRequest(prevRequest);
			
		}
		catch(CorrException e)
		{
			Utility.LOG.error(e);
			throw e;
		}
		catch(Exception e)
		{
			throw new CorrException("Exception " + e.getMessage());
		}
	}
	
	public CorrObject(Connection con, TbitsTreeRequestData ttrd) throws CorrException
	{
		try
		{
			if( null == ttrd )
				throw new IllegalArgumentException( "Request Data supplied was null.");
			
			this.setTtrd(ttrd);
			BusinessArea ba = null;
			Type onBehalfType1 = null;
			Type onBehalfType2 = null;
			Type onBehalfType3 = null;
			Type userMapType1 = null;
			Type userMapType2 = null;
			Type userMapType3 = null;
			User loginUser = null ;
			ArrayList<User> userMapUsers = null; 
			int type;
			int source;
			
			POJO si = ttrd.getAsPOJO(Field.BUSINESS_AREA);
			POJOInt sysId = (POJOInt) si ;
			if( null == sysId || null == sysId.getValue() )
				throw new CorrException("Illegal Business Area with sys_id = " + sysId);
			
			ba = BusinessArea.lookupBySystemId(sysId.getValue());
			if( null == ba )
				throw new CorrException("Cannot find business areas with sys_id = " + sysId.getValue());
			
			this.setBa(ba);
			
			POJOInt userInt = (POJOInt) ttrd.getAsPOJO(Field.USER);
			if( null == userInt || null == userInt.getValue() )
				throw new CorrException("Illegal Login User value with user_id = " + userInt);

			loginUser = User.lookupByUserId(userInt.getValue());
			if( null == loginUser )
				throw new CorrException("Cannot find user with user_id = " + userInt.getValue());
			
			this.setLoginUser(loginUser);
			
			source = SourcePreview;
			this.setSource(source);
			
			POJOInt requestId = (POJOInt) ttrd.getAsPOJO(Field.REQUEST);
			if( null == requestId || requestId.getValue() == null )
				throw new CorrException("Illegal value in request_id : " + requestId);
			
			if( 0 != requestId.getValue() )
			{
				prevRequest = Request.lookupBySystemIdAndRequestId(con, ba.getSystemId(), requestId.getValue());
				type = TypeUpdateRequest ;
			}
			else
			{
				type = TypeAddRequest ;
			}			
			this.setType(type);
			
			Hashtable<String,FieldNameEntry> fm = lookupFieldNameMap(ba.getSystemPrefix());
			if( null == fm )
				throw new CorrException("Cannot find FieldNameMaping for ba with sysPrefix : " + ba.getSystemPrefix());
			
			FieldNameEntry obt1 = fm.get(OnBehalfType1);
			if( null != obt1 )
			{
				String typeName = ttrd.getAsString(obt1.getBaFieldName());
				if( null != typeName )
				{
					onBehalfType1 = Type.lookupAllBySystemIdAndFieldNameAndTypeName(this.getBa().getSystemId(), obt1.getBaFieldName(), typeName);
				}
			}

			FieldNameEntry obt2 = fm.get(OnBehalfType2);
			if( null != obt2 )
			{
				String typeName = ttrd.getAsString(obt2.getBaFieldName());
				if( null != typeName )
				{
					onBehalfType2 = Type.lookupBySystemIdAndFieldNameAndTypeName(this.getBa().getSystemId(), obt2.getBaFieldName(), typeName);
				}
			}
			
			FieldNameEntry obt3 = fm.get(OnBehalfType3);
			if( null != obt3 )
			{
				String typeName = ttrd.getAsString(obt3.getBaFieldName());
				if( null != typeName )
				{
					onBehalfType3 = Type.lookupBySystemIdAndFieldNameAndTypeName(this.getBa().getSystemId(), obt3.getBaFieldName(), typeName);
				}
			}
			
			FieldNameEntry umt1 = fm.get(UserMapType1);
			if( null != umt1 )
			{
				String typeName = ttrd.getAsString(umt1.getBaFieldName());
				if( null != typeName )
				{
					userMapType1 = Type.lookupBySystemIdAndFieldNameAndTypeName(this.getBa().getSystemId(), umt1.getBaFieldName(), typeName);
				}
			}
			
			FieldNameEntry umt2 = fm.get(UserMapType2);
			if( null != umt2 )
			{
				String typeName = ttrd.getAsString(umt2.getBaFieldName());
				if( null != typeName )
				{
					userMapType2 = Type.lookupBySystemIdAndFieldNameAndTypeName(this.getBa().getSystemId(), umt2.getBaFieldName(), typeName);
				}
			}
			
			FieldNameEntry umt3 = fm.get(UserMapType3);
			if( null != umt3 )
			{
				String typeName = ttrd.getAsString(umt3.getBaFieldName());
				if( null != typeName )
				{
					userMapType3 = Type.lookupBySystemIdAndFieldNameAndTypeName(this.getBa().getSystemId(), umt3.getBaFieldName(), typeName);
				}
			}
			
			FieldNameEntry umutf = fm.get(LoggerFieldName);
			if( null != umutf )
			{
				userMapUsers = Utility.toUsers(ttrd.getAsString(umutf.getBaFieldName()));
			}
			
			FieldNameEntry orig = fm.get(OriginatorFieldName);
			if( null != orig )
			{
				String typeName = ttrd.getAsString(orig.getBaFieldName());
				if( null != typeName )
				{
					this.setOriginator(Type.lookupBySystemIdAndFieldNameAndTypeName(this.getBa().getSystemId(), orig.getBaFieldName(), typeName));
				}
			}
			
			FieldNameEntry genAgency = fm.get(GenerationAgencyFieldName);
			if( null != genAgency )
			{
				String typeName = ttrd.getAsString(genAgency.getBaFieldName());
				if( null != typeName )
				{
					this.setGenerationAgency(Type.lookupBySystemIdAndFieldNameAndTypeName(this.getBa().getSystemId(), genAgency.getBaFieldName(), typeName));
				}
			}
			
			FieldNameEntry recepientAgency = fm.get(RecepientAgencyFieldName);
			if( null != recepientAgency )
			{
				String typeName = ttrd.getAsString(recepientAgency.getBaFieldName());
				if( null != typeName )
				{
					this.setRecepientAgency(Type.lookupBySystemIdAndFieldNameAndTypeName(this.getBa().getSystemId(), recepientAgency.getBaFieldName(), typeName));
				}
			}
			
			FieldNameEntry reportType1 = fm.get(ReportType1);
			if( null != reportType1 && null != reportType1.getBaFieldName())
			{
				String typeName = ttrd.getAsString(reportType1.getBaFieldName());
				if( null != typeName )
				{
					this.setReportType1(Type.lookupBySystemIdAndFieldNameAndTypeName(this.getBa().getSystemId(), reportType1.getBaFieldName(), typeName));
				}
			}
			
			FieldNameEntry reportType2 = fm.get(ReportType2);
			if( null != reportType2 && null != reportType2.getBaFieldName())
			{
				String typeName = ttrd.getAsString(reportType2.getBaFieldName());
				if( null != typeName )
				{
					this.setReportType2(Type.lookupBySystemIdAndFieldNameAndTypeName(this.getBa().getSystemId(), reportType2.getBaFieldName(), typeName));
				}
			}
			
			FieldNameEntry reportType3 = fm.get(ReportType3);
			if( null != reportType3 && null != reportType3.getBaFieldName())
			{
				String typeName = ttrd.getAsString(reportType3.getBaFieldName());
				if( null != typeName )
				{
					this.setReportType3(Type.lookupBySystemIdAndFieldNameAndTypeName(this.getBa().getSystemId(), reportType3.getBaFieldName(), typeName));
				}
			}
			
			FieldNameEntry reportType4 = fm.get(ReportType4);
			if( null != reportType4 && null != reportType4.getBaFieldName())
			{
				String typeName = ttrd.getAsString(reportType4.getBaFieldName());
				if( null != typeName )
				{
					this.setReportType4(Type.lookupBySystemIdAndFieldNameAndTypeName(this.getBa().getSystemId(), reportType4.getBaFieldName(), typeName));
				}
			}
			
			FieldNameEntry reportType5 = fm.get(ReportType5);
			if( null != reportType5 && null != reportType5.getBaFieldName())
			{

				String typeName = ttrd.getAsString(reportType5.getBaFieldName());
				if( null != typeName )
				{
					this.setReportType5(Type.lookupBySystemIdAndFieldNameAndTypeName(this.getBa().getSystemId(), reportType5.getBaFieldName(), typeName));
				}
			}
			
			FieldNameEntry disProtFne = fm.get(DisableProtocolFieldName);
			if( null != disProtFne && null != disProtFne.getBaFieldName())
			{
				String disProtString = ttrd.getAsString(disProtFne.getBaFieldName());
				if( null != disProtString )
				{
					this.setDisableProtocol(Type.lookupBySystemIdAndFieldNameAndTypeName(this.getBa().getSystemId(), disProtFne.getBaFieldName(), disProtString));
				}
			}
			
			FieldNameEntry generateFne = fm.get(GenerateCorrespondenceFieldName);
			if( null != generateFne && null != generateFne.getBaFieldName())
			{
				String generateStr = ttrd.getAsString(generateFne.getBaFieldName());
				if( null != generateStr )
				{
					this.setGenerate(Type.lookupBySystemIdAndFieldNameAndTypeName(this.getBa().getSystemId(), generateFne.getBaFieldName(), generateStr));
				}
			}
			
			this.setOnBehalfType1(onBehalfType1);
			this.setOnBehalfType2(onBehalfType2);
			this.setOnBehalfType3(onBehalfType3);
			this.setUserMapType1(userMapType1);
			this.setUserMapType2(userMapType2);
			this.setUserMapType3(userMapType3);
			this.setUserMapUsers(userMapUsers);
			
			this.setFieldNameMap(fm);
			Hashtable<String, ProtocolOptionEntry> optionMap = ProtocolOptionsManager.lookupAllProtocolEntry(ba.getSystemPrefix());
			this.setOptionMap(optionMap);
//			this.setPrevRequest(prevRequest);
		}		
		catch( CorrException te)
		{
			Utility.LOG.error(te);
			throw te;
		}
		catch(Exception e)
		{
			Utility.LOG.error(e);
			throw new CorrException("Exception " + e.getMessage());
		}
	}
	/**
	 * @param originator the originator to set
	 */
	private void setOriginator(Type originator) {
		this.originator = originator;
	}
	/**
	 * @param generationAgency the generationAgency to set
	 */
	private void setGenerationAgency(Type generationAgency) {
		this.generationAgency = generationAgency;
	}
	/**
	 * @param recepientAgency the recepientAgency to set
	 */
	private void setRecepientAgency(Type recepientAgency) {
		this.recepientAgency = recepientAgency;
	}
	/**
	 * @param request the request to set
	 */
	private void setRequest(Request request) {
		this.request = request;
	}
	/**
	 * @param prevRequest the prevRequest to set
	 */
	private void setPrevRequest(Request prevRequest) {
		this.prevRequest = prevRequest;
	}
	/**
	 * @return the request
	 */
	public Request getRequest() {
		return request;
	}
	/**
	 * @return the prevRequest
	 */
	public Request getPrevRequest() {
		return prevRequest;
	}
	public String getAsString(String fieldName)
	{
		if( (this.getSource() == SourceReal) && ( this.request != null ) )
			return request.get(fieldName);
		else if( this.getSource() == SourcePreview && (this.getTtrd() != null) )
			return this.getTtrd().getAsString(fieldName); 
		
		return null;
	}
	
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return the source
	 */
	public int getSource() {
		return source;
	}

	//	private ArrayList<User> loggers ; 
	/**
	 * @return the ba
	 */
	public BusinessArea getBa() {
		return ba;
	}
	/**
	 * @return the onBehalfType1
	 */
	public Type getOnBehalfType1() {
		return onBehalfType1;
	}
	/**
	 * @return the onBehalfType2
	 */
	public Type getOnBehalfType2() {
		return onBehalfType2;
	}
	/**
	 * @return the onBehalfType3
	 */
	public Type getOnBehalfType3() {
		return onBehalfType3;
	}
	/**
	 * @return the userMapType1
	 */
	public Type getUserMapType1() {
		return userMapType1;
	}
	/**
	 * @return the userMapType2
	 */
	public Type getUserMapType2() {
		return userMapType2;
	}
	/**
	 * @return the userMapType3
	 */
	public Type getUserMapType3() {
		return userMapType3;
	}
	/**
	 * @return the loginUser
	 */
	public User getLoginUser() {
		return loginUser;
	}
}
