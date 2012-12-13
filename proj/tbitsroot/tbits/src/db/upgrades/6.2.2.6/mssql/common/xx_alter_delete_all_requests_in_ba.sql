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
	delete from related_requests where primary_sys_id = @systemId or related_sys_id = @systemId
	delete actions_ex where sys_id = @systemId
	delete from actions where sys_id = @systemId
	delete from requests_ex where sys_id = @systemId
	delete from tags_requests where sys_id = @systemId
	delete from requests where sys_id = @systemId
	update business_areas set max_request_id = 0  where sys_id = @systemId
	update business_areas set max_version_no = 0  where sys_id = @systemId
	delete from versions where sys_id = @systemId
	delete from requestfilemaxid where sys_id = @systemId
END
