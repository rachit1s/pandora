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
 * SysPrefixes.java
 *
 * $Header:
 */
package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

//transbit Imports
import transbit.tbits.common.Configuration;
import transbit.tbits.common.TBitsLogger;

//Static imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_UTIL;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Hashtable;

//~--- classes ----------------------------------------------------------------

/**
 * Thsi file is used to read and store tbits.prefixes file.
 *
 * @author  Vinod Gupta
 * @version $Id: $
 */
public class SysPrefixes {

    // Prefixes file for reading all business area prefixes for smart links
    private final static String              PREFIX_FILE      = "etc/tbits.prefixes";////modifies by Vinod
    private static Hashtable<String, String> ourPrefixList    = new Hashtable<String, String>();
    private static Hashtable<String, String> ourPrefixToEmail = new Hashtable<String, String>();
    private static Hashtable<String, String> ourPrefixToName  = new Hashtable<String, String>();
    private static Hashtable<String, String> ourPrefixToSite  = new Hashtable<String, String>();
    private static ArrayList<String>         ourBAList        = new ArrayList<String>();

    // The Logger.
    public static TBitsLogger LOG = TBitsLogger.getLogger(PKG_UTIL);

    //~--- static initializers ------------------------------------------------

    static {
        loadPrefixes();
    }

    //~--- methods ------------------------------------------------------------

    /*
     * This function reads prefixes from file
     */
    private static synchronized void loadPrefixes() {
        try {
            File file = Configuration.findPath(PREFIX_FILE);

            if (file == null) {
                throw new FileNotFoundException(PREFIX_FILE + " is not found.");
            }

            BufferedReader            br      = new BufferedReader(new FileReader(file));
            String                    line    = "";
            Hashtable<String, String> tempMap = new Hashtable<String, String>();
            Hashtable<String, String> mailMap = new Hashtable<String, String>();
            Hashtable<String, String> nameMap = new Hashtable<String, String>();
            Hashtable<String, String> siteMap = new Hashtable<String, String>();
            ArrayList<String>         baList  = new ArrayList<String>();

            /*
             * Each line in the file is in the following format.
             *
             *  PREFIX,DISPLAY_NAME,SITE,OTHER_EMAILS_AS_CSV.
             *
             * We are interested only in the primary email address. So,
             * the other email addresses are not retrieved.
             */
            while ((line = br.readLine()) != null) {
                String record = line.trim();

                if (line.startsWith("#")) {
                    continue;
                }

                String[] arr = record.split("\\|");

                if (arr == null) {
                    continue;
                }

                String prefix        = arr[0].trim();
                String primaryprefix = prefix;
                String name          = arr[1].trim();
                String site          = arr[2].trim();
                String email         = arr[3].trim();

                if (arr.length > 4) {
                    String   supportedPrefixes = arr[4].trim();
                    String[] tmparrprefixes    = supportedPrefixes.split(",");

                    /**
                     * Following code creates a hash table enrty for each
                     *  supported prefix with name, site and BA email list
                     *  details.
                     */
                    for (int iarrcount = 0; iarrcount < tmparrprefixes.length; iarrcount++) {
                        tempMap.put(tmparrprefixes[iarrcount].toUpperCase().toUpperCase(), primaryprefix);
                        mailMap.put(tmparrprefixes[iarrcount].toUpperCase(), email);
                        nameMap.put(tmparrprefixes[iarrcount].toUpperCase(), name);
                        siteMap.put(tmparrprefixes[iarrcount].toUpperCase(), site);
                    }
                }

                tempMap.put(prefix.toUpperCase(), primaryprefix);
                mailMap.put(prefix.toUpperCase(), email);
                nameMap.put(prefix.toUpperCase(), name);
                siteMap.put(prefix.toUpperCase(), site);
                baList.add(name + "\n" + primaryprefix);
            }

            baList           = TBitsHelper.sortList(baList);
            ourPrefixList    = tempMap;
            ourPrefixToEmail = mailMap;
            ourPrefixToName  = nameMap;
            ourPrefixToSite  = siteMap;
            ourBAList        = baList;
        } catch (FileNotFoundException fnfe) {
            LOG.severe("",(fnfe));
        } catch (IOException ioe) {
            LOG.severe("",(ioe));
        }
    }

    /**
     * Main method
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            return;
        }

        LOG.info(SysPrefixes.getPrefix(args[0]));
        SysPrefixes.reload();
        LOG.info(SysPrefixes.getPrefix(args[0]));
        LOG.info("Display Name: " + getDisplayName(args[0]));
        LOG.info("Email: " + getEmail(args[0]));
        LOG.info("Site: " + getLocation(args[0]));
    }

    /**
     * This method is to force the SysPrefixes to update itself with the
     * latest list.
     */
    public static void reload() {
        loadPrefixes();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the BA List.
     *
     */
    public static ArrayList<String> getBAList() {
        return ourBAList;
    }

    /**
     * This is the public method to get the name corresponding to given prefix
     *
     * @param aPrefix
     *
     * @return name if aPrefix present in the list.
     */
    public static String getDisplayName(String aPrefix) {
        if ((aPrefix == null) || aPrefix.trim().equals("")) {
            return null;
        }

        return ourPrefixToName.get(aPrefix.trim().toUpperCase());
    }

    /**
     * This is the public method to get the email corresponding to given prefix
     *
     * @param aPrefix
     *
     * @return email if aPrefix present in the list.
     */
    public static String getEmail(String aPrefix) {
        if ((aPrefix == null) || aPrefix.trim().equals("")) {
            return null;
        }

        return ourPrefixToEmail.get(aPrefix.trim().toUpperCase());
    }

    /**
     * This is the public method to get the site corresponding to given prefix
     *
     * @param aPrefix
     *
     * @return name if aPrefix present in the list.
     */
    public static String getLocation(String aPrefix) {
        if ((aPrefix == null) || aPrefix.trim().equals("")) {
            return null;
        }

        return ourPrefixToSite.get(aPrefix.trim().toUpperCase());
    }

    /**
     * This is the public method to check if String is a valid sysPrefix.
     *
     * @param aPrefix
     *
     * @return Prefix if aPrefix present in the list.
     */
    public static String getPrefix(String aPrefix) {
        if ((aPrefix == null) || aPrefix.trim().equals("")) {
            return null;
        }

        return ourPrefixList.get(aPrefix.trim().toUpperCase());
    }

    /**
     * This returns true if the prefix is present. otherwise false.
     *
     * @param aPrefix
     *
     * @return True if aPrefix present in the list, otherwise false.
     */
    public static boolean isValid(String aPrefix) {
        if ((aPrefix == null) || aPrefix.trim().equals("")) {
            return false;
        }

        if (ourPrefixList.get(aPrefix.trim().toUpperCase()) != null) {
            return true;
        }

        return false;
    }
}
