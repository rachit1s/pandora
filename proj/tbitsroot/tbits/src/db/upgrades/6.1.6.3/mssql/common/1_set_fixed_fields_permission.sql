update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'sys_id'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'request_id'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'category_id'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'status_id'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'severity_id'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'request_type_id'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'logger_ids'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'assignee_ids'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'subscriber_ids'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'to_ids'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'cc_ids'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'subject'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(0)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(0)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'description'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'is_private'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'parent_request_id'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'user_id'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'max_action_id'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'due_datetime'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'logged_datetime'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(0)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(0)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'lastupdated_datetime'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'header_description'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'attachments'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'summary'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'memo'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(0)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(0)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'append_interface'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'notify'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'notify_loggers'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'replied_to_action'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'related_requests'

update fields
set permission=( case (fields.permission&64)
	when 0 then ( case(1)
		when 0 then fields.permission
		else fields.permission + 64
		end)
	else (case(1)
		when 0 then fields.permission - 64
		else fields.permission
		end)
	end)
where fields.is_extended=0 and fields.name = 'office_id'

