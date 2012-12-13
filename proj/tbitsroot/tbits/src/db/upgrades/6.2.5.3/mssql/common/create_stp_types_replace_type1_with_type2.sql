-- ================================================
-- Template generated from Template Explorer using:
-- Create Procedure (New Menu).SQL
--
-- Use the Specify Values for Template Parameters 
-- command (Ctrl-Shift-M) to fill in the parameter 
-- values below.
--
-- This block of comments will not be included in
-- the definition of the procedure.
-- ================================================
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:nitiraj		
-- Create date: 28 sept 2011
-- Description:	this will blindly replace the type1 with type2 for a particular dropdown field of a particular  business area 
-- =============================================
create PROCEDURE stp_types_replace_type1_with_type2
	-- Add the parameters for the stored procedure here
	@sys_id int,
	@field_id int,
	@type1_id int,
	@type2_id int,
	@dry_run varchar(1)
AS
BEGIN
	if( @dry_run = 'n' )
	begin
		select 'You are in EXECUTION MODE'
	end
	else
	begin
		select 'You are in DRY-RUN MODE'
	end
	declare @field_name varchar(50)
	if ( @field_id < 30 )
	begin
--category_id	3
--status_id	4
--severity_id	5
--request_type_id	6
--office_id	30
		if( @field_id = 3 )
		begin 
			set @field_name = 'category_id'
		end
		else if ( @field_id = 4 )
		begin 
			set @field_name = 'status_id'
		end
		else if ( @field_id = 5 ) 
		begin 
			set @field_name = 'severity_id'
		end
		else if ( @field_id = 6 ) 
		begin 
			set @field_name = 'request_type_id'
		end
		else if ( @field_id = 30 ) 
		begin
			set @field_name = 'office_id'
		end

		declare @script varchar(3999)
		if( @dry_run = 'n' )
		begin
			set @script = 'update actions set ' + @field_name + '=' + convert(varchar,@type2_id) + ' where sys_id='+convert(varchar,@sys_id) + ' and ' + @field_name + '=' + convert(varchar,@type1_id)
		end
		else
		begin
			set @script = ' select ''' +  @field_name + ' will be changed in following actions from ' + convert(varchar,@type1_id) + ' to '  + convert(varchar,@type2_id) + ''' GO select * from actions where sys_id='+convert(varchar,@sys_id) + ' and ' + @field_name + '=' + convert(varchar,@type1_id)
		end
		print @script
		exec ( @script )		

		declare @script2 varchar(3999)
		if( @dry_run = 'n' )
		begin
			set @script2 = 'update requests set ' + @field_name + '=' + convert(varchar,@type2_id) + ' where sys_id='+convert(varchar,@sys_id) + ' and ' + @field_name + '=' + convert(varchar,@type1_id)
		end
		else
		begin
			set @script2 = ' select ''' +  @field_name + ' will be changed in following requests from ' + convert(varchar,@type1_id) + ' to '  + convert(varchar,@type2_id) + '''GO select * from requests where sys_id='+convert(varchar,@sys_id) + ' and ' + convert(varchar,@field_name) + '=' + convert(varchar,@type1_id )
		end
		print @script2
		exec ( @script2 )		
	end
	else		
	begin
		if( @dry_run = 'n' )
		begin
			update actions_ex set type_value = @type2_id where sys_id = @sys_id and field_id = @field_id and type_value = @type1_id
		end
		else 
		begin
			select 'following action_ex''s type_values will be changed from ' + convert(varchar,@type1_id) + ' to ' + convert(varchar,@type2_id )
			select * from actions_ex where sys_id = @sys_id and field_id = @field_id and type_value = @type1_id
		end	

		if( @dry_run = 'n' )
		begin
			update requests_ex set type_value = @type2_id where sys_id = @sys_id and field_id = @field_id and type_value = @type1_id
		end
		else 
		begin
			select 'following requests_ex''s type_values will be changed from ' + convert(varchar,@type1_id) + ' to ' + convert(varchar,@type2_id)
			select * from requests_ex where sys_id = @sys_id and field_id = @field_id and type_value = @type1_id
		end	
	end

	if( @dry_run = 'n' )
	begin
		delete from type_users where sys_id = @sys_id and field_id = @field_id and type_id = @type1_id
	end
	else
	begin
		select 'following type_user entries will be delete.'
		select * from type_users where sys_id = @sys_id and field_id = @field_id and type_id = @type1_id
	end

END
GO


