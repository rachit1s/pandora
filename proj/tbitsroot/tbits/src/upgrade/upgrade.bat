@ECHO OFF
set USER=transbit
set RSYNC_PASSWORD=tBitsrsync4upgrades

set src=upgrades.mytbits.com::upgrades/tbits/current
set dest=..

set opts=-avz --progress --modify-window=1 --delete --chmod=ugo=rwX

echo "SRC: %src%"
echo "DEST: %dest%"


set dir=build/bin/
rsync.exe %opts% --exclude-from=binblacklist %src%/%dir% %dest%/%dir%

set dir=build/birt-runtime
rsync.exe %opts% %src%/%dir%/ %dest%/%dir%/

set dir=build/db
rsync.exe %opts% %src%/%dir%/ %dest%/%dir%/

set dir=build/webapps
echo rsync.exe %opts% --exclude-from=webappblacklist  %src%/%dir%/ %dest%/%dir%/
rsync.exe %opts% --exclude-from=webappblacklist %src%/%dir%/ %dest%/%dir%/

set dir=build/etc/upgradeclasses.properties
echo rsync.exe %opts% %src%/%dir% %dest%/%dir%
rsync.exe %opts% %src%/%dir% %dest%/%dir%

set dir=tomcat/
echo rsync.exe %opts% --exclude-from=tomcatblacklist %src%/%dir% %dest%/%dir%
rsync.exe %opts% --exclude-from=tomcatblacklist %src%/%dir% %dest%/%dir%

set dir=jaguarsource
echo rsync.exe --exclude=plugins %opts% %src%/%dir% %dest%/
rsync.exe %opts% %src%/%dir% %dest%/

set dir=version.properties
rsync %opts% %src%/%dir% %dest%/%dir%

set dir=svn-revision.txt
rsync %opts% %src%/%dir% %dest%/%dir%

set dir=upgrade
rsync %opts% %src%/%dir%/ %dest%/%dir%/new/

set /p ans="Finished updating the code base. Press enter to correct file permissions..." 
chmod -R 777 ..
cd ..\build\bin
set /p ans="Finished file permission correction. Press enter to start the DB upgrade..."
call dbupgrade.bat

echo "Upgrading the upgrader..."
cd ..\..\upgrade
copy %dest%\%dir%\new\* %dest%\%dir%\
