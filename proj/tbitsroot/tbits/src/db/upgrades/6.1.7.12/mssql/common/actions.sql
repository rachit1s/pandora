/*
   Wednesday, March 24, 20102:41:26 PM
   User: 
   Server: SOURABH\SQLEXPRESS
   Database: tbits_ksk
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
ALTER TABLE dbo.actions
	DROP CONSTRAINT DF__actions__office___17635F73
GO
CREATE TABLE dbo.Tmp_actions
	(
	sys_id int NOT NULL,
	request_id int NOT NULL,
	action_id int NOT NULL,
	category_id int NOT NULL,
	status_id int NOT NULL,
	severity_id int NOT NULL,
	request_type_id int NOT NULL,
	subject nvarchar(2048) NOT NULL,
	description ntext NOT NULL,
	description_content_type int NULL,
	is_private bit NOT NULL,
	parent_request_id int NOT NULL,
	user_id int NOT NULL,
	due_datetime datetime NULL,
	logged_datetime datetime NOT NULL,
	lastupdated_datetime datetime NOT NULL,
	header_description ntext NULL,
	attachments ntext NULL,
	summary ntext NULL,
	summary_content_type int NULL,
	memo ntext NULL,
	append_interface int NOT NULL,
	notify int NOT NULL,
	notify_loggers bit NOT NULL,
	replied_to_action int NULL,
	office_id int NOT NULL
	)  ON [PRIMARY]
	 TEXTIMAGE_ON [PRIMARY]
GO
ALTER TABLE dbo.Tmp_actions ADD CONSTRAINT
	DF__actions__office___17635F73 DEFAULT ((0)) FOR office_id
GO
IF EXISTS(SELECT * FROM dbo.actions)
	 EXEC('INSERT INTO dbo.Tmp_actions (sys_id, request_id, action_id, category_id, status_id, severity_id, request_type_id, subject, description, is_private, parent_request_id, user_id, due_datetime, logged_datetime, lastupdated_datetime, header_description, attachments, summary, memo, append_interface, notify, notify_loggers, replied_to_action, office_id)
		SELECT sys_id, request_id, action_id, category_id, status_id, severity_id, request_type_id, subject, description, is_private, parent_request_id, user_id, due_datetime, logged_datetime, lastupdated_datetime, header_description, attachments, summary, memo, append_interface, notify, notify_loggers, replied_to_action, office_id FROM dbo.actions WITH (HOLDLOCK TABLOCKX)')
GO
DROP TABLE dbo.actions
GO
EXECUTE sp_rename N'dbo.Tmp_actions', N'actions', 'OBJECT' 
GO
ALTER TABLE dbo.actions ADD CONSTRAINT
	PK_actions PRIMARY KEY CLUSTERED 
	(
	sys_id,
	request_id,
	action_id
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

GO
COMMIT
