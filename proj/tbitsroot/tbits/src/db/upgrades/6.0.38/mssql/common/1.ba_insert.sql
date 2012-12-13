SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_ba_insert] 
	-- Add the parameters for the stored procedure here	
	@name 			NVARCHAR(128),
	@display_name 		NVARCHAR(128),
	@email 			NVARCHAR(255),
	@sys_prefix 		NVARCHAR(32),
	@description 		NVARCHAR(255),
	@type 			NVARCHAR(32),
	@location 		NVARCHAR(32),
	@date_created 		DATETIME,
	@max_request_id 	INT,
	@max_email_actions 	INT,
	@is_email_active 	BIT,
	@is_active 		BIT,
	@is_private 		BIT,
	@sys_config 		NTEXT,
	@field_config 		NTEXT,
	@rSystemId     INT OUTPUT
AS
BEGIN

DECLARE @systemId INT

SELECT 
	@systemId = ISNULL(max(sys_id), 0) 
FROM 
	business_areas

SELECT @systemId = @systemId + 1

-- Insert statements for procedure here
INSERT INTO business_areas 
( 
	sys_id, 
	name, 
	display_name, 
	email, 
	sys_prefix, 
	description, 
	type, 
	location, 
	max_request_id, 
	max_email_actions, 
	is_email_active, 
	is_active, 
	date_created, 
	is_private, 
	sys_config, 
	field_config	
)
VALUES
(
	@systemId,
	@name,
	@display_name,
	@email,
	@sys_prefix,
	@description,
	@type,
	@location,	
	@max_request_id,
	@max_email_actions,
	@is_email_active,
	@is_active,
	@date_created,
	@is_private,
	@sys_config,
	@field_config
)
SELECT @rSystemId = @systemId
END

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_roles_insert_existing_role] 
	-- Add the parameters for the stored procedure here
	@sys_id int,
	@role_id int,
	@rolename nvarchar(50),
	@description nvarchar(250)
AS
BEGIN
	INSERT INTO [dbo].[roles]
           ([sys_id]
           ,[role_id]
           ,[rolename]
           ,[description])
     VALUES
		(@sys_id,@role_id,@rolename,@description)
END

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_field_insertWithExistingFieldId]
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
	@field_id,
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
SELECT @i = 1
SELECT @maxRoleId = max(role_id) from roles
WHILE (@i < @maxRoleId)
BEGIN
	print 'INSERT INTO roles_permissions values(' 
			+ cast(@sys_id as varchar(20)) + ',' 
			+ cast(@i as varchar(20)) + ',' + 
			+ cast(@field_id as varchar(20)) + ' , 4, 0)';
	INSERT INTO roles_permissions (sys_id, role_id, field_id, gpermissions, dpermissions) values(@sys_id, @i, @field_id, 4, 0)
	print 'FINISHED INSERT'
	SELECT @i = @i +1
END

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_roles_users_update] 
(
	@sys_id 	INT,
	@role_id 	INT,
	@user_id 	INT,
	@is_active	BIT
)
AS
UPDATE roles_users 
SET	
	role_id = @role_id,
	is_active = @is_active
WHERE 
	sys_id = @sys_id and
	user_id = @user_id
GO
ALTER TABLE versions
ADD [file_action] [varchar](10) NULL

