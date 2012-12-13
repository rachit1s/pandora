@rem upgradeplugins.bat [customer_folder] [plugin_to_upgrade]
@echo off
set USER=transbit
set RSYNC_PASSWORD=tBitsrsync4upgrades
set server=upgrades.mytbits.com::upgrades/plugins/
set pluginFolder=../build/plugins/
set cmd=rsync.exe -avz --progress --modify-window=1 --delete

IF "%1"=="" GOTO Error
IF "%2"=="" GOTO Error1

set source=%server%%1/%2
set dest=%pluginFolder%

:Start
echo Caution: This script will upgrade the new version of plugins from the upgrade center by using following command.
echo %cmd% %source%  %dest%
set /p ans="Are you sure you want to continue? [yes/no]"
IF NOT "%ans%"=="yes" GOTO End
%cmd% %source%  %dest%
if errorlevel 1 (
   echo ""
   echo Upgrade FAILED !!! Reason Given is %errorlevel%
   GOTO End
)
echo Changing permissions. Please wait.....
chmod.exe -R 777 %dest%
echo The Upgrade process completed SUCCESSFULLY.
GOTO End

:Error
echo "Usage: upgradeplugins.bat <server-plugin-folder> <your-plugin-folder>"
echo Example upgradeplugins.bat ksk kskCorres
echo The various folder available are: 
rsync.exe %server%
cmd
GOTO End

:Error1
echo "Usage: upgradeplugins.bat <server-plugin-folder> <your-plugin-folder>"
echo NOTE: if your-plugin-folder is *, it will upgrade all your plugins without deleting other plugins. 
echo If it is "/" (without quotes), it would upgrade all your plugins and also delete the other plugins which the server do not have.
echo Example upgradeplugins.bat ksk kskCorres
echo The various plugins available for %1 are: 
rsync.exe %server%"%1"/

:End

