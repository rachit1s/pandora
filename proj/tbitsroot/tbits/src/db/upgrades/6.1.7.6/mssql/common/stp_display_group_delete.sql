set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO

update fields 
set display_group = 1
where display_group not in (select id from display_groups) and display_group !=0 

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
	@ret_value int output
AS
BEGIN
	delete from display_groups where id = @id;
	update fields set display_group = 1 where display_group = @id
	select @ret_value = 1;
END
