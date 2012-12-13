package impCorr.others;

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
import corrGeneric.com.tbitsGlobal.server.managers.ProtocolOptionsManager;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class GenCorrNoHelper 
{
	public static TBitsLogger LOG = TBitsLogger.getLogger("impCorr");
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
				throw new CorrException("Please remove any number in field " + Utility.fdn(co.getBa(),corrFieldName) + " as it will be generated by system.");
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
		return getDefaultCorrMaxIdName(co);
	}

	public static String getMaxIdNameForPrefix(String sysPrefix,String corrPrefix) throws CorrException
	{
		return getDefaultNameForPrefix(corrPrefix);
	}
	
	private static String getDefaultNameForPrefix(String corrPrefix) 
	{
		// CORRNO : corr_no_prefix - recepeintAgency.description - "Corr" -
		// MAXIDNAME : corr_no_prefix - recepeintAgency.description - "Corr"
		String[] parts = corrPrefix.split("-");
		Integer[] indexes = { 0,1,2 } ;
		String maxName = joinParts(parts,indexes);
		LOG.info("For corrNumber : " + corrPrefix + " : the MaxIdName is : " + maxName);
		return maxName ;
	}

	public static String getCorrPrefix(CorrObject co ) throws CorrException
	{
		return getDefaultCorrPrefix(co);
	}

	public static String getDefaultCorrPrefix(CorrObject co ) throws CorrException
	{
		ProtocolOptionEntry prefixPOE = ProtocolOptionsManager.lookupProtocolEntry(co.getBa().getSystemPrefix(), Constants.NumberPrefix);
		if( null == prefixPOE || null == prefixPOE.getValue() )
			throw new CorrException("Correspondence was not properly configured for protocol option " + Constants.NumberPrefix + " for ba : " + co.getBa().getSystemPrefix());
		
		// corr_no_prefix - recepeintAgency.description - "Corr" -
		return prefixPOE.getValue().trim() + "-" + co.getRecepientAgency().getDescription().trim() + "-" + "Corr" + "-" ;
	}
	
	public static String getDefaultCorrMaxIdName(CorrObject co) throws CorrException
	{
		ProtocolOptionEntry prefixPOE = ProtocolOptionsManager.lookupProtocolEntry(co.getBa().getSystemPrefix(), Constants.NumberPrefix);
		if( null == prefixPOE || null == prefixPOE.getValue() )
			throw new CorrException("Correspondence was not properly configured for protocol option " + Constants.NumberPrefix + " for ba : " + co.getBa().getSystemPrefix());
		
		// corr_no_prefix - recepeintAgency.description - "Corr"
		return prefixPOE.getValue().trim() + "-" + co.getRecepientAgency().getDescription().trim() + "-" + "Corr" ;
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
	
	public static String getFinancialYear(Date d) 
	{
		Calendar ndd = Calendar.getInstance() ;
		ndd.setTime(d);
		int currMonth = ndd.get(Calendar.MONTH) ;
		if( currMonth < Calendar.APRIL )
		{
			Calendar other = Calendar.getInstance(); 
			other.add(Calendar.YEAR, -1);
			return Timestamp.toCustomFormat(other.getTime(),"yyyy") + "-" + Timestamp.toCustomFormat(ndd.getTime(),"yy") ;
		}
		else
		{
			Calendar other = Calendar.getInstance(); 
			other.add(Calendar.YEAR, 1);
			return Timestamp.toCustomFormat(ndd.getTime(),"yyyy") + "-" + Timestamp.toCustomFormat(other.getTime(),"yy") ;
		}		
	}
}
