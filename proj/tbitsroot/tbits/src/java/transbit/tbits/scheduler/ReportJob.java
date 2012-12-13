/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



/**
 * ReportJob.java
 *
 * $Header:
 */
package transbit.tbits.scheduler;

//~--- non-JDK imports --------------------------------------------------------

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.Helper.TBitsConstants.OutputFormat;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Mail;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DateTimeFormat;
import transbit.tbits.domain.User;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.scheduler.ui.JobUtil;
import transbit.tbits.scheduler.ui.ParameterType;
import transbit.tbits.search.Searcher;
import transbit.tbits.webapps.HtmlSearch;
import transbit.tbits.webapps.WebUtil;

import static transbit.tbits.Helper.TBitsConstants.PKG_SCHEDULER;
import static transbit.tbits.Helper.TBitsConstants.SITE_ZONE;
import static transbit.tbits.search.SearchConstants.ANCHOR;
import static transbit.tbits.search.SearchConstants.NO_FORMATTING;
import static transbit.tbits.search.SearchConstants.NO_HIERARCHY;
import static transbit.tbits.search.SearchConstants.TEXT_SEVERITY;

//~--- JDK imports ------------------------------------------------------------

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TimeZone;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author  Vaibhav.
 * @version $Id: $
 */
public class ReportJob implements Job {

    // Application logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_SCHEDULER);

    // Name of the html interface that renders the search results.
    public static final String RESULTS_EXCEL_FILE   = "web/tbits-search-results-excel.htm";
    public static final String CMD_PARAM_USER       = "user";
    public static final String CMD_PARAM_SUBJECT    = "subject";
    public static final String CMD_PARAM_RECIPIENTS = "recipients";
    public static final String CMD_PARAM_QUERY      = "dqlQuery";
    public static final String CMD_PARAM_PREFIX     = "prefix";
    public static final String CMD_PARAM_FROM       = "fromAddress";

    //~--- fields -------------------------------------------------------------

    private BusinessArea myBusinessArea;

    // DQL Query to be executed to generate the report.
    private String myDQLQuery;

    // User on whose behalf the mail should be sent: From address.
    private String          myFromAddress;
    private User            myFromUser;
    private ArrayList<User> myRecUserList;

    // Comma separated list of users who should receive the report.
    private String myRecipientList;
    private String myReportContent;

    // Subject of the report to be mailed.
    private String mySubject;

    /*
     *  Input attributes to this Job.
     */

    // Business area on which the report should be run.
    private String mySysPrefix;

    /*
     * Following variables will be used internally.
     */
    private User myUser;

    // User on whose behalf the report should be run.
    private String myUserLogin;

    //~--- methods ------------------------------------------------------------

    /**
     * The method that gets executed when the job is triggered.
     *
     * @param arg0  JobExecutionContext that holds the JobDetail and Trigger
     *              information.
     *
     * @exception   JobExecutionException
     */
    public void execute(JobExecutionContext arg0) throws JobExecutionException {

        // Get the JobDetail object.
        JobDetail jd = arg0.getJobDetail();

        // Read the properties of the Job from the JobDetail object.
        String     jobName  = jd.getName();
        String     jobGroup = jd.getGroup();
        JobDataMap jdm      = jd.getJobDataMap();

        generateReport(jdm);
    }

    /**
     * This method executes the given DQL query and returns the searcher object
     * which can be used to render results.
     *
     * @param systemId    ID of the business area
     * @param userId      ID of the user one whose behalf the query is executed
     * @param query       DQL Query
     * @return            Searcher object
     * @throws JobExecutionException Incase of any exception during search.
     */
    private Searcher executeQuery(int systemId, int userId, String query) throws JobExecutionException {
        Searcher searcher = null;

        try {
            searcher = new Searcher(systemId, userId, query);
            searcher.search();
        } catch (Exception e) {
            throw new JobExecutionException(e.toString());
        }

        return searcher;
    }

    public void generateReport(JobDataMap jdm) throws JobExecutionException {

        /*
         * Read the inputs to this job from the data map.
         */
        mySysPrefix = jdm.getString(CMD_PARAM_PREFIX);

        /*
         * Check if the sysprefix corresponds to a valid busniess area in this
         * instance.
         */
        try {
            myBusinessArea = BusinessArea.lookupBySystemPrefix(mySysPrefix);
        } catch (DatabaseException de) {
            String message = "No business area found for the prefix: " + mySysPrefix;

            LOG.severe(message);

            throw new JobExecutionException(message);
        }

        /*
         * Check if the userlogin on whose behalf the report should be generated
         * is valid according to our user database. Check only in the active
         * user database.
         */
        myUserLogin = jdm.getString(CMD_PARAM_USER);

        try {
            myUser = User.lookupByUserLogin(myUserLogin);
        } catch (DatabaseException de) {
            String message = "No user found for the login: " + myUserLogin;

            LOG.severe(message);

            throw new JobExecutionException(message);
        }

        /*
         * Check the fromaddress on whose name the report should be sent out is
         * valid. Again, this should be checked only in the active user database
         */
        myFromAddress = jdm.getString(CMD_PARAM_FROM);

        try {
            myFromUser = User.lookupByUserLogin(myFromAddress);
        } catch (DatabaseException de) {
            String message = "Invalid From Address: " + myFromAddress;

            LOG.severe(message);

            throw new JobExecutionException(message);
        }

        /*
         * Resolve the list of recipients using the active user database.
         * 1. Replace any occurance of semicolon with comma.
         * 2. Get the arraylist of logins from this CSV.
         * 3. Iterate through the list and resolve the user names.
         */
        myRecipientList = jdm.getString(CMD_PARAM_RECIPIENTS);

        if ((myRecipientList == null) || myRecipientList.trim().equals("")) {
            String message = "Recipients list is empty.";

            throw new JobExecutionException(message);
        }

        myRecipientList = myRecipientList.replace(';', ',');

        ArrayList<String> recLoginList = Utilities.toArrayList(myRecipientList);

        myRecUserList = new ArrayList<User>();

        for (String login : recLoginList) {
            try {
                User user = User.lookupByUserLogin(login);

                if (user != null) {
                    myRecUserList.add(user);
                } else {
                    LOG.severe("Invalid recipient: " + login);
                }
            } catch (DatabaseException e) {
                LOG.severe("Invalid recipient: " + login);
            }
        }

        /*
         * If none of the recipients could be resolved, then there is no point
         * in proceeding with the generation.
         */
        if (myRecUserList.size() == 0) {
            String message = "Recipients list is empty.";

            throw new JobExecutionException(message);
        }

        myDQLQuery = jdm.getString(CMD_PARAM_QUERY);

        Searcher  searcher = executeQuery(myBusinessArea.getSystemId(), myUser.getUserId(), myDQLQuery);
        String    results  = renderResults(myBusinessArea, myUser.getWebConfigObject(), searcher);
        Timestamp ts       = new Timestamp();

        try {
            DTagReplacer hp = new DTagReplacer(RESULTS_EXCEL_FILE);

            /*
             * Get the date format used for emails from this business area.
             */
            SysConfig sysconfig  = myBusinessArea.getSysConfigObject();
            int       formatId   = sysconfig.getEmailDateFormat();
            String    dateFormat = "MM/dd/yyyy HH:mm:ss ZZZ";

            try {
                DateTimeFormat dtf = DateTimeFormat.lookupByDateTimeFormatId(formatId);

                dateFormat = dtf.getFormat();
            } catch (Exception e) {
                LOG.warn(e.toString());
            }

            String cdate = ts.toCustomFormat(dateFormat);

            hp.replace("nearestPath", WebUtil.getNearestPath(""));
            hp.replace("businessArea", myBusinessArea.getDisplayName());
            hp.replace("query", myDQLQuery);
            hp.replace("currentDate", cdate);
            hp.replace("searchResults", results);
            myReportContent = hp.parse(myBusinessArea.getSystemId());
        } catch (Exception e) {
            throw new JobExecutionException(e.toString());
        }

        /*
         * Check if the subject contains $date. If so, replace it with the
         * current date in yyyy-MM-dd format.
         */
        mySubject = jdm.getString(CMD_PARAM_SUBJECT);
        mySubject = (mySubject == null)
                    ? ""
                    : mySubject.trim();

        if (mySubject.indexOf("$date") >= 0) {
            mySubject = mySubject.replace("$date", ts.toCustomFormat("yyyy-MM-dd"));
        }

        sendReport();
        //Mapper.stop();
        //DataSourcePool.shutdownPooling();
    }

    /**
     * @param args
     */
    public static int main(String[] args) {
        if (args.length != 6) {
            StringBuilder usage = new StringBuilder();

            usage.append("Usage:\n\t").append("ReportJob <BAPrefix> <User> <DQL> <FromAddress> ").append("<Subject> <Recipients>").append("\n\nWhere\n\t").append(
                "BAPrefix    - Prefix of the business area\n\t").append("User        - User on whose behalf the report ").append("should be run (generally the BA Administrator)\n\t").append(
                "DQL         - Query in DQL\n\t").append("FromAddress - From Address on the envelope of ").append("mail\n\t").append("Subject     - Subject of the mail\n\t").append(
                "              (can contain $date to print date ").append("in yyyy-MM-dd format\n\t").append("Recipients  - Recipients of the mail\n\n");
            System.err.println(usage.toString());
            return 1;
        }

        /*
         * Form the data map out of the command line arguments and generate
         * the report.
         */
        JobDataMap jdm = new JobDataMap();

        jdm.put(CMD_PARAM_PREFIX, args[0]);
        jdm.put(CMD_PARAM_USER, args[1]);
        jdm.put(CMD_PARAM_QUERY, args[2]);
        jdm.put(CMD_PARAM_FROM, args[3]);
        jdm.put(CMD_PARAM_SUBJECT, args[4]);
        jdm.put(CMD_PARAM_RECIPIENTS, args[5]);

        try {
            ReportJob rj = new ReportJob();

            rj.generateReport(jdm);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        } finally {
            //Mapper.stop();
            //DataSourcePool.shutdownPooling();
        }

        return 0;
    }

    /**
     * This method uses the searcher and renders the results in HTML.
     *
     * @param aBA       BusinessArea object.
     * @param aConfig   User configuration object.
     * @param aSearcher Searcher object.
     * @return
     */
    private String renderResults(BusinessArea aBA, WebConfig aConfig, Searcher aSearcher) {
        StringBuffer results = new StringBuffer();
        int          flags   = ANCHOR | TEXT_SEVERITY | NO_FORMATTING | NO_HIERARCHY;

        /*
         * Distribute the entire width of the page among the columns.
         */
        String colGroup = HtmlSearch.getColumnGroup(aBA, aSearcher.getDisplayHeader(), flags);

        results.append(colGroup);

        /*
         * Form the column header in the results table.
         */
        String header = HtmlSearch.getHeader(aBA, aSearcher, false, false, flags, null);

        results.append(header);

        /*
         * The timestamps in the results are always in site zone.
         */
        TimeZone           zone   = WebUtil.getPreferredZone(SITE_ZONE, 0);
        ArrayList<Integer> reqIds = new ArrayList<Integer>();

        /*
         * Render the results using flat renderer as we do not want to
         * show the hierarchies in reports for now.
         */
        HtmlSearch.flatRenderer(aBA, aConfig, aSearcher, zone, results, reqIds, flags, OutputFormat.HTML, null);

        return results.toString();
    }

    /**
     * This method sends out the report to the valid list of recipients.
     */
    private void sendReport() {
        String        fromAddress = myFromUser.getEmail();
        StringBuilder toAddrList  = new StringBuilder();
        boolean       first       = true;

        for (User user : myRecUserList) {
            if (first == false) {
                toAddrList.append(",");
            } else {
                first = false;
            }

            toAddrList.append(user.getEmail());
        }

        Mail.sendWithHtml(toAddrList.toString(), "", fromAddress, mySubject, myReportContent);
    }
}
