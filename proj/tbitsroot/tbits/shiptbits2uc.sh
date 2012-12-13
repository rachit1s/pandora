#!/bin/bash
export USER=transbit
#export RSYNC_PASSWORD=tBitsrsync4upgrades
echo "Caution: This script will ship the new version to the upgrade center."
echo "Are you sure you want to continue [yes/no]"
read ans;
if [ $ans = "yes" ]
then 
	echo "Enter the password for rsync user transbit: "
	read password
	export RSYNC_PASSWORD=$password
	touch version.properties
	upgradetbits.sh dist upgrades.mytbits.com::upgrades/tbits/current
fi
