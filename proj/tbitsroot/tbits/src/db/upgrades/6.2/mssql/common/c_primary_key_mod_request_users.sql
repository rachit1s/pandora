/* To prevent any potential data loss issues, you should review this script in detail before running it outside the context of the database designer.*/
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
CREATE TABLE dbo.Tmp_request_users
	(
	sys_id int NOT NULL,
	request_id int NOT NULL,
	user_type_id int NOT NULL,
	user_id int NOT NULL,
	ordering int NULL,
	is_primary bit NOT NULL,
	field_id int NOT NULL
	)  ON [PRIMARY]
GO
IF EXISTS(SELECT * FROM dbo.request_users)
	 EXEC('INSERT INTO dbo.Tmp_request_users (sys_id, request_id, user_type_id, user_id, ordering, is_primary, field_id)
		SELECT sys_id, request_id, user_type_id, user_id, ordering, is_primary, field_id FROM dbo.request_users WITH (HOLDLOCK TABLOCKX)')
GO
DROP TABLE dbo.request_users
GO
EXECUTE sp_rename N'dbo.Tmp_request_users', N'request_users', 'OBJECT' 
GO
ALTER TABLE dbo.request_users ADD CONSTRAINT
	pk_request_users PRIMARY KEY CLUSTERED 
	(
	sys_id,
	request_id,
	user_id,
	field_id
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

GO
COMMIT
GO
