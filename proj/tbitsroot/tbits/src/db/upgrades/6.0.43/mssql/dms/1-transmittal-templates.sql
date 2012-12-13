/****** Object:  Table [dbo].[transmittal_templates]    Script Date: 07/14/2008 19:31:32 ******/

CREATE TABLE [dbo].[transmittal_templates](
	[sys_id] [int] NOT NULL,
	[template_id] [int] NOT NULL,
	[template_name] [varchar](50)  NOT NULL,
	[template_file_name] [varchar](50)  NOT NULL,
	[to_list] [varchar](3000) NULL,
	[cc_list] [varchar](3000)  NULL
)


/******  StoredProcedure [dbo].[stp_tr_template_lookupBySystemIdAndTemplateName]    Script Date: 09/10/2008 13:38:59 ******/
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

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_get_requestIdByDocNo] 
	(
	@systemId INT,
	@fieldId  INT,
	@fieldValue  nvarchar(3500)	
)
AS
BEGIN	
	SELECT request_id from requests_ex 
where 
	sys_id = @systemId and 
	field_id = @fieldId and 
	varchar_value = @fieldValue
END

