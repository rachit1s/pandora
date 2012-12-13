alter table versions
add field_id int, request_file_id int, file_id int, tvn_file_name varchar(100)
GO

alter table requests
add max_request_file_id int
GO

create table max_ids
(name varchar(50),
id	int)
GO

CREATE TABLE [dbo].[file_repo_index](
	[id] [int] NULL,
	[location] [varchar](300) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[name] [varchar](250) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[create_date] [datetime] NULL,
	[size] [bigint] NULL
) ON [PRIMARY]
GO


CREATE PROCEDURE stp_getAndIncrMaxId 
	@tableName varchar(250)
AS
BEGIN
	BEGIN TRANSACTION
		declare @maxid int
		select @maxid = id from max_ids where name=@tableName
		if @maxid is null
		begin
			insert into max_ids (name, id) values (@tableName, 1)
			select 1 max_id;
		end
		else
		begin
			update max_ids set id = @maxid +1 where name=@tableName
			select @maxid + 1 max_id
		end
	commit TRAN
END
GO

create table requestfilemaxid
(
	sys_id int, request_id int, maxfileid int
)
GO

CREATE PROCEDURE stp_request_getAndIncrRequestFileMaxrId 
	@sysId int, @requestId int
AS
BEGIN
	BEGIN TRANSACTION
		declare @maxid int
		select @maxid = maxfileid from requestfilemaxid where sys_id = @sysId and request_id = @requestId
		if @maxid is null
		begin
			insert into requestfilemaxid (sys_id, request_id, maxfileid) values (@sysId, @requestId, 1)
			select 1 max_id;
		end
		else
		begin
			update requestfilemaxid set maxfileid = @maxid +1 where sys_id = @sysId and request_id = @requestId
			select @maxid + 1 max_id
		end
	commit TRAN
END
GO

set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Sandeep
-- Create date: 10 Jan 07
-- Description:	Deletes all the requests in a ba,. Dont use it at all.
-- =============================================
ALTER PROCEDURE [dbo].[stp_request_delete_all_requests] 
	
AS
BEGIN
	delete from action_users
	delete from request_users
	select * from user_read_actions
	delete from user_read_actions
	delete from transferred_requests
	delete from user_drafts
	delete from related_requests
	delete actions_ex
	delete from actions
	delete from requests_ex
	delete from requests
	update business_areas set max_request_id = 0
	delete from locks
	delete from versions
	delete from file_repo_index
	delete from requestfilemaxid
	delete from max_ids
END
GO

set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO
ALTER PROCEDURE [dbo].[stp_request_delete_all_requests_in_ba] 
(
	@sys_prefix	varchar(30)
	
)
AS
declare @systemId INT
BEGIN
	select @systemId = sys_id from business_areas where sys_prefix = @sys_prefix
	delete from action_users where sys_id = @systemId
	delete from request_users where sys_id = @systemId
	select * from user_read_actions where sys_id = @systemId
	delete from user_read_actions where sys_id = @systemId
	delete from transferred_requests where source_prefix = @sys_prefix or target_prefix = @sys_prefix
	delete from user_drafts where sys_id = @systemId
	delete from related_requests where primary_sys_prefix = @sys_prefix or related_sys_prefix = @sys_prefix
	delete actions_ex where sys_id = @systemId
	delete from actions where sys_id = @systemId
	delete from requests_ex where sys_id = @systemId
	delete from requests where sys_id = @systemId
	update business_areas set max_request_id = 0  where sys_id = @systemId
	delete from versions where sys_id = @systemId
	delete from requestfilemaxid where sys_id = @systemId
END
GO

set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO
ALTER procedure [dbo].[stp_request_updateAttachments]
(
	@systemId	INT,
	@requestId	INT,
	@actionId	INT,
	@attachments	TEXT
)
AS
UPDATE requests
SET
	attachments = @attachments
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId AND
	max_action_id   = @actionId
UPDATE actions
SET
	attachments = @attachments
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId AND
	action_id	= @actionId
GO

insert into datatypes (datatype_id, name, description) values(11,	'attachments',	'attachments')
GO

update fields set data_type_id = 11 where name = 'attachments' and field_id = 22
GO

Create procedure [dbo].[stp_request_updateAttachments_ex]
(
	@systemId	INT,
	@requestId	INT,
	@actionId	INT,
	@fieldId	INT,
	@attachments	TEXT
)
AS
UPDATE requests_ex
SET
	text_value = @attachments
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId AND
	field_id	= @fieldId
UPDATE actions_ex
SET
	text_value = @attachments
WHERE
	sys_id 		= @systemId AND
	request_id	= @requestId AND
	field_id	= @fieldId AND
	action_id	= @actionId
GO
