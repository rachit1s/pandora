/****** Object:  Table [dbo].[pdf_generation_config_table]    Script Date: 12/15/2010 18:53:39 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[pdf_generation_config_table]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
	CREATE TABLE [dbo].[pdf_generation_config_table](
		[id] [int] NOT NULL,
		[sys_id] [int] NOT NULL,
		[report_template_name] [nvarchar](200) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL
	) ON [PRIMARY]
END
