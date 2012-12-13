declare @maxid integer
select @maxid=max(format_id) from datetime_formats
insert into datetime_formats values (@maxid+1, 'dd-MM-yyyy')
insert into datetime_formats values (@maxid+2, 'dd/MM/yyyy')
