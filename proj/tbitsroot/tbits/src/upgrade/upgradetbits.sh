if [ $# != 1 ]
then
	echo "Usage: upgradetbits.sh <src location>"
	echo "Example: upgradetbits.sh upgrades.mytbits.com::upgrades/tbits/current"
	exit 1;
fi

export USER=${USER-transbit}
export RSYNC_PASSWORD=${RSYNC_PASSWORD-tBitsrsync4upgrades}

export src=$1
export dest=..

export opts="-avz --progress --modify-window=1 --delete --chmod=ugo=rwX"

echo "SRC: $src"
echo "DEST: $dest"


export dir=build/bin/
rsync $opts --exclude-from=binblacklist $src/$dir $dest/$dir

export dir=build/birt-runtime
rsync $opts $src/$dir/ $dest/$dir/

export dir=build/db
rsync $opts $src/$dir/ $dest/$dir/

export dir=build/webapps
echo rsync $opts --exclude-from=webappblacklist  $src/$dir/ $dest/$dir/
rsync $opts --exclude-from=webappblacklist   $src/$dir/ $dest/$dir/

export dir=build/etc/upgradeclasses.properties
echo rsync $opts $src/$dir $dest/$dir
rsync $opts $src/$dir $dest/$dir

export dir=tomcat/
echo rsync $opts --exclude-from=tomcatblacklist $src/$dir $dest/$dir
rsync $opts --exclude-from=tomcatblacklist $src/$dir $dest/$dir

export dir=jaguarsource
echo rsync $opts --exclude=plugins $src/$dir $dest/
rsync $opts $src/$dir $dest/

export dir=version.properties
rsync $opts $src/$dir $dest/$dir

export dir=svn-revision.txt
rsync $opts $src/$dir $dest/$dir

export dir=upgrade
rsync $opts --exclude=set-env-pull-from-staging.* $src/$dir/ $dest/$dir/new/
cp $dest/$dir/new/* $dest/$dir/


echo "Finished updating the code based. Correcting file permissions..."
chmod -R 777 ..
cd ../build/bin
echo "Finished file permission correction. Starting the DB upgrade..."
./dbupgrade.sh

