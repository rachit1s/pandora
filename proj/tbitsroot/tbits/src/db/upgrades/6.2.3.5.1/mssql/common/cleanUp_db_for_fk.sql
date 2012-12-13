-----------------clean Up Sql for FK entries.

----delete all report Roles without exesting reports
IF Exists (select * from report_roles where report_id not in (select report_id from reports))
	BEGIN
	delete from report_roles where report_id not in (select report_id from reports)
	END
Else
	Print 'No Orphan report roles here'

----------------------------
IF Exists(Select * from report_specific_users  where report_id not in (select report_id from reports))
	BEGIN
	delete from report_specific_users  where report_id not in (select report_id from reports)
	END
Else
	Print 'No Orphan reports specific users here'
---------------------------------
IF Exists(select * from report_params where report_id not in ( select report_id from reports ))
	BEGIN
	delete from report_params where report_id not in ( select report_id from reports )
	END
Else
	Print 'No orphan report params here'
---------------------------------
IF Exists(select * from gadget_user_config where id not in (select report_id from reports))
	BEGIN
	delete from gadget_user_config where id not in (select report_id from reports)
	END
Else
	print 'No Orphan Gadget User Config here'
--------- delete all role users if roles are not defined in Business Areas
IF Exists(select ru.* from roles_users ru left join roles r on ru.sys_id = r.sys_id and ru.role_id = r.role_id where r.sys_id is null)
	BEGIN
	delete ru from roles_users ru
	left join roles r on ru.sys_id = r.sys_id and ru.role_id = r.role_id
	where r.sys_id is null
	END
Else
	Print 'No orphan Role users here'
-----------------------------role permissions for 
IF Exists(select  rp.* from roles_permissions rp left join roles r on rp.sys_id = r.sys_id and rp.role_id = r.role_id where r.sys_id is null)
	BEGIN
	delete rp from roles_permissions rp
	left join roles r on rp.sys_id = r.sys_id and rp.role_id = r.role_id
	where r.sys_id is null
	END
Else
	Print 'No orphan Role permission here'
------------------type users delete from type_users if threre is no users in the users table.
If Exists (select * from type_users where user_id not in (select user_id from users))
	BEGIN
	delete from type_users where user_id not in (select user_id from users)
	END
Else
	print 'No Orphan type_users here' 

-----delete request_users && actions_users if users is not exists.
If Exists (select * from request_users  where user_id not in ( select user_id from users ))
	BEGIN
	delete from request_users  where user_id not in ( select user_id from users )
	END
Else
	Print 'No orphan Request Users here' 
-----------------------------------
If Exists (select *  from action_users where user_id not in ( select user_id from users ))
	BEGIN
	delete  from action_users where user_id not in ( select user_id from users )
	END
Else
	Print 'No Orphan action users here' 

