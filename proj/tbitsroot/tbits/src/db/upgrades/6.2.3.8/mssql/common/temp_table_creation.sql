--fields : table name is valid
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME='temporal_fields') BEGIN select * into temporal_fields from fields where 1 = 1
ALTER TABLE  temporal_fields add  audit_StartDateTime datetime, audit_EndDateTime datetime  END
update temporal_fields set audit_StartDateTime = getdate() ;
   update temporal_fields set audit_EndDateTime = '9/9/9999' 
--temporal_fields: table is created 
-------------
--Types : table name is valid
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME='temporal_Types') BEGIN select * into temporal_Types from Types where 1 = 1
ALTER TABLE  temporal_Types add  audit_StartDateTime datetime, audit_EndDateTime datetime  END
update temporal_Types set audit_StartDateTime = getdate() ;
   update temporal_Types set audit_EndDateTime = '9/9/9999' 
--temporal_Types: table is created 
--business_area_users : table name is valid
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME='temporal_business_area_users') BEGIN select * into temporal_business_area_users from business_area_users where 1 = 1
ALTER TABLE  temporal_business_area_users add  audit_StartDateTime datetime, audit_EndDateTime datetime  END
update temporal_business_area_users set audit_StartDateTime = getdate() ;
   update temporal_business_area_users set audit_EndDateTime = '9/9/9999' 
--temporal_business_area_users: table is created 
-------------
---display_groups : table name is valid
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME='temporal_display_groups') BEGIN select * into temporal_display_groups from display_groups where 1 = 1
ALTER TABLE  temporal_display_groups add  audit_StartDateTime datetime, audit_EndDateTime datetime  END
update temporal_display_groups set audit_StartDateTime = getdate() ;
   update temporal_display_groups set audit_EndDateTime = '9/9/9999' 
--temporal_display_groups: table is created 
-------------
--permissions : table name is valid
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME='temporal_permissions') BEGIN select * into temporal_permissions from permissions where 1 = 1
ALTER TABLE  temporal_permissions add  audit_StartDateTime datetime, audit_EndDateTime datetime  END
update temporal_permissions set audit_StartDateTime = getdate() ;
   update temporal_permissions set audit_EndDateTime = '9/9/9999' 
--temporal_permissions: table is created 
-------------
--roles : table name is valid
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME='temporal_roles') BEGIN select * into temporal_roles from roles where 1 = 1
ALTER TABLE  temporal_roles add  audit_StartDateTime datetime, audit_EndDateTime datetime  END
update temporal_roles set audit_StartDateTime = getdate() ;
   update temporal_roles set audit_EndDateTime = '9/9/9999' 
--temporal_roles: table is created 
-------------
--roles_permissions : table name is valid
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME='temporal_roles_permissions') BEGIN select * into temporal_roles_permissions from roles_permissions where 1 = 1
ALTER TABLE  temporal_roles_permissions add  audit_StartDateTime datetime, audit_EndDateTime datetime  END
update temporal_roles_permissions set audit_StartDateTime = getdate() ;
   update temporal_roles_permissions set audit_EndDateTime = '9/9/9999' 
--temporal_roles_permissions: table is created 
-------------
--roles_users : table name is valid
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME='temporal_roles_users') BEGIN select * into temporal_roles_users from roles_users where 1 = 1
ALTER TABLE  temporal_roles_users add  audit_StartDateTime datetime, audit_EndDateTime datetime  END
update temporal_roles_users set audit_StartDateTime = getdate() ;
   update temporal_roles_users set audit_EndDateTime = '9/9/9999' 
--temporal_roles_users: table is created 
--type_users : table name is valid
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME='temporal_type_users') BEGIN select * into temporal_type_users from type_users where 1 = 1
ALTER TABLE  temporal_type_users add  audit_StartDateTime datetime, audit_EndDateTime datetime  END
update temporal_type_users set audit_StartDateTime = getdate() ;
   update temporal_type_users set audit_EndDateTime = '9/9/9999' 
--temporal_type_users: table is created 
-------------
--user_types : table name is valid
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME='temporal_user_types') BEGIN select * into temporal_user_types from user_types where 1 = 1
ALTER TABLE  temporal_user_types add  audit_StartDateTime datetime, audit_EndDateTime datetime  END
update temporal_user_types set audit_StartDateTime = getdate() ;
   update temporal_user_types set audit_EndDateTime = '9/9/9999' 
--temporal_user_types: table is created 
--------users : table name is valid
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME='temporal_users') BEGIN select * into temporal_users from users where 1 = 1
ALTER TABLE  temporal_users add  audit_StartDateTime datetime, audit_EndDateTime datetime  END
update temporal_users set audit_StartDateTime = getdate() ;
   update temporal_users set audit_EndDateTime = '9/9/9999' 
--temporal_users: table is created 