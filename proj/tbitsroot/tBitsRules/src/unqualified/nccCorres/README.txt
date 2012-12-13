
The following rules have been implemented.
The value of the fields General Attributes and WBS Attributes  and Corr Type cannot be "-" if the logger's agency is NCCP NCCB KNPL

The value of the field Package cannot be "-" if the logger's agency is  KNPL

The value of the field Decision cannot be "-" if the logger's agency is DCPL

More than one logger not allowed. More than one assignee is allowed. The recepient will be decided according to the first assignee in the list.

Corr Type can be set to ION only during add request. Once set to ION it cannot be set to any thing other.

If Corr Type is ION then only NCCP and NCCB users will be allowed in loggers, assignees, subscribers, ccs,

The LOGIN user must be NCCP or NCCB if he has to set the corr type to ION. ( no front end rule for this yet)

Separate numbering system for KNPL ( KNPL - assignee FirmCode - contractReference - package - FinancialYear - Running Number )
( the max_ids corresponding it are of format : loggerFirmCode - contractReference - package - FinancialYear - Running Number )


For agency = NCCP and NCCB
numbering system for any corr-type including ION ( logger firmcode - assignee FirmCode - contractReference - corr Type - FinancialYear - Running Number )
 ( the max_ids corresponding it are of format : "NPT10109 -financialYear )


For Agency = DCPL 
the numbering system is  ( K9210-discipline-running.no. )
( the max_id corresponding to this is a constant : DCPL-K9210 )
 
For Agency = DESN
the numbering system is ( D3034-VC20-<HYD/"">-Running number)
(max_id is : DESEIN )

Numbering system for all other agency ( NPT10109  - logger firmcode - assignee FirmCode - contractReference - corr Type - FinancialYear - Running Number )
 ( the max_ids corresponding it are of format : NPT10109  - logger firmcode - assignee FirmCode - contractReference - corr Type - FinancialYear )

If any of the user belongs to KNPL then include the  knpl.kvk@gmail.com in CC in the post-rule so that the mail is sent to this email.

A list of <user_login>:<user_login>[,<user_login>] is maintained in the plugin folder which will mention that which user will be able to log 
request on behalf of which other users.if the list is empty then the user will not be able to log the request at all. For all other cases
the user will be able to log request only with himself as logger. This mapping is maintained in a table called corres_map in the db
