set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Nitiraj
-- Create date: 
-- Description:	Deletes a user if there is no request reference into DB
-- =============================================
CREATE PROCEDURE [dbo].[stp_user_delete_user] 
	-- Add the parameters for the stored procedure here
	@userLogin varchar(100)
	
AS
BEGIN
	DECLARE @userid int
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
    --print @userLogin 
	select @userid=user_id from users where user_login=@userLogin

	--select @userid as myuserid
	--select * from users where user_id=@userid
	--select * from users where user_login=@userLogin
	if( @userid IS NULL )
		begin 
			print 'User does not exist. userLogin = ' + @userLogin  
		end
	else
		begin 
			--print 'userid is not null' 
			declare @reqCount int 
			select @reqCount = 0
			select @reqCount=count(*) from requests where user_id=@userid
			if( @reqCount != 0 ) 
				begin
					print 'User (' + @userLogin + ') cannot be deleted as it references some requests'
				end
			else
				begin
					declare @actionCount int 
					select @actionCount = 0 
					select @actionCount = count(*) from actions where user_id=@userid
					if( @actionCount != 0 )
						begin 
							print 'User (' + @userLogin + ') cannot be delete as it references some of the actions' 
						end
					else
						begin
							declare @parentCount int 
							select @parentCount = 0 
							select @parentCount=count(*) from escalation_heirarchy where parent_user_id=@userid	
							if( @parentCount != 0 )
								begin 
										print 'User (' + @userLogin + ') cannot be delete as it exists as parent in excalation_heirarchy' 
								end
							else 
								begin
									declare @requestUserCount int 
									declare @actionUserCount int
									select @requestUserCount = 0
									select @actionUserCount = 0  
									select @requestUserCount=count(*) from request_users where user_id=@userid
									select @actionUserCount=count(*) from action_users where user_id=@userid
									if( @requestUserCount != 0 OR @actionUserCount != 0 ) 
										begin
											print 'User (' + @userLogin + ') cannot be deleted as it has references in action_users table or request_users table.Please resolve this issue first.'
										end
									else
										begin
											--print 'Proceding with delete' 	
											--select * from request_users where user_id=@userid
											--delete from request_users where user_id=@userid 

											--select * from action_users where user_id=@userid
											--delete from action_users where user_id=@userid

											select * from business_area_users where user_id=@userid
											delete from business_area_users where user_id=@userid

											select * from report_specific_users where user_id=@userid 
											delete from report_specific_users where user_id=@userid 

											select * from roles_users where user_id=@userid							
											delete from roles_users where user_id=@userid

											select * from type_users where user_id=@userid
											delete from type_users where user_id=@userid

											select * from user_drafts where user_id=@userid
											delete from user_drafts where user_id=@userid

											select * from user_passwords where user_login=@userLogin
											delete from user_passwords where user_login=@userLogin

											select * from user_read_actions where user_id=@userid
											delete from user_read_actions where user_id=@userid 

											select * from escalation_heirarchy where user_id=@userid
											delete from escalation_heirarchy where user_id=@userid

											select * from exclusion_list where user_id=@userid
											delete from exclusion_list where user_id=@userid

											select * from gadget_user_config where user_id=@userid
											delete from gadget_user_config where user_id=@userid

											select * from gadget_user_params where user_id=@userid
											delete from gadget_user_params where user_id=@userid

											select * from job_notifiers where user_id=@userid
											delete from job_notifiers where user_id=@userid

											select * from mail_list_users where user_id=@userid
											delete from mail_list_users where user_id=@userid

											select * from sms_log where user_id=@userid 
											delete from sms_log where user_id=@userid

											select * from super_users where user_id=@userid
											delete from super_users where user_id=@userid

											select * from users where user_id=@userid
											delete from users where user_id=@userid

											--print 'User Information deleted from following tables : users, request_users, action_users,' +
											--	  ' business_area_users, report_specific_users, roles_users, type_users, user_drafts,' +
											--	  ' user_passwords, user_read_actions, escalation_heirarchy, exclusion_list'
										end
								end
						end
				end
		end
	
END

