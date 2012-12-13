/*
 * AuthUtils.java
 *
 * Created on September 29, 2006, 1:37 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package transbit.tbits.authentication;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import sun.misc.BASE64Encoder;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.events.BeforePasswordChangeEvent;
import transbit.tbits.events.EventFailureException;
import transbit.tbits.events.EventManager;

/**
 *
 * @author Administrator
 */
public class AuthUtils {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_WEBAPPS);
    /** Creates a new instance of AuthUtils */
    public AuthUtils() {
    }
    
    public static void setPassword(String user, String password)throws DatabaseException, EventFailureException
    {
    	BeforePasswordChangeEvent bpce = new BeforePasswordChangeEvent(password, user);
    	EventManager.getInstance().fireEvent(bpce);
    	Connection connection = null;
        try {
            connection = DataSourcePool.getConnection();
            CallableStatement cs = connection.prepareCall("stp_admin_set_user_password ?, ?");

            cs.setString(1, user);
            cs.setString(2, encrypt(password));
            cs.execute();
            cs.close();
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();
            message.append("An exception occurred while setting user's authentication details.").append("\nUser Login: ").append(user).append("\n");
            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.info("Exception while closing the connection");
            }
        }
    }
    
    /*
     * User for encrypting a password.
     * TODO: Need to make it work.
     */
    public static String encrypt(String str)
    {
    	return new BASE64Encoder().encode(str.getBytes());
    }
    
    public static boolean validateUser(String user, String password) throws DatabaseException
    {
        Connection connection = null;
        boolean isValidUser = false;
        try {
            connection = DataSourcePool.getConnection();
            CallableStatement cs = connection.prepareCall("stp_user_lookupByLoginPassword ?, ?");

            cs.setString(1, user);
            cs.setString(2, encrypt(password));
            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    isValidUser = true;
                }
                rs.close();
            }

            cs.close();
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occurred while retrieving the user's authentication details.").append("\nUser Login: ").append(user).append("\n");
            throw new DatabaseException(message.toString(), sqle);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.info("Exception while closing the connection");
            }
        }
        return isValidUser;
    }
}
