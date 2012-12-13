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
 * RequestIndexer.java
 *
 * $Header:
 */
package transbit.tbits.indexer;

//~--- non-JDK imports --------------------------------------------------------

//Lucene Imports
import static transbit.tbits.Helper.TBitsConstants.PKG_INDEXER;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.api.APIUtil;
import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Uploader;
import transbit.tbits.common.readerizer.Readerizer;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;

//~--- classes ----------------------------------------------------------------

/**
 * This class is used for indexing a request or a business area.
 * 
 * @author Vaibhav, Nitiraj
 * @version $Id: $
 * 
 */
public class RequestIndexer implements Runnable, TBitsPropEnum {

	private static final String ALL_TEXT = "alltext";

	private static final String ALL = "all";

	private static final String SYS_PREFIX = "sysPrefix";

	// Name of the logger.
	public static final String LOGGER_NAME = "indexer";

	// Application Logger.
	public static final TBitsLogger LOG = TBitsLogger.getLogger(LOGGER_NAME,
			PKG_INDEXER);

	// Location where the attachments are stored.
//	private static String ourAttachmentLocation;

	// ~--- static initializers ------------------------------------------------

	public static Object lockObject = new Object() ;
	/*
	 * Static block that reads the application's property file.
	 */
//	static {
//		try {
//
//			// Get the location of attachments.
//			ourAttachmentLocation = Configuration
//					.findAbsolutePath(PropertiesHandler
//							.getProperty(KEY_ATTACHMENTDIR));
//		} catch (Exception e) {
//			e.printStackTrace() ;
//			// Any Exception during the above process is severe. Log It.
//			LOG.error("",(e));
//		}	
//	}

	// ~--- fields -------------------------------------------------------------
	// Hashtable<requestId, Hashtable<field_name,field_value>>
	private Hashtable<Integer, Hashtable<String,String> > myRequestData = new Hashtable<Integer, Hashtable<String,String> >() ;
	// Hashtable<requestId, Hashtable<field_name,field_value>>
	private Hashtable<Integer, Hashtable<Integer,Hashtable<String,String>>> myActionData = new Hashtable<Integer , Hashtable< Integer, Hashtable<String,String> > >() ;
	
	// Hashtable< requestId, Hashtable<actionId, AllAttachementsFiles>> 
	Hashtable<Integer, Hashtable<Integer, Collection<ActionFileInfo>> > myAttInfo = new Hashtable<Integer,Hashtable<Integer, Collection<ActionFileInfo> > >() ;
	// List of lucene documents.
	private ArrayList<Document> myActionDocs = new ArrayList<Document>() ;
	private ArrayList<Document> myRequestDocs = new ArrayList<Document>() ;

	// Index related parametes.
	private String myPrimaryIndexLocation;
	private String myIndexName;
	private IndexingType myIndexingType;

	// Indexing process related parameters.
	private String mySysPrefix;
	private int myRequestId;
	private int mySystemId;
	private String myTempIndexName;

	// ~--- constant enums -----------------------------------------------------

	// Mode of indexing depending the constructor invoked.
	private enum IndexingType {
		BULK, REQ
	}

	// ~--- constructors -------------------------------------------------------

	/**
	 * This constructor is suitable for BulkIndexing. This will facilitate the
	 * user to index a given business area and merge the index to the given
	 * primary location.
	 * 
	 * @param aSysPrefix
	 *            Business Area Prefix.
	 * @param aPrimaryIndexLocation
	 *            Location of Primary Index.
	 */
	public RequestIndexer(String aSysPrefix, String aPrimaryIndexLocation) {
		if ((aSysPrefix == null) || (aSysPrefix.trim().equals("") == true)
				|| (aPrimaryIndexLocation == null)
				|| (aPrimaryIndexLocation.trim().equals("") == true)) {
			throw new IllegalArgumentException(
					"Business Area Id and the Location of primary index are "
							+ "mandatory for indexing a Business area.");
		}

		mySysPrefix = aSysPrefix;
		myPrimaryIndexLocation = aPrimaryIndexLocation;
		myIndexingType = IndexingType.BULK;
		myIndexName = myPrimaryIndexLocation + "/" + mySysPrefix.toLowerCase();
	}

	/**
	 * This constructor is suitable for Indexing a single Request.
	 * 
	 * @param aSystemId
	 *            Business Area ID.
	 * @param aRequestId
	 *            Request ID.
	 * @param aPrimaryIndexLocation
	 *            Location of Primary Index.
	 */
	public RequestIndexer(String aSysPrefix, int aSystemId, int aRequestId,
			String aPrimaryIndexLocation) {
		if ((aSysPrefix == null) || (aSysPrefix.trim().equals("") == true)
				|| (aRequestId < 1) || (aPrimaryIndexLocation == null)
				|| (aPrimaryIndexLocation.trim().equals("") == true)) {
			throw new IllegalArgumentException(
					"Business Area prefix, RequestId and the Location of "
							+ "primary index are required for indexing a Business area.");
		}

		mySystemId = aSystemId;
		mySysPrefix = aSysPrefix;
		myRequestId = aRequestId;
		myPrimaryIndexLocation = aPrimaryIndexLocation;
		myIndexingType = IndexingType.REQ;
		myIndexName = myPrimaryIndexLocation + "/" + mySysPrefix.toLowerCase();
	}

	// ~--- methods ------------------------------------------------------------

	/**
	 * This method adds the Request &amp; Action documents to the corresponding
	 * stores in the given location.
	 * 
	 * @param aLocation
	 *            Location of the index.
	 */
	private void addDocsToIndex(String aLocation) {
		String lReqStore = aLocation + "/RequestStore";
		String lActStore = aLocation + "/ActionStore";
		IndexWriter iwReqStore = null;
		IndexWriter iwActStore = null;

		try {
			LOG.info("Adding " + myRequestDocs.size()
					+ " Request Documents to the Request Store.");

			// Open IndexWriter to the Request Store.
			iwReqStore = IndexUtil.openWriter(lReqStore);

			if (iwReqStore == null) {
				System.out.println("The returned IndexWriter for Request Store is null.");
				return;
			}

			for (Document document : myRequestDocs) {
				try {
					iwReqStore.addDocument(document);
				} catch (IOException ioe) {
					ioe.printStackTrace();
					LOG.info("",(ioe));
				}
			}
		} finally {
			try {
				if (iwReqStore != null) {
					iwReqStore.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		try {
			LOG.info("Adding " + myActionDocs.size()
					+ " Action Documents to the Action Store.");
			iwActStore = IndexUtil.openWriter(lActStore);

			if (iwActStore == null) {
				System.out.println("The returned IndexWriter for ActionStore is null.");
				return;
			}

			for (Document document : myActionDocs) {
				try {
					iwActStore.addDocument(document);
				} catch (IOException ioe) {
					ioe.printStackTrace() ;
					LOG.info("",(ioe));
				}
			}
		} finally {
			try {
				if (iwActStore != null) {
					iwActStore.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		return;
	}

	/**
	 * This method reads the request ex data.
	 * 
	 * @param rsReqEx
	 * @throws SQLException
	 */
	private void readReqExData(ResultSet rsReqEx) throws SQLException {
		while (rsReqEx.next() == true) 
		{
			try
			{
				int requestId = rsReqEx.getInt(Field.REQUEST);
				String fieldName = rsReqEx.getString("field_name");
				int dataType = rsReqEx.getInt("data_type_id");
	
				Hashtable<String, String> rec = myRequestData.get(requestId);
	
				if( null == rec )
					rec = new Hashtable<String,String>() ;
			
				String value = "";
				Boolean bValue;
				int iValue;
				double dValue;
				java.sql.Timestamp sts;
				Timestamp ts;
	
				switch (dataType) 
				{
					case DataType.BOOLEAN:
						bValue = rsReqEx.getBoolean("bit_value");
						addValue( rec, fieldName, bValue ) ;
		
						break;
		
					case DataType.DATE:
					case DataType.TIME:
					case DataType.DATETIME:
						sts = rsReqEx.getTimestamp("datetime_value");
						ts = Timestamp.getTimestamp(sts);
						addValue(rec, fieldName, ts) ;
						
						break;
		
					case DataType.INT:
						iValue = rsReqEx.getInt("int_value");
						addValue( rec,fieldName, iValue ) ;
		
						break;
		
					case DataType.REAL:
						dValue = rsReqEx.getDouble("real_value");
						addValue( rec, fieldName, dValue ) ;
		
						break;
		
					case DataType.STRING:
						value = rsReqEx.getString("varchar_value");
						addValue( rec, fieldName, value ) ;
		
						break;
		
					case DataType.TEXT:
						value = rsReqEx.getString("text_value");
						addValue( rec, fieldName, value ) ;
						break;
		
					case DataType.TYPE:
						value = rsReqEx.getString("type_value");
						addValue( rec, fieldName, value ) ;
						break;
				}
	
				myRequestData.put(requestId, rec);
			}
			catch( Exception e )
			{
				e.printStackTrace() ;
				// do nothing // continue // this request might not get indexed.
			}
		}
	}

	/**
	 * this method reads the request user details.
	 * 
	 * @param rsReqUser
	 * @throws SQLException
	 */
	private void readReqUserData(ResultSet rsReqUser) throws SQLException 
	{
		while (rsReqUser.next() == true) 
		{
			int requestId = rsReqUser.getInt(Field.REQUEST);
//			int userType = rsReqUser.getInt("user_type_id");
			int fieldId = rsReqUser.getInt("field_id");
			String fieldName = rsReqUser.getString("field_name");
			String userLogin = rsReqUser.getString("user_login");
		
			Hashtable<String, String> rec = myRequestData.get(requestId) ;
			if( null == rec )
				rec = new Hashtable<String,String>() ;

			String key = fieldName  ;
//			switch (field_) 
//			{
//				case UserType.LOGGER:
//					key = Field.LOGGER ;
//	
//					break;
//	
//				case UserType.ASSIGNEE:
//					key = Field.ASSIGNEE ;
//	
//					break;
//	
//				case UserType.SUBSCRIBER:
//					key = Field.SUBSCRIBER ;
//	
//					break;
//	
//				case UserType.TO:
//					key = Field.TO ;
//	
//					break;
//	
//				case UserType.CC:
//					key = Field.CC ;
//	
//					break;
//			}

			if( key == null )
				continue ;
			
			String value = rec.get(key);

			if (value == null) 
			{
				value = "" ;
			}

			value += " " + userLogin ;
			rec.put(key, value);
			myRequestData.put(requestId, rec) ;
		}
	}

	void addValue(Hashtable<String,String> rec , String field, Integer i ) 
	{
		rec.put(field, IndexUtil.toLuceneInt(i)) ;
	}
	void addValue(Hashtable<String,String> rec , String field, Double d ) 
	{
		rec.put(field, d+"") ;
	}
	void addValue(Hashtable<String,String> rec , String field, String str ) 
	{
		rec.put(field, str ) ;
	}
	void addValue(Hashtable<String,String> rec , String field, Timestamp ts ) 
	{
		rec.put(field, IndexUtil.toLuceneDate(ts)) ;
	}
	void addValue(Hashtable<String,String> rec, String field, Boolean b )
	{
		rec.put(field, IndexUtil.toLuceneBoolean(b)) ;
	}
	
	void addAllFields( Hashtable<String,String> rec, Iterator<Field> iter, ResultSet rs )
	{
		while(iter.hasNext())
		{
			try
			{
				Field field = iter.next() ;				
				// switch according to types
				switch( field.getDataTypeId() )
				{
					case DataType.BOOLEAN :
						Boolean boolValue = null ;
						boolValue = rs.getBoolean(field.getName()) ;
						if( null == boolValue )
							break ;
						addValue( rec, field.getName(), boolValue ) ;
						break ;
					case DataType.DATE : 
					case DataType.DATETIME :
					case DataType.TIME :
						java.sql.Timestamp sqlTs = null;
						Timestamp ts = null;
						sqlTs = rs.getTimestamp(field.getName() ) ;
						if(null == sqlTs )
							break ;
						ts = Timestamp.getTimestamp(sqlTs) ;
						if( null == ts )
							break ;
						addValue(rec, field.getName(), ts ) ;
						break ;
					case DataType.INT : 
						Integer intValue = null ;
						intValue = new Integer( rs.getInt(field.getName()) );
						if( null == intValue ) 
							break ;
						addValue(rec, field.getName(), intValue ) ;
						break ;
					case DataType.REAL : 
						Double realValue = null ;
						realValue = new Double( rs.getDouble(field.getName()) ) ;
						if( null == realValue )
							break ;
						addValue(rec, field.getName(), realValue ) ;
						break ;
					case DataType.STRING : 
					case DataType.TEXT :					 
					case DataType.TYPE :						
						 String stringValue = null ;
						 stringValue = rs.getString(field.getName()) ;
						if( null == stringValue )
							break ;
						
						// convert the discription from html to text
						if( field.getName().equals(Field.DESCRIPTION))
						{
							Reader htmlReader = new StringReader( stringValue ) ;
							Reader textReader = Readerizer.extractTextFromHtml(htmlReader) ;
							int n = -1 ;
							String description = "" ;
							char [] buf = new char[1000] ; 
							while( ( n = textReader.read(buf) ) != -1  )
							{
								description += new String( buf, 0, n ) ; 
							}
							stringValue = description ;
						}
							
						addValue(rec,field.getName(),stringValue) ;
						break ;				
					default : 
						// do nothing					
				}
			}
			catch( Exception e )
			{
				// e.printStackTrace() ;
				// do nothing // continue : this field will not get indexed
			}
		}		
	}
	/**
	 * This method reads the request details from the result set.
	 * 
	 * @param rsRequest
	 * @throws DatabaseException 
	 * @throws DatabaseException 
	 * @throws SQLException
	 * @throws DatabaseException 
	 * @throws SQLException 
	 * @throws SQLException 
	 */
	private void readRequestData(ResultSet rsRequest) throws DatabaseException, SQLException 
	{
		ArrayList<Field> fixedFields= Field.getFixedFieldsBySystemId(mySystemId) ;
		while (rsRequest.next() == true) 
		{
			try
			{
				String sysPrefix = rsRequest.getString(SYS_PREFIX);
				int sys_id = rsRequest.getInt(Field.BUSINESS_AREA ) ;
				int requestId = rsRequest.getInt(Field.REQUEST);
				Hashtable<String,String> rec = myRequestData.get(new Integer(requestId)) ;
				if( null == rec )
					rec = new Hashtable<String,String>() ;
				
				// add these primary values 
				rec.put(SYS_PREFIX, sysPrefix);
				rec.put(Field.BUSINESS_AREA, sys_id+"") ;
				rec.put(Field.REQUEST, requestId+"") ;
				
				// now iterate for all values and add them according to their type
				Iterator<Field> iter = fixedFields.iterator() ;			
				addAllFields(rec,iter,rsRequest);
				
				myRequestData.put( requestId,rec ) ;
			}
			catch(Exception e )
			{
				 e.printStackTrace() ;
				// do nothing // continue // this request will not get indexed.
			}
		}
	}

	/**
	 * The run method of Thread overridden for ActionIndexer.
	 */
	public void run() {
		try {

			// Get the connection.
			// myConnection = DataSourcePool.getConnection();//ourDbServer,
			// ourDbName, ourDbUser, ourDbPass, ourDriverClass, ourDriverTag);

			// Start indexing process based on the type.
			if (myIndexingType == IndexingType.BULK) {
				startBAIndexProcess();
			} else if (myIndexingType == IndexingType.REQ) {
				startReqIndexProcess();
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace() ;
			LOG.error("",(e));
		} 
		finally 
		{
		}
	}

	/**
	 * This method runs the steps required to index a given business area.
	 */
	private void startBAIndexProcess() {

		/*
		 * Steps involved are (1) Get the Request Count in this business area.
		 * (2) Calculate the number of iterations needed. (3) Check if the Index
		 * Location for this BA is present. (4) Create the temporary index
		 * location. (5) Call the getRequestInfo method in each iteration. (6)
		 * Open a Reader to primary and delete documents of this BA. (7) Open a
		 * Writer to Primary and merge the temporary into it. (8) Delete the
		 * temporary index.
		 */
		Connection conn = null;
		try {

			// Step 1: Get the Request Count.
			int maxRequestId = 0;
			int blockSize = 50;
			conn = DataSourcePool.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select " + "sys_id, "
					+ "sys_prefix, " + "max_request_id " + "from "
					+ "business_areas " + "where " + "sys_prefix = '"
					+ mySysPrefix + "'");

			if ((rs == null) || (rs.next() == false)) {
				LOG.info("There are no requests in this Business Area: "
						+ mySysPrefix + "\n"
						+ "Check if the business area specified is correct.");

				return;
			}

			maxRequestId = rs.getInt("max_request_id");
			mySystemId = rs.getInt("sys_id");
			mySysPrefix = rs.getString(SYS_PREFIX);
			rs.close();
			stmt.close();

			if (maxRequestId == 0) {
				LOG.debug("There are no requests in this Business Area: "
						+ mySysPrefix + "\n"
						+ "Check if the business area specified is correct.");

				return;
			}

			// Step 2: Number of iterations needed.
			int iterations = maxRequestId / blockSize + 2;

			// Step 3: Check if the location is present.
			IndexUtil.checkIndex(myIndexName, true);

			// Step 4: Create the temporary Index.
			myTempIndexName = myIndexName + "/temp_" + mySysPrefix;

			try {
				IndexUtil.createIndex(myTempIndexName);
			} catch (IOException ioe) {
				StringBuffer message = new StringBuffer();

				message.append("Unable to create lucene index at: ").append(
						myTempIndexName);
				LOG.error(message.toString(), ioe);

				return;
			}

			// Step 5: Index the requests in blocks of 4000.
			for (int i = 0; i < iterations; i++) {
				int start = i * blockSize;
				int end = (i + 1) * blockSize;

				LOG.info(mySysPrefix + ": Request Range: [ " + start + ", "
						+ end + " ]");
				getRequestInfo(start, end);

				if (myRequestDocs.size() == 0) {
					continue;
				}

				addDocsToIndex(myTempIndexName);
				System.gc();
			}

			LOG.debug("Indexed the requests to a temporary location.");

			// Step 6: Delete the documents of this Business Area.
			String key = "sysPrefix";
			String value = mySysPrefix;
			int delCount = 0;

			// Delete the documents from the Action Store.
			String pActStore = myIndexName + "/ActionStore";

			delCount = IndexUtil.deleteDocuments(pActStore, key, value);
			LOG.info(delCount + " Documents deleted from Action Store.");

			// Delete the documents from the Request Store.
			String pReqStore = myIndexName + "/RequestStore";

			delCount = IndexUtil.deleteDocuments(pReqStore, key, value);
			LOG.info(delCount + " Documents deleted from Request Store.");

			// Step 8: Merge the temporary one with the primary.
			String tActStore = myTempIndexName + "/ActionStore";
			String tReqStore = myTempIndexName + "/RequestStore";

			// Merge the action stores.
			IndexUtil.merge(pActStore, tActStore);

			// Merge the request stores.
			IndexUtil.merge(pReqStore, tReqStore);

			// Step 9: Delete the temporary index.
			IndexUtil.deleteIndex(myTempIndexName);

			// Optimize the primary index stores.
			LOG.info("Optimizing the index...");
			IndexOptimizer.optimizeIndex(pActStore);
			IndexOptimizer.optimizeIndex(pReqStore);
			LOG.info("Optimizing the index: Done");
			LOG.info("Business area indexed successfully.");
		} catch (Exception e) {
			LOG.error("",(e));
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * This method indexes a single request.
	 */
	private void startReqIndexProcess() 
	{	
			/*
			 * Steps involved in indexing a request. 1. Check if the index location
			 * is present. If not create it. 2. Get the information related to
			 * request from the database. 3. Delete the old documents related to
			 * this request from the action store. 4. Delete the old documents
			 * related to this request from the Request store. 5. Add the documents
			 * to the index.
			 */
			try {
				LOG.debug("Checking the existence of index location.");
	
				synchronized( lockObject )
				{
//					System.out.println("Nitiraj : startReqIndexProcess : inside synchronized block1" ) ;
					// Step 1:
					IndexUtil.checkIndex(myIndexName, true);
//					System.out.println("Nitiraj : startReqIndexProcess :returning from sychronized block1");
				}
	
				String lReqStore = myIndexName + "/RequestStore";
				String lActStore = myIndexName + "/ActionStore";
	
				// Step 2:
			//	getRequestInfo(myRequestId, myRequestId + 1);
				getRequestInfo(myRequestId,myRequestId+1) ;
				LOG.info("Obtained the request information: " + mySysPrefix + "#"
						+ myRequestId);
	
				// Step 3:
	
				/*
				 * Since the indexes are separate for each business area, the
				 * request Id field is unique and can be used to delete the
				 * documents correctly.
				 * 
				 * WARNING: If we plan to have one single index for all business
				 * areas, then the key should be changed for it to be unique.
				 */
				String key = Field.REQUEST;
				String value = Integer.toString(myRequestId);
				int delCount = 0 ;
				synchronized( lockObject )
				{
					//System.out.println("Inside synchronized block2.");
					delCount = IndexUtil.deleteDocuments(lActStore, key, value);
					
					LOG.info(delCount + " Documents deleted from Action Store.");
		
					// Step 4:
					key = Field.REQUEST;
					value = Integer.toString(myRequestId);
					
					delCount = IndexUtil.deleteDocuments(lReqStore, key, value);
					
					LOG.info(delCount + " Documents deleted from Request Store.");
					
					// Step 5:
					addDocsToIndex(myIndexName);
//					System.out.println("Nitiraj : startReqIndexProcess : returning from synchronized block2");
				}
			} catch (Exception e) 
			{
				e.printStackTrace() ;
				LOG.error("",(e));
			}			
		
			return;
		
	}

	/**
	 * @throws SQLException
	 * @throws DatabaseException 
	 * 
	 */
	private void getRequestInfo(int aStart, int aEnd) throws SQLException, DatabaseException 
	{
		// 
		
		String stmt = "stp_tbits_getRequestInfoInRange ?, ?, ?";
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			CallableStatement cs = conn.prepareCall(stmt);

			cs.setInt(1, mySystemId);
			cs.setInt(2, aStart);
			cs.setInt(3, aEnd);
			cs.execute();

			/*
			 * Following result sets are returned as a result of executing the
			 * above stored procedure in that order: 
			 * 1. Request records. 
			 * 2.Request User records 
			 * 3. Request Ex records 
			 * 4. Action Records.
			 * 5. action user records
			 * 6. action ex records
			 */

			// Request Records.
			ResultSet rsRequest = cs.getResultSet();

			if (rsRequest != null) {
				readRequestData(rsRequest);
			}

			cs.getMoreResults();

			// Request User Records.
			ResultSet rsReqUser = cs.getResultSet();

			if (rsReqUser != null) {
				readReqUserData(rsReqUser);
			}

			cs.getMoreResults();

			// Request User Records.
			ResultSet rsReqEx = cs.getResultSet();

			if (rsReqEx != null) {
				readReqExData(rsReqEx);
			}

			cs.getMoreResults();

			// Request User Records.
			ResultSet rsActions = cs.getResultSet();

			if (rsActions != null) {
				readActionData( rsActions );
				//readAndDocumentActionData(rsActions);
			}
			
			cs.getMoreResults() ;
			ResultSet rsActUsers = cs.getResultSet() ;
			if( rsActUsers != null )
			{
				readActUsers( rsActUsers );
			}
			
			cs.getMoreResults() ;
			ResultSet rsActExData = cs.getResultSet() ;
			
			if( null != rsActExData ) 
			{
				readActExData( rsActExData ) ;
			}
			
			rsRequest.close() ;
			rsReqUser.close() ;
			rsReqEx.close() ;
			rsActions.close() ;
			rsActUsers.close() ;
			rsActExData.close() ;
			cs.close();

			readAttachments( aStart , aEnd ) ;
			
			// Finally Document the requests and actions 
			documentRequestsAndActionsAndAttachments();
			
		} catch (SQLException e) {
			e.printStackTrace() ;
			throw e;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) 
				{
					e.printStackTrace();
				}
			}
		}
	}
		
	private void documentRequestsAndActionsAndAttachments() 
	{
		// this will create documents for each request and add them to the myRequestDocs and myActionDocs
		// first requests
		if( null != myRequestData )
		{
			for( Enumeration<Integer> keys = myRequestData.keys() ; keys.hasMoreElements() ; )
			{
				try
				{
					Integer requestId = keys.nextElement() ;
					Hashtable<String,String> params = myRequestData.get(requestId) ;
					if( null == params )
						continue ;
	
					Hashtable<ActionFileInfo,Reader> attReaders = getAttReaders( requestId, 1 ) ;

					String sysId = params.get(Field.BUSINESS_AREA) ;
					sysId = Integer.parseInt(sysId)+"";
					
					Document doc = new Document() ;				
					doc.add(org.apache.lucene.document.Field.Keyword(Field.REQUEST, requestId+"")) ;			
					doc.add(org.apache.lucene.document.Field.Keyword(Field.BUSINESS_AREA, sysId)) ;
					
					// add fixed and extended fields and users ( i.e. also multi-value fields ) 
					for( Enumeration<String> pk = params.keys() ;  pk.hasMoreElements() ; )
					{					
						String fieldName = pk.nextElement() ;
						String value = params.get(fieldName) ;
						if( fieldName.equals(Field.REQUEST) || fieldName.equals(Field.BUSINESS_AREA))
							continue ;
						
						doc.add(new org.apache.lucene.document.Field(fieldName, value, false , true , true , false )) ;
						doc.add(new org.apache.lucene.document.Field(ALL_TEXT, value, false , true , true , false )) ;
						doc.add(new org.apache.lucene.document.Field(ALL, value, false , true , true , false )) ;
					}
					
					// add attachments
					for( Enumeration<ActionFileInfo> attKeys = attReaders.keys() ; attKeys.hasMoreElements() ;)
					{
						Reader reader = null ;
						try
						{
							ActionFileInfo afi = attKeys.nextElement() ;
							reader = attReaders.get(afi) ;
							if( reader == null )
								return ;
							String fileName = afi.getName() ;
							doc.add(new org.apache.lucene.document.Field(Field.lookupBySystemIdAndFieldId(afi.getSystemId(), afi.getFieldId()).getName(), fileName, false, true, true, false )) ;
							doc.add(new org.apache.lucene.document.Field(ALL_TEXT, fileName, false , true , true , false )) ;
							doc.add(new org.apache.lucene.document.Field(ALL, fileName, false , true , true , false )) ;
							String stringValue = getStringValue(reader);
							doc.add(org.apache.lucene.document.Field.Text(ALL, stringValue)) ;
						}
						catch( Exception e )
						{
							e.printStackTrace() ;
							// this attachment will not indexed
						}
						finally
						{
							if( reader != null )
							{
								reader.close() ;
							}
						}
					}
					
					myRequestDocs.add(doc);
				}
				catch( Exception e )
				{
					e.printStackTrace() ;
					// ignore this request -- it will not be indexed
				}
			}
		}
		
		
		if( null != myActionData )
		{
			for( Enumeration<Integer> keys = myActionData.keys() ; keys.hasMoreElements() ; )
			{
				Integer requestId = keys.nextElement() ;
				Hashtable<Integer, Hashtable<String,String> > aparams = myActionData.get(requestId) ;
				if( null == aparams )
					continue ;
				
				for( Enumeration<Integer> akeys = aparams.keys() ; akeys.hasMoreElements() ; )
				{
					try
					{
						Integer actionId = akeys.nextElement() ;
						Hashtable<String,String> params = aparams.get(actionId) ;
						
						Hashtable<ActionFileInfo,Reader> attReaders = getAttReaders( requestId, actionId ) ;
						
						String sysId = params.get(Field.BUSINESS_AREA) ;
						sysId = Integer.parseInt(sysId)+"";
						
						Document doc = new Document() ;				
						doc.add(org.apache.lucene.document.Field.Keyword(Field.REQUEST, requestId+"")) ;			
						doc.add(org.apache.lucene.document.Field.Keyword(Field.BUSINESS_AREA, sysId)) ;
						doc.add(org.apache.lucene.document.Field.Keyword(Field.ACTION, actionId+"")) ;
						
						// add fixed and extended fields and users ( i.e. also multi-value fields ) 
						for( Enumeration<String> pk = params.keys() ;  pk.hasMoreElements() ; )
						{					
							String fieldName = pk.nextElement() ;
							String value = params.get(fieldName) ;
							if( fieldName.equals(Field.REQUEST) || fieldName.equals(Field.BUSINESS_AREA) || fieldName.equals(Field.ACTION))
								continue ;
							
							doc.add(new org.apache.lucene.document.Field(fieldName, value, false , true , true , false )) ;
							doc.add(new org.apache.lucene.document.Field(ALL_TEXT, value, false , true , true , false )) ;
							doc.add(new org.apache.lucene.document.Field(ALL, value, false , true , true , false )) ;
						}
						
						// add attachments
						for( Enumeration<ActionFileInfo> attKeys = attReaders.keys() ; attKeys.hasMoreElements() ;)
						{
							try
							{
								
									ActionFileInfo afi = attKeys.nextElement() ;
									Reader reader = attReaders.get(afi) ;
									String fileName = afi.getName() ;
									doc.add(new org.apache.lucene.document.Field(Field.lookupBySystemIdAndFieldId(afi.getSystemId(), afi.getFieldId()).getName(), fileName, false, true, true, false )) ;
									doc.add(new org.apache.lucene.document.Field(ALL_TEXT, fileName, false , true , true , false )) ;
									doc.add(new org.apache.lucene.document.Field(ALL, fileName, false , true , true , false )) ;
									doc.add(org.apache.lucene.document.Field.Text(ALL, reader)) ;
							
							}
							catch(Exception e)
							{
								// e.printStackTrace() ;
								// this attachment will not get indexed.
							}
						}
						myActionDocs.add(doc) ;
					}
					catch( Exception e )
					{
						e.printStackTrace() ;
						// ignore this action -- it will not be indexed
					}
				}
			}
		}
	}

	private String getStringValue(Reader reader) 
	{
		if( null == reader )
			return "";
		StringBuffer sb = new StringBuffer();		
		try
		{
			int buffLength = 1024 * 1024 ; // one MB
			char[] buff = new char[buffLength];
			int lengthRead = -1 ;
			while( (lengthRead = reader.read(buff)) != -1 )
			{
				sb.append(buff, 0, lengthRead);
			}
		}
		catch(Exception e)
		{
			LOG.error(e.getMessage(), e);
		}
		
		return sb.toString();
	}

	private Hashtable<ActionFileInfo, Reader> getAttReaders(Integer requestId, Integer actionId ) 
	{
		Hashtable<ActionFileInfo, Reader> attReaders = new Hashtable<ActionFileInfo, Reader>() ;
		Hashtable<Integer, Collection<ActionFileInfo>> attInfos = myAttInfo.get(requestId) ;
		if( null == attInfos ) 
			return attReaders ;
		
		Collection<ActionFileInfo> fileInfos = attInfos.get(actionId) ;
		if( null == fileInfos )
			return attReaders ;
		
		for( ActionFileInfo afi : fileInfos ) 
		{
			try
			{
				if( null == afi )
					continue ;
				
				Integer fileId = afi.getFileId()  ;
				String path = APIUtil.getAttachmentLocation() + "/" + Uploader.getFileLocation(fileId.intValue()) ;
				File file = new File(path) ;
				if( ! file.exists() )
					continue ;
				
				Reader reader = null ;
				
				reader = Readerizer.readerize( path ) ;
				
				if( null == reader )
					continue ;
			
				attReaders.put(afi, reader) ;
			}
			catch( Exception e )
			{
				e.printStackTrace() ;
				// do nothing  // continue // this attachement will not be indexed.
			}
		}
		
		return attReaders;
	}

	private void readAttachments(int start, int end ) 
	{
		for( int requestId = start ; requestId < end ; requestId++ )
		{
			
			Hashtable<Integer, Collection<ActionFileInfo>> attInfo = null ;
			try {
				attInfo = Action.getAllActionFiles(mySystemId, requestId);
				if( null == attInfo )
					continue ;
				
				myAttInfo.put(requestId, attInfo ) ;
			} catch (DatabaseException e) 
			{
				e.printStackTrace();
				// do nothing // continue // this request might not get indexed.
			}			
		}		
	}

	private void readActExData(ResultSet rsActExData) throws SQLException 
	{
		while (rsActExData.next() == true) 
		{
			try
			{
				int requestId = rsActExData.getInt(Field.REQUEST);
				int actionId = rsActExData.getInt(Field.ACTION) ;
				String fieldName = rsActExData.getString("field_name");
				int dataType = rsActExData.getInt("data_type_id");
	
				Hashtable<Integer, Hashtable<String, String>> arec = myActionData.get(requestId);
	
				if( null == arec )
					arec = new Hashtable<Integer,Hashtable<String,String>>() ;
			
				Hashtable<String,String> rec = arec.get(actionId) ;
				if( null == rec )
					rec = new Hashtable<String,String>() ;
				
				String value = "";
				Boolean bValue;
				int iValue;
				double dValue;
				java.sql.Timestamp sts;
				Timestamp ts;
	
				switch (dataType) 
				{
					case DataType.BOOLEAN:
						bValue = rsActExData.getBoolean("bit_value");
						addValue( rec, fieldName, bValue ) ;
		
						break;
		
					case DataType.DATE:
					case DataType.TIME:
					case DataType.DATETIME:
						sts = rsActExData.getTimestamp("datetime_value");
						ts = Timestamp.getTimestamp(sts);
						addValue(rec, fieldName, ts) ;
						
						break;
		
					case DataType.INT:
						iValue = rsActExData.getInt("int_value");
						addValue( rec,fieldName, iValue ) ;
		
						break;
		
					case DataType.REAL:
						dValue = rsActExData.getDouble("real_value");
						addValue( rec, fieldName, dValue ) ;
		
						break;
		
					case DataType.STRING:
						value = rsActExData.getString("varchar_value");
						addValue( rec, fieldName, value ) ;
		
						break;
		
					case DataType.TEXT:
						value = rsActExData.getString("text_value");
						addValue( rec, fieldName, value ) ;
						break;
		
					case DataType.TYPE:
						value = rsActExData.getString("type_value");
						addValue( rec, fieldName, value ) ;
						break;
				}
	
				arec.put(actionId, rec);
				myActionData.put(requestId, arec);
			}
			catch(Exception e)
			{
			//	e.printStackTrace() ;
				// do nothing // continue // this request might not get index.
			}
		}	
	}

	private void readActUsers(ResultSet rsActUsers) throws SQLException 
	{
		while (rsActUsers.next() == true) 
		{
			try
			{
				int requestId = rsActUsers.getInt(Field.REQUEST);
//				int userType = rsActUsers.getInt("user_type_id");
				String fieldName = rsActUsers.getString("field_name");
				int field_id=rsActUsers.getInt("field_id");
				String userLogin = rsActUsers.getString("user_login");
				int actionId = rsActUsers.getInt(Field.ACTION) ;
				Hashtable<Integer,Hashtable<String, String>> arec = myActionData.get(requestId) ;
				if( null == arec )
					arec = new Hashtable<Integer, Hashtable<String,String>>() ;
				
				Hashtable<String,String> rec = arec.get(actionId) ;
				
				if( null == rec ) 
					rec = new Hashtable<String, String>() ;
	
				String key = fieldName  ;
//				switch (userType) 
//				{
//					case UserType.LOGGER:
//						key = Field.LOGGER ;
//		
//						break;
//		
//					case UserType.ASSIGNEE:
//						key = Field.ASSIGNEE ;
//		
//						break;
//		
//					case UserType.SUBSCRIBER:
//						key = Field.SUBSCRIBER ;
//		
//						break;
//		
//					case UserType.TO:
//						key = Field.TO ;
//		
//						break;
//		
//					case UserType.CC:
//						key = Field.CC ;
//		
//						break;
//				}
	
				if( key == null )
					continue ;
				
				String value = rec.get(key);
	
				if (value == null) 
				{
					value = "" ;
				}
	
				value += " " + userLogin ;
				rec.put(key, value);
				arec.put(actionId, rec) ;
				myActionData.put(requestId, arec) ;
			}
			catch(Exception e )
			{
			//	e.printStackTrace() ;
				// do nothing //continue // this request might not get indexed.
			}
		}
	}

	private void readActionData(ResultSet rsActions) throws DatabaseException, SQLException 
	{
		ArrayList<Field> fixedFields= Field.getFixedFieldsBySystemId(mySystemId) ;
		while (rsActions.next() == true) 
		{
			try
			{
				String sysPrefix = rsActions.getString(SYS_PREFIX);
				int sys_id = rsActions.getInt(Field.BUSINESS_AREA ) ;
				int requestId = rsActions.getInt(Field.REQUEST);
				int actionId = rsActions.getInt(Field.ACTION) ;
				Hashtable< Integer, Hashtable<String,String> > arec = myActionData.get(new Integer(requestId)) ;
				if( null == arec )
					arec = new Hashtable< Integer, Hashtable<String,String>>() ;
				
				Hashtable<String,String> rec = arec.get(actionId) ;
				if( null == rec ) 
					rec = new Hashtable<String,String>() ;
				
				// add these primary values 
				rec.put(SYS_PREFIX, sysPrefix);
				rec.put(Field.BUSINESS_AREA, sys_id+"") ;
				rec.put(Field.REQUEST, requestId+"") ;
				rec.put( Field.ACTION, actionId+"");
				// now iterate for all values and add them according to their type
				Iterator<Field> iter = fixedFields.iterator() ;			
				addAllFields(rec,iter,rsActions);
				
				arec.put(actionId, rec) ;
				myActionData.put( requestId,arec ) ;
			}
			catch(Exception e)
			{
				//e.printStackTrace() ;
				// do nothing  // continue // this field might not get indexed
			}
		}		
	}

	/**
	 * This method returns dValue if aValue is null. If dValue is also null,
	 * then it returns an empty string.
	 * 
	 * @param aValue
	 * @param dValue
	 * @return
	 */
	private String isNull(String aValue, String dValue) {

		// If default value itself is null, then let us return empty string.
		if (dValue == null) {
			dValue = "";
		}

		// If the actual value is null, then return the default value.
		if (aValue == null) {
			return dValue;
		}

		// Else return the actual value.
		return aValue;
	}
}
