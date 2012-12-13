--- ------adding the not nullable contraints for PK Ids
BEGIN
    Alter table ba_mail_accounts
    alter Column ba_mail_ac_id int not null

    Alter table escalation_heirarchy 
	Alter column sys_id int not null 
	Alter table escalation_heirarchy
	alter column user_id int not null
	Alter table escalation_heirarchy
	alter column parent_user_id int not null
    Alter table escalation_history 
	Alter column sys_id int not null 
    Alter table escalation_history
	alter column request_id int not null
    Alter table report_params 
	Alter column report_id int not null  
	Alter table report_params
	alter column param_name varchar(200) not null
END
-- -----------------ba_menu_table
if not Exists ( select * from sys.objects where name = 'PK_ba_menu_table')
	BEGIN
	ALTER TABLE ba_menu_table
	ADD CONSTRAINT PK_ba_menu_table PRIMARY KEY CLUSTERED (menu_id)
	END
else 
	print 'PK_ba_menu_table already exists'

GO
------captions_properties
if not Exists ( select * from sys.objects where name = 'PK_captions_properties')
	BEGIN
	ALTER TABLE captions_properties
	ADD CONSTRAINT PK_captions_properties PRIMARY KEY CLUSTERED (sys_id,name)
	END
else
	print ' PK_captions_properties already exist'
GO
---------escalation_heirarchy (adding Not null constaints
if not Exists ( select * from sys.objects where name = 'PK_escalation_heirarchy')
	BEGIN
	ALTER TABLE escalation_heirarchy
	ADD CONSTRAINT PK_escalation_heirarchy  PRIMARY KEY CLUSTERED (sys_id,user_id,parent_user_id)
    END
	else print 'PK_escalation_heirarchy already exist'
GO
------escalation_history
if not Exists ( select * from sys.objects where name = 'PK_escalation_history')
	BEGIN
	------
	ALTER TABLE escalation_history
	ADD CONSTRAINT PK_escalation_history  PRIMARY KEY CLUSTERED (sys_id,request_id)
	END
else print 'PK_escalation_history is already exist'
GO
----------gadget_user_config
if not Exists ( select * from sys.objects where name = 'PK_gadget_user_config')
	BEGIN
	ALTER TABLE gadget_user_config
	ADD CONSTRAINT PK_gadget_user_config  PRIMARY KEY CLUSTERED (user_id,id)
	END
else print'PK_gadget_user_config already exist'
GO
--------------gadget_user_params
if not Exists ( select * from sys.objects where name = 'PK_gadget_user_params')
	BEGIN
	ALTER TABLE gadget_user_params
	ADD CONSTRAINT PK_gadget_user_params  PRIMARY KEY CLUSTERED (user_id,id,name)
	END
else print 'PK_gadget_user_params already exist'
GO
----mail_list_users
if not Exists ( select * from sys.objects where name = 'PK_mail_list_users')
	BEGIN
	ALTER TABLE mail_list_users
	ADD CONSTRAINT PK_mail_list_users  PRIMARY KEY CLUSTERED (mail_list_id,user_id)
	END
else print 'PK_mail_list_users already exist'
GO
----report_params
if not Exists ( select * from sys.objects where name = 'PK_report_params')
	BEGIN
	ALTER TABLE report_params
	ADD CONSTRAINT PK_report_params PRIMARY KEY CLUSTERED (report_id,param_name)
	END
else print 'PK_report_params already exist'
GO
-----report_roles
if not Exists ( select * from sys.objects where name = 'PK_report_roles')
	BEGIN
	ALTER TABLE report_roles
	ADD CONSTRAINT PK_report_roles PRIMARY KEY CLUSTERED (sys_id,report_id,role_id)
	END
else print 'PK_report_roles already exist'
GO
----report_specific_users
if not Exists ( select * from sys.objects where name = 'PK_report_specific_users')
	BEGIN
	ALTER TABLE report_specific_users
	ADD CONSTRAINT PK_report_specific_users PRIMARY KEY CLUSTERED (report_id,user_id)
	END
else print 'PK_report_specific_users already exist'
GO
-------------------------reports
if not Exists ( select * from sys.objects where name = 'PK_reports')
BEGIN
ALTER TABLE reports
ADD CONSTRAINT PK_reports PRIMARY KEY CLUSTERED (report_id)
END
else print 'PK_reports already exist'
GO
-----------------------tbits_properties
if not Exists ( select * from sys.objects where name = 'PK_tbits_properties')
	BEGIN
	ALTER TABLE tbits_properties
	ADD CONSTRAINT PK_tbits_properties PRIMARY KEY CLUSTERED (name)
    END
else print 'PK_tbits_properties already exist'
---------------------------ba Mail Account
if not Exists ( select * from sys.objects where name = 'PK_ba_mail_accounts')
	BEGIN
	ALTER TABLE ba_mail_accounts
	ADD CONSTRAINT PK_ba_mail_accounts PRIMARY KEY CLUSTERED (ba_mail_ac_id)
	END
else 
	print 'PK_ba_mail_accounts already exists'

GO
