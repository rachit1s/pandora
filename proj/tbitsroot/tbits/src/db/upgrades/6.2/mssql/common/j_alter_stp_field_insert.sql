set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
go



ALTER PROCEDURE [dbo].[stp_field_insert]
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
	@tracking_option 	INT,
	@permission		INT,
	@regex			VARCHAR(7999),
	@is_dependent	BIT,
	@display_order	INT,
	@display_group	INT
)
AS
DECLARE @fieldID int
SELECT 	@fieldID = (ISNULL(MAX(field_id), 0) + 1) from fields where sys_id = @sys_id
INSERT INTO fields
(
	sys_id,
	field_id,
	name,
	display_name,
	description,
	data_type_id,
	is_active,
	is_extended,
	is_private,
	tracking_option,
	permission,
	regex,
	is_dependent,
	display_order,
	display_group
)
VALUES
(
	@sys_id,
	@fieldID,
	@name,
	@display_name,
	@description,
	@data_type_id,
	@is_active,
	@is_extended,
	@is_private,
	@tracking_option,
	@permission,
	@regex,
	@is_dependent,
	@display_order,
	@display_group
)
DECLARE @i INT
DECLARE @maxRoleId INT
set @i = 1
select @maxRoleId = max(role_id) from roles
WHILE (@i < @maxRoleId)
BEGIN
--	print 'INSERT INTO roles_permissions values(' 
--			+ cast(@sys_id as varchar(20)) + ',' 
--			+ cast(@i as varchar(20)) + ',' + 
--			+ cast(@fieldId as varchar(20)) + ' , 4, 0)';
	INSERT INTO roles_permissions (sys_id, role_id, field_id, gpermissions, dpermissions) values(@sys_id, @i, @fieldId, 12, 0)
--	print 'FINISHED INSERT'
	set @i = @i +1
END

select @fieldID as 'field_id' -- returning the fieldID
GO
