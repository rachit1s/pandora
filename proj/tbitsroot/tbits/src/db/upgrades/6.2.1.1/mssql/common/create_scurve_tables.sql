/****** Object:  Table [dbo].[scurve_curves]    Script Date: 09/06/2010 14:54:57 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[scurve_curves](
	[sys_id] [int] NULL,
	[name] [varchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[curve_id] [int] NULL,
	[start_datetime] [datetime] NULL,
	[end_datetime] [datetime] NULL,
	[query] [ntext] COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[is_dql] [bit] NULL,
	[user_id] [int] NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
SET ANSI_PADDING OFF

/****** Object:  Table [dbo].[scurve_factors]    Script Date: 09/06/2010 14:55:34 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[scurve_factors](
	[curve_id] [int] NULL,
	[factor] [real] NULL,
	[turn_around_time] [real] NULL
) ON [PRIMARY]

/****** Object:  Table [dbo].[scurve_curve_requests]    Script Date: 09/06/2010 14:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[scurve_curve_requests](
	[curve_id] [int] NULL,
	[request_id] [int] NULL
) ON [PRIMARY]

/****** Object:  Table [dbo].[scurve_curve_points]    Script Date: 09/06/2010 14:56:39 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[scurve_curve_points](
	[curve_id] [int] NULL,
	[date] [datetime] NULL,
	[cumulative_early] [real] NULL,
	[cumulative_actual] [real] NULL,
	[cumulative_late] [real] NULL
) ON [PRIMARY]
