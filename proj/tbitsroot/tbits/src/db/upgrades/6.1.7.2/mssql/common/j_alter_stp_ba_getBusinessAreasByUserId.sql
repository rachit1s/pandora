if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[stp_ba_getBusinessAreasByUserId]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[stp_ba_getBusinessAreasByUserId]
GO

set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
go

create PROCEDURE [dbo].[stp_ba_getBusinessAreasByUserId]
(
	@userId		INT
)
AS

/*
dereference all the mailing lists in which this user is included
*/
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

DECLARE @baFieldId INT
DECLARE @ipFieldId INT
SELECT @baFieldId = field_id FROM fields WHERE name = 'sys_id'
SELECT @ipFieldId = field_id FROM fields WHERE name = 'is_private'
/*
 * Script that lists out the Business area visible to a list of roles specified.
 */
SELECT 
	*
FROM 
	business_areas 
WHERE 
	sys_id IN
(
	SELECT 
		ba.sys_id
	FROM
		business_areas ba
		JOIN roles_permissions rp 			-- This is for checking the user/logger permission on sys_id field.
		ON rp.sys_id = ba.sys_id
		JOIN roles_permissions rpp		-- This is for checking the user/logger permission on is_private field.
		ON rp.sys_id = rpp.sys_id AND rp.field_id = @baFieldId AND rpp.field_id = @ipFieldId
		LEFT JOIN roles_users ru		-- This is for considering the user's BA specific roles.
		ON rp.sys_id = ru.sys_id AND ( ru.user_id = @userId OR ru.user_id in ( select mailListId from #resultSet ) ) AND ru.is_active = 1
		LEFT JOIN roles_permissions rpba	-- This is for checking the BA's Specific Role's permission on sys_id field.
		ON rpba.sys_id = ru.sys_id AND rpba.role_id = ru.role_id AND rpba.field_id = @baFieldId
		LEFT JOIN roles_permissions rpip	-- This is for checking the BA's Specific Role's permission on is_private field.
		ON rpip.sys_id = ru.sys_id AND rpip.role_id = ru.role_id AND rpip.field_id = @ipFieldId
	WHERE
		ba.is_active = 1 AND 
		rp.role_id in (1) AND
		rpp.role_id in (1) AND 
		(
			(
				-- If BA is normal, then VIEW permission is required on sys_id 
				-- by virtue either of User/Logger role OR BA Role the user is associated with.
				ba.is_private = 0 AND ((rp.gpermissions & 4) <> 0  OR (rpba.gpermissions & 4) <> 0 )
			)
			OR
			(
				-- If BA is Private, then VIEW permission is required on sys_id along with VIEW permission on private field
				-- by virtue either of User/Logger role OR of BA Role the user is associated with.
				ba.is_private = 1 AND 
				(
					(
						(rp.gpermissions & 4) <> 0 OR 
						(rpba.gpermissions & 4) <> 0
					)AND 
					(
						(rpp.gpermissions & 4) <> 0 OR
						(rpip.gpermissions & 4) <> 0
					)
				)
			)
		)
)
ORDER BY display_name

DROP TABLE #resultSet
