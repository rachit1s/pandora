package browse.com.tbitsglobal.browse.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.server.plugins.GWTProxyServletManager;

import browse.com.tbitsglobal.browse.client.BrowseService;
import browse.com.tbitsglobal.browse.client.Params;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.plugin.IActivator;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;

public class Activator extends TbitsRemoteServiceServlet implements BrowseService, IActivator{
	public static TBitsLogger LOG = TBitsLogger.getLogger("browse.com.tbitsglobal.browse.server");
	
	@Override
	public void activate() {
		GWTProxyServletManager.getInstance().subscribe(BrowseService.class.getName(), Activator.class);
	}

	@Override
	public HashMap<String, Params> getParamsMap() throws TbitsExceptionClient {
		HashMap<String, Params> stagesMap = new HashMap<String, Params>();
		
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			
			String sql = "SELECT * FROM browse_params GROUP BY sys_prefix, [property], [value]";
			PreparedStatement statement = conn.prepareStatement(sql);
			
			ResultSet rs = statement.executeQuery();
			if(rs != null){
				while(rs.next()) {
					String sysPrefix = rs.getString("sys_prefix");
					if(stagesMap.get(sysPrefix) == null){
						stagesMap.put(sysPrefix, new Params());
					}
					
					String name = rs.getString("property");
					String value = rs.getString("value");
					
					stagesMap.get(sysPrefix).set(name, value);
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
}
