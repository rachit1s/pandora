package com.tbitsGlobal.admin.client.services;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.JobClassClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.JobDetailClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.JobParameterClient;

/**
 * @author Kshitiz 
 *
 */

@RemoteServiceRelativePath("admin")
public interface JobActionService extends RemoteService{
	
	public static final String EDIT_JOB = "Edit";
	public static final String CREATE_JOB = "Create job";
	
	public ArrayList<JobDetailClient> getJobDetails() throws TbitsExceptionClient;
	public boolean pauseJob(String jobName,String jobGroup) throws TbitsExceptionClient;
	public boolean resumeJob(String jobName,String jobGroup) throws TbitsExceptionClient;
	public ArrayList<JobClassClient> getJobClasses();
	public ArrayList<JobParameterClient> getJobParams(String jobClassStr,String jobParamsStr);
	public boolean deleteJob(String jobName, String jobGroup) throws TbitsExceptionClient;
	public boolean executeJob(String jobName, String jobGroup) throws TbitsExceptionClient;
	public boolean saveJob(String mode,JobDetailClient jobDetail,ArrayList<JobParameterClient> jpcList,String preJobName , String preJobGroup) throws TbitsExceptionClient;
	public ArrayList<Date> getNextExecutions(String cron, Date start , Date end, int numExecutions) throws TbitsExceptionClient;
}
