set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
go

-- =============================================
-- Author:		Sandeep Giri,Utkarsh
-- Create date: 05 Jan 07
-- Description:	Inserts a role. Returns the newly created role.
-- =============================================
ALTER PROCEDURE [dbo].[stp_roles_insert] 
	-- Add the parameters for the stored procedure here
	@sys_id int,
	@rolename nvarchar(50),
	@description nvarchar(250),
        @field_id int,
        @can_be_deleted int
AS
BEGIN
	DECLARE @role_id INT
	SELECT 
		@role_id = ISNULL(max(role_id), 0) 
	FROM 
		roles
	SELECT @role_id = @role_id + 1

	INSERT INTO [dbo].[roles]
           ([sys_id]
           ,[role_id]
           ,[rolename]
           ,[description]
           ,[field_id]
	   ,[can_be_deleted])
     VALUES
	(@sys_id,@role_id,@rolename,@description,@field_id,@can_be_deleted)
	select @role_id
END
GO
