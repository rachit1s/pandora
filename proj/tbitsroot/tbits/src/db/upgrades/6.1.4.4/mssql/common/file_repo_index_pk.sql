/*
   Tuesday, November 03, 20092:54:19 AM
   User: 
   Server: SNOWWHITE\SQLEXPRESS
   Database: ksk
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
BEGIN TRANSACTION
GO
CREATE TABLE dbo.Tmp_file_repo_index
	(
	id int NOT NULL,
	location varchar(300) NULL,
	name varchar(250) NULL,
	create_date datetime NULL,
	size bigint NULL
	)  ON [PRIMARY]
GO
IF EXISTS(SELECT * FROM dbo.file_repo_index)
	 EXEC('INSERT INTO dbo.Tmp_file_repo_index (id, location, name, create_date, size)
		SELECT id, location, name, create_date, size FROM dbo.file_repo_index WITH (HOLDLOCK TABLOCKX)')
GO
DROP TABLE dbo.file_repo_index
GO
EXECUTE sp_rename N'dbo.Tmp_file_repo_index', N'file_repo_index', 'OBJECT' 
GO
ALTER TABLE dbo.file_repo_index ADD CONSTRAINT
	PK_file_repo_index PRIMARY KEY CLUSTERED 
	(
	id
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

GO
COMMIT
