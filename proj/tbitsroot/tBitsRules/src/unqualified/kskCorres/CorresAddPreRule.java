package kskCorres;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.regex.Pattern;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Uploader;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

import static kskCorres.KskConstants.* ;

public class CorresAddPreRule implements IRule 
{
	private static final String ILL_YEAR = "Year field not properly set.";
	private static final String DO_NOT_CONFORM = "Correspondance number do not conform to the Filed values.";
	private static final String NUM_EXPECTED = "Number Expected in the last token";
	private static final String ILL_FORMAT = "Illegal format of correspondence number.";
	public static final String ERR_CORR_GEN = "Exception occured : Correspondance file cannot be generated" +
						"<br> Please uncheck the generate correspondance checkbox.";
	public static final TBitsLogger LOG = TBitsLogger.getLogger("kskCorres");
		
	public int getNextCorrNo(Connection con, String corrCat ) throws SQLException
	{
	System.out.println("generating corr. no. for : " + corrCat );
		try {	
			CallableStatement stmt = con.prepareCall("stp_getAndIncrMaxId ?");
			stmt.setString(1, corrCat );
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("max_id");
				return id;
			} else {
				throw new SQLException();
			}
		} catch (SQLException e) {
			throw e;
		}		
	}
	
	String getCorrNo(Connection con, Hashtable<String,String> logger0Info, Type corrType ) throws SQLException, TBitsException
	{		
		String corresNo = "KMP-" ;
		String firmLetter = "?" ;
		if( logger0Info.get(UserInfoManager.FIRM).toUpperCase().equalsIgnoreCase(WPCL_FIRM_NAME) )
			firmLetter = "K";
		else if( logger0Info.get(UserInfoManager.FIRM).toUpperCase().equalsIgnoreCase(SEPCO_FIRM_NAME) )
			firmLetter = "S" ;
		else
			throw new TBitsException("Illegal value for firm_code for the user for the logger." );
		
		corresNo += firmLetter + "-" + logger0Info.get(UserInfoManager.LOCATION) + "-" ;		
		corresNo += corrType.getName() ;
		String yr = new Timestamp().toCustomFormat("yy") ;
		corresNo += "-" + yr ;
		// generate complete correspondence no.
		int ncid = getNextCorrNo(con, corresNo ) ;
		String nextCorresId =  Integer.toString(ncid);  //Integer.toString(fc.getMaxRequestId() + 1);
		switch( nextCorresId.length() )
		{
			case 1 : nextCorresId = "000" + nextCorresId ; break ;
			case 2 : nextCorresId = "00" + nextCorresId ; break ;
			case 3 : nextCorresId = "0" + nextCorresId ; break ;
		}	
		// get year 		
		corresNo += "-" + nextCorresId ;
		
		return corresNo ;
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
	/*
	 * formats : KMP-[SK]-[two digit location]
	 */	
	VerificationResult sanitizeAndValidateCorrNo(String iCorrNo, Hashtable<String,String> logger0Info, Type corrType, Connection con ) throws TBitsException
	{
		
		iCorrNo =  replaceStrangeDash(iCorrNo) ;//iCorrNo.replaceAll(regex,replacement) ; // replace all strange dash
		iCorrNo = iCorrNo.toUpperCase() ; // change every-thing to uppercase
		iCorrNo = removeRedundantDashes(iCorrNo) ;
		// now this has been properly formated .. if it still contains any other special character we should
		// remove it
		iCorrNo = keepOnlyAllowedCharacters(iCorrNo) ;
		if( iCorrNo.length() == 0  )
			return new VerificationResult(false, "") ;
				
		// checking format
//		int count = 0 ; // no. of dashes
//		for(int i = 0 ; i < iCorrNo.length() ; i++ )
//		{
//			if( '-' == iCorrNo.charAt(i))
//				count++ ;
//		}
		
		String[] tokens = iCorrNo.split("-") ;
		String prefix = GenCorresHelper.getCorrPrefix(logger0Info, corrType) ;
		int num = 0 ;
		String yr = new Timestamp().toCustomFormat("yy") ;
		prefix += "-" + yr ;
		String myYear = null ;
		switch( tokens.length )
		{
			case 0 : 
				return new VerificationResult(false, ILL_FORMAT) ;
			case 1 : // number-only e.g. 1, 01 ,001, 0001 ... 
//				iCorrNo = removeFrontal0s(iCorrNo) ;
//				if( iCorrNo.length() == 0 )
//					return false ;
				try
				{
					num = Integer.parseInt(iCorrNo) ;
				}
				catch(Exception e )
				{
					e.printStackTrace() ;
					return new VerificationResult(false, NUM_EXPECTED) ;
				}
//				prefix = getPrefix() ;
				break ;
			case 2 : // 
				return new VerificationResult(false, ILL_FORMAT) ;
			case 3 : //
				// special exceptional case just for import purposes 
				// format = [SKsk]-[0-9]?[0-9]-[0-9]* i.e. no corr. Type info
				// no verification done in this case ... just pass this value on after
				Pattern ex = Pattern.compile("[SK]-[0-9]?[0-9]-[0-9]*") ;
				if( !ex.matcher(iCorrNo).matches() )
				{
					return new VerificationResult(false, ILL_FORMAT) ;
				}
				else
				{
					String tempx = tokens[0] +"-" + makeXDigit(tokens[1],2) +"-"+ makeXDigit(tokens[2],4) ;
					return new VerificationResult( true, -1 , tempx, "This is an exception case when I am allowing the illegal corr. nos. ") ;
				}				
				// return new VerificationResult(false, "Illegal format of correspondence number.") ;
			case 4 : // format = [SW]-loc-corr.type-num // [SW]-[0-9]?[0-9]-[A-Z][A-Z]-[0-9]*
				String temp4 = "KMP-" + tokens[0] + "-" + makeXDigit(tokens[1],2) + "-" + tokens[2] ;
				if( !prefix.equals(temp4) ) 
					return new VerificationResult(false, DO_NOT_CONFORM) ;				
				try
				{
					num = Integer.parseInt(tokens[3]) ;
				}
				catch(Exception e )
				{
					e.printStackTrace() ;
					return new VerificationResult(false, NUM_EXPECTED) ;
				}
				break ;
			case 5 : //  [SWPM]-[0-9]?[0-9]-[A-Z][A-Z]-[0-9]?[0-9]-[0-9]+
//				Pattern p = Pattern.compile("[SWPM]-[0-9]?[0-9]-[A-Z][A-Z]-[0-9]?[0-9]-[0-9]+") ;
				String temp = "KMP-" + tokens[0] + "-" + makeXDigit(tokens[1],2) + "-" +tokens[2] ;
				// check year 
				Pattern yp = Pattern.compile("[0-9]?[0-9]") ;
				if( ! yp.matcher(tokens[3]).matches() )
					return new VerificationResult(false, ILL_YEAR) ;
				myYear = makeXDigit(tokens[3],2) ;//tokens[3].length()==1 ? "0"+tokens[3] : tokens[3] ;
				temp += "-" + myYear ;
				if( !prefix.equals(temp) )
					return new VerificationResult(false, DO_NOT_CONFORM) ;				
				try
				{
					num = Integer.parseInt(tokens[4]) ;
				}
				catch(Exception e )
				{
					e.printStackTrace() ;
					return new VerificationResult(false, NUM_EXPECTED) ;
				}
				break ;
			case 6 : 
				String temp1 = tokens[0] + "-" + tokens[1] + "-" + makeXDigit(tokens[2],2) + "-" + tokens[3] ; //iCorrNo.substring(0, iCorrNo.lastIndexOf('-'));
				
				Pattern yp1 = Pattern.compile("[0-9]?[0-9]") ;
				if( ! yp1.matcher(tokens[4]).matches() )
					return new VerificationResult(false, ILL_YEAR) ;
				myYear = makeXDigit( tokens[4], 2 ) ;// tokens[4].length()==1 ? "0"+tokens[4] : tokens[4] ;
				
				temp1 += "-" + myYear ;
				
				if( ! temp1.equals(prefix) )
					return new VerificationResult(false, DO_NOT_CONFORM) ;				
				try
				{
					num = Integer.parseInt(tokens[5]) ;
				}
				catch(Exception e )
				{
					e.printStackTrace() ;
					return new VerificationResult(false,NUM_EXPECTED) ;
				}
				break ;
				
			default : 
				return new VerificationResult(false, ILL_FORMAT ) ;				
		}
				
		VerificationResult vr = verifyAndIncrement( prefix, num, con ) ; 
		if(vr.success)
		{
			String nextCorresId =  Integer.toString(num);  //Integer.toString(fc.getMaxRequestId() + 1);
			nextCorresId = makeXDigit(nextCorresId,4) ;
//			switch( nextCorresId.length() )
//			{
//				case 1 : nextCorresId = "000" + nextCorresId ; break ;
//				case 2 : nextCorresId = "00" + nextCorresId ; break ;
//				case 3 : nextCorresId = "0" + nextCorresId ; break ;
//			}
			
			// get year 
//			if(myYear == null )
//				myYear = new Timestamp().toCustomFormat("yy") ;		
			
			vr.corrStr = prefix + "-" + nextCorresId ;
		}
		return vr ;
	}
	
	private String keepOnlyAllowedCharacters(String iCorrNo) 
	{
		if( null == iCorrNo )
			return "" ;
		
		// [^a-zA-Z0-9-] should be removed by ""
		iCorrNo = iCorrNo.replaceAll("[^a-zA-Z0-9-]", "") ;
		return iCorrNo ;
	}

	public String makeXDigit(String string, int x)
	{
		if(null == string)
			string = "" ;
		if( string.length() >= x ) // this is valid even if x is negative .. so no explicit check
			return string ;
		
		while( string.length() <  x )
			string = "0" + string ;
		
		return string ;
	}
	class VerificationResult
	{
		public boolean success ;
		public int max_id ; 
		public String corrStr ;
		String msg ;
		public VerificationResult( boolean suc, int id , String cs, String msg )
		{
			success = suc ;
			max_id = id ;
			corrStr = cs ;
			this.msg = msg ;
		}
		public VerificationResult( boolean suc, String msg ) 
		{
			success = suc ;
			corrStr = "" ;
			max_id = -1 ;
			this.msg = msg ;
			
		}
	}
	
	public VerificationResult verifyAndIncrement(String name, int value, Connection con ) 
	{
//		Connection con = null ;
		
		try
		{
//			con = DataSourcePool.getConnection() ;
//			con.setAutoCommit(false) ; // I think this is not required ?? is it ? 
			System.out.println("verifying and incrementing corr. no. for : " + name );
				
				CallableStatement stmt = con.prepareCall("stp_verifyAndIncrMaxId ?, ?");
				stmt.setString(1, name );
				stmt.setInt(2, value ) ;
//				stmt.set
				ResultSet rs = stmt.executeQuery();
				if (rs.next()) 
				{
					String result = rs.getString("result") ;
					int id = rs.getInt("max_id");
					Boolean res = Boolean.parseBoolean(result) ;

					String msg = "" ;
					if( res == false )
					{
						msg = "The max value of running correspondence number should NOT be greater than " + id ;
					}
					return new VerificationResult( res.booleanValue(), id , name, msg )  ;
				} else {

					return new VerificationResult( false, -1, null , "No result found.") ;
				}
			
		}catch (Exception e) {
			return new VerificationResult( false, -1 , null, "Exception occurred.") ;
		}		
	}
	// TODO :write a prerule to validate the input-request so that proper report can be generated in this post rule
	/**
	 * STEPS : for ba = "CORR" and when generate correspondence is true ( both for add request and update request )  
	 * 1. generate the correspondence no.
	 * 2. generate the correspondence file 
	 * 3. rename the correspondence file 
	 * 4. attach the correspondence file 
	 */
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{	
		long start = System.currentTimeMillis() ;
		if( (null == ba) || (ba.getSystemPrefix() == null) )  
			return new RuleResult( false , "The supplied ba was null.", true ) ;
		
		if( !ba.getSystemPrefix().equals(KskConstants.CORR_SYSPREFIX) ) 
			return new RuleResult( true, "Skipping : " + getName() + ", because the ba is not : " + KskConstants.CORR_SYSPREFIX , true ) ;
				
		// test if the correpondance protocol is WPCL-SEPCO ? yes : check permission ; no : go ahead		
			User myLoginUser = null ;
			User myLogger0User = null ;
			User myAss0User = null ;
			
			Hashtable<String,String> myLoginInfo = null ;
			Hashtable<String,String> myLogger0Info = null ;	
			Hashtable<String,String> myAss0Info = null ;
			
			Hashtable<Integer,ArrayList<String>> myLogger0Map = null ;	
			
			Type myCorrProt = null ;
			Type myCorrType = null ;
			Type myCorrInit = null ;
			Type myPrevCorrProt = null ;
			Type myPrevCorrType = null ;
			Type myPrevCorrInit = null ;
			
			Request myCurrRequest = null ;	
			Request myPrevRequest = null ;
			BusinessArea myBA = null ; 
//			boolean isAddRequest ;
			
			String myLoggerList = null ;
			String mySubList = null ;
			String myAssList = null ;
			ArrayList<String> mySubArray = null ;
			
			String myLogger0 = null ;
			String myAss0 = null ;
			
			myLoginUser = user ;
			myCurrRequest = currentRequest ;
			myPrevRequest = oldRequest ;
			myBA = ba ;
			
			try
			{	
				try
				{
					myLoginInfo = UserInfoManager.getUserInfo(myLoginUser.getUserId()) ;
				}catch(Exception e )
				{
					throw new TBitsException( "No user info found for (" + myLoginUser.getUserLogin() + ")" ) ;
				}
				

				String myLoginInfoFirm = ( myLoginInfo.get(UserInfoManager.FIRM ) != null ? myLoginInfo.get(UserInfoManager.FIRM ).toUpperCase().trim() : "" ) ;
				if( !myLoginInfoFirm.equalsIgnoreCase(WPCL_FIRM_NAME) && !myLoginInfoFirm.equalsIgnoreCase(SEPCO_FIRM_NAME) )
					throw new TBitsException("You ( " + myLoginUser.getUserLogin() + " ) are not allowed to create correspondence.") ;				
				
				// if it is an email reply then set the GenerateCorrespondence to false		
				try
				{
					Boolean genCor = currentRequest.getExBoolean(KskConstants.CORR_GENERATE_CORRESPONDANCE_FIELD_NAME) ;
					
					if( TBitsConstants.SOURCE_EMAIL == Source )
					{
						// put the genCor to false
						genCor = new Boolean(false) ;
						LOG.info("Setting the generate correspondence to false as the request is from email.");
						Field field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), KskConstants.CORR_GENERATE_CORRESPONDANCE_FIELD_NAME);
						if( null != field )
						{
							currentRequest.setObject(field, false);
						}
					}
					
					if( null == genCor || !genCor  )
						return new RuleResult(true, "Skipping : " + getName()  + ", because the " +  KskConstants.CORR_GENERATE_CORRESPONDANCE_FIELD_NAME + " field is not true." , true ) ;
				}
				catch(Exception e )
				{
					e.printStackTrace() ;
					return new RuleResult(false, "Cannot Continue : " + getName() + " : because exception occured while accesing the field : " + KskConstants.CORR_GENERATE_CORRESPONDANCE_FIELD_NAME , true ) ;
				}
				
				myLoggerList = (String) myCurrRequest.get(KskConstants.CORR_LOGGER_FIELD_NAME) ;
				
				ArrayList<String> loggerArray = Utilities.toArrayList(myLoggerList) ;
				if( null == myLoggerList || myLoggerList.trim().equals("") || loggerArray.size() == 0  )
					throw new TBitsException( "Logger field cannot be empty." ) ;
			
				if( loggerArray.size() > 1 )
					throw new TBitsException("More than one logger is not allowed.") ;
				
				myLogger0 = loggerArray.get(0).trim() ;
				if( myLogger0.equals(""))
					throw new TBitsException( "Illegal value in logger Field.") ;
				try {
					myLogger0User = User.lookupAllByUserLogin(myLogger0) ;
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException("Exception while accesing user(" + myLogger0 + ")" ) ;
				}
				if( null == myLogger0User )
					throw new TBitsException( "The user with userLogin = " + myLogger0 + " not found." ) ;
			//	myLogger0Id = myLogger0User.getUserId() ;
				
				myAssList = (String) myCurrRequest.get(KskConstants.CORR_ASSIGNEE_FIELD_NAME ) ;
				if(null == myAssList || myAssList.trim().equals(""))
					throw new TBitsException( "Assignee Field cannot be empty.") ;
				
				ArrayList<String> assArray = Utilities.toArrayList(myAssList) ;
				if( assArray.size() > 1 || assArray.size() == 0 )
					throw new TBitsException("There must be exactly one assignee.") ;
				
				myAss0 = assArray.get(0).trim() ;
				if( myAss0.equals("") ) 
					throw new TBitsException("Illegal value in Assignee field.") ;
				
				try {
					myAss0User = User.lookupAllByUserLogin(myAss0) ;
				} catch (DatabaseException e) {			
					e.printStackTrace();
					throw new TBitsException("Exception while accesing user(" + myAss0 + ")" ) ;
				}
				if( null == myAss0User )
					throw new TBitsException( "The user with userLogin = " + myAss0 + " not found." ) ;

				String corrProtName = (String) myCurrRequest.get(KskConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME) ;		
				try {
					myCorrProt = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), KskConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME, corrProtName ) ;
				} catch (DatabaseException e) {			
					e.printStackTrace();
					throw new TBitsException(e) ;
				}
				if( null == myCorrProt )
					throw new TBitsException("The Correspondance Protocol Field was not properly set.") ;
				
				String corrTypeName = (String) myCurrRequest.get(KskConstants.CORR_TYPE_FIELD_NAME) ;
				try {
					myCorrType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), KskConstants.CORR_TYPE_FIELD_NAME, corrTypeName ) ;
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException(e) ;
				}
				if( null == myCorrType )
					throw new TBitsException("The Correspondance Type Field was not properly set.") ;
				
				String corrInitName = (String) myCurrRequest.get(KskConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME) ;
				try {
					myCorrInit = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), KskConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME, corrInitName ) ;
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException(e) ;
				}
				if( null == myCorrInit )
					throw new TBitsException("The Correspondance initiator Field was not properly set.") ;
				
				if( false == isAddRequest )
				{
					String corrPrevProtName = (String) myPrevRequest.get(KskConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME) ;		
					try {
						myPrevCorrProt = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), KskConstants.CORR_CORRESPONDANCE_PROTOCOL_FIELD_NAME, corrPrevProtName ) ;
					} catch (DatabaseException e) {			
						e.printStackTrace();
						throw new TBitsException(e) ;
					}
					if( null == myPrevCorrProt )
						throw new TBitsException("The Correspondance Protocol Field was not properly set when the request was added.") ;
					
					String corrPrevTypeName = (String) myPrevRequest.get(KskConstants.CORR_TYPE_FIELD_NAME) ;
					try {
						myPrevCorrType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), KskConstants.CORR_TYPE_FIELD_NAME, corrPrevTypeName ) ;
					} catch (DatabaseException e) {
						e.printStackTrace();
						throw new TBitsException(e) ;
					}
					if( null == myPrevCorrType )
						throw new TBitsException("The Correspondance Type Field was not properly set when the request was added.") ;
					
					String corrPrevInitName = (String) myPrevRequest.get(KskConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME) ;
					try {
						myPrevCorrInit = Type.lookupAllBySystemIdAndFieldNameAndTypeName(myBA.getSystemId(), KskConstants.CORR_CORRESPONDANCE_INITIATOR_FIELD_NAME, corrPrevInitName ) ;
					} catch (DatabaseException e) {
						e.printStackTrace();
						throw new TBitsException(e) ;
					}
					if( null == myPrevCorrInit )
						throw new TBitsException("The Correspondance Initiator Field was not properly set when the request was added.") ;
				}				

				try
				{
					myLogger0Info = UserInfoManager.getUserInfo(myLogger0User.getUserId()) ;
				}catch(Exception e )
				{
					throw new TBitsException( "No user info found for (" + myLogger0User.getUserLogin() + ")" ) ;
				}

				String myLogger0InfoFirm = myLogger0Info.get(UserInfoManager.FIRM) ;
				if( null == myLogger0InfoFirm || null == myLoginInfoFirm )
				{
					throw new TBitsException("You (" + myLoginUser.getUserLogin() + ") are not allowed to create correspondence on behalf of (" + myLogger0User.getUserLogin() + ")" ) ;
				}
				else if( ! myLogger0InfoFirm.equalsIgnoreCase(myLoginInfoFirm) )
				{
					throw new TBitsException("You (" + myLoginUser.getUserLogin() + ") are not allowed to create correspondence on behalf of (" + myLogger0User.getUserLogin() + ")" ) ;
				}
				
				// corrInit type 
				if( isAddRequest )
				{
					String cin = myCorrInit.getName() ;
					String lfirm = myLogger0Info.get(UserInfoManager.FIRM) ;
					String lloc = myLogger0Info.get(UserInfoManager.LOCATION) ;
					if( !( cin.indexOf(lfirm) >= 0  && cin.substring(cin.trim().length()-2).equals(lloc.trim()) ) )
					{
						throw new TBitsException("Correpondance Initiator field was not correctly set.") ;
					}
				}
				
				try
				{
					myAss0Info = UserInfoManager.getUserInfo(myAss0User.getUserId()) ;
				}catch(Exception e )
				{
					throw new TBitsException( "No user info found for (" + myAss0User.getUserLogin() + ")" ) ;
				}
				
				mySubList = (String)myCurrRequest.get(KskConstants.CORR_SUBSCRIBER_FIELD_NAME) ;
				mySubArray = Utilities.toArrayList(mySubList, ",") ;
				
				if( myCorrProt.getName().equals(KskConstants.CORR_CORR_PROT_WCPL_SEPCO))
				{
					//checkCorrRestrictions() ;
					// check if assignee0 and subs list contains the minimum requirements
					myLogger0Map = UserMapManager.getMappingAsUserLogin(myLogger0User.getUserId(), myCorrType.getName() )	;
					ArrayList<String> toArray = myLogger0Map.get(UserMapManager.TO) ; 
					if(toArray.size() == 0 ) 
						throw new TBitsException("No assignee mapping found for " + myLogger0User.getUserLogin() ) ;
					
					String tass0 = toArray.get(0) ;
					if( !tass0.equals(myAss0User.getUserLogin()) )
						throw new TBitsException("Illegal assignee field.") ;
					
					ArrayList<String> yArray = myLogger0Map.get(UserMapManager.YOUR_CC) ;
					ArrayList<String> oArray = myLogger0Map.get(UserMapManager.OUR_CC) ;
					HashSet<String> subSet = new HashSet<String>( yArray ) ;
					subSet.addAll(oArray) ;
					
//					if(myLoginUser.getUserId() != myLogger0User.getUserId() )		
//						subSet.add(KskConstants.KSK_BOSS_LOGIN) ;		
					
					for( Iterator<String> iter = mySubArray.iterator() ; iter.hasNext() ; )
					{
						subSet.remove(iter.next()) ;
					}
					
					subSet.remove(myLogger0User.getUserLogin() ) ;
					subSet.remove(myAss0User.getUserLogin()) ;
					
					if( subSet.size() > 0 ) 
					{						
						// throw new TBitsException("The subscriber list must also contain : " + subSet.toString() ) ;
						// intead of sending error I will add these users to the subscriber's list

						Collection<RequestUser> reqUser = currentRequest.getSubscribers() ;
						int currentLength = reqUser.size() ;
						Field subField = Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), Field.SUBSCRIBER);
						for( Iterator<String> iter = subSet.iterator() ; iter.hasNext() ; )
						{
							String n = iter.next() ;
							User u = User.lookupAllByUserLogin(n) ;
//							public RequestUser(int aSystemId, int aRequestId, int aUserId, int aOrdering, boolean aIsPrimary,int aFieldId) {
							
							RequestUser ru = new RequestUser(currentRequest.getSystemId(),currentRequest.getRequestId(),u.getUserId(),++currentLength,false,subField.getFieldId()) ;
//							ru.setSystemId();
//					        ru.setRequestId(currentRequest.getRequestId());
//				            ru.setUserTypeId(UserType.SUBSCRIBER);
//				            ru.setUserId(u.getUserId());
//				            ru.setOrdering(++currentLength);
//				            ru.setIsPrimary(false);
				            reqUser.add(ru) ;			            

						}
						currentRequest.setSubscribers(reqUser) ;
					}
				}
				
				/* check if the corr. no. field has some value
				 * KMP Number: ([Ww][Cc][Gg][ ]*-[ ]*[SWPMswpm][^\t]+)
					SEPCO Number: ([Ss][ ]*-[ ]*[0-9]+[ ]*-[ ]*[0-9]+)
					
					Strange Dash: –
					
					1st Santization:
					
					* Replaces the strange dash.
					* Removes Spaces
					* ToUpperCase
					
					sed  's/–/-/g'|sed 's/ +//g'|/usr/bin/tr 'a-z' 'A-Z'
					
					2nd Santization: Correct the number of zeros such that the last digits are always 4 in number. e.g. 0001, 0100, 0069, 0123. 
				 */
				String iCorrNo = null ; 
				try {
					iCorrNo = currentRequest.getExString(KskConstants.CORR_CORRESPONDANCE_NUMBER_FIELD) ;
				} catch (IllegalStateException e2) {
					e2.printStackTrace();
					return new RuleResult(false, "Cannot Continue : " + getName() + " : because exception occured while accesing the field : " + KskConstants.CORR_CORRESPONDANCE_NUMBER_FIELD , true ) ;
				} catch (DatabaseException e2) {
					e2.printStackTrace();
					return new RuleResult(false, "Cannot Continue : " + getName() + " : because exception occured while accesing the field : " + KskConstants.CORR_CORRESPONDANCE_NUMBER_FIELD , true ) ;
				}
					
				boolean createCorresFile = true ;
				if( null != iCorrNo )
				{
					iCorrNo = iCorrNo.replaceAll("[ \t\n]", "" ) ; // remove all spaces
					if( ! iCorrNo.equalsIgnoreCase(""))
					{
						VerificationResult vres = sanitizeAndValidateCorrNo(iCorrNo, myLogger0Info, myCorrType, connection ) ;
						if( vres.success )			
						{
							// set the corr. no. to what was sent in the result
							// set the correspondance no. 
//							RequestEx corrNoRE = extendedFields.get(Field.lookupBySystemIdAndFieldName(ba.getSystemId(), )) ;
//							corrNoRE.setVarcharValue(vres.corrStr) ;
							currentRequest.setObject(KskConstants.CORR_CORRESPONDANCE_NUMBER_FIELD, vres.corrStr);
							createCorresFile = false ; 
//							return new RuleResult( true, "Skipping the generation of correspondance as the correpondence number field was set and correct.", true ) ;
						}
						else
						{
							return new RuleResult(false, "Illegal value of correspondence number." + (vres.msg!=null?(vres.msg+"<br />"):"") + "It should be " + GenCorresHelper.getExpectedCorrNo(myLogger0Info, myCorrType), false )  ;
						}
					}
					// else go ahead and create the correspondence and correspondence no.
				}
				
			if(createCorresFile )
			{
				Field fileField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), KskConstants.CORR_CORRESPONDANCE_FILE_FIELD_NAME) ;
				
//				RequestEx fileRequestex = extendedFields.get(fileField) ;
				String curratt = currentRequest.get(fileField.getName());//fileRequestex.getTextValue() ;
				System.out.println("Pre rule : curratt : " + curratt );
				
				ArrayList<String> attListPresent = new ArrayList<String>() ;
				
				if( !isAddRequest )
				{
					String oldatt = oldRequest.getExString(KskConstants.CORR_CORRESPONDANCE_FILE_FIELD_NAME) ;
					System.out.println("Pre rule : oldatt : " + oldatt );
					attListPresent = GenCorresHelper.getAttachListForRequest( curratt, oldatt ) ;
				}
				else
				{
					attListPresent = GenCorresHelper.getAttachListForRequest( curratt , null ) ;
				}
				
				System.out.println("Prerule : my corrAttach list : " + attListPresent );
				if( attListPresent.size() > 0 )
				{
					String attachList = "<br />" ;
					for( Iterator<String> i = attListPresent.iterator() ; i.hasNext() ; )
						attachList += i.next() + "<br />" ;
					
					throw new TBitsException("You are not allowed to change files in Correspondance File attachment field.\n Please upload files in Other Attachments" ) ;
				}
			    // for both add-request and update-request		
				// now generate the correspondence file 
				// get all js parameters
				Hashtable<String, String> params = new Hashtable<String,String>() ;
				long preProcessEnd = System.currentTimeMillis() ;
				LOG.info("Stress : protocolVerification time : " + (preProcessEnd-start) + " ms");				
				String corresNo = getCorrNo(connection, myLogger0Info, myCorrType ) ;
				long timeCorrNo = System.currentTimeMillis() ;
				LOG.info("Stress : getCorrNo time : " + (timeCorrNo - preProcessEnd) + " ms");
				params.put(KskConstants.REP_CURRENTCORRESPONDENCE_NO, corresNo ) ; 
	
				params.put(KskConstants.REP_TO, GenCorresHelper.getTo(myAss0Info, myLogger0Info, myCorrProt) ) ;
				params.put(KskConstants.REP_KIND_ATT, GenCorresHelper.getKindAtt(myAss0User , myAss0Info ) ) ;
	//			String dear = "Dear " + ( assignee_sex.trim().equalsIgnoreCase("M") ? "Sir," : "Madam," );
				params.put(KskConstants.REP_DEAR, GenCorresHelper.getDear(myAss0Info) ) ;
				// TODO : assumption : the address is in html format i.e. using <br /> instead of endline
				
				params.put(KskConstants.REP_TITLE, currentRequest.get(Field.SUBJECT)) ;
				params.put(KskConstants.REP_DESCRIPTION, currentRequest.get(Field.DESCRIPTION)) ;			
				params.put(KskConstants.REP_COMPANY, GenCorresHelper.getCompany( myLogger0Info, myAss0Info, myCorrProt ) ) ;
				params.put(KskConstants.REP_LOGGER, GenCorresHelper.getLogger( myLogger0User ) ) ;
				params.put(KskConstants.REP_DESIGNATION, GenCorresHelper.getDesignation( myLogger0Info ) ) ;			
				// creating cc_list 
				String ccs = GenCorresHelper.getCCs(currentRequest.get(KskConstants.CORR_SUBSCRIBER_FIELD_NAME)) ;
				params.put(KskConstants.REP_CC, ccs ) ;
				ArrayList<String> attList = new ArrayList<String>() ;
				
				if( !isAddRequest )
					attList = GenCorresHelper.getAttachListForRequest( currentRequest.get(Field.ATTACHMENTS), oldRequest.get(Field.ATTACHMENTS) ) ;
				else
					attList = GenCorresHelper.getAttachListForRequest( currentRequest.get(Field.ATTACHMENTS), null ) ;
				
				String attachList = "<br />" ; 
				for( Iterator<String> i = attList.iterator() ; i.hasNext() ; )
					attachList += i.next() + "<br />" ;
				
				params.put( KskConstants.REP_ATTACHMENT, attachList ) ;
				
				String imageName = myLogger0User.getUserLogin() + ".gif" ;
				System.out.println( "imagename = " + imageName ) ;
				File imageFile = Configuration.findPath("tbitsreports/" + imageName);			
				if( imageFile != null )
				{
					String imageLocation = imageFile.getAbsolutePath() ;
					params.put(KskConstants.REP_IMAGE_PATH, imageLocation ) ;
				}
				
				String currentDate = new Timestamp().toCustomFormat("yyyy-MM-dd") ;
				params.put(KskConstants.REP_CURRENT_DATE, currentDate) ;
	
				String loggerFirm = myLogger0Info.get(UserInfoManager.FIRM) ;
				String reportName = "CorrespondanceTemplate.rptdesign" ;
	            reportName = loggerFirm.trim().toUpperCase() + reportName ;		
				// TODO :set report parameters
				HashMap<String,String> reportParamMap = new HashMap<String,String>() ;
				String rid = Integer.toString(currentRequest.getRequestId()) ;
				reportParamMap.put(KskConstants.REP_RID, rid  ) ;			
				String tbits_base_url = WebUtil.getNearestPath("") ; // PropertiesHandler.getProperty(TBitsPropEnum.KEY_NEAREST_INSTANCE)
				System.out.println( "tbits_base_url : " + tbits_base_url ) ;
				reportParamMap.put(KskConstants.REP_TBITS_BASE_URL_KEY, tbits_base_url );
	
				String format = "pdf";
				long beforePDF = System.currentTimeMillis() ;				
				File pdfFile = GenCorresHelper.generateReport( reportName, params, reportParamMap, format ) ;
				long createPDF = System.currentTimeMillis();
				LOG.info("Stress : BirtCreatingPdf : " + ( createPDF - beforePDF ) + " ms");
		
				////////////////////////////////////
				if( pdfFile == null ) 
				{						
					return new RuleResult( false , "Cannot Generate the Correspondance File.", false ) ;					
				}
				else
				{				
					// upload this file
					// generate display name this file
					String displayName = new Timestamp().toCustomFormat("yyyyMMdd") + "_" + corresNo + ".pdf";
										
					int requestId = currentRequest.getRequestId() ;
					int actionId = currentRequest.getMaxActionId() ;
					String prefix = ba.getSystemPrefix() ;
					long beforeUpload = System.currentTimeMillis() ;
					Uploader up = new Uploader( requestId, actionId, prefix ) ;
					AttachmentInfo atinfo = up.moveIntoRepository(pdfFile) ;
					// change display name 
					long afterUpload = System.currentTimeMillis() ;
					LOG.info("Stress : time to move corrFile into repo" + ( afterUpload - beforeUpload ) + " ms");
					atinfo.name = displayName ;
	
					Field field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), KskConstants.CORR_CORRESPONDANCE_FILE_FIELD_NAME) ;
					// TODO: check field null 
					ArrayList<AttachmentInfo> attachArray = new ArrayList<AttachmentInfo>() ;// arrayList is a Collection
					attachArray.add(atinfo) ;
					currentRequest.setObject(field, attachArray);
					// set the correspondance no. 
					currentRequest.setObject(KskConstants.CORR_CORRESPONDANCE_NUMBER_FIELD, corresNo);
//					RequestEx corrNoRE = extendedFields.get(Field.lookupBySystemIdAndFieldName(ba.getSystemId(), )) ;
//					corrNoRE.setVarcharValue(corresNo) ;
				}				
			}
			
			// update the due-date
			Calendar ndd = Calendar.getInstance() ;
			if( isAddRequest )
			{
				ndd.add(Calendar.DAY_OF_MONTH, KskConstants.DUEDATE_SLIDE_ADDREQUEST) ;						
			}
			else
			{
				ndd.add(Calendar.DAY_OF_MONTH, KskConstants.DUEDATE_SLIDE_UPDATEREQUEST) ;
			}
			
			Timestamp nts = new Timestamp( ndd.getTimeInMillis() ) ;
			currentRequest.setDueDate(nts) ;
			
			long end = System.currentTimeMillis() ;
			LOG.info("Stress : KskCorres PreRule : " + (end-start) + " ms");
			
			return new RuleResult(true, "Rule executed successfully.", true ) ;

		}catch( TBitsException e )
		{
			e.printStackTrace() ;
			return new RuleResult( false , e.getDescription() , false ) ;
		}
		catch( Exception e )
		{
			e.printStackTrace() ;
			return new RuleResult( false , e.getMessage() , false ) ;
		}		
	}
	

	
	public String getName() 
	{
		return getClass().getName() + " : Prerule for creating and adding the correspondance file.";
	}

	public double getSequence() 
	{
		return 0;
	}
	
	public static void main(String argv[] )
	{
		System.out.println("\n"+new CorresAddPreRule().makeXDigit("10111", 4));
	}

}
