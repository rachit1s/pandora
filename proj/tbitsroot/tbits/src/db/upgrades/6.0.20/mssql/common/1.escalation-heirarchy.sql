SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[escalation_conditions]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
CREATE TABLE [dbo].[escalation_conditions](
	[sys_id] [int] NULL,
	[severity_id] [int] NULL,
	[span] [int] NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[escalation_heirarchy]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
CREATE TABLE [dbo].[escalation_heirarchy](
	[sys_id] [int] NULL,
	[user_id] [int] NULL,
	[parent_user_id] [int] NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[stp_severity_escalation]') AND OBJECTPROPERTY(id,N'IsProcedure') = 1)
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author:		<Sandeep Giri>
-- Create date: <28 Jan>
-- Description:	<Get the users with requests which need to be included in assigneed based on escalation rules>
-- =============================================
CREATE PROCEDURE [dbo].[stp_severity_escalation]
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
    WHERE TABLE_TYPE=''BASE TABLE'' 
    AND TABLE_NAME=''escalation_history'') 
	create table escalation_history (sys_id int, request_id int, last_escalated_time datetime) 

select 
r.sys_id sys_id, r.request_id request_id, ec.span timespan, ru.user_id cur_assignee, eh.parent_user_id new_assignee_id, u.user_login new_assignee
into #esctmp
from requests r 
LEFT OUTER JOIN escalation_history hist on 
	r.request_id = hist.request_id and hist.sys_id = r.sys_id
JOIN escalation_conditions ec  on 
	r.sys_id = ec.sys_id and ec.severity_id = r.severity_id 
	and DateDiff(mi,isnull(hist.last_escalated_time, r.logged_datetime), GetDate()) > ec.span
JOIN request_users ru on r.sys_id = ru.sys_id and user_type_id = 3 and r.request_id = ru.request_id
LEFT OUTER JOIN escalation_heirarchy eh on 
	r.sys_id = eh.sys_id and ru.user_id = eh.user_id
LEFT OUTER JOIN users u on u.user_id = eh.parent_user_id
where r.status_id != 3

select t1.* from #esctmp t1  where t1.new_assignee_id 
	not in (select cur_assignee from #esctmp t2 where t2.sys_id = t1.sys_id and t2.request_id = t1.request_id) 
	
drop table #esctmp
END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[stp_update_last_escalation_time]') AND OBJECTPROPERTY(id,N'IsProcedure') = 1)
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author:		Sandeep
-- Create date: 30/1/08
-- Description:	Updates an last escalated time of a request
-- =============================================

CREATE PROCEDURE [dbo].[stp_update_last_escalation_time] 
	@sys_id int, @request_id int, @val datetime
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
	IF EXISTS (select * from escalation_history where sys_id = @sys_id and request_id = @request_id)
		update escalation_history set last_escalated_time = @val 
		where sys_id = @sys_id and request_id = @request_id
	ELSE 
		insert into escalation_history (sys_id, request_id, last_escalated_time) VALUES(@sys_id, @request_id, @val)
END

' 
END
GO
