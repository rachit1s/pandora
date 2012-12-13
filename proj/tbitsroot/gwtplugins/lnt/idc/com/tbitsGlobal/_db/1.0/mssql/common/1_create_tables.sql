GO
/****** Object:  Table [dbo].[plugins_idc_ba_map]    Script Date: 09/05/2010 19:22:27 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[plugins_idc_ba_map]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
BEGIN
CREATE TABLE [dbo].[plugins_idc_ba_map](
	[src_ba] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[target_ba] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
) ON [PRIMARY]
END