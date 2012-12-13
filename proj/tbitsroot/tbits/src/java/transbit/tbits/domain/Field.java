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
 * Field.java
 *
 * $Header:
 *
 */
package transbit.tbits.domain;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.Helper.HashCodeUtil;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.DataSourcePool;

//Imports from the current package.
//Other TBits Imports.
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.exception.TBitsException;

import static transbit.tbits.Helper.TBitsConstants.ASC_ORDER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;
import static transbit.tbits.api.Mapper.ourFieldListMap;

//Static imports from the mapper.
import static transbit.tbits.api.Mapper.ourFieldMap;

//~--- JDK imports ------------------------------------------------------------

//Java Imports.
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import static java.sql.Types.INTEGER;

//~--- classes ----------------------------------------------------------------

//Third party imports.

/**
 * This class is the domain object corresponding to the fields table
 * in the database.
 *
 * @author  : nitiraj
 * @version : $Id: $
 *
 */
public class Field implements Comparable<Field>, Serializable {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);

    // Static Final Variables to represent the standard fields.
    public static final String BUSINESS_AREA     = "sys_id";
    public static final String REQUEST           = "request_id";
    public static final String ACTION 			 = "action_id";
    public static final String CATEGORY          = "category_id";
    public static final String STATUS            = "status_id";
    public static final String SEVERITY          = "severity_id";
    public static final String REQUEST_TYPE      = "request_type_id";
	/*public static final String SUB_CATEGORY      = "sub_category";*/
    public static final String LOGGER            = "logger_ids";
    public static final String ASSIGNEE          = "assignee_ids";
    public static final String SUBSCRIBER        = "subscriber_ids";
    public static final String TO                = "to_ids";
    public static final String SUBJECT           = "subject";
    public static final String PARENT_REQUEST_ID = "parent_request_id";
    public static final String IS_PRIVATE        = "is_private";
    public static final String DESCRIPTION       = "description";
    public static final String CC                = "cc_ids";
    public static final String USER              = "user_id";
    public static final int    TRACKINGOPTION    = 10;

    // Enum sort of fields for Attributes.
    public static final int    SYSTEMID           = 1;
    public static final String SUMMARY            = "summary";
    public static final String REPLIED_TO_ACTION  = "replied_to_action";
    public static final String RELATED_REQUESTS   = "related_requests";
    public static final int    REGEX              = 12;
    public static final int    PERMISSION         = 11;
    public static final String OFFICE             = "office_id";
    public static final String NOTIFY_LOGGERS     = "notify_loggers";
    public static final String NOTIFY             = "notify";
    public static final int    NAME               = 3;
    public static final String MEMO               = "memo";
    public static final String MAX_ACTION_ID      = "max_action_id";
    public static final String LOGGED_DATE        = "logged_datetime";
    public static final String LASTUPDATED_DATE   = "lastupdated_datetime";
    public static final int    ERROR              = 16;
    public static final int    IS_DEPENDENT       = 13;
    public static final int    ISPRIVATE          = 9;
    public static final int    ISEXTENDED         = 8;
    public static final int    ISACTIVE           = 7;
    public static final String HEADER_DESCRIPTION = "header_description";
    public static final int    FIELDID            = 2;
    public static final String DUE_DATE           = "due_datetime";
    public static final int    DISPLAYNAME        = 4;
    public static final int    DESC               = 5;
    public static final int    DATATYPEID         = 6;
    public static final String ATTACHMENTS        = "attachments";
    public static final String APPEND_INTERFACE   = "append_interface";
    public static final int DISPLAYORDER 	= 14;
    public static final int DISPLAYGROUP 	= 15;
    
    //Tracking options
    public static final int TRACK_NO_DISPLAY = 0;
    public static final int TRACK_CURRENT = 1;
    public static final int TRACK_CURRENT_IFF_NOT_EMPTY = 2;
    public static final int TRACK_CHANGE = 3;
    public static final int TRACK_CHANGE_OR_CURRENT_IF_NO_CHANGE = 4;
    public static final int TRACK_CHANGE_OR_CURRENT_IF_NO_CHANGE_AND_NOT_EMPTY = 5;

    // Static attributes related to sorting.
    private static int ourSortField;
    private static int ourSortOrder;

    //~--- fields -------------------------------------------------------------

    private int     myDataTypeId;
    private String  myDescription;
    private String  myDisplayName;
    private int     myFieldId;
    private boolean myIsActive;
    private boolean myIsDependent;
    private boolean myIsExtended;
    private boolean myIsPrivate;
    private String  myName;
    private int     myPermission;
    private String  myRegex;
    private String  myError;
    private int 	myDisplayOrder = 0;
    private int 	myDisplayGroup = 1;
    
    // Attributes of this Domain Object.
    private int mySystemId;
    private int myTrackingOption;

    //~--- constructors -------------------------------------------------------

    /**
     * The default constructor.
     */
    public Field() {}

 

    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aFieldId
     *  @param aName
     *  @param aDisplayName
     *  @param aDescription
     *  @param aDataTypeId
     *  @param aIsActive
     *  @param aIsExtended
     *  @param aIsPrivate
     *  @param aTrackingOption
     *  @param aPermission
     *  @param aRegex
     *  @param aIsDependent
     *  @param aError
     */
    public Field(int aSystemId, int aFieldId, String aName, String aDisplayName, String aDescription, int aDataTypeId, boolean aIsActive, boolean aIsExtended, boolean aIsPrivate, int aTrackingOption,
                 int aPermission, String aRegex, boolean aIsDependent,String aError) {
        mySystemId       = aSystemId;
        myFieldId        = aFieldId;
        myName           = aName;
        myDisplayName    = aDisplayName;
        myDescription    = aDescription;
        myDataTypeId     = aDataTypeId;
        myIsActive       = aIsActive;
        myIsExtended     = aIsExtended;
        myIsPrivate      = aIsPrivate;
        myTrackingOption = aTrackingOption;
        myPermission     = aPermission;
        myRegex          = aRegex;
        myIsDependent    = aIsDependent;
        myError          = aError;
    }

    public Field(int aSystemId, int aFieldId, String aName,
			String aDisplayName, String aDescription, int aDataTypeId,
			boolean aIsActive, boolean aIsExtended, boolean aIsPrivate,
			int aTrackingOption, int aPermission, String aRegex,
			boolean aIsDependent, int aDisplayOrder, String aError) {
		this(aSystemId, aFieldId, aName, aDisplayName, aDescription,
				aDataTypeId, aIsActive, aIsExtended, aIsPrivate,
				aTrackingOption, aPermission, aRegex, aIsDependent,aError);
		myDisplayOrder = aDisplayOrder;
	}
    
    public Field(int aSystemId, int aFieldId, String aName,
			String aDisplayName, String aDescription, int aDataTypeId,
			boolean aIsActive, boolean aIsExtended, boolean aIsPrivate,
			int aTrackingOption, int aPermission, String aRegex,
			boolean aIsDependent, int aDisplayOrder, int aDisplayGroup,String aError) {
		this(aSystemId, aFieldId, aName, aDisplayName, aDescription,
				aDataTypeId, aIsActive, aIsExtended, aIsPrivate,
				aTrackingOption, aPermission, aRegex, aIsDependent, aDisplayOrder,aError );
		myDisplayGroup = aDisplayGroup;
	}
    /**
     * The complete constructor.
     *
     *  @param aSystemId
     *  @param aFieldId
     *  @param aName
     *  @param aDisplayName
     *  @param aDescription
     *  @param aDataTypeId
     *  @param aIsActive
     *  @param aIsExtended
     *  @param aIsPrivate
     *  @param aTrackingOption
     *  @param aPermission
     *  @param aRegex
     *  @param aIsDependent
     */
    public Field(int aSystemId, int aFieldId, String aName, String aDisplayName, String aDescription, int aDataTypeId, boolean aIsActive, boolean aIsExtended, boolean aIsPrivate, int aTrackingOption,
                 int aPermission, String aRegex, boolean aIsDependent) {
        mySystemId       = aSystemId;
        myFieldId        = aFieldId;
        myName           = aName;
        myDisplayName    = aDisplayName;
        myDescription    = aDescription;
        myDataTypeId     = aDataTypeId;
        myIsActive       = aIsActive;
        myIsExtended     = aIsExtended;
        myIsPrivate      = aIsPrivate;
        myTrackingOption = aTrackingOption;
        myPermission     = aPermission;
        myRegex          = aRegex;
        myIsDependent    = aIsDependent;
     
    }

    public Field(int aSystemId, int aFieldId, String aName,
			String aDisplayName, String aDescription, int aDataTypeId,
			boolean aIsActive, boolean aIsExtended, boolean aIsPrivate,
			int aTrackingOption, int aPermission, String aRegex,
			boolean aIsDependent, int aDisplayOrder) {
		this(aSystemId, aFieldId, aName, aDisplayName, aDescription,
				aDataTypeId, aIsActive, aIsExtended, aIsPrivate,
				aTrackingOption, aPermission, aRegex, aIsDependent);
		myDisplayOrder = aDisplayOrder;
	}
    
    public Field(int aSystemId, int aFieldId, String aName,
			String aDisplayName, String aDescription, int aDataTypeId,
			boolean aIsActive, boolean aIsExtended, boolean aIsPrivate,
			int aTrackingOption, int aPermission, String aRegex,
			boolean aIsDependent, int aDisplayOrder, int aDisplayGroup) {
		this(aSystemId, aFieldId, aName, aDisplayName, aDescription,
				aDataTypeId, aIsActive, aIsExtended, aIsPrivate,
				aTrackingOption, aPermission, aRegex, aIsDependent, aDisplayOrder);
		myDisplayGroup = aDisplayGroup;
	}
    
    //~--- methods ------------------------------------------------------------

    /**
    * getter method for checking if the set option is enabled for the field
    * @return true if enabled else false
    */
   public boolean getIsSetEnabled()
   {
   	return ( (myPermission & Permission.SET) != 0 ) ; 
   }
   
   /**
    * getter method for checking if the Display option is enabled for the field
    * @return true if enabled else false
    */
   public boolean getIsDisplayEnabled()
   {
   	return ((myPermission & Permission.DISPLAY ) != 0 ) ;
   }
   
   /**
    * getter method for checking if the D-Action option is enabled for the field
    * @return true if enabled else false 
    */
   public boolean getIsDActionEnabled()
   {
   	return ( (myPermission & Permission.D_ACTION) != 0 ) ;
   }
   
   /**
    * getter method for checking if the Search option is enabled for the field
    * @return true if enabled else false
    */
   public boolean getIsSearchEnabled()
   {
   	return ((myPermission & Permission.SEARCH ) != 0 ) ;
   }
   
   /**
    * getter method for checking if the HyperLink option is enabled for the field
    * @return true if enabled else false
    */
   public boolean getIsHyperLinkEnabled()
   {
   	return ((myPermission & Permission.HYPERLINK) != 0 ) ;
   }
    
    /**
     * Method that compares this object with the one passed W.R.T the
     * ourSortField.
     *
     * @param aObject  Object to be compared.
     *
     * @return 0 - If they are equal.
     *         1 - If this is greater.
     *        -1 - If this is smaller.
     *
     */
    public int compareTo(Field aObject) {
        switch (ourSortField) {
        case SYSTEMID : {
            Integer i1 = mySystemId;
            Integer i2 = aObject.mySystemId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case FIELDID : {
            Integer i1 = myFieldId;
            Integer i2 = aObject.myFieldId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case NAME : {
            if (ourSortOrder == ASC_ORDER) {
                return myName.compareToIgnoreCase(aObject.myName);
            }

            return aObject.myName.compareToIgnoreCase(myName);
        }

        case DISPLAYNAME : {
            if (ourSortOrder == ASC_ORDER) {
                return myDisplayName.compareToIgnoreCase(aObject.myDisplayName);
            }

            return aObject.myDisplayName.compareToIgnoreCase(myDisplayName);
        }

        case DESC : {
            if (ourSortOrder == ASC_ORDER) {
                return myDescription.compareToIgnoreCase(aObject.myDescription);
            }

            return aObject.myDescription.compareToIgnoreCase(myDescription);
        }

        case DATATYPEID : {
            Integer i1 = myDataTypeId;
            Integer i2 = aObject.myDataTypeId;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case ISACTIVE : {
            Boolean b1 = myIsActive;
            Boolean b2 = aObject.myIsActive;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }

        case ISEXTENDED : {
            Boolean b1 = myIsExtended;
            Boolean b2 = aObject.myIsExtended;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }

        case ISPRIVATE : {
            Boolean b1 = myIsPrivate;
            Boolean b2 = aObject.myIsPrivate;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }

        case TRACKINGOPTION : {
            Integer i1 = myTrackingOption;
            Integer i2 = aObject.myTrackingOption;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case PERMISSION : {
            Integer i1 = myPermission;
            Integer i2 = aObject.myPermission;

            if (ourSortOrder == ASC_ORDER) {
                return i1.compareTo(i2);
            }

            return i2.compareTo(i1);
        }

        case REGEX : {
            if (ourSortOrder == ASC_ORDER) {
                return myRegex.compareTo(aObject.myRegex);
            }

            return aObject.myRegex.compareTo(myRegex);
        }
       

        case IS_DEPENDENT : {
            Boolean b1 = myIsDependent;
            Boolean b2 = aObject.myIsDependent;

            if (ourSortOrder == ASC_ORDER) {
                return b1.compareTo(b2);
            }

            return b2.compareTo(b1);
        }
        case DISPLAYORDER: {
        	Integer do1 = myDisplayOrder;
        	Integer do2 = aObject.myDisplayOrder;
        	
        	if(ourSortOrder == ASC_ORDER)
        	{
        		return do1.compareTo(do2);
        	}
        	return do2.compareTo(do1);
        }
        case DISPLAYGROUP: {
        	Integer dg1 = myDisplayGroup;
        	Integer dg2 = aObject.myDisplayGroup;
        	
        	if(ourSortOrder == ASC_ORDER)
        	{
        		return dg1.compareTo(dg2);
        	}
        	return dg2.compareTo(dg1);
        }
        case ERROR : {
            if (ourSortOrder == ASC_ORDER) {
                return myError.compareTo(aObject.myError);
            }

            return aObject.myError.compareTo(myError);
        }
        }
        return 0;
    }

    /**
     * This method is used to create the Field object from the ResultSet
     *
     * @param  aResultSet the result object containing the fields
     * corresponding to a row of the Field table in the database
     * @return the corresponding Field object created from the ResutlSet
     */
    public static Field createFromResultSet(ResultSet aResultSet) throws SQLException {
        Field field = new Field(aResultSet.getInt("sys_id"), aResultSet.getInt("field_id"), aResultSet.getString("name"), aResultSet.getString("display_name"), aResultSet.getString("description"),
                                aResultSet.getInt("data_type_id"), aResultSet.getBoolean("is_active"), aResultSet.getBoolean("is_extended"), aResultSet.getBoolean("is_private"),
                                aResultSet.getInt("tracking_option"), aResultSet.getInt("permission"), aResultSet.getString("regex"), aResultSet.getBoolean("is_dependent"), 
                                aResultSet.getInt("display_order"), aResultSet.getInt("display_group"),aResultSet.getString("error"));

        return field;
    }

    /**
     * 
     * @param field
     * @param con
     * @return field object if every thing goes fine. else throws following exception.
     * @throws SQLException : unexpected sql exception
     * @throws DatabaseException : exception concerning the developers
     * @throws TBitsException : exception that can be shown to user.
     */
    public static Field delete(Field field, Connection con) throws SQLException,DatabaseException, TBitsException
    {    	
    	if( null == con || con.isClosed() == true )
    	{
    		LOG.info("The connection object supplied was null or closed.");
    		throw new DatabaseException("The connection object supplied was null or closed.", new SQLException());
    	}
    	
    	if( con.getAutoCommit() == true )
    	{
    		LOG.info("Cannot accept a connection with auto commit to true. As this calls other stored procedures too." );
    		throw new DatabaseException("Cannot accept a connection with auto commit to true. As this calls other stored procedures too.", new SQLException());
    	}
    	
    	if( null == field ) 
    	{
    		LOG.info("The supplied field was null.");    		
    		throw new TBitsException("The supplied field was null.");
    	}   	
    	
    	int returnValue = 0 ; 
    	 CallableStatement cs = con.prepareCall("stp_field_delete " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?,?, " + "?, ?, ?, ?,?,? ");

         field.setCallableParameters(cs);
         cs.registerOutParameter(17, INTEGER);
         cs.execute();
         returnValue = cs.getInt(17);
         cs.close();
         
         if(returnValue == 1)
         {
        	 if( field.getDataTypeId() == DataType.USERTYPE )
         	{    		
         		// delete the field.
         		// delete the associated-role         		
         			Role r = Role.lookupBySystemIdAndRoleName(field.getSystemId(), field.getName()) ;
         			if( null != r )
         			{
         				if( r.getFieldId() != field.getFieldId() )
         				{
         					LOG.severe("The found role : " + r + " does not corresponds to the field : " + field ) ;
         					throw new TBitsException("Exception occured while deleting the role corresponding to the field " + field.getName());
         				}
         				try
         				{	// get the role associated with this field.
         					// set its can be deleted to 1. so that it can be deleted even if it is marked
         					// as can_be_deleted to false in the db.
         					Role role = new Role(r);
         					role.setCanBeDeleted(1);
	             			Role roleDeleted = Role.delete(role, con);             			
	             			// if everything is fine then say that the field was deleted.
	             			LOG.info("Both field and associated Role was successfully deleted for field : " + field.getName() ) ;
	             	        return field ;
         				}
         				catch(Exception e)
         				{
         					LOG.info("Deletion of field failed because the deletion of associate role failed for the field: " + field ) ;
         					throw new TBitsException("Deletion of field failed because the deletion of associate role failed for the field: " + field ) ;
         				}         		
         			}
         			else
         			{
         				// the associate role was not found.
         				LOG.info("The associated Role was not found. So deleting only the field.");
         				return field;
         			}
         	}
	    	else
	    	 {
	    		LOG.info("Field was successfully deleted for field : " + field ) ;
	    		 return field;
	    	 }
         }
         else
         {
        	 LOG.info("Field deletion failed for field : " + field);
        	 throw new TBitsException("Field deletion failed for field : " + field.getName()) ;
         }      
    }
    
    /**
     * Method to delete the corresponding Field object in the database.
     *
     * @param aObject Object to be updated
     *
     * @return Delete domain object.
     * @throws TBitsException 
     *
     */
    public static Field delete(Field aObject) throws DatabaseException, TBitsException {

        // Update logic here.
        if (aObject == null) {
            throw new TBitsException("The supplied object was null.");
        }

        Connection con        = null;
        Field f = null ;
        try {
            con = DataSourcePool.getConnection();
            con.setAutoCommit(false);
            f = delete(aObject,con);
            con.commit() ;
        } catch (SQLException sqle) {
        	try {
				con.rollback();
			} catch (SQLException e) {				
				StringBuilder message = new StringBuilder();
	            message.append("An exception occured while deleting the field.").append("\n");
	            LOG.info("",(e));
	            throw new DatabaseException(message.toString(), sqle);			
			}
        	
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while deleting the field.").append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException sqle) {
                	LOG.info("",(sqle));	
                    // Should this be logged.?
                }
            }
        }

       return f;
    }

    public int hashCode()
    {
    	int result = HashCodeUtil.SEED ;
    	result = HashCodeUtil.hash(result, this.getSystemId());
    	result = HashCodeUtil.hash(result, this.getFieldId()) ;
    	
    	return result ;
    }
    
    /**
     * This method is used to compare two Field objects.
     *
     * @param aField Field object.
     *
     * @return True if equal and False otherwise.
     *
     */
    public boolean equals(Field aField) {
        if (aField == null) {
            return false;
        }

        return ((mySystemId == aField.mySystemId) && (myFieldId == aField.myFieldId));
    }

    /**
     * This method is used to compare two Field objects.
     *
     * @param o Field object.
     *
     * @return True if equal and False otherwise.
     *
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        Field aField = null;

        try {
            aField = (Field) o;
        } catch (ClassCastException cce) {
            return false;
        }

        return this.equals(aField);
    }

    /**
     * Method to insert a Field object into database.     
     * @param field -- the field object to be inserted into the database. the value of field_id is ignored
     * @param con : the database connection 
     * @return if successfule returns the Field object with its field_id set. otherwise returns null. 
     * @throws SQLException : unexpected exception 
     * @throws TBitsException : exception that has to be shown to the user
     * @throws DatabaseException : the exception concerning the developers.
     */
    public static Field insert(Field field, Connection con) throws SQLException, TBitsException, DatabaseException
    {
    	if( null == con || con.isClosed() == true )
    	{
    		LOG.info("The supplied connection object was null or was closed.");
    		throw new DatabaseException("The supplied connection object was null or was closed.", new SQLException()) ;
    	}
    	
    	if( con.getAutoCommit() == true )
    	{
    		LOG.info("Cannot accept a connection with auto commit to true. As this calls other stored procedures too." );
    		throw new DatabaseException("Cannot accept a connection with auto commit to true. As this calls other stored procedures too.", new SQLException()) ;
    	}
    	
    	if( null == field )
    	{
    		LOG.info("The supplied field object was null.");
    		throw new DatabaseException("The supplied field object was null.", new SQLException()) ;
    	}  	
    	
        CallableStatement cs = con.prepareCall("stp_field_insert " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?," + "?, ?, ?,?,? ");

        field.setCallableParameters(cs);
        ResultSet rs = cs.executeQuery();
        if( null != rs && rs.next() == true )
        {
        		int fieldId = 0 ;
        		fieldId = rs.getInt("field_id");
        		field.setFieldId(fieldId);
        		LOG.info("Field was inserted properly and the returned fieldId was : " + fieldId ) ;
        		if( fieldId != 0 )
        		{
        			if( field.getDataTypeId() == DataType.USERTYPE )
        	        {            	
        				try
        				{
	        	    		Role role = Role.insert(field.getSystemId(), field.getName(), "This role was automatically created in response to the creation of UserTypeField called " + field.getName(), field.getFieldId(), 0, con) ;  		
	        	    		LOG.info("Both Field and role were inserted properly.");
	    	    			LOG.info("FieldId : " + fieldId);
	    	    			LOG.info("Role : " + role) ;            			
        				}
        				catch(Exception e)
        				{
        					throw new TBitsException("The creation of role with name " + field.getName() + " failed. So UserType Field creation also failed.") ;
            	    	}        	    		
        	        }       			
        		}
        		else
        		{
        			throw new TBitsException("Field creation failed.") ;
        		} 
    	}
    	else
		{
			throw new TBitsException("Field creation failed.") ;
		}   
    
        cs.close();
        
        return field  ;        
    }
    /**
     * Method to insert a Field object into database.
     *
     * @param aObject Object to be inserted
     * @return if successfule eturns the field with its field_id inserted else return null.
     * @throws DatabaseException 
     * @throws TBitsException    
     */
    public static Field insert(Field aObject) throws TBitsException, DatabaseException{

        // Insert logic here.
        if (aObject == null) {
            throw new TBitsException("The field supplied was null");
        }

        Connection aCon        = null;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);
            Field newField = insert(aObject,aCon);
            
            aCon.commit();
            return newField;
        } catch (SQLException sqle) {
        	try {
        		if(aCon != null)
					aCon.rollback();
			} catch (SQLException e) {				
				e.printStackTrace();
			}
            StringBuilder message = new StringBuilder();
            sqle.printStackTrace();
            message.append("An exception occured while inserting the field.").append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {

                    // Should this be logged.?
                }
            }
        }
    }
    
    /**
     * Method to insert a Field object into database.
     *
     * @param aObject Object to be inserted
     */
    public static boolean insertWithExistingFieldId(Field aObject) throws DatabaseException {

        // Insert logic here.
        if (aObject == null) {
            return false;
        }

        Connection aCon        = null;
        boolean    returnValue = false;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_field_insertWithExistingFieldId " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?,?, " + "?, ?, ?,?,? ");

            aObject.setCallableParameters(cs);
            cs.execute();
            cs.close();
            returnValue = true;
            aCon.commit();
        } catch (SQLException sqle) {
        	
        	try {
        		if(aCon != null)
					aCon.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
            StringBuilder message = new StringBuilder();
            sqle.printStackTrace();
            message.append("An exception occured while inserting the field.").append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {

                    // Should this be logged.?
                }
            }
        }

        return returnValue;
    }
    
    public static List<Field> lookupActiveBySystemId(int aSystemId) throws DatabaseException {
    	List<Field> filteredFields = new ArrayList<Field>();
    	
    	List<Field> allFields = lookupBySystemId(aSystemId);
    	if(allFields == null)
    		return null;
    	
    	for(Field f:allFields)
    	{
    		if(f.getIsActive())
    			filteredFields.add(f);
    	}
    	return filteredFields;
    }
    
    
    /**
     * This method returns the List of Field objects corresponding to the given
     * SystemId.
     *
     * @param aSystemId  BusinessArea Id.
     *
     * @return List of Field objects.
     *
     * @exception DatabaseException incase of any database error.
     */
    public static ArrayList<Field> lookupBySystemId(int aSystemId) throws DatabaseException {
        ArrayList<Field> fieldList = null;

        // Look in the mapper first.
        if (ourFieldListMap != null) {
            int key = aSystemId;
           
            fieldList = ourFieldListMap.get(key);

            if (fieldList != null) {
                Field.setSortParams(DISPLAYNAME, 0);
                fieldList = Field.sort(fieldList);

                return fieldList;
            } else {
                return new ArrayList<Field>();
            }
        }

        // else try to get the Field record from the database.
        fieldList = new ArrayList<Field>();

        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_field_lookupBySystemId ?");

            cs.setInt(1, aSystemId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    Field field = createFromResultSet(rs);

                    fieldList.add(field);
                }

                // Close the result set.
                rs.close();
            }

            // Close the statement.
            cs.close();

            //
            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            //
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the fields.").append("\nSystem Id: ").append(aSystemId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warn("Exception while closing the connection:", sqle);
                }

                connection = null;
            }
        }

        return fieldList;
    }

    /**
     * 
     * @param aSystemId
     * @param dataType
     * @return the array of fields which have the dataTypeId as the supplied dataType
     * @throws DatabaseException
     */
    public static ArrayList<Field> lookupBySystemId(int aSystemId,int dataType) throws DatabaseException 
    {
        ArrayList<Field> fieldList = new ArrayList<Field>();
        // Look in the mapper first.

            ArrayList<Field>fl = lookupBySystemId(aSystemId);
            if( null != fl )
            	fieldList.addAll(fl);
     
                for( Iterator<Field> iter = fieldList.iterator() ; iter.hasNext() ;)
                {
                	Field field = iter.next() ;
                	if( field.getDataTypeId() != dataType )
                		iter.remove() ;
                }
                return fieldList;    
	}

    public static ArrayList<Field> lookupBySystemId(int systemId, boolean isExtended) throws DatabaseException
    {
    	ArrayList<Field> fieldList = new ArrayList<Field>() ;
    	ArrayList<Field> fl = lookupBySystemId(systemId) ;
    	
    	if( null != fl )
    		fieldList.addAll(fl);
    	
    	for( Iterator<Field> iter = fieldList.iterator() ; iter.hasNext() ; )
    	{
    		Field field = iter.next() ;
    		if( field.getIsExtended() != isExtended )
    			iter.remove() ;    		
    	}
    	
    	return fieldList ;
    }
    public static ArrayList<Field> lookupBySystemId(int aSystemId,boolean isExtended, int dataType) throws DatabaseException 
    {
        ArrayList<Field> fieldList = new ArrayList<Field>();
        ArrayList<Field>fl = lookupBySystemId(aSystemId);
        if( null != fl )
        	fieldList.addAll(fl);
             
        for( Iterator<Field> iter = fieldList.iterator() ; iter.hasNext() ;)
        {
        	Field field = iter.next() ;
        	if( field.getDataTypeId() != dataType || field.getIsExtended() != isExtended)
        		iter.remove() ;
        }
        return fieldList;    
	}

    
    /**
     * This method returns the Field object corresponding to the given
     * SystemId and FieldId.
     *
     * @param aSystemId  BusinessArea Id.
     * @param aFieldId   Field Id.
     *
     * @return Field object.
     *
     * @exception DatabaseException incase of any database error.
     */
    public static Field lookupBySystemIdAndFieldId(int aSystemId, int aFieldId) throws DatabaseException {
        Field field = null;

        // Look in the mapper first.
        if (ourFieldMap != null) {
            String key = aSystemId + "-" + aFieldId;

            field = ourFieldMap.get(key);

            return field;
        }

        // else try to get the Field record from the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_field_lookupBySystemIdAndFieldId ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aFieldId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    field = createFromResultSet(rs);
                }

                // Close the result set.
                rs.close();
            }

            // Close the statement.
            cs.close();

            //
            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            //
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the field.").append("\nSystem Id: ").append(aSystemId).append("\nField Id : ").append(aFieldId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warn("Exception while closing the connection:", sqle);
                }

                connection = null;
            }
        }

        return field;
    }

    /**
     * This method returns the Field object corresponding to the given
     * SystemId and FieldName.
     *
     * @param aSystemId  BusinessArea Id.
     * @param aFieldName   Field Name.
     *
     * @return Field object.
     *
     * @exception DatabaseException incase of any database error.
     */
    public static Field lookupBySystemIdAndFieldName(int aSystemId, String aFieldName) throws DatabaseException {
        Field field = null;

        // Look in the mapper first.
        if (ourFieldMap != null) {
            String key = aSystemId + "-" + aFieldName.toUpperCase();
            field = ourFieldMap.get(key);

            return field;
        }

        // else try to get the Field record from the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_field_lookupBySystemIdAndFieldName ?, ?");

            cs.setInt(1, aSystemId);
            cs.setString(2, aFieldName);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                if (rs.next() != false) {
                    field = createFromResultSet(rs);
                }

                // Close the result set.
                rs.close();
            }

            // Close the statement.
            cs.close();

            //
            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            //
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the field.").append("\nSystem Id : ").append(aSystemId).append("\nField Name: ").append(aFieldName).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warn("Exception while closing the connection:", sqle);
                }

                connection = null;
            }
        }

        return field;
    }

    /**
     * This method returns the Field object minimal and uniquely matching to the given
     * SystemId and FieldName.
     *
     * @param aSystemId  BusinessArea Id.
     * @param aName   Field Name.
     *
     * @return Field object.
     *
     * @exception DatabaseException incase of any database error.
     */
    public static Field lookupBySystemIdAndMinimalMatch(int aSystemId, String aName) throws DatabaseException {
        aName = aName.toLowerCase();

        Field                    field         = null;
        int                      i             = 0;
        ArrayList<Field>         matchedFields = new ArrayList<Field>();
        Hashtable<String, Field> fieldsTable   = FieldDescriptor.getDescriptorTable(aSystemId);
        Enumeration<String>      keys          = fieldsTable.keys();

        while (keys.hasMoreElements()) {
            if (i > 1) {
                return null;
            }

            String str = keys.nextElement().toLowerCase();

            if (str.startsWith(aName) == true) {
                field = fieldsTable.get(str);

                if (matchedFields.contains(field) == false) {
                    i++;
                    matchedFields.add(field);
                }
            }
        }

        return field;
    }

    public static void main(String args[]) throws Exception {
        Field field = Field.lookupBySystemIdAndFieldId(1, 3);

        if (field != null) {
            System.out.println("\nname: " + field.getName() + "\nid: " + field.getFieldId() + "\ndisplayName: " + field.getDisplayName() + "\ndescription: " + field.getDescription() + "\ndtaType: "
                               + field.getDataTypeId() + "\nactive: " + field.getIsActive() + "\nextended: " + field.getIsExtended() + "\nprivate: " + field.getIsPrivate() + "\ntracking: "
                               + field.getTrackingOption() + "\npermission: " + field.getPermission() + "\nregex: " + field.getRegex() + "\nDLevel: " + field.getIsDependent()+ "\nerror: " + field.getError() );
            Field.update(field);
        }
    }

    /**
     * Method to return the source arraylist in the sorted order
     *
     * @param  source the array list of Type objects
     * @return the ArrayList of the Field objects in sorted order
     */
    public static ArrayList<Field> sort(ArrayList<Field> source) {
        int     size     = source.size();
        Field[] srcArray = new Field[size];

        for (int i = 0; i < size; i++) {
            srcArray[i] = source.get(i);
        }

        Arrays.sort(srcArray);

        ArrayList<Field> target = new ArrayList<Field>();

        for (int i = 0; i < size; i++) {
            target.add(srcArray[i]);
        }

        return target;
    }

    /**
     * This method returns the string representation of the field object.
     *
     * @return String
     */
    public String toString() {
        String message =
        		"[ "
        		+ "Id: " + myFieldId + ", "
        		+ "Type: " + myDataTypeId + ", " 
        		+ "Description: " + myDescription + ", "
        		+ "DisplayName: " + myDisplayName + ", "
        		+ "IsActive: " + myIsActive + ", "
        		+ "IsDependent: " + myIsDependent + ", "
        		+ "IsExtended: " + myIsExtended + ", " 
        		+ "IsPrivate: " + myIsPrivate + ", "
        		+ "Name: " + myName + ", "
        		+ "Permission: " + myPermission + ", "
        		+ "Regex: " + myRegex + ", "
        		+ "DisplayOrder: " + myDisplayOrder + ", "
        		+ "DisplayGroup: " + myDisplayGroup + ", "
        		+ "Error: " + myError + ", "
        		+ "SystemId: " + mySystemId + ", "
        		+ "TrackingOption: "+ myTrackingOption 
        		+ " ]";
        return message;
    }

    /**
     * Method to update the corresponding Field object in the database.
     *
     * @param aObject Object to be updated
     *
     * @return Update domain object.
     *
     */
    public static Field update(Field aObject) throws DatabaseException {

        // Update logic here.
        if (aObject == null) {
            return aObject;
        }

        Connection aCon = null;

        try {
            aCon = DataSourcePool.getConnection();
            aCon.setAutoCommit(false);

            CallableStatement cs = aCon.prepareCall("stp_field_update " + "?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?," + "?, ?, ?, ?,? ");

            aObject.setCallableParameters(cs);
            cs.execute();
            cs.close();
            aCon.commit();
        } catch (SQLException sqle) {
        	
        	try {
        		if(aCon != null)
					aCon.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while updating the field.").append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {

                    // Should this be logged.?
                }
            }
        }

        return aObject;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Accessor method for DataTypeId property.
     *
     * @return Current Value of DataTypeId
     *
     */
    public int getDataTypeId() {
        return myDataTypeId;
    }

    /**
     * Accessor method for Description property.
     *
     * @return Current Value of Description
     *
     */
    public String getDescription() {
        return myDescription;
    }

    /**
     * Accessor method for DisplayName property.
     *
     * @return Current Value of DisplayName
     *
     */
    public String getDisplayName() {
        return myDisplayName;
    }

    /**
     * This method returns the Hashtable containing the (datatype,ArrayList
     * of all the corresponding extended fields) pairs for a given SystemId.
     *
     * @param aSystemId  BusinessArea Id.
     *
     * @return List of Field objects.
     *
     * @exception DatabaseException incase of any database error.
     */
    public static Hashtable<String, ArrayList<Field>> getExtendedFields(int aSystemId) throws DatabaseException {
        Hashtable<String, ArrayList<Field>> etable   = new Hashtable<String, ArrayList<Field>>();
        ArrayList<Field>                    eBoolean = new ArrayList<Field>();
        ArrayList<Field>                    eString  = new ArrayList<Field>();
        ArrayList<Field>                    eReal    = new ArrayList<Field>();
        ArrayList<Field>                    eInt     = new ArrayList<Field>();
        ArrayList<Field>                    eTypes   = new ArrayList<Field>();
        ArrayList<Field>                    eDates   = new ArrayList<Field>();
        ArrayList<Field>                    eText    = new ArrayList<Field>();
        ArrayList<Field>                    eUserType= new ArrayList<Field>();
        
        
        // String key = ""+aSystemId;
        // ArrayList<Field> fieldList =
        // (ArrayList<Field>) ourFieldListMap.get(key);
        ArrayList<Field> fieldList = Field.lookupBySystemId(aSystemId);

        if (fieldList == null) {
            return etable;
        }

        Field field = null;
        int   i;

        for (i = 0; i < fieldList.size(); i++) {
            field = (Field) fieldList.get(i);
            
            // Skip the inactive fields.
            if (field.getIsActive() == false) {
                continue;
            }

            // Skip the fixed fields
            if (field.getIsExtended() == false) {
                continue;
            }

            if (field.getDataTypeId() == DataType.BOOLEAN) {
                eBoolean.add(field);
            }

            // insert string fields in to eString.
            if (field.getDataTypeId() == DataType.TYPE) {
                eTypes.add(field);
            }

            if ((field.getDataTypeId() == DataType.DATE) || (field.getDataTypeId() == DataType.TIME) || (field.getDataTypeId() == DataType.DATETIME)) {
                eDates.add(field);
            }

            if (field.getDataTypeId() == DataType.STRING) {
                eString.add(field);
            }

            if (field.getDataTypeId() == DataType.REAL) {
                eReal.add(field);
            }

            if (field.getDataTypeId() == DataType.INT) {
                eInt.add(field);
            }

            if (field.getDataTypeId() == DataType.TEXT) {
                eText.add(field);
            }
            if(field.getDataTypeId() == DataType.USERTYPE){
            	eUserType.add(field);
            }
           }
            
          

        etable.put("__String__", eString);	
        etable.put("__Type__", eTypes);		
        etable.put("__Datetime__", eDates);
        etable.put("__Boolean__", eBoolean);	
        etable.put("__Int__", eInt);
        etable.put("__Real__", eReal);
        etable.put("__Text__", eText);
        etable.put("__MultiValue__",eUserType );

        return etable;
    }

    /**
     * This method returns the List of Extended Field objects corresponding
     * to the given SystemId.
     *
     * @param aSystemId  BusinessArea Id.
     *
     * @return List of Field objects.
     *
     * @exception DatabaseException incase of any database error.
     */
    public static ArrayList<Field> getExtendedFieldsBySystemId(int aSystemId) throws DatabaseException {
        ArrayList<Field> fieldList = new ArrayList<Field>();

        // Look in the mapper first.
        if (ourFieldListMap != null) {

            // ArrayList<Field> tempList = ourFieldListMap.get(key);
            ArrayList<Field> tempList = Field.lookupBySystemId(aSystemId);

            if (tempList != null) {
                for (Field field : tempList) {
                    if ((field.getIsExtended() == true) && (field.getIsActive() == true)) {
                        fieldList.add(field);
                    }
                }
            }
            Field.setSortParams(Field.DISPLAYORDER, TBitsConstants.ASC_ORDER);
            return Field.sort(fieldList);
        }

        // else try to get the List of extended fields from the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_field_getExtendedFieldsBySystemId ?");

            cs.setInt(1, aSystemId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    Field field = createFromResultSet(rs);

                    fieldList.add(field);
                }

                // Close the result set.
                rs.close();
            }

            // Close the statement.
            cs.close();

            //
            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            //
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the ").append("extended fields.").append("\nSystem Id: ").append(aSystemId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warn("Exception while closing the connection:", sqle);
                }

                connection = null;
            }
        }
        Field.setSortParams(Field.DISPLAYORDER, TBitsConstants.ASC_ORDER);
        return Field.sort(fieldList);
    }

    /**
     * Accessor method for FieldId property.
     *
     * @return Current Value of FieldId
     *
     */
    public int getFieldId() {
        return myFieldId;
    }

    /**
     * This method returns the List of Field objects corresponding to the
     * given SystemId and User Id.
     *
     * @param aSystemId  BusinessArea Id.
     * @param aUserId    User Id.
     *
     * @return List of Field objects.
     *
     * @exception DatabaseException incase of any database error.
     */
    public static ArrayList<Field> getFieldsBySystemIdAndUserId(int aSystemId, int aUserId) throws DatabaseException {
        ArrayList<Field> fieldList = new ArrayList<Field>();

        // This should be a database call as it involves some permissioning
        // related issues.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_field_getFieldsBySystemIdAndUserId ?, ?");

            cs.setInt(1, aSystemId);
            cs.setInt(2, aUserId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    Field field = createFromResultSet(rs);

                    fieldList.add(field);
                }

                // Close the result set.
                rs.close();
            }

            // Close the statement.
            cs.close();

            //
            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            //
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the fields.").append("\nSystem Id: ").append(aSystemId).append("\nUser Id  : ").append(aUserId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warn("Exception while closing the connection:", sqle);
                }

                connection = null;
            }
        }

        return fieldList;
    }

    /**
     * This method returns the table of (fieldname, Field object) corresponding
     * to the given SystemId.
     *
     * @param aSystemId  BusinessArea Id.
     *
     * @return List of Field objects.
     *
     * @exception DatabaseException incase of any database error.
     */
    public static Hashtable<String, Field> getFieldsTableBySystemId(int aSystemId) throws DatabaseException {
        Hashtable<String, Field> fieldTable = new Hashtable<String, Field>();

        // Look in the mapper first.
        if (ourFieldListMap != null) {
            int              key       = aSystemId;
            ArrayList<Field> fieldList = ourFieldListMap.get(key);

            if (fieldList != null) {
                for (Field field : fieldList) {
                    fieldTable.put(field.getName(), field);
                }
            }

            return fieldTable;
        }

        // else try to get the Field record from the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_field_lookupBySystemId ?");

            cs.setInt(1, aSystemId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    Field field = createFromResultSet(rs);

                    fieldTable.put(field.getName(), field);
                }

                // Close the result set.
                rs.close();
            }

            // Close the statement.
            cs.close();

            //
            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            //
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the fields.").append("\nSystem Id: ").append(aSystemId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warn("Exception while closing the connection:", sqle);
                }

                connection = null;
            }
        }

        return fieldTable;
    }

    /**
     * This method returns the List of Fixed Field objects corresponding
     * to the given SystemId.
     *
     * @param aSystemId  BusinessArea Id.
     *
     * @return List of Field objects.
     *
     * @exception DatabaseException incase of any database error.
     */
    public static ArrayList<Field> getFixedFieldsBySystemId(int aSystemId) throws DatabaseException {
        ArrayList<Field> fieldList = new ArrayList<Field>();

        // Look in the mapper first.
        if (ourFieldListMap != null) {
            int              key      = aSystemId;
            ArrayList<Field> tempList = ourFieldListMap.get(key);

            if (tempList != null) {
                for (Field field : tempList) {
                    if (field.getIsExtended() == false) {
                        fieldList.add(field);
                    }
                }
            }

            return fieldList;
        }

        // else try to get the List of extended fields from the database.
        Connection connection = null;

        try {
            connection = DataSourcePool.getConnection();

            CallableStatement cs = connection.prepareCall("stp_field_getFixedFieldsBySystemId ?");

            cs.setInt(1, aSystemId);

            ResultSet rs = cs.executeQuery();

            if (rs != null) {
                while (rs.next() != false) {
                    Field field = createFromResultSet(rs);

                    fieldList.add(field);
                }

                // Close the result set.
                rs.close();
            }

            // Close the statement.
            cs.close();

            //
            // Release the memory by nullifying the references so that these
            // are recovered by the Garbage collector.
            //
            rs = null;
            cs = null;
        } catch (SQLException sqle) {
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while retrieving the ").append("fixed fields.").append("\nSystem Id: ").append(aSystemId).append("\n");

            throw new DatabaseException(message.toString(), sqle);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    LOG.warn("Exception while closing the connection:", sqle);
                }

                connection = null;
            }
        }

        return fieldList;
    }

    /**
     * Accessor method for IsActive property.
     *
     * @return Current Value of IsActive
     *
     */
    public boolean getIsActive() {
        return myIsActive;
    }

    /**
     * Accessor method for Dependency Level property.
     *
     * @return Current value of DependencyLevel
     */
    public boolean getIsDependent() {
        return myIsDependent;
    }

    /**
     * Accessor method for IsExtended property.
     *
     * @return Current Value of IsExtended
     *
     */
    public boolean getIsExtended() {
        return myIsExtended;
    }

    /**
     * Accessor method for IsPrivate property.
     *
     * @return Current Value of IsPrivate
     *
     */
    public boolean getIsPrivate() {
        return myIsPrivate;
    }

    /**
     * Accessor method for Name property.
     *
     * @return Current Value of Name
     *
     */
    public String getName() {
        return myName;
    }

    /**
     * Accessor method for Permission property.
     *
     * @return Current Value of Permission
     *
     */
    public int getPermission() {
        return myPermission;
    }

    /**
     * Accessor method for Regex property.
     *
     * @return Current Value of Regex
     *
     */
    public String getRegex() {
        return myRegex;
    }
    /**
     * Accessor method for Error property.
     *
     * @return Current Value of Error
     *
     */
    public String getError() {
        return myError;
    }

    /**
     * Accessor method for SystemId property.
     *
     * @return Current Value of SystemId
     *
     */
    public int getSystemId() {
        return mySystemId;
    }

    /**
     * Accessor method for TrackingOption property.
     *
     * @return Current Value of TrackingOption
     *
     */
    public int getTrackingOption() {
        return myTrackingOption;
    }

    public int getDisplayOrder()
    {
    	return myDisplayOrder;
    }
    //~--- set methods --------------------------------------------------------

    /**
     * This method sets the parameters in the CallableStatement.
     *
     * @param aCS     CallableStatement whose params should be set.
     *
     * @exception SQLException
     */
    public void setCallableParameters(CallableStatement aCS) throws SQLException {
        aCS.setInt(SYSTEMID, mySystemId);
        aCS.setInt(FIELDID, myFieldId);
        aCS.setString(NAME, myName);
        aCS.setString(DISPLAYNAME, myDisplayName);
        aCS.setString(DESC, myDescription);
        aCS.setInt(DATATYPEID, myDataTypeId);
        aCS.setBoolean(ISACTIVE, myIsActive);
        aCS.setBoolean(ISEXTENDED, myIsExtended);
        aCS.setBoolean(ISPRIVATE, myIsPrivate);
        aCS.setInt(TRACKINGOPTION, myTrackingOption);
        aCS.setInt(PERMISSION, myPermission);
        aCS.setString(REGEX, myRegex);
        aCS.setBoolean(IS_DEPENDENT, myIsDependent);
        aCS.setInt(DISPLAYORDER, myDisplayOrder);
        aCS.setInt(DISPLAYGROUP, myDisplayGroup);
        aCS.setString(ERROR, myError);
    }

    /**
     * Mutator method for DataTypeId property.
     *
     * @param aDataTypeId New Value for DataTypeId
     *
     */
    public void setDataTypeId(int aDataTypeId) {
        myDataTypeId = aDataTypeId;
    }

    /**
     * Mutator method for IsDependent property.
     *
     * @param aIsDependent New value for IsDependent
     *
     */
    public void setDependencyLevel(boolean aIsDependent) {
        myIsDependent = aIsDependent;
    }

    /**
     * Mutator method for Description property.
     *
     * @param aDescription New Value for Description
     *
     */
    public void setDescription(String aDescription) {
        myDescription = aDescription;
    }

    /**
     * Mutator method for DisplayName property.
     *
     * @param aDisplayName New Value for DisplayName
     *
     */
    public void setDisplayName(String aDisplayName) {
        myDisplayName = aDisplayName;
    }

    public void setDisplayOrder(int aDisplayOrder)
    {
    	myDisplayOrder = aDisplayOrder;
    }
    /**
     * Mutator method for FieldId property.
     *
     * @param aFieldId New Value for FieldId
     *
     */
    public void setFieldId(int aFieldId) {
        myFieldId = aFieldId;
    }

    /**
     * Mutator method for IsActive property.
     *
     * @param aIsActive New Value for IsActive
     *
     */
    public void setIsActive(boolean aIsActive) {
        myIsActive = aIsActive;
    }

    /**
     * Mutator method for IsExtended property.
     *
     * @param aIsExtended New Value for IsExtended
     *
     */
    public void setIsExtended(boolean aIsExtended) {
        myIsExtended = aIsExtended;
    }

    /**
     * Mutator method for IsPrivate property.
     *
     * @param aIsPrivate New Value for IsPrivate
     *
     */
    public void setIsPrivate(boolean aIsPrivate) {
        myIsPrivate = aIsPrivate;
    }

    /**
     * Mutator method for Name property.
     *
     * @param aName New Value for Name
     *
     */
    public void setName(String aName) {
        myName = aName;
    }

    /**
     * Mutator method for Permission property.
     *
     * @param aPermission New Value for Permission
     *
     */
    public void setPermission(int aPermission) {
        myPermission = aPermission;
    }

    /**
     * Mutator method for Regex property.
     *
     * @param aRegex New Value for Regex
     *
     */
    public void setRegex(String aRegex) {
        myRegex = aRegex;
    }

    /**
     * Mutator method for Error property.
     *
     * @param aRegex New Value for Error
     *
     */
    public void setError(String aError) {
        myError = aError;
    }
    /**
     * Mutator method for SortField property.
     *
     * @param aSortField New Value of SortField
     *
     */
    public static void setSortField(int aSortField) {
        ourSortField = aSortField;
    }

    /**
     * Mutator method for SortOrder property.
     *
     * @param aSortOrder New Value of SortOrder
     *
     */
    public static void setSortOrder(int aSortOrder) {
        ourSortOrder = aSortOrder;
    }

    /**
     * Mutator method for ourSortField and ourSortOrder properties.
     *
     * @param aSortField New Value of SortField
     * @param aSortOrder New Value of SortOrder
     *
     */
    public static void setSortParams(int aSortField, int aSortOrder) {
        ourSortField = aSortField;
        ourSortOrder = aSortOrder;
    }

    /**
     * Mutator method for SystemId property.
     *
     * @param aSystemId New Value for SystemId
     *
     */
    public void setSystemId(int aSystemId) {
        mySystemId = aSystemId;
    }

    /**
     * Mutator method for TrackingOption property.
     *
     * @param aTrackingOption New Value for TrackingOption
     *
     */
    public void setTrackingOption(int aTrackingOption) {
        myTrackingOption = aTrackingOption;
    }
    
    public int getDisplayGroup()
    {
    	return myDisplayGroup;
    }
    public void setDisplayGroup(int aDisplayGroup)
    {
    	myDisplayGroup = aDisplayGroup;
    }
}

