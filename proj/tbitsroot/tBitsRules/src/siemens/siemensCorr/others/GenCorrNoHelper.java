package siemensCorr.others;

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

import static siemensCorr.others.CorrConstants.*;
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
		{
			String corrProt = co.getAsString(CORR_CORR_PROTOCOL);
			if( null == corrProt )
				throw new CorrException("The field " + Utility.fdn(co.getBa(), CORR_CORR_PROTOCOL) + " was not set properly. It was set to : " + corrProt );
			
			return co.getBa().getSystemPrefix() + "-" + corrProt + "-max-id" ;
		}
		else
		{
			throw new CorrException("Max-id numbering not configured for this ba.");
		}	
	}


	public static String getCorrPrefix(CorrObject co ) throws CorrException
	{
		if( co.getBa().getSystemPrefix().equals(CorrConstants.CORR_SYSPREFIX))
		{
			return getCORRCorrPrefix(co);
		}
		else
		{
			throw new CorrException("Correspondence number system is not configured for ba : " + co.getBa().getSystemPrefix());
		}
	}

	private static String getCORRCorrPrefix(CorrObject co) throws CorrException 
	{
		String corrProt = co.getAsString(CORR_CORR_PROTOCOL);
		if( null == corrProt )
			throw new CorrException("The field " + Utility.fdn(co.getBa(), CORR_CORR_PROTOCOL) + " was not set properly. It was set to : " + corrProt );
		
		if( corrProt.equals(CORR_CORR_PROT_SLI))
			return getSLICorrPrefix(co);
		else if( corrProt.equals(CORR_CORR_PROT_SLI1))
			return getSLI1CorrPrefix(co);
		else throw new CorrException("Protocol was not configured to work with : " + Utility.tdn(co.getBa(), CORR_CORR_PROTOCOL, corrProt) + " value of field " + Utility.fdn(co.getBa(), CORR_CORR_PROTOCOL) );
	}

	/**
ETPS-PGCB-<Project ID>-<Initiator>-<N>-<CC>-<NNN>

ETPS - Constant
PGCB - Constant
Project ID - N-000030 [Constant]
Initiator - Should pick from description part of "CorrGenerationAgency" type field.
<N> - Should pick from description part of "FilingSystem" type field
<CC> - Should pick from description part of "PackageCode" type field
<NNN>-  Running Serial No
	 * @param co
	 * @return
	 * @throws CorrException 
	 */
	private static String getSLICorrPrefix(CorrObject co) throws CorrException 
	{
		String filingSystem = co.getAsString(CORR_FilingSystem);
		String packageCode = co.getAsString(CORR_PackageCode);
		
		if( null == filingSystem )
			throw new CorrException("null value in field : " + Utility.fdn(co.getBa(), CORR_FilingSystem));
		if( null == packageCode )
			throw new CorrException("null value in field : " + Utility.fdn(co.getBa(), CORR_PackageCode));
		
		Type filingType = null;
		try {
			filingType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), CORR_FilingSystem, filingSystem);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Type packType = null ;
		try {
			packType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), CORR_PackageCode, packageCode);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		if( null == filingType )
			throw new CorrException("Illegal value in field : " + Utility.fdn(co.getBa(), CORR_FilingSystem));
		if( null == packType )
			throw new CorrException("Illegal value in field : " + Utility.fdn(co.getBa(), CORR_PackageCode));
		
		if( null == co.getGenerationAgency() )
			throw new CorrException("null value in Generation Agency Field.");
		
		return "ETPS-PGCB-N-000030-" + co.getGenerationAgency().getDescription()  + "-" + filingType.getDescription() + "-" + packType.getDescription() ;  
	}

	/**
	 * ETPS/PGCB/<Project Name or PO No.>/<initials>/<Subcontractor>_<N>_<CC>

ETPS - Constant
PGCB - Constant
<Project Name> - Should pick from description part of "ProjectName" type field.
<Initiator> - Should pick from description part of "CorrGenerationAgency" type field.
<Subcontractor>-Should pick from description part of "RecepientAgency" type field.
<N> - Should pick from description part of "FilingSystem" type field
<CC> - Should pick from description part of "PackageCode" type field
<NNN>-  Running Serial No

	 * @param co
	 * @return
	 * @throws CorrException 
	 */
	private static String getSLI1CorrPrefix(CorrObject co) throws CorrException 
	{
		String projectName = co.getAsString(CORR_ProjectName);
		if( null == projectName )
			throw new CorrException("null not allowed in field : " + Utility.fdn(co.getBa(), CORR_ProjectName));
		
		Type projectNameType = null;
		try {
			 projectNameType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), CORR_ProjectName, projectName);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if( null == projectNameType )
			throw new CorrException("Illegal value in field : " + Utility.fdn(co.getBa(), CORR_ProjectName) + " : value = " + projectName);
		
		String subContractor = co.getAsString(CORR_Subcontractor);
		if( null == subContractor )
			throw new CorrException("null not allowed in field : " + Utility.fdn(co.getBa(), CORR_Subcontractor));
		
		String filingSystem = co.getAsString(CORR_FilingSystem);
		String packageCode = co.getAsString(CORR_PackageCode);
		
		if( null == filingSystem )
			throw new CorrException("null value in field : " + Utility.fdn(co.getBa(), CORR_FilingSystem));
		if( null == packageCode )
			throw new CorrException("null value in field : " + Utility.fdn(co.getBa(), CORR_PackageCode));
		
		Type filingType = null;
		try {
			filingType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), CORR_FilingSystem, filingSystem);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Type packType = null ;
		try {
			packType = Type.lookupBySystemIdAndFieldNameAndTypeName(co.getBa().getSystemId(), CORR_PackageCode, packageCode);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		if( null == filingType )
			throw new CorrException("Illegal value in field : " + Utility.fdn(co.getBa(), CORR_FilingSystem));
		if( null == packType )
			throw new CorrException("Illegal value in field : " + Utility.fdn(co.getBa(), CORR_PackageCode));
		
		if( null == co.getGenerationAgency() )
			throw new CorrException("null value in Generation Agency Field.");
		if( null == co.getRecepientAgency() )
			throw new CorrException("null value not allowed in Recepient Agency.");
		
		return "ETPS-PGCB-" + projectNameType.getDescription() + "-" + co.getGenerationAgency().getDescription() + "-" + co.getRecepientAgency().getDescription() + "-" + filingType.getDescription() + "-" + packType.getDescription() ;
		
	}	
}
