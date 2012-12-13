USE [tbitsccr]
GO
/****** Object:  Table [dbo].[ccr_user_map]    Script Date: 11/30/2009 11:05:11 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[ccr_user_map](
	[user_id] [int] NOT NULL,
	[type] [varchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[recepient_type] [int] NULL,
	[recepient_id] [int] NULL
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF