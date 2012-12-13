-----Addition of foreign key relation in basic tables.

---BUSINESS_AREAS RELATED TABLES
-------------------request tables to business_areas tables
IF NOT EXISTS( select * from sys.objects where name = 'FK_requests_business_areas')
	BEGIN
	ALTER TABLE requests 
	ADD CONSTRAINT FK_requests_business_areas foreign key (sys_id)
	REFERENCES business_areas(sys_id) 
    END
ELSE 
   print 'FK_requests_business_areas already exists'
GO
-------------------actions to business_areas
-------------------request_ex to business_areas
IF NOT EXISTS( select * from sys.objects where name = 'FK_requests_ex_business_areas')
	BEGIN
	ALTER TABLE requests_ex
	ADD CONSTRAINT  FK_requests_ex_business_areas foreign key (sys_id)
	REFERENCES business_areas(sys_id)
    END
ELSE 
   print 'FK_requests_ex_business_areas already exists'
GO
--------------------actions_ex to business_areas
--------------- fields to business-areas
IF NOT EXISTS( select * from sys.objects where name = 'FK_fields_business_areas')
	BEGIN
	ALTER TABLE fields
	ADD CONSTRAINT  FK_fields_business_areas foreign key (sys_id)
	REFERENCES business_areas(sys_id)
    END
ELSE 
   print 'FK_fields_business_areas already exists'
GO
----------------types to business-areas
---REQUESTS RELATED TABLE
----------------- actions to requests 
IF NOT EXISTS( select * from sys.objects where name = 'FK_action_request')
	BEGIN
	ALTER TABLE actions 
	ADD CONSTRAINT FK_action_request FOREIGN KEY(sys_id,request_id)
	REFERENCES requests(sys_id,request_id) 
	END
ELSE 
   print 'FK_action_request already exists'
GO
----------------- request_ex to requests.
IF NOT EXISTS( select * from sys.objects where name = 'FK_requests_ex_request')
	BEGIN
	ALTER TABLE requests_ex 
	ADD CONSTRAINT FK_requests_ex_request FOREIGN KEY(sys_id,request_id)
	REFERENCES requests(sys_id,request_id)
	END
ELSE 
   print 'FK_requests_ex_request already exists'
GO
----------------- actions_ex to requests
IF NOT EXISTS( select * from sys.objects where name = 'FK_actions_ex_request')
	BEGIN
	ALTER TABLE actions_ex 
	ADD CONSTRAINT FK_actions_ex_request FOREIGN KEY(sys_id,request_id)
	REFERENCES requests(sys_id,request_id) 
	END
ELSE 
   print 'FK_actions_ex_request already exists'
GO
--------------------related_request to requests
IF NOT EXISTS( select * from sys.objects where name = 'FK_related_requests_request')
	BEGIN
	ALTER TABLE related_requests 
	ADD CONSTRAINT FK_related_requests_request FOREIGN KEY(primary_sys_id,primary_request_id)
	REFERENCES requests(sys_id,request_id)
	END
ELSE 
   print 'FK_related_requests_request already exists'
GO
-------------requests_users to requests
IF NOT EXISTS( select * from sys.objects where name = 'FK_request_users_request')
	BEGIN
	ALTER TABLE request_users
	ADD CONSTRAINT FK_request_users_request FOREIGN KEY(sys_id,request_id)
	REFERENCES requests(sys_id,request_id) 
	END
ELSE 
   print 'FK_request_users_request already exists'
GO
-------------action_users to requests
----ACTION RELATED TABLES

-------------------actions_ex to actions.
IF NOT EXISTS( select * from sys.objects where name = 'FK_actions_ex_actions')
	BEGIN
	ALTER TABLE actions_ex 
	ADD CONSTRAINT FK_actions_ex_actions FOREIGN KEY(sys_id,request_id,action_id)
	REFERENCES actions(sys_id,request_id,action_id) 
	END
ELSE 
   print 'FK_actions_ex_actions already exists'
GO
----------------------action_users to actions
IF NOT EXISTS( select * from sys.objects where name = 'FK_action_users_actions')
	BEGIN
	ALTER TABLE action_users 
	ADD CONSTRAINT FK_action_users_actions FOREIGN KEY(sys_id,request_id,action_id)
	REFERENCES actions(sys_id,request_id,action_id) 
	END
ELSE 
   print 'FK_action_users_actions already exists'
GO
----REQUESTS_EX RELATED TABLES
---------------------actions-ex to requests_ex
-----------types to Fields
IF NOT EXISTS( select * from sys.objects where name = 'FK_types_fields')
	BEGIN
	ALTER TABLE types 
	ADD CONSTRAINT FK_types_fields FOREIGN KEY(sys_id,field_id)
	REFERENCES fields(sys_id,field_id) ON DELETE CASCADE
	END
ELSE 
   print 'FK_types_fields already exists'
GO
-----------fields to datatypes
IF NOT EXISTS( select * from sys.objects where name = 'FK_fields_datatypes')
	BEGIN
	ALTER TABLE fields 
	ADD CONSTRAINT FK_fields_datatypes FOREIGN KEY(data_type_id)
	REFERENCES datatypes(datatype_id)
	END
ELSE 
   print 'FK_fields_datatypes already exists'
GO

------REPORT RELATED TABLES
-----------------report_roles to reports
IF NOT EXISTS( select * from sys.objects where name = 'FK_report_roles_reports')
	BEGIN
	ALTER TABLE report_roles 
	ADD CONSTRAINT FK_report_roles_reports FOREIGN KEY(report_id)
	REFERENCES reports(report_id) ON DELETE CASCADE
	END
ELSE 
   print 'FK_report_roles_reports already exists'
GO
------------------ report_specific_users to reports
IF NOT EXISTS( select * from sys.objects where name = 'FK_report_specific_users_reports')
	BEGIN
	ALTER TABLE report_specific_users 
	ADD CONSTRAINT FK_report_specific_users_reports FOREIGN KEY(report_id)
	REFERENCES reports(report_id) ON DELETE CASCADE
	END
ELSE 
   print 'FK_report_specific_users_reports already exists'
GO
----------------report_params to reports
IF NOT EXISTS( select * from sys.objects where name = 'FK_report_params_reports')
	BEGIN
	ALTER TABLE report_params 
	ADD CONSTRAINT FK_report_params_reports FOREIGN KEY(report_id)
	REFERENCES reports(report_id) ON DELETE CASCADE
    END
ELSE 
   print 'FK_report_params_reports already exists'
GO
----------------gadget_user_config to reports
IF NOT EXISTS( select * from sys.objects where name = 'FK_gadget_user_config_reports')
	BEGIN
	ALTER TABLE gadget_user_config 
	ADD CONSTRAINT FK_gadget_user_config_reports FOREIGN KEY(id)
	REFERENCES reports(report_id) ON DELETE CASCADE
	END
ELSE 
   print 'FK_gadget_user_config_reports already exists'
GO
------------------gadget_user_params to reports
------------------gadget_user_params to gadget_user_config
/*IF NOT EXISTS( select * from sys.objects where name = 'FK_gadget_user_params_gadget_user_config')
	BEGIN
	ALTER TABLE gadget_user_params 
	ADD CONSTRAINT FK_gadget_user_params_gadget_user_config FOREIGN KEY(user_id,id)
	REFERENCES gadget_user_config(user_id,id) ON DELETE CASCADE
	END
ELSE 
   print 'FK_gadget_user_params_gadget_user_config already exists'
GO*/
------ROLES RELATED TABLES

------------------------roles to business-areas
IF NOT EXISTS( select * from sys.objects where name = 'FK_roles_business_areas')
	BEGIN
	ALTER TABLE roles 
	ADD CONSTRAINT FK_roles_business_areas FOREIGN KEY(sys_id)
	REFERENCES business_areas(sys_id) ON DELETE CASCADE
	END
ELSE 
   print 'FK_roles_business_areas already exists'
GO
-----------------------roles_users to roles
IF NOT EXISTS( select * from sys.objects where name = 'FK_roles_users_roles')
	BEGIN
	ALTER TABLE roles_users 
	ADD CONSTRAINT FK_roles_users_roles FOREIGN KEY(sys_id,role_id)
	REFERENCES roles(sys_id,role_id) ON DELETE CASCADE
	END
ELSE 
   print 'FK_roles_users_roles already exists'
GO
--------------------------roles_permissions to roles
IF NOT EXISTS( select * from sys.objects where name = 'FK_roles_permissions_roles')
	BEGIN
	ALTER TABLE roles_permissions 
	ADD CONSTRAINT FK_roles_permissions_roles FOREIGN KEY(sys_id,role_id)
	REFERENCES roles(sys_id,role_id) ON DELETE CASCADE
	END
ELSE 
   print 'FK_roles_permissions_roles already exists'
GO
-----------------USER RELATED TABLES

-----------request_users to Users
IF NOT EXISTS( select * from sys.objects where name = 'FK_request_users_users')
	BEGIN
	ALTER TABLE request_users 
	ADD CONSTRAINT FK_request_users_users FOREIGN KEY(user_id)
	REFERENCES users(user_id) 
	END
ELSE 
   print 'FK_request_users_users already exists'
GO
---------------action_users to users
IF NOT EXISTS( select * from sys.objects where name = 'FK_action_users_users')
	BEGIN
	ALTER TABLE action_users 
	ADD CONSTRAINT FK_action_users_users FOREIGN KEY(user_id)
	REFERENCES users(user_id)
	END
ELSE 
   print 'FK_action_users_users already exists'
GO
----------------roles_users to users
IF NOT EXISTS( select * from sys.objects where name = 'FK_roles_users_users')
	BEGIN
	ALTER TABLE roles_users 
	ADD CONSTRAINT FK_roles_users_users FOREIGN KEY(user_id)
	REFERENCES users(user_id)
	END
ELSE 
   print 'FK_roles_users_users already exists'
GO
----------------type_users to users
IF NOT EXISTS( select * from sys.objects where name = 'FK_type_users_users')
	BEGIN
	ALTER TABLE type_users 
	ADD CONSTRAINT FK_type_users_users FOREIGN KEY(user_id)
	REFERENCES users(user_id)
	END
ELSE 
   print 'FK_type_users_users already exists'
GO
-------------------Users to user passwords
IF NOT EXISTS( select * from sys.objects where name = 'FK_users_user_passwords')
	BEGIN
	ALTER TABLE user_passwords
	ADD CONSTRAINT FK_users_user_passwords FOREIGN KEY(user_login)
	REFERENCES user_passwords(user_login)
	END
ELSE 
   print 'FK_users_user_passwords already exists'
GO
-----------------------------


