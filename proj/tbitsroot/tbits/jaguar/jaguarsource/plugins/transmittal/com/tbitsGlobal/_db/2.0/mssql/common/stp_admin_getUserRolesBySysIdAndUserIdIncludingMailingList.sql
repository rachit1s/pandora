
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[stp_admin_getUserRolesBySysIdAndUserIdIncludingMailingList]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[stp_admin_getUserRolesBySysIdAndUserIdIncludingMailingList] 
(
	@sys_id int,
	@user_id int 
)
AS
SELECT 
	r.role_id, 
	r.rolename, 
	ISNULL (ru.user_id, -1)as ''user_id''
FROM 
	roles r
	LEFT join roles_users ru
	on r.sys_id = ru.sys_id and r.role_id = ru.role_id AND ru.is_active = 1
	LEFT join mail_list_users mlu
	on ru.user_id = mlu.mail_list_id
WHERE
	r.field_id=0 -- this condition basically implies that the role is not dynamic and has no association to any field.	
	and r.sys_id = @sys_id and (ru.user_id = @user_id or mlu.user_id=@user_id)
	-- and r.role_id > 6 -- this condition is no longer required
ORDER BY r.role_id
'
END
