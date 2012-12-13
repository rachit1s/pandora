package transbit.tbits.upgrade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import org.jfree.util.Log;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.utils.Pair;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.scheduler.MoveJobDataMap;
import transbit.tbits.scheduler.TBitsScheduler;

public class UpgradeQuartzJobData implements IUpgrade{

	public boolean upgrade(Connection conn, String folder, String sysType)
			throws SQLException, DatabaseException, TBitsException {
		try {
			MoveJobDataMap.moveJDMTableToBlob(conn);
		} catch (SchedulerException e) {
			Log.error("Unable to move the job data from table to blob", e);
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
}
