if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[stp_roleperm_getPermissionsBySystemIdAndRequestIdAndActionIdAndUserIdList]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[stp_roleperm_getPermissionsBySystemIdAndRequestIdAndActionIdAndUserIdList] 
GO

set ANSI_NULLS OFF
set QUOTED_IDENTIFIER OFF
GO



create PROCEDURE [dbo].[stp_roleperm_getPermissionsBySystemIdAndRequestIdAndActionIdAndUserIdList] 
(
	@sysId		int,
	@requestId 	int,
	@actionId	int,
	@userId	varchar(1024)
)
AS
declare @query varchar(7999)

select @query = 
'
create table #myUserList( userId int )

-- initialize the myUserList
insert into #myUserList 
	select u.user_id 
	from users u 
	where u.user_id in ( ' + @userId + ') 	

create table #completeMap( mailListId int, userId int )

DECLARE @currUserId int
DECLARE users_cursor CURSOR FOR
	SELECT userId FROM #myUserList

OPEN users_cursor

-- Perform the first fetch and store the values in variables.
-- Note: The variables are in the same order as the columns
-- in the SELECT statement. 

FETCH NEXT FROM users_cursor
INTO @currUserId

CREATE TABLE #resultSetPerUser( mailListId INT, userId int )

create table #iterationSet( mailListId int, userId int )

create table #iterationElement( mailListId int, userId int )

create table #iterationResult( mailListId int, userId int )

while( @@FETCH_STATUS = 0 )
begin
	INSERT INTO #resultSetPerUser(mailListId,userId)
	select distinct
		mail_list_id,user_id
	from 
		mail_list_users 
	where 
		user_id = @currUserId

	insert INTO #iterationSet(mailListId,userId) select mailListId,userId FROM #resultSetPerUser

	WHILE (EXISTS(SELECT * FROM #iterationSet)) -- not using fetch here to avoid any confusion
	BEGIN
		/*
		 * Get the mailing lists where the id in #iterationSet is a member.
		 * which is already not part of #resultSetPerUser
		 */	

		insert into #iterationElement(mailListId,userId) select top 1 mailListId,userId from #iterationSet 

		insert into #iterationResult(mailListId,userId) select mail_list_users.mail_list_id, @currUserId
			FROM
				mail_list_users
				JOIN #iterationElement
				ON mail_list_users.user_id = #iterationElement.mailListId
				LEFT JOIN #resultSetPerUser -- rspu
				ON mail_list_users.mail_list_id = #resultSetPerUser.mailListId
			WHERE
				#resultSetPerUser.mailListId IS NULL

		insert INTO #iterationSet(mailListId,userId) select ir.mailListId,ir.userId FROM #iterationResult ir

		insert INTO #resultSetPerUser(mailListId,userId) select ir.mailListId,ir.userId FROM #iterationResult ir

		DELETE from #iterationSet
		where #iterationSet.mailListId in ( select #iterationElement.mailListId from #iterationElement ) -- join #iterationSet on #iterationElement.mailListId=#iterationSet.mailListId )

		DELETE FROM #iterationElement
		DELETE FROM #iterationResult
	
	END

	insert into #completeMap(mailListId,userId) select rspu.mailListId,rspu.userId from #resultSetPerUser rspu
	
	FETCH NEXT FROM users_cursor -- get next userid
		INTO @currUserId

	-- clean up the tables
	delete from #iterationSet
	delete from #iterationElement
	delete from #iterationResult
	delete from #resultSetPerUser
end

CLOSE users_cursor
DEALLOCATE users_cursor
drop TABLE #iterationSet
drop table #iterationElement
drop table #iterationResult
drop table #resultSetPerUser

SELECT 
	f.name ''name'', 
	f.field_id  ''field_id'',
	padd,
	pchange,
	pview,	
	pEmailView,
	u.user_id "userId"
	into #tmp3
FROM
	fields f
	LEFT JOIN roles_permissions rp
	ON f.sys_id = rp.sys_id AND f.field_id = rp.field_id 
	LEFT JOIN users u
	ON u.user_id in (' + @userId + ') 
	JOIN permissions pm
	ON pm.permission = rp.gpermissions
WHERE
	f.sys_id = ' + CONVERT(varchar(10), @sysId) + ' AND
	(
		rp.role_id = 1
	)


Insert into #tmp3
SELECT 
	f.name  ''name'', 
	f.field_id  ''field_id'',
	padd,
	pchange,
	pview,
	pEmailView,
	u.user_id "userId"
FROM
	fields f
	LEFT JOIN roles_permissions rp
	ON f.sys_id = rp.sys_id AND f.field_id = rp.field_id 
	LEFT JOIN roles_users ru
	ON ru.sys_id = rp.sys_id AND ru.role_id = rp.role_id 
	LEFT JOIN #completeMap mlu
	ON mlu.mailListId = ru.user_id 
	LEFT JOIN users u
	ON u.user_id = ru.user_id or mlu.userId = u.user_id 
	JOIN permissions pm
	ON pm.permission = rp.gpermissions
WHERE
	f.sys_id = ' + CONVERT(varchar(10), @sysId) + ' AND
	(
		u.user_id in (' + @userId + ')
	) 

Insert into #tmp3
SELECT 
	f.name  ''name'', 
	f.field_id  ''field_id'',
	padd,
	pchange,
	pview,
	pEmailView,
	u.user_id "userId"
FROM
	fields f
	LEFT JOIN roles_permissions rp
	ON f.sys_id = rp.sys_id AND f.field_id = rp.field_id 
	LEFT JOIN action_users aq
	ON aq.sys_id = rp.sys_id AND aq.request_id = ' + CONVERT(varchar(10),@requestId) + ' AND aq.action_id = ' + CONVERT(varchar(10),@actionId) + '	and aq.user_type_id = rp.role_id  
	LEFT JOIN #completeMap mlu
	ON mlu.mailListId =aq.user_id
	LEFT JOIN users u
	ON u.user_id = aq.user_id or mlu.userId = u.user_id 
	JOIN permissions pm
	ON pm.permission = rp.gpermissions
WHERE
	f.sys_id = ' + CONVERT(varchar(10), @sysId) + ' AND
	(
		u.user_id in ('  + @userId  + ')
	) 


select userId, 
	case sum(CONVERT(INT,padd))
		when 0 then 0
		else 1
	end
	+
	case sum(CONVERT(INT, pchange))
		when 0 then 0
		else 2
	end
	+
	case sum(CONVERT(INT,pview))
		when 0 then 0
		else 4
	end 
	+
	case sum( CONVERT(INT,pEmailView) )
		 when 0 then 0
		 else 8
	end ''permission'',
	name,
	field_id
into #tmp4
from #tmp3
GROUP BY name, field_id,userId

select userId,field_id,permission,name
from #tmp4
order by userId,field_id

drop table #tmp3
drop table #tmp4
drop table #myUserList
drop table #completeMap
'

exec(@query)
