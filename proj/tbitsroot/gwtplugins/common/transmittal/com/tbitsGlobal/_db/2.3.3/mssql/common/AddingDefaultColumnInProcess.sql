alter table trn_processes add is_default bit
GO

update trn_processes set is_default = 0
