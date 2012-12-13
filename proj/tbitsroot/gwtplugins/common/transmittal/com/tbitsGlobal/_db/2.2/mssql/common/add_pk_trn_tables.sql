--- ------adding the not nullable contraints for PK Ids
BEGIN
    Alter table trn_post_transmittal_field_values 
	Alter column src_sys_id int not null
	Alter table trn_process_parameters 
	Alter column src_sys_id int not null 
    Alter table trn_processes 
	Alter column src_sys_id int not null 
	Alter table trn_processes 
	Alter column trn_process_id int not null
    Alter table trn_src_target_field_mapping 
	Alter column trn_process_id int not null 
END
---transmittal  related tables

---------trn_attachment_selection_table_columns
if not Exists ( select * from sys.objects where name = 'PK_trn_attachment_selection_table_columns')
	BEGIN
	ALTER TABLE trn_attachment_selection_table_columns
	ADD CONSTRAINT PK_trn_attachment_selection_table_columns 
	PRIMARY KEY CLUSTERED (trn_process_id,field_id)
END
ELSE print 'PK_trn_attachment_selection_table_columns already exist'
GO
------------trn_distribution_table_column_config
if not Exists ( select * from sys.objects where name = 'PK_trn_distribution_table_column_config')
	BEGIN
	ALTER TABLE trn_distribution_table_column_config
	ADD CONSTRAINT PK_trn_distribution_table_column_config 
	PRIMARY KEY CLUSTERED (trn_process_id,name)
    END
ELSE Print 'PK_trn_distribution_table_column_config already exist'
GO
----------trn_drawing_number_field
if not Exists ( select * from sys.objects where name = 'PK_trn_drawing_number_field')
	BEGIN
	ALTER TABLE trn_drawing_number_field
	ADD CONSTRAINT PK_trn_drawing_number_field
	PRIMARY KEY CLUSTERED (sys_id)
    END 
ELSE print 'PK_trn_drawing_number_field already exist'
GO
---------- trn_max_serial
IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_max_serial]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
begin
if not Exists ( select * from sys.objects where name = 'PK_trn_max_serial')
	BEGIN
	ALTER TABLE trn_max_serial
	ADD CONSTRAINT PK_trn_max_serial
	PRIMARY KEY CLUSTERED (trn_max_sn_key)
    END
ELSE print 'PK_trn_max_serial already exist'
end
GO
-----------trn_post_transmittal_field_values
if not Exists ( select * from sys.objects where name = 'PK_trn_post_transmittal_field_values')
	BEGIN
	ALTER TABLE trn_post_transmittal_field_values
	ADD CONSTRAINT PK_trn_post_transmittal_field_values
	PRIMARY KEY CLUSTERED (src_sys_id,trn_process_id,target_sys_id,target_field_id)
	END
ELSE print 'PK_trn_post_transmittal_field_values already exist'
GO
---------------trn_process_parameters
if not Exists ( select * from sys.objects where name = 'PK_trn_process_parameters')
	BEGIN 
	ALTER TABLE trn_process_parameters
	ADD CONSTRAINT PK_trn_process_parameters
	PRIMARY KEY CLUSTERED (src_sys_id,trn_process_id,parameter)
	END
ELSE print 'PK_trn_process_parameters already exist'
GO
-----------------trn_processes
if not Exists ( select * from sys.objects where name = 'PK_trn_processes')
	BEGIN 
	ALTER TABLE trn_processes
	ADD CONSTRAINT PK_trn_processes
	PRIMARY KEY CLUSTERED (src_sys_id,trn_process_id)
	END
ELSE print 'PK_trn_processes already exist'
GO
-------------------trn_src_target_field_mapping
if not Exists ( select * from sys.objects where name = 'PK_trn_src_target_field_mapping')
	BEGIN
	ALTER TABLE trn_src_target_field_mapping
	ADD CONSTRAINT PK_trn_src_target_field_mapping
	PRIMARY KEY CLUSTERED (trn_process_id,src_sys_id,src_field_id,target_sys_id,target_field_id)
	END
ELSE print 'PK_trn_src_target_field_mapping already exist'
----------------------------