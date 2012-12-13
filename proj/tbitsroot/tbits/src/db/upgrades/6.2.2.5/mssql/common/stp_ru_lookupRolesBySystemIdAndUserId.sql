SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO


CREATE PROCEDURE [dbo].[stp_ru_lookupRolesBySystemIdAndUserId]
(
	@sysId		int,
	@userId 	int
)
AS
SELECT r.* FROM roles_users ru
JOIN roles r 
ON r.sys_id = ru.sys_id and r.role_id = ru.role_id
WHERE 
	ru.sys_id = @sysId AND
	ru.user_id = @userId

