package com.tbitsGlobal.admin.client.services;
/**
 * @author Kshitiz 
 *
 */
import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.domainObjects.JobClassClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.JobDetailClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.JobParameterClient;

public interface JobActionServiceAsync {
	void getJobDetails(AsyncCallback<ArrayList<JobDetailClient>> callback);
	
	void pauseJob(String jobName, String jobGroup,
			AsyncCallback<Boolean> callback);

	void resumeJob(String jobName, String jobGroup,
			AsyncCallback<Boolean> callback);

	void getJobClasses(
			AsyncCallback<ArrayList<JobClassClient>> callback);

	void getJobParams(String jobClassStr, String jobParamsStr,
			AsyncCallback<ArrayList<JobParameterClient>> callback);

	void deleteJob(String jobName, String jobGroup,
			AsyncCallback<Boolean> callback);

	void saveJob(String mode, JobDetailClient jobDetail,ArrayList<JobParameterClient> jpcList, String preJobName,
			String preJobGroup, AsyncCallback<Boolean> callback);

	void getNextExecutions(String cron,Date start , Date end , int numExecutions,
			AsyncCallback<ArrayList<Date>> callback);

	void executeJob(String jobName, String jobGroup,
			AsyncCallback<Boolean> callback);
	

}
