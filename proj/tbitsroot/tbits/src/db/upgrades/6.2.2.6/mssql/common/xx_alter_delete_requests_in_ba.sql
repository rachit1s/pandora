set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO


ALTER PROCEDURE [dbo].[stp_request_delete_requests_in_ba] 
(
    @sys_prefix	varchar(30),
    @request_id int,
    @description varchar(200),
    @executor_name Varchar(50)
     
)
AS
declare @systemId INT
BEGIN

	  select @systemId = sys_id from business_areas where sys_prefix = @sys_prefix

   
      if not exists( select * from INFORMATION_SCHEMA.TABLES where Table_name = 'deleted_requests')
      Create table deleted_requests ( sys_id int,request_id int,Description varchar(200),executor_name varchar(50) )         
               
    
  BEGIN TRANSACTION            
   
        Select * from requests where sys_id = @systemId and request_id = @request_id

	delete from action_users where sys_id = @systemId and  request_id = @request_id

	delete from request_users where sys_id = @systemId and request_id = @request_id


	delete from user_read_actions where sys_id = @systemId and request_id = @request_id
    
        delete from user_drafts where sys_id = @systemId and request_id = @request_id


	delete from transferred_requests where source_prefix = @sys_prefix and  source_request_id = @request_id
    
	delete from transferred_requests where target_prefix = @sys_prefix and  target_request_id = @request_id


	delete from related_requests where primary_sys_id = @systemId and primary_request_id = @request_id

        delete from related_requests where related_sys_id = @systemId and related_request_id = @request_id
	
	delete from tags_requests where sys_id = @systemId and request_id = @request_id

        delete actions_ex where sys_id = @systemId and request_id = @request_id

	delete from actions where sys_id = @systemId and request_id = @request_id

	delete from requests_ex where sys_id = @systemId and request_id = @request_id
	
        delete from requests where sys_id = @systemId and request_id = @request_id
	
	delete from versions where sys_id = @systemId and request_id = @request_id

  COMMIT TRANSACTION

  insert into deleted_requests values (@systemId ,@request_id,@description,@executor_name) 

    
	
END
