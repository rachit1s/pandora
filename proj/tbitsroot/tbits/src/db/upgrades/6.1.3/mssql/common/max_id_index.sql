/*
   Thursday, October 01, 20095:04:05 AM
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
CREATE TABLE dbo.Tmp_max_ids
	(
	name varchar(50) NOT NULL,
	id int NULL
	)  ON [PRIMARY]
GO
IF EXISTS(SELECT * FROM dbo.max_ids)
	 EXEC('INSERT INTO dbo.Tmp_max_ids (name, id)
		SELECT name, id FROM dbo.max_ids WITH (HOLDLOCK TABLOCKX)')
GO
DROP TABLE dbo.max_ids
GO
EXECUTE sp_rename N'dbo.Tmp_max_ids', N'max_ids', 'OBJECT' 
GO
ALTER TABLE dbo.max_ids ADD CONSTRAINT
	PK_max_ids PRIMARY KEY CLUSTERED 
	(
	name
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

GO
COMMIT
