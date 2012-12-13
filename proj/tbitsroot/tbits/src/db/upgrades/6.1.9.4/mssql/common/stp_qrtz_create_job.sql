SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'stp_qrtz_create_job') AND type in (N'P', N'PC'))
BEGIN
	DROP PROCEDURE stp_qrtz_create_job
END
GO
-- =============================================
-- Author:		Sandeep
-- Create date: 29 May 2010
-- Description:	create A job
-- =============================================
CREATE PROCEDURE stp_qrtz_create_job 
	-- Add the parameters for the stored procedure here
	@job_name nvarchar(100), 
	@job_group nvarchar(100),
	@job_description varchar(100),
	@cron_expr varchar(100),
	@job_class varchar(100)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
	insert into QRTZ_JOB_DETAILS(JOB_NAME, JOB_GROUP, DESCRIPTION, JOB_CLASS_NAME, IS_DURABLE, IS_VOLATILE, IS_STATEFUL, REQUESTS_RECOVERY)
						VALUES(@job_name, @job_group, @job_description, @job_class, 1, 0, 0, 0);
	
	insert into dbo.QRTZ_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP, JOB_NAME, JOB_GROUP, IS_VOLATILE, DESCRIPTION, NEXT_FIRE_TIME, PREV_FIRE_TIME, TRIGGER_STATE, TRIGGER_TYPE, START_TIME, END_TIME, CALENDAR_NAME, MISFIRE_INSTR) 
	VALUES(@job_name + 'Trigger', @job_group, @job_name, @job_group, 0, NULL, 0, -1, 'WAITING', 'CRON', 1271944098000, 0,NULL, 0)

	insert into QRTZ_CRON_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP, CRON_EXPRESSION, TIME_ZONE_ID) 
	VALUES (@job_name + 'Trigger', @job_group, @cron_expr, 'GMT+05:30')

END
GO
