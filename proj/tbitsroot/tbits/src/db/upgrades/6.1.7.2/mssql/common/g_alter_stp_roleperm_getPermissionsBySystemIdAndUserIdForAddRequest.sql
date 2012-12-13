if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[stp_roleperm_getPermissionsBySystemIdAndUserIdForAddRequest]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[stp_roleperm_getPermissionsBySystemIdAndUserIdForAddRequest]
GO

set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO


create procedure [dbo].[stp_roleperm_getPermissionsBySystemIdAndUserIdForAddRequest]
(
	@systemId INT, 
	@userId INT
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

DECLARE @privateFieldId INT
SELECT @privateFieldId = ISNULL(field_id, 0) FROM fields WHERE sys_id = @systemId AND name = 'is_private'

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
	case sum( p.pEmailView)
	when 0 then 0 
	else 8 
	end 'permission'
FROM
	permissions p
	JOIN
	(
	/*
	 * Get the permissions the user gets by virtue of being a user of the system and 
	 * being a logger of the request.
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
		(
			rp.role_id = 1 OR -- User Role.
			rp.role_id = 2  -- Logger Role.
		)
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
	) t
	ON p.permission = t.gpermissions 
GROUP BY t.name, t.field_id

UNION

/*
 * Get the list of application specific roles the user is present in.
 */
SELECT
	CASE rolename
	WHEN 'Analyst' THEN '__ROLE_ANALYST__'
	WHEN 'Admin' THEN '__ADMIN__'
	WHEN 'PermissionAdmin' THEN '__PERMISSIONADMIN__'
	ELSE rolename
	END,
	-1, 
	-1
FROM
	roles_users ru
	JOIN roles r
	ON r.sys_id = ru.sys_id AND r.role_id = ru.role_id AND ru.is_active = 1
WHERE
	ru.sys_id = @systemId AND	
	(
		ru.user_id = @userId OR
		ru.user_id IN (SELECT mailListId FROM #resultSet)
	)
	AND
	r.rolename in ('Analyst', 'Admin', 'PermissionAdmin')

UNION

/*
 * Check if the user is a part of super user list.
 */
SELECT
	'__SUPER_USER__', -1, -1
FROM
	super_users
WHERE
	user_id = @userId aND
	is_active = 1

UNION

/*
 * Check the contextual roles in this BA that have permission to view private requests.
 */
SELECT
	CASE rolename
	WHEN 'Logger' THEN '__LOGGER_PRIVATE__'
	WHEN 'Assignee' THEN '__ASSIGNEE_PRIVATE__'
	WHEN 'Subscriber' THEN '__SUBSCRIBER_PRIVATE__'
	ELSE rolename
	END,
	-1, 
	gpermissions
FROM
	roles_permissions rp
	JOIN roles r
	ON rp.sys_id = r.sys_id AND rp.role_id = r.role_id
WHERE
	rp.sys_id = @systemId AND
	rp.field_id = @privateFieldId AND
	r.rolename IN ('Logger', 'Assignee', 'Subscriber')

/*
 * Finally drop the temp table used for holding the mailing lists this user is part of directly or indirectly.
 */
DROP TABLE #resultSet
