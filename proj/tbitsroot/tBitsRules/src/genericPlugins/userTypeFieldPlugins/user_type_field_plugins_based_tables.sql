GO
/****** Object:  Table [dbo].[plugin_src_to_target_user_type_update_config]    Script Date: 12/16/2010 14:48:29 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[plugin_src_to_target_user_type_update_config](
	[sys_id] [int] NOT NULL,
	[condition_field_id] [int] NOT NULL,
	[condition_field_value] [nvarchar](150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[src_user_type_field_id] [int] NOT NULL,
	[target_user_type_field_id] [int] NOT NULL
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[plugin_user_type_field_update_config]    Script Date: 12/16/2010 14:55:26 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[plugin_user_type_field_update_config](
	[sys_id] [int] NOT NULL,
	[condition_field_id] [int] NOT NULL,
	[condition_field_value] [nvarchar](150) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[role_id] [int] NOT NULL,
	[user_type_field_id] [int] NOT NULL,
	[only_on_add_request] [bit] NOT NULL
) ON [PRIMARY]
