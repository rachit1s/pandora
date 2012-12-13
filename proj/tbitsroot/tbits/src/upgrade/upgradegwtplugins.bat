set USER=transbit
set RSYNC_PASSWORD=tBitsrsync4upgrades

set src=upgrades.mytbits.com::upgrades/betagwtplugins
rem set src=127.0.0.1::upgrades/gwtplugins
set dest=../jaguarsource/plugins
@ECHO OFF
:Loop
IF "%1"=="" GOTO Error
echo "This will overwrite the plugins.";
rsync.exe -avz --progress --modify-window=1 --delete %src%/%1/ %dest%/
echo "Finished fetching. Changing permissions..."
chmod.exe -R 777 %dest%
echo "Finished changing permissions. Building jagaur..."
cd ..\jaguarsource
bin\ant.bat
GOTO Exit

:Error
echo "Usage: upgradetobetagwtplugins.bat <plugin-folder>"
echo "The available folders are: "
rsync.exe %src%/
cmd
:Exit
pause
