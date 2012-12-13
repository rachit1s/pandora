SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[transmittal_ba_mapping]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[transmittal_ba_mapping](
	[sys_id] [int] NOT NULL,
	[sys_prefix] [nvarchar](50) NOT NULL,
	[transmittal_sys_prefix] [nvarchar](50) NULL,
	[latest_ba_sys_prefix] [nvarchar](50) NULL
) ON [PRIMARY]
END
GO

/****** Object:  Table [dbo].[ba_max_transmittal_ids]    Script Date: 07/27/2009 10:57:49 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ba_max_transmittal_ids](
	[sys_id] [int] NOT NULL,
	[max_transmittal_id] [int] NOT NULL
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[transmittal_templates]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[transmittal_templates](
	[sys_id] [int] NOT NULL,
	[template_id] [int] NOT NULL,
	[template_name] [varchar](50) NOT NULL,
	[template_file_name] [varchar](50) NOT NULL,
	[to_list] [varchar](3000) NULL,
	[cc_list] [varchar](3000) NULL
) ON [PRIMARY]
END
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_transmittal_getMaxTransmittalId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author:		Lokesh
-- Create date: 16-Apr-2009
-- Description:	Fetch max transmittal id.
-- =============================================
CREATE PROCEDURE [dbo].[stp_transmittal_getMaxTransmittalId]
	-- Add the parameters for the stored procedure here
	@sysId INT
AS

DECLARE @max_id int
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
	BEGIN TRANSACTION
	SELECT @max_id = ISNULL(max_transmittal_id, 0) + 1
	FROM ba_max_transmittal_ids
	WHERE sys_id=@sysId

	UPDATE ba_max_transmittal_ids
	SET max_transmittal_id = @max_id
	WHERE sys_id=@sysId

	SELECT max_transmittal_id from ba_max_transmittal_ids
	COMMIT TRAN
END


' 
END
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_get_requestIdByDocNo]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author:		Lokesh
-- Create date: 05/08/2008
-- Description:	
-- =============================================
CREATE PROCEDURE [dbo].[stp_get_requestIdByDocNo]
	-- Add the parameters for the stored procedure here
	@systemId INT,
	@fieldId  INT,
	@fieldValue  nvarchar(3500)	
AS
BEGIN	
	SELECT request_id from requests_ex 
where 
	sys_id = @systemId and 
	field_id = @fieldId and 
	varchar_value = @fieldValue
END

' 
END
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_get_mapped_business_areas]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
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

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tbits_insertTransmittalMapping]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author:		<Author,,Lokesh>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[stp_tbits_insertTransmittalMapping]  
	-- Add the parameters for the stored procedure here
	@sysId				INT, 
	@dcrSysPrefix		NVARCHAR(32),
	@dtnSysPrefix		NVARCHAR(32),
	@latestSysPrefix	NVARCHAR(32)	

AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
	IF EXISTS(SELECT * FROM transmittal_ba_mapping WHERE sys_id=@sysId)
		UPDATE transmittal_ba_mapping
			SET
				transmittal_sys_prefix = @dtnSysPrefix, 
				latest_ba_sys_prefix = @latestSysPrefix
			WHERE
				sys_id = @sysId
	ELSE
		INSERT INTO transmittal_ba_mapping
		(
			sys_id,
			sys_prefix,
			transmittal_sys_prefix,
			latest_ba_sys_prefix
		)
		VALUES
		(
			@sysId,
			@dcrSysPrefix,
			@dtnSysPrefix, 
			@latestSysPrefix
		)
END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tr_template_insert]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[stp_tr_template_insert](
	@sysId				INT,
	@templateName		VARCHAR(50),
	@fileName			VARCHAR(50),
	@toList				VARCHAR(3000),
	@ccList				VARCHAR(3000)
)
AS

BEGIN
DECLARE @templateId INT
SET @templateId = (SELECT MAX(template_id) from transmittal_templates) + 1

INSERT INTO transmittal_templates (
	sys_id, 
	template_id,
	template_name,
	template_file_name,
	to_list,
	cc_list
)
values(
	@sysId,
	@templateId,
	@templateName,
	@fileName,
	@toList,
	@ccList
)
END

/****** Object:  StoredProcedure [dbo].[stp_tr_template_lookupBySystemIdAndTemplateName]    Script Date: 02/02/2009 20:27:24 ******/
SET ANSI_NULLS ON
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_tr_template_lookupBySystemIdAndTemplateName]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
CREATE PROCEDURE [dbo].[stp_tr_template_lookupBySystemIdAndTemplateName]
(
	@systemId INT,
	@templateName VARCHAR(50)
)
AS
SELECT 
    *
FROM
	transmittal_templates
WHERE
	sys_id = @systemId and 
	template_name = @templateName

' 
END
GO
