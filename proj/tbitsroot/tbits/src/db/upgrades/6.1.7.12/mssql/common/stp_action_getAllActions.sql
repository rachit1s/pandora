set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO


ALTER PROCEDURE [dbo].[stp_action_getAllActions] 
( 
        @systemId int, 
        @requestId bigint , 
        @sortOrder varchar(10) 
) 
as 
begin 
declare @sort varchar(10) 
declare @query varchar(7999) 
select @query = 
' 
        SELECT 
        sys_id   , 
        request_id   , 
        action_id   , 
        category_id   , 
        status_id   , 
        severity_id   , 
        request_type_id   , 
        subject  , 
        description , 
		description_content_type,
        is_private   , 
        parent_request_id   , 
        user_id   , 
        due_datetime, 
        logged_datetime, 
        lastupdated_datetime, 
        header_description, 
        attachments, 
        summary, 
		summary_content_type,
        '''' as "memo", 
        append_interface   , 
        notify   , 
        notify_loggers   , 
        replied_to_action, 
        office_id 
        FROM 
                actions 
        WHERE 
                sys_id = ' + str(@systemId) + ' and 
                request_id=' +str( @requestId) + ' 
        order by action_id ' + @sortOrder + ' 
' 
exec(@query) 
end




