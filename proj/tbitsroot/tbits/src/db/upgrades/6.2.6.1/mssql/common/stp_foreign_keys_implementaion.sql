CREATE  PROCEDURE [dbo].[stp_foreign_keys_implementaion]

AS

BEGIN

---Author:MM
--Create date: 27/12/2011
--Description:

----~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~--------------------

---The main objective of this stp is to Sanitize data and implement the missing FKs.
---Following tables and FKs have been covered:

--Type_users---->types(sys_id,field_id,type_id)         
--roles_permissions ----->fields(sys_id,field_id) 
--report_roles---- roles (sys_id,field_id)  
--report_specific_users ---- users(user_id)
--request_users----> request(sys_id,request_id)
--action_users----> actions(sys_id,request_id,action_id)
--user_passwords--> correction of self referencing FK, -- refers to ---> users(user_login)
--tags_requests-----> request(sys_id,request_id)
--requests---- users (user_id) 
--requests_ex----> fields(sys_id,field_id)





----FOR USER_PASSWORDS

---THIS PORTION TO BE RUN SEPARATELY, AS IT WILL MOST PROBABLY HAVE DUPLICATE RECORDS
---THERE IS SEPERATE SP WRITTEN FOR DELETING DUPLICATE USER_LOGINS.

--- Query to find duplicate user_login
BEGIN
IF EXISTS (SELECT MIN(user_id) MIN_ID,user_login FROM dbo.users 
	WHERE user_login IN (
	SELECT user_login No_of_times FROM dbo.users
	GROUP BY user_login
	HAVING COUNT(user_login)>1)
GROUP BY dbo.users.user_login
)
BEGIN
PRINT 'DUPLICATE USER_LOGINS EXITS, PLS SANITIZE THE DATA USING stp_user_repalce_srcUserId_with_destUserId'
END
END

BEGIN
IF EXISTS (SELECT MIN(user_id) MIN_ID,user_login FROM dbo.users 
	WHERE user_login IN (
	SELECT user_login No_of_times FROM dbo.users
	GROUP BY user_login
	HAVING COUNT(user_login)>1)
GROUP BY dbo.users.user_login)
BEGIN
PRINT '***** FOLLOWING ARE THE DUPLICATE USER_LOGINS ***** '

SELECT MIN(user_id) MIN_ID,user_login FROM dbo.users 
	WHERE user_login IN (
	SELECT user_login No_of_times FROM dbo.users
	GROUP BY user_login
	HAVING COUNT(user_login)>1)
	GROUP BY dbo.users.user_login

RETURN
END
END
---------------
---------------


---1ST SANITZING DATA IN ALL TABLES , FOR THAT WE CAN APPLY FOREIGN KEYS
---ROLE PERMISSION TABLE

IF EXISTS (SELECT DISTINCT r.sys_id, r.field_id FROM dbo.roles_permissions r
	LEFT OUTER JOIN dbo.fields f ON f.sys_id=r.sys_id AND f.field_id=r.field_id
	WHERE f.sys_id IS NULL  AND f.field_id IS NULL)

BEGIN
PRINT 'DELETING ORPHAN CHILDS IN ROLE_PERMISSIONS'

PRINT 'FOLLOWING ARE THE ORPHAN VALUES'
SELECT DISTINCT r.sys_id, r.field_id FROM dbo.roles_permissions r
	LEFT OUTER JOIN dbo.fields f ON f.sys_id=r.sys_id AND f.field_id=r.field_id
	WHERE f.sys_id IS NULL  AND f.field_id IS NULL
	
DELETE r FROM roles_permissions r
	LEFT OUTER JOIN dbo.fields f ON f.sys_id=r.sys_id AND f.field_id=r.field_id
	WHERE f.sys_id IS NULL  AND f.field_id IS NULL
END
ELSE
BEGIN
PRINT 'NO ORPHAN VALUES IN ROLE_PERMISSIONS'
END

IF EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_roles_permissions_fields]')
			AND parent_object_id = OBJECT_ID(N'[dbo].[roles_permissions]'))
BEGIN
	 PRINT 'FOREIGN KEY FK_roles_permissions_fields ALREADY EXITS ' 
END

ELSE

BEGIN
ALTER TABLE [dbo].[roles_permissions]  WITH CHECK 
	ADD  CONSTRAINT [FK_roles_permissions_fields] 
	FOREIGN KEY([sys_id], [field_id])
	REFERENCES [dbo].fields ([sys_id], [field_id])
	ON DELETE CASCADE
	ON UPDATE CASCADE

IF EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_roles_permissions_fields]')
			AND parent_object_id = OBJECT_ID(N'[dbo].[roles_permissions]'))

PRINT 'FOREIGN KEY FK_roles_permissions_fields ADDED SUCCESSFULLY '

END




-----ADDING FOREIGN KEY IN REPORT_ROLES TABLE

IF EXISTS(SELECT DISTINCT rr.sys_id, rr.role_id FROM dbo.report_roles rr
		LEFT OUTER JOIN dbo.roles ro ON rr.sys_id=ro.sys_id AND rr.role_id = ro.role_id
		WHERE ro.sys_id IS NULL  AND ro.role_id IS NULL)

BEGIN
PRINT 'DELETING ORPHAN CHILDS IN REPORT_ROLES'

PRINT 'FOLLOWING ARE THE ORPHAN VALUES'
SELECT DISTINCT rr.sys_id, rr.role_id FROM dbo.report_roles rr
		LEFT OUTER JOIN dbo.roles ro ON rr.sys_id=ro.sys_id AND rr.role_id = ro.role_id
		WHERE ro.sys_id IS NULL  AND ro.role_id IS NULL
END
ELSE
BEGIN
PRINT 'THERE ARE NO ORPHAN CHILD IN REPORT_ROLES '
END




IF EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_report_roles_roles]')
			AND parent_object_id = OBJECT_ID(N'[dbo].[report_roles]'))
BEGIN
PRINT 'FOREIGN KEY FK_report_roles_roles ALREADY EXISTS '
END

ELSE

BEGIN
ALTER TABLE [dbo].[report_roles]  WITH CHECK ADD  CONSTRAINT [FK_report_roles_roles] 
	FOREIGN KEY([sys_id], [role_id])
	REFERENCES [dbo].roles ([sys_id], [role_id])
	ON DELETE CASCADE


IF EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_report_roles_roles]')
			AND parent_object_id = OBJECT_ID(N'[dbo].[report_roles]'))
PRINT 'ADDED FOREIGN KEY FK_report_roles_roles SUCCESSFULLY'

END




-----ADDING FOREIGN KEY IN report_specific_users

IF EXISTS(SELECT DISTINCT rsu.user_id FROM dbo.report_specific_users rsu
		LEFT OUTER JOIN dbo.users u ON rsu.user_id = u.user_id
		WHERE u.user_id IS NULL )

BEGIN
PRINT 'DELETING ORPHAN CHILDS IN REPORT_SPECIFIC_USERS'

PRINT 'FOLLOWING ARE THE ORPHAN VALUES'
SELECT DISTINCT rsu.user_id FROM dbo.report_specific_users rsu
		LEFT OUTER JOIN dbo.users u ON rsu.user_id = u.user_id
		WHERE u.user_id IS NULL
END
ELSE
BEGIN
PRINT 'THERE ARE NO ORPHAN CHILD IN report_specific_users '
END



IF EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_report_specific_users_users]')
			AND parent_object_id = OBJECT_ID(N'[dbo].[report_specific_users]'))
BEGIN
PRINT 'FOREIGN KEY FK_report_specific_users_users ALREADY EXISTS '
END

ELSE

BEGIN

ALTER TABLE [dbo].[report_specific_users]  WITH CHECK ADD  CONSTRAINT [FK_report_specific_users_users] 
	FOREIGN KEY([user_id])
	REFERENCES [dbo].users ([user_id])
	ON DELETE CASCADE



IF EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_report_specific_users_users]')
			AND parent_object_id = OBJECT_ID(N'[dbo].[report_specific_users]'))
PRINT 'ADDED FOREIGN KEY FK_report_specific_users_users SUCCESSFULLY'

END




--- done uptil here.


---- Dropping constraint 'FK_request_users_request' and again appying it with cascade delete
---- for request_users

BEGIN

IF  EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_request_users_request]') 
			AND parent_object_id = OBJECT_ID(N'[dbo].[request_users]'))
ALTER TABLE [dbo].[request_users] 
DROP CONSTRAINT [FK_request_users_request]

PRINT 'CONSTRAINT [FK_request_users_request] DROPPED AND WILL BE RECREADTED WITH ON DELETE CASCADE'

ALTER TABLE [dbo].[request_users]  WITH CHECK 
	ADD  CONSTRAINT [FK_request_users_request] FOREIGN KEY([sys_id], [request_id])
	REFERENCES [dbo].[requests] ([sys_id], [request_id])
	ON DELETE CASCADE

ALTER TABLE [dbo].[request_users] WITH CHECK CHECK CONSTRAINT [FK_request_users_request]


PRINT 'CONSTRAINT [FK_request_users_request] DROPPED AND RECREADTED WITH ON DELETE CASCADE'


END

--- Drop FK FK_action_users_actions and recreate with cascade delete

BEGIN

IF  EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_action_users_actions]')
			AND parent_object_id = OBJECT_ID(N'[dbo].[action_users]'))
ALTER TABLE [dbo].[action_users] DROP CONSTRAINT [FK_action_users_actions]


PRINT 'CONSTRAINT [FK_action_users_actions] DROPPED AND WILL BE RECREADTED WITH ON DELETE CASCADE'


ALTER TABLE [dbo].[action_users]  WITH CHECK 
	ADD  CONSTRAINT [FK_action_users_actions] FOREIGN KEY([sys_id], [request_id], [action_id])
	REFERENCES [dbo].[actions] ([sys_id], [request_id], [action_id])
	ON DELETE CASCADE

ALTER TABLE [dbo].[action_users]  WITH CHECK CHECK CONSTRAINT [FK_action_users_actions]

PRINT 'CONSTRAINT [FK_action_users_actions] DROPPED AND RECREADTED WITH ON DELETE CASCADE'

END



--- tags_requests Table

-----------------


IF EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_tag_requests_requests]')
			AND parent_object_id = OBJECT_ID(N'[dbo].[tags_requests]'))
BEGIN
PRINT 'FOREIGN KEY FK_tag_requests_requests ALREADY EXISTS '
END

ELSE

BEGIN
ALTER TABLE [dbo].tags_requests  WITH CHECK 
	ADD  CONSTRAINT [FK_tag_requests_requests] 
	FOREIGN KEY([sys_id], [request_id])
	REFERENCES [dbo].requests ([sys_id], [request_id])
	ON DELETE CASCADE
	ON UPDATE CASCADE



IF EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_tag_requests_requests]')
			AND parent_object_id = OBJECT_ID(N'[dbo].[tags_requests]'))
PRINT 'ADDED FOREIGN KEY FK_tag_requests_requests SUCCESSFULLY'

END



-----------------

----Next is requests(user_id) to users(user_id)

IF EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_requests_users]')
			AND parent_object_id = OBJECT_ID(N'[dbo].[requests]'))
BEGIN
PRINT 'FOREIGN KEY FK_requests_users ALREADY EXISTS '
END

ELSE

BEGIN

ALTER TABLE [dbo].requests  WITH CHECK 
	ADD  CONSTRAINT [FK_requests_users] 
	FOREIGN KEY([user_id])
	REFERENCES [dbo].users ([user_id])
	ON DELETE CASCADE
	ON UPDATE CASCADE



IF EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_requests_users]')
			AND parent_object_id = OBJECT_ID(N'[dbo].[requests]'))
PRINT 'ADDED FOREIGN KEY FK_requests_users SUCCESSFULLY'

END



-------------------
--- requests_ex ---- fields (sys_id,field_id)

IF EXISTS(SELECT DISTINCT r.sys_id, r.field_id FROM dbo.requests_ex r
	LEFT OUTER JOIN dbo.fields f ON f.sys_id=r.sys_id AND f.field_id=r.field_id
	WHERE f.sys_id IS NULL  AND f.field_id IS NULL)

BEGIN

PRINT 'There are Orphan values in requests_ex which do not have any Parent entry in fields' 

PRINT ' Following are the Orphan values'

SELECT DISTINCT r.sys_id, r.field_id FROM dbo.requests_ex r
	LEFT OUTER JOIN dbo.fields f ON f.sys_id=r.sys_id AND f.field_id=r.field_id
	WHERE f.sys_id IS NULL  AND f.field_id IS NULL

END
ELSE

BEGIN

PRINT ' No Orphan Value found in requests_ex'
END


IF EXISTS (SELECT DISTINCT r.sys_id, r.field_id FROM dbo.requests_ex r
	LEFT OUTER JOIN dbo.fields f ON f.sys_id=r.sys_id AND f.field_id=r.field_id
	WHERE f.sys_id IS NULL  AND f.field_id IS NULL)

BEGIN

PRINT 'Deleting Orphan Values in requests_ex table which do not have any entry in fields '
DELETE r FROM dbo.requests_ex r
	LEFT OUTER JOIN dbo.fields f ON f.sys_id=r.sys_id AND f.field_id=r.field_id
	WHERE f.sys_id IS NULL  AND f.field_id IS NULL

PRINT 'Orphan requests_ex values deleted'

END

--- finally adding foreign key here after sanitizing requests_ex	

IF EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_requests_ex_fields]')
			AND parent_object_id = OBJECT_ID(N'[dbo].[requests_ex]'))
BEGIN
PRINT 'FOREIGN KEY FK_requests_ex_fields ALREADY EXISTS '
END

ELSE

BEGIN

ALTER TABLE [dbo].requests_ex  WITH CHECK 
	ADD  CONSTRAINT [FK_requests_ex_fields] 
	FOREIGN KEY([sys_id],[field_id])
	REFERENCES [dbo].fields ([sys_id],[field_id])
	ON DELETE CASCADE
	ON UPDATE CASCADE


IF EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_requests_ex_fields]')
			AND parent_object_id = OBJECT_ID(N'[dbo].[requests_ex]'))
PRINT 'ADDED FOREIGN KEY FK_requests_ex_fields SUCCESSFULLY'

END


------------------------------------------------------------------------

--- Similary doing it for actions_ex table


IF EXISTS(SELECT DISTINCT r.sys_id, r.field_id FROM dbo.actions_ex r
	LEFT OUTER JOIN dbo.fields f ON f.sys_id=r.sys_id AND f.field_id=r.field_id
	WHERE f.sys_id IS NULL  AND f.field_id IS NULL)

BEGIN

PRINT 'There are Orphan values in actions_ex which do not have any Parent entry in fields' 

PRINT ' Following are the Orphan values'

SELECT DISTINCT r.sys_id, r.field_id FROM dbo.actions_ex r
	LEFT OUTER JOIN dbo.fields f ON f.sys_id=r.sys_id AND f.field_id=r.field_id
	WHERE f.sys_id IS NULL  AND f.field_id IS NULL

END
ELSE

BEGIN

PRINT ' No Orphan Value found in actions_ex'
END


IF EXISTS (SELECT DISTINCT r.sys_id, r.field_id FROM dbo.actions_ex r
	LEFT OUTER JOIN dbo.fields f ON f.sys_id=r.sys_id AND f.field_id=r.field_id
	WHERE f.sys_id IS NULL  AND f.field_id IS NULL)

BEGIN

PRINT 'Deleting Orphan Values in actions_ex table which do not have any entry in fields '
DELETE r FROM dbo.actions_ex r
	LEFT OUTER JOIN dbo.fields f ON f.sys_id=r.sys_id AND f.field_id=r.field_id
	WHERE f.sys_id IS NULL  AND f.field_id IS NULL

PRINT 'Orphan actions_ex values deleted'

END

--- finally adding foreign key here after sanitizing requests_ex	

IF EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_actions_ex_fields]')
			AND parent_object_id = OBJECT_ID(N'[dbo].[actions_ex]'))
BEGIN
PRINT 'FOREIGN KEY FK_actions_ex_fields ALREADY EXISTS '
END

ELSE

BEGIN

ALTER TABLE [dbo].actions_ex  WITH CHECK 
	ADD  CONSTRAINT [FK_actions_ex_fields] 
	FOREIGN KEY([sys_id],[field_id])
	REFERENCES [dbo].fields ([sys_id],[field_id])
	ON DELETE CASCADE
	ON UPDATE CASCADE


IF EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_actions_ex_fields]')
			AND parent_object_id = OBJECT_ID(N'[dbo].[actions_ex]'))
PRINT 'ADDED FOREIGN KEY FK_actions_ex_fields SUCCESSFULLY'

END


-----Type_users ----- Types (sys_id,field_id,type_id)

IF EXISTS (SELECT DISTINCT tu.sys_id, tu.type_id,tu.field_id FROM dbo.type_users tu
	LEFT OUTER JOIN dbo.types t ON tu.sys_id=t.sys_id AND tu.field_id = t.field_id AND tu.type_id = t.type_id
	WHERE t.sys_id IS NULL  AND t.field_id IS NULL AND t.type_id IS NULL)

BEGIN

PRINT 'There are Orphan values in type_users which do not have any Parent value in types'

PRINT 'Following are the Orphan valus in type_users :'

SELECT DISTINCT tu.sys_id, tu.type_id,tu.field_id FROM dbo.type_users tu
	LEFT OUTER JOIN dbo.types t ON tu.sys_id=t.sys_id AND tu.field_id = t.field_id AND tu.type_id = t.type_id
	WHERE t.sys_id IS NULL  AND t.field_id IS NULL AND t.type_id IS NULL

END	
ELSE
BEGIN
PRINT ' There are no Orphan values in type_users table'
END



IF EXISTS (SELECT DISTINCT tu.sys_id, tu.type_id,tu.field_id FROM dbo.type_users tu
	LEFT OUTER JOIN dbo.types t ON tu.sys_id=t.sys_id AND tu.field_id = t.field_id AND tu.type_id = t.type_id
	WHERE t.sys_id IS NULL  AND t.field_id IS NULL AND t.type_id IS NULL
)

BEGIN


PRINT 'Deleting Orphan Values in type_users table which do not have any entry in types '
DELETE tu FROM dbo.type_users tu
	LEFT OUTER JOIN dbo.types t ON tu.sys_id = t.sys_id AND tu.field_id = t.field_id AND tu.type_id = t.type_id
	WHERE t.sys_id IS NULL  AND t.field_id IS NULL AND t.type_id IS NULL

PRINT 'Orphan Values in type_users deleted'

END



IF EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_type_users_types]')
			AND parent_object_id = OBJECT_ID(N'[dbo].[type_users]'))
BEGIN
PRINT 'FOREIGN KEY FK_type_users_types ALREADY EXISTS '
END

ELSE

BEGIN

ALTER TABLE [dbo].[type_users]  WITH CHECK ADD  CONSTRAINT [FK_type_users_types] FOREIGN KEY([sys_id], [field_id], [type_id])
REFERENCES [dbo].[types] ([sys_id], [field_id], [type_id])
ON UPDATE CASCADE
ON DELETE CASCADE


ALTER TABLE [dbo].[type_users] WITH CHECK CHECK CONSTRAINT [FK_type_users_types]



IF EXISTS (SELECT * FROM sys.foreign_keys 
			WHERE object_id = OBJECT_ID(N'[dbo].[FK_type_users_types]')
			AND parent_object_id = OBJECT_ID(N'[dbo].[type_users]'))
PRINT 'ADDED FOREIGN KEY FK_type_users_types SUCCESSFULLY'

END

-----------~~~~~~~~~~~~~ for user_passwords~~~~~~~~~~~~~~~~~-----------



-------------------------------------------

---After user has sanitized the data using  stp_user_repalce_srcUserId_with_destUserId
---then run the following script again

---1st drop the existing constraint 'FK_users_user_passwords'

--2nd make user_login UNIQUE in users table before making FK
-- otherwise it will throw following error:

--"Msg 1776, Level 16, State 0, Line 1
--There are no primary or candidate keys in the referenced table 'dbo.users' that match the referencing column list in the foreign key 'FK_user_passwords_users'.
--Msg 1750, Level 16, State 0, Line 1
--Could not create constraint. See previous errors."

IF EXISTS (SELECT * FROM sys.objects WHERE name LIKE 'UK_users_user_id')
BEGIN
PRINT 'UNIQUE KEY UK_users_user_id ALREADY EXITS'
END
ELSE
BEGIN
ALTER TABLE [dbo].users 
	ADD CONSTRAINT UK_users_user_id UNIQUE (user_login) 

IF EXISTS (SELECT * FROM sys.objects WHERE name LIKE 'UK_users_user_id')
PRINT 'UNIQUE KEY UK_users_user_id ADDED SUCCESSFULLY'

END


--- we can successfully make user_login unique but while making foreign keys it will throw error:
---
--Msg 1778, Level 16, State 0, Line 2
--Column 'dbo.users.user_login' is not the same data type as referencing column 'user_passwords.user_login' in foreign key 'FK_user_passwords_users'.
--Msg 1750, Level 16, State 0, Line 2
--Could not create constraint. See previous errors.


----So prior to applying this foreign key we need to change the datatype of 
----user_login of user_passwords TABLE from varchar to nvarchar.


--- For doing so we need to drop the dependent objects

--Msg 5074, Level 16, State 1, Line 1
--The object 'PK_user_passwords' is dependent on column 'user_login'.
--Msg 5074, Level 16, State 1, Line 1
--The index 'idx_user_passwords_login_password' is dependent on column 'user_login'.
--Msg 4922, Level 16, State 9, Line 1
--ALTER TABLE ALTER COLUMN user_login failed because one or more objects access this column.

----Oops--! cannot drop a PRIMARY KEY..!!--- Basic rule of RDBMS

--Step:1



SELECT * INTO #temp_user_passwords FROM dbo.user_passwords

PRINT 'All user_passwords data taken into temp table'

IF EXISTS (
--- All user_logins that are not there in users table
SELECT DISTINCT up.user_login FROM dbo.#temp_user_passwords up
LEFT OUTER JOIN dbo.users u ON u.user_login=up.user_login 
WHERE u.user_login IS NULL 
)
BEGIN

PRINT 'Following are the user_logins in user_passwords table that are not there in users table'

SELECT DISTINCT up.user_login FROM dbo.#temp_user_passwords up
LEFT OUTER JOIN dbo.users u ON u.user_login=up.user_login 
WHERE u.user_login IS NULL 

PRINT 'All such values will be cleaned up'

END


--Step:2
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[user_passwords]') AND type in (N'U'))
BEGIN
DROP TABLE [dbo].[user_passwords]

PRINT 'Dropping User_Passwords table and recreating the table with new nvarchar columns and ..'
PRINT 'Taking the User_passwords data in temp table'

END

--Step:3
CREATE TABLE [dbo].[user_passwords](
	[user_login] [nvarchar](255) NOT NULL,
	[password] [nvarchar](255) NULL,
 CONSTRAINT [PK_user_passwords] PRIMARY KEY CLUSTERED([user_login] ASC),
 CONSTRAINT FK_user_logins_users FOREIGN KEY ([user_login])
 REFERENCES users([user_login])
 ON DELETE CASCADE
 ON UPDATE CASCADE
)

PRINT 'User_Passwords  Table created with new nvarchar columns'

--- Here we might ger error:
----Msg 547, Level 16, State 0, Line 2
--The INSERT statement conflicted with the FOREIGN KEY constraint "fk_user_logins_users". The conflict occurred in database "lnt", table "dbo.users", column 'user_login'.
--The statement has been terminated.

-- This occurs cause there is a orphan child in user_passwords table.
-- One needs to delete it before proceeding.




--Step:5
--- Now deleting

DELETE up FROM #temp_user_passwords up
	LEFT OUTER JOIN dbo.users u ON u.user_login=up.user_login 
WHERE u.user_login IS NULL 


--Step:6
INSERT INTO dbo.user_passwords
SELECT * FROM #temp_user_passwords

PRINT 'user_passwords table recreated with sanitized data and proper FK with users table'

PRINT 'Foreign keys applied and the database is sanitized now'

PRINT 'If this stp has returned any error please notify your Administrator'

DROP TABLE #temp_user_passwords

END