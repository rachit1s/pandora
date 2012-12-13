package commons.com.tbitsGlobal.utils.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import com.google.gwt.user.client.rpc.SerializationException;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.User;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;

/**
 * Class used to intercept the getRequest call to first check whether the user sending the request
 * is a super user or not. Calls are not processed if the user is not a super user.
 * @author devashish
 *
 */
public class AdminUtilServiceImpl extends UtilServiceImpl {
	
	@Override
	public String processCall(String payload) throws SerializationException {
		String userLogin = this.getThreadLocalRequest().getRemoteUser();
		User currentUser;
		try {
			currentUser = User.lookupAllByUserLogin(userLogin);
			HttpServletRequest request = this.getThreadLocalRequest();
			String currentValidatedModule = (String) request.getSession().getAttribute("TBITS_ADMIN");
			if((null == currentValidatedModule) || (currentValidatedModule.trim().equals(""))){
//				if(!isAdminValidForUser("tbits", currentUser)){
//					validateUserForAdmin("tbits", currentUser);
//				}
				
				if(!isModuleExistsForRoot("tbits")){
					validateRootForModule("tbits");
				}
				
				if(!isSuperUser(currentUser, "tbits")){
					throw new TbitsExceptionClient("User ' " + userLogin + " ' Not Authorized to Access This Page");
				}else{
					this.getThreadLocalRequest().getSession().setAttribute("TBITS_ADMIN", "tbits");
				}
			}
			
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (TbitsExceptionClient e) {
			e.printStackTrace();
			return null;
		}
		return super.processCall(payload);
	}
	
	/**
	 * Check if the module name exists with root as its user
	 * @param moduleName
	 * @return
	 */
	private boolean isModuleExistsForRoot(String moduleName){
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "select * from super_users where user_id = ? and module_name = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, 1);
			ps.setString(2, moduleName);
			
			ResultSet rs = ps.executeQuery();
			if(null != rs){
				while(rs.next()){
					Integer userId = rs.getInt("user_id");
					if(1 == userId){
						return true;
					}
				}
			}
		}catch (SQLException e){
			try{
				if(connection != null)
					connection.rollback();		
			}catch(SQLException e1){
				e1.printStackTrace();
			}
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			try {
				if((connection != null) && (!connection.isClosed())){
					connection.close();
				}
			}catch (SQLException e) {
					e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Check if the User is a super user or not
	 * @param currentUser
	 * @return true if superuser; false otherwise
	 */
	private boolean isSuperUser(User currentUser){
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "select * from super_users";
			PreparedStatement ps = connection.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			if(null != rs){
				while(rs.next()){
					Integer userId = rs.getInt("user_id");
					if(currentUser.getUserId() == userId){
						return true;
					}
				}
			}
		}catch (SQLException e){
			try{
				if(connection != null)
					connection.rollback();		
			}catch(SQLException e1){
				e1.printStackTrace();
			}
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			try {
				if((connection != null) && (!connection.isClosed())){
					connection.close();
				}
			}catch (SQLException e) {
					e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Put root as a valid user for the specified module
	 * @param moduleName
	 */
	private void validateRootForModule(String moduleName){
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "insert into super_users " +
					"(user_id, is_active, module_name) " +
					"values(?,?,?)";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, 1);
			ps.setInt(2, 1);
			ps.setString(3, moduleName);
			ps.execute();
			ps.close();
			connection.commit();
		}catch (SQLException e){
			try{
				if(connection != null)
					connection.rollback();		
			}catch(SQLException e1){
				e1.printStackTrace();
			}
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			try {
				if((connection != null) && (!connection.isClosed())){
					connection.close();
				}
			}catch (SQLException e) {
					e.printStackTrace();
			}
		}
	}
	
	/**
	 * Put root as a valid user for the specified module
	 * @param moduleName
	 */
	private void validateUserForAdmin(String moduleName, User user){
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "insert into super_users " +
					"(user_id, is_active, module_name) " +
					"values(?,?,?)";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, user.getUserId());
			ps.setInt(2, 1);
			ps.setString(3, moduleName);
			ps.execute();
			ps.close();
			connection.commit();
		}catch (SQLException e){
			try{
				if(connection != null)
					connection.rollback();		
			}catch(SQLException e1){
				e1.printStackTrace();
			}
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			try {
				if((connection != null) && (!connection.isClosed())){
					connection.close();
				}
			}catch (SQLException e) {
					e.printStackTrace();
			}
		}
	}
	
	/**
	 * Check if the module name exists with root as its user
	 * @param moduleName
	 * @return
	 */
	private boolean isAdminValidForUser(String moduleName, User user){
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "select * from super_users where user_id = ? and module_name = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, user.getUserId());
			ps.setString(2, moduleName);
			
			ResultSet rs = ps.executeQuery();
			if(null != rs){
				while(rs.next()){
					Integer userId = rs.getInt("user_id");
					if(1 == userId){
						return true;
					}
				}
			}
		}catch (SQLException e){
			try{
				if(connection != null)
					connection.rollback();		
			}catch(SQLException e1){
				e1.printStackTrace();
			}
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			try {
				if((connection != null) && (!connection.isClosed())){
					connection.close();
				}
			}catch (SQLException e) {
					e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Check if the User is a super user or not
	 * @param currentUser
	 * @return true if superuser; false otherwise
	 */
	private boolean isSuperUser(User currentUser, String moduleName){
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "select * from super_users where user_id = ? and module_name = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, currentUser.getUserId());
			ps.setString(2, moduleName);
			
			ResultSet rs = ps.executeQuery();
			if(null != rs){
				while(rs.next()){
					Integer userId = rs.getInt("user_id");
					if(currentUser.getUserId() == userId){
						return true;
					}
				}
			}
		}catch (SQLException e){
			try{
				if(connection != null)
					connection.rollback();		
			}catch(SQLException e1){
				e1.printStackTrace();
			}
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			try {
				if((connection != null) && (!connection.isClosed())){
					connection.close();
				}
			}catch (SQLException e) {
					e.printStackTrace();
			}
		}
		return false;
	}
}
