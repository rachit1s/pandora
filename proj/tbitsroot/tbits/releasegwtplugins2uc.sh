# shippluginsbeta2uc.sh [customer_folder] [plugin_to_ship]
export USER=transbit
export RSYNC_PASSWORD=tBitsrsync4upgrades
#export server=upgrades.mytbits.com::upgrades/betagwtplugins/
server=upgrades.mytbits.com::upgrades/gwtplugins/
#source=dist/jaguarsource/plugins/
source=dist/jaguarsource/plugins/
cmd="rsync -avz --progress --modify-window=1 --delete"

if [ $# -lt 1 ] 
then
echo "Usage: $0 <server-plugin-folder> <your-plugin-folder>"
echo Example $0 ksk kskCorres
echo The various folder available are: 
rsync $server
exit 1
fi

if [ $# -lt 2 ] 
then
echo "Usage: $0 <server-plugin-folder> <your-plugin-folder>"
echo 'NOTE: if your-plugin-folder is "*" ( NOTE : the quotes "" around * are must otherwise the shell will interpret the quotes), it will ship all your plugins without deleting other plugins.'
echo 'If it is "/" (without quotes), it would ship all your plugins and also delete the other plugins which you do not have.'
echo Example $0 ksk kskCorres
echo The various rules available for $1 are: 
rsync $server"$1"/
exit 1
fi

dest="$server""$1""/"
source="$source""$2"

$cmd $source  $dest
