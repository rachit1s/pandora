
-----------------------------
delete a  from actions a
left join requests  r on a.sys_id = r.sys_id and a.request_id = r.request_id
where r.request_id is null
----------------------
delete a  from action_users a
left join requests  r on a.sys_id = r.sys_id and a.request_id = r.request_id
where r.request_id is null
--------------------------

delete re  from requests_ex re
left join requests  r on re.sys_id = r.sys_id and re.request_id = r.request_id
where r.request_id is null

--------------------------------

delete re  from actions_ex re
left join requests  r on re.sys_id = r.sys_id and re.request_id = r.request_id
where r.request_id is null
------------------------------------
delete a  from request_users a
left join requests  r on a.sys_id = r.sys_id and a.request_id = r.request_id
where r.request_id is null

------------------------------
delete ae  from actions_ex ae
left join actions  a on a.sys_id = ae.sys_id and a.request_id = ae.request_id and a.action_id = ae.action_id
where a.request_id is null
--------------------------------
delete ae  from actions_ex ae
left join requests_ex re on ae.sys_id = re.sys_id and ae.request_id = re.request_id 
where re.request_id is null

------related requests
delete rr from related_requests rr
left join requests r on r.sys_id = rr.primary_sys_id and r.request_id = rr.primary_request_id
where r.sys_id is null
---------------
delete rr from related_requests rr
left join requests r on r.sys_id = rr.related_sys_id and r.request_id = rr.related_request_id
where r.sys_id is null