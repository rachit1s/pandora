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
 * DbQueryGenerator.java
 *
 * $Header:
 *
 */
package transbit.tbits.search;

//~--- non-JDK imports --------------------------------------------------------

//TBits Imports.
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.MailListUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;

//Static imports
import static transbit.tbits.Helper.TBitsConstants.PKG_SEARCH;
import static transbit.tbits.domain.DataType.INT;
import static transbit.tbits.domain.DataType.STRING;

//~--- JDK imports ------------------------------------------------------------

import java.text.SimpleDateFormat;

//Java Imports.
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

//~--- classes ----------------------------------------------------------------

public class DbQueryGenerator implements SearchConstants {
    private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_SEARCH);

    //~--- methods ------------------------------------------------------------

    /**
     * Returns the filter statements for a boolean field.
     *
     * @param fieldName  Name of the boolean field.
     * @param entry      Parse Entry corresponding to this field.
     * @param first      If this entry is first in the block.
     *
     * @return Filtering Part of the query.
     */
    private static String booleanFilter(String fieldName, ParseEntry entry, boolean first) {
        StringBuilder filter = new StringBuilder();
        Connective    conn   = entry.getConnective();

        filter.append("\n\t\t").append(getConnective(first, conn));

        ArrayList<String> argList = entry.getArgList();

        filter.append(fieldName);

        // If the argument list is empty, then check is to see if the boolean
        // field is null or not.
        if ((argList == null) || (argList.size() == 0)) {
            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                filter.append(" = 0 ");
            } else {
                filter.append(" = 1 ");
            }
        } else {

            //
            // Expected formats of argument list are
            // - [ Boolean Value(1/0) ]
            //
            if ((conn == Connective.C_NOT) || (conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT)) {
                filter.append(" <> ");
            } else {
                filter.append(" = ");
            }

            filter.append(argList.get(0));
        }

        return filter.toString();
    }

    /**
     * Returns the filter statements for a date field.
     *
     * @param fieldName  Name of the boolean field.
     * @param entry      Parse Entry corresponding to this field.
     * @param first      If this entry is first in the block.
     *
     * @return Filtering Part of the query.
     */
    private static String dateFilter(String fieldName, ParseEntry entry, boolean first) {
        StringBuilder     filter  = new StringBuilder();
        ArrayList<String> argList = entry.getArgList();

        // Append the right connective.
        filter.append("\n\t\t");

        Connective conn = entry.getConnective();

        filter.append(getConnective(first, conn)).append(fieldName);

        // If the argument list is empty, then check is to see if the date
        // field is null or not.
        if ((argList == null) || (argList.size() == 0)) {
            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                filter.append(" IS NOT NULL ");
            } else {
                filter.append(" IS NULL ");
            }
        } else {

            //
            // Expected formats of argument list are
            // - [ BEFORE, Date ]
            // - [ AFTER, Date ]
            // - [ BETWEEN, StartDate, EndDate ]
            // - [ Date ]
            //
            String optr = argList.get(0);

            if (optr.equals(BEFORE)) {
                if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                    filter.append(" >= ");
                } else {
                    filter.append(" < ");
                }

                filter.append("'").append(toGmt(argList.get(1), false)).append("'");
            } else if (optr.equals(AFTER)) {
                if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                    filter.append(" <= ");
                } else {
                    filter.append(" > ");
                }

                filter.append("'").append(toGmt(argList.get(1), true)).append("'");
            } else if (optr.equals(BETWEEN)) {
                if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                    filter.append(" NOT BETWEEN");
                } else {
                    filter.append(" BETWEEN ");
                }

                filter.append("'").append(toGmt(argList.get(1), false)).append("' AND '").append(toGmt(argList.get(2), false)).append("'");
            } else {
                if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                    filter.append(" <> ");
                } else {
                    filter.append(" = ");
                }

                filter.append("'").append(toGmt(argList.get(0), false)).append("'");
            }
        }

        LOG.debug(filter.toString());

        return filter.toString();
    }

    public static void main(String arg[]) throws Exception {}

    /**
     * Returns the filter statements for a boolean field.
     *
     * @param fieldName  Name of the boolean field.
     * @param entry      Parse Entry corresponding to this field.
     * @param first      If this entry is first in the block.
     *
     * @return Filtering Part of the query.
     */
    private static String numericFilter(String fieldName, Field field, ParseEntry entry, boolean first) {
        StringBuilder     filter  = new StringBuilder();
        ArrayList<String> argList = entry.getArgList();
        Connective        conn    = entry.getConnective();

        filter.append("\n\t\t").append(getConnective(first, conn));

        if (field.getName().equals(Field.PARENT_REQUEST_ID) && argList.get(0).equals("-1")) {
            int systemId = field.getSystemId();

            filter.append("r.request_id ");

            // Default to EQ operator.
            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                filter.append(" IN ");
            } else {
                filter.append(" NOT IN ");
            }

            filter.append("(").append("SELECT parent_request_id FROM requests WHERE ").append("sys_id = ").append(systemId).append("AND parent_request_id <> 0").append(")");

            return filter.toString();
        }

        filter.append(fieldName);

        int    size = argList.size();
        String optr = argList.get(0);

        if (optr.equals(IN)) {
            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                filter.append(" NOT IN ");
            } else {
                filter.append(" IN ");
            }

            filter.append("(");

            for (int i = 1; i < size; i++) {
                if (i != 1) {
                    filter.append(", ");
                }

                filter.append(argList.get(i));
            }

            filter.append(")");
        } else if (optr.equals(GT)) {
            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                filter.append(" <= ");
            } else {
                filter.append(" > ");
            }

            filter.append(argList.get(1));
        } else if (optr.equals(GE)) {
            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                filter.append(" < ");
            } else {
                filter.append(" >= ");
            }

            filter.append(argList.get(1));
        } else if (optr.equals(LT)) {
            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                filter.append(" >= ");
            } else {
                filter.append(" < ");
            }

            filter.append(argList.get(1));
        } else if (optr.equals(LE)) {
            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                filter.append(" > ");
            } else {
                filter.append(" <= ");
            }

            filter.append(argList.get(1));
        } else if (optr.equals(BETWEEN)) {
            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                filter.append(" NOT BETWEEN ");
            } else {
                filter.append(" BETWEEN ");
            }

            filter.append(argList.get(1)).append(" AND ").append(argList.get(2));
        } else {

            // Default to EQ operator.
            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                filter.append(" <> ");
            } else {
                filter.append(" = ");
            }

            filter.append(argList.get(0));
        }

        return filter.toString();
    }

    /**
     * Returns the filter statements for a is_private field.
     *
     * @param fieldName  Name of the boolean field.
     * @param entry      Parse Entry corresponding to this field.
     * @param first      If this entry is first in the block.
     *
     * @return Filtering Part of the query.
     */
    private static String privateFilter(String fieldName, ParseEntry entry, boolean first) {
        StringBuilder filter = new StringBuilder();
        Connective    conn   = entry.getConnective();

        filter.append("\n\t\t").append(getConnective(first, conn));

        ArrayList<String> argList = entry.getArgList();
        String            value   = "0";

        if ((argList == null) || (argList.size() == 0)) {
            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                value = "0";
            } else {
                value = "1";
            }
        } else {
            if ((conn == Connective.C_NOT) || (conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT)) {
                if (argList.get(0).equals("0")) {
                    value = "1";
                } else {
                    value = "0";
                }
            } else {
                value = argList.get(0);
            }
        }

        filter.append("\n\t\t(").append("\n\t\t\tr.is_private = ").append(value).append(" OR ").append("\n\t\t\tcat.is_private = ").append(value).append(" OR ").append(
            "\n\t\t\tstat.is_private = ").append(value).append(" OR ").append("\n\t\t\tsev.is_private = ").append(value).append(" OR ").append("\n\t\t\treqType.is_private = ").append(value).append(
            "\n\t\t)");

        return filter.toString();
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

                    for (User member : members) {
                        if (first == false) {
                            aResolvedList.append(",");
                        } else {
                            first = false;
                        }

                        aResolvedList.append(member.getUserId());
                    }

                    userCount = members.size();
                }    // End If member list is non-null and non-empty.
                        else {
                    aResolvedList.append(user.getUserId());
                    userCount = 1;
                }
            }        // End If userType is Mailing list.
                    else {
                aResolvedList.append(user.getUserId());
                userCount = 1;
            }
        }            // End If member/expand is true.
                else {
            aResolvedList.append(user.getUserId());
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

            for (User member : members) {
                if (first == false) {
                    aResolvedList.append(",");
                } else {
                    first = false;
                }

                aResolvedList.append(member.getUserId());
            }

            userCount = members.size();
        } else {
            userCount = 1;
            aResolvedList.append("-1");
        }

        return userCount;
    }

    /**
     * This method returns the statement in the select for the given table and
     * field.
     *
     * @param tableAlias Alias name of the table the field belongs to.
     * @param fieldName  Name of the field.
     * @param fieldAlias Alias name of the field we want it to be.
     *
     * @return Select statement.
     */
    public static String select(String tableAlias, String fieldName, String fieldAlias) {
        StringBuilder select = new StringBuilder();

        select.append("\n\t").append(tableAlias).append(".").append(fieldName).append(" '").append(fieldAlias).append("'");

        return select.toString();
    }

    /**
     * This method returns the join and select statement for a UserValue field
     *
     * @param field   Field Object.
     * @param join    Output parameter that holds the join part of query.
     * @param select  Output parameter that holds the filtering part of query.
     *
     */
    public static void selectUsers(Field field, StringBuilder join, StringBuilder select) {
        String fieldName = field.getName();

        if (fieldName.equals(Field.USER)) {
            join.append("\n\tLEFT JOIN users usr").append("\n\tON usr.user_id = r.user_id");
            select.append("\n\tusr.user_login 'user_id'");
        }

        return;
    }

    /**
     * This method returns the join and select statement for a type field
     *
     * @param field   Field Object.
     * @param join    Output parameter that holds the join part of query.
     * @param select  Output parameter that holds the filtering part of query.
     *
     */
    public static void selectType(Field field, StringBuilder join, StringBuilder select) {
        int    fieldId    = field.getFieldId();
        String tableAlias = "r";
        String fieldName  = field.getName();
        String tName      = "t" + (fieldId + 1);

        if (fieldName.equals(Field.CATEGORY)) {
            tName = "cat";
        } else if (fieldName.equals(Field.STATUS)) {
            tName = "stat";
        } else if (fieldName.equals(Field.SEVERITY)) {
            tName = "sev";
        } else if (fieldName.equals(Field.REQUEST_TYPE)) {
            tName = "reqType";
        }

        if (join != null) {
            join.append("\n\tLEFT JOIN types ").append(tName).append("\n\tON ").append(tableAlias).append(".sys_id = ").append(tName).append(".sys_id AND ").append(tName).append(
                ".field_id = ").append(fieldId).append(" AND ").append(tName).append(".type_id = ").append(tableAlias).append(".").append(fieldName);
        }

        select.append("\n\tcase ").append(tName).append(".is_private").append("\n\twhen 1 then '+' + ").append(tName).append(".display_name").append("\n\twhen 0 then ").append(tName).append(
            ".display_name").append("\n\tend ").append(" '").append(fieldName).append("'");

        return;
    }

    /**
     * This method returns the query portion for the given word and the string
     * field.
     *
     * @param arg        Argument.
     * @param connective Connective.
     * @param fieldName  Field name.
     *
     * @return SQL Query part to search for the given argument in the given
     *         field.
     */
    public static String textFilter(String arg, Connective connective, String fieldName) {
        StringBuilder filter = new StringBuilder();
        boolean       start  = false,
                      end    = false;
        String        conn   = " OR ";
        String        optr   = "";

        if ((connective == Connective.C_NOT) || (connective == Connective.C_AND_NOT) || (connective == Connective.C_OR_NOT)) {
            optr = " NOT ";
            conn = " AND ";
        } else {
            optr = "";
            conn = " OR ";
        }

        // Check if the argument starts with a *
        if (arg.startsWith("*") == true) {
            start = true;
            arg   = arg.substring(1);
        }

        // Check if the argument ends with a *
        if (arg.endsWith("*") == true) {
            end = true;
            arg = arg.substring(0, arg.length() - 1);
        }

        filter.append("(").append(fieldName).append(optr).append(" LIKE '").append(arg).append("' ").append(conn).append(fieldName).append(optr).append(" LIKE '").append(arg);

        /*
         * If there is no wildcard at the end, then it should not be followed
         * by alphabets.
         */
        if (end == false) {
            filter.append("[^a-z]");
        }

        filter.append("%' {escape '/'} ").append(conn).append(fieldName).append(optr).append(" LIKE '%");

        /*
         * If there is no wildcard at the start, then it should not be followed
         * by alphabets.
         */
        if (start == false) {
            filter.append("[^a-z]");
        }

        filter.append(arg);

        /*
         * If there is no wildcard at the end, then it should not be followed
         * by alphabets.
         */
        if (end == false) {
            filter.append("[^a-z]");
        }

        filter.append("%' {escape '/'} ").append(conn).append(fieldName).append(optr).append(" LIKE '%");

        /*
         * If there is no wildcard at the start, then it should not be followed
         * by alphabets.
         */
        if (start == false) {
            filter.append("[^a-z]");
        }

        filter.append(arg).append("' {escape '/'}").append(")");

        return filter.toString();
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
        TimeZone sourceZone = TimeZone.getDefault();
        String   format     = "yyyy-MM-dd HH:mm:ss";

        try {
            SimpleDateFormat gmt = new SimpleDateFormat(format);

            gmt.setTimeZone(TimeZone.getTimeZone("GMT"));

            SimpleDateFormat pre = new SimpleDateFormat(format);

            pre.setTimeZone(sourceZone);

            Calendar cal = Calendar.getInstance();

            cal.setTime(pre.parse(aTime));

            if (aAdd == true) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }

            return gmt.format(cal.getTime());
        } catch (Exception e) {
            return aTime;
        }
    }

    /**
     * This method returns the comma separated list of the elements in the list
     * enclosed in quotes.
     *
     * @param aList List of elements.
     *
     * @return Comma separated list of elements in the list enclosed in quotes.
     */
    private static String toQuotedList(ArrayList<String> aList) {
        StringBuilder buffer = new StringBuilder();
        boolean       first  = true;

        for (String str : aList) {
            if (first == false) {
                buffer.append(",");
            } else {
                first = false;
            }

            buffer.append("'").append(str).append("'");
        }

        return buffer.toString();
    }

    /**
     * This method returns the comma separated list of the ids corresponding to
     * the given type names/display names.
     *
     * @param aField        Field object this type corresponds to.
     * @param aList         List of type names/display names.
     *
     * @return Comma separated list of Type ids.
     */
    private static String toTypeIdList(Field aField, ArrayList<String> aList) {
        StringBuilder buffer = new StringBuilder();
        boolean       first  = true;

        for (String str : aList) {
            Type type = null;

            try {
                type = Type.lookupBySystemIdAndFieldNameAndTypeName(aField.getSystemId(), aField.getName(), str);

                if (type == null) {
                    LOG.warn("Type object corresponding to " + str + " could not be obtained.");

                    continue;
                }
            } catch (DatabaseException de) {
                LOG.warn("",(de));

                continue;
            }

            if (first == false) {
                buffer.append(",");
            } else {
                first = false;
            }

            buffer.append(type.getTypeId());
        }

        return buffer.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     *
     * @param field   Field Object.
     * @param entry   Parse Entry corresponding to this Field Object.
     * @param first   Flag to specify if this entry is the first one in a block
     * @param join    Output parameter that holds the join part of query.
     * @param filter  Output parameter that holds the filtering part of query.
     *
     */
    public static void getBooleanQuery(Field field, ParseEntry entry, boolean first, StringBuilder join, StringBuilder filter, boolean flagJoin) {
        if (entry == null) {
            return;
        }

        int    fieldId    = field.getFieldId();
        String tableAlias = "r";
        String fieldName  = field.getName();
        String fieldAlias = field.getName();

        if (field.getIsExtended() == true) {
            tableAlias = "re" + fieldId;
            fieldName  = "bit_value";

            if (flagJoin == true) {
                join.append(getJoinQuery(fieldId, tableAlias));
            }
        }

        if (fieldName.equals(Field.IS_PRIVATE)) {
            filter.append(privateFilter(tableAlias + "." + fieldName, entry, first));
        } else {
            filter.append(booleanFilter(tableAlias + "." + fieldName, entry, first));
        }

        return;
    }

    private static String getConnective(boolean first, Connective connective) {
        String str = "";

        if (first == false) {
            switch (connective) {
            case C_AND :
            case C_AND_NOT :
                str = "AND ";

                break;

            case C_OR :
            case C_OR_NOT :
                str = "OR  ";

                break;

            case C_NOT :
            case C_NOT_NOT :
                str = "AND ";

                break;
            }
        }

        return str;
    }

    /**
     *
     * @param field   Field Object.
     * @param entry   Parse Entry corresponding to this Field Object.
     * @param first   Flag to specify if this entry is the first one in a block
     * @param join    Output parameter that holds the join part of query.
     * @param filter  Output parameter that holds the filtering part of query.
     *
     */
    public static void getDateQuery(Field field, ParseEntry entry, boolean first, StringBuilder join, StringBuilder filter, boolean flagJoin) {
        if (entry == null) {
            return;
        }

        int    fieldId    = field.getFieldId();
        String tableAlias = "r";
        String fieldName  = field.getName();
        String fieldAlias = field.getName();

        if (field.getIsExtended() == true) {
            tableAlias = "re" + fieldId;
            fieldName  = "datetime_value";

            if (flagJoin == true) {
                join.append(getJoinQuery(fieldId, tableAlias));
            }
        }

        filter.append(dateFilter(tableAlias + "." + fieldName, entry, first));
    }

    /**
     * This method returns the statements to join a requests_ex table, given
     * the table name and the field id
     *
     * @param fieldId   Id of the field.
     * @param aliasName alias name to the table.
     *
     * @return Join statements.
     */
    private static String getJoinQuery(int fieldId, String aliasName) {
        StringBuilder join = new StringBuilder();

        join.append("\n\tLEFT JOIN requests_ex ").append(aliasName).append("\n\tON ").append(aliasName).append(".sys_id = r.sys_id AND ").append(aliasName).append(
            ".request_id = r.request_id AND ").append(aliasName).append(".field_id = ").append(fieldId);

        return join.toString();
    }

    /**
     * This method returns the query portion related to a list of given
     * userlogins in the argument list of the parse entry connected with AND
     * or OR or NOT.
     *
     * @param field      Field Object.
     * @param entry      Parse Entry corresponding to this Field Object.
     * @param first      Flag to specify if this entry is the first in a block
     * @param join       Output param that holds the join part of query.
     * @param filter     Output param that holds the filtering part of query.
     * @param userTypeId User Type Id.
     * @param counter    Counter to be appended to the table alias to make
     *                   them unique.
     */
    private static void getMVQuery(Field field, ParseEntry entry, boolean first, StringBuilder join, StringBuilder filter, int counter, boolean member, boolean expand) {

        //
        // Get the field name, connective and the argument list.
        //
        int               fieldId   = field.getFieldId();
        String            fieldName = entry.getFieldName();
        Connective        conn      = entry.getConnective();
        ArrayList<String> argList   = entry.getArgList();
        int               iCtr      = 1;

        //
        // Always join the first request users table with the requests table.
        // We always alias the requests table with "r".
        //
        String ruName = "r";

        // Indenting the query.
        filter.append("\n\t\t");

        // This is to check if the first argument is processed.
        boolean argFirst = true;

        for (String arg : argList) {
            boolean prim = false;
            boolean like = false;
            int     optr = 0;

            // Check if this argument starts with "+".
            if (arg.startsWith("+")) {
                optr = 1;
                arg  = arg.substring(1);
            }

            // Check if this argument starts with "-".
            else if (arg.startsWith("-")) {
                optr = -1;
                arg  = arg.substring(1);
            }

            // Check if this argument is to filter on primary column.
            if (arg.startsWith("*")) {
                prim = true;
                arg  = arg.substring(1);
            }

            // Check if the argument requires us to perform a like search
            if (arg.endsWith("*")) {
                like = true;
                arg  = arg.substring(0, arg.length() - 1);
            }

            // If the operator is AND or OR
            if ((optr == 0) || (optr == 1)) {

                // Add the connective accordingly.
                if (argFirst == false) {
                    if (optr == 0) {
                        filter.append(" OR ");
                    } else {
                        filter.append(" AND ");
                    }
                } else {
                    argFirst = false;
                }

                // Join the request users table.
                String rujName = "ru" + fieldId + counter + iCtr;

                iCtr = iCtr + 1;
                join.append("\n\tLEFT JOIN request_users ").append(rujName).append("\n\tON ").append(ruName).append(".sys_id = ").append(rujName).append(".sys_id AND ").append(ruName).append(
                    ".request_id = ").append(rujName).append(".request_id AND ").append(rujName).append(".field_id = ").append(field.getFieldId());
                ruName = rujName;
                filter.append("\n\t\t\t(");

                if (prim == true) {
                    filter.append("\n\t\t\t\t").append(rujName).append(".is_primary = 1 AND ");
                }

                filter.append("\n\t\t\t\t").append(rujName).append(".user_id ");

                if (like == true) {
                    if ((expand == true) || (member == true)) {
                        StringBuffer resolvedList = new StringBuffer();
                        int          userCount    = 0;

                        resolveLike(arg, member, expand, resolvedList);
                        filter.append(" IN (").append(resolvedList).append(")");
                    } else {
                        filter.append(" IN \n\t\t\t\t(").append("\n\t\t\t\t\t").append("SELECT user_id FROM users WHERE ").append("user_login LIKE '").append(arg).append("%'\n\t\t\t\t)");
                    }
                } else {
                    StringBuffer resolvedList = new StringBuffer();
                    int          userCount    = resolveAbsolute(arg, member, expand, resolvedList);
                    StringBuffer userPart     = new StringBuffer();

                    if (userCount > 1) {
                        userPart.append(" IN (").append(resolvedList).append(")");
                    } else {
                        userPart.append(" = ").append(resolvedList).append(" ");
                    }

                    filter.append(userPart);
                }

                filter.append("\n\t\t\t)");
            } else if (optr == -1) {
                if (argFirst == false) {
                    filter.append("\n\t\t\tAND ");
                } else {
                    argFirst = false;
                }

                filter.append("\n\t\t\t(").append("\n\t\t\t\tr.request_id NOT IN ").append("\n\t\t\t\t(");
                filter.append("\n\t\t\t\t\tSELECT ").append("\n\t\t\t\t\t\trequest_id ").append("\n\t\t\t\t\tFROM ").append("\n\t\t\t\t\t\trequest_users ").append("\n\t\t\t\t\tWHERE ").append(
                    "\n\t\t\t\t\t\tsys_id = ").append(field.getSystemId()).append(" AND ").append("\n\t\t\t\t\t\field_id = ").append(field.getFieldId()).append(" AND ");

                if (prim == true) {
                    filter.append("\n\t\t\t\t\t\tis_primary = 1 AND ");
                }

                filter.append("\n\t\t\t\t\t\tuser_id ");

                if (like == true) {
                    if ((expand == true) || (member == true)) {
                        StringBuffer resolvedList = new StringBuffer();
                        int          userCount    = 0;

                        resolveLike(arg, member, expand, resolvedList);
                        filter.append(" IN (").append(resolvedList).append(")");
                    } else {
                        filter.append(" IN ").append("\n\t\t\t\t\t\t(").append("\n\t\t\t\t\t\t\tSELECT ").append("\n\t\t\t\t\t\t\t\tuser_id ").append("\n\t\t\t\t\t\t\tFROM ").append(
                            "\n\t\t\t\t\t\t\t\tusers ").append("\n\t\t\t\t\t\t\tWHERE ").append("\n\t\t\t\t\t\t\t\tuser_login LIKE '").append(arg).append("%'\n\t\t\t\t\t\t)");
                    }
                } else {
                    StringBuffer resolvedList = new StringBuffer();
                    int          userCount    = resolveAbsolute(arg, member, expand, resolvedList);
                    StringBuffer userPart     = new StringBuffer();

                    if (userCount > 1) {
                        userPart.append(" IN (").append(resolvedList).append(")");
                    } else {
                        userPart.append(" = ").append(resolvedList).append(" ");
                    }

                    filter.append(userPart);
                }

                filter.append("\n\t\t\t\t)\n\t\t\t)");
            }
        }

        return;
    }

    /**
     * This method returns the join part and the condition part of the query
     * for a user-typed field.
     *
     * @param field   Field Object.
     * @param entry   Parse Entry corresponding to this Field Object.
     * @param first   Flag to specify if this entry is the first one in a block
     * @param join    Output parameter that holds the join part of query.
     * @param filter  Output parameter that holds the filtering part of query.
     *
     */
    public static void getUserTypeQuery(Field field, ParseEntry entry, boolean first, StringBuilder join, StringBuilder filter, int counter) {
        if (entry == null) {
            return;
        }

        String fieldName = entry.getFieldName();

        if (fieldName.equalsIgnoreCase(Field.USER)) {
            getUserQuery(field, entry, first, filter);

            return;
        }

        Connective        conn       = entry.getConnective();
        ArrayList<String> argList    = entry.getArgList();
//        int               userTypeId = getUserTypeId(fieldName);
        String            ruName     = "ru" + field.getFieldId() + Integer.toString(counter);

        if ((argList == null) || (argList.size() == 0)) {

            /*
             * This is an entry to check if the field is null or not.
             * Simplest of all the user-type queries.
             */
            join.append("\n\tLEFT JOIN request_users ").append(ruName).append("\n\tON ").append(ruName).append(".sys_id = r.sys_id AND ").append(ruName).append(
                ".request_id = r.request_id AND ").append(ruName).append(".field_id = ").append(field.getFieldId());
            filter.append("\n\t\t").append(getConnective(first, conn));
            filter.append(ruName).append(".user_id IS ").append(((conn == Connective.C_NOT) || (conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT))
                    ? " NOT "
                    : "").append("NULL");

            return;
        }

        String  clause = entry.getClause();
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

        int     iCtr  = 1;
        boolean bDisj = true;

        for (String arg : argList) {
            if (arg.startsWith("+") || arg.startsWith("-")) {
                bDisj = false;
            }
        }

        StringBuilder jq = new StringBuilder();
        StringBuilder fq = new StringBuilder();

        if (bDisj == true) {
            getORUserTypeQuery(field, entry, first, jq, fq, counter, member, expand);
        } else {
            getMVQuery(field, entry, first, jq, fq, counter, member, expand);
        }

        if ((conn == Connective.C_NOT) || (conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT)) {
            filter.append("\n\t\tr.request_id NOT IN \n\t\t(").append("\n\t\t\tSELECT ").append("\n\t\t\t\tr.request_id ").append("\n\t\t\tFROM\n\t\t\t\trequests r").append(
                jq.toString().replaceAll("\n\t", "\n\t\t\t\t")).append(" \n\t\t\tWHERE \n\t\t\t\tr.sys_id = ").append(field.getSystemId()).append(" AND \n\t\t\t\t(").append(
                fq.toString().replaceAll("\n", "\n\t\t\t")).append(")\n\t\t)");
        } else {
            filter.append(fq.toString());
            join.append(jq.toString());
        }

        //
        // Finally apply the connective to this query portion.
        //
        if (first == false) {
            switch (conn) {
            case C_AND :
            case C_AND_NOT :
                filter.insert(0, "\n\t\tAND ");

                break;

            case C_OR :
            case C_OR_NOT :
                filter.insert(0, "\n\t\tOR  ");

                break;

            case C_NOT :
            case C_NOT_NOT :
                filter.insert(0, "\n\t\tAND ");

                break;
            }
        }

        return;
    }

    /**
     *
     * @param field   Field Object.
     * @param entry   Parse Entry corresponding to this Field Object.
     * @param first   Flag to specify if this entry is the first one in a block
     * @param join    Output parameter that holds the join part of query.
     * @param filter  Output parameter that holds the filtering part of query.
     *
     */
    public static void getNumericQuery(Field field, ParseEntry entry, boolean first, StringBuilder join, StringBuilder filter, boolean flagJoin) {
        if (entry == null) {
            return;
        }

        int    fieldId    = field.getFieldId();
        String tableAlias = "r";
        String fieldName  = field.getName();
        String fieldAlias = field.getName();

        if (field.getIsExtended() == true) {
            tableAlias = "re" + fieldId;

            if (field.getDataTypeId() == INT) {
                fieldName = "int_value";
            } else {
                fieldName = "real_value";
            }

            if (flagJoin == true) {
                join.append(getJoinQuery(fieldId, tableAlias));
            }
        }

        filter.append(numericFilter(tableAlias + "." + fieldName, field, entry, first));

        return;
    }

    /**
     * This method returns the query portion related to a disjunction list
     * of given userlogins in the argument list of the parse entry.
     *
     * @param field      Field Object.
     * @param entry      Parse Entry corresponding to this Field Object.
     * @param first      Flag to specify if this entry is the first in a block
     * @param join       Output param that holds the join part of query.
     * @param filter     Output param that holds the filtering part of query.
     * @param userTypeId User Type Id.
     * @param counter    Counter to be appended to the table alias to make
     *                   them unique.
     */
    private static void getORUserTypeQuery(Field field, ParseEntry entry, boolean first, StringBuilder join, StringBuilder filter, int counter, boolean member, boolean expand) {

        /*
         * Get the field name, connective and the argument list.
         */
        String            fieldName = entry.getFieldName();
        Connective        conn      = entry.getConnective();
        ArrayList<String> argList   = entry.getArgList();

        // form the table alias with the given counter and the field id.
        String ruName = "ru" + field.getFieldId() + Integer.toString(counter);

        /*
         * Join the request users table to the list with the given usertypeid
         * in the join condition to reduce the record count after join.
         */
        join.append("\n\tLEFT JOIN request_users ").append(ruName).append("\n\tON ").append(ruName).append(".sys_id = r.sys_id AND ").append(ruName).append(".request_id = r.request_id AND ").append(
            ruName).append(".field_id = ").append(field.getFieldId());

        // This contains the comma-separated list of user ids.
        StringBuilder idList = new StringBuilder();

        /*
         * This contains the "OR"-separated list of "user_login like 'user%'"
         * entries.
         */
        StringBuilder likeList = new StringBuilder();

        // This is to check if the first argument is processed.
        boolean argFirst = true;

        // This is to check if user id to be put in the idList is the first one
        boolean lstFirst = true;

        /*
         * This is to check if the like record to be put in the Like List is
         * the first one.
         */
        boolean likeFirst = true;

        for (String arg : argList) {
            boolean prim = false;
            boolean like = false;

            // Check if this argument is to check for primary user.
            if (arg.startsWith("*")) {
                prim = true;
                arg  = arg.substring(1);
            }

            // Check if the argument requires us to perform a like search
            if (arg.endsWith("*")) {
                like = true;
                arg  = arg.substring(0, arg.length() - 1);
            }

            // If it is like search...
            if (like == true) {
                if ((expand == true) || (member == true)) {
                    StringBuffer resolvedList = new StringBuffer();
                    int          userCount    = 0;

                    resolveLike(arg, member, expand, resolvedList);

                    if (prim == true) {
                        if (argFirst == false) {
                            filter.append(" OR ");
                        }

                        filter.append("\n\t\t(\n\t\t\t").append(ruName).append(".user_id ").append(" IN \n\t\t\t(").append(resolvedList).append(")").append(" AND \n\t\t\t").append(ruName).append(
                            ".is_primary = 1");
                        filter.append("\n\t\t)");

                        /*
                         * Since atleast one is processed, set argFirst = false;
                         */
                        argFirst = false;
                    }    // End If primary is true.
                            else {
                        if (likeFirst == false) {
                            likeList.append(" OR ");
                        } else {
                            likeFirst = false;
                        }

                        {
                            likeList.append("\n\t\t\t\tuser_id IN (").append(resolvedList).append(")");
                        }
                    }
                }        // End If expand/member == true;
                        else {

                    /*
                     * And, If the user should be primary, then join a new
                     * user table to the existing request users table on the
                     * user_id column, doing a like search on the user login.
                     * Also, apply the is_primary condition at the same time
                     * on the request users table.
                     * e.g.
                     *      (
                     *          ru.user_id IN
                     *          (
                     *              SELECT
                     *                  user_id
                     *              FROM
                     *                  users
                     *              WHERE
                     *                  user_login LIKE 'arg%'
                     *           )
                     *           AND ru.is_primary = 1
                     *      )
                     */
                    if (prim == true) {
                        if (argFirst == false) {
                            filter.append(" OR ");
                        }

                        filter.append("\n\t\t(\n\t\t\t").append(ruName).append(".user_id ");
                        filter.append(" IN \n\t\t\t(\n\t\t\t\tSELECT ").append("\n\t\t\t\t\tuser_id \n\t\t\t\tFROM ").append("\n\t\t\t\t\tusers \n\t\t\t\tWHERE").append(
                            "\n\t\t\t\t\tuser_login like '").append(arg).append("%'\n\t\t\t)");
                        filter.append(" AND \n\t\t\t").append(ruName).append(".is_primary = 1");
                        filter.append("\n\t\t)");

                        /*
                         * Since atleast one is processed, set argFirst = false;
                         */
                        argFirst = false;
                    }    // End If primary is true.

                    /*
                     * And user does not want the primary filter along with LIKE
                     * search, then add an entry to the like list as follows.
                     * "user_login LIKE 'arg%'"
                     */
                    else {
                        if (likeFirst == false) {
                            likeList.append(" OR ");
                        } else {
                            likeFirst = false;
                        }

                        {
                            likeList.append("\n\t\t\t\tuser_login LIKE '").append(arg).append("%'");
                        }
                    }
                }
            }            // End If Like search is true.
                    else {
                StringBuffer resolvedList = new StringBuffer();
                int          userCount    = resolveAbsolute(arg, member, expand, resolvedList);
                StringBuffer userPart     = new StringBuffer();

                if (userCount > 1) {
                    userPart.append(" IN (").append(resolvedList).append(")");
                } else {
                    userPart.append(" = ").append(resolvedList).append(" ");
                }

                if (prim == true) {

                    /*
                     * If this user should be primary, then apply the filter
                     * here itself.
                     * e.g.
                     *    (
                     *        ru.user_id = userId AND ru.is_primary = 1
                     *    )
                     */
                    if (argFirst == false) {
                        filter.append(" OR ");
                    }

                    filter.append("\n\t\t(\n\t\t\t").append(ruName).append(".user_id ").append(userPart).append(" AND \n\t\t\t").append(ruName).append(".is_primary = 1\n\t\t)");

                    // Since this argument is processed, falsify the argFirst.
                    argFirst = false;
                } else {

                    /*
                     * If there is no primary filter on this user, then
                     * add the user id to the comma-separated list.
                     */
                    if (lstFirst == false) {
                        idList.append(",");
                    } else {
                        lstFirst = false;
                    }

                    idList.append(resolvedList);
                }
            }
        }

        /*
         * Check if the comma-separated list of user ids is empty. If it is not
         * then, add the list to the query.
         * e.g.   (ru.user_id IN (userId1, userId2,...userIdN)
         */
        if (idList.toString().trim().equals("") == false) {
            if (argFirst == false) {
                filter.append(" OR ");
            }

            filter.append("\n\t\t\t(\n\t\t\t\t").append(ruName).append(".user_id IN (").append(idList.toString()).append(")\n\t\t\t)");
            argFirst = false;
        }

        /*
         * Check if the OR-separated list of user logins is empty. If it is not
         * then, add the list to the query.
         * e.g.   (ru.user_id IN (
         *                        SELECT user_id
         *                        FROM
         *                               users
         *                        WHERE
         *                               user_login LIKE 'arg1%' OR
         *                               user_id IN (--- user id list---) OR
         *                               user_login LIKE 'arg2%' OR
         *                               .
         *                               .
         *                               .
         *                               user_login LIKE 'argN%'
         *                       )
         * This method reduces the scan-count on users and request_users tables
         * to a large extent.
         */
        if (likeList.toString().trim().equals("") == false) {
            if (argFirst == false) {
                filter.append(" OR ");
            }

            filter.append("\n\t(\n\t\t").append(ruName).append(".user_id IN \n\t\t\t(\n\t\t\t").append("SELECT \n\t\t\t\tuser_id \n\t\t\tFROM ").append("\n\t\t\t\tusers \n\t\t\tWHERE ").append(
                likeList.toString()).append("\n\t\t\t)\n\t\t)");
            argFirst = false;
        }

        /*
         * If the filter is not empty, enclose it in parenthesis for better
         * organisation.
         */
        if (filter.toString().trim().equals("") == false) {
            filter.insert(0, "\n\t\t(").append("\n\t\t)");
        }

        return;
    }

    /**
     * This method returns the join part and the condition part of the query
     * for a numeric field.
     *
     * @param field   Field Object.
     * @param entry   Parse Entry corresponding to this Field Object.
     * @param first   Flag to specify if this entry is the first one in a block
     * @param join    Output parameter that holds the join part of query.
     * @param filter  Output parameter that holds the filtering part of query.
     *
     */
    public static void getTextQuery(Field field, ParseEntry entry, boolean first, StringBuilder join, StringBuilder filter, boolean flagJoin) {
        if (entry == null) {
            return;
        }

        int    fieldId    = field.getFieldId();
        String tableAlias = "r";
        String fieldName  = field.getName();

        if (field.getIsExtended() == true) {
            tableAlias = "re" + fieldId;

            if (field.getDataTypeId() == STRING) {
                fieldName = "varchar_value";
            } else {
                fieldName = "text_value";
            }

            if (flagJoin == true) {
                join.append(getJoinQuery(fieldId, tableAlias));
            }
        }

        Connective conn = entry.getConnective();

        filter.append("\n\t\t").append(getConnective(first, conn));

        ArrayList<String> argList = entry.getArgList();
        String            fname   = new StringBuilder().append(tableAlias).append(".").append(fieldName).toString();

        if ((argList == null) || (argList.size() == 0)) {
            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                filter.append("(").append(fname).append(" IS NOT NULL").append(" AND LTRIM(RTRIM(CONVERT(VARCHAR(7999),").append(fname).append("))) <> '' ) ");
            } else {
                filter.append("(").append(fname).append(" IS NULL").append(" OR LTRIM(RTRIM(CONVERT(VARCHAR(7999),").append(fname).append("))) = '' ) ");
            }
        } else {
            boolean firstArg = true;

            for (String arg : argList) {
                arg = arg.trim();
                arg = arg.replaceAll("'", "''");
                arg = arg.replaceAll("/", "//");
                arg = arg.replaceAll("%", "/%");
                arg = arg.replaceAll("\\[", "/[");
                arg = arg.replaceAll("\\]", "/]");

                if (firstArg == false) {
                    if (arg.startsWith("+") == true) {
                        arg = arg.substring(1).trim();
                        filter.append(" AND \n\t\t").append(textFilter(arg, conn, fname));
                    } else if (arg.startsWith("-") == true) {
                        arg = arg.substring(1).trim();
                        filter.append(" AND \n\t\t").append(textFilter(arg, conn, fname));
                    } else {
                        filter.append(" OR \n\t\t").append(textFilter(arg, conn, fname));
                    }
                } else {
                    firstArg = false;
                    filter.append("\n\t\t").append(textFilter(arg, conn, fname));
                }
            }
        }

        return;
    }

    /**
     * This method returns the join part and the condition part of the query
     * for a type field.
     *
     * @param field   Field Object.
     * @param entry   Parse Entry corresponding to this Field Object.
     * @param first   Flag to specify if this entry is the first one in a block
     * @param join    Output parameter that holds the join part of query.
     * @param filter  Output parameter that holds the filtering part of query.
     *
     */
    public static void getTypeQuery(Field field, ParseEntry entry, boolean first, StringBuilder join, StringBuilder filter, boolean flagJoin, boolean useIds) {
        if (entry == null) {
            return;
        }

        int    fieldId    = field.getFieldId();
        String tableAlias = "r";
        String fieldName  = field.getName();
        String tName      = "t" + (fieldId + 1);

        if (field.getIsExtended() == true) {
            tableAlias = "re" + fieldId;
            fieldName  = "type_value";

            if (flagJoin == true) {
                join.append(getJoinQuery(fieldId, tableAlias));
            }
        } else {
            if (fieldName.equals(Field.CATEGORY)) {
                tName = "cat";
            } else if (fieldName.equals(Field.STATUS)) {
                tName = "stat";
            } else if (fieldName.equals(Field.SEVERITY)) {
                tName = "sev";
            } else if (fieldName.equals(Field.REQUEST_TYPE)) {
                tName = "reqType";
            }
        }

        Connective conn = entry.getConnective();

        filter.append("\n\t\t").append(getConnective(first, conn));

        if (useIds == true) {
            String qlist = toTypeIdList(field, entry.getArgList());

            if ((qlist == null) || (qlist.trim().equals("") == true)) {
                qlist = "(-1)";
            } else {
                qlist = "(" + qlist + ")";
            }

            filter.append(tableAlias).append(".").append(fieldName);

            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                filter.append(" NOT IN ");
            } else {
                filter.append(" IN ");
            }

            filter.append(qlist);
        } else {
            String qlist  = toQuotedList(entry.getArgList());
            String constr = "";

            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                constr = "NOT IN";
            } else {
                constr = "IN";
            }

            filter.append("(\n\t\t\t").append(tName).append(".name ").append(constr).append(" (").append(qlist).append(") OR \n\t\t\t").append(tName).append(".display_name ").append(constr).append(
                " (").append(qlist).append(")\n\t\t)");
        }
    }

    /**
     * This method returns the condition part of the query for a user field.
     *
     * @param field   Field Object.
     * @param entry   Parse Entry corresponding to this Field Object.
     * @param first   Flag to specify if this entry is the first one in a block
     * @param filter  Output parameter that holds the filtering part of query.
     *
     */
    public static void getUserQuery(Field field, ParseEntry entry, boolean first, StringBuilder filter) {
        if (entry == null) {
            return;
        }

        String            fieldName = entry.getFieldName();
        Connective        conn      = entry.getConnective();
        ArrayList<String> argList   = entry.getArgList();
        String            clause    = entry.getClause();
        boolean           member    = false;
        boolean           expand    = false;

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

        StringBuffer idList = new StringBuffer();
        boolean      flag   = true;

        for (String user : argList) {
            if (user.startsWith("+") || user.startsWith("-") || user.startsWith("*")) {
                user = user.substring(1);
            }

            if (flag == false) {
                idList.append(",");
            } else {
                flag = false;
            }

            StringBuffer resolvedList = new StringBuffer();

            resolveAbsolute(user, member, expand, resolvedList);
            idList.append(resolvedList);
        }

        filter.append("\n\t\t");

        if (first == false) {
            switch (conn) {
            case C_AND :
            case C_AND_NOT :
                filter.append("AND ");

                break;

            case C_OR :
            case C_OR_NOT :
                filter.append("OR  ");

                break;

            case C_NOT :
            case C_NOT_NOT :
                filter.append("AND ");

                break;
            }
        }

        filter.append("r.user_id ");

        if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
            filter.append("NOT ");
        }

        filter.append("IN (").append(idList.toString()).append(")");

        return;
    }

    /**
     * This method returns the user-type-id given the userfield name.
     *
     * @param fieldName Name of the userField.
     *
     * @return User Type Id.
     */
    private static int getUserTypeId(String fieldName) {
        if (fieldName.equals(Field.LOGGER)) {
            return UserType.LOGGER;
        }

        if (fieldName.equals(Field.ASSIGNEE)) {
            return UserType.ASSIGNEE;
        }

        if (fieldName.equals(Field.SUBSCRIBER)) {
            return UserType.SUBSCRIBER;
        }

        if (fieldName.equals(Field.TO)) {
            return UserType.TO;
        }

        if (fieldName.equals(Field.CC)) {
            return UserType.CC;
        }

        return -1;
    }
}
