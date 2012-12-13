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
	update business_areas set max_version_no = 0  where sys_id = @systemId
	delete from versions where sys_id = @systemId
	delete from requestfilemaxid where sys_id = @systemId
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
	update business_areas set max_version_no = 0
	delete from locks
	delete from versions
	delete from file_repo_index
	delete from requestfilemaxid
	delete from max_ids
END


GO
