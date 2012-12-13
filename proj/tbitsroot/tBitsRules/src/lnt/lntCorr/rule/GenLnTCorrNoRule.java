package lntCorr.rule;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import lntCorr.others.LnTConst;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.managers.ProtocolOptionsManager;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class GenLnTCorrNoRule implements IRule
{
	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");
	public static String getLnTCorrNo(CorrObject co, Connection con) throws CorrException 
	{
		String corrNo = null;
		corrNo = getRealCorrNo(co,con) ;
		if( null == corrNo )
			throw new CorrException("Excetion occured while generating correspondence number.");
		return corrNo ;
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

	public static String getCorrPrefix(CorrObject co ) throws CorrException
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

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		if( Source == TBitsConstants.SOURCE_EMAIL )
			return new RuleResult(true,"Rule not applicable for request from email.", true);
		
		try
		{
			ArrayList<String> validBAs = new ArrayList<String>();
			
			validBAs.add("kdi_corr");
			validBAs.add("Malwa_Corr");
			validBAs.add("APPDCL_Corr");
			validBAs.add("VPGL2_Corr");
			validBAs.add("JPN_Corr");
			
			if( null == ba || null == ba.getSystemPrefix() || !validBAs.contains(ba.getSystemPrefix()))
				return new RuleResult(true,"Rule not applicable.", true	);
			
			CorrObject coob = new CorrObject(currentRequest, oldRequest);
			
			FieldNameEntry genCorrFne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.GenerateCorrespondenceFieldName);
			
			if( genCorrFne == null || genCorrFne.getBaFieldName() == null )
			{
				LOG.info("No Mapping found for " + GenericParams.GenerateCorrespondenceFieldName + " in the ba : " + ba.getSystemPrefix() + ". Hence not considering it as a part of corr. protocol.");
				return new RuleResult(true,"Rule executed successfully for request : " + currentRequest, true);			
			}
			
			String value = currentRequest.get(genCorrFne.getBaFieldName());
			if ( value != null && !value.equals( GenericParams.GenerateCorr_NoPdforCorrNumber ) )
			{
				String lntCorrNo = getLnTCorrNo(coob, connection);
				currentRequest.setObject(LnTConst.LnTNumberFieldName, lntCorrNo);
			}
			
			return new RuleResult(true,"Rule executed successfully.",true);
		}
		catch(Exception te)
		{
			LOG.error(TBitsLogger.getStackTrace(te));
			return new RuleResult(false,te.getMessage(),false);
		}
		
	}

	public String getName() {
		return "Returns and sets the LnT Correspondence number.";
	}

	public double getSequence() {
		return 10;
	}
}
