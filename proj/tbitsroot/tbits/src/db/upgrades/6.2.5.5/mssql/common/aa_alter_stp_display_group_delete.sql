
/****** Object:  StoredProcedure [dbo].[stp_display_group_delete]    Script Date: 01/17/2012 16:14:49 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO


-- =============================================
-- Author:		<lokesh,giris>
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
	@is_default bit,
	@ret_value int output
AS
BEGIN
	delete from display_groups where id = @id;
	update fields set display_group = 1 where display_group = @id
	select @ret_value = 1;
END
