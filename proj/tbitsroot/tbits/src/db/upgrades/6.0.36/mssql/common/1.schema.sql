SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO

--Table to store captions
CREATE TABLE [dbo].[captions_properties](
	[sys_id] [int] NOT NULL,
	[name] [varchar](150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[value] [text] COLLATE SQL_Latin1_General_CP1_CI_AS NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO

CREATE TABLE [dbo].[tbits_properties](
	[name] [varchar](150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[value] [text] COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[displayName] [varchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[description] [text] COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[category] [varchar](150) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[type] [varchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO

CREATE TABLE [dbo].[log4j_conf](
	[name] [varchar](150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[value] [text] COLLATE SQL_Latin1_General_CP1_CI_AS NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO

CREATE TABLE [dbo].[quartz_properties](
	[name] [varchar](150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[value] [text] COLLATE SQL_Latin1_General_CP1_CI_AS NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
--# thanks to George Papastamatopoulos for submitting this ... and Marko Lahma for
--# updating it.
--#
--# In your Quartz properties file, you'll need to set 
--# org.quartz.jobStore.driverDelegateClass = org.quartz.impl.jdbcjobstore.MSSQLDelegate
--#
--# you shouse enter your DB instance's name on the next line in place of "enter_db_name_here"
--#
--#
--# From a helpful (but anonymous) Quartz user:
--#
--# Regarding this error message:  
--#
--#     [Microsoft][SQLServer 2000 Driver for JDBC]Can't start a cloned connection while in manual transaction mode.
--#
--#
--#     I added "SelectMethod=cursor;" to my Connection URL in the config file. 
--#     It Seems to work, hopefully no side effects.
--#
--#		example:
--#		"jdbc:microsoft:sqlserver://dbmachine:1433;SelectMethod=cursor"; 
--#
--# Another user has pointed out that you will probably need to use the 
--# JTDS driver
--#




IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[FK_QRTZ_JOB_LISTENERS_QRTZ_JOB_DETAILS]') AND OBJECTPROPERTY(id, N'ISFOREIGNKEY') = 1)
ALTER TABLE [dbo].[QRTZ_JOB_LISTENERS] DROP CONSTRAINT FK_QRTZ_JOB_LISTENERS_QRTZ_JOB_DETAILS

GO
IF  EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[FK_QRTZ_JOB_DATA_MAP_QRTZ_JOB_DETAIL]') AND parent_object_id = OBJECT_ID(N'[dbo].[QRTZ_JOB_DATA_MAP]'))
ALTER TABLE [dbo].[QRTZ_JOB_DATA_MAP] DROP CONSTRAINT [FK_QRTZ_JOB_DATA_MAP_QRTZ_JOB_DETAIL]
GO

IF  EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[QRTZ_JOB_DATA_MAP]') AND type in (N'U'))
DROP TABLE [dbo].[QRTZ_JOB_DATA_MAP]

GO
IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[FK_QRTZ_TRIGGERS_QRTZ_JOB_DETAILS]') AND OBJECTPROPERTY(id, N'ISFOREIGNKEY') = 1)
ALTER TABLE [dbo].[QRTZ_TRIGGERS] DROP CONSTRAINT FK_QRTZ_TRIGGERS_QRTZ_JOB_DETAILS
GO

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[FK_QRTZ_CRON_TRIGGERS_QRTZ_TRIGGERS]') AND OBJECTPROPERTY(id, N'ISFOREIGNKEY') = 1)
ALTER TABLE [dbo].[QRTZ_CRON_TRIGGERS] DROP CONSTRAINT FK_QRTZ_CRON_TRIGGERS_QRTZ_TRIGGERS
GO

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[FK_QRTZ_SIMPLE_TRIGGERS_QRTZ_TRIGGERS]') AND OBJECTPROPERTY(id, N'ISFOREIGNKEY') = 1)
ALTER TABLE [dbo].[QRTZ_SIMPLE_TRIGGERS] DROP CONSTRAINT FK_QRTZ_SIMPLE_TRIGGERS_QRTZ_TRIGGERS
GO

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[FK_QRTZ_TRIGGER_LISTENERS_QRTZ_TRIGGERS]') AND OBJECTPROPERTY(id, N'ISFOREIGNKEY') = 1)
ALTER TABLE [dbo].[QRTZ_TRIGGER_LISTENERS] DROP CONSTRAINT FK_QRTZ_TRIGGER_LISTENERS_QRTZ_TRIGGERS
GO

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[QRTZ_CALENDARS]') AND OBJECTPROPERTY(id, N'ISUSERTABLE') = 1)
DROP TABLE [dbo].[QRTZ_CALENDARS]
GO

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[QRTZ_CRON_TRIGGERS]') AND OBJECTPROPERTY(id, N'ISUSERTABLE') = 1)
DROP TABLE [dbo].[QRTZ_CRON_TRIGGERS]
GO

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[QRTZ_BLOB_TRIGGERS]') AND OBJECTPROPERTY(id, N'ISUSERTABLE') = 1)
DROP TABLE [dbo].[QRTZ_BLOB_TRIGGERS]
GO

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[QRTZ_FIRED_TRIGGERS]') AND OBJECTPROPERTY(id, N'ISUSERTABLE') = 1)
DROP TABLE [dbo].[QRTZ_FIRED_TRIGGERS]
GO

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[QRTZ_PAUSED_TRIGGER_GRPS]') AND OBJECTPROPERTY(id, N'ISUSERTABLE') = 1)
DROP TABLE [dbo].[QRTZ_PAUSED_TRIGGER_GRPS]
GO

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[QRTZ_SCHEDULER_STATE]') AND OBJECTPROPERTY(id, N'ISUSERTABLE') = 1)
DROP TABLE [dbo].[QRTZ_SCHEDULER_STATE]
GO

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[QRTZ_LOCKS]') AND OBJECTPROPERTY(id, N'ISUSERTABLE') = 1)
DROP TABLE [dbo].[QRTZ_LOCKS]
GO

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[QRTZ_JOB_DETAILS]') AND OBJECTPROPERTY(id, N'ISUSERTABLE') = 1)
DROP TABLE [dbo].[QRTZ_JOB_DETAILS]
GO

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[QRTZ_JOB_LISTENERS]') AND OBJECTPROPERTY(id, N'ISUSERTABLE') = 1)
DROP TABLE [dbo].[QRTZ_JOB_LISTENERS]
GO

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[QRTZ_SIMPLE_TRIGGERS]') AND OBJECTPROPERTY(id, N'ISUSERTABLE') = 1)
DROP TABLE [dbo].[QRTZ_SIMPLE_TRIGGERS]
GO

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[QRTZ_TRIGGER_LISTENERS]') AND OBJECTPROPERTY(id, N'ISUSERTABLE') = 1)
DROP TABLE [dbo].[QRTZ_TRIGGER_LISTENERS]
GO

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[QRTZ_TRIGGERS]') AND OBJECTPROPERTY(id, N'ISUSERTABLE') = 1)
DROP TABLE [dbo].[QRTZ_TRIGGERS]
GO

CREATE TABLE [dbo].[QRTZ_CALENDARS] (
  [CALENDAR_NAME] [VARCHAR] (80)  NOT NULL ,
  [CALENDAR] [IMAGE] NOT NULL
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[QRTZ_CRON_TRIGGERS] (
  [TRIGGER_NAME] [VARCHAR] (80)  NOT NULL ,
  [TRIGGER_GROUP] [VARCHAR] (80)  NOT NULL ,
  [CRON_EXPRESSION] [VARCHAR] (80)  NOT NULL ,
  [TIME_ZONE_ID] [VARCHAR] (80) 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[QRTZ_FIRED_TRIGGERS] (
  [ENTRY_ID] [VARCHAR] (95)  NOT NULL ,
  [TRIGGER_NAME] [VARCHAR] (80)  NOT NULL ,
  [TRIGGER_GROUP] [VARCHAR] (80)  NOT NULL ,
  [IS_VOLATILE] [VARCHAR] (1)  NOT NULL ,
  [INSTANCE_NAME] [VARCHAR] (80)  NOT NULL ,
  [FIRED_TIME] [BIGINT] NOT NULL ,
  [STATE] [VARCHAR] (16)  NOT NULL,
  [JOB_NAME] [VARCHAR] (80)  NULL ,
  [JOB_GROUP] [VARCHAR] (80)  NULL ,
  [IS_STATEFUL] [VARCHAR] (1)  NULL ,
  [REQUESTS_RECOVERY] [VARCHAR] (1)  NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[QRTZ_PAUSED_TRIGGER_GRPS] (
  [TRIGGER_GROUP] [VARCHAR] (80)  NOT NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[QRTZ_SCHEDULER_STATE] (
  [INSTANCE_NAME] [VARCHAR] (80)  NOT NULL ,
  [LAST_CHECKIN_TIME] [BIGINT] NOT NULL ,
  [CHECKIN_INTERVAL] [BIGINT] NOT NULL ,
  [RECOVERER] [VARCHAR] (80)  NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[QRTZ_LOCKS] (
  [LOCK_NAME] [VARCHAR] (40)  NOT NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[QRTZ_JOB_DETAILS] (
  [JOB_NAME] [VARCHAR] (80)  NOT NULL ,
  [JOB_GROUP] [VARCHAR] (80)  NOT NULL ,
  [DESCRIPTION] [VARCHAR] (120) NULL ,
  [JOB_CLASS_NAME] [VARCHAR] (128)  NOT NULL ,
  [IS_DURABLE] [VARCHAR] (1)  NOT NULL ,
  [IS_VOLATILE] [VARCHAR] (1)  NOT NULL ,
  [IS_STATEFUL] [VARCHAR] (1)  NOT NULL ,
  [REQUESTS_RECOVERY] [VARCHAR] (1)  NOT NULL 
) ON [PRIMARY]
GO


CREATE TABLE [dbo].[QRTZ_JOB_DATA_MAP](
	[JOB_NAME] [varchar](80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[JOB_GROUP] [varchar](80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ENTRY] [varchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[VALUE] [varchar](300) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL
 ) ON [PRIMARY]
GO

CREATE TABLE [dbo].[QRTZ_JOB_LISTENERS] (
  [JOB_NAME] [VARCHAR] (80)  NOT NULL ,
  [JOB_GROUP] [VARCHAR] (80)  NOT NULL ,
  [JOB_LISTENER] [VARCHAR] (80)  NOT NULL
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[QRTZ_SIMPLE_TRIGGERS] (
  [TRIGGER_NAME] [VARCHAR] (80)  NOT NULL ,
  [TRIGGER_GROUP] [VARCHAR] (80)  NOT NULL ,
  [REPEAT_COUNT] [BIGINT] NOT NULL ,
  [REPEAT_INTERVAL] [BIGINT] NOT NULL ,
  [TIMES_TRIGGERED] [BIGINT] NOT NULL
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[QRTZ_BLOB_TRIGGERS] (
  [TRIGGER_NAME] [VARCHAR] (80)  NOT NULL ,
  [TRIGGER_GROUP] [VARCHAR] (80)  NOT NULL ,
  [BLOB_DATA] [IMAGE] NULL
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[QRTZ_TRIGGER_LISTENERS] (
  [TRIGGER_NAME] [VARCHAR] (80)  NOT NULL ,
  [TRIGGER_GROUP] [VARCHAR] (80)  NOT NULL ,
  [TRIGGER_LISTENER] [VARCHAR] (80)  NOT NULL
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[QRTZ_TRIGGERS] (
  [TRIGGER_NAME] [VARCHAR] (80)  NOT NULL ,
  [TRIGGER_GROUP] [VARCHAR] (80)  NOT NULL ,
  [JOB_NAME] [VARCHAR] (80)  NOT NULL ,
  [JOB_GROUP] [VARCHAR] (80)  NOT NULL ,
  [IS_VOLATILE] [VARCHAR] (1)  NOT NULL ,
  [DESCRIPTION] [VARCHAR] (120) NULL ,
  [NEXT_FIRE_TIME] [BIGINT] NULL ,
  [PREV_FIRE_TIME] [BIGINT] NULL ,
  [TRIGGER_STATE] [VARCHAR] (16)  NOT NULL ,
  [TRIGGER_TYPE] [VARCHAR] (8)  NOT NULL ,
  [START_TIME] [BIGINT] NOT NULL ,
  [END_TIME] [BIGINT] NULL ,
  [CALENDAR_NAME] [VARCHAR] (80)  NULL ,
  [MISFIRE_INSTR] [SMALLINT] NULL
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[QRTZ_CALENDARS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_CALENDARS] PRIMARY KEY  CLUSTERED
  (
    [CALENDAR_NAME]
  )  ON [PRIMARY]
GO

ALTER TABLE [dbo].[QRTZ_CRON_TRIGGERS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_CRON_TRIGGERS] PRIMARY KEY  CLUSTERED
  (
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  )  ON [PRIMARY]
GO

ALTER TABLE [dbo].[QRTZ_FIRED_TRIGGERS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_FIRED_TRIGGERS] PRIMARY KEY  CLUSTERED
  (
    [ENTRY_ID]
  )  ON [PRIMARY]
GO

ALTER TABLE [dbo].[QRTZ_PAUSED_TRIGGER_GRPS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_PAUSED_TRIGGER_GRPS] PRIMARY KEY  CLUSTERED
  (
    [TRIGGER_GROUP]
  )  ON [PRIMARY]
GO

ALTER TABLE [dbo].[QRTZ_SCHEDULER_STATE] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_SCHEDULER_STATE] PRIMARY KEY  CLUSTERED
  (
    [INSTANCE_NAME]
  )  ON [PRIMARY]
GO

ALTER TABLE [dbo].[QRTZ_LOCKS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_LOCKS] PRIMARY KEY  CLUSTERED
  (
    [LOCK_NAME]
  )  ON [PRIMARY]
GO

ALTER TABLE [dbo].[QRTZ_JOB_DETAILS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_JOB_DETAILS] PRIMARY KEY  CLUSTERED
  (
    [JOB_NAME],
    [JOB_GROUP]
  )  ON [PRIMARY]

GO

ALTER TABLE [dbo].[QRTZ_JOB_DATA_MAP] ADD  CONSTRAINT [PK_QRTZ_JOB_DATA_MAP] PRIMARY KEY CLUSTERED 
(
	[JOB_NAME] ASC,
	[JOB_GROUP] ASC,
	[ENTRY] ASC
)WITH (PAD_INDEX  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF) ON [PRIMARY]

GO
ALTER TABLE [dbo].[QRTZ_JOB_DATA_MAP]  WITH CHECK ADD  CONSTRAINT [FK_QRTZ_JOB_DATA_MAP_QRTZ_JOB_DETAIL] FOREIGN KEY([JOB_NAME], [JOB_GROUP])
REFERENCES [dbo].[QRTZ_JOB_DETAILS] ([JOB_NAME], [JOB_GROUP])
GO

ALTER TABLE [dbo].[QRTZ_JOB_DATA_MAP] CHECK CONSTRAINT [FK_QRTZ_JOB_DATA_MAP_QRTZ_JOB_DETAIL]
GO

ALTER TABLE [dbo].[QRTZ_JOB_LISTENERS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_JOB_LISTENERS] PRIMARY KEY  CLUSTERED
  (
    [JOB_NAME],
    [JOB_GROUP],
    [JOB_LISTENER]
  )  ON [PRIMARY]
GO

ALTER TABLE [dbo].[QRTZ_SIMPLE_TRIGGERS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_SIMPLE_TRIGGERS] PRIMARY KEY  CLUSTERED
  (
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  )  ON [PRIMARY]
GO

ALTER TABLE [dbo].[QRTZ_TRIGGER_LISTENERS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_TRIGGER_LISTENERS] PRIMARY KEY  CLUSTERED
  (
    [TRIGGER_NAME],
    [TRIGGER_GROUP],
    [TRIGGER_LISTENER]
  )  ON [PRIMARY]
GO

ALTER TABLE [dbo].[QRTZ_TRIGGERS] WITH NOCHECK ADD
  CONSTRAINT [PK_QRTZ_TRIGGERS] PRIMARY KEY  CLUSTERED
  (
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  )  ON [PRIMARY]
GO

ALTER TABLE [dbo].[QRTZ_CRON_TRIGGERS] ADD
  CONSTRAINT [FK_QRTZ_CRON_TRIGGERS_QRTZ_TRIGGERS] FOREIGN KEY
  (
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  ) REFERENCES [dbo].[QRTZ_TRIGGERS] (
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  ) ON DELETE CASCADE
GO

ALTER TABLE [dbo].[QRTZ_JOB_LISTENERS] ADD
  CONSTRAINT [FK_QRTZ_JOB_LISTENERS_QRTZ_JOB_DETAILS] FOREIGN KEY
  (
    [JOB_NAME],
    [JOB_GROUP]
  ) REFERENCES [dbo].[QRTZ_JOB_DETAILS] (
    [JOB_NAME],
    [JOB_GROUP]
  ) ON DELETE CASCADE
GO

ALTER TABLE [dbo].[QRTZ_SIMPLE_TRIGGERS] ADD
  CONSTRAINT [FK_QRTZ_SIMPLE_TRIGGERS_QRTZ_TRIGGERS] FOREIGN KEY
  (
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  ) REFERENCES [dbo].[QRTZ_TRIGGERS] (
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  ) ON DELETE CASCADE
GO

ALTER TABLE [dbo].[QRTZ_TRIGGER_LISTENERS] ADD
  CONSTRAINT [FK_QRTZ_TRIGGER_LISTENERS_QRTZ_TRIGGERS] FOREIGN KEY
  (
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  ) REFERENCES [dbo].[QRTZ_TRIGGERS] (
    [TRIGGER_NAME],
    [TRIGGER_GROUP]
  ) ON DELETE CASCADE
GO

ALTER TABLE [dbo].[QRTZ_TRIGGERS] ADD
  CONSTRAINT [FK_QRTZ_TRIGGERS_QRTZ_JOB_DETAILS] FOREIGN KEY
  (
    [JOB_NAME],
    [JOB_GROUP]
  ) REFERENCES [dbo].[QRTZ_JOB_DETAILS] (
    [JOB_NAME],
    [JOB_GROUP]
  )
GO

INSERT INTO [dbo].[QRTZ_LOCKS] VALUES('TRIGGER_ACCESS');
INSERT INTO [dbo].[QRTZ_LOCKS] VALUES('JOB_ACCESS');
INSERT INTO [dbo].[QRTZ_LOCKS] VALUES('CALENDAR_ACCESS');
INSERT INTO [dbo].[QRTZ_LOCKS] VALUES('STATE_ACCESS');
INSERT INTO [dbo].[QRTZ_LOCKS] VALUES('MISFIRE_ACCESS');
GO

------TVN Upgrade
CREATE TABLE [dbo].[locks](
	[token] [nvarchar](150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[path] [nvarchar](2048) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[type] [varchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[scope] [varchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[owner] [varchar](150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[comment] [text] COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[depth] [int] NOT NULL,
	[creation_date] [datetime] NOT NULL,
 CONSTRAINT [PK_locks] PRIMARY KEY CLUSTERED 
(
	[token] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
CREATE TABLE [dbo].[versions](
	[sys_id] [int] NOT NULL,
	[request_id] [int] NOT NULL,
	[action_id] [int] NOT NULL,
	[attachment] [nvarchar](200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[version_no] [int] NOT NULL
) ON [PRIMARY]

GO

/****** Object:  StoredProcedure [dbo].[stp_request_delete_all_requests]    Script Date: 08/07/2008 20:58:25 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Sandeep
-- Create date: 10 Jan 07
-- Description:	Deletes all the requests in a ba,. Dont use it at all.
-- =============================================
ALTER PROCEDURE [dbo].[stp_request_delete_all_requests] 
	
AS
BEGIN
	delete from action_users
	delete from request_users
	select * from user_read_actions
	delete from user_read_actions
	delete from transferred_requests
	delete from user_drafts
	delete from related_requests
	delete actions_ex
	delete from actions
	delete from requests_ex
	delete from requests
	update business_areas set max_request_id = 0
	delete from locks
	delete from versions
END
GO

/****** Object:  StoredProcedure [dbo].[stp_request_delete_all_requests_in_ba]    Script Date: 08/07/2008 20:58:34 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER PROCEDURE [dbo].[stp_request_delete_all_requests_in_ba] 
(
	@sys_prefix	varchar(30)
	
)
AS
declare @systemId INT
BEGIN
	select @systemId = sys_id from business_areas where sys_prefix = @sys_prefix
	delete from action_users where sys_id = @systemId
	delete from request_users where sys_id = @systemId
	select * from user_read_actions where sys_id = @systemId
	delete from user_read_actions where sys_id = @systemId
	delete from transferred_requests where source_prefix = @sys_prefix or target_prefix = @sys_prefix
	delete from user_drafts where sys_id = @systemId
	delete from related_requests where primary_sys_prefix = @sys_prefix or related_sys_prefix = @sys_prefix
	delete actions_ex where sys_id = @systemId
	delete from actions where sys_id = @systemId
	delete from requests_ex where sys_id = @systemId
	delete from requests where sys_id = @systemId
	update business_areas set max_request_id = 0  where sys_id = @systemId
	delete from versions where sys_id = @systemId
END

GO
/****** Object:  Table [dbo].[transmittal_ba_mapping]    Script Date: 08/08/2008 21:36:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[transmittal_ba_mapping](
	[sys_id] [int] NOT NULL,
	[sys_prefix] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[transmittal_sys_prefix] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[latest_ba_sys_prefix] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
) ON [PRIMARY]
GO
/****** Object:  StoredProcedure [dbo].[stp_get_requestIdByExtendedFields]    Script Date: 08/08/2008 21:33:39 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Lokesh
-- Create date: 05/08/2008
-- Description:	
-- =============================================
CREATE PROCEDURE [dbo].[stp_get_requestIdByExtendedFields] 
	-- Add the parameters for the stored procedure here
	@systemId INT,
	@fieldId1  INT,
	@fieldValue1  INT,
	@fieldId2 INT,
	@fieldValue2 INT,
	@fieldId3 INT,
	@fieldValue3 REAL
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
	SELECT 
	re.request_id 
from 
	requests_ex re 
join 
	requests_ex re1 
on 
	re.sys_id = re1.sys_id and 
	re.request_id = re1.request_id and
	re1.field_id = @fieldId2 and 
	re1.type_value = @fieldValue2 
join 
	requests_ex re2 
on 
	re.request_id = re2.request_id and 
	re.sys_id = re2.sys_id and
	re2.field_id = @fieldId3 and 
	re2.real_value = @fieldValue3
where 
	re.sys_id = @systemId and 
	re.field_id = @fieldId1 and 
	re.type_value = @fieldValue1
END

Go
/****** Object:  StoredProcedure [dbo].[stp_get_mapped_business_areas]    Script Date: 08/08/2008 21:34:41 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Lokesh
-- Create date: 08/08/08
-- Description:	Retrieves mapped business areas for transmittal
-- =============================================
CREATE PROCEDURE [dbo].[stp_get_mapped_business_areas] 
	-- Add the parameters for the stored procedure here
	@sys_prefix varchar(50) 
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
	SELECT transmittal_sys_prefix, latest_ba_sys_prefix 
	from transmittal_ba_mapping
	where sys_prefix = @sys_prefix 
END

GO
