package transbit.tbits.scheduler.ui;

import java.sql.SQLException;
import java.util.Hashtable;

import org.quartz.Job;

public interface ITBitsJob extends Job{
	public Hashtable<String, JobParameter> getParameters() throws SQLException;
	public String getDisplayName();
	public boolean validateParams(Hashtable<String,String> params) throws IllegalArgumentException;
}
