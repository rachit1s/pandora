#cp build.properties.generic build.properties;
#ant build;
if [ $# != 1 ]
then
        echo "Usage: $0 <build-properties-suffix>"
        echo "Example: $0 lnt"
        exit 1;
fi

cp build.properties.common build.properties;
ant build

cp build.properties.$1 build.properties;
ant build

#cd ~/Work/releasetrunk/jaguar
#ant build

#cd ~/Work/releasetrunk/dist/jaguarsource
#ant javac

#cd ~/Work/gwtplugins
