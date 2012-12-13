if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[stp_roleperm_getPermissionsByUserId]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[stp_roleperm_getPermissionsByUserId] 
GO

set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
go

create procedure [dbo].[stp_roleperm_getPermissionsByUserId] 
(
	@userId 	INT,
	@prefixList 	VARCHAR(7999)
)
AS
DECLARE @privateFieldId INT
SELECT @privateFieldId = ISNULL(field_id, 0) FROM fields WHERE name = 'is_private'
DECLARE @query VARCHAR(7999)
SELECT @query = 
'

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
	user_id = ' + CONVERT( VARCHAR, @userId ) + '

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
	ba.sys_prefix ''SysPrefix'',
	f.sys_id ''SystemId'',
	f.field_id ''FieldId'',
	f.name ''FieldName'',
	case sum(CONVERT(INT, padd))
		when 0 then 0
		else 1
	end
	+
	case sum(CONVERT(INT, pchange))
		when 0 then 0
		else 2
	end
	+
	case sum(CONVERT(INT, pview))
		when 0 then 0
		else 4
	end + 
	case sum( convert(int,pEmailView))
		 when 0 then 0 
		else 8 
	end 
	''Permission''
FROM 
	fields f
	JOIN business_areas ba
	ON f.sys_id = ba.sys_id
	LEFT JOIN roles_permissions rp 
	ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id AND f.is_active = 1 
	LEFT JOIN roles_users ru
	ON ru.sys_id = rp.sys_id AND ru.role_id = rp.role_id AND (ru.user_id = ' + convert(varchar, @userId) + ' OR ru.user_id in (select mailListId from #resultSet)) AND ru.is_active = 1
	JOIN permissions p
	ON rp.gpermissions = p.permission
WHERE 
	ba.sys_prefix in (' + @prefixList + ') AND 
	f.is_extended = 0 AND
	f.is_active = 1 AND
	(
		rp.role_id IN (1) OR
		rp.role_id = ru.role_id
	)
group by ba.sys_prefix, f.sys_id, f.name, f.field_id
UNION
SELECT
	ba.sys_prefix ''SysPrefix'',
	rp.sys_id ''SystemId'',
	-1 ''FieldId'', 
	''__LOGGER_PRIVATE__'' ''FieldName'', 
	case (gpermissions & 4) 
	  when 0 then 0
	  else 4
	end ''Permission''
FROM
	roles_permissions rp
	JOIN business_areas ba
	ON rp.sys_id = ba.sys_id
WHERE
	ba.sys_prefix in (' + @prefixList + ') AND 
	role_id = 2 AND -- LOGGER ROLE
	field_id = ' + CONVERT( VARCHAR, @privateFieldId) + '
UNION
SELECT
	ba.sys_prefix ''SysPrefix'',
	rp.sys_id ''SystemId'',
	-1 ''FieldId'', 
	''__ASSIGNEE_PRIVATE__'' ''FieldName'', 
	case (gpermissions & 4) 
	  when 0 then 0
	  else 4
	end ''Permission''
FROM
	roles_permissions rp
	JOIN business_areas ba
	ON rp.sys_id = ba.sys_id
WHERE
	ba.sys_prefix in (' + @prefixList + ') AND 
	role_id = 3 AND -- ASSIGNEE ROLE
	field_id = ' + CONVERT( VARCHAR, @privateFieldId) + '
UNION
SELECT
	ba.sys_prefix ''SysPrefix'',
	rp.sys_id ''SystemId'',
	-1 ''FieldId'', 
	''__SUBSCRIBER_PRIVATE__'' ''FieldName'', 
	case (gpermissions & 4) 
	  when 0 then 0
	  else 4
	end ''Permission''
FROM
	roles_permissions rp
	JOIN business_areas ba
	ON rp.sys_id = ba.sys_id
WHERE
	ba.sys_prefix in (' + @prefixList + ') AND 
	role_id = 4 AND -- SUBSCRIBER ROLE
	field_id = ' + CONVERT( VARCHAR, @privateFieldId) + ' 
ORDER by  SystemId, FieldId, FieldName, Permission

DROP TABLE #resultSet

'
print @query
EXEC (@query)
