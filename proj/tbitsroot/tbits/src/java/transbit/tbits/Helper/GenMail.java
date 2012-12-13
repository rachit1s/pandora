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
 * GenMail.java
 *
 * $Header:
 */
package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;

//TBits Imports
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.UserType;
import transbit.tbits.mail.TBitsMailer;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Hashtable;

//~--- classes ----------------------------------------------------------------

/**
 * This class is used to Generate mail for an action of a request in
 * a given business area.
 *
 * @author  : Vinod Gupta
 * @version : $Id: $
 *
 */
@Deprecated
public class GenMail {

    /*
     *
     */
	@Deprecated
    public static void generateMail(int aSystemId, int aRequestId, int aActionId) {
        try {
            Request request = getRequestBySystemIdAndRequestIdAndActionId(aSystemId, aRequestId, aActionId);
            TBitsMailer tm = new TBitsMailer(request) ;
            tm.sendMail();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return;
    }

    /*
     *
     */
    @Deprecated
    public static void main(String arg[]) {
        if (arg.length < 3) {
            System.out.println("Usage: \n\tGenMail <System ID> <Request ID> <Action ID>");
            return;
            //System.exit(1);
        }

        try {
            int systemId  = Integer.parseInt(arg[0]);
            int requestId = Integer.parseInt(arg[1]);
            int actionId  = Integer.parseInt(arg[2]);

            GenMail.generateMail(systemId, requestId, actionId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //~--- get methods --------------------------------------------------------

    /**
     *
     */
    @Deprecated // Nitiraj : har kahi look-up function bana ke rakhe hai ??? hey kya backchodi hai !
    public static Request getRequestBySystemIdAndRequestIdAndActionId(int aSystemId, int aRequestId, int aActionId) throws DatabaseException {
//        BusinessArea              ba              = BusinessArea.lookupBySystemId(aSystemId);
//        String                    prefix          = ba.getSystemPrefix() + "#";
//        Request                   request         = null;
//        ArrayList<RequestUser>    logList         = new ArrayList<RequestUser>();
//        ArrayList<RequestUser>    assList         = new ArrayList<RequestUser>();
//        ArrayList<RequestUser>    subList         = new ArrayList<RequestUser>();
//        ArrayList<RequestUser>    toList          = new ArrayList<RequestUser>();
//        ArrayList<RequestUser>    ccList          = new ArrayList<RequestUser>();
//        Hashtable<String, String> subRequests     = new Hashtable<String, String>();
//        Hashtable<String, String> siblingRequests = new Hashtable<String, String>();
//        Hashtable<String, String> relatedRequests = new Hashtable<String, String>();
//        Hashtable<String, String> parentRequests  = new Hashtable<String, String>();
//        Connection                connection      = null;
//
//        try {
//            connection = DataSourcePool.getConnection();
//
//            CallableStatement cs = connection.prepareCall("stp_req_action_lookupBySystemIdAndRequestIdAndActionId " + "?, ?, ?");
//
//            cs.setInt(1, aSystemId);
//            cs.setInt(2, aRequestId);
//            cs.setInt(3, aActionId);
//
//            boolean flag = cs.execute();
//
//            //
//            // This flag should be true, because, the underlying stored
//            // procedure returns three result sets which contains
//            // - Request Information.
//            // - Request user Information.
//            // - SubRequests Information.
//            // - SiblingRequests Information.
//            // - RelatedRequests Information.
//            //
//            if (flag == true) {
//                ResultSet rsRequest = cs.getResultSet();
//
//                if ((rsRequest != null) && (rsRequest.next() != false)) {
//                    request = Request.createFromResultSet(rsRequest);
//
//                    //
//                    // Do not close rsRequest. Because, a call to
//                    // cs.getMoreResults() closes this internally. If we close
//                    // the result set, then call to cs.getMoreResults() throws
//                    // an SQLException.
//                    //
//                    cs.getMoreResults();
//
//                    ResultSet rsReqUser = cs.getResultSet();
//
//                    if (rsReqUser != null) {
//                        while (rsReqUser.next() != false) {
//                            RequestUser reqUser = RequestUser.createFromResultSet(rsReqUser);
//
//                            switch (reqUser.getUserTypeId()) {
//                            case UserType.LOGGER :
//                                logList.add(reqUser);
//
//                                break;
//
//                            case UserType.ASSIGNEE :
//                                assList.add(reqUser);
//
//                                break;
//
//                            case UserType.SUBSCRIBER :
//                                subList.add(reqUser);
//
//                                break;
//
//                            case UserType.TO :
//                                toList.add(reqUser);
//
//                                break;
//
//                            case UserType.CC :
//                                ccList.add(reqUser);
//
//                                break;
//                            }
//                        }
//
//                        request.setLoggers(logList);
//                        request.setAssignees(assList);
//                        request.setSubscribers(subList);
//                        request.setTos(toList);
//                        request.setCcs(ccList);
//                    }
//
//                    //
//                    // Again, no need to close the previous result set.
//                    // Get SubRequests
//                    //
//                    cs.getMoreResults();
//
//                    ResultSet subReq = cs.getResultSet();
//
//                    if (subReq != null) {
//                        while (subReq.next() != false) {
//                            subRequests.put(prefix + subReq.getInt("request_id"), subReq.getString("subject"));
//                        }
//                    }
//
//                    request.setSubRequests(subRequests);
//
//                    //
//                    // Again, no need to close the previous result set.
//                    // Get SiblingRequests
//                    //
//                    cs.getMoreResults();
//
//                    ResultSet sibReq = cs.getResultSet();
//
//                    if (sibReq != null) {
//                        while (sibReq.next() != false) {
//                            siblingRequests.put(prefix + sibReq.getInt("request_id"), sibReq.getString("subject"));
//                        }
//                    }
//
//                    request.setSiblingRequests(siblingRequests);
//
//                    //
//                    // Again, no need to close the previous result set.
//                    // Get RelatedRequests
//                    //
//                    // Skip all the update counts.
//                    while ((cs.getMoreResults() == false) && (cs.getUpdateCount() != -1));
//
//                    ResultSet relReq = cs.getResultSet();
//
//                    if (relReq != null) {
//                        while (relReq.next() != false) {
//                            relatedRequests.put(relReq.getString("request_id"), relReq.getString("subject"));
//                        }
//                    }
//
//                    request.setRelatedRequests(relatedRequests);
//
//                    //
//                    // Again, no need to close the previous result set.
//                    // Get Parent Requests
//                    //
//                    while ((cs.getMoreResults() == false) && (cs.getUpdateCount() != -1));
//
//                    ResultSet parReq = cs.getResultSet();
//
//                    if (parReq != null) {
//                        while (parReq.next() != false) {
//                            parentRequests.put(prefix + parReq.getInt("request_id"), parReq.getString("subject"));
//                        }
//                    }
//
//                    request.setParentRequests(parentRequests);
//
//                    // This will close the previous resultset.
//                    cs.getMoreResults();
//
//                    // We are no more interested on any thing that follows.
//                }
//            }
//
//            // Close the statement.
//            cs.close();
//
//            //
//            // Release the memory by nullifying the references so that these
//            // are recovered by the Garbage collector.
//            //
//            cs = null;
//        } catch (SQLException sqle) {
//            StringBuilder message = new StringBuilder();
//
//            message.append("An exception while retrieving the request.").append("\nSystem Id  : ").append(aSystemId).append("\nRequest Id : ").append(aRequestId).append("\nAction Id : ").append(
//                aActionId).append("\n");
//
//            throw new DatabaseException(message.toString(), sqle);
//        } finally {
//            if (connection != null) {
//                try {
//                    connection.close();
//                } catch (SQLException sqle) {
//                    System.out.println("Exception while closing the connection:" + sqle.toString());
//                }
//
//                connection = null;
//            }
//        }
//
//        return request;
    	return null ;
    }
}
