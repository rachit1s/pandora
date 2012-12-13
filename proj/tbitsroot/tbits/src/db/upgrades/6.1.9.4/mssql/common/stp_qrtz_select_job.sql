SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'stp_qrtz_select_job') AND type in (N'P', N'PC'))
BEGIN
	DROP PROCEDURE stp_qrtz_select_job
END
GO
-- =============================================
-- Author:		Sandeep
-- Create date: 29 May 2010
-- Description:	deletes A job
-- =============================================
CREATE PROCEDURE stp_qrtz_select_job 
	-- Add the parameters for the stored procedure here
	@job_name nvarchar(100), 
	@job_group nvarchar(100)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
	select t.* from QRTZ_JOB_DETAILS t where t.job_name like @job_name and t.job_group like @job_group

	select ct.* from QRTZ_CRON_TRIGGERS ct 
	JOIN QRTZ_TRIGGERS t on t.TRIGGER_NAME like ct.TRIGGER_NAME and t.TRIGGER_GROUP like ct.TRIGGER_GROUP
	where t.job_name like @job_name and t.job_group like @job_group
	
	select * from QRTZ_JOB_DATA_MAP where JOB_NAME like @job_name and JOB_GROUP like @job_group
	
	
	select t.* from QRTZ_TRIGGERS t where t.job_name like @job_name and t.job_group like @job_group
	
	
END
GO
