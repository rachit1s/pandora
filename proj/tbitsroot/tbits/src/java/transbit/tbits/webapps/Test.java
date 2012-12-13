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
 * Test.java
 *
 * $Header:
 */
package transbit.tbits.webapps;

//~--- non-JDK imports --------------------------------------------------------

//Lucene Imports.
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import transbit.tbits.common.DataSourcePool;

//Imports from current package.
//TBits imports.
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.indexer.IndexUtil;
import transbit.tbits.indexer.TBitsAnalyzer;

//Static imports.
//Package name.
import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

//~--- JDK imports ------------------------------------------------------------

//Java imports.
import java.io.IOException;
import java.io.PrintWriter;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//~--- classes ----------------------------------------------------------------

/**
 * This servlet accomplishes the search requests in TBits.
 *
 * @author Vaibhav.
 * @version $Id: $
 */
public class Test extends HttpServlet {

    // Application logger.
    public static TBitsLogger   LOG                    = TBitsLogger.getLogger(PKG_WEBAPPS);
    private static final String MULTIPART_CONTENT_TYPE = "multipart/form-data";
    public static String        ourTmpLocation;

    //~--- methods ------------------------------------------------------------

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        /*
         * Inform the servlet to read all the parameters assuming they are UTF-8
         * encoded.
         */
        request.setCharacterEncoding("UTF-8");

        /*
         * Set the content-type of output to HTML and character encoding to
         * UTF-8
         */
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        LOG.info(request.getCharacterEncoding());

        String value = request.getParameter("textBox");

        LOG.info("\n" + value);
        out.println(value);

        return;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        request.setCharacterEncoding("UTF-8");
        LOG.info(request.getCharacterEncoding());

        String       value  = request.getParameter("textBox");
        StringBuffer buffer = new StringBuffer();

        Connection con = null;
        try {
            con = DataSourcePool.getConnection();

            con.setAutoCommit(false);

            CallableStatement stmt = con.prepareCall("stp_insert_testtable ?");

            stmt.setString(1, value);
            stmt.execute();
            stmt.close();
            con.commit();
        } catch (Exception e) {
        	try {
        		if(con != null)
					con.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            e.printStackTrace();
        }
        finally
		{
			if(con != null)
			{
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

        String retValue = "";

        con = null ;
        try {
            con = DataSourcePool.getConnection();

            con.setAutoCommit(false);

            Statement stmt = con.createStatement();
            ResultSet rs   = stmt.executeQuery("SELECT value from test_table");

            if ((rs != null) && (rs.next() == true)) {
                retValue = rs.getString(1);
                rs.close();
                rs = null;
            }

            stmt.close();
            con.commit();
        } catch (Exception e) {
        	try {
        		if(con != null)
					con.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            e.printStackTrace();
        }
        finally
		{
			if(con != null)
			{
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

        byte[] arr = retValue.getBytes("UTF-8");

        for (int i = 0; i < arr.length; i++) {
            LOG.info(arr[i]);
        }

        String bValue = new String(arr, "UTF-8");

        buffer.append("<input type='text' value='").append(bValue).append("'/>");
        out.println(value + buffer.toString());

        return;
    }

    public static void indexData() {
        ArrayList<String> list = new ArrayList<String>();

        Connection con = null;
        try {
            con = DataSourcePool.getConnection();

            con.setAutoCommit(false);

            Statement stmt = con.createStatement();
            ResultSet rs   = stmt.executeQuery("SELECT value from test_table");

            if (rs != null) {
                while (rs.next() == true) {
                    list.add(rs.getString(1));
                }

                rs.close();
                rs = null;
            }

            stmt.close();
            con.commit();

            String location = "/u/Vinod/TBits/testIndex";

            IndexUtil.createIndex(location);

            for (int i = 0; i < list.size(); i++) {
                String   item = list.get(i);
                Document doc  = new Document();

                doc.add(Field.Text("column", item));
                doc.add(Field.Text("id", Integer.toString(i)));

                IndexWriter iw = IndexUtil.openWriter(location + "/RequestStore");

                iw.addDocument(doc);
                iw.close();
            }
        } catch (Exception e) {
        	try {
        		if(con != null)
					con.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            e.printStackTrace();
        } 
        finally
		{
			if(con != null)
			{
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
    }

    public static int main(String arg[]) {
        try {
            indexData();
        } 
        catch(Exception e)
        {
            LOG.error(e);
            return 1;
        }
        finally {
            
        }
        return 0;
    }

    /**
     *
     * @param query
     */
    public static void searchData(String query) {
        IndexSearcher ourSearcher = null;

        try {
            long   start    = System.currentTimeMillis();
            String location = "/u/Vinod/TBits/testIndex";

            // Open the IndexSearcher to the Index location.
            ourSearcher = new IndexSearcher(location + "/RequestStore");

            // Parse the query.
            Query objQuery = QueryParser.parse(query, "", new TBitsAnalyzer());

            // Get the documents that matched the query.
            Hits hits   = ourSearcher.search(objQuery);
            int  length = hits.length();

            //
            // Print out the request ids of the documents that matched
            // the criteria.
            //
            for (int i = 0; (i < length) && (i < 2000); i++) {
                LOG.info("Request: " + hits.doc(i).get("primary_key") + "\tCategory: " + hits.doc(i).get("category_id"));
            }

            long end = System.currentTimeMillis();

            LOG.info(length + " result(s) found in " + ((double) (end - start)) / 1000 + " secs");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ourSearcher != null) {
                try {
                    ourSearcher.close();
                } catch (IOException ioe) {
                    LOG.severe("",(ioe));
                }
            }
        }
    }
}
