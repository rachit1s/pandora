/*
   Tuesday, May 25, 20103:41:12 PM
   User: 
   Server: RATHORE\SQLEXPRESS
   Database: uttest
   Application: 
*/

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
GO
BEGIN TRANSACTION
GO
CREATE TABLE dbo.Tmp_roles
	(
	sys_id int NOT NULL,
	role_id int NOT NULL,
	rolename nvarchar(50) NOT NULL,
	description nvarchar(250) NOT NULL,
	field_id int NOT NULL,
	can_be_deleted int NOT NULL
	)  ON [PRIMARY]
GO
IF EXISTS(SELECT * FROM dbo.roles)
	 EXEC('INSERT INTO dbo.Tmp_roles (sys_id, role_id, rolename, description, field_id, can_be_deleted)
		SELECT sys_id, role_id, rolename, description, field_id, can_be_deleted FROM dbo.roles WITH (HOLDLOCK TABLOCKX)')
GO
DROP TABLE dbo.roles
GO
EXECUTE sp_rename N'dbo.Tmp_roles', N'roles', 'OBJECT' 
GO
ALTER TABLE dbo.roles ADD CONSTRAINT
	PK_roles PRIMARY KEY CLUSTERED 
	(
	sys_id,
	role_id
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

GO
ALTER TABLE dbo.roles ADD CONSTRAINT
	unique_sys_id_and_rolename UNIQUE NONCLUSTERED 
	(
	sys_id,
	rolename
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

GO
COMMIT
GO
