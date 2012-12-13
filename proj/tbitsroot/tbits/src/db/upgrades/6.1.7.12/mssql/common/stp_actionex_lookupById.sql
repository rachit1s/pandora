set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO

ALTER PROCEDURE [dbo].[stp_actionex_lookupById]
(
        @systemId     INT,
        @requestId     INT,
        @actionId     INT,
    @fieldId    INT
)
as

SELECT
    sys_id,
    request_id,
    action_id,
    field_id,
    bit_value,
    datetime_value,
    int_value,
    real_value,
    varchar_value,
    text_value,
	text_value_content_type,
    type_value   
FROM
        actions_ex act_ex
WHERE
        sys_id          = @systemId AND
        request_id      = @requestId AND
        action_id       = @actionId AND
    field_id    = @fieldId 

