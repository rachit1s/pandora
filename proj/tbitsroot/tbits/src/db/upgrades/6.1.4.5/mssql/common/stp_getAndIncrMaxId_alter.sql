set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Sandeep Giri
-- Create date: 
-- Description:	Gets the current max id And increases the max of Repo Id. It 
-- =============================================
ALTER PROCEDURE [dbo].[stp_getAndIncrMaxId] 
	@tableName varchar(250)
AS
BEGIN

declare @maxid int
BEGIN TRANSACTION
select @maxid = id from max_ids with (xlock, rowlock) 
where name=@tableName 
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
COMMIT TRANSACTION
END


