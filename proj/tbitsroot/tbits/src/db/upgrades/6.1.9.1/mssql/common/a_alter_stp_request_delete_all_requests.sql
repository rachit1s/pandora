if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[stp_request_delete_all_requests]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[stp_request_delete_all_requests]
GO

set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
go

create PROCEDURE [dbo].[stp_request_delete_all_requests] 
	
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
	update business_areas set max_version_no = 0
	delete from locks
	delete from versions
	delete from file_repo_index
	delete from requestfilemaxid
END
