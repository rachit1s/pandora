/****** Object:  Table [dbo].[mom_templates]    Script Date: 07/26/2010 08:57:54 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[mom_templates](
	[sys_id] [int] NULL,
	[field_id] [int] NULL,
	[type_id] [int] NULL,
	[is_meeting] [int] NULL,
	[template] [varchar](5000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF