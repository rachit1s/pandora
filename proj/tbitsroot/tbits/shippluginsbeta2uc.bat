@rem shippluginsbeta2uc.bat [customer_folder] [plugin_to_ship]
@echo off
set USER=transbit
set RSYNC_PASSWORD=tBitsrsync4upgrades
set server=upgrades.mytbits.com::upgrades/betaplugins/
@rem set server=snowwhite::plugins/
set source=dist/build/plugins/
set cmd=src\upgrade\rsync.exe -avz --modify-window=1 --progress --delete

IF "%1"=="" GOTO Error
IF "%2"=="" GOTO Error1

set dest=%server%%1/
set source=%source%%2

:Start
echo Caution: This script will ship the new version to the upgrade center by using following command.
echo %cmd% %source%  %dest%
set /p ans="Are you sure you want to continue? [yes/no]"
IF NOT "%ans%"=="yes" GOTO End
%cmd% %source%  %dest%
GOTO End

:Error
echo "Usage: shippluginsbeta2uc.bat <server-plugin-folder> <your-plugin-folder>"
echo Example shippluginsbeta2uc.bat ksk kskCorres
echo The various folder available are: 
src\upgrade\rsync.exe %server%
GOTO End

:Error1
echo "Usage: shippluginsbeta2uc.bat <server-plugin-folder> <your-plugin-folder>"
echo NOTE: if your-plugin-folder is *, it will ship all your plugins without deleting other plugins. 
echo If it is "/" (without quotes), it would ship all your plugins and also delete the other plugins which you do not have.
echo Example shippluginsbeta2uc.bat ksk kskCorres
echo The various rules available for %1 are: 
src\upgrade\rsync.exe %server%"%1"/

:End

