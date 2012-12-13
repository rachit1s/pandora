---Authors: Raghu & MM

---This script implements temporal DB structure in tBits database 
---for following tables:

/*

trn_drawing_number_field
trn_rolename_for_past_data_input_permission
trn_change_note_configuration
trn_change_note_field_map
trn_attachment_selection_table_columns

trn_dropdown
trn_post_transmittal_field_values
trn_processes
trn_src_target_field_mapping
trn_watermark_fields_info



-------------------------------------------------------------------------------------
---trn TABLES ON WHICH IMPLEMENTATION WAS NOT POSSIBLE DUE TO DATATYPE CONSTRAINT ARE:
-------------------------------------------------------------------------------------
  | Constrain of ntext column for magic table INSERTED|
  | --------------------------------------------------|
	
	trn_distribution_table_column_config
	trn_process_parameters  --- DATA TYPE MODIFIED TO NVARCHAR(MAX)  AND TEMPORAL TABLE CREATED
	trn_wizard_fields

--------------------------------------------------------------------------------------



*/




--$$$$$$$$   STEP 1: CREATING TEMPORAL_TRN_PROCESSES TABLE $$$$$$$$$$$$$$$$$$
--$$$$$$$$                                                 $$$$$$$$$$$$$$$$$$

IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_trn_processes')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_trn_processes](
	[src_sys_id] [int] NOT NULL,
	[trn_process_id] [int] NOT NULL,
	
	[description] nvarchar(50) NOT NULL,
	
	[trn_max_sn_key] nvarchar(50) NOT NULL,
	
	[dtn_sys_id] [int] NOT NULL,
	
	[dtr_sys_id] [int] NOT NULL,
	
	[trn_dropdown_id] [int] NOT NULL,
	
	[is_default] [bit] NULL,
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL
) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_trn_processes]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_trn_processes]'))
DROP TRIGGER [dbo].[audit_trn_processes]
GO

/****** Object:  Trigger [dbo].[audit_trn_processes]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_trn_processes] ON [dbo].[trn_processes] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_trn_processes
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_trn_processes 
 WHERE temporal_trn_processes.trn_process_id=deleted.trn_process_id
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_trn_processes(src_sys_id,trn_process_id,description,trn_max_sn_key,dtn_sys_id,dtr_sys_id,trn_dropdown_id,is_default,audit_StartDateTime, 
 audit_EndDateTime)

 SELECT src_sys_id,trn_process_id,description,trn_max_sn_key,dtn_sys_id,dtr_sys_id,trn_dropdown_id,is_default, @TrigTime , '9/9/9999'FROM INSERTED

GO
-----------------------------------------------------------------------------------------------

-- $$$$$$$$$$ STEP 1: CREATING TEMPORAL_TRN_PROCESS_PARAMETERS TABLE $$$$$$$$$$$$
-- $$$$$$$$$$                                                                   $$$$$$$$$$$$$$$$$$


alter table trn_process_parameters
alter column value nvarchar(max)
go

IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_trn_process_parameters')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_trn_process_parameters](
	[src_sys_id] [int] NOT NULL,
	[trn_process_id] [int] NOT NULL,
	[parameter] [nvarchar](50) NOT NULL,
	[value] [ntext] NULL,
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL
) ON [PRIMARY] 

END


SET ANSI_PADDING OFF
GO


SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_trn_processes_parameters]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_trn_process_parameters]'))
DROP TRIGGER [dbo].[audit_trn_process_parameters]
GO

CREATE TRIGGER [dbo].[audit_trn_process_parameters] ON [dbo].[trn_process_parameters] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE [temporal_trn_process_parameters]
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,[temporal_trn_process_parameters] 
 WHERE [temporal_trn_process_parameters].src_sys_id=deleted.src_sys_id
 AND temporal_trn_process_parameters.trn_process_id=deleted.trn_process_id
 and temporal_trn_process_parameters.parameter=deleted.parameter
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO [temporal_trn_process_parameters](src_sys_id,trn_process_id,parameter,value,audit_StartDateTime, 
 audit_EndDateTime)

 SELECT src_sys_id,trn_process_id,parameter,value, @TrigTime , '9/9/9999'FROM INSERTED



----------------------------------------------------------------------------------------------------------

-- $$$$$$$$$$ STEP 1: CREATING TEMPORAL_TRN_POST_TRANSMITTAL_FIELD_VALUES TABLE $$$$$$$$$$$$
-- $$$$$$$$$$                                                                   $$$$$$$$$$$$$$$$$$


IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_trn_post_transmittal_field_values')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_trn_post_transmittal_field_values](
	[src_sys_id] [int] NOT NULL,
	[trn_process_id] [int] NOT NULL,
	
	[target_sys_id] [int] NOT NULL,
	
	[target_field_id] [int] NOT NULL,
	
	[target_field_value] nvarchar(3500) NULL,
	
	[temp] nchar(10) NULL,
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL
) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_trn_post_transmittal_field_values]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_trn_post_transmittal_field_values]'))
DROP TRIGGER [dbo].[audit_trn_post_transmittal_field_values]
GO

/****** Object:  Trigger [dbo].[audit_trn_post_transmittal_field_values]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_trn_post_transmittal_field_values] ON [dbo].[trn_post_transmittal_field_values] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_trn_post_transmittal_field_values
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_trn_post_transmittal_field_values 
 WHERE temporal_trn_post_transmittal_field_values.trn_process_id=deleted.trn_process_id and temporal_trn_post_transmittal_field_values.src_sys_id = deleted.src_sys_id
 and temporal_trn_post_transmittal_field_values.target_sys_id = deleted.target_sys_id and temporal_trn_post_transmittal_field_values.target_field_id = deleted.target_field_id
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_trn_post_transmittal_field_values(src_sys_id,trn_process_id,target_sys_id,target_field_id,target_field_value,temp,audit_StartDateTime, 
 audit_EndDateTime)

 SELECT src_sys_id,trn_process_id,target_sys_id,target_field_id,target_field_value,temp, @TrigTime , '9/9/9999'FROM INSERTED

GO

---------------------------------------------------------

--$$$$$$$$  STEP 1: CREATING TEMPORAL_TRN_SRC_TARGET_FIELD_MAPPING TABLE $$$$$$$$$$$$$$$
--$$$$$$$$$                                                              $$$$$$$$$$$$$$$

IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_trn_src_target_field_mapping')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_trn_src_target_field_mapping](
	[trn_process_id] [int] NOT NULL,
	
	[src_sys_id] [int] NOT NULL,
	
	[src_field_id] [int] NOT NULL,
	
	[target_sys_id] [int] NOT NULL,
	
	[target_field_id] [int] NOT NULL,
	
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL
) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_trn_src_target_field_mapping]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_trn_src_target_field_mapping]'))
DROP TRIGGER [dbo].[audit_trn_src_target_field_mapping]
GO

/****** Object:  Trigger [dbo].[audit_trn_src_target_field_mapping]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_trn_src_target_field_mapping] ON [dbo].[trn_src_target_field_mapping] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_trn_src_target_field_mapping
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_trn_src_target_field_mapping 
 WHERE temporal_trn_src_target_field_mapping.trn_process_id=deleted.trn_process_id and temporal_trn_src_target_field_mapping.src_sys_id = deleted.src_sys_id
 and temporal_trn_src_target_field_mapping.target_sys_id = deleted.target_sys_id and temporal_trn_src_target_field_mapping.target_field_id = deleted.target_field_id
 and temporal_trn_src_target_field_mapping.src_field_id = deleted.src_field_id
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_trn_src_target_field_mapping(trn_process_id,src_sys_id,src_field_id,target_sys_id,target_field_id,audit_StartDateTime, 
 audit_EndDateTime)

 SELECT trn_process_id,src_sys_id,src_field_id,target_sys_id,target_field_id, @TrigTime , '9/9/9999'FROM INSERTED

GO

----------------------------------------------

--$$$$$$$$$$   STEP 1: CREATING TEMPORAL_TRN_ATTACHMENT_SELECTION_TABLE_COLUMNS $$$$$$$$$$$$$

IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_trn_attachment_selection_table_columns')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_trn_attachment_selection_table_columns](
	[trn_process_id] [int] NOT NULL,
	
	[name] nvarchar(50) NOT NULL,
	
	[field_id] [int] NOT NULL,
	
	[data_type_id] [int] NOT NULL,
	
	[default_value] nvarchar(50) NULL,
	
	[is_editable] [bit] NOT NULL,
	
	[is_active] [bit] NOT NULL,
	
	[column_order] [int] NOT NULL,
	
	[type_value_source] [int] NOT NULL,
	
	[is_included] [bit] NOT NULL,
	
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL
) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_trn_attachment_selection_table_columns]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_trn_attachment_selection_table_columns]'))
DROP TRIGGER [dbo].[audit_trn_attachment_selection_table_columns]
GO

/****** Object:  Trigger [dbo].[audit_trn_attachment_selection_table_columns]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_trn_attachment_selection_table_columns] ON [dbo].[trn_attachment_selection_table_columns] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_trn_attachment_selection_table_columns
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_trn_attachment_selection_table_columns 
 WHERE temporal_trn_attachment_selection_table_columns.trn_process_id=deleted.trn_process_id
 and temporal_trn_attachment_selection_table_columns.field_id = deleted.field_id
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_trn_attachment_selection_table_columns(trn_process_id,name,field_id,data_type_id,default_value,is_editable,is_active,column_order,
 type_value_source,is_included,audit_StartDateTime,audit_EndDateTime)

 SELECT trn_process_id,name,field_id,data_type_id,default_value,is_editable,is_active,column_order,
 type_value_source,is_included, @TrigTime , '9/9/9999'FROM INSERTED

GO



------------------------------------------------------

--$$$$$$$$    STEP 1: CREATING TEMPORAL_TRN_DRAWING_NUMBER_FIELD $$$$$$$$$$$$$$

IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_trn_drawing_number_field')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_trn_drawing_number_field](
	[sys_id] [int] NOT NULL,
	
	[field_name] nvarchar(100) NOT NULL,
	
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL
) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_trn_drawing_number_field]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_trn_drawing_number_field]'))
DROP TRIGGER [dbo].[audit_trn_drawing_number_field]
GO

/****** Object:  Trigger [dbo].[audit_trn_drawing_number_field]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_trn_drawing_number_field] ON [dbo].[trn_drawing_number_field] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_trn_drawing_number_field
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_trn_drawing_number_field 
 WHERE temporal_trn_drawing_number_field.sys_id=deleted.sys_id
 and temporal_trn_drawing_number_field.field_name = deleted.field_name
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_trn_drawing_number_field(sys_id,field_name,audit_StartDateTime,audit_EndDateTime)

 SELECT sys_id,field_name, @TrigTime , '9/9/9999'FROM INSERTED

GO

----------------------------------------

--$$$$$$$$$$   STEP 1: CREATING TEMPORAL_TRN_DROPDOWN  $$$$$$$$$$$$$$
--$$$$$$$$$$                                           $$$$$$$$$$$$$$


IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_trn_dropdown')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_trn_dropdown](
	[src_sys_id] [int] NOT NULL,
	
	[id] [int] NOT NULL,
	
	[name] nvarchar(100) NOT NULL,
	
	[sort_order] [int] NOT NULL,
	
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL
) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_trn_dropdown]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_trn_dropdown]'))
DROP TRIGGER [dbo].[audit_trn_dropdown]
GO

/****** Object:  Trigger [dbo].[audit_trn_dropdown]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_trn_dropdown] ON [dbo].[trn_dropdown] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_trn_dropdown
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_trn_dropdown 
 WHERE temporal_trn_dropdown.src_sys_id=deleted.src_sys_id
 and temporal_trn_dropdown.id = deleted.id
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_trn_dropdown(src_sys_id,id,name,sort_order,audit_StartDateTime,audit_EndDateTime)

 SELECT src_sys_id,id,name,sort_order, @TrigTime , '9/9/9999'FROM INSERTED

GO

--------------------------------------------------

--$$$$$$$$   STEP 1: CREATING TEMPORAL_TRN_WATERMARK_FIELDS_INFO  $$$$$$$$$$$
--$$$$$$$$                                                        $$$$$$$$$$$


IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_trn_watermark_fields_info')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_trn_watermark_fields_info](
	[sys_id] [int] NOT NULL,
	
	[src_field_id] [int] NOT NULL,
	
	[target_field_id] [int] NOT NULL,
	
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL
) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_trn_watermark_fields_info]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_trn_watermark_fields_info]'))
DROP TRIGGER [dbo].[audit_trn_watermark_fields_info]
GO

/****** Object:  Trigger [dbo].[audit_trn_watermark_fields_info]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_trn_watermark_fields_info] ON [dbo].[trn_watermark_fields_info] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_trn_watermark_fields_info
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_trn_watermark_fields_info 
 WHERE temporal_trn_watermark_fields_info.sys_id=deleted.sys_id
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_trn_watermark_fields_info(sys_id,src_field_id,target_field_id,audit_StartDateTime,audit_EndDateTime)

 SELECT sys_id,src_field_id,target_field_id, @TrigTime , '9/9/9999'FROM INSERTED

GO

--------------------------------------------------------------

--$$$$$$$$$$   STEP 1: CREATING TEMPORAL_TRN_CHANGE_NOTE_CONFIGURATION $$$$$$$$$$
--$$$$$$$$$$                                                           $$$$$$$$$$

---Checking whether change_node_id column exits for not.


IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME LIKE 'trn_change_note_configuration' 
and COLUMN_NAME like 'change_note_id')
BEGIN
RETURN
END

ELSE

BEGIN

ALTER TABLE trn_change_note_configuration
ADD change_note_id int
end

IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_trn_change_note_configuration')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_trn_change_note_configuration](
	[change_note_id] [int] NULL,
	
	[src_sys_prefix] nvarchar(50) NOT NULL,
	
	[ba_type] nvarchar(50) NOT NULL,
	
	[target_sys_prefix] nvarchar(50) NOT NULL,
	
	[caption] nvarchar(100) NULL,
	
	[template_name] nvarchar(300) NULL,
	
	[src_attachment_field_id] [int] NOT NULL,
	
	[target_attachment_field_id] [int]	NOT NULL, 
	
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL
) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_trn_change_note_configuration]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_trn_change_note_configuration]'))
DROP TRIGGER [dbo].[audit_trn_change_note_configuration]
GO

/****** Object:  Trigger [dbo].[audit_trn_change_note_configuration]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_trn_change_note_configuration] ON [dbo].[trn_change_note_configuration] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_trn_change_note_configuration
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_trn_change_note_configuration 
 WHERE temporal_trn_change_note_configuration.change_note_id=deleted.change_note_id
 and temporal_trn_change_note_configuration.src_sys_prefix = deleted.src_sys_prefix
 and temporal_trn_change_note_configuration.target_sys_prefix = deleted.target_sys_prefix
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_trn_change_note_configuration(change_note_id,src_sys_prefix,ba_type,target_sys_prefix,
 caption,template_name,src_attachment_field_id,target_attachment_field_id,audit_StartDateTime,audit_EndDateTime)

 SELECT change_note_id,src_sys_prefix,ba_type,target_sys_prefix,
 caption,template_name,src_attachment_field_id,target_attachment_field_id, @TrigTime , '9/9/9999'FROM INSERTED

GO

-------------------------------------------------------------------

--$$$$$$$$$$$   STEP 1: CREATING TEMPORAL_TRN_CHANGE_NOTE_FIELD_MAP $$$$$$$$$$
--$$$$$$$$$$$                                                       $$$$$$$$$$


IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_trn_change_note_field_map')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_trn_change_note_field_map](
	[src_sys_id] [int] NOT NULL,
	
	[template_field_name] nvarchar(100) NOT NULL,
	
	[field_name] nvarchar(100) NOT NULL,
	
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL
) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_trn_change_note_field_map]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_trn_change_note_field_map]'))
DROP TRIGGER [dbo].[audit_trn_change_note_field_map]
GO

/****** Object:  Trigger [dbo].[audit_trn_change_note_field_map]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_trn_change_note_field_map] ON [dbo].[trn_change_note_field_map] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_trn_change_note_field_map
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_trn_change_note_field_map 
 WHERE temporal_trn_change_note_field_map.src_sys_id=deleted.src_sys_id
 and temporal_trn_change_note_field_map.field_name = deleted.field_name
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_trn_change_note_field_map(src_sys_id,template_field_name,field_name,audit_StartDateTime,audit_EndDateTime)

 SELECT src_sys_id,template_field_name,field_name, @TrigTime , '9/9/9999'FROM INSERTED

GO

----------------------------------------------

--$$$$$$$$$  STEP 1: CREATING TEMPORAL_TRN_ROLENAME_FOR_PAST_DATA_INPUT_PERMISSION  $$$$$$$$$$$
--$$$$$$$$$                                                                         $$$$$$$$$$$


IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_trn_rolename_for_past_data_input_permission')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_trn_rolename_for_past_data_input_permission](
	[sys_id] [int] NOT NULL,
	
	[role_name] nvarchar(100) NOT NULL,
	
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL
) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_trn_rolename_for_past_data_input_permission]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_trn_rolename_for_past_data_input_permission]'))
DROP TRIGGER [dbo].[audit_trn_rolename_for_past_data_input_permission]
GO

/****** Object:  Trigger [dbo].[audit_trn_rolename_for_past_data_input_permission]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_trn_rolename_for_past_data_input_permission] ON [dbo].[trn_rolename_for_past_data_input_permission] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_trn_rolename_for_past_data_input_permission
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_trn_rolename_for_past_data_input_permission
 WHERE temporal_trn_rolename_for_past_data_input_permission.sys_id=deleted.sys_id
 and temporal_trn_rolename_for_past_data_input_permission.role_name = deleted.role_name
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_trn_rolename_for_past_data_input_permission(sys_id,role_name,audit_StartDateTime,audit_EndDateTime)

 SELECT sys_id,role_name, @TrigTime , '9/9/9999'FROM INSERTED

GO

