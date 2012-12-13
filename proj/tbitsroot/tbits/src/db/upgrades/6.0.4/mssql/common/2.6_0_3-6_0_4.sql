/*
*  1. Add SEND SMS field to existing BAs as well as to create_ba
*  2. Add a role 'BA Users' to existing BAs as well as insertDefaultPermissions
*  3. Add permissions related to SMS field for each role in existing BAs as well as insertDefaultPermissions
*  4. Add all users to 'BA Users' in existing BAs 
*  5. When adding a user to a BA she should autmatically get added to 'BA Users'
*/

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET XACT_ABORT ON
GO
BEGIN TRAN
declare @maxBA INT;
select @maxBA = max(sys_id) from business_areas
declare @i INT
declare @new_field_id int;
declare @maxRole int;
declare @role int;
declare @maxUserId int;
declare @userId int;
declare @ba_user_id int;
declare @baUserSysId int

set @i = 1
while @i <= @maxBA 
BEGIN
	INSERT INTO roles(sys_id, role_id, rolename, description) VALUES(@i, 12, 'BAUsers', 'BAUsers')
	print 'Starting bulk operations for  ba=' + cast(@i as varchar(20)) +  ' and 12 role_id';
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 1, 4, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 2, 7, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 3, 6, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 4, 6, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 5, 6, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 6, 6, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 7, 4, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 8, 7, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 9, 7, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 10, 7, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 11, 7, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 12, 7, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 13, 5, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 14, 0, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 15, 4, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 16, 4, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 17, 0, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 18, 6, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 19, 4, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 20, 4, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 21, 4, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 22, 5, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 23, 7, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 24, 3, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 25, 0, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 26, 7, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 27, 7, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 28, 0, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 29, 7, 0)
	INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@i, 12, 30, 4, 0)
	print 'Finished'
	INSERT INTO field_descriptors(sys_id, field_id, field_descriptor, is_primary) VALUES(@i, 31, 'sendsms', 1)
	set @i = @i + 1	
END

set @i = 1
while @i <= @maxBA 
BEGIN
	exec stp_field_insert @i, 1, 'SendSMS', 'Send SMS', 'Sends SMS', 1, 1, 1, 0, 0, 47, '', 0, 0, 0
	select @new_field_id = max(field_id) from fields where sys_id = @i

	set @i = @i + 1	
END
print 'Adding role users';
declare ba_users_cursor CURSOR FOR
select sys_id, user_id from business_area_users 

open ba_users_cursor

fetch next from ba_users_cursor into @baUserSysId, @ba_user_id

while @@FETCH_STATUS = 0
BEGIN
	print 'Addeding role users: sys_id = ';
	print cast(@baUserSysId as varchar(10)) + ', role_id = 12, user_id = '
			+ cast(@ba_user_id as varchar(10));
	exec stp_admin_insert_roles_users @baUserSysId, 12, @ba_user_id, 1
	fetch next from ba_users_cursor into @baUserSysId, @ba_user_id
END
close ba_users_cursor 
deallocate ba_users_cursor

COMMIT TRAN
