package corrGeneric.constraints;

import static corrGeneric.com.tbitsGlobal.server.util.Utility.fdn;
import static corrGeneric.com.tbitsGlobal.server.util.Utility.getExtraInB;
import static corrGeneric.com.tbitsGlobal.server.util.Utility.toCSLoginList;
import static corrGeneric.com.tbitsGlobal.server.util.Utility.toUsers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import corrGeneric.com.tbitsGlobal.server.interfaces.ICorrConstraintPlugin;
import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.managers.UserMapManager;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.UserMapEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class CorrUserMapConstraint implements ICorrConstraintPlugin {

	public static TBitsLogger LOG = TBitsLogger.getLogger("corrGeneric.constraints");
	private void checkUserMapConstraint(CorrObject coob) throws CorrException 
	{
		User firstLogger = coob.getUserMapUsers().get(0);
		ArrayList<UserMapEntry> map = UserMapManager.lookupUserMap(coob.getBa().getSystemPrefix(), firstLogger.getUserLogin());
		if( null == map )
		{
			LOG.info("No correpondence mapping found for user(" + firstLogger.getUserLogin() + ") for BusinessArea " + coob.getBa().getSystemPrefix() + ". So Ignoring the constraint.");
			return;
			//throw new CorrException("No correpondence mapping found for user(" + firstLogger.getUserLogin() + ") for BusinessArea " + coob.getBa().getSystemPrefix());
		}

		Hashtable<String,Integer> fieldStrict = new Hashtable<String,Integer>();
		Hashtable<String,HashSet<User>> fieldUsers = new Hashtable<String,HashSet<User>>();
		
		for( UserMapEntry ume : map )
		{
			if( (ume.getType1() == null && coob.getUserMapType1() == null) 
					|| ( ume.getType1() != null && coob.getUserMapType1() != null && ume.getType1().equals(coob.getUserMapType1().getName()) )  )
			{
				if( (ume.getType2() == null && coob.getUserMapType2() == null) 
						|| ( ume.getType2() != null && coob.getUserMapType2() != null && ume.getType2().equals(coob.getUserMapType2().getName()) )  )
				{
					if( (ume.getType3() == null && coob.getUserMapType3() == null) 
							|| ( ume.getType3() != null && coob.getUserMapType3() != null && ume.getType3().equals(coob.getUserMapType3().getName()) )  )
					{
						String fieldName = ume.getUserTypeFieldName();
						Integer now = fieldStrict.get(fieldName);
						if( null == now )
							now = 0 ;
						if( ume.getStrictNess() > now )
							now = ume.getStrictNess();
						fieldStrict.put(fieldName, now);
						
						String userLogin = ume.getUserLoginValue();
						
						// ignore the empty login string assuming it to be
						// a value set for clearing the field
						if( null == userLogin || userLogin.trim().equals(""))
							continue;
						
						User user = null;
						try {
							user = User.lookupByUserLogin(userLogin);
						} catch (DatabaseException e) {
							Utility.LOG.warn(TBitsLogger.getStackTrace(e));
							throw new CorrException("Exception occured while accessing the user with user login : " + userLogin);
						}
						if( null == user )
							throw new CorrException("Cannot find user with login : " + userLogin );
						
						HashSet<User> userSet = fieldUsers.get(fieldName);
						if( null == userSet )
							userSet = new HashSet<User>();
						
						userSet.add(user);
						fieldUsers.put(fieldName, userSet);
					}
				}
			}
		}
		
		for( Enumeration<String> fnkeys = fieldUsers.keys() ; fnkeys.hasMoreElements() ;)
		{
			String fieldName = fnkeys.nextElement();
			HashSet<User> users = fieldUsers.get(fieldName);
			Integer strict = fieldStrict.get(fieldName);
			if( null == strict )
				strict = UserMapEntry.StrictNess_Strict;
			
			switch( strict )
			{
				case UserMapEntry.StrictNess_AllowExtra :
					handleAllowExtra(coob,fieldName,users);
					break;
				case UserMapEntry.StrictNess_AllowAny :
					handleAllowAny(coob,fieldName,users);
					break;
				case UserMapEntry.StrictNess_FromThese :
					handleFromThese(coob,fieldName,users);
					break;
				default : 
				case UserMapEntry.StrictNess_Strict :
					handleStrict(coob,fieldName,users);		
			}				
		}
	}
	
	private void handleFromThese(CorrObject coob, String fieldName,
			HashSet<User> users) throws CorrException 
	{
		String userlist = coob.getAsString(fieldName);
		if( null == userlist && ( null != users && users.size() != 0 ))
			throw new CorrException("Atleast one user must be present in " + fdn(coob.getBa().getSystemPrefix(), fieldName) + ":\n" + toCSLoginList(users) );
	
		ArrayList<User> inUsers = toUsers(userlist);
		
		if(null == inUsers || inUsers.size() == 0 ) 
			if( null != users && users.size() != 0 )
				throw new CorrException("Atleast one user must be present in " + fdn(coob.getBa().getSystemPrefix(), fieldName) + ":\n" + toCSLoginList(users) );
			else
				return ;
					
		
//		Collection<User> notFound = getExtraInB(inUsers, users);
		Collection<User> extra = getExtraInB(users,inUsers);
		
//		if( null != notFound && notFound.size() != 0 )
//			throw new CorrException("Following users must be present in " + fdn(coob.getBa(), fieldName) + ":\n" + toCSLoginList(notFound));
		
		if( null != extra && extra.size() != 0 )
			throw new CorrException("Following users are not allowed in " + fdn(coob.getBa(),fieldName) + ":\n" + toCSLoginList(extra));
	}

	private void handleStrict(CorrObject coob, String fieldName, HashSet<User> users) throws CorrException 
	{
		String userlist = coob.getAsString(fieldName);
		if( null == userlist && ( null != users && users.size() != 0 ))
			throw new CorrException("Following users must be present in " + fdn(coob.getBa().getSystemPrefix(), fieldName) + ":\n" + toCSLoginList(users) );
	
		ArrayList<User> inUsers = toUsers(userlist);
		
		Collection<User> notFound = getExtraInB(inUsers, users);
		Collection<User> extra = getExtraInB(users,inUsers);
		
		if( null != notFound && notFound.size() != 0 )
			throw new CorrException("Following users must be present in " + fdn(coob.getBa(), fieldName) + ":\n" + toCSLoginList(notFound));
		
		if( null != extra && extra.size() != 0 )
			throw new CorrException("Following users are not allowed in " + fdn(coob.getBa(),fieldName) + ":\n" + toCSLoginList(extra));
	}
	
	private void handleAllowAny(CorrObject coob, String fieldName, HashSet<User> users) 
	{
		// do nothing !
	}
	
	private void handleAllowExtra(CorrObject coob, String fieldName, HashSet<User> users) throws CorrException 
	{
		String userlist = coob.getAsString(fieldName);
		if( null == userlist && ( null != users && users.size() != 0 ))
			throw new CorrException("Following users must be present in " + fdn(coob.getBa(), fieldName) + ":\n" + toCSLoginList(users) );
		
		ArrayList<User> inUsers = toUsers(userlist);
		
		Collection<User> notFound = getExtraInB(inUsers, users);
		if( null != notFound && notFound.size() != 0 )
			throw new CorrException("Following users must present in " + fdn(coob.getBa().getSystemPrefix(), fieldName) + ":\n" + toCSLoginList(notFound));
	}
	
	public void execute(Hashtable<String, Object> params) throws CorrException 
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
	
//		FieldNameEntry disableProtFne = FieldNameManager.lookupFieldNameEntry(coob.getBa().getSystemPrefix(), GenericParams.DisableProtocolFieldName);
		
		if(null != coob.getDisableProtocol())
		{
			Type disValue = coob.getDisableProtocol();
			if(disValue.getName().equalsIgnoreCase(GenericParams.DisableProtocol_True))
			{
				return;							
			}
		}
		
		checkUserMapConstraint(coob);
	}

	public String getName() 
	{
		return "Checks the users present in respective fields of " + UserMapEntry.TableName;
	}

	public double getOrder() {
		return 2;
	}

}
