/****** Object:  Table [dbo].[trn_change_note_configuration]    Script Date: 11/23/2010 12:06:19 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_change_note_configuration]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[trn_change_note_configuration](
		[src_sys_prefix] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
		[ba_type] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
		[target_sys_prefix] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
		[caption] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
		[template_name] [nvarchar](300) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
		[src_attachment_field_id] [int] NOT NULL,
		[target_attachment_field_id] [int] NOT NULL CONSTRAINT [DF_trn_change_note_ba_map_target_field_id]  DEFAULT ((0)),
	 CONSTRAINT [PK_trn_change_note_configuration] PRIMARY KEY CLUSTERED 
	(
		[src_sys_prefix] ASC,
		[target_sys_prefix] ASC
	)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
	) ON [PRIMARY]
END

GO
/****** Object:  Table [dbo].[trn_change_note_field_map]    Script Date: 11/23/2010 12:06:45 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[trn_change_note_field_map]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[trn_change_note_field_map](
		[src_sys_id] [int] NOT NULL,
		[template_field_name] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
		[field_name] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL
	) ON [PRIMARY]
END
GO
/****** Object:  Table [dbo].[change_note_source_target_field_map]    Script Date: 12/11/2010 13:16:56 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[change_note_source_target_field_map]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[change_note_source_target_field_map](
		[src_sys_prefix] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
		[src_field_name] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
		[target_sys_prefix] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
		[target_field_name] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL
	) ON [PRIMARY]
END