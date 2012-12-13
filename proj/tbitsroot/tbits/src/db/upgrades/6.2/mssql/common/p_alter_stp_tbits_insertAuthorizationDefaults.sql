set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
go




ALTER PROCEDURE [dbo].[stp_tbits_insertAuthorizationDefaults]
(
    @systemId INT
)
AS

/*
 * Delete standard roles.
 */
DELETE roles WHERE sys_id = @systemId

INSERT INTO roles(sys_id, role_id, rolename, description, field_id,can_be_deleted) VALUES(@systemId, 1, 'User', 'User',0,0)
INSERT INTO roles(sys_id, role_id, rolename, description, field_id,can_be_deleted) VALUES(@systemId, 2, 'Logger', 'Logger',7,0)
INSERT INTO roles(sys_id, role_id, rolename, description, field_id,can_be_deleted) VALUES(@systemId, 3, 'Assignee', 'Assignee',8,0)
INSERT INTO roles(sys_id, role_id, rolename, description, field_id,can_be_deleted) VALUES(@systemId, 4, 'Subscriber', 'Subscriber',9,0)
INSERT INTO roles(sys_id, role_id, rolename, description, field_id,can_be_deleted) VALUES(@systemId, 5, 'Cc', 'Cc',11,0)
INSERT INTO roles(sys_id, role_id, rolename, description, field_id,can_be_deleted) VALUES(@systemId, 6, 'To', 'To',10,0)
INSERT INTO roles(sys_id, role_id, rolename, description, field_id,can_be_deleted) VALUES(@systemId, 7, 'Analyst', 'Analyst',0,0)
INSERT INTO roles(sys_id, role_id, rolename, description, field_id,can_be_deleted) VALUES(@systemId, 8, 'Manager', 'Manager',0,0)
INSERT INTO roles(sys_id, role_id, rolename, description, field_id,can_be_deleted) VALUES(@systemId, 9, 'Admin', 'Admin',0,0)
INSERT INTO roles(sys_id, role_id, rolename, description, field_id,can_be_deleted) VALUES(@systemId, 10, 'PermissionAdmin', 'Permission Admin',0,0)
INSERT INTO roles(sys_id, role_id, rolename, description, field_id,can_be_deleted) VALUES(@systemId, 12, 'BAUsers', 'BAUsers',0,0)
/*
 * Delete permissions standard roles have on standard fields.
 */
DELETE roles_permissions 
WHERE 
    sys_id = @systemId AND 
    field_id IN 
    (
        SELECT 
            field_id 
        FROM 
            fields 
        WHERE 
            sys_id = @systemId AND 
            is_extended = 0
    )

INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,1,0,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,2,14,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,3,12,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,4,12,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,5,12,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,6,12,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,7,12,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,8,12,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,9,14,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,10,15,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,11,15,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,12,12,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,13,13,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,15,15,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,16,12,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,17,12,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,18,12,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,19,12,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,20,12,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,21,12,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,22,13,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,23,12,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,24,15,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,26,12,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,27,12,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,28,12,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,29,12,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,1,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,1,0,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,2,1,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,7,2,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,9,1,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,11,1,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,12,3,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,14,14,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,23,3,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,2,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,1,12,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,3,2,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,4,2,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,5,2,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,6,2,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,8,2,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,9,2,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,12,2,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,14,14,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,18,2,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,23,2,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,26,2,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,27,2,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,29,2,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,3,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,1,12,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,9,0,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,12,0,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,14,14,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,23,0,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,4,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,1,12,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,2,15,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,3,12,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,4,12,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,5,15,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,6,12,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,7,12,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,8,12,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,9,15,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,10,15,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,11,15,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,12,12,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,13,13,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,15,15,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,16,12,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,17,12,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,18,12,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,19,12,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,20,12,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,21,12,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,22,13,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,23,12,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,24,15,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,26,12,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,27,12,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,28,12,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,29,15,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,5,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,1,12,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,2,15,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,3,12,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,4,12,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,5,15,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,6,12,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,7,12,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,8,12,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,9,15,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,10,15,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,11,15,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,12,12,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,13,13,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,15,15,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,16,12,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,17,12,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,18,12,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,19,12,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,20,12,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,21,12,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,22,13,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,23,12,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,24,15,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,26,12,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,27,12,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,28,12,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,29,15,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,6,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,1,0,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,9,0,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,12,0,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,23,0,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,7,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,1,0,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,9,0,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,12,0,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,23,0,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,8,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,1,12,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,3,2,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,5,2,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,6,2,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,8,3,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,9,2,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,12,2,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,14,14,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,18,2,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,23,2,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,26,2,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,27,2,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,29,2,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,9,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,1,12,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,2,15,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,3,15,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,4,15,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,5,15,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,6,15,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,7,15,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,8,15,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,9,15,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,10,15,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,11,15,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,12,15,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,13,13,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,14,14,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,15,15,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,16,12,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,17,12,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,18,15,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,19,12,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,20,12,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,21,12,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,22,13,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,23,15,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,24,15,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,26,15,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,27,15,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,28,12,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,29,15,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,30,15,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,10,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,1,0,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,9,0,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,12,0,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,23,0,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,11,31,0,0)	--SendSMS
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,1,12,0)	--BusinessArea
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,2,0,0)	--Request
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,3,0,0)	--Category
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,4,0,0)	--Status
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,5,0,0)	--Severity
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,6,0,0)	--RequestType
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,7,0,0)	--Logger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,8,0,0)	--Assignee
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,9,0,0)	--Subscribers
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,10,0,0)	--To
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,11,0,0)	--Cc
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,12,0,0)	--Subject
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,13,0,0)	--Description
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,14,0,0)	--Private
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,15,0,0)	--Parent
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,16,0,0)	--LastUpdateBy
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,17,0,0)	--#Updates
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,18,0,0)	--DueDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,19,0,0)	--LoggedDate
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,20,0,0)	--LastUpdated
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,21,0,0)	--HeaderDescription
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,22,0,0)	--Attachments
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,23,0,0)	--Summary
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,24,0,0)	--Memo
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,25,0,0)	--AppendInterface
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,26,0,0)	--Notify
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,27,0,0)	--NotifyLogger
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,28,0,0)	--RepliedToAction
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,29,0,0)	--LinkedRequests
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,30,0,0)	--Office
INSERT INTO roles_permissions(sys_id, role_id, field_id, gpermissions, dpermissions) VALUES(@systemId,12,31,0,0)	--SendSMS

GO
