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
 * LuceneQueryGenerator.java
 *
 * $Header:
 *
 */
package transbit.tbits.search;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.DatabaseException;

//TBits Imports.
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.MailListUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;

//Static Imports.
import static transbit.tbits.Helper.TBitsConstants.PKG_SEARCH;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

//~--- classes ----------------------------------------------------------------

public class LuceneQueryGenerator implements SearchConstants {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_SEARCH);

    //~--- methods ------------------------------------------------------------

    /**
     * This method returns the given argument with all the special characters
     * of lucene query syntax escaped with a back-slash.
     */
    public static String escapeLuceneSpecialChars(String aArg) {
        aArg = aArg.trim();

        /*
         * Remove any leading * characters.
         */
        if (aArg.startsWith("*") == true) {
            while (aArg.startsWith("*") == true) {
                aArg = aArg.substring(1);
            }
        }

        /*
         * Escape all the special characters
         * + - && || ! ( ) { } [ ] ^ " ~ * ? : \ (space)
         */
        if (aArg.indexOf('\\') >= 0) {
            aArg = aArg.replace("\\", "\\\\");
        }

        if (aArg.indexOf('+') >= 0) {
            aArg = aArg.replace("+", "\\+");
        }

        if (aArg.indexOf('-') >= 0) {
            aArg = aArg.replace("-", "\\-");
        }

        if (aArg.indexOf('&') >= 0) {
            aArg = aArg.replace("&", "\\&");
        }

        if (aArg.indexOf('|') >= 0) {
            aArg = aArg.replace("|", "\\|");
        }

        if (aArg.indexOf('!') >= 0) {
            aArg = aArg.replace("!", "\\!");
        }

        if (aArg.indexOf('(') >= 0) {
            aArg = aArg.replace("(", "\\(");
        }

        if (aArg.indexOf(')') >= 0) {
            aArg = aArg.replace(")", "\\)");
        }

        if (aArg.indexOf('{') >= 0) {
            aArg = aArg.replace("{", "\\{");
        }

        if (aArg.indexOf('}') >= 0) {
            aArg = aArg.replace("}", "\\}");
        }

        if (aArg.indexOf('[') >= 0) {
            aArg = aArg.replace("[", "\\[");
        }

        if (aArg.indexOf(']') >= 0) {
            aArg = aArg.replace("]", "\\]");
        }

        if (aArg.indexOf('^') >= 0) {
            aArg = aArg.replace("^", "\\^");
        }

        if (aArg.indexOf('"') >= 0) {
            aArg = aArg.replace("\"", "\\\"");
        }

        if (aArg.indexOf('~') >= 0) {
            aArg = aArg.replace("~", "\\~");
        }

//      if (aArg.indexOf('*') >= 0)         aArg = aArg.replace("*", "\\*");
        if (aArg.indexOf('?') >= 0) {
            aArg = aArg.replace("?", "\\?");
        }

        if (aArg.indexOf(':') >= 0) {
            aArg = aArg.replace(":", "\\:");
        }

        if (aArg.indexOf(' ') >= 0) {
            aArg = "\"" + aArg + "\"";
        }

        return aArg;
    }

    public static void main(String arg[]) throws Exception {
        ParseEntry        pe        = null;
        String            fieldName = null;
        ArrayList<String> argList   = null;

        try {
            pe        = new ParseEntry();
            fieldName = Field.CATEGORY;
            argList   = new ArrayList<String>();
            argList.add("pending");
            argList.add("+indexer");
            pe.setFieldName(fieldName);
            pe.setArgList(argList);
            pe.setConnective(Connective.C_NOT);
            System.out.println(getTypeQuery(fieldName, pe, false, "DFlow"));
            System.out.println(getTextQuery(fieldName, pe, true, "DFlow"));
        } catch (Exception e) {}
    }

    /**
     * This method resolves the given user login first. If aMember is true and
     * the user login corresponds to a mailing list then the immediate members
     * of this mailing list are retrieved and a comma-separated list of ids
     * is returned. If aExpand is true then the mailing list is recursively
     * resolved until it contains no more mailing list members and then a
     * comma separated list of Ids is returned.
     *
     * @param aLogin         User Login.
     * @param aMember        True if member of this Mailing list are required.
     * @param aExpand        True if member users of this mailing list are
     *                       needed
     * @param aResolvedList  OUT parameter to hold the ids.
     * @return Number of ids present in the resolved list.
     */
    private static int resolveAbsolute(String aLogin, boolean aMember, boolean aExpand, StringBuffer aResolvedList) {
        int  userCount = 0;
        User user      = null;

        try {
            user = User.lookupAllByUserLogin(aLogin);
        } catch (DatabaseException de) {
            LOG.warning("",(de));
        }

        if (user == null) {

            /*
             * Since the login could not be resolved, return -1.
             */
            aResolvedList.append("-1");
            userCount = 1;

            return userCount;
        }

        /*
         * Check if the caller wants us to consider the members or expand this
         * entry.
         */
        if ((aMember == true) || (aExpand == true)) {
            int userId     = user.getUserId();
            int userTypeId = user.getUserTypeId();

            // Then the prerequisite is, this entry should be a mailing list.
            if (userTypeId == UserType.INTERNAL_MAILINGLIST) {
                ArrayList<User> members = null;

                if (aExpand == true) {
                    members = MailListUser.getMemberUsers(userId);
                } else {
                    members = MailListUser.getImmediateMembers(userId);
                }

                if ((members != null) && (members.size() != 0)) {
                    boolean first = true;

                    aResolvedList.append("(");

                    for (User member : members) {
                        if (first == false) {
                            aResolvedList.append(" OR ");
                        } else {
                            first = false;
                        }

                        aResolvedList.append(member.getUserLogin());
                    }

                    aResolvedList.append(")");
                    userCount = members.size();
                }    // End If member list is non-null and non-empty.
                        else {
                    aResolvedList.append(user.getUserLogin());
                    userCount = 1;
                }
            }        // End If userType is Mailing list.
                    else {
                aResolvedList.append(user.getUserLogin());
                userCount = 1;
            }
        }            // End If member/expand is true.
                else {
            aResolvedList.append(user.getUserLogin());
            userCount = 1;
        }

        return userCount;
    }

    /**
     * This method resolves the given user login first. If aMember is true and
     * the user login corresponds to a mailing list then the immediate members
     * of this mailing list are retrieved and a comma-separated list of ids
     * is returned. If aExpand is true then the mailing list is recursively
     * resolved until it contains no more mailing list members and then a
     * comma separated list of Ids is returned.
     *
     * @param aLogin         User Login.
     * @param aMember        True if member of this Mailing list are required.
     * @param aExpand        True if member users of this mailing list are
     *                       needed
     * @param aResolvedList  OUT parameter to hold the ids.
     * @return Number of ids present in the resolved list.
     */
    private static int resolveLike(String aLogin, boolean aMember, boolean aExpand, StringBuffer aResolvedList) {
        int             userCount = 0;
        ArrayList<User> members   = null;

        try {

            /*
             * Check if the caller wants us to consider the members or expand
             * this entry.
             */
            if ((aMember == true) || (aExpand == true)) {
                if (aExpand == true) {
                    members = MailListUser.getMemberUsersByEmailLike(aLogin);
                } else {
                    members = MailListUser.getImmediateMembersByEmailLike(aLogin);
                }
            } else {
                members = User.lookupByUserLoginLike(aLogin, false);
            }
        } catch (DatabaseException de) {
            LOG.warn("",(de));
            members = null;
        }

        if ((members != null) && (members.size() != 0)) {
            boolean first = true;

            aResolvedList.append("(");

            for (User member : members) {
                if (first == false) {
                    aResolvedList.append(" OR ");
                } else {
                    first = false;
                }

                aResolvedList.append(member.getUserLogin());
            }

            aResolvedList.append(")");
            userCount = members.size();
        } else {
            userCount = 1;
            aResolvedList.append("-1");
        }

        return userCount;
    }

    /**
     * This method returns the GMT equivalent of the time passed given the
     * zone and the format the time is in.
     *
     * @param aTime         Time as a string.
     * @param aAdd          If a day should be added.
     *
     * @return GMT Equivalent of the given time in given zone.
     */
    private static String toGmt(String aTime, boolean aAdd) {
        TimeZone sourceZone   = TimeZone.getDefault();
        String   targetFormat = TBitsConstants.LUCENE_DATE_FORMAT;
        String   sourceFormat = TBitsConstants.API_DATE_FORMAT;

        try {
            SimpleDateFormat gmt = new SimpleDateFormat(targetFormat);

            gmt.setTimeZone(TimeZone.getTimeZone("GMT"));

            SimpleDateFormat pre = new SimpleDateFormat(sourceFormat);

            pre.setTimeZone(sourceZone);

            Calendar cal = Calendar.getInstance();

            cal.setTime(pre.parse(aTime));

            if (aAdd == true) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }

            String strTime = gmt.format(cal.getTime());

            return strTime;
        } catch (Exception e) {
            return aTime;
        }
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method returns the join part and the condition part of the query
     * for a boolean field.
     *
     */
    public static String getBooleanQuery(String fieldName, ParseEntry entry, boolean first, String prefix) {
        StringBuilder filter = new StringBuilder();
        Connective    conn   = entry.getConnective();

        filter.append(getConnective(first, conn, prefix)).append(" ").append(fieldName).append(":(");

        ArrayList<String> argList = entry.getArgList();

        if ((argList == null) || (argList.size() == 0)) {
            return "";
        }

        String arg = argList.get(0);

        if (arg.trim().equals("true") || arg.trim().equals("1") || arg.trim().equals("yes")) {
            filter.append("true");
        } else {
            filter.append("false");
        }

        filter.append(")");

        return filter.toString();
    }

    private static String getConnective(boolean first, Connective conn, String prefix) {
        StringBuilder query = new StringBuilder();

        if (first == false) {
            switch (conn) {
            case C_AND :
                query.append(" +");

                break;

            case C_OR :
                query.append(" OR ");

                break;

            case C_NOT :
                query.append(" -");

                break;

            case C_AND_NOT :
                query.append(" -");

                break;

            case C_OR_NOT :
                query.append(" OR -");

                break;

            case C_NOT_NOT :
                query.append(" +");

                break;
            }
        } else {
            switch (conn) {
            case C_AND :
            case C_NOT_NOT :
                query.append(" +");

                break;

            case C_OR :
                query.append(" ");

                break;

            case C_NOT :
            case C_AND_NOT :
            case C_OR_NOT :

                /*
                 * Query portion nested in parentheses is executed separately.
                 * And a negation at the beginning of a block is not supported
                 * by lucene 1.4.3. So, we need to add a term that is always
                 * true to get the query working.
                 */
                query.append("+sysPrefix: ").append(prefix);
                query.append(" -");

                break;
            }
        }

        return query.toString();
    }

    /**
     * This method returns the join part and the condition part of the query
     * for a boolean field.
     *
     */
    public static String getDateQuery(String fieldName, ParseEntry entry, boolean first, String prefix) {
        StringBuilder     filter  = new StringBuilder();
        ArrayList<String> argList = entry.getArgList();

        if ((argList == null) || (argList.size() == 0)) {
            return filter.toString();
        }

        Connective conn = entry.getConnective();
        String     desc = entry.getDescriptor();

        if (desc.equals("appenddate") || desc.equals("dateappended")) {
            fieldName = "append_date";
        } else if (desc.equals("closeddate") || desc.equals("dateclosed")) {
            fieldName = "closeddate";
        }

        filter.append(getConnective(first, conn, prefix));

        //
        // Expected formats of argument list are
        // - [ BEFORE, Date ]
        // - [ AFTER, Date ]
        // - [ BETWEEN, StartDate, EndDate ]
        // - [ Date ]
        //
        String optr = argList.get(0);

        if (optr.equals(BEFORE)) {
            String date = toGmt(argList.get(1), false);

            filter.append(" ").append(fieldName).append(":");

            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                filter.append("[").append(date).append(" TO 99999999999999]");
            } else {
                filter.append("[00000000000000 TO ").append(date).append("]");
            }
        } else if (optr.equals(AFTER)) {
            String date = toGmt(argList.get(1), true);

            filter.append(" ").append(fieldName).append(":");

            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                filter.append("[00000000000000 TO ").append(date).append("]");
            } else {
                filter.append("[").append(date).append(" TO 99999999999999]");
            }
        } else if (optr.equals(BETWEEN)) {
            if (argList.size() != 3) {
                return "";
            }

            String fromDate = toGmt(argList.get(1), false);
            String toDate   = toGmt(argList.get(2), false);

            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                filter.append(" (").append(fieldName).append(":").append("[00000000000000 TO ").append(fromDate).append("] OR ").append(fieldName).append(":").append(toDate).append(" TO ").append(
                    "99999999999999]").append(") ");
            } else {
                filter.append(" ").append(fieldName).append(":").append("[").append(fromDate).append(" TO ").append(toDate).append("]");
            }
        } else {
            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                filter.append(" <> ");
            } else {
                filter.append(" = ");
            }

            filter.append("'").append(toGmt(argList.get(0), false)).append("'");
        }

        LOG.info(filter.toString());

        return filter.toString();
    }

    /**
     * This method returns the join part and the condition part of the query
     * for a multi-valued field.
     *
     */
    public static String getMultiValueQuery(String fieldName, ParseEntry entry, boolean first, String prefix) {
        StringBuilder query      = new StringBuilder();
        Connective    connective = entry.getConnective();
        String        clause     = entry.getClause();
        String        desc       = entry.getDescriptor();

        if (desc.equals("appender") || desc.equals("actionuser")) {
            fieldName = "appender";
        } else if (desc.equals("closedby")) {
            fieldName = "closedby";
        }

        boolean member = false;
        boolean expand = false;

        /*
         * If the clause is MEMBER_OF/MEMBERS_OF then set the member flag to
         * true and expand flag to false.
         *
         * If the clause is EXPAND then set both the member and expand flags to
         * true.
         */
        if (clause != null) {
            clause = clause.trim();

            if (clause.equals(MEMBER_OF) || clause.equals(MEMBERS_OF)) {
                member = true;
                expand = false;
            }

            if (clause.equals(EXPAND)) {
                member = true;
                expand = true;
            }
        }

        query.append(getConnective(first, connective, prefix));
        query.append(fieldName).append(":(");

        boolean           start   = true;
        ArrayList<String> argList = entry.getArgList();

        for (String arg : argList) {
            if (start == false) {
                if (arg.startsWith("+") == true) {
                    arg = arg.substring(1);
                    query.append(" AND ");
                } else if (arg.startsWith("-") == true) {
                    arg = arg.substring(1);
                    query.append(" NOT ");
                } else {
                    query.append(" OR ");
                }
            } else {
                start = false;
            }

            boolean like = false;

            // Check if the argument requires us to perform a like search
            if (arg.endsWith("*")) {
                like = true;
            }

            if ((expand == true) || (member == true)) {
                StringBuffer resolvedList = new StringBuffer();

                if (like == true) {
                    arg = arg.substring(0, arg.length() - 1);
                    resolveLike(arg, member, expand, resolvedList);
                } else {
                    resolveAbsolute(arg, member, expand, resolvedList);
                }

                query.append(" ").append(escapeLuceneSpecialChars(resolvedList.toString()));
            } else {
                query.append(escapeLuceneSpecialChars(arg));
            }
        }

        query.append(")");

        return query.toString();
    }

    /**
     * This method returns the join part and the condition part of the query
     * for a numeric field.
     *
     */
    public static String getNumericQuery(String fieldName, ParseEntry entry, boolean first, String prefix) {
        StringBuilder filter = new StringBuilder();

        return filter.toString();
    }

    /**
     * This method returns the join part and the condition part of the query
     * for a numeric field.
     *
     */
    public static String getTextQuery(String fieldName, ParseEntry entry, boolean first, String prefix) {
        StringBuilder query      = new StringBuilder();
        Connective    connective = entry.getConnective();

        query.append(getConnective(first, connective, prefix));

        ArrayList<String> argList = entry.getArgList();

        query.append(fieldName).append(":(");

        boolean start = true;

        for (String arg : argList) {
            if (start == false) {
                if (arg.startsWith("+") == true) {
                    arg = arg.substring(1).trim();
                    query.append(" AND ");
                } else if (arg.startsWith("-") == true) {
                    arg = arg.substring(1).trim();
                    query.append(" NOT ");
                } else {
                    query.append(" OR ");
                }
            } else {
                start = false;
            }

            arg = escapeLuceneSpecialChars(arg);
            query.append(arg);
        }

        query.append(")");

        return query.toString();
    }

    /**
     * This method returns the join part and the condition part of the query
     * for a type field.
     *
     */
    public static String getTypeQuery(String fieldName, ParseEntry entry, boolean first, String prefix) {
        StringBuilder query      = new StringBuilder();
        Connective    connective = entry.getConnective();

        query.append(getConnective(first, connective, prefix));
        query.append(fieldName).append(":(");

        boolean start = true;

        for (String arg : entry.getArgList()) {
            if (start == false) {
                query.append(" OR ");
            } else {
                start = false;
            }

            query.append(escapeLuceneSpecialChars(arg));
        }

        query.append(")");

        return query.toString();
    }
}
