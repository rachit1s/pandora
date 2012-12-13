package kskCorr.others;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;

import corrGeneric.com.tbitsGlobal.server.managers.AbstractManager;
import corrGeneric.com.tbitsGlobal.server.managers.ManagerRegistry;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import static corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry.*;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class KskManager extends AbstractManager 
{

	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");
	public ArrayList<ProtocolOptionEntry> targetDateOptions = null;
//	public ArrayList<ProtocolOptionEntry> readAccessMailingListOptions = null;
	
	private static KskManager instance = null;
	private KskManager() throws CorrException
	{
		ManagerRegistry.getInstance().registerManager(KskManager.class, this);
	}
	
	public synchronized static KskManager getInstance() throws CorrException
	{
		if( null == instance )
			instance = new KskManager();
		
		return instance;
	}
	
	public ArrayList<ProtocolOptionEntry> lookupTargetDateOptions() throws CorrException
	{
		KskManager manager = getInstance() ;
		if( null == manager.getTargetDateOptions() )
		{
			manager.setTargetDateOptions(manager.getTargetDateOptionsFromDB());
		}
		
		return manager.getTargetDateOptions();
	}
	
//	public ArrayList<ProtocolOptionEntry> lookupReadAccessMailingListOptions() throws CorrException
//	{
//		KskManager manager = getInstance() ;
//		if( null == manager.getReadAccessMailingListOptions() )
//		{
//			manager.setReadAccessMailingListOptions(manager.getReadAccessMailingListOptionsFromDB());
//		}
//		
//		return manager.getReadAccessMailingListOptions();
//	}
	
	private ArrayList<ProtocolOptionEntry> getTargetDateOptionsFromDB() throws CorrException 
	{
		String sql = " select  * from " + TableName + " where " + OptionName + " = '" + KskConst.TargetDateFieldName + "'";
		
		ArrayList<ProtocolOptionEntry> poeList = new ArrayList<ProtocolOptionEntry>();
		
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if( null != rs )
			{
				while(rs.next())
				{
					ProtocolOptionEntry poe = Utility.createProtocolOptionEntryFromResultSet(rs);
					if( null != poe )
						poeList.add(poe);
				}
			}
		}
	   catch (SQLException e) 
	   {
		   LOG.error(TBitsLogger.getStackTrace(e));
		   throw new CorrException("Exception occured while retriving data from db : " + e.getMessage());
		}
		
		return poeList;
	}
	
//	private ArrayList<ProtocolOptionEntry> getReadAccessMailingListOptionsFromDB() throws CorrException 
//	{
//		String sql = " select  * from " + TableName + " where " + OptionName + " = '" + KskConst.ReadAccessMailList + "'";
//		
//		ArrayList<ProtocolOptionEntry> poeList = new ArrayList<ProtocolOptionEntry>();
//		
//		Connection con = null;
//		try
//		{
//			con = DataSourcePool.getConnection();
//			PreparedStatement ps = con.prepareStatement(sql);
//			ResultSet rs = ps.executeQuery();
//			if( null != rs )
//			{
//				while(rs.next())
//				{
//					ProtocolOptionEntry poe = Utility.createProtocolOptionEntryFromResultSet(rs);
//					if( null != poe )
//						poeList.add(poe);
//				}
//			}
//		}
//	   catch (SQLException e) 
//	   {
//		   LOG.error(TBitsLogger.getStackTrace(e));
//		   throw new CorrException("Exception occured while retriving data from db : " + e.getMessage());
//		}
//		
//		return poeList;
//	}

	public void clearCaches() 
	{
		targetDateOptions = null ;
//		readAccessMailingListOptions = null;
	}

	/**
	 * @return the targetDateOptions
	 */
	private ArrayList<ProtocolOptionEntry> getTargetDateOptions() {
		return targetDateOptions;
	}
	
	/**
	 * @return the readAccessMailingListOptions
	 */
//	private ArrayList<ProtocolOptionEntry> getReadAccessMailingListOptions() {
//		return readAccessMailingListOptions;
//	}

	/**
	 * @param targetDateOptions the targetDateOptions to set
	 */
	private void setTargetDateOptions(
			ArrayList<ProtocolOptionEntry> targetDateOptions) {
		this.targetDateOptions = targetDateOptions;
	}

	@Override
	public void refresh() throws CorrException {
		clearCaches();
	}
	
	/**
	 * @param readAccessMailingListOptions the readAccessMailingListOptions to set
	 */
//	private void setReadAccessMailingListOptions(
//			ArrayList<ProtocolOptionEntry> readAccessMailingListOptions) {
//		this.readAccessMailingListOptions = readAccessMailingListOptions;
//	}

}
