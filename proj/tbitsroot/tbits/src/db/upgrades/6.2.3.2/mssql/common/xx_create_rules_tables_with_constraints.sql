/****** Object:  Table [dbo].[rules_storage]    Script Date: 01/13/2011 14:05:04 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[rules_storage](
	[id] [int] NOT NULL,
	[name] [varchar](50) NULL,
	[code] [varchar](max) NULL,
	[class] [varbinary](max) NULL,
 CONSTRAINT [PK_rules_storage] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF

/****** Object:  Table [dbo].[rules_definitions]    Script Date: 01/13/2011 14:03:54 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[rules_definitions](
	[id] [int] NOT NULL,
	[name] [varchar](50) NULL,
	[type] [varchar](50) NULL,
	[seq_number] [real] NULL,
 CONSTRAINT [PK_rules_definitions] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
ALTER TABLE [dbo].[rules_definitions]  WITH CHECK ADD  CONSTRAINT [FK_rules_definitions_rules_storage] FOREIGN KEY([id])
REFERENCES [dbo].[rules_storage] ([id])
GO
ALTER TABLE [dbo].[rules_definitions] CHECK CONSTRAINT [FK_rules_definitions_rules_storage]