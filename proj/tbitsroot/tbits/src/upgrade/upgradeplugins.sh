#!/bin/bash
export USER=transbit
export RSYNC_PASSWORD=tBitsrsync4upgrades

export src=upgrades.mytbits.com::upgrades/plugins
export dest=../build/plugins
echo "Arg Count: $#"
if [ $# != 1 ]
then
	echo "Usage: upgradeplugins.bat <plugin-folder>"
	echo "The available folders are: "
	rsync $src/
	exit 1;
fi

echo "This will overwrite the plugins.";
rsync -avz --progress --modify-window=1 --delete $src/$1/ $dest/
echo "Finished fetching. Changing permissions..."
chmod -R 777 $dest

