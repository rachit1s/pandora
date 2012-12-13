
/****** Object:  StoredProcedure [dbo].[stp_field_update]    Script Date: 04/19/2012 10:45:06 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER PROCEDURE [dbo].[stp_field_update] 
(
	@sys_id 		INT,	
	@field_id 		INT,
	@name 			NVARCHAR(128),
	@display_name 		NVARCHAR(128),
	@description 		NVARCHAR(128),
	@data_type_id 		INT,
	@is_active 		BIT,
	@is_extended 		BIT,
	@is_private 		INT,
	@tracking_option 	INT,
	@permission 		INT,
	@regex 			NVARCHAR(2048),
	@is_dependent	BIT,
	@display_order	INT,
	@display_group	INT,
	@error  NVARCHAR(2048)
)
AS
UPDATE fields
SET
	name 			= @name,
	display_name 		= @display_name,
	description 		= @description,
	data_type_id 		= @data_type_id,
	is_active 		= @is_active,
	is_extended 		= @is_extended,
	is_private 		= @is_private,
	tracking_option 	= @tracking_option,
	permission 		= @permission,
	regex 			= @regex,
	is_dependent	= @is_dependent,
	display_order	= @display_order,
	display_group	= @display_group,
	error            = @error
WHERE 
        field_id = @field_id AND 
        sys_id = @sys_id
