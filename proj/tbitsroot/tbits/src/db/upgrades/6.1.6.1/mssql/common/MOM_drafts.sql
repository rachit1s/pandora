/****** Object:  Table [dbo].[mom_drafts]    Script Date: 11/04/2009 19:18:56 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[mom_drafts](
	[meeting_id] [numeric](18, 0) NOT NULL,
	[user_id] [numeric](18, 0) NOT NULL,
	[header] [nvarchar](max) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[actions] [nvarchar](max) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
) ON [PRIMARY]
