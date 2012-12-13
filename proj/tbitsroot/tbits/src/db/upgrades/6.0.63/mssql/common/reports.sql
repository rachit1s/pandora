set ANSI_NULLS ON

set QUOTED_IDENTIFIER ON

GO

ALTER PROCEDURE [dbo].[stp_report_insert] 

	-- Add the parameters for the stored procedure here	

	@reportName 			NVARCHAR(3000),

	@reportDescription		NTEXT,

	@fileName			NVARCHAR(3000),

	@isPrivate			BIT,

	@isEnabled			BIT,

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

	@isEnabled

)

SELECT @reportId = @rep_Id

END



/****** Object:  StoredProcedure [dbo].[stp_report_delete]    Script Date: 09/17/2008 16:51:29 ******/

SET ANSI_NULLS ON




