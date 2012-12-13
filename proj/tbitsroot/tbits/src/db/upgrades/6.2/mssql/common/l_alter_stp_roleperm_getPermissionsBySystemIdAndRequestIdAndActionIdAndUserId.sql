set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
go




ALTER PROCEDURE [dbo].[stp_roleperm_getPermissionsBySystemIdAndRequestIdAndActionIdAndUserId]
(
	@systemId	int,
	@requestId 	int,
	@actionId	int,
	@userId		int
)
AS

CREATE TABLE #resultSet
(
	mailListId INT
)

/*
 * Get the mailing lists where the user is a direct member.
 */
INSERT INTO #resultSet(mailListId)
select distinct
	mail_list_id 
from 
	mail_list_users 
where 
	user_id = @userId

create table #iterationSet
(
	mailListId int
)

insert INTO #iterationSet select * FROM #resultSet

create table #iterationElement
(
	mailListId int
)

create table #iterationResult
(
	mailListId int
)

WHILE (EXISTS(SELECT * FROM #iterationSet))
BEGIN
	/*
	 * Get the mailing lists where the id in #iterationSet is a member.
         * which is already not part of #resultSet
	 */
	 
	insert into #iterationElement select top 1 * from #iterationSet 

	insert into #iterationResult select mail_list_users.mail_list_id
		FROM
			mail_list_users
			JOIN #iterationElement
			ON mail_list_users.user_id = #iterationElement.mailListId			
			LEFT JOIN #resultSet 
			ON mail_list_users.mail_list_id = #resultSet.mailListId
		WHERE
			#resultSet.mailListId IS NULL

	insert INTO #iterationSet select * FROM #iterationResult	

	insert INTO #resultSet select * FROM #iterationResult

	DELETE from #iterationSet
	where #iterationSet.mailListId in ( select #iterationElement.mailListId from #iterationElement ) -- join #iterationSet on #iterationElement.mailListId=#iterationSet.mailListId )

	DELETE FROM #iterationElement
	DELETE FROM #iterationResult
	
END

DROP TABLE #iterationSet
drop table #iterationElement
drop table #iterationResult

SELECT
	t.name 'name',
	t.field_id 'field_id',
	CASE SUM(p.padd)
	WHEN 0 then 0
	ELSE 1
	END + 
	CASE SUM(p.pchange)
	WHEN 0 then 0
	ELSE 2
	END + 
	CASE SUM(p.pview)
	WHEN 0 then 0
	ELSE 4
	END +
	CASE SUM(p.pEmailView)
	WHEN 0 then 0 
	else 8
	end 'permission'
FROM
	permissions p
	JOIN
	(
	/*
	 * Get the permissions the user gets by virtue of being a user of the system.
	 */
	SELECT
		f.name,
		f.field_id,
		rp.gpermissions
	FROM
		roles_permissions rp
		JOIN fields f
		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id
	WHERE
		rp.sys_id = @systemId AND 
		rp.role_id = 1
	
	UNION
	
	/*
	 * Get the permissions the user gets by virtue of being a part of the BA.
	 */
	SELECT
		f.name,
		f.field_id,
		rp.gpermissions
	FROM
		roles_permissions rp
		JOIN fields f
		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id
		JOIN roles_users ru
		ON ru.sys_id = rp.sys_id AND ru.role_id = rp.role_id
	WHERE
		rp.sys_id = @systemId AND
		(
			ru.user_id = @userId OR
			ru.user_id IN (SELECT mailListId FROM #resultSet)
		)
	UNION
	/*
	 * Get the permissions the user gets by virtue of being a part of this action of the request.
	 */
	SELECT
		f.name,
		f.field_id,
		rp.gpermissions
	FROM
		roles_permissions rp
		JOIN fields f
		ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id
		join roles rs
		on rs.sys_id=rp.sys_id and rs.role_id=rp.role_id
		JOIN action_users au
		ON au.sys_id = @systemId AND au.request_id = @requestId AND au.action_id = @actionId AND au.field_id = rs.field_id
	WHERE
		rp.sys_id = @systemId AND
		(
			au.user_id = @userId OR
			au.user_id IN (SELECT mailListId FROM #resultSet)
		)
	) t
	ON p.permission = t.gpermissions 
GROUP BY t.name, t.field_id

/*
 * Finally drop the temp table used for holding the mailing lists this user is part of directly or indirectly.
 */
DROP TABLE #resultSet
GO

