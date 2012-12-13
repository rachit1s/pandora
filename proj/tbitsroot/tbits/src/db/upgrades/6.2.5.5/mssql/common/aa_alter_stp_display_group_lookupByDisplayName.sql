
/****** Object:  StoredProcedure [dbo].[stp_display_group_lookupByDisplayName]    Script Date: 01/17/2012 16:17:49 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

ALTER PROCEDURE [dbo].[stp_display_group_lookupByDisplayName] 
	-- Add the parameters for the stored procedure here
	@displayName 	VARCHAR(128)
AS
BEGIN
	select id, display_name, display_order, is_active, is_default 
	from display_groups
	where display_name = @displayName
END

/****** Object:  StoredProcedure [dbo].[stp_field_insertWithExistingFieldId]    Script Date: 11/11/2008 20:16:09 ******/
SET ANSI_NULLS ON
