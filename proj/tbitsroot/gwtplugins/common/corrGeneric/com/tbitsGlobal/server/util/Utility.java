package corrGeneric.com.tbitsGlobal.server.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import corrGeneric.com.tbitsGlobal.client.utils.ClientUtility;
import corrGeneric.com.tbitsGlobal.shared.domain.BaFieldEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.CorrNumberEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.OnBehalfEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ReportEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ReportNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ReportParamEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.UserMapEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class Utility 
{

	public static ArrayList<String> splitToArrayList(String str, String separator)
	{
		return ClientUtility.splitToArrayList(str, separator);
	}
	
	public static ArrayList<String> splitToArrayList(String str )
	{
		return ClientUtility.splitToArrayList(str,",");
	}
	public static ArrayList<User> getUsersFromRequestUser(Collection<RequestUser> rus) throws DatabaseException 
	{
		if( null == rus )
			return null;
		
		ArrayList<User> users = new ArrayList<User>(rus.size());
		for( RequestUser ru : rus )
		{
			User u = User.lookupByUserId(ru.getUserId());
			if( null != u )
				users.add(u);
		}
		
		return users;
	}
	
	public static String tdn( String sysPrefix, String fieldName, String typeName)
	{
		if( null == sysPrefix )
			return "null";
		BusinessArea ba = null;
		try {
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		
		return tdn( ba,fieldName,typeName );
	}
	
	public static String tdn(BusinessArea ba, String fieldName, String typeName) 
	{
		if( null == ba || null == fieldName || null == typeName )
			return "null";
		
		Type type = null;
		try {
			type = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), fieldName, typeName);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		if( null == type )
			return "null" ;
		
		return type.getDisplayName();
	}

	public static String fdn( String sysPrefix , String fieldName )
	{
		try
		{
			if( null == sysPrefix || null == fieldName )
			{
				Utility.LOG.info("Either the sysPrefix Or the fieldName was null. Their supplied values are respectively : " + sysPrefix + "," + fieldName);
				return null;
			}
			
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			return fdn( ba, fieldName );
		} catch (Exception e) {
			Utility.LOG.info(TBitsLogger.getStackTrace(e));
			return null;
		}
	}

	public static String fdn(BusinessArea ba, String fieldName) 
	{
		try
		{
			if( null == ba || fieldName == null)
			{
				Utility.LOG.info("Either the ba or the field was null. Their supplied values are respectively : " + ba + "," + fieldName);
				return null;
			}
			Field f = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), fieldName);
			if(null == f)
			{
				Utility.LOG.info("The field with name : " + fieldName + " was not found in ba with sysPrefix : " + ba.getSystemPrefix());
				return null;
			}
			
			return f.getDisplayName();
		}
		catch(Exception e)
		{
			Utility.LOG.info(TBitsLogger.getStackTrace(e));
			return null;
		}
	}
	
	public static String toCSLoginList( Collection<User> users)
	{
		return toLoginList(users, ",");
	}
	
	public static String toLoginList( Collection<User> users, String separator )
	{
		if( null == users )
			return null;
		
		StringBuffer sb = new StringBuffer();
		
		boolean isFirst = true;
		for( User user : users )
		{
			if( isFirst )
			{
				isFirst = false;
				sb.append(user.getUserLogin());
			}
			else
			{
				sb.append(separator + user.getUserLogin());
			}
		}
		
		return sb.toString();
	}
	
	public static ArrayList<User> toUsers(String csUserList) throws CorrException
	{
		return toUsers(csUserList,",");
	}
	
	public static <G> Collection<G> getExtraInB(Collection<G> a, Collection<G> b)
	{
		if( null == a )
			return b ;
		
		ArrayList<G> notFound = new ArrayList<G>();
		
		if( null == b )
			return notFound;
		
		for( G g : b )
		{
			if( !a.contains(g))
				notFound.add(g);
		}
		
		return notFound;
	}
	
	public static ArrayList<User> toUsers(String userList, String separators) throws CorrException
	{
		if( null == userList )
			return null;
		
		String[] userLogins = userList.split(separators);
		ArrayList<User> users = new ArrayList<User>(userLogins.length);
		
		for( String ul : userLogins )
		{
			String userLog = ul.trim();
			if( userLog.equals(""))
				continue;
			User user = null;
			try {
				user = User.lookupByUserLogin(userLog);
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw new CorrException("Exception occured while accessing user with login : " + userLog);
			}
			
			if( null == user )
				throw new CorrException("Cannot find user with login : " + userLog);
			
			users.add(user);
		}
		
		return users;
	}

	public static <G> String getStringWithSeparator( Collection<G> things, String separator )
	{
		if( null == things )
			return null;
		
		boolean first = true;
		
		String str = "";
		for( G g : things )
		{
			if( first == true )
			{
				str += g.toString();
				first = false;
			}
			else
			{
				str += separator + g.toString();
			}
		}
		
		return str;
	}
	
	public static FieldNameEntry createFieldNameEntryFromResultSet(ResultSet rs) throws SQLException
	{
		long id = rs.getLong(FieldNameEntry.Id);
		String sysPrefix = rs.getString(corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry.SysPrefix);
		String corrFieldName = rs.getString(corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry.CorrFieldName);
		String baFieldName = rs.getString(corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry.FieldName);
		
		return new FieldNameEntry(id,corrFieldName, sysPrefix, baFieldName);
	}

	public static BaFieldEntry createBaFieldEntryFromResultSet(ResultSet rs) throws SQLException
	{
		long id = rs.getLong(BaFieldEntry.Id);
		String sysPrefix = rs.getString(BaFieldEntry.FromSysPrefix);
		String fieldName = rs.getString(BaFieldEntry.FromFieldName);
		String toSysPrefix = rs.getString(BaFieldEntry.ToSysPrefix);
		String toFieldName = rs.getString(BaFieldEntry.ToFieldName);
		
		return new BaFieldEntry(id,sysPrefix, fieldName, toSysPrefix, toFieldName);
	}

	public static OnBehalfEntry createOnBehalfEntryFromResultSet(ResultSet rs) throws SQLException
	{
		long id = rs.getLong(OnBehalfEntry.Id);
		String sysPrefix = rs.getString(corrGeneric.com.tbitsGlobal.shared.domain.OnBehalfEntry.SysPrefix);
		String userLogin = rs.getString(corrGeneric.com.tbitsGlobal.shared.domain.OnBehalfEntry.UserLogin);
		String type1 = rs.getString(corrGeneric.com.tbitsGlobal.shared.domain.OnBehalfEntry.Type1);
		String type2 = rs.getString(corrGeneric.com.tbitsGlobal.shared.domain.OnBehalfEntry.Type2);
		String type3 = rs.getString(corrGeneric.com.tbitsGlobal.shared.domain.OnBehalfEntry.Type3);
		String onBehalf = rs.getString(corrGeneric.com.tbitsGlobal.shared.domain.OnBehalfEntry.OnBehalfLogin);
		
		return new OnBehalfEntry(id,sysPrefix, userLogin, type1, type2, type3, onBehalf);
	}

	public static PropertyEntry createPropertyEntryFromResultSet(ResultSet rs) throws SQLException
	{
		long id = rs.getLong(PropertyEntry.Id);
		String name = rs.getString(PropertyEntry.PropertyName);
		String value = rs.getString(PropertyEntry.PropertyValue);
		String description = rs.getString(PropertyEntry.PropertyDescription);
		
		return new PropertyEntry(id,name, value, description);
	}

	public static ProtocolOptionEntry createProtocolOptionEntryFromResultSet(ResultSet rs) throws SQLException
	{
		long id = rs.getLong(ProtocolOptionEntry.Id);
		String sysPrefix = rs.getString(ProtocolOptionEntry.SysPrefix);
		String name = rs.getString(ProtocolOptionEntry.OptionName);
		String value = rs.getString(ProtocolOptionEntry.OptionValue);
		String description = rs.getString(ProtocolOptionEntry.OptionDescription);
		
		return new ProtocolOptionEntry(id,sysPrefix, name, value, description);
	}

	public static ReportEntry createReportEntryFromResultSet(ResultSet rs) throws SQLException
	{
		long id = rs.getLong(ReportEntry.Id);
		String sysPrefix = rs.getString(ReportEntry.SysPrefix);
		String type1 = rs.getString(ReportEntry.Type1);
		String type2 = rs.getString(ReportEntry.Type2);
		String type3 = rs.getString(ReportEntry.Type3);
		String type4 = rs.getString(ReportEntry.Type4);
		String type5 = rs.getString(ReportEntry.Type5);
		int reportId = rs.getInt(ReportEntry.ReportId);
		
		return  new ReportEntry(id,sysPrefix, type1, type2, type3, type4, type5, reportId);
	}

	public static ReportNameEntry createReportNameEntryFromResultSet(ResultSet rs) throws SQLException
	{
		long id = rs.getLong(ReportNameEntry.Id);
		int reportId = rs.getInt(ReportNameEntry.ReportId);
		String reportFileName = rs.getString(ReportNameEntry.ReportFileName);
		
		return new ReportNameEntry(id,reportId, reportFileName);
	}

	public static ReportParamEntry createReportParamEntryFromResultSet(ResultSet rs) throws SQLException
	{
		long id = rs.getLong(ReportParamEntry.Id);
		int reportId = rs.getInt(ReportParamEntry.ReportId);
		String paramName = rs.getString(ReportParamEntry.ParamName);
		String paramValue = rs.getString(ReportParamEntry.ParamValue);
		String paramType = rs.getString(ReportParamEntry.ParamType);
		String paramValueType = rs.getString(ReportParamEntry.ParamValueType);
		
		return new ReportParamEntry(id,reportId, paramType, paramName, paramValueType, paramValue);
	}

	public static UserMapEntry createUserMapEntryFromResultSet(ResultSet rs) throws SQLException
	{
		long id = rs.getLong(UserMapEntry.Id);
		String userLogin = rs.getString(UserMapEntry.UserLogin);
		String sysPrefix = rs.getString(UserMapEntry.SysPrefix);
		String type1 = rs.getString(UserMapEntry.Type1);
		String type2 = rs.getString(UserMapEntry.Type2);
		String type3 = rs.getString(UserMapEntry.Type3);
		String userTypeFieldName = rs.getString(UserMapEntry.UserTypeFieldName);
		String userLoginValue = rs.getString(UserMapEntry.UserLoginValue);
		int strictNess = rs.getInt(UserMapEntry.StrictNess);
		return new UserMapEntry(id,userLogin, sysPrefix, type1, type2, type3, userTypeFieldName, userLoginValue, strictNess);
	}

	public static TBitsLogger LOG = TBitsLogger.getLogger("corrGeneric");

	public static HashMap<String,HashMap<String,HashMap<String,Collection<String>>>> getOnBehalfMap(ArrayList<OnBehalfEntry> obArr )
		{
			if( null == obArr )
				return null;
			
			HashMap<String,HashMap<String,HashMap<String,Collection<String>>>> map = new HashMap<String,HashMap<String,HashMap<String,Collection<String>>>>();
			
			for( OnBehalfEntry ob : obArr )
			{
				String type1 = ob.getType1();
				String type2 = ob.getType2();
				String type3 = ob.getType3();
	//			Collection<User> users = ob.getOnBehalfUsers() ;
				
				HashMap<String,HashMap<String,Collection<String>>> map2 = map.get(type1);
				if( null == map2 )
					map2 = new HashMap<String,HashMap<String,Collection<String>>>();
				
				HashMap<String,Collection<String>> map3 = map2.get(type2);
				if( null == map3 )
					map3 = new HashMap<String,Collection<String>>();
				
				Collection<String> userLogins = map3.get(type3);
				if( null == userLogins )
					userLogins = new ArrayList<String>();
				
	//			for( User user : users )
	//			{
	//				userLogins.add(user.getUserLogin());
	//			}
				userLogins.add(ob.getOnBehalfUser());
				map3.put(type3, userLogins);
				map2.put(type2, map3);
				map.put(type1, map2);
			}
			
			return map;
		}

	public static CorrNumberEntry createCorrNumberEntryFromResultSet(ResultSet rs) throws SQLException 
	{
		int id = rs.getInt(CorrNumberEntry.Id);
		String sysPrefix = rs.getString(CorrNumberEntry.SysPrefix);
		String numType1 = rs.getString(CorrNumberEntry.NumType1);
		String numType2 = rs.getString(CorrNumberEntry.NumType2);
		String numType3 = rs.getString(CorrNumberEntry.NumType3);
		String numFormat = rs.getString(CorrNumberEntry.NumFormat);
		String numFields = rs.getString(CorrNumberEntry.NumFields);
		String maxIdFormat = rs.getString(CorrNumberEntry.MaxIdFormat);
		String maxIdFields = rs.getString(CorrNumberEntry.MaxIdFields);
		
		return new CorrNumberEntry(id, sysPrefix, numType1, numType2, numType3, numFormat, numFields, maxIdFormat, maxIdFields);
	}
}