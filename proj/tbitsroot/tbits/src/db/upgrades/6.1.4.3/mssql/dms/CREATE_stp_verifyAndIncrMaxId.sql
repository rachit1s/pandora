set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:	 Nitiraj 
-- Create date: 
-- Description:	verifies if the given value is the same as current id + 1
--			     if yes it increments the current value and returns true 
--				 else it returns false
-- =============================================
CREATE PROCEDURE [dbo].[stp_verifyAndIncrMaxId] 
	@name varchar(250),
	@value int 
AS
BEGIN
BEGIN TRANSACTION
declare @maxid int
select @maxid = id from max_ids where name=@name
if @maxid is null
begin	
	if( @value = 1 ) 
		begin 
			insert into max_ids (name, id) values (@name, 1)			
			select 'true' result, 1 max_id;
		end
	else
		begin
			select 'false' result, 1 max_id;
		end	
end
else
begin
	if( @value = @maxid + 1 )
		begin
			update max_ids set id = @maxid + 1 where name=@name
			select 'true' result, @maxid+1 max_id ;
		end 
	else
		begin
			if( @value < @maxid + 1 )
				begin
					select 'true' result, @maxid + 1 max_id ;
				end 
			 else
				begin 
					select 'false' result, @maxid+1 max_id ;
				end
		end
end	
	commit TRAN
END



