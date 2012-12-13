/****** Object:  StoredProcedure [dbo].[stp_barule_insert]    Script Date: 09/13/2008 11:51:19 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_barule_insert] 
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

/****** Object:  StoredProcedure [dbo].[stp_wr_insert] **********/
SET ANSI_NULLS ON

set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_wr_insert] 
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

