
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

ALTER PROCEDURE [dbo].[stp_field_delete] 
(
	@sys_id			INT,
	@field_id		INT,
	@name			NVARCHAR(255),
	@display_name		NVARCHAR(255),
	@description		NVARCHAR(255),
	@data_type_id		INT,
	@is_active		BIT,
	@is_extended		BIT,
	@is_private		BIT,
	@tracking_option	INT,
	@permission		INT,
	@regex			VARCHAR(7999),
	@is_dependent		BIT,
	@display_order		INT,
	@display_group		INT,
	@returnValue		INT OUTPUT
)
AS
DECLARE @delete int
DECLARE @dataTypeId int

SELECT @dataTypeId = datatype_id from datatypes where name = 'type'

IF not exists(SELECT field_id from actions_ex where sys_id = @sys_id and field_id = @field_id)
BEGIN
	DELETE FROM fields 
	WHERE  
		sys_id = @sys_id AND 
		field_id = @field_id
	
	DELETE FROM roles_permissions 
	WHERE  
		sys_id = @sys_id AND
		field_id = @field_id
  
	IF(@data_type_id = @dataTypeId)
	BEGIN
		DELETE FROM types 
		WHERE
			sys_id = @sys_id AND
			field_id = @field_id	
		
		DELETE FROM type_users
		WHERE
			sys_id = @sys_id AND
			field_id = @field_id
	END

	DELETE FROM user_grid_col_prefs
	WHERE
		sys_id = @sys_id AND
		field_id = @field_id

	DELETE FROM tvn_folder_structure
	WHERE
		sys_id = @sys_id AND
		identifier = @name
	
	SELECT @returnValue = 1
END
ELSE
BEGIN
	SELECT @returnValue = 0
END
GO

SET ANSI_NULLS OFF
GO
SET QUOTED_IDENTIFIER OFF
GO

