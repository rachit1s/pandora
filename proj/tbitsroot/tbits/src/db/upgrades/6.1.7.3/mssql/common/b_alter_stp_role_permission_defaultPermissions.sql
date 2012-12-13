if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[stp_role_permission_defaultPermissions]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[stp_role_permission_defaultPermissions]
GO


set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
go



create procedure [dbo].[stp_role_permission_defaultPermissions]
(
	@sys_id 	INT,
	@role_name 	VARCHAR(40)
)
AS
DECLARE @max_field_id INT
DECLARE @role_id INT

SELECT 
	@max_field_id = MAX(field_id) 
FROM 
	fields 
WHERE 
	sys_id = @sys_id


SELECT 
	@role_id = role_id 
FROM 
	roles 
WHERE 
	rolename = @role_name and 
	sys_id = @sys_id

IF(@role_id = 1)
BEGIN
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 1 and role_id = 1
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 2 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 3 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 4 and role_id = 1
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 5 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 6 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 7 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 8 and role_id = 1
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 9 and role_id = 1
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 10 and role_id = 1
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 11 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 12 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 13 and role_id = 1
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 14 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 15 and role_id = 1
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 16 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 17 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 18 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 19 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 20 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 21 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 22 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 23 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 24 and role_id = 1
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 26 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 27 and role_id = 1
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 28 and role_id = 1
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 29 and role_id = 1
END
IF(@role_id = 2)
begin
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 1 and role_id = 2
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 2 and role_id = 2
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 3 and role_id = 2
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 4 and role_id = 2
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 5 and role_id = 2
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 6 and role_id = 2
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 7 and role_id = 2
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 8 and role_id = 2
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 9 and role_id = 2
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 10 and role_id = 2
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 11 and role_id = 2
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 12 and role_id = 2
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 13 and role_id = 2
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 14 and role_id = 2
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 15 and role_id = 2
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 16 and role_id = 2
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 17 and role_id = 2
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 18 and role_id = 2
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 19 and role_id = 2
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 20 and role_id = 2
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 21 and role_id = 2
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 22 and role_id = 2
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 23 and role_id = 2
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 24 and role_id = 2
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 2
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 26 and role_id = 2
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 27 and role_id = 2
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 28 and role_id = 2
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 29 and role_id = 2
END
IF(@role_id = 3)
begin
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 1 and role_id = 3
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 2 and role_id = 3
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 3 and role_id = 3
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 4 and role_id = 3
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 5 and role_id = 3
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 6 and role_id = 3
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 7 and role_id = 3
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 8 and role_id = 3
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 9 and role_id = 3
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 10 and role_id = 3
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 11 and role_id = 3
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 12 and role_id = 3
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 13 and role_id = 3
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 14 and role_id = 3
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 15 and role_id = 3
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 16 and role_id = 3
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 17 and role_id = 3
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 18 and role_id = 3
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 19 and role_id = 3
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 20 and role_id = 3
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 21 and role_id = 3
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 22 and role_id = 3
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 23 and role_id = 3
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 24 and role_id = 3
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 3
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 26 and role_id = 3
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 27 and role_id = 3
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 28 and role_id = 3
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 29 and role_id = 3
END
IF(@role_id = 4)
begin
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 1 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 2 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 3 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 4 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 5 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 6 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 7 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 8 and role_id = 4
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 9 and role_id = 4
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 10 and role_id = 4
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 11 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 12 and role_id = 4
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 13 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 14 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 15 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 16 and role_id = 4
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 17 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 18 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 19 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 20 and role_id = 4
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 21 and role_id = 4
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 22 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 23 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 24 and role_id = 4
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 26 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 27 and role_id = 4
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 28 and role_id = 4
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 29 and role_id = 4
END
IF(@role_id = 5)
begin
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 1 and role_id = 5
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 2 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 3 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 4 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 5 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 6 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 7 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 8 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 9 and role_id = 5
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 10 and role_id = 5
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 11 and role_id = 5
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 12 and role_id = 5
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 13 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 14 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 15 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 16 and role_id = 5
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 17 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 18 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 19 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 20 and role_id = 5
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 21 and role_id = 5
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 22 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 23 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 24 and role_id = 5
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 26 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 27 and role_id = 5
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 28 and role_id = 5
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 29 and role_id = 5
END
IF(@role_id = 6)
begin
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 1 and role_id = 6
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 2 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 3 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 4 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 5 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 6 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 7 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 8 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 9 and role_id = 6
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 10 and role_id = 6
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 11 and role_id = 6
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 12 and role_id = 6
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 13 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 14 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 15 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 16 and role_id = 6
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 17 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 18 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 19 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 20 and role_id = 6
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 21 and role_id = 6
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 22 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 23 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 24 and role_id = 6
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 26 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 27 and role_id = 6
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 28 and role_id = 6
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 29 and role_id = 6
END
IF(@role_id = 7)
begin
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 1 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 2 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 3 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 4 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 5 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 6 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 7 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 8 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 9 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 10 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 11 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 12 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 13 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 14 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 15 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 16 and role_id = 7
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 17 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 18 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 19 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 20 and role_id = 7
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 21 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 22 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 23 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 24 and role_id = 7
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 26 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 27 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 28 and role_id = 7
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 29 and role_id = 7
END
IF(@role_id = 8)
begin
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 1 and role_id = 8
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 2 and role_id = 8
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 3 and role_id = 8
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 4 and role_id = 8
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 5 and role_id = 8
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 6 and role_id = 8
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 7 and role_id = 8
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 8 and role_id = 8
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 9 and role_id = 8
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 10 and role_id = 8
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 11 and role_id = 8
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 12 and role_id = 8
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 13 and role_id = 8
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 14 and role_id = 8
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 15 and role_id = 8
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 16 and role_id = 8
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 17 and role_id = 8
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 18 and role_id = 8
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 19 and role_id = 8
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 20 and role_id = 8
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 21 and role_id = 8
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 22 and role_id = 8
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 23 and role_id = 8
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 24 and role_id = 8
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 8
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 26 and role_id = 8
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 27 and role_id = 8
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 28 and role_id = 8
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 29 and role_id = 8
END
IF(@role_id = 9)
begin
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 1 and role_id = 9
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 2 and role_id = 9
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 3 and role_id = 9
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 4 and role_id = 9
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 5 and role_id = 9
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 6 and role_id = 9
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 7 and role_id = 9
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 8 and role_id = 9
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 9 and role_id = 9
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 10 and role_id = 9
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 11 and role_id = 9
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 12 and role_id = 9
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 13 and role_id = 9
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 14 and role_id = 9
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 15 and role_id = 9
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 16 and role_id = 9
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 17 and role_id = 9
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 18 and role_id = 9
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 19 and role_id = 9
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 20 and role_id = 9
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 21 and role_id = 9
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 22 and role_id = 9
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 23 and role_id = 9
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 24 and role_id = 9
UPDATE roles_permissions SET gpermissions = 0 , dpermissions = 0   WHERE sys_id = @sys_id and field_id = 25 and role_id = 9
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 26 and role_id = 9
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 27 and role_id = 9
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 28 and role_id = 9
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = 29 and role_id = 9
END
IF(@max_field_id > 29)
BEGIN
declare @i int
SELECT @i = 30
   while(@i <= @max_field_id)
     BEGIN
       IF(@role_id = 1)
       begin
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 1
       END
       IF(@role_id = 2)
       begin
UPDATE roles_permissions SET gpermissions = 13 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 2
       END
       IF(@role_id = 3)
       begin
UPDATE roles_permissions SET gpermissions = 14 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 3
       END
       IF(@role_id = 4)
       begin
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 4
       END
       IF(@role_id = 5)
       begin
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 5
       END
       IF(@role_id = 6)
       begin
UPDATE roles_permissions SET gpermissions = 12 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 6
       END
       IF(@role_id = 7)
       begin
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 7
       END
       IF(@role_id = 8)
       begin
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 8
       END
       IF(@role_id = 9)
       begin
UPDATE roles_permissions SET gpermissions = 15 , dpermissions = 0 WHERE sys_id = @sys_id and field_id = @i and role_id = 9
       END
       SELECT @i = @i + 1
     END
END




