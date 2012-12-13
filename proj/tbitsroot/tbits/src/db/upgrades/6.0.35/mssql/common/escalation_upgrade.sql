SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON

ALTER TABLE [dbo].[escalation_conditions] ADD
[category_id] [int] NULL CONSTRAINT [DF_escalation_conditions_category_id]  DEFAULT (0),
[status_id] [int] NULL CONSTRAINT [DF_escalation_conditions_status_id]  DEFAULT (0),
[type_id] [int] NULL CONSTRAINT [DF_escalation_conditions_type_id]  DEFAULT (0)
GO

/****** Object:  Table [dbo].[escalation_conditions]    Script Date: 07/31/2008 03:42:48 ******/
ALTER TABLE [dbo].[escalation_conditions] 
ADD CONSTRAINT [DF_escalation_conditions_severity_id]  DEFAULT (0) FOR [severity_id]; 
GO

ALTER TABLE [dbo].[escalation_conditions] 
ALTER COLUMN [sys_id] [int] NOT NULL;
GO

ALTER TABLE [dbo].[escalation_conditions] 
ALTER COLUMN [span] [int] NOT NULL;
GO

/****** Object:  StoredProcedure [dbo].[stp_get_escalation_span]    Script Date: 07/31/2008 03:22:52 ******/
-- =============================================
-- Author:		Sandeep Giri
-- Create date: 
-- Description:	
-- =============================================
CREATE PROCEDURE [dbo].[stp_get_escalation_span] 
	-- Add the parameters for the stored procedure here
	@sys_id int, 
	@category_id int,
	@status_id int,
	@severity_id int,
	@type_id int
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

	select * from escalation_conditions
	where sys_id = @sys_id 
	and ((status_id IS NULL) or (status_id = 0) or (status_id = @status_id)) 
	and ((category_id IS NULL) or (category_id = 0) or (category_id = @category_id))
	and ((severity_id IS NULL) or (severity_id = 0) or (severity_id = @severity_id))
	and ((type_id IS NULL) or (type_id = 0) or (type_id = @type_id))
END
GO

/****** Object:  StoredProcedure [dbo].[stp_severity_escalation]    Script Date: 07/31/2008 03:22:35 ******/
-- =============================================
-- Author:		<Sandeep Giri>
-- Create date: <28 Jan>
-- Description:	<Get the users with requests which need to be included in assigneed based on escalation rules>
-- =============================================
ALTER PROCEDURE [dbo].[stp_severity_escalation]
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
	
	-- new Due date can be in the following format +2mon +1wk +30d +2hours -5m
	-- First param of DateDiff need to be changed based on our unit of time. It can be : 
	--	Datepart 	Abbreviations
	--	Year 	yy, yyyy
	--	quarter 	qq, q
	--	Month 	mm, m
	--	dayofyear 	dy, y
	--	Day 	dd, d
	--	Week 	wk, ww
	--	Hour 	hh
	--	minute 	mi, n
	--	second 	ss, s
	--	millisecond 	ms

IF NOT EXISTS (SELECT 1 
    FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_TYPE='BASE TABLE' 
    AND TABLE_NAME='escalation_history') 
	create table escalation_history (sys_id int, request_id int, last_escalated_time datetime) 

select 
r.sys_id sys_id, r.request_id request_id, ru.user_id cur_assignee, eh.parent_user_id new_assignee_id, u.user_login new_assignee
into #esctmp
from requests r 
JOIN request_users ru on r.sys_id = ru.sys_id and user_type_id = 3 and r.request_id = ru.request_id
LEFT OUTER JOIN escalation_heirarchy eh on 
	r.sys_id = eh.sys_id and ru.user_id = eh.user_id
LEFT OUTER JOIN users u on u.user_id = eh.parent_user_id
where r.status_id != 3 and  getdate() > dateadd(mi,330, r.due_datetime)

select t1.* from #esctmp t1  where t1.new_assignee_id 
	not in (select cur_assignee from #esctmp t2 where t2.sys_id = t1.sys_id and t2.request_id = t1.request_id) 
	
drop table #esctmp
END
GO