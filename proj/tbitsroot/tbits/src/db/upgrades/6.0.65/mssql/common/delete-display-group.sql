set ANSI_NULLS ON
GO
set QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
ALTER PROCEDURE [dbo].[stp_display_group_delete]
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
