package corrGeneric.com.tbitsGlobal.server.managers;

import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.FAILED_CON;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.FAILED_TO_RETRIEVE;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.PropOnBehalfUserCacheSize;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.User;
//import transbit.tbits.exception.CorrException;
import corrGeneric.com.tbitsGlobal.server.cache.OnBehalfTableCache;
//import corrGeneric.com.tbitsGlobal.server.cache.OnBehalfUserCache;
//import corrGeneric.com.tbitsGlobal.server.objects.OnBehalf;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.OnBehalfEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.key.OnBehalfKey;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

import static corrGeneric.com.tbitsGlobal.shared.domain.OnBehalfEntry.*;

public class OnBehalfManager extends AbstractManager {
	private OnBehalfTableCache onBehalfTableCache = null;
//	private OnBehalfUserCache onBehalfUserCache = null;

	private static OnBehalfManager instance = null;

	private OnBehalfManager() throws CorrException {
		initialize();

		ManagerRegistry.getInstance().registerManager(OnBehalfManager.class,this);
	}

	public static synchronized OnBehalfManager getInstance()
			throws CorrException {
		if (null == instance)
			instance = new OnBehalfManager();
		return instance;
	}

	public static final String GetAllFromDB = "select * from " + TableName
			+ " where " + SysPrefix + "=? and " + UserLogin + "=?" ;
	
	public static ArrayList<OnBehalfEntry> lookupOnBehalfList(String sysPrefix,
			String userLogin) throws CorrException {
		return OnBehalfManager.getInstance().getOnBehalfTableCache().get(new OnBehalfKey(sysPrefix, userLogin));
	}

	/**
	 * return the list of on behalf entries for a particular userlogin in the specified ba
	 * @throws CorrException 
	 */
//	public static ArrayList<OnBehalfEntry> lookupOnBehalfListFromLogin(String sysPrefix, String userLogin) throws CorrException
//	{		
//		ArrayList<OnBehalfEntry> dbobArray = OnBehalfManager.getInstance().getOnBehalfTableCache().get(new OnBehalfKey(sysPrefix, userLogin));
//		if( null == dbobArray )
//			return null;
//		
//		ArrayList<OnBehalfEntry> retArray = new ArrayList<OnBehalfEntry>(dbobArray);
//		for( Iterator<OnBehalfEntry> iter = retArray.iterator() ; iter.hasNext() ; )
//		{
//			OnBehalfEntry obe = iter.next();
//			if( !obe.getUserLogin().equals(userLogin) )
//				iter.remove();
//		}
//		
//		return retArray;
//	}
	
//	public static ArrayList<OnBehalf> getResolvedMappingFromTable(
//			String sysPrefix, String userLogin) throws CorrException {
//		ArrayList<OnBehalf> obArray = new ArrayList<OnBehalf>();
//		ArrayList<OnBehalfEntry> dbobArray = OnBehalfManager.getInstance()
//				.getOnBehalfTableCache().get(sysPrefix);
//
//		if (null == dbobArray) {
//			return null;// throw new
//						// CorrException("On_Behalf_Of mapping not found for BusinessArea with sysPrefix : "
//						// + sysPrefix);
//		}
//
//		for (OnBehalfEntry d : dbobArray) {
//			Hashtable<String, OnBehalf> tab = OnBehalf
//					.createOnBehalfFromOnBehalfEntry(d);
//			OnBehalf ob = tab.get(userLogin);
//			if (null != ob)
//				obArray.add(ob);
//		}
//
//		if (obArray.size() == 0)
//			return null;
//
//		return obArray;
//	}

	public OnBehalfTableCache getOnBehalfTableCache() {
		return onBehalfTableCache;
	}

//	public OnBehalfUserCache getOnBehalfUserCache() {
//		return onBehalfUserCache;
//	}

	public static ArrayList<OnBehalfEntry> getAllFromDB(OnBehalfKey onBehalfKey)
			throws CorrException {
		ArrayList<OnBehalfEntry> obMap = new ArrayList<OnBehalfEntry>();

		Connection con = null;
		try {
			con = DataSourcePool.getConnection();
			if (null != con) {
				PreparedStatement ps = con.prepareStatement(GetAllFromDB);
				ps.setString(1, onBehalfKey.getSysPrefix());
				ps.setString(2, onBehalfKey.getUserLogin());

				ResultSet rs = ps.executeQuery();
				if (null != rs) {
					while (rs.next()) {
						OnBehalfEntry dbob = corrGeneric.com.tbitsGlobal.server.util.Utility
								.createOnBehalfEntryFromResultSet(rs);
						// put into cache only if the the userLogin and onBehalfOfLogin user exists and are active
						User loginUser = null;
						try {
							loginUser = User.lookupByUserLogin(dbob.getUserLogin());
						} catch (DatabaseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						User onBehalfUser = null;
						try {
							onBehalfUser = User.lookupByUserLogin(dbob.getOnBehalfUser());
						} catch (DatabaseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if( null == loginUser || null == onBehalfUser 
								|| loginUser.getIsActive() == false
								|| onBehalfUser.getIsActive() == false
						)
						{
							// don't put into the cache.
							System.out.println("The onbehalf entry : " + dbob + " is illegal at this point. Reasons could be.\n" +
									"1.user_login is null or inactive or does not exists\n" +
									"2.onbehalf_of_login is null or inactive or does not exists");
						}
						else
						{
							obMap.add(dbob);
						}
					}
				}
			} else {
				throw new CorrException(FAILED_CON);
			}
		} catch (SQLException e) {
			Utility.LOG.warn(TBitsLogger.getStackTrace(e));
			throw new CorrException(FAILED_TO_RETRIEVE + " on behalf map.", e);
		} finally {
			try {
				if (null != con && con.isClosed() == false) {
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (obMap.size() == 0)
			return null;

		return obMap;
	}

	public int persistEntry(OnBehalfEntry entry) throws CorrException {
		if (null == entry)
			throw new CorrException("OnBehalfEntry was null.");

		if (entry.getId() == -1) {
			return insertEntry(entry);
		} else {
			return updateEntry(entry);
		}
	}

	public int insertEntry(OnBehalfEntry e) throws CorrException {
		Connection con = null;
		try {
			con = DataSourcePool.getConnection();
			return insertEntry(con, e);
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
			sqle.printStackTrace();
			throw new CorrException(sqle);
		} finally {
			try {
				if (null != con && con.isClosed() == false) {
					con.close();
				}
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private int insertEntry(Connection con, OnBehalfEntry e)
			throws CorrException {
		try {
			if (null == e)
				throw new CorrException("Provided FieldNameEntry was null.");

			if (null == con || con.isClosed() == true)
				throw new CorrException(
						"Connection was either null or closed.");

			String sql = "insert into " + TableName + " values (?,?,?,?,?,?)";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, e.getSysPrefix());
			ps.setString(2, e.getUserLogin());
			ps.setString(3, e.getType1());
			ps.setString(4, e.getType2());
			ps.setString(5, e.getType3());
			ps.setString(6, e.getOnBehalfUser());

			int count = ps.executeUpdate();
			refresh();
			return count;
		} catch (Exception ex) {
			Utility.LOG.error(ex);
			throw new CorrException(ex);
		}
	}

	public int updateEntry(OnBehalfEntry e) throws CorrException {
		Connection con = null;
		try {
			con = DataSourcePool.getConnection();
			return updateEntry(con, e);
		} catch (Exception ex) {
			Utility.LOG.error(ex);
			throw new CorrException(ex);
		} finally {
			try {
				if (null != con && con.isClosed() == false) {
					con.close();
				}
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}

	private int updateEntry(Connection con, OnBehalfEntry entry)
			throws CorrException {
		try {
			if (null == entry)
				throw new CorrException("Provided OnBehalfEntry was null.");

			if (null == con || con.isClosed() == true)
				throw new CorrException(
						"Connection was either null or closed.");

			String sql = "update " + TableName + " set " + SysPrefix + " = ?, "
					+ UserLogin + " = ? , " + Type1 + " = ?, " + Type2 + " = ?, "
					+ Type3 + " = ?, " + OnBehalfLogin + " = ? " + " where " + Id + "= ?";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, entry.getSysPrefix());
			ps.setString(2, entry.getUserLogin());
			ps.setString(3, entry.getType1());
			ps.setString(4, entry.getType2());
			ps.setString(5, entry.getType3());
			ps.setString(6, entry.getOnBehalfUser());

			ps.setLong(7, entry.getId());

			int count = ps.executeUpdate();
			refresh();

			return count;
		} catch (Exception e) {
			Utility.LOG.error(e);
			throw new CorrException(e);
		}
	}

	public int deleteEntry(OnBehalfEntry entry) throws CorrException {
		Connection con = null;
		try {
			con = DataSourcePool.getConnection();
			return deleteEntry(con, entry);
		} catch (Exception e) {
			Utility.LOG.error(e);
			throw new CorrException(e);
		} finally {
			try {
				if (null != con && con.isClosed() == false) {
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public int deleteEntry(Connection con, OnBehalfEntry bfe)
			throws CorrException {
		try {

			if (null == bfe)
				throw new CorrException("Provided OnBehalfEntry was null.");

			if (null == con || con.isClosed() == true)
				throw new CorrException(
						"Connection was either null or closed.");

			String sql = "delete from " + TableName + " where " + Id + "= ?";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setLong(1, bfe.getId());

			int count = ps.executeUpdate();
			refresh();

			return count;
		} catch (Exception e) {
			Utility.LOG.error(e);
			throw new CorrException(e);
		}
	}

//	public void clearCaches() {
//		this.getOnBehalfTableCache().clear();
//		this.getOnBehalfUserCache().clear();
//	}

	protected void initialize() throws CorrException {
		PropertyEntry cacheSize = PropertyManager
				.lookupProperty(PropOnBehalfUserCacheSize);
		PropertyEntry windowSize = PropertyManager
				.lookupProperty(PropOnBehalfUserCacheWindowSize);
		
		PropertyEntry sysCacheSize = PropertyManager
		.lookupProperty(PropOnBehalfSysCacheSize);
PropertyEntry sysWindowSize = PropertyManager
		.lookupProperty(PropOnBehalfSysCacheWindowSize);

		Integer cs = null;
		Integer ws = null;
		try {
			cs = Integer.parseInt(cacheSize.getValue());
			ws = Integer.parseInt(windowSize.getValue());
		} catch (NumberFormatException nfe) {
			throw new CorrException("Integer value expected for "
					+ PropOnBehalfUserCacheSize + " and "
					+ PropOnBehalfUserCacheWindowSize
					+ " : found values respectively are : "
					+ cacheSize.getValue() + "," + windowSize.getValue());
		}
		
		Integer scs = null;
		Integer sws = null;
		try {
			scs = Integer.parseInt(sysCacheSize.getValue());
			sws = Integer.parseInt(sysWindowSize.getValue());
		} catch (NumberFormatException nfe) {
			throw new CorrException("Integer value expected for "
					+ PropOnBehalfSysCacheSize + " and "
					+ PropOnBehalfSysCacheWindowSize
					+ " : found values respectively are : "
					+ sysCacheSize.getValue() + "," + sysWindowSize.getValue());
		}
		
		onBehalfTableCache = new OnBehalfTableCache(scs,sws);
//		onBehalfUserCache = new OnBehalfUserCache(cs, ws);
	}

	@Override
	public void refresh() throws CorrException {
		initialize();
	}

}
