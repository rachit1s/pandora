update trn_processes set is_default = 1 from trn_processes a

inner join trn_dropdown b on a.src_sys_id=b.src_sys_id and a.trn_process_id = b.id

where b.sort_order = 1
