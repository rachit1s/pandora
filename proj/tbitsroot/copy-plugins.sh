if [ $# != 1 ]
then
        echo "Usage: $0 <customer-suffix>"
        echo "Example: $0 lnt"
        exit 1;
fi

cd gwtplugins
./build-all.sh $1

cd ../

cd tBitsRules
./copy-plugins.sh $1 || echo "Build has failed.Try to excute the following: cd tbits;ant dist;cd ..;$0 $1"
