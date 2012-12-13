GO
/****** Object:  Table [dbo].[tags_requests]    Script Date: 09/23/2010 17:28:29 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[tags_requests](
	[tag_id] [int] NULL,
	[sys_id] [int] NULL,
	[request_id] [int] NULL
) ON [PRIMARY]

GO
ALTER TABLE [dbo].[tags_requests]  WITH CHECK 
ADD  CONSTRAINT [FK_tags_requests_tags_definitions] FOREIGN KEY([tag_id])
REFERENCES [dbo].[tags_definitions] ([tag_id])
GO
ALTER TABLE [dbo].[tags_requests] CHECK CONSTRAINT [FK_tags_requests_tags_definitions]
