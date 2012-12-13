IF NOT EXISTS (select * from sys.objects where name = 'uq_corr_onbehalf_entry')
BEGIN
alter table corr_onbehalf_map
add constraint uq_corr_onbehalf_entry unique ( sys_prefix, user_login , onbehalf_type1, onbehalf_type2, onbehalf_type3,onbehalf_of_login)
END
