

/****** Object:  Table [dbo].[ddc_config]    Script Date: 05/11/2012 12:17:21 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING ON
GO

IF EXISTS(select * from information_schema.tables where table_name like 'ddc_fields')
print 'table ddc_fields already exists' 

ELSE

CREATE TABLE [dbo].[ddc_fields](
	[sys_id] [int] NOT NULL,
	[regex_order] [varchar](200) NOT NULL,
	[field_name] [varchar](200) NOT NULL,
	[is_editable] [bit] NOT NULL
) ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO



