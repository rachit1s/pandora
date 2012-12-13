package logistics.com.tbitsGlobal.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import logistics.com.tbitsGlobal.client.LogisticsService;
import logistics.com.tbitsGlobal.client.Stage;
import logistics.com.tbitsGlobal.client.StageParams;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.IActivator;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;
import transbit.tbits.webapps.WebUtil;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;
import commons.com.tbitsGlobal.utils.server.plugins.GWTProxyServletManager;

public class Activator extends TbitsRemoteServiceServlet implements LogisticsService, IActivator{

	public static TBitsLogger LOG = TBitsLogger.getLogger("logistics.com.tbitsGlobal.server");
	
	@Override
	public void activate() {
		GWTProxyServletManager.getInstance().subscribe(LogisticsService.class.getName(), Activator.class);
	}

	@Override
	public HashMap<String, Stage> getStagesMap() throws TbitsExceptionClient {
		HashMap<String, Stage> stagesMap = new HashMap<String, Stage>();
		
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			
			String sql = "SELECT * FROM logistics_stages";
			PreparedStatement statement = conn.prepareStatement(sql);
			
			ResultSet rs = statement.executeQuery();
			if(rs != null){
				while(rs.next()) {
					int stageId = rs.getInt("stage_id");
					String sourceSysPrefix = rs.getString("source_sys_prefix");
					String preSysPrefix = rs.getString("pre_sys_prefix");
					
					Stage stage = new Stage(stageId);
					stage.setSourceSysPrefix(sourceSysPrefix);
					stage.setPreSysPrefix(preSysPrefix);
					
					String sql_params = "SELECT * " +
							"FROM logistics_stage_params " +
							"WHERE stage_id = ?";
					PreparedStatement statement_params = conn.prepareStatement(sql_params);
					statement_params.setInt(1, stageId);
					
					ResultSet rs_params = statement_params.executeQuery();
					if(rs_params != null){
						StageParams stageParams = new StageParams();
						while(rs_params.next()) {
							String property = rs_params.getString("property");
							String value = rs_params.getString("value");
							stageParams.set(property, value);
						}
						stage.setParams(stageParams);
					}
					
					stagesMap.put(sourceSysPrefix, stage);
				}
			}
		} catch (SQLException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.info(TBitsLogger.getStackTrace(e));
					throw new TbitsExceptionClient(e);
				}
			}
		}
		
		return stagesMap;
	}

	@Override
	public List<TbitsTreeRequestData> getPreStageRequests(Stage stage, int requestId) throws TbitsExceptionClient {
		List<TbitsTreeRequestData> requests = new ArrayList<TbitsTreeRequestData>();
		
		User user = null;
		BusinessArea ba = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(stage.getPreSysPrefix());
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		if(user != null && ba != null){
			Connection conn = null;
			try {
				conn = DataSourcePool.getConnection();
				
				String sql = "SELECT * FROM logistics_request_heirarchy where stage_id = ? and source_request_id = ?";
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setInt(1, stage.getStageId());
				statement.setInt(2, requestId);
				
				ResultSet rs = statement.executeQuery();
				if(rs != null){
					while(rs.next()) {
						int preRequestId = rs.getInt("prev_request_id");
						
						TbitsTreeRequestData request = GWTServiceHelper.getDataByRequestId(user, ba, preRequestId);
						requests.add(request);
					}
				}
			} catch (SQLException e) {
				LOG.info(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			}finally {
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						LOG.info(TBitsLogger.getStackTrace(e));
						throw new TbitsExceptionClient(e);
					}
				}
			}
		}
		
		return requests;
	}

	@Override
	public boolean setPreStageRequests(Stage stage, int requestId,
			List<Integer> preRequestIds) throws TbitsExceptionClient {
		User user = null;
		BusinessArea ba = null;
		try {
			user = WebUtil.validateUser(this.getRequest());
			ba = BusinessArea.lookupBySystemPrefix(stage.getPreSysPrefix());
		} catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		} catch (TBitsException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		if(user != null && ba != null){
			Connection conn = null;
			try {
				conn = DataSourcePool.getConnection();
				conn.setAutoCommit(false);
				
				String sql = "DELETE FROM logistics_request_heirarchy where stage_id = ? and source_request_id = ?";
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setInt(1, stage.getStageId());
				statement.setInt(2, requestId);
				
				statement.execute();
				
				for(int preRequestId : preRequestIds){
					sql = "INSERT INTO logistics_request_heirarchy(stage_id, source_request_id, prev_request_id) values(?, ?, ?)";
					statement = conn.prepareStatement(sql);
					statement.setInt(1, stage.getStageId());
					statement.setInt(2, requestId);
					statement.setInt(3, preRequestId);
					
					statement.execute();
				}
				
				conn.commit();
			} catch (SQLException e) {
				if (conn != null) {
					try {
						conn.rollback();
					} catch (SQLException e1) {
						LOG.info(TBitsLogger.getStackTrace(e));
						throw new TbitsExceptionClient(e);
					}
				}
				LOG.info(TBitsLogger.getStackTrace(e));
				throw new TbitsExceptionClient(e);
			}finally {
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						LOG.info(TBitsLogger.getStackTrace(e));
						throw new TbitsExceptionClient(e);
					}
				}
			}
		}
		return true;
	}

}
