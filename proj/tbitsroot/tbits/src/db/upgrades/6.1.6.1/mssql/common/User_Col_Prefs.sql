/****** Object:  Table [dbo].[user_col_prefs]    Script Date: 11/04/2009 19:22:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[user_col_prefs](
	[user_id] [int] NOT NULL,
	[sys_id] [int] NOT NULL,
	[grid_type] [varchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[col_name] [nchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
