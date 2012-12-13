set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
go



ALTER PROCEDURE [dbo].[stp_admin_getUserRolesBySysIdAndUserId] 
(
	@sys_id int ,
	@user_id int 
)
AS
SELECT 
	r.role_id, 
	r.rolename, 
	ISNULL (ru.user_id, -1)as 'user_id'
FROM 
	roles r
	LEFT join roles_users ru
	on r.sys_id = ru.sys_id and r.role_id = ru.role_id and ru.user_id = @user_id AND ru.is_active = 1
WHERE
	r.field_id=0 -- this condition basically implies that the role is not dynamic and has no association to any field.	
	and r.sys_id = @sys_id 
	-- and r.role_id > 6 -- this condition is no longer required
ORDER BY r.role_id



