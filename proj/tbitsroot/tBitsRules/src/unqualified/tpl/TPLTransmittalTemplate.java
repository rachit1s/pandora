/**
 * 
 */
package tpl;

import static transbit.tbits.Helper.TBitsConstants.PKG_DOMAIN;

import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import transbit.tbits.admin.AdminTransmittals;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;

/**
 * @author Lokesh
 *
 */
public class TPLTransmittalTemplate {
	private static final String TRANSMITTAL_TEMPLATES = "transmittal_templates";
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_DOMAIN);
	private int mySystemId;
	private int myTemplateId;
	private String myTemplateName;
	private String myFileName;
	private String myToList = "";
	private String myCcList = "";
	
	public TPLTransmittalTemplate(int systemId, int templateId, String templateName, String fileName){
		mySystemId = systemId;
		myTemplateId = templateId;
		myTemplateName = templateName;
		myFileName = fileName;
	}
	
	public TPLTransmittalTemplate(int systemId, int templateId, String templateName, String fileName, String toList, String ccList){
		mySystemId = systemId;
		myTemplateId = templateId;
		myTemplateName = templateName;
		myFileName = fileName;
		myToList = toList;
		myCcList = ccList;
	}
	
	public int getSystemId(){
		return mySystemId;
	}
	
	public int getTemplateId(){
		return myTemplateId;
	}
	
	public String getTemplateName(){
		return myTemplateName;
	}
	
	public String getTemplateFileName(){
		return myFileName;
	}
	
	public String getAssigneeList(){
		return myToList;		
	}
	
	public String getSubscribersList(){
		return myCcList;
	}
	
	public static ArrayList<?> lookupBySystemId(int aSystemId) throws DatabaseException {
		TPLTransmittalTemplate transTemplate = null;
		ArrayList<TPLTransmittalTemplate> transTemplateList = new ArrayList<TPLTransmittalTemplate>();
		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM transmittal_templates WHERE sys_id=" + aSystemId);
			ResultSet rs = ps.executeQuery();
			
			if (rs != null) {
				while (rs.next()) {
					transTemplate = createFromResultSet(rs);
					if (transTemplate != null)
						transTemplateList.add(transTemplate);
				}
				rs.close();
				rs = null;
			}			
			ps.close();
			ps = null;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occurred while retrieving the TransmittalTemplate details for").append("\nSystem Id : ").append(aSystemId);
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException sqle) {
				LOG.warning("An Exception has occured while closing a request");
			}
		}
		return transTemplateList;
	}
	
	public static TPLTransmittalTemplate lookupBySystemIdAndTemplateName(int aSystemId, String aTemplateName) throws DatabaseException {		
		TPLTransmittalTemplate transTemplate = null;
		Connection connection = null;

		try {			
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + TRANSMITTAL_TEMPLATES + " WHERE sys_id=" 
					+ aSystemId + " and template_name='" + aTemplateName + "'");
			ResultSet rs = ps.executeQuery();
			
			if (rs != null) {
				if (rs.next()) {
					transTemplate = createFromResultSet(rs);
				}
				rs.close();
				rs = null;
			}			
			ps.close();
			ps = null;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occurred while retrieving the TransmittalTemplate details for").append("\nSystem Id : ")
				.append(aSystemId).append("\nTemplate Name : ").append(aTemplateName);
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException sqle) {
				LOG.warning("An Exception has occured while closing a request");
			}
		}
		return transTemplate;
	}
	
	public static void insert (TPLTransmittalTemplate tt) throws DatabaseException {		
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			CallableStatement cs = connection.prepareCall("stp_tr_template_insert ?,?,?,?,?");
			cs.setInt(1, tt.getSystemId());
			cs.setString(2, tt.getTemplateName());
			cs.setString(3, tt.getTemplateFileName());
			cs.setString(4, tt.getAssigneeList());
			cs.setString(5, tt.getSubscribersList());
			cs.execute();
			cs.close();
			connection.close();			
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occurred while inserting new template " + tt.getTemplateFileName());
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException sqle) {
				LOG.warning("An Exception has occured while closing a request");
			}
		}
	}
	
	public static void updateTransmittalTemplate(TPLTransmittalTemplate tt) throws DatabaseException {
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			int templateId = tt.getTemplateId();
			PreparedStatement ps = connection.prepareStatement("IF EXISTS(SELECT * FROM " + TRANSMITTAL_TEMPLATES + " WHERE template_id="+ templateId + ") " 
					+ "UPDATE " + TRANSMITTAL_TEMPLATES + " SET template_name='" + tt.getTemplateName() + "',template_file_name='" + tt.getTemplateFileName()
					+"',to_list='" + tt.getAssigneeList() + "',cc_list='"+ tt.getSubscribersList()
					+"' WHERE template_id=" + templateId);
			ps.executeUpdate();
			ps.close();
			connection.close();			
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occurred while updating template " + tt.getTemplateName());
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException sqle) {
				LOG.warning("An Exception has occured while closing a request");
			}
		}
	}
	
	public static void delete(int aSystemId, String templateName) throws DatabaseException{
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("DELETE FROM " + TRANSMITTAL_TEMPLATES + " WHERE sys_id=" +
					+ aSystemId + " and template_name='" + templateName + "'");
			ps.execute();
			ps.close();
			connection.close();
		}catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occurred while deleting template " + templateName);
			throw new DatabaseException(message.toString(), sqle);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException sqle) {
				LOG.warning("An Exception has occured while closing a request");
			}
		}
	}
	
	public static JSONArray getTemplatesJSONArrayForSysId (int aSystemId) throws DatabaseException{
		JSONArray trTemplates = new JSONArray();
		ArrayList<?> ttList = lookupBySystemId(aSystemId);System.out.println("list: " + ttList.toString());
		for (Object obj: ttList){
			TPLTransmittalTemplate tt = (TPLTransmittalTemplate)obj;
			JSONObject tmpTTObj = new JSONObject();
			tmpTTObj.accumulate("template_id", tt.getTemplateId());
			tmpTTObj.accumulate("template_name", tt.getTemplateName());
			tmpTTObj.accumulate("file_name", tt.getTemplateFileName());
			tmpTTObj.accumulate("to_list", tt.getAssigneeList());
			tmpTTObj.accumulate("cc_list", tt.getSubscribersList());
			tmpTTObj.accumulate("edit", "Edit");
			trTemplates.add(tmpTTObj);
		}		
		return trTemplates;
	}
	
	private static TPLTransmittalTemplate createFromResultSet(ResultSet rs) throws SQLException {
		TPLTransmittalTemplate transTemplate = null;
		try {
			transTemplate = new TPLTransmittalTemplate (rs.getInt("sys_id"), rs.getInt("template_id"), 
					((rs.getBytes("template_name") != null)
							? new String(rs.getBytes("template_name"), "ISO-8859-1") : ""),
							((rs.getBytes("template_file_name") != null)
									? new String(rs.getBytes("template_file_name"), "ISO-8859-1"): ""), 
									((rs.getBytes("to_list") != null)
											? new String(rs.getBytes("to_list"), "ISO-8859-1"): ""), 
											((rs.getBytes("cc_list") != null)
													? new String(rs.getBytes("cc_list"), "ISO-8859-1"): ""));		                
		} catch (UnsupportedEncodingException e) {
			LOG.severe(TBitsLogger.getStackTrace(e));
		}
		return transTemplate;
	}
	
	public static String getMappedBusinessAreas(int aSystemId) throws DatabaseException{
		BusinessArea ba = BusinessArea.lookupBySystemId(aSystemId);
		return getMappedBusinessAreas(ba.getSystemPrefix());		
	}

	public static String getMappedBusinessAreas(String sysPrefix) throws DatabaseException{
		
		StringBuffer sb = new StringBuffer("");
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			
	        CallableStatement cs = connection.prepareCall("stp_get_mapped_business_areas ?");
	        cs.setString(1, sysPrefix);
	        
	        // execute method returns a flag . It is true if the first
	        // result is a resultSet object.
	        boolean flag = cs.execute();
	        
	        if (flag == true) {		            	
	            ResultSet rs = cs.getResultSet();
	            if ((rs != null) && (rs.next()!= false)){
	            	sb.append(rs.getString(AdminTransmittals.TRANSMITTAL_SYS_PREFIX)).append(",").
	            		append(rs.getString(AdminTransmittals.LATEST_BA_SYS_PREFIX));
	            }		                
	            else{
	            	System.out.println("Resultset is null");
	            }
	        }
	        else{
	        	System.out.println("Did not found any matches");		            	
	        }	   
	        cs.close();
	        cs = null;
	    	return sb.toString();
	    } catch (SQLException sqle) {
	        StringBuilder message = new StringBuilder();	
	        message.append("An exception occurred while retrieving mapped Business Areas ");	
	        throw new DatabaseException(message.toString(), sqle);
	    } finally {
	        try {
	            if (connection != null) {
	                connection.close();
	            }
	        } catch (SQLException sqle) {
	            LOG.warn("An Exception has occured while closing a request");
	        }
	    }		
	}
	
	public static void addTransmittalMapping(int sysId, String sysPrefix, String dtnBASysPrefix, String latestBASysPrefix) throws DatabaseException{
		Connection connection = null;
		try {			
			connection = DataSourcePool.getConnection();			
			CallableStatement cs = connection.prepareCall("stp_tbits_insertTransmittalMapping ?,?,?,?");
			cs.setInt(1, sysId);
			cs.setString(2, sysPrefix);
			cs.setString(3, dtnBASysPrefix);
			cs.setString(4, latestBASysPrefix);
			cs.execute();
			cs.close();
			cs = null;
			connection.close();
		} catch (SQLException sqle) {
	        StringBuilder message = new StringBuilder();	
	        message.append("An exception occurred while retrieving mapped Business Areas ");	
	        throw new DatabaseException(message.toString(), sqle);
	    } finally {
	        try {
	            if (connection != null) {
	                connection.close();
	            }
	        } catch (SQLException sqle) {
	            LOG.warn("An Exception has occured while closing a request");
	        }
	    }		
	}
	
	public static int removeTransmittalMapping(int aSystemId) throws DatabaseException{
		Connection connection = null;
		try {			
			connection = DataSourcePool.getConnection();			
			PreparedStatement ps = connection.prepareStatement("IF EXISTS(SELECT * FROM transmittal_ba_mapping WHERE sys_id="+ aSystemId 
					+ ") " + "DELETE FROM transmittal_ba_mapping WHERE sys_id=" + aSystemId);
			int deleted = ps.executeUpdate();
			ps.close();
			ps = null;
			connection.close();
			return deleted;
		} catch (SQLException sqle) {
	        StringBuilder message = new StringBuilder();	
	        message.append("An exception occurred while retrieving mapped Business Areas ");	
	        throw new DatabaseException(message.toString(), sqle);
	    } finally {
	        try {
	            if (connection != null) {
	                connection.close();
	            }
	        } catch (SQLException sqle) {
	            LOG.warn("An Exception has occured while closing a request");
	        }
	    }		
	}
	
	public static int deleteTransmittalTemplate(int aTemplateId) throws DatabaseException{
		Connection connection = null;
		try {			
			connection = DataSourcePool.getConnection();			
			PreparedStatement ps = connection.prepareStatement("IF EXISTS(SELECT * FROM " + TRANSMITTAL_TEMPLATES + " WHERE template_id="+ aTemplateId 
					+ ") " + "DELETE FROM " + TRANSMITTAL_TEMPLATES + " WHERE template_id=" + aTemplateId);
			int deleted = ps.executeUpdate();
			ps.close();
			ps = null;
			connection.close();
			return deleted;
		} catch (SQLException sqle) {
	        StringBuilder message = new StringBuilder();	
	        message.append("An exception occurred while deleting transmittal template with id: " + aTemplateId);	
	        throw new DatabaseException(message.toString(), sqle);
	    } finally {
	        try {
	            if (connection != null) {
	                connection.close();
	            }
	        } catch (SQLException sqle) {
	            LOG.warn("An Exception has occured while deleting transmittal template with id: " + aTemplateId);
	        }
	    }		
	}
	
	/**
	 * Gets the maximum transmittal number for a particular business area.
	 * @param aSystemId
	 * @return maximum transmittal number
	 * @throws DatabaseException
	 */
	public static int getMaxTransmittalNumber(int aSystemId) throws DatabaseException{
		int maxTransmittalNumber = -1;		
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			maxTransmittalNumber = getMaxTransmittalNumber(connection, aSystemId);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Error occurred while retrieving max transmittal number for sysId: " + aSystemId, e);
		}finally{
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				LOG.warn("Error occurred while retrieving max transmittal number for sysId: " + aSystemId);
			}
		}
		return maxTransmittalNumber;
	}
	
	public static int getMaxTransmittalNumber(Connection connection, int aSystemId) throws SQLException{
		int maxTransmittalNumber = -1;
		CallableStatement cs = connection.prepareCall("stp_transmittal_getMaxTransmittalId ?");
		cs.setInt(1, aSystemId);
		ResultSet rs = cs.executeQuery();
		if ((rs != null) && (rs.next())){
			maxTransmittalNumber = rs.getInt(1);
		}
		rs.close();
		cs.close();
		rs = null;
		cs = null;
		return maxTransmittalNumber;
	}
	
	/**
	 * Gets the maximum transmittal number for a particular business area.
	 * @param aSystemId
	 * @return maximum transmittal number
	 * @throws DatabaseException
	 */
	public static void resetMaxTransmittalNumber(int aSystemId) throws DatabaseException{
		//int maxTransmittalNumber = -1;		
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			resetMaxTransmittalNumber(connection, aSystemId);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Error occurred while resetting max transmittal number for sysId: " + aSystemId, e);
		}finally{
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				LOG.warn("Error occurred while resetting max transmittal number for sysId: " + aSystemId);
			}
		}
		//return maxTransmittalNumber;
	}
	
	public static void resetMaxTransmittalNumber(Connection connection, int aSystemId) throws SQLException{
		System.out.println("Reversing transmittal number %%%%%%%%%%");
		CallableStatement cs = connection.prepareCall("stp_transmittal_max_id_reversal ?");
		cs.setInt(1, aSystemId);
		cs.execute();
		cs.close();
		cs = null;
	}
	
	public static TPLTransmittalTemplate lookupBySystemIdAndTemplateName(Connection connection, int aSystemId, String aTemplateName) throws DatabaseException {		
		TPLTransmittalTemplate transTemplate = null;

		try {			
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + TRANSMITTAL_TEMPLATES + " WHERE sys_id=" 
					+ aSystemId + " and template_name='" + aTemplateName + "'");
			ResultSet rs = ps.executeQuery();
			
			if (rs != null) {
				if (rs.next()) {
					transTemplate = TPLTransmittalTemplate.createFromResultSet(rs);
				}
				rs.close();
				rs = null;
			}			
			ps.close();
			ps = null;
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();
			message.append("An exception occurred while retrieving the TransmittalTemplate details for").append("\nSystem Id : ")
				.append(aSystemId).append("\nTemplate Name : ").append(aTemplateName);
			throw new DatabaseException(message.toString(), sqle);
		} 
		return transTemplate;
	}
	
	public static void main(String[] args){
		try {
			/*TransmittalTemplate tt = new TransmittalTemplate(6,6,"test2","ttFileName1","toList","ccList");
			TransmittalTemplate.updateTransmittalTemplate(tt);*/
			//System.out.println("Max Transmittal Id: " + getMaxTransmittalNumber(16));
			resetMaxTransmittalNumber(16);
			System.out.println("Done..");
			//TransmittalTemplate.delete(8, "ttName");
			//System.out.println(getTemplatesJSONArrayForSysId(6).toString());
			/*TransmittalTemplate tt = lookupBySystemIdAndTemplateName(6, "Client");
			System.out.println("tt: "+ tt.getTemplateFileName());*/
			//removeTransmittalMapping(6);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
}
