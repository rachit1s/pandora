SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[file_repo_index]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[file_repo_index](
	[id] [int] NULL,
	[location] [varchar](300) NULL,
	[name] [varchar](250) NULL,
	[create_date] [datetime] NULL,
	[size] [bigint] NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[type_descriptors]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[type_descriptors](
	[sys_id] [int] NOT NULL,
	[field_id] [int] NOT NULL,
	[type_id] [int] NOT NULL,
	[type_descriptor] [nvarchar](50) NOT NULL,
	[is_primary] [bit] NOT NULL CONSTRAINT [DF_type_descriptors_is_primary]  DEFAULT ((0)),
 CONSTRAINT [PK_type_descriptors] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[field_id] ASC,
	[type_id] ASC,
	[type_descriptor] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [IX_type_descriptors] UNIQUE NONCLUSTERED 
(
	[sys_id] ASC,
	[field_id] ASC,
	[type_descriptor] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[ba_forms]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[ba_forms](
	[sys_id] [int] NOT NULL,
	[form_id] [int] NOT NULL,
	[name] [varchar](128) NOT NULL,
	[title] [varchar](512) NULL,
	[shortname] [varchar](64) NULL,
	[form_config] [text] NULL,
PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[form_id] ASC,
	[name] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[requestfilemaxid]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[requestfilemaxid](
	[sys_id] [int] NULL,
	[request_id] [int] NULL,
	[maxfileid] [int] NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[actions_ex]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[actions_ex](
	[sys_id] [int] NOT NULL,
	[request_id] [int] NOT NULL,
	[action_id] [int] NOT NULL,
	[field_id] [int] NOT NULL,
	[bit_value] [bit] NULL,
	[datetime_value] [datetime] NULL,
	[int_value] [int] NULL,
	[real_value] [real] NULL,
	[varchar_value] [nvarchar](3500) NULL,
	[text_value] [ntext] NULL,
	[type_value] [int] NULL,
 CONSTRAINT [PK_actions_ex] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[request_id] ASC,
	[action_id] ASC,
	[field_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[ba_mail_accounts]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[ba_mail_accounts](
	[email_id] [varchar](50) NOT NULL,
	[mail_server] [varchar](100) NOT NULL,
	[ba_prefix] [varchar](100) NOT NULL,
	[passward] [varchar](50) NOT NULL,
	[protocol] [varchar](50) NOT NULL CONSTRAINT [DF_ba_mail_accounts_protocol]  DEFAULT ('pop3'),
	[port] [int] NULL CONSTRAINT [DF_ba_mail_accounts_port]  DEFAULT ((110)),
	[is_active] [bit] NULL CONSTRAINT [DF_ba_mail_accounts_is_active]  DEFAULT ((0)),
	[ba_mail_ac_id] [int] NULL,
	[category_id] [int] NULL,
	[email_address] [varchar](200) NULL
) ON [PRIMARY]
END
GO
IF NOT EXISTS (SELECT * FROM ::fn_listextendedproperty(N'MS_Description' , N'SCHEMA',N'dbo', N'TABLE',N'ba_mail_accounts', N'COLUMN',N'protocol'))
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Table to store email addresses of different business Areas' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'ba_mail_accounts', @level2type=N'COLUMN',@level2name=N'protocol'
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[ba_menu_table]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[ba_menu_table](
	[menu_id] [int] NOT NULL,
	[menu_caption] [nvarchar](max) NOT NULL,
	[parent_menu_id] [int] NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[type_users]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[type_users](
	[sys_id] [int] NOT NULL,
	[field_id] [int] NOT NULL,
	[type_id] [int] NOT NULL,
	[user_id] [int] NOT NULL,
	[user_type_id] [int] NOT NULL,
	[notification_id] [int] NOT NULL,
	[is_volunteer] [bit] NOT NULL,
	[rr_volunteer] [bit] NOT NULL,
	[is_active] [bit] NOT NULL CONSTRAINT [DF_type_users_is_active]  DEFAULT ((1)),
 CONSTRAINT [PK_category_users] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[field_id] ASC,
	[type_id] ASC,
	[user_type_id] ASC,
	[user_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[ba_menu_mapping]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[ba_menu_mapping](
	[menu_id] [int] NOT NULL,
	[sys_id] [int] NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[ba_rules]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[ba_rules](
	[sys_id] [int] NOT NULL,
	[rule_id] [int] NOT NULL,
	[sequence_no] [int] NULL,
 CONSTRAINT [PK_ba_rules] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[rule_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[types]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[types](
	[sys_id] [int] NOT NULL,
	[field_id] [int] NOT NULL,
	[type_id] [int] NOT NULL,
	[name] [nvarchar](255) NOT NULL,
	[display_name] [nvarchar](255) NOT NULL,
	[description] [nvarchar](255) NOT NULL,
	[ordering] [int] NOT NULL,
	[is_active] [bit] NOT NULL,
	[is_default] [bit] NOT NULL,
	[is_checked] [bit] NOT NULL,
	[is_private] [bit] NOT NULL,
	[is_final] [bit] NULL CONSTRAINT [DF_types_is_final]  DEFAULT ((0)),
 CONSTRAINT [PK_types] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[field_id] ASC,
	[type_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[business_area_users]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[business_area_users](
	[sys_id] [int] NOT NULL,
	[user_id] [int] NOT NULL,
	[is_active] [bit] NOT NULL CONSTRAINT [DF_business_area_users_is_active]  DEFAULT ((1)),
 CONSTRAINT [PK_business_area_users] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[user_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[user_drafts]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[user_drafts](
	[user_id] [int] NOT NULL,
	[time_stamp] [datetime] NOT NULL,
	[sys_id] [int] NOT NULL,
	[request_id] [int] NOT NULL,
	[draft] [text] NOT NULL,
	[draft_id] [int] NULL,
 CONSTRAINT [PK_user_drafts] PRIMARY KEY CLUSTERED 
(
	[user_id] ASC,
	[time_stamp] ASC,
	[sys_id] ASC,
	[request_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[business_areas]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[business_areas](
	[sys_id] [int] NOT NULL,
	[name] [nvarchar](128) NOT NULL,
	[display_name] [nvarchar](128) NOT NULL,
	[email] [nvarchar](255) NOT NULL,
	[sys_prefix] [nvarchar](32) NOT NULL,
	[description] [nvarchar](255) NOT NULL,
	[type] [nvarchar](32) NOT NULL,
	[location] [nvarchar](32) NOT NULL,
	[date_created] [datetime] NOT NULL,
	[max_request_id] [int] NOT NULL,
	[max_email_actions] [int] NOT NULL,
	[is_email_active] [bit] NOT NULL,
	[is_active] [bit] NOT NULL,
	[is_private] [bit] NOT NULL,
	[sys_config] [ntext] NULL,
	[field_config] [ntext] NULL,
	[max_version_no] [int] NULL,
 CONSTRAINT [PK_business_areas] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[user_passwords]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[user_passwords](
	[user_login] [varchar](50) NOT NULL,
	[password] [varchar](50) NULL,
 CONSTRAINT [PK_user_passwords] PRIMARY KEY CLUSTERED 
(
	[user_login] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[daction_log]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[daction_log](
	[sys_id] [int] NOT NULL,
	[request_id] [int] NOT NULL,
	[action_id] [int] NOT NULL,
	[daction_log] [text] NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[user_read_actions]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[user_read_actions](
	[sys_id] [int] NOT NULL,
	[request_id] [int] NOT NULL,
	[action_id] [int] NOT NULL,
	[user_id] [int] NOT NULL,
 CONSTRAINT [PK_user_read_actions] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[request_id] ASC,
	[action_id] ASC,
	[user_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_is_super_user]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE procedure [dbo].[stp_is_super_user]
(
@userId INT
)
AS
select count(*) as usercount from super_users where user_id = @userId and is_active = 1
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[datatypes]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[datatypes](
	[datatype_id] [int] NOT NULL,
	[name] [nvarchar](255) NOT NULL,
	[description] [nvarchar](255) NULL,
 CONSTRAINT [PK_datatypes] PRIMARY KEY CLUSTERED 
(
	[datatype_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[datetime_formats]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[datetime_formats](
	[format_id] [int] NOT NULL,
	[format] [nvarchar](255) NOT NULL,
 CONSTRAINT [PK_datetime_formats] PRIMARY KEY CLUSTERED 
(
	[format_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_mlu_getMembersByEmailLike]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_mlu_getMembersByEmailLike]
(
	@email	VARCHAR(255)
)

AS

SELECT DISTINCT
	mlu.user_id
FROM 
	mail_list_users mlu
	JOIN users uc
	ON mlu.mail_list_id = uc.user_id
WHERE
	uc.email LIKE @email + ''%'' OR
	uc.user_login LIKE @email + ''%''

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[users]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[users](
	[user_id] [int] NOT NULL,
	[user_login] [nvarchar](255) NOT NULL,
	[first_name] [nvarchar](255) NULL,
	[last_name] [nvarchar](255) NULL,
	[display_name] [nvarchar](255) NULL CONSTRAINT [DF_users_display_name]  DEFAULT (''),
	[email] [nvarchar](255) NOT NULL,
	[is_active] [bit] NULL,
	[user_type_id] [int] NULL,
	[web_config] [text] NULL,
	[windows_config] [text] NULL,
	[is_on_vacation] [bit] NULL,
	[is_display] [bit] NULL,
	[cn] [varchar](255) NULL,
	[distinguished_name] [varchar](255) NULL,
	[name] [varchar](255) NULL,
	[member_of] [text] NULL,
	[member] [text] NULL,
	[mail_nickname] [varchar](255) NULL,
	[location] [varchar](64) NULL,
	[extension] [varchar](64) NULL,
	[mobile] [varchar](64) NULL,
	[home_phone] [varchar](64) NULL,
 CONSTRAINT [PK_users] PRIMARY KEY CLUSTERED 
(
	[user_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[user_types]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[user_types](
	[user_type_id] [int] NOT NULL,
	[name] [nvarchar](255) NOT NULL,
 CONSTRAINT [PK_user_types] PRIMARY KEY CLUSTERED 
(
	[user_type_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[dependencies]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[dependencies](
	[sys_id] [int] NOT NULL,
	[dep_id] [int] NOT NULL,
	[dep_name] [varchar](1024) NOT NULL,
	[dep_level] [varchar](30) NOT NULL,
	[dep_type] [varchar](30) NOT NULL,
	[dep_config] [text] NOT NULL,
 CONSTRAINT [PK__dependency_info__4203A4B5] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[dep_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[dependent_fields]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[dependent_fields](
	[sys_id] [int] NOT NULL,
	[dep_id] [int] NOT NULL,
	[field_id] [int] NOT NULL,
	[dep_role] [varchar](50) NOT NULL,
 CONSTRAINT [PK__dependent_fields__6F95653B] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[dep_id] ASC,
	[field_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[workflow_rules]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[workflow_rules](
	[rule_id] [int] NOT NULL,
	[rule_name] [nvarchar](3000) NOT NULL,
	[rule_definition] [ntext] NOT NULL,
 CONSTRAINT [PK_workflow_rules] PRIMARY KEY CLUSTERED 
(
	[rule_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[escalation_heirarchy]') AND type in (N'U'))
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
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[escalation_conditions]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[escalation_conditions](
	[sys_id] [int] NOT NULL,
	[severity_id] [int] NULL CONSTRAINT [DF_escalation_conditions_severity_id]  DEFAULT ((0)),
	[span] [int] NOT NULL,
	[category_id] [int] NULL CONSTRAINT [DF_escalation_conditions_category_id]  DEFAULT ((0)),
	[status_id] [int] NULL CONSTRAINT [DF_escalation_conditions_status_id]  DEFAULT ((0)),
	[type_id] [int] NULL CONSTRAINT [DF_escalation_conditions_type_id]  DEFAULT ((0))
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[exclusion_list]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[exclusion_list](
	[sys_id] [int] NOT NULL,
	[user_id] [int] NOT NULL,
	[user_type_id] [int] NOT NULL,
 CONSTRAINT [PK_exclusion_list] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[user_id] ASC,
	[user_type_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_report_getUnclosedRequestsByUserRole]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'


CREATE PROCEDURE [dbo].[stp_report_getUnclosedRequestsByUserRole]
(
	@systemId 	INT,
	@start 		datetime,
	@end 		datetime,
	@userTypeId 	INT
)
AS

--IF(
--	@systemId != 106 AND 	--GBO
--	@systemId != 109 AND 	--FinopHyd
--	@systemId != 115 AND	--CashTrack
--	@systemId != 201 AND	--tBits
--	@systemId != 228 AND	--GBOTact
--	@systemId != 105		--GenDev
--  )
--BEGIN
--	SELECT @systemId = -1
--END

DECLARE @closedStatusId INT

SELECT 
	@closedStatusId = type_id 
FROM 
	types 
WHERE 
	sys_id = @systemId AND 
	field_id = 4 AND 
	name like ''close%''

-----------------------------------------------------------------------------------------------
-- Get the Data at the start of the interval
-----------------------------------------------------------------------------------------------
SELECT 
	a.request_id, replace(ISNULL(u.display_name, ''-''), '','', '' '') "user_login"
FROM 
        requests a
	LEFT JOIN action_users au
	ON au.sys_id = a.sys_id AND au.request_id = a.request_id AND au.action_id = a.max_action_id AND au.user_type_id = @userTypeId
	LEFT JOIN users u
	ON u.user_id = au.user_id
WHERE
	a.sys_id = @systemId AND
	a.sys_id = @systemId AND 
	a.status_id <> @closedStatusId AND
        a.lastupdated_datetime < @start
ORDER BY a.request_id DESC

-----------------------------------------------------------------------------------------------
-- Get the Data at the end of the interval
-----------------------------------------------------------------------------------------------
SELECT 
	a.request_id, replace(ISNULL(u.display_name, ''-''), '','', '' '') "user_login"
FROM 
        requests a
	LEFT JOIN action_users au
	ON au.sys_id = a.sys_id AND au.request_id = a.request_id AND au.action_id = a.max_action_id AND au.user_type_id = @userTypeId
	LEFT JOIN users u
	ON au.user_id = u.user_id
WHERE
	a.sys_id = @systemId AND
	a.sys_id = @systemId AND 
	a.status_id <> @closedStatusId AND
        a.lastupdated_datetime < @end
ORDER BY a.request_id DESC


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[external_resources]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[external_resources](
	[resource_id] [int] NOT NULL,
	[resource_name] [varchar](128) NULL,
	[resource_def] [text] NULL,
PRIMARY KEY CLUSTERED 
(
	[resource_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[resource_name] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[current_version]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[current_version](
	[systype] [nchar](20) NULL,
	[major] [nchar](10) NULL,
	[minor] [nchar](10) NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_update_last_escalation_time]') AND type in (N'P', N'PC'))
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
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[field_descriptors]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[field_descriptors](
	[sys_id] [int] NOT NULL,
	[field_id] [int] NOT NULL,
	[field_descriptor] [nvarchar](32) NOT NULL,
	[is_primary] [bit] NOT NULL CONSTRAINT [DF_field_descriptors_is_primary]  DEFAULT ((0)),
 CONSTRAINT [PK_field_descriptors] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[field_id] ASC,
	[field_descriptor] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[fields]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[fields](
	[sys_id] [int] NOT NULL,
	[field_id] [int] NOT NULL,
	[name] [nvarchar](128) NOT NULL,
	[display_name] [nvarchar](128) NOT NULL,
	[description] [nvarchar](1024) NOT NULL,
	[data_type_id] [int] NOT NULL,
	[is_active] [bit] NOT NULL,
	[is_extended] [bit] NOT NULL,
	[is_private] [bit] NOT NULL,
	[tracking_option] [int] NOT NULL,
	[permission] [int] NOT NULL,
	[regex] [nvarchar](2048) NULL,
	[is_dependent] [bit] NOT NULL CONSTRAINT [DF__fields__is_depen__1B33F057]  DEFAULT ((0)),
	[display_order] [int] NULL CONSTRAINT [DF_fields_display_order]  DEFAULT ((0)),
	[display_group] [int] NULL CONSTRAINT [DF_fields_display_group]  DEFAULT ((1)),
 CONSTRAINT [PK_fields] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[field_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[captions_properties]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[captions_properties](
	[sys_id] [int] NOT NULL,
	[name] [varchar](150) NOT NULL,
	[value] [text] NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[tbits_properties]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[tbits_properties](
	[name] [varchar](150) NOT NULL,
	[value] [text] NULL,
	[displayName] [varchar](100) NULL,
	[description] [text] NULL,
	[category] [varchar](150) NULL,
	[type] [varchar](50) NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[log4j_conf]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[log4j_conf](
	[name] [varchar](150) NOT NULL,
	[value] [text] NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[quartz_properties]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[quartz_properties](
	[name] [varchar](150) NOT NULL,
	[value] [text] NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[holidays_list]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[holidays_list](
	[office] [nvarchar](255) NOT NULL,
	[holiday_date] [nvarchar](50) NOT NULL,
	[office_zone] [nvarchar](50) NOT NULL,
	[description] [varchar](2048) NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_CALENDARS]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[QRTZ_CALENDARS](
	[CALENDAR_NAME] [varchar](80) NOT NULL,
	[CALENDAR] [image] NOT NULL,
 CONSTRAINT [PK_QRTZ_CALENDARS] PRIMARY KEY CLUSTERED 
(
	[CALENDAR_NAME] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[job_definitions]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[job_definitions](
	[job_id] [int] NOT NULL,
	[job_user_id] [int] NOT NULL,
	[job_criteria] [text] NOT NULL,
	[job_month] [smallint] NOT NULL,
	[job_date] [smallint] NOT NULL,
	[job_day] [smallint] NOT NULL,
	[job_hour] [smallint] NOT NULL,
	[job_minute] [smallint] NOT NULL,
 CONSTRAINT [PK_job_definitions] PRIMARY KEY CLUSTERED 
(
	[job_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[job_notifiers]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[job_notifiers](
	[job_id] [int] NOT NULL,
	[user_id] [int] NOT NULL,
 CONSTRAINT [PK_job_notifiers] PRIMARY KEY CLUSTERED 
(
	[job_id] ASC,
	[user_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_PAUSED_TRIGGER_GRPS]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[QRTZ_PAUSED_TRIGGER_GRPS](
	[TRIGGER_GROUP] [varchar](80) NOT NULL,
 CONSTRAINT [PK_QRTZ_PAUSED_TRIGGER_GRPS] PRIMARY KEY CLUSTERED 
(
	[TRIGGER_GROUP] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[mail_list_users]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[mail_list_users](
	[mail_list_id] [int] NOT NULL,
	[user_id] [int] NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_LOCKS]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[QRTZ_LOCKS](
	[LOCK_NAME] [varchar](40) NOT NULL,
 CONSTRAINT [PK_QRTZ_LOCKS] PRIMARY KEY CLUSTERED 
(
	[LOCK_NAME] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_roleperm_getAuthUsersBySystemIdAndRequestIdAndActionId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'


CREATE PROCEDURE [dbo].[stp_roleperm_getAuthUsersBySystemIdAndRequestIdAndActionId]
(
	@sysId		int,
	@requestId 	int,
	@actionId 	int
)
AS

/*
 * Get the users directly in any role or directly related to the request
 */
 CREATE TABLE #tmpUsers
(
	userId INT
)

INSERT INTO #tmpUsers(userId)
SELECT 
	ru.user_id
FROM
	roles r
	JOIN roles_users ru
	ON r.sys_id = ru.sys_id and r.role_id = ru.role_id
where
	ru.sys_id = @sysId and  ru.is_active = 1
	
INSERT INTO #tmpUsers(userId)
SELECT 
	au.user_id
FROM	
	action_users au
WHERE
	au.sys_id = @sysId AND au.request_id = @requestId AND au.action_id =@actionId
	
/*
 * Get the users of all mailing lists (present in #tmpUser i.e associated directly or indirectly to the request) 
 * recursively
 */
CREATE TABLE #tmp
(
	mailListId INT
)

/*
 * Get the first level of mailing lists (from #tmpUser) 
 */
INSERT INTO #tmp(mailListId)
select 
	mail_list_id 
from 
	mail_list_users 
where 
	mail_list_id in (select * from #tmpUsers)


/*
 * if first level exists, recusively get users mailing lists 
 */
WHILE (EXISTS(SELECT * FROM #tmp))
BEGIN
	
	INSERT INTO #tmpUsers(userId)
	select 
		user_id 
	from 
		mail_list_users 
	where 
		mail_list_id in (select * from #tmp)
		
	SELECT 
		user_id
	INTO #tmp1
	FROM
		mail_list_users 
	where 
	mail_list_id in (select * from #tmp) 

	delete from #tmp
	INSERT INTO #tmp SELECT * FROM #tmp1
	DELETE #tmp1
	DROP TABLE #tmp1	
END

delete from #tmp
INSERT INTO #tmp SELECT distinct * FROM #tmpUsers

delete from #tmpUsers
INSERT INTO #tmpUsers(userId) SELECT distinct * FROM #tmp

delete from #tmp	
DROP TABLE #tmp


SELECT 
	u.user_login, max(rp.gpermissions)
FROM
	fields f
	JOIN roles_permissions rp
	ON f.sys_id = rp.sys_id AND f.field_id = rp.field_id  AND f.name=''is_private''
	JOIN roles_users ru
	on ru.sys_id = rp.sys_id and ru.role_id = rp.role_id AND ru.is_active = 1 AND ru.user_id in (select userId from #tmpUsers)
	LEFT JOIN roles_permissions rqp
	ON f.sys_id = rqp.sys_id AND f.field_id = rqp.field_id AND f.name=''is_private''
	LEFT JOIN action_users au
	ON rqp.sys_id = au.sys_id AND rqp.role_id = au.user_type_id AND au.request_id = @requestId AND au.action_id =@actionId and au.user_id in (select userId from #tmpUsers)
	LEFT JOIN users u
	ON u.user_id = ru.user_id or u.user_id = au.user_id or u.user_id in (select userId from #tmpUsers)
	JOIN permissions p
	ON p.permission = rp.gpermissions
WHERE
	f.sys_id =@sysId and rp.gpermissions >=4
group by u.user_login


DROP TABLE #tmpUsers


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[msg_format]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[msg_format](
	[msg_format_id] [int] NOT NULL,
	[msg_template] [text] NOT NULL,
	[sys_id] [int] NOT NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_JOB_DETAILS]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[QRTZ_JOB_DETAILS](
	[JOB_NAME] [varchar](80) NOT NULL,
	[JOB_GROUP] [varchar](80) NOT NULL,
	[DESCRIPTION] [varchar](120) NULL,
	[JOB_CLASS_NAME] [varchar](128) NOT NULL,
	[IS_DURABLE] [varchar](1) NOT NULL,
	[IS_VOLATILE] [varchar](1) NOT NULL,
	[IS_STATEFUL] [varchar](1) NOT NULL,
	[REQUESTS_RECOVERY] [varchar](1) NOT NULL,
 CONSTRAINT [PK_QRTZ_JOB_DETAILS] PRIMARY KEY CLUSTERED 
(
	[JOB_NAME] ASC,
	[JOB_GROUP] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_roleperm_getPermissionsBySystemIdAndRequestIdAndActionIdAndUserId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_roleperm_getPermissionsBySystemIdAndRequestIdAndActionIdAndUserId]
(
	@systemId	int,
	@requestId 	int,
	@actionId	int,
	@userId		int
)
AS

/*
 * Get the mailing lists where the user is a member directly or indirectly.
 */
CREATE TABLE #tmp
(
	mailListId INT
)
CREATE TABLE #tmp1
(
	mailListId INT
)

/*
 * Get the mailing lists where the user is a direct member.
 */
INSERT INTO #tmp(mailListId)
select 
	mail_list_id 
from 
	mail_list_users 
where 
	user_id = @userId

INSERT INTO #tmp1 SELECT mailListId FROM #tmp
WHILE (EXISTS(SELECT * FROM #tmp1))
BEGIN
	/*
	 * Get the mailing lists where the id in #tmp1 is a member.
         * which is already not part of #tmp
	 */
	SELECT 
		mlu.mail_list_id ''mailListId''
	INTO #tmp2
	FROM
		mail_list_users mlu
		JOIN #tmp1 t1
		ON mlu.user_id = t1.mailListId
		LEFT JOIN #tmp t
		ON mlu.mail_list_id = t.mailListId
	WHERE
		t.mailListId IS NULL

	INSERT INTO #tmp1 SELECT mailListId FROM #tmp2
	INSERT INTO #tmp SELECT mailListId FROM #tmp2
	DROP TABLE #tmp2
	DELETE #tmp1
END
DROP TABLE #tmp1

SELECT
	t.name ''name'',
	t.field_id ''field_id'',
	CASE SUM(p.padd)
	WHEN 0 then 0
	ELSE 1
	END + 
	CASE SUM(p.pchange)
	WHEN 0 then 0
	ELSE 2
	END + 
	CASE SUM(p.pview)
	WHEN 0 then 0
	ELSE 4
	END ''permission''
FROM
	permissions p
	JOIN
	(
	/*
	 * Get the permissions the user gets by virtue of being a user of the system.
	 */
	SELECT
		f.name,
		f.field_id,
		rp.gpermissions
	FROM
		roles_permissions rp
		JOIN fields f
		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id
	WHERE
		rp.sys_id = @systemId AND 
		rp.role_id = 1
	
	UNION
	
	/*
	 * Get the permissions the user gets by virtue of being a part of the BA.
	 */
	SELECT
		f.name,
		f.field_id,
		rp.gpermissions
	FROM
		roles_permissions rp
		JOIN fields f
		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id
		JOIN roles_users ru
		ON ru.sys_id = rp.sys_id AND ru.role_id = rp.role_id
	WHERE
		rp.sys_id = @systemId AND
		(
			ru.user_id = @userId OR
			ru.user_id IN (SELECT mailListId FROM #tmp)
		)
	UNION
	/*
	 * Get the permissions the user gets by virtue of being a part of this action of the request.
	 */
	SELECT
		f.name,
		f.field_id,
		rp.gpermissions
	FROM
		roles_permissions rp
		JOIN fields f
		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id
		JOIN action_users au
		ON au.sys_id = @systemId AND au.request_id = @requestId AND au.action_id = @actionId AND au.user_type_id = rp.role_id
	WHERE
		rp.sys_id = @systemId AND
		(
			au.user_id = @userId OR
			au.user_id IN (SELECT mailListId FROM #tmp)
		)
	) t
	ON p.permission = t.gpermissions 
GROUP BY t.name, t.field_id

/*
 * Finally drop the temp table used for holding the mailing lists this user is part of directly or indirectly.
 */
DROP TABLE #tmp

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[notification_rules]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[notification_rules](
	[notification_id] [int] NOT NULL,
	[name] [nvarchar](64) NOT NULL,
	[display_name] [nvarchar](128) NOT NULL,
	[rules_config] [text] NOT NULL,
 CONSTRAINT [PK_notification_rules] PRIMARY KEY CLUSTERED 
(
	[notification_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_SCHEDULER_STATE]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[QRTZ_SCHEDULER_STATE](
	[INSTANCE_NAME] [varchar](80) NOT NULL,
	[LAST_CHECKIN_TIME] [bigint] NOT NULL,
	[CHECKIN_INTERVAL] [bigint] NOT NULL,
	[RECOVERER] [varchar](80) NULL,
 CONSTRAINT [PK_QRTZ_SCHEDULER_STATE] PRIMARY KEY CLUSTERED 
(
	[INSTANCE_NAME] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_FIRED_TRIGGERS]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[QRTZ_FIRED_TRIGGERS](
	[ENTRY_ID] [varchar](95) NOT NULL,
	[TRIGGER_NAME] [varchar](80) NOT NULL,
	[TRIGGER_GROUP] [varchar](80) NOT NULL,
	[IS_VOLATILE] [varchar](1) NOT NULL,
	[INSTANCE_NAME] [varchar](80) NOT NULL,
	[FIRED_TIME] [bigint] NOT NULL,
	[STATE] [varchar](16) NOT NULL,
	[JOB_NAME] [varchar](80) NULL,
	[JOB_GROUP] [varchar](80) NULL,
	[IS_STATEFUL] [varchar](1) NULL,
	[REQUESTS_RECOVERY] [varchar](1) NULL,
 CONSTRAINT [PK_QRTZ_FIRED_TRIGGERS] PRIMARY KEY CLUSTERED 
(
	[ENTRY_ID] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER OFF
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_roleperm_getPermissionsBySystemIdAndRequestIdAndActionIdAndUserIdList]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'


CREATE PROCEDURE [dbo].[stp_roleperm_getPermissionsBySystemIdAndRequestIdAndActionIdAndUserIdList] 
(
	@sysId		int,
	@requestId 	int,
	@actionId	int,
	@userId	varchar(1024)
)
AS
declare @query varchar(7999)

select @query = '' 
CREATE TABLE #tmp
(
	mailListId INT,
	userId 	   INT	 
)

/*
 * Get the mailing lists where the user is a direct member.
 */
INSERT INTO #tmp(mailListId,userId)
select 
	mail_list_id,user_id 
from 
	mail_list_users 
where 
	user_id in ('' + @userId + '') 

SELECT * INTO #tmp1 FROM #tmp
WHILE (EXISTS(SELECT * FROM #tmp1))
BEGIN
	/*
	 * Get the mailing lists where the id in #tmp1 is a member.
         * which is already not part of #tmp
	 */
	SELECT 
		mlu.mail_list_id,t1.userId
	INTO #tmp2
	FROM
		mail_list_users mlu
		JOIN #tmp1 t1
		ON mlu.user_id = t1.mailListId
		LEFT JOIN #tmp t
		ON mlu.mail_list_id = t.mailListId 
	WHERE
		t.mailListId IS NULL or (mlu.user_id = t1.mailListId and mlu.mail_list_id = t.mailListId)

	INSERT INTO #tmp1 SELECT * FROM #tmp2
	INSERT INTO #tmp SELECT * FROM #tmp2
	DROP TABLE #tmp2
	DELETE #tmp1
END
DROP TABLE #tmp1

SELECT 
	f.name ''''name'''', 
	f.field_id  ''''field_id'''',
	padd,
	pchange,
	pview,	
	u.user_id "userId"
	into #tmp3
FROM
	fields f
	LEFT JOIN roles_permissions rp
	ON f.sys_id = rp.sys_id AND f.field_id = rp.field_id 
	LEFT JOIN users u
	ON u.user_id in ('' + @userId + '') 
	JOIN permissions pm
	ON pm.permission = rp.gpermissions
WHERE
	f.sys_id = '' + CONVERT(varchar(10), @sysId) + '' AND
	(
		rp.role_id = 1
	)


Insert into #tmp3
SELECT 
	f.name  ''''name'''', 
	f.field_id  ''''field_id'''',
	padd,
	pchange,
	pview,
	u.user_id "userId"
FROM
	fields f
	LEFT JOIN roles_permissions rp
	ON f.sys_id = rp.sys_id AND f.field_id = rp.field_id 
	LEFT JOIN roles_users ru
	ON ru.sys_id = rp.sys_id AND ru.role_id = rp.role_id 
	LEFT JOIN #tmp mlu
	ON mlu.mailListId = ru.user_id 
	LEFT JOIN users u
	ON u.user_id = ru.user_id or mlu.userId = u.user_id 
	JOIN permissions pm
	ON pm.permission = rp.gpermissions
WHERE
	f.sys_id = '' + CONVERT(varchar(10), @sysId) + '' AND
	(
		u.user_id in ('' + @userId + '')
	) 

Insert into #tmp3
SELECT 
	f.name  ''''name'''', 
	f.field_id  ''''field_id'''',
	padd,
	pchange,
	pview,
	u.user_id "userId"
FROM
	fields f
	LEFT JOIN roles_permissions rp
	ON f.sys_id = rp.sys_id AND f.field_id = rp.field_id 
	LEFT JOIN action_users aq
	ON aq.sys_id = rp.sys_id AND aq.request_id = '' + CONVERT(varchar(10),@requestId) + '' AND aq.action_id = '' + CONVERT(varchar(10),@actionId) + ''	and aq.user_type_id = rp.role_id  
	LEFT JOIN #tmp mlu
	ON mlu.mailListId =aq.user_id
	LEFT JOIN users u
	ON u.user_id = aq.user_id or mlu.userId = u.user_id 
	JOIN permissions pm
	ON pm.permission = rp.gpermissions
WHERE
	f.sys_id = '' + CONVERT(varchar(10), @sysId) + '' AND
	(
		u.user_id in (''  + @userId  + '')
	) 


select userId, 
	case sum(CONVERT(INT,padd))
		when 0 then 0
		else 1
	end
	+
	case sum(CONVERT(INT, pchange))
		when 0 then 0
		else 2
	end
	+
	case sum(CONVERT(INT,pview))
		when 0 then 0
		else 4
	end  ''''permission'''',
	name,
	field_id
into #tmp4
from #tmp3
GROUP BY name, field_id,userId

select field_id, name, min(permission) ''''permission''''
from #tmp4
group by field_id,name
order by field_id

drop table #tmp3
drop table #tmp4
drop table #tmp
''

exec(@query)


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_roleperm_getPermissionsBySystemIdAndRequestIdAndUserId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_roleperm_getPermissionsBySystemIdAndRequestIdAndUserId]
(
	@systemId		int,
	@requestId 	int,
	@userId		int
)
AS

/*
 * Get the mailing lists where the user is a member directly or indirectly.
 */
CREATE TABLE #tmp
(
	mailListId INT
)

/*
 * Get the mailing lists where the user is a direct member.
 */
INSERT INTO #tmp(mailListId)
select 
	mail_list_id 
from 
	mail_list_users 
where 
	user_id = @userId

SELECT * INTO #tmp1 FROM #tmp
WHILE (EXISTS(SELECT * FROM #tmp1))
BEGIN
	/*
	 * Get the mailing lists where the id in #tmp1 is a member.
         * which is already not part of #tmp
	 */
	SELECT 
		mlu.mail_list_id
	INTO #tmp2
	FROM
		mail_list_users mlu
		JOIN #tmp1 t1
		ON mlu.user_id = t1.mailListId
		LEFT JOIN #tmp t
		ON mlu.mail_list_id = t.mailListId
	WHERE
		t.mailListId IS NULL

	INSERT INTO #tmp1 SELECT * FROM #tmp2
	INSERT INTO #tmp SELECT * FROM #tmp2
	DROP TABLE #tmp2
	DELETE #tmp1
END
DROP TABLE #tmp1

SELECT
	CASE SUM(p.padd)
	WHEN 0 then 0
	ELSE 1
	END + 
	CASE SUM(p.pchange)
	WHEN 0 then 0
	ELSE 2
	END + 
	CASE SUM(p.pview)
	WHEN 0 then 0
	ELSE 4
	END ''permission'',
	t.name ''name'',
	t.field_id ''field_id''
FROM
	permissions p
	JOIN
	(
	/*
	 * Get the permissions the user gets by virtue of being a user of the system.
	 */
	SELECT
		f.name,
		f.field_id,
		rp.gpermissions
	FROM
		roles_permissions rp
		JOIN fields f
		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id
	WHERE
		rp.sys_id = @systemId AND 
		rp.role_id = 1
	
	UNION
	
	/*
	 * Get the permissions the user gets by virtue of being a part of the BA.
	 */
	SELECT
		f.name,
		f.field_id,
		rp.gpermissions
	FROM
		roles_permissions rp
		JOIN fields f
		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id
		JOIN roles_users ru
		ON ru.sys_id = rp.sys_id AND ru.role_id = rp.role_id
	WHERE
		rp.sys_id = @systemId AND
		(
			ru.user_id = @userId OR
			ru.user_id IN (SELECT mailListId FROM #tmp)
		)
	UNION
	/*
	 * Get the permissions the user gets by virtue of being a part of this request.
	 */
	SELECT
		f.name,
		f.field_id,
		rp.gpermissions
	FROM
		roles_permissions rp
		JOIN fields f
		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id
		JOIN request_users ru
		ON ru.sys_id = @systemId AND ru.request_id = @requestId AND ru.user_type_id = rp.role_id
	WHERE
		rp.sys_id = @systemId AND
		(
			ru.user_id = @userId OR
			ru.user_id IN (SELECT mailListId FROM #tmp)
		)
	) t
	ON p.permission = t.gpermissions 
GROUP BY t.name, t.field_id

UNION

/*
 * Get the list of application specific roles the user is present in.
 */
SELECT
	-1, 
	CASE rolename
	WHEN ''Analyst'' THEN ''__ROLE_ANALYST__''
	WHEN ''Admin'' THEN ''__ADMIN__''
	WHEN ''PermissionAdmin'' THEN ''__PERMISSIONADMIN__''
	ELSE rolename
	END
	, 
	10
FROM
	roles_users ru
	JOIN roles r
	ON r.sys_id = ru.sys_id AND r.role_id = ru.role_id AND ru.is_active = 1
WHERE
	ru.sys_id = @systemId AND	
	(
		ru.user_id = @userId OR
		ru.user_id IN (SELECT mailListId FROM #tmp)
	)
	AND
	r.rolename in (''Analyst'', ''Admin'', ''PermissionAdmin'')

UNION

/*
 * Check if the user is a part of super user list.
 */
SELECT
	-1, ''__SUPER_USER__'', -1
FROM
	super_users
WHERE
	user_id = @userId aND
	is_active = 1

/*
 * Finally drop the temp table used for holding the mailing lists this user is part of directly or indirectly.
 */
DROP TABLE #tmp

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[permissions]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[permissions](
	[permission] [int] NOT NULL,
	[padd] [int] NULL,
	[pchange] [int] NULL,
	[pview] [int] NULL,
 CONSTRAINT [PK_permissions] PRIMARY KEY CLUSTERED 
(
	[permission] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_BLOB_TRIGGERS]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[QRTZ_BLOB_TRIGGERS](
	[TRIGGER_NAME] [varchar](80) NOT NULL,
	[TRIGGER_GROUP] [varchar](80) NOT NULL,
	[BLOB_DATA] [image] NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[post_process_rules]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[post_process_rules](
	[rule_id] [int] NOT NULL,
	[user_id] [int] NOT NULL,
	[sys_id] [int] NOT NULL,
	[priority] [int] NOT NULL,
	[xml_string] [text] NOT NULL,
	[description] [text] NOT NULL,
	[enabled] [bit] NOT NULL,
 CONSTRAINT [PK_post_process_rules] PRIMARY KEY CLUSTERED 
(
	[rule_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER OFF
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_roleperm_getPrivatePermissionsBySystemIdAndRequestIdAndActionIdAndUserIdList]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_roleperm_getPrivatePermissionsBySystemIdAndRequestIdAndActionIdAndUserIdList]
(
	@sysId		int,
	@requestId 	int,
	@actionId 	int,
	@userId              varchar(1024)
)
AS
declare @query varchar(7999)
select @query = ''

CREATE TABLE #tmp
(
	mailListId INT,
	userId 	   INT	 
)

/*
 * Get the mailing lists where the user is a direct member.
 */
INSERT INTO #tmp(mailListId,userId)
select 
	mail_list_id,user_id 
from 
	mail_list_users 
where 
	user_id in ('' + @userId + '') 

SELECT * INTO #tmp1 FROM #tmp
WHILE (EXISTS(SELECT * FROM #tmp1))
BEGIN
	/*
	 * Get the mailing lists where the id in #tmp1 is a member.
         * which is already not part of #tmp
	 */
	SELECT 
		mlu.mail_list_id,t1.userId
	INTO #tmp2
	FROM
		mail_list_users mlu
		JOIN #tmp1 t1
		ON mlu.user_id = t1.mailListId
		LEFT JOIN #tmp t
		ON mlu.mail_list_id = t.mailListId 
	WHERE
		t.mailListId IS NULL or (mlu.user_id = t1.mailListId and mlu.mail_list_id = t.mailListId)

	INSERT INTO #tmp1 SELECT * FROM #tmp2
	INSERT INTO #tmp SELECT * FROM #tmp2
	DROP TABLE #tmp2
	DELETE #tmp1
END
DROP TABLE #tmp1

-- Get Private Permissions on User role

SELECT 
	f.name ''''name'''', 
	f.field_id  ''''field_id'''',
	padd,
	pchange,
	pview,
	u.user_id ''''userId''''
	into #tmp3
FROM
	fields f
	LEFT JOIN roles_permissions rp
	ON f.sys_id = rp.sys_id AND f.field_id = rp.field_id  AND f.name=''''is_private''''
	LEFT JOIN users u
	ON u.user_id in ('' + @userId + '') 
	JOIN permissions pm
	ON pm.permission = rp.gpermissions
WHERE
	f.sys_id = '' + CONVERT(varchar(10), @sysId) + '' AND
	(
		rp.role_id = 1
	)

-- Get Private Permissions for Roles User_Id_List user are in 

Insert into #tmp3
SELECT 
	f.name  ''''name'''', 
	f.field_id  ''''field_id'''',
	padd,
	pchange,
	pview,
	u.user_id "userId"
FROM
	fields f
	LEFT JOIN roles_permissions rp
	ON f.sys_id = rp.sys_id AND f.field_id = rp.field_id AND f.name=''''is_private''''
	LEFT JOIN roles_users ru
	ON ru.sys_id = rp.sys_id AND ru.role_id = rp.role_id 
	LEFT JOIN #tmp mlu
	ON mlu.mailListId = ru.user_id 
	LEFT JOIN users u
	ON u.user_id = ru.user_id or mlu.userId = u.user_id 
	JOIN permissions pm
	ON pm.permission = rp.gpermissions
WHERE
	f.sys_id = '' + CONVERT(varchar(10), @sysId) + '' AND
	(
		u.user_id in ('' + @userId + '')
	) 

-- Get Private Permissions for Action_USER roles  User_Id_List user are in

Insert into #tmp3
SELECT 
	f.name  ''''name'''', 
	f.field_id  ''''field_id'''',
	padd,
	pchange,
	pview,
	u.user_id "userId"
FROM
	fields f
	LEFT JOIN roles_permissions rp
	ON f.sys_id = rp.sys_id AND f.field_id = rp.field_id   AND f.name=''''is_private''''
	LEFT JOIN action_users aq
	ON aq.sys_id = rp.sys_id AND aq.request_id = '' + CONVERT(varchar(10),@requestId) + '' AND aq.action_id = '' + CONVERT(varchar(10),@actionId) + '' 	and aq.user_type_id = rp.role_id   
	LEFT JOIN #tmp mlu
	ON mlu.mailListId =aq.user_id
	LEFT JOIN users u
	ON u.user_id = aq.user_id or mlu.userId = u.user_id 
	JOIN permissions pm
	ON pm.permission = rp.gpermissions
WHERE
	f.sys_id = '' + CONVERT(varchar(10), @sysId) + '' AND
	(
		u.user_id in (''  + @userId  + '')
	) 

select userId, 
	case sum(CONVERT(INT,padd))
		when 0 then 0
		else 1
	end
	+
	case sum(CONVERT(INT, pchange))
		when 0 then 0
		else 2
	end
	+
	case sum(CONVERT(INT,pview))
		when 0 then 0
		else 4
	end  ''''permission''''
from #tmp3
GROUP BY name, field_id,userId
drop table #tmp3
drop table #tmp
''
exec (@query)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[related_requests]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[related_requests](
	[primary_sys_prefix] [nvarchar](64) NOT NULL,
	[primary_request_id] [int] NOT NULL,
	[primary_action_id] [int] NOT NULL CONSTRAINT [DF_related_requests_primary_action_id]  DEFAULT ((0)),
	[related_sys_prefix] [nvarchar](64) NOT NULL,
	[related_request_id] [int] NOT NULL,
	[related_action_id] [int] NOT NULL CONSTRAINT [DF_related_requests_related_action_id]  DEFAULT ((0)),
 CONSTRAINT [PK_related_requests] PRIMARY KEY CLUSTERED 
(
	[primary_sys_prefix] ASC,
	[primary_request_id] ASC,
	[primary_action_id] ASC,
	[related_sys_prefix] ASC,
	[related_request_id] ASC,
	[related_action_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[request_users]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[request_users](
	[sys_id] [int] NOT NULL,
	[request_id] [int] NOT NULL,
	[user_type_id] [int] NOT NULL,
	[user_id] [int] NOT NULL,
	[ordering] [int] NULL,
	[is_primary] [bit] NOT NULL,
 CONSTRAINT [pk_request_users] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[request_id] ASC,
	[user_id] ASC,
	[user_type_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ru_getBAAdminList]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_ru_getBAAdminList]
AS
select 
	DISTINCT
	u.user_id,
	u.email,
	u.user_login
from
	roles_users ru 
	join users u 
	on ru.user_id = u.user_id 
where 
	ru.role_id = 9 AND
	u.is_active = 1 AND
	ru.is_active = 1
order by email

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ru_getUserBAList]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_ru_getUserBAList]
(
	@userId		INT,
	@userTypeIdList	VARCHAR(256),
	@isPrimary 	BIT
)
AS
DECLARE @query VARCHAR(7999)
IF (@userTypeIdList IS NULL OR @userTypeIdList = '''')
BEGIN
	SELECT @userTypeIdList = ''-1''
END
SELECT @query = 
''
select DISTINCT sys_prefix
FROM 
	request_users ru
	JOIN business_areas ba
	ON ru.sys_id = ba.sys_id
WHERE
	ru.user_id = '' + CONVERT(VARCHAR(20), @userId) + '' AND
	(
		ru.user_type_id IN ('' + @userTypeIdList + '')''
IF (@isPrimary = 1)
BEGIN
	SELECT @query = @query + '' OR 
		(
			ru.user_type_id = 3 AND 
			ru.is_primary = 1
		)
''

END
SELECT @query = @query + 
''
	)
''
EXEC (@query)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[requests]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[requests](
	[sys_id] [int] NOT NULL,
	[request_id] [int] NOT NULL,
	[category_id] [int] NOT NULL,
	[status_id] [int] NOT NULL,
	[severity_id] [int] NOT NULL,
	[request_type_id] [int] NOT NULL,
	[subject] [nvarchar](2048) NOT NULL,
	[description] [ntext] NOT NULL,
	[is_private] [bit] NOT NULL,
	[parent_request_id] [int] NOT NULL,
	[user_id] [int] NOT NULL,
	[max_action_id] [int] NOT NULL,
	[due_datetime] [datetime] NULL,
	[logged_datetime] [datetime] NOT NULL,
	[lastupdated_datetime] [datetime] NOT NULL,
	[header_description] [ntext] NULL,
	[attachments] [ntext] NULL,
	[summary] [ntext] NULL,
	[memo] [ntext] NULL,
	[append_interface] [int] NOT NULL,
	[notify] [int] NOT NULL,
	[notify_loggers] [bit] NOT NULL,
	[replied_to_action] [int] NULL,
	[office_id] [int] NOT NULL CONSTRAINT [DF__requests__office__166F3B3A]  DEFAULT ((0)),
	[max_request_file_id] [int] NULL,
 CONSTRAINT [PK_requests] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[request_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[requests_ex]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[requests_ex](
	[sys_id] [int] NOT NULL,
	[request_id] [int] NOT NULL,
	[field_id] [int] NOT NULL,
	[bit_value] [bit] NULL,
	[datetime_value] [datetime] NULL,
	[int_value] [int] NULL,
	[real_value] [real] NULL,
	[varchar_value] [nvarchar](3500) NULL,
	[text_value] [ntext] NULL,
	[type_value] [int] NULL,
 CONSTRAINT [PK_requests_ex] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[request_id] ASC,
	[field_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[roles]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[roles](
	[sys_id] [int] NOT NULL,
	[role_id] [int] NOT NULL,
	[rolename] [nvarchar](50) NOT NULL,
	[description] [nvarchar](250) NOT NULL,
 CONSTRAINT [PK_roles] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[role_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[roles_permissions]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[roles_permissions](
	[sys_id] [int] NOT NULL,
	[role_id] [int] NOT NULL,
	[field_id] [int] NOT NULL,
	[gpermissions] [smallint] NOT NULL,
	[dpermissions] [smallint] NOT NULL,
 CONSTRAINT [PK_roles_permissions] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[role_id] ASC,
	[field_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tbits_getRequestInfoInRange]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_tbits_getRequestInfoInRange]
(
	@systemId	INT,
	@start		INT,
	@end		INT
)
AS

/*
 * Return the request records.
 */
SELECT DISTINCT
	/*
         * Boolean Fields.
         */
	r.is_private,
	r.notify,
	r.notify_loggers,

	/*
         * Date Fields.
         */
	r.logged_datetime,
	r.lastupdated_datetime,
	r.due_datetime,

	/*
         * Integer Fields.
         */
	r.request_id,
	r.parent_request_id, 
	r.max_action_id,
	r.append_interface,
	r.replied_to_action,

	/*
         * String Fields.
         */
	ba.sys_prefix ''sys_prefix'',
	r.subject,
	
	/*
	 * Type fields. Take the name and display name.
	 * User can search on any of them.
	 */
	cat.name  + '' '' + cat.display_name ''category_id'',
	stat.name + '' '' + stat.display_name ''status_id'',
	sev.name  + '' '' + sev.display_name ''severity_id'',
	req.name  + '' '' + req.display_name ''request_type_id'',
	office.name + '' '' + office.display_name ''office_id'',
	
	/*
         * User Fields.
         */
	usr.user_login ''user_id''
FROM 
	requests r
	LEFT JOIN business_areas ba
	ON ba.sys_id = r.sys_id
	LEFT JOIN types cat
	ON cat.sys_id = r.sys_id AND cat.field_id = 3 and cat.type_id = r.category_id
	LEFT JOIN types stat
	ON stat.sys_id = r.sys_id AND stat.field_id = 4 and stat.type_id = r.status_id
	LEFT JOIN types sev
	ON sev.sys_id = r.sys_id AND sev.field_id = 5 and sev.type_id = r.severity_id
	LEFT JOIN types req
	ON req.sys_id = r.sys_id AND req.field_id = 6 and req.type_id = r.request_type_id
	LEFT JOIN types office
	ON req.sys_id = r.sys_id AND office.field_id = 30 and office.type_id = r.office_id
	LEFT JOIN users usr
	ON usr.user_id = r.user_id 
WHERE 
	r.sys_id = @systemId AND
	r.request_id >= @start AND
	r.request_id < @end
ORDER BY r.request_id

/*
 * Send out the request user details.
 */
SELECT
	r.request_id,
	r.user_type_id,
	usr.user_login,
	r.is_primary
FROM
	request_users r
	JOIN users usr
	ON usr.user_id = r.user_id 
WHERE 
	r.sys_id = @systemId AND
	r.request_id >= @start AND
	r.request_id < @end
ORDER BY r.request_id, r.user_type_id, user_login


/*
 * Send the requests ex details.
 */
SELECT
	r.request_id,
	f.name ''field_name'',
	f.data_type_id,
	r.bit_value,
	r.datetime_value,
	r.int_value,
	r.real_value,
	r.varchar_value,
	r.text_value,
	ISNULL(t.name, '''') + '' '' + ISNULL(t.display_name, '''') ''type_value''
FROM
	requests_ex r
	JOIN fields f
	ON r.sys_id = f.sys_id AND r.field_id = f.field_id
	LEFT JOIN types t
	ON r.sys_id = t.sys_id AND r.field_id = t.field_id AND r.type_value = t.type_id
WHERE
	r.sys_id = @systemId AND
	r.request_id >= @start AND
	r.request_id < @end
ORDER BY r.request_id

/*
 * Return the action records
 */
SELECT 
	a.request_id,
	a.action_id,
	stat.name ''status_id'',
	a.description,
	a.summary,
	a.attachments,
	usr.user_login ''user_id'',
	a.lastupdated_datetime
FROM
	actions a 
	JOIN types stat
	ON stat.sys_id = a.sys_id AND stat.field_id = 4 and stat.type_id = a.status_id
	JOIN users usr
	ON usr.user_id = a.user_id
WHERE 
	a.sys_id = @systemId AND
	a.request_id >= @start AND
	a.request_id < @end
ORDER BY a.request_id, a.action_id

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[sms_log]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[sms_log](
	[request_id] [int] NOT NULL,
	[sys_id] [int] NOT NULL,
	[cell_no] [text] NOT NULL,
	[date] [datetime] NOT NULL,
	[user_id] [int] NOT NULL,
	[action_id] [int] NOT NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[locks]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[locks](
	[token] [nvarchar](150) NOT NULL,
	[path] [nvarchar](2048) NOT NULL,
	[type] [varchar](100) NOT NULL,
	[scope] [varchar](100) NOT NULL,
	[owner] [varchar](150) NOT NULL,
	[comment] [text] NULL,
	[depth] [int] NOT NULL,
	[creation_date] [datetime] NOT NULL,
 CONSTRAINT [PK_locks] PRIMARY KEY CLUSTERED 
(
	[token] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_td_delete]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_td_delete]
(
	@sys_id		    	INT,
	@field_id		INT,
	@type_id		INT,	
	@type_descriptor	NVARCHAR(32),
	@is_primary		BIT
)
AS
delete type_descriptors
where 
        sys_id          = @sys_id AND
        field_id        = @field_id AND
        type_id	  	= @type_id AND	
        type_descriptor = @type_descriptor

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_td_getAllTypeDescriptors]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_td_getAllTypeDescriptors]
AS
SELECT 
	t.sys_id,
	t.field_id,
	t.type_id,
	ISNULL(td.type_descriptor, t.name) ''type_descriptor'',
	ISNULL(td.is_primary, 0) ''is_primary''
FROM 
	fields f
	JOIN types t
	ON f.sys_id = t.sys_id AND f.field_id = t.field_id 
	LEFT JOIN type_descriptors td
	ON t.sys_id = td.sys_id AND t.field_id = td.field_id AND t.type_id = td.type_id
ORDER by t.sys_id, t.field_id,t.type_id

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_action_getAllActions]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_action_getAllActions] 
( 
        @systemId int, 
        @requestId bigint , 
        @sortOrder varchar(10) 
) 
as 
begin 
declare @sort varchar(10) 
declare @query varchar(7999) 
select @query = 
'' 
        SELECT 
        sys_id   , 
        request_id   , 
        action_id   , 
        category_id   , 
        status_id   , 
        severity_id   , 
        request_type_id   , 
        subject  , 
        description , 
        is_private   , 
        parent_request_id   , 
        user_id   , 
        due_datetime, 
        logged_datetime, 
        lastupdated_datetime, 
        header_description, 
        attachments, 
        summary, 
        '''''''' as "memo", 
        append_interface   , 
        notify   , 
        notify_loggers   , 
        replied_to_action, 
        office_id 
        FROM 
                actions 
        WHERE 
                sys_id = '' + str(@systemId) + '' and 
                request_id='' +str( @requestId) + '' 
        order by action_id '' + @sortOrder + '' 
'' 
exec(@query) 
end

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[versions]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[versions](
	[sys_id] [int] NOT NULL,
	[request_id] [int] NOT NULL,
	[action_id] [int] NOT NULL,
	[attachment] [nvarchar](200) NULL,
	[version_no] [int] NOT NULL,
	[file_action] [varchar](10) NULL,
	[field_id] [int] NULL,
	[request_file_id] [int] NULL,
	[file_id] [int] NULL,
	[tvn_name] [varchar](100) NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_td_getPrimaryDescriptorByFieldIdAndTypeId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_td_getPrimaryDescriptorByFieldIdAndTypeId]
(
	@systemId	INT,
	@fieldId        INT,
	@typeId         INT
)
AS

SELECT
    *
FROM
	type_descriptors 
WHERE
    sys_id      = @systemId  AND
    field_id    = @fieldId AND
    type_id     = @typeId AND
    is_primary  = 1

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_td_getTypeDescriptorsBySystemIdAndFieldIdAndTypeId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_td_getTypeDescriptorsBySystemIdAndFieldIdAndTypeId]
(
	@sys_id 	INT,
	@field_id 	INT,
	@type_id 	INT
)
AS

SELECT 
	t.sys_id,
	t.field_id,
        t.type_id,
	ISNULL(td.type_descriptor, t.display_name) ''type_descriptor'',
	ISNULL(td.is_primary, 0) ''is_primary''
FROM 
	types t
	LEFT JOIN type_descriptors td
	ON t.sys_id = td.sys_id AND t.field_id = td.field_id AND t.type_id = td.type_id

WHERE 
	t.sys_id = @sys_id AND
	t.field_id = @field_id AND
	t.type_id = @type_id

ORDER by t.sys_id, t.field_id, t.type_id

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_td_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_td_insert]
(
	@sys_id		    INT,
	@field_id	    INT,
	@type_id	    INT,	
	@type_descriptor    NVARCHAR(32),
	@is_primary         BIT
)
AS
INSERT INTO type_descriptors
(
	sys_id,
	field_id,
	type_id	,
	type_descriptor,
	is_primary
)
VALUES
(
	@sys_id,
	@field_id,
	@type_id,
	@type_descriptor,
	@is_primary
)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[roles_users]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[roles_users](
	[sys_id] [int] NOT NULL,
	[role_id] [int] NOT NULL,
	[user_id] [int] NOT NULL,
	[is_active] [bit] NOT NULL CONSTRAINT [DF_roles_users_is_active]  DEFAULT ((1)),
 CONSTRAINT [PK_roles_users] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[role_id] ASC,
	[user_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_td_lookupBySystemId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_td_lookupBySystemId]
(
	@sysId int
)
AS
SELECT 
	t.sys_id,
	t.field_id,
	t.type_id,
	ISNULL(td.type_descriptor, t.name) ''type_descriptor'',
	ISNULL(td.is_primary, 0) ''is_primary''
FROM 
	fields f
	 JOIN types t
	ON f.sys_id = t.sys_id AND f.field_id = t.field_id
	LEFT JOIN type_descriptors td
	ON t.sys_id = td.sys_id AND t.field_id = td.field_id AND t.type_id = td.type_id
WHERE 
	f.sys_id = @sysId 
ORDER by t.sys_id, t.field_id,t.type_id

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_td_lookupBySystemIdAndFieldIdAndTypeDescriptor]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_td_lookupBySystemIdAndFieldIdAndTypeDescriptor]
(
	@sysId int,
	@fieldd int,
	@typeDesc nvarchar(52)
)
AS
SELECT 
	t.*
FROM 
	fields f
	LEFT JOIN types t
	ON f.sys_id = t.sys_id AND f.field_id = t.field_id
	LEFT JOIN type_descriptors td
	ON t.sys_id = td.sys_id AND t.field_id = td.field_id 
WHERE
	 t.sys_id = @sysId  AND
	(
		td.type_descriptor = @typeDesc OR
		t.name = @typeDesc OR
		t.display_name = @typeDesc
	)
ORDER by t.sys_id, t.field_id,t.type_id

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tr_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_tr_insert]
(
	@srcPrefix	VARCHAR(50),
	@srcRequestId	INT,
	@tarPrefix	VARCHAR(50),
	@tarRequestId	INT
)
AS

INSERT INTO transferred_requests
(
	source_prefix,
	source_request_id,
	target_prefix,
	target_request_id
)
VALUES
(
	@srcPrefix,
	@srcRequestId,
	@tarPrefix,
	@tarRequestId
)

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tr_lookupBySourcePrefixAndRequestId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_tr_lookupBySourcePrefixAndRequestId]
(
	@srcPrefix	VARCHAR(50),
	@srcRequestId	INT
)
AS

SELECT
	*
FROM transferred_requests
WHERE
	source_prefix		= @srcPrefix AND
	source_request_id	= @srcRequestId

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tr_update]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_tr_update]
(
	@srcPrefix	VARCHAR(50),
	@srcRequestId	INT,
	@tarPrefix	VARCHAR(50),
	@tarRequestId	INT
)
AS

UPDATE transferred_requests
SET 
	target_prefix		= @tarPrefix,
	target_request_id	= @tarRequestId
WHERE
	source_prefix		= @srcPrefix AND
	source_request_id	= @srcRequestId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tu_getAnalystInfo]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_tu_getAnalystInfo]
(
	@systemId	INT,
	@fieldId	INT,
	@userId		INT
)
AS

SELECT
	t.sys_id ''sys_id'',
	u.user_login ''user_login'',
	t.display_name ''type_name'',
	t.is_private ''is_private'',
	nr.display_name ''email_option'',
	tu.is_volunteer ''is_volunteer''
FROM 
	types t
	LEFT JOIN type_users tu
	ON t.sys_id = tu.sys_id AND t.field_id = tu.field_id AND t.type_id = tu.type_id AND tu.user_id = @userId
	LEFT JOIN notification_rules nr
	ON tu.notification_id = nr.notification_id
	LEFT JOIN users u
	ON tu.user_id = u.user_id AND tu.is_active = 1
WHERE
	t.sys_id   = @systemId AND
	t.field_id = @fieldId
ORDER BY t.ordering

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tu_updateNextVolunteer]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_tu_updateNextVolunteer]
(
	@systemId	INT,
	@fieldId	INT,
	@typeId		INT,
	@userId		INT
)
AS
-- Clear the bit set for the previous rr_volunteer.
UPDATE type_users
SET
	rr_volunteer = 0
WHERE
	sys_id = @systemId AND
	field_id = @fieldId AND
	type_id = @typeId AND
	rr_volunteer = 1
--- Set the given user as the next rr_volunteer.
UPDATE type_users
SET
	rr_volunteer = 1
WHERE
	sys_id = @systemId AND
	field_id = @fieldId AND
	type_id = @typeId AND
	user_id = @userId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_type_descriptor_delete]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_type_descriptor_delete]
(
	@sys_id			    INT,
	@field_id		    INT,
	@type_id                    INT,
	@type_descriptor	    NVARCHAR(32),
	@is_primary                 BIT
)
AS
delete type_descriptors
where 
        sys_id              = @sys_id AND
        field_id            = @field_id AND
        type_id             = @type_id AND
        type_descriptor    =  @type_descriptor AND
        is_primary          = @is_primary

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_type_descriptor_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_type_descriptor_insert]
(
	@sys_id			    INT,
	@field_id		    INT,
	@type_id                    INT,
	@type_descriptor	    NVARCHAR(32),
	@is_primary                 BIT
)
AS
INSERT INTO type_descriptors
(
	sys_id,
	field_id,
	type_id,
	type_descriptor,
	is_primary
)
VALUES
(
	@sys_id,
	@field_id,
        @type_id,
	@type_descriptor,
	@is_primary
)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_type_getAllTypes]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_type_getAllTypes]
AS
SELECT 
	* 
FROM 
	types

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_type_getDefaultTypeBySystemIdAndFieldId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE procedure [dbo].[stp_type_getDefaultTypeBySystemIdAndFieldId]
(
	@systemId	INT,
	@fieldId 	INT
)
AS
SELECT 
	*
FROM 
	types 
WHERE
	sys_id = @systemId AND 
	field_id = @fieldId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_type_getDistinctStatusSeverities]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_type_getDistinctStatusSeverities]
(
	@sysIdList VARCHAR(7999)
)
AS
DECLARE @query VARCHAR(7999)
SELECT @query = 
''
SELECT DISTINCT 
	name,
	display_name
FROM 
	types 
WHERE
	sys_id IN ('' + @sysIdList + '') AND
	field_id = 4 AND
	is_private = 0
ORDER BY name
SELECT DISTINCT 
	name,
	display_name
FROM 
	types
WHERE
	sys_id IN ('' + @sysIdList + '') AND
	field_id = 5 AND
	is_private = 0
ORDER BY name
''
EXEC (@query)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_type_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_type_insert]
(
	@sys_id		INT,
	@field_id	INT,
	@type_id	INT OUTPUT,
	@name		NVARCHAR(255),
	@display_name	NVARCHAR(255),
	@description	NVARCHAR(255),
	@ordering	INT,
	@is_active	BIT,
	@is_default	BIT,
	@is_checked	BIT,
	@is_private	BIT,
	@is_final	BIT
)
AS
DECLARE @typeID int
DECLARE @orderingID int

/*
 * Get the maximum type id for this field.
 */
SELECT 
	@typeID = (ISNULL(MAX(type_id), 0) + 1) 
from 
	types 
where 
	sys_id = @sys_id and 
	field_id = @field_id


IF (@ordering =  0)
BEGIN
    SELECT @ordering = (ISNULL(MAX(type_id),0) + 1) from types where sys_id = @sys_id and field_id = @field_id
END
INSERT INTO types
(
	sys_id,
	field_id,
	type_id,
	name,
	display_name,
	description,
	ordering,
	is_active,
	is_default,
	is_checked,
	is_private,
	is_final
)
VALUES
(
	@sys_id,
	@field_id,
	@typeID,
	@name,
	@display_name,
	@description,
	@ordering,
	@is_active,
	@is_default,
	@is_checked,
	@is_private,
	@is_final
)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_admin_set_user_password]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_admin_set_user_password]
(
	@userLogin VARCHAR(50),
	@password VARCHAR(50)
)
AS
DECLARE @already_defined NUMERIC
select @already_defined = 0;
select @already_defined = count(*) from user_passwords where user_login = @userLogin
if @already_defined = 0 
BEGIN
	insert into user_passwords (user_login, password) VALUES (@userLogin,@password)
END
ELSE
BEGIN
	update user_passwords set password=@password where user_login = @userLogin
END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_display_group_lookupByDisplayName]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
CREATE PROCEDURE [dbo].[stp_display_group_lookupByDisplayName] 
	-- Add the parameters for the stored procedure here
	@displayName 	VARCHAR(128)
AS
BEGIN
	select id, display_name, display_order, is_active 
	from display_groups
	where display_name = @displayName
END

/****** Object:  StoredProcedure [dbo].[stp_field_insertWithExistingFieldId]    Script Date: 11/11/2008 20:16:09 ******/
SET ANSI_NULLS ON
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_type_lookupAllBySystemIdAndFieldName]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_type_lookupAllBySystemIdAndFieldName]
(
	@sysId		int,
	@fieldName	varchar(255)
)
AS
SELECT 
	* 
FROM 
	types 
WHERE 
	sys_id = @sysId AND 
	field_id = (SELECT field_id FROM fields WHERE sys_id = @sysId AND  name = @fieldName) 
ORDER BY ordering

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_type_lookupBySystemId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_type_lookupBySystemId]
(
	@systemId INT
)
AS
SELECT 
	* 
FROM 
	types
WHERE
	sys_id = @systemId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_adsync_getExcludedEntries]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_adsync_getExcludedEntries]
AS
SELECT 
	u.user_login
FROM 
	exclusion_list el
	JOIN users u
	ON el.user_id = u.user_id
WHERE 
	el.sys_id = 0 AND
	el.user_type_id = -1 
ORDER BY el.user_type_id, user_login

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_type_lookupBySystemIdAndFieldIdAndTypeId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_type_lookupBySystemIdAndFieldIdAndTypeId]
(
	@systemId INT,
	@fieldId  INT,
	@typeId   INT
)
AS
SELECT 
	* 
FROM 
	types
WHERE
	sys_id = @systemId AND
	field_id = @fieldId AND
	type_id = @typeId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_adsync_insertUser]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_adsync_insertUser]
(
	@user_id		int OUTPUT,
	@user_login		nvarchar(510),
	@first_name		nvarchar(510),
	@last_name		nvarchar(510),
	@display_name		nvarchar(510),
	@email			nvarchar(510),
	@is_active		bit,
	@user_type_id		int,
	@web_config		text,
	@windows_config		text,
	@is_on_vacation		bit,
	@is_display		bit,
	@cn			varchar(255),
	@distinguished_name	varchar(4000),
	@name			varchar(255),
	@member_of		text,
	@member			text,
	@mail_nickname		varchar(255),
	@location		varchar(255),
	@extension		varchar(255),
	@mobile			varchar(255),
	@home_phone		varchar(255)
)

AS
SELECT @user_id = ISNULL(MAX(user_id), 0) + 1 FROM users
WHERE user_id < 50000

INSERT INTO users
(
	user_id,
	user_login,
	first_name,
	last_name,
	display_name,
	email,
	is_active,
	user_type_id,
	web_config,
	windows_config,
	is_on_vacation,
	is_display,
	cn,
	distinguished_name,
	name,
	member_of,
	member,
	mail_nickname,
	location,
	extension,
	mobile,
	home_phone
)
VALUES
(
	@user_id,
	@user_login,
	@first_name,
	@last_name,
	@display_name,
	@email,
	@is_active,
	@user_type_id,
	@web_config,
	@windows_config,
	@is_on_vacation,
	@is_display,
	@cn,
	@distinguished_name,
	@name,
	@member_of,
	@member,
	@mail_nickname,
	@location,
	@extension,
	@mobile,
	@home_phone
)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_type_update]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_type_update]
(
	@sys_id INT,
	@field_id INT,
	@type_id INT,
	@name NVARCHAR(255),
	@display_name NVARCHAR(255),
	@description NVARCHAR(255),
	@ordering int,
	@is_active BIT,
	@is_default BIT,
	@is_checked BIT,
	@is_private BIT,
	@is_final BIT
)
AS
UPDATE types
SET
	name = @name,
	display_name = @display_name,
	description = @description,
	is_default = @is_default,
	is_active = @is_active,
	is_checked = @is_checked,
	is_private = @is_private,
	is_final = @is_final,
        ordering = @ordering
WHERE   sys_id = @sys_id AND 
	field_id = @field_id AND
        type_id = @type_id
If @is_default = 1 
	BEGIN
		UPDATE types SET is_default = 0 WHERE sys_id = @sys_id AND field_id = @field_id AND type_id != @type_id
	END 
ELSE
	BEGIN
		DECLARE @totalCount INT
		SELECT @totalCount = (SELECT COUNT(*) FROM types WHERE sys_id = @sys_id AND field_id = @field_id)
		IF @totalCount= (SELECT COUNT(*) FROM types WHERE sys_id = @sys_id AND field_id = @field_id AND is_default=0)
		BEGIN
			UPDATE types SET is_default = 1 WHERE sys_id = @sys_id AND field_id = @field_id AND type_id = (SELECT MIN(type_id) FROM types WHERE sys_id = @sys_id AND field_id = @field_id)
		END
	END
SET QUOTED_IDENTIFIER OFF

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_typeuser_delete]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_typeuser_delete] 
(
	@sys_id INT,
	@field_id INT,
	@type_id INT,
	@user_id INT,
	@user_type_id INT,
	@notification_id INT,
	@is_volunteer BIT,
	@rr_volunteer BIT,
	@is_active BIT
)
AS
delete from type_users
where sys_id = @sys_id AND 
      field_id = @field_id AND
      type_id = @type_id AND
      user_id = @user_id
SET QUOTED_IDENTIFIER OFF

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_typeuser_getAllTypeUsers]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_typeuser_getAllTypeUsers]
AS
SELECT 
	tu.* 
FROM 
	type_users tu
	JOIN users u
	ON tu.user_id = u.user_id 
WHERE
	tu.is_active = 1 AND
	u.is_active = 1

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ba_getAnalystBusinessAreas]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_ba_getAnalystBusinessAreas] 
(
	@userId	INT
)
AS
DECLARE @userTypeId INT

SELECT @userTypeId = user_type_id FROM user_types ut where ut.name = ''Assignee''

SELECT 
	* 
FROM 
	business_areas 
WHERE 
	sys_id IN
	(
		SELECT 
			tu.sys_id
		FROM
			type_users tu
		WHERE
			tu.user_id = @userId AND
			tu.user_type_id = @userTypeId 
	) AND 
	is_active = 1

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_typeuser_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_typeuser_insert] 
(
	@sys_id INT,
	@field_id INT,
	@type_id INT,
	@user_id INT,
	@user_type_id INT,
	@notification_id INT,
	@is_volunteer BIT,
	@rr_volunteer BIT,
	@is_active BIT
)
AS
insert into type_users
(
	sys_id,
	field_id,
	type_id,
	user_id,
	user_type_id,
	notification_id,
	is_volunteer,
	rr_volunteer,
	is_active
)
values
(
	@sys_id,
	@field_id,
	@type_id,
	@user_id,
	@user_type_id,
	@notification_id,
	@is_volunteer,
	@rr_volunteer,
	@is_active
)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[reports]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[reports](
	[report_id] [int] NOT NULL,
	[report_name] [nvarchar](3000) NOT NULL,
	[description] [ntext] NULL,
	[file_name] [nvarchar](3000) NULL,
	[is_private] [bit] NOT NULL,
	[is_enabled] [bit] NOT NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_typeuser_lookupBySystemId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_typeuser_lookupBySystemId]
(
	@systemId INT
)
AS
SELECT 
	* 
FROM 
	type_users
WHERE
	sys_id = @systemId AND
	is_active = 1

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[report_roles]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[report_roles](
	[sys_id] [int] NOT NULL,
	[report_id] [int] NOT NULL,
	[role_id] [int] NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_typeuser_lookupBySystemIdAndFieldIdAndTypeId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_typeuser_lookupBySystemIdAndFieldIdAndTypeId]
(
	@systemId INT,
	@fieldId  INT,
	@typeId   INT
)
AS
SELECT 
	* 
FROM 
	type_users
WHERE
	sys_id 		= @systemId AND
	field_id 	= @fieldId AND
	type_id 	= @typeId AND 
	is_active 	= 1

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[report_specific_users]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[report_specific_users](
	[report_id] [int] NOT NULL,
	[user_id] [int] NOT NULL,
	[is_included] [bit] NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_typeuser_lookupBySystemIdAndFieldIdAndTypeIdAndUserId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_typeuser_lookupBySystemIdAndFieldIdAndTypeIdAndUserId]
(
	@systemId	INT,
	@fieldId	INT,
	@typeId	INT,
	@userId	INT
)
AS
SELECT 
	*
FROM
	type_users
WHERE
	sys_id	  = @systemId AND
	field_id  = @fieldId  AND
	type_id	  = @typeId   AND
	user_id	  = @userId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_typeuser_lookupVolunteersBySystemIdAndFieldIdAndTypeId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_typeuser_lookupVolunteersBySystemIdAndFieldIdAndTypeId]
(
	@systemId INT,
	@fieldId  INT,
	@typeId   INT
)
AS
SELECT 
	* 
FROM 
	type_users
WHERE
	sys_id 		= @systemId AND
	field_id 	= @fieldId AND
	type_id 	= @typeId AND
	is_volunteer 	= 1 AND
	is_active 	= 1

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_typeuser_update]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_typeuser_update] 
(
	@sys_id INT,
	@field_id INT,
	@type_id INT,
	@user_id INT,
	@user_type_id INT,
	@notification_id INT,
	@is_volunteer BIT,
	@rr_volunteer BIT,
	@is_active BIT
)
AS
UPDATE type_users
SET
	notification_id = @notification_id,
	is_volunteer = @is_volunteer,
	rr_volunteer = @rr_volunteer,
	is_active = @is_active,
	user_type_id = @user_type_id 
WHERE sys_id = @sys_id
 AND field_id = @field_id
 AND type_id = @type_id
 AND user_id = @user_id



' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ura_lookupBySystemIdAndRequestIdAndUserId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_ura_lookupBySystemIdAndRequestIdAndUserId]
(
	@systemId 	INT,
	@requestId	INT,
	@userId	INT
)
AS
SELECT * FROM user_read_actions 
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId AND
	user_id		= @userId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ura_registerUserReadAction]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_ura_registerUserReadAction]
(
	@systemId	INT,
	@requestId	INT,
	@actionId	INT,
	@userId		INT
)
AS

/*
* If a Record with larger or equal actionId is present, simply return
*/
declare @prevActionId INT
SELECT @prevActionId = 0

SELECT @prevActionId = action_id 
	FROM 
		user_read_actions 
	WHERE
		sys_id 		= @systemId 	AND
		request_id	= @requestId 	AND
		user_id 	= @userId 

 if (@prevActionId >= @actionId)
return


/*
 * Check if a record already exists.
 */
IF EXISTS 
	(
	SELECT 
		* 
	FROM 
		user_read_actions 
	WHERE
		sys_id 		= @systemId 	AND
		request_id	= @requestId 	AND
		user_id 	= @userId
	)
BEGIN
	/*
	 * Update the record if it already exists.
	 */
	UPDATE user_read_actions
	SET
		action_id = @actionId
	WHERE
		sys_id 		= @systemId 	AND
		request_id	= @requestId 	AND
		user_id 	= @userId
END
ELSE
BEGIN
	/*
	 * Insert the record if it does not exist.
	 */
	INSERT INTO user_read_actions
	(
		sys_id, 
		request_id, 
		action_id, 
		user_id
	)
	VALUES
	(
		@systemId, 
		@requestId, 
		@actionId, 
		@userId
	)
END

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ura_removeRequestEntry]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_ura_removeRequestEntry]
(
	@systemId	INT,
	@requestId	INT
)
AS
DELETE  user_read_actions 
WHERE
	sys_id 	   = @systemId AND
	request_id = @requestId

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_draft_delete]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_user_draft_delete]
(
	@userId             INT,
	@sysId              INT,
	@draftId      INT
)
AS
delete from  user_drafts 
where
	user_id = @userId And
	sys_id=@sysId And 
	draft_id = @draftId
' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_draft_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_user_draft_insert]
(
	@userId           INT,
	@timeStamp  datetime,
	@sysId              INT,
	@requestId      INT,
	@draft               text,
	@draftid	INT OUT
)
AS
declare @maxDraftId INT;
select @maxDraftId=ISNULL(max(draft_id),0) from user_drafts;
select @draftId = @maxDraftId + 1;
--delete from  user_drafts 
--where
--	user_id = @userId And
--	sys_id=@sysId And 
--	request_id = @requestId And
--	convert(smallDateTime, time_stamp) = convert(smallDateTime, @timeStamp) AND
--	abs(Datepart(millisecond,time_stamp) - Datepart(millisecond,@timeStamp)) < 10
	

INSERT INTO user_drafts
(
	user_id,
	time_stamp,
	sys_id, 
	request_id,
	draft,
	draft_id
)
VALUES
(
	@userId,
	@timeStamp,
	@sysId,
	@requestId,
	@draft,
	@draftId
)


' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_draft_lookupByUserId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_user_draft_lookupByUserId] 
(
	@userid	INT
)
AS
SELECT 
	*
FROM 
	user_drafts ud
WHERE
	user_id = @userId

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_draft_lookupByUserIdAndSystemIdAndDraftId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_user_draft_lookupByUserIdAndSystemIdAndDraftId]
(
	@userId	INT,
	@systemId 	INT,
	@draftId	INT
	
)
AS
SELECT * FROM user_drafts
WHERE
	sys_id 		= @systemId AND
	draft_id = @draftId AND
	user_id		= @userId

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_draft_lookupByUserIdAndSystemIdAndRequestId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_user_draft_lookupByUserIdAndSystemIdAndRequestId]
(
	@userId	INT,
	@systemId 	INT,
	@requestId	INT
	
)
AS
SELECT * FROM user_drafts
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId AND
	user_id		= @userId

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_draft_lookupByUserIdAndSystemIdAndRequestIdAndTimestamp]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_user_draft_lookupByUserIdAndSystemIdAndRequestIdAndTimestamp]
(
	@userId	INT,
	@systemId 	INT,
	@requestId	INT,
	@timeStamp     DATETIME
	
)
AS
SELECT * FROM user_drafts
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId AND
	user_id		= @userId AND
	convert(smallDateTime, time_stamp) = convert(smallDateTime, @timeStamp) AND
	abs(Datepart(millisecond,time_stamp) - Datepart(millisecond,@timeStamp)) < 10

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_draft_update]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_user_draft_update]
(
	@userId         INT,
	@timeStamp  	DATETIME,
	@sysId          INT,
	@requestId      INT,
	@draft          TEXT,
	@draftId	INT
)
AS

update user_drafts 
set  
	draft = @draft
where
	user_id 	= @userId And
	sys_id 		= @sysId And 
	draft_id 	= @draftId


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_dbsync_insertUser]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_dbsync_insertUser]
(
	@user_id		int,
	@user_login		nvarchar(512),
	@first_name		nvarchar(512),
	@last_name		nvarchar(512),
	@display_name		nvarchar(512),
	@email			nvarchar(512),
	@is_active		bit,
	@user_type_id		int,
	@web_config		text,
	@windows_config		text,
	@is_on_vacation		bit,
	@is_display		bit,
	@cn			nvarchar(512),
	@distinguished_name	nvarchar(512),
	@name			nvarchar(512),
	@member_of		text,
	@member			text,
	@mail_nickname		nvarchar(512),
	@location		varchar(255),
	@extension		varchar(255),
	@mobile			varchar(255),
	@home_phone		varchar(255)
)
AS

INSERT INTO users
(
	user_id,
	user_login,
	first_name,
	last_name,
	display_name,
	email,
	is_active,
	user_type_id,
	web_config,
	windows_config,
	is_on_vacation,
	is_display,
	cn,
	distinguished_name,
	name,
	member_of,
	member,
	mail_nickname,
	location,
	extension,
	mobile,
	home_phone
)
VALUES
(
	@user_id,
	@user_login,
	@first_name,
	@last_name,
	@display_name,
	@email,
	@is_active,
	@user_type_id,
	@web_config,
	@windows_config,
	@is_on_vacation,
	@is_display,
	@cn,
	@distinguished_name,
	@name,
	@member_of,
	@member,
	@mail_nickname,
	@location,
	@extension,
	@mobile,
	@home_phone
)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_getAllUsers]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_user_getAllUsers]
AS
SELECT 
	user_id,
	user_login,
	first_name,
	last_name,
	display_name,
	email,
	is_active,
	user_type_id,
	web_config,
	windows_config,
	is_on_vacation,
	is_display,
	cn,
	distinguished_name,
	name,
	member_of,
	member,
	mail_nickname,
	location,
	extension,
	mobile,
	home_phone
FROM 
	users

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_expandMailingListByEmail]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_user_expandMailingListByEmail]
(
	@mailListName VARCHAR(256)
)
AS

/*
 * If there is a * at the start, remove it and prepend % for like search.
 */
IF (CHARINDEX(''*'', @mailListName) = 1)
BEGIN
	SELECT @mailListName = ''%'' + SUBSTRING(@mailListName, 2, LEN(@mailListName))
END
/*
 * If there is a * at the end, remove it
 */
IF (CHARINDEX(''*'', @mailListName) = LEN(@mailListName))
BEGIN
	SELECT @mailListName = SUBSTRING(@mailListName, 1, LEN(@mailListName) - 1)
END

-- Append % for like search.
SELECT @mailListName = @mailListName + ''%''

CREATE TABLE #tmp1
(
	user_id INT,
	user_login VARCHAR(256),
	user_type_id INT
)
CREATE TABLE #tmp2
(
	user_id INT,
	user_login VARCHAR(256),
	user_type_id INT
)
/**
 * #tmp1 contains the internal_users
 * #tmp2 contains the internal_mailing_lists
 */
INSERT INTO #tmp1 
SELECT 
	ml.user_id,
	u.user_login,
	u.user_type_id
FROM 
	mail_list_users ml
	JOIN users uc
	ON ml.mail_list_id = uc.user_id
	JOIN users u
	ON ml.user_id = u.user_id
WHERE
	uc.email like @mailListName AND
	u.user_type_id <> 8
INSERT INTO #tmp2
SELECT 
	ml.user_id,
	u.user_login,
	u.user_type_id
FROM 
	mail_list_users ml
	JOIN users u
	ON ml.user_id = u.user_id
	JOIN users uc
	ON ml.mail_list_id = uc.user_id
WHERE
	uc.email like @mailListName AND
	u.user_type_id = 8
/**
 * As long as there are mailing lists in #tmp2, continue with resolving process.
 */
WHILE (EXISTS (SELECT * FROM #tmp2))
BEGIN
	/**
         * Get the list of users present in the mailing lists in #tmp2 who are not present in #tmp1
	 */
	SELECT
		ml.user_id,
		u.user_login,
		u.user_type_id
	INTO #tmp3
	FROM
		mail_list_users ml
		LEFT JOIN users u
		ON ml.user_id = u.user_id
	WHERE
		ml.mail_list_id IN (SELECT user_id FROM #tmp2 WHERE user_type_id = 8) AND
		u.user_id NOT IN (SELECT user_id FROM #tmp1 WHERE user_type_id = 7)
	/**
	 * Insert all the users into #tmp1.
	 * Insert all the mailinglists at this level into #tmp2 after clearing the table.
	 */
	INSERT INTO #tmp1 SELECT * FROM #tmp3 WHERE user_type_id <> 8
	-- Clear the earlier mailing lists
	DELETE #tmp2
	-- Get the fresh list of mailing lists found at this level.
	INSERT INTO #tmp2 SELECT * FROM #tmp3 WHERE user_type_id = 8
	-- Delete #tmp3.
	DROP TABLE #tmp3
END
/**
 * #tmp1 contains the final list of users for the given mailing list.
 */
SELECT DISTINCT user_id FROM #tmp1

-- Drop the temporary tables.
DROP TABLE #tmp2
DROP TABLE #tmp1

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_insertExternalUser]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_user_insertExternalUser]
(
	@user_id		int OUTPUT,
	@user_login		nvarchar(510),
	@first_name		nvarchar(510),
	@last_name		nvarchar(510),
	@display_name		nvarchar(510),
	@email			nvarchar(510),
	@is_active		bit,
	@user_type_id		int,
	@web_config		text,
	@windows_config		text,
	@is_on_vacation		bit,
	@is_display		bit,
	@cn			varchar(255),
	@distinguished_name	varchar(4000),
	@name			varchar(255),
	@member_of		text,
	@member			text,
	@mail_nickname		varchar(255),
	@location		varchar(255),
	@extension		varchar(255),
	@mobile			varchar(255),
	@home_phone		varchar(255)
)
AS

SELECT @user_id = ISNULL(max(user_id), 50000) FROM users WHERE user_id >=50000
SELECT @user_id = @user_id + 1

INSERT INTO users
(
	user_id,
	user_login,
	first_name,
	last_name,
	display_name,
	email,
	is_active,
	user_type_id,
	web_config,
	windows_config,
	is_on_vacation,
	is_display,
	cn,
	distinguished_name,
	name,
	member_of,
	member,
	mail_nickname,
	location,
	extension,
	mobile,
	home_phone
)
VALUES
(
	@user_id,
	@user_login,
	@first_name,
	@last_name,
	@display_name,
	@email,
	@is_active,
	@user_type_id,
	@web_config,
	@windows_config,
	@is_on_vacation,
	@is_display,
	@cn,
	@distinguished_name,
	@name,
	@member_of,
	@member,
	@mail_nickname,
	@location,
	@extension,
	@mobile,
	@home_phone
)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_insertMinUser]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_user_insertMinUser]
(

/*
 * Inserts a user with min required values
 */
	@user_login		nvarchar(510),
	@email			nvarchar(510)
)

AS
declare @user_id int
SELECT @user_id = ISNULL(MAX(user_id), 0) + 1 FROM users

INSERT INTO users
(
	user_id,
	user_login,
	email,
	is_active
)
VALUES
(
	@user_id,
	@user_login,
	@email,
	1
)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_display_group_delete]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[stp_display_group_delete]
	-- Add the parameters for the stored procedure here
	@sys_id int,
	@id int,
	@display_name varchar(50),
	@display_order int,
	@is_active bit,
	@ret_value int output
AS
BEGIN
	delete from display_groups where id = @id;
	select @ret_value = 1;
END
' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER OFF
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_lookupByLoginPassword]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
CREATE  PROCEDURE [dbo].[stp_user_lookupByLoginPassword]
(
	@userLogin VARCHAR(50),
	@password VARCHAR(50)
)
AS
SELECT 
	user_login
FROM 
	user_passwords
WHERE
	user_login = @userLogin
	and password = @password
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_display_group_lookupAll]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author:		Sandeep Giri
-- Create date: 3 Jan 2007
-- Description:	Returns all display groups
-- =============================================
CREATE PROCEDURE [dbo].[stp_display_group_lookupAll] 
	-- Add the parameters for the stored procedure here
AS
BEGIN
	select id, display_name, display_order, is_active from display_groups
END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_display_group_reorder_sys_id_column]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE procedure [dbo].[stp_display_group_reorder_sys_id_column]
as
Begin
ALTER TABLE [dbo].[display_groups] DROP CONSTRAINT [DF_display_groups_is_active]
ALTER TABLE [dbo].[display_groups] DROP CONSTRAINT [DF_display_groups_display_order]

CREATE TABLE dbo.Tmp_display_groups
	(
	 sys_id int NULL,
	 id int NOT NULL IDENTITY (1, 1),
	 display_name varchar(50) NULL,
	 display_order int NULL CONSTRAINT DF_display_groups_display_order DEFAULT ((0)),
	 is_active bit NOT NULL CONSTRAINT DF_display_groups_is_active DEFAULT ((1))
	)
SET IDENTITY_INSERT dbo.Tmp_display_groups ON

IF EXISTS(SELECT * FROM dbo.display_groups)
	 EXEC(''INSERT INTO dbo.Tmp_display_groups (sys_id, id, display_name, display_order, is_active)
		SELECT sys_id, id, display_name, display_order, is_active FROM dbo.display_groups WITH (HOLDLOCK TABLOCKX)'')

SET IDENTITY_INSERT dbo.Tmp_display_groups OFF

DROP TABLE dbo.display_groups

EXECUTE sp_rename N''dbo.Tmp_display_groups'', N''display_groups'', ''OBJECT''

ALTER TABLE dbo.display_groups ADD CONSTRAINT
	PK_display_groups PRIMARY KEY CLUSTERED 
	(
	id
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) 
END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_lookupByUserId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_user_lookupByUserId]
(
	@userId INT
)
AS
SELECT 
	user_id,
	user_login,
	first_name,
	last_name,
	display_name,
	email,
	is_active,
	user_type_id,
	web_config,
	windows_config,
	is_on_vacation,
	is_display,
	cn,
	distinguished_name,
	name,
	member_of,
	member,
	mail_nickname,
	location,
	extension,
	mobile,
	home_phone
FROM 
	users
WHERE
	user_id = @userId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_lookupByEmail]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_user_lookupByEmail]
(
	@email VARCHAR(512)
)
AS
SELECT 
	user_id,
	user_login,
	first_name,
	last_name,
	display_name,
	email,
	is_active,
	user_type_id,
	web_config,
	windows_config,
	is_on_vacation,
	is_display,
	cn,
	distinguished_name,
	name,
	member_of,
	member,
	mail_nickname,
	location,
	extension,
	mobile,
	home_phone
FROM 
	users
WHERE
	email = @email

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[display_groups]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[display_groups](
	[sys_id] [int] NULL,
	[id] [int] IDENTITY(1,1) NOT NULL,
	[display_name] [varchar](50) NULL,
	[display_order] [int] NULL CONSTRAINT [DF_display_groups_display_order]  DEFAULT ((0)),
	[is_active] [bit] NOT NULL CONSTRAINT [DF_display_groups_is_active]  DEFAULT ((1)),
 CONSTRAINT [PK_display_groups] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_lookupByUserLogin]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_user_lookupByUserLogin]
(
	@userLogin VARCHAR(512)
)
AS
SELECT 
	user_id,
	user_login,
	first_name,
	last_name,
	display_name,
	email,
	is_active,
	user_type_id,
	web_config,
	windows_config,
	is_on_vacation,
	is_display,
	cn,
	distinguished_name,
	name,
	member_of,
	member,
	mail_nickname,
	location,
	extension,
	mobile,
	home_phone
FROM 
	users
WHERE
	user_login = @userLogin

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_lookupByUserLoginLike]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_user_lookupByUserLoginLike]
(
	@userLogin	VARCHAR(256),
	@noInActive	BIT
)
AS

IF (@noInActive = 0)
BEGIN
	SELECT 
		user_id
	FROM 
		users
	WHERE
		user_login LIKE @userLogin + ''%''
END
ELSE
BEGIN
	SELECT 
		user_id
	FROM 
		users
	WHERE
		user_login LIKE @userLogin + ''%'' AND
		is_active = 1
END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_user_update]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_user_update]
(
	@user_id		int,
	@user_login		nvarchar(510),
	@first_name		nvarchar(510),
	@last_name		nvarchar(510),
	@display_name		nvarchar(510),
	@email			nvarchar(510),
	@is_active		bit,
	@user_type_id		int,
	@web_config		text,
	@windows_config	text,
	@is_on_vacation	bit,
	@is_display		bit,
	@cn			varchar(255),
	@distinguished_name	varchar(4000),
	@name			varchar(255),
	@member_of		text,
	@member		text,
	@mail_nickname	varchar(255),
	@location		varchar(255),
	@extension		varchar(255),
	@mobile		varchar(255),
	@home_phone		varchar(255)
)

AS
UPDATE users
SET 
	user_login 		= @user_login,
	first_name 		= @first_name,
	last_name  		= @last_name,
	display_name  		= @display_name,
	email  			= @email,
	is_active  		= @is_active,
	user_type_id  		= @user_type_id,
	web_config  		= @web_config,
	windows_config  	= @windows_config,
	is_on_vacation  	= @is_on_vacation,
	is_display  		= @is_display,
	cn  			= @cn,
	distinguished_name  	= @distinguished_name,
	name  			= @name,
	member_of  		= @member_of,
	member  		= @member,
	mail_nickname 		= @mail_nickname,
	location			= @location,
	extension		= @extension,
	mobile			= @mobile,
	home_phone		= @home_phone
WHERE
	user_id = @user_id

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_wr_getAllWorkflowRules]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_wr_getAllWorkflowRules]
AS

SELECT 
	* 
FROM 
	workflow_rules

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_wr_lookupByRuleId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_wr_lookupByRuleId]
(
	@ruleId	INT
)
AS
SELECT
	*
FROM
	workflow_rules
WHERE
	rule_id = @ruleId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[action_users]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[action_users](
	[sys_id] [int] NOT NULL,
	[request_id] [int] NOT NULL,
	[action_id] [int] NOT NULL,
	[user_type_id] [int] NOT NULL,
	[user_id] [int] NOT NULL,
	[ordering] [int] NULL,
	[is_primary] [int] NOT NULL,
 CONSTRAINT [PK_action_users] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[request_id] ASC,
	[action_id] ASC,
	[user_type_id] ASC,
	[user_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[super_users]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[super_users](
	[user_id] [int] NOT NULL,
	[is_active] [bit] NOT NULL,
 CONSTRAINT [PK_super_users] PRIMARY KEY CLUSTERED 
(
	[user_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[actions]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[actions](
	[sys_id] [int] NOT NULL,
	[request_id] [int] NOT NULL,
	[action_id] [int] NOT NULL,
	[category_id] [int] NOT NULL,
	[status_id] [int] NOT NULL,
	[severity_id] [int] NOT NULL,
	[request_type_id] [int] NOT NULL,
	[subject] [nvarchar](2048) NOT NULL,
	[description] [ntext] NOT NULL,
	[is_private] [bit] NOT NULL,
	[parent_request_id] [int] NOT NULL,
	[user_id] [int] NOT NULL,
	[due_datetime] [datetime] NULL,
	[logged_datetime] [datetime] NOT NULL,
	[lastupdated_datetime] [datetime] NOT NULL,
	[header_description] [ntext] NULL,
	[attachments] [ntext] NULL,
	[summary] [ntext] NULL,
	[memo] [ntext] NULL,
	[append_interface] [int] NOT NULL,
	[notify] [int] NOT NULL,
	[notify_loggers] [bit] NOT NULL,
	[replied_to_action] [int] NULL,
	[office_id] [int] NOT NULL CONSTRAINT [DF__actions__office___17635F73]  DEFAULT ((0)),
 CONSTRAINT [PK_actions] PRIMARY KEY CLUSTERED 
(
	[sys_id] ASC,
	[request_id] ASC,
	[action_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[tracking_options]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[tracking_options](
	[tracking_option_id] [int] NOT NULL,
	[description] [varchar](1024) NOT NULL,
 CONSTRAINT [PK_tracking_options] PRIMARY KEY CLUSTERED 
(
	[tracking_option_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[max_ids]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[max_ids](
	[name] [varchar](50) NULL,
	[id] [int] NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[transferred_requests]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[transferred_requests](
	[source_prefix] [varchar](50) NOT NULL,
	[source_request_id] [int] NOT NULL,
	[target_prefix] [varchar](50) NULL,
	[target_request_id] [int] NULL,
 CONSTRAINT [PK_transferred_requests] PRIMARY KEY CLUSTERED 
(
	[source_prefix] ASC,
	[source_request_id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_TRIGGERS]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[QRTZ_TRIGGERS](
	[TRIGGER_NAME] [varchar](80) NOT NULL,
	[TRIGGER_GROUP] [varchar](80) NOT NULL,
	[JOB_NAME] [varchar](80) NOT NULL,
	[JOB_GROUP] [varchar](80) NOT NULL,
	[IS_VOLATILE] [varchar](1) NOT NULL,
	[DESCRIPTION] [varchar](120) NULL,
	[NEXT_FIRE_TIME] [bigint] NULL,
	[PREV_FIRE_TIME] [bigint] NULL,
	[TRIGGER_STATE] [varchar](16) NOT NULL,
	[TRIGGER_TYPE] [varchar](8) NOT NULL,
	[START_TIME] [bigint] NOT NULL,
	[END_TIME] [bigint] NULL,
	[CALENDAR_NAME] [varchar](80) NULL,
	[MISFIRE_INSTR] [smallint] NULL,
 CONSTRAINT [PK_QRTZ_TRIGGERS] PRIMARY KEY CLUSTERED 
(
	[TRIGGER_NAME] ASC,
	[TRIGGER_GROUP] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_JOB_LISTENERS]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[QRTZ_JOB_LISTENERS](
	[JOB_NAME] [varchar](80) NOT NULL,
	[JOB_GROUP] [varchar](80) NOT NULL,
	[JOB_LISTENER] [varchar](80) NOT NULL,
 CONSTRAINT [PK_QRTZ_JOB_LISTENERS] PRIMARY KEY CLUSTERED 
(
	[JOB_NAME] ASC,
	[JOB_GROUP] ASC,
	[JOB_LISTENER] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_JOB_DATA_MAP]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[QRTZ_JOB_DATA_MAP](
	[JOB_NAME] [varchar](80) NOT NULL,
	[JOB_GROUP] [varchar](80) NOT NULL,
	[ENTRY] [varchar](100) NOT NULL,
	[VALUE] [varchar](300) NOT NULL,
 CONSTRAINT [PK_QRTZ_JOB_DATA_MAP] PRIMARY KEY CLUSTERED 
(
	[JOB_NAME] ASC,
	[JOB_GROUP] ASC,
	[ENTRY] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_CRON_TRIGGERS]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[QRTZ_CRON_TRIGGERS](
	[TRIGGER_NAME] [varchar](80) NOT NULL,
	[TRIGGER_GROUP] [varchar](80) NOT NULL,
	[CRON_EXPRESSION] [varchar](80) NOT NULL,
	[TIME_ZONE_ID] [varchar](80) NULL,
 CONSTRAINT [PK_QRTZ_CRON_TRIGGERS] PRIMARY KEY CLUSTERED 
(
	[TRIGGER_NAME] ASC,
	[TRIGGER_GROUP] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_TRIGGER_LISTENERS]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[QRTZ_TRIGGER_LISTENERS](
	[TRIGGER_NAME] [varchar](80) NOT NULL,
	[TRIGGER_GROUP] [varchar](80) NOT NULL,
	[TRIGGER_LISTENER] [varchar](80) NOT NULL,
 CONSTRAINT [PK_QRTZ_TRIGGER_LISTENERS] PRIMARY KEY CLUSTERED 
(
	[TRIGGER_NAME] ASC,
	[TRIGGER_GROUP] ASC,
	[TRIGGER_LISTENER] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_SIMPLE_TRIGGERS]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[QRTZ_SIMPLE_TRIGGERS](
	[TRIGGER_NAME] [varchar](80) NOT NULL,
	[TRIGGER_GROUP] [varchar](80) NOT NULL,
	[REPEAT_COUNT] [bigint] NOT NULL,
	[REPEAT_INTERVAL] [bigint] NOT NULL,
	[TIMES_TRIGGERED] [bigint] NOT NULL,
 CONSTRAINT [PK_QRTZ_SIMPLE_TRIGGERS] PRIMARY KEY CLUSTERED 
(
	[TRIGGER_NAME] ASC,
	[TRIGGER_GROUP] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tbits_advancedActOnGroup]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_tbits_advancedActOnGroup]
(
	@systemId	INT,
	@requestList	VARCHAR(7999),
	@isPrivate	VARCHAR(255),
	@category	VARCHAR(255),
	@status  	VARCHAR(255),
	@severity	VARCHAR(255),
	@requestType	VARCHAR(255),
	@description	VARCHAR(7999),
	@userId		INT,
	@rejectedList	VARCHAR(7999) OUTPUT
)
AS
DECLARE @catFieldId		INT,
	@statFieldId		INT,
	@sevFieldId		INT,
	@reqTypeFieldId		INT,
	@catFieldName		VARCHAR(255),
	@statFieldName		VARCHAR(255),
	@sevFieldName		VARCHAR(255),
	@reqTypeFieldName	VARCHAR(255),
	@confFieldId		INT
DECLARE @categoryId	INT,
	@statusId	INT,
	@severityId	INT,
	@requestTypeId	INT,
	@isPrivateValue	INT
DECLARE @index		INT,
	@requestId	INT
SELECT @rejectedList = ''''
-- Get the Field ID of status. and typeId of closed.
SELECT @catFieldId = field_id, @catFieldName = display_name FROM fields WHERE sys_id = @systemId AND name = ''category_id''
SELECT @statFieldId = field_id, @statFieldName = display_name FROM fields WHERE sys_id = @systemId AND name = ''status_id''
SELECT @sevFieldId = field_id, @sevFieldName = display_name FROM fields WHERE sys_id = @systemId AND name = ''severity_id''
SELECT @reqTypeFieldId = field_id, @reqTypeFieldName = display_name FROM fields WHERE sys_id = @systemId AND name = ''request_type_id''
SELECT @confFieldId = field_id FROM fields WHERE sys_id = @systemId AND name = ''is_private''
--- Trim the input params
SELECT @category = ltrim(rtrim(@category))
SELECT @status 	= ltrim(rtrim(@status))
SELECT @severity = ltrim(rtrim(@severity))
SELECT @requestType = ltrim(rtrim(@requestType))
-- Get the corresponding type ids if they are not empty.
SELECT @categoryId = -1
SELECT @statusId = -1
SELECT @severityId = -1
SELECT @requestTypeId = -1
--- Get the Category.
IF (@category != '''')
BEGIN
	SELECT 
		@categoryId = ISNULL(type_id, -1),
		@category = ISNULL(display_name, @category) 
	FROM 
		types 
	WHERE 
		sys_id = @systemId AND 
		field_id = @catFieldId AND 
		(
			name = @category OR
			display_name = @category
		)
END
--- Get the status.
IF (@status != '''')
BEGIN
	SELECT 
		@statusId = ISNULL(type_id, -1) ,
		@status = ISNULL(display_name, @status) 
	FROM 
		types 
	WHERE 
		sys_id = @systemId AND 
		field_id = @statFieldId AND 
		(
			name = @status OR
			display_name = @status
		)
END
--- Get the Severity.
IF (@severity != '''')
BEGIN
	SELECT 
		@severityId = ISNULL(type_id, -1) ,
		@severity = ISNULL(display_name, @severity) 
	FROM 
		types 
	WHERE 
		sys_id = @systemId AND 
		field_id = @sevFieldId AND 
		(
			name = @severity OR
			display_name = @severity
		)
END
--- Get the Request Type.
IF (@requestType != '''')
BEGIN
	SELECT 
		@requestTypeId = ISNULL(type_id, -1) ,
		@requestType = ISNULL(display_name, @requestType) 
	FROM 
		types 
	WHERE 
		sys_id = @systemId AND 
		field_id = @reqTypeFieldId AND 
		(
			name = @requestType OR
			display_name = @requestType
		)
END
SELECT @isPrivateValue = 
	CASE @isPrivate 
		WHEN ''none'' THEN -1
		WHEN ''private'' THEN 1
		WHEN ''public'' THEN 0
		ELSE -1
	END
PRINT ''
Category	: '' + CONVERT(VARCHAR, @categoryId) + ''
Status  	: '' + CONVERT(VARCHAR, @statusId) + ''
Severity 	: '' + CONVERT(VARCHAR, @severityId) + ''
RequestType	: '' + CONVERT(VARCHAR, @requestTypeId) + ''
Private		: '' + CONVERT(VARCHAR, @isPrivateValue)
IF (@categoryId = -1 AND @statusId = -1 AND @severityId = -1 AND @requestTypeId = -1 AND @isPrivateValue = -1)
BEGIN
	SELECT @rejectedList = @requestList
	RETURN
END
WHILE (@requestList <> '''')
BEGIN
	SELECT @index = charindex('','', @requestList)
	IF (@index > 0) 
	BEGIN
		SELECT @requestId = CONVERT ( INT, substring(@requestList, 0, @index) )
		SELECT @requestList = substring ( @requestList, @index + 1, len(@requestList))
	END
	ELSE 
	BEGIN
		SELECT @requestId = CONVERT ( INT, @requestList )	
		SELECT @requestList = ''''
	END
	/*
	   Get the following information of this request:
		1. MaxActionId
		2. Current Category Id
		3. Current Status Id
		4. Current Severity Id
		5. Current Request Type Id.
		6. Current Private Value.
	 */
	DECLARE @maxActionId 		INT,
		@curCatId		INT,
		@curStatId		INT,
		@curSevId		INT,
		@curReqTypeId		INT,
		@curPrivateValue	BIT,
		@curCatName		VARCHAR(255),
		@curStatName		VARCHAR(255),
		@curSevName		VARCHAR(255),
		@curReqTypeName		VARCHAR(255),
		@actionId 		INT,
		@headerDesc 		VARCHAR(1000),
		@continue		BIT
	SELECT @continue = 0
	SELECT 
		@curCatId 	= category_id,
		@curStatId	= status_id,
		@curSevId	= severity_id,
		@curReqTypeId	= request_type_id,
		@curPrivateValue= is_private,
		@maxActionId 	= max_action_id 
	FROM 
		requests 
	WHERE 
		sys_id = @systemId AND 
		request_id = @requestId
	-- Increment the max_action_id to get the actionid.
	SELECT @actionId = @maxActionId + 1
	-- Generate the header description.
	SELECT @headerDesc = ''''
	IF (@categoryId != -1 AND @categoryId <> @curCatId)
	BEGIN
		SELECT @curCatName = display_name FROM types WHERE sys_id = @systemId AND field_id = @catFieldId AND type_id = @curCatId
		SELECT @headerDesc = @headerDesc + ''category_id##'' + CONVERT(VARCHAR, @catFieldId) + ''##[ '' + @catFieldName +  '' changed from '''''' + 
				    @curCatName + '''''' to '''''' + @category + '''''' ] '' + char(10)
		SELECT @continue = 1
	END
	IF (@statusId != -1 AND @statusId <> @curStatId)
	BEGIN
		SELECT @curStatName = display_name FROM types WHERE sys_id = @systemId AND field_id = @statFieldId AND type_id = @curStatId
		IF (@status = ''closed'')
		BEGIN
			SELECT @headerDesc = @headerDesc + ''status_id##'' + CONVERT(VARCHAR, @statFieldId) + ''##[ Closed ] '' + char(10)
		END
		ELSE
		BEGIN
			SELECT @headerDesc = @headerDesc + ''status_id##'' + CONVERT(VARCHAR, @statFieldId) + ''##[ '' + @statFieldName +  '' changed from '''''' + 
					    @curStatName + '''''' to '''''' + @status + '''''' ] '' + char(10)
		END
		SELECT @continue = 1
	END
	IF (@severityId != -1 AND @severityId <> @curSevId)
	BEGIN
		SELECT @curSevName = display_name FROM types WHERE sys_id = @systemId AND field_id = @sevFieldId AND type_id = @curSevId
		SELECT @headerDesc = @headerDesc + ''severity_id##'' + CONVERT(VARCHAR, @sevFieldId) + ''##[ '' + @sevFieldName +  '' changed from '''''' + 
				    @curSevName + '''''' to '''''' + @severity + '''''' ] '' + char(10)
		SELECT @continue = 1
	END
	IF (@requestTypeId != -1 AND @requestTypeId <> @curReqTypeId)
	BEGIN
		SELECT @curReqTypeName = display_name FROM types WHERE sys_id = @systemId AND field_id = @reqTypeFieldId AND type_id = @curReqTypeId
		SELECT @headerDesc = @headerDesc + ''request_type_id##'' + CONVERT(VARCHAR, @reqTypeFieldId) + ''##[ '' + @reqTypeFieldName +  '' changed from '''''' + 
				     @curReqTypeName + '''''' to '''''' + @requestType + '''''' ] '' + char(10)
		SELECT @continue = 1
	END
	IF (@isPrivateValue != -1 AND @isPrivateValue <> @curPrivateValue)
	BEGIN
		IF (@isPrivateValue = 1)
		BEGIN
			SELECT @headerDesc = @headerDesc + ''is_private##'' + CONVERT(VARCHAR, @confFieldId) + ''##[ Marked private ]'' + char(10)
			SELECT @continue = 1
		END
		IF (@isPrivateValue = 0)
		BEGIN
			SELECT @headerDesc = @headerDesc + ''is_private##'' + CONVERT(VARCHAR, @confFieldId) + ''##[ Marked public ]'' + char(10)
			SELECT @continue = 1
		END
	END
	-- If there is no change in any of the above five fields, there is no point in just appending.
	-- So, add this request to the rejected list and continue.
	IF (@continue = 0)
	BEGIN
		IF (@rejectedList = '''') 
		BEGIN
			SELECT @rejectedList = CONVERT(VARCHAR, @requestId)
		END
		ELSE 
		BEGIN
			SELECT @rejectedList = @rejectedList + '','' + CONVERT(VARCHAR, @requestId)
		END
		-- Continue with the next request id.
		CONTINUE
	END
	SELECT @headerDesc = @headerDesc + ''[ No email notification ] '' + char(10)
	-- Update the request
	UPDATE requests
	SET
		category_id = 
			CASE @categoryId
				WHEN -1 THEN category_id
				ELSE @categoryId
			END,
		status_id = 
			CASE @statusId
				WHEN -1 THEN status_id
				ELSE @statusId
			END,
		severity_id = 
			CASE @severityId
				WHEN -1 THEN severity_id
				ELSE @severityId
			END,
		request_type_id = 
			CASE @requestTypeId
				WHEN -1 THEN request_type_id
				ELSE @requestTypeId
			END,
		is_private = 
			CASE @isPrivateValue
				WHEN -1 THEN is_private
				ELSE @isPrivateValue
			END,
		description = @description,
		user_id = @userId,
		lastupdated_datetime = GETUTCDATE(),
		max_action_id = @actionId
	WHERE
		sys_id = @systemId AND
		request_id = @requestId
	-- Insert the corresponding actions records
	INSERT INTO actions 
	(
		sys_id,
		request_id,
		action_id,
		category_id,
		status_id,
		severity_id,
		request_type_id,
		subject,
		description,
		is_private,
		parent_request_id,
		user_id,
		due_datetime,
		logged_datetime,
		lastupdated_datetime,
		header_description,
		attachments,
		summary,
		memo,
		append_interface,
		notify,
		notify_loggers,
		replied_to_action
	)
	SELECT 
		sys_id,
		request_id,
		@actionId,
		CASE @categoryId
			WHEN -1 THEN category_id
			ELSE @categoryId
		END,
		CASE @statusId
			WHEN -1 THEN status_id
			ELSE @statusId
		END,
		CASE @severityId
			WHEN -1 THEN severity_id
			ELSE @severityId
		END,
		CASE @requestTypeId
			WHEN -1 THEN request_type_id
			ELSE @requestTypeId
		END,
		subject,
		@description, 
		CASE @isPrivateValue
			WHEN -1 THEN is_private
			ELSE @isPrivateValue
		END,
		parent_request_id,
		@userId, 
		due_datetime,
		logged_datetime,
		GETUTCDATE(),
		@headerDesc, 
		'''',
		summary,
		memo,
		append_interface,
		0,
		0,
		0
	FROM 
		actions
	WHERE
		sys_id 		= @systemId 	AND
		request_id	= @requestId	AND
		action_id	= @maxActionId
	-- Insert the corresponding actions_ex records
	INSERT INTO actions_ex
	(
		sys_id,
		request_id,
		action_id,
		field_id,
		bit_value,
		datetime_value,
		int_value,
		real_value,
		varchar_value,
		text_value,
		type_value
	)
	SELECT
		sys_id,
		request_id,
		@actionId,
		field_id,
		bit_value,
		datetime_value,
		int_value,
		real_value,
		varchar_value,
		text_value,
		type_value
	FROM
		actions_ex
	WHERE
		sys_id 		= @systemId 	AND
		request_id	= @requestId	AND
		action_id	= @maxActionId
END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tbits_deleteBusinessArea]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_tbits_deleteBusinessArea]
(  
 @systemId  int  
)  
as  

PRINT ''Deleting Exclusion List:''
delete exclusion_list where sys_id = @systemId  
PRINT ''Deleting Request Extended Fields:''
delete requests_ex where sys_id = @systemId  
PRINT ''Deleting Request Users:''
delete request_users where sys_id = @systemId  
PRINT ''Deleting Requests:''
delete requests where sys_id = @systemId  
PRINT ''Deleting Action Extended Fields:''
delete actions_ex where sys_id = @systemId  
PRINT ''Deleting Action Users:''
delete action_users where sys_id = @systemId  
PRINT ''Deleting Actions:''
delete actions where sys_id = @systemId  
PRINT ''Deleting Role-Users:''
delete roles_users where sys_id = @systemId  
PRINT ''Deleting Role-Permissions:''
delete roles_permissions where sys_id = @systemId  
PRINT ''Deleting Roles:''
delete roles where sys_id = @systemId  
PRINT ''Deleting BA Users:''
delete business_area_users where sys_id = @systemId
PRINT ''Deleting Type Users:''
delete type_users where sys_id = @systemId  
PRINT ''Deleting Types:''
delete types where sys_id = @systemId  
PRINT ''Deleting Field Descriptors:''
delete field_descriptors where sys_id = @systemId  
PRINT ''Deleting Fields:''
delete fields where sys_id = @systemId  
PRINT ''Deleting BA Record:''
delete business_areas where sys_id = @systemId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_action_insertTransferAction]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_action_insertTransferAction]
(
	@systemId	INT,
	@requestId	INT,
	@userId		INT
)

AS

DECLARE @sysPrefix VARCHAR(256)
DECLARE @statusFieldId INT
DECLARE @statusFieldName VARCHAR(256)

DECLARE @actionId INT
DECLARE @oldStatusId INT
DECLARE @closedStatusId INT

DECLARE @oldStatus VARCHAR(128)
DECLARE @headerDescription VARCHAR(4096)

/*
 * Get the Prefix of the business area.
 */
SELECT 
	@sysPrefix = sys_prefix 
FROM 
	business_areas 
WHERE 
	sys_id = @systemId

/*
 * Get the id and the display name of status field in this business area.
 */
SELECT 
	@statusFieldId = field_id, 
	@statusFieldName = display_name 
FROM 
	fields 
WHERE 
	sys_id = @systemId AND 
	name = ''status_id''

/*
 * Get the Id of the status closed in this business area.
 */
SELECT 
	@closedStatusId = ISNULL(type_id, 0) 
FROM 
	types 
WHERE 
	sys_id = @systemId AND 
	field_id = @statusFieldId AND 
	name = ''closed''
/*
 * Get the id of the old status.
 */
SELECT 
	@oldStatusId = status_id 
FROM 
	requests 
WHERE 
	sys_id = @systemId AND 
	request_id = @requestId 

/*
 * Get the name of the old status
 */
SELECT 
	@oldStatus = display_name 
FROM 
	types t 
WHERE 
	sys_id = @systemId AND 
	field_id = @statusFieldId AND 
	type_id = @oldStatusId

/*
 * Get the max_request_id
 */
SELECT 
	@actionId = max_action_id 
FROM 
	requests 
WHERE 
	sys_id = @systemId AND 
	request_id = @requestId

/*
 * Prepare the header description.
 */
IF (@oldStatusId <> @closedStatusId)
BEGIN
	SELECT 
		@headerDescription = '' [ '' + @statusFieldName + 
				     '' changed from '''''' + @oldStatus + 
				     '''''' to ''''Closed'''' ] '' + char(10)
END
ELSE
BEGIN
	SELECT @headerDescription = ''''
END

SELECT 
	@headerDescription = @headerDescription +  
			     '' [ Transfer to '' + @sysPrefix + 
			     ''# pending... ] '' + char(10)

/*
 * Increment the action id by 1.
 */
SELECT @actionId = @actionId + 1

UPDATE requests
SET
	status_id 		= @closedStatusId,
	user_id			= @userId,
	max_action_id 		= @actionId,
	lastupdated_datetime	= getUTCDate(),
	append_interface	= 101,
	notify			= 1,
	notify_loggers		= 1,
	replied_to_action 	= 0
WHERE
	sys_id = @systemId AND
	request_id = @requestId

INSERT INTO actions 
(
	sys_id,
	request_id,
	action_id,
	category_id,
	status_id,
	severity_id,
	request_type_id,
	subject,
	description,
	is_private,
	parent_request_id,
	user_id,
	due_datetime,
	logged_datetime,
	lastupdated_datetime,
	header_description,
	attachments,
	summary,
	memo,
	append_interface,
	notify,
	notify_loggers,
	replied_to_action
)
SELECT
	sys_id,
	request_id,
	@actionId,
	category_id,
	@closedStatusId,
	severity_id,
	request_type_id,
	subject,
	'''',
	is_private,
	parent_request_id,
	@userId,
	due_datetime,
	logged_datetime,
	getUTCDate(),
	@headerDescription,
	'''',
	'''',
	'''',
	101,
	1,
	1,
	0
FROM
	actions
WHERE
	sys_id = @systemId	AND
	request_id = @requestId	AND
	action_id = @actionId - 1

INSERT INTO action_users 
SELECT 
	sys_id,
	request_id,
	@actionId,
	user_type_id,
	user_id,
	ordering,
	is_primary
FROM 
action_users 
WHERE 
	sys_id = @systemId AND 
	request_id = @requestId AND 
	action_id = @actionId - 1

INSERT INTO actions_ex 
SELECT 
	sys_id,
	request_id,
	@actionId,
	field_id,
	bit_value,
	datetime_value,
	int_value,
	real_value,
	varchar_value,
	text_value,
	type_value
FROM 
	actions_ex 
WHERE 
	sys_id = @systemId AND 
	request_id = @requestId AND 
	action_id = @actionId - 1

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_action_getDiffActions]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_action_getDiffActions]
(
	@sys_id   		int,
	@request_id 		int,
	@replied_to_action 	int,
	@max_action_id 		int
)
AS

SELECT 
   sys_id,
   request_id,
   action_id,
   category_id,
   status_id,
   severity_id,
   request_type_id,
   subject,
   '''' as "description",
   is_private,
   parent_request_id,
   user_id,
   due_datetime,
   logged_datetime,
   lastupdated_datetime,
   '''' as "header_description",
   '''' as "attachments",
   summary,
   '''''''' as "memo",
   0 as "append_interface",
   0 as "notify",
   0 as "notify_loggers",
   0 as "replied_to_action",
   office_id

FROM
      actions
WHERE sys_id = @sys_id and
      request_id = @request_id and
      action_id >= @replied_to_action and
      action_id <=  @max_action_id  
order by action_id desc

SELECT
       *
FROM
       action_users
WHERE
       sys_id = @sys_id AND
       request_id = @request_id AND
       action_id >=  @replied_to_action AND
       action_id <=  @max_action_id

SELECT 
	*
FROM 
	actions_ex
WHERE
	sys_id 	= @sys_id AND
	request_id = @request_id AND
        action_id >=  @replied_to_action AND
        action_id <=  @max_action_id

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tbits_simpleActOnGroup]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'



CREATE PROCEDURE [dbo].[stp_tbits_simpleActOnGroup]
(
	@systemId	int,
	@requestList	varchar(7999),
	@action		int,
	@description	varchar(7999),
	@userId		int,
	@rejectedList	varchar(7999) OUTPUT
)
AS
DECLARE @maxActionId 		int,
	@closedStatusId		int,
	@statusFieldId		int,
	@privateFieldId		int,
	@finalPrivateStatus	int,
	@isPrivate		bit

DECLARE @index		int,
	@requestId	int

DECLARE @actionId int
DECLARE @statusDisplayName VARCHAR(128)
DECLARE @headerDesc varchar(1000)
SELECT @rejectedList = ''''

/*
 * Get the Field ID of status. and typeId of closed.
 */
SELECT 
	@statusFieldId = field_id 
FROM 
	fields 
WHERE 
	sys_id = @systemId AND 
	name = ''status_id''

SELECT 
	@closedStatusId = type_id,
	@statusDisplayName = display_name
FROM 
	types t 
WHERE 
	t.sys_id   = @systemId AND 
	t.field_id = @statusFieldId AND 
	name = ''closed''

/*
 * GET the field ID of is_private
 */
SELECT @privateFieldId = field_id FROM fields WHERE sys_id = @systemId AND name = ''is_private''

WHILE (@requestList <> '''')
BEGIN
	SELECT @index = charindex('','', @requestList)
	IF (@index > 0) 
	BEGIN
		SELECT @requestId = CONVERT ( INT, substring(@requestList, 0, @index) )
		SELECT @requestList = substring ( @requestList, @index + 1, len(@requestList))
	END
	ELSE 
	BEGIN
		SELECT @requestId = CONVERT ( INT, @requestList )	
		SELECT @requestList = ''''
	END
	-- Get the Max ActionId
	SELECT @maxActionId = max_action_id FROM requests WHERE sys_id = @systemId AND request_id = @requestId
	IF (@action = 1)
	BEGIN
		SELECT @headerDesc = ''status_id##'' + convert(varchar, @statusFieldId) + ''##[ '' + @statusDisplayName  + '' ]'' + Char(10) + ''[ No e-mail notification ]''
		if ((SELECT status_id FROM requests WHERE sys_id = @systemId AND request_id = @requestId) = @closedStatusId)
		BEGIN
			if (@rejectedList is NOT null AND @rejectedList != '''') 
			BEGIN
				SELECT @rejectedList = @rejectedList + '',''
			END
			SELECT @rejectedList = @rejectedList + convert(varchar(100), @requestId)
		END
		ELSE
		BEGIN
			-- Update the Status for this request.
			SELECT @actionId = @maxActionId + 1
			UPDATE requests
			SET 
				status_id 	= @closedStatusId,
				description	= @description,
				user_id		= @userId,
				max_action_id	= @actionId,
				lastupdated_datetime = GETUTCDATE(),
				attachments	= ''''
			WHERE
				sys_id 		= @systemId AND
				request_id 	= @requestId
			INSERT INTO actions 
			(
				sys_id,
				request_id,
				action_id,
				category_id,
				status_id,
				severity_id,
				request_type_id,
				subject,
				description,
				is_private,
				parent_request_id,
				user_id,
				due_datetime,
				logged_datetime,
				lastupdated_datetime,
				header_description,
				attachments,
				summary,
				memo,
				append_interface,
				notify,
				notify_loggers,
				replied_to_action
			)
			SELECT 
				sys_id,
				request_id,
				@actionId,
				category_id,
				@closedStatusId, 
				severity_id,
				request_type_id,
				subject,
				@description, 
				is_private,
				parent_request_id,
				@userId, 
				due_datetime,
				logged_datetime,
				GETUTCDATE(),
				@headerDesc, 
				'''',
				summary,
				memo,
				append_interface,
				0,
				0,
				0
			FROM 
				actions
			WHERE
				sys_id 		= @systemId 	AND
				request_id	= @requestId	AND
				action_id	= @maxActionId
			-- Insert the corresponding actions_ex records
			INSERT INTO actions_ex
			(
				sys_id,
				request_id,
				action_id,
				field_id,
				bit_value,
				datetime_value,
				int_value,
				real_value,
				varchar_value,
				text_value,
				type_value
			)
			SELECT
				sys_id,
				request_id,
				@actionId,
				field_id,
				bit_value,
				datetime_value,
				int_value,
				real_value,
				varchar_value,
				text_value,
				type_value
			FROM
				actions_ex
			WHERE
				sys_id 		= @systemId 	AND
				request_id	= @requestId	AND
				action_id	= @maxActionId
			PRINT ''Closing the request: '' + convert(varchar, @requestId)
		END
	END
	ELSE IF (@action = 2 OR @action = 3)
	BEGIN
		IF (@action = 2)
		BEGIN
			SELECT @finalPrivateStatus = 1
			SELECT @headerDesc = ''is_private##'' + convert(varchar, @privateFieldId) + ''##[ Marked Private ]'' + Char(10) + ''[ No e-mail notification ]''
		END
		IF (@action = 3)
		BEGIN
			SELECT @finalPrivateStatus = 0
			SELECT @headerDesc = ''is_private##'' + convert(varchar, @privateFieldId) + ''##[ Marked Public ]'' + Char(10) + ''[ No e-mail notification ]''
		END
		if ((SELECT is_private FROM requests WHERE sys_id = @systemId AND request_id = @requestId) = @finalPrivateStatus)
		BEGIN
			if (@rejectedList is NOT null AND @rejectedList != '''') 
			BEGIN
				SELECT @rejectedList = @rejectedList + '',''
			END
			SELECT @rejectedList = @rejectedList + convert(varchar(100), @requestId)
		END
		ELSE
		BEGIN
			-- Update the is_private for this request.
			SELECT @actionId = @maxActionId + 1
			UPDATE requests
			SET 
				is_private	= @finalPrivateStatus,
				description	= @description,
				user_id		= @userId,
				max_action_id	= @actionId,
				lastupdated_datetime = GETUTCDATE(),
				attachments	= ''''
			WHERE
				sys_id 		= @systemId AND
				request_id 	= @requestId
			INSERT INTO actions 
			(
				sys_id,
				request_id,
				action_id,
				category_id,
				status_id,
				severity_id,
				request_type_id,
				subject,
				description,
				is_private,
				parent_request_id,
				user_id,
				due_datetime,
				logged_datetime,
				lastupdated_datetime,
				header_description,
				attachments,
				summary,
				memo,
				append_interface,
				notify,
				notify_loggers,
				replied_to_action
			)
			SELECT 
				sys_id,
				request_id,
				@actionId,
				category_id,
				status_id, 
				severity_id,
				request_type_id,
				subject,
				@description, 
				@finalPrivateStatus,
				parent_request_id,
				@userId, 
				due_datetime,
				logged_datetime,
				GETUTCDATE(),
				@headerDesc, 
				'''',
				summary,
				memo,
				append_interface,
				0,
				0,
				0
			FROM 
				actions
			WHERE
				sys_id 		= @systemId 	AND
				request_id	= @requestId	AND
				action_id	= @maxActionId
			-- Insert the corresponding actions_ex records
			INSERT INTO actions_ex
			(
				sys_id,
				request_id,
				action_id,
				field_id,
				bit_value,
				datetime_value,
				int_value,
				real_value,
				varchar_value,
				text_value,
				type_value
			)
			SELECT
				sys_id,
				request_id,
				@actionId,
				field_id,
				bit_value,
				datetime_value,
				int_value,
				real_value,
				varchar_value,
				text_value,
				type_value
			FROM
				actions_ex
			WHERE
				sys_id 		= @systemId 	AND
				request_id	= @requestId	AND
				action_id	= @maxActionId
		END
	END
END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tbits_renameBusinessArea]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_tbits_renameBusinessArea]
(  
	@oldId  int,
	@newId  INT
)  
as  
PRINT ''Updating Exclusion List:''
update exclusion_list set sys_id = @newId where sys_id = @oldId  
PRINT ''Updating Request Extended Fields:''
update requests_ex set sys_id = @newId where sys_id = @oldId  
PRINT ''Updating Request Users:''
update request_users set sys_id = @newId where sys_id = @oldId  
PRINT ''Updating Requests:''
update requests set sys_id = @newId where sys_id = @oldId  
PRINT ''Updating Action Extended Fields:''
update actions_ex set sys_id = @newId where sys_id = @oldId  
PRINT ''Updating Action Users:''
update action_users set sys_id = @newId where sys_id = @oldId  
PRINT ''Updating Actions:''
update actions set sys_id = @newId where sys_id = @oldId  
PRINT ''Updating Role-Users:''
update roles_users set sys_id = @newId where sys_id = @oldId  
PRINT ''Updating Role-Permissions:''
update roles_permissions set sys_id = @newId where sys_id = @oldId  
PRINT ''Updating Roles:''
update roles set sys_id = @newId where sys_id = @oldId  
PRINT ''Updating BA Users:''
update business_area_users set sys_id = @newId where sys_id = @oldId
PRINT ''Updating Type Users:''
update type_users set sys_id = @newId where sys_id = @oldId  
PRINT ''Updating Types:''
update types set sys_id = @newId where sys_id = @oldId  
PRINT ''Updating Fields:''
update fields set sys_id = @newId where sys_id = @oldId  
PRINT ''Updating BA Record:''
update business_areas set sys_id = @newId where sys_id = @oldId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_request_delete_all_requests_in_ba]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_request_delete_all_requests_in_ba] 
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
	update business_areas set max_version_no = 0  where sys_id = @systemId
	delete from versions where sys_id = @systemId
	delete from requestfilemaxid where sys_id = @systemId
END


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_request_delete_all_requests]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author:		Sandeep
-- Create date: 10 Jan 07
-- Description:	Deletes all the requests in a ba,. Dont use it at all.
-- =============================================
CREATE PROCEDURE [dbo].[stp_request_delete_all_requests] 
	
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
	update business_areas set max_version_no = 0
	delete from locks
	delete from versions
	delete from file_repo_index
	delete from requestfilemaxid
	delete from max_ids
END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_request_updateAttachments_ex]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
CREATE procedure [dbo].[stp_request_updateAttachments_ex]
(
	@systemId	INT,
	@requestId	INT,
	@actionId	INT,
	@fieldId	INT,
	@attachments	TEXT
)
AS
UPDATE requests_ex
SET
	text_value = @attachments
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId AND
	field_id	= @fieldId

UPDATE actions_ex
SET
	text_value = @attachments
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId AND
	field_id	= @fieldId AND
	action_id	= @actionId
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_field_delete]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_field_delete] 
(
	@sys_id			INT,
	@field_id		INT,
	@name			NVARCHAR(255),
	@display_name		NVARCHAR(255),
	@description		NVARCHAR(255),
	@data_type_id		INT,
	@is_active		BIT,
	@is_extended		BIT,
	@is_private		BIT,
	@tracking_option	INT,
	@permission		INT,
	@regex			VARCHAR(7999),
	@is_dependent		BIT,
	@display_order		INT,
	@display_group		INT,
	@returnValue		INT OUTPUT
)
AS

DECLARE @delete int
DECLARE @dataTypeId int

SELECT @dataTypeId = datatype_id from datatypes where name = ''type''

IF not exists(SELECT field_id from actions_ex where sys_id = @sys_id and field_id = @field_id)
BEGIN
	DELETE FROM fields 
	WHERE  
		sys_id = @sys_id AND 
		field_id = @field_id
	
	DELETE FROM roles_permissions 
	WHERE  
		sys_id = @sys_id AND
		field_id = @field_id
  
	IF(@data_type_id = @dataTypeId)
	BEGIN
		DELETE FROM types 
		WHERE
			sys_id = @sys_id AND
			field_id = @field_id	
		
		DELETE FROM type_users
		WHERE
			sys_id = @sys_id AND
			field_id = @field_id
	END
	
	SELECT @returnValue = 1
END
ELSE
BEGIN
	SELECT @returnValue = 0
END




' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_type_delete]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_type_delete] 
(
	@sys_id INT,
	@field_id INT,
	@type_id INT,
	@name NVARCHAR(255),
	@display_name NVARCHAR(255),
	@description NVARCHAR(255),
	@ordering INT,
	@is_active BIT,
	@is_default BIT,
	@is_checked BIT,
	@is_private BIT,
	@is_final BIT,
	@returnValue INT OUTPUT
)
AS

DECLARE @fieldName varchar(255)
DECLARE @Delete INT

SELECT @fieldName = name FROM fields WHERE field_id = @field_id
SET @Delete = 0

IF(@field_id < 29) 
BEGIN
    IF(@fieldName = ''category_id'')
    BEGIN
        IF NOT EXISTS(SELECT * FROM actions a WHERE a.sys_id = @sys_id AND a.category_id = @type_id)
        BEGIN
           SET @Delete = 1
        END
    END
    IF(@fieldName = ''status_id'')
    BEGIN
        IF NOT EXISTS(SELECT * FROM actions a WHERE a.sys_id = @sys_id AND a.status_id = @type_id)
        BEGIN
           SET @Delete = 1
        END
    END
    IF(@fieldName = ''severity_id'')
    BEGIN
        IF NOT EXISTS(SELECT * FROM actions a WHERE a.sys_id = @sys_id AND a.severity_id = @type_id)
        BEGIN
           SET @Delete = 1
        END
    END
    IF(@fieldName = ''request_type_id'')
    BEGIN
        IF NOT EXISTS(SELECT * FROM actions a WHERE a.sys_id = @sys_id AND a.request_type_id = @type_id)
        BEGIN
           SET @Delete = 1
        END
    END
END

IF(@field_id > 29) 
BEGIN
    IF NOT EXISTS(SELECT * FROM actions_ex  WHERE sys_id = @sys_id AND field_id = @field_id AND type_value = @type_id)
    BEGIN
       SET @Delete = 1
    END
END

IF(@Delete = 1)
BEGIN
    DELETE types 
    WHERE  
        sys_id = @sys_id AND 
        field_id = @field_id AND
        type_id = @type_id

    IF (@is_default = 1)
    BEGIN
       DECLARE @totalCount INT
       SELECT @totalCount = (SELECT COUNT(*) FROM types WHERE sys_id = @sys_id AND field_id = @field_id)
       IF (@totalCount= (SELECT COUNT(*) FROM types WHERE sys_id = @sys_id AND field_id = @field_id AND is_default=0))
       BEGIN
        UPDATE types 
        SET 
            is_default = 1 
        WHERE 
            sys_id = @sys_id AND 
            field_id = @field_id AND 
            type_id = (SELECT MIN(type_id) FROM types WHERE sys_id = @sys_id AND field_id = @field_id)
       END
    END
    SELECT @returnValue = 1
END
ELSE
BEGIN
     SELECT @returnValue = 0
END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ba_forms_update]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
CREATE PROCEDURE [dbo].[stp_ba_forms_update]
(
	@sys_id		INT,
	@form_id	INT,
	@name		VARCHAR(128),
	@title		VARCHAR(512),
	@shortname	VARCHAR(64),
	@form_config	TEXT
)

AS

UPDATE ba_forms
SET
	name		= @name,
	title		= @title,
	shortname	= @shortname,
	form_config	= @form_config
WHERE
	sys_id	= @sys_id AND
	form_id	= @form_id


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ba_forms_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
CREATE PROCEDURE [dbo].[stp_ba_forms_insert]
(
	@sys_id		INT,
	@form_id	INT,
	@name		VARCHAR(128),
	@title		VARCHAR(512),
	@shortname	VARCHAR(64),
	@form_config	TEXT
)

AS

INSERT INTO ba_forms
(
	sys_id,
	form_id,
	name,
	title,
	shortname,
	form_config
)
VALUES
(
	@sys_id,
	@form_id,
	@name,
	@title,
	@shortname,
	@form_config
)



' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ba_forms_lookupBySystemIdAndName]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
CREATE PROCEDURE [dbo].[stp_ba_forms_lookupBySystemIdAndName]
(
	@systemId	INT,
	@name		VARCHAR(128)
)

AS

SELECT 
	sys_id,
	form_id,
	name,
	title,
	shortname,
	form_config
FROM
	ba_forms
WHERE
	sys_id = @systemId AND
	(
		name = @name OR
		shortname = @name
	)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_request_getAndIncrRequestFileMaxrId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_request_getAndIncrRequestFileMaxrId] 
	@sysId int, @requestId int
AS
BEGIN
	BEGIN TRANSACTION
		declare @maxid int
		select @maxid = maxfileid from requestfilemaxid where sys_id = @sysId and request_id = @requestId
		if @maxid is null
		begin
			insert into requestfilemaxid (sys_id, request_id, maxfileid) values (@sysId, @requestId, 1)
			select 1 max_id;
		end
		else
		begin
			update requestfilemaxid set maxfileid = @maxid +1 where sys_id = @sysId and request_id = @requestId
			select @maxid + 1 max_id
		end
	commit TRAN
END' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_barule_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_barule_insert] 
	-- Add the parameters for the stored procedure here	
	@system_Id 			INT,
	@rule_Id			INT,
	@sequence_No		INT
AS
BEGIN

-- Insert statements for procedure here
INSERT INTO ba_rules
( 
	sys_id, 
	rule_id, 
	sequence_no	
)
VALUES
(
	@system_Id,
	@rule_Id,
	@sequence_No
)

END

/****** Object:  StoredProcedure [dbo].[stp_wr_insert]*/
SET ANSI_NULLS ON

set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_barules_lookupBySystemIdAndRuleId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_barules_lookupBySystemIdAndRuleId]
(
	@systemId	INT
)
AS
SELECT 
	*
FROM
	ba_rules
WHERE
	sys_id = @systemId

ORDER BY sequence_no

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_barules_getAllBARules]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_barules_getAllBARules]
AS

SELECT 
	* 
FROM 
	ba_rules

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_bauser_getAllBAUsers]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_bauser_getAllBAUsers]
AS

SELECT 
	* 
FROM 
	business_area_users

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_adsync_updateUser]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_adsync_updateUser]
(
	@user_id		int,
	@user_login		nvarchar(510),
	@first_name		nvarchar(510),
	@last_name		nvarchar(510),
	@display_name		nvarchar(510),
	@email			nvarchar(510),
	@is_active		bit,
	@user_type_id		int,
	@web_config		text,
	@windows_config		text,
	@is_on_vacation		bit,
	@is_display		bit,
	@cn			varchar(255),
	@distinguished_name	varchar(4000),
	@name			varchar(255),
	@member_of		text,
	@member			text,
	@mail_nickname		varchar(255),
	@location		varchar(255),
	@extension		varchar(255),
	@mobile			varchar(255),
	@home_phone		varchar(255)
)

AS
UPDATE users
SET 
	user_login 		= @user_login,
	first_name 		= @first_name,
	last_name  		= @last_name,
	display_name  		= @display_name,
	email  			= @email,
	is_active  		= @is_active,
	user_type_id  		= @user_type_id,
	web_config  		= @web_config,
	windows_config  	= @windows_config,
	is_on_vacation  	= @is_on_vacation,
	is_display  		= @is_display,
	cn  			= @cn,
	distinguished_name  	= @distinguished_name,
	name  			= @name,
	member_of  		= @member_of,
	member  		= @member,
	mail_nickname 		= @mail_nickname,
	location		= @location,
	extension		= @extension,
	mobile			= @mobile,
	home_phone		= @home_phone
WHERE
	user_id = @user_id

/*
 * Mark the corresponding ba_user record as active.
 */ 
UPDATE business_area_users
SET
	is_active = @is_active
WHERE
	user_id  = @user_id

/*
 * Mark the corresponding type user records as active.
 */ 
UPDATE type_users
SET
	is_active = @is_active
WHERE
	user_id  = @user_id

/*
 * Mark the corresponding role user records as active
 */
UPDATE roles_users
SET
	is_active = @is_active
WHERE
	user_id  = @user_id

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_adsync_deactivateUser]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_adsync_deactivateUser]
(
	@userId  int
)

AS

/*
 * Mark the user record as inactive.
 */
UPDATE users
SET 
	is_active = 0
WHERE
	user_id = @userId

/*
 * Mark the corresponding type user records as inactive.
 */ 
UPDATE type_users
SET
	is_active = 0
WHERE
	user_id  = @userId

/*
 * Mark the corresponding roles_users records as inactive
 */
UPDATE roles_users
SET
	is_active = 0
WHERE
	user_id = @userId


/*
 * Mark the corresponding BAUser records as inactive
 */
UPDATE business_area_users
SET
	is_active = 0
WHERE
	user_id = @userId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_business_area_users_delete]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_business_area_users_delete] 
(
	@sys_id INT,
	@user_id INT,
	@is_active BIT
)
AS

DELETE FROM roles_users
WHERE
	sys_id 	= @sys_id AND
	user_id = @user_id

DELETE FROM type_users
WHERE
	sys_id 	= @sys_id AND
	user_id = @user_id

DELETE FROM business_area_users 
WHERE
	sys_id 	= @sys_id AND
	user_id = @user_id

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_bauser_lookupBySystemId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_bauser_lookupBySystemId]
(
	@systemId INT
)
AS
SELECT 
	* 
FROM 
	business_area_users 
WHERE 
	sys_id = @systemId

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_bauser_getUserByBA]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_bauser_getUserByBA]
(
	@systemId INT
)
AS
SELECT 
	sys_id,
	user_id
FROM 
	business_area_users
WHERE 
	sys_id = @systemId AND 
	is_active = 1

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_req_action_lookupBySystemIdAndRequestIdAndActionId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_req_action_lookupBySystemIdAndRequestIdAndActionId]
(
	@systemId 	INT,
	@requestId	INT,
	@actionId	INT
)
AS

DECLARE @parentRequestId int
SELECT 
	@parentRequestId = parent_request_id 
FROM 
	requests 
WHERE 	
	sys_id = @systemId AND
	request_id = @requestId

SELECT sys_id,
	request_id,
	category_id,
	status_id,
	severity_id,
	request_type_id,
	subject,
	description,
	is_private,
	 @parentRequestId as "parent_request_id",
	user_id,
	action_id as "max_action_id",
	due_datetime,
	logged_datetime,
	lastupdated_datetime,
	header_description,
	attachments,
	summary,
	'''' as "memo",
	append_interface,
	notify,
	notify_loggers,
	replied_to_action,
	office_id
 FROM actions 
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId AND
	action_id	=  @actionId

SELECT * FROM action_users 
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId  AND
	action_id	=  @actionId
order by user_type_id, ordering

--get sub_requests
select request_id,subject from requests WHERE sys_id=@systemId AND
	 parent_request_id= @requestId and @requestId <>0
--get siblings
select request_id,subject from requests WHERE sys_id=@systemId AND
	 parent_request_id=
	(select  parent_request_id from requests WHERE sys_id=@systemId and request_id=@requestId) 	
	 AND
	 request_id <> @requestId and parent_request_id <>0

--get all related requests
declare @sysPrefix  varchar(50)
select  @sysPrefix= sys_prefix  from business_areas where sys_id = @systemId
select distinct  related_sys_prefix "sys_prefix"  , related_request_id "request_id" , related_action_id "action_id" ,convert(varchar(50),''primary'') "subject" into #tmp1
	from related_requests where primary_sys_prefix =@sysPrefix
		and primary_request_id=@requestId
Insert into #tmp1
	 select distinct primary_sys_prefix "sys_prefix"   , primary_request_id   "request_id"  ,primary_action_id "action_id",''secondary'' "subject" 
		from related_requests where related_sys_prefix =@sysPrefix
		and related_request_id=@requestId

-- #tmp1 contains all directly related request as of now
-- get transitive related requests into #tmp1 if any
 
IF ((SELECT COUNT(*) FROM #tmp1) > 0)
BEGIN
	select * into #tmp2 from #tmp1
while ((SELECT COUNT(*) FROM #tmp2) > 0)
BEGIN
	select distinct  related_sys_prefix "sys_prefix"  , related_request_id "request_id"  , related_action_id "action_id",  ''transitive'' "subject" into #tmp3
	from related_requests, #tmp2 where 
		primary_sys_prefix = #tmp2.sys_prefix and primary_request_id = #tmp2.request_id
		 and 
		 (NOT(related_sys_prefix = @sysPrefix and related_request_id = @requestId) )
		and
		(NOT( related_sys_prefix = #tmp2.sys_prefix and related_request_id = #tmp2.request_id))
Insert into #tmp3
 	select distinct primary_sys_prefix "sys_prefix"   , primary_request_id   "request_id"  ,primary_action_id "action_id",  ''transitive'' "subject"
		from related_requests, #tmp2  where
		 related_sys_prefix = #tmp2.sys_prefix and related_request_id = #tmp2.request_id
		 and
		(NOT(primary_sys_prefix = @sysPrefix and primary_request_id = @requestId))
		and
		(NOT(primary_sys_prefix = #tmp2.sys_prefix and primary_request_id = #tmp2.request_id))
	
IF ((SELECT COUNT(*) FROM #tmp3) = 0)
	BEGIN
		BREAK
	END
	DELETE #tmp2
	INSERT INTO #tmp2
	
	SELECT * FROM #tmp3 
 	where (sys_prefix +''#'' +  convert(varchar(50),request_id))
			 not in 
				(select sys_prefix +''#'' +  convert(varchar(50),request_id) from #tmp1)
	INSERT INTO #tmp1
	SELECT *  FROM #tmp3
	
	DROP TABLE #tmp3
END
DROP TABLE #tmp2
END
-- return all direct and transitive related requests

select distinct  
	case
	when #tmp1.action_id > 0 then #tmp1.sys_prefix + ''#'' + convert(varchar(50),#tmp1.request_id)  + ''#'' +  convert(varchar(50),#tmp1.action_id)
	else
	#tmp1.sys_prefix + ''#'' + convert(varchar(50),#tmp1.request_id)
	end
	 "request_id", subject
	 from
	 #tmp1
	
DROP TABLE #tmp1

SELECT @requestId "request_id",subject from requests where request_id = @parentRequestId and sys_id = @systemId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_request_lookupBySystemIdAndRequestIdForViewRequest]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_request_lookupBySystemIdAndRequestIdForViewRequest]
(
	@systemId 	INT,
	@requestId	INT
)
AS
declare @sysPrefix varchar(50)
SELECT sys_id,
	request_id,
	category_id,
	status_id,
	severity_id,
	request_type_id,
	subject,
	description,
	is_private,
	parent_request_id,
	user_id,
	max_action_id,
	due_datetime,
	logged_datetime,
	lastupdated_datetime,
	header_description,
	attachments,
	summary,
	'''' as "memo",
	append_interface,
	notify,
	notify_loggers,
	replied_to_action,
	office_id
 FROM requests 
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId 
SELECT * FROM request_users 
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId 
order by user_type_id, ordering

SELECT 
	sys_id,
	request_id,
	field_id,
	bit_value,
	datetime_value,
	int_value,
	convert(varchar, real_value) ''real_value'',
	varchar_value,
	text_value,
	type_value 
FROM 
	requests_ex
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId
--get sub_requests
select request_id,subject from requests WHERE sys_id=@systemId AND
	 parent_request_id= @requestId and @requestId <>0
--get siblings
select request_id,subject from requests WHERE sys_id=@systemId AND
	 parent_request_id=
	(select  parent_request_id from requests WHERE sys_id=@systemId and request_id=@requestId) 	
	 AND
	 request_id <> @requestId and parent_request_id <>0
--get related requests
select  @sysPrefix= sys_prefix  from business_areas where sys_id = @systemId
select distinct  related_sys_prefix "sys_prefix"  , related_request_id "request_id" , related_action_id "action_id" into #tmp1
	from related_requests where primary_sys_prefix =@sysPrefix
		and primary_request_id=@requestId
Insert into #tmp1
	 select distinct primary_sys_prefix "sys_prefix"   , primary_request_id   "request_id"  ,primary_action_id "action_id"
		from related_requests where related_sys_prefix =@sysPrefix
		and related_request_id=@requestId
-- #tmp1 contains all directly related request as of now
-- get transitive related requests into #tmp1 if any
 
IF ((SELECT COUNT(*) FROM #tmp1) > 0)
BEGIN
	select * into #tmp2 from #tmp1
while ((SELECT COUNT(*) FROM #tmp2) > 0)
BEGIN
	select distinct  related_sys_prefix "sys_prefix"  , related_request_id "request_id"  , related_action_id "action_id" into #tmp3
	from related_requests, #tmp2 where 
		primary_sys_prefix = #tmp2.sys_prefix and primary_request_id = #tmp2.request_id
		 and 
		 (NOT(related_sys_prefix = @sysPrefix and related_request_id = @requestId) )
		and
		(NOT( related_sys_prefix = #tmp2.sys_prefix and related_request_id = #tmp2.request_id))
Insert into #tmp3
 	select distinct primary_sys_prefix "sys_prefix"   , primary_request_id   "request_id"  ,primary_action_id "action_id"
		from related_requests, #tmp2  where
		 related_sys_prefix = #tmp2.sys_prefix and related_request_id = #tmp2.request_id
		 and
		(NOT(primary_sys_prefix = @sysPrefix and primary_request_id = @requestId))
		and
		(NOT(primary_sys_prefix = #tmp2.sys_prefix and primary_request_id = #tmp2.request_id))
	
IF ((SELECT COUNT(*) FROM #tmp3) = 0)
	BEGIN
		BREAK
	END
	DELETE #tmp2
	INSERT INTO #tmp2
	
	SELECT * FROM #tmp3 
 	where (sys_prefix +''#'' +  convert(varchar(50),request_id))
			 not in 
				(select sys_prefix +''#'' +  convert(varchar(50),request_id) from #tmp1)
	INSERT INTO #tmp1
	SELECT *  FROM #tmp3
	
	DROP TABLE #tmp3
END
DROP TABLE #tmp2
END
-- return all direct and transitive related requests
select distinct  
	case
	when #tmp1.action_id > 0 then #tmp1.sys_prefix + ''#'' + convert(varchar(50),#tmp1.request_id)  + ''#'' +  convert(varchar(50),#tmp1.action_id)
	else
	#tmp1.sys_prefix + ''#'' + convert(varchar(50),#tmp1.request_id)
	end
	 "request_id",  isNull(requests.subject,'' '') "subject"
	 from
	 #tmp1 LEFT JOIN  business_areas ON
		#tmp1.sys_prefix = business_areas.sys_prefix
	LEFT JOIN requests ON
	 	#tmp1.request_id = requests.request_id  AND
		 business_areas.sys_id  =  requests.sys_id
	
DROP TABLE #tmp1

DECLARE @parentRequestId int
SELECT @parentRequestId = parent_request_id from requests where request_id = @requestId and sys_id = @systemId
SELECT @requestId "request_id",subject from requests where request_id = @parentRequestId and sys_id = @systemId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_request_lookupBySystemIdAndRequestId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_request_lookupBySystemIdAndRequestId]
(
	@systemId 	INT,
	@requestId	INT
)
AS
SELECT sys_id,
	request_id,
	category_id,
	status_id,
	severity_id,
	request_type_id,
	subject,
	description,
	is_private,
	parent_request_id,
	user_id,
	max_action_id,
	due_datetime,
	logged_datetime,
	lastupdated_datetime,
	header_description,
	attachments,
	summary,
	'''' as "memo",
	append_interface,
	notify,
	notify_loggers,
	replied_to_action,
	office_id
 FROM requests 
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId 
SELECT * FROM request_users 
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId 
order by user_type_id, ordering

SELECT 
	sys_id,
	request_id,
	field_id,
	bit_value,
	datetime_value,
	int_value,
	convert(varchar(1024), real_value) ''real_value'',
	varchar_value,
	text_value,
	type_value 
FROM 
	requests_ex
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId
--get sub_requests
select request_id,subject from requests WHERE sys_id=@systemId AND
	 parent_request_id= @requestId and @requestId <>0
--get siblings
select request_id,subject from requests WHERE sys_id=@systemId AND
	 parent_request_id=
	(select  parent_request_id from requests WHERE sys_id=@systemId and request_id=@requestId) 	
	 AND
	 request_id <> @requestId and parent_request_id <>0

--get all related requests
declare @sysPrefix  varchar(50)
select  @sysPrefix= sys_prefix  from business_areas where sys_id = @systemId
select distinct  related_sys_prefix "sys_prefix"  , related_request_id "request_id" , related_action_id "action_id" ,convert(varchar(50),''primary'') "subject" into #tmp1
	from related_requests where primary_sys_prefix =@sysPrefix
		and primary_request_id=@requestId
Insert into #tmp1
	 select distinct primary_sys_prefix "sys_prefix"   , primary_request_id   "request_id"  ,primary_action_id "action_id",''secondary'' "subject" 
		from related_requests where related_sys_prefix =@sysPrefix
		and related_request_id=@requestId

-- #tmp1 contains all directly related request as of now
-- get transitive related requests into #tmp1 if any
 
IF ((SELECT COUNT(*) FROM #tmp1) > 0)
BEGIN
	select * into #tmp2 from #tmp1
while ((SELECT COUNT(*) FROM #tmp2) > 0)
BEGIN
	select distinct 
		related_sys_prefix "sys_prefix", 
		related_request_id "request_id", 
		related_action_id "action_id",  
		''transitive'' "subject" 
	into 
		#tmp3
	from 
		related_requests, 
		#tmp2 
	where 
		primary_sys_prefix = #tmp2.sys_prefix and primary_request_id = #tmp2.request_id
		 and 
		 (NOT(related_sys_prefix = @sysPrefix and related_request_id = @requestId) )
		and
		(NOT( related_sys_prefix = #tmp2.sys_prefix and related_request_id = #tmp2.request_id))
Insert into #tmp3
 	select distinct primary_sys_prefix "sys_prefix"   , primary_request_id   "request_id"  ,primary_action_id "action_id",  ''transitive'' "subject"
		from related_requests, #tmp2  where
		 related_sys_prefix = #tmp2.sys_prefix and related_request_id = #tmp2.request_id
		 and
		(NOT(primary_sys_prefix = @sysPrefix and primary_request_id = @requestId))
		and
		(NOT(primary_sys_prefix = #tmp2.sys_prefix and primary_request_id = #tmp2.request_id))
	
IF ((SELECT COUNT(*) FROM #tmp3) = 0)
	BEGIN
		BREAK
	END
	DELETE #tmp2
	INSERT INTO #tmp2
	
	SELECT * FROM #tmp3 
 	where (sys_prefix +''#'' +  convert(varchar(50),request_id))
			 not in 
				(select sys_prefix +''#'' +  convert(varchar(50),request_id) from #tmp1)
	INSERT INTO #tmp1
	SELECT *  FROM #tmp3
	
	DROP TABLE #tmp3
END
DROP TABLE #tmp2
END
-- return all direct and transitive related requests

select distinct  
	case
	when #tmp1.action_id > 0 then #tmp1.sys_prefix + ''#'' + convert(varchar(50),#tmp1.request_id)  + ''#'' +  convert(varchar(50),#tmp1.action_id)
	else
	#tmp1.sys_prefix + ''#'' + convert(varchar(50),#tmp1.request_id)
	end
	 "request_id", subject
	 from
	 #tmp1
	
DROP TABLE #tmp1

DECLARE @parentRequestId int
SELECT @parentRequestId = parent_request_id from requests where request_id = @requestId and sys_id = @systemId
SELECT @requestId "request_id",subject from requests where request_id = @parentRequestId and sys_id = @systemId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tbits_getCompleteAction]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_tbits_getCompleteAction]
(
	@sysPrefix VARCHAR(100),
	@requestId INT,
	@userLogin VARCHAR(20),
	@actionOrder bit
)
AS

DECLARE @systemId INT
DECLARE @privateFieldId INT
DECLARE @userId INT 

SELECT @userId = 0
SELECT @userId = user_id FROM users where user_login = @userLogin
/*
 * Check if the USER ID is valid in TBits user database.
 */
IF (@userId IS NULL OR @userId = 0)
BEGIN
	SELECT * FROM requests WHERE 1=2	
END

SELECT @systemId = 0
SELECT @systemId = sys_id FROM business_areas where sys_prefix = @sysPrefix
PRINT @systemId

/*
 * Check if the BA ID is valid in tBits database.
 */
IF (@systemId IS NULL OR @systemId = 0)
BEGIN
	SELECT * FROM requests WHERE 1=2	
END

-- Get the Field ID of the is_private field.
SELECT @privateFieldId = field_id FROM fields WHERE sys_id = @systemId AND name = ''is_private''

SELECT
	req.sys_id,
	req.request_id, 
	ba.sys_prefix "sys_prefix",
	req.subject,
	(req.is_private | cat.is_private | ba.is_private) "is_private",
	cat.display_name "category",
	stat.display_name "status",
	sev.display_name "severity",
	rt.display_name "request_type",
	req.logged_datetime,
	req.lastupdated_datetime,
	req.due_datetime
FROM
	business_areas ba
	JOIN requests req
	ON ba.sys_id = req.sys_id

	JOIN types cat
	ON cat.sys_id = req.sys_id AND cat.field_id = 3 AND cat.type_id = req.category_id

	JOIN types stat
	ON stat.sys_id = req.sys_id AND stat.type_id = req.status_id AND stat.field_id = 4

	JOIN types sev
	ON sev.sys_id = req.sys_id AND sev.type_id = req.severity_id AND sev.field_id = 5

	JOIN types rt
	ON rt.sys_id = req.sys_id AND rt.type_id = req.request_type_id AND rt.field_id = 6
WHERE
	ba.sys_id = @systemId AND
	request_id = @requestId

SELECT 
	ru.user_type_id, 
	u.display_name,
	u.user_login
FROM
	request_users ru
	JOIN users u
	on ru.user_id = u.user_id
WHERE
	ru.sys_id = @systemId AND
	ru.request_id = @requestId


SELECT 
	CASE SUM(CONVERT(INT, p.pview))
		WHEN 0 THEN 0
		ELSE 4
	END ''permission''
FROM
(
	SELECT DISTINCT
		(rup.gpermissions | rqp.gpermissions) ''permission''
	FROM
		fields f
		/*
		 * Join with the mail_list_users to consider if the group the user is present in 
		 * has any association with the business area or the request.
		 */
		LEFT JOIN mail_list_users mlu
		ON mlu.user_id = @userId
		/*
		 * Get the permissions the user has by virtue of his association with the 
		 * business area.
		 */
		JOIN roles_permissions rup
		ON f.sys_id = rup.sys_id AND f.field_id = rup.field_id
		LEFT JOIN roles_users ru
		ON rup.sys_id = ru.sys_id AND rup.role_id = ru.role_id AND ru.is_active = 1
		/*
		 * Get the permissions the user has by virtue of his association with the 
		 * request.
		 */
		JOIN roles_permissions rqp
		ON f.sys_id = rqp.sys_id AND f.field_id = rqp.field_id
		LEFT JOIN request_users rq
		ON rqp.sys_id = rq.sys_id AND rqp.role_id = rq.user_type_id AND rq.request_id = @requestId
	WHERE
		f.sys_id = @systemId AND
		f.field_id = @privateFieldId AND
		(
			rup.role_id = 1 OR
			(
				ru.user_id IS NOT NULL AND
				(
					ru.user_id = @userId OR
					ru.user_id = mlu.mail_list_id
				)
			)
		) AND
		(
			rqp.role_id = 1 OR
			(
				rq.user_id IS NOT NULL AND
				(
					rq.user_id = @userId OR
					rq.user_id = mlu.mail_list_id
				)
			)
		)
	) as a
	JOIN permissions p
	ON a.permission = p.permission

IF (@actionOrder = 0)
BEGIN             
	SELECT 
		action_id,
		request_id,
		is_private,
		sys_id,
		u.display_name,
		u.user_login,
		lastupdated_datetime,
		description,
		header_description
	FROM
		actions a
		JOIN users u
		on a.user_id = u.user_id
	WHERE
		a.sys_id = @systemId AND
		a.request_id = @requestId
	ORDER BY action_id asc
END
ELSE
BEGIN
	SELECT 
		action_id,
		request_id,
		is_private,
		sys_id,
		u.display_name,
		u.user_login,
		lastupdated_datetime,
		description,
		header_description
	FROM
		actions a
		JOIN users u
		on a.user_id = u.user_id
	WHERE
		a.sys_id = @systemId AND
		a.request_id = @requestId
	ORDER BY action_id desc
END


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_admin_getBusinessAreasByUserId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_admin_getBusinessAreasByUserId] 
(
	@user_id int 
)
AS

DECLARE @superUserId INT
SELECT 
	@superUserId = user_id 
FROM 
	super_users 
WHERE 
	user_id = @user_id

IF (@superUserId is not null)
BEGIN
	SELECT 
		*
	FROM 
		business_areas ba
END
ELSE
BEGIN
	SELECT 
		* 
	FROM 
		business_areas 
	WHERE 
		sys_id in 
		(
		SELECT 
			ba.sys_id
		FROM 
			business_areas ba
			JOIN roles_users ru
			ON ba.sys_id = ru.sys_id 
		WHERE
			ru.role_id in (8, 9, 10) AND
			ru.user_id = @user_id AND
			ru.is_active = 1
		)
END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ba_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_ba_insert] 
	-- Add the parameters for the stored procedure here	
	@name 			NVARCHAR(128),
	@display_name 		NVARCHAR(128),
	@email 			NVARCHAR(255),
	@sys_prefix 		NVARCHAR(32),
	@description 		NVARCHAR(255),
	@type 			NVARCHAR(32),
	@location 		NVARCHAR(32),
	@date_created 		DATETIME,
	@max_request_id 	INT,
	@max_email_actions 	INT,
	@is_email_active 	BIT,
	@is_active 		BIT,
	@is_private 		BIT,
	@sys_config 		NTEXT,
	@field_config 		NTEXT,
	@rSystemId     INT OUTPUT
AS
BEGIN

DECLARE @systemId INT

SELECT 
	@systemId = ISNULL(max(sys_id), 0) 
FROM 
	business_areas

SELECT @systemId = @systemId + 1

-- Insert statements for procedure here
INSERT INTO business_areas 
( 
	sys_id, 
	name, 
	display_name, 
	email, 
	sys_prefix, 
	description, 
	type, 
	location, 
	max_request_id, 
	max_email_actions, 
	is_email_active, 
	is_active, 
	date_created, 
	is_private, 
	sys_config, 
	field_config	
)
VALUES
(
	@systemId,
	@name,
	@display_name,
	@email,
	@sys_prefix,
	@description,
	@type,
	@location,	
	@max_request_id,
	@max_email_actions,
	@is_email_active,
	@is_active,
	@date_created,
	@is_private,
	@sys_config,
	@field_config
)
SELECT @rSystemId = @systemId
END

SET ANSI_NULLS ON
' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_action_getUpdatedRequests]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_action_getUpdatedRequests]
(
	@since 	datetime
)
AS
SELECT DISTINCT
	a.sys_id ''sys_id'',
	a.request_id ''request_id'',
	ba.sys_prefix ''sys_prefix''
FROM 
	actions a
	JOIN business_areas ba
	ON a.sys_id = ba.sys_id
WHERE
	lastupdated_datetime >= @since

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ba_incrAndGetVersionNumber]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- TODO: Alter the procedure to delete_all_requests and delete_all_requests_in_ba such that it resets the max_version_no too.
CREATE PROCEDURE [dbo].[stp_ba_incrAndGetVersionNumber] 
	@systemId INT
AS
BEGIN
	SET NOCOUNT ON;
	--- Read the max request id from business areas and add one to it.
	--SET TRANSACTION ISOLATION LEVEL READ COMMITTED
	BEGIN TRANSACTION
	UPDATE business_areas 
	SET 
			max_version_no = ISNULL(max_version_no, 0) + 1 
	WHERE 
			sys_id = @systemId 
	
	SELECT 
			max_version_no
	FROM 
			business_areas 
	WHERE 
			sys_id = @systemId 
	commit TRAN
END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ba_incrAndGetRequestId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author:		Sandeep Giri
-- Create date: 14 Feb 2009
-- Description:	increments the requestId value and gets the previous value
-- =============================================
CREATE PROCEDURE [dbo].[stp_ba_incrAndGetRequestId] 
	@systemId INT
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
	--- Read the max request id from business areas and add one to it.
	--SET TRANSACTION ISOLATION LEVEL READ COMMITTED
	BEGIN TRANSACTION
	UPDATE business_areas 
	SET 
			max_request_id = ISNULL(max_request_id, 0) + 1 
	WHERE 
			sys_id = @systemId 
	
	SELECT 
			max_request_id
	FROM 
			business_areas 
	WHERE 
			sys_id = @systemId 
	--- We cannot hold this value of max_request_id with us till the end of this transaction 
	--- as other processes might be interested in inserting requests. 
	--- So, update the business_areas table with this new max_request_id value. 
	commit TRAN
END


set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ba_update]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_ba_update] 
(
	@sys_id 		INT,
	@name 			NVARCHAR(128),
	@display_name 		NVARCHAR(128),
	@email 			NVARCHAR(255),
	@sys_prefix 		NVARCHAR(32),
	@description 		NVARCHAR(255),
	@type 			NVARCHAR(32),
	@location 		NVARCHAR(32),
	@date_created 		DATETIME,
	@max_request_id 	INT,
	@max_email_actions 	INT,
	@is_email_active 	BIT,
	@is_active 		BIT,
	@is_private 		BIT,
	@sys_config 		NTEXT,
	@field_config 		NTEXT
)
AS
UPDATE business_areas
SET
	name 			= @name,
	display_name 		= @display_name,
	email 			= @email,
	sys_prefix 		= @sys_prefix,
	description 		= @description,
	type 			= @type,
	location 		= @location,                     
	max_email_actions 	= @max_email_actions,
	is_email_active 	= @is_email_active,
	is_active 		= @is_active,
	date_created 		= @date_created,
	is_private 		= @is_private,
	sys_config 		= @sys_config,
	field_config 		= @field_config
WHERE sys_id = @sys_id

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ba_lookupBySystemPrefix]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_ba_lookupBySystemPrefix]
(
	@systemPrefix VARCHAR(128)
)
AS
SELECT 
	* 
FROM
	business_areas
WHERE
	sys_prefix = @systemPrefix

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ba_lookupBySystemId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_ba_lookupBySystemId]
(
	@systemId INT
)
AS
SELECT 
	* 
FROM
	business_areas
WHERE
	sys_id = @systemId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ba_getAllBusinessAreas]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_ba_getAllBusinessAreas]
AS

SELECT 
	* 
FROM 
	business_areas

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ba_lookupByName]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_ba_lookupByName]
(
	@name VARCHAR(256)
)
AS
SELECT 
	* 
FROM
	business_areas
WHERE
	name = @name

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ba_lookupByEmail]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_ba_lookupByEmail]
(
	@email VARCHAR(256)
)
AS
SELECT 
	* 
FROM
	business_areas
WHERE
	email = @email OR 		--- For those BAs with a single email id.
	email like ''%'' + @email + ''%'' 	--- For those BAs with multiple email ids.

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ba_getBusinessAreasByUserId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_ba_getBusinessAreasByUserId]
(
	@userId		INT
)
AS
DECLARE @baFieldId INT
DECLARE @ipFieldId INT
SELECT @baFieldId = field_id FROM fields WHERE name = ''sys_id''
SELECT @ipFieldId = field_id FROM fields WHERE name = ''is_private''
/*
 * Script that lists out the Business area visible to a list of roles specified.
 */
SELECT 
	*
FROM 
	business_areas 
WHERE 
	sys_id IN
(
	SELECT 
		ba.sys_id
	FROM
		business_areas ba
		JOIN roles_permissions rp 			-- This is for checking the user/logger permission on sys_id field.
		ON rp.sys_id = ba.sys_id
		JOIN roles_permissions rpp		-- This is for checking the user/logger permission on is_private field.
		ON rp.sys_id = rpp.sys_id AND rp.field_id = @baFieldId AND rpp.field_id = @ipFieldId
		LEFT JOIN roles_users ru		-- This is for considering the user''s BA specific roles.
		ON rp.sys_id = ru.sys_id AND ru.user_id = @userId AND ru.is_active = 1
		LEFT JOIN roles_permissions rpba	-- This is for checking the BA''s Specific Role''s permission on sys_id field.
		ON rpba.sys_id = ru.sys_id AND rpba.role_id = ru.role_id AND rpba.field_id = @baFieldId
		LEFT JOIN roles_permissions rpip	-- This is for checking the BA''s Specific Role''s permission on is_private field.
		ON rpip.sys_id = ru.sys_id AND rpip.role_id = ru.role_id AND rpip.field_id = @ipFieldId
	WHERE
		ba.is_active = 1 AND 
		rp.role_id in (1) AND
		rpp.role_id in (1) AND 
		(
			(
				-- If BA is normal, then VIEW permission is required on sys_id 
				-- by virtue either of User/Logger role OR BA Role the user is associated with.
				ba.is_private = 0 AND ((rp.gpermissions & 4) <> 0  OR (rpba.gpermissions & 4) <> 0 )
			)
			OR
			(
				-- If BA is Private, then VIEW permission is required on sys_id along with VIEW permission on private field
				-- by virtue either of User/Logger role OR of BA Role the user is associated with.
				ba.is_private = 1 AND 
				(
					(
						(rp.gpermissions & 4) <> 0 OR 
						(rpba.gpermissions & 4) <> 0
					)AND 
					(
						(rpp.gpermissions & 4) <> 0 OR
						(rpip.gpermissions & 4) <> 0
					)
				)
			)
		)
)
ORDER BY display_name

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_daction_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_daction_insert]
(
	@systemId	INT,
	@requestId	INT,
	@actionId	INT,
	@dActionLog	text
)
AS
INSERT INTO daction_log
(
	sys_id, 
	request_id, 
	action_id, 
	daction_log
)
VALUES
(
	@systemId, 
	@requestId, 
	@actionId, 
	@dActionLog
)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_admin_getAllDataTypes]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_admin_getAllDataTypes]
AS

SELECT 
	* 
FROM
	datatypes

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_dtf_getAllDateTimeFormats]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_dtf_getAllDateTimeFormats]
AS

SELECT 
	*
FROM 
	datetime_formats

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_severity_escalation]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
/****** Object:  StoredProcedure [dbo].[stp_severity_escalation]    Script Date: 07/31/2008 03:22:35 ******/
-- =============================================
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
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_dependencies_getAppSyncDependencies]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_dependencies_getAppSyncDependencies]
AS

SELECT 
	d.sys_id,
	d.dep_id,
	d.dep_name,
	d.dep_level,
	d.dep_type,
	d.dep_config
FROM 
	dependencies d
	JOIN 
	(
		SELECT DISTINCT 
			d.sys_id, 
			d.dep_id 
		FROM 
			fields f
			JOIN dependent_fields df
			ON f.sys_id = df.sys_id AND f.field_id = df.field_id
			JOIN dependencies d
			ON df.sys_id = d.sys_id AND df.dep_id = d.dep_id
		WHERE
			f.data_type_id = 9 AND
			d.dep_level = ''APP_DEPENDENCY''
	) as temp
	ON d.sys_id = temp.sys_id AND d.dep_id = temp.dep_id

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_dependencies_getAllDependencies]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_dependencies_getAllDependencies]
AS

SELECT 
	sys_id,
	dep_id,
	dep_name,
	dep_level,
	dep_type,
	dep_config
FROM 
	dependencies

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_depfield_getAllDependentFields]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_depfield_getAllDependentFields]
AS
SELECT 
	* 
FROM 
	dependent_fields

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_wr_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_wr_insert] 
	-- Add the parameters for the stored procedure here	
	@ruleName 			NVARCHAR(3000),
	@ruleDefinition			NTEXT,
	@wrId				INT OUTPUT
AS
BEGIN

DECLARE @wfRule_Id INT

SELECT 
	@wfRule_Id = ISNULL(max(rule_id), 0) 
FROM 
	workflow_rules

SELECT @wfRule_Id = @wfRule_Id + 1

-- Insert statements for procedure here
INSERT INTO workflow_rules
( 
	rule_id, 
	rule_name, 
	rule_definition	
)
VALUES
(
	@wfRule_Id,
	@ruleName,
	@ruleDefinition
)
SELECT @wrId = @wfRule_Id
END

SET ANSI_NULLS ON' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_admin_insert_escalation_heirarchy]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_admin_insert_escalation_heirarchy] 
(
	@sysId 			INT,
	@userId			INT,
	@parentUserId 	INT
)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
IF NOT EXISTS (SELECT * FROM escalation_heirarchy
					WHERE sys_id=@sysId and 
						user_id=@userId and
						parent_user_id=@parentUserId)
INSERT INTO escalation_heirarchy (
	sys_id,
	user_id,
	parent_user_id
)
VALUES(
	@sysId,
	@userId,
	@parentUserId
)
END

/****** Object:  StoredProcedure [dbo].[stp_get_escalation_span]    Script Date: 07/31/2008 03:22:52 ******/
-- =============================================
-- Author:		Sandeep Giri
-- Create date: 
-- Description:	
-- =============================================
SET ANSI_NULLS ON
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_admin_delete_escalation_heirarchy]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_admin_delete_escalation_heirarchy] 
(
	@sysId 			INT,
	@userId			INT
)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
IF EXISTS (SELECT * FROM escalation_heirarchy
					WHERE sys_id=@sysId and 
						user_id=@userId)
DELETE FROM escalation_heirarchy WHERE 
	sys_id=@sysId and 
	user_id=@userId 
END

/****** Object:  StoredProcedure [dbo].[stp_admin_insert_escalation_condition]    Script Date: 02/02/2009 20:22:07 ******/
SET ANSI_NULLS ON
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_admin_insert_escalation_condition]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_admin_insert_escalation_condition] 
(
	@sysId 			INT,
	@severityId 	INT,
	@span		 	INT,
	@categoryId		INT,
	@statusId		INT,
	@typeId			INT
)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
IF NOT EXISTS (SELECT * FROM escalation_conditions 
					WHERE sys_id = @sysId and 
						severity_id= @severityId and
						span=@span and
						category_id=@categoryId and
						status_id=@statusId and
						type_id=@typeId)
INSERT INTO escalation_conditions (
	sys_id,
	severity_id,
	span,
	category_id,
	status_id,
	type_id
)
VALUES(
	@sysId,
	@severityId,
	@span,
	@categoryId,
	@statusId,
	@typeId
)
END

/****** Object:  StoredProcedure [dbo].[stp_admin_insert_escalation_heirarchy]    Script Date: 02/02/2009 20:23:07 ******/
SET ANSI_NULLS ON
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_admin_delete_escalation_condition]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_admin_delete_escalation_condition] 
(
	@sysId 			INT,
	@severityId 	INT,
	@span		 	INT,
	@categoryId		INT,
	@statusId		INT,
	@typeId			INT
)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

DELETE FROM escalation_conditions WHERE 
	sys_id=@sysId and
	severity_id=@severityId and
	span=@span and
	category_id=@categoryId and
	status_id=@statusId and
	type_id=@typeId

END

/****** Object:  StoredProcedure [dbo].[stp_admin_delete_escalation_heirarchy]    Script Date: 02/02/2009 20:20:32 ******/
SET ANSI_NULLS ON
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_get_escalation_span]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
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
	and ((status_id = 0) or (status_id = @status_id)) 
	and ((category_id = 0) or (category_id = @category_id))
	and ((severity_id = 0) or (severity_id = @severity_id))
	and ((type_id = 0) or (type_id = @type_id))
END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_el_getCompleteExclusionList]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_el_getCompleteExclusionList]
AS

SELECT 
	*
FROM 
	exclusion_list

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_er_getAllExternalResources]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_er_getAllExternalResources]

AS

SELECT
	resource_id,
	resource_name,
	resource_def
FROM 
	external_resources

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_er_lookupByName]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_er_lookupByName]
(
	@resource_name 	VARCHAR(128)
)
AS

SELECT
	resource_id,
	resource_name,
	resource_def
FROM 
	external_resources
WHERE
	resource_name = @resource_name

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_er_lookupById]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_er_lookupById]
(
	@resource_id 	INT
)
AS

SELECT
	resource_id,
	resource_name,
	resource_def
FROM 
	external_resources
WHERE
	resource_id = @resource_id

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_fd_getAllFieldDescriptors]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_fd_getAllFieldDescriptors]
AS
SELECT 
	f.sys_id,
	f.field_id,
	ISNULL(fd.field_descriptor, f.name) ''field_descriptor'',
	ISNULL(fd.is_primary, 0) ''is_primary''
FROM 
	fields f
	LEFT JOIN field_descriptors fd
	ON f.sys_id = fd.sys_id AND f.field_id = fd.field_id
ORDER by f.sys_id, f.field_id

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_fd_getFieldDescriptorsBySystemIdAndFieldId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_fd_getFieldDescriptorsBySystemIdAndFieldId]
(
	@sys_id 	INT,
	@field_id 	INT
)
AS

SELECT 
	f.sys_id,
	f.field_id,
	ISNULL(fd.field_descriptor, f.name) ''field_descriptor'',
	ISNULL(fd.is_primary, 0) ''is_primary''
FROM 
	fields f
	LEFT JOIN field_descriptors fd
	ON f.sys_id = fd.sys_id AND f.field_id = fd.field_id

WHERE 
	  f.sys_id = @sys_id AND
       	  f.field_id = @field_id

ORDER by f.sys_id, f.field_id

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_fd_getDescriptorTable]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_fd_getDescriptorTable]
(
	@systemId	INT
)
AS
SELECT 
	f.*, 
	fd.field_descriptor
FROM
	fields f
	LEFT JOIN field_descriptors fd
	ON f.sys_id = fd.sys_id AND f.field_id = fd.field_id
WHERE
	f.sys_id = @systemId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_fd_getAllDescriptors]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_fd_getAllDescriptors]
AS
SELECT DISTINCT
	0 ''sys_id'',
	f.field_id ''field_id'',
	f.name ''name'',
	f.display_name ''display_name'',
	f.description ''description'',
	f.data_type_id ''data_type_id'',
	f.is_active ''is_active'',
	f.is_extended ''is_extended'',
	f.is_private ''is_private'',
	f.tracking_option ''tracking_option'',
	f.permission ''permission'',
	f.regex ''regex'',
	f.is_dependent,
	f.display_order ''display_order'',
	f.display_group ''display_group'',
	fd.field_descriptor ''field_descriptor''
FROM
	fields f
	JOIN field_descriptors fd
	ON f.sys_id = fd.sys_id AND f.field_id = fd.field_id
WHERE
	f.is_active = 1
ORDER BY f.field_id, f.name

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tbits_insertStandardFieldDefaults]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'



CREATE PROCEDURE [dbo].[stp_tbits_insertStandardFieldDefaults]
(
    @systemId INT
)
AS

/*
 * DELETE standard fields if any for this business area and insert them again.
 */
DELETE fields WHERE sys_id = @systemId AND is_extended = 0

INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  1, ''sys_id'', ''Business Area'', ''sys_id'', 5, 1, 0, 0, 0, 6, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  2, ''request_id'', ''Request'', ''Request'', 5, 1, 0, 0, 0, 47, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  3, ''category_id'', ''Category'', ''category_id'', 9, 1, 0, 0, 3, 254, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  4, ''status_id'', ''Status'', ''status_id'', 9, 1, 0, 0, 3, 126, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  5, ''severity_id'', ''Severity'', ''severity_id'', 9, 1, 0, 0, 3, 126, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  6, ''request_type_id'', ''Article Type'', ''request_type_id'', 9, 1, 0, 0, 3, 126, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  7, ''logger_ids'', ''Author'', ''logger_ids'', 10, 1, 0, 0, 3, 191, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  8, ''assignee_ids'', ''Reviewer'', ''assignee_ids'', 10, 1, 0, 0, 3, 191, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  9, ''subscriber_ids'', ''Subscribers'', ''subscriber_ids'', 10, 1, 0, 0, 5, 63, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  10, ''to_ids'', ''To'', ''to_ids'', 10, 1, 0, 0, 2, 125, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  11, ''cc_ids'', ''Cc'', ''cc_ids'', 10, 1, 0, 0, 2, 125, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  12, ''subject'', ''Subject'', ''summary'', 7, 1, 0, 0, 3, 127, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  13, ''description'', ''Description'', ''description'', 8, 1, 0, 0, 0, 103, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  14, ''is_private'', ''Private'', ''is_private'', 1, 1, 0, 0, 3, 118, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  15, ''parent_request_id'', ''Parent'', ''parent_request_id'', 5, 1, 0, 0, 3, 127, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  16, ''user_id'', ''Last Update By'', ''user_id'', 10, 1, 0, 0, 0, 44, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  17, ''max_action_id'', ''# Updates'', ''max_action_id'', 5, 1, 0, 0, 0, 44, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  18, ''due_datetime'', ''Due Date'', ''due_datetime'', 4, 1, 0, 0, 3, 127, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  19, ''logged_datetime'', ''Logged Date'', ''logged_datetime'', 4, 1, 0, 0, 0, 44, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  20, ''lastupdated_datetime'', ''Last Updated'', ''lastupdated_datetime'', 4, 1, 0, 0, 0, 44, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  21, ''header_description'', ''Header Description'', ''header_description'', 8, 1, 0, 0, 0, 4, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  22, ''attachments'', ''Attachments'', ''attachments'', 8, 1, 0, 0, 0, 103, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  23, ''summary'', ''Summary'', ''Summary'', 8, 1, 0, 0, 1, 103, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  24, ''memo'', ''Memo'', ''Memo'', 8, 1, 0, 0, 0, 7, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  25, ''append_interface'', ''Append Interface'', ''append_interface'', 5, 1, 0, 0, 0, 0, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  26, ''notify'', ''Notify'', ''notify'', 1, 1, 0, 0, 1, 126, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  27, ''notify_loggers'', ''Notify Author'', ''notify_loggers'', 1, 1, 0, 0, 1, 126, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  28, ''replied_to_action'', ''Replied To Action'', ''replied_to_action'', 5, 1, 0, 0, 0, 70, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  29, ''related_requests'', ''Linked Requests'', ''related_requests'', 7, 1, 0, 0, 4, 23, '''', 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent) VALUES(@systemId,  30, ''office_id'', ''Office'', ''Office'', 9, 1, 0, 0, 3, 126, '''', 0)

/*
 * DELETE field descriptors of standard fields if any and insert them again.
 */
DELETE field_descriptors 
WHERE 
    sys_id = @systemId AND
    field_id IN 
    (
        SELECT 
            field_id 
        FROM 
            fields 
        WHERE 
            sys_id = @systemId AND 
            is_extended = 0
    )
    
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 1, ''ba'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 2, ''req'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 3, ''cat'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 4, ''stat'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 5, ''sev'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 6, ''reqtype'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 6, ''type'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 7, ''log'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 8, ''ass'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 8, ''assignee'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 9, ''sub'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 10, ''to'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 11, ''cc'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 12, ''subj'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 13, ''alltext'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 13, ''desc'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 14, ''conf'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 14, ''private'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 15, ''par'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 16, ''user'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 17, ''updates'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 18, ''ddate'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 18, ''due'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 18, ''dueby'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 18, ''duedate'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 19, ''ldate'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 19, ''loggeddate'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 20, ''udate'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 20, ''updateddate'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 22, ''att'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 23, ''sum'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 23, ''summary'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 26, ''mail'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 26, ''notify'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 27, ''notlog'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 29, ''link'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 29, ''relreq'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 30, ''off'', 1)

 


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_fd_lookupBySystemIdAndFieldDescriptor]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_fd_lookupBySystemIdAndFieldDescriptor]
(
	@systemId	INT,
	@fieldDesc	VARCHAR(128)
)
AS
SELECT
	f.*
FROM
	fields f
	LEFT JOIN field_descriptors fd
	ON f.sys_id = fd.sys_id AND f.field_id = fd.field_id
WHERE
	f.sys_id = @systemId AND
	(
		fd.field_descriptor = @fieldDesc OR
		(
			f.name = @fieldDesc OR
			f.display_name = @fieldDesc
		)
	)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_field_descriptor_delete]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_field_descriptor_delete]
(
	@sys_id			INT,
	@field_id		INT,
	@field_descriptor	NVARCHAR(32),
	@is_primary         	BIT
)
AS
delete field_descriptors
where 
        sys_id              = @sys_id AND
        field_id            = @field_id AND
        field_descriptor    = @field_descriptor AND
        is_primary          = @is_primary

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_fd_lookupBySystemId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_fd_lookupBySystemId]
(	
	@sysId int
)
AS
SELECT 
	f.sys_id,
	f.field_id,
	ISNULL(fd.field_descriptor, f.name) ''field_descriptor'',
	ISNULL(fd.is_primary, 0) ''is_primary''
FROM 
	fields f
	LEFT JOIN field_descriptors fd
	ON f.sys_id = fd.sys_id AND f.field_id = fd.field_id
WHERE 
	fd.sys_id = @sysId
ORDER by f.sys_id, f.field_id

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_fd_getPrimaryDescriptorByFieldId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_fd_getPrimaryDescriptorByFieldId]
(
	@systemId	INT,
	@fieldId		INT
)
AS

SELECT
    *
FROM
	field_descriptors 
WHERE
	sys_id      = @systemId AND
	field_id    = @fieldId 	AND
	is_primary  = 1

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_field_descriptor_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_field_descriptor_insert]
(
	@sys_id			INT,
	@field_id		INT,
	@field_descriptor	NVARCHAR(32),
	@is_primary         	BIT
)
AS
INSERT INTO field_descriptors
(
	sys_id,
	field_id,
	field_descriptor,
	is_primary
)
VALUES
(
	@sys_id,
	@field_id,
	@field_descriptor,
	@is_primary
)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_field_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_field_insert]
(
	@sys_id			INT,
	@field_id		INT,
	@name			NVARCHAR(255),
	@display_name		NVARCHAR(255),
	@description		NVARCHAR(255),
	@data_type_id		INT,
	@is_active		BIT,
	@is_extended		BIT,
	@is_private		BIT,
	@tracking_option 	INT,
	@permission		INT,
	@regex			VARCHAR(7999),
	@is_dependent	BIT,
	@display_order	INT,
	@display_group	INT
)
AS
DECLARE @fieldID int
SELECT 	@fieldID = (ISNULL(MAX(field_id), 0) + 1) from fields where sys_id = @sys_id
INSERT INTO fields
(
	sys_id,
	field_id,
	name,
	display_name,
	description,
	data_type_id,
	is_active,
	is_extended,
	is_private,
	tracking_option,
	permission,
	regex,
	is_dependent,
	display_order,
	display_group
)
VALUES
(
	@sys_id,
	@fieldID,
	@name,
	@display_name,
	@description,
	@data_type_id,
	@is_active,
	@is_extended,
	@is_private,
	@tracking_option,
	@permission,
	@regex,
	@is_dependent,
	@display_order,
	@display_group
)
DECLARE @i INT
DECLARE @maxRoleId INT
SELECT @i = 1
SELECT @maxRoleId = max(role_id) from roles
WHILE (@i < @maxRoleId)
BEGIN
	print ''INSERT INTO roles_permissions values('' 
			+ cast(@sys_id as varchar(20)) + '','' 
			+ cast(@i as varchar(20)) + '','' + 
			+ cast(@fieldId as varchar(20)) + '' , 4, 0)'';
	INSERT INTO roles_permissions (sys_id, role_id, field_id, gpermissions, dpermissions) values(@sys_id, @i, @fieldId, 4, 0)
	print ''FINISHED INSERT''
	SELECT @i = @i +1
END


' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_field_getFixedFieldsBySystemId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_field_getFixedFieldsBySystemId]
(
	@systemId INT
)
AS
SELECT 
    *
FROM 
	fields
WHERE
	sys_id = @systemId AND
	is_extended = 0

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_field_getFieldsBySystemIdAndUserId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_field_getFieldsBySystemIdAndUserId]
(
	@systemId INT,
	@userId   INT
)
AS
SELECT DISTINCT 
	f.*
FROM
	fields f
	JOIN roles_permissions rp
	ON f.sys_id = rp.sys_id AND (rp.role_id = 1 OR rp.role_id = 2) AND f.field_id = rp.field_id
	LEFT JOIN roles_users ru
	ON ru.sys_id = f.sys_id AND ru.user_id = @userId AND ru.is_active = 1
	LEFT JOIN roles_permissions rpp
	ON ru.sys_id = rpp.sys_id AND ru.role_id = rpp.role_id AND rpp.field_id = f.field_id 
WHERE
	f.sys_id = @systemId AND
	( 	
		/*
		 * Check if user has permissions by virtue of the user/logger role.
		 */
		(
			
			(rp.gpermissions & 4)  <> 0  
		)
		OR 
		/*
		 * Check if user has permissions by virtue of his association with the BA.
		 */
		(
			(rpp.gpermissions & 4) <> 0
		)
	)
ORDER BY f.field_id


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_field_getExtendedFieldsBySystemId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_field_getExtendedFieldsBySystemId]
(
	@systemId INT
)
AS
SELECT 
    *
FROM 
	fields
WHERE
	sys_id = @systemId AND
	is_extended = 1

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_field_getAllFields]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_field_getAllFields]
AS

SELECT 
	* 
FROM 
	fields

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_field_update]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_field_update] 
(
	@sys_id 		INT,	
	@field_id 		INT,
	@name 			NVARCHAR(128),
	@display_name 		NVARCHAR(128),
	@description 		NVARCHAR(128),
	@data_type_id 		INT,
	@is_active 		BIT,
	@is_extended 		BIT,
	@is_private 		INT,
	@tracking_option 	INT,
	@permission 		INT,
	@regex 			NVARCHAR(2048),
	@is_dependent	BIT,
	@display_order	INT,
	@display_group	INT
)
AS
UPDATE fields
SET
	name 			= @name,
	display_name 		= @display_name,
	description 		= @description,
	data_type_id 		= @data_type_id,
	is_active 		= @is_active,
	is_extended 		= @is_extended,
	is_private 		= @is_private,
	tracking_option 	= @tracking_option,
	permission 		= @permission,
	regex 			= @regex,
	is_dependent	= @is_dependent,
	display_order	= @display_order,
	display_group	= @display_group
WHERE 
        field_id = @field_id AND 
        sys_id = @sys_id


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_field_lookupBySystemIdAndFieldId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'


CREATE PROCEDURE [dbo].[stp_field_lookupBySystemIdAndFieldId] 
( 
        @systemId INT, 
        @fieldId  INT 
) 
AS 
SELECT 
        sys_id, 
        field_id, 
        name, 
        display_name, 
        description, 
        data_type_id, 
        is_active, 
        is_extended, 
        is_private, 
        tracking_option, 
        permission, 
        regex, 
        is_dependent,
		display_order,
		display_group
FROM 
        fields 
WHERE 
        sys_id = @systemId AND 
        field_id = @fieldId 




' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_field_lookupBySystemIdAndFieldName]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_field_lookupBySystemIdAndFieldName]
(
	@systemId  INT,
	@fieldName VARCHAR(256)
)
AS
SELECT 
    *
FROM
	fields
WHERE
	sys_id 	= @systemId AND
	name	= @fieldName

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_field_lookupBySystemId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_field_lookupBySystemId]
(
	@systemId INT
)
AS
SELECT 
    *
FROM
	fields
WHERE
	sys_id = @systemId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_request_getRequestPrivacy]') AND type in (N'FN', N'IF', N'TF', N'FS', N'FT'))
BEGIN
execute dbo.sp_executesql @statement = N'
CREATE FUNCTION [dbo].[stp_request_getRequestPrivacy]
(
	@systemId 	INT,
	@requestId      INT
)
RETURNS BIT 

AS

BEGIN

   DECLARE @isPrivate  BIT
   DECLARE @private    BIT
   DECLARE @fieldId    INT
   DECLARE @typeId     INT
   DECLARE @parentId   INT

   SELECT @private = 0
   SELECT @isPrivate = is_private FROM requests WHERE sys_id = @systemId AND request_id = @requestId

    IF (@isPrivate = 1)
      BEGIN
        SELECT @private = 1
        RETURN @private 
      END

    SELECT @typeId= category_id FROM requests WHERE sys_id = @systemId AND request_id = @requestId
    SELECT @fieldId = field_id FROM fields WHERE sys_id = @systemId AND name=''category_id''
    SELECT @isPrivate = is_private FROM types WHERE sys_id = @systemId AND field_id = @fieldId AND type_id = @typeId
    IF (@isPrivate = 1)
      BEGIN
        SELECT @private = 1
        RETURN @private 
      END

    SELECT @typeId= status_id FROM requests WHERE sys_id = @systemId AND request_id = @requestId
    SELECT @fieldId = field_id FROM fields WHERE sys_id = @systemId AND name=''status_id''
    SELECT @isPrivate = is_private FROM types WHERE sys_id = @systemId AND field_id = @fieldId AND type_id = @typeId
    IF (@isPrivate = 1)
      BEGIN
        SELECT @private = 1
        RETURN @private 
      END

    SELECT @typeId= severity_id FROM requests WHERE sys_id = @systemId AND request_id = @requestId
    SELECT @fieldId = field_id FROM fields WHERE sys_id = @systemId AND name=''severity_id''
    SELECT @isPrivate = is_private FROM types WHERE sys_id = @systemId AND field_id = @fieldId AND type_id = @typeId
    IF (@isPrivate = 1)
      BEGIN
         SELECT @private = 1
        RETURN @private 
      END

     SELECT @typeId= request_type_id FROM requests WHERE sys_id = @systemId AND request_id = @requestId
     SELECT @fieldId = field_id FROM fields WHERE sys_id = @systemId AND name=''request_type_id''
     SELECT @isPrivate = is_private FROM types WHERE sys_id = @systemId AND field_id = @fieldId AND type_id = @typeId
     IF (@isPrivate = 1)
       BEGIN
          SELECT @private = 1
          RETURN @private 
        END

      SELECT @parentId = parent_request_id FROM requests WHERE sys_id = @systemId AND request_id = @requestId

      WHILE(@parentId != 0)
         BEGIN
            SELECT @isPrivate = is_private FROM requests WHERE sys_id = @systemId AND request_id = @parentId
            IF (@isPrivate = 1)
              BEGIN
                 SELECT @private = 1
                 RETURN @private
               END
            SELECT @parentId = parent_request_id FROM requests WHERE sys_id = @systemId AND request_id = @parentId
       END 

RETURN @private
END

' 
END

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_request_getPrivacy]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
CREATE PROCEDURE [dbo].[stp_request_getPrivacy]
(
	@systemId 	INT,
	@requestId      INT,
	@private        BIT OUTPUT
)
AS

DECLARE @isPrivate BIT
DECLARE @parentId INT
DECLARE @fieldId INT
DECLARE @typeId INT


SELECT @isPrivate = is_private FROM requests WHERE sys_id = @systemId AND request_id = @requestId

if(@isPrivate = 1)
BEGIN
    SELECT @private = 1
    RETURN  
END

SELECT @typeId= category_id FROM requests WHERE sys_id = @systemId AND request_id = @requestId
SELECT @fieldId = field_id FROM fields WHERE sys_id = @systemId AND name=''category_id''
SELECT @isPrivate = is_private FROM types WHERE sys_id = @systemId AND field_id = @fieldId AND type_id = @typeId
    IF (@isPrivate = 1)
    BEGIN
       SELECT @private = 1
       RETURN
    END

SELECT @typeId= status_id FROM requests WHERE sys_id = @systemId AND request_id = @requestId
SELECT @fieldId = field_id FROM fields WHERE sys_id = @systemId AND name=''status_id''
SELECT @isPrivate = is_private FROM types WHERE sys_id = @systemId AND field_id = @fieldId AND type_id = @typeId
    IF (@isPrivate = 1)
    BEGIN
       SELECT @private = 1
       RETURN
    END

SELECT @typeId= severity_id FROM requests WHERE sys_id = @systemId AND request_id = @requestId
SELECT @fieldId = field_id FROM fields WHERE sys_id = @systemId AND name=''severity_id''
SELECT @isPrivate = is_private FROM types WHERE sys_id = @systemId AND field_id = @fieldId AND type_id = @typeId
    IF (@isPrivate = 1)
    BEGIN
       SELECT @private = 1
       RETURN
    END

SELECT @typeId= request_type_id FROM requests WHERE sys_id = @systemId AND request_id = @requestId
SELECT @fieldId = field_id FROM fields WHERE sys_id = @systemId AND name=''request_type_id''
SELECT @isPrivate = is_private FROM types WHERE sys_id = @systemId AND field_id = @fieldId AND type_id = @typeId
    IF (@isPrivate = 1)
    BEGIN
       SELECT @private = 1
       RETURN
    END

SELECT @parentId = parent_request_id FROM requests WHERE sys_id = @systemId AND request_id = @requestId

WHILE(@parentId != 0)
BEGIN
    SELECT @isPrivate = is_private FROM requests WHERE sys_id = @systemId AND request_id = @parentId
    IF (@isPrivate = 1)
    BEGIN
       SELECT @private = 1
       RETURN
    END
    SELECT @parentId = parent_request_id FROM requests WHERE sys_id = @systemId AND request_id = @parentId
END 

SELECT @isPrivate = 0


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_request_getUserPrivatePermissions]') AND type in (N'FN', N'IF', N'TF', N'FS', N'FT'))
BEGIN
execute dbo.sp_executesql @statement = N'
CREATE FUNCTION [dbo].[stp_request_getUserPrivatePermissions]
(
	@systemId 	INT,
	@requestId      INT,
	@userId      INT
)
RETURNS INT

AS

BEGIN

DECLARE @privateField INT
DECLARE @privatePermission INT

SELECT @privateField = field_id FROM fields WHERE name = ''is_private''
   
    /*
     * Get the mailing lists where the user is a member directly or indirectly.
     */
     DECLARE @tmp TABLE
     (
	mailListId INT
     )

     DECLARE @tmp1 TABLE
     (
	mailListId INT
     )

     DECLARE @tmp2 TABLE
     (
	mailListId INT
     )
     /*
      * Get the mailing lists where the user is a direct member.
      */
     INSERT INTO @tmp(mailListId)
     SELECT 
	     mail_list_id 
     FROM 
	     mail_list_users 
     WHERE 
	     user_id = @userId

     
     INSERT INTO @tmp1(mailListId)
     SELECT 
             mailListId
     FROM
             @tmp

     WHILE (EXISTS(SELECT * FROM @tmp1))
     BEGIN
     /*
      * Get the mailing lists where the id in #tmp1 is a member.
      * which is already not part of #tmp
      */
     
     INSERT INTO @tmp2
     SELECT 
          mlu.mail_list_id
     FROM
          mail_list_users mlu
     JOIN @tmp1 t1
     ON mlu.user_id = t1.mailListId
     LEFT JOIN @tmp t
     ON mlu.mail_list_id = t.mailListId
     WHERE
	t.mailListId IS NULL

     INSERT INTO @tmp1 SELECT * FROM @tmp2
     INSERT INTO @tmp SELECT * FROM @tmp2
     DELETE @tmp2
     DELETE @tmp1
     END

DELETE @tmp1

SELECT @privatePermission = 
	CASE SUM(p.padd)
	WHEN 0 then 0
	ELSE 1
	END + 
	CASE SUM(p.pchange)
	WHEN 0 then 0
	ELSE 2
	END + 
	CASE SUM(p.pview)
	WHEN 0 then 0
	ELSE 4
	END 
FROM
	permissions p
	JOIN
	(
	/*
	 * Get the permissions the user gets by virtue of being a user of the system.
	 */
	SELECT
		f.name,
		f.field_id,
		rp.gpermissions
	FROM
		roles_permissions rp
		JOIN fields f
		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id
	WHERE
		rp.sys_id = @systemId AND 
		rp.role_id = 1
	
	UNION
	
	/*
	 * Get the permissions the user gets by virtue of being a part of the BA.
	 */
	SELECT
		f.name,
		f.field_id,
		rp.gpermissions
	FROM
		roles_permissions rp
		JOIN fields f
		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id
		JOIN roles_users ru
		ON ru.sys_id = rp.sys_id AND ru.role_id = rp.role_id
	WHERE
		rp.sys_id = @systemId AND
		(
			ru.user_id = @userId OR
			ru.user_id IN (SELECT mailListId FROM @tmp)
		)
	UNION
	/*
	 * Get the permissions the user gets by virtue of being a part of this request.
	 */
	SELECT
		f.name,
		f.field_id,
		rp.gpermissions
	FROM
		roles_permissions rp
		JOIN fields f
		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id
		JOIN request_users ru
		ON ru.sys_id = @systemId AND ru.request_id = @requestId AND ru.user_type_id = rp.role_id
	WHERE
		rp.sys_id = @systemId AND
		(
			ru.user_id = @userId OR
			ru.user_id IN (SELECT mailListId FROM @tmp)
		)
	) t
	ON p.permission = t.gpermissions WHERE t.field_id =  @privateField



/*
 * Finally drop the temp table used for holding the mailing lists this user is part of directly or indirectly.
 */
DELETE @tmp
DELETE @tmp1


RETURN @privatePermission
END

' 
END

GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_field_insertWithExistingFieldId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_field_insertWithExistingFieldId]
(
	@sys_id			INT,
	@field_id		INT,
	@name			NVARCHAR(255),
	@display_name		NVARCHAR(255),
	@description		NVARCHAR(255),
	@data_type_id		INT,
	@is_active		BIT,
	@is_extended		BIT,
	@is_private		BIT,
	@tracking_option 	INT,
	@permission		INT,
	@regex			VARCHAR(7999),
	@is_dependent	BIT,
	@display_order	INT,
	@display_group	INT
)
AS
INSERT INTO fields
(
	sys_id,
	field_id,
	name,
	display_name,
	description,
	data_type_id,
	is_active,
	is_extended,
	is_private,
	tracking_option,
	permission,
	regex,
	is_dependent,
	display_order,
	display_group
)
VALUES
(
	@sys_id,
	@field_id,
	@name,
	@display_name,
	@description,
	@data_type_id,
	@is_active,
	@is_extended,
	@is_private,
	@tracking_option,
	@permission,
	@regex,
	@is_dependent,
	@display_order,
	@display_group
)
DECLARE @i INT
DECLARE @maxRoleId INT
SELECT @i = 1
SELECT @maxRoleId = max(role_id) from roles
WHILE (@i < @maxRoleId)
BEGIN
	print ''INSERT INTO roles_permissions values('' 
			+ cast(@sys_id as varchar(20)) + '','' 
			+ cast(@i as varchar(20)) + '','' + 
			+ cast(@field_id as varchar(20)) + '' , 4, 0)'';
	INSERT INTO roles_permissions (sys_id, role_id, field_id, gpermissions, dpermissions) values(@sys_id, @i, @field_id, 4, 0)
	print ''FINISHED INSERT''
	SELECT @i = @i +1
END

SET ANSI_NULLS ON
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tbits_insertAuthorizationDefaults]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
CREATE PROCEDURE [dbo].[stp_tbits_insertAuthorizationDefaults]
(
    @systemId INT
)
AS

/*
 * Delete standard roles.
 */
DELETE roles WHERE sys_id = @systemId

INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 1, ''User'', ''User'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 2, ''Logger'', ''Logger'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 3, ''Assignee'', ''Assignee'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 4, ''Subscriber'', ''Subscriber'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 5, ''Cc'', ''Cc'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 6, ''To'', ''To'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 7, ''Analyst'', ''Analyst'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 8, ''Manager'', ''Manager'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 9, ''Admin'', ''Admin'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 10, ''PermissionAdmin'', ''Permission Admin'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 11, ''Customer'', ''Customer'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 12, ''BAUsers'', ''BAUsers'')
/*
 * Delete permissions standard roles have on standard fields.
 */
DELETE roles_permissions 
WHERE 
    sys_id = @systemId AND 
    field_id IN 
    (
        SELECT 
            field_id 
        FROM 
            fields 
        WHERE 
            sys_id = @systemId AND 
            is_extended = 0
    )

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,1,0,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,2,6,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,3,4,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,4,4,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,5,4,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,6,4,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,7,4,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,8,4,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,9,6,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,10,7,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,11,7,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,12,4,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,13,5,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,15,7,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,16,4,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,17,4,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,18,4,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,19,4,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,20,4,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,21,4,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,22,5,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,23,4,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,24,7,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,26,4,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,27,4,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,28,4,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,29,4,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,1,0,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,2,1,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,7,2,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,9,1,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,11,1,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,12,3,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,14,6,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,23,3,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,1,4,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,3,2,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,4,2,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,5,2,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,6,2,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,8,2,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,9,2,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,12,2,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,14,6,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,18,2,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,23,2,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,26,2,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,27,2,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,29,2,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,1,4,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,9,0,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,12,0,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,14,6,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,23,0,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,1,4,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,2,7,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,3,4,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,4,4,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,5,7,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,6,4,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,7,4,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,8,4,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,9,7,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,10,7,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,11,7,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,12,4,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,13,5,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,15,7,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,16,4,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,17,4,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,18,4,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,19,4,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,20,4,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,21,4,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,22,5,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,23,4,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,24,7,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,26,4,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,27,4,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,28,4,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,29,7,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,1,4,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,2,7,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,3,4,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,4,4,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,5,7,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,6,4,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,7,4,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,8,4,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,9,7,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,10,7,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,11,7,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,12,4,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,13,5,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,15,7,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,16,4,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,17,4,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,18,4,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,19,4,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,20,4,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,21,4,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,22,5,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,23,4,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,24,7,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,26,4,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,27,4,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,28,4,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,29,7,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,1,0,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,9,0,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,12,0,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,23,0,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,1,0,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,9,0,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,12,0,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,23,0,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,1,4,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,3,2,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,5,2,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,6,2,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,8,3,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,9,2,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,12,2,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,14,6,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,18,2,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,23,2,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,26,2,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,27,2,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,29,2,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,1,4,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,2,7,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,3,7,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,4,7,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,5,7,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,6,7,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,7,7,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,8,7,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,9,7,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,10,7,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,11,7,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,12,7,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,13,5,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,14,6,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,15,7,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,16,4,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,17,4,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,18,7,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,19,4,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,20,4,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,21,4,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,22,5,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,23,7,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,24,7,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,26,7,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,27,7,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,28,4,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,29,7,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,30,7,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,1,0,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,9,0,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,12,0,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,23,0,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,1,4,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,9,0,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,12,0,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,23,0,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,31,0,0)	--SendSMS
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_roleperm_getPermissionsBySystemIdAndUserId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_roleperm_getPermissionsBySystemIdAndUserId]
(
	@systemId INT, 
	@userId INT
)
AS
/*
 * Get the mailing lists where the user is a member directly or indirectly.
 */
CREATE TABLE #tmp
(
	mailListId INT
)

/*
 * Get the mailing lists where the user is a direct member.
 */
INSERT INTO #tmp(mailListId)
select 
	mail_list_id 
from 
	mail_list_users 
where 
	user_id = @userId

SELECT * INTO #tmp1 FROM #tmp
WHILE (EXISTS(SELECT * FROM #tmp1))
BEGIN
	/*
	 * Get the mailing lists where the id in #tmp1 is a member.
         * which is already not part of #tmp
	 */
	SELECT 
		mlu.mail_list_id
	INTO #tmp2
	FROM
		mail_list_users mlu
		JOIN #tmp1 t1
		ON mlu.user_id = t1.mailListId
		LEFT JOIN #tmp t
		ON mlu.mail_list_id = t.mailListId
	WHERE
		t.mailListId IS NULL

	INSERT INTO #tmp1 SELECT * FROM #tmp2
	INSERT INTO #tmp SELECT * FROM #tmp2
	DROP TABLE #tmp2
	DELETE #tmp1
END
DROP TABLE #tmp1

DECLARE @privateFieldId INT
SELECT @privateFieldId = ISNULL(field_id, 0) FROM fields WHERE sys_id = @systemId AND name = ''is_private''

SELECT
	t.name ''name'',
	t.field_id ''field_id'',
	CASE SUM(p.padd)
	WHEN 0 then 0
	ELSE 1
	END + 
	CASE SUM(p.pchange)
	WHEN 0 then 0
	ELSE 2
	END + 
	CASE SUM(p.pview)
	WHEN 0 then 0
	ELSE 4
	END ''permission''
FROM
	permissions p
	JOIN
	(
	/*
	 * Get the permissions the user gets by virtue of being a user of the system.
	 */
	SELECT
		f.name,
		f.field_id,
		rp.gpermissions
	FROM
		roles_permissions rp
		JOIN fields f
		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id
	WHERE
		rp.sys_id = @systemId AND 
		rp.role_id = 1
	
	UNION
	
	/*
	 * Get the permissions the user gets by virtue of being a part of the BA.
	 */
	SELECT
		f.name,
		f.field_id,
		rp.gpermissions
	FROM
		roles_permissions rp
		JOIN fields f
		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id
		JOIN roles_users ru
		ON ru.sys_id = rp.sys_id AND ru.role_id = rp.role_id
	WHERE
		rp.sys_id = @systemId AND
		(
			ru.user_id = @userId OR
			ru.user_id IN (SELECT mailListId FROM #tmp)
		)
	) t
	ON p.permission = t.gpermissions 
GROUP BY t.name, t.field_id

UNION

/*
 * Get the list of application specific roles the user is present in.
 */
SELECT
	CASE rolename
	WHEN ''Analyst'' THEN ''__ROLE_ANALYST__''
	WHEN ''Admin'' THEN ''__ADMIN__''
	WHEN ''PermissionAdmin'' THEN ''__PERMISSIONADMIN__''
	ELSE rolename
	END,
	-1, 
	10
FROM
	roles_users ru
	JOIN roles r
	ON r.sys_id = ru.sys_id AND r.role_id = ru.role_id AND ru.is_active = 1
WHERE
	ru.sys_id = @systemId AND	
	(
		ru.user_id = @userId OR
		ru.user_id IN (SELECT mailListId FROM #tmp)
	)
	AND
	r.rolename in (''Analyst'', ''Admin'', ''PermissionAdmin'')

UNION

/*
 * Check if the user is a part of super user list.
 */
SELECT
	''__SUPER_USER__'', -1, 7
FROM
	super_users
WHERE
	user_id = @userId aND
	is_active = 1

UNION

/*
 * Check the contextual roles in this BA that have permission to view private requests.
 */
SELECT
	CASE rolename
	WHEN ''Logger'' THEN ''__LOGGER_PRIVATE__''
	WHEN ''Assignee'' THEN ''__ASSIGNEE_PRIVATE__''
	WHEN ''Subscriber'' THEN ''__SUBSCRIBER_PRIVATE__''
	ELSE rolename
	END,
	-1, 
	gpermissions
FROM
	roles_permissions rp
	JOIN roles r
	ON rp.sys_id = r.sys_id AND rp.role_id = r.role_id
WHERE
	rp.sys_id = @systemId AND
	rp.field_id = @privateFieldId AND
	r.rolename IN (''Logger'', ''Assignee'', ''Subscriber'')

/*
 * Finally drop the temp table used for holding the mailing lists this user is part of directly or indirectly.
 */
DROP TABLE #tmp

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER OFF
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_roleperm_getPermissionsByUserId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'


CREATE procedure [dbo].[stp_roleperm_getPermissionsByUserId] 
(
	@userId 	INT,
	@prefixList 	VARCHAR(7999)
)
AS
DECLARE @privateFieldId INT
SELECT @privateFieldId = ISNULL(field_id, 0) FROM fields WHERE name = ''is_private''
DECLARE @query VARCHAR(7999)
SELECT @query = 
''
CREATE TABLE #tmp
(
	mailListId INT
)

/*
 * Get the mailing lists where the user is a direct member.
 */
INSERT INTO #tmp(mailListId)
select 
	mail_list_id 
from 
	mail_list_users 
where 
	user_id = '' + CONVERT(VARCHAR, @userId) + ''

SELECT * INTO #tmp1 FROM #tmp
WHILE (EXISTS(SELECT * FROM #tmp1))
BEGIN
	/*
	 * Get the mailing lists where the id in #tmp1 is a member.
         * which is already not part of #tmp
	 */
	SELECT 
		mlu.mail_list_id
	INTO #tmp2
	FROM
		mail_list_users mlu
		JOIN #tmp1 t1
		ON mlu.user_id = t1.mailListId
		LEFT JOIN #tmp t
		ON mlu.mail_list_id = t.mailListId
	WHERE
		t.mailListId IS NULL

	INSERT INTO #tmp1 SELECT * FROM #tmp2
	INSERT INTO #tmp SELECT * FROM #tmp2
	DROP TABLE #tmp2
	DELETE #tmp1
END
DROP TABLE #tmp1

SELECT 
	ba.sys_prefix ''''SysPrefix'''',
	f.sys_id ''''SystemId'''',
	f.field_id ''''FieldId'''',
	f.name ''''FieldName'''',
	case sum(CONVERT(INT, padd))
		when 0 then 0
		else 1
	end
	+
	case sum(CONVERT(INT, pchange))
		when 0 then 0
		else 2
	end
	+
	case sum(CONVERT(INT, pview))
		when 0 then 0
		else 4
	end ''''Permission''''
FROM 
	fields f
	JOIN business_areas ba
	ON f.sys_id = ba.sys_id
	LEFT JOIN roles_permissions rp 
	ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id AND f.is_active = 1 
	LEFT JOIN roles_users ru
	ON ru.sys_id = rp.sys_id AND ru.role_id = rp.role_id AND (ru.user_id = '' + convert(varchar, @userId) + '' OR ru.user_id in (select mailListID from #tmp)) AND ru.is_active = 1
	JOIN permissions p
	ON rp.gpermissions = p.permission
WHERE 
	ba.sys_prefix in ('''''' + @prefixList + '''''') AND 
	f.is_extended = 0 AND
	f.is_active = 1 AND
	(
		rp.role_id IN (1) OR
		rp.role_id = ru.role_id
	)
group by ba.sys_prefix, f.sys_id, f.name, f.field_id
UNION
SELECT
	ba.sys_prefix ''''SysPrefix'''',
	rp.sys_id ''''SystemId'''',
	-1 ''''FieldId'''', 
	''''__LOGGER_PRIVATE__'''' ''''FieldName'''', 
	case (gpermissions & 4) 
	  when 0 then 0
	  else 4
	end ''''Permission''''
FROM
	roles_permissions rp
	JOIN business_areas ba
	ON rp.sys_id = ba.sys_id
WHERE
	ba.sys_prefix in ('''''' + @prefixList + '''''') AND 
	role_id = 2 AND -- LOGGER ROLE
	field_id = '' + CONVERT( VARCHAR, @privateFieldId) + ''
UNION
SELECT
	ba.sys_prefix ''''SysPrefix'''',
	rp.sys_id ''''SystemId'''',
	-1 ''''FieldId'''', 
	''''__ASSIGNEE_PRIVATE__'''' ''''FieldName'''', 
	case (gpermissions & 4) 
	  when 0 then 0
	  else 4
	end ''''Permission''''
FROM
	roles_permissions rp
	JOIN business_areas ba
	ON rp.sys_id = ba.sys_id
WHERE
	ba.sys_prefix in ('''''' + @prefixList + '''''') AND 
	role_id = 3 AND -- ASSIGNEE ROLE
	field_id = '' + CONVERT( VARCHAR, @privateFieldId) + ''
UNION
SELECT
	ba.sys_prefix ''''SysPrefix'''',
	rp.sys_id ''''SystemId'''',
	-1 ''''FieldId'''', 
	''''__SUBSCRIBER_PRIVATE__'''' ''''FieldName'''', 
	case (gpermissions & 4) 
	  when 0 then 0
	  else 4
	end ''''Permission''''
FROM
	roles_permissions rp
	JOIN business_areas ba
	ON rp.sys_id = ba.sys_id
WHERE
	ba.sys_prefix in ('''''' + @prefixList + '''''') AND 
	role_id = 4 AND -- SUBSCRIBER ROLE
	field_id = '' + CONVERT( VARCHAR, @privateFieldId) + '' 
ORDER by  SystemId, FieldId, FieldName, Permission

DROP TABLE #tmp
''
print @query
EXEC (@query)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_roleperm_getPermissionsBySystemIdAndUserIdForAddRequest]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_roleperm_getPermissionsBySystemIdAndUserIdForAddRequest]
(
	@systemId INT, 
	@userId INT
)
AS
/*
 * Get the mailing lists where the user is a member directly or indirectly.
 */
CREATE TABLE #tmp
(
	mailListId INT
)

/*
 * Get the mailing lists where the user is a direct member.
 */
INSERT INTO #tmp(mailListId)
select 
	mail_list_id 
from 
	mail_list_users 
where 
	user_id = @userId

SELECT * INTO #tmp1 FROM #tmp
WHILE (EXISTS(SELECT * FROM #tmp1))
BEGIN
	/*
	 * Get the mailing lists where the id in #tmp1 is a member.
         * which is already not part of #tmp
	 */
	SELECT 
		mlu.mail_list_id
	INTO #tmp2
	FROM
		mail_list_users mlu
		JOIN #tmp1 t1
		ON mlu.user_id = t1.mailListId
		LEFT JOIN #tmp t
		ON mlu.mail_list_id = t.mailListId
	WHERE
		t.mailListId IS NULL

	INSERT INTO #tmp1 SELECT * FROM #tmp2
	INSERT INTO #tmp SELECT * FROM #tmp2
	DROP TABLE #tmp2
	DELETE #tmp1
END
DROP TABLE #tmp1

DECLARE @privateFieldId INT
SELECT @privateFieldId = ISNULL(field_id, 0) FROM fields WHERE sys_id = @systemId AND name = ''is_private''

SELECT
	t.name ''name'',
	t.field_id ''field_id'',
	CASE SUM(p.padd)
	WHEN 0 then 0
	ELSE 1
	END + 
	CASE SUM(p.pchange)
	WHEN 0 then 0
	ELSE 2
	END + 
	CASE SUM(p.pview)
	WHEN 0 then 0
	ELSE 4
	END ''permission''
FROM
	permissions p
	JOIN
	(
	/*
	 * Get the permissions the user gets by virtue of being a user of the system and 
	 * being a logger of the request.
	 */
	SELECT
		f.name,
		f.field_id,
		rp.gpermissions
	FROM
		roles_permissions rp
		JOIN fields f
		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id
	WHERE
		rp.sys_id = @systemId AND 
		(
			rp.role_id = 1 OR -- User Role.
			rp.role_id = 2  -- Logger Role.
		)
	UNION
	
	/*
	 * Get the permissions the user gets by virtue of being a part of the BA.
	 */
	SELECT
		f.name,
		f.field_id,
		rp.gpermissions
	FROM
		roles_permissions rp
		JOIN fields f
		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id
		JOIN roles_users ru
		ON ru.sys_id = rp.sys_id AND ru.role_id = rp.role_id
	WHERE
		rp.sys_id = @systemId AND
		(
			ru.user_id = @userId OR
			ru.user_id IN (SELECT mailListId FROM #tmp)
		)
	) t
	ON p.permission = t.gpermissions 
GROUP BY t.name, t.field_id

UNION

/*
 * Get the list of application specific roles the user is present in.
 */
SELECT
	CASE rolename
	WHEN ''Analyst'' THEN ''__ROLE_ANALYST__''
	WHEN ''Admin'' THEN ''__ADMIN__''
	WHEN ''PermissionAdmin'' THEN ''__PERMISSIONADMIN__''
	ELSE rolename
	END,
	-1, 
	-1
FROM
	roles_users ru
	JOIN roles r
	ON r.sys_id = ru.sys_id AND r.role_id = ru.role_id AND ru.is_active = 1
WHERE
	ru.sys_id = @systemId AND	
	(
		ru.user_id = @userId OR
		ru.user_id IN (SELECT mailListId FROM #tmp)
	)
	AND
	r.rolename in (''Analyst'', ''Admin'', ''PermissionAdmin'')

UNION

/*
 * Check if the user is a part of super user list.
 */
SELECT
	''__SUPER_USER__'', -1, -1
FROM
	super_users
WHERE
	user_id = @userId aND
	is_active = 1

UNION

/*
 * Check the contextual roles in this BA that have permission to view private requests.
 */
SELECT
	CASE rolename
	WHEN ''Logger'' THEN ''__LOGGER_PRIVATE__''
	WHEN ''Assignee'' THEN ''__ASSIGNEE_PRIVATE__''
	WHEN ''Subscriber'' THEN ''__SUBSCRIBER_PRIVATE__''
	ELSE rolename
	END,
	-1, 
	gpermissions
FROM
	roles_permissions rp
	JOIN roles r
	ON rp.sys_id = r.sys_id AND rp.role_id = r.role_id
WHERE
	rp.sys_id = @systemId AND
	rp.field_id = @privateFieldId AND
	r.rolename IN (''Logger'', ''Assignee'', ''Subscriber'')

/*
 * Finally drop the temp table used for holding the mailing lists this user is part of directly or indirectly.
 */
DROP TABLE #tmp

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_role_permission_defaultPermissions]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_role_permission_defaultPermissions]
(
	@sys_id 	INT,
	@role_name 	VARCHAR(40)
)
AS
DECLARE @max_field_id INT
DECLARE @role_id INT

SELECT 
	@max_field_id = MAX(field_id) 
FROM 
	fields 
WHERE 
	sys_id = @sys_id


SELECT 
	@role_id = role_id 
FROM 
	roles 
WHERE 
	rolename = @role_name and 
	sys_id = @sys_id

IF(@role_id = 1)
BEGIN
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 1 and role_id = 1 
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 2 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 3 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 4 and role_id = 1
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 5 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 6 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 7 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 8 and role_id = 1
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 9 and role_id = 1
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 10 and role_id = 1
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 11 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 12 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 13 and role_id = 1
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 14 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 15 and role_id = 1
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 16 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 17 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 18 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 19 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 20 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 21 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 22 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 23 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 24 and role_id = 1
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 26 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 27 and role_id = 1
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 28 and role_id = 1
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 29 and role_id = 1
END
IF(@role_id = 2)
begin
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 1 and role_id = 2
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 2 and role_id = 2
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 3 and role_id = 2
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 4 and role_id = 2
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 5 and role_id = 2
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 6 and role_id = 2
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 7 and role_id = 2
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 8 and role_id = 2
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 9 and role_id = 2
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 10 and role_id = 2
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 11 and role_id = 2
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 12 and role_id = 2
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 13 and role_id = 2
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 14 and role_id = 2
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 15 and role_id = 2
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 16 and role_id = 2
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 17 and role_id = 2
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 18 and role_id = 2
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 19 and role_id = 2
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 20 and role_id = 2
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 21 and role_id = 2
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 22 and role_id = 2
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 23 and role_id = 2
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 24 and role_id = 2
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 2
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 26 and role_id = 2
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 27 and role_id = 2
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 28 and role_id = 2
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 29 and role_id = 2
END
IF(@role_id = 3)
begin
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 1 and role_id = 3
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 2 and role_id = 3
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 3 and role_id = 3
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 4 and role_id = 3
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 5 and role_id = 3
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 6 and role_id = 3
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 7 and role_id = 3
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 8 and role_id = 3
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 9 and role_id = 3
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 10 and role_id = 3
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 11 and role_id = 3
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 12 and role_id = 3
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 13 and role_id = 3
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 14 and role_id = 3
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 15 and role_id = 3
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 16 and role_id = 3
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 17 and role_id = 3
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 18 and role_id = 3
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 19 and role_id = 3
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 20 and role_id = 3
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 21 and role_id = 3
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 22 and role_id = 3
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 23 and role_id = 3
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 24 and role_id = 3
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 3
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 26 and role_id = 3
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 27 and role_id = 3
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 28 and role_id = 3
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 29 and role_id = 3
END
IF(@role_id = 4)
begin
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 1 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 2 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 3 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 4 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 5 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 6 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 7 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 8 and role_id = 4
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 9 and role_id = 4
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 10 and role_id = 4
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 11 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 12 and role_id = 4
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 13 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 14 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 15 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 16 and role_id = 4
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 17 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 18 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 19 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 20 and role_id = 4
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 21 and role_id = 4
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 22 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 23 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 24 and role_id = 4
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 26 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 27 and role_id = 4
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 28 and role_id = 4
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 29 and role_id = 4
END
IF(@role_id = 5)
begin
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 1 and role_id = 5
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 2 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 3 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 4 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 5 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 6 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 7 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 8 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 9 and role_id = 5
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 10 and role_id = 5
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 11 and role_id = 5
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 12 and role_id = 5
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 13 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 14 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 15 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 16 and role_id = 5
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 17 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 18 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 19 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 20 and role_id = 5
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 21 and role_id = 5
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 22 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 23 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 24 and role_id = 5
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 26 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 27 and role_id = 5
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 28 and role_id = 5
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 29 and role_id = 5
END
IF(@role_id = 6)
begin
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 1 and role_id = 6
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 2 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 3 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 4 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 5 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 6 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 7 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 8 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 9 and role_id = 6
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 10 and role_id = 6
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 11 and role_id = 6
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 12 and role_id = 6
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 13 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 14 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 15 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 16 and role_id = 6
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 17 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 18 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 19 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 20 and role_id = 6
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 21 and role_id = 6
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 22 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 23 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 24 and role_id = 6
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 26 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 27 and role_id = 6
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 28 and role_id = 6
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 29 and role_id = 6
END
IF(@role_id = 7)
begin
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 1 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 2 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 3 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 4 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 5 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 6 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 7 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 8 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 9 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 10 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 11 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 12 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 13 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 14 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 15 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 16 and role_id = 7
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 17 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 18 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 19 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 20 and role_id = 7
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 21 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 22 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 23 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 24 and role_id = 7
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 26 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 27 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 28 and role_id = 7
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 29 and role_id = 7
END
IF(@role_id = 8)
begin
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 1 and role_id = 8
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 2 and role_id = 8
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 3 and role_id = 8
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 4 and role_id = 8
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 5 and role_id = 8
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 6 and role_id = 8
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 7 and role_id = 8
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 8 and role_id = 8
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 9 and role_id = 8
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 10 and role_id = 8
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 11 and role_id = 8
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 12 and role_id = 8
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 13 and role_id = 8
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 14 and role_id = 8
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 15 and role_id = 8
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 16 and role_id = 8
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 17 and role_id = 8
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 18 and role_id = 8
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 19 and role_id = 8
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 20 and role_id = 8
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 21 and role_id = 8
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 22 and role_id = 8
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 23 and role_id = 8
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 24 and role_id = 8
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 8
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 26 and role_id = 8
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 27 and role_id = 8
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 28 and role_id = 8
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 29 and role_id = 8
END
IF(@role_id = 9)
begin
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 1 and role_id = 9
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 2 and role_id = 9
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 3 and role_id = 9
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 4 and role_id = 9
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 5 and role_id = 9
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 6 and role_id = 9
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 7 and role_id = 9
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 8 and role_id = 9
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 9 and role_id = 9
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 10 and role_id = 9
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 11 and role_id = 9
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 12 and role_id = 9
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 13 and role_id = 9
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 14 and role_id = 9
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 15 and role_id = 9
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 16 and role_id = 9
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 17 and role_id = 9
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 18 and role_id = 9
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 19 and role_id = 9
UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 20 and role_id = 9
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 21 and role_id = 9
UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 22 and role_id = 9
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 23 and role_id = 9
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 24 and role_id = 9
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 9
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 26 and role_id = 9
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 27 and role_id = 9
UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 28 and role_id = 9
UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 29 and role_id = 9
END
IF(@max_field_id > 29)
BEGIN
declare @i int
SELECT @i = 30
   while(@i <= @max_field_id)
     BEGIN
       IF(@role_id = 1)
       begin
       UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 1
       END
       IF(@role_id = 2)
       begin
       UPDATE roles_permissions SET gpermissions = 5 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 2
       END
       IF(@role_id = 3)
       begin
       UPDATE roles_permissions SET gpermissions = 6 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 3
       END
       IF(@role_id = 4)
       begin
       UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 4
       END
       IF(@role_id = 5)
       begin
       UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 5
       END
       IF(@role_id = 6)
       begin
       UPDATE roles_permissions SET gpermissions = 4 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 6
       END
       IF(@role_id = 7)
       begin
       UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 7
       END
       IF(@role_id = 8)
       begin
       UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 8
       END
       IF(@role_id = 9)
       begin
       UPDATE roles_permissions SET gpermissions = 7 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 9
       END
       SELECT @i = @i + 1
     END
END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_display_group_insert_sys_id_column]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
create procedure [dbo].[stp_display_group_insert_sys_id_column]
AS
Declare @sys_id int
Declare @display_group int
Declare @NewDisplay_group int

update fields set display_group = 1 where display_name = ''SMS Id''
update fields set display_group = 1 where display_name = ''send SMS''

update fields set display_order = (select max(display_order) from fields where display_group = 1) where display_name = ''SMS Id''
update fields set display_order = (select max(display_order) from fields where display_group = 1) where display_name = ''Send SMS''


----change in the display_group add the sys_id and 

alter table display_groups add  sys_id int

---
begin

--------------------------------

select id,display_name,display_order,is_active  
into #tmp 
from display_groups 
----------------------------------
select distinct f.sys_id,display_group ,dg.display_name
into #tmp1
from fields f 
join display_groups dg on dg.id = f.display_group
where is_extended = 1 and display_group not in ( 0,1)
----------------------------------

delete from display_groups where id in (select display_group from #tmp1)

insert into display_groups (display_name,display_order,is_active,sys_id )
( select t.display_name,t.display_order,t.is_active,t1.sys_id 
from #tmp t 
join #tmp1 t1 on t.id = t1.display_group )
----------------------------------
--- now update fields with new display_id
select dg.id,dg.sys_id,t1.display_group
into #tmp2
from display_groups dg 
join #tmp1 t1 on dg.sys_id = t1.sys_id and dg.display_name = t1.display_name
------------------------------------
while(exists(select * from #tmp2))
begin
select 
   @NewDisplay_group = id,
   @sys_id = sys_id,
   @display_group = display_group
from #tmp2
--update field
update fields 
 set display_group = @NewDisplay_group
 where sys_id = @sys_id 
 and display_group = @display_group
--delete from #tmp
delete from #tmp2
 where sys_id = @sys_id
 and id = @NewDisplay_group
 and display_group = @display_group

select * from #tmp2
END

-------------------
drop table #tmp
drop table #tmp1
drop table #tmp2
END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_type_lookupBySystemIdAndFieldName]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_type_lookupBySystemIdAndFieldName]
(
	@systemId  INT,
	@fieldName VARCHAR(512)
)
AS
DECLARE @fieldId INT
SELECT 
	@fieldId = ISNULL(field_id, 0) 
FROM 
	fields
WHERE
	sys_id = @systemId AND
	name = @fieldName
SELECT 
	* 
FROM 
	types
WHERE
	sys_id = @systemId AND
	field_id = @fieldId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_type_lookupBySystemIdAndFieldNameAndTypeName]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_type_lookupBySystemIdAndFieldNameAndTypeName]
(
	@systemId  INT,
	@fieldName VARCHAR(256),
	@typeName  VARCHAR(256)
)
AS
DECLARE @fieldId INT
SELECT 
	@fieldId = ISNULL(field_id, 0) 
FROM 
	fields
WHERE
	sys_id = @systemId AND
	(
		name = @fieldName OR display_name = @fieldName
	)
SELECT 
	* 
FROM 
	types
WHERE
	sys_id = @systemId AND
	field_id = @fieldId AND
	(
		name = @typeName OR display_name = @typeName
	)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_type_getDefaultTypeBySystemIdAndFieldName]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_type_getDefaultTypeBySystemIdAndFieldName]
(
	@systemId	INT,
	@fieldName	VARCHAR(256)
)
AS
DECLARE @fieldId INT
SELECT @fieldId = field_id FROM fields where sys_id = @systemId AND name = @fieldName
SELECT 
	*
FROM 
	types 
WHERE
	sys_id = @systemId AND 
	field_id = @fieldId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_type_lookupBySystemIdAndFieldNameAndTypeId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_type_lookupBySystemIdAndFieldNameAndTypeId]
(
	@systemId  INT,
	@fieldName VARCHAR(512),
	@typeId    INT
)
AS
DECLARE @fieldId INT
SELECT 
	@fieldId = ISNULL(field_id, 0) 
FROM 
	fields
WHERE
	sys_id = @systemId AND
	name = @fieldName
SELECT 
	* 
FROM 
	types
WHERE
	sys_id = @systemId AND
	field_id = @fieldId AND
	type_id = @typeId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_admin_getRolePermissionsBysysIdAndRoleId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_admin_getRolePermissionsBysysIdAndRoleId] 
(
	@sys_id INT,
	@role_id INT 
)

AS

SELECT DISTINCT
	f.name,
	ISNULL (rp.sys_id,  	@sys_id) as ''sys_id'',
	ISNULL (rp.role_id, 	@role_id) as ''role_id'',
	ISNULL (rp.field_id, 	f.field_id) as ''field_id'',
	ISNULL (rp.gpermissions, 0) as ''permission'', 
	ISNULL (rp.dpermissions, 0) as ''dpermission''
FROM 
	fields f
	LEFT JOIN roles_permissions rp
	ON f.sys_id = rp.sys_id AND rp.role_id = @role_id AND f.field_id = rp.field_id
where
	f.sys_id = @sys_id

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_admin_getUserCategoriesForBA]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_admin_getUserCategoriesForBA] 
(
	@sys_id 	INT,
	@user_id 	INT
)
AS

DECLARE @field_id INT

SELECT 
	@field_id = field_id 
FROM 
	fields 
WHERE 
	name=''category_id''

SELECT 
	*
FROM 
	type_users
WHERE 
	sys_id = @sys_id AND
	field_id = @field_id AND
	user_id = @user_id
ORDER BY type_id

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ba_caption_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_ba_caption_insert]
(
	@sys_id			INT,
	@name			VARCHAR(150),
	@value			TEXT
)
AS
INSERT INTO CAPTIONS_PROPERTIES 
(
	sys_id,
	name,
	value
) 
VALUES
(
	@sys_id ,
	@name,
	@value
)' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_hl_getHolidays]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_hl_getHolidays]
AS
SELECT 
	office,
	CONVERT(DATETIME, holiday_date) ''holiday_date'',
	office_zone,
	description
FROM
	holidays_list

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_mlu_getAllMailListUsers]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_mlu_getAllMailListUsers]
AS

SELECT 
	* 
FROM 
	mail_list_users

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_mlu_getMailingLists]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_mlu_getMailingLists]
AS
SELECT 
	mail_list_id,
	user_id
FROM 
	mail_list_users 
ORDER BY mail_list_id, user_id

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_nr_lookupByNotificationRuleId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_nr_lookupByNotificationRuleId]
(
	@ruleId INT
)
AS
SELECT 
	* 
FROM 
	notification_rules
WHERE
	notification_id = @ruleId

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_nr_getAllNotificationRules]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_nr_getAllNotificationRules]
AS
SELECT 
	* 
FROM 
	notification_rules

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tbits_changeReadStatus]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_tbits_changeReadStatus]
(
	@systemId	int,
	@requestList	varchar(7999),
	@action		int,
	@userId		int,
	@rejectedList	varchar(7999) OUTPUT
)
AS
DECLARE @index		int
DECLARE @requestId	int
DECLARE @actionId 	int

DECLARE @GA_READ INT
DECLARE @GA_UNREAD INT

SELECT @GA_READ = 4
SELECT @GA_UNREAD = 5

SELECT @rejectedList = ''''

WHILE (@requestList <> '''')
BEGIN
	SELECT @index = charindex('','', @requestList)
	IF (@index > 0) 
	BEGIN
		SELECT @requestId = CONVERT ( INT, substring(@requestList, 0, @index) )
		SELECT @requestList = substring ( @requestList, @index + 1, len(@requestList))
	END
	ELSE 
	BEGIN
		SELECT @requestId = CONVERT ( INT, @requestList )	
		SELECT @requestList = ''''
	END
	IF (@action = @GA_READ)
	BEGIN
		-- Get the Max ActionId
		SELECT 
			@actionId = max_action_id 
		FROM 
			requests 
		WHERE 
			sys_id = @systemId AND 
			request_id = @requestId

		IF EXISTS (
				SELECT 
					sys_id, 
					request_id, 
					user_id 
				FROM 
					user_read_actions 
				WHERE
					sys_id = @systemId AND
					request_id = @requestId AND
					user_id = @userId
			)
		BEGIN
			/*
			 * Update the record with max_action_id for this request, if it is already existing.
			 */
			UPDATE user_read_actions
			SET
				action_id = @actionId
			WHERE
				sys_id 		= @systemId AND
				request_id 	= @requestId AND
				user_id 	= @userId
		END
		ELSE
		BEGIN
			/*
			 * Insert a new record.
			 */
			INSERT INTO user_read_actions
			(
				sys_id,
				request_id,
				action_id,
				user_id
			)
			VALUES
			(
				@systemId,
				@requestId,
				@actionId,
				@userId
			)
		END
	END
	ELSE IF (@action = @GA_UNREAD)
	BEGIN
		/*
		 * User wants to mark this request as unread, delete the record.
		 */
	
		DELETE user_read_actions
		WHERE
			sys_id 		= @systemId AND
			request_id 	= @requestId AND
			user_id 	= @userId
			
	END
END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_request_updateAttachments]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
CREATE procedure [dbo].[stp_request_updateAttachments]
(
	@systemId	INT,
	@requestId	INT,
	@actionId	INT,
	@attachments	TEXT
)
AS
UPDATE requests
SET
	attachments = @attachments
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId AND
	max_action_id   = @actionId
UPDATE actions
SET
	attachments = @attachments
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId AND
	action_id	= @actionId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_request_update]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_request_update] 
( 
        @systemId       INT, 
        @requestId      BIGINT, 
        @categoryId     INT, 
        @statusId       INT, 
        @severityId     INT, 
        @requestTypeId  INT, 
        @subject        NVARCHAR(4000), 
        @description    NTEXT, 
        @IsPrivate      BIT, 
        @parentId       BIGINT, 
        @userId         INT, 
        @maxActionId    INT OUTPUT, 
        @dueDate        DATETIME, 
        @loggedDate     DATETIME, 
        @updatedDate    DATETIME, 
        @headerDesc     NTEXT, 
        @attachments    NTEXT, 
        @summary        NTEXT, 
        @memo           NTEXT, 
        @append         INT, 
        @notify         INT, 
        @notifyLoggers  BIT, 
        @repliedToAction INT, 
        @officeId             INT 
) 
AS 
--- Read the max action id from requests and add one to it. 
SELECT 
        @maxActionId = ISNULL(max_action_id, 0) + 1 
FROM 
        requests 
WHERE 
        sys_id          = @systemId AND 
        request_id      = @requestId 
--- We cannot hold this value of max_action_id with us till the end of this transaction 
--- as other processes might be interested in inserting actions in this request in the meantime. 
--- So, update the requests table with this new max_action_id value. 
UPDATE requests 
SET 
        max_action_id = @maxActionId 
WHERE 
        sys_id          = @systemId AND 
        request_id      = @requestId 
--- Now update the request. 
UPDATE requests 
SET 
        sys_id                  = @systemId, 
        request_id              = @requestId, 
        category_id             = @categoryId, 
        status_id               = @statusId, 
        severity_id             = @severityId, 
        request_type_id         = @requestTypeId, 
        subject                 = @subject, 
        description             = @description, 
        is_private              = @isPrivate, 
        parent_request_id       = @parentId, 
        user_id                 = @userId, 
        max_action_id           = @maxActionId, 
        due_datetime            = @dueDate, 
        logged_datetime         = @loggedDate, 
        lastupdated_datetime    = @updatedDate, 
        header_description      = @headerDesc, 
        attachments             = @attachments, 
        memo                    = @memo, 
        append_interface        = @append, 
        notify                  = @notify, 
        notify_loggers          = @notifyLoggers, 
        replied_to_action             =  @repliedToAction, 
        office_id                          =  @officeId 
WHERE 
        sys_id          = @systemId AND 
        request_id      = @requestId 
-- If summary is NOT NULL, then it should be updated in the request. 
-- Otherwise retain the old subject. 
IF (@summary IS NOT NULL) 
BEGIN 
        UPDATE requests 
        SET summary = @summary 
        WHERE 
                sys_id          = @systemId AND 
                request_id      = @requestId 
END 
---- Insert the corresponding record into actions table. 
INSERT INTO actions 
( 
        sys_id,                 request_id,             action_id, 
        category_id,            status_id,              severity_id, 
        request_type_id,        subject,                description, 
        is_private,             parent_request_id,      user_id, 
        due_datetime,           logged_datetime,        lastupdated_datetime, 
        header_description,     attachments,            summary, 
        memo,                   append_interface,       notify, 
        notify_loggers,         replied_to_action,      office_id 
) 
VALUES 
( 
        @systemId,              @requestId,             @maxActionId, 
        @categoryId,            @statusId,              @severityId, 
        @requestTypeId,         @subject,               @description, 
        @IsPrivate,             @parentId,              @userId, 
        @dueDate,               @loggedDate,            @updatedDate, 
        @headerDesc,            @attachments,           @summary, 
        @memo,                  @append,                @notify, 
        @notifyLoggers,         @repliedToAction,       @officeId 
)

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_request_updateHeaderDesc]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE procedure [dbo].[stp_request_updateHeaderDesc]
(
	@systemId	INT,
	@requestId	INT,
	@actionId	INT,
	@headerDesc	TEXT
)
AS
UPDATE requests
SET
	header_description = @headerDesc	
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId AND
	max_action_id   =  @actionId
UPDATE actions
SET
	header_description = @headerDesc	
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId AND
	action_id	= @actionId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_action_updateWithTransferInfo]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'



CREATE PROCEDURE [dbo].[stp_action_updateWithTransferInfo]
(
	@systemId	INT,
	@requestId	INT,
	@targetRequest	VARCHAR(100)
)

AS

DECLARE @actionId INT
DECLARE @startIndex INT
DECLARE @endIndex INT
DECLARE @description VARCHAR(4096)
DECLARE @headerDescription VARCHAR(7999)

SELECT @description = ''[ Request transferred to '' + @targetRequest + '' ]''

-- Get the max_request_id
SELECT 
	@actionId = max_action_id 
FROM 
	requests 
WHERE 
	sys_id = @systemId AND 
	request_id = @requestId

select @headerDescription = ''''
select @headerDescription = convert( varchar(7999), header_description) from actions 
WHERE
	sys_id = @systemId AND 
	request_id = @requestId AND
	action_id = @actionId

select @startIndex = CHARINDEX(''[ Transfer'',@headerDescription)
select @endIndex = CHARINDEX(''# pending... ]'',@headerDescription)

if ((@startIndex >= 0) and (@endIndex  > 0) and (@endIndex > @startIndex))
begin
	select @headerDescription =  replace(@headerDescription, substring(@headerDescription, @startIndex, @endIndex + 14), @description) 
end
else
begin
	select @headerDescription =  @headerDescription + @description
end

-- Update the requests table.
UPDATE requests
SET
	header_description = @headerDescription
WHERE
	sys_id = @systemId AND 
	request_id = @requestId
	
-- Update the actions table.
UPDATE actions
SET
	header_description = @headerDescription
WHERE
	sys_id = @systemId AND 
	request_id = @requestId AND
	action_id = @actionId
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_report_getUnclosedRequestsByField]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_report_getUnclosedRequestsByField]
(
	@systemId 	INT,
	@start 		datetime,
	@end 		datetime
)
AS

--IF(
--	@systemId != 106 AND 	--GBO
--	@systemId != 109 AND 	--FinopHyd
--	@systemId != 115 AND	--CashTrack
--	@systemId != 201 AND	--tBits
--	@systemId != 228 AND	--GBOTact
--	@systemId != 105		--GenDev
--  )
--BEGIN
--	SELECT @systemId = -1
--END

DECLARE @closedStatusId INT

SELECT 
	@closedStatusId = type_id 
FROM 
	types 
WHERE 
	sys_id = @systemId AND 
	field_id = 4 AND 
	name like ''close%''

SELECT 
      sys_id,request_id,max(action_id) "action_id" 
INTO  
      #tmp1
FROM 
       actions 

WHERE 
       sys_id = @systemId AND lastupdated_datetime < @start AND 
       request_id not in (select request_id from requests where sys_id = @systemId AND status_id = @closedStatusId)

GROUP BY sys_id, request_id

SELECT 
      sys_id,request_id,max(action_id) "action_id" 
INTO  
      #tmp2
FROM 
       actions 

WHERE 
       sys_id = @systemId AND lastupdated_datetime < @end AND 
       request_id not in (select request_id from requests where sys_id = @systemId AND status_id = @closedStatusId)

GROUP BY sys_id, request_id

	SELECT 
		isNull ( table2.Category, table1.Category ) "Type",
		isNull ( StartCount, 0 ) "Start", 
		isNull ( EndCount, 0 ) "End", 
		( isNull ( EndCount, 0 ) - isNull ( StartCount, 0 ) ) "Difference"
	FROM 
		(
		SELECT
			t.display_name "Category", count(*) "StartCount"
		FROM
			(
			SELECT 
				a.sys_id, a.request_id, a.action_id, a.category_id
			FROM 
				actions a
				JOIN #tmp1
				ON a.sys_id = #tmp1.sys_id AND a.request_id = #tmp1.request_id AND a.action_id = #tmp1.action_id
			WHERE
				a.sys_id = @systemId AND 
				a.status_id <> @closedStatusId
			) as tmp
			JOIN types t
			ON t.sys_id = tmp.sys_id AND t.type_id = tmp.category_id
		WHERE
			t.sys_id = @systemId AND
			t.field_id = 3 
		GROUP BY t.display_name 
		) as table1
		FULL OUTER JOIN 
		(
		SELECT
			t.display_name "Category", count(*) "EndCount"
		FROM
			(
			SELECT 
				a.sys_id, a.request_id, a.action_id, a.category_id
			FROM 
				actions a
				JOIN #tmp2
 
				ON a.sys_id = #tmp2.sys_id AND a.request_id = #tmp2.request_id AND a.action_id = #tmp2.action_id
			WHERE
				a.sys_id = @systemId AND 
				a.status_id <> @closedStatusId
			) as tmp
			JOIN types t
			ON t.sys_id = tmp.sys_id AND t.type_id = tmp.category_id
		WHERE
			t.sys_id = @systemId AND
			t.field_id = 3 
		GROUP BY t.display_name 
		) as table2
		ON table2.Category = table1.Category
	ORDER BY TYPE

	SELECT 
		isNull ( table2.Status, table1.Status ) "Type",
		isNull ( StartCount, 0 ) "Start", 
		isNull ( EndCount, 0 ) "End", 
		( isNull ( EndCount, 0 ) - isNull ( StartCount, 0 ) ) "Difference"
	FROM 
		(
		SELECT
			t.display_name "Status", count(*) "StartCount"
		FROM
			(
			SELECT 
				a.sys_id, a.request_id, a.action_id, a.Status_id
			FROM 
				actions a
				JOIN #tmp1
				ON a.sys_id = #tmp1.sys_id AND a.request_id = #tmp1.request_id AND a.action_id = #tmp1.action_id
			WHERE
				a.sys_id = @systemId AND 
				a.status_id <> @closedStatusId
			) as tmp
			JOIN types t
			ON t.sys_id = tmp.sys_id AND t.type_id = tmp.Status_id
		WHERE
			t.sys_id = @systemId AND
			t.field_id = 4
		GROUP BY t.display_name 
		) as table1
		FULL OUTER JOIN 
		(
		SELECT
			t.display_name "Status", count(*) "EndCount"
		FROM
			(
			SELECT 
				a.sys_id, a.request_id, a.action_id, a.Status_id
			FROM 
				actions a
				JOIN #tmp2
				ON a.sys_id = #tmp2.sys_id AND a.request_id = #tmp2.request_id AND a.action_id = #tmp2.action_id
			WHERE
				a.sys_id = @systemId AND 
				a.status_id <> @closedStatusId
			) as tmp
			JOIN types t
			ON t.sys_id = tmp.sys_id AND t.type_id = tmp.Status_id
		WHERE
			t.sys_id = @systemId AND
			t.field_id = 4
		GROUP BY t.display_name 
		) as table2
		ON table2.Status = table1.Status
	ORDER BY TYPE

	SELECT 
		isNull ( table2.Severity, table1.Severity ) "Type",
		isNull ( StartCount, 0 ) "Start", 
		isNull ( EndCount, 0 ) "End", 
		( isNull ( EndCount, 0 ) - isNull ( StartCount, 0 ) ) "Difference"
	FROM 
		(
		SELECT
			t.display_name "Severity", count(*) "StartCount"
		FROM
			(
			SELECT 
				a.sys_id, a.request_id, a.action_id, a.Severity_id
			FROM 
				actions a
				JOIN #tmp1
				ON a.sys_id = #tmp1.sys_id AND a.request_id = #tmp1.request_id AND a.action_id = #tmp1.action_id
			WHERE
				a.sys_id = @systemId AND 
				a.status_id <> @closedStatusId
			) as tmp
			JOIN types t
			ON t.sys_id = tmp.sys_id AND t.type_id = tmp.Severity_id
		WHERE
			t.sys_id = @systemId AND
			t.field_id = 5
		GROUP BY t.display_name 
		) as table1
		FULL OUTER JOIN 
		(
		SELECT
			t.display_name "Severity", count(*) "EndCount"
		FROM
			(
			SELECT 
				a.sys_id, a.request_id, a.action_id, a.Severity_id
			FROM 
				actions a
				JOIN #tmp2
				ON a.sys_id = #tmp2.sys_id AND a.request_id = #tmp2.request_id AND a.action_id = #tmp2.action_id
			WHERE
				a.sys_id = @systemId AND 
				a.status_id <> @closedStatusId
			) as tmp
			JOIN types t
			ON t.sys_id = tmp.sys_id AND t.type_id = tmp.Severity_id
		WHERE
			t.sys_id = @systemId AND
			t.field_id = 5
		GROUP BY t.display_name 
		) as table2
		ON table2.Severity = table1.Severity
	ORDER BY TYPE

	SELECT 
		isNull ( table2.Request_Type, table1.Request_Type ) "Type",
		isNull ( StartCount, 0 ) "Start", 
		isNull ( EndCount, 0 ) "End", 
		( isNull ( EndCount, 0 ) - isNull ( StartCount, 0 ) ) "Difference"
	FROM 
		(
		SELECT
			t.display_name "Request_Type", count(*) "StartCount"
		FROM
			(
			SELECT 
				a.sys_id, a.request_id, a.action_id, a.Request_Type_id
			FROM 
				actions a
				JOIN #tmp1
				ON a.sys_id = #tmp1.sys_id AND a.request_id = #tmp1.request_id AND a.action_id = #tmp1.action_id
			WHERE
				a.sys_id = @systemId AND 
				a.status_id <> @closedStatusId
			) as tmp
			JOIN types t
			ON t.sys_id = tmp.sys_id AND t.type_id = tmp.Request_Type_id
		WHERE
			t.sys_id = @systemId AND
			t.field_id = 6 
		GROUP BY t.display_name 
		) as table1
		FULL OUTER JOIN 
		(
		SELECT
			t.display_name "Request_Type", count(*) "EndCount"
		FROM
			(
			SELECT 
				a.sys_id, a.request_id, a.action_id, a.Request_Type_id
			FROM 
				actions a
				JOIN #tmp2
				ON a.sys_id = #tmp2.sys_id AND a.request_id = #tmp2.request_id AND a.action_id = #tmp2.action_id
			WHERE
				a.sys_id = @systemId AND 
				a.status_id <> @closedStatusId
			) as tmp
			JOIN types t
			ON t.sys_id = tmp.sys_id AND t.type_id = tmp.Request_Type_id
		WHERE
			t.sys_id = @systemId AND
			t.field_id = 6
		GROUP BY t.display_name 
		) as table2
		ON table2.Request_Type = table1.Request_Type
	ORDER BY TYPE

-----------------------------------------------------------------------------------------------
-- Get the Data at the start of the interval
-----------------------------------------------------------------------------------------------
SELECT 
	t.request_id, replace(ISNULL(u.display_name, ''-''), '','', '' '') "user_login"
FROM 
	(
		SELECT 
			a.sys_id, a.request_id, a.action_id, a.status_id
		FROM 
			actions a
			JOIN #tmp1
			ON #tmp1.sys_id = a.sys_id AND #tmp1.request_id = a.request_id AND #tmp1.action_id = a.action_id
		WHERE
			a.sys_id = @systemId AND 
			a.status_id <> @closedStatusId
	)
	as t
	LEFT JOIN action_users au
	ON au.sys_id = t.sys_id AND au.request_id = t.request_id AND au.action_id = t.action_id AND au.user_type_id = 2
	LEFT JOIN users u
	ON u.user_id = au.user_id
WHERE
	t.sys_id = @systemId
ORDER BY t.request_id DESC

-----------------------------------------------------------------------------------------------
-- Get the Data at the end of the interval
-----------------------------------------------------------------------------------------------
SELECT 
	t.request_id, replace(ISNULL(u.display_name, ''-''), '','', '' '') "user_login"
FROM 
	(
		SELECT 
			a.sys_id, a.request_id, a.action_id, a.status_id
		FROM 
			actions a
			JOIN #tmp2
			ON a.sys_id = #tmp2.sys_id AND a.request_id = #tmp2.request_id AND a.action_id = #tmp2.action_id 
		WHERE
			a.sys_id = @systemId AND 
			a.status_id <> @closedStatusId
	)
	as t
	LEFT JOIN action_users au
	ON au.sys_id = t.sys_id AND au.request_id = t.request_id AND au.action_id = t.action_id AND au.user_type_id = 2
	LEFT JOIN users u
	ON au.user_id = u.user_id
WHERE
	t.sys_id = @systemId
ORDER BY t.request_id DESC


-----------------------------------------------------------------------------------------------
-- Get the Data at the start of the interval
-----------------------------------------------------------------------------------------------
SELECT 
	t.request_id, replace(ISNULL(u.display_name, ''-''), '','', '' '') "user_login"
FROM 
	(
		SELECT 
			a.sys_id, a.request_id, a.action_id, a.status_id
		FROM 
			actions a
			JOIN #tmp1
			ON #tmp1.sys_id = a.sys_id AND #tmp1.request_id = a.request_id AND #tmp1.action_id = a.action_id
		WHERE
			a.sys_id = @systemId AND 
			a.status_id <> @closedStatusId
	)
	as t
	LEFT JOIN action_users au
	ON au.sys_id = t.sys_id AND au.request_id = t.request_id AND au.action_id = t.action_id AND au.user_type_id = 3
	LEFT JOIN users u
	ON u.user_id = au.user_id
WHERE
	t.sys_id = @systemId
ORDER BY t.request_id DESC

-----------------------------------------------------------------------------------------------
-- Get the Data at the end of the interval
-----------------------------------------------------------------------------------------------
SELECT 
	t.request_id, replace(ISNULL(u.display_name, ''-''), '','', '' '') "user_login"
FROM 
	(
		SELECT 
			a.sys_id, a.request_id, a.action_id, a.status_id
		FROM 
			actions a
			JOIN #tmp2
			ON a.sys_id = #tmp2.sys_id AND a.request_id = #tmp2.request_id AND a.action_id = #tmp2.action_id 
		WHERE
			a.sys_id = @systemId AND 
			a.status_id <> @closedStatusId
	)
	as t
	LEFT JOIN action_users au
	ON au.sys_id = t.sys_id AND au.request_id = t.request_id AND au.action_id = t.action_id AND au.user_type_id = 3
	LEFT JOIN users u
	ON au.user_id = u.user_id
WHERE
	t.sys_id = @systemId
ORDER BY t.request_id DESC

DROP table #tmp1
DROP table #tmp2


' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_request_lookupBySystemIdAndRequestData]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_request_lookupBySystemIdAndRequestData]
(
	@systemId int,
	@userId      int,
	@subject    nvarchar(4000),
	@updatedtime datetime
)
AS
SELECT 
	request_id
FROM 
	requests 
WHERE
	sys_id = @systemId and 
	user_id = @userId and 
	subject = @subject and
	abs(DateDiff(minute, lastupdated_datetime, @updatedtime)) < 5

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_request_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
CREATE PROCEDURE [dbo].[stp_request_insert] 
( 
        @systemId               INT, 
        @requestId              BIGINT, 
        @categoryId             INT, 
        @statusId               INT, 
        @severityId             INT, 
        @requestTypeId          INT, 
        @subject                NVARCHAR(4000), 
        @description            NTEXT, 
        @IsPrivate              BIT, 
        @parentId               BIGINT, 
        @userId                 INT, 
        @maxActionId            INT, 
        @dueDate                DATETIME, 
        @loggedDate             DATETIME, 
        @updatedDate            DATETIME, 
        @headerDesc             NTEXT, 
        @attachments            NTEXT, 
        @summary                NTEXT, 
        @memo                   NTEXT, 
        @append         INT, 
        @notify                 INT, 
        @notifyLoggers          BIT, 
        @repliedToAction        INT, 
        @officeId                         INT 
) 
AS 
--- Max Action Id is always 1 when inserting the request. 
SELECT @maxActionId = 1 
--- Now insert the request. 
INSERT INTO requests 
( 
        sys_id,                 request_id,             category_id, 
        status_id,              severity_id,            request_type_id, 
        subject,                description,            is_private, 
        parent_request_id,      user_id,                max_action_id, 
        due_datetime,           logged_datetime,        lastupdated_datetime, 
        header_description,     attachments,			summary, 
        memo,                   append_interface,       notify, 
        notify_loggers, replied_to_action,office_id 
) 
VALUES 
( 
        @systemId,              @requestId,             @categoryId, 
        @statusId,              @severityId,            @requestTypeId, 
        @subject,               @description,           @IsPrivate, 
        @parentId,              @userId,                @maxActionId, 
        @dueDate,               @loggedDate,            @updatedDate, 
        @headerDesc,            @attachments,			@summary, 
        @memo,                  @append,                @notify, 
        @notifyLoggers,         @repliedToAction,          @officeId 
)
---- Insert the corresponding record into actions table. 
INSERT INTO actions 
( 
        sys_id,                 request_id,             action_id, 
        category_id,            status_id,              severity_id, 
        request_type_id,        subject,                description, 
        is_private,             parent_request_id,      user_id, 
        due_datetime,           logged_datetime,        lastupdated_datetime, 
        header_description,     attachments,			summary, 
        memo,                   append_interface,       notify, 
        notify_loggers,         replied_to_action,      office_id 
) 
VALUES 
( 
        @systemId,              @requestId,             @maxActionId, 
        @categoryId,            @statusId,              @severityId, 
        @requestTypeId,         @subject,               @description, 
        @IsPrivate,             @parentId,              @userId, 
        @dueDate,               @loggedDate,            @updatedDate, 
        @headerDesc,            @attachments,			@summary, 
        @memo,                  @append,                @notify, 
        @notifyLoggers,         @repliedToAction,       @officeId 
)


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_request_getChildrenBySystemId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_request_getChildrenBySystemId]
(
	@systemId	INT
)
AS
SELECT 
	request_id ''childId'',
	parent_request_id ''parentId''
FROM
	requests
WHERE
	sys_id = @systemId AND
	parent_request_id <> 0

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_report_requestsWorkedOn]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'


CREATE PROCEDURE [dbo].[stp_report_requestsWorkedOn]
(
	@systemId int,
	@startTime varchar(255),
	@endTime varchar(255)
) 
as
--IF(
--	@systemId != 106 AND 	--GBO
--	@systemId != 109 AND 	--FinopHyd
--	@systemId != 115 AND	--CashTrack
--	@systemId != 201 AND	--tBits
--	@systemId != 228 AND	--GBOTact
--	@systemId != 105		--GenDev
--  )
--BEGIN
--	SELECT @systemId = -1
--END


SELECT 
	DISTINCT 
	r.request_id 
INTO 
	#tmp
FROM 
	requests r
	JOIN actions a
	ON r.sys_id = a.sys_id AND r.request_id = a.request_id 
WHERE 
	r.sys_id = @systemId and 
	r.request_id = a.request_id and 
	a.lastupdated_datetime >= @startTime  and 
	a.lastupdated_datetime <= @endTime

SELECT 
        req.sys_id,
	req.request_id,
	req.category_id,
	req.status_id,
	req.severity_id,
	req.request_type_id,
	req.subject,
	req.description "description",
	req.is_private,
	req.parent_request_id,
	req.user_id,
	req.max_action_id,
	req.due_datetime,
	req.logged_datetime,
	req.lastupdated_datetime,
	isnull(req.header_description, '''''''') "header_description",
	isnull(req.attachments, '''''''') "attachments",
	isnull(req.summary, '''''''') "summary",
	isnull(req.memo, '''''''') "memo",
	req.append_interface,
	req.notify,
	req.notify_loggers,
	req.replied_to_action,
	req.office_id
FROM 
	requests req
WHERE 
	req.sys_id = @systemId AND 
	req.request_id in (select * from #tmp)
	order by req.request_id desc 

-- Get the Corresponding request-user objects
SELECT 
	* 
FROM 
	request_users ru 
WHERE 
	ru.sys_id = @systemId AND 
	ru.request_id in (select * from #tmp)
	order by ru.request_id desc





' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_request_lookupBySystemIdAndParentId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
CREATE PROCEDURE [dbo].[stp_request_lookupBySystemIdAndParentId]
(
	@systemId int,
	@parentId int
)
AS
SELECT 
	count(*) requestCount
FROM 
	requests 
WHERE
	sys_id = @systemId and 
	parent_request_id = @parentId 

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_get_requestIdByExtendedFields]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
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

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_roles_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author:		Sandeep Giri
-- Create date: 05 Jan 07
-- Description:	Inserts a role. Returns the newly created role.
-- =============================================
CREATE PROCEDURE [dbo].[stp_roles_insert] 
	-- Add the parameters for the stored procedure here
	@sys_id int,
	@rolename nvarchar(50),
	@description nvarchar(250)
AS
BEGIN
	DECLARE @role_id INT
	SELECT 
		@role_id = ISNULL(max(role_id), 0) 
	FROM 
		roles
	SELECT @role_id = @role_id + 1

	INSERT INTO [dbo].[roles]
           ([sys_id]
           ,[role_id]
           ,[rolename]
           ,[description])
     VALUES
		(@sys_id,@role_id,@rolename,@description)
	select @role_id
END

' 
END
GO

set ANSI_NULLS ON
GO
set QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_role_delete] 
(
	@systemId INT,
	@roleId	INT
)
AS
BEGIN
	DELETE FROM roles WHERE sys_id = @systemId AND role_id = @roleId
	DELETE FROM roles_users WHERE sys_id = @systemId AND role_id = @roleId
	DELETE FROM roles_permissions WHERE sys_id = @systemId AND role_id = @roleId	
END
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_roles_delete ]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author:		Sandeep Giri
-- Create date: 05 Jan 07
-- Description:	Deletes a role and association with users and permissions
-- =============================================
CREATE PROCEDURE [dbo].[stp_roles_delete ]
	@role_id int = 0 
AS
BEGIN
	DELETE FROM ROLES
	WHERE     (role_id = @role_id)
	DELETE FROM roles_permissions
	WHERE     (role_id = @role_id)
	DELETE FROM roles_users
	WHERE     (role_id = @role_id)
END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_roles_insert_existing_role]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_roles_insert_existing_role] 
	-- Add the parameters for the stored procedure here
	@sys_id int,
	@role_id int,
	@rolename nvarchar(50),
	@description nvarchar(250)
AS
BEGIN
	INSERT INTO [dbo].[roles]
           ([sys_id]
           ,[role_id]
           ,[rolename]
           ,[description])
     VALUES
		(@sys_id,@role_id,@rolename,@description)
END

SET ANSI_NULLS ON
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_admin_getUserRolesBySysIdAndUserId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_admin_getUserRolesBySysIdAndUserId] 
(
	@sys_id int ,
	@user_id int 
)
AS
SELECT 
	r.role_id, 
	r.rolename, 
	ISNULL (ru.user_id, -1)as ''user_id''
FROM 
	roles r
	LEFT join roles_users ru
	on r.sys_id = ru.sys_id and r.role_id = ru.role_id and ru.user_id = @user_id AND ru.is_active = 1
WHERE 
	r.sys_id = @sys_id and
        r.role_id > 6
ORDER BY r.role_id

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_admin_getRolesBySysId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_admin_getRolesBySysId] 
(
	@sys_id INT
)
AS

SELECT 
	*
FROM 
	roles
WHERE 
	sys_id = @sys_id

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_admin_role_lookupBySystemIdAndRoleName]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_admin_role_lookupBySystemIdAndRoleName]
(
	@sys_id		int,
        @rolename	varchar(255)
)
AS

SELECT 
	* 
FROM 
	roles 
WHERE
	sys_id 	 = @sys_id  AND
	rolename = @rolename

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_roles_permissions_update]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_roles_permissions_update] 
(
	@sys_id INT,
	@role_id INT,
	@field_id INT,
	@gpermissions INT,
	@dpermissions INT
)
AS
DECLARE @already_defined NUMERIC
select @already_defined = 0;
select @already_defined = count(*) from roles_permissions where sys_id = @sys_id AND role_id = @role_id AND field_id = @field_id
if @already_defined = 0 
BEGIN
	INSERT INTO roles_permissions 
		(sys_id, role_id, field_id, gpermissions, dpermissions)
	VALUES
		(@sys_id, @role_id, @field_id, @gpermissions, @dpermissions)
END
ELSE
BEGIN
	UPDATE roles_permissions
	SET
		gpermissions = @gpermissions,
		dpermissions = @dpermissions
	WHERE 
		sys_id = @sys_id AND
		role_id = @role_id AND 
		field_id = @field_id
END


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ru_getAllRoleUsers]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_ru_getAllRoleUsers]
AS

SELECT
	sys_id,
	role_id,
	user_id,
	is_active
FROM 
	roles_users

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_ru_lookupBySystemIdAndRoleId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_ru_lookupBySystemIdAndRoleId]
(
	@sysId		int,
	@roleId 	int
)
AS
SELECT 
	*
FROM 
	roles_users ru
WHERE
	sys_id  = @sysId AND
	role_id = @roleId AND 
	is_active = 1

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_roles_users_update]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_roles_users_update] 
(
	@sys_id 	INT,
	@role_id 	INT,
	@user_id 	INT,
	@is_active	BIT
)
AS
UPDATE roles_users 
SET	
	role_id = @role_id,
	is_active = @is_active
WHERE 
	sys_id = @sys_id and
	user_id = @user_id
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_admin_delete_roles_users]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_admin_delete_roles_users] 
(
	@sys_id 	INT,
	@role_id 	INT,
	@user_id 	INT,
	@is_active	BIT
)
AS

DELETE 
	roles_users 
WHERE  
	sys_id = @sys_id
	AND role_id = @role_id
	AND user_id = @user_id

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_admin_getRolesBySysIdAndUserId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_admin_getRolesBySysIdAndUserId] 
(
	@sys_id 	INT,
	@user_id 	INT
)
AS
SELECT 
	*
FROM 
	roles_users
WHERE 
	sys_id = @sys_id and 
	user_id=@user_id
ORDER BY role_id 

SELECT
       *
FROM
	super_users
WHERE
	user_id = @user_id

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_admin_insert_roles_users]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_admin_insert_roles_users] 
(
	@sys_id 	INT,
	@role_id 	INT,
	@user_id 	INT,
	@is_active	BIT
)
AS
INSERT INTO roles_users 
(
	sys_id,
	role_id,
	user_id,
	is_active
) 
VALUES 
(
	@sys_id,
	@role_id,
	@user_id,
	@is_active
)

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_report_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_report_insert] 
	-- Add the parameters for the stored procedure here	
	@reportName 			NVARCHAR(3000),
	@reportDescription		NTEXT,
	@fileName			NVARCHAR(3000),
	@isPrivate			BIT,
	@reportId			INT OUTPUT
AS
BEGIN

DECLARE @rep_Id INT

SELECT 
	@rep_Id = ISNULL(max(report_id), 0) 
FROM 
	reports

SELECT @rep_Id = @rep_Id + 1

-- Insert statements for procedure here
INSERT INTO reports
( 
	report_id, 
	report_name, 
	description,
	file_name,
	is_private,
	is_enabled
)
VALUES
(
	@rep_Id,
	@reportName,
	@reportDescription,
	@fileName,
	@isPrivate,	
	''false''
)
SELECT @reportId = @rep_Id
END

/****** Object:  StoredProcedure [dbo].[stp_report_delete]    Script Date: 09/17/2008 16:51:29 ******/
SET ANSI_NULLS ON
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_report_delete]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
CREATE PROCEDURE [dbo].[stp_report_delete] 
(
	@reportId		INT,	
	@returnValue	INT OUTPUT,
	@returnFileName NVARCHAR (3000)OUTPUT
)
AS

IF exists(SELECT report_id from reports where report_id = @reportId)
BEGIN
	SELECT @returnFileName = (SELECT file_name from reports where report_id = @reportId)

	DELETE FROM reports WHERE report_id = @reportId	
	DELETE FROM report_roles WHERE report_id = @reportId
	DELETE FROM report_specific_users WHERE report_id = @reportId

	SELECT @returnValue = 1		
END
ELSE
BEGIN
	SELECT @returnValue = 0
END

/****** Object:  StoredProcedure [dbo].[stp_report_update]    Script Date: 11/29/2008 16:15:36 ******/
SET ANSI_NULLS ON
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_report_update]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
CREATE PROCEDURE [dbo].[stp_report_update] 
(
	@report_id 		int,	
	@report_name 	nvarchar(3000),
	@description 	ntext,
	@file_name		nvarchar(3000),
	@is_private		bit,
	@is_enabled		bit
)
AS
UPDATE reports
SET
	report_name		= @report_name,
	description		= @description,
	file_name		= @file_name,
	is_private	    	= @is_private,
	is_enabled		= @is_enabled	
WHERE 
    report_id = @report_id

/****** Object:  StoredProcedure [dbo].[stp_report_role_insert]    Script Date: 11/29/2008 16:49:03 ******/
SET ANSI_NULLS ON
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_report_lookupByReportId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_report_lookupByReportId]
(
	@reportId int
)
AS
	SELECT * from reports where report_id = @reportId


/****** Object:  StoredProcedure [dbo].[stp_report_lookupBySysIdAndRoleId]    Script Date: 11/29/2008 16:19:01 ******/
SET ANSI_NULLS ON
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_report_lookupBySysIdAndRoleId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE procedure [dbo].[stp_report_lookupBySysIdAndRoleId]
(@sys_id int,
 @role_id int)
as
begin
select * from reports 
where report_id in (select report_id from report_roles 
where sys_id = @sys_id and
      role_id = @role_id)
end









' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_report_role_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_report_role_insert] 
	-- Add the parameters for the stored procedure here	
	@sysId					INT,
	@reportId				INT,
	@roleId					INT 
AS
BEGIN
-- Insert statements for procedure here
INSERT INTO report_roles
( 
	sys_id,
	report_id, 
	role_id
)
VALUES
(
	@sysId,
	@reportId,
	@roleId
)
END

/****** Object:  StoredProcedure [dbo].[stp_report_specific_user_insert]    Script Date: 11/29/2008 16:17:14 ******/
SET ANSI_NULLS ON
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_report_specific_user_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_report_specific_user_insert] 
	-- Add the parameters for the stored procedure here	
	
	@reportId				INT,
	@userId					INT,
	@isIncluded				BIT
AS
BEGIN
-- Insert statements for procedure here
INSERT INTO report_specific_users
( 
	report_id, 
	user_id,
	is_included
)
VALUES
(
	@reportId,
	@userId,
	@isIncluded
)
END

/****** Object:  StoredProcedure [dbo].[stp_report_lookupByReportId]    Script Date: 11/29/2008 16:18:27 ******/
SET ANSI_NULLS ON
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_display_group_lookupBySystemIdAndDisplayName]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
CREATE PROCEDURE [dbo].[stp_display_group_lookupBySystemIdAndDisplayName] 
	-- Add the parameters for the stored procedure here
	@sys_id			INT,
	@displayName 	VARCHAR(128)
AS
BEGIN
	select sys_id, id, display_name, display_order, is_active 
	from display_groups
	where display_name = @displayName and sys_id = @sys_id
END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_display_group_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author: Lokesh
-- Create date: 4 APR 2009
-- Description:	Inserts the display group
-- =============================================
CREATE PROCEDURE [dbo].[stp_display_group_insert]
	@sys_id int,
	@id int,
	@display_name varchar(50),
	@display_order int,
	@is_active bit,
	@new_id int output
AS
BEGIN
	insert into display_groups (sys_id, display_name, display_order, is_active) 
	Values(@sys_id, @display_name, @display_order, @is_active);
	select @new_id = @@IDENTITY;
END

/****** Object:  StoredProcedure [dbo].[stp_display_group_update]    Script Date: 04/10/2009 17:28:19 ******/
SET ANSI_NULLS ON
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_display_group_update]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[stp_display_group_update]
	@sys_id int,
	@id int,
	@display_name varchar(50),
	@display_order int,
	@is_active bit
AS
BEGIN
	update display_groups set display_name = @display_name, display_order = @display_order, is_active = @is_active
	where id = @id
END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_action_lookupById]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_action_lookupById] 
( 
        @systemId 	INT, 
        @requestId 	INT, 
        @actionId 	INT
) 
as 

SELECT 
    sys_id, 
    request_id, 
    action_id, 
    category_id, 
    status_id, 
    severity_id, 
    request_type_id, 
    subject, 
    description, 
    is_private, 
    parent_request_id, 
    user_id, 
    due_datetime, 
    logged_datetime, 
    lastupdated_datetime, 
    isnull(header_description, '''') "header_description", 
    isnull(attachments, '''') "attachments", 
    isnull(summary, '''') "summary", 
    isnull(memo, '''') "memo", 
    append_interface, 
    notify, 
    notify_loggers, 
    replied_to_action, 
    office_id 
FROM 
        actions act 
WHERE 
        sys_id          = @systemId AND 
        request_id      = @requestId AND 
        action_id       = @actionId 

SELECT 
        * 
FROM 
        action_users 
WHERE 
        sys_id          = @systemId AND 
        request_id      = @requestId AND 
        action_id       = @actionId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_getAndIncrMaxId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author:		Sandeep Giri
-- Create date: 
-- Description:	Gets the current max id And increases the max of Repo Id. It 
-- =============================================
CREATE PROCEDURE [dbo].[stp_getAndIncrMaxId] 
	@tableName varchar(250)
AS
BEGIN
BEGIN TRANSACTION
declare @maxid int
select @maxid = id from max_ids where name=@tableName
if @maxid is null
begin
	insert into max_ids (name, id) values (@tableName, 1)
	select 1 max_id;
end
else
begin
	update max_ids set id = @maxid +1 where name=@tableName
	select @maxid + 1 max_id
end

	commit TRAN
END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_action_getActionText]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_action_getActionText]
(
	@systemId	INT,
	@requestId	INT,
	@actionId	INT
)
AS

SELECT
	sys_id,
	request_id,
	action_id,
	description,
	header_description,
	attachments
FROM
	actions
WHERE
	sys_id = @systemId AND
	request_id = @requestId AND
	action_id = @actionId



' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_action_updateActionText]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_action_updateActionText]
(
	@systemId	INT,
	@requestId	INT,
	@actionId	INT,
	@description	TEXT,
	@headerDesc	TEXT,
	@attachments	TEXT
)
AS
UPDATE actions
SET
	description 		= @description,
	header_description	= @headerDesc,
	attachments		= @attachments
WHERE
	sys_id = @systemId AND
	request_id = @requestId AND
	action_id = @actionId

' 
END
GO
SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_action_updateAttachments]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_action_updateAttachments]
(
	@systemId	INT,
	@requestId	INT,
	@actionId	INT,
	@attachments   TEXT
)
AS

UPDATE actions
SET 
	attachments = @attachments
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId AND
	action_id	= @actionId

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_business_area_users_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
CREATE PROCEDURE [dbo].[stp_business_area_users_insert] 
(
	@sys_id 	INT,
	@user_id 	INT,
	@is_active 	BIT
)
AS
INSERT INTO business_area_users 
(
	sys_id,
	user_id,
	is_active
) 
VALUES 
(
	@sys_id,
	@user_id,
	@is_active
)

exec stp_admin_insert_roles_users @sys_id, 12, @user_id, 1
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_request_lookupSubject]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_request_lookupSubject]
(
	@systemId 	INT,
	@requestId      INT,
	@userId         INT,
	@email          BIT 
)
AS

DECLARE @private BIT
DECLARE @privatePermission INT

SELECT @private = DBO.stp_request_getRequestPrivacy(@systemId, @requestId)

IF (@private = 1)
BEGIN
    IF(@email = 1)
        BEGIN
           RETURN 
        END
    ELSE
        BEGIN
           SELECT @privatePermission = DBO.stp_request_getUserPrivatePermissions(@systemId, @requestId, @userId)
           IF (@privatePermission > 3)
             BEGIN
               SELECT 
	           subject 
               FROM
	           requests 
               WHERE 
	           sys_id = @systemId AND
	           request_id = @requestId
               END
        END
END

ELSE

BEGIN
   SELECT 
      subject 
   FROM
      requests 
   WHERE 
      sys_id = @systemId AND
      request_id = @requestId
END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tbits_insertStandardFieldDefaultsWrap]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_tbits_insertStandardFieldDefaultsWrap]
(
    @systemId INT
)
AS
exec stp_tbits_insertStandardFieldDefaults @systemId 

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tbits_createBusinessArea]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_tbits_createBusinessArea] 
(
	@sysName	VARCHAR(128),
	@sysPrefix	VARCHAR(128)
)
AS
DECLARE @systemId INT
DECLARE @userId INT

SELECT 
	@userId = user_id 
FROM 
	users 
WHERE user_login = ''root''

SELECT 
	@systemId = ISNULL(max(sys_id), 0) 
FROM 
	business_areas

SELECT @systemId = @systemId + 1

-- Insert the Business Area Record.
INSERT INTO business_areas 
(
	sys_id, 
	name, 
	display_name, 
	email, 
	sys_prefix, 
	description, 
	type, 
	location, 
	max_request_id, 
	max_email_actions, 
	is_email_active, 
	is_active, 
	date_created, 
	is_private, 
	sys_config, 
	field_config
)
VALUES
(
	@systemId, 
	@sysName, 
	@sysName, 
	@sysprefix+''@localhost'', 
	@sysPrefix, 
	@sysName, 
	''Development'', 
	''hyd'', 
	0, 
	10, 
	1, 
	1, 
	getUTCDate(), 
	0, 
	''
	<SysConfig>
	    <!-- Default CSS to be used for this business area. -->
	    <Stylesheet web="tbits.css" email="null" />
		<!-- Datetime related options. -->
	    <DefaultDueDate allowNull="true" duration="0" />
	    <DateFormat list="6" email="6" />
		<!-- Mailing options. -->
	    <Notify request="1" action="1" />
	    <NotifyLogger request="true" action="true" />
	    <MailFormat format="1" />
	    <!-- Severity related options. -->
	    <Severity>
	        <Incoming highValue="critical" lowValue="low" />
	        <Outgoing highValue="critical" lowValue="low" />
		</Severity>
		<!-- Other options.-->
		<Assign all="false" volunteer="0" />
		<LegacyPrefixes list="" />
		<!-- Custom links if any. -->
		<CustomLinks>
		</CustomLinks>
	</SysConfig>
	'', 
	null
)

--- Insert the Field Records.
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 1, ''sys_id'', ''Business Area'', ''BA'', 5, 0, 0, 1, '''', 6, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 2, ''request_id'', ''Request'', ''Request'', 5, 0, 0, 1, '''', 47, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 3, ''category_id'', ''Category'', ''Category'', 9, 0, 3, 1, '''', 254, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 4, ''status_id'', ''Status'', ''Status'', 9, 0, 3, 1, '''', 126, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 5, ''severity_id'', ''Priority'', ''Severity'', 9, 0, 3, 1, '''', 126, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 6, ''request_type_id'', ''Request Type'', ''Request Type'', 9, 0, 3, 1, '''', 126, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 7, ''logger_ids'', ''Logger'', ''Loggers'', 10, 0, 3, 1, '''', 191, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 8, ''assignee_ids'', ''Assignee'', ''Assignees'', 10, 0, 3, 1, '''', 191, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 9, ''subscriber_ids'', ''Subscribers'', ''Subscribers'', 10, 0, 5, 1, '''', 63, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private)
 VALUES(@systemId, 10, ''to_ids'', ''To'', ''To'', 10, 0, 2, 1, '''', 125, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 11, ''cc_ids'', ''Cc'', ''CC'', 10, 0, 2, 1, '''', 125, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 12, ''subject'', ''Subject'', ''Subject'', 7, 0, 3, 1, '''', 127, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 13, ''description'', ''Description'', ''Description'', 8, 0, 0, 1, '''', 103, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 14, ''is_private'', ''Private'', ''Private'', 1, 0, 1, 1, '''', 126, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 15, ''parent_request_id'', ''Parent'', ''Parent'', 5, 0, 3, 1, '''', 127, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 16, ''user_id'', ''Last Update By.'', ''User'', 10, 0, 0, 1, '''', 44, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 17, ''max_action_id'', ''# U'', ''Action'', 5, 0, 0, 1, '''', 44, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 18, ''due_datetime'', ''Due Date'', ''Due'', 4, 0, 3, 1, '''', 127, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 19, ''logged_datetime'', ''Submitted Date'', ''Logged'', 4, 0, 0, 1, '''', 44, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 20, ''lastupdated_datetime'', ''Last Updated'', ''Last Updated'', 4, 0, 0, 1, '''', 44, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 21, ''header_description'', ''Header Description'', ''Header'', 8, 0, 0, 1, '''', 4, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 22, ''attachments'',  ''Attachments'', ''Attach'', 11, 0, 0, 1, '''', 103, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 23, ''summary'', ''Summary'', ''Summary'', 8, 0, 1, 1, '''', 103, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 24, ''memo'', ''Memo'', ''Memo'', 8, 0, 1, 1, '''', 7, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 25, ''append_interface'', ''append_interface'', ''append_interface'', 5, 0, 0, 1, '''',0, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 26, ''notify'', ''Notify'', ''Notify'', 1, 0, 1, 1, '''', 126, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 27, ''notify_loggers'', ''Notify Logger'', ''Notify Logger'', 1, 0, 1, 1, '''', 126, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 28, ''replied_to_action'', ''replied_to_action'', ''replied_to_action'', 5, 0, 1, 1, '''', 70, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 29, ''related_requests'', ''Linked Requests'', ''Linked Requests'', 7, 0, 4, 1, '''', 23, 0)
INSERT INTO fields (sys_id, field_id, name, display_name, description, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private) 
VALUES(@systemId, 30, ''office_id'', ''Office'', ''Office'', 9, 0, 3, 1, '''', 126, 0)
INSERT INTO fields(sys_id, field_id, name, display_name, description, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent, display_order, display_group) 
VALUES(@systemId,  31, ''SendSMS'', ''Send SMS'', ''Sends SMS'', 1, 1, 1, 0, 0, 47, '''', 0, 0, 0)


--- Insert into field_descriptor table
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 1, ''ba'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 2, ''req'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 2, ''request'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 3, ''cat'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 3, ''category'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 4, ''stat'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 4, ''status'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 5, ''sev'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 5, ''severity'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 6, ''reqtype'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 6, ''requesttype'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 6, ''type'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 7, ''log'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 7, ''logger'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 7, ''loggers'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 8, ''ass'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 8, ''assignee'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 8, ''assignees'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 8, ''assign'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 9, ''sub'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 9, ''subscriber'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 9, ''subscribers'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 10, ''to'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 11, ''cc'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 12, ''subj'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 13, ''desc'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 13, ''alltext'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 14, ''conf'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 15, ''par'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 15, ''parent'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 16, ''user'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 17, ''action'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 18, ''ddate'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 18, ''due'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 18, ''dueby'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 18, ''duedate'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 19, ''ldate'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 19, ''loggeddate'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 20, ''udate'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 20, ''updateddate'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 21, ''hdr'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 22, ''att'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 23, ''sum'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 23, ''summary'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 24, ''memo'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 25, ''actfrm'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 26, ''notify'', 0)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 26, ''mail'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 27, ''notlog'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 28, ''reAction'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 29, ''relReq'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 29, ''link'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 29, ''linked'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 30, ''off'', 1)
INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@systemId, 31, ''sendsms'', 1)

---- Insert the default Types.

---- Categories
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 3, 1, ''pending'', ''Others'', ''Pending - Not specified or Misc'', 1, 1, 1, 1, 0, 0)

---- Statuses
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 4,1, ''Pending'', ''Pending'', ''Pending'', 1, 1, 1, 1, 0, 0)
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 4,2, ''Active'', ''Active'', ''Active'', 2, 1, 0, 1, 0, 0)
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 4,3, ''Closed'', ''Closed'', ''Closed'', 3, 1, 0, 0, 0, 0)
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 4,4, ''Reopened'', ''Reopened'', ''Reopened'', 4, 1, 0, 0, 0, 0)
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 4,5, ''Suspended'', ''Suspended'', ''Suspended'', 5, 1, 0, 0, 0, 0)

---- Severities
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 5, 1, ''low'', ''Low'', ''Low'', 1, 1, 0, 1, 0, 0)
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 5, 2, ''medium'', ''Medium'', ''Medium'', 2, 1, 1, 1, 0, 0)
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 5, 3, ''high'', ''High'', ''High'', 3, 1, 0, 1, 0, 0)
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 5, 4, ''critical'', ''Critical'', ''Critical'', 4, 1, 0, 1, 0, 0)

---- Request Types.
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 6, 1, ''request'', ''Request'', ''Request'', 1, 1, 1, 1, 0, 0)
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
VALUES(@systemId, 6, 2, ''question'', ''Question'', ''Question'', 2, 1, 0, 1, 0, 0)
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
--VALUES(@systemId, 6, 3, ''countermeasure'', ''Counter Measure'', ''Counter Measure'', 3, 1, 0, 1, 0, 0)
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final) 
--VALUES(@systemId, 6, 4, ''manual'', ''Manual'', ''Manual'', 4, 1, 0, 1, 0, 0)


----Insert default locations
INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
VALUES(@systemId, 30, 1, ''default'', ''-'', ''default'', 1, 1, 1, 1, 0, 0)
--
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
--VALUES (@systemId, 30, 2, ''Gurgaon Plant'', ''Gurgaon Plant'', ''Houston'', 2, 1, 0, 0, 0, 0)
--
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
--VALUES (@systemId, 30, 3, ''Hyderabad'', ''HYD'', ''Hyderabad'', 3, 1, 0, 0, 0, 0) 
--
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
--VALUES (@systemId, 30, 4, ''Kansas City'', ''KC'', ''Kansas City'', 4, 1, 0, 0, 0, 0) 
--
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
--VALUES (@systemId, 30, 5, ''London'', ''LON'', ''London'', 5, 1, 0, 0, 0, 0 )
--
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
--VALUES (@systemId, 30, 6, ''New York'', ''NYC'', ''New York'', 6, 1, 0, 0, 0, 0) 
--
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
--VALUES ( @systemId, 30, 7, ''San Fransisco'', ''SF'', ''San Fransisco'', 7, 1, 0, 0, 0, 0) 
--
--
--INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
--VALUES (@systemId, 30, 8, ''Silicon Valley'', ''SV'', ''Silicon Valley'', 8, 1, 0, 0, 0, 0) 


---- INSERT INTO roles.
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 1, ''User'', ''User'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 2, ''Logger'', ''Logger'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 3, ''Assignee'', ''Assignee'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 4, ''Subscriber'', ''Subscriber'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 5, ''To'', ''To'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 6, ''Cc'', ''Cc'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 7, ''Analyst'', ''Analyst'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 8, ''Manager'', ''Manager'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 9, ''Admin'', ''Admin'')
INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@systemId, 10, ''PermissionAdmin'', ''PermissionAdmin'')

--Insert one BAUser
--INSERT INTO business_area_users(sys_id,user_id) VALUES(@systemId, @userId)
exec stp_business_area_users_insert @systemId, @userId, 1

/**
Permission Values.
===================
|   Type | Value. |
=================== 
|    Add |  1     |
| Change |  2     |
|   View |  4     |
=================== 
*/
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 1, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 2, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 3, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 4, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 5, 6, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 6, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 7, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 8, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 9, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 10, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 11, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 12, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 13, 5, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 14, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 15, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 16, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 17, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 18, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 19, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 20, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 21, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 22, 5, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 23, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 24, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 25, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 26, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 27, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 28, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 29, 7, 0)
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 1, 30, 4, 0)  

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 1, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 2, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 3, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 4, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 5, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 6, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 7, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 8, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 9, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 10, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 11, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 12, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 13, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 14, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 15, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 16, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 17, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 18, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 19, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 20, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 21, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 22, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 23, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 24, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 25, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 26, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 27, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 28, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 29, 0, 0)
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 2, 30, 0, 0)  

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 1, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 2, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 3, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 4, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 5, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 6, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 7, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 8, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 9, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 10, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 11, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 12, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 13, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 14, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 15, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 16, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 17, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 18, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 19, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 20, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 21, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 22, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 23, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 24, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 25, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 26, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 27, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 28, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 29, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 3, 30, 7, 0) 

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 1, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 2, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 3, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 4, 0, 0) 

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 5, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 6, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 7, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 8, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 9, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 10, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 11, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 12, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 13, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 14, 4, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 15, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 16, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 17, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 18, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 19, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 20, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 21, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 22, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 23, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 24, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 25, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 26, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 27, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 28, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 29, 0, 0)
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 4, 30, 0, 0)  

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 1, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 2, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 3, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 4, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 5, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 6, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 7, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 8, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 9, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 10, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 11, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 12, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 13, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 14, 7, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 15, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 16, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 17, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 18, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 19, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 20, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 21, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 22, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 23, 3, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 24, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 25, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 26, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 27, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 28, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 29, 0, 0)
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 7, 30, 3, 0)  

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 1, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 2, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 3, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 4, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 5, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 6, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 7, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 8, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 9, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 10, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 11, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 12, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 13, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 14, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 15, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 16, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 17, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 18, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 19, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 20, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 21, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 22, 2, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 23, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 24, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 25, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 26, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 27, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 28, 0, 0) 
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 29, 0, 0)
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId, 8, 30, 0, 0)

EXEC stp_tbits_insertAuthorizationDefaults @systemId
' 
END
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[FK_QRTZ_TRIGGERS_QRTZ_JOB_DETAILS]') AND parent_object_id = OBJECT_ID(N'[dbo].[QRTZ_TRIGGERS]'))
ALTER TABLE [dbo].[QRTZ_TRIGGERS]  WITH CHECK ADD  CONSTRAINT [FK_QRTZ_TRIGGERS_QRTZ_JOB_DETAILS] FOREIGN KEY([JOB_NAME], [JOB_GROUP])
REFERENCES [dbo].[QRTZ_JOB_DETAILS] ([JOB_NAME], [JOB_GROUP])
GO
ALTER TABLE [dbo].[QRTZ_TRIGGERS] CHECK CONSTRAINT [FK_QRTZ_TRIGGERS_QRTZ_JOB_DETAILS]
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[FK_QRTZ_JOB_LISTENERS_QRTZ_JOB_DETAILS]') AND parent_object_id = OBJECT_ID(N'[dbo].[QRTZ_JOB_LISTENERS]'))
ALTER TABLE [dbo].[QRTZ_JOB_LISTENERS]  WITH CHECK ADD  CONSTRAINT [FK_QRTZ_JOB_LISTENERS_QRTZ_JOB_DETAILS] FOREIGN KEY([JOB_NAME], [JOB_GROUP])
REFERENCES [dbo].[QRTZ_JOB_DETAILS] ([JOB_NAME], [JOB_GROUP])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[QRTZ_JOB_LISTENERS] CHECK CONSTRAINT [FK_QRTZ_JOB_LISTENERS_QRTZ_JOB_DETAILS]
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[FK_QRTZ_JOB_DATA_MAP_QRTZ_JOB_DETAIL]') AND parent_object_id = OBJECT_ID(N'[dbo].[QRTZ_JOB_DATA_MAP]'))
ALTER TABLE [dbo].[QRTZ_JOB_DATA_MAP]  WITH CHECK ADD  CONSTRAINT [FK_QRTZ_JOB_DATA_MAP_QRTZ_JOB_DETAIL] FOREIGN KEY([JOB_NAME], [JOB_GROUP])
REFERENCES [dbo].[QRTZ_JOB_DETAILS] ([JOB_NAME], [JOB_GROUP])
GO
ALTER TABLE [dbo].[QRTZ_JOB_DATA_MAP] CHECK CONSTRAINT [FK_QRTZ_JOB_DATA_MAP_QRTZ_JOB_DETAIL]
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[FK_QRTZ_CRON_TRIGGERS_QRTZ_TRIGGERS]') AND parent_object_id = OBJECT_ID(N'[dbo].[QRTZ_CRON_TRIGGERS]'))
ALTER TABLE [dbo].[QRTZ_CRON_TRIGGERS]  WITH CHECK ADD  CONSTRAINT [FK_QRTZ_CRON_TRIGGERS_QRTZ_TRIGGERS] FOREIGN KEY([TRIGGER_NAME], [TRIGGER_GROUP])
REFERENCES [dbo].[QRTZ_TRIGGERS] ([TRIGGER_NAME], [TRIGGER_GROUP])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[QRTZ_CRON_TRIGGERS] CHECK CONSTRAINT [FK_QRTZ_CRON_TRIGGERS_QRTZ_TRIGGERS]
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[FK_QRTZ_TRIGGER_LISTENERS_QRTZ_TRIGGERS]') AND parent_object_id = OBJECT_ID(N'[dbo].[QRTZ_TRIGGER_LISTENERS]'))
ALTER TABLE [dbo].[QRTZ_TRIGGER_LISTENERS]  WITH CHECK ADD  CONSTRAINT [FK_QRTZ_TRIGGER_LISTENERS_QRTZ_TRIGGERS] FOREIGN KEY([TRIGGER_NAME], [TRIGGER_GROUP])
REFERENCES [dbo].[QRTZ_TRIGGERS] ([TRIGGER_NAME], [TRIGGER_GROUP])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[QRTZ_TRIGGER_LISTENERS] CHECK CONSTRAINT [FK_QRTZ_TRIGGER_LISTENERS_QRTZ_TRIGGERS]
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[FK_QRTZ_SIMPLE_TRIGGERS_QRTZ_TRIGGERS]') AND parent_object_id = OBJECT_ID(N'[dbo].[QRTZ_SIMPLE_TRIGGERS]'))
ALTER TABLE [dbo].[QRTZ_SIMPLE_TRIGGERS]  WITH CHECK ADD  CONSTRAINT [FK_QRTZ_SIMPLE_TRIGGERS_QRTZ_TRIGGERS] FOREIGN KEY([TRIGGER_NAME], [TRIGGER_GROUP])
REFERENCES [dbo].[QRTZ_TRIGGERS] ([TRIGGER_NAME], [TRIGGER_GROUP])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[QRTZ_SIMPLE_TRIGGERS] CHECK CONSTRAINT [FK_QRTZ_SIMPLE_TRIGGERS_QRTZ_TRIGGERS]
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[gadgets]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[gadgets](
	[caption] [nvarchar](50) NOT NULL,
	[id] [int] NOT NULL,
	[report_file] [nvarchar](50) NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[gadget_user_config]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[gadget_user_config](
	[user_id] [int] NOT NULL,
	[id] [int] NOT NULL,
	[col] [int] NOT NULL,
	[height] [int] NOT NULL,
	[is_visible] [int] NOT NULL,
	[is_minimized] [int] NOT NULL,
	[refresh_rate] [int] NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[gadget_user_params]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[gadget_user_params](
	[user_id] [int] NOT NULL,
	[id] [int] NOT NULL,
	[name] [nvarchar](50) NOT NULL,
	[value] [nvarchar](50) NOT NULL,
	[type] [nvarchar](50) NULL
) ON [PRIMARY]
END