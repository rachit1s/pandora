set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
go




Create PROCEDURE [dbo].[stp_request_delete_all_requests_except_given_bas] 
(
@Ba_ID varchar(1024)
)
	
AS

BEGIN
	delete from action_users where sys_id not in (select value from dbo.SplitWords(@Ba_ID) )
	delete from request_users where sys_id not in (select value from dbo.SplitWords(@Ba_ID) )
	select * from user_read_actions where sys_id not in ( select value from dbo.SplitWords(@Ba_ID) )
	delete from user_read_actions where sys_id not in (select value from dbo.SplitWords(@Ba_ID) )
	delete from transferred_requests 
	delete from user_drafts where sys_id not in ( select value from dbo.SplitWords(@Ba_ID) )
	delete from related_requests 
	delete actions_ex where sys_id not in ( select value from dbo.SplitWords(@Ba_ID) )
	delete from actions where sys_id not in ( select value from dbo.SplitWords(@Ba_ID))
	delete from requests_ex where sys_id not in ( select value from dbo.SplitWords(@Ba_ID) ) 
	delete from requests where sys_id not in(select value from dbo.SplitWords(@Ba_ID) )
	delete from versions 
	delete from file_repo_index 
	delete from requestfilemaxid 
END

