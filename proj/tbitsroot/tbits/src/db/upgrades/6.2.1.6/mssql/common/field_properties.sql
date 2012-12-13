/****** Object:  Table [dbo].[field_properties]    Script Date: 09/16/2010 03:44:11 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[field_properties](
	[sys_id] [int] NULL,
	[field_id] [int] NULL,
	[property] [varchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[value] [nvarchar](max) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[description] [nvarchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
