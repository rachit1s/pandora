# shipplugins2uc.sh [customer_folder] [plugin_to_ship]
export USER=transbit
export RSYNC_PASSWORD=tBitsrsync4upgrades
server=upgrades.mytbits.com::upgrades/plugins/
source=dist/build/plugins/
cmd="rsync -avz --modify-window=1 --progress --delete"

if [ $# -lt 1 ] 
then
echo "Usage: shipplugins2uc.sh <server-plugin-folder> <your-plugin-folder>"
echo Example shipplugins2uc.sh ksk kskCorres
echo The various folder available are: 
rsync $server
exit 1
fi

if [ $# -lt 2 ] 
then
echo "Usage: shipplugins2uc.sh <server-plugin-folder> <your-plugin-folder>"
echo 'NOTE: if your-plugin-folder is "*" ( NOTE : the quotes "" around * are must otherwise the shell will interpret the quotes), it will ship all your plugins without deleting other plugins.'
echo 'If it is "/" (without quotes), it would ship all your plugins and also delete the other plugins which you do not have.'
echo Example shipplugins2uc.sh ksk kskCorres
echo The various rules available for $1 are: 
rsync $server"$1"/
exit 1
fi

dest="$server""$1""/"
source="$source""$2"

echo Caution: This script will ship the new version to the upgrade center by using following command.
echo $cmd $source $dest
echo -n "Are you sure you want to continue? [yes/no]:"
read ans;
if [ $ans == "yes" ]
then
	$cmd $source  $dest
else
	echo "Aborting.."
fi
