IF "%1"=="" GOTO ERROR        

cd gwtplugins
./build-all.bat %1

cd ../

cd tBitsRules
./copy-plugins.bat %1 || echo "Build has failed.Try to excute the following: cd tbits;ant dist;cd ..;%0 %1"


:ERROR
echo "Usage: %0 <customer-suffix>"
        echo "Example: $0 lnt"
PAUSE
