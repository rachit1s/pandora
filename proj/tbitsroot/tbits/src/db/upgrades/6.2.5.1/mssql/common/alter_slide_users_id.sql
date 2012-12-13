/****** Object:  StoredProcedure [dbo].[stp_tbits_slide_userid]    Script Date: 09/09/2011 13:37:14 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:Syeda
-- Create date: 2/9/2011
-- Description:Sliding the userid by a given offset
-- =============================================
CREATE  PROCEDURE [dbo].[stp_tbits_slide_userid] 
	-- Add the parameters for the stored procedure here
	@startingid int  , 
	@endingid int  ,
	@offset int 
		
AS
BEGIN
	begin tran 
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
		--what if we dont take the offset value from users?my suggestion :
	--declare @offset int 
		--select @offset=max(user_id) from users

if ( exists ( select user_id  from users where user_id >= @startingid + @offset and user_id <= @endingid + @offset ) )
begin
 RAISERROR ('Error User ID already exist', 16, 1) 
end 
else
begin
    
    -- altering the check constraints
alter  table request_users nocheck constraint all
alter  table action_users nocheck constraint all
alter  table roles_users nocheck constraint all
alter  table type_users nocheck constraint all
---updating user_id of all users between the userid specified
 
update users set user_id = user_id+@offset where user_id >= @startingid and  user_id <= @endingid



---- updating the user_id in various other tables

update action_users set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update actions set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update business_area_users set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update escalation_heirarchy set parent_user_id=parent_user_id+@offset  where parent_user_id >= @startingid and parent_user_id<= @endingid	
update escalation_heirarchy set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update exclusion_list set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update gadget_user_config set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update gadget_user_params set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update job_definitions set job_user_id=job_user_id+@offset  where job_user_id >= @startingid and job_user_id<= @endingid	
update job_notifiers set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update mail_list_users set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update mom_drafts set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update post_process_rules set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update report_specific_users set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update request_users set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update requests set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update roles_users set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update scurve_curves set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update sms_log set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update super_users set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update tags_definitions set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update tags_requests set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update type_users set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update user_drafts set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update user_grid_col_prefs set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	
update user_read_actions set user_id=user_id+@offset  where user_id >= @startingid and user_id<= @endingid	

-- altering the check constraints
alter  table request_users check constraint all
alter  table action_users check constraint all
alter  table roles_users check constraint all
alter  table type_users check constraint all

end
if @@ERROR<>0
rollback
commit tran 
END
