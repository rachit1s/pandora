/****** Object:  StoredProcedure [dbo].[stp_admin_delete_escalation_condition]    Script Date: 02/02/2009 20:16:58 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_admin_delete_escalation_condition] 
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
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_admin_delete_escalation_heirarchy] 
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
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_admin_insert_escalation_condition] 
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
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_admin_insert_escalation_heirarchy] 
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
GO
SET QUOTED_IDENTIFIER ON
GO
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

/****** Object:  StoredProcedure [dbo].[stp_tr_template_insert]    Script Date: 02/02/2009 20:27:00 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_tr_template_insert](
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
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[stp_tr_template_lookupBySystemIdAndTemplateName]
(
	@systemId INT,
	@templateName VARCHAR(50)
)
AS
BEGIN
SELECT 
    *
FROM
	transmittal_templates
WHERE
	sys_id = @systemId and 
	template_name = @templateName
END

/****** Object:  StoredProcedure [dbo].[stp_tbits_insertTransmittalMapping]    Script Date: 02/02/2009 21:10:15 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
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

