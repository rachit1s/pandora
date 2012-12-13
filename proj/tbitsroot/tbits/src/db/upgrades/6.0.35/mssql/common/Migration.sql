-- This query sets the due date based on escalation conditions of unclosed request. So, that the process can work smoothly as it was happening.
select 
r.sys_id,r.request_id,dateadd(mi, ec.span, isnull(rh.last_escalated_time, r.logged_datetime)) newdueby
into #tmp
from requests r 
LEFT OUTER JOIN escalation_history rh on r.sys_id = rh.sys_id and r.request_id = rh.request_id 
JOIN escalation_conditions ec on r.sys_id = ec.sys_id and r.severity_id = ec.severity_id
JOIN business_areas bas on r.sys_id = bas.sys_id
where  r.status_id <> 3
order by r.sys_id


update requests
set due_datetime = (select newdueby from #tmp t where requests.sys_id = t.sys_id and requests.request_id = t.request_id)
where EXISTS (select 1 from #tmp t where requests.request_id = t.request_id and requests.sys_id = t.sys_id)
drop table #tmp
