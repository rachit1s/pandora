/****** Object:  Table [dbo].[reports]    Script Date: 11/29/2008 16:03:14 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[reports](
	[report_id] [int] NOT NULL,
	[report_name] [nvarchar](3000) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[description] [ntext] COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[file_name] [nvarchar](3000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[is_private] [bit] NOT NULL,
	[is_enabled] [bit] NOT NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]


/****** Object:  Table [dbo].[report_roles]    Script Date: 11/29/2008 16:00:51 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[report_roles](
	[sys_id] [int] NOT NULL,
	[report_id] [int] NOT NULL,
	[role_id] [int] NOT NULL
) ON [PRIMARY]


/****** Object:  Table [dbo].[report_specific_users]    Script Date: 11/29/2008 16:02:37 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[report_specific_users](
	[report_id] [int] NOT NULL,
	[user_id] [int] NOT NULL,
	[is_included] [bit] NOT NULL
) ON [PRIMARY]


/****** Object:  StoredProcedure [dbo].[stp_report_insert]    Script Date: 11/29/2008 16:13:04 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_report_insert] 
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
	'false'
)
SELECT @reportId = @rep_Id
END

/****** Object:  StoredProcedure [dbo].[stp_report_delete]    Script Date: 09/17/2008 16:51:29 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

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
GO
SET QUOTED_IDENTIFIER ON
GO

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
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_report_role_insert] 
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
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_report_specific_user_insert] 
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
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_report_lookupByReportId]
(
	@reportId int
)
AS
	SELECT * from reports where report_id = @reportId


/****** Object:  StoredProcedure [dbo].[stp_report_lookupBySysIdAndRoleId]    Script Date: 11/29/2008 16:19:01 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[stp_report_lookupBySysIdAndRoleId]
(@sys_id int,
 @role_id int)
as
begin
select * from reports 
where report_id in (select report_id from report_roles 
where sys_id = @sys_id and
      role_id = @role_id)
end

GO
IF NOT EXISTS(SELECT * FROM captions_properties WHERE sys_id=0 AND name='captions.all.my_reports')
BEGIN
INSERT INTO captions_properties 
	(sys_id,name,value) 
VALUES  (0,'captions.all.my_reports','My Reports')
END







