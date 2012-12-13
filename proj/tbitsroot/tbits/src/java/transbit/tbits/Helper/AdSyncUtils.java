/*
 * AdSyncUtils.java
 *
 * Created on October 11, 2006, 4:51 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package transbit.tbits.Helper;

import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import transbit.tbits.domain.User;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.DataSourcePool;

//Static Imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_UTIL;
//import static transbit.tbits.domain.UserType.INTERNAL_CONTACT;
import static transbit.tbits.domain.UserType.INTERNAL_MAILINGLIST;
//import static transbit.tbits.domain.UserType.INTERNAL_USER;
/**
 *
 * @author Administrator
 */
public class AdSyncUtils {

    // Application logger to log the messages.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_UTIL);

    /**
     * This method updates the user object in the database.
     *
     * @param user
     */
    public static void updateUser(User user) {
        Connection con = null;

        try {

            // Obtain a connection from the pool.
            con = DataSourcePool.getConnection();
            con.setAutoCommit(true);

            // Execute the procedure to get the list of all users.
            CallableStatement cs = con.prepareCall("stp_adsync_updateUser " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?  ");

            user.setCallableParameters(cs);
            cs.execute();
        } catch (SQLException sqle) {
        	try {
        		if(con != null)
					con.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            LOG.info("Exception while updating a new user in the database:" + "\nUserLogin: " + user.getUserLogin() + "\n" + "",(sqle));
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException sqle) {

                // Ignore this exception. Good to log this as INFO.
                LOG.info(sqle.toString());
            }
        }

        return;
    }
    /**
     * This method creates an ADEntry obtaining the values for the data members
     * from the Attributes object passed.
     *
     * @param attr      Object that holds the attributes of this Entry in AD.
     * @param entryType Type of entry: User/Group/Contact.
     * @return          ADEntry object
     * @throws NamingException Incase of any naming-exceptions.
     */
    public static ADEntry toADEntry(Attributes attr, int entryType) throws NamingException {
        ADEntry entry = null;

        entry = new ADEntry(entryType, getAllValues(attr, "name"), getAllValues(attr, "sn"), getAllValues(attr, "givenName"), getAllValues(attr, "displayName"), getAllValues(attr, "mailNickname"),
                            getAllValues(attr, "mail"), getAllValues(attr, "objectClass"), getAllValues(attr, "showInAddressBook"), getAllValues(attr, "sAMAccountName"),
                            getAllValues(attr, "physicalDeliveryOfficeName"), getAllValues(attr, "telephoneNumber"), getAllValues(attr, "mobile"), getAllValues(attr, "homePhone"));

        return entry;
    }
   
    //~--- get methods --------------------------------------------------------
    
    /**
     * This method returns the value returned for this key in the Attributes
     * object. If the key has multiple values, then a semicolon separated list
     * of values is returned. If the value is null, then empty string is
     * returned.
     *
     * @param attr      Attributes object.
     * @param key       Key, whose value is needed.
     * @return          Value for this key in the attributes object.
     * @throws NamingException Incase of any exceptions.
     */
    public static String getAllValues(Attributes attr, String key) throws NamingException {
        StringBuffer value = new StringBuffer();
        
        if (attr != null) {
            Attribute nameAttr = attr.get(key);
            
            if (nameAttr != null) {
                NamingEnumeration ne    = nameAttr.getAll();
                boolean           first = true;
                
                while (ne.hasMoreElements() == true) {
                    if (first == false) {
                        value.append(";");
                    } else {
                        first = false;
                    }
                    
                    value.append(ne.nextElement().toString());
                }
            }
        }
        
        return value.toString();
    }
    
    /**
     * This method returns the value returned for this key in the Attributes
     * object. If the key has multiple values, then an ArrayList of values
     * is returned. If the value is null, then empty string is returned.
     *
     * @param attr      Attributes object.
     * @param key       Key, whose value is needed.
     * @return          Value for this key in the attributes object.
     * @throws NamingException Incase of any exceptions.
     */
    public static ArrayList<String> getList(Attributes attr, String key) throws NamingException {
        ArrayList<String> list = new ArrayList<String>();
        
        if (attr != null) {
            Attribute nameAttr = attr.get(key);
            
            if (nameAttr != null) {
                NamingEnumeration ne = nameAttr.getAll();
                
                while (ne.hasMoreElements() == true) {
                    list.add(ne.nextElement().toString());
                }
            }
        }
        
        return list;
    }
    
    /**
     * This method returns the value returned for this key in the Attributes
     * object. If the key has multiple values, then the first value is
     * returned. If the value is null, then empty string is returned.
     *
     * @param attr      Attributes object.
     * @param key       Key, whose value is needed.
     * @return          Value for this key in the attributes object.
     * @throws NamingException Incase of any exceptions.
     */
    public static String getValue(Attributes attr, String key) throws NamingException {
        String value = "";
        
        if (attr != null) {
            Attribute nameAttr = attr.get(key);
            
            if (nameAttr != null) {
                value = nameAttr.get().toString();
            }
        }
        
        return value;
    }
    
    /**
     * This method checks if there is a change in the entry.
     *
     * @param adEntry Entry from DB.
     * @param dbEntry Entry from AD.
     * @param userTypeId Type of user.
     * @return True if there is a change in the attributes. False otherwise.
     */
    public static boolean isChanged(ADEntry adEntry, User dbEntry, int userTypeId) {
        
        /*
         * This method checks if any of the following properties of the DBEntry
         * are changed in the AD.
         */
        boolean changed = false;
        
        // Check the first name if this is not a mailing list entry.
        if (userTypeId !=  INTERNAL_MAILINGLIST) {
            if (adEntry.getGivenName().equals(dbEntry.getFirstName()) == false) {
                dbEntry.setFirstName(adEntry.getGivenName());
                changed = true;
            }
        }
        
        // Check the last name if this is not a mailing list entry.
        if (userTypeId != INTERNAL_MAILINGLIST) {
            if (adEntry.getSurName().equals(dbEntry.getLastName()) == false) {
                dbEntry.setLastName(adEntry.getSurName());
                changed = true;
            }
        }
        
        // Check the display name.
        if (adEntry.getDisplayName().equals(dbEntry.getDisplayName()) == false) {
            dbEntry.setDisplayName(adEntry.getDisplayName());
            changed = true;
        }
        
        // Check the email.
        if (adEntry.getMail().equals(dbEntry.getEmail()) == false) {
            dbEntry.setEmail(adEntry.getMail());
            changed = true;
        }
        
        // Check if the dbEntry is inactive. If so mark it as active.
        if (dbEntry.getIsActive() == false) {
            dbEntry.setIsActive(true);
            changed = true;
        }
        
        // Check if there is a change in the location.
        if (adEntry.getLocation().equals(dbEntry.getLocation()) == false) {
            dbEntry.setLocation(adEntry.getLocation());
            changed = true;
        }
        
        // Check if there is a change in the extension number.
        if (adEntry.getExtension().equals(dbEntry.getExtension()) == false) {
            dbEntry.setExtension(adEntry.getExtension());
            changed = true;
        }
        
        // Check if there is a change in the mobile number.
        if (adEntry.getMobile().equals(dbEntry.getMobile()) == false) {
            dbEntry.setMobile(adEntry.getMobile());
            changed = true;
        }
        
        // Check if there is a change in the Home phone number.
        if (adEntry.getHomePhone().equals(dbEntry.getHomePhone()) == false) {
            dbEntry.setHomePhone(adEntry.getHomePhone());
            changed = true;
        }
        
        return changed;
    }
}
