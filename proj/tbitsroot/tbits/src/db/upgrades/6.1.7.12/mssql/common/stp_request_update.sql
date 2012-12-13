set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO



ALTER PROCEDURE [dbo].[stp_request_update] 
( 
        @systemId       INT, 
        @requestId      BIGINT, 
        @categoryId     INT, 
        @statusId       INT, 
        @severityId     INT, 
        @requestTypeId  INT, 
        @subject        NVARCHAR(4000), 
        @description    NTEXT,
        @IsPrivate      BIT, 
        @parentId       BIGINT, 
        @userId         INT, 
        @maxActionId    INT OUTPUT, 
        @dueDate        DATETIME, 
        @loggedDate     DATETIME, 
        @updatedDate    DATETIME, 
        @headerDesc     NTEXT, 
        @attachments    NTEXT, 
        @summary        NTEXT, 
        @memo           NTEXT, 
        @append         INT, 
        @notify         INT, 
        @notifyLoggers  BIT, 
        @repliedToAction INT, 
        @officeId             INT ,
		@descriptionContentType    INT,
		@summaryContentType		INT
) 
AS 
--- Read the max action id from requests and add one to it. 
SELECT 
        @maxActionId = ISNULL(max_action_id, 0) + 1 
FROM 
        requests 
WHERE 
        sys_id          = @systemId AND 
        request_id      = @requestId 
--- We cannot hold this value of max_action_id with us till the end of this transaction 
--- as other processes might be interested in inserting actions in this request in the meantime. 
--- So, update the requests table with this new max_action_id value. 
UPDATE requests 
SET 
        max_action_id = @maxActionId 
WHERE 
        sys_id          = @systemId AND 
        request_id      = @requestId 
--- Now update the request. 
UPDATE requests 
SET 
        sys_id                  = @systemId, 
        request_id              = @requestId, 
        category_id             = @categoryId, 
        status_id               = @statusId, 
        severity_id             = @severityId, 
        request_type_id         = @requestTypeId, 
        subject                 = @subject, 
        description             = @description, 
		description_content_type = @descriptionContentType,
        is_private              = @isPrivate, 
        parent_request_id       = @parentId, 
        user_id                 = @userId, 
        max_action_id           = @maxActionId, 
        due_datetime            = @dueDate, 
        logged_datetime         = @loggedDate, 
        lastupdated_datetime    = @updatedDate, 
        header_description      = @headerDesc, 
        attachments             = @attachments, 
        memo                    = @memo, 
        append_interface        = @append, 
        notify                  = @notify, 
        notify_loggers          = @notifyLoggers, 
        replied_to_action       =  @repliedToAction, 
        office_id               =  @officeId,
		summary_content_type	= @summaryContentType 
WHERE 
        sys_id          = @systemId AND 
        request_id      = @requestId 
-- If summary is NOT NULL, then it should be updated in the request. 
-- Otherwise retain the old subject. 
IF (@summary IS NOT NULL) 
BEGIN 
        UPDATE requests 
        SET summary = @summary 
        WHERE 
                sys_id          = @systemId AND 
                request_id      = @requestId 
END 
---- Insert the corresponding record into actions table. 
INSERT INTO actions 
( 
        sys_id,                 request_id,             action_id, 
        category_id,            status_id,              severity_id, 
        request_type_id,        subject,                description,		description_content_type,
        is_private,             parent_request_id,      user_id, 
        due_datetime,           logged_datetime,        lastupdated_datetime, 
        header_description,     attachments,            summary,			summary_content_type,
        memo,                   append_interface,       notify, 
        notify_loggers,         replied_to_action,      office_id 
) 
VALUES 
( 
        @systemId,              @requestId,             @maxActionId, 
        @categoryId,            @statusId,              @severityId, 
        @requestTypeId,         @subject,               @description,		@descriptionContentType,
        @IsPrivate,             @parentId,              @userId, 
        @dueDate,               @loggedDate,            @updatedDate, 
        @headerDesc,            @attachments,           @summary, 			@summaryContentType,
        @memo,                  @append,                @notify, 
        @notifyLoggers,         @repliedToAction,       @officeId 
)







