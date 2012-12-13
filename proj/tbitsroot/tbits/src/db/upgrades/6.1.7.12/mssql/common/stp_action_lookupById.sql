set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO


ALTER PROCEDURE [dbo].[stp_action_lookupById] 
( 
        @systemId 	INT, 
        @requestId 	INT, 
        @actionId 	INT
) 
as 

SELECT 
    sys_id, 
    request_id, 
    action_id, 
    category_id, 
    status_id, 
    severity_id, 
    request_type_id, 
    subject, 
    description, 
	description_content_type,
    is_private, 
    parent_request_id, 
    user_id, 
    due_datetime, 
    logged_datetime, 
    lastupdated_datetime, 
    isnull(header_description, '') "header_description", 
    isnull(attachments, '') "attachments", 
    isnull(summary, '') "summary",
	summary_content_type, 
    isnull(memo, '') "memo", 
    append_interface, 
    notify, 
    notify_loggers, 
    replied_to_action, 
    office_id 
FROM 
        actions act 
WHERE 
        sys_id          = @systemId AND 
        request_id      = @requestId AND 
        action_id       = @actionId 

SELECT 
        * 
FROM 
        action_users 
WHERE 
        sys_id          = @systemId AND 
        request_id      = @requestId AND 
        action_id       = @actionId




