SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[type_dependency](
	[sys_id] [int] NULL,
	[src_field_id] [int] NULL,
	[src_type_id] [int] NULL,
	[dest_field_id] [int] NULL,
	[dest_type_id] [int] NULL
) ON [PRIMARY]
