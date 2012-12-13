
/****** Object:  StoredProcedure [dbo].[stp_display_group_update]    Script Date: 01/17/2012 16:17:58 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
ALTER PROCEDURE [dbo].[stp_display_group_update]
	@sys_id int,
	@id int,
	@display_name varchar(50),
	@display_order int,
	@is_active bit,
	@is_default bit
AS

	update display_groups set display_name = @display_name, display_order = @display_order, is_active = @is_active, is_default=@is_default
	where id = @id
If @is_default = 1 
	BEGIN
		UPDATE display_groups SET is_default = 0 WHERE sys_id = @sys_id AND id != @id
	END 
ELSE
BEGIN 
		DECLARE @totalCount INT
		SELECT @totalCount = (SELECT COUNT(*) FROM display_groups WHERE sys_id = @sys_id )
		IF @totalCount= (SELECT COUNT(*) FROM display_groups WHERE sys_id = @sys_id  AND is_default=0)
		BEGIN
			UPDATE display_groups SET is_default = 1 WHERE sys_id = @sys_id  AND id = (SELECT MIN(id) FROM display_groups WHERE sys_id = @sys_id )
		END
END
SET QUOTED_IDENTIFIER OFF
