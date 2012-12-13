set ANSI_NULLS ON
set QUOTED_IDENTIFIER ON
GO


ALTER PROCEDURE [dbo].[stp_tbits_getRequestInfoInRange]
(
	@systemId	INT,
	@start		INT,
	@end		INT
)
AS

/*
 * Return the request records.
 */
SELECT 
	/*
         * Boolean Fields.
         */
	r.is_private,	
	r.notify,
	r.notify_loggers,

	/*
         * Date Fields.
         */
	r.logged_datetime,
	r.lastupdated_datetime,
	r.due_datetime,

	/*
         * Integer Fields.
         */
    r.sys_id,
	r.request_id,
	r.parent_request_id, 
	r.max_action_id,
	r.append_interface,
	r.replied_to_action,

	/*
         * String Fields.
         */
	ba.sys_prefix 'sysPrefix',
	r.subject,
	r.description,
	r.summary,
	
	/*
	 * Type fields. Take the name and display name.
	 * User can search on any of them.
	 */
	cat.name  + ' ' + cat.display_name 'category_id',
	stat.name + ' ' + stat.display_name 'status_id',
	sev.name  + ' ' + sev.display_name 'severity_id',
	req.name  + ' ' + req.display_name 'request_type_id',
	office.name + ' ' + office.display_name 'office_id',
	
	/*
         * User Fields.
         */
	usr.user_login 'user_id'
FROM 
	requests r
	LEFT JOIN business_areas ba
	ON ba.sys_id = r.sys_id
	LEFT JOIN types cat
	ON cat.sys_id = r.sys_id AND cat.field_id = 3 and cat.type_id = r.category_id
	LEFT JOIN types stat
	ON stat.sys_id = r.sys_id AND stat.field_id = 4 and stat.type_id = r.status_id
	LEFT JOIN types sev
	ON sev.sys_id = r.sys_id AND sev.field_id = 5 and sev.type_id = r.severity_id
	LEFT JOIN types req
	ON req.sys_id = r.sys_id AND req.field_id = 6 and req.type_id = r.request_type_id
	LEFT JOIN types office
	ON office.sys_id = r.sys_id AND office.field_id = 30 and office.type_id = r.office_id
	LEFT JOIN users usr
	ON usr.user_id = r.user_id 
WHERE 
	r.sys_id = @systemId AND
	r.request_id >= @start AND
	r.request_id < @end
ORDER BY r.request_id

/*
 * Send out the request user details.
 */
SELECT
	r.request_id,
	r.user_type_id,
	usr.user_login,
	r.is_primary
FROM
	request_users r
	JOIN users usr
	ON usr.user_id = r.user_id 
WHERE 
	r.sys_id = @systemId AND
	r.request_id >= @start AND
	r.request_id < @end
ORDER BY r.request_id, r.user_type_id, user_login


/*
 * Send the requests ex details.
 */
SELECT
	r.request_id,
	f.name 'field_name',
	f.data_type_id,
	r.bit_value,
	r.datetime_value,
	r.int_value,
	r.real_value,
	r.varchar_value,
	r.text_value,
	ISNULL(t.name, '') + ' ' + ISNULL(t.display_name, '') 'type_value'
FROM
	requests_ex r
	JOIN fields f
	ON r.sys_id = f.sys_id AND r.field_id = f.field_id
	LEFT JOIN types t
	ON r.sys_id = t.sys_id AND r.field_id = t.field_id AND r.type_value = t.type_id
WHERE
	r.sys_id = @systemId AND
	r.request_id >= @start AND
	r.request_id < @end
ORDER BY r.request_id

/*
 * Return the action records
 */
SELECT
	/*
         * Boolean Fields.
         */
	a.is_private,
	a.notify,
	a.notify_loggers,

	/*
         * Date Fields.
         */
	a.logged_datetime,
	a.lastupdated_datetime,
	a.due_datetime,

	/*
         * Integer Fields.
         */  
	a.sys_id,
	a.request_id,
	a.action_id,
	a.parent_request_id, 
	a.append_interface,
	a.replied_to_action,

	/*
         * String Fields.
         */
	aba.sys_prefix 'sysPrefix',
	a.subject,
	a.description,
	a.summary,
	
	/*
	 * Type fields. Take the name and display name.
	 * User can search on any of them.
	 */
	acat.name  + ' ' + acat.display_name 'category_id',
	astat.name + ' ' + astat.display_name 'status_id',
	asev.name  + ' ' + asev.display_name 'severity_id',
	areq.name  + ' ' + areq.display_name 'request_type_id',
	aoffice.name + ' ' + aoffice.display_name 'office_id',
	
	/*
         * User Fields.
         */
	ausr.user_login 'user_id'
FROM 
	actions a
	LEFT JOIN business_areas aba
	ON aba.sys_id = a.sys_id
	LEFT JOIN types acat
	ON acat.sys_id = a.sys_id AND acat.field_id = 3 and acat.type_id = a.category_id
	LEFT JOIN types astat
	ON astat.sys_id = a.sys_id AND astat.field_id = 4 and astat.type_id = a.status_id
	LEFT JOIN types asev
	ON asev.sys_id = a.sys_id AND asev.field_id = 5 and asev.type_id = a.severity_id
	LEFT JOIN types areq
	ON areq.sys_id = a.sys_id AND areq.field_id = 6 and areq.type_id = a.request_type_id
	LEFT JOIN types aoffice
	ON aoffice.sys_id = a.sys_id AND aoffice.field_id = 30 and aoffice.type_id = a.office_id
	LEFT JOIN users ausr
	ON ausr.user_id = a.user_id 
WHERE 
	a.sys_id = @systemId AND
	a.request_id >= @start AND
	a.request_id < @end
ORDER BY a.sys_id, a.request_id, a.action_id 

/*
 * Send out the action user details.
 */
SELECT
	a.sys_id,
	a.request_id,
	a.action_id,
	a.user_type_id,
	ausr.user_login,
	a.is_primary
FROM
	action_users a
	JOIN users ausr
	ON ausr.user_id = a.user_id 
WHERE 
	a.sys_id = @systemId AND
	a.request_id >= @start AND
	a.request_id < @end
ORDER BY a.sys_id, a.request_id, a.action_id, a.user_type_id, user_login


/*
 * Send the actions ex details.
 */
SELECT
	ax.sys_id,
	ax.request_id,
	ax.action_id,
	af.name 'field_name',
	af.data_type_id,
	ax.bit_value,
	ax.datetime_value,
	ax.int_value,
	ax.real_value,
	ax.varchar_value,
	ax.text_value,
	ISNULL(at.name, '') + ' ' + ISNULL(at.display_name, '') 'type_value'
FROM
	actions_ex ax
	JOIN fields af
	ON ax.sys_id = af.sys_id AND ax.field_id = af.field_id
	LEFT JOIN types at
	ON ax.sys_id = at.sys_id AND ax.field_id = at.field_id AND ax.type_value = at.type_id
WHERE
	ax.sys_id = @systemId AND
	ax.request_id >= @start AND
	ax.request_id < @end
ORDER BY ax.sys_id, ax.request_id, ax.action_id 



