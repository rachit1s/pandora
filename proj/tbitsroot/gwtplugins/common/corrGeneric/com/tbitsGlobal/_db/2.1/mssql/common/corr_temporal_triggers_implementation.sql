---Authors: Raghu & MM

---This script implements temporal DB structure in tBits database 
---for following tables:


/*
corr_number_config
corr_ba_field_map
corr_field_name_map
corr_onbehalf_map

corr_report_map
corr_report_name_map
corr_report_params_map
corr_user_map

*/




---$$$$$$$$   corr_number_config  $$$$$$$$$------

--STEP 1: CREATING TEMPORAL_MAIL_LIST_USERS TABLE

IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_corr_number_config')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_corr_number_config](
	[id] [int] NOT NULL,
	[sys_prefix] varchar(32) NULL,
	num_type1	varchar(255) NULL,
	num_type2	varchar(255) NULL,
	num_type3	varchar(255) NULL,
	num_format	varchar(1023) NULL,
	num_fields	varchar(1023) NULL,
	max_id_format	varchar(1023) NULL,
	max_id_fields	varchar(1023) NULL,
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL

	) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_corr_number_config]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_corr_number_config]'))
DROP TRIGGER [dbo].[audit_corr_number_config]
GO

/****** Object:  Trigger [dbo].[audit_corr_number_config]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_corr_number_config] ON [dbo].[corr_number_config] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_corr_number_config
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_corr_number_config
 WHERE temporal_corr_number_config.id=deleted.id and temporal_corr_number_config.sys_prefix=deleted.sys_prefix
 AND temporal_corr_number_config.num_type1=deleted.num_type1 and temporal_corr_number_config.num_type2=deleted.num_type2
 AND temporal_corr_number_config.num_type3=deleted.num_type3
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_corr_number_config(id,sys_prefix,num_type1,num_type2,
 num_type3,num_format,num_fields,max_id_format,max_id_fields,audit_StartDateTime, 
 audit_EndDateTime)

 SELECT id,sys_prefix,num_type1,num_type2,num_type3,num_format,num_fields,
 max_id_format,max_id_fields, @TrigTime , '9/9/9999'FROM INSERTED

GO





---$$$$$$$$   corr_ba_field_map  $$$$$$$$$------

--STEP 1: CREATING corr_ba_field_map TABLE

IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_corr_ba_field_map')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_corr_ba_field_map](
	[id] [int] NOT NULL,
	[from_sys_prefix] varchar(32) NULL,
	[from_field_name]	varchar(100) NULL,
	[to_sys_prefix]	varchar(32) NULL,
	[to_field_name]	varchar(100) NULL,
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL

	) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_corr_ba_field_map]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_corr_ba_field_map]'))
DROP TRIGGER [dbo].[audit_corr_ba_field_map]
GO

/****** Object:  Trigger [dbo].[audit_corr_ba_field_map]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_corr_ba_field_map] ON [dbo].[corr_ba_field_map] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_corr_ba_field_map
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_corr_ba_field_map
 WHERE temporal_corr_ba_field_map.id=deleted.id
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_corr_ba_field_map(id,from_sys_prefix,from_field_name,to_sys_prefix,
 to_field_name,audit_StartDateTime, 
 audit_EndDateTime)

 SELECT id,from_sys_prefix,from_field_name,to_sys_prefix,
 to_field_name, @TrigTime , '9/9/9999'FROM INSERTED

GO





---$$$$$$$$   corr_field_name_map  $$$$$$$$$------

--STEP 1: CREATING temporal_corr_field_name_map TABLE

IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_corr_field_name_map')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_corr_field_name_map](
	[id] [int] NOT NULL,
	[corr_field_name] varchar(100) NULL,
	[sys_prefix]	varchar(32) NULL,
	[field_name]	varchar(100) NULL,
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL

	) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_corr_field_name_map]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_corr_field_name_map]'))
DROP TRIGGER [dbo].[audit_corr_field_name_map]
GO

/****** Object:  Trigger [dbo].[audit_corr_ba_field_map]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_corr_field_name_map] ON [dbo].[corr_field_name_map] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_corr_field_name_map
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_corr_field_name_map
 WHERE temporal_corr_field_name_map.id=deleted.id
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_corr_field_name_map(id,corr_field_name,sys_prefix,field_name,
 audit_StartDateTime, audit_EndDateTime)

 SELECT id,corr_field_name,sys_prefix,field_name, @TrigTime , '9/9/9999'FROM INSERTED

GO





---$$$$$$$$   corr_onbehalf_map  $$$$$$$$$------

--STEP 1: CREATING temporal_corr_onbehalf_map TABLE

IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_corr_onbehalf_map')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_corr_onbehalf_map](
	[id] [int] NOT NULL,
	[sys_prefix]	varchar(32) NULL,
	[user_login] varchar(100),
	[onbehalf_type1] varchar(100),
	[onbehalf_type2] varchar(100),
	[onbehalf_type3] varchar(100),
	[onbehalf_of_login] varchar(100),
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL

	) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_corr_onbehalf_map]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_corr_onbehalf_map]'))
DROP TRIGGER [dbo].[audit_corr_onbehalf_map]
GO

/****** Object:  Trigger [dbo].[audit_corr_ba_field_map]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_corr_onbehalf_map] ON [dbo].[corr_onbehalf_map] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_corr_onbehalf_map
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_corr_onbehalf_map
 WHERE temporal_corr_onbehalf_map.id=deleted.id
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_corr_onbehalf_map(id,sys_prefix,user_login,onbehalf_type1,onbehalf_type2,onbehalf_type3,
 onbehalf_of_login,
 audit_StartDateTime, audit_EndDateTime)

 SELECT id,sys_prefix,user_login,onbehalf_type1,onbehalf_type2,onbehalf_type3,
 onbehalf_of_login, @TrigTime , '9/9/9999'FROM INSERTED

GO





---$$$$$$$$   corr_report_map  $$$$$$$$$------

--STEP 1: CREATING temporal_corr_onbehalf_map TABLE

IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_corr_report_map')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_corr_report_map](
	[id] [int] NOT NULL,
	[sys_prefix]	varchar(32) NULL,
	[report_type1] varchar(100),
	[report_type2] varchar(100),
	[report_type3] varchar(100),
	[report_type4] varchar(100),
	[report_type5] varchar(100),
	[report_id]	int NOT NULL,
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL

	) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_corr_onbehalf_map]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_corr_report_map]'))
DROP TRIGGER [dbo].[audit_corr_report_map]
GO

/****** Object:  Trigger [dbo].[audit_corr_report_map]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_corr_report_map] ON [dbo].[corr_report_map] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_corr_report_map
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_corr_report_map
 WHERE temporal_corr_report_map.id=deleted.id
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_corr_report_map(id,sys_prefix,report_type1,report_type2,report_type3,report_type4,
 report_type5,report_id,
 audit_StartDateTime, audit_EndDateTime)

 SELECT id,sys_prefix,report_type1,report_type2,report_type3,report_type4,
 report_type5,report_id, @TrigTime , '9/9/9999'FROM INSERTED

GO




---$$$$$$$$   corr_report_name_map  $$$$$$$$$------

--STEP 1: CREATING temporal_corr_report_name_map TABLE

IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_corr_report_name_map')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_corr_report_name_map](
	[id] [int] NOT NULL,
	[report_id] int NOT NULL ,
	[report_file_name] varchar(255) NOT NULL,
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL

	) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_corr_report_name_map]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_corr_report_name_map]'))
DROP TRIGGER [dbo].[audit_corr_report_name_map]
GO

/****** Object:  Trigger [dbo].[audit_corr_report_name_map]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_corr_report_name_map] ON [dbo].[corr_report_name_map] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_corr_report_name_map
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_corr_report_name_map
 WHERE temporal_corr_report_name_map.id=deleted.id
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_corr_report_name_map(id,report_id,report_file_name,
 audit_StartDateTime, audit_EndDateTime)

 SELECT id,report_id,report_file_name, @TrigTime , '9/9/9999'FROM INSERTED

GO



---$$$$$$$$   corr_report_params_map  $$$$$$$$$------

--STEP 1: CREATING temporal_corr_report_params_map TABLE

IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_corr_report_params_map')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_corr_report_params_map](
	[id] [int] NOT NULL,
	[report_id] int NOT NULL ,
	[param_type] varchar(32) NOT NULL,
	[param_name] varchar(511) NOT NULL,
	[param_value_type] varchar(32) NOT NULL,
	[param_value] varchar(1024),
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL

	) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_corr_report_params_map]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_corr_report_params_map]'))
DROP TRIGGER [dbo].[audit_corr_report_params_map]
GO

/****** Object:  Trigger [dbo].[audit_corr_report_params_map]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_corr_report_params_map] ON [dbo].[corr_report_params_map] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_corr_report_params_map
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_corr_report_params_map
 WHERE temporal_corr_report_params_map.id=deleted.id
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_corr_report_params_map(id,report_id,param_type,
 param_name,param_value_type,param_value,
 audit_StartDateTime, audit_EndDateTime)

 SELECT id,report_id,param_type,
 param_name,param_value_type,param_value, @TrigTime , '9/9/9999'FROM INSERTED

GO







---$$$$$$$$   corr_user_map  $$$$$$$$$------

--STEP 1: CREATING temporal_corr_user_map TABLE

IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'temporal_corr_user_map')

BEGIN
RETURN
END

ELSE

BEGIN

CREATE TABLE [dbo].[temporal_corr_user_map](
	[id] [int] NOT NULL,
	[sys_prefix] varchar(32) NOT NULL,
	[user_login] VARCHAR(100) NOT NULL,	
	[user_map_type1] VARCHAR(100),
	[user_map_type2] VARCHAR(100),
	[user_map_type3] VARCHAR(100),
	[user_type_field_name] VARCHAR(100) NOT NULL,
	[strictness] INT NOT NULL,
	[audit_StartDateTime] [datetime] NULL,
	[audit_EndDateTime] [datetime] NULL

	) ON [PRIMARY]

END

SET ANSI_PADDING OFF
GO


---STEP 2: CREATING TRIGGER
/****** Object:  Trigger [audit_corr_user_map]    Script Date: 03/27/2012 12:44:04 ******/
IF  EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[audit_corr_user_map]'))
DROP TRIGGER [dbo].[audit_corr_user_map]
GO

/****** Object:  Trigger [dbo].[audit_corr_user_map]    Script Date: 03/27/2012 12:44:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[audit_corr_user_map] ON [dbo].[corr_user_map] 
 FOR INSERT, UPDATE, DELETE NOT FOR REPLICATION 
 As 
DECLARE  
 @TrigTime DateTime  
 set @TrigTime = getDate() 
 UPDATE temporal_corr_user_map
 SET  audit_EndDateTime = (@TrigTime) 
 FROM
 deleted,temporal_corr_user_map
 WHERE temporal_corr_user_map.id=deleted.id
 AND
 audit_EndDateTime = '9/9/9999'
 
 INSERT INTO temporal_corr_user_map(id,sys_prefix,user_login,
 user_map_type1,user_map_type2,user_map_type3,user_type_field_name,strictness,
 audit_StartDateTime, audit_EndDateTime)

 SELECT id,sys_prefix,user_login,
 user_map_type1,user_map_type2,user_map_type3,user_type_field_name,strictness, @TrigTime , '9/9/9999'FROM INSERTED

GO

