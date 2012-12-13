set ANSI_NULLS ON
GO
set QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[stp_role_delete] 
(
	@systemId INT,
	@roleId	INT
)
AS
BEGIN
	DELETE FROM roles WHERE sys_id = @systemId AND role_id = @roleId
	DELETE FROM roles_users WHERE sys_id = @systemId AND role_id = @roleId
	DELETE FROM roles_permissions WHERE sys_id = @systemId AND role_id = @roleId	
END


