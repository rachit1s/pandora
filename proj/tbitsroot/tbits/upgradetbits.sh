#!/bin/bash
if [ $# != 2 ]
then
	echo "Usage: $0 <src> <destination_INSTALL_PATH>"
	echo "example upgradetbits.sh dist demo.mytbits.com::C/Program\ Files/tBits"
	exit 1;
fi

PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
export scriptfolder=`dirname $PRG`
export src=$1
export dest=$2



export opts=" -avz --progress --delete --no-p --no-g --chmod=ugo=rwX "
#export opts=" -avz --progress --delete "

echo "SRC: $src"
echo "DEST: $dest"
echo "Script folder: $scriptfolder"


export dir=build/bin/
#mkdir -p $dest/$dir
echo rsync $opts --exclude-from=$scriptfolder/binblacklist -R $src/./$dir "$dest"/
rsync $opts --exclude-from=$scriptfolder/binblacklist -R $src/./$dir "$dest"/

for dir in build/birt-runtime build/db build/webapps jaguarsource
do
	#mkdir -pv $dest/$dir
	echo rsync $opts -R $src/./$dir/ "$dest"/
	rsync $opts -R $src/./$dir/ "$dest"/
done

export dir=build/etc/upgradeclasses.properties
#mkdir -p $dest/$dir
echo rsync $opts -R $src/./$dir "$dest"/
rsync $opts -R $src/./$dir "$dest"/

export dir=tomcat/
#mkdir -p $dest/$dir
echo rsync $opts --exclude-from=$scriptfolder/tomcatblacklist $src/$dir "$dest"/$dir
rsync $opts --exclude-from=$scriptfolder/tomcatblacklist $src/$dir "$dest"/$dir

export dir=upgrade/
rsync $opts $src/$dir "$dest"/$dir

export dir=version.properties
rsync $opts $src/$dir "$dest"/$dir

export dir=svn-revision.txt
echo "rsync $opts $src/$dir $dest/"
rsync $opts $src/$dir "$dest"/
