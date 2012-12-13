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
CREATE TABLE dbo.Tmp_action_users
(
sys_id int NOT NULL,
request_id int NOT NULL,
action_id int NOT NULL,
user_type_id int NOT NULL,
user_id int NOT NULL,
ordering int NULL,
is_primary int NOT NULL,
field_id int NOT NULL
)  ON [PRIMARY]
GO
IF EXISTS(SELECT * FROM dbo.action_users)
EXEC('INSERT INTO dbo.Tmp_action_users (sys_id, request_id, action_id, user_type_id, user_id, ordering, is_primary, field_id)
SELECT sys_id, request_id, action_id, user_type_id, user_id, ordering, is_primary, field_id FROM dbo.action_users WITH (HOLDLOCK TABLOCKX)')
GO
DROP TABLE dbo.action_users
GO
EXECUTE sp_rename N'dbo.Tmp_action_users', N'action_users', 'OBJECT' 
GO
ALTER TABLE dbo.action_users ADD CONSTRAINT
PK_action_users PRIMARY KEY CLUSTERED 
(
sys_id,
request_id,
action_id,
user_type_id,
user_id,
field_id
) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

GO
COMMIT
GO
