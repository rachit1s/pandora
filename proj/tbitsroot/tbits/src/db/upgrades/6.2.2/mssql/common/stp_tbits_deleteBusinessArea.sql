set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO



ALTER PROCEDURE [dbo].[stp_tbits_deleteBusinessArea]
(  
 @systemId  int  
)  
as  

PRINT 'Deleting Exclusion List:'
delete exclusion_list where sys_id = @systemId  
PRINT 'Deleting Request Extended Fields:'
delete requests_ex where sys_id = @systemId  
PRINT 'Deleting Request Users:'
delete request_users where sys_id = @systemId  
PRINT 'Deleting Requests:'
delete requests where sys_id = @systemId  
PRINT 'Deleting Action Extended Fields:'
delete actions_ex where sys_id = @systemId  
PRINT 'Deleting Action Users:'
delete action_users where sys_id = @systemId  
PRINT 'Deleting Actions:'
delete actions where sys_id = @systemId  
PRINT 'Deleting Role-Users:'
delete roles_users where sys_id = @systemId  
PRINT 'Deleting Role-Permissions:'
delete roles_permissions where sys_id = @systemId  
PRINT 'Deleting Roles:'
delete roles where sys_id = @systemId  
PRINT 'Deleting BA Users:'
delete business_area_users where sys_id = @systemId
PRINT 'Deleting Type Users:'
delete type_users where sys_id = @systemId  
PRINT 'Deleting Types:'
delete types where sys_id = @systemId  
PRINT 'Deleting Field Descriptors:'
delete field_descriptors where sys_id = @systemId  
PRINT 'Deleting Fields:'
delete fields where sys_id = @systemId  
PRINT 'Deleting BA Record:'
delete business_areas where sys_id = @systemId

PRINT 'Deleting ba_menu_mapping:'
delete ba_menu_mapping where sys_id = @systemId
PRINT 'Deleting captions_properties:'
delete captions_properties where sys_id = @systemId

PRINT 'Deleting display_groups:'
delete display_groups where sys_id = @systemId
PRINT 'Deleting escalation_conditions:'
delete escalation_conditions where sys_id = @systemId

PRINT 'Deleting escalation_history:'
delete escalation_history where sys_id = @systemId
PRINT 'Deleting escalation_heirarchy:'
delete escalation_heirarchy where sys_id = @systemId

PRINT 'Deleting user_grid_col_prefs:'
delete user_grid_col_prefs where sys_id = @systemId

PRINT 'Deleting folder_structure:'
delete folder_structure where sys_id = @systemId


