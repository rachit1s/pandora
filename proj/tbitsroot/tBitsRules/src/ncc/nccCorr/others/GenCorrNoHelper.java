package nccCorr.others;

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
import transbit.tbits.domain.Type;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

import static nccCorr.others.CorresConstants.*;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

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
				throw new CorrException("You have specified to take the number from correspondence number field." +
											" But such a field is not configured for this ba : " + co.getBa().getSystemPrefix());
			corrNo = co.getAsString(corrFieldName);
			if( null == corrNo || corrNo.trim().equals(""))
				throw new CorrException("You have selected to generate correspondence file with given number but not" +
											" provided the correspondence number.") ;
			else
			{
				corrNo = corrNo.trim();
				return validateCorrNumber(con,co,corrNo) ;							
			}
		}
		else //only no. / no. and pdf  
		{
			corrNo = null ;
			if( co.getSource() == CorrObject.SourcePreview )
				corrNo = getExpectedCorrNo(con,co) ;
			else
			{
				corrNo = getRealCorrNo(co,con) ;
				if(null == corrFieldName)
					throw new CorrException("You have chose to generate correspondence file. But the correspondence " +
												"number field is not configured for this BA : " + co.getBa().getSystemPrefix());
				if( null == corrNo )
					throw new CorrException("Excetion occured while generating correspondence number.");
				
			}
			
			if( null == corrNo )
				throw new CorrException("Cannot generate the correspondence number.") ;
			
			
			return corrNo ;
		}
	}
	
	private static String validateCorrNumber(Connection con, CorrObject co, String corrNo) throws CorrException 
	{		
		corrNo = sanitize( corrNo );
		
		String maxIdName = getCorrMaxIdName(co);
		
		int expectedRunningNo = getMaxCorrNo(con,maxIdName);
		expectedRunningNo++ ;
		DecimalFormat df = new DecimalFormat("0000") ;
		String nextCorresId =  df.format(expectedRunningNo);
		
		String corrPrefix = getCorrPrefix(co) ;
		try
		{		
			int dashLoc = corrNo.lastIndexOf("-");
			if( dashLoc > 0 && (dashLoc < corrNo.length()-1))
			{
				String runNum = corrNo.substring(dashLoc + 1);
				int rn = Integer.parseInt(runNum);	
				
				String prefix = corrNo.substring(0, dashLoc);
				if(!prefix.equalsIgnoreCase(corrPrefix))
					throw new CorrException("The prefix should be : " + corrPrefix );
				
				if(rn < 1 || rn > expectedRunningNo )
					throw new CorrException("The running number for prefix(" + corrPrefix + ") is : " + nextCorresId );
				
				if( co.getSource() == CorrObject.SourceReal && rn == expectedRunningNo )
				{
					incrAndGetCorrNo(con,maxIdName);
				}
				
				String rnStr = df.format(rn);
				corrNo = corrPrefix + "-" + rnStr ; 			
			}
			else
			{
				throw new Exception("Illegal corr. no.");
			}
			
			return corrNo;
		}
		catch(CorrException e )
		{
			throw e ;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new CorrException("Illegal value in Correspondence Number field.\nExpected value is : " + corrPrefix + "-" + nextCorresId ); 
		}
	}
	
	private static String sanitize(String corrNo) 
	{		
		return removeRedundantDashes(replaceStrangeDash(corrNo));
	}

	public static String removeRedundantDashes(String corrNo )
	{
		if( null == corrNo || corrNo.equalsIgnoreCase("") ) 
			return "" ;
		
		String ncn = "" ;
		boolean gotD = false ;
		 
		for( int i = 0 ; i < corrNo.length() ; i++ )
		{
			if(  '-' == corrNo.charAt(i) )
			{
				if( true == gotD  )
					continue ;
				else 
				{
					ncn += corrNo.charAt(i) ;
					gotD = true ;
				}
			}
			else
			{
				ncn += corrNo.charAt(i) ;
				gotD = false ;
			}
		}
		
		if(ncn.length() == 0 )
			return ncn ;
		else
		{
			// remove trailing -es
			if( '-' == ncn.charAt(0) ) // first character
			{
				if( ncn.length() > 1 )
				{
					ncn = ncn.substring(1) ;				
				}
				else return "" ;
			}
			
			if('-' == ncn.charAt(ncn.length()-1)) // last character
			{
				if( ncn.length() > 1 )					
				{
					ncn = ncn.substring(0, ncn.length()-1) ;
				}
				else return "" ;
			}
		}
		
		return ncn ;
	}	
		
	public static String replaceStrangeDash( String str ) 
	{
		String out = "" ;
		if( null == str ) 
			return out ;
		
		char strangeDash1 = (char)150 ;
		char strangeDash2 = (char)8211 ;
		char validDash = '-' ;
//		String regex = "[" + strangeDash1 + strangeDash2 + "]" ;
//		String replacement = validDash+"" ;
		
		for( int i = 0 ; i < str.length() ; i++ )
		{
			if( str.charAt(i) == strangeDash1 || str.charAt(i) == strangeDash2 )
				out += validDash ;
			else
				out += str.charAt(i) ;
		}
		
		return out ;
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
		corresNo += "-";
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
		
		corresNo += "-";
		corresNo += nextCorresId ;
				
		return corresNo ;
	}
	
	public static String getFinancialYear(Date d) 
	{
		Calendar ndd = Calendar.getInstance() ;
		ndd.setTime(d);
		int currMonth = ndd.get(Calendar.MONTH) ;
		
		if( currMonth < Calendar.APRIL )
		{
			Calendar other = Calendar.getInstance(); 
			other.add(Calendar.YEAR, -1);
			return Timestamp.toCustomFormat(other.getTime(),"yy") + "-" + Timestamp.toCustomFormat(ndd.getTime(),"yy") ;
		}
		else
		{
			Calendar other = Calendar.getInstance(); 
			other.add(Calendar.YEAR, 1);
			return Timestamp.toCustomFormat(ndd.getTime(),"yy") + "-" + Timestamp.toCustomFormat(other.getTime(),"yy") ;
		}		
	}
	
	public static String getCorrMaxIdName(CorrObject co) throws CorrException
	{
		if( co.getBa().getSystemPrefix().equals(CORR_SYSPREFIX) )
			return getCorrCorrMaxIdName(co);
		else if ( co.getBa().getSystemPrefix().equals(NAGAI_CORR_SYSPREFIX))
			return getNAGAICorrMaxIdName(co);
		else
		{
			return getCorrPrefix(co);
		}	
	}

	private static String getNAGAICorrMaxIdName(CorrObject co) throws CorrException 
	{
		String genAgen = co.getUserMapUsers().get(0).getLocation();
		if( null == genAgen )
			throw new CorrException("The Location for user : " + co.getUserMapUsers().get(0).getUserLogin() + " is not defined.");
		
		try
		{
			if( genAgen.equals(CORR_ORIG_NCCP))
			{
				return getNCCPMaxIdName(co);
			}
			else if( genAgen.equals(CORR_ORIG_ACBL))
			{
				return getACBLMaxIdName(co);
			}
			else if (genAgen.equals(CORR_ORIG_TPSC))
			{
				return getTPSCMaxIdName(co);
			}
			else if( genAgen.equals(CORR_ORIG_NPPL))
			{
				return getNPPLMaxIdName(co);
			}
			else if( genAgen.equals(CORR_ORIG_AEC))
			{
				return getAECMaxIdName(co);
			}
			if( genAgen.equals(CORR_ORIG_EDTD))
			{
				return getEDTDMaxIdName(co);
			}
			else throw new CorrException("Correspondence number system is not configured for agency : " + genAgen);
		}
		catch(CorrException te)
		{
			throw te;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new CorrException(e.getMessage());
		}
	}

	private static String getEDTDMaxIdName(CorrObject co) {
		String projectCode = "NPT10110" ;
		return projectCode + "-" + co.getGenerationAgency().getDescription().trim() + "-" + getFinancialYear(new Date());
	}

	private static String getAECMaxIdName(CorrObject co) {
		return getAECCorrPrefix(co);
	}

	private static String getNPPLMaxIdName(CorrObject co) throws CorrException, DatabaseException {
		return getNPPLCorrPrefix(co);
	}

	private static String getTPSCMaxIdName(CorrObject co) throws CorrException, DatabaseException {
		return getTPSCCorrPrefix(co);
	}

	private static String getACBLMaxIdName(CorrObject co) {
		String yyyy = Timestamp.toCustomFormat(new Date(), "yyyy");
		
		return yyyy + "-" + co.getGenerationAgency().getDescription().trim() ;
	}

	private static String getNCCPMaxIdName(CorrObject co) throws CorrException, DatabaseException {
		String projectCode = "NPT10110" ;
//		String contractRef = co.getAsString(NAGAI_CONTRACT_REF);
//		if( null == contractRef )
//			throw new CorrException(Utility.fdn(co.getBa(), NAGAI_CONTRACT_REF) + " was not set properly.");
//		
//		Type contType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), NAGAI_CONTRACT_REF, contractRef);
//		
//		String unitCode = co.getAsString(NAGAI_UNIT_CODE);
//		if( null == unitCode )
//			throw new CorrException(Utility.fdn(co.getBa(), NAGAI_UNIT_CODE) + " was not set properly.");
//		
//		Type unitType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), NAGAI_UNIT_CODE, unitCode);
//		
//		String corrCode = co.getAsString(NAGAI_CORR_CATEGORY);
//		if( null == corrCode )
//			throw new CorrException(Utility.fdn(co.getBa(), NAGAI_CORR_CATEGORY) + " was not set properly.");
//		
//		Type corrType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), NAGAI_CORR_CATEGORY, corrCode);
		
		return projectCode + "-" + co.getGenerationAgency().getDescription().trim() + "-" + getFinancialYear(new Date());
	}

	private static String getCorrCorrMaxIdName(CorrObject co) throws CorrException {
		Date date = new Date() ;
		// for both ION and non-ION .. and for both NCCB and NCCP
		if( co.getLoginUser().getFirmCode().equals(CorresConstants.CORR_ORIG_NCCB) ||
				co.getLoginUser().getFirmCode().equals(CorresConstants.CORR_ORIG_NCCP))
		{
			String projectCode = "NPT10109" ; // hard-coded			
			String financialYear = getFinancialYear(date) ; 
			String cp = projectCode + "-" + financialYear ;
			return cp ;
		}
		else if( co.getLoginUser().getFirmCode().equals(CorresConstants.CORR_ORIG_DCPL))
		{
			return CorresConstants.DCPL_CORRES_MAX_ID ;
		}
		else if (co.getLoginUser().getFirmCode().equals(CorresConstants.CORR_ORIG_KNPL))
		{
//			KNPL-NCCP-CR-Package-10-11-0001
//			For E.g:If Contract reference code is 1 then the number will be like
//			KNPL-NCCP-1-BOP-10-11-0001
			return co.getLoginUser().getFirmCode() + 
								"-" + co.getAsString(CorresConstants.CORR_CONTRACT_REFERENCE_FIELD_NAME) +
								"-" + co.getAsString(CorresConstants.CORR_PACKAGE_FIELD_NAME) + "-" + getFinancialYear(date);
		}
		else if (co.getLoginUser().getFirmCode().equals(CorresConstants.CORR_ORIG_DESEIN))
		{
			return CorresConstants.DESEIN_CORRES_MAX_ID ;
		}
		else
		{
			return getCORRCorrPrefix(co);
		}
	}

	public static String getCorrPrefix(CorrObject co ) throws CorrException
	{
		if( co.getBa().getSystemPrefix().equals(CorresConstants.CORR_SYSPREFIX))
		{
			return getCORRCorrPrefix(co);
		}
		else if( co.getBa().getSystemPrefix().equals(CorresConstants.NAGAI_CORR_SYSPREFIX))
		{
			return getNAGAICorrPrefix(co);
		}
		else
		{
			throw new CorrException("Correspondence number system is not configured for ba : " + co.getBa().getSystemPrefix());
		}
	}	
	
	private static String getNAGAICorrPrefix(CorrObject co) throws CorrException 
	{
		String genAgen = co.getUserMapUsers().get(0).getLocation();
		if( null == genAgen )
			throw new CorrException("The Location for user : " + co.getUserMapUsers().get(0).getUserLogin() + " is not defined.");
		
		try
		{
			if( genAgen.equals(CORR_ORIG_NCCP))
			{
				return getNCCPCorrPrefix(co);
			}
			else if( genAgen.equals(CORR_ORIG_ACBL))
			{
				return getACBLCorrPrefix(co);
			}
			else if (genAgen.equals(CORR_ORIG_TPSC))
			{
				return getTPSCCorrPrefix(co);
			}
			else if( genAgen.equals(CORR_ORIG_NPPL))
			{
				return getNPPLCorrPrefix(co);
			}
			else if( genAgen.equals(CORR_ORIG_AEC))
			{
				return getAECCorrPrefix(co);
			}
			if( genAgen.equals(CORR_ORIG_EDTD))
			{
				return getEDTDCorrPrefix(co);
			}
			else throw new CorrException("Correspondence number system is not configured for agency : " + genAgen);
		}
		catch(CorrException te)
		{
			throw te;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new CorrException(e.getMessage());
		}
	}

	private static String getEDTDCorrPrefix(CorrObject co) throws CorrException {
		String projectCode = "NPT10110" ;
		String contractRef = co.getAsString(NAGAI_CONTRACT_REF);
		if( null == contractRef )
			throw new CorrException(Utility.fdn(co.getBa(), NAGAI_CONTRACT_REF) + " was not set properly.");
		
		Type contType = null;
		try {
			contType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), NAGAI_CONTRACT_REF, contractRef);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if( null == contType )
			throw new CorrException("Cannot find any value for " + NAGAI_CONTRACT_REF);
		
		String unitCode = co.getAsString(NAGAI_UNIT_CODE);
		if( null == unitCode )
			throw new CorrException(Utility.fdn(co.getBa(), NAGAI_UNIT_CODE) + " was not set properly.");
		
		Type unitType = null;
		try {
			unitType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), NAGAI_UNIT_CODE, unitCode);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if( null == unitType )
			throw new CorrException("Cannot find any value for : " + NAGAI_UNIT_CODE);
		String corrCode = co.getAsString(NAGAI_CORR_CATEGORY);
		if( null == corrCode )
			throw new CorrException(Utility.fdn(co.getBa(), NAGAI_CORR_CATEGORY) + " was not set properly.");
		
		Type corrType = null;
		try {
			corrType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), NAGAI_CORR_CATEGORY, corrCode);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if( null == corrType )
			throw new CorrException("Cannot find any value for : " + NAGAI_CORR_CATEGORY);
		
		return projectCode + "-" + contType.getDescription().trim() + "-" + unitType.getDescription().trim() + "-" + co.getGenerationAgency().getDescription().trim() + "-" + co.getRecepientAgency().getDescription().trim() + "-" + corrType.getDescription().trim() + "-" + getFinancialYear(new Date());
	}

	private static String getAECCorrPrefix(CorrObject co) {
		return "AEC-9209-KVK-G";
	}

	/**
	 * NPPL Numbering System
		Orig - Rece - CR - Package - YY - Serial No
		Eg: NPPL - NCCP - 1 - BOP - 0001
		Note  :  Number should change with change in Recepient , CR  & Package .
		i.e; NPPL-NCCP-1-BOP-0001
      NPPL-NCCP-1-BTG-0001
     NPPL-NCCP-1-CMN-0001
     NPPL-NCCP-2-BOP-0001
     NPPL-TPSC-1-BOP-0001
	 * @param co
	 * @return
	 * @throws CorrException 
	 * @throws DatabaseException 
	 */
	private static String getNPPLCorrPrefix(CorrObject co) throws CorrException, DatabaseException 
	{
		String contractRef = co.getAsString(NAGAI_CONTRACT_REF);
		if( null == contractRef )
			throw new CorrException(Utility.fdn(co.getBa(), NAGAI_CONTRACT_REF) + " was not set properly.");
		
		Type contType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), NAGAI_CONTRACT_REF, contractRef);
	
		String pack = co.getAsString(NAGAI_PACKAGE);
		if( null == pack )
			throw new CorrException(Utility.fdn(co.getBa(), NAGAI_PACKAGE) + " was not set properly.");
		
		Type packType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), NAGAI_PACKAGE, pack);
		
		return "NPPL" + "-" + co.getRecepientAgency().getDescription().trim() + "-" + contType.getDescription().trim() + "-" + packType.getDescription().trim() ;
	}

	/**
	 * TPSC (Toshiba Plant Systems & Services Corporation )
	ITNI1 - XXX -05 - XXXX
	CTNI1 - XXX -05 - XXXX
	ETNI1 - XXX -05 - XXXX
	MTNI1 - XXX -05 - XXXX
	JTNI1 - XXX -05 - XXXX
	I - C&I Department Code / C - Civil Department Code / E - Electrical l Department Code / M - Mechanical Department code / J - PJMT Department Code
	T - Thermal
	NI - Nagai Project Code
	1 - Phase 1
	XXX - Package Code
	05 - Email to Client / 06 -Email to Vendor / 07 - Email to TPSC
	XXXX - Serial Number
	 * @param co
	 * @return
	 * @throws CorrException 
	 * @throws DatabaseException 
	 */
	private static String getTPSCCorrPrefix(CorrObject co) throws CorrException, DatabaseException 
	{
		String deptCode = co.getAsString(NAGAI_DEPARTMENT_CODE);
		if( null == deptCode )
			throw new CorrException(Utility.fdn(co.getBa(),NAGAI_DEPARTMENT_CODE) + " cannot be empty.");
		
		Type deptType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), NAGAI_DEPARTMENT_CODE, deptCode);
		
		String packageCode = co.getAsString(NAGAI_PACKAGE_TYPE);
		if( null == packageCode )
			throw new CorrException(Utility.fdn(co.getBa(),NAGAI_PACKAGE_TYPE) + " cannot be empty.");
		
		Type packageType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), NAGAI_PACKAGE_TYPE, packageCode);
		
		return deptType.getDescription().trim() + "-" + "TNI1" + "-" + packageType.getDescription().trim() + "-" + "05" ;
	}

	/**
	 * Ansaldo Caldaie Boilers India Pvt Limited (ACBL)
		NNNN / Orig / Rece / Serial No
		NNNN - Year
		Originator  - Originator Code
		Recepient - Recepient Code
		Serial No : 0001
		Eg: 2010-ACBL-NCCP-0001
	 * @param co
	 * @return
	 */
	private static String getACBLCorrPrefix(CorrObject co) 
	{
		String yyyy = Timestamp.toCustomFormat(new Date(), "yyyy");
		
		return yyyy + "-" + co.getGenerationAgency().getDescription().trim() + "-" + co.getRecepientAgency().getDescription().trim() ;
	}

	
	/**
	 * NCCP Correspondence Numbering system
	Project Code - NPT10110
	Contract Reference - 0/1/2/3/4 ( 0-General, 1-Agreement for Supply, 2-Agreement for Civil Construction & Erection Works, 3-Agreement for Engineering-Testing-Commissioning Services, 4-NonEPC )
	Unit Code - ( 1 - Unit 1, 2 - Unit 2 , 0 - General)
	Originator - NCCP / TPSC / ACBL / NPPL / FTSI / GMMN
	Recepient - NCCP / TPSC / ACBL / NPPL / FTSI / GMMN
	Correspondence Code - ( LTR / EML / FAX / ION / MOM )
	Financial Year - 10-11
	Serial No : 0001
	Eg: NPT10110-1-0-NCCP-ACBL-EML-10-11-0001
	Note : The numbering system should change with change in Recepient & Correspondence Code
	i.e: NPT10110-1-0-NCCP-ACBL-EML-10-11-0001 (Type 1)
      NPT10110-1-0-NCCP-TPSC-EML-10-11-0001(Type 2 )
      NPT10110-1-0-NCCP-TPSC-LTR-10-11-0001 (Type 3 )
	 * @param co
	 * @return
	 * @throws CorrException 
	 * @throws DatabaseException 
	 */
	private static String getNCCPCorrPrefix(CorrObject co) throws CorrException, DatabaseException 
	{
		String projectCode = "NPT10110" ;
		String contractRef = co.getAsString(NAGAI_CONTRACT_REF);
		if( null == contractRef )
			throw new CorrException(Utility.fdn(co.getBa(), NAGAI_CONTRACT_REF) + " was not set properly.");
		
		Type contType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), NAGAI_CONTRACT_REF, contractRef);
		
		String unitCode = co.getAsString(NAGAI_UNIT_CODE);
		if( null == unitCode )
			throw new CorrException(Utility.fdn(co.getBa(), NAGAI_UNIT_CODE) + " was not set properly.");
		
		Type unitType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), NAGAI_UNIT_CODE, unitCode);
		
		String corrCode = co.getAsString(NAGAI_CORR_CATEGORY);
		if( null == corrCode )
			throw new CorrException(Utility.fdn(co.getBa(), NAGAI_CORR_CATEGORY) + " was not set properly.");
		
		Type corrType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), NAGAI_CORR_CATEGORY, corrCode);
		
		return projectCode + "-" + contType.getDescription().trim() + "-" + unitType.getDescription().trim() + "-" + co.getGenerationAgency().getDescription().trim() + "-" + co.getRecepientAgency().getDescription().trim() + "-" + corrType.getDescription().trim() + "-" + getFinancialYear(new Date());
	}

	private static String getCORRCorrPrefix(CorrObject co) {
		Date date = new Date();
		if( co.getLoginUser().getFirmCode().equals(CorresConstants.CORR_ORIG_KNPL))
		{
			return co.getLoginUser().getFirmCode() + "-" + co.getAsString(CorresConstants.CORR_RECEPIENT_FIELD_NAME) +
				   "-" + co.getAsString(CorresConstants.CORR_CONTRACT_REFERENCE_FIELD_NAME) +
				   "-" + co.getAsString(CorresConstants.CORR_PACKAGE_FIELD_NAME) + "-" + getFinancialYear(date);
		}else if( co.getLoginUser().getFirmCode().equals(CorresConstants.CORR_ORIG_DCPL))
		{
			return CorresConstants.DCPL_PROJECT_CONSTANT + "-" + co.getAsString(CorresConstants.CORR_DISCIPLINE_FIELD_NAME) ;
		}
		else if ( co.getLoginUser().getFirmCode().equals(CorresConstants.CORR_ORIG_DESEIN))
		{
			String prefix = CorresConstants.DESEIN_PROJECT_CONSTANT ;
			if( co.getLoginUser().getLocation().equals(CorresConstants.CORR_LOC_HYD))
				prefix += "-HYD" ;
			
			return  prefix ;
		}
		
		if(co.getAsString(CorresConstants.CORR_CORR_TYPE_FIELD_NAME).equals(CorresConstants.CORR_CORR_TYPE_ION))
		{
			String projectCode = "NPT10109" ; // hard-coded
			String contRefCode = co.getAsString(CorresConstants.CORR_CONTRACT_REFERENCE_FIELD_NAME) ;
			String financialYear = getFinancialYear(date) ;
			
			String cp = projectCode + "-" + contRefCode + "-" + co.getAsString(CorresConstants.CORR_RECEPIENT_FIELD_NAME) + "-" + co.getRecepientAgency().getName() +
						"-" + co.getAsString(CorresConstants.CORR_CORR_TYPE_FIELD_NAME) +"-"+ financialYear ;
			return cp ;
		}
		else
		{
			String projectCode = "NPT10109" ; // hard-coded
			String contRefCode = co.getAsString(CorresConstants.CORR_CONTRACT_REFERENCE_FIELD_NAME) ;
			String financialYear = getFinancialYear(date) ; 
			String cp = projectCode + "-" + contRefCode + "-" + co.getOriginator().getName() + "-" + co.getRecepientAgency().getName() +
						"-" + co.getAsString(CorresConstants.CORR_CORR_TYPE_FIELD_NAME) + "-" + financialYear ;
			return cp ;
		}
	}

	public static void main(String argv[])
	{
		
	}
}
