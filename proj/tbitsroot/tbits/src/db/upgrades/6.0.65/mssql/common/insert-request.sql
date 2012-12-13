ALTER PROCEDURE [dbo].[stp_request_insert] 
( 
        @systemId               INT, 
        @requestId              BIGINT, 
        @categoryId             INT, 
        @statusId               INT, 
        @severityId             INT, 
        @requestTypeId          INT, 
        @subject                NVARCHAR(4000), 
        @description            NTEXT, 
        @IsPrivate              BIT, 
        @parentId               BIGINT, 
        @userId                 INT, 
        @maxActionId            INT, 
        @dueDate                DATETIME, 
        @loggedDate             DATETIME, 
        @updatedDate            DATETIME, 
        @headerDesc             NTEXT, 
        @attachments            NTEXT, 
        @summary                NTEXT, 
        @memo                   NTEXT, 
        @append         INT, 
        @notify                 INT, 
        @notifyLoggers          BIT, 
        @repliedToAction        INT, 
        @officeId                         INT 
) 
AS 
--- Max Action Id is always 1 when inserting the request. 
SELECT @maxActionId = 1 
--- Now insert the request. 
INSERT INTO requests 
( 
        sys_id,                 request_id,             category_id, 
        status_id,              severity_id,            request_type_id, 
        subject,                description,            is_private, 
        parent_request_id,      user_id,                max_action_id, 
        due_datetime,           logged_datetime,        lastupdated_datetime, 
        header_description,     attachments,			summary, 
        memo,                   append_interface,       notify, 
        notify_loggers, replied_to_action,office_id 
) 
VALUES 
( 
        @systemId,              @requestId,             @categoryId, 
        @statusId,              @severityId,            @requestTypeId, 
        @subject,               @description,           @IsPrivate, 
        @parentId,              @userId,                @maxActionId, 
        @dueDate,               @loggedDate,            @updatedDate, 
        @headerDesc,            @attachments,			@summary, 
        @memo,                  @append,                @notify, 
        @notifyLoggers,         @repliedToAction,          @officeId 
)
---- Insert the corresponding record into actions table. 
INSERT INTO actions 
( 
        sys_id,                 request_id,             action_id, 
        category_id,            status_id,              severity_id, 
        request_type_id,        subject,                description, 
        is_private,             parent_request_id,      user_id, 
        due_datetime,           logged_datetime,        lastupdated_datetime, 
        header_description,     attachments,			summary, 
        memo,                   append_interface,       notify, 
        notify_loggers,         replied_to_action,      office_id 
) 
VALUES 
( 
        @systemId,              @requestId,             @maxActionId, 
        @categoryId,            @statusId,              @severityId, 
        @requestTypeId,         @subject,               @description, 
        @IsPrivate,             @parentId,              @userId, 
        @dueDate,               @loggedDate,            @updatedDate, 
        @headerDesc,            null,			@summary, 
        @memo,                  @append,                @notify, 
        @notifyLoggers,         @repliedToAction,       @officeId 
)




