IF NOT EXISTS(SELECT  * FROM  SYS.OBJECTS WHERE type = N'TR' and Name = N'audit_fields')
BEGIN
DECLARE @sqlcommand nvarchar(2000)
SET @sqlcommand = N'
CREATE TRIGGER [dbo].[audit_fields] ON [dbo].[fields] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_fields 
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_fields WHERE temporal_fields.sys_id = deleted.sys_id AND temporal_fields.field_id = deleted.field_id 
 AND
 audit_EndDateTime = ''9/9/9999''
 INSERT INTO temporal_fields(sys_id,field_id,name,display_name,description,data_type_id,is_active,is_extended,is_private,tracking_option,permission,regex,is_dependent,display_order,display_group
 ,audit_StartDateTime, audit_EndDateTime) SELECT sys_id,field_id,name,display_name,description,data_type_id,is_active,is_extended,is_private,tracking_option,permission,regex,is_dependent,display_order,display_group, @TrigTime , ''9/9/9999''FROM INSERTED
 '
EXEC SP_EXECUTESQL  @sqlcommand 
END 
ELSE
print'audit_fields already exists'
GO
IF NOT EXISTS(SELECT  * FROM  SYS.OBJECTS WHERE type = N'TR' and Name = N'audit_Types')
BEGIN
DECLARE @sqlcommand nvarchar(2000)
SET @sqlcommand = N'
CREATE TRIGGER [dbo].[audit_Types] ON [dbo].[Types] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_Types 
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_Types WHERE temporal_Types.sys_id = deleted.sys_id AND temporal_Types.field_id = deleted.field_id AND temporal_Types.type_id = deleted.type_id 
 AND
 audit_EndDateTime = ''9/9/9999''
 INSERT INTO temporal_Types(sys_id,field_id,type_id,name,display_name,description,ordering,is_active,is_default,is_checked,is_private,is_final
 ,audit_StartDateTime, audit_EndDateTime) SELECT sys_id,field_id,type_id,name,display_name,description,ordering,is_active,is_default,is_checked,is_private,is_final, @TrigTime , ''9/9/9999''FROM INSERTED
'
EXEC SP_EXECUTESQL  @sqlcommand 
END 
ELSE
print'audit_Types already exists' 
GO
----
IF NOT EXISTS(SELECT  * FROM  SYS.OBJECTS WHERE type = N'TR' and Name = N'audit_business_area_users')
BEGIN
DECLARE @sqlcommand nvarchar(2000)
SET @sqlcommand = N'
CREATE TRIGGER [dbo].[audit_business_area_users] ON [dbo].[business_area_users] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_business_area_users 
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_business_area_users WHERE temporal_business_area_users.sys_id = deleted.sys_id AND temporal_business_area_users.user_id = deleted.user_id 
 AND
 audit_EndDateTime = ''9/9/9999''
 INSERT INTO temporal_business_area_users(sys_id,user_id,is_active
 ,audit_StartDateTime, audit_EndDateTime) SELECT sys_id,user_id,is_active, @TrigTime , ''9/9/9999''FROM INSERTED
'
EXEC SP_EXECUTESQL  @sqlcommand 
END 
ELSE
print'audit_business_area_users already exists' 
GO
----
IF NOT EXISTS(SELECT  * FROM  SYS.OBJECTS WHERE type = N'TR' and Name = N'audit_display_groups')
BEGIN
DECLARE @sqlcommand nvarchar(2000)
SET @sqlcommand = N'

CREATE TRIGGER [dbo].[audit_display_groups] ON [dbo].[display_groups] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_display_groups 
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_display_groups WHERE temporal_display_groups.id = deleted.id 
 AND
 audit_EndDateTime = ''9/9/9999''
 SET IDENTITY_INSERT dbo.temporal_display_groups ON
 INSERT INTO temporal_display_groups(sys_id,id,display_name,display_order,is_active
 ,audit_StartDateTime, audit_EndDateTime) SELECT sys_id,id,display_name,display_order,is_active, @TrigTime , ''9/9/9999''FROM INSERTED
'
EXEC SP_EXECUTESQL  @sqlcommand 
END 
ELSE
print'audit_display_groups already exists' 
GO
----
IF NOT EXISTS(SELECT  * FROM  SYS.OBJECTS WHERE type = N'TR' and Name = N'audit_permissions')
BEGIN
DECLARE @sqlcommand nvarchar(2000)
SET @sqlcommand = N'
CREATE TRIGGER [dbo].[audit_permissions] ON [dbo].[permissions] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_permissions 
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_permissions WHERE temporal_permissions.permission = deleted.permission 
 AND
 audit_EndDateTime = ''9/9/9999''
 INSERT INTO temporal_permissions(permission,padd,pchange,pview,pEmailView
 ,audit_StartDateTime, audit_EndDateTime) SELECT permission,padd,pchange,pview,pEmailView, @TrigTime , ''9/9/9999''FROM INSERTED
'
EXEC SP_EXECUTESQL  @sqlcommand 
END 
ELSE
print'audit_permissions already exists' 
GO
----
IF NOT EXISTS(SELECT  * FROM  SYS.OBJECTS WHERE type = N'TR' and Name = N'audit_roles')
BEGIN
DECLARE @sqlcommand nvarchar(2000)
SET @sqlcommand = N'
CREATE TRIGGER [dbo].[audit_roles] ON [dbo].[roles] 
FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_roles 
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_roles WHERE temporal_roles.sys_id = deleted.sys_id AND temporal_roles.role_id = deleted.role_id 
 AND
 audit_EndDateTime = ''9/9/9999''
 INSERT INTO temporal_roles(sys_id,role_id,rolename,description,field_id,can_be_deleted
 ,audit_StartDateTime, audit_EndDateTime) SELECT sys_id,role_id,rolename,description,field_id,can_be_deleted, @TrigTime , ''9/9/9999''FROM INSERTED
'
EXEC SP_EXECUTESQL  @sqlcommand 
END 
ELSE
print'audit_roles already exists' 
GO
----
IF NOT EXISTS(SELECT  * FROM  SYS.OBJECTS WHERE type = N'TR' and Name = N'audit_roles_permissions')
BEGIN
DECLARE @sqlcommand nvarchar(2000)
SET @sqlcommand = N'
CREATE TRIGGER [dbo].[audit_roles_permissions] ON [dbo].[roles_permissions] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_roles_permissions 
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_roles_permissions WHERE temporal_roles_permissions.sys_id = deleted.sys_id AND temporal_roles_permissions.role_id = deleted.role_id AND temporal_roles_permissions.field_id = deleted.field_id 
 AND
 audit_EndDateTime =''9/9/9999''
 INSERT INTO temporal_roles_permissions(sys_id,role_id,field_id,gpermissions,dpermissions
 ,audit_StartDateTime, audit_EndDateTime) SELECT sys_id,role_id,field_id,gpermissions,dpermissions, @TrigTime , ''9/9/9999''FROM INSERTED
'
EXEC SP_EXECUTESQL  @sqlcommand 
END 
ELSE
print'audit_roles_permissions already exists' 
GO
----
IF NOT EXISTS(SELECT  * FROM  SYS.OBJECTS WHERE type = N'TR' and Name = N'audit_roles_users')
BEGIN
DECLARE @sqlcommand nvarchar(2000)
SET @sqlcommand = N'
CREATE TRIGGER [dbo].[audit_roles_users] ON [dbo].[roles_users] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_roles_users 
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_roles_users WHERE temporal_roles_users.sys_id = deleted.sys_id AND temporal_roles_users.role_id = deleted.role_id AND temporal_roles_users.user_id = deleted.user_id 
 AND
 audit_EndDateTime = ''9/9/9999''
 INSERT INTO temporal_roles_users(sys_id,role_id,user_id,is_active
 ,audit_StartDateTime, audit_EndDateTime) SELECT sys_id,role_id,user_id,is_active, @TrigTime , ''9/9/9999''FROM INSERTED
'
EXEC SP_EXECUTESQL  @sqlcommand 
END 
ELSE
print'audit_roles_users already exists' 
GO
----
IF NOT EXISTS(SELECT  * FROM  SYS.OBJECTS WHERE type = N'TR' and Name = N'audit_type_users')
BEGIN
DECLARE @sqlcommand nvarchar(2000)
SET @sqlcommand = N'
CREATE TRIGGER [dbo].[audit_type_users] ON [dbo].[type_users] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_type_users 
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_type_users WHERE temporal_type_users.sys_id = deleted.sys_id AND temporal_type_users.field_id = deleted.field_id AND temporal_type_users.type_id = deleted.type_id AND temporal_type_users.user_id = deleted.user_id AND temporal_type_users.user_type_id = deleted.user_type_id 
 AND
 audit_EndDateTime = ''9/9/9999''
 INSERT INTO temporal_type_users(sys_id,field_id,type_id,user_id,user_type_id,notification_id,is_volunteer,rr_volunteer,is_active
 ,audit_StartDateTime, audit_EndDateTime) SELECT sys_id,field_id,type_id,user_id,user_type_id,notification_id,is_volunteer,rr_volunteer,is_active, @TrigTime , ''9/9/9999''FROM INSERTED
'
EXEC SP_EXECUTESQL  @sqlcommand 
END 
ELSE
print'audit_roles_users already exists' 
GO
----
IF NOT EXISTS(SELECT  * FROM  SYS.OBJECTS WHERE type = N'TR' and Name = N'audit_user_types')
BEGIN
DECLARE @sqlcommand nvarchar(2000)
SET @sqlcommand = N'
CREATE TRIGGER [dbo].[audit_user_types] ON [dbo].[user_types] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_user_types 
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_user_types WHERE temporal_user_types.user_type_id = deleted.user_type_id 
 AND
 audit_EndDateTime = ''9/9/9999''
 INSERT INTO temporal_user_types(user_type_id,name
 ,audit_StartDateTime, audit_EndDateTime) SELECT user_type_id,name, @TrigTime , ''9/9/9999''FROM INSERTED
'
EXEC SP_EXECUTESQL  @sqlcommand 
END 
ELSE
print'user_type already exists' 
GO
----
IF NOT EXISTS(SELECT  * FROM  SYS.OBJECTS WHERE type = N'TR' and Name = N'audit_users')
BEGIN
DECLARE @sqlcommand nvarchar(2000)
SET @sqlcommand = N'
CREATE TRIGGER [dbo].[audit_users] ON [dbo].[users] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_users 
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_users WHERE temporal_users.user_id = deleted.user_id 
 AND
 audit_EndDateTime = ''9/9/9999''
 INSERT INTO temporal_users(user_id,user_login,first_name,last_name,display_name,email,is_active,user_type_id,is_on_vacation,is_display,cn,distinguished_name,name,mail_nickname,location,extension,mobile,home_phone,firm_code,designation,firm_address,sex,full_firm_name,other_emails
 ,audit_StartDateTime, audit_EndDateTime) SELECT user_id,user_login,first_name,last_name,display_name,email,is_active,user_type_id,is_on_vacation,is_display,cn,distinguished_name,name,mail_nickname,location,extension,mobile,home_phone,firm_code,designation,firm_address,sex,full_firm_name,other_emails, @TrigTime , ''9/9/9999''FROM INSERTED
'
EXEC SP_EXECUTESQL  @sqlcommand 
END 
ELSE
print'audit_users already exists' 



