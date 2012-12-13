call set-env-pull-from-staging.bat
set dest=..

set opts=-avz --progress --modify-window=1 --delete --no-p --no-g --chmod=ugo=rwX

echo "SRC: %src%"
echo "DEST: %dest%"


set dir=build/bin/
rsync.exe %opts% --exclude-from=binblacklist %src%/%dir% %dest%/%dir%

set dir=build/birt-runtime
rsync.exe %opts% %src%/%dir%/ %dest%/%dir%/

set dir=build/db
rsync.exe %opts% %src%/%dir%/ %dest%/%dir%/

set dir=build/webapps
echo rsync.exe %opts% %src%/%dir%/ %dest%/%dir%/
rsync.exe %opts% %src%/%dir%/ %dest%/%dir%/

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
if errorlevel 1 (
   echo ""
   echo  Upgrade FAILED !!! Reason Given is %errorlevel%
   GOTO End
)


set dir=upgrade
rsync %opts% --exclude=set-env-pull-from-staging.* %src%/%dir%/ %dest%/%dir%/new/
if errorlevel 1 (
   echo ""
   echo Self Upgrade FAILED !!! Reason Given is %errorlevel%
   GOTO End
)

echo "Finished updating the code base. Going to correct file permissions..." 
chmod -R 777 ..
cd ..\build\bin
set /p ans="Finished file permission correction. Press enter to start the DB upgrade..."
call dbupgrade.bat
cd ..\..\upgrade

echo "Upgrading the Classic Plugins."
rsync %opts% %src%/build/plugins/  %dest%/build/plugins/
if errorlevel 1 (
   echo ""
   echo Classic Plugin Upgrade FAILED !!! Reason Given is %errorlevel%
   GOTO End
)

echo "Upgrading the GWT Plugins."
rsync %opts% %src%/jaguarsource/plugins/  %dest%/jaguarsource/plugins/
if errorlevel 1 (
   echo ""
   echo GWT Plugin Upgrade FAILED !!! Reason Given is %errorlevel%
   GOTO End
)

echo "Upgrading the reports."
rsync %opts% %src%/build/tbitsreports/  %dest%/build/tbitsreports/
if errorlevel 1 (
   echo ""
   echo Reports upgrade FAILED !!! Reason Given is %errorlevel%
   GOTO End
)

echo "Correcting the file permissions of plugins."
chmod -R 755 %dest%/build/plugins/ %dest%/jaguarsource/plugins

echo "Upgrading the upgrader..."
cd ..\upgrade
copy %dest%\%dir%\new\* %dest%\%dir%\

:End
pause

