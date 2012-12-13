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
 * Users.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.MailListUser;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.exception.DSQLParseException;
import transbit.tbits.search.DSQLParser;
import transbit.tbits.search.ParseEntry;
import transbit.tbits.search.SearchConstants;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;
import static transbit.tbits.domain.DataType.USERTYPE;
import static transbit.tbits.domain.DataType.STRING;
import static transbit.tbits.domain.DataType.TYPE;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//~--- classes ----------------------------------------------------------------

/**
 * This API services requests related to users.
 *
 * @author  Vaibhav
 * @version $Id: $
 */
public class Users extends HttpServlet implements SearchConstants {
    public static final TBitsLogger           LOG         = TBitsLogger.getLogger(PKG_WEBAPPS);
    private static final int                  XML_FORMAT  = 1;
    private static final int                  TEXT_FORMAT = 2;
    private static final int                  JSON_FORMAT = 3;
    private static Hashtable<String, Integer> ourDescTable;
    private static Hashtable<String, String>  ourFDMap;

    //~--- static initializers ------------------------------------------------

    static {
        ourDescTable = new Hashtable<String, Integer>();
        ourDescTable.put("user_login", new Integer(STRING));
        ourDescTable.put("first_name", new Integer(STRING));
        ourDescTable.put("last_name", new Integer(STRING));
        ourDescTable.put("display_name", new Integer(STRING));
        ourDescTable.put("email", new Integer(STRING));
        ourDescTable.put("user_type_id", new Integer(TYPE));
        ourDescTable.put("member", new Integer(USERTYPE));
        ourDescTable.put("membersof", new Integer(USERTYPE));
        ourFDMap = new Hashtable<String, String>();
        ourFDMap.put("login", "user_login");
        ourFDMap.put("user_login", "user_login");
        ourFDMap.put("fname", "first_name");
        ourFDMap.put("fn", "first_name");
        ourFDMap.put("first_name", "first_name");
        ourFDMap.put("lname", "last_name");
        ourFDMap.put("ln", "last_name");
        ourFDMap.put("last_name", "last_name");
        ourFDMap.put("dname", "display_name");
        ourFDMap.put("dn", "display_name");
        ourFDMap.put("display_name", "display_name");
        ourFDMap.put("mail", "email");
        ourFDMap.put("email", "email");
        ourFDMap.put("utype", "user_type_id");
        ourFDMap.put("user_type_id", "user_type_id");
        ourFDMap.put("member", "member");
        ourFDMap.put("membersof", "membersof");
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method that handles the Http Get Requests.
     *
     * @param aRequest  Http Request object.
     * @param aResponse Http Response object.
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        PrintWriter out         = aResponse.getWriter();
        String      aRemoteUser = aRequest.getRemoteUser();
        User        remoteUser  = null;

        if (aRemoteUser != null) {
            try {
                remoteUser = WebUtil.validateUser(aRequest);
            } catch (Exception de) {
                LOG.info("",(de));
            }
        }

        /*
         * Print the help either if "help" request parameter is found in the
         * query string or the query string itself is empty.
         */
        if ((aRequest.getQueryString() == null) || aRequest.getQueryString().trim().equals("")) {
            if (remoteUser != null) {
                ArrayList<User> userList = new ArrayList<User>();

                userList.add(remoteUser);
                aResponse.setContentType("text/xml");

                StringBuffer output = new StringBuffer();

                renderInXML(userList, output);
                out.println(output.toString());

                return;
            }

            aResponse.setContentType("text/plain");
            out.println(getHelp());

            return;
        } else if (aRequest.getParameter("help") != null) {
            aResponse.setContentType("text/plain");
            out.println(getHelp());

            return;
        }

        // Check if format is specified as a request parameter.
        String sFormat = aRequest.getParameter("format");

        // Check if query is specified as a request parameter.
        String query = getQuery(aRequest);

        LOG.info("Query: " + query);
        LOG.info("Format:" + sFormat);

        int format = getOutputFormat(sFormat);

        if (format == XML_FORMAT) {
            aResponse.setContentType("text/xml");
        } else {
            aResponse.setContentType("text/plain");
        }

        out.println(process(sFormat, query));

        return;
    }

    /**
     * Method that handles the Http Post Requests.
     *
     * @param aRequest  Http Request object.
     * @param aResponse Http Response object.
     */
    public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        doGet(aRequest, aResponse);
    }

    /**
     * This method executes the query in the database and returns a list of
     * user objects.
     *
     * @param query Database query to be executed on users table.
     * @return List of users objects that matched the criteria.
     */
    private ArrayList<User> executeQuery(String query) {
        ArrayList<User> userList = new ArrayList<User>();
        Connection      con      = null;

        try {
            con = DataSourcePool.getConnection();

            Statement stmt = con.createStatement();
            ResultSet rs   = stmt.executeQuery(query);

            if (rs != null) {
                while (rs.next() != false) {
                    User user = new User();

                    user.setUserId(rs.getInt("user_id"));
                    user.setUserLogin(rs.getString("user_login"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setDisplayName(rs.getString("display_name"));
                    user.setEmail(rs.getString("email"));
                    user.setIsActive(rs.getBoolean("is_active"));
                    user.setUserTypeId(rs.getInt("user_type_id"));
                    user.setLocation(rs.getString("location"));
                    user.setExtension(rs.getString("extension"));
                    user.setMobile(rs.getString("mobile"));
                    user.setHomePhone(rs.getString("home_phone"));
                    userList.add(user);
                }

                rs.close();
            }

            stmt.close();
            rs   = null;
            stmt = null;
        } catch (SQLException de) {
            LOG.severe("",(de));
            LOG.info("Query:\n" + query);
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {}
        }

        return userList;
    }

    /**
     * Main method for commandline invocation of this.
     *
     * @param args Commandline argument list.
     */
    public static int main(String[] args) {
        if ((args.length != 2) && (args.length != 4)) {
            LOG.info(getHelp());
            return 1;
        }

        String sFormat = "json";
        String query   = "";

        if (args.length >= 2) {
            query = args[1];
        }

        if (args.length == 4) {
            sFormat = args[3];
        }

        Users obj = new Users();

        LOG.info("\n" + obj.process(sFormat, query));
        return 0;
    }

    /**
     * This method parses the DSQL query and returns the parser object.
     *
     * @param aQuery DQL query.
     * @return Parser object
     */
    private DSQLParser parseQuery(String aQuery) {
        DSQLParser parser = new DSQLParser(aQuery, ourDescTable, ourFDMap);

        try {
            parser.parse();
        } catch (DSQLParseException dpe) {
            LOG.info("",(dpe));
        }

        return parser;
    }

    /**
     * This method services the request in following steps:
     *     - Parses the DQL query
     *     - Generates a DB Query
     *     - Executes the Query in the database
     *     - Retrieve the results
     *     - Render the results in required format
     *
     * @param aFormat Format of output.
     * @param aQuery  DQL Query.
     * @return Output String in required format.
     */
    public String process(String aFormat, String aQuery) {

        // Get the Format.
        int format = getOutputFormat(aFormat);

        // Parse the query.
        DSQLParser parser = parseQuery(aQuery);

        // Generate the database query.
        String dbQuery = getDBQuery(parser);

        LOG.info("Query: " + dbQuery);

        // Execute the query.
        ArrayList<User> userList = executeQuery(dbQuery);

        LOG.info("Result Count: " + userList.size());

        // Render the results in requested format.
        StringBuffer output = new StringBuffer();

        switch (format) {
        case XML_FORMAT :
            renderInXML(userList, output);

            break;

        case TEXT_FORMAT :
            renderInText(userList, output);

            break;

        case JSON_FORMAT :
            renderInJSON(userList, output);

            break;
        }

        // Done. Return the results.
        return output.toString();
    }

    /**
     * JSON Renderer.
     *
     * @param aUserList         List of users.
     * @param aOutput           Out param to hold the output string.
     */
    private void renderInJSON(ArrayList<User> aUserList, StringBuffer aOutput) {
        aOutput.append("[");

        boolean first = true;

        for (User user : aUserList) {
            if (first == false) {
                aOutput.append(",\n");
            } else {
                first = false;
            }

            aOutput.append(getUserInfoRecord(user));
        }

        aOutput.append("]");
    }

    /**
     * Text Renderer.
     *
     * @param aUserList         List of users.
     * @param aOutput           Out param to hold the output string.
     */
    private void renderInText(ArrayList<User> aUserList, StringBuffer aOutput) {
        aOutput.append("\nUserLogin;FirstName;LastName;DisplayName;Email;").append("UserType;Location;Extension;Mobile;HomePhone;\n");

        for (User user : aUserList) {
            aOutput.append("\n").append(user.getUserLogin()).append(";").append(user.getFirstName()).append(";").append(user.getLastName()).append(";").append(user.getDisplayName()).append(
                ";").append(user.getEmail()).append(";").append(getUserType(user.getUserTypeId())).append(";").append(user.getLocation()).append(";").append(user.getExtension()).append(";").append(
                user.getMobile()).append(";").append(user.getHomePhone()).append(";");
        }

        aOutput.append("\n");

        return;
    }

    /**
     * XML Renderer.
     *
     * @param aUserList         List of users.
     * @param aOutput           Out param to hold the output string.
     */
    private void renderInXML(ArrayList<User> aUserList, StringBuffer aOutput) {
        aOutput.append("<UserList>").append("\n\t<Header>").append("\n\t\t<Field name='Login' type='String' />").append("\n\t\t<Field name='FirstName' type='String' />").append(
            "\n\t\t<Field name='LastName' type='String' />").append("\n\t\t<Field name='DisplayName' type='String' />").append("\n\t\t<Field name='Email' type='String' />").append(
            "\n\t\t<Field name='UserType' type='String' />").append("\n\t\t<Field name='Location' type='String' />").append("\n\t\t<Field name='Extension' type='String' />").append(
            "\n\t\t<Field name='Mobile' type='String' />").append("\n\t\t<Field name='HomePhone' type='String' />").append("\n\t</Header>").append("\n\t<Data count=\"").append(
            aUserList.size()).append("\">");

        for (User user : aUserList) {
            aOutput.append("\n\t\t<User>").append("\n\t\t\t<Login>").append(user.getUserLogin()).append("</Login>").append("\n\t\t\t<FirstName>").append(user.getFirstName()).append(
                "</FirstName>").append("\n\t\t\t<LastName>").append(user.getLastName()).append("</LastName>").append("\n\t\t\t<DisplayName>").append(user.getDisplayName()).append(
                "</DisplayName>").append("\n\t\t\t<Email>").append(user.getEmail()).append("</Email>").append("\n\t\t\t<UserType>").append(getUserType(user.getUserTypeId())).append(
                "</UserType>").append("\n\t\t\t<Location>").append(user.getLocation()).append("</Location>").append("\n\t\t\t<Extension>").append(user.getExtension()).append("</Extension>").append(
                "\n\t\t\t<Mobile>").append(user.getMobile()).append("</Mobile>").append("\n\t\t\t<HomePhone>").append(user.getHomePhone()).append("</HomePhone>").append("\n\t\t</User>");
        }

        aOutput.append("\n\t</Data>").append("\n</UserList>");

        return;
    }

    /**
     * Returns the text filter.
     *
     * @param arg               argument
     * @param connective        connective
     * @param fieldName         name of the field.
     * @return  Text Filter.
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
            arg   = "%" + arg.substring(1);
        }

        // Check if the argument ends with a *
        if (arg.endsWith("*") == true) {
            end = true;
            arg = arg.substring(0, arg.length() - 1) + "%";
        }

        filter.append("(").append(fieldName).append(optr).append(" LIKE '").append(arg).append("' )");

        return filter.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * This method generates the DB query for the parse table in the parser.
     *
     * @param aParser  Parser object.
     * @return DB Query.
     */
    private String getDBQuery(DSQLParser aParser) {
        if (aParser == null) {
            return "";
        }

        ArrayList<ParseEntry> parseTable = aParser.getParseTable();

        if (parseTable == null) {
            return "";
        }

        LOG.info(parseTable);

        StringBuffer dbQuery  = new StringBuffer();
        StringBuffer joinPart = new StringBuffer();
        StringBuffer condPart = new StringBuffer();

        joinPart.append("users u");

        boolean first = false;

        for (ParseEntry entry : parseTable) {
            String fieldName = entry.getFieldName();

            if (fieldName.equals("user_login") || fieldName.equals("first_name") || fieldName.equals("last_name") || fieldName.equals("display_name") || fieldName.equals("email")) {
                getTextQuery(entry, first, "u", condPart);
            } else if (fieldName.equals("user_type_id")) {
                getTypeQuery(entry, first, condPart);
            } else if (fieldName.equals("member") || fieldName.equals("membersof")) {
                getMVQuery(entry, first, joinPart, condPart);
            }

            first = false;
        }

        String primSortField = "user_login ASC";
        String secSortField  = "";
        String sortField     = aParser.getSortField();

        sortField = (sortField == null)
                    ? "user_login"
                    : sortField.trim();

        int    sortOrder    = aParser.getSortOrder();
        String strSortOrder = (sortOrder == ASC_ORDER)
                              ? " ASC"
                              : " DESC";

        sortField = ourFDMap.get(sortField);

        if ((sortField == null) || sortField.trim().equals("") || sortField.trim().equals("user_login") || sortField.trim().equals("member") || sortField.trim().equals("membersof")) {
            primSortField = "u.user_login" + strSortOrder;
            secSortField  = "";
        } else {
            primSortField = "u." + sortField + strSortOrder;
            secSortField  = ", u.user_login ASC";
        }

        dbQuery.append("\nSELECT DISTINCT ").append("\n\tu.user_id,").append("\n\tu.user_login,").append("\n\tu.first_name,").append("\n\tu.last_name,").append("\n\tu.display_name,").append(
            "\n\tu.email,").append("\n\tu.is_active,").append("\n\tu.user_type_id,").append("\n\tu.location,").append("\n\tu.extension,").append("\n\tu.mobile,").append("\n\tu.home_phone").append(
            "\nFROM ").append(joinPart).append("\nWHERE").append("\n\tu.is_active = 1 ").append(condPart).append("\nORDER BY ").append(primSortField).append(secSortField).append("");

        return dbQuery.toString();
    }

    public static String getHelp() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("\nUsage:\n").append("\n\thttp://TBits/users?format=<Format>&q=<Query>").append("\nWhere").append("\n\tFormat can be one of xml/json/text. ").append(
            "json is the default format.").append("\n").append("\n\tQuery is a DQL query using the following descriptors:").append("\n\t====================================================").append(
            "\n\t|login            | Querying on user's login.      |").append("\n\t|                 | e.g.                           |").append(
            "\n\t|                 |      login:Vaibhav            |").append("\n\t|                 |      login:k*                  |").append(
            "\n\t|--------------------------------------------------|").append("\n\t|fname (or)       | Querying on user's first name  |").append(
            "\n\t|fn               | e.g.                           |").append("\n\t|                 |      fname:a*                  |").append(
            "\n\t|                 |      fn:Dan*                   |").append("\n\t|--------------------------------------------------|").append(
            "\n\t|lname (or)       | Querying on user's last name   |").append("\n\t|ln               | e.g.                           |").append(
            "\n\t|                 |      lname:a*                  |").append("\n\t|                 |      ln:set*                   |").append(
            "\n\t|--------------------------------------------------|").append("\n\t|dname (or)       | Querying on user's Display Name|").append(
            "\n\t|dn               | e.g.                           |").append("\n\t|                 |      dname:ka*                 |").append(
            "\n\t|                 |      dn:Dan*                   |").append("\n\t|--------------------------------------------------|").append(
            "\n\t|email (or)       | Querying on user's email       |").append("\n\t|mail             | e.g.                           |").append(
            "\n\t|                 |      email:tech*               |").append("\n\t|--------------------------------------------------|").append(
            "\n\t|utype            | Querying on user's entry type. |").append("\n\t|                 | arguments to this descriptor   |").append(
            "\n\t|                 | can be user/mailinglist/contact|").append("\n\t|                 | e.g.                           |").append(
            "\n\t|                 |      utype:contact             |").append("\n\t|--------------------------------------------------|").append(
            "\n\t|member           | To get the mailing list where  |").append("\n\t|                 | the specified is user is a     |").append(
            "\n\t|                 | member.                        |").append("\n\t|                 | e.g.                           |").append(
            "\n\t|                 |     member:Vaibhav            |").append("\n\t|--------------------------------------------------|").append(
            "\n\t|membersof        | To get the member of given     |").append("\n\t|                 | mailing list. This will return |").append(
            "\n\t|                 | the actual members of the list |").append("\n\t|                 | without expanding mailing lists|").append(
            "\n\t|                 | if any, present in it.         |").append("\n\t|                 | e.g.                           |").append(
            "\n\t|                 |    membersof:techincal@world   |").append("\n\t|--------------------------------------------------|").append(
            "\n\t|membersof:expand | To get the member of given     |").append("\n\t|                 | mailing list. This will expand |").append(
            "\n\t|                 | the mailing lists if any,      |").append("\n\t|                 | present in it recursively.     |").append(
            "\n\t|                 | e.g.                           |").append("\n\t|                 |     membersof:expand:techincal |").append(
            "\n\t====================================================").append("\n");

        return buffer.toString();
    }

    /**
     * This method forms the condition and join parts of query related to this
     * multi valued field.
     *
     * @param entry  ParseEntry
     * @param first  True if this is the first entry.
     * @param join   Out param to hold the join conditions.
     * @param filter Out param to hold the where conditions.
     */
    private void getMVQuery(ParseEntry entry, boolean first, StringBuffer join, StringBuffer filter) {
        String            fieldName = entry.getFieldName();
        ArrayList<String> argList   = entry.getArgList();

        if ((argList == null) || (argList.size() == 0)) {
            return;
        }

        join.append("\n\tJOIN mail_list_users mlu");

        if (fieldName.equals("membersof")) {
            join.append("\n\tON u.user_id = mlu.user_id").append("\n\tJOIN users mu").append("\n\tON mlu.mail_list_id = mu.user_id");
        } else    // fieldName.equals("member")
        {
            join.append("\n\tON u.user_id = mlu.mail_list_id").append("\n\tJOIN users mu").append("\n\tON mlu.user_id = mu.user_id");
        }

        filter.append(" ");

        Connective conn = entry.getConnective();

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

        String  clause = entry.getClause();
        boolean expand = false;

        if ((clause != null) && (clause.trim().equals("expand") == true)) {
            expand = true;
        }

        String fname = (expand == true)
                       ? "u.user_id"
                       : "mu.user_id";

        filter.append("\n\t").append(fname);

        if ((conn == Connective.C_NOT) || (conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT)) {
            filter.append(" NOT ");
        }

        filter.append(" IN (");

        boolean start = true;

        for (String arg : argList) {
            if (start == false) {
                filter.append(", ");
            } else {
                start = false;
            }

            if (arg.startsWith("+") || arg.startsWith("-")) {
                arg = arg.substring(1);
            }

            String userId = getUserId(arg, expand);

            filter.append(userId);
        }

        filter.append(") ");
    }

    /**
     * This method returns the format constant depending on the value passed.
     *
     * @param strFormat Format as  string.
     * @return Format constant.
     */
    private int getOutputFormat(String strFormat) {
        int format = JSON_FORMAT;

        if ((strFormat == null) || (strFormat.trim().equals("") == true)) {

            // Default is JSON.
            format = JSON_FORMAT;
        } else {
            strFormat = strFormat.trim();

            if (strFormat.equalsIgnoreCase("xml")) {
                format = XML_FORMAT;
            } else if (strFormat.equalsIgnoreCase("text")) {
                format = TEXT_FORMAT;
            } else {
                format = JSON_FORMAT;
            }
        }

        return format;
    }

    /**
     * This method checks if query is passed as request parameter.
     *
     * @param aRequest HttpServletRequest object.
     * @return Value of query request parameter.
     */
    private String getQuery(HttpServletRequest aRequest) {
        String query = "";

        query = aRequest.getParameter("query");

        if ((query == null) || query.trim().equals("")) {
            query = aRequest.getParameter("q");

            if ((query == null) || query.trim().equals("")) {
                query = "";
            }
        }

        query = query.trim();

        return query;
    }

    /**
     * This method forms the condition and join parts of query related to this
     * text field.
     *
     * @param entry  ParseEntry
     * @param first  True if this is the first entry.
     * @param prefix Prefix of the field name.
     * @param filter Out param to hold the where conditions.
     */
    private void getTextQuery(ParseEntry entry, boolean first, String prefix, StringBuffer filter) {
        filter.append(" ");

        Connective conn = entry.getConnective();

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

        String            fname   = prefix + "." + entry.getFieldName();
        ArrayList<String> argList = entry.getArgList();

        if ((argList == null) || (argList.size() == 0)) {
            if ((conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT) || (conn == Connective.C_NOT)) {
                filter.append("(").append(fname).append(" IS NOT NULL").append(" AND LTRIM(RTRIM(CONVERT(VARCHAR(7999),").append(fname).append("))) <> '' ) ");
            } else {
                filter.append("(").append(fname).append(" IS NULL").append(" OR LTRIM(RTRIM(CONVERT(VARCHAR(7999),").append(fname).append("))) = '' ) ");
            }
        } else {
            boolean firstArg = true;

            for (String arg : argList) {
                arg = arg.replaceAll("'", "''");
                arg = arg.replaceAll("/", "//");
                arg = arg.replaceAll("%", "/%");
                arg = arg.replaceAll("\\[", "/[");
                arg = arg.replaceAll("\\]", "/]");

                if (firstArg == false) {
                    if (arg.startsWith("+") == true) {
                        arg = arg.substring(1);
                        filter.append(" AND ").append(textFilter(arg, conn, fname));
                    } else if (arg.startsWith("-") == true) {
                        arg = arg.substring(1);
                        filter.append(" AND ").append(textFilter(arg, conn, fname));
                    } else {
                        filter.append(" OR ").append(textFilter(arg, conn, fname));
                    }
                } else {
                    firstArg = false;
                    filter.append("\n\t").append(textFilter(arg, conn, fname));
                }
            }
        }

        return;
    }

    /**
     * This method forms the condition and join parts of query related to this
     * type field.
     *
     * @param entry  ParseEntry
     * @param first  True if this is the first entry.
     * @param filter Out param to hold the where conditions.
     */
    private void getTypeQuery(ParseEntry entry, boolean first, StringBuffer filter) {
        ArrayList<String> argList = entry.getArgList();

        if ((argList == null) || (argList.size() == 0)) {
            return;
        }

        filter.append(" ");

        Connective conn = entry.getConnective();

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

        String fname = "u.user_type_id";

        filter.append("\n\t").append(fname);

        if ((conn == Connective.C_NOT) || (conn == Connective.C_AND_NOT) || (conn == Connective.C_OR_NOT)) {
            filter.append(" NOT ");
        }

        filter.append(" IN (");

        boolean start = true;

        for (String arg : argList) {
            if (start == false) {
                filter.append(", ");
            } else {
                start = false;
            }

            if (arg == null) {
                filter.append("7");
            } else {
                arg = arg.trim();

                if (arg.equals("user")) {
                    filter.append("7");
                } else if (arg.equals("mailinglist")) {
                    filter.append("8");
                } else if (arg.equals("contact")) {
                    filter.append("10");
                } else {
                    filter.append("0");
                }
            }
        }

        filter.append(") ");

        return;
    }

    /**
     * This method returns the userid corresponding to the given user login.
     *
     * @param aUserLogin userLogin.
     *
     * @return User id.
     */
    private static String getUserId(String aUserLogin, boolean expand) {
        User         user   = null;
        StringBuffer buffer = new StringBuffer();

        try {
            user = User.lookupAllByUserLogin(aUserLogin);

            if (user != null) {
                int userId = user.getUserId();

                if ((user.getUserTypeId() == UserType.INTERNAL_MAILINGLIST) && (expand == true)) {
                    ArrayList<User> list  = MailListUser.getMemberUsers(userId);
                    boolean         first = true;

                    for (User tmp : list) {
                        if (first == false) {
                            buffer.append(",");
                        } else {
                            first = false;
                        }

                        buffer.append(tmp.getUserId());
                    }
                } else {
                    buffer.append(userId);
                }
            } else if (expand == true) {
                ArrayList<User> list = MailListUser.getMemberUsersByEmailLike(aUserLogin);

                if ((list != null) && (list.size() != 0)) {
                    boolean first = true;

                    for (User tmp : list) {
                        if (first == false) {
                            buffer.append(",");
                        } else {
                            first = false;
                        }

                        buffer.append(tmp.getUserId());
                    }
                } else {
                    buffer.append("-1");
                }
            } else {
                buffer.append("-1");
            }
        } catch (DatabaseException de) {
            LOG.warning("",(de));
            buffer.append("-1");
        }

        return buffer.toString();
    }

    /**
     * This method returns the userinfo needed for autocomplete feature.
     *
     * @param value User object.
     * @return UserInfo record.
     */
    private String getUserInfoRecord(User value) {
        StringBuffer buffer      = new StringBuffer();
        String       userLogin   = value.getUserLogin();
        String       displayName = value.getDisplayName();

        displayName = displayName.replaceAll(".transbittech.com", "");
        userLogin   = userLogin.replaceAll(".transbittech.com", "");
        buffer.append("\"");

        if (displayName.equals(userLogin)) {
            buffer.append(userLogin);
        } else {
            buffer.append(displayName).append(" <").append(userLogin).append(">");
        }

        buffer.append("\"");

        return buffer.toString();
    }

    /**
     * Returns the UserType based on the id passed.
     *
     * @param userTypeId  Id .
     *
     * @return User Type as a string.
     */
    private String getUserType(int userTypeId) {
        switch (userTypeId) {
        case UserType.INTERNAL_USER :
            return "User";

        case UserType.INTERNAL_MAILINGLIST :
            return "Mailing List";

        case UserType.INTERNAL_CONTACT :
            return "Contact";

        case UserType.INTERNAL_HIDDEN_LIST :
            return "Unix Mailing List";

        default :
            return "User";
        }
    }
}
