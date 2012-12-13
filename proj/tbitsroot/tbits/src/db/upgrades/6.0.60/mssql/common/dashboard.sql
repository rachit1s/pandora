SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[gadgets]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[gadgets](
	[caption] [nvarchar](50) NOT NULL,
	[id] [int] NOT NULL,
	[report_file] [nvarchar](50) NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[gadget_user_config]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[gadget_user_config](
	[user_id] [int] NOT NULL,
	[id] [int] NOT NULL,
	[col] [int] NOT NULL,
	[height] [int] NOT NULL,
	[is_visible] [int] NOT NULL,
	[is_minimized] [int] NOT NULL,
	[refresh_rate] [int] NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[gadget_user_params]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[gadget_user_params](
	[user_id] [int] NOT NULL,
	[id] [int] NOT NULL,
	[name] [nvarchar](50) NOT NULL,
	[value] [nvarchar](50) NOT NULL,
	[type] [nvarchar](50) NULL
) ON [PRIMARY]
END
