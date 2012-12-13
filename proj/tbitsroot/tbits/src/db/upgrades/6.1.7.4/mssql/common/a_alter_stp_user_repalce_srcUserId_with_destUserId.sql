if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[stp_user_repalce_srcUserId_with_destUserId]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[stp_user_repalce_srcUserId_with_destUserId]
GO


set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
go




-- =======================
-- Author:		Nitiraj
-- Create date: 
-- Description: repalces the occurences of srcUserId with destUserId
--		if the @isexec parameter is set to anything than 'y' then 
--		actual changes will not occur. 
--		This store procedure is intended for direct use by the 
--		administrator and not to be called by the API
--		Its primary (at the time of inception ) use is to replace the occurences of srcUserId with destUserId before
--		deleting the srcUserId using the stored procedure stp_user_delete_user
-- =======================

create PROCEDURE [dbo].[stp_user_repalce_srcUserId_with_destUserId] 
	-- Add the parameters for the stored procedure here
	@srcUserId int,
	@destUserId int,
	@isexec varchar(1)
AS
BEGIN
	DECLARE @srcUserLogin varchar(100)
	declare @destUserLogin varchar(100)
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
	
	select @srcUserLogin=user_login from users where user_id=@srcUserId
	select @destUserLogin=user_login from users where user_id=@destUserId
	
	print 'All the occurences of srcUserId = ' + cast(@srcUserId as varchar(100)) + ' ( ' + @srcUserLogin + ' ) will be replaced by the destUserId = ' + cast(@destUserId as varchar(100)) + ' ( ' + @destUserLogin + ' ) '

	if( @srcUserId IS NULL OR @destUserId IS NULL )
		begin 
			print 'User does not exist with user_id = ' + cast(@srcUserId as varchar(100)) + ' OR with user_id = ' + cast( @destUserId as varchar(100))
		end
	else
		begin 
			--print 'userid is not null' 
			if( @isexec != 'y' )
			begin
				select 'This is test only mode' as message
			end
			else
			begin
				select 'This is execution mode' as message 
			end

			select 'user_id in following requests will be changed from ' + cast( @srcUserId as varchar(100) ) + ' to ' + cast( @destUserId as varchar(100) )  as message
			select * from requests where user_id = @srcUserId			
			if( @isexec = 'y' )
			begin
				update requests
				set user_id = @destUserId
				where user_id = @srcUserId
			end
						
			
			select 'user_id in following actions will be changed from ' + cast( @srcUserId as varchar(100)) + ' to ' + cast( @destUserId as varchar(100)) as message
			select * from actions where user_id = @srcUserId			
			if( @isexec = 'y' )
			begin
				update actions
				set user_id = @destUserId
				where user_id = @srcUserId
			end

			select 'user_id in following request_users will be changed from ' + cast( @srcUserId as varchar(100)) + ' to ' + cast( @destUserId as varchar(100)) as message
			select * from request_users where user_id = @srcUserId			
			if( @isexec = 'y' )
			begin
				update request_users
				set user_id = @destUserId
				where user_id = @srcUserId
			end

			select 'user_id in following action_users will be changed from ' + cast( @srcUserId as varchar(100))  + ' to ' + cast(@destUserId as varchar(100)) as message
			select * from action_users where user_id = @srcUserId			
			if( @isexec = 'y' )
			begin
				update action_users
				set user_id = @destUserId
				where user_id = @srcUserId
			end

			if( EXISTS( select * from escalation_heirarchy where user_id=@srcUserId OR parent_user_id=@srcUserId )
				  AND EXISTS( select * from escalation_heirarchy where user_id = @destUserId OR parent_user_id = @destUserId )
			   )
			begin 
			select cast( @srcUserId as varchar(100) ) + ' and ' + cast( @destUserId as varchar(100) ) + ' both are present in escalation heirarchy table.\nPlease review this table for loops after replacement' as message
			end

			select 'parent_user_id in following escalation_heirarchy will be changed from ' + cast( @srcUserId as varchar(100)) + ' to ' + cast( @destUserId as varchar(100)) as message
			select * from escalation_heirarchy where parent_user_id = @srcUserId			
			if( @isexec = 'y' )
			begin
				update escalation_heirarchy
				set parent_user_id = @destUserId
				where parent_user_id = @srcUserId
			end

			select 'user_id in following escalation_heirarchy will be changed from ' + cast( @srcUserId as varchar(100)) + ' to ' + cast( @destUserId as varchar(100))  as message
			select * from escalation_heirarchy where user_id = @srcUserId			
			if( @isexec = 'y' )
			begin
				update escalation_heirarchy
				set user_id = @destUserId
				where user_id = @srcUserId
			end		

		end
	
END





