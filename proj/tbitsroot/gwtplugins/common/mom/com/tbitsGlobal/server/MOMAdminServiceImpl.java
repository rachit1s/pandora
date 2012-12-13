package mom.com.tbitsGlobal.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.SysConfigClient;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;

import mom.com.tbitsGlobal.client.admin.models.MOMTemplate;
import mom.com.tbitsGlobal.client.service.MOMAdminService;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;
import transbit.tbits.webapps.WebUtil;

/**
 * Server side implementation of methods called from client
 * @author devashish
 *
 */
public class MOMAdminServiceImpl extends TbitsRemoteServiceServlet implements MOMAdminService {

	/**
	 * return the all the properties specified in mom templates for the specified ba
	 */
	public List<MOMTemplate> getMOMTemplatesForBa(BusinessAreaClient currentBa) throws TbitsExceptionClient {
		List<MOMTemplate> propertiesList = new ArrayList<MOMTemplate>();
		
		User user;
		try {
			user = WebUtil.validateUser(this.getRequest());
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		}
		
		Connection connection = null;
		
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "select * from mom_templates where sys_id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, currentBa.getSystemId());
			
			ResultSet rs = ps.executeQuery();
			if(null != rs){
				while(rs.next()){
					Integer fieldId = rs.getInt("field_id");
					Integer typeId 	= rs.getInt("type_id");
					Integer isMeeting = rs.getInt("is_meeting");
					String template	  = rs.getString("template");
					
					MOMTemplate entry = new MOMTemplate();
					entry.setBa(currentBa);
					
					Field field;
					try {
						field = Field.lookupBySystemIdAndFieldId(currentBa.getSystemId(), fieldId);
						BAField baField = GWTServiceHelper.fromField(field, user, null, BAField.class);
						entry.setField(baField);
						
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
					
					entry.setTypeId(typeId);
					entry.setIsMeeting(isMeeting);
					entry.setTemplate(template);
					
					propertiesList.add(entry);
				}
			}
			
			connection.commit();
		}catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                    throw new TbitsExceptionClient(sqle);
                }
                connection = null;
            }
        }
		return propertiesList;
	}
	
	/**
	 * Save the template properties for mom
	 * @param properties - values that have to be saved
	 * @return list of saved properties
	 * @throws TbitsExceptionClient
	 */
	public List<MOMTemplate> setMomTemplateProperties(List<MOMTemplate> properties) throws TbitsExceptionClient{
		
		Connection connection = null;
		try{
			connection = DataSourcePool.getConnection();
			connection.setAutoCommit(false);
			
			String sql = "delete from mom_templates";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.execute();
			ps.close();
			
			for(MOMTemplate entry : properties){
				insertIntoMOMTemplates(connection, entry);
			}
			
			connection.commit();
		}catch (SQLException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient(e);
		} finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                    throw new TbitsExceptionClient(sqle);
                }
                connection = null;
            }
        }
		return properties;
	}
	
	public void insertIntoMOMTemplates(Connection connection, MOMTemplate entry) throws SQLException{
		String sql = "insert into mom_templates " +
					" (sys_id, field_id, type_id, is_meeting, template) " +
					" values(?,?,?,?,?) ";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, entry.getBa().getSystemId());
		if(null == entry.getField()){
			ps.setInt(2, 0);
		}else ps.setInt(2, entry.getField().getFieldId());
		
		ps.setInt(3, entry.getTypeId());
		ps.setInt(4, entry.getIsMeeting());
		ps.setString(5, entry.getTemplate());
		
		ps.execute();
		ps.close();
	}
	/**
	 * Get the business area client object for the sys id provided
	 * @param sysId
	 * @return
	 */
	public static BusinessAreaClient getBAforSysId(int sysId){
		BusinessArea ba;
		
			try {
				ba = BusinessArea.lookupBySystemId(sysId);
				if(null == ba)
					return null;
				
				BusinessAreaClient baClient = new BusinessAreaClient();
				GWTServiceHelper.setValuesInDomainObject(ba, baClient);
				return baClient;
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		return null;
	}

	/**
	 * Return the business area client object for sysprefix provided
	 * @param sysPrefix
	 * @return
	 */
	public static BusinessAreaClient getBAforSysPrefix(String sysPrefix){
		BusinessArea ba;
		
			try {
				ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
				if(null == ba)
					return null;
				
				BusinessAreaClient baClient = new BusinessAreaClient();
				GWTServiceHelper.setValuesInDomainObject(ba, baClient);
				return baClient;
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		return null;
	}
	
	/**
	 * Get the list of BA for which MOM is configured
	 */
	public List<BusinessAreaClient> getMOMBA() throws TbitsExceptionClient {
		List<BusinessAreaClient> momBAList = new ArrayList<BusinessAreaClient>();
		
		if(null == PropertiesHandler.getProperty("MOM_PREFIXES"))
			return null;
		String [] momBASysPrefixes = PropertiesHandler.getProperty("MOM_PREFIXES").split(",");
		if(momBASysPrefixes.length == 0)
			return null;
		for(String sysPrefix : momBASysPrefixes){
			momBAList.add(getBAforSysPrefix(sysPrefix));
		}
		return momBAList;
	}
	
	//-----------------------dummy methods--------------------//
	public BusinessAreaClient getBa(BusinessAreaClient ba) {
		return ba;
	}
	
	public BAField getBaField(BAField baField) {
		return baField;
	}

	public SysConfigClient getSysconfigClient(SysConfigClient sysconfig) {
		return sysconfig;
	}
}
