IF  EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[fk_Sys_id_Request_id]') AND parent_object_id = OBJECT_ID(N'[dbo].[trn_approval_cycle_transient_data]'))
ALTER TABLE [dbo].[trn_approval_cycle_transient_data] DROP CONSTRAINT [fk_Sys_id_Request_id]

delete   a     
from 
trn_approval_cycle_transient_data a left join   requests b 
	on a.sys_id=b.sys_id and a.request_id = b.request_id 
where  b.sys_id is null and b.request_id is null


ALTER TABLE trn_approval_cycle_transient_data
ADD CONSTRAINT fk_Sys_id_Request_id
FOREIGN KEY (sys_id,request_id)
REFERENCES requests(sys_id,request_id)
ON DELETE CASCADE;

