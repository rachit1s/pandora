/**
 * 
 */
package transbit.tbits.webapps;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import transbit.tbits.Helper.Messages;
import transbit.tbits.admin.AdminUtil;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Role;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

/**
 * @author Lokesh
 *
 */
public class RoleHandler extends HttpServlet {
	
	// Logger that logs information/error messages to the Application Log.
    private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
    
    private static final String TBITS_ADD_ROLE_HTM = "web/tbits-add-role.htm";

	private static final int USERS = 4;
    
	protected void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse)
	throws ServletException, IOException {
		try{
			handleRequest (aRequest, aResponse);
		} catch (DatabaseException e) {
			LOG.severe("",(e));
		} catch (TBitsException e) {
			LOG.severe("",(e));
		}
	}

	protected void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse)
	throws ServletException, IOException {
		try{
			handleRequest (aRequest, aResponse);
		} catch (DatabaseException e) {
			LOG.severe("",(e));
		} catch (TBitsException e) {
			LOG.severe("",(e));
		}
	}
	
	private void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws  DatabaseException, TBitsException, IOException, ServletException{
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();		
		User user = WebUtil.validateUser(request);
		
		String actionType = request.getParameter("actionType");
		if ((actionType == null) || (actionType.trim().equals(""))){
			//TODO return properly
			out.print("Please provide proper action type(\"add/edit/delete\")");
			return;
		}
		else
			actionType = actionType.trim();
		
		WebConfig userConfig = user.getWebConfigObject();        
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(request, userConfig, USERS);
        BusinessArea ba = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }
	    int systemId  = ba.getSystemId();
        String sysPrefix = ba.getSystemPrefix();
			
		if (actionType.equals("delete")){
			String roleName = request.getParameter("roleName");
			if ((roleName == null) || (roleName.trim().equals("")))
				out.print ("Role does not exist or invalid role name.");
			else
				roleName = roleName.trim();
			
			Role role = Role.lookupBySystemIdAndRoleName(systemId, roleName);
			if (role == null)
				out.print ("Role does not exist or invalid role name : " + roleName);

			Role retRole = null ;
			try
			{			
				retRole = Role.delete(role);
				out.print("Role: \"" + retRole.getRoleName() + "\" deleted." );				
			}
			catch(TBitsException e)
			{
				out.print(e.getDescription());
			}
			catch(DatabaseException e )
			{
				out.print("Error occured while deleting the role");
			}			
			return;
		}
		else if (actionType.equals("create")){
			String roleName = request.getParameter("roleName");
			
			if ((roleName == null) || (roleName.trim().equals(""))){
				out.print ("Provide a role name.");
				return;
			}
			else
				roleName = roleName.trim();
			
			String roleDescription = request.getParameter("description");
			roleDescription = ((roleDescription == null)  || 
									roleDescription.trim().equals(""))? 
											roleName : roleDescription.trim();			
			Role newRole = null ; 
			try
			{
				newRole = Role.insert(systemId, roleName, roleDescription,0,1);
			}
			catch(TBitsException te)
			{
				LOG.info("",(te));
				out.print(te.getDescription());
				return ;
			}
			if( null != newRole && newRole.getRoleId() > 0 )
			{
				out.print("Role (" + newRole.getRoleName() + ") created. Please close this window.");
			}
			else
			{
				out.print("Error occured while creating the role") ;
			}
			return;
		}		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
