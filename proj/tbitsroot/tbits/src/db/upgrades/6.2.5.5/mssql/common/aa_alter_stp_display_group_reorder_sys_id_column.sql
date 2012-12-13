
/****** Object:  StoredProcedure [dbo].[stp_display_group_reorder_sys_id_column]    Script Date: 01/17/2012 16:17:55 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER procedure [dbo].[stp_display_group_reorder_sys_id_column]
as
Begin
ALTER TABLE [dbo].[display_groups] DROP CONSTRAINT [DF_display_groups_is_active]
ALTER TABLE [dbo].[display_groups] DROP CONSTRAINT [DF_display_groups_display_order]

CREATE TABLE dbo.Tmp_display_groups
	(
	 sys_id int NULL,
	 id int NOT NULL IDENTITY (1, 1),
	 display_name varchar(50) NULL,
	 display_order int NULL CONSTRAINT DF_display_groups_display_order DEFAULT ((0)),
	 is_active bit NOT NULL CONSTRAINT DF_display_groups_is_active DEFAULT ((1)),
	 is_default bit not null CONSTRAINT [DF_display_groups_is_default]  DEFAULT ((0))
	)
SET IDENTITY_INSERT dbo.Tmp_display_groups ON

IF EXISTS(SELECT * FROM dbo.display_groups)
	 EXEC('INSERT INTO dbo.Tmp_display_groups (sys_id, id, display_name, display_order, is_active, is_default)
		SELECT sys_id, id, display_name, display_order, is_active ,is_default FROM dbo.display_groups WITH (HOLDLOCK TABLOCKX)')

SET IDENTITY_INSERT dbo.Tmp_display_groups OFF

DROP TABLE dbo.display_groups

EXECUTE sp_rename N'dbo.Tmp_display_groups', N'display_groups', 'OBJECT'

ALTER TABLE dbo.display_groups ADD CONSTRAINT
	PK_display_groups PRIMARY KEY CLUSTERED 
	(
	id
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) 
END
