package ddc.com.tbitsglobal.ddc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import transbit.tbits.common.DataSourcePool;


import ddc.com.tbitsglobal.ddc.dao.exception.FailedToInsert;
import ddc.com.tbitsglobal.ddc.dao.exception.FailedToUpdate;
import ddc.com.tbitsglobal.ddc.domain.FirmProperty;
import ddc.com.tbitsglobal.ddc.domain.SearchAlgo;
import ddc.com.tbitsglobal.ddc.exception.FailedToDelete;
import ddc.com.tbitsglobal.ddc.exception.FailedToFindObject;
import ddc.com.tbitsglobal.ddc.rest.DDCHelper;

public class SearchAlgoDao 
{
	private static final Logger logger = Logger.getLogger(SearchAlgoDao.class);
	private static SearchAlgoDao sad = new SearchAlgoDao();

	
	/* 
CREATE TABLE dbo.search_algo
(id bigint IDENTITY (1,1) NOT NULL,
search_type varchar(25) NOT NULL,
search_all BIT NOT NULL,
pattern varchar(255),
first_keyword varchar(255),
second_keyword varchar(255),
PRIMARY KEY (id))
	 */
	public static final String TableName = "search_algo";
	public static final String Id = "id";
	public static final String Search_Type= "search_type";
	public static final String Search_All = "search_all";
	public static final String Pattern = "pattern";
	public static final String FirstKeyword = "first_keyword";
	public static final String SecondKeyword = "second_keyword";
	
	public static SearchAlgoDao getInstance()
	{
		return sad;
	}
	

	private static final String InsertSQL = "insert into " + TableName + " values(?,?,?,?,?) ";
	private static final String UpdateSQL = "update " + TableName + " set " + Search_Type + "=?," +
				Search_All + "=?," + Pattern + "=?," + FirstKeyword + "=?," + SecondKeyword + "=?" +
				" where " + Id + "=?";
	private static final String DeleteSQL = "delete from " + TableName + " where " + Id + "=?";
	private static final String GetAllSQL = "select * from " + TableName;
	private static final String GetByIdSQL = "select * from " + TableName + " where " + Id +  "=?";
	
	private static final String CreateTableSQL = "create table " + TableName + "(" +
													Id + " BIGINT IDENTITY," +
													Search_Type + " varchar(256), " +
													Search_All + " BIT, " + 
													Pattern + " varchar(256), " + 
													FirstKeyword + " varchar(256), " +
													SecondKeyword + " varchar(256) " +
													" PRIMARY KEY ( " + Id + ") " +
													")";
	
	private static final String DropTableSQL = "drop table " + TableName ;

	private SearchAlgo createFromResultSet(ResultSet rs) throws SQLException {
 		long id                    =rs.getLong(Id);
 		String searchType       =rs.getString(Search_Type);
 		Boolean searchAll        =rs.getBoolean(Search_All);
 		String pattern =rs.getString(Pattern);
 		String firstKeyword    =rs.getString(FirstKeyword);
 		String secondKeyword     =rs.getString(SecondKeyword);
 		
 		return new SearchAlgo(id, searchType, searchAll, pattern, firstKeyword, secondKeyword);
	}

	
	public SearchAlgo getById(Long id) throws FailedToFindObject
	{
		Connection con = null;
		SearchAlgo sa = null; 
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(GetByIdSQL);
			ps.setLong(1, id);
			
			ResultSet rs = ps.executeQuery();
			if( null != rs && rs.next() )
			{
				sa = createFromResultSet(rs);
			}
			rs.close();
		}
		catch(Exception e)
		{
			logger.error("",e);
			throw new FailedToFindObject("Exception occured while finding SearchAlgo with Id = " + id);
		}
		finally
		{
			if( null != con )
				try {
					if( con.isClosed() == false )
						con.close();
				} catch (SQLException e) {
					logger.error(e);
				}
		}
		if( null == sa )
			throw new FailedToFindObject("Exception occured while finding SearchAlgo with Id = " + id);

		return sa;
	}

	public void createTable() throws FailedToDelete
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(CreateTableSQL);
			ps.execute();
			logger.debug("Created table " + TableName);
		} catch (SQLException e) {
			logger.error("Error occured while creating table " + TableName, e);
			throw new FailedToDelete("Error occured while creating table " + TableName, e);
		}
		finally
		{
			try {
				if( null != con && !con.isClosed() )
					con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void dropTable() throws FailedToDelete
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(DropTableSQL);
			ps.execute();
			logger.debug("Dropped table " + TableName);
		} catch (SQLException e) {
			logger.error("Error occured while dropping table " + TableName, e);
			throw new FailedToDelete("Error occured while dropping table " + TableName, e);
		}
		finally
		{
			try {
				if( null != con && !con.isClosed() )
					con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void delete(SearchAlgo sa) throws FailedToDelete
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(DeleteSQL);
			ps.setLong(1, sa.getId());
			
			int count = ps.executeUpdate();
			logger.debug(count + " rows affected.");
		} catch (SQLException e) {
			logger.error("Error occured while deleting SearchAlgo", e);
			throw new FailedToDelete("Error occured while deleting SearchAlgo", e);
		}
		finally
		{
			try {
				if( null != con && !con.isClosed() )
					con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public SearchAlgo insert(SearchAlgo sa) throws FailedToInsert
	{
		Connection con = null;
		try
		{
			int[] idKeyColumn = new int[1];
			idKeyColumn[0] = 1;
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(InsertSQL, idKeyColumn);
			DDCHelper.setNotNull(ps,1, sa.getSearchType());
			DDCHelper.setNotNull(ps,2, sa.isSearchAll());
			DDCHelper.setNotNull(ps,3,sa.getPattern());
			DDCHelper.setNotNull(ps, 4, sa.getFirstKeyword());
			DDCHelper.setNotNull(ps,5,sa.getSecondKeyword());
			
			int count = ps.executeUpdate();
			if( count != 0 )
			{
				ResultSet rs = ps.getGeneratedKeys();
				if( rs.next() )
				{
					long id = rs.getLong(1);
					logger.info("Newly generated id : " + id);
					sa.setId(id);
				}
			}
			logger.info(count + " rows affected.\nCreated new SearchAlgo : " + sa);
			
			return sa;
		} catch (SQLException e) {
			logger.error("Error occured while inserting Search Algo", e);
			throw new FailedToInsert("Error occured while inserting SearchAlgo", e);
		}
		finally
		{
			try {
				if( null != con && !con.isClosed() )
					con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public SearchAlgo update(SearchAlgo sa) throws FailedToUpdate
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(UpdateSQL);
			ps.setString(1, sa.getSearchType());
			ps.setBoolean(2, sa.isSearchAll());
			DDCHelper.setNotNull(ps,3,sa.getPattern());
			DDCHelper.setNotNull(ps, 4, sa.getFirstKeyword());
			DDCHelper.setNotNull(ps,5,sa.getSecondKeyword());
			ps.setLong(6, sa.getId());
			
			int count = ps.executeUpdate();
			logger.debug(count + " rows affected.");
			
			return sa;
		} catch (SQLException e) {
			logger.error("Error occured while updating SearchAlgo", e);
			throw new FailedToUpdate("Error occured while updating SearchAlgo", e);
		}
		finally
		{
			try {
				if( null != con && !con.isClosed() )
					con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public List<SearchAlgo> getAll() throws FailedToFindObject
	{
		Connection con = null;
		List<SearchAlgo> fpList = new ArrayList<SearchAlgo>(); 
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(GetAllSQL);
			
			ResultSet rs = ps.executeQuery();
			if( null != rs  )
			{
				while(rs.next()){
					SearchAlgo fp = createFromResultSet(rs);
					fpList.add(fp);
				}
				
			}
			rs.close();
		}
		catch(Exception e)
		{
			logger.error(e);
			throw new FailedToFindObject("Exception occured while fetching all search-algos");
		}
		finally
		{
			if( null != con )
				try {
					if( con.isClosed() == false )
						con.close();
				} catch (SQLException e) {
					logger.error(e);
				}
		}

		return fpList;
	}

	
	public static void main(String[] args) {
		
		try {
			
			SearchAlgoDao.getInstance().dropTable();
			SearchAlgoDao.getInstance().createTable();
//			SearchAlgo sa = SearchAlgoDao.getInstance().getById(6L);
//			logger.info("sa = " + sa);
//		} catch (FailedToFindObject e) {
//			e.printStackTrace();
		} catch (FailedToDelete e) {
			e.printStackTrace();
		}
	}
}
