package com.tbitsglobal.ddc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import transbit.tbits.common.DataSourcePool;

import com.tbitsglobal.ddc.dao.exception.FailedToInsert;
import com.tbitsglobal.ddc.domain.KeywordSet;
import com.tbitsglobal.ddc.exception.FailedToDelete;
import com.tbitsglobal.ddc.exception.FailedToFindObject;
import com.tbitsglobal.ddc.rest.DDCHelper;

public class KeywordSetDao
{
	/**
	 * 
	 */
	private static final String DDC_KEYWORDS_TABLE_ID = "DDC_KEYWORDS_TABLE_ID";
	private static final Logger logger = Logger.getLogger(KeywordSetDao.class);
	private static KeywordSetDao dao = new KeywordSetDao();
	
	private static KeywordSet keywordSet ;
	
	static
	{
		ArrayList<String> keywords = new ArrayList<String>();
		keywords.add("FMG DTN Number:");
		keywordSet = new KeywordSet(0L, keywords);
	}
	
	/*
	 CREATE TABLE nitiraj_test.dbo.keyword_set
(id bigint NOT NULL,
keywords varchar(255))
	 */
	/*
	 * private Long id;
	
	private ArrayList<String> keyWords;

	 */
	public static final String TableName = "keyword_set";
	public static final String Id = "id";
	public static final String Keywords = "keywords";
	
	public static final String InsertSQL = "insert into " + TableName + " values(?,?) ;";
	public static final String DeleteSQL = "delete from " + TableName + " where " + Id + "=?";
	public static final String GetByIdSQL = "select * from " + TableName + " where " + Id + "=?";
	public static final String GetAllSQL = " select * from " + TableName + " order by " + Id;
	
	public static KeywordSetDao getInstance()
	{
		return dao;
	}
	
	public void delete(KeywordSet ks) throws FailedToDelete
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(DeleteSQL);
			ps.setLong(1, ks.getId());
			int count = ps.executeUpdate();
			logger.debug(count + " rows deleted from " + TableName );
		}
		catch(Exception e)
		{
			logger.error(e);
			throw new FailedToDelete(e);
		}
		finally
		{
			try {
				if( null != con && con.isClosed() == false)
					con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public KeywordSet insert(KeywordSet ks) throws FailedToInsert
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			con.setAutoCommit(false);
			
			long nextNumber = DDCHelper.incrAndGetNext(con, DDC_KEYWORDS_TABLE_ID);
			StringBuffer sb = new StringBuffer();
			for( String key : ks.getKeyWords() )
			{
				sb.append("insert into " + TableName + " values(" + nextNumber + ",'" + key + "');\n");
			}
			
			Statement s = con.createStatement();
			int count = s.executeUpdate(sb.toString());
			logger.debug(count + " rows inserted into " + TableName);
			con.commit();
			
			ks.setId(nextNumber);
			return ks;
		}
		catch(Exception e)
		{
			try {
				con.rollback();
			} catch (SQLException e1) {
				logger.error(e1);
			}
			logger.error(e);
			throw new FailedToInsert(e);
		}
		finally
		{
			try {
				if( null != con && con.isClosed() ==false)
					con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public KeywordSet getById(long id) throws FailedToFindObject
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(GetByIdSQL);
			ps.setLong(1, id);
			
			ArrayList<String>ks = new ArrayList<String>();
			
			ResultSet rs = ps.executeQuery();
			if( null != rs )
			{
				while(rs.next())
				{
					String keyword = rs.getString(Keywords);
					ks.add(keyword);
				}
			}
			
			KeywordSet kset = new KeywordSet(id, ks);
			return kset;
		}
		catch(Exception e)
		{
			logger.error(e);
			throw new FailedToFindObject(e);
		}
		finally{
			try {
				if( null != con && con.isClosed() == false)
					con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	public Hashtable<Long,KeywordSet> getAll() throws FailedToFindObject
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(GetAllSQL);
			
			Hashtable<Long,KeywordSet> table = new Hashtable<Long,KeywordSet>();
			
			ResultSet rs = ps.executeQuery();
			if( null != rs )
			{
				while(rs.next())
				{
					Long id = rs.getLong(Id);
					String keyword = rs.getString(Keywords);
					if( null == table.get(id))
						table.put(id, new KeywordSet());
					
					table.get(id).getKeyWords().add(keyword);
				}
			}
			
			return table;
		}
		catch(Exception e)
		{
			logger.error(e);
			throw new FailedToFindObject(e);
		}
		finally{
			try {
				if( null != con && con.isClosed() == false)
					con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
