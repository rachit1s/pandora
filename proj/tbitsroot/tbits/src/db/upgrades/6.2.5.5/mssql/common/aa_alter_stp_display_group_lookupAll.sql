
/****** Object:  StoredProcedure [dbo].[stp_display_group_lookupAll]    Script Date: 01/17/2012 16:17:47 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Sandeep Giri
-- Create date: 3 Jan 2007
-- Description:	Returns all display groups
-- =============================================
ALTER PROCEDURE [dbo].[stp_display_group_lookupAll] 
	-- Add the parameters for the stored procedure here
AS
BEGIN
	select id, display_name, display_order, is_active,is_default from display_groups
END
