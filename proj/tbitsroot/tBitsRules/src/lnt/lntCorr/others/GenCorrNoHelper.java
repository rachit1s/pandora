package lntCorr.others;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Type;
import corrGeneric.com.tbitsGlobal.server.managers.ProtocolOptionsManager;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class GenCorrNoHelper 
{
	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");
	public static String getCorrNo(CorrObject co, Connection con) throws CorrException 
	{
		/*
		 * cases 
		 * 1 : generate both file and number
		 * 2 : generate only file with given number
		 * 3 : don't generate anything
		 * 4 : generate only number 
		 */
		String genCorr = co.getAsString(co.getFieldNameMap().get(GenericParams.GenerateCorrespondenceFieldName).getBaFieldName());
		FieldNameEntry cfn = co.getFieldNameMap().get(GenericParams.CorrespondenceNumberFieldName);
		String corrFieldName = null ;
		if( null != cfn )
		 corrFieldName = cfn.getBaFieldName() ;
		String corrNo = null;
		
		if( genCorr.equals(GenericParams.GenerateCorr_NoPdforCorrNumber))
			throw new CorrException("You have selected not to generate a correspondence file and correspondence number.");
			
		if( genCorr.equals(GenericParams.GenerateCorr_OnlyPdfWithSpecifiedNumber))
		{
			if( null == corrFieldName )
				throw new CorrException("You have specified to take the number from correspondence number field. But such a field is not configured for this ba : " + co.getBa().getSystemPrefix());
			corrNo = co.getAsString(corrFieldName);
			if( null == corrNo || corrNo.trim().equals(""))
				throw new CorrException("You have selected to generate correspondence file with given number but not provided the correspondence number.") ;
			else
			{
				corrNo = corrNo.trim();
				return validateCorrNumber(con,co,corrNo) ;							
			}
		}
		else //only no. / no. and pdf  
		{
			corrNo = co.getAsString(corrFieldName);
			if( null != corrNo && !corrNo.trim().equals(""))
			{
				return null ;// do not try to regenerate the corr. no. if already generated.
			}
			corrNo = null ;
			if( co.getSource() == CorrObject.SourcePreview )
				corrNo = getExpectedCorrNo(con,co) ;
			else
			{
				corrNo = getRealCorrNo(co,con) ;
				if(null == corrFieldName)
					throw new CorrException("You have chose to generate correspondence Number. But the correspondence number field is not configured for this BA : " + co.getBa().getSystemPrefix());
				if( null == corrNo )
					throw new CorrException("Excetion occured while generating correspondence number.");
				
			}
			
			if( null == corrNo )
				throw new CorrException("Cannot generate the correspondence number.") ;
			
			
			return corrNo ;
		}
	}
	
	private static String validateCorrNumber(Connection conn, CorrObject co, String corrNo) throws CorrException 
	{
		Field corrNoField = null;
		try {
			corrNoField = Field.lookupBySystemIdAndFieldName(co.getBa().getSystemId(),co.getFieldNameMap().get(GenericParams.CorrespondenceNumberFieldName).getBaFieldName());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new CorrException(e);
		}
		
		int lastInd = corrNo.lastIndexOf('-');
		if( lastInd == -1 || ( lastInd == corrNo.length() -1 ) ) // number does not contain '-' OR '-' is the last letter of the number ex : 'corrNo-'
		{
			return corrNo ;//throw new CorrException( corrNoField.getDisplayName() + " is not in format of prefix-runningNo.\n The expected number and format is : " + getExpectedCorrNo(conn, co));
		}
		
		String prefix = corrNo.substring(0, lastInd);
		String runningNumber = corrNo.substring(lastInd+1);
		
		String expectedPrefix = getCorrPrefix(co);
		expectedPrefix = expectedPrefix.substring(0, expectedPrefix.lastIndexOf('-'));
		if( !expectedPrefix.equals(prefix) )
			return corrNo ;
			//throw new CorrException("The provided number has prefix : " + prefix + " while the expected prefix according to the parameters is : " + expectedPrefix );
		
		// running number should be integer.
		int runningNo = 0;
		try
		{
			runningNo = Integer.parseInt(runningNumber);
		}
		catch(NumberFormatException nfe)
		{
			LOG.error("The running number : " + runningNumber + " of the " + corrNoField.getDisplayName() + " : " + corrNo + " was not integer. So aborting the rule.");
			throw new CorrException("The running number : " + runningNumber + " of the " + corrNoField.getDisplayName() + corrNo + " was not Integer.");
		}
		
		// no extra sanitization.
		String maxIdName = getCorrMaxIdName(co);
		int maxNumber = getMaxCorrNo(conn, maxIdName);
		if( runningNo > maxNumber + 1 )
		{
			LOG.info("The maximum running number for the correspondence prefix = '" + prefix + "' can be set to : " + (maxNumber +1) );
			throw new CorrException("The maximum running number for the correspondence prefix = '" + prefix + "' can be set to : " + (maxNumber + 1));
		}
		else
		{
			if( runningNo == maxNumber + 1 )
			{
				if( co.getSource() == CorrObject.SourceReal )
				{
					int incrNumber = incrAndGetCorrNo(conn, prefix);
					if( incrNumber != runningNo )
					{
						LOG.error("The max_id was " + maxNumber + " but when incremented to make it according to the given number of :" + runningNo + ", it returned " + incrNumber + " and this mismatch cannot be handled." );
						throw new CorrException("Race condition occurred while finding and incrementing the max number. Please try again for valid results.");
					}
					else
					{
						LOG.info("The increment of number to " + incrNumber + " was correct.");					
					}
				}
			}
			else
				LOG.info("The running number of : " + runningNo + " was less than or equal to : " + maxNumber + " for the prefix : " + prefix + " and is allowed.");
		}
		
		DecimalFormat df = new DecimalFormat("0000") ;
		String runNo = df.format(runningNo);
		
		return expectedPrefix + "-" + runNo;
	}

	public static int getMaxCorrNo( Connection con, String name ) throws CorrException
	{
		String query = "select id from max_ids where name='" + name + "'" ;
		try
		{
			PreparedStatement ps = con.prepareStatement(query) ;
			ResultSet rs = ps.executeQuery() ;
			if( rs.next()  )
			{
				String max_id = rs.getString("id") ;
				if( null == max_id )
				{
					// treat this as 0 
					return 0 ;
				}
				else
				{
					return Integer.parseInt(max_id) ;
				}
			}
			else
			{
				// treat this also as 0
				return 0 ;
			}
		}
		catch(SQLException s)
		{
			throw new CorrException("Cannot find the next correspondence number") ;
		}
		catch(NumberFormatException n )
		{
			throw new CorrException("Cannot find the next correspondence number") ;
		}
		finally
		{
//			if( null != con )
//			{
//				try {
//					con.close() ;
//				} catch (SQLException e) {					
//					e.printStackTrace();
//				}
//			}
		}
	}
	
	public static String getExpectedCorrNo( Connection con, CorrObject co ) throws CorrException
	{	
		String maxIdName = getCorrMaxIdName(co);
		// generate complete correspondence no.		
		int ncid = getMaxCorrNo( con, maxIdName );
		ncid += 1 ;
		DecimalFormat df = new DecimalFormat("0000") ;
		String nextCorresId =  df.format(ncid);  //Integer.toString(fc.getMaxRequestId() + 1);
	
		String corresNo = getCorrPrefix(co) ;
		// get year 		
		corresNo += nextCorresId ;
		
		return "[Likely]" + corresNo ;
	}
	
	public static int incrAndGetCorrNo(Connection con, String corrCat ) throws CorrException
	{
		System.out.println("generating corr. no. for : " + corrCat );
		try {	
			CallableStatement stmt = con.prepareCall("stp_getAndIncrMaxId ?");
			stmt.setString(1, corrCat );
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("max_id");
				System.out.println("Returning the next corr. no. = " + id );
				return id;
			} else {
				throw new CorrException("Cannot generate the next correspondence number");
			}
		} catch (SQLException e) {
			throw new CorrException("Cannot generate the next correspondence number");
		}		
	}
	
	public static String getRealCorrNo(CorrObject co, Connection con) throws CorrException 
	{
		String maxIdName = getCorrMaxIdName(co);
		int ncid = incrAndGetCorrNo(con,maxIdName);
				
		DecimalFormat df = new DecimalFormat("0000") ;
		String nextCorresId =  df.format(ncid); 
		String corresNo = getCorrPrefix(co) ;
		
		corresNo += nextCorresId ;
				
		return corresNo ;
	}
	
	public static String getCorrMaxIdName(CorrObject co) throws CorrException
	{
//		if( co.getBa().getSystemPrefix().equalsIgnoreCase("kdi_di") || co.getBa().getSystemPrefix().equalsIgnoreCase("kdi_corr"))
//			return getKdiMaxIdName(co);
//		else if( co.getBa().getSystemPrefix().equalsIgnoreCase("JPN_DI") || co.getBa().getSystemPrefix().equalsIgnoreCase("JPN_Corr"))
//			return getJPNMaxIdName(co);
//		else 
//			if( co.getBa().getSystemPrefix().equalsIgnoreCase("Malwa_DI") || co.getBa().getSystemPrefix().equalsIgnoreCase("Malwa_Corr"))
//			return getMalwaMaxIdName(co);
//		else if( co.getBa().getSystemPrefix().equalsIgnoreCase("APPDCL_DI") || co.getBa().getSystemPrefix().equalsIgnoreCase("APPDCL_Corr"))
//			return getAPPDCLMaxIdName(co);
//		else if( co.getBa().getSystemPrefix().equalsIgnoreCase("RJP_DI") || co.getBa().getSystemPrefix().equalsIgnoreCase("RJP_Corr"))
//			return getRJPMaxIdName(co);
//		else if( co.getBa().getSystemPrefix().equalsIgnoreCase("VPGL2_DI") || co.getBa().getSystemPrefix().equalsIgnoreCase("VPGL2_Corr"))
//			return getVPGL2MaxIdName(co);
//		else 
		if( co.getBa().getSystemPrefix().equalsIgnoreCase("IOM"))
			return getIOMMaxName(co);
//		else if( co.getBa().getSystemPrefix().equalsIgnoreCase("Kdi_IC_DI") || co.getBa().getSystemPrefix().equalsIgnoreCase("Kdi_IC_Corr" ))
//			return getKDI_ICMaxIdName(co);
		
		return getDefaultCorrMaxIdName(co);
	}

	private static String getKDI_ICMaxIdName(CorrObject co) throws CorrException {
		String baName = co.getBa().getSystemPrefix();
		String corrCatFieldName = "CorrespondenceCategory";
		String yy = Timestamp.toCustomFormat(new Date(), "yy");
		
		Field field = null;
		try {
			field = Field.lookupBySystemIdAndFieldName(co.getBa().getSystemId(), corrCatFieldName);
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if( null == field )
			throw new CorrException("Field not found with name : " + corrCatFieldName);
		
		String corrCat = co.getAsString(corrCatFieldName);
		if( null == corrCat )
			throw new CorrException(field.getDisplayName() + " was not set properly.");
		
		Type corrCatType = null;
		try {
			corrCatType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), corrCatFieldName, corrCat);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if( null == corrCatType )
			throw new CorrException(field.getDisplayName() + " was not set properly.");
		
		return baName + "-" + co.getGenerationAgency().getDescription() + "-" + co.getRecepientAgency().getDescription() + "-" + "KORADI-BTG-C10901" + "-" + yy  ;
	}

	public static String getMaxIdNameForPrefix(String sysPrefix,String corrPrefix) throws CorrException
	{
		if( sysPrefix.equalsIgnoreCase("kdi_di") || sysPrefix.equalsIgnoreCase("kdi_corr"))
			return getKdiMaxIdNameForPrefix(corrPrefix);
		else if( sysPrefix.equalsIgnoreCase("JPN_DI") || sysPrefix.equalsIgnoreCase("JPN_Corr"))
			return getJPNNameForPrefix(corrPrefix);
		else if( sysPrefix.equalsIgnoreCase("Malwa_DI") || sysPrefix.equalsIgnoreCase("Malwa_Corr"))
			return getMalwaNameForPrefix(corrPrefix);
		else if( sysPrefix.equalsIgnoreCase("APPDCL_DI") || sysPrefix.equalsIgnoreCase("APPDCL_Corr"))
			return getAPPDCLNameForPrefix(corrPrefix);
		else if( sysPrefix.equalsIgnoreCase("RJP_DI") || sysPrefix.equalsIgnoreCase("RJP_Corr"))
			return getRJPNameForPrefix(corrPrefix);
		else if( sysPrefix.equalsIgnoreCase("VPGL2_DI") || sysPrefix.equalsIgnoreCase("VPGL2_Corr"))
			return getVPGL2NameForPrefix(corrPrefix);
		else if( sysPrefix.equalsIgnoreCase("IOM"))
			return getIOMNameForPrefix(corrPrefix);
		else if( sysPrefix.equalsIgnoreCase("Kdi_IC_DI") || sysPrefix.equalsIgnoreCase("Kdi_IC_Corr" ))
			return getKDI_ICMaxIdNameForPrefix(corrPrefix,sysPrefix);
		
		return getDefaultNameForPrefix(corrPrefix);
//		throw new CorrException("Correspondence number generation is not configured for the business-area : " + sysPrefix);
		
	}
	
	private static String getKDI_ICMaxIdNameForPrefix(String corrPrefix, String sysPrefix) {
		//prefix : co.getGenerationAgency().getDescription() + co.getRecepientAgency().getDescription() + "KORADI-BTG-C10901" + yy + corrCatType.getDescription() + "-" ;
		// maxIdName : return baName + "-" + co.getGenerationAgency().getDescription() + "-" + co.getRecepientAgency().getDescription() + "-" + "KORADI-BTG-C10901" + "-" + yy  ;
		
		String[] parts = corrPrefix.split("-");
		Integer[] indexes = { 0,1,2,3,4,5 } ;
		String maxName = sysPrefix + "-" + joinParts(parts,indexes);
		LOG.info("For corrNumber : " + corrPrefix + " : the MaxIdName is : " + maxName);
		return maxName ;
	}

	private static String getDefaultNameForPrefix(String corrPrefix) 
	{
		// corrPrefix : prefixPOE.getValue().trim() + "-" + fl + "-" + co.getRecepientAgency().getDescription().trim() + "-" + scode1.getDescription().trim() + scode2.getDescription().trim() + "-" + co.getGenerationAgency().getDescription().trim() + "-" ;
		// maxIdName : prefixPOE.getValue().trim() + "-" + fl + "-" + co.getRecepientAgency().getDescription() + "-" + co.getGenerationAgency().getDescription().trim() ;
		String[] parts = corrPrefix.split("-");
		Integer[] indexes = { 0,1,2,5 } ;
		String maxName = joinParts(parts,indexes);
		LOG.info("For corrNumber : " + corrPrefix + " : the MaxIdName is : " + maxName);
		return maxName ;
	}

	private static String getIOMNameForPrefix(String corrPrefix) 
	{
		//prefix : type.getName().trim() + "-I-" + toType.getDescription().trim() + "-" + fromType.getDescription().trim() + "-" ;
		// maxIdName : type.getName().trim() + "-I-" + toType.getDescription().trim() + "-" + fromType.getDescription().trim() ;
		
		String[] parts = corrPrefix.split("-");
		Integer[] indexes = { 0,1,2,3 } ;
		String maxName = joinParts(parts,indexes);
		LOG.info("For corrNumber : " + corrPrefix + " : the MaxIdName is : " + maxName);
		return maxName ;
	}

	private static String getVPGL2NameForPrefix(String corrPrefix) 
	{
		//prefix :proj-Const + co.getGenerationAgency().getDescription().trim() + "-" + co.getRecepientAgency().getDescription().trim() + "-" + fl + "-" ;
		// maxIdName : proj-Const + co.getGenerationAgency().getDescription().trim() + "-" + co.getRecepientAgency().getDescription().trim() + "-" + fl ;
		
		String[] parts = corrPrefix.split("-");
		Integer[] indexes = { 0,1,2,3,4 } ;
		String maxName = joinParts(parts,indexes);
		LOG.info("For corrNumber : " + corrPrefix + " : the MaxIdName is : " + maxName);
		return maxName ;
	}

	private static String getRJPNameForPrefix(String corrPrefix) 
	{
		//prefix : prefixPOE.getValue().trim() + "-" + fl + "-" + co.getRecepientAgency().getDescription().trim() + "-" + scode1.getDescription().trim() + scode2.getDescription().trim() + "-" + co.getGenerationAgency().getDescription().trim() + "-" ;
		// maxIdName : prefixPOE.getValue().trim() + "-" + fl + "-" + co.getRecepientAgency().getDescription() + "-" + co.getGenerationAgency().getDescription().trim() ;
		
		String[] parts = corrPrefix.split("-");
		Integer[] indexes = { 0,1,2,5 } ;
		String maxName = joinParts(parts,indexes);
		LOG.info("For corrNumber : " + corrPrefix + " : the MaxIdName is : " + maxName);
		return maxName ;
	}

	private static String getAPPDCLNameForPrefix(String corrPrefix) throws CorrException 
	{
		String[] parts = corrPrefix.split("-");
		// Prefix : co.getGenerationAgency().getDescription().trim() + "-" + co.getRecepientAgency().getDescription().trim() + "-L489000-" + yy + "-" + fl + "-" ; // the code may or may not contain a dash
		// MaxIdName : co.getGenerationAgency().getDescription().trim() + "-" + co.getRecepientAgency().getDescription().trim() + "-L489000-" + yy + "-" + fl + "-" ; // the code may or may not contain a dash

		String maxName = null;
//		if( parts.length == 5 )
//		{
//			Integer[] indexes = {0,1,3};
//			maxName = joinParts(parts,indexes);
//		}
//		else if( parts.length == 4 )
//		{
			Integer[] indexes = {0,1,2,3,4};
			maxName = joinParts(parts,indexes);
//		}

		LOG.info("For corrNumber : " + corrPrefix + " : the MaxIdName is : " + maxName);
		return maxName ;
	}

	private static String getMalwaNameForPrefix(String corrPrefix) {
		String[] parts = corrPrefix.split("-");
		// Prefix : co.getGenerationAgency().getDescription() + "-" + type.getDescription().trim() + "-Malwa-" + yyyy-yy + "-"; // other are similar
		// MaxIdName : co.getGenerationAgency().getDescription() + "-" + type.getDescription().trim() + "-Malwa-" + yyyy-yy ;
		// 6-5
		
		String maxName = null;
		int size = parts.length ;
		if( size == 6 )
		{
			Integer[] indexes = { 0,1,2,3,4 } ;
			maxName = joinParts(parts,indexes);
		}
		else if( size == 7 )
		{
			Integer[] indexes = {0,1,2,3,4,5};
			maxName = joinParts(parts,indexes);
		}
		
		LOG.info("For corrNumber : " + corrPrefix + " : the MaxId Name is : " + maxName);
		return maxName ;
	}

	private static String getJPNNameForPrefix(String corrPrefix) 
	{
		String[] parts = corrPrefix.split("-");
		// Prefix : co.getGenerationAgency().getName() + "-" + co.getRecepientAgency().getName() + "-JAYPEE_NIGRIE-T209001-" + yy + "-" +fl + "-";
		// MaxIdName : co.getGenerationAgency().getName() + "-" + co.getRecepientAgency().getName() + "-JAYPEE_NIGRIE-T209001-" + yy +"-"+ fl;
		Integer[] indexes = { 0,1,2,3,4,5 } ;
		String maxName = joinParts(parts,indexes);
		LOG.info("For corrNumber : " + corrPrefix + " : the MaxIdName is : " + maxName);
		return maxName ;
	}

	private static String getKdiMaxIdNameForPrefix(String corrPrefix) 
	{
		String[] parts = corrPrefix.split("-");
		// Prefix : co.getGenerationAgency().getName() + "-" + co.getRecepientAgency().getName() + "-KORADI-BTG-C10901-" + yy + "-" + fl + "-";
		// MaxIdName : co.getGenerationAgency().getName() + "-" + co.getRecepientAgency().getName() + "-KORADI-BTG-C10901-" + yy +"-"+ fl;
		Integer[] indexes = { 0,1,2,3,4,5,6 } ;
		String maxName = joinParts(parts,indexes);
		LOG.info("For corrNumber : " + corrPrefix + " : the MaxIdName is : " + maxName);
		return maxName ;
	}

	private static String joinParts(String[] parts, Integer[] indexes) 
	{
		if( null == parts || null == indexes )
			return null;
		
		String str = "" ;
		boolean first = true;
		for( Integer index : indexes )
		{
			try
			{
				if( first )
				{
					str += parts[index];
					first = false;
				}
				else
				{
					str += "-" + parts[index];
				}
			}
			catch( ArrayIndexOutOfBoundsException e)
			{
				LOG.error("Exception occured while joining parts : " + parts + " from indexes : " + indexes);
				LOG.error(TBitsLogger.getStackTrace(e));
				return null;
			}
		}
		
		return str;
	}

	private static String getVPGL2MaxIdName(CorrObject co) throws CorrException {
		String corrCat = co.getAsString("CorrespondenceCategory");
		if( null == corrCat )
			throw new CorrException("Cannot find field with name : CorrespondenceCategory" );
		String fl = corrCat.substring(0,1).toUpperCase();
		
		if( null == co.getGenerationAgency() )
			throw new CorrException("GenerationAgency was not properly set.");
		
		if( null == co.getRecepientAgency() )
			throw new CorrException("RecepientAgency was not properly set.");
		
		String projConst = "LT-VEM2-" ;
		
		return projConst + co.getGenerationAgency().getDescription().trim() + "-" + co.getRecepientAgency().getDescription().trim() + "-" + fl ;
	}

	private static String getRJPMaxIdName(CorrObject co) throws CorrException 
	{
		String corrCat = co.getAsString("CorrespondenceCategory");
		if( null == corrCat )
			throw new CorrException("Cannot find field with name : CorrespondenceCategory" );
		String fl = corrCat.substring(0,1).toUpperCase();

		ProtocolOptionEntry prefixPOE = ProtocolOptionsManager.lookupProtocolEntry(co.getBa().getSystemPrefix(), LnTConst.LnTNumberPrefix);
		if( null == prefixPOE || null == prefixPOE.getValue() )
			throw new CorrException("Correspondence was not properly configured for protocol option " + LnTConst.LnTNumberPrefix + " for ba : " + co.getBa().getSystemPrefix());
		
		// prefix - corrType.firstLetter - genAgency.description 
		return prefixPOE.getValue().trim() + "-" + fl + "-" + co.getRecepientAgency().getDescription().trim() + "-" + co.getGenerationAgency().getDescription().trim() ;

	}

	public static String getMalwaMaxIdName(CorrObject co) throws CorrException 
	{
		Date date = new Date() ;
		String yyyy = getFinancialYear(date);//Timestamp.toCustomFormat(date, "yyyy");

		String packageField = "category_id";
		String packageName = co.getAsString(packageField);
		
		Type type = null;
		try {
			type = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), packageField, packageName);
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			e.printStackTrace();
		}
		if( null == type )
			throw new CorrException("Cannot find any value in field " + packageField);
		
		String corrCatFN = "CorrespondenceCategory";
		
		String corrCatName = co.getAsString(corrCatFN);
		
		if( null == corrCatName )
			throw new CorrException("Cannot find any value in field : " + Utility.fdn(co.getBa(),corrCatFN));
		
		Type corrCat = null;
		try {
			 corrCat = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), corrCatFN, corrCatName);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}

		if( null == corrCat )
			throw new CorrException("Cannot find any value in field : " + Utility.fdn(co.getBa(),corrCatFN));
		
		
		Type genAgency = co.getGenerationAgency() ;
		if( genAgency.getName().equals("MPPGCL"))
		{
			return "MPPGCL-MALWA-Phase1-" + yyyy + corrCat.getDescription() ;
		}
//		else if( genAgency.getName().equals("LTSL"))
//		{
//			return "LTSL-MALWA-Phase1-"  + yyyy + corrCat.getDescription() ;
//		}
//		else if( genAgency.getName().equals("DCPL") )
//		{
//			return "DCPL-MALWA-Phase1-"  + yyyy + corrCat.getDescription() ;
//		}
		
		
//		if( co.getRecepientAgency().getName().equals("MPPGCL") )
//		{
//			return co.getGenerationAgency().getDescription().trim() + "-" + type.getDescription().trim() + "-Singaji-" + yyyy + corrCat.getDescription() ;
//		}
		
		return co.getGenerationAgency().getDescription().trim() + "-" + type.getDescription().trim() + "-Malwa-" + yyyy + corrCat.getDescription() ;
	}
	
	public static String getCorrPrefix(CorrObject co ) throws CorrException
	{
//		if( co.getBa().getSystemPrefix().equalsIgnoreCase("kdi_di") || co.getBa().getSystemPrefix().equalsIgnoreCase("kdi_corr") )
//			return getKdiCorrPrefix(co);
//		else if( co.getBa().getSystemPrefix().equalsIgnoreCase("JPN_DI") || co.getBa().getSystemPrefix().equalsIgnoreCase("JPN_Corr") )
//			return getJPNCorrPrefix(co);
//		else 
//			if(  co.getBa().getSystemPrefix().equalsIgnoreCase("Malwa_DI") || co.getBa().getSystemPrefix().equalsIgnoreCase("Malwa_Corr"))
//			return getMalwaCorrPrefix(co);
//		else if ( co.getBa().getSystemPrefix().equalsIgnoreCase("VPGL2_DI" ) || co.getBa().getSystemPrefix().equalsIgnoreCase("VPGL2_Corr" ) )
//			return getVPGL2CorrPrefix(co);
//		else if ( co.getBa().getSystemPrefix().equalsIgnoreCase("APPDCL_DI" ) || co.getBa().getSystemPrefix().equalsIgnoreCase("APPDCL_Corr" ))
//			return getAPPDCLCorrNumber(co);
//		else if (co.getBa().getSystemPrefix().equalsIgnoreCase("RJP_DI" ) || co.getBa().getSystemPrefix().equalsIgnoreCase("RJP_Corr" ))
//			return getRJPCorrNumber(co);
//		else 
			if( co.getBa().getSystemPrefix().equalsIgnoreCase("IOM") )
			return getIOMCorrPrefix(co);
//		else if( co.getBa().getSystemPrefix().equalsIgnoreCase("Kdi_IC_DI") || co.getBa().getSystemPrefix().equalsIgnoreCase("Kdi_IC_Corr" ))
//			return getKDI_ICCorrPrefix(co);

			return getDefaultCorrPrefix(co);
//		throw new CorrException("Cannot generate correspondence no. as no matching corr-prefix found.");
	}

	private static String getKDI_ICCorrPrefix(CorrObject co) throws CorrException
	{
//		String baName = co.getBa().getSystemPrefix();
		String corrCatFieldName = "CorrespondenceCategory";
		String yy = Timestamp.toCustomFormat(new Date(), "yy");
		
		Field field = null;
		try {
			field = Field.lookupBySystemIdAndFieldName(co.getBa().getSystemId(), corrCatFieldName);
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if( null == field )
			throw new CorrException("Field not found with name : " + corrCatFieldName);
		
		String corrCat = co.getAsString(corrCatFieldName);
		if( null == corrCat )
			throw new CorrException(field.getDisplayName() + " was not set properly.");
		
		Type corrCatType = null;
		try {
			corrCatType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), corrCatFieldName, corrCat);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if( null == corrCatType )
			throw new CorrException(field.getDisplayName() + " was not set properly.");
		
		return co.getGenerationAgency().getDescription() + "-" + co.getRecepientAgency().getDescription() + "-" + "KORADI-BTG-C10901" + "-" + yy + "-" + corrCatType.getDescription() + "-" ; 
	}

	public static String getDefaultCorrMaxIdName(CorrObject co) throws CorrException
	{
		String corrCat = co.getAsString("CorrespondenceCategory");
		if( null == corrCat )
			throw new CorrException("Cannot find field with name : CorrespondenceCategory" );
		String fl = corrCat.substring(0,1).toUpperCase();

		ProtocolOptionEntry prefixPOE = ProtocolOptionsManager.lookupProtocolEntry(co.getBa().getSystemPrefix(), LnTConst.LnTNumberPrefix);
		if( null == prefixPOE || null == prefixPOE.getValue() )
			throw new CorrException("Correspondence was not properly configured for protocol option " + LnTConst.LnTNumberPrefix + " for ba : " + co.getBa().getSystemPrefix());
		
		// prefix - corrType.firstLetter - code1.description  code2.description - scode1.description scode2.description - genAgency.description - running no.
		return prefixPOE.getValue().trim() + "-" + fl + "-" + co.getRecepientAgency().getDescription().trim() + "-" + co.getGenerationAgency().getDescription().trim() ;

	}
	
	public static String getDefaultCorrPrefix(CorrObject co ) throws CorrException
	{
		String corrCat = co.getAsString("CorrespondenceCategory");
		if( null == corrCat )
			throw new CorrException("Cannot find field with name : CorrespondenceCategory" );
		String fl = corrCat.substring(0,1).toUpperCase();

		String scode1Field = "SCode1";
		String scode1Name = co.getAsString(scode1Field);
		
		Type scode1 = null;
		try {
			scode1 = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), scode1Field, scode1Name);
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			e.printStackTrace();
		}
		if( null == scode1 )
			throw new CorrException("Cannot find any value in field " + scode1Field);
		
		String scode2Field = "SCode2";
		String scode2Name = co.getAsString(scode2Field);
		
		Type scode2 = null;
		try {
			scode2 = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), scode2Field, scode2Name);
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			e.printStackTrace();
		}
		if( null == scode2 )
			throw new CorrException("Cannot find any value in field " + scode2Field);

		ProtocolOptionEntry prefixPOE = ProtocolOptionsManager.lookupProtocolEntry(co.getBa().getSystemPrefix(), LnTConst.LnTNumberPrefix);
		if( null == prefixPOE || null == prefixPOE.getValue() )
			throw new CorrException("Correspondence was not properly configured for protocol option " + LnTConst.LnTNumberPrefix + " for ba : " + co.getBa().getSystemPrefix());
		
		// prefix - corrType.firstLetter - code1.description  code2.description - scode1.description scode2.description - genAgency.description - running no.
		return prefixPOE.getValue().trim() + "-" + fl + "-" + co.getRecepientAgency().getDescription().trim() + "-" + scode1.getDescription().trim() + scode2.getDescription().trim() + "-" + co.getGenerationAgency().getDescription().trim() + "-" ;
	}
	private static String getVPGL2CorrPrefix(CorrObject co) throws CorrException 
	{
		String corrCat = co.getAsString("CorrespondenceCategory");
		if( null == corrCat )
			throw new CorrException("Cannot find field with name : CorrespondenceCategory" );
		String fl = corrCat.substring(0,1).toUpperCase();
		
		if( null == co.getGenerationAgency() )
			throw new CorrException("GenerationAgency was not properly set.");
		
		if( null == co.getRecepientAgency() )
			throw new CorrException("RecepientAgency was not properly set.");
		
		String projConst = "LT-VEM2-" ;
		
		return projConst + co.getGenerationAgency().getDescription().trim() + "-" + co.getRecepientAgency().getDescription().trim() + "-" + fl + "-" ;
	}

	private static String getRJPCorrNumber(CorrObject co) throws CorrException 
	{
		String corrCat = co.getAsString("CorrespondenceCategory");
		if( null == corrCat )
			throw new CorrException("Cannot find field with name : CorrespondenceCategory" );
		String fl = corrCat.substring(0,1).toUpperCase();

		String scode1Field = "SCode1";
		String scode1Name = co.getAsString(scode1Field);
		
		Type scode1 = null;
		try {
			scode1 = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), scode1Field, scode1Name);
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			e.printStackTrace();
		}
		if( null == scode1 )
			throw new CorrException("Cannot find any value in field " + scode1Field);
		
		String scode2Field = "SCode2";
		String scode2Name = co.getAsString(scode2Field);
		
		Type scode2 = null;
		try {
			scode2 = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), scode2Field, scode2Name);
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			e.printStackTrace();
		}
		if( null == scode2 )
			throw new CorrException("Cannot find any value in field " + scode2Field);

		ProtocolOptionEntry prefixPOE = ProtocolOptionsManager.lookupProtocolEntry(co.getBa().getSystemPrefix(), LnTConst.LnTNumberPrefix);
		if( null == prefixPOE || null == prefixPOE.getValue() )
			throw new CorrException("Correspondence was not properly configured for protocol option " + LnTConst.LnTNumberPrefix + " for ba : " + co.getBa().getSystemPrefix());
		
		// prefix - corrType.firstLetter - code1.description  code2.description - scode1.description scode2.description - genAgency.description - running no.
		return prefixPOE.getValue().trim() + "-" + fl + "-" + co.getRecepientAgency().getDescription().trim() + "-" + scode1.getDescription().trim() + scode2.getDescription().trim() + "-" + co.getGenerationAgency().getDescription().trim() + "-" ;

	}

	private static String getIOMCorrPrefix(CorrObject co) throws CorrException 
	{
		String fromDep = "from_department";
		String toDep = "to_department" ;
		
		String from = co.getAsString(fromDep);
		String to = co.getAsString(toDep);
		
		if( null == from )
			throw new CorrException("Please select a value in " + Utility.fdn(co.getBa(),fromDep));
		if( null == to )
			throw new CorrException("Please select a value in " + Utility.fdn(co.getBa(),toDep));
		
		Type fromType = null;
		try {
			 fromType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), fromDep, from);
		} catch (DatabaseException e1) {
			e1.printStackTrace();
		}
		
		Type toType = null;
		try {
			toType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), toDep, to);
		} catch (DatabaseException e1) {
			e1.printStackTrace();
		}
		
		if( null == fromType )
			throw new CorrException("Please select a value in " + Utility.fdn(co.getBa(),fromDep));
		if( null == toType )
			throw new CorrException("Please select a value in " + Utility.fdn(co.getBa(),toDep));
		
		String fieldName = "status_id";
		String fieldValue = co.getAsString(fieldName);
		if( null == fieldValue )
			throw new CorrException("Please select a value in " + Utility.fdn(co.getBa(),fieldName));
		
		Type type = null;
		try {
			type = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), fieldName, fieldValue);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		
		if( null == type )
			throw new CorrException("Please select a value in " + Utility.fdn(co.getBa(),fieldName));
		
		return type.getName().trim() + "-I-" + toType.getDescription().trim() + "-" + fromType.getDescription().trim() + "-" ;
	}
	
	private static String getIOMMaxName(CorrObject co) throws CorrException 
	{
		String fromDep = "from_department";
		String toDep = "to_department" ;
		
		String from = co.getAsString(fromDep);
		String to = co.getAsString(toDep);
		
		if( null == from )
			throw new CorrException("Please select a value in " + Utility.fdn(co.getBa(),fromDep));
		if( null == to )
			throw new CorrException("Please select a value in " + Utility.fdn(co.getBa(),toDep));
		
		Type fromType = null;
		try {
			 fromType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), fromDep, from);
		} catch (DatabaseException e1) {
			e1.printStackTrace();
		}
		
		Type toType = null;
		try {
			toType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), toDep, to);
		} catch (DatabaseException e1) {
			e1.printStackTrace();
		}
		
		if( null == fromType || fromType.getName().equalsIgnoreCase("null"))
			throw new CorrException("Please select a value in " + Utility.fdn(co.getBa(),fromDep));
		if( null == toType || toType.getName().equalsIgnoreCase("null"))
			throw new CorrException("Please select a value in " + Utility.fdn(co.getBa(),toDep));
		
		String fieldName = "status_id";
		String fieldValue = co.getAsString(fieldName);
		if( null == fieldValue )
			throw new CorrException("Please select a value in " + Utility.fdn(co.getBa(),fieldName));
		
		Type type = null;
		try {
			type = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), fieldName, fieldValue);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		
		if( null == type || type.getName().equalsIgnoreCase("null"))
			throw new CorrException("Please select a value in " + Utility.fdn(co.getBa(),fieldName));
		
		return type.getName().trim() + "-I-" + toType.getDescription().trim() + "-" + fromType.getDescription().trim() ;
	}

	public static String getMalwaCorrPrefix(CorrObject co) throws CorrException {
		Date date = new Date() ;
		String yyyy =  getFinancialYear(date);//Timestamp.toCustomFormat(date, "yyyy");

		String packageField = "category_id";
		String packageName = co.getAsString(packageField);
		
		Type type = null;
		try {
			type = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), packageField, packageName);
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			e.printStackTrace();
		}
		if( null == type )
			throw new CorrException("Cannot find any value in field " + packageField);
	
		String corrCatFN = "CorrespondenceCategory";
		
		String corrCatName = co.getAsString(corrCatFN);
		
		if( null == corrCatName )
			throw new CorrException("Cannot find any value in field : " + Utility.fdn(co.getBa(),corrCatFN));
		
		Type corrCat = null;
		try {
			 corrCat = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), corrCatFN, corrCatName);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}

		if( null == corrCat )
			throw new CorrException("Cannot find any value in field : " + Utility.fdn(co.getBa(),corrCatFN));
		
		Type genAgency = co.getGenerationAgency() ;
		if( genAgency.getName().equals("MPPGCL"))
		{
			return "MPPGCL-MALWA-Phase1-" + yyyy + corrCat.getDescription()  + "-" ;
		}
//		else if( genAgency.getName().equals("LTSL"))
//		{
//			return "LTSL-MALWA-Phase1-"  + yyyy + corrCat.getDescription()  + "-" ;
//		}
//		else if( genAgency.getName().equals("DCPL") )
//		{
//			return "DCPL-MALWA-Phase1-"  + yyyy + corrCat.getDescription()  + "-" ;
//		}
//
//		if( co.getRecepientAgency().getName().equals("MPPGCL") )
//		{
//			return co.getGenerationAgency().getDescription().trim() + "-" + type.getDescription().trim() + "-Singaji-" + yyyy + corrCat.getDescription() + "-";
//		}
		
		return co.getGenerationAgency().getDescription().trim() + "-" + type.getDescription().trim() + "-Malwa-" + yyyy + corrCat.getDescription() + "-" ;
	}

	public static String getFinancialYear(Date d) 
	{
		Calendar ndd = Calendar.getInstance() ;
		ndd.setTime(d);
		int currMonth = ndd.get(Calendar.MONTH) ;
//		int currYear = ndd.get(Calendar.YEAR);
		
		if( currMonth < Calendar.APRIL )
		{
			Calendar other = Calendar.getInstance(); 
			other.add(Calendar.YEAR, -1);
//			Timestamp nowTs = new Timestamp(ndd.getTimeInMillis()) ;
//			Timestamp otherTs = new Timestamp(other.getTimeInMillis()) ;
			return Timestamp.toCustomFormat(other.getTime(),"yyyy") + "-" + Timestamp.toCustomFormat(ndd.getTime(),"yy") ;
		}
		else
		{
			Calendar other = Calendar.getInstance(); 
			other.add(Calendar.YEAR, 1);
//			Timestamp nowTs = new Timestamp(ndd.getTimeInMillis()) ;
//			Timestamp otherTs = new Timestamp(other.getTimeInMillis()) ;
			return Timestamp.toCustomFormat(ndd.getTime(),"yyyy") + "-" + Timestamp.toCustomFormat(other.getTime(),"yy") ;
		}		
	}
	
	public static String getAPPDCLCorrNumber(CorrObject co) throws CorrException 
	{
//		Date date = new Date() ;
		String corrCat = co.getAsString("CorrespondenceCategory");
		if( null == corrCat )
			throw new CorrException("Cannot find field with name : CorrespondenceCategory" );
		String fl = corrCat.substring(0,1).toUpperCase();

//		String packageField = "category_id";
//		String packageName = co.getAsString(packageField);
//		
//		Type type = null;
//		try {
//			type = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), packageField, packageName);
//		} catch (DatabaseException e) {
//			LOG.info(TBitsLogger.getStackTrace(e));
//			e.printStackTrace();
//		}
//		if( null == type )
//			throw new CorrException("Cannot find any value in field " + packageField);
	
//		String fieldName = "severity_id";
//		String value = co.getAsString(fieldName);
//		String code = "" ;
//		if( value.equals("CM"))
//		{
//			code = "-C";
//		}
//		else if ( value.equals("TN"))
//		{
//			code = "-T";
//		}

		Date date = new Date() ;
		String yy = Timestamp.toCustomFormat(date, "yy");
		
		return co.getGenerationAgency().getDescription().trim() + "-" + co.getRecepientAgency().getDescription().trim() + "-L489000-" + yy + "-" + fl + "-" ;
	}
	
	public static String getAPPDCLMaxIdName(CorrObject co) throws CorrException 
	{

		Date date = new Date() ;
//		String yy = Timestamp.toCustomFormat(date, "yy");
		
		String corrCat = co.getAsString("CorrespondenceCategory");
		if( null == corrCat )
			throw new CorrException("Cannot find field with name : CorrespondenceCategory" );

		String fl = corrCat.substring(0,1).toUpperCase();
		
		// removed the yy from the max-id name from appdcl as it should not depend on the year but made the slot as 10 so that we don't have
		// to change the max id's as of yet. I future all the numbers will have 10 instead of year which is hardcoded now.
		return co.getGenerationAgency().getDescription().trim() + "-" + co.getRecepientAgency().getDescription().trim() + "-L489000-" + "10" + "-" + fl ;
	}

	
	public static String getJPNCorrPrefix(CorrObject co) throws CorrException 
	{
		Date date = new Date() ;
		String yy = Timestamp.toCustomFormat(date, "yy");
		String corrCat = co.getAsString("CorrespondenceCategory");
		if( null == corrCat )
			throw new CorrException("Cannot find field with name : CorrespondenceCategory" );
		String fl = corrCat.substring(0,1).toUpperCase();
		// " L&T-JPVLO-JAYPEE_NIGRIE-T209001-09-L-001"
		return co.getGenerationAgency().getName() + "-" + co.getRecepientAgency().getName() + "-JAYPEE_NIGRIE-T209001-" + yy + "-" +fl + "-";
	}

	public static String getJPNMaxIdName(CorrObject co) throws CorrException 
	{
		Date date = new Date() ;
		String yy = Timestamp.toCustomFormat(date, "yy");
		String corrCat = co.getAsString("CorrespondenceCategory");
		if( null == corrCat )
			throw new CorrException("Cannot find field with name : CorrespondenceCategory" );
		String fl = corrCat.substring(0,1).toUpperCase();
		// " L&T-JPVLO-JAYPEE_NIGRIE-T209001-09-L-001"
		return co.getGenerationAgency().getName() + "-" + co.getRecepientAgency().getName() + "-JAYPEE_NIGRIE-T209001-" + yy +"-"+ fl; 
	}
	
	public static String getKdiCorrPrefix(CorrObject co) throws CorrException 
	{
		Date date = new Date() ;
		String yy = Timestamp.toCustomFormat(date, "yy");
		String corrCat = co.getAsString("CorrespondenceCategory");
		if( null == corrCat )
			throw new CorrException("Cannot find field with name : CorrespondenceCategory" );
		String fl = corrCat.substring(0,1).toUpperCase();
		// KORADI - BTG - C10901
		return co.getGenerationAgency().getName() + "-" + co.getRecepientAgency().getName() + "-KORADI-BTG-C10901-" + yy + "-" + fl + "-"; 
	}

	public static String getKdiMaxIdName(CorrObject co) throws CorrException 
	{
		Date date = new Date() ;
		String yy = Timestamp.toCustomFormat(date, "yy");
		String corrCat = co.getAsString("CorrespondenceCategory");
		if( null == corrCat )
			throw new CorrException("Cannot find field with name : CorrespondenceCategory" );
		String fl = corrCat.substring(0,1).toUpperCase();
		return co.getGenerationAgency().getName() + "-" + co.getRecepientAgency().getName() + "-KORADI-BTG-C10901-" + yy +"-"+ fl; 
	}
	
	
	public static void main(String argv[])
	{
		
	}
}
