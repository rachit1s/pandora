cd tbits
rm -rf jaguar/jaguarsource/plugins/*
#svn update
#svn revert -R src
#svn revert -R jaguar
#ant build
cd ../gwtplugins
#svn update
./build-all.sh lnt
cd ../tbits
ant -Dsystype=dms release
