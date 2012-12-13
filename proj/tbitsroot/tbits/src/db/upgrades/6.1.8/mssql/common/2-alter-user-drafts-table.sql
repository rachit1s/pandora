/*
   Thursday, April 15, 201012:24:13 PM
   User: 
   Server: localhost
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
CREATE TABLE dbo.Tmp_user_drafts
	(
	user_id int NOT NULL,
	time_stamp datetime NOT NULL,
	sys_id int NOT NULL,
	request_id int NOT NULL,
	draft ntext NOT NULL,
	draft_id int NULL
	)  ON [PRIMARY]
	 TEXTIMAGE_ON [PRIMARY]
GO
IF EXISTS(SELECT * FROM dbo.user_drafts)
	 EXEC('INSERT INTO dbo.Tmp_user_drafts (user_id, time_stamp, sys_id, request_id, draft, draft_id)
		SELECT user_id, time_stamp, sys_id, request_id, CONVERT(ntext, draft), draft_id FROM dbo.user_drafts WITH (HOLDLOCK TABLOCKX)')
GO
DROP TABLE dbo.user_drafts
GO
EXECUTE sp_rename N'dbo.Tmp_user_drafts', N'user_drafts', 'OBJECT' 
GO
ALTER TABLE dbo.user_drafts ADD CONSTRAINT
	PK_user_drafts PRIMARY KEY CLUSTERED 
	(
	user_id,
	time_stamp,
	sys_id,
	request_id
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

GO
COMMIT
