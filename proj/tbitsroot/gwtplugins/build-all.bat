IF "%1"=="" GOTO ERROR 

copy build.properties.common build.properties;
ant build

copy build.properties.%1 build.properties;
ant build


:ERROR 

        echo "Usage: %0 <build-properties-suffix>"
        echo "Example: %0 lnt"
PAUSE
