package transbit.tbits.domain;

import static java.sql.Types.INTEGER;
import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;
import static transbit.tbits.api.Mapper.ourFieldPropertyMap;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import transbit.tbits.api.Mapper;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.exception.TBitsException;

public class FieldProperties implements Serializable{
	
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);
	
	public static final int SYS_ID		= 1;
	public static final int FIELD_ID	= 2;
	public static final int PROPERTY	= 3;
	public static final int VALUE    	= 4;
	public static final int DESCRIPTION	= 5;
	
	private int sysId;
	private int fieldId;
	private String property;
	private String value;
	private String description;
	
	public FieldProperties() {
		super();
	}

	public FieldProperties(int sys_id, int field_id, String property, String value, String description) {
		super();
		
		this.description = description;
		this.fieldId = field_id;
		this.property = property;
		this.sysId = sys_id;
		this.value = value;
	}
	
	public String toString()
	{
		return "{" + sysId + "-" + fieldId + "-" + property + ":" + value + "}";
	}
	/**
     * This method is used to create the {@link FieldProperties} object from the ResultSet
     *
     * @param  aResultSet the result object containing the field properties
     * corresponding to a row of the Field table in the database
     * @return the corresponding {@link FieldProperties} object created from the ResutlSet
     */
	public static FieldProperties createFromResultSet(ResultSet aResultSet) throws SQLException {
        FieldProperties fieldProperty = new FieldProperties(aResultSet.getInt("sys_id"), aResultSet.getInt("field_id"), 
        		aResultSet.getString("property"), aResultSet.getString("value"), aResultSet.getString("description"));

        return fieldProperty;
    }
	
	 /**
     * 
     * @param fieldProperty
     * @param con
     * @return {@link FieldProperties} object if every thing goes fine. else throws following exception.
     * @throws SQLException : unexpected sql exception
     * @throws DatabaseException : exception concerning the developers
     * @throws TBitsException : exception that can be shown to user.
     */
	public static FieldProperties delete(FieldProperties fieldProperty, Connection con) throws SQLException,DatabaseException, TBitsException
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
    	
    	if( null == fieldProperty ) 
    	{
    		LOG.info("The supplied field property was null.");    		
    		throw new TBitsException("The supplied field property was null.");
    	}   	
    	
    	int returnValue = 0 ; 
    	 CallableStatement cs = con.prepareCall("stp_field_property_delete " + "?, ?, ?, ?, ?, ?");

         fieldProperty.setCallableParameters(cs);
         cs.registerOutParameter(6, INTEGER);
         cs.execute();
         returnValue = cs.getInt(6);
         
         if(returnValue == 1){
        	 LOG.info("Field was successfully deleted for field : " + fieldProperty ) ;
        	 return fieldProperty;
         }
         else
         {
        	 LOG.info("Field Property deletion failed for field property : " + fieldProperty);
        	 throw new TBitsException("Field property deletion failed for field property : " + fieldProperty.getProperty()) ;
         }      
    }
	
	 /**
     * Method to delete the corresponding {@link FieldProperties} object in the database.
     *
     * @param aObject Object to be deleted
     *
     * @return Delete domain object.
     * @throws TBitsException 
     *
     */
	public static FieldProperties delete(FieldProperties aObject) throws TBitsException, DatabaseException{
		if (aObject == null) {
            throw new TBitsException("The supplied object was null.");
        }

        Connection con        = null;
        FieldProperties fieldProperty = null ;
        try {
            con = DataSourcePool.getConnection();
            con.setAutoCommit(false);
            fieldProperty = delete(aObject,con);
            con.commit() ;
        } catch (SQLException sqle) {
        	try {
				con.rollback();
			} catch (SQLException e) {				
				StringBuilder message = new StringBuilder();
	            message.append("An exception occured while deleting the field property.").append("\n");
	            LOG.info("",(e));
	            throw new DatabaseException(message.toString(), sqle);			
			}
        	
            StringBuilder message = new StringBuilder();

            message.append("An exception occured while deleting the field property.").append("\n");

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

       return fieldProperty;
	}
	
	/**
     * Method to insert a {@link FieldProperties} object into database.     
     * @param fieldProperty -- the {@link FieldProperties} object to be inserted into the database
     * @param con : the database connection 
     * @return if successfule returns the {@link FieldProperties} object. otherwise returns null. 
     * @throws SQLException : unexpected exception 
     * @throws TBitsException : exception that has to be shown to the user
     * @throws DatabaseException : the exception concerning the developers.
     */
	public static FieldProperties insert(FieldProperties fieldProperty, Connection con) throws SQLException, TBitsException, DatabaseException{
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
    	
    	if( null == fieldProperty )
    	{
    		LOG.info("The supplied field property object was null.");
    		throw new DatabaseException("The supplied field property object was null.", new SQLException()) ;
    	}  	
    	
        CallableStatement cs = con.prepareCall("stp_field_property_insert " + "?, ?, ?, ?, ?");

        fieldProperty.setCallableParameters(cs);
        cs.execute();
        LOG.info("Field Property was inserted properly") ;  
    
        return fieldProperty  ;        
	}
	 
	    /**
	     * Method to insert a {@link FieldProperties} object into database.
	     *
	     * @param aObject Object to be inserted
	     * @return if successfule returns the {@link FieldProperties} else return null.
	     * @throws DatabaseException 
	     * @throws TBitsException    
	     */
	    public static FieldProperties insert(FieldProperties aObject) throws TBitsException, DatabaseException{

	        // Insert logic here.
	        if (aObject == null) {
	            throw new TBitsException("The field property supplied was null");
	        }

	        Connection aCon        = null;

	        try {
	            aCon = DataSourcePool.getConnection();
	            aCon.setAutoCommit(false);
	            FieldProperties newFieldProperty = insert(aObject,aCon);
	            
	            aCon.commit();
	            return newFieldProperty;
	        } catch (SQLException sqle) {
	        	try {
	        		if(aCon != null)
						aCon.rollback();
				} catch (SQLException e) {				
					e.printStackTrace();
				}
	            StringBuilder message = new StringBuilder();
	            sqle.printStackTrace();
	            message.append("An exception occured while inserting the field property.").append("\n");

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
	     * This method returns the list of {@link FieldProperties} object corresponding to the given
	     * SystemId and FieldId.
	     *
	     * @param aSystemId  BusinessArea Id.
	     * @param aFieldId   Field Id.
	     *
	     * @return List of {@link FieldProperties} object.
	     *
	     * @exception DatabaseException incase of any database error.
	     */
	    public static List<FieldProperties> lookupBySystemIdAndFieldId(int aSystemId, int aFieldId) throws DatabaseException {
	    	ArrayList<FieldProperties> fieldList = null;

	        // Look in the mapper first.
	        if (ourFieldPropertyMap != null) {
	            int key = hashCode(aSystemId, aFieldId);
	           
	            fieldList = ourFieldPropertyMap.get(key);

	            if (fieldList != null) {
	                return fieldList;
	            } else {
	                return new ArrayList<FieldProperties>();
	            }
	        }

	        // else try to get the Field record from the database.
	        fieldList = new ArrayList<FieldProperties>();

	        Connection connection = null;

	        try {
	            connection = DataSourcePool.getConnection();

	            CallableStatement cs = connection.prepareCall("stp_field_property_lookupBySystemIdAndFieldId ?,?");

	            cs.setInt(1, aSystemId);
	            cs.setInt(2, aFieldId);

	            ResultSet rs = cs.executeQuery();

	            if (rs != null) {
	                while (rs.next() != false) {
	                	FieldProperties fieldProperty = createFromResultSet(rs);

	                    fieldList.add(fieldProperty);
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

	            message.append("An exception occured while retrieving the field properties.")
	            	.append("\nSystem Id: ").append(aSystemId).append("\nField Id: ").append(aFieldId).append("\n");

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
	     * This method returns the list of {@link FieldProperties} objects corresponding to the given
	     * SystemId and FieldId and property.
	     *
	     * @param aSystemId  BusinessArea Id.
	     * @param aFieldId   Field Id.
	     * @param aProperty  Property.
	     *
	     * @return {@link FieldProperties} object.
	     *
	     * @exception DatabaseException incase of any database error.
	     */
	    public static List<FieldProperties> lookupBySystemIdAndFieldIdAndProperty(int aSystemId, int aFieldId, String aProperty) throws DatabaseException {
	    	List<FieldProperties> allFieldPropertyList = lookupBySystemIdAndFieldId(aSystemId, aFieldId);
	    	
	    	List<FieldProperties> fieldPropertyList = new ArrayList<FieldProperties>();
	    	
	    	if(allFieldPropertyList != null){
	    		for(FieldProperties fieldProperty : allFieldPropertyList){
	    			if(fieldProperty.getProperty().equals(aProperty)){
	    				fieldPropertyList.add(fieldProperty);
	    			}
	    		}
	    	}
	    	
	    	return fieldPropertyList;
	    }
	
	/**
     * This method sets the parameters in the CallableStatement.
     *
     * @param aCS     CallableStatement whose params should be set.
     *
     * @exception SQLException
     */
    public void setCallableParameters(CallableStatement aCS) throws SQLException {
        aCS.setInt(SYS_ID, sysId);
        aCS.setInt(FIELD_ID, fieldId);
        aCS.setString(PROPERTY, property);
        aCS.setString(VALUE, value);
        aCS.setString(DESCRIPTION, description);
    }

	public int getSysId() {
		return sysId;
	}

	public void setSysId(int sys_id) {
		this.sysId = sys_id;
	}

	public int getFieldId() {
		return fieldId;
	}

	public void setFieldId(int field_id) {
		this.fieldId = field_id;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public int hashCode() {
		return hashCode(sysId, fieldId);
	}
	
	public static int hashCode(int sysId, int fieldId){
		return (sysId + "-" + fieldId).hashCode(); 
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldProperties other = (FieldProperties) obj;
		if (fieldId != other.fieldId)
			return false;
		if (sysId != other.sysId)
			return false;
		return true;
	}
	
	public static void main(String[] args) {
		
//		FieldProperties fieldProperty = new FieldProperties(47, 8, "user_filter", "usertype:9", "test_desc");
//		
//		try {
//			FieldProperties.insert(fieldProperty);
////			FieldProperties.delete(fieldProperty);
//		} catch (TBitsException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (DatabaseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		Mapper.refreshBOMapper();
//		
//		try {
//			List<FieldProperties> properties = lookupBySystemIdAndFieldId(47, 8);
//			for(FieldProperties property : properties){
//				System.out.println(property.toString());
//			}
//		} catch (DatabaseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		try {
			printProperties(lookupBySystemIdAndFieldId(84, 8));
			System.out.println("==========");
			printProperties(lookupBySystemIdAndFieldId(84, 9));
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void printProperties(List<FieldProperties> props)
	{
		for(FieldProperties prop:props)
		{
			System.out.println(prop);
		}
	}
}
