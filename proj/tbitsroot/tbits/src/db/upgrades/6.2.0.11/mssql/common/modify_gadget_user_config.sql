BEGIN TRANSACTION
SET QUOTED_IDENTIFIER ON
SET ARITHABORT ON
SET NUMERIC_ROUNDABORT OFF
SET CONCAT_NULL_YIELDS_NULL ON
SET ANSI_NULLS ON
SET ANSI_PADDING ON
SET ANSI_WARNINGS ON
COMMIT
BEGIN TRANSACTION
GO
CREATE TABLE dbo.Tmp_gadget_user_config
	(
	user_id int NOT NULL,
	id int NOT NULL,
	col int NOT NULL,
	height int NOT NULL,
	width int NULL,
	is_visible int NOT NULL,
	is_minimized int NOT NULL,
	refresh_rate int NOT NULL,
	x int NULL,
	y int NULL
	)  ON [PRIMARY]
GO
IF EXISTS(SELECT * FROM dbo.gadget_user_config)
	 EXEC('INSERT INTO dbo.Tmp_gadget_user_config (user_id, id, col, height, is_visible, is_minimized, refresh_rate)
		SELECT user_id, id, col, height, is_visible, is_minimized, refresh_rate FROM dbo.gadget_user_config WITH (HOLDLOCK TABLOCKX)')
GO
DROP TABLE dbo.gadget_user_config
GO
EXECUTE sp_rename N'dbo.Tmp_gadget_user_config', N'gadget_user_config', 'OBJECT' 
GO
COMMIT
