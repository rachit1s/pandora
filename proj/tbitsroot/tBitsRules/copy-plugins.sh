if [ $# != 1 ]
then
        echo "Usage: $0 <customer-suffix>"
        echo "Example: $0 lnt"
        exit 1;
fi

cp build.properties.$1 build.properties
ant build

