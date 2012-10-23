package com.tbitsglobal.ddc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import transbit.tbits.common.DataSourcePool;

import com.tbitsglobal.ddc.dao.exception.FailedToInsert;
import com.tbitsglobal.ddc.dao.exception.FailedToUpdate;
import com.tbitsglobal.ddc.domain.FirmProperty;
import com.tbitsglobal.ddc.exception.FailedToDelete;
import com.tbitsglobal.ddc.exception.FailedToFindObject;
import com.tbitsglobal.ddc.rest.DDCHelper;
import static com.tbitsglobal.ddc.rest.DDCHelper.*;

public class FirmPropertyDao 
{
	private static final Logger logger = Logger.getLogger(FirmPropertyDao.class);
	private static FirmProperty fp;
	static
	{
		fp = new FirmProperty(0, "BA1", "@gmail.com", "crazy.nattu@gmail.com", 1L, "number1Field", 2L, "number2Field", 3L," number3Field", 1L);
	}
	private static FirmPropertyDao fpDao = new FirmPropertyDao();;
	
	// no need of synch etc.. 
	public static FirmPropertyDao getInstance()
	{
		return fpDao;
	}

	/*
 CREATE TABLE nitiraj_test.dbo.firm_property
(id bigint IDENTITY (1,1) NOT NULL,
logging_ba_name varchar(25) NOT NULL,
email_pattern varchar(255) NOT NULL,
doc_controller_user_login varchar(255) NOT NULL,
number1_algo_id bigint,
number1_field varchar(255),
number2_algo_id bigint,
number2_field varchar(255),
number3_algo_id bigint,
number3_field varchar(255),
dtn_keywords_id bigint,
PRIMARY KEY (id))
	 */
	/**
	 * will search for email id pattern in the firm
	 * @param emailId
	 * @return
	 */
	private static final String TableName = "firm_property";
	private static final String Id = "id";
	private static final String LoggingBAName = "logging_ba_name";
	private static final String EmailPattern = "email_pattern";
	private static final String DocControllerUserLogin = "doc_controller_user_login";
	private static final String Number1AlgoId = "number1_algo_id";
	private static final String Number2AlgoId = "number2_algo_id";
	private static final String Number3AlgoId = "number3_algo_id";
	private static final String Number1Field = "number1_field";
	private static final String Number2Field = "number2_field";
	private static final String Number3Field = "number3_field";
	private static final String DtnKeywordsId = "dtn_keywords_id";
	
	private static final String Search_By_EmailId_Pattern = new StringBuffer().append("select * from ").append(TableName)
							.append(" where ").append(EmailPattern).append( " = ?").toString();
	
	private static final String Search_By_Doc_Controller = new StringBuffer().append("select * from ").append(TableName)
			.append(" where ").append(DocControllerUserLogin).append( " = ?").toString();

	public FirmProperty findFirmPropertyByEmailId(String emailId) throws FailedToFindObject
	{
		String pattern = emailId.substring(emailId.indexOf('@'));
		Connection con = null;
		FirmProperty fp = null; 
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(Search_By_EmailId_Pattern);
			ps.setString(1, pattern);
			
			ResultSet rs = ps.executeQuery();
			if( null != rs && rs.next() )
			{
				fp = createFromResultSet(rs);
			}
			rs.close();
		}
		catch(Exception e)
		{
			logger.error(e);
			throw new FailedToFindObject("Exception occured while finding firm with email id pattern : " + pattern);
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
		if( null == fp )
			throw new FailedToFindObject("Firm not found with email id pattern : " + pattern);

		return fp;
	}
	
	/**
	 * @param rs
	 * @return
	 * @throws SQLException 
	 */
	private FirmProperty createFromResultSet(ResultSet rs) throws SQLException {
 		long id                    =rs.getLong(Id);
 		String loggingBAName       =rs.getString(LoggingBAName);
 		String emailPattern        =rs.getString(EmailPattern);
 		String docControlUserLogin =rs.getString(DocControllerUserLogin);
 		Long number1AlgoId      =rs.getLong(Number1AlgoId);
 		String number1Field        =rs.getString(Number1Field);
 		Long number2AlgoId      =rs.getLong(Number2AlgoId);
 		String number2Field        =rs.getString(Number2Field);
 		Long number3AlgoId      =rs.getLong(Number3AlgoId);
 		String number3Field        =rs.getString(Number3Field);
 		Long dtnKeywordsId      =rs.getLong(DtnKeywordsId);
 		
 		return new FirmProperty(id, loggingBAName, emailPattern, docControlUserLogin, number1AlgoId, number1Field, number2AlgoId, number2Field, number3AlgoId, number3Field, dtnKeywordsId);
	}

	private static final String InsertSQL = "insert into " + TableName + " values(?,?,?,?,?,?,?,?,?,?) ";
	private static final String UpdateSQL = "update " + TableName + " set " + LoggingBAName + "=?," +
		
			EmailPattern + "=?," + DocControllerUserLogin + "=?," + Number1AlgoId + "=?," + Number1Field + "=?," +
			Number2AlgoId +	"=?," + Number2Field + "=?," + Number3AlgoId + "=?," + Number3Field + "=?," + DtnKeywordsId + "=?" +
					" where " + Id + "=?";
	private static final String DeleteSQL = "delete from " + TableName + " where " + Id + "=?";
	private static final String GetAllSQL = "select * from " + TableName;
	private static final String GetByIdSQL = "select * from " + TableName + " where " + Id+  "=?";
	
	public void delete(FirmProperty fp) throws FailedToDelete
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(DeleteSQL);
			ps.setLong(1, fp.getId());
			
			int count = ps.executeUpdate();
			logger.debug(count + " rows affected.");
		} catch (SQLException e) {
			logger.error("Error occured while deleting FirmProperty", e);
			throw new FailedToDelete("Error occured while deleting FirmProperty", e);
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
	public FirmProperty insert(FirmProperty fp) throws FailedToInsert
	{
		Connection con = null;
		try
		{
			int[] idKeyColumn = new int[1];
			idKeyColumn[0] = 1;
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(InsertSQL, idKeyColumn);
			ps.setString(1, fp.getLoggingBAName());
			ps.setString(2, fp.getEmailPattern());
			DDCHelper.setNotNull(ps,3,fp.getDocControlUserLogin());
			DDCHelper.setNotNull(ps, 4, fp.getNumber1AlgoId());
			DDCHelper.setNotNull(ps,5,fp.getNumber1Field());
			DDCHelper.setNotNull(ps,6,fp.getNumber2AlgoId());
			DDCHelper.setNotNull(ps,7,fp.getNumber2Field());
			DDCHelper.setNotNull(ps,8,fp.getNumber3AlgoId());
			DDCHelper.setNotNull(ps, 9, fp.getNumber3Field());
			DDCHelper.setNotNull(ps, 10, fp.getDtnKeywordsId());
			
			int count = ps.executeUpdate();
			if( count != 0 )
			{
				ResultSet rs = ps.getGeneratedKeys();
				if( rs.next() )
				{
					long id = rs.getLong(1);
					fp.setId(id);
				}
			}
			logger.debug(count + " rows affected. Newly generated id : " + idKeyColumn[0]);
			
			return fp;
		} catch (SQLException e) {
			logger.error("Error occured while inserting FirmProperty", e);
			throw new FailedToInsert("Error occured while inserting FirmProperty", e);
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

	public FirmProperty update(FirmProperty fp) throws FailedToUpdate
	{
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(UpdateSQL);
			ps.setString(1, fp.getLoggingBAName());
			ps.setString(2, fp.getEmailPattern());
			DDCHelper.setNotNull(ps,3,fp.getDocControlUserLogin());
			DDCHelper.setNotNull(ps, 4, fp.getNumber1AlgoId());
			DDCHelper.setNotNull(ps,5,fp.getNumber1Field());
			DDCHelper.setNotNull(ps,6,fp.getNumber2AlgoId());
			DDCHelper.setNotNull(ps,7,fp.getNumber2Field());
			DDCHelper.setNotNull(ps,8,fp.getNumber3AlgoId());
			DDCHelper.setNotNull(ps, 9, fp.getNumber3Field());
			DDCHelper.setNotNull(ps, 10, fp.getDtnKeywordsId());
			ps.setLong(11, fp.getId());
			
			int count = ps.executeUpdate();
			logger.debug(count + " rows affected.");
			
			return fp;
		} catch (SQLException e) {
			logger.error("Error occured while updating FirmProperty", e);
			throw new FailedToUpdate("Error occured while updating FirmProperty", e);
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

	public List<FirmProperty> getAll() throws FailedToFindObject
	{
		Connection con = null;
		List<FirmProperty> fpList = new ArrayList<FirmProperty>(); 
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(GetAllSQL);
			
			ResultSet rs = ps.executeQuery();
			if( null != rs  )
			{
				while(rs.next()){
					FirmProperty fp = createFromResultSet(rs);
					fpList.add(fp);
				}
				
			}
			rs.close();
		}
		catch(Exception e)
		{
			logger.error(e);
			throw new FailedToFindObject("Exception occured while fetching all firm-properties");
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
	
	public FirmProperty findFirmPropertyByDocController(String userLogin) throws FailedToFindObject
	{
		Connection con = null;
		FirmProperty fp = null; 
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(Search_By_Doc_Controller);
			ps.setString(1, userLogin);
			
			ResultSet rs = ps.executeQuery();
			if( null != rs && rs.next() )
			{
				fp = createFromResultSet(rs);
			}
			rs.close();
		}
		catch(Exception e)
		{
			logger.error(e);
			throw new FailedToFindObject("Exception occured while finding firm with doc_controller : " + userLogin);
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
		if( null == fp )
			throw new FailedToFindObject("Firm not found with doc controller : " + userLogin);

		return fp;
	}
	
	public FirmProperty getById(Long id) throws FailedToFindObject
	{
		Connection con = null;
		FirmProperty fp = null; 
		try
		{
			con = DataSourcePool.getConnection();
			PreparedStatement ps = con.prepareStatement(GetByIdSQL);
			ps.setLong(1, id);
			
			ResultSet rs = ps.executeQuery();
			if( null != rs && rs.next() )
			{
				fp = createFromResultSet(rs);
			}
			rs.close();
		}
		catch(Exception e)
		{
			logger.error(e);
			throw new FailedToFindObject("Exception occured while finding firm with Id = " + id);
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
		if( null == fp )
			throw new FailedToFindObject("Exception occured while finding firm with Id = " + id);

		return fp;
	}
}
