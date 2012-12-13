#TODO: checkout, prompt for changing the version
version=`cat version.properties|awk -F= '{print $2}'|sed 's/ //'`
ant svnupdate || exit 1
svn list http://symphron:83/svn/MyNewRepository/tags/$version && (echo "the tag already exists" && exit 1)
ant svntag || exit 1
rm -rf dist installer
ant -Dsystype=request release 
ant -Dsystype=kms release
rm -rf dist installer
ant build
