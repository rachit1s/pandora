
/****** Object:  StoredProcedure [dbo].[stp_display_group_insert]    Script Date: 01/02/2012 18:10:29 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author: Lokesh
-- Create date: 4 APR 2009
-- Description:	Inserts the display group
-- =============================================
ALTER PROCEDURE [dbo].[stp_display_group_insert]
	@sys_id int,
	@id int,
	@display_name varchar(50),
	@display_order int,
	@is_active bit,
	@is_default bit,
	@new_id int output
AS
BEGIN
	insert into display_groups (sys_id, display_name, display_order, is_active,is_default) 
	Values(@sys_id, @display_name, @display_order, @is_active,@is_default);
	select @new_id = @@IDENTITY;
END

/****** Object:  StoredProcedure [dbo].[stp_display_group_update]    Script Date: 04/10/2009 17:28:19 ******/
SET ANSI_NULLS ON
