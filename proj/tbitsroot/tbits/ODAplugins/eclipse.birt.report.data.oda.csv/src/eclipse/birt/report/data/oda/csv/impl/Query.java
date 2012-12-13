/*
 *************************************************************************
 * Copyright (c) 2007 <<Your Company Name here>>
 *  
 *************************************************************************
 */

package eclipse.birt.report.data.oda.csv.impl;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;

import org.eclipse.datatools.connectivity.drivers.DriverManager;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.SortSpec;

import transbit.*;
import transbit.tbits.*;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.Field;
import transbit.tbits.search.*;
import transbit.tbits.domain.DataType;

/**
 * Implementation class of IQuery for an ODA runtime driver.
 * <br>
 * For demo purpose, the auto-generated method stubs have
 * hard-coded implementation that returns a pre-defined set
 * of meta-data and query results.
 * A custom ODA driver is expected to implement own data source specific
 * behavior in its place. 
 */
public class Query implements IQuery
{
	private int m_maxRows;
	private IResultSetMetaData resultMetadata;
	private String myQuery;
	private int systemID;
	private int userID;
	//private ArrayList<Result> resultData;
	//private IResultSet resultSet;
	//static boolean queryExecuted = false;
	
	
	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#prepare(java.lang.String)
	 */
	
	public Query(int asystemID, int auserID)
	{
		System.out.println("\n\n\n++++++++++++++++In Query Constructor+++++++++++++++++++++++++++\n\n\n");
		systemID = asystemID;
		userID = auserID;
	}
	public void prepare( String queryText ) throws OdaException
	{
        // TODO Auto-generated method stub
		myQuery = queryText;
		System.out.println("Query:prepare");
		
	}
	
	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setAppContext(java.lang.Object)
	 */
	public void setAppContext( Object context ) throws OdaException
	{
	    // do nothing; assumes no support for pass-through context
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#close()
	 */
	public void close() throws OdaException
	{
        // TODO Auto-generated method stub
	}
	
	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getMetaData()
	 */
	public IResultSetMetaData mygetMetaData() throws OdaException
	{
		System.out.println("Query:mygetMetaData()");
		//System.out.println("resultSetMetadata "+ resultSet.getMetaData());
		return resultMetadata;
	}
	
	
	private String getDataType(int dataType)
	{
		String reportDataType = null;
		switch(dataType)
   	    {
	   	 	case DataType.INT:
	   	 	{
	   	 		reportDataType = "INT" ;
	   	 		break;
	   	 	}
	   	 	case DataType.DATE :
	   	 	{
	   	 		reportDataType = "TIMESTAMP";
	   	 		break;
	   	 	}
	   	 	case DataType.BOOLEAN :
	   	 	{
	   	 		reportDataType = "BOOLEAN";
	   	 		break;
	   	 	}
	   	 	case DataType.TIME :
	   	 	{
	   	 		reportDataType = "TIMESTAMP";
	   	 		break;
	   	 	}
	   	 	case DataType.TEXT :
	   	 	{
	   	 		reportDataType = "STRING";
	   	 		break;
	   	 	}
	   	 	case DataType.STRING :
	   	 	{
	   	 		reportDataType = "STRING";
	   	 		break;
	   	 	}
	   	 	case DataType.REAL :
	   	 	{
	   	 		reportDataType = "DOUBLE";
	   	 		break;
	   	 	}
	   	 	case DataType.DATETIME :
	   	 	{
	   	 		reportDataType = "TIMESTAMP";
	   	 		break;
	   	 	}
	   	 	case DataType.TYPE :
	   	 	{
	   	 		reportDataType = "STRING";
	   	 		break;
	   	 	}
	   	 	case DataType.MULTI_VALUE :
	   	 	{
	   	 		reportDataType = "STRING";
	   	 		break;
	   	 	}
   	 	}
		return reportDataType;
		
	}
	public IResultSetMetaData getMetaData() throws OdaException
	{
	     System.out.println("Query:getMetaData()");
	     int aSystemId = systemID;
	     
	     Hashtable<String,Field> fieldsTable = null;
	     ArrayList<Field> fixedFields = null;
	     int columnCount = 1;
	     try{
	    	 if(systemID == 0)
	         {
	    		 fixedFields = Field.getFixedFieldsBySystemId(1);
	    		 columnCount = fixedFields.size();
	         }
	         else
	         {
	        	 fieldsTable = Field.getFieldsTableBySystemId(aSystemId); 
	        	 columnCount = fieldsTable.size();
	         }
	     }
	     catch(DatabaseException de)
	     {
	    	 throw new OdaException("Database Exception occured while getting all fields");
	     }
	     
	    
	     String[] columnLabels = new String[columnCount];
	     String[] columnTypeNames = new String[columnCount];
	     String[] columnNames = new String[columnCount];
	     
	     ///*********************
	    //using 0-based index
	     
	     int index = 0;
	     if(systemID == 0)   //for all BA search
	     {
	    	 for(Field field : fixedFields)
	    	 {
	    		 columnLabels[index] = field.getName();
		    	 columnNames[index] = field.getDisplayName();
		    	 int dataType =  field.getDataTypeId();
		    	 String reportDataType = null;
		    	 reportDataType = getDataType(dataType);
		    	 columnTypeNames[index] = reportDataType;
		    	 index++;
	    	 }
	     }
	     else			//single BA search
	     {
	    	  Enumeration<Field> e = null;
	    	  e = fieldsTable.elements();
	    	  for( ; e.hasMoreElements() ;)
	    	     {
	    	    	 Field field = e.nextElement();
	    	    	 columnLabels[index] = field.getName();
	    	    	 columnNames[index] = field.getDisplayName();
	    	    	 int dataType =  field.getDataTypeId();
	    	    	 String reportDataType = null;
	    	    	 reportDataType = getDataType(dataType);
	    	    	 columnTypeNames[index] = reportDataType;
	    	    	 index++;
	    	     }
	    	  
	     }
	     
	     resultMetadata = new ResultSetMetaData(columnNames, columnTypeNames, columnLabels );
		 return resultMetadata;
	}
	
	
	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#executeQuery()
	 */
	public IResultSet executeQuery() throws OdaException
	{
		System.out.println("Query:executeQuery");
//        if(!queryExecuted)
//        {
		//*************************************
		
		
		SearchForReport s = new SearchForReport();
		System.out.println("Query:executeQuery:SearchForReport created");
//	  	int userid = 1;
//	  	int sysid = 1;
	  	ArrayList<String> columns = new ArrayList<String>();
//	  	columns.add("assignee_ids");
//	  	columns.add("category_id");
//	  	columns.add("logger_ids");
//	  	
	  	for(int index=1; index<=resultMetadata.getColumnCount();index++)
	  	{
	  		columns.add(resultMetadata.getColumnLabel(index));
	  	}
	  	System.out.println("Query:executeQuery:before SearchAllFieldsPerBA\nQuery:"+ myQuery);
	  	ArrayList<Result> results = null;
	  	if(systemID == 0)
	  	{
	  		results = s.SearchFieldsAllBA(userID,myQuery, columns);
	  	}
	  	else
	  	{
	  		results = s.SearchAllFieldsPerBA(userID,systemID,myQuery,columns);
	  	}
	  	
	  	
	  	System.out.println("Query:executeQuery:after SearchAllFieldsPerBA");
	  	//for(Result r : results)
		//System.out.println("\nResult:" + r);
		
		//************************************
//        	String[][] resultData ={ 
//    				{"col11", "col12"}, 
//    				{"col21" , "col22"}
//    				};
	  	IResultSet resultSet = null;
    		if(results == null)
    		{
    			System.out.println("resuls are null");	
    		}
    		else 
    		{
	    		resultSet = new ResultSet(results, resultMetadata);
	    		resultSet.setMaxRows( getMaxRows() );
    		}
//    		queryExecuted = true;
    		
//        }
		return resultSet;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty( String name, String value ) throws OdaException
	{
		// do nothing; assumes no data set query property
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setMaxRows(int)
	 */
	public void setMaxRows( int max ) throws OdaException
	{
	    m_maxRows = max;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getMaxRows()
	 */
	public int getMaxRows() throws OdaException
	{
		return m_maxRows;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#clearInParameters()
	 */
	public void clearInParameters() throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setInt(java.lang.String, int)
	 */
	public void setInt( String parameterName, int value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to named input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setInt(int, int)
	 */
	public void setInt( int parameterId, int value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setDouble(java.lang.String, double)
	 */
	public void setDouble( String parameterName, double value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to named input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setDouble(int, double)
	 */
	public void setDouble( int parameterId, double value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setBigDecimal(java.lang.String, java.math.BigDecimal)
	 */
	public void setBigDecimal( String parameterName, BigDecimal value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to named input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setBigDecimal(int, java.math.BigDecimal)
	 */
	public void setBigDecimal( int parameterId, BigDecimal value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setString(java.lang.String, java.lang.String)
	 */
	public void setString( String parameterName, String value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to named input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setString(int, java.lang.String)
	 */
	public void setString( int parameterId, String value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setDate(java.lang.String, java.sql.Date)
	 */
	public void setDate( String parameterName, Date value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to named input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setDate(int, java.sql.Date)
	 */
	public void setDate( int parameterId, Date value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setTime(java.lang.String, java.sql.Time)
	 */
	public void setTime( String parameterName, Time value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to named input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setTime(int, java.sql.Time)
	 */
	public void setTime( int parameterId, Time value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setTimestamp(java.lang.String, java.sql.Timestamp)
	 */
	public void setTimestamp( String parameterName, Timestamp value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to named input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setTimestamp(int, java.sql.Timestamp)
	 */
	public void setTimestamp( int parameterId, Timestamp value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to input parameter
	}

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setBoolean(java.lang.String, boolean)
     */
    public void setBoolean( String parameterName, boolean value )
            throws OdaException
    {
        // TODO Auto-generated method stub
        // only applies to named input parameter
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setBoolean(int, boolean)
     */
    public void setBoolean( int parameterId, boolean value )
            throws OdaException
    {
        // TODO Auto-generated method stub       
        // only applies to input parameter
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setNull(java.lang.String)
     */
    public void setNull( String parameterName ) throws OdaException
    {
        // TODO Auto-generated method stub
        // only applies to named input parameter
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setNull(int)
     */
    public void setNull( int parameterId ) throws OdaException
    {
        // TODO Auto-generated method stub
        // only applies to input parameter
    }

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#findInParameter(java.lang.String)
	 */
	public int findInParameter( String parameterName ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to named input parameter
		return 0;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getParameterMetaData()
	 */
	public IParameterMetaData getParameterMetaData() throws OdaException
	{
        /* TODO Auto-generated method stub
         * Replace with implementation to return an instance 
         * based on this prepared query.
         */
		return new ParameterMetaData();
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setSortSpec(org.eclipse.datatools.connectivity.oda.SortSpec)
	 */
	public void setSortSpec( SortSpec sortBy ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to sorting, assumes not supported
        throw new UnsupportedOperationException();
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getSortSpec()
	 */
	public SortSpec getSortSpec() throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to sorting
		return null;
	}
    
}
