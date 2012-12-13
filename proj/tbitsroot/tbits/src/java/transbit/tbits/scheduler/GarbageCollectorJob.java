package transbit.tbits.scheduler;

import java.sql.SQLException;
import java.util.Hashtable;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.filegc.GarbageCollector;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;

public class GarbageCollectorJob implements ITBitsJob{

	public String getDisplayName() {
		return "Garbage Collector";
	}

	public Hashtable<String, JobParameter> getParameters() throws SQLException {
		return new Hashtable<String, JobParameter>();
	}

	public boolean validateParams(Hashtable<String, String> params)
			throws IllegalArgumentException {
		return true;
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		GarbageCollector.getInstance().run();
	}
}
