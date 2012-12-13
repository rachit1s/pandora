
GO
/****** Object:  Table [dbo].[plugins_bill_properties]    Script Date: 02/04/2011 14:13:13 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[plugins_bill_properties]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
CREATE TABLE [dbo].[plugins_bill_properties](
	[key_data] [varchar](30) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[value_data] [varchar](30) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
) ON [PRIMARY]
END

GO
SET ANSI_PADDING OFF

GO
/****** Object:  Table [dbo].[plugins_decision_table]    Script Date: 02/04/2011 14:14:36 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[plugins_decision_table]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
CREATE TABLE [dbo].[plugins_decision_table](
	[decision_flow] [nchar](10) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[key_data] [nchar](30) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[value_data] [nchar](30) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[current_state_id] [int] NULL,
	[next_state_id] [int] NULL,
	[process_id] [int] NULL,
	[rej_state_id] [int] NULL
) ON [PRIMARY]
END

GO
/****** Object:  Table [dbo].[plugins_process_identification_table]    Script Date: 02/04/2011 14:14:50 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[plugins_process_identification_table]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
CREATE TABLE [dbo].[plugins_process_identification_table](
	[process_id] [int] NULL,
	[key_data] [nchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[value_data] [nchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
) ON [PRIMARY]
END


GO
/****** Object:  Table [dbo].[plugins_process_state_flow]    Script Date: 02/04/2011 14:15:15 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[plugins_process_state_flow]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
CREATE TABLE [dbo].[plugins_process_state_flow](
	[plugin_name] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[process_id] [int] NULL,
	[state_id] [int] NULL,
	[key_data] [nvarchar](200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[value_data] [nvarchar](200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
) ON [PRIMARY]
END

