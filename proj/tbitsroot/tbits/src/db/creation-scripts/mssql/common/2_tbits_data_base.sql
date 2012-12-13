/*
 * Pre-requisite data for running tBits.
 *
 * @author  : vaibhav
 * @version : $Id: $
 */
 
-- Delete the old records if any and populate again.
DELETE datetime_formats
DELETE datatypes
DELETE exclusion_list
DELETE permissions
DELETE user_types
DELETE notification_rules
DELETE holidays_list 
DELETE workflow_rules


-- Insert the datetime formats.
INSERT INTO datetime_formats(format_id, format) VALUES(1, 'MM/dd/yyyy')
INSERT INTO datetime_formats(format_id, format) VALUES(2, 'yyyy-MM-dd')
INSERT INTO datetime_formats(format_id, format) VALUES(3, 'dd-MMM-yyyy')
INSERT INTO datetime_formats(format_id, format) VALUES(4, 'MMM dd, yyyy')
INSERT INTO datetime_formats(format_id, format) VALUES(5, 'MM/dd/yyyy HH:mm:ss')
INSERT INTO datetime_formats(format_id, format) VALUES(6, 'MM/dd/yyyy HH:mm:ss zzz')
INSERT INTO datetime_formats(format_id, format) VALUES(7, 'MM/dd/yyyy HH:mm:ss.SSS')
INSERT INTO datetime_formats(format_id, format) VALUES(8, 'dd-MMM-yyyy HH:mm:ss')
INSERT INTO [display_groups] (sys_id,display_name,display_order,is_active) VALUES (0,'Default',0,0)

------- INSERT Data Types -------------
INSERT INTO datatypes(datatype_id, name, description) VALUES(1, 'bit', 'bit')
INSERT INTO datatypes(datatype_id, name, description) VALUES(2, 'date', 'date')
INSERT INTO datatypes(datatype_id, name, description) VALUES(3, 'time', 'time')
INSERT INTO datatypes(datatype_id, name, description) VALUES(4, 'datetime', 'datetime')
INSERT INTO datatypes(datatype_id, name, description) VALUES(5, 'int', 'int')
INSERT INTO datatypes(datatype_id, name, description) VALUES(6, 'real', 'real')
INSERT INTO datatypes(datatype_id, name, description) VALUES(7, 'varchar', 'varchar')
INSERT INTO datatypes(datatype_id, name, description) VALUES(8, 'text', 'text')
INSERT INTO datatypes(datatype_id, name, description) VALUES(9, 'type', 'type')
INSERT INTO datatypes(datatype_id, name, description) VALUES(10, 'multi-value', 'multi-value')
INSERT INTO datatypes (datatype_id, name, description) values(11,	'attachments',	'attachments')

---------- INSERT PERMISSIONS -----------------
INSERT INTO permissions(permission, pview, pchange, padd) VALUES(0, 0, 0, 0)
INSERT INTO permissions(permission, pview, pchange, padd) VALUES(1, 0, 0, 1)
INSERT INTO permissions(permission, pview, pchange, padd) VALUES(2, 0, 1, 0)
INSERT INTO permissions(permission, pview, pchange, padd) VALUES(3, 0, 1, 1)
INSERT INTO permissions(permission, pview, pchange, padd) VALUES(4, 1, 0, 0)
INSERT INTO permissions(permission, pview, pchange, padd) VALUES(5, 1, 0, 1)
INSERT INTO permissions(permission, pview, pchange, padd) VALUES(6, 1, 1, 0)
INSERT INTO permissions(permission, pview, pchange, padd) VALUES(7, 1, 1, 1)

------ INSERT USER TYPES ------------
INSERT INTO user_types(user_type_id, name) VALUES(1,'User')
INSERT INTO user_types(user_type_id, name) VALUES(2,'Logger')
INSERT INTO user_types(user_type_id, name) VALUES(3,'Assignee')
INSERT INTO user_types(user_type_id, name) VALUES(4,'Subscriber')
INSERT INTO user_types(user_type_id, name) VALUES(5,'To')
INSERT INTO user_types(user_type_id, name) VALUES(6,'Cc')
INSERT INTO user_types(user_type_id, name) VALUES(7,'Internal-User')
INSERT INTO user_types(user_type_id, name) VALUES(8,'Internal-Mail-List')
INSERT INTO user_types(user_type_id, name) VALUES(9,'External-E-Mail')
INSERT INTO user_types(user_type_id, name) VALUES(10,'Internal-Contact')

-----------INSERT INTO QRTZ_JOB_DETAILS---------------------------------
INSERT INTO QRTZ_JOB_DETAILS (JOB_NAME, JOB_GROUP, DESCRIPTION, JOB_CLASS_NAME, IS_DURABLE, IS_VOLATILE, IS_STATEFUL, REQUESTS_RECOVERY)  VALUES('BirtAgeingReportSender','MaintenanceGroup','The Ageing Report','transbit.tbits.scheduler.BirtReportMailer',1,0,0,0)
INSERT INTO QRTZ_JOB_DETAILS (JOB_NAME, JOB_GROUP, DESCRIPTION, JOB_CLASS_NAME, IS_DURABLE, IS_VOLATILE, IS_STATEFUL, REQUESTS_RECOVERY)  VALUES('BirtDailyReportSender','MaintenanceGroup','End of the day report','transbit.tbits.scheduler.BirtReportMailer',1,0,0,0)
INSERT INTO QRTZ_JOB_DETAILS (JOB_NAME, JOB_GROUP, DESCRIPTION, JOB_CLASS_NAME, IS_DURABLE, IS_VOLATILE, IS_STATEFUL, REQUESTS_RECOVERY)  VALUES('DailyTaskList','MaintenanceGroup','The Daily Task Report','transbit.tbits.report.PerUserReportEmailer',1,0,0,0)
INSERT INTO QRTZ_JOB_DETAILS (JOB_NAME, JOB_GROUP, DESCRIPTION, JOB_CLASS_NAME, IS_DURABLE, IS_VOLATILE, IS_STATEFUL, REQUESTS_RECOVERY)  VALUES('IndexerDaemon','MaintenanceGroup','Incremental Indexer.','transbit.tbits.scheduler.MaintenanceJob',1,0,0,0)
INSERT INTO QRTZ_JOB_DETAILS (JOB_NAME, JOB_GROUP, DESCRIPTION, JOB_CLASS_NAME, IS_DURABLE, IS_VOLATILE, IS_STATEFUL, REQUESTS_RECOVERY)  VALUES('IndexOptimizer','MaintenanceGroup','Index Optimizer.','transbit.tbits.scheduler.MaintenanceJob',1,0,0,0)
INSERT INTO QRTZ_JOB_DETAILS (JOB_NAME, JOB_GROUP, DESCRIPTION, JOB_CLASS_NAME, IS_DURABLE, IS_VOLATILE, IS_STATEFUL, REQUESTS_RECOVERY)  VALUES('LoggedByMeDailyTaskList','MaintenanceGroup','The Daily Task Report','transbit.tbits.report.PerUserReportEmailer',1,0,0,0)
INSERT INTO QRTZ_JOB_DETAILS (JOB_NAME, JOB_GROUP, DESCRIPTION, JOB_CLASS_NAME, IS_DURABLE, IS_VOLATILE, IS_STATEFUL, REQUESTS_RECOVERY)  VALUES('PreventiveAlerts','PreventiveAlertsGroup','Send preventive alerts','transbit.tbits.scheduler.PreventiveAlerts',1,0,0,0)
INSERT INTO QRTZ_JOB_DETAILS (JOB_NAME, JOB_GROUP, DESCRIPTION, JOB_CLASS_NAME, IS_DURABLE, IS_VOLATILE, IS_STATEFUL, REQUESTS_RECOVERY)  VALUES('RecieveAllMails','MaintenanceGroup','Receive All Mails of business areas.','transbit.tbits.scheduler.MaintenanceJob',1,0,0,0)
INSERT INTO QRTZ_JOB_DETAILS (JOB_NAME, JOB_GROUP, DESCRIPTION, JOB_CLASS_NAME, IS_DURABLE, IS_VOLATILE, IS_STATEFUL, REQUESTS_RECOVERY)  VALUES('SeverityBasedEscalation','MaintenanceGroup','SeverityBasedEscalation.','transbit.tbits.scheduler.SeverityBasedEscalation',1,0,0,0)
INSERT INTO QRTZ_JOB_DETAILS (JOB_NAME, JOB_GROUP, DESCRIPTION, JOB_CLASS_NAME, IS_DURABLE, IS_VOLATILE, IS_STATEFUL, REQUESTS_RECOVERY)  VALUES('SyncDependentTypes','MaintenanceGroup','Sync Dependent Types.','transbit.tbits.scheduler.MaintenanceJob',1,0,0,0)

-------------INSERT INTO QRZT_JOB_DATA_MAP-----------------------------------

INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('BirtAgeingReportSender','MaintenanceGroup','body','Please find the overdue tasks report attached.')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('BirtAgeingReportSender','MaintenanceGroup','fileName','ageingreport.pdf')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('BirtAgeingReportSender','MaintenanceGroup','fromAddress','donotreply@localhost')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('BirtAgeingReportSender','MaintenanceGroup','recipients','sandeep@localhost')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('BirtAgeingReportSender','MaintenanceGroup','reportURL','http://localhost:81/reports/frameset?__report=express_it_ageing.rptdesign&sys_id=1&__format=pdf')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('BirtAgeingReportSender','MaintenanceGroup','subject','The ageing report of IT Helpdesk')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('BirtDailyReportSender','MaintenanceGroup','body','Please find the report  on daily opened and closed requests attached.')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('BirtDailyReportSender','MaintenanceGroup','fileName','opened-closed-eod.pdf')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('BirtDailyReportSender','MaintenanceGroup','fromAddress','donotreply@localhost')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('BirtDailyReportSender','MaintenanceGroup','recipients','root@localhost')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('BirtDailyReportSender','MaintenanceGroup','reportURL','http://alpha0:84/reports/frameset?__report=daily_report.rptdesign&daysAgo=30&__format=pdf')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('BirtDailyReportSender','MaintenanceGroup','subject','End of the day report- Opened/Closed Requests on $date')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('DailyTaskList','MaintenanceGroup','essentialData','overduetasks')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('DailyTaskList','MaintenanceGroup','fromAddress','donotreply@localhost')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('DailyTaskList','MaintenanceGroup','includeExternalUsers','false')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('DailyTaskList','MaintenanceGroup','leaveOutputFile','false')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('DailyTaskList','MaintenanceGroup','reportfile','dailytasklist.rptdesign')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('DailyTaskList','MaintenanceGroup','sendmails','true')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('DailyTaskList','MaintenanceGroup','subject','Tasks assigned to $user as on $date')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('DailyTaskList','MaintenanceGroup','upcomingreqsdays','3')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('IndexerDaemon','MaintenanceGroup','CmdLineArgumentNameCSV','')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('IndexerDaemon','MaintenanceGroup','FQCN','transbit.tbits.indexer.IndexerDaemon')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('IndexOptimizer','MaintenanceGroup','CmdLineArgumentNameCSV','')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('IndexOptimizer','MaintenanceGroup','FQCN','transbit.tbits.indexer.OptimizeIndex')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('LoggedByMeDailyTaskList','MaintenanceGroup','essentialData','overduetasks')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('LoggedByMeDailyTaskList','MaintenanceGroup','fromAddress','donotreply@localhost')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('LoggedByMeDailyTaskList','MaintenanceGroup','includeExternalUsers','true')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('LoggedByMeDailyTaskList','MaintenanceGroup','leaveOutputFile','false')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('LoggedByMeDailyTaskList','MaintenanceGroup','reportfile','taskslogggedbyme.rptdesign')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('LoggedByMeDailyTaskList','MaintenanceGroup','sendmails','true')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('LoggedByMeDailyTaskList','MaintenanceGroup','subject','Tasks created by $user as on $date')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('LoggedByMeDailyTaskList','MaintenanceGroup','upcomingreqsdays','3')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('PreventiveAlerts','PreventiveAlertsGroup','alertTime','60')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('PreventiveAlerts','PreventiveAlertsGroup','fromAddress','donotreply@tbits')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('PreventiveAlerts','PreventiveAlertsGroup','recipients','a')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('PreventiveAlerts','PreventiveAlertsGroup','schFreq','5')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('PreventiveAlerts','PreventiveAlertsGroup','subject','Upcoming requests alert.')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('RecieveAllMails','MaintenanceGroup','CmdLineArgumentNameCSV','')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('RecieveAllMails','MaintenanceGroup','FQCN','transbit.tbits.mail.RecieveMail')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('SeverityBasedEscalation','MaintenanceGroup','description','[The request is being escalated automatically by tBits.]')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('SeverityBasedEscalation','MaintenanceGroup','user_id','root')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('SyncDependentTypes','MaintenanceGroup','CmdLineArgumentNameCSV','')
INSERT INTO QRTZ_JOB_DATA_MAP (JOB_NAME, JOB_GROUP, ENTRY, VALUE)  VALUES('SyncDependentTypes','MaintenanceGroup','FQCN','transbit.tbits.util.SyncDependentTypes')


--------------INSERT INTO QRTZ_TRIGGERS---------------------

INSERT INTO QRTZ_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP, JOB_NAME, JOB_GROUP, IS_VOLATILE, DESCRIPTION, NEXT_FIRE_TIME, PREV_FIRE_TIME, TRIGGER_STATE, TRIGGER_TYPE, START_TIME, END_TIME, CALENDAR_NAME, MISFIRE_INSTR)  VALUES('BirtAgeingReportSenderTriger','MaintenanceGroup','BirtAgeingReportSender','MaintenanceGroup',0,NULL,1213068600000,-1,'WAITING','CRON',1213046330000,0,NULL,0)
INSERT INTO QRTZ_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP, JOB_NAME, JOB_GROUP, IS_VOLATILE, DESCRIPTION, NEXT_FIRE_TIME, PREV_FIRE_TIME, TRIGGER_STATE, TRIGGER_TYPE, START_TIME, END_TIME, CALENDAR_NAME, MISFIRE_INSTR)  VALUES('BirtDailyReportSenderTrigger','MaintenanceGroup','BirtDailyReportSender','MaintenanceGroup',0,NULL,1213101000000,-1,'WAITING','CRON',1213046329000,0,NULL,0)
INSERT INTO QRTZ_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP, JOB_NAME, JOB_GROUP, IS_VOLATILE, DESCRIPTION, NEXT_FIRE_TIME, PREV_FIRE_TIME, TRIGGER_STATE, TRIGGER_TYPE, START_TIME, END_TIME, CALENDAR_NAME, MISFIRE_INSTR)  VALUES('DailyTaskListTriger','MaintenanceGroup','DailyTaskList','MaintenanceGroup',0,NULL,1213068600000,-1,'WAITING','CRON',1213046330000,0,NULL,0)
INSERT INTO QRTZ_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP, JOB_NAME, JOB_GROUP, IS_VOLATILE, DESCRIPTION, NEXT_FIRE_TIME, PREV_FIRE_TIME, TRIGGER_STATE, TRIGGER_TYPE, START_TIME, END_TIME, CALENDAR_NAME, MISFIRE_INSTR)  VALUES('IndexerDaemonTrigger','MaintenanceGroup','IndexerDaemon','MaintenanceGroup',0,NULL,1213046400000,-1,'WAITING','CRON',1213046330000,0,NULL,0)
INSERT INTO QRTZ_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP, JOB_NAME, JOB_GROUP, IS_VOLATILE, DESCRIPTION, NEXT_FIRE_TIME, PREV_FIRE_TIME, TRIGGER_STATE, TRIGGER_TYPE, START_TIME, END_TIME, CALENDAR_NAME, MISFIRE_INSTR)  VALUES('IndexOptimizerTrigger','MaintenanceGroup','IndexOptimizer','MaintenanceGroup',0,NULL,1213068600000,-1,'WAITING','CRON',1213046330000,0,NULL,0)
INSERT INTO QRTZ_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP, JOB_NAME, JOB_GROUP, IS_VOLATILE, DESCRIPTION, NEXT_FIRE_TIME, PREV_FIRE_TIME, TRIGGER_STATE, TRIGGER_TYPE, START_TIME, END_TIME, CALENDAR_NAME, MISFIRE_INSTR)  VALUES('LoggedByMeDailyTaskListTriger','MaintenanceGroup','LoggedByMeDailyTaskList','MaintenanceGroup',0,NULL,1213070400000,-1,'WAITING','CRON',1213046330000,0,NULL,0)
INSERT INTO QRTZ_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP, JOB_NAME, JOB_GROUP, IS_VOLATILE, DESCRIPTION, NEXT_FIRE_TIME, PREV_FIRE_TIME, TRIGGER_STATE, TRIGGER_TYPE, START_TIME, END_TIME, CALENDAR_NAME, MISFIRE_INSTR)  VALUES('PreventiveAlerts','PreventiveAlertsGroup','PreventiveAlerts','PreventiveAlertsGroup',0,NULL,1213046400000,-1,'WAITING','CRON',1213046329000,0,NULL,0)
INSERT INTO QRTZ_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP, JOB_NAME, JOB_GROUP, IS_VOLATILE, DESCRIPTION, NEXT_FIRE_TIME, PREV_FIRE_TIME, TRIGGER_STATE, TRIGGER_TYPE, START_TIME, END_TIME, CALENDAR_NAME, MISFIRE_INSTR)  VALUES('RecieveMailTrigger','MaintenanceGroup','RecieveAllMails','MaintenanceGroup',0,NULL,1213046340000,-1,'WAITING','CRON',1213046329000,0,NULL,0)
INSERT INTO QRTZ_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP, JOB_NAME, JOB_GROUP, IS_VOLATILE, DESCRIPTION, NEXT_FIRE_TIME, PREV_FIRE_TIME, TRIGGER_STATE, TRIGGER_TYPE, START_TIME, END_TIME, CALENDAR_NAME, MISFIRE_INSTR)  VALUES('SeverityBasedEscalationTrigger','MaintenanceGroup','SeverityBasedEscalation','MaintenanceGroup',0,NULL,1213046340000,-1,'WAITING','CRON',1213046330000,0,NULL,0)
INSERT INTO QRTZ_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP, JOB_NAME, JOB_GROUP, IS_VOLATILE, DESCRIPTION, NEXT_FIRE_TIME, PREV_FIRE_TIME, TRIGGER_STATE, TRIGGER_TYPE, START_TIME, END_TIME, CALENDAR_NAME, MISFIRE_INSTR)  VALUES('SyncDependentTypesTrigger','MaintenanceGroup','SyncDependentTypes','MaintenanceGroup',0,NULL,1213068600000,-1,'WAITING','CRON',1213046330000,0,NULL,0)

--------------INSERT INTO QRTZ_CRON_TRIGGERS-------------------------------

INSERT INTO QRTZ_CRON_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP,CRON_EXPRESSION,TIME_ZONE_ID)  VALUES('BirtAgeingReportSenderTriger','MaintenanceGroup','0 0 9 ? * *','Asia/Calcutta')
INSERT INTO QRTZ_CRON_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP,CRON_EXPRESSION,TIME_ZONE_ID)  VALUES('BirtDailyReportSenderTrigger','MaintenanceGroup','0 0 18 ? * *','Asia/Calcutta')
INSERT INTO QRTZ_CRON_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP,CRON_EXPRESSION,TIME_ZONE_ID)  VALUES('DailyTaskListTriger','MaintenanceGroup','0 0 9 ? * *','Asia/Calcutta')
INSERT INTO QRTZ_CRON_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP,CRON_EXPRESSION,TIME_ZONE_ID)  VALUES('IndexerDaemonTrigger','MaintenanceGroup','0 0/5 * ? * *','Asia/Calcutta')
INSERT INTO QRTZ_CRON_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP,CRON_EXPRESSION,TIME_ZONE_ID)  VALUES('IndexOptimizerTrigger','MaintenanceGroup','0 0 9 ? * *','Asia/Calcutta')
INSERT INTO QRTZ_CRON_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP,CRON_EXPRESSION,TIME_ZONE_ID)  VALUES('LoggedByMeDailyTaskListTriger','MaintenanceGroup','0 30 9 ? * *','Asia/Calcutta')
INSERT INTO QRTZ_CRON_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP,CRON_EXPRESSION,TIME_ZONE_ID)  VALUES('PreventiveAlerts','PreventiveAlertsGroup','0 0/5 * ? * *','Asia/Calcutta')
INSERT INTO QRTZ_CRON_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP,CRON_EXPRESSION,TIME_ZONE_ID)  VALUES('RecieveMailTrigger','MaintenanceGroup','0 0/1 * ? * *','Asia/Calcutta')
INSERT INTO QRTZ_CRON_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP,CRON_EXPRESSION,TIME_ZONE_ID)  VALUES('SeverityBasedEscalationTrigger','MaintenanceGroup','0 */1 * ? * *','Asia/Calcutta')
INSERT INTO QRTZ_CRON_TRIGGERS (TRIGGER_NAME, TRIGGER_GROUP,CRON_EXPRESSION,TIME_ZONE_ID)  VALUES('SyncDependentTypesTrigger','MaintenanceGroup','0 0 9 ? * *','Asia/Calcutta')

------------- INSERT GLOBALLY EXCLUDED ADDRESSES  [WITH USER_TYPE_ID = 0 ]-----------------
/*
admin@localhost'
*/
INSERT INTO exclusion_list
(
    sys_id, 
    user_id, 
    user_type_id
)
SELECT
	0,
	user_id,
	0
FROM 
	users
WHERE
	user_login in 
	(
        'admin'       
	)
	
INSERT INTO notification_rules (notification_id, name, display_name, rules_config) VALUES
(1, 'NeverSendMail', 'Never', 
'
<NotificationConfig>
	<SendMail value="never">
    </SendMail> 
</NotificationConfig>
'
)

INSERT INTO notification_rules (notification_id, name, display_name, rules_config) VALUES
(2, 'AlwaysSendMail', 'Always', 
'
<NotificationConfig>
	<SendMail value="always">
    </SendMail> 
</NotificationConfig>
'
)

INSERT INTO notification_rules (notification_id, name, display_name, rules_config) VALUES
(3, 'Off(NYC)', 'NYC Off Hours', 
'
<NotificationConfig>
	<SendMail value="rules">
		<Rule id="1" day="holiday" startTime="0:00" endTime="23:59" zone="EST" />
		<Rule id="2" day="monday,tuesday,wednesday,thursday,friday" startTime="0:00" endTime="9:00" zone="EST" />
		<Rule id="3" day="monday,tuesday,wednesday,thursday,friday" startTime="17:30" endTime="23:59" zone="EST" />
		<Rule id="4" day="saturday,sunday" startTime="0:00" endTime="23:59" zone="EST" />
</SendMail> 
</NotificationConfig>
')

INSERT INTO notification_rules (notification_id, name, display_name, rules_config) VALUES
(4, 'Off(HYD)', 'HYD  Off Hours', 
'
<NotificationConfig>
	<SendMail value="rules">
		<Rule id="1" day="holiday" startTime="0:00" endTime="23:59" zone="IST" />
		<Rule id="2" day="monday,tuesday,wednesday,thursday,friday" startTime="0:00" endTime="9:00" zone="IST" />
		<Rule id="3" day="monday,tuesday,wednesday,thursday,friday" startTime="17:30" endTime="23:59" zone="IST" />
		<Rule id="4" day="saturday,sunday" startTime="0:00" endTime="23:59" zone="IST" />
</SendMail> 
</NotificationConfig>
')


/* Populating India  and New York Office Holidays List*/
insert into holidays_list(office, holiday_date, office_zone) values('Hyd', '01/14/2005', 'IST')
insert into holidays_list(office, holiday_date, office_zone) values('Hyd', '01/26/2005', 'IST')
insert into holidays_list(office, holiday_date, office_zone) values('Hyd', '03/25/2005', 'IST')
insert into holidays_list(office, holiday_date, office_zone) values('Hyd', '04/22/2005', 'IST')
insert into holidays_list(office, holiday_date, office_zone) values('Hyd', '05/23/2005', 'IST')
insert into holidays_list(office, holiday_date, office_zone) values('Hyd', '08/15/2005', 'IST')
insert into holidays_list(office, holiday_date, office_zone) values('Hyd', '08/26/2005', 'IST')
insert into holidays_list(office, holiday_date, office_zone) values('Hyd', '09/07/2005', 'IST')
insert into holidays_list(office, holiday_date, office_zone) values('Hyd', '10/11/2005', 'IST')
insert into holidays_list(office, holiday_date, office_zone) values('Hyd', '10/12/2005', 'IST')
insert into holidays_list(office, holiday_date, office_zone) values('Hyd', '10/31/2005', 'IST')
insert into holidays_list(office, holiday_date, office_zone) values('Hyd', '11/04/2005', 'IST')
insert into holidays_list(office, holiday_date, office_zone) values('Nyc', '01/17/2005', 'EST')
insert into holidays_list(office, holiday_date, office_zone) values('Nyc', '02/21/2005', 'EST')
insert into holidays_list(office, holiday_date, office_zone) values('Nyc', '05/30/2005', 'EST')
insert into holidays_list(office, holiday_date, office_zone) values('Nyc', '07/04/2005', 'EST')
insert into holidays_list(office, holiday_date, office_zone) values('Nyc', '09/05/2005', 'EST')
insert into holidays_list(office, holiday_date, office_zone) values('Nyc', '11/24/2005', 'EST')
insert into holidays_list(office, holiday_date, office_zone) values('Nyc', '12/26/2005', 'EST')



INSERT INTO workflow_rules(rule_id, rule_name, rule_definition)
VALUES
(
1,
'Requests in pending category cannot be transferred without an assignee.',
'

<Rule>
    <Condition>
        <Check state="CHANGE" fieldName="category_id">
            <Old operator="EQ" value="pending" />
            <New operator="NE" value="pending" />
        </Check>
    </Condition>
    <Action>
        <Validate fieldName="assignee_ids" operator="NE" value="" />
    </Action>
    <Message value="Requests in pending category cannot be transferred without an assignee." />
</Rule>

'
)

INSERT INTO workflow_rules(rule_id, rule_name, rule_definition)
VALUES
(
2,
'Requests in pending category cannot be closed.',
'

<Rule>
    <Condition>
        <Check state="CURRENT" fieldName="category_id">
            <Current operator="EQ" value="pending" />
        </Check>
        <Check state="CHANGE" fieldName="status_id">
            <Old operator="NE" value="closed" />
            <New operator="EQ" value="closed" />
        </Check>
    </Condition>
    <Action>
        <Validate fieldName="status_id" operator="NE" value="closed" />
    </Action>
    <Message value="Requests in pending category cannot be closed." />
</Rule>

'
)

INSERT INTO workflow_rules(rule_id, rule_name, rule_definition)
VALUES
(
3,
'Unassigned Requests cannot be closed.',
'

<Rule>
    <Condition>
        <Check state="current" fieldName="assignee_ids">
            <Current operator="EQ" value="" />
        </Check>
        <Check state="CHANGE" fieldName="status_id">
            <Old operator="NE" value="closed" />
            <New operator="EQ" value="*" />
        </Check>
    </Condition>
    <Action>
        <Validate fieldName="status_id" operator="NE" value="closed" />
    </Action>
    <Message value="Unassigned Requests cannot be closed." />
</Rule>

'
)

INSERT INTO workflow_rules(rule_id, rule_name, rule_definition)
VALUES
(
4,
'Unassigned Requests cannot be in active state.',
'

<Rule>
    <Condition>
        <Check state="current" fieldName="assignee_ids">
            <Current operator="EQ" value="" />
        </Check>
        <Check state="CHANGE" fieldName="status_id">
            <Old operator="NE" value="active" />
            <New operator="EQ" value="*" />
        </Check>
    </Condition>
    <Action>
        <Validate fieldName="status_id" operator="NE" value="active" />
    </Action>
    <Message value="Unassigned Requests cannot be in active state." />
</Rule>

'
)

INSERT INTO workflow_rules(rule_id, rule_name, rule_definition)
VALUES
(
5,
'Send notification if a request is logged or appended from email.',
'
<Rule>
    <Condition>
        <Check state="current" fieldName="append_interface">
            <Current value="102" operator="EQ" />
        </Check>
        <Check state="current" fieldName="max_action_id">
            <Current value="1" operator="EQ" />
        </Check>
    </Condition>
    <Action>
        <Modify fieldName="notify" value="1" operator="EQ" />
    </Action>
    <Message value="Send notification if a request is logged or appended from email." />
</Rule>
'
)

INSERT INTO workflow_rules(rule_id, rule_name, rule_definition)
VALUES
(
8,
'Subscribe giris,ritesh when the logger is a memeber of rms@localhost',
'

<Rule>
    <Condition>
        <Check state="current" fieldName="max_action_id">
            <Current value="1" operator="EQ" />
        </Check>
        <Check state="current" fieldName="logger_ids">
            <Current operator="IN" value="rms@localhost"/>
        </Check>
    </Condition>
    <Action>
        <Modify fieldName="subscriber_ids" value="giris,ritesh" operator="APPEND" />
    </Action>
    <Message value="Subscribe giris,ritesh when the logger is a memeberof rms@localhost." />
</Rule>

'
)

INSERT INTO workflow_rules(rule_id, rule_name, rule_definition)
VALUES
(
9,
'Do not send mail when ops logs a request from email.',
'

<Rule>
    <Condition>
        <Check state="current" fieldName="max_action_id">
            <Current value="1" operator="EQ" />
        </Check>
        <Check state="current" fieldName="append_interface">
            <Current value="102" operator="EQ" />
        </Check>
        <Check state="current" fieldName="logger_ids">
            <Current operator="EQ" value="ops@localhost"/>
        </Check>
    </Condition>
    <Action>
        <Modify fieldName="notify" value="0" operator="SET" />
    </Action>
    <Message value="Do not send mail when ops@localhost logs a request from email." />
</Rule>

'
)

INSERT INTO workflow_rules(rule_id, rule_name, rule_definition)
VALUES
(
10,
'Do not send mail when nobody@localhost logs a request from email.',
'

<Rule>
    <Condition>
        <Check state="current" fieldName="max_action_id">
            <Current value="1" operator="EQ" />
        </Check>
        <Check state="current" fieldName="append_interface">
            <Current value="102" operator="EQ" />
        </Check>
        <Check state="current" fieldName="logger_ids">
            <Current operator="EQ" value="nobody@localhost"/>
        </Check>
    </Condition>
    <Action>
        <Modify fieldName="notify" value="0" operator="SET" />
    </Action>
    <Message value="Do not send mail when nobody@localhost logs a request from email." />
</Rule>

'
)

INSERT INTO workflow_rules(rule_id, rule_name, rule_definition)
VALUES
(
11,
'Add ops@localhost to the Cc list when a closed request is appended',
'

<Rule>
    <Condition>
        <Check state="change" fieldName="status_id">
            <Old value="closed" operator="EQ" />
	    <New value="" operator="NE" />
        </Check>
	<Check state="current" fieldName="notify">
            <Current operator="NE" value="0" />	
	</Check>
    </Condition>
    <Action>
        <Modify fieldName="cc_ids" value="ops@localhost" operator="APPEND" />
    </Action>
    <Message value="Add ops@localhost to the Cc list when a closed request is appended" />
</Rule>

'
)


INSERT INTO workflow_rules(rule_id, rule_name, rule_definition)
VALUES
(
14,
'Change the status of new requests to assigned if the assignee field is not empty.',
'

<Rule>
    <Condition>
        <Check state="current" fieldName="assignee_ids">
            <Current value="" operator="NE" />
        </Check>
        <Check state="current" fieldName="status_id">
            <Current value="new" operator="EQ" />
        </Check>
    </Condition>
    <Action>
        <Modify fieldName="status_id" value="assigned" operator="SET" />
    </Action>
    <Message value="Change the status of new requests to assigned if the assignee field is not empty." />
</Rule>

'
)

INSERT INTO workflow_rules(rule_id, rule_name, rule_definition)
VALUES
(
15,
'Change the status of open requests to active if the assignee field is not empty.',
'

<Rule>
    <Condition>
        <Check state="current" fieldName="assignee_ids">
            <Current value="" operator="NE" />
        </Check>
        <Check state="current" fieldName="status_id">
            <Current value="open" operator="EQ" />
        </Check>
    </Condition>
    <Action>
        <Modify fieldName="status_id" value="active" operator="SET" />
    </Action>
    <Message value="Change the status of open requests to active if the assignee field is not empty." />
</Rule>

'
)

INSERT INTO workflow_rules(rule_id, rule_name, rule_definition)
VALUES
(
16,
'Mark the request as private as soon as it is logged.',
'
<Rule>
    <Condition>
        <Check state="current" fieldName="max_action_id">
            <Current value="1" operator="EQ" />
        </Check>
    </Condition>
    <Action>
        <Modify fieldName="is_private" value="true" operator="SET" />
    </Action>
    <Message value="Mark the request as private as soon as it is logged." />
</Rule>
'
)

INSERT INTO workflow_rules(rule_id, rule_name, rule_definition)
VALUES
(
17,
'Only the logger can close a request.',
'
<Rule>
 <Condition>
  <Check state="change" fieldName="status_id">
   <OLD operator="NE" value="closed"/>
   <NEW operator="EQ" value="closed"/>
  </Check>
 </Condition>
 <Action>          
  <Validate fieldName="logger_ids" operator="EQ" value="$user_id" />      
 </Action>      
 <Message value="Only the logger can close a request."/>
</Rule>
'
)

INSERT INTO workflow_rules(rule_id, rule_name, rule_definition)
VALUES
(
18,
'Addition of requests is not allowed in Trial Version.',
'
<Rule>      
 <Condition>          
  <Check state="current" fieldName="max_action_id">              
   <Current value="1" operator="EQ" />          
  </Check>  
 </Condition>      
 <Action>          
  <Validate fieldName="max_action_id" operator="NE" value="1" /> 
 </Action>      
 <Message value="Addition of requests is not allowed in Trial Version." />  
</Rule>
'
)

INSERT INTO workflow_rules(rule_id, rule_name, rule_definition)
VALUES
(
19,
'Only the logger and few more can close a request.',
'
<Rule>
 <Condition>
  <Check state="change" fieldName="status_id">
   <OLD operator="NE" value="closed"/>
   <NEW operator="EQ" value="closed"/>
  </Check>
 </Condition>
 <Condition>
	<Check state="current" fieldName="user_id">
		<Current operator="NE" value="ritesh" />
	</Check>
	<Check state="current" fieldName="user_id">
		<Current operator="NE" value="sandeep" />
	</Check>
 </Condition>
 <Action>          
  <Validate fieldName="logger_ids" operator="EQ" value="$user_id" />      
 </Action>      
 <Message value="Only the logger can close a request."/>
</Rule>
'
)

INSERT INTO workflow_rules(rule_id, rule_name, rule_definition)
VALUES
(
20,
'Reopen the request if there is an action on closed request.',
'<Rule>
	<Condition>
		<Check state="CHANGE" fieldName="status_id">
		  <Old operator="EQ" value="closed" />
		  <New operator="EQ" value="closed" />
		</Check>
	</Condition>
	<Action>
		<Modify fieldName="status_id" value="open" operator="SET" />
	</Action>
	<Message value="Reopen the request if there is an action on closed request." />
</Rule>'
)

INSERT INTO users (user_id, user_login, first_name, last_name, display_name, email, is_active, user_type_id, web_config, windows_config, is_on_vacation) 
VALUES(1,'root','tBits','Root','Root, tBits','tbitsroot@localhost',1,7,'','',0)
insert into super_users(user_id, is_active) VALUES (1,1)
exec stp_admin_set_user_password 'root','root'
exec stp_tbits_createBusinessArea 'tBits Default', 'tbits'
INSERT INTO msg_format
(
	msg_format_id,
	msg_template,
	sys_id
)
VALUES
(
	0,
	'$sys_id$#$request_id$#$max_action_id$:$subject$ - $user_id$.',
	1
)
INSERT INTO msg_format
(
	msg_format_id,
	msg_template,
	sys_id
)
VALUES
(
	1,
	'Added $request_id$:$subject$ by  $user_id$.',
	1
)
INSERT INTO msg_format
(
	msg_format_id,
	msg_template,
	sys_id
)
VALUES
(
	2,
	'Updated $request_id$:$subject$  by $user_id$.',
	1
)

INSERT INTO tbits_properties(name,value) Values('transbit.app.version','6.0.32.2640')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.instanceList','india,us')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.intenet.proxypassword','proxypassword')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.extract','bin/xls2csv.exe')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.smtp.authenticate','n')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.smtp.port','465')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.isautovueenabled','true  ')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.nearestInstance','http://localhost:80')
INSERT INTO tbits_properties(name,value) Values('mail.pop3s.socketFactory.fallback','false')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.jvueserver','http://localhost:5098/servlet/VueServlet')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.adContactSearchQuery','(&(objectCategory=person)(objectClass=contact))')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.pdftotext','bin/pdftotext.exe')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.sms.UserAgent','Firefox/2.0.0.11')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.adGroupSearchQuery','(&(objectCategory=Group)(objectClass=group))')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.hydUrl','http://localhost:80')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.allowAutoAddUser','true')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.smtp.protocol','smtp')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.smsgatewayurl','http://api.znisms.com/post/message.asp?userid=sandeepgiri&pwd=****&messageid=$MESSAGE$&senderid=sandeepgiri&sendto=$CELLNO$&flash=no')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.intenet.proxyuser','proxyuser')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.intenet.isproxyenabled','false')
INSERT INTO tbits_properties(name,value) Values('mail.pop3s.connectiontimeout','600000')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.logdir','logs')
INSERT INTO tbits_properties(name,value) Values('transbit.logger.notifyFailureTo','root@snowwhite.mshome.net')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.adSearchBase','CN=Users,DC=ABHISHEK,DC=com')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.adHost','ldap://transbit2000')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.intenet.proxyport','3128')
INSERT INTO tbits_properties(name,value) Values('mail.smtps.socketFactory.fallback','false')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.transmittal','false')
INSERT INTO tbits_properties(name,value) Values('transbit.logger.notifyFailureFrom','tbits@transbittech.com ')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.intenet.proxyserver','myproxyservername')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.smtp.password','san1deep')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.attachmentdir','attachments')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.smtp.isdebug','true')
INSERT INTO tbits_properties(name,value) Values('mail.pop3.timeout','600000')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.sms.enabled','false')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.myDomain','hyd')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.severityimgs','critical/+critical;critical.gif;Critical,high/+high;high.gif;High,low/+low;critical.gif;Low,very-low/very low/+very-low/+very low;v-low.gif;Very low"')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.redirectionURL','http://localhost:80')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.indexdir','index')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.responsepattern','^Message Sent..Success:.+')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.tmpdir','tmp')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.includeRecipients','true')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.antiword','bin/antiword.exe')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.ntlmEnabled','true')
INSERT INTO tbits_properties(name,value) Values('mail.smtps.socketFactory.class','transbit.tbits.mail.DummySSLSocketFactory')
INSERT INTO tbits_properties(name,value) Values('transbit.app.name','tBits')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.sendmail','${build.transbit.tbits.sendmail}')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.jvuecodebase','http://localhost/jVue')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.smtp.login','sandeep')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.smtp.server','localhost')
INSERT INTO tbits_properties(name,value) Values('mail.pop3.connectiontimeout','600000')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.adUserSearchQuery','(&(objectCategory=person)(objectClass=user))')
INSERT INTO tbits_properties(name,value) Values('mail.pop3s.socketFactory.class','transbit.tbits.mail.DummySSLSocketFactory')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.nycUrl','http://localhost:80')
INSERT INTO tbits_properties(name,value) Values('mail.pop3s.timeout','600000')
INSERT INTO tbits_properties(name,value) Values('transbit.tbits.auth.sessionMaxAge','31536000')


GO
SET ANSI_PADDING OFF

GO

INSERT INTO log4j_conf(name,value) Values('log4j.appender.INDEX.layout','org.apache.log4j.PatternLayout')
INSERT INTO log4j_conf(name,value) Values('log4j.logger.org.apache','INFO, ORG')
INSERT INTO log4j_conf(name,value) Values('log4j.logger.org.quartz','INFO,SCHED')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.aWeb.layout','org.apache.log4j.PatternLayout')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.aTest.layout.ConversionPattern','[ %d [%t] %C{2}.%M():%L %x %-5p]: %m%n')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.ORG.layout.ConversionPattern','[ %d [%t] %C{2}.%M():%L %x %-5p]: %m%n')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.aEmail','transbit.tbits.common.TBitsFileAppender')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.aTest.layout','org.apache.log4j.PatternLayout')
INSERT INTO log4j_conf(name,value) Values('log4j.maillevel','OFF')
INSERT INTO log4j_conf(name,value) Values('log4j.ConversionPattern','[ %d [%t] %C{2}.%M():%L %x %-5p]: %m%n')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.INDEX.File','${tbits.home}/logs/aIndex.log')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.INDEX.layout.ConversionPattern','[ %d [%t] %C{2}.%M():%L %x %-5p]: %m%n')
INSERT INTO log4j_conf(name,value) Values('log4j.logger.net','INFO, ORG')
INSERT INTO log4j_conf(name,value) Values('log4j.logger.indexer','INFO,INDEX')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.SCHED','transbit.tbits.common.TBitsFileAppender')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.aCmdline.layout','org.apache.log4j.PatternLayout')
INSERT INTO log4j_conf(name,value) Values('log4j.rootLogger','INFO')
INSERT INTO log4j_conf(name,value) Values('log4j.logger.email','INFO,aEmail')
INSERT INTO log4j_conf(name,value) Values('log4j.logger.web','INFO,aWeb')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.aCmdline.File','${tbits.home}/logs/aCmdLine.log')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.SCHED.layout.ConversionPattern','[ %d [%t] %C{2}.%M():%L %x %-5p]: %m%n')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.aEmail.layout','org.apache.log4j.PatternLayout')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.ORG','transbit.tbits.common.TBitsAppender')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.aEmail.File','${tbits.home}/logs/aEmail.log')
INSERT INTO log4j_conf(name,value) Values('log4j.logger.test','INFO,aTest')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.aWeb','transbit.tbits.common.TBitsAppender')
INSERT INTO log4j_conf(name,value) Values('log4j.logger.cmdline','INFO, aCmdline')
INSERT INTO log4j_conf(name,value) Values('log4j.logger.cmdline.transbit.tbits.util','INFO,SCHED')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.SCHED.File','${tbits.home}/logs/aSched.log')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.aTest','transbit.tbits.common.TBitsAppender')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.aEmail.layout.ConversionPattern','[ %d [%t] %C{2}.%M():%L %x %-5p]: %m%n')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.aCmdline','transbit.tbits.common.TBitsFileAppender')
INSERT INTO log4j_conf(name,value) Values('log4j.logger.org.apache.commons','INFO,SCHED')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.aCmdline.layout.ConversionPattern','[ %d [%t] %C{2}.%M():%L %x %-5p]: %m%n')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.SCHED.layout','org.apache.log4j.PatternLayout')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.aWeb.layout.ConversionPattern','[ %d [%t] %C{2}.%M():%L %x %-5p]: %m%n')
INSERT INTO log4j_conf(name,value) Values('log4j.logger.indexer.transbit.tbits.indexer','INFO,aWeb')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.INDEX','transbit.tbits.common.TBitsFileAppender')
INSERT INTO log4j_conf(name,value) Values('log4j.appender.ORG.layout','org.apache.log4j.PatternLayout')
INSERT INTO log4j_conf(name,value) Values('log4j.logger.cmdline.transbit.tbits.scheduler','INFO,SCHED')

GO

INSERT INTO quartz_properties(name,value) Values('org.quartz.scheduler.instanceId','AUTO')
INSERT INTO quartz_properties(name,value) Values('org.quartz.scheduler.instanceName','TBitsQuartzScheduler')
INSERT INTO quartz_properties(name,value) Values('org.quartz.jobStore.tablePrefix','QRTZ_')
INSERT INTO quartz_properties(name,value) Values('org.quartz.jobStore.class','org.quartz.impl.jdbcjobstore.JobStoreTX')
INSERT INTO quartz_properties(name,value) Values('org.quartz.scheduler.rmi.registryPort','2098')
INSERT INTO quartz_properties(name,value) Values('org.quartz.threadPool.class','org.quartz.simpl.SimpleThreadPool')
INSERT INTO quartz_properties(name,value) Values('org.quartz.jobStore.useProperties','true')
INSERT INTO quartz_properties(name,value) Values('org.quartz.scheduler.rmi.registryHost','localhost')
INSERT INTO quartz_properties(name,value) Values('org.quartz.threadPool.threadPriority','5')
INSERT INTO quartz_properties(name,value) Values('org.quartz.jobStore.isClustered','false ')
INSERT INTO quartz_properties(name,value) Values('org.quartz.scheduler.rmi.createRegistry','true')
INSERT INTO quartz_properties(name,value) Values('org.quartz.scheduler.rmi.serverPort','0')
INSERT INTO quartz_properties(name,value) Values('org.quartz.scheduler.rmi.export','true')
INSERT INTO quartz_properties(name,value) Values('org.quartz.threadPool.threadCount','10')
INSERT INTO quartz_properties(name,value) Values('org.quartz.jobStore.driverDelegateClass','org.quartz.impl.jdbcjobstore.MSSQLDelegate')

Go

insert into current_version values ('request', '6.0.61', '2031')
GO