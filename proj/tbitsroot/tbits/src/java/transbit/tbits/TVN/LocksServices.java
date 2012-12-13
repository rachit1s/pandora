package transbit.tbits.TVN;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.util.MD5Encoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import transbit.tbits.common.DataSourcePool;

import com.tbitsglobal.tvncore.SVNXMLElement;
import com.tbitsglobal.tvncore.TvnException;
import com.tbitsglobal.tvncore.TvnLockInfo;
import com.tbitsglobal.tvncore.UserInfo;
import com.tbitsglobal.tvncore.Utils;

/**
 * Manages all the interaction and services related to locking of paths
 * 
 * @author karan
 *
 */

public class LocksServices {

	//====================================================================================
	
	private static final String LOCKS_TABLE = "locks";
	public static final String SYS_ID = "sys_id";
	public static final String REQUEST_ID = "request_id";
	public static final String FIELD_ID = "field_id";
	public static final String REQUEST_FILE_ID = "request_file_id";
	
	//====================================================================================
	 
	 public static TvnLockInfo getLockInfo(String path) {
		 
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			
			HashMap<String, Integer> params = Services.getParamsFromPath(path);
			TvnLockInfo lock = null;
			
			String sql = "SELECT token, path, type, scope, owner, comment, creation_date, depth, sys_id, request_id, field_id, request_file_id" +
						" FROM " + LOCKS_TABLE + 
						" WHERE sys_id= ? and request_id=? and field_id=? and request_file_id=?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, params.get(SYS_ID));
			ps.setInt(2, params.get(REQUEST_ID));
			ps.setInt(3, params.get(FIELD_ID));
			ps.setInt(4, params.get(REQUEST_FILE_ID));
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				lock = new TvnLockInfo(rs.getString(2),rs.getString(3),rs.getString(4),rs.getInt(8),
									rs.getString(5),rs.getString(1),rs.getString(6));
				lock.creationDate = rs.getTimestamp(7);
			}
			if(null != rs)
				rs.close();
			ps.close();
			return lock;
		} catch (Exception e) {
			// ignore this exception
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	//====================================================================================

	public static boolean insertLockInfo(TvnLockInfo lock) {
		if(lock == null) {
			System.err.println("Cannot Insert Null Lock Into Database");
			return false;
		}
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			String sql = "INSERT INTO " + LOCKS_TABLE + 
							"( token, path, type, scope, owner, comment, creation_date, depth, sys_id, request_id, field_id, request_file_id)" + 
							"  VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
			
			PreparedStatement ps = conn.prepareStatement(sql);
			
			HashMap<String, Integer> params = Services.getParamsFromPath(lock.path);
			
			ps.setString(1, lock.token);
			ps.setString(2,lock.path);
			ps.setString(3,lock.type);
			ps.setString(4,lock.scope);
			ps.setString(5,lock.owner);
			ps.setString(6,lock.comment);
			ps.setTimestamp(7,new Timestamp(lock.creationDate.getTime()));
			ps.setString(8, String.valueOf(lock.depth));
			ps.setInt(9, params.get(SYS_ID));
			ps.setInt(10, params.get(REQUEST_ID));
			ps.setInt(11, params.get(FIELD_ID));
			ps.setInt(12, params.get(REQUEST_FILE_ID));
			
			int result = ps.executeUpdate();
			ps.close();
			if(result != 0)
				return true;
			
		} catch (Exception e) {
			System.err.println("Error in inserting the lock info");
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return false;
	}
	
	//====================================================================================

	public static boolean removeLock(String path) {
		
		if(path == null)
			return false;
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			
			HashMap<String, Integer> params = Services.getParamsFromPath(path);
			
			String sql = "DELETE FROM " + LOCKS_TABLE + " WHERE sys_id=? and request_id=? and field_id=? and request_file_id=?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, params.get(SYS_ID));
			ps.setInt(2, params.get(REQUEST_ID));
			ps.setInt(3, params.get(FIELD_ID));
			ps.setInt(4, params.get(REQUEST_FILE_ID));
			int result = ps.executeUpdate();
			ps.close();
			if(result != 0)
				return true;
		} catch (Exception e) {
			// ignore this exception
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
		
	}

	//====================================================================================

	public static boolean hasRightsTo(UserInfo user, String path) {
		
		TvnLockInfo lock = getLockInfo(path);
		
		if(lock == null)
			return true;
		else if(lock.owner.equals(user.getUserLogin()))
			return true;
		return false;
	}

	//====================================================================================

	public static String addLock(String path, UserInfo user, HttpServletRequest request) {
		
		String locktype = null;
		String lockMessage = "";
		String lockscope = null;
		
		Document document = null;
		try {
			document = Utils.readRequest(request);
		} 
		catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		catch (TvnException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Element rootElement = document.getDocumentElement();
		NodeList childNodes = rootElement.getChildNodes();
		for(int i=0;i<childNodes.getLength();i++) {
			Node currentNode = childNodes.item(i);
			if(currentNode.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			if(currentNode.getLocalName().equals(SVNXMLElement.LOCK_TYPE.getName())) {
				locktype = currentNode.getFirstChild().getLocalName();
			}
			else if(currentNode.getLocalName().equals(SVNXMLElement.LOCK_SCOPE.getName())) {
				lockscope = currentNode.getFirstChild().getLocalName();
			}
			else if(currentNode.getLocalName().equals(SVNXMLElement.LOCK_OWNER.getName())) {
				lockMessage = currentNode.getTextContent();
			}
		}
		
		String lockTokenStr = request.getServletPath() + "-" + locktype + "-"
								+ lockscope + "-" + user.getUserLogin() + "-" + System.currentTimeMillis();
		String lockToken = "";
		try {
			MD5Encoder md5Encoder = new MD5Encoder();
			MessageDigest md5Helper = MessageDigest.getInstance("MD5");
			lockToken = md5Encoder.encode(md5Helper.digest(lockTokenStr.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
		}
		
		TvnLockInfo newLock = new TvnLockInfo(path,locktype,lockscope,0,user.getUserLogin(),lockToken,lockMessage);
		boolean result = insertLockInfo(newLock);
		
		if(result)
			return lockToken;
		else
			return null;
	}
	
	//====================================================================================
	
}
