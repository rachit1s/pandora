UPDATE log4j_conf SET value = N'INFO,aWeb,aWebFile' WHERE name = N'log4j.logger.web' AND value like N'INFO,aWeb';
Insert into log4j_conf (name, value ) VALUES (N'log4j.appender.aWebFile.layout.ConversionPattern', N'[ %d [%t] %C{2}.%M():%L %x %-5p]: %m%n')
Insert into log4j_conf (name, value ) VALUES (N'log4j.appender.aWebFile.layout' , N'org.apache.log4j.PatternLayout')
Insert into log4j_conf (name, value ) VALUES (N'log4j.appender.aWebFile'  , N'transbit.tbits.common.TBitsFileAppender')
INSERT INTO log4j_conf (name, value) VALUES (N'log4j.appender.aWebFile.File', N'${tbits.home}/logs/tbits.log')
