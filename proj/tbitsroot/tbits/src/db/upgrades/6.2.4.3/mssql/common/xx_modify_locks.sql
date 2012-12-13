drop table locks

/****** Object:  Table [dbo].[locks]    Script Date: 05/12/2011 16:55:33 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[locks](
	[token] [nvarchar](150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[path] [nvarchar](2048) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[type] [varchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[scope] [varchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[owner] [varchar](150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[comment] [text] COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[depth] [int] NOT NULL,
	[creation_date] [datetime] NOT NULL,
	[sys_id] [int] NOT NULL,
	[request_id] [int] NOT NULL,
	[field_id] [int] NOT NULL,
	[request_file_id] [int] NOT NULL,
 CONSTRAINT [PK_locks] PRIMARY KEY CLUSTERED 
(
	[token] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
SET ANSI_PADDING OFF
