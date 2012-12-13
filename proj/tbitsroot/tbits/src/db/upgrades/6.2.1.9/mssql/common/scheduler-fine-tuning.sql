delete from quartz_properties  where name = 'org.quartz.scheduler.dbFailureRetryInterval'
insert into quartz_properties (name, value) values ('org.quartz.scheduler.dbFailureRetryInterval', '1000')

delete from quartz_properties  where name = 'org.quartz.scheduler.skipUpdateCheck'
insert into quartz_properties (name, value) values ('org.quartz.scheduler.skipUpdateCheck', 'true')

delete from quartz_properties  where name = 'org.quartz.dataSource.tbits.validationQuery'
insert into quartz_properties (name, value) values ('org.quartz.dataSource.tbits.validationQuery', 'select getdate()')
