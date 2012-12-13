
/****** Object:  Table [dbo].[trn_processkey_field]    Script Date: 03/20/2012 12:27:55 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO

IF EXISTS(select * from information_schema.tables where table_name like 'trn_processkey_field')
print 'table trb_processskey_field already exists' 

ELSE
CREATE TABLE [dbo].[trn_processkey_field](
	[trn_process_id] [int] NOT NULL,
	[field_id] [int] NOT NULL,
	[type_id] [int] NOT NULL,
	[value] [varchar](max) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
 
