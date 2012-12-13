/*
 *************************************************************************
 * Copyright (c) 2007 <<Your Company Name here>>
 *  
 *************************************************************************
 */

package eclipse.birt.report.data.oda.csv.impl;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.datatools.connectivity.oda.IBlob;
import org.eclipse.datatools.connectivity.oda.IClob;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

import transbit.tbits.search.Result;

import com.ibm.icu.text.DateFormat;


public class ResultSet implements IResultSet
{

    public static final int DEFAULT_MAX_ROWS = 1000;
    private static final int CURSOR_INITIAL_VALUE = -1;
  //  private String[][] sourceData = null;
    private ArrayList<Result> results;
    private IResultSetMetaData resultSetMetaData = null;
    private int maxRows = 0;
    private int cursor = CURSOR_INITIAL_VALUE;

    //Boolean which marks whether it is successful of last call to getXXX();
    private boolean wasNull = false;

    public ResultSet() {
	}

	/**
     * Constructor
     * @param sData a two-dimension array which holds the data extracted from a
     *            csv file.
     * @param rsmd the metadata of sData
     */
    
    
//    ResultSet( String[][] sData, IResultSetMetaData rsmd )
//    {
//        this.sourceData = sData;
//        this.resultSetMetaData = rsmd;
//    }
    
    ResultSet( ArrayList<Result> results, IResultSetMetaData rsmd )throws OdaException
    {
    	System.out.println("ResultSet Constructor");
    	if(results == null)
    		throw new IllegalArgumentException("Results are null");
//    	if(results.size()==0)
//    	{
////    		try{
//    			System.out.println("\nColumnCount"+ rsmd.getColumnCount());
//        		this.sourceData = new String[][]{};
//        		
////        		sourceData = 
//        		maxRows = 1;
////    		}
////    		catch(Throwable t)
////    		{
////    			System.out.println("\nThrowable: " + t);
////    		}
//    		
//    	}
//    	else{
//    		this.sourceData = new String [results.size()][rsmd.getColumnCount()];
//        	int rowIndex = 0;
//        	for(Result result: results)
//        	{
//        		for(int colIndex=0;colIndex<rsmd.getColumnCount();colIndex++)
//            	{
//        			sourceData[rowIndex][colIndex] = result.get(rsmd.getColumnLabel(colIndex+1)).toString();    			
//            	}
//        		rowIndex++;
//        	}
//        	maxRows = rowIndex;
//        	
//    	}
    	this.results = results;
    	
    	//this.sourceData = sData;
        this.resultSetMetaData = rsmd;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getMetaData()
     */
    public IResultSetMetaData getMetaData() throws OdaException
    {
        return this.resultSetMetaData;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#close()
     */
    public void close() throws OdaException
    {
        this.cursor = 0;
      //  this.sourceData = null;
        this.resultSetMetaData = null;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#setMaxRows(int)
     */
    public void setMaxRows( int max ) throws OdaException
    {
        this.maxRows = max;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#next()
     */
    public boolean next() throws OdaException
    {
        if( ( this.maxRows <= 0 ? false : cursor >= this.maxRows - 1 )
                || cursor >= this.results.size() - 1 )
        {
            cursor = CURSOR_INITIAL_VALUE;
            return false;
        }
        cursor++;
        return true;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getRow()
     */
    public int getRow() throws OdaException
    {
        validateCursorState();
        return this.cursor;
    }
    
    private Object getObject (int index) throws OdaException
    {
//    	System.out.println("\nResultSet: getObject1");
    	validateCursorState();
//    	System.out.println("\nResultSet: getObject2");
    	Object result = null;
//    	System.out.println("\nResultSet: getObject3");
    	try{
    		System.out.println("Column: "+resultSetMetaData.getColumnLabel(index));
    		result = results.get(cursor).get(resultSetMetaData.getColumnLabel(index).toString());
//    		System.out.println("\nResultSet: getObject4");
    	}
    	catch (Throwable t)
    	{
    		System.out.println("Throwable: "+t);
    	}
    	if(result == null)
    	{
    		System.out.println("\nResult is null");
    		this.wasNull = true;
    		return result;
    	}
//    	System.out.println("\n\n***"+resultSetMetaData.getColumnLabel(index).toString());
    	System.out.println("Result: "+result.toString());
    	
    	int dataType = resultSetMetaData.getColumnType(index); 
    	if(dataType != DataTypes.STRING)
    	{
    		System.out.println("Type is not string");
    		if(result.toString() == "-")
        		result = null;
    	}
    	
    	System.out.println("\nResultSet: getObject:"+result);
    	this.wasNull = result == null ? true : false;
    	
    	return result;
    	
    }
    
    public Object getObject( String columnName ) throws OdaException
    {
    	System.out.println("\nResultSet: getObject(String column)");
        validateCursorState();
        int columnIndex = findColumn( columnName );
        return getObject( columnIndex );
    }
    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getString(int)
     */
    public String getString( int index ) throws OdaException
    {
//        validateCursorState();
        System.out.println("Index: " + index);
//        String result = (String) results.get(cursor).get(resultSetMetaData.getColumnLabel(index).toString());
//        int length = sourceData[cursor].length;
//        
//        String result = sourceData[cursor][index - 1];
        String result = (String)getObject(index);
//        if( result.length() == 0 )
//            result = null;
//        this.wasNull = result == null ? true : false;
        return result;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getString(java.lang.String)
     */
    public String getString( String columnName ) throws OdaException
    {
//        validateCursorState();
//        int columnIndex = findColumn( columnName );
        return (String)getObject( columnName );
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getInt(int)
     */
    public int getInt( int index ) throws OdaException
    {
    	Integer value = (Integer)getObject(index); 
    	if(value == null)
    		return 0;
    	return value;
//        return stringToInt( getString( index ) );
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getInt(java.lang.String)
     */
    public int getInt( String columnName ) throws OdaException
    {
    	Integer value = (Integer)getObject(columnName);
    	if(value == null)
    		return 0;
    	return value;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDouble(int)
     */
    public double getDouble( int index ) throws OdaException
    {
    	Double value = (Double)getObject(index);
    	if(value == null)
    		return 0;
    	return value;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDouble(java.lang.String)
     */
    public double getDouble( String columnName ) throws OdaException
    {
        Double value = (Double)getObject(columnName);
        if(value == null)
    		return 0;
    	return value;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBigDecimal(int)
     */
    public BigDecimal getBigDecimal( int index ) throws OdaException
    {
        return (BigDecimal)getObject(index);
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBigDecimal(java.lang.String)
     */
    public BigDecimal getBigDecimal( String columnName ) throws OdaException
    {
        return (BigDecimal)getObject(columnName);
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDate(int)
     */
    public Date getDate( int index ) throws OdaException
    {
        return (Date)getObject( index ) ;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDate(java.lang.String)
     */
    public Date getDate( String columnName ) throws OdaException
    {
        return (Date)getObject( columnName ) ;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTime(int)
     */
    public Time getTime( int index ) throws OdaException
    {
        return (Time)getObject( index ) ;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTime(java.lang.String)
     */
    public Time getTime( String columnName ) throws OdaException
    {
    	return (Time)getObject( columnName ) ;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTimestamp(int)
     */
    public Timestamp getTimestamp( int index ) throws OdaException
    {
    	return convertToTimeStamp(getObject(index)) ;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTimestamp(java.lang.String)
     */
    public Timestamp getTimestamp( String columnName ) throws OdaException
    {
        return convertToTimeStamp(getObject(columnName)) ;
    }

    private Timestamp convertToTimeStamp(Object o)
    {
    	if(o==null) return null;
    	transbit.tbits.common.Timestamp tBitsTimeStamp = (transbit.tbits.common.Timestamp)o;
    	return tBitsTimeStamp.toSqlTimestamp();
    }
    
    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBlob(int)
     */
    public IBlob getBlob( int index ) throws OdaException
    {
        throw new UnsupportedOperationException();
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBlob(java.lang.String)
     */
    public IBlob getBlob( String columnName ) throws OdaException
    {
        throw new UnsupportedOperationException();
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getClob(int)
     */
    public IClob getClob( int index ) throws OdaException
    {
        throw new UnsupportedOperationException();
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getClob(java.lang.String)
     */
    public IClob getClob( String columnName ) throws OdaException
    {
        throw new UnsupportedOperationException();
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#wasNull()
     */
    public boolean wasNull() throws OdaException
    {
        return this.wasNull;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#findColumn(java.lang.String)
     */
    public int findColumn( String columnName ) throws OdaException
    {
        String trimmedColumnName = columnName.trim();
        int columnCount = this.getMetaData().getColumnCount();
        for( int i = 1; i <= columnCount; i++ )
        {
            if( trimmedColumnName.equalsIgnoreCase(
                    this.getMetaData().getColumnName( i ) ) )
            {
                return i;
            }
        }
        throw new OdaException( "resultSet_COLUMN_NOT_FOUND"  + columnName ); //$NON-NLS-1$
    }

    /**
     * Validate whether the cursor has been initialized and at a valid row.
     * @throws OdaException if the cursor is not initialized
     */
    private void validateCursorState() throws OdaException
    {
        if( this.cursor < 0 )
            throw new OdaException( "resultSet_CURSOR_HAS_NOT_BEEN_INITIALIZED" ); //$NON-NLS-1$
    }

    /**
     * Transform a String value to an int value
     * @param stringValue String value
     * @return Corresponding int value
     */
//    private int stringToInt( String stringValue )
//    {
//        if( stringValue != null )
//        {
//            try
//            {
//                return new Integer( stringValue ).intValue();
//            }
//            catch( NumberFormatException e )
//            {
//                this.wasNull = true;
//            }
//        }
//        return 0;
//    }

    /**
     * Transform a String value to a double value
     * @param stringValue String value
     * @return Corresponding double value
     */
//    private double stringToDouble( String stringValue )
//    {
//        if( stringValue != null )
//        {
//            try
//            {
//                return new Double( stringValue ).doubleValue();
//            }
//            catch( NumberFormatException e )
//            {
//                this.wasNull = true;
//            }
//        }
//        return 0;
//    }

    /**
     * Transform a String value to a big decimal value
     * @param stringValue String value
     * @return Corresponding BigDecimal value
     */
//    private BigDecimal stringToBigDecimal( String stringValue )
//    {
//        if( stringValue != null )
//        {
//            try
//            {
//                return new BigDecimal( stringValue );
//            }
//            catch( NumberFormatException e )
//            {
//                this.wasNull = true;
//            }
//        }
//        return null;
//    }

    /**
     * Transform a String value to a date value
     * @param stringValue String value
     * @return Corresponding date value
     */

//    private Date stringToDate( String stringValue )
//    {
//        if( stringValue != null )
//        {
//            try
//            {
//                return Date.valueOf( stringValue );
//            }
//            catch( IllegalArgumentException e )
//            {
//                try
//                {
//                    return new Date( stringToLongDate( stringValue ) );
//                }
//                catch( ParseException e1 )
//                {
//                    this.wasNull = true;
//                }
//            }
//        }
//        return null;
//    }

    /**
     * Transform a String value to a Time value
     * @param stringValue String value
     * @return Corresponding Time value
     */
//    private Time stringToTime( String stringValue )
//    {
//        if( stringValue != null )
//        {
//            try
//            {
//                return Time.valueOf( stringValue );
//            }
//            catch( IllegalArgumentException e )
//            {
//                try
//                {
//                    return new Time( stringToLongDate( stringValue ) );
//                }
//                catch( ParseException e1 )
//                {
//                    this.wasNull = true;
//                }
//            }
//        }
//        return null;
//    }

    /**
     * Transform a String value to a Timestamp value
     * @param stringValue String value
     * @return Corresponding Timestamp value
     */
//    private Timestamp stringToTimestamp( String stringValue )
//    {
//        if( stringValue != null )
//        {
//            try
//            {
//                return Timestamp.valueOf( stringValue );
//            }
//            catch( IllegalArgumentException e )
//            {
//                this.wasNull = true;
//            }
//        }
//        return null;
//    }

//    private long stringToLongDate( String stringValue ) throws ParseException
//    {
//        DateFormat dateFormat = null;
//        java.util.Date resultDate = null;
//
//        //For each pattern, we try to format a date for a default Locale
//        //If format fails, we format it for Locale.US
//
//        //Date style is SHORT such as 12.13.52
//        //Time sytle is MEDIUM such as 3:30:32pm
//        try
//        {
//            dateFormat = DateFormat.getDateTimeInstance( DateFormat.SHORT,
//                    DateFormat.MEDIUM );
//            resultDate = dateFormat.parse( stringValue );
//        }
//        catch( ParseException e )
//        {
//            try
//            {
//                dateFormat = DateFormat.getDateTimeInstance( DateFormat.SHORT,
//                        DateFormat.MEDIUM, Locale.US );
//                resultDate = dateFormat.parse( stringValue );
//            }
//            catch( ParseException e1 )
//            {
//            }
//        }
//
//        if( resultDate == null )
//        {
//            //Date style is SHORT such as 12.13.52
//            //Time sytle is SHORT such as 3:30pm
//            try
//            {
//                dateFormat = DateFormat.getDateTimeInstance( DateFormat.SHORT,
//                        DateFormat.SHORT );
//                resultDate = dateFormat.parse( stringValue );
//            }
//            catch( ParseException e )
//            {
//                try
//                {
//                    dateFormat = DateFormat.getDateTimeInstance(
//                            DateFormat.SHORT, DateFormat.SHORT, Locale.US );
//                    resultDate = dateFormat.parse( stringValue );
//                }
//                catch( ParseException e1 )
//                {
//                }
//            }
//        }
//
//        if( resultDate == null )
//        {
//            //No Date style
//            //Time style is short such as 13:05:55
//            try
//            {
//                dateFormat = DateFormat.getTimeInstance( DateFormat.MEDIUM );
//                resultDate = dateFormat.parse( stringValue );
//            }
//            catch( ParseException e )
//            {
//            }
//        }
//
//        if( resultDate == null )
//        {
//            //Date style is SHORT such as 12.13.52
//            //No Time sytle
//            try
//            {
//                dateFormat = DateFormat.getDateInstance( DateFormat.SHORT );
//                resultDate = dateFormat.parse( stringValue );
//            }
//            catch( ParseException e )
//            {
//                dateFormat = DateFormat.getDateInstance( DateFormat.SHORT,
//                        Locale.US );
//                resultDate = dateFormat.parse( stringValue );
//            }
//        }
//
//        return resultDate.getTime();
//    }

	public boolean getBoolean(int index) throws OdaException {
		// TODO Auto-generated method stub
		Boolean value = (Boolean)getObject( index) ; 
		if(value == null)
    		return false;
    	return value;
		//return false;
	}

	public boolean getBoolean(String columnName) throws OdaException {
		// TODO Auto-generated method stub
		Boolean value = (Boolean)getObject( columnName) ; 
		if(value == null)
    		return false;
    	return value;
	}

}