
/****** Object:  StoredProcedure [dbo].[stp_display_group_lookupBySystemIdAndDisplayName]    Script Date: 01/17/2012 16:17:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

ALTER PROCEDURE [dbo].[stp_display_group_lookupBySystemIdAndDisplayName] 
	-- Add the parameters for the stored procedure here
	@sys_id			INT,
	@displayName 	VARCHAR(128)
AS
BEGIN
	select sys_id, id, display_name, display_order, is_active ,is_default
	from display_groups
	where display_name = @displayName and sys_id = @sys_id
END
