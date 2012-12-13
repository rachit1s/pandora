export USER=transbit
export RSYNC_PASSWORD=tBitsrsync4upgrades
export RECIPIENTS=team@tbitsglobal.com

rsync upgrades.mytbits.com::upgrades/tbits/beta/svn-revision.txt svn-revision-prev.txt
export PREV_REVISION=`cat svn-revision-prev.txt|awk -F'=' '{print $2}'`

((ant dist && ./upgradetbits.sh dist upgrades.mytbits.com::upgrades/tbits/beta) 2>&1) > ship-log.tmp

if [ $? = 0 ]
then 
export subject_status="The dist prepared and shipped to beta: `cat dist/svn-revision.txt`"
cat ship-log.tmp|mail $RECIPIENTS -s "$subject_status"
export NEW_REVISION=`svn info|grep '^Revision:'|awk '{print $2}'`
svn log http://symphron/svn/MyNewRepository/trunk/ -r 4906:HEAD|email -s "The changes between now shipped($NEW_REVISION) and previous shipped($PREV_REVISION)"  $RECIPIENTS

else
export subject_status="The dist failed"
cat ship-log.tmp|mail $RECIPIENTS -s "$subject_status"
fi
