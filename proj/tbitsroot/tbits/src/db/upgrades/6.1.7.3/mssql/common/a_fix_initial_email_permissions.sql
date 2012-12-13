-- this script will give email permission for the fields which currently have view permission
update roles_permissions
set gpermissions = gpermissions + 8
where
 (gpermissions & 8 = 0 ) -- does not have email permission ( to avoid the case where this script is run more than one time)
	and 
 (gpermissions & 4 != 0) -- give email permission if it has view permission
