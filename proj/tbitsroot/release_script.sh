CLIENTNAME=$1
TBITSROOT=/home/nitiraj/work/tbitsroot
TBITS=$TBITSROOT/tbits
GWTPLUGINS=$TBITSROOT/gwtplugins
TBITSRULES=$TBITSROOT/tBitsRules
DIST=$TBITS/dist
JAGUAR=$TBITS/jaguar
JAGUARPLUGINS=$JAGUAR/jaguarsource/plugins
PLUGINS=$DIST/build/plugins
RELEASEDIR=$TBITSROOT/tbits-releases

cd $TBITSROOT
svn update

rm -rf  $JAGUARPLUGINS
mkdir -p $JAGUARPLUGINS
rm -rf $TBITS/src/plugins
mkdir $TBITS/src/plugins

cd $GWTPLUGINS
cp build.properties.common build.properties
ant

if [ -f build.properties.$CLIENTNAME ];
then
cp build.properties.$CLIENTNAME build.properties
ant
fi

# some extra work to find the revision number
cd $TBITS
REVNUMBER=`svn info | grep "Last Changed Rev" | awk '{print $4}'`
# found the revision number
rm -rf $RELEASEDIR/$REVNUMBER
mkdir -p $RELEASEDIR/$REVNUMBER

cd $TBITS
ant release -Dsystype=dms


cd $TBITSRULES
if [ -f build.properties.$CLIENTNAME ];
then
cp build.properties.$CLIENTNAME build.properties
ant
fi

cd $PLUGINS
zip -r $CLIENTNAME"Plugins.zip" *

mkdir -p $RELEASEDIR/$REVNUMBER/plugins
cp  $CLIENTNAME"Plugins.zip"  $RELEASEDIR/$REVNUMBER/plugins/
rm $CLIENTNAME"Plugins.zip"

cd $JAGUARPLUGINS
zip -r $CLIENTNAME"GWTPlugins.zip" *

mkdir -p $RELEASEDIR/$REVNUMBER/gwtPlugins
cp  $CLIENTNAME"GWTPlugins.zip"  $RELEASEDIR/$REVNUMBER/gwtPlugins/
rm $CLIENTNAME"GWTPlugins.zip" 

cd $TBITS
chmod +x *.sh

./releaseplugins2uc.sh $CLIENTNAME /
./releasepluginsbeta2uc.sh $CLIENTNAME /
./releasegwtpluginsbeta2uc.sh $CLIENTNAME /
./releasegwtplugins2uc.sh $CLIENTNAME /

./releasetbits2uc.sh
./releasetbitsbeta2uc.sh

cd $TBITSROOT
svn log > $RELEASEDIR/$REVNUMBER/svnLogs.txt
svn log -v > $RELEASEDIR/$REVNUMBER/completeSVNLogs.txt

cd $RELEASEDIR/$REVNUMBER
md5sum `find -type f` > md5checksums

cd $RELEASEDIR
export USER=transbit
export RSYNC_PASSWORD=tBitsrsync4upgrades
touch rsyncReleaseLogs.txt
rsync --log-file=rsyncReleaseLogs.txt $REVNUMBER upgrades.mytbits.com::releases/ -avz --progress

echo "You can download it through ftp://upgrades.mytbits.com/releases/$REVNUMBER" > emailDescription.txt
echo "" >> emailDescription.txt
echo "" >> emailDescription.txt
echo "MD5 Checksums of the files are below" >> emailDescription.txt
echo "" >> emailDescription.txt 

cat $RELEASEDIR/$REVNUMBER/md5checksums >> emailDescription.txt

tmail -u "New TBits-Jaguar Release[$REVNUMBER] shipped." -t team@tbitsglobal.com -o message-file=emailDescription.txt

echo "Release Completed."

