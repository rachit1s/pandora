/****** Object:  Table [dbo].[trn_dropdown]    Script Date: 12/15/2010 19:07:20 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_dropdown]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[trn_dropdown](
		[src_sys_id] [int] NOT NULL,
		[id] [int] NOT NULL,
		[name] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
		[sort_order] [int] NOT NULL CONSTRAINT [DF_transmittal_processes_sort_order]  DEFAULT ((0))
	) ON [PRIMARY]
END
GO
/****** Object:  Table [dbo].[trn_processes]    Script Date: 12/15/2010 19:07:41 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_processes]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[trn_processes](
		[src_sys_id] [int] NULL,
		[trn_process_id] [int] NULL,
		[description] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
		[trn_max_sn_key] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
		[dtn_sys_id] [int] NULL,
		[dtr_sys_id] [int] NULL,
		[trn_dropdown_id] [int] NULL
	) ON [PRIMARY]
END
GO
/****** Object:  Table [dbo].[trn_process_parameters]    Script Date: 12/15/2010 19:09:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_process_parameters]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[trn_process_parameters](
		[src_sys_id] [int] NULL,
		[trn_process_id] [int] NOT NULL,
		[parameter] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
		[value] [ntext] COLLATE SQL_Latin1_General_CP1_CI_AS NULL
	) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
/****** Object:  Table [dbo].[trn_post_transmittal_field_values]    Script Date: 12/15/2010 19:15:17 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_post_transmittal_field_values]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[trn_post_transmittal_field_values](
		[src_sys_id] [int] NULL,
		[trn_process_id] [int] NOT NULL,
		[target_sys_id] [int] NOT NULL,
		[target_field_id] [int] NOT NULL,
		[target_field_value] [nvarchar](3500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
		[temp] [int] NULL
	) ON [PRIMARY]
END
GO
/****** Object:  Table [dbo].[trn_src_target_field_mapping]    Script Date: 12/15/2010 19:08:07 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_src_target_field_mapping]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[trn_src_target_field_mapping](
		[trn_process_id] [int] NULL,
		[src_sys_id] [int] NOT NULL,
		[src_field_id] [int] NOT NULL,
		[target_sys_id] [int] NOT NULL,
		[target_field_id] [int] NOT NULL
	) ON [PRIMARY]
END
GO
/****** Object:  Table [dbo].[trn_attachment_selection_table_columns]    Script Date: 12/15/2010 19:08:31 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_attachment_selection_table_columns]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[trn_attachment_selection_table_columns](
		[trn_process_id] [int] NOT NULL,
		[name] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
		[field_id] [int] NOT NULL,
		[data_type_id] [int] NOT NULL,
		[default_value] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
		[is_editable] [bit] NOT NULL CONSTRAINT [DF_trn_attachment_selection_table_columns_is_editor_enabled]  DEFAULT ((1)),
		[is_active] [bit] NOT NULL CONSTRAINT [DF_trn_attachment_selection_table_columns_is_active]  DEFAULT ((1)),
		[column_order] [int] NOT NULL CONSTRAINT [DF_trn_attachment_selection_table_columns_column_order]  DEFAULT ((1)),
		[type_value_source] [int] NOT NULL DEFAULT ((0)),
		[is_included] [bit] NOT NULL DEFAULT ((0))
	) ON [PRIMARY]
END
GO
/****** Object:  Table [dbo].[trn_distribution_table_column_config]    Script Date: 12/15/2010 19:08:56 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_distribution_table_column_config]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[trn_distribution_table_column_config](
		[trn_process_id] [int] NOT NULL,
		[name] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
		[display_name] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
		[data_type_id] [int] NOT NULL,
		[field_config] [ntext] COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
		[is_editable] [bit] NOT NULL,
		[is_active] [bit] NOT NULL,
		[column_order] [int] NOT NULL CONSTRAINT [DF_trn_distribution_table_column_config_column_order]  DEFAULT ((1))
	) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
/****** Object:  Table [dbo].[trn_approval_cycle_transient_data]    Script Date: 12/15/2010 19:09:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_approval_cycle_transient_data]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[trn_approval_cycle_transient_data](
		[sys_id] [int] NOT NULL,
		[request_id] [int] NOT NULL,
		[parameter] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
		[value] [ntext] COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	 CONSTRAINT [PK_trn_approval_cycle_transient_data] PRIMARY KEY CLUSTERED 
	(
		[sys_id] ASC,
		[request_id] ASC,
		[parameter] ASC
	)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
	) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
/****** Object:  Table [dbo].[trn_approval_cycle_transient_data_attachments]    Script Date: 12/15/2010 19:09:22 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_approval_cycle_transient_data_attachments]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[trn_approval_cycle_transient_data_attachments](
		[sys_id] [int] NOT NULL,
		[request_id] [int] NOT NULL,
		[src_request_id] [int] NOT NULL,
		[fieldName] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
		[attachments] [ntext] COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	 CONSTRAINT [PK_trn_approval_cycle_transient_data_attachments] PRIMARY KEY CLUSTERED 
	(
		[sys_id] ASC,
		[request_id] ASC,
		[src_request_id] ASC,
		[fieldName] ASC
	)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
	) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
/****** Object:  Table [dbo].[trn_wizard_fields]    Script Date: 12/15/2010 19:12:45 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_wizard_fields]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[trn_wizard_fields](
		[trn_process_id] [int] NOT NULL,
		[name] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
		[field_id] [int] NOT NULL,
		[field_config] [ntext] COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
		[is_editable] [bit] NOT NULL,
		[is_active] [bit] NOT NULL,
		[field_order] [int] NOT NULL CONSTRAINT [DF_trn_wizard_fields_field_order]  DEFAULT ((1)),
		[is_dtn_number_part] [bit] NOT NULL CONSTRAINT [DF_trn_wizard_fields_is_dtn_number_part]  DEFAULT ((0))
	) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END

GO
/****** Object:  Table [dbo].[trn_validation_rules]    Script Date: 12/15/2010 19:13:22 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_validation_rules]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[trn_validation_rules](
		[trn_process_id] [int] NOT NULL,
		[field_id] [int] NOT NULL,
		[value] [ntext] COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL
	) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END
GO
/****** Object:  Table [dbo].[trn_rolename_for_past_data_input_permission]    Script Date: 10/05/2010 00:04:54 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_rolename_for_past_data_input_permission]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[trn_rolename_for_past_data_input_permission](
		[sys_id] [int] NOT NULL,
		[role_name] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL
	) ON [PRIMARY]
END
GO
/****** Object:  Table [dbo].[trn_watermark_fields_info]    Script Date: 12/15/2010 19:12:27 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_watermark_fields_info]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[trn_watermark_fields_info](
		[sys_id] [int] NOT NULL,
		[src_field_id] [int] NOT NULL,
		[target_field_id] [int] NOT NULL,
	 CONSTRAINT [PK_trn_watermark_fields_info] PRIMARY KEY CLUSTERED 
	(
		[sys_id] ASC,
		[src_field_id] ASC,
		[target_field_id] ASC
	)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
	) ON [PRIMARY]
END
GO
/****** Object:  Table [dbo].[trn_drawing_number_field]    Script Date: 12/15/2010 19:09:38 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_drawing_number_field]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[trn_drawing_number_field](
		[sys_id] [int] NOT NULL,
		[field_name] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	 CONSTRAINT [PK_trn_drawing_number_field] PRIMARY KEY CLUSTERED 
	(
		[sys_id] ASC,
		[field_name] ASC
	)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
	) ON [PRIMARY]
END
GO
/****** Object:  Table [dbo].[trn_dtn_attachment_mapping]    Script Date: 02/10/2012 04:30:38 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_dtn_attachment_mapping]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[trn_dtn_attachment_mapping](
		[trn_process_id] [int] NOT NULL,
		[src_field_id] [int] NOT NULL,
                [target_field_id] [int] NOT NULL,
	) ON [PRIMARY]
END
GO
