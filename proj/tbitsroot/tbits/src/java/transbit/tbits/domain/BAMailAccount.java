/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */

/*
 * BAMailAccount.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;

//~--- classes ----------------------------------------------------------------

/**
 * Represents and Manages the POP3 Account information pertaining to a Business
 * Area.
 * TODO: this class desperately requires to be converted into Singleton
 * @author  :
 * @version : $Id: $
 *
 */
public class BAMailAccount implements Serializable {
	public static final TBitsLogger LOG = TBitsLogger
			.getLogger(TBitsConstants.PKG_CONFIG);
	public static final String[] ALLOWED_PROTOCOLS = {"pop3", "imap", "pop3s"};
	public static final String DEFAULT_PROTOCOL = "pop3";
	public static final int DEFAULT_PORT = 110;
	//~--- fields -------------------------------------------------------------

	private String myEmailID;

	private String myMailServer;

	private String myBAPrefix;

	private String myPassward;

	private String myProtocol = "";
	private String myBAEmailAddress = "";
	private int myPort = DEFAULT_PORT;
	private int myBAMailAcId;
	public static boolean isAllowedProtocol(String protocol)
	{
		if(protocol == null)
			throw new IllegalArgumentException("Parameter can not be null");
		for(String p:ALLOWED_PROTOCOLS)
		{
			if(p.equals(protocol))
				return true;
		}
		return false;
	}
	private boolean myIsActive = true;
	public int myCategoryId;

	//~--- constructors -------------------------------------------------------

	/**
	 * The default constructor.
	 */
	public BAMailAccount() {
		myBAMailAcId = 0;
	}

	/**
	 * The complete constructor.
	 *
	 */
	public BAMailAccount(int aBAMailAcId, String aEmailID, String aPassward, String aMailServer,
			String aBAPrefix, String aProtocol,int port, boolean isActive, int categoryId, String aBAEmailAddress) {
		this();
		myBAMailAcId = aBAMailAcId;
		myEmailID = aEmailID;
		myMailServer = aMailServer;
		myBAPrefix = aBAPrefix;
		myPassward = aPassward;
		myProtocol = aProtocol;
		myPort = port;
		myIsActive = isActive; 
		myCategoryId = categoryId;
		myBAEmailAddress = aBAEmailAddress;
	}
	
	public BAMailAccount(String aEmailID, String aPassward, String aMailServer,
			String aBAPrefix)
	{
		this(0, aEmailID, aPassward, aMailServer, aBAPrefix, DEFAULT_PROTOCOL, DEFAULT_PORT, false, 0, "");
	}
	
	//~--- methods ------------------------------------------------------------

	public static BAMailAccount createFromResultSet(ResultSet aRS)
			throws SQLException {
		
		BAMailAccount er = new BAMailAccount(aRS.getInt("ba_mail_ac_id"), aRS.getString("email_id"), aRS
				.getString("passward"), aRS.getString("mail_server"), aRS
				.getString("ba_prefix"), aRS.getString("protocol"), aRS.getInt("port"), aRS.getBoolean("is_active"), aRS.getInt("category_id"), aRS.getString("email_address"));
		return er;
	}

	public String toString()
	{
		return "myBAMailAcId + " + getMyBAMailAcId() + ","
		+ "myEmailID:" + myEmailID + ","
		+ "myMailServer:" + myMailServer + ","
		+ "myBAPrefix:" + myBAPrefix + ","
		+ "myPassward:" + myPassward + ","
		+ "myProtocol:" + myProtocol + ","
		+ "myPort:" + myPort + ","
		+ "myIsActive:" + myIsActive + ","
		+ "categoryId:" + myCategoryId + ","
		+ "BAEmail Address: " + myBAEmailAddress;
	}
	public boolean equals(Object obj)
	{
		if(obj instanceof BAMailAccount)
		{
			BAMailAccount arg = (BAMailAccount) obj;
			return (this.myBAPrefix == arg.getMyBAPrefix()) 
				&& (this.myEmailID == arg.getMyEmailID()) 
				&& (this.myMailServer == arg.getMyMailServer())
				&& (this.myPassward == arg.getMyPassward())
				&& (this.myProtocol == arg.getMyProtocol())
				&& (this.myPort == arg.getPort())
				&& (this.isActive() == arg.isActive())
				&& (this.myCategoryId == arg.myCategoryId)
				&& (this.myBAEmailAddress == arg.getBAEmailAddress());
		}
		else 
			return super.equals(obj);
	}
	/*
	 * Add new object of BA Account to DB
	 *
	 */
	public boolean SaveToDB() throws DatabaseException {
		//BAMailAccount baMailAccount = lookupByBA(this.myBAPrefix);
		boolean retVal = false;
		if (getMyBAMailAcId() == 0) {
			//If there is no account so far, add
			retVal = this.AddToDB();
		} else {
			retVal = this.updateToDB();
		}
		refreshAccounts();
		return retVal;
	}

	private int getMaxId()
	{
		int max = 0;
		Connection con = null;
		try {
			con = DataSourcePool.getConnection();
			PreparedStatement statement = con.prepareStatement("select max(ba_mail_ac_id) from ba_mail_accounts");
			
			ResultSet rs = statement.executeQuery();
			if(rs != null)
			{
				if(rs.next())
					max = rs.getInt(1);
			}
			statement.close();
		} catch (SQLException e) {
			LOG.error("Exception while inserting record in ba-mail acounts.", e);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException sqle) {
					LOG.error("Exception while closing the connection", sqle);
				}
			}
		}
		return max;
	}
	/*
	 * Inserts record to db. Doesn't check if it exists or not. Should not be used directly. 
	 * Instead use SaveToDB. 
	 * @return true, if addition is successful. Otherwise, return false.
	 */
	private boolean AddToDB() {
		int maxId = getMaxId();
		myBAMailAcId = maxId + 1;
		Connection con = null;
		try {
			con = DataSourcePool.getConnection();
			PreparedStatement statement = con.prepareStatement("insert into ba_mail_accounts "
							+ "(ba_prefix, email_id, passward, mail_server, protocol, port, is_active, category_id, ba_mail_ac_id, email_address) VALUES (?, ?, ?, ?, ?,?,?,?,?,?)");
			statement.setString(1, this.myBAPrefix);
			statement.setString(2, this.myEmailID);
			statement.setString(3, this.myPassward);
			statement.setString(4, this.myMailServer);
			statement.setString(5, this.myProtocol);
			statement.setInt(6, this.myPort);
			statement.setBoolean(7, this.isActive());
			statement.setInt(8, this.myCategoryId);
			statement.setInt(9, this.getMyBAMailAcId());
			statement.setString(10, this.getBAEmailAddress());
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			LOG.error("Exception while inserting record in ba-mail acounts.", e);
			return false;
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException sqle) {
					LOG.error("Exception while closing the connection", sqle);
				}
			}
		}
		return true;
	}

	/*
	 * Update the object info in DB
	 * @return true, if updation is successful. Otherwise, return false.
	 */
	public boolean updateToDB() {
		Connection con = null;
		try {
			con = DataSourcePool.getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE ba_mail_accounts "
							+ " SET email_id = ?, passward = ?, mail_server = ?, protocol = ?, port = ?, " 
							+ "is_active = ?, ba_prefix =? , category_id = ?, email_address = ? WHERE ba_mail_ac_id = ?");
			statement.setString(1, this.myEmailID);
			statement.setString(2, this.myPassward);
			statement.setString(3, this.myMailServer);
			statement.setString(4, this.myProtocol); 
			statement.setInt(5, this.myPort);
			statement.setBoolean(6, this.isActive());
			statement.setString(7, this.myBAPrefix);
			statement.setInt(8, this.myCategoryId);
			statement.setString(9, this.getBAEmailAddress());
			statement.setInt(10, this.getMyBAMailAcId());
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LOG.error("Exception while inserting record in ba-mail acounts.", e);
			return false; 
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException sqle) {
					LOG.error("Exception while closing the connection", sqle);
				}
			}
		}
		return true;
	}
	private static Vector<BAMailAccount> allBAMailAccounts = null; 
	private static Hashtable<String, BAMailAccount> allBAMailAccountsByEmail = null;
	public static Vector<BAMailAccount> lookupAll() throws DatabaseException {
		if(allBAMailAccounts == null)
		{
			loadAllAccounts();
		}
		return allBAMailAccounts;
	}
	public static void refreshAccounts()
	{
		try {
			loadAllAccounts();
		} catch (DatabaseException e) {
			LOG.error("Unable to load BA mail accounts.",e);
		}
	}
	
	private static void loadAllAccounts() throws DatabaseException
	{
		allBAMailAccounts = loadAllAccountsDB();
		if (allBAMailAccounts != null) {
			allBAMailAccountsByEmail = new Hashtable<String, BAMailAccount>();
			for (BAMailAccount bam : allBAMailAccounts) {
				if(bam.getBAEmailAddress() != null)
					allBAMailAccountsByEmail.put(bam.getBAEmailAddress(), bam);
			}
		}
	}
	private static Vector<BAMailAccount> loadAllAccountsDB() throws DatabaseException {
		Vector<BAMailAccount> result = new Vector<BAMailAccount>();
		BAMailAccount er = null;
		Connection con = null;
		try {
			con = DataSourcePool.getConnection();

			Statement cs = con.createStatement();
			ResultSet rs = cs.executeQuery("SELECT * from ba_mail_accounts");
			if (rs != null) {
				while (rs.next() == true) {
					er = createFromResultSet(rs);
					result.add(er);
				}

				rs.close();
				rs = null;
			}

			cs.close();
			cs = null;
		} catch (SQLException sqle) {
			StringBuffer message = new StringBuffer();
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException sqle) {
					LOG.error("Exception while closing the connection", sqle);
				}
			}
		}

		return result;
	}

	public static BAMailAccount lookupByEmail(String email)
	{
		if(allBAMailAccounts == null)
		{
			try {
				loadAllAccounts();
			} catch (DatabaseException e) {
				LOG.error("Unable to load BA accounts.", e);
				return null;
			}
		}
		return allBAMailAccountsByEmail.get(email);
	}
	public static ArrayList<BAMailAccount> lookupByBA(String prefix)
			throws DatabaseException {
		ArrayList<BAMailAccount> baMailAccounts = new ArrayList<BAMailAccount>();
		
		Connection con = null;
		try {
			con = DataSourcePool.getConnection();

			PreparedStatement statement = con
					.prepareStatement("SELECT * from ba_mail_accounts where ba_prefix= ?");
			statement.setString(1, prefix);
			ResultSet rs = statement.executeQuery();
			while ((rs != null) && (rs.next())) {
				BAMailAccount mailAccount = createFromResultSet(rs);
				baMailAccounts.add(mailAccount);
			}
			rs.close();
			statement.close();
			statement = null;
		} catch (SQLException sqle) {
			StringBuffer message = new StringBuffer();
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException sqle) {
					LOG.error("Exception while closing the connection", sqle);
				}
			}
		}
		return baMailAccounts;
	}
	public static BAMailAccount lookupByEmailId(String email)
			throws DatabaseException {

		Connection con = null;
		try {
			con = DataSourcePool.getConnection();

			PreparedStatement statement = con
					.prepareStatement("SELECT * from ba_mail_accounts where email_address = ?");
			statement.setString(1, email);
			ResultSet rs = statement.executeQuery();
			if ((rs != null) && (rs.next())) {
				BAMailAccount mailAccount = createFromResultSet(rs);
				return mailAccount;
			}
			rs.close();
			statement.close();
			statement = null;
		} catch (SQLException sqle) {
			StringBuffer message = new StringBuffer();
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException sqle) {
					LOG.error("Exception while closing the connection", sqle);
				}
			}
		}
		return null;
	}
	
	public static boolean updateBAMailAccounts(String sysPrefix, ArrayList<BAMailAccount> toUpdate) throws DatabaseException{
		ArrayList<BAMailAccount> mailAccounts = BAMailAccount.lookupByBA(sysPrefix);
		ArrayList<BAMailAccount> toBeDeleted = new ArrayList<BAMailAccount>();
		for (BAMailAccount mailAccount : mailAccounts){
			boolean flag = false;
			for (BAMailAccount newAcc : toUpdate){
				if (newAcc.myBAMailAcId == mailAccount.myBAMailAcId){
					flag = true;
					break;
				}
			}
			if (!flag){
				toBeDeleted.add(mailAccount);
			} 
		}
		for (BAMailAccount mailAccount : toBeDeleted){
			mailAccount.deleteAc();
		}
		for (BAMailAccount mailAccount : toUpdate){
			mailAccount.SaveToDB();
		}
		return true;
	}
	
// ~--- get methods --------------------------------------------------------
	public String getMyEmailID() {
		return myEmailID;
	}

	public String getMyMailServer() {
		return myMailServer;
	}

	public void setMyMailServer(String myMailServer) {
		this.myMailServer = myMailServer;
	}

	public String getMyBAPrefix() {
		return myBAPrefix;
	}

	public void setMyBAPrefix(String myBAPrefix) {
		this.myBAPrefix = myBAPrefix;
	}

	public String getMyPassward() {
		return myPassward;
	}

	public void setMyPassward(String myPassward) {
		this.myPassward = myPassward;
	}

	public String getMyProtocol() {
		return myProtocol;
	}

	public void setMyProtocol(String protocol) {
		protocol = protocol.toLowerCase();
		if(!isAllowedProtocol(myProtocol))
		{
			throw new IllegalArgumentException("The mail protocol '" + protocol + "' is not supported.");
		}
		this.myProtocol = protocol;
	}

	public void setPort(int myPort) {
		this.myPort = myPort;
	}

	public int getPort() {
		return myPort;
	}

	public void setIsActive(boolean myIsActive) {
		this.myIsActive = myIsActive;
	}

	public boolean isActive() {
		return myIsActive;
	}

	public int getMyBAMailAcId() {
		return myBAMailAcId;
	}

	public boolean deleteAc() {
		Connection con = null;
		try {
			con = DataSourcePool.getConnection();
			PreparedStatement statement = con.prepareStatement("delete from ba_mail_accounts WHERE ba_mail_ac_id = ?");
			statement.setInt(1, this.getMyBAMailAcId());
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LOG.error("Exception while deleting a record in ba-mail acounts.", e);
			return false; 
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException sqle) {
					LOG.error("Exception while closing the connection", sqle);
				}
			}
		}
		return true;
	}
	public int getCategoryId()
	{
		return myCategoryId;
	}

	
	public String getBAEmailAddress() {
		return myBAEmailAddress;
	}
}
