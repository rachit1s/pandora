/*
   Saturday, June 19, 201011:33:16 AM
   User: 
   Server: RATHORE\SQLEXPRESS
   Database: trunktest
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
ALTER TABLE dbo.requests
	DROP CONSTRAINT DF__requests__office__166F3B3A
GO
CREATE TABLE dbo.Tmp_requests
	(
	sys_id int NOT NULL,
	request_id int NOT NULL,
	category_id int NULL,
	status_id int NULL,
	severity_id int NULL,
	request_type_id int NULL,
	subject nvarchar(2048) NULL,
	description ntext NULL,
	description_content_type int NULL,
	is_private bit NULL,
	parent_request_id int NULL,
	user_id int NOT NULL,
	max_action_id int NOT NULL,
	due_datetime datetime NULL,
	logged_datetime datetime NOT NULL,
	lastupdated_datetime datetime NOT NULL,
	header_description ntext NULL,
	attachments ntext NULL,
	summary ntext NULL,
	summary_content_type int NULL,
	memo ntext NULL,
	append_interface int NULL,
	notify int NULL,
	notify_loggers bit NULL,
	replied_to_action int NULL,
	office_id int NULL,
	max_request_file_id int NULL
	)  ON [PRIMARY]
	 TEXTIMAGE_ON [PRIMARY]
GO
IF EXISTS(SELECT * FROM dbo.requests)
	 EXEC('INSERT INTO dbo.Tmp_requests (sys_id, request_id, category_id, status_id, severity_id, request_type_id, subject, description, description_content_type, is_private, parent_request_id, user_id, max_action_id, due_datetime, logged_datetime, lastupdated_datetime, header_description, attachments, summary, summary_content_type, memo, append_interface, notify, notify_loggers, replied_to_action, office_id, max_request_file_id)
		SELECT sys_id, request_id, category_id, status_id, severity_id, request_type_id, subject, description, description_content_type, is_private, parent_request_id, user_id, max_action_id, due_datetime, logged_datetime, lastupdated_datetime, header_description, attachments, summary, summary_content_type, memo, append_interface, notify, notify_loggers, replied_to_action, office_id, max_request_file_id FROM dbo.requests WITH (HOLDLOCK TABLOCKX)')
GO
DROP TABLE dbo.requests
GO
EXECUTE sp_rename N'dbo.Tmp_requests', N'requests', 'OBJECT' 
GO
ALTER TABLE dbo.requests ADD CONSTRAINT
	PK_requests PRIMARY KEY CLUSTERED 
	(
	sys_id,
	request_id
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

GO
CREATE NONCLUSTERED INDEX [status+sys_id] ON dbo.requests
	(
	sys_id,
	status_id
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX idx_requests_parent ON dbo.requests
	(
	sys_id,
	request_id,
	parent_request_id
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX idx_requests_sys_id_cat ON dbo.requests
	(
	sys_id,
	category_id
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX idx_requests_sys_id_sev ON dbo.requests
	(
	sys_id,
	severity_id
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX idx_requests_sys_id_type ON dbo.requests
	(
	sys_id,
	request_type_id
	) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
COMMIT
