delete from tbits_properties
where name='transbit.tbits.maximumEmailAttachmentSizeInBytes'

insert into tbits_properties
values ('transbit.tbits.maximumEmailAttachmentSizeInBytes', '10485760', 'Max.Email.Attachment.Size.In.Bytes','The upper limit on the size of all the files that can be attached to a mail',NULL,NULL ) -- 10 MB default size.. 

