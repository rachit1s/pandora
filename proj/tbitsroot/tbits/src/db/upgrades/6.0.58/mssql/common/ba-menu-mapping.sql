GO
/****** Object:  Table [dbo].[ba_menu_table]    Script Date: 05/27/2009 10:21:25 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ba_menu_table](
	[menu_id] [int] NOT NULL,
	[menu_caption] [nvarchar](max) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[parent_menu_id] [int] NOT NULL
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[ba_menu_mapping]    Script Date: 05/27/2009 10:21:49 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ba_menu_mapping](
	[menu_id] [int] NOT NULL,
	[sys_id] [int] NOT NULL
) ON [PRIMARY]

GO
IF NOT EXISTS(SELECT * FROM captions_properties WHERE sys_id=0 AND name='captions.view.add_summary')
BEGIN
INSERT INTO captions_properties 
	(sys_id,name,value) 
VALUES  (0,'captions.view.add_summary','Add Summary')
END

GO
IF NOT EXISTS(SELECT * FROM captions_properties WHERE sys_id=0 AND name='captions.view.update_summary')
BEGIN
INSERT INTO captions_properties 
	(sys_id,name,value) 
VALUES  (0,'captions.view.update_summary','Update Summary')
END