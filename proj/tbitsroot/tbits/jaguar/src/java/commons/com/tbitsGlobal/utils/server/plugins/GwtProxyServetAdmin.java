package commons.com.tbitsGlobal.utils.server.plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.User;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;

/**
 * Servlet where all the requests originating from Admin Modules of Plugins is redirected.
 * This has to check if the user accessing the admin panel is allowed to do so by verifying from
 * super_users table. By default, the user id 1 will be allowed to access all the admin modules.
 * For others, relevant entries have to be provided in the super_users table.
 * @author devashish
 *
 */
public class GwtProxyServetAdmin extends GWTProxyServlet {

	@Override
	public String processCall(String payload) throws SerializationException {
		RPCRequest rpcRequest = RPC.decodeRequest(payload, null, this);
		onAfterRequestDeserialized(rpcRequest);
		String clazz = rpcRequest.getMethod().getDeclaringClass().getName();
		
		String userLogin = this.getThreadLocalRequest().getRemoteUser();
		String moduleName = GWTProxyServletManager.getInstance().getServletForAdmin(clazz).getModuleName();
		Class<? extends TbitsRemoteServiceServlet> servletClazz = GWTProxyServletManager.getInstance().getServletForAdmin(clazz).getServerClass();
		TbitsRemoteServiceServlet servlet = null;
		try{
			HttpServletRequest request = this.getThreadLocalRequest();
			String currentValidatedModule = (String) request.getSession().getAttribute("VALIDATED_MODULE");
			if((null == currentValidatedModule) || (currentValidatedModule.trim().equals(""))){
				if((null != moduleName) && (!moduleName.trim().equals("")) && (moduleName.toLowerCase().contains("admin"))){
					if(!isModuleExistsForRoot(moduleName)){
						validateRootForModule(moduleName);
					}
					User currentUser = User.lookupAllByUserLogin(userLogin);
					if(!isSuperUser(currentUser, moduleName)){
						throw new TbitsExceptionClient("User ' " + userLogin + " ' Not Authorized to Access This Page");
					}else{
						this.getThreadLocalRequest().getSession().setAttribute("VALIDATED_MODULE", moduleName);
					}
				}
			}
			
			servlet = servletClazz.newInstance();
			servlet.init(this.getServletConfig());
			servlet.setRequest(this.getThreadLocalRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (TbitsExceptionClient e) {
			e.printStackTrace();
			return null;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ServletException e) {
			e.printStackTrace();
		}
		
		return RPC.invokeAndEncodeResponse(servlet, rpcRequest.getMethod(), rpcRequest.getParameters(), rpcRequest.getSerializationPolicy());
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
