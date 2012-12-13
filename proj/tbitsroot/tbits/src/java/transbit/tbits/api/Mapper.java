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
 * Mapper.java
 * $Header:
 */
package transbit.tbits.api;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.DataSourcePool;

//TBits Imports
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BARule;
import transbit.tbits.domain.BAUser;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DateTimeFormat;
import transbit.tbits.domain.Dependency;
import transbit.tbits.domain.DependentField;
import transbit.tbits.domain.ExclusionList;
import transbit.tbits.domain.ExternalResource;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.FieldDescriptor;
import transbit.tbits.domain.FieldProperties;
import transbit.tbits.domain.HolidaysList;
import transbit.tbits.domain.NotificationRule;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.TypeDescriptor;
import transbit.tbits.domain.TypeUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.WorkflowRule;

//~--- JDK imports ------------------------------------------------------------

//Java imports.
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

//~--- classes ----------------------------------------------------------------

/**
 * This class maps field_id, type_id, user_id to their corresponding objects.
 *
 * @author  : Vinod
 * @version : $Id: $
 */
public final class Mapper implements TBitsConstants {

    /*
     * Used to log application messages
     */
    public static final TBitsLogger LOG               = TBitsLogger.getLogger(PKG_API);
    private static boolean          ourIsMapperLoaded = false;

    /*
     * The following variables hold the BA information which is needed if the
     * mapper is invoked because of an email or from command line. In these
     * two cases, only the BA specific meta data is loaded into the mapper.
     */
//    private static int       ourSystemId   = -1;
    private static final int USER_INTERVAL = 30;
    private static final int SEC_TO_MIN    = 60;

    /*
     * Time related constants.
     */
    private static final int MILLIS_TO_SEC = 1000;
    private static final int GC_INTERVAL   = 60;
    private static final int BO_INTERVAL   = 60;

    /*
     * Timer task that refreshes the BusinessObjects.
     */
    private static BOMapBuilder boRefresher;

    /*
     * Timer task that run garbage collector.
     */
    private static GCRunner gcRunner;

    /*
     * 1. ourBAMap maps the BA ID/Name/DisplayName/Email to the corresponding
     *    business area object.
     */
    public static Map<String, BusinessArea> ourBAMap;

    /*
     * Maps the workflow rule id and BA ID to the BARule object.
     */
    public static Map<String, ArrayList<BARule>> ourBARuleMap;

    /*
     * ourBAUserListMap maps the business area to the corresponding list of
     * BAUsers.
     */
    public static Map<String, ArrayList<BAUser>> ourBAUserListMap;

    /*
     * Maps the date format ids to the corresponding objects.
     */
    public static Map<String, DateTimeFormat> ourDateFormatMap;

    /*
     * Dependency List Mapper.
     */
    public static Map<String, ArrayList<Dependency>> ourDependencyMap;

    /*
     * Dependendent Field Map.
     */
    public static Map<String, ArrayList<DependentField>> ourDependentFieldMap;
    public static Map<String, Field>                     ourDistinctFieldTable;

    /*
     * This maps the business area and user to the exclusion list object.
     */
    public static Map<String, ArrayList<ExclusionList>> ourExclusionListMap;

    /*
     * External resource mapper.
     */
    public static Map<String, ExternalResource>           ourExternalResourceMap;
    public static Map<String, ArrayList<FieldDescriptor>> ourFieldDescListMap;

    /*
     * 1. ourFieldDescMap maps the field descriptor to the corresponding field
     *    descriptor object in the given business area.
     *
     * 2. ourFieldDescListMap maps the field name/display name with the list of
     *    field descriptors corresponding to this field in a given business area
     */
    public static Map<String, FieldDescriptor>   ourFieldDescMap;
    public static Map<Integer, ArrayList<Field>> ourFieldListMap;
    
    /*
     * 1. ourFieldPropertyMap maps the field property's hashcode to the corresponding field
     *    property object.
     *
     * 2. ourFieldPropertyListMap maps the list of
     *    field properties in a given business area
     */
    public static Map<Integer, ArrayList<FieldProperties>> ourFieldPropertyMap;
    
    //public static Map<Integer, ArrayList<Field>> ourDisplayGroupListMap;

    /*
     * 1. ourFieldMap maps the Field ID/Name/Display Name to the corresponding
     *    field in the business area.
     *
     * 2. ourFieldListMap maps the BA ID with the corresponding list of field
     *    objects.
     *
     * 3. ourDistinctFieldTable holds the field objects per business area.
     */
    public static Map<String, Field>                   ourFieldMap;
    public static Map<String, ArrayList<HolidaysList>> ourHolidaysListMap;

    /*
     * Holidays List mapper.
     */
    public static Map<String, HolidaysList> ourHolidaysMap;

    /*
     * 1. ourMailListUserMap maps the mailing list ID to the list of immediate
     *    members of this list.
     *
     * 2. ourUserMailListMap maps the user to the list of mailing lists where
     *    the user is a direct member.
     */
    public static Map<Integer, ArrayList<User>>            ourMailListUserMap;
    public static Map<String, ArrayList<NotificationRule>> ourNotificationRuleListMap;

    /*
     * Notification Rules Mapper.
     */
    public static Map<String, NotificationRule> ourNotificationRuleMap;

    /*
     * Role User Map.
     */
    public static Map<String, ArrayList<RoleUser>> ourRoleUserMap;

    /*
     * This variable holds the type of invocation of this Mapper. It can be
     *     - Web
     *     - Email
     *     - Command Line.
     */
//    private static int    ourSource;
    private static String ourSysName;
    private static String ourSysPrefix;

    /*
     * Finally, the timer that runs the jobs periodically.
     */
    private static Timer                                 ourTimer;
    public static Map<String, ArrayList<TypeDescriptor>> ourTypeDescListMap;

    /*
     * 1. ourTypeDescMap maps a type descriptor to the corresponding descriptor
     *    object.
     *
     * 2. ourTypeDescListMap maps the type id/name/display name to the list
     *    of type descriptors.
     */
    public static Map<String, TypeDescriptor> ourTypeDescMap;

    /*
     * 1. ourTypeMap maps the type of a field to the corresponding type object.
     *
     * 2. ourTypeListMap maps the field id/field name in a BA to the list of
     *    type objects corresponding to this field.
     */
    public static Map<String, Type>                ourTypeIdMap;
    public static Map<String, ArrayList<Type>>     ourTypeListMap;
    public static Map<String, Type>                ourTypeNameMap;
    public static Map<String, ArrayList<TypeUser>> ourTypeUserListMap;

    /*
     * 1. ourTypeUserMap maps the type_id + user_id to the type user object
     *    for a field in a business area.
     *
     * 2. ourTypeUserListMap maps the type_id to the list of type users for a
     *    field in a business area.
     */
    public static Map<String, TypeUser>         ourTypeUserMap;
    public static StringBuilder                 ourUserInfo;
    public static Map<Integer, ArrayList<User>> ourUserMailListMap;

    /*
     * 1. ourUserMap maps the UserID/Login/Email/Email without .transbittech.com to
     *    the corresponding user object.
     *
     * 2. ourUserInfo holds the user information in JavaScript Object Notation
     *    (JSON) which is used by the auto-complete feature in the web.
     */
    public static Hashtable<Integer,User> ourUserMap = new Hashtable<Integer,User>() ;
    public static Hashtable<String,User> ourUserEmailMap = new Hashtable<String, User>() ;
    public static Hashtable<String,User> ourUserLoginMap = new Hashtable<String, User>() ;
    /*
     * Maps the workflow rule ids to the corresponding objects.
     */
    public static Map<String, WorkflowRule> ourWorkflowRuleMap;

    /*
     * Timer task that refreshes the User mapper.
     */
    private static UserMapBuilder userRefresher;

    //~--- static initializers ------------------------------------------------

    static {
        long start = System.currentTimeMillis();

        load();

        long end = System.currentTimeMillis();

        LOG.info("Time Taken to load Mapper: " + (end - start) + " msecs");
    }

    //~--- methods ------------------------------------------------------------

    /**
     * This method loads the mapper for the first time.
     */
    public static synchronized void load() {
        if (ourIsMapperLoaded == true) {
            return;
        }

        /*
         * Let us assume that this is an invocation from a web process.
         */
//        ourSource = SOURCE_WEB;

//        try {
//
//            /*
//             * Check if this is an email invocation.
//             */
//            ourSysName = PropertiesHandler.getProperty(PROP_BA_NAME);
//            ourSource  = SOURCE_EMAIL;
//            LOG.debug("E-mail invocation");
//        } catch (IllegalArgumentException iae) {
//
//            // This is not for email invocation.
//        }

        /*
         * If ourSource is still web, check if this is cmdline invocation.
         */
//        if (ourSource == SOURCE_WEB) {
//            try {
//
//                /*
//                 * Check if this is to be built for an email invocation.
//                 */
//            	ourSysPrefix = PropertiesHandler.getProperty(PROP_BA_PREFIX);
//                ourSource    = SOURCE_CMDLINE;
//                LOG.debug("Command-line invocation");
//            } catch (IllegalArgumentException iae) {
//
//                /*
//                 * This is not for command-line invocation.
//                 */
//            }
//        }

        /*
         * Depending on the source, we decide whether to schedule the mapper
         * for periodical refreshing or just instantiate it.
         */
//        if ((ourSource == SOURCE_EMAIL) || (ourSource == SOURCE_CMDLINE)) {
//
//            /*
//             * We shall set up the mapper and leave because, the tasks
//             * initiated from email/cmdline span for a few minutes only.
//             */
//            userRefresher = new UserMapBuilder();
//            userRefresher.run();
//            boRefresher = new BOMapBuilder();
//            boRefresher.run();
//        } else {

            /*
             * This is an invocation for web application. So, we need to
             * set up the mapper and schedule it to refresh periodically.
             */
            userRefresher = new UserMapBuilder();
            userRefresher.run();

            long userDelay = MILLIS_TO_SEC * SEC_TO_MIN * USER_INTERVAL;

            boRefresher = new BOMapBuilder();
            boRefresher.run();

            long boDelay = MILLIS_TO_SEC * SEC_TO_MIN * BO_INTERVAL;

            gcRunner = new GCRunner();

            long gcDelay = MILLIS_TO_SEC * SEC_TO_MIN * GC_INTERVAL;

            ourTimer = new Timer();

            /*
             * Schedule the the timer tasks with the predefined periodicity.
             */
            ourTimer.schedule(userRefresher, userDelay, userDelay);
            ourTimer.schedule(boRefresher, boDelay, boDelay);
            ourTimer.schedule(gcRunner, gcDelay, gcDelay);

        /*
         * Mark that mapper is completely loaded so that another thread that
         * tries to initialize the mapper at the same time will return.
         */
        ourIsMapperLoaded = true;
    }

    /**
     * Main method - Entry point into the program from command-line.
     */
    public static void main(String arg[]) {}

    /**
     * This method refreshes the BA Map.
     */
    public static void refreshBAMap() {
        Connection con = null;

        try {
            con      = DataSourcePool.getConnection();
            ourBAMap = boRefresher.buildBAMap(con);
        } catch (SQLException sqle) {
            LOG.severe("Exception while refreshing the BA Mapper.\n" + "",(sqle));
        }
        finally
        {
        	try {
				if(con != null)
					con.close();
			} catch (SQLException e) {
				LOG.warn("Unable to close the connection in refreshBAMap");
			}
        }
    }

    /**
     * This method makes an explicit call to refresh the bo mapper.
     *
     */
    public static void refreshBOMapper() {
        boRefresher.run();
    }

    /**
     * This method makes an explicit call to refresh the user mapper.
     *
     */
    public static void refreshUserMapper() {
        userRefresher.run();
    }

    /**
     * This method invokes the garbage collector.
     *
     */
    public static void runGC() {
        System.gc();
    }

    /**
     * This method stops the mapper by cancelling the timer tasks and the
     * timers..
     */
    public static void stop() {
        if (userRefresher != null) {
            userRefresher.cancel();
        }

        if (boRefresher != null) {
            boRefresher.cancel();
        }

        if (gcRunner != null) {
            gcRunner.cancel();
        }

        if (ourTimer != null) {
            ourTimer.cancel();
        }
    }

    /**
     */
    public static void updateBA(BusinessArea aBusinessArea) {
        if (aBusinessArea == null) {
            return;
        }

        /*
         * Store <SystemId, BA>
         */
        String key = Integer.toString(aBusinessArea.getSystemId());

        ourBAMap.put(key, aBusinessArea);

        /*
         * Store <NAME, BA>
         */
        key = aBusinessArea.getName().toUpperCase();
        ourBAMap.put(key, aBusinessArea);

        /*
         * Store <SYSPREFIX, BA>
         */
        key = aBusinessArea.getSystemPrefix().toUpperCase();
        ourBAMap.put(key, aBusinessArea);

        /*
         * Store <EMAIL, BA>
         */
        key = aBusinessArea.getEmail().toUpperCase();
        ourBAMap.put(key, aBusinessArea);

        /*
         * Store <EMAIL.USERNAME, BA>
         */
        /*int index = key.indexOf('@');

        if (index > 0) {
            key = key.substring(0, index);
            ourBAMap.put(key, aBusinessArea);
        }*/

        return;
    }

    /**
     * Method to update the user information in the mapper.
     */
    public static void updateUser(User aUser) {
        if (aUser == null) {
            return;
        }

        aUser.setWebConfigObject(null);

        /*
         * Store <UserId, User> pair
         */
        String key = Integer.toString(aUser.getUserId());

        ourUserMap.put(new Integer(key), aUser);

        /*
         * Store <USERLOGIN, User> Pair
         */
        key = aUser.getUserLogin().toUpperCase();
        ourUserLoginMap.put(key, aUser);

        /*
         * Store <EMAIL, User> Pair
         */
        key = aUser.getEmail().toUpperCase();
        ourUserEmailMap.put(key, aUser);
        
        for(String otherEmail:aUser.getOtherEmails())
        {
        	ourUserEmailMap.put(otherEmail, aUser);
        }
        
        return;
    }

    //~--- inner classes ------------------------------------------------------

    /**
     * This class builds the mappers for Business Objects.
     *
     * $version : $Id: $
     */
    private static class BOMapBuilder extends TimerTask {

        /**
         * Default constructor.
         */
        public BOMapBuilder() {}

        //~--- methods --------------------------------------------------------

        /**
         * This method builds and returns a Business Area map.
         *
         * @param aCon   connection object to the database.
         *
         * @return BusinessArea Map completely built.
         *
         * @throws SQLException incase of any database errors.
         *
         */
        public Map<String, BusinessArea> buildBAMap(Connection aCon) throws SQLException {
            Map<String, BusinessArea> aBAMap = Collections.synchronizedMap(new HashMap<String, BusinessArea>());

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement cs = null;

            cs = aCon.prepareCall("stp_ba_getAllBusinessAreas");

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    String       key;
                    BusinessArea value = BusinessArea.createFromResultSet(rs);

                    /*
                     * Store <SystemId, BA>
                     */
                    key = Integer.toString(value.getSystemId());
                    aBAMap.put(key, value);

                    /*
                     * Store <NAME, BA>
                     */
                    key = value.getName().toUpperCase();
                    aBAMap.put(key, value);

                    /*
                     * Store <SYSPREFIX, BA>
                     */
                    key = value.getSystemPrefix().toUpperCase();
                    aBAMap.put(key, value);

                    /*
                     * Store <EMAIL, BA>
                     */
                    key = value.getEmail().toUpperCase();
                    if(key.length() > 0)
                    	aBAMap.put(key, value);

                    /*
                     * Store <EMAIL.USERNAME, BA>
                     */
                   /* int index = key.indexOf('@');

                    if (index > 0) {
                        key = key.substring(0, index);
                        aBAMap.put(key, value);
                    }*/

                    /*
                     * Capture the system id incase this is an email/command
                     * line invocation. This system id is need while loading the
                     * meta data. In these two types of invocations, only the
                     * meta data specific to the business area is loaded.
                     */
                                                            		
//                    if ((ourSource == SOURCE_EMAIL) && (value.getSystemPrefix().equalsIgnoreCase(ourSysName) == true)) {
//                        ourSystemId = value.getSystemId();
//                        
//                    } else if ((ourSource == SOURCE_CMDLINE) && (value.getSystemPrefix().equalsIgnoreCase(ourSysPrefix) == true)) {
//                        ourSystemId = value.getSystemId();
//                       
//                    }
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;

            return aBAMap;
        }

        /**
         * This method builds and returns a BARule map.
         *
         * @param aCon   connection object to the database.
         *
         * @return BARule Map completely built.
         *
         * @throws SQLException incase of any database errors.
         *
         */
        public Map<String, ArrayList<BARule>> buildBARuleMap(Connection aCon) throws SQLException, DatabaseException {
            Map<String, ArrayList<BARule>> aMap = Collections.synchronizedMap(new HashMap<String, ArrayList<BARule>>());

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement cs = null;

            cs = aCon.prepareCall("stp_barules_getAllBARules");

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    String key;
                    BARule value = BARule.createFromResultSet(rs);

                    /*
                     * Store (Sys_id, ArrayList<BARule>)
                     */
                    key = Integer.toString(value.getSystemId());

                    ArrayList<BARule> ruleList = aMap.get(key);

                    /*
                     * Check if we already have a list for this key.
                     */
                    if (ruleList == null) {
                        ruleList = new ArrayList<BARule>();
                    }

                    ruleList.add(value);
                    aMap.put(key, ruleList);
                    ruleList = null;
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;

            return aMap;
        }

        /**
         * This method builds and returns a BAUser map.
         *
         * @param aCon   connection object to the database.
         *
         * @return BAUser Map completely built.
         *
         * @throws SQLException incase of any database errors.
         *
         */
        public Map<String, ArrayList<BAUser>> buildBAUserMap(Connection aCon) throws SQLException {
            Map<String, ArrayList<BAUser>> aMap = Collections.synchronizedMap(new HashMap<String, ArrayList<BAUser>>());

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement cs = null;

           // if ((ourSource == SOURCE_EMAIL) || (ourSource == SOURCE_CMDLINE)) {
            //    cs = aCon.prepareCall("stp_bauser_lookupBySystemId ? ");
            //    cs.setInt(1, ourSystemId);
           // } else {
//                ourSource = SOURCE_WEB;
                cs        = aCon.prepareCall("stp_bauser_getAllBAUsers");
           // }

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    String key;
                    BAUser value = BAUser.createFromResultSet(rs);

                    /*
                     * Store (Sys_id, ArrayList<BAUser>)
                     */
                    key = Integer.toString(value.getSystemId());

                    ArrayList<BAUser> baUserList = aMap.get(key);

                    /*
                     * Check if we already have a list for this key.
                     */
                    if (baUserList == null) {
                        baUserList = new ArrayList<BAUser>();
                    }

                    baUserList.add(value);
                    aMap.put(key, baUserList);
                    baUserList = null;

                    /*
                     * Store ( Sys_id - User_Id, BAUser)
                     */
                    key        = "SystemId: " + Integer.toString(value.getSystemId()) + " - " + "UserId: " + Integer.toString(value.getUserId());
                    baUserList = new ArrayList<BAUser>();
                    baUserList.add(value);
                    aMap.put(key, baUserList);
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;

            return aMap;
        }

        /**
         * This method builds and returns a DateTimeFormat map.
         *
         * @param aCon   connection object to the database.
         *
         * @return DateTimeFormat Map completely built.
         *
         * @throws SQLException incase of any database errors.
         *
         */
        public Map<String, DateTimeFormat> buildDateFormatMap(Connection aCon) throws SQLException {
            Map<String, DateTimeFormat> aMap = Collections.synchronizedMap(new HashMap<String, DateTimeFormat>());

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement cs = null;

            cs = aCon.prepareCall("stp_dtf_getAllDateTimeFormats");

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    String         key;
                    DateTimeFormat val = DateTimeFormat.createFromResultSet(rs);

                    /*
                     * Store (datetime_format_id, DateTimeFormat)
                     */
                    key = Integer.toString(val.getFormatId());
                    aMap.put(key, val);
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;

            return aMap;
        }

        /**
         * This method builds and returns a Dependancy map.
         *
         * @param aCon connection object to the database.
         *
         * @return UserMap completely built.
         *
         * @throws SQLException incase of any database errors.
         */
        public Map<String, ArrayList<Dependency>> buildDependencyMap(Connection aCon) throws SQLException {
            Map<String, ArrayList<Dependency>> aDepMap = Collections.synchronizedMap(new HashMap<String, ArrayList<Dependency>>());

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement stmt = aCon.prepareCall("stp_dependencies_getAllDependencies");
            ResultSet         rs   = stmt.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    boolean first = true;

                    do {
                        Dependency            value = Dependency.createFromResultSet(rs);
                        String                key;
                        ArrayList<Dependency> list;

                        // Store <SystemId, Dependency> Pair
                        key  = Integer.toString(value.getSystemId());
                        list = aDepMap.get(key);

                        if (list == null) {
                            list = new ArrayList<Dependency>();
                        }

                        list.add(value);
                        aDepMap.put(key, list);

                        // Store <SystemId-DepId, Dependency> pair
                        key  = value.getSystemId() + "-" + value.getDependencyId();
                        list = new ArrayList<Dependency>();
                        list.add(value);
                        aDepMap.put(key, list);

                        // Store <Level-Type, Dependency> Pair
                        key  = value.getLevel() + "-" + value.getType();
                        list = aDepMap.get(key);

                        if (list == null) {
                            list = new ArrayList<Dependency>();
                        }

                        list.add(value);
                        aDepMap.put(key, list);
                    } while (rs.next() != false);
                }

                rs.close();
            }

            stmt.close();
            rs   = null;
            stmt = null;

            return aDepMap;
        }

        /**
         * This method builds and returns a user map.
         *
         * @param aCon connection object to the database.
         *
         * @return UserMap completely built.
         *
         * @throws SQLException incase of any database errors.
         */
        public Map<String, ArrayList<DependentField>> buildDependentFieldMap(Connection aCon) throws SQLException {
            Map<String, ArrayList<DependentField>> aDepMap = Collections.synchronizedMap(new HashMap<String, ArrayList<DependentField>>());

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement stmt = aCon.prepareCall("stp_depfield_getAllDependentFields");
            ResultSet         rs   = stmt.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    boolean first = true;

                    do {
                        DependentField            value = DependentField.createFromResultSet(rs);
                        String                    key;
                        ArrayList<DependentField> list;

                        /*
                         * Store
                         *      <SystemId, List Of DependenctFields>
                         * pair
                         */
                        key  = Integer.toString(value.getSystemId());
                        list = aDepMap.get(key);

                        if (list == null) {
                            list = new ArrayList<DependentField>();
                        }

                        list.add(value);
                        aDepMap.put(key, list);

                        /*
                         * Store
                         *      <SystemId-DepId, List of DependentFields>
                         * pair.
                         */
                        key  = value.getSystemId() + "-" + value.getDependencyId();
                        list = aDepMap.get(key);

                        if (list == null) {
                            list = new ArrayList<DependentField>();
                        }

                        list.add(value);
                        aDepMap.put(key, list);

                        /*
                         * Store
                         *      <SystemId-DepId, List of DependentFields>
                         * pari.
                         */
                        key  = value.getSystemId() + "-" + value.getDependencyId();
                        list = aDepMap.get(key);

                        if (list == null) {
                            list = new ArrayList<DependentField>();
                        }

                        list.add(value);
                        aDepMap.put(key, list);

                        /*
                         * Store
                         *      <SystemId-DepRole, List of DependentFields>
                         * pari.
                         */
                        key  = value.getSystemId() + "-" + value.getDepRole();
                        list = aDepMap.get(key);

                        if (list == null) {
                            list = new ArrayList<DependentField>();
                        }

                        list.add(value);
                        aDepMap.put(key, list);

                        /*
                         * Store
                         *      <SystemId-DepId-FieldId, DependentField>
                         * pair.
                         */
                        key  = value.getSystemId() + "-" + value.getDependencyId() + "-" + value.getFieldId();
                        list = new ArrayList<DependentField>();
                        list.add(value);
                        aDepMap.put(key, list);
                    } while (rs.next() != false);
                }

                rs.close();
            }

            stmt.close();
            rs   = null;
            stmt = null;

            return aDepMap;
        }

        /**
         * This method builds and returns a ExclusionList map.
         *
         * @param aCon   connection object to the database.
         *
         * @return ExclusionList Map completely built.
         *
         * @throws SQLException incase of any database errors.
         *
         */
        public Map<String, ArrayList<ExclusionList>> buildExclusionListMap(Connection aCon) throws SQLException {
            Map<String, ArrayList<ExclusionList>> aMap = Collections.synchronizedMap(new HashMap<String, ArrayList<ExclusionList>>());

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement cs = null;

            cs = aCon.prepareCall("stp_el_getCompleteExclusionList");

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    String        key;
                    ExclusionList value = ExclusionList.createFromResultSet(rs);

                    /*
                     * Store (Sys_id, ArrayList<BAUser>)
                     */
                    key = Integer.toString(value.getSystemId());

                    ArrayList<ExclusionList> elList = aMap.get(key);

                    /*
                     * Check if we already have a list for this key.
                     */
                    if (elList == null) {
                        elList = new ArrayList<ExclusionList>();
                    }

                    elList.add(value);
                    aMap.put(key, elList);
                    elList = null;
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;

            return aMap;
        }

        /**
         * This method builds the External Resource Mapper.
         *
         * @param aCon Connection object to be used.
         *
         * @return External resource map.
         *
         * @throws SQLException
         */
        public Map<String, ExternalResource> buildExternalResourceMap(Connection aCon) throws SQLException {
            Map<String, ExternalResource> aMap = Collections.synchronizedMap(new HashMap<String, ExternalResource>());
            CallableStatement             cs   = aCon.prepareCall("stp_er_getAllExternalResources");
            ResultSet                     rs   = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    ExternalResource value = ExternalResource.createFromResultSet(rs);
                    String           key   = "";

                    /*
                     * [ Id, Resource ]
                     */
                    key = Integer.toString(value.getResourceId());
                    aMap.put(key, value);

                    /*
                     * [ Name, Resource ]
                     */
                    key = value.getResourceName().toUpperCase();
                    aMap.put(key, value);
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;

            return aMap;
        }

        /**
         * This method builds and returns a Field Descriptor map.
         *
         * @param aCon   connection object to the database.
         *
         * @return FieldDescriptorRule Map completely built.
         *
         * @throws SQLException incase of any database errors.
         *
         */
        public Map<String, FieldDescriptor> buildFieldDescriptorMap(Connection aCon, Map<String, ArrayList<FieldDescriptor>> aList) throws SQLException {
            Map<String, FieldDescriptor> aMap = Collections.synchronizedMap(new HashMap<String, FieldDescriptor>());

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement cs = null;

//            if ((ourSource == SOURCE_EMAIL) || (ourSource == SOURCE_CMDLINE)) {
//                cs = aCon.prepareCall("stp_fd_lookupBySystemId ? ");
//                cs.setInt(1, ourSystemId);
//            } else {
//                ourSource = SOURCE_WEB;
                cs        = aCon.prepareCall("stp_fd_getAllFieldDescriptors");
//            }

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    String          key;
                    FieldDescriptor value = FieldDescriptor.createFromResultSet(rs);

                    /*
                     * Store (sys_id-field_descriptor, FieldDescriptor)
                     */
                    key = Integer.toString(value.getSystemId()) + "-" + value.getDescriptor().toUpperCase().trim();
                    aMap.put(key, value);

                    if (value.getIsPrimary() == true) {

                        /*
                         * Store (sys_id-field_id__PRIMARY_, FieldDescriptor)
                         */
                        key = Integer.toString(value.getSystemId()) + "-" + value.getFieldId() + "__PRIMARY_";
                        aMap.put(key, value);
                    }

                    key = Integer.toString(value.getSystemId());

                    ArrayList<FieldDescriptor> tempList = aList.get(key);

                    if (tempList == null) {
                        tempList = new ArrayList<FieldDescriptor>();
                    }

                    tempList.add(value);
                    aList.put(key, tempList);

                    /*
                     * Store (sys_id-field_id, ArrayList<FieldDescriptor>)
                     */
                    key      = Integer.toString(value.getSystemId()) + "-" + value.getFieldId();
                    tempList = aList.get(key);

                    if (tempList == null) {
                        tempList = new ArrayList<FieldDescriptor>();
                    }

                    tempList.add(value);
                    aList.put(key, tempList);
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;

            return aMap;
        }

        /**
         * This method builds and returns a Field map.
         *
         * @param aCon   connection object to the database.
         * @param aList  Out Param for FieldList Map.
         *
         * @return Field Map completely built.
         *
         * @throws SQLException incase of any database errors.
         *
         */
        public Map<String, Field> buildFieldMap(Connection aCon, Map<Integer, ArrayList<Field>> aList) throws SQLException {
            Map<String, Field> aMap         = Collections.synchronizedMap(new HashMap<String, Field>());
            Map<String, Field> aDistinctMap = Collections.synchronizedMap(new HashMap<String, Field>());

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement cs = null;

//            if ((ourSource == SOURCE_EMAIL) || (ourSource == SOURCE_CMDLINE)) {
//                cs = aCon.prepareCall("stp_field_lookupBySystemId ? ");
//                cs.setInt(1, ourSystemId);
//                
//            } else {
//                ourSource = SOURCE_WEB;
                cs        = aCon.prepareCall("stp_field_getAllFields");
//            }

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    String key;
                    Field  value = Field.createFromResultSet(rs);

                    /*
                     * Store (Sys_id-Field_id, Field)
                     */
                    key = Integer.toString(value.getSystemId()) + "-" + Integer.toString(value.getFieldId());
                    aMap.put(key, value);

                    /*
                     * Store (Sys_id-FieldName, Field)
                     */
                    key = Integer.toString(value.getSystemId()) + "-" + value.getName().toUpperCase();
                    aMap.put(key, value);

                    /*
                     * Store (Sys_id-Field Display Name, Field)
                     */
                    key = Integer.toString(value.getSystemId()) + "-" + value.getDisplayName().toUpperCase();
                    aMap.put(key, value);

                    /*
                     * Store the list of fields per BA in the list.
                     */
                    int              iKey        = value.getSystemId();
                    ArrayList<Field> baFieldList = aList.get(iKey);

                    if (baFieldList == null) {
                        baFieldList = new ArrayList<Field>();
                    }

                    baFieldList.add(value);
                    aList.put(iKey, baFieldList);
                    baFieldList = null;

                    /*
                     * Store [ fieldName, Field ]
                     */
                    key = value.getName();
                    aDistinctMap.put(key, value);
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs                    = null;
            ourDistinctFieldTable = aDistinctMap;

            return aMap;
        }
        
        /**
         * This method builds and returns a Field Property map.
         *
         * @param aCon   connection object to the database.
         * @param aList  Out Param for FieldPropertyList Map.
         *
         * @return Field Property Map completely built.
         *
         * @throws SQLException incase of any database errors.
         *
         */
        public Map<Integer, ArrayList<FieldProperties>> buildFieldPropertyMap(Connection aCon) throws SQLException {
            Map<Integer, ArrayList<FieldProperties>> aMap         = Collections.synchronizedMap(new HashMap<Integer, ArrayList<FieldProperties>>());

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement cs = aCon.prepareCall("stp_field_property_getAllFieldProperties");

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    int key;
                    FieldProperties  value = FieldProperties.createFromResultSet(rs);

                    key = value.hashCode();
                    ArrayList<FieldProperties> baFieldPropertyList = aMap.get(key);
                    if (baFieldPropertyList == null) {
                    	baFieldPropertyList = new ArrayList<FieldProperties>();
                    }
                    baFieldPropertyList.add(value);
                    aMap.put(key, baFieldPropertyList);
                    baFieldPropertyList = null;
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs                    = null;

            return aMap;
        }

        /**
         * This method builds and returns a HolidaysList map.
         *
         * @param aCon   connection object to the database.
         *
         * @return HolidaysList Map.
         *
         * @throws SQLException incase of any database errors.
         *
         */
        public Map<String, HolidaysList> buildHolidaysListMap(Connection aCon, Map<String, ArrayList<HolidaysList>> aListMap) throws SQLException {
            Map<String, HolidaysList> aMap = Collections.synchronizedMap(new HashMap<String, HolidaysList>());

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement cs = null;

            cs = aCon.prepareCall("stp_hl_getHolidays");

            ResultSet rs = null;

            try {
                rs = cs.executeQuery();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (rs != null) {
                while (rs.next() != false) {
                    HolidaysList value = HolidaysList.createFromResultSet(rs);
                    String       key   = "";

                    /*
                     * [<Zone-Date>, Value]
                     */
                    key = value.getOfficeZone() + "-" + value.getHolidayDate().toCustomFormat("MM/dd/yyyy");
                    aMap.put(key, value);

                    /*
                     * [Office, List of holidays.]
                     */
                    key = value.getOffice().toUpperCase();

                    ArrayList<HolidaysList> list = aListMap.get(key);

                    if (list == null) {
                        list = new ArrayList<HolidaysList>();
                    }

                    list.add(value);
                    aListMap.put(key, list);
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;

            return aMap;
        }

        /**
         * This method builds and returns a NotificationRule map.
         *
         * @param aCon connection object to the database.
         *
         * @return NotificationRule Map completely built.
         *
         * @throws SQLException incase of any database errors.
         *
         */
        public Map<String, NotificationRule> buildNotificationRuleMap(Connection aCon, Map<String, ArrayList<NotificationRule>> aList) throws SQLException, DatabaseException {
            Map<String, NotificationRule> aMap   = Collections.synchronizedMap(new HashMap<String, NotificationRule>());
            ArrayList<NotificationRule>   nrList = new ArrayList<NotificationRule>();

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement cs = null;

            cs = aCon.prepareCall("stp_nr_getAllNotificationRules");

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    String           key;
                    NotificationRule value = NotificationRule.createFromResultSet(rs);

                    /*
                     * Store (rule_id, NotificationRule)
                     */
                    key = Integer.toString(value.getNotificationRuleId());
                    aMap.put(key, value);
                    nrList.add(value);
                }

                aList.put("ALLRULES", nrList);
                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;

            return aMap;
        }

        /**
         * This method builds the roleuser map.
         *
         * @param con
         * @return
         * @throws Exception
         */
        public Map<String, ArrayList<RoleUser>> buildRoleUserMap(Connection con) throws Exception {
            Map<String, ArrayList<RoleUser>> aMap = Collections.synchronizedMap(new HashMap<String, ArrayList<RoleUser>>());

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement stmt = con.prepareCall("stp_ru_getAllRoleUsers");
            ResultSet         rs   = stmt.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    boolean first = true;

                    do {
                        RoleUser            value = RoleUser.createFromResultSet(rs);
                        String              key;
                        ArrayList<RoleUser> list;

                        /*
                         * Store
                         *      <SystemId, List Of RoleUsers>
                         * pair
                         */
                        key  = Integer.toString(value.getSystemId());
                        list = aMap.get(key);

                        if (list == null) {
                            list = new ArrayList<RoleUser>();
                        }

                        list.add(value);
                        aMap.put(key, list);

                        /*
                         * Store
                         *      <SystemId-RoleId, List of DependentFields>
                         * pair.
                         */
                        key  = value.getSystemId() + "-" + value.getRoleId();
                        list = aMap.get(key);

                        if (list == null) {
                            list = new ArrayList<RoleUser>();
                        }

                        list.add(value);
                        aMap.put(key, list);
                    } while (rs.next() != false);
                }

                rs.close();
            }

            stmt.close();
            rs   = null;
            stmt = null;

            return aMap;
        }

        /**
         * This method builds and returns a Type Descriptor map.
         *
         * @param aCon   connection object to the database.
         *
         * @return TypeDescriptorRule Map completely built.
         *
         * @throws SQLException incase of any database errors.
         *
         */
        public Map<String, TypeDescriptor> buildTypeDescriptorMap(Connection aCon, Map<String, ArrayList<TypeDescriptor>> aList) throws SQLException {
            Map<String, TypeDescriptor> aMap = Collections.synchronizedMap(new HashMap<String, TypeDescriptor>());

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement cs = null;

//            if ((ourSource == SOURCE_EMAIL) || (ourSource == SOURCE_CMDLINE)) {
//                cs = aCon.prepareCall("stp_td_lookupBySystemId ? ");
//                cs.setInt(1, ourSystemId);
//            } else {
//                ourSource = SOURCE_WEB;
                cs        = aCon.prepareCall("stp_td_getAlltypeDescriptors");
//            }

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    String         key;
                    TypeDescriptor value = TypeDescriptor.createFromResultSet(rs);

                    /*
                     * Store (sys_id-field_id-type_descriptor, TypeDescriptor)
                     */
                    key = Integer.toString(value.getSystemId()) + "-" + Integer.toString(value.getFieldId()) + "-" + value.getDescriptor().toUpperCase().trim();
                    aMap.put(key, value);

                    if (value.getIsPrimary() == true) {

                        /*
                         * Store(sys_id-field_id-type_id__PRIMARY_,
                         * TypeDescriptor)
                         */
                        key = Integer.toString(value.getSystemId()) + "-" + value.getFieldId() + "-" + value.getTypeId() + "__PRIMARY_";
                        aMap.put(key, value);
                    }

                    /*
                     * Store (sys_id-field_id-type_id,
                     * ArrayList<TypeDescriptor>)
                     */
                    key = Integer.toString(value.getSystemId()) + "-" + value.getFieldId() + "-" + value.getTypeId();

                    ArrayList<TypeDescriptor> tempList = aList.get(key);

                    if (tempList == null) {
                        tempList = new ArrayList<TypeDescriptor>();
                    }

                    tempList.add(value);
                    aList.put(key, tempList);

                    /*
                     * Store (sys_id-field_id, ArrayList<TypeDescriptor>)
                     */
                    key      = Integer.toString(value.getSystemId()) + "-" + value.getFieldId();
                    tempList = aList.get(key);

                    if (tempList == null) {
                        tempList = new ArrayList<TypeDescriptor>();
                    }

                    tempList.add(value);
                    aList.put(key, tempList);

                    /*
                     * Store (sys_id, ArrayList<TypeDescriptor>)
                     */
                    key      = Integer.toString(value.getSystemId());
                    tempList = aList.get(key);

                    if (tempList == null) {
                        tempList = new ArrayList<TypeDescriptor>();
                    }

                    tempList.add(value);
                    aList.put(key, tempList);
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;

            return aMap;
        }

        /**
         * This method builds and returns a Type map.
         *
         * @param aCon   connection object to the database.
         * @param aList       Out Param for TypeList Map.
         *
         * @return Type Map completely built.
         *
         * @throws SQLException incase of any database errors.
         *
         */
        public void buildTypeMap(Connection aCon, Map<String, ArrayList<Type>> aList, Map<String, Type> aIdMap, Map<String, Type> aNameMap) throws SQLException {

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement cs = null;

//            if ((ourSource == SOURCE_EMAIL) || (ourSource == SOURCE_CMDLINE)) {
//                cs = aCon.prepareCall("stp_type_lookupBySystemId ? ");
//                cs.setInt(1, ourSystemId);
//            } else {
//                ourSource = SOURCE_WEB;
                cs        = aCon.prepareCall("stp_type_getAllTypes");
//            }

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    String key;
                    Type   value = Type.createFromResultSet(rs);

                    /*
                     * Store (Sys_id-Field_id-Type_id, Type)
                     */
                    key = Integer.toString(value.getSystemId()) + "-" + Integer.toString(value.getFieldId()) + "-" + Integer.toString(value.getTypeId());
                    aIdMap.put(key, value);

                    /*
                     * Store (Sys_id-FieldId-TypeName, Type)
                     */
                    key = Integer.toString(value.getSystemId()) + "-" + Integer.toString(value.getFieldId()) + "-" + value.getName().toUpperCase().trim();
                    aNameMap.put(key, value);

                    /*
                     * Store (Sys_id-FieldId-TypeDisplayName, Type)
                     */
                    // Nitiraj msg : maintain a different map for display-name mapping.
                    // because on type may override other if name of one is same as display-name of other.
                    key = Integer.toString(value.getSystemId()) + "-" + Integer.toString(value.getFieldId()) + "-" + value.getDisplayName().toUpperCase().trim();
                    aNameMap.put(key, value);

                    /*
                     * If this type is the default, store it under the key:
                     * (Sys_id-FieldId-DEFAULT)
                     */
                    if (value.getIsDefault() == true) {
                        key = Integer.toString(value.getSystemId()) + "-" + Integer.toString(value.getFieldId()) + "-DEFAULT";
                        aIdMap.put(key, value);
                        aNameMap.put(key, value);
                    }

                    /*
                     * Store the list of Types per BA-Field in the list.
                     */
                    key = Integer.toString(value.getSystemId()) + "-" + Integer.toString(value.getFieldId());

                    ArrayList<Type> baTypeList = aList.get(key);

                    if (baTypeList == null) {
                        baTypeList = new ArrayList<Type>();
                    }

                    baTypeList.add(value);
                    aList.put(key, baTypeList);
                    baTypeList = null;
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;

            return;
        }

        /**
         * This method builds and returns a TypeUser map.
         *
         * @param aCon   connection object to the database.
         *
         * @return TypeUser Map completely built.
         *
         * @throws SQLException incase of any database errors.
         *
         */
        public Map<String, TypeUser> buildTypeUserMap(Connection aCon, Map<String, ArrayList<TypeUser>> aList) throws SQLException, DatabaseException {
            Map<String, TypeUser> aMap = Collections.synchronizedMap(new HashMap<String, TypeUser>());

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement cs = null;

//            if ((ourSource == SOURCE_EMAIL) || (ourSource == SOURCE_CMDLINE)) {
//                cs = aCon.prepareCall("stp_typeuser_lookupBySystemId ? ");
//                cs.setInt(1, ourSystemId);
//            } else {
//                ourSource = SOURCE_WEB;
                cs        = aCon.prepareCall("stp_typeuser_getAllTypeUsers");
//            }

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    String   key;
                    TypeUser value = TypeUser.createFromResultSet(rs);

                    if (value.getUser() == null) {

                        /*
                         * This typeUser entry corresponds to an inactive user.
                         * Ignore and continue with next one.
                         */
                        continue;
                    }

                    /*
                     * Store (Sys_id-Field_id-Type_id, ArrayList<TypeUser>)
                     */
                    key = Integer.toString(value.getSystemId()) + "-" + Integer.toString(value.getFieldId()) + "-" + Integer.toString(value.getTypeId()) + "-"
                          + Integer.toString(value.getUser().getUserId());
                    aMap.put(key, value);

                    /*
                     * Store (Sys_id-Field_id-Type_id, ArrayList<TypeUser>)
                     */
                    key = Integer.toString(value.getSystemId()) + "-" + Integer.toString(value.getFieldId()) + "-" + Integer.toString(value.getTypeId());

                    ArrayList<TypeUser> baTypeUserList = aList.get(key);

                    /*
                     * Check if we already have a list for this key.
                     */
                    if (baTypeUserList == null) {
                        baTypeUserList = new ArrayList<TypeUser>();
                    }

                    baTypeUserList.add(value);
                    aList.put(key, baTypeUserList);
                    baTypeUserList = null;

                    /*
                     * Store (Sys_id-Field_id-User_id, ArrayList<TypeUser>)
                     */
                    key = Integer.toString(value.getSystemId()) + "-" + Integer.toString(value.getFieldId()) + "-" + Integer.toString((value.getUser()).getUserId()) + "-USER";

                    ArrayList<TypeUser> baFieldUserList = aList.get(key);

                    /*
                     * Check if we already have a list for this key.
                     */
                    if (baFieldUserList == null) {
                        baFieldUserList = new ArrayList<TypeUser>();
                    }

                    baFieldUserList.add(value);
                    aList.put(key, baFieldUserList);
                    baFieldUserList = null;

                    /*
                     * Check if this type-user is a volunteer. If so, we shall
                     * include in the volunteer list for this type.
                     */
                    if (value.getIsVolunteer() == true) {
                        key            = key + "-VOLUNTEER";
                        baTypeUserList = aList.get(key);

                        /*
                         * Check if we already have a list for this key.
                         */
                        if (baTypeUserList == null) {
                            baTypeUserList = new ArrayList<TypeUser>();
                        }

                        baTypeUserList.add(value);
                        aList.put(key, baTypeUserList);
                        baTypeUserList = null;
                    }
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;

            return aMap;
        }

        /**
         * This method builds and returns a WorkflowRule map.
         *
         * @param aCon   connection object to the database.
         *
         * @return WorkflowRule Map completely built.
         *
         * @throws SQLException incase of any database errors.
         *
         */
        public Map<String, WorkflowRule> buildWorkflowRuleMap(Connection aCon) throws SQLException {
            Map<String, WorkflowRule> aMap = Collections.synchronizedMap(new HashMap<String, WorkflowRule>());

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement cs = null;

            cs = aCon.prepareCall("stp_wr_getAllWorkflowRules");

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    String       key;
                    WorkflowRule value = WorkflowRule.createFromResultSet(rs);

                    /*
                     * Store (rule_id, WorkflowRule)
                     */
                    key = Integer.toString(value.getRuleId());
                    aMap.put(key, value);
                }

                rs.close();
                rs = null;
            }

            cs.close();
            cs = null;

            return aMap;
        }

        /**
         * Run method that builds the mapper for Business objects.
         */
        public void run() {
            Connection con = null;

            try {
                con = DataSourcePool.getConnection();

                if (con == null) {
                    return;
                }

                try {
                    ourBAMap = buildBAMap(con);
                } catch (SQLException sqle) {
                    LOG.severe("Exception while refreshing the BA Mapper." + "",(sqle));
                }

                try {
                    Map<Integer, ArrayList<Field>> aList = Collections.synchronizedMap(new HashMap<Integer, ArrayList<Field>>());

                    ourFieldMap     = buildFieldMap(con, aList);
                    ourFieldListMap = aList;
                } catch (SQLException sqle) {
                    LOG.severe("Exception while refreshing the Field Mapper." + "",(sqle));
                }
                
                try {
                    ourFieldPropertyMap     = buildFieldPropertyMap(con);
                } catch (SQLException sqle) {
                    LOG.severe("Exception while refreshing the Field Property Mapper." + "",(sqle));
                }

                try {
                    Map<String, ArrayList<FieldDescriptor>> aList = Collections.synchronizedMap(new HashMap<String, ArrayList<FieldDescriptor>>());

                    ourFieldDescMap     = buildFieldDescriptorMap(con, aList);
                    ourFieldDescListMap = aList;
                } catch (SQLException sqle) {
                    LOG.severe("Exception while refreshing the FD Mapper." + "",(sqle));
                }

                try {
                    Map<String, Type>            aIdMap   = Collections.synchronizedMap(new HashMap<String, Type>());
                    Map<String, Type>            aNameMap = Collections.synchronizedMap(new HashMap<String, Type>());
                    Map<String, ArrayList<Type>> aList    = Collections.synchronizedMap(new HashMap<String, ArrayList<Type>>());

                    buildTypeMap(con, aList, aIdMap, aNameMap);
                    ourTypeIdMap   = aIdMap;
                    ourTypeNameMap = aNameMap;
                    ourTypeListMap = aList;
                } catch (SQLException sqle) {
                    LOG.severe("Exception while refreshing the Type Mapper." + "",(sqle));
                }

                try {
                    Map<String, ArrayList<TypeDescriptor>> aList = Collections.synchronizedMap(new HashMap<String, ArrayList<TypeDescriptor>>());

                    ourTypeDescMap     = buildTypeDescriptorMap(con, aList);
                    ourTypeDescListMap = aList;
                } catch (SQLException sqle) {
                    LOG.severe("Exception while refreshing the FD Mapper." + "",(sqle));
                }

                try {
                    Map<String, ArrayList<TypeUser>> aList = Collections.synchronizedMap(new HashMap<String, ArrayList<TypeUser>>());

                    ourTypeUserMap     = buildTypeUserMap(con, aList);
                    ourTypeUserListMap = aList;
                } catch (SQLException sqle) {
                    LOG.severe("Exception while refreshing the " + "TypeUser Mapper." + "",(sqle));
                } catch (DatabaseException de) {
                    LOG.severe("Exception while refreshing the " + "TypeUser Mapper." + "",(de));
                }

                try {
                    ourBAUserListMap = buildBAUserMap(con);
                } catch (SQLException sqle) {
                    LOG.severe("Exception while refreshing the " + "BAUser Mapper." + "",(sqle));
                }

                try {
                    ourExclusionListMap = buildExclusionListMap(con);
                } catch (SQLException sqle) {
                    LOG.severe("Exception while refreshing the EL Mapper." + "",(sqle));
                }

                try {
                    ourDateFormatMap = buildDateFormatMap(con);
                } catch (SQLException sqle) {
                    LOG.severe("Exception while refreshing the DF Mapper." + "",(sqle));
                }

                try {
                    ourWorkflowRuleMap = buildWorkflowRuleMap(con);
                } catch (SQLException sqle) {
                    LOG.severe("Exception while refreshing the WR Mapper." + "",(sqle));
                }

                try {
                    ourBARuleMap = buildBARuleMap(con);
                } catch (SQLException sqle) {
                    LOG.severe("Exception while refreshing the BARule Mapper." + "",(sqle));
                } catch (DatabaseException de) {
                    LOG.severe("Exception while refreshing the BARule Mapper.", de);
                }

                try {
                    Map<String, ArrayList<NotificationRule>> aList = Collections.synchronizedMap(new HashMap<String, ArrayList<NotificationRule>>());

                    ourNotificationRuleMap     = buildNotificationRuleMap(con, aList);
                    ourNotificationRuleListMap = aList;
                } catch (SQLException sqle) {
                    LOG.severe("Exception while refreshing the" + " NotificationRule Mapper." + "",(sqle));
                } catch (DatabaseException de) {
                    LOG.severe("Exception while refreshing the Notification" + " rule Mapper." + "",(de));
                }

                try {
                    Map<String, ArrayList<HolidaysList>> aListMap = Collections.synchronizedMap(new HashMap<String, ArrayList<HolidaysList>>());

                    ourHolidaysMap     = buildHolidaysListMap(con, aListMap);
                    ourHolidaysListMap = aListMap;
                } catch (SQLException sqle) {
                    LOG.severe("Exception while refreshing the holidays List" + " Mapper." + "",(sqle));
                }

                try {
                    ourExternalResourceMap = buildExternalResourceMap(con);
                } catch (SQLException sqle) {
                    LOG.severe("Exception while refreshing the external " + "resource mapper." + "",(sqle));
                }

                try {
                    ourDependencyMap = buildDependencyMap(con);
                } catch (SQLException sqle) {
                    LOG.severe("Exception while refreshing the dependency " + "mapper." + "",(sqle));
                }

                try {
                    ourDependentFieldMap = buildDependentFieldMap(con);
                } catch (SQLException sqle) {
                    LOG.severe("Exception while refreshing the dependent " + "field mapper." + "",(sqle));
                }

                try {
                    ourRoleUserMap = buildRoleUserMap(con);
                } catch (Exception e) {
                    LOG.warn("Exception while building the roleuser map" + "",(e));
                }
            } catch (SQLException sqle) {
                LOG.severe("Exception while building the mapper for " + "Business Objects." + "",(sqle));
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException sqle) {
                        LOG.severe("Exception while closing the connection." + "",(sqle));
                    }

                    con = null;
                }
            }
        }
    }


    /**
     * This class invokes the garbage collector.
     *
     * $version : $Id: $
     */
    private static class GCRunner extends TimerTask {

        /**
         * Default constructor.
         */
        public GCRunner() {}

        //~--- methods --------------------------------------------------------

        /**
         * Run method that actually invokes the Garbage Collector.
         */
        public void run() {
            System.gc();
        }
    }


    /**
     * This class Builds the UserMapper.
     *
     * $version : $Id: $
     */
    private static class UserMapBuilder extends TimerTask {

        /**
         * Default constructor.
         */
        public UserMapBuilder() {}

        //~--- methods --------------------------------------------------------

        /**
         * This method builds and returns a mail list user map.
         *
         * @param aCon connection object to the database.
         *
         * @return MailListUserMap completely built.
         *
         * @throws SQLException incase of any database errors.
         */
        private Map<Integer, ArrayList<User>> buildMailListUserMap(Connection aCon, Map<Integer, ArrayList<User>> aUserMailListMap) throws SQLException, DatabaseException {
            Map<Integer, ArrayList<User>> aMailListUserMap = Collections.synchronizedMap(new HashMap<Integer, ArrayList<User>>());

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement stmt = aCon.prepareCall("stp_mlu_getAllMailListUsers");
            ResultSet         rs   = stmt.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    int             mailListId  = rs.getInt("mail_list_id");
                    int             memberId    = rs.getInt("user_id");
                    User            member      = User.lookupAllByUserId(memberId);
                    User            mailingList = User.lookupAllByUserId(mailListId);
                    ArrayList<User> tempList    = null;

                    /*
                     * Insert the user into the list that is maintained per
                     * mailing list.
                     */
                    tempList = aMailListUserMap.get(mailListId);

                    if (tempList == null) {
                        tempList = new ArrayList<User>();
                    }

                    tempList.add(member);
                    aMailListUserMap.put(mailListId, tempList);

                    /*
                     * Insert the mailing list into the list that is maintained
                     * per user.
                     */
                    tempList = aUserMailListMap.get(memberId);

                    if (tempList == null) {
                        tempList = new ArrayList<User>();
                    }

                    tempList.add(mailingList);
                    aUserMailListMap.put(memberId, tempList);
                }

                rs.close();
            }

            stmt.close();
            rs   = null;
            stmt = null;

            return aMailListUserMap;
        }

        /**
         * This method builds and returns a user map.
         *
         * @param aCon connection object to the database.
         *
         * @return UserMap completely built.
         *
         * @throws SQLException incase of any database errors.
         */
        private void buildUserMap(Connection aCon, StringBuilder aUserInfo, Hashtable<Integer, User> userIdTable, Hashtable<String,User> userLoginTable, Hashtable<String,User> userEmailTable ) throws SQLException {
//            Map<String, User> aUserMap = Collections.synchronizedMap(new HashMap<String, User>());

            /*
             * We do not want to handle any exceptions here. Let them be
             * escalated to the caller. This is to retain the old mapper
             * in case of any exceptions while building the latest one.
             */
            CallableStatement stmt = aCon.prepareCall("stp_user_getAllUsers");
            ResultSet         rs   = stmt.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    boolean first = true;

                    do {
                        User   value = User.createFromResultSetAll(rs);
                        String key;

                        /*
                         * Store <UserId, User> pair
                         */
                        Integer userId = new Integer(value.getUserId());
                        userIdTable.put(userId, value);

                        /*
                         * Store <USERLOGIN, User> Pair
                         */
                        key = value.getUserLogin().toUpperCase();
                        userLoginTable.put(key, value);

                        /*
                         * Store <EMAIL, User> Pair
                         */
                        key = value.getEmail().toUpperCase();
                        userEmailTable.put(key, value);
                        if(value.getOtherEmails() != null)
                        {
                        	for(String otherEmail:value.getOtherEmails())
                        	{
                        		if( (otherEmail != null) && (otherEmail.trim().length() > 0))
                        		{
                        			userEmailTable.put(otherEmail.toUpperCase(), value);
                        		}
                        	}
                        }
//
//                        /*
//                         * Store <EMAIL(without .transbittech.com), User> pair.
//                         */
//                        key = value.getEmail().toUpperCase();
////                        key = key.replace(".transbittech.com", "");
//                        userEmailTable.put(key, value);

                        /*
                         * If the user is active, then add this record to the
                         * list used by the auto complete feature.
                         */
                        if (value.getIsActive() == true) {

                            /*
                             * Append to the user-info a record in the
                             * following format
                             *   - ["<UserLogin", "<User Display Name>"]
                             */
                            if (first == false) {
                                aUserInfo.append(",\n");
                            } else {
                                first = false;
                            }

                            aUserInfo.append(getUserInfoRecord(value));
                        }
                    } while (rs.next() != false);
                }

                rs.close();
            }

            stmt.close();
            rs   = null;
            stmt = null;

//            return aUserMap;
        }

        /**
         * Run method that builds the user mapper.
         */
        public void run() {
            Connection con = null;

            try {
                con = DataSourcePool.getConnection();

                if (con == null) {
                    return;
                }

                StringBuilder userInfo = new StringBuilder();

                buildUserMap(con, userInfo, ourUserMap, ourUserLoginMap, ourUserEmailMap);
                	
                ourUserMailListMap = new HashMap<Integer, ArrayList<User>>();
                ourMailListUserMap = buildMailListUserMap(con, ourUserMailListMap);
                ourUserInfo        = userInfo;
                
            } catch (SQLException sqle) {
                LOG.severe("SQL Exception in UserRefresher" + "",(sqle));
            } catch (DatabaseException dbe) {
                LOG.severe("Database Exception in UserRefresher" + "",(dbe));
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException sqle) {
                        LOG.severe("Exception while closing the connection" + "",(sqle));
                    }

                    con = null;
                }
            }
            
            UserRadixTree.createNewInstance();
        }

        //~--- get methods ----------------------------------------------------

        /**
         * This method returns the userinfo needed for auto-complete feature.
         *
         * @param value User object.
         *
         * @return UserInfo record.
         */
        private String getUserInfoRecord(User value) {
            StringBuffer buffer      = new StringBuffer();
            String       userLogin   = value.getUserLogin();
            String       displayName = value.getDisplayName();

   //         displayName = displayName.replaceAll(".transbittech.com", "");
            if(displayName != null)
            	displayName = displayName.replaceAll(",", " ");
            else
            	displayName = "";
            
            userLogin  = userLogin.replaceAll(",", " ");
 //           userLogin   = userLogin.replaceAll(".transbittech.com", "");
            buffer.append("\"");

            if (displayName.equals(userLogin)) {
                buffer.append(taintJSONString(userLogin));
            } else {
                buffer.append(taintJSONString(displayName)).append(" <").append(taintJSONString(userLogin)).append(">");
            }

            buffer.append("\"");

            return buffer.toString();
        }
        private String taintJSONString(String input)
        {
        	return input.replaceAll("\"", "\\\\\"");
        }
    }
}
