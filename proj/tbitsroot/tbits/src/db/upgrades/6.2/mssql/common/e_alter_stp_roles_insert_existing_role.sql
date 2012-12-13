



set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
go

ALTER PROCEDURE [dbo].[stp_roles_insert_existing_role] 
	-- Add the parameters for the stored procedure here
	@sys_id int,
	@role_id int,
	@rolename nvarchar(50),
	@description nvarchar(250),
    @field_id int,
    @can_be_deleted int
AS
BEGIN
	INSERT INTO [dbo].[roles]
           ([sys_id]
           ,[role_id]
           ,[rolename]
           ,[description]
           ,[field_id]
           ,[can_be_deleted])
     VALUES
		(@sys_id,@role_id,@rolename,@description,@field_id,@can_be_deleted)
END

SET ANSI_NULLS ON
GO



